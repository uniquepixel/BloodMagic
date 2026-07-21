package wayoftime.bloodmagic.common.alchemyarray;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.common.blockentity.AlchemyArrayTile;

/**
 * Full-fidelity port of 1.20.1's {@code AlchemyArrayEffect}. One instance is attached to an
 * {@link AlchemyArrayTile} once its two input slots satisfy an {@code AlchemyArrayRecipe}, and
 * drives whatever happens while the array stays active - either a slow "craft into a dropped
 * item" process (see {@link AlchemyArrayEffectCrafting} and {@link AlchemyArrayEffectBinding}),
 * or a continuous/triggered effect that never produces an item on its own (movement, updraft,
 * spike, bounce, day, night).
 */
public abstract class AlchemyArrayEffect {
    public abstract AlchemyArrayEffect getNewCopy();

    public abstract void readFromNBT(CompoundTag compound);

    public abstract void writeToNBT(CompoundTag compound);

    /**
     * Called once per tick while the array is active.
     *
     * @return {@code true} once the effect has finished, at which point the array consumes one
     * of each ingredient and removes itself.
     */
    public abstract boolean update(AlchemyArrayTile array, int activeCounter);

    /**
     * Called for every entity standing on/inside the array's collision box this tick.
     */
    public void onEntityCollidedWithBlock(AlchemyArrayTile array, Level world, BlockPos pos, BlockState state, Entity entity) {
    }
}
