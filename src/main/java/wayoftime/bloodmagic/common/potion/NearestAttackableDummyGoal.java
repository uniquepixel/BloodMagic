package wayoftime.bloodmagic.common.potion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

/**
 * Ported from 1.20.1 as-is: a no-op {@code TargetGoal} registered at the highest priority while
 * {@link BMPotions#PASSIVITY} is active. It never actually assigns a target (its {@code start()}
 * is empty) - the trick is that occupying the top-priority target-goal slot suppresses the mob's
 * normal targeting goals for as long as it "can use", which is what makes the mob passive.
 */
public class NearestAttackableDummyGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    public NearestAttackableDummyGoal(Mob goalOwnerIn, Class<T> targetClassIn, boolean checkSight) {
        super(goalOwnerIn, targetClassIn, checkSight);
    }

    @Override
    public void start() {
    }

    @Override
    public boolean canUse() {
        return this.mob.hasEffect(BMPotions.PASSIVITY);
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.hasEffect(BMPotions.PASSIVITY);
    }
}
