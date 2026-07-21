package wayoftime.bloodmagic.common.potion;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.api.BMIdentifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Ported from 1.20.1: every 10 ticks, force-grows a handful of random bonemealable blocks near the
 * afflicted entity and damages it proportionally to how many actually grew - the effect behind
 * {@link wayoftime.bloodmagic.common.ritual.types.GreenGroveRitual}'s Corrosive-Will "leech"
 * sub-effect.
 */
public class PlantLeechMobEffect extends MobEffect {
    private static final Random RANDOM = new Random();

    public PlantLeechMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x00FF00);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        damageMobAndGrowSurroundingPlants(entity, 2 + amplifier, 1, 0.5 * 3 / (amplifier + 3), 25 * (1 + amplifier));
        return true;
    }

    public static double damageMobAndGrowSurroundingPlants(LivingEntity entity, int horizontalRadius, int verticalRadius, double damageRatio, int maxPlantsGrown) {
        Level level = entity.level();
        if (level.isClientSide || !entity.isAlive() || !(level instanceof ServerLevel serverLevel)) {
            return 0;
        }

        double incurredDamage = 0;
        List<BlockPos> growList = new ArrayList<>();

        for (int i = 0; i < maxPlantsGrown; i++) {
            BlockPos blockPos = entity.blockPosition().offset(
                    RANDOM.nextInt(horizontalRadius * 2 + 1) - horizontalRadius,
                    RANDOM.nextInt(verticalRadius * 2 + 1) - verticalRadius,
                    RANDOM.nextInt(horizontalRadius * 2 + 1) - horizontalRadius);
            BlockState state = level.getBlockState(blockPos);

            if (state.getBlock() instanceof BonemealableBlock) {
                growList.add(blockPos);
            }
        }

        for (BlockPos blockPos : growList) {
            BlockState preBlockState = level.getBlockState(blockPos);
            for (int n = 0; n < 10; n++) {
                BlockState currentState = level.getBlockState(blockPos);
                if (!currentState.is(preBlockState.getBlock()) || !(currentState.getBlock() instanceof BonemealableBlock)) {
                    break;
                }
                currentState.randomTick(serverLevel, blockPos, level.random);
            }

            BlockState newState = level.getBlockState(blockPos);
            if (!newState.equals(preBlockState)) {
                level.levelEvent(2005, blockPos, 0);
                incurredDamage += damageRatio;
            }
        }

        if (incurredDamage > 0) {
            entity.hurt(level.damageSources().source(BMIdentifiers.DamageTypes.SACRIFICE, entity), (float) incurredDamage);
        }

        return incurredDamage;
    }
}
