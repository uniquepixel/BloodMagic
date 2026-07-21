package wayoftime.bloodmagic.common.alchemyarray;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.common.blockentity.AlchemyArrayTile;

/**
 * Ported from 1.20.1's {@code AlchemyArrayEffectBounce}: continuous effect, reverses downward
 * fall momentum for anything touching it (unless the entity is sneaking, in which case it just
 * resets fall distance without bouncing).
 */
public class AlchemyArrayEffectBounce extends AlchemyArrayEffect {
    @Override
    public boolean update(AlchemyArrayTile tile, int ticksActive) {
        return false;
    }

    @Override
    public void onEntityCollidedWithBlock(AlchemyArrayTile array, Level world, BlockPos pos, BlockState state, Entity entity) {
        if (entity.isShiftKeyDown()) {
            entity.fallDistance = 0;
        } else if (entity.getDeltaMovement().y < 0.0D) {
            Vec3 motion = entity.getDeltaMovement();
            motion = motion.multiply(1, -1, 1);

            if (!(entity instanceof LivingEntity)) {
                motion = motion.multiply(1, 0.8, 1);
            }

            entity.setDeltaMovement(motion);
            entity.fallDistance = 0;
        }
    }

    @Override
    public AlchemyArrayEffect getNewCopy() {
        return new AlchemyArrayEffectBounce();
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
    }
}
