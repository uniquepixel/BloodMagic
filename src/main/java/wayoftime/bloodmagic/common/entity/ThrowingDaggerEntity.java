package wayoftime.bloodmagic.common.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.common.will.WorldWillHelper;

/**
 * Full-fidelity-in-spirit port of 1.20.1's throwing dagger projectile ({@code EntityThrowingDagger}
 * / {@code AbstractEntityThrowingDagger}): a real thrown weapon that deals flat damage and, on a
 * killing blow, drops ambient Will into the world at the kill site (the original credited the
 * thrower's own Will pool directly; this branch's Will economy is aura-based rather than
 * player-carried-on-hit, so the drop goes into the world aura instead, consistent with how every
 * other Will-producing mechanic in this branch works). Not ported: in-ground sticking/pickup,
 * potion-tipped and Will-extraction syringe variants, and the custom renderer - this uses vanilla's
 * generic {@code ThrownItemRenderer}, the same one snowballs/eggs use.
 */
public class ThrowingDaggerEntity extends ThrowableItemProjectile {
    private static final double WILL_DROP_PER_MAX_HEALTH = 0.25;

    private double damage = 10;

    public ThrowingDaggerEntity(EntityType<? extends ThrowingDaggerEntity> type, Level level) {
        super(type, level);
    }

    public ThrowingDaggerEntity(Level level, LivingEntity thrower) {
        super(BMEntities.THROWING_DAGGER.get(), thrower, level);
    }

    public ThrowingDaggerEntity(Level level, double x, double y, double z) {
        super(BMEntities.THROWING_DAGGER.get(), x, y, z, level);
    }

    /**
     * For subclasses (e.g. {@link ThrowingDaggerSyringeEntity}) that need their own {@link EntityType}
     * instead of the hardcoded {@code BMEntities.THROWING_DAGGER} used by the public constructor above.
     */
    protected ThrowingDaggerEntity(EntityType<? extends ThrowingDaggerEntity> type, Level level, LivingEntity thrower) {
        super(type, thrower, level);
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return BMItems.THROWING_DAGGER.get();
    }

    private ParticleOptions getParticle() {
        ItemStack stack = getItem();
        return !stack.isEmpty() ? new ItemParticleOption(ParticleTypes.ITEM, stack) : ParticleTypes.CRIT;
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
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        Entity entity = hitResult.getEntity();
        Entity owner = getOwner();
        double maxHealthBeforeHit = entity instanceof LivingEntity living ? living.getMaxHealth() : 0;

        entity.hurt(entity.damageSources().thrown(this, owner), (float) damage);

        if (!entity.isAlive() && owner instanceof Player) {
            WorldWillHelper.addWill(level(), entity.blockPosition(), EnumWillType.DEFAULT, maxHealthBeforeHit * WILL_DROP_PER_MAX_HEALTH);
        }

        discard();
    }
}
