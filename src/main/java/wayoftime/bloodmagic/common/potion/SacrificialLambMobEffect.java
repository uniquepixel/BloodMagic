package wayoftime.bloodmagic.common.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Ported from 1.20.1: an aggressive, self-destructive buff - the mob chases the nearest monster
 * (via {@link SacrificialLambMeleeAttackGoal}, which never lands a real hit) and detonates once
 * it's within melee range, with blast power scaling off the effect's amplifier.
 */
public class SacrificialLambMobEffect extends MobEffect {
    public SacrificialLambMobEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFFFFF);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof PathfinderMob animal) {
            TargetGoal goal = new NearestAttackableTargetGoal<>(animal, Monster.class, false);
            SacrificialLambMeleeAttackGoal attackGoal = new SacrificialLambMeleeAttackGoal(animal, 2.0D, false);

            animal.targetSelector.addGoal(2, goal);
            animal.goalSelector.addGoal(2, attackGoal);

            MobEffectInstance instance = animal.getEffect(BMPotions.SACRIFICIAL_LAMB);
            if (animal.getTarget() != null && animal.distanceToSqr(animal.getTarget()) < 4 && instance != null) {
                animal.level().explode(null, animal.getX(), animal.getY() + animal.getBbHeight() / 16.0F, animal.getZ(),
                        2 + instance.getAmplifier() * 1.5f, false, Level.ExplosionInteraction.NONE);
            }
        }

        return true;
    }
}
