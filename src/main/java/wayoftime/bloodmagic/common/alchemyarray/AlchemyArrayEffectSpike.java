package wayoftime.bloodmagic.common.alchemyarray;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.common.blockentity.AlchemyArrayTile;

/**
 * Ported from 1.20.1's {@code AlchemyArrayEffectSpike}: continuous effect, damages any living
 * entity standing on it every tick like a cactus.
 */
public class AlchemyArrayEffectSpike extends AlchemyArrayEffect {
    @Override
    public boolean update(AlchemyArrayTile tile, int ticksActive) {
        return false;
    }

    @Override
    public void onEntityCollidedWithBlock(AlchemyArrayTile array, Level world, BlockPos pos, BlockState state, Entity entity) {
        if (entity instanceof LivingEntity) {
            entity.hurt(entity.damageSources().cactus(), 2);
        }
    }

    @Override
    public AlchemyArrayEffect getNewCopy() {
        return new AlchemyArrayEffectSpike();
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
    }
}
