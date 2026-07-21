package wayoftime.bloodmagic.common.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Monster;

/**
 * Ported from 1.20.1: makes a mob passive by installing {@link NearestAttackableDummyGoal} at the
 * top of its target selector for as long as this effect is active.
 */
public class PassivityMobEffect extends MobEffect {
    public PassivityMobEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFFFFF);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof PathfinderMob animal) {
            TargetGoal goal = new NearestAttackableDummyGoal<>(animal, Monster.class, false);
            animal.targetSelector.addGoal(0, goal);
        }

        return true;
    }
}
