package wayoftime.bloodmagic.common.alchemyarray;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.common.blockentity.AlchemyArrayTile;

/**
 * Ported from 1.20.1's {@code AlchemyArrayEffectUpdraft}: continuous effect that launches
 * anything standing on it straight upward, boosted by extra glowstone dust/feathers stacked in
 * the input slots.
 */
public class AlchemyArrayEffectUpdraft extends AlchemyArrayEffect {
    @Override
    public boolean update(AlchemyArrayTile tile, int ticksActive) {
        return false;
    }

    @Override
    public void onEntityCollidedWithBlock(AlchemyArrayTile array, Level world, BlockPos pos, BlockState state, Entity entity) {
        double motionY = 1;
        double motionYGlowstoneMod = 0.1;
        double motionYFeatherMod = 0.05;

        motionY += motionYGlowstoneMod * (array.getInventory().getStackInSlot(0).getCount() - 1); // Glowstone Dust
        motionY += motionYFeatherMod * (array.getInventory().getStackInSlot(1).getCount() - 1); // Feathers

        entity.fallDistance = 0;
        entity.setDeltaMovement(new Vec3(0, motionY, 0));
    }

    @Override
    public AlchemyArrayEffect getNewCopy() {
        return new AlchemyArrayEffectUpdraft();
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
    }
}
