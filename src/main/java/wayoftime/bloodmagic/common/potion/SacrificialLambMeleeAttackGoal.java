package wayoftime.bloodmagic.common.potion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/**
 * Ported from 1.20.1 as-is: a melee-attack goal, active only while {@link BMPotions#SACRIFICIAL_LAMB}
 * is active, that never actually attacks ({@code checkAndPerformAttack} is empty) - it exists purely
 * so the mob paths toward its target; the real payoff (an explosion) happens in
 * {@link SacrificialLambMobEffect#applyEffectTick} once it's close enough.
 */
public class SacrificialLambMeleeAttackGoal extends MeleeAttackGoal {
    public SacrificialLambMeleeAttackGoal(PathfinderMob creature, double speedIn, boolean useLongMemory) {
        super(creature, speedIn, useLongMemory);
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity enemy) {
    }

    @Override
    public boolean canUse() {
        return this.mob.hasEffect(BMPotions.SACRIFICIAL_LAMB) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.hasEffect(BMPotions.SACRIFICIAL_LAMB) && super.canContinueToUse();
    }
}
