package wayoftime.bloodmagic.common.potion;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class FireFuseMobEffect extends MobEffect {
    public FireFuseMobEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF0000);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();
        if (level.isClientSide) {
            return true;
        }

        RandomSource random = level.random;
        level.addParticle(ParticleTypes.FLAME, entity.getX() + random.nextDouble() * 0.3, entity.getY() + random.nextDouble() * 0.3, entity.getZ() + random.nextDouble() * 0.3, 0, 0.06D, 0);

        MobEffectInstance instance = entity.getEffect(BMPotions.FIRE_FUSE);
        if (instance != null && instance.getDuration() <= 3) {
            int radius = amplifier + 1;
            level.explode(null, entity.getX(), entity.getY(), entity.getZ(), radius, Level.ExplosionInteraction.NONE);
        }

        return true;
    }
}
