package wayoftime.bloodmagic.common.entity;

import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import wayoftime.bloodmagic.common.item.BMItems;

/**
 * Full-fidelity port of 1.20.1's Syringe Throwing Dagger projectile ({@code EntityThrowingDaggerSyringe}):
 * behaves exactly like the base {@link ThrowingDaggerEntity} (flat damage, ambient Will drop on a killing
 * blow - see that class's javadoc for why the Will goes into the world aura rather than the thrower's own
 * pool on this branch) but additionally harvests Slate Ampoules from a killing blow, using the same
 * drop-count formula as 1.20.1: one Ampoule per 20 points of the victim's max health, plus a chance-based
 * extra roll for the health remainder.
 */
public class ThrowingDaggerSyringeEntity extends ThrowingDaggerEntity {
    public ThrowingDaggerSyringeEntity(EntityType<? extends ThrowingDaggerSyringeEntity> type, Level level) {
        super(type, level);
    }

    public ThrowingDaggerSyringeEntity(Level level, LivingEntity thrower) {
        super(BMEntities.THROWING_DAGGER_SYRINGE.get(), level, thrower);
    }

    @Override
    protected Item getDefaultItem() {
        return BMItems.THROWING_DAGGER_SYRINGE.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        Entity entity = hitResult.getEntity();
        double maxHealthBeforeHit = entity instanceof LivingEntity living ? living.getMaxHealth() : 0;

        // Handles damage, the ambient Will drop on a kill, particles and discard() - see
        // ThrowingDaggerEntity#onHitEntity.
        super.onHitEntity(hitResult);

        if (maxHealthBeforeHit > 0 && !entity.isAlive()) {
            int count = (int) (maxHealthBeforeHit / 20D) + (level().random.nextDouble() < ((maxHealthBeforeHit % 20D) / 20D) ? 1 : 0);
            if (count > 0) {
                Containers.dropItemStack(level(), entity.getX(), entity.getY(), entity.getZ(), new ItemStack(BMItems.SLATE_AMPOULE.get(), count));
            }
        }
    }
}
