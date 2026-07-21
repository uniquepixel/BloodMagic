package wayoftime.bloodmagic.common.alchemyarray;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.common.blockentity.AlchemyArrayTile;

/**
 * Ported from 1.20.1's {@code AlchemyArrayEffectMovement}: never "completes" (it's a
 * continuous effect, not a craft), and instead launches whatever walks onto it in the array's
 * facing direction. Extra feather/redstone dust stacked in the two input slots boosts the
 * vertical/horizontal speed, exactly like the original.
 */
public class AlchemyArrayEffectMovement extends AlchemyArrayEffect {
    @Override
    public boolean update(AlchemyArrayTile tile, int ticksActive) {
        return false;
    }

    @Override
    public void onEntityCollidedWithBlock(AlchemyArrayTile array, Level world, BlockPos pos, BlockState state, Entity entity) {
        double motionY = 0.5;
        double motionYGlowstoneMod = 0.05;
        double speed = 1.5;
        double speedRedstoneMod = 0.15;

        Direction direction = array.getRotation();

        motionY += motionYGlowstoneMod * (array.getInventory().getStackInSlot(0).getCount() - 1);
        speed += speedRedstoneMod * (array.getInventory().getStackInSlot(1).getCount() - 1);

        entity.fallDistance = 0;

        switch (direction) {
            case NORTH -> entity.setDeltaMovement(new Vec3(0, motionY, -speed));
            case SOUTH -> entity.setDeltaMovement(new Vec3(0, motionY, speed));
            case WEST -> entity.setDeltaMovement(new Vec3(-speed, motionY, 0));
            case EAST -> entity.setDeltaMovement(new Vec3(speed, motionY, 0));
            default -> {
            }
        }
    }

    @Override
    public AlchemyArrayEffect getNewCopy() {
        return new AlchemyArrayEffectMovement();
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
    }
}
