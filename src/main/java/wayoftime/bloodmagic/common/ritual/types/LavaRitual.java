package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.api.BMIdentifiers;
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
 * Full-fidelity port of 1.20.1's Ritual of the Crucible ({@code RitualLava}): fills its lava range
 * (which Destructive Will can widen), Fire Fuses nearby non-players with Vengeful Will, grants Fire
 * Resistance to nearby players with Steadfast Will, and periodically true-damages fire-immune
 * mobs with Corrosive Will. This branch has no fluid-tank capability, so the tank-filling
 * sub-effect from the original is dropped (matching FullSpringRitual/other Will-draining rituals).
 * The original's corrosive-damage condition checked {@code !entity.isAlive()}, which - given
 * {@code getEntitiesOfClass} only ever returns live entities - made that whole branch dead code;
 * this ports the evident intent (damage entities that are otherwise fire-immune) instead.
 */
public class LavaRitual extends Ritual {
    public static final String LAVA_RANGE = "lavaRange";
    public static final String FIRE_FUSE_RANGE = "fireFuse";
    public static final String FIRE_RESIST_RANGE = "fireResist";
    public static final String FIRE_DAMAGE_RANGE = "fireDamage";

    public static final double VENGEFUL_WILL_DRAIN = 1;
    public static final double STEADFAST_WILL_DRAIN = 0.5;
    public static final double CORROSIVE_WILL_DRAIN = 0.2;
    public static final int CORROSIVE_REFRESH_TIME = 20;
    private int timer = 0;

    public LavaRitual() {
        super(RitualRegistry.rl("lava"), 0, 10000, "ritual.bloodmagic.lava");
        addBlockRange(LAVA_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));
        addBlockRange(FIRE_FUSE_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-2, -2, -2), 5));
        addBlockRange(FIRE_RESIST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 0, 0), 1));
        addBlockRange(FIRE_DAMAGE_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 0, 0), 1));

        setMaximumVolumeAndDistanceOfRange(LAVA_RANGE, 9, 3, 3);
        setMaximumVolumeAndDistanceOfRange(FIRE_FUSE_RANGE, 0, 10, 10);
        setMaximumVolumeAndDistanceOfRange(FIRE_RESIST_RANGE, 0, 10, 10);
        setMaximumVolumeAndDistanceOfRange(FIRE_DAMAGE_RANGE, 0, 10, 10);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        timer++;
        Level level = masterRitualStone.getWorldObj();
        int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();
        int lpDrain = 0;

        if (currentEssence < getRefreshCost()) {
            masterRitualStone.causeNausea();
            return;
        }

        BlockPos pos = masterRitualStone.getMasterBlockPos();
        List<EnumWillType> willConfig = masterRitualStone.getActiveWillConfig();

        double rawWill = this.getWillRespectingConfig(level, pos, EnumWillType.DEFAULT, willConfig);
        double rawDrained = 0;

        AreaDescriptor lavaRange = masterRitualStone.getBlockRange(LAVA_RANGE);

        int maxLavaVolume = getMaxVolumeForRange(LAVA_RANGE, willConfig, level, pos);
        if (!lavaRange.isWithinRange(getMaxVerticalRadiusForRange(LAVA_RANGE, willConfig, level, pos), getMaxHorizontalRadiusForRange(LAVA_RANGE, willConfig, level, pos))
                || (maxLavaVolume != 0 && lavaRange.getVolume() > maxLavaVolume)) {
            return;
        }

        for (BlockPos newPos : lavaRange.getContainedPositions(pos)) {
            BlockState state = level.getBlockState(newPos);
            boolean flowingLiquid = !state.getFluidState().isEmpty() && !state.getFluidState().isSource();
            if (level.isEmptyBlock(newPos) || flowingLiquid) {
                int lpCost = getLPCostForRawWill(rawWill);
                if (currentEssence < lpCost) {
                    break;
                }

                level.setBlockAndUpdate(newPos, Blocks.LAVA.defaultBlockState());
                currentEssence -= lpCost;
                lpDrain += lpCost;
                if (rawWill > 0) {
                    double drain = getWillCostForRawWill(rawWill);
                    rawWill -= drain;
                    rawDrained += drain;
                }
            }
        }

        double vengefulWill = this.getWillRespectingConfig(level, pos, EnumWillType.VENGEFUL, willConfig);
        double steadfastWill = this.getWillRespectingConfig(level, pos, EnumWillType.STEADFAST, willConfig);
        double corrosiveWill = this.getWillRespectingConfig(level, pos, EnumWillType.CORROSIVE, willConfig);

        if (vengefulWill >= VENGEFUL_WILL_DRAIN) {
            double vengefulDrained = 0;
            AreaDescriptor fuseRange = masterRitualStone.getBlockRange(FIRE_FUSE_RANGE);
            AABB fuseArea = fuseRange.getAABB(pos);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, fuseArea);

            for (LivingEntity entity : entities) {
                if (vengefulWill < VENGEFUL_WILL_DRAIN) {
                    break;
                }

                if (entity instanceof Player) {
                    continue;
                }

                if (!entity.hasEffect(BMPotions.FIRE_FUSE)) {
                    entity.addEffect(new MobEffectInstance(BMPotions.FIRE_FUSE, 100, 0));
                    vengefulDrained += VENGEFUL_WILL_DRAIN;
                    vengefulWill -= VENGEFUL_WILL_DRAIN;
                }
            }

            if (vengefulDrained > 0) {
                WorldWillHelper.drainWill(level, pos, EnumWillType.VENGEFUL, vengefulDrained);
            }
        }

        if (steadfastWill >= STEADFAST_WILL_DRAIN) {
            double steadfastDrained = 0;
            AreaDescriptor resistRange = masterRitualStone.getBlockRange(FIRE_RESIST_RANGE);
            AABB resistArea = resistRange.getAABB(pos);
            List<Player> entities = level.getEntitiesOfClass(Player.class, resistArea);

            for (Player entity : entities) {
                if (steadfastWill < STEADFAST_WILL_DRAIN) {
                    break;
                }

                MobEffectInstance existing = entity.getEffect(MobEffects.FIRE_RESISTANCE);
                if (existing == null || existing.getDuration() < 2) {
                    entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0));
                    steadfastDrained += STEADFAST_WILL_DRAIN;
                    steadfastWill -= STEADFAST_WILL_DRAIN;
                }
            }

            if (steadfastDrained > 0) {
                WorldWillHelper.drainWill(level, pos, EnumWillType.STEADFAST, steadfastDrained);
            }
        }

        if (timer % CORROSIVE_REFRESH_TIME == 0 && corrosiveWill >= CORROSIVE_WILL_DRAIN) {
            double corrosiveDrained = 0;
            AreaDescriptor damageRange = masterRitualStone.getBlockRange(FIRE_DAMAGE_RANGE);
            float damage = getCorrosiveDamageForWill(corrosiveWill);
            AABB damageArea = damageRange.getAABB(pos);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, damageArea);

            for (LivingEntity entity : entities) {
                if (corrosiveWill < CORROSIVE_WILL_DRAIN) {
                    break;
                }

                if (entity.isAlive() && entity.hurtTime <= 0 && entity.fireImmune()) {
                    if (entity.hurt(level.damageSources().source(BMIdentifiers.DamageTypes.SACRIFICE, entity), damage)) {
                        corrosiveDrained += CORROSIVE_WILL_DRAIN;
                        corrosiveWill -= CORROSIVE_WILL_DRAIN;
                    }
                }
            }

            if (corrosiveDrained > 0) {
                WorldWillHelper.drainWill(level, pos, EnumWillType.CORROSIVE, corrosiveDrained);
            }
        }

        if (rawDrained > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.DEFAULT, rawDrained);
        }

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(lpDrain));
    }

    @Override
    public int getRefreshTime() {
        return 1;
    }

    @Override
    public int getRefreshCost() {
        return 500;
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
        addParallelRunes(components, 1, 0, EnumRuneType.FIRE);
    }

    @Override
    public Ritual getNewCopy() {
        return new LavaRitual();
    }

    @Override
    public int getMaxVolumeForRange(String range, List<EnumWillType> activeTypes, Level level, BlockPos pos) {
        if (LAVA_RANGE.equals(range) && activeTypes.contains(EnumWillType.DESTRUCTIVE)) {
            double destructiveWill = WorldWillHelper.getWill(level, pos, EnumWillType.DESTRUCTIVE);
            if (destructiveWill > 0) {
                return 9 + (int) Math.pow(destructiveWill / 10, 1.5);
            }
        }

        return volumeRangeMap.getOrDefault(range, 0);
    }

    @Override
    public int getMaxVerticalRadiusForRange(String range, List<EnumWillType> activeTypes, Level level, BlockPos pos) {
        if (LAVA_RANGE.equals(range) && activeTypes.contains(EnumWillType.DESTRUCTIVE)) {
            double destructiveWill = WorldWillHelper.getWill(level, pos, EnumWillType.DESTRUCTIVE);
            if (destructiveWill > 0) {
                return (int) (3 + destructiveWill / 10d);
            }
        }

        return verticalRangeMap.getOrDefault(range, 0);
    }

    @Override
    public int getMaxHorizontalRadiusForRange(String range, List<EnumWillType> activeTypes, Level level, BlockPos pos) {
        if (LAVA_RANGE.equals(range) && activeTypes.contains(EnumWillType.DESTRUCTIVE)) {
            double destructiveWill = WorldWillHelper.getWill(level, pos, EnumWillType.DESTRUCTIVE);
            if (destructiveWill > 0) {
                return (int) (3 + destructiveWill / 10d);
            }
        }

        return horizontalRangeMap.getOrDefault(range, 0);
    }

    public int getFireResistForWill(double steadfastWill) {
        return (int) (200 + steadfastWill * 3);
    }

    public float getCorrosiveDamageForWill(double corrosiveWill) {
        return (float) (1 + corrosiveWill * 0.05);
    }

    public int getLPCostForRawWill(double raw) {
        return Math.max((int) (500 - raw), 0);
    }

    public double getWillCostForRawWill(double raw) {
        return Math.min(1, raw / 500);
    }
}
