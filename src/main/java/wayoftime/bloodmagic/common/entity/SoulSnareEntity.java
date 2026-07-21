package wayoftime.bloodmagic.common.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.common.potion.BMPotions;

/**
 * Full-fidelity port of 1.20.1's Soul Snare projectile ({@code EntitySoulSnare}): a thrown marker
 * bolt that deals no real damage, but on hitting a living entity applies the {@code SOUL_SNARE}
 * mob effect (300 ticks, amplifier 0) - {@link wayoftime.bloodmagic.common.potion.BMPotionEventHandler#onLivingDrops}
 * already turns a snared kill into a guaranteed bonus raw-Will drop on this branch, so no changes
 * were needed there; this entity is simply what was missing to actually apply the marker.
 */
public class SoulSnareEntity extends ThrowableItemProjectile {
    public SoulSnareEntity(EntityType<? extends SoulSnareEntity> type, Level level) {
        super(type, level);
    }

    public SoulSnareEntity(Level level, LivingEntity thrower) {
        super(BMEntities.SOUL_SNARE.get(), thrower, level);
    }

    public SoulSnareEntity(Level level, double x, double y, double z) {
        super(BMEntities.SOUL_SNARE.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return BMItems.SOUL_SNARE.get();
    }

    private ParticleOptions getParticle() {
        ItemStack stack = getItem();
        return !stack.isEmpty() ? new ItemParticleOption(ParticleTypes.ITEM, stack) : ParticleTypes.ITEM_SNOWBALL;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ParticleOptions particle = getParticle();
            for (int i = 0; i < 8; i++) {
                level().addParticle(particle, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        if (entity == getOwner() || tickCount < 2 || level().isClientSide) {
            return;
        }

        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(BMPotions.SOUL_SNARE, 300, 0));
            living.hurt(living.damageSources().thrown(this, getOwner()), 0.0F);
        }

        discard();
    }
}
