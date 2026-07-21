package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.IRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.block.MasterRitualStoneBlock;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.common.will.WorldWillHelper;

import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of the Crusher ({@code RitualCrushing}): each pulse (pulse
 * rate sped up by Default Will) mines one non-protected block out of its crushing range - Silk Touch
 * if Steadfast Will is present, else a Destructive-Will-gated Fortune 3 - and deposits the drops in
 * its chest range (falling back to an {@link ItemEntity} if full/absent). Loot is computed via
 * {@link Block#getDrops} with an enchanted mock pickaxe, the same approach this branch's Anointment
 * loot modifiers use, in place of the original's {@code LootParams.Builder}. The original's
 * Corrosive-Will "cutting fluid" bonus-drop system ({@code ICrushingHandler}/{@code CrushingRegistry})
 * and Vengeful-Will inventory-compression bonus were already dead, fully commented-out code upstream
 * in 1.20.1 (never wired up even there), so neither is ported.
 */
public class CrushingRitual extends Ritual {
    public static final String CRUSHING_RANGE = "crushingRange";
    public static final String CHEST_RANGE = "chest";

    private static final double RAW_WILL_DRAIN = 0.05;
    private static final double STEADFAST_WILL_DRAIN = 0.2;
    private static final double DESTRUCTIVE_WILL_DRAIN = 0.2;
    private static final int DEFAULT_REFRESH_TIME = 40;

    private int refreshTime = DEFAULT_REFRESH_TIME;

    public CrushingRitual() {
        super(RitualRegistry.rl("crushing"), 0, 5000, "ritual.bloodmagic.crushing");
        addBlockRange(CRUSHING_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-1, -3, -1), 3));
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

        setMaximumVolumeAndDistanceOfRange(CRUSHING_RANGE, 50, 10, 10);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();
        if (currentEssence < getRefreshCost()) {
            masterRitualStone.causeNausea();
            return;
        }

        BlockPos pos = masterRitualStone.getMasterBlockPos();
        List<BlockPos> chestList = masterRitualStone.getBlockRange(CHEST_RANGE).getContainedPositions(pos);
        BlockPos chestPos = chestList.isEmpty() ? null : chestList.get(0);
        IItemHandler tile = chestPos == null ? null : level.getCapability(Capabilities.ItemHandler.BLOCK, chestPos, Direction.DOWN);

        if (tile != null && !hasFreeSlot(tile)) {
            return;
        }

        List<EnumWillType> willConfig = masterRitualStone.getActiveWillConfig();
        double rawWill = this.getWillRespectingConfig(level, pos, EnumWillType.DEFAULT, willConfig);
        double steadfastWill = this.getWillRespectingConfig(level, pos, EnumWillType.STEADFAST, willConfig);
        double destructiveWill = this.getWillRespectingConfig(level, pos, EnumWillType.DESTRUCTIVE, willConfig);

        refreshTime = getRefreshTimeForRawWill(rawWill);
        boolean consumeRawWill = rawWill >= RAW_WILL_DRAIN && refreshTime != DEFAULT_REFRESH_TIME;
        boolean isSilkTouch = steadfastWill >= STEADFAST_WILL_DRAIN;
        int fortune = destructiveWill > 0 ? 3 : 0;

        double rawDrain = 0;

        for (BlockPos newPos : masterRitualStone.getBlockRange(CRUSHING_RANGE).getContainedPositions(pos)) {
            if (level.isEmptyBlock(newPos)) {
                continue;
            }

            BlockState state = level.getBlockState(newPos);
            Block block = state.getBlock();

            if (block instanceof MasterRitualStoneBlock || block instanceof IRitualStone || state.getDestroySpeed(level, newPos) == -1.0F || !state.getFluidState().isEmpty()) {
                continue;
            }

            List<ItemStack> drops;
            if (isSilkTouch) {
                if (steadfastWill < STEADFAST_WILL_DRAIN) {
                    continue;
                }
                drops = Block.getDrops(state, serverLevel, newPos, level.getBlockEntity(newPos), null, mockPick(serverLevel, Enchantments.SILK_TOUCH, 1));
                steadfastWill -= STEADFAST_WILL_DRAIN;
            } else {
                int useFortune = fortune > 0 && destructiveWill < DESTRUCTIVE_WILL_DRAIN ? 0 : fortune;
                drops = Block.getDrops(state, serverLevel, newPos, level.getBlockEntity(newPos), null, mockPick(serverLevel, Enchantments.FORTUNE, useFortune));
                if (useFortune > 0) {
                    destructiveWill -= DESTRUCTIVE_WILL_DRAIN;
                }
            }

            level.setBlockAndUpdate(newPos, Blocks.AIR.defaultBlockState());

            for (ItemStack drop : drops) {
                insertOrDrop(level, newPos, tile, drop);
            }

            masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost()));

            if (consumeRawWill) {
                rawDrain += RAW_WILL_DRAIN;
                rawWill -= RAW_WILL_DRAIN;
            }

            break;
        }

        if (rawDrain > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.DEFAULT, rawDrain);
        }
    }

    private static boolean hasFreeSlot(IItemHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private static void insertOrDrop(Level level, BlockPos pos, IItemHandler inv, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack remainder = inv == null ? stack : ItemHandlerHelper.insertItem(inv, stack, false);
        if (!remainder.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, remainder));
        }
    }

    private static ItemStack mockPick(ServerLevel level, ResourceKey<Enchantment> enchantment, int enchantLevel) {
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        if (enchantLevel <= 0) {
            return stack;
        }
        Holder<Enchantment> holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(holder, enchantLevel));
        return stack;
    }

    public int getRefreshTimeForRawWill(double rawWill) {
        if (rawWill >= RAW_WILL_DRAIN) {
            return Math.max(1, (int) (40 - rawWill / 5));
        }

        return DEFAULT_REFRESH_TIME;
    }

    @Override
    public int getRefreshTime() {
        return refreshTime;
    }

    @Override
    public int getRefreshCost() {
        return 7;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addParallelRunes(components, 1, 0, EnumRuneType.EARTH);
        addParallelRunes(components, 2, 0, EnumRuneType.FIRE);
        addCornerRunes(components, 2, 0, EnumRuneType.DUSK);
        addParallelRunes(components, 2, 1, EnumRuneType.AIR);
    }

    @Override
    public Component[] provideInformationOfRitualToPlayer(Player player) {
        return new Component[]{Component.translatable(this.getTranslationKey() + ".info"),
                Component.translatable(this.getTranslationKey() + ".default.info"),
                Component.translatable(this.getTranslationKey() + ".corrosive.info"),
                Component.translatable(this.getTranslationKey() + ".steadfast.info"),
                Component.translatable(this.getTranslationKey() + ".destructive.info"),
                Component.translatable(this.getTranslationKey() + ".vengeful.info")};
    }

    @Override
    public Ritual getNewCopy() {
        return new CrushingRitual();
    }
}
