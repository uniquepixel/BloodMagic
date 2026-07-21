package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import wayoftime.bloodmagic.api.BMIdentifiers;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.common.will.WorldWillHelper;

import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity-in-spirit port of 1.20.1's Ritual of the Complex Crystal ({@code RitualGeode}): a
 * five-Will-type utility ritual that harvests blocks in its harvest range (Steadfast Will grants
 * Silk Touch, Destructive Will grants Fortune 3, base Default Will stores drops in the chest range),
 * random-ticks growable blocks in its acceleration range under Corrosive Will, and hurts non-player
 * mobs in its harm range under Vengeful Will, feeding the resulting drops/damage back into
 * per-effect Will drains.
 * <p>
 * Adapted in two ways. First, this branch has none of the original's {@code ConfigManager}-backed
 * tunables (min-Will thresholds, per-effect Will costs, activation/refresh cost, harm damage, max
 * budding blocks/mobs harmed) since no such config system exists here - they're inlined as
 * reasonable constants matching the original's rough scale. Second, and more importantly, this
 * branch has no Blood Magic "geode"/budding-ore content at all (no {@code GEODE_HARVESTABLE}/
 * {@code GEODE_ACCELERATABLE} block tags exist here), so the harvest range instead targets any
 * block in NeoForge's common {@code Tags.Blocks.ORES} tag (mirroring {@link MagnetismRitual}'s
 * reuse of the ore tag) and the acceleration range targets any {@link BonemealableBlock} (mirroring
 * {@link GreenGroveRitual}'s growable check) - keeping the ritual's real five-Will-type harvest/
 * grow/harm mechanic intact against the closest existing equivalents. Loot is computed via
 * {@link Block#getDrops} with an enchanted mock tool (same approach as this branch's Anointment loot
 * modifiers) in place of the original's {@code LootParams.Builder}/fake-player lookup.
 */
public class GeodeRitual extends Ritual {
    public static final String ACCELERATION_RANGE = "acceleration";
    public static final String HARVEST_RANGE = "harvest";
    public static final String CHEST_RANGE = "chest";
    public static final String HARM_RANGE = "harm";

    private static final double MIN_WILL = 1.0;
    private static final double WILL_PER_SILK = 1.0;
    private static final double WILL_PER_FORTUNE = 1.0;
    private static final double WILL_PER_GROWTH = 1.0;
    private static final double WILL_PER_STORE = 0.5;
    private static final double WILL_PER_HARM = 1.0;

    private static final int ACTIVATION_COST = 20000;
    private static final int REFRESH_COST = 20;
    private static final int HURT_DAMAGE = 4;
    private static final int MAX_BLOCKS = 8;
    private static final int MAX_HARM = 4;

    public GeodeRitual() {
        super(RitualRegistry.rl("geode"), 0, ACTIVATION_COST, "ritual.bloodmagic.geode");
        addBlockRange(HARVEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-1, 2, -1), 3, 3, 3));
        addBlockRange(ACCELERATION_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 3, 0), 1));
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));
        addBlockRange(HARM_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-1, -3, -1), 3, 3, 3));

        setMaximumVolumeAndDistanceOfRange(HARVEST_RANGE, 3 * 3 * (2 + MAX_BLOCKS), 15, 15);
        setMaximumVolumeAndDistanceOfRange(ACCELERATION_RANGE, MAX_BLOCKS, 15, 15);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 15, 15);
        setMaximumVolumeAndDistanceOfRange(HARM_RANGE, 5 * 5 * 3, 15, 15);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos pos = masterRitualStone.getMasterBlockPos();
        List<EnumWillType> willConfig = masterRitualStone.getActiveWillConfig();

        double rawWill = this.getWillRespectingConfig(level, pos, EnumWillType.DEFAULT, willConfig);
        double steadfastWill = this.getWillRespectingConfig(level, pos, EnumWillType.STEADFAST, willConfig);
        double corrosiveWill = this.getWillRespectingConfig(level, pos, EnumWillType.CORROSIVE, willConfig);
        double destructiveWill = this.getWillRespectingConfig(level, pos, EnumWillType.DESTRUCTIVE, willConfig);
        double vengefulWill = this.getWillRespectingConfig(level, pos, EnumWillType.VENGEFUL, willConfig);

        BlockPos chestPos = masterRitualStone.getBlockRange(CHEST_RANGE).getContainedPositions(pos).get(0);
        IItemHandler inv = level.getCapability(Capabilities.ItemHandler.BLOCK, chestPos, Direction.DOWN);

        boolean doHarm = vengefulWill > MIN_WILL;
        boolean doAccel = corrosiveWill > MIN_WILL;
        boolean doStore = rawWill > MIN_WILL && inv != null;
        boolean doFortune = false;
        boolean doSilk = false;

        ItemStack toolStack = new ItemStack(Items.NETHERITE_PICKAXE);
        if (destructiveWill > MIN_WILL) {
            toolStack = mockPick(serverLevel, Enchantments.FORTUNE, 3);
            doFortune = true;
        }
        if (steadfastWill > MIN_WILL) {
            toolStack = mockPick(serverLevel, Enchantments.SILK_TOUCH, 1);
            doSilk = true;
            doFortune = false;
        }

        double fortuneWill = destructiveWill;
        double silkWill = steadfastWill;
        double storeWill = rawWill;

        for (BlockPos harvestPos : masterRitualStone.getBlockRange(HARVEST_RANGE).getContainedPositions(pos)) {
            BlockState state = level.getBlockState(harvestPos);
            if (state.isAir() || !state.is(Tags.Blocks.ORES)) {
                continue;
            }

            if (doFortune && fortuneWill < WILL_PER_FORTUNE) {
                continue;
            }
            if (doSilk && silkWill < WILL_PER_SILK) {
                continue;
            }

            List<ItemStack> blockDrops = Block.getDrops(state, serverLevel, harvestPos, level.getBlockEntity(harvestPos), null, toolStack);
            level.setBlockAndUpdate(harvestPos, Blocks.AIR.defaultBlockState());

            if (doFortune) {
                fortuneWill -= WILL_PER_FORTUNE;
            }
            if (doSilk) {
                silkWill -= WILL_PER_SILK;
            }

            for (ItemStack dropStack : blockDrops) {
                if (doStore && storeWill >= WILL_PER_STORE) {
                    dropStack = ItemHandlerHelper.insertItem(inv, dropStack, false);
                    storeWill -= WILL_PER_STORE;
                }
                if (!dropStack.isEmpty()) {
                    dropAt(level, harvestPos, dropStack);
                }
            }
        }

        double harmWill = vengefulWill;
        int harmed = 0;
        if (doHarm) {
            for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, masterRitualStone.getBlockRange(HARM_RANGE).getAABB(pos))) {
                if (harmWill < WILL_PER_HARM || harmed >= MAX_HARM) {
                    break;
                }
                if (mob.hurt(level.damageSources().source(BMIdentifiers.DamageTypes.SACRIFICE, mob), HURT_DAMAGE)) {
                    harmed++;
                    harmWill -= WILL_PER_HARM;
                }
            }
        }

        double accelWill = corrosiveWill;
        for (BlockPos accelPos : masterRitualStone.getBlockRange(ACCELERATION_RANGE).getContainedPositions(pos)) {
            BlockState state = level.getBlockState(accelPos);
            if (!(state.getBlock() instanceof BonemealableBlock)) {
                continue;
            }

            for (int i = 0; i < harmed; i++) {
                state.randomTick(serverLevel, accelPos, serverLevel.getRandom());
            }

            if (doAccel && accelWill >= WILL_PER_GROWTH) {
                state.randomTick(serverLevel, accelPos, serverLevel.getRandom());
                state.randomTick(serverLevel, accelPos, serverLevel.getRandom());
                accelWill -= WILL_PER_GROWTH;
            }
        }

        if (doStore) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.DEFAULT, rawWill - storeWill);
        }
        if (doFortune) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.DESTRUCTIVE, destructiveWill - fortuneWill);
        }
        if (doSilk) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.STEADFAST, steadfastWill - silkWill);
        }
        if (doAccel) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.CORROSIVE, corrosiveWill - accelWill);
        }
        if (doHarm) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.VENGEFUL, vengefulWill - harmWill);
        }

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost()));
    }

    private static void dropAt(Level level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, stack));
    }

    private static ItemStack mockPick(ServerLevel level, ResourceKey<Enchantment> enchantment, int enchantLevel) {
        ItemStack stack = new ItemStack(Items.NETHERITE_PICKAXE);
        Holder<Enchantment> holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(holder, enchantLevel));
        return stack;
    }

    @Override
    public int getRefreshCost() {
        return REFRESH_COST;
    }

    @Override
    public int getRefreshTime() {
        return 20;
    }

    @Override
    public Component[] provideInformationOfRitualToPlayer(Player player) {
        return new Component[]{
                Component.translatable(this.getTranslationKey() + ".info"),
                Component.translatable(this.getTranslationKey() + ".default.info"),
                Component.translatable(this.getTranslationKey() + ".corrosive.info"),
                Component.translatable(this.getTranslationKey() + ".steadfast.info"),
                Component.translatable(this.getTranslationKey() + ".destructive.info"),
                Component.translatable(this.getTranslationKey() + ".vengeful.info")
        };
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addCornerRunes(components, 1, 0, EnumRuneType.EARTH);
        addParallelRunes(components, 1, 0, EnumRuneType.AIR);
        addParallelRunes(components, 2, 0, EnumRuneType.FIRE);
    }

    @Override
    public Ritual getNewCopy() {
        return new GeodeRitual();
    }
}
