package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.potion.BMPotions;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.common.will.WorldWillHelper;

import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of the Green Grove ({@code RitualGreenGrove}): random-ticks
 * bonemealable/cactus/sugarcane blocks in its growing range (Vengeful Will raises the chance,
 * Default Will speeds up the pulse rate), hydrates dirt/farmland in its hydrate range with
 * Steadfast Will, and afflicts non-player mobs in its leech range with Plant Leech under Corrosive
 * Will. The AgriCraft soft-dependency crop-weed-removal hook is dropped - that mod isn't present in
 * this branch - and the auto-seed-planting call in the hydrate step is dropped too, a pure QoL
 * layer on top of hydration, not the hydration itself.
 */
public class GreenGroveRitual extends Ritual {
    public static final String GROW_RANGE = "growing";
    public static final String LEECH_RANGE = "leech";
    public static final String HYDRATE_RANGE = "hydrate";

    public static final double RAW_WILL_DRAIN = 0.05;
    public static final double VENGEFUL_WILL_DRAIN = 0.05;
    public static final double STEADFAST_WILL_DRAIN = 0.05;
    public static final double CORROSIVE_WILL_DRAIN = 0.2;
    public static final int DEFAULT_REFRESH_TIME = 20;
    public static final double DEFAULT_GROWTH_CHANCE = 0.3;
    public static final BlockState FARMLAND_STATE = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7);

    private int refreshTime = DEFAULT_REFRESH_TIME;

    public GreenGroveRitual() {
        super(RitualRegistry.rl("green_grove"), 0, 5000, "ritual.bloodmagic.green_grove");
        addBlockRange(GROW_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-1, 2, -1), 3, 1, 3));
        addBlockRange(LEECH_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-1, 2, -1), 3, 1, 3));
        addBlockRange(HYDRATE_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-1, 1, -1), 3, 2, 3));
        setMaximumVolumeAndDistanceOfRange(GROW_RANGE, 81, 4, 4);
        setMaximumVolumeAndDistanceOfRange(LEECH_RANGE, 0, 15, 15);
        setMaximumVolumeAndDistanceOfRange(HYDRATE_RANGE, 0, 15, 15);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos pos = masterRitualStone.getMasterBlockPos();
        int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();

        if (currentEssence < getRefreshCost()) {
            masterRitualStone.causeNausea();
            return;
        }

        int maxGrowths = currentEssence / getRefreshCost();
        int totalGrowths = 0;

        List<EnumWillType> willConfig = masterRitualStone.getActiveWillConfig();

        double rawWill = this.getWillRespectingConfig(level, pos, EnumWillType.DEFAULT, willConfig);
        double steadfastWill = this.getWillRespectingConfig(level, pos, EnumWillType.STEADFAST, willConfig);
        double vengefulWill = this.getWillRespectingConfig(level, pos, EnumWillType.VENGEFUL, willConfig);
        double corrosiveWill = this.getWillRespectingConfig(level, pos, EnumWillType.CORROSIVE, willConfig);

        refreshTime = getRefreshTimeForRawWill(rawWill);
        double growthChance = getPlantGrowthChanceForWill(vengefulWill);

        boolean consumeRawWill = rawWill >= RAW_WILL_DRAIN && refreshTime != DEFAULT_REFRESH_TIME;
        boolean consumeVengefulWill = vengefulWill >= VENGEFUL_WILL_DRAIN && growthChance != DEFAULT_GROWTH_CHANCE;

        double rawDrain = 0;
        double vengefulDrain = 0;

        AreaDescriptor growingRange = masterRitualStone.getBlockRange(GROW_RANGE);

        int maxGrowthVolume = getMaxVolumeForRange(GROW_RANGE, willConfig, level, pos);
        if (!growingRange.isWithinRange(getMaxVerticalRadiusForRange(GROW_RANGE, willConfig, level, pos), getMaxHorizontalRadiusForRange(GROW_RANGE, willConfig, level, pos))
                || (maxGrowthVolume != 0 && growingRange.getVolume() > maxGrowthVolume)) {
            return;
        }

        for (BlockPos newPos : growingRange.getContainedPositions(pos)) {
            BlockState state = level.getBlockState(newPos);
            Block block = state.getBlock();

            boolean growable = block instanceof BonemealableBlock || block instanceof CactusBlock || block instanceof SugarCaneBlock;
            if (growable && level.random.nextDouble() < growthChance) {
                state.randomTick(serverLevel, newPos, serverLevel.random);
                BlockState newState = level.getBlockState(newPos);
                if (!newState.equals(state)) {
                    level.levelEvent(2005, newPos, 0);
                    totalGrowths++;
                    if (consumeRawWill) {
                        rawWill -= RAW_WILL_DRAIN;
                        rawDrain += RAW_WILL_DRAIN;
                    }

                    if (consumeVengefulWill) {
                        vengefulWill -= VENGEFUL_WILL_DRAIN;
                        vengefulDrain += VENGEFUL_WILL_DRAIN;
                    }
                }
            }

            if (totalGrowths >= maxGrowths || (consumeRawWill && rawWill < RAW_WILL_DRAIN) || (consumeVengefulWill && vengefulWill < VENGEFUL_WILL_DRAIN)) {
                break;
            }
        }

        if (rawDrain > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.DEFAULT, rawDrain);
        }

        if (vengefulDrain > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.VENGEFUL, vengefulDrain);
        }

        AreaDescriptor hydrateRange = masterRitualStone.getBlockRange(HYDRATE_RANGE);
        double steadfastDrain = 0;
        if (steadfastWill > STEADFAST_WILL_DRAIN) {
            for (BlockPos newPos : hydrateRange.getContainedPositions(pos)) {
                if (steadfastWill < STEADFAST_WILL_DRAIN) {
                    break;
                }

                BlockState state = level.getBlockState(newPos);
                Block block = state.getBlock();

                boolean hydratedBlock = false;
                if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK) {
                    level.setBlockAndUpdate(newPos, FARMLAND_STATE);
                    hydratedBlock = true;
                } else if (block == Blocks.FARMLAND) {
                    int moisture = state.getValue(FarmBlock.MOISTURE);
                    if (moisture < 7) {
                        level.setBlockAndUpdate(newPos, FARMLAND_STATE);
                        hydratedBlock = true;
                    }
                }

                if (hydratedBlock) {
                    steadfastWill -= STEADFAST_WILL_DRAIN;
                    steadfastDrain += STEADFAST_WILL_DRAIN;
                }
            }
        }

        if (steadfastDrain > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.STEADFAST, steadfastDrain);
        }

        double corrosiveDrain = 0;
        if (corrosiveWill > CORROSIVE_WILL_DRAIN) {
            AreaDescriptor leechRange = masterRitualStone.getBlockRange(LEECH_RANGE);
            AABB mobArea = leechRange.getAABB(pos);
            List<LivingEntity> entityList = level.getEntitiesOfClass(LivingEntity.class, mobArea);

            for (LivingEntity entityLiving : entityList) {
                if (corrosiveWill < CORROSIVE_WILL_DRAIN) {
                    break;
                }

                if (entityLiving instanceof Player) {
                    continue;
                }

                if (entityLiving.hasEffect(BMPotions.PLANT_LEECH) || !entityLiving.canBeAffected(new MobEffectInstance(BMPotions.PLANT_LEECH))) {
                    continue;
                }

                entityLiving.addEffect(new MobEffectInstance(BMPotions.PLANT_LEECH, 200, 0));
                corrosiveWill -= CORROSIVE_WILL_DRAIN;
                corrosiveDrain += CORROSIVE_WILL_DRAIN;
            }

            if (corrosiveDrain > 0) {
                WorldWillHelper.drainWill(level, pos, EnumWillType.CORROSIVE, corrosiveDrain);
            }
        }

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(totalGrowths * getRefreshCost()));
    }

    public double getPlantGrowthChanceForWill(double will) {
        return will > 0 ? 0.3 + will / 200 : DEFAULT_GROWTH_CHANCE;
    }

    public int getRefreshTimeForRawWill(double rawWill) {
        return rawWill > 0 ? 10 : DEFAULT_REFRESH_TIME;
    }

    @Override
    public int getRefreshTime() {
        return refreshTime;
    }

    @Override
    public int getMaxVolumeForRange(String range, List<EnumWillType> activeTypes, Level level, BlockPos pos) {
        if (GROW_RANGE.equals(range) && activeTypes.contains(EnumWillType.DESTRUCTIVE)) {
            double destructiveWill = WorldWillHelper.getWill(level, pos, EnumWillType.DESTRUCTIVE);
            if (destructiveWill > 0) {
                return 81 + (int) Math.pow(destructiveWill / 4, 1.5);
            }
        }

        return volumeRangeMap.getOrDefault(range, 0);
    }

    @Override
    public int getMaxVerticalRadiusForRange(String range, List<EnumWillType> activeTypes, Level level, BlockPos pos) {
        if (GROW_RANGE.equals(range) && activeTypes.contains(EnumWillType.DESTRUCTIVE)) {
            double destructiveWill = WorldWillHelper.getWill(level, pos, EnumWillType.DESTRUCTIVE);
            if (destructiveWill > 0) {
                return (int) (4 + destructiveWill / 10d);
            }
        }

        return verticalRangeMap.getOrDefault(range, 0);
    }

    @Override
    public int getMaxHorizontalRadiusForRange(String range, List<EnumWillType> activeTypes, Level level, BlockPos pos) {
        if (GROW_RANGE.equals(range) && activeTypes.contains(EnumWillType.DESTRUCTIVE)) {
            double destructiveWill = WorldWillHelper.getWill(level, pos, EnumWillType.DESTRUCTIVE);
            if (destructiveWill > 0) {
                return (int) (4 + destructiveWill / 10d);
            }
        }

        return horizontalRangeMap.getOrDefault(range, 0);
    }

    @Override
    public int getRefreshCost() {
        return 20;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addCornerRunes(components, 1, 0, EnumRuneType.EARTH);
        addParallelRunes(components, 1, 0, EnumRuneType.WATER);
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
    public Ritual getNewCopy() {
        return new GreenGroveRitual();
    }
}
