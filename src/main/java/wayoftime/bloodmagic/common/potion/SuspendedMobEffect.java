package wayoftime.bloodmagic.common.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class SuspendedMobEffect extends MobEffect {
    public SuspendedMobEffect() {
        super(MobEffectCategory.NEUTRAL, 0x23DDE1);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.setNoGravity(true);
        if (entity.getEffect(BMPotions.SUSPENDED) != null && entity.getEffect(BMPotions.SUSPENDED).getDuration() <= 1) {
            entity.setNoGravity(false);
        }
        return true;
    }
}
