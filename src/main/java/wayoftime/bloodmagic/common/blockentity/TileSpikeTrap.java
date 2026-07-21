package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.block.BlockSpikeTrap;

/**
 * Ported from 1.20.1's common/tile/TileSpikeTrap.java. Backs BlockSpikeTrap - every tick, compares its
 * ACTIVE blockstate against the live redstone signal and, on a rising edge, pops a BlockSpikes hazard
 * into the block in front of it (removing it again on a falling edge). See TileDungeonAlternator's
 * javadoc for why this uses the plain static-tick-method convention instead of 1.20.1's TileTicking
 * base class.
 */
public class TileSpikeTrap extends BaseTile {
    public TileSpikeTrap(BlockPos pos, BlockState state) {
        super(BMTiles.SPIKE_TRAP_TYPE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileSpikeTrap tile) {
        if (level.isClientSide) {
            return;
        }

        boolean wasActive = state.getValue(BlockSpikeTrap.ACTIVE);
        BlockPos spikePos = pos.relative(state.getValue(BlockStateProperties.FACING));
        if (wasActive != level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.cycle(BlockSpikeTrap.ACTIVE), 2);
        }

        boolean active = level.getBlockState(pos).getValue(BlockSpikeTrap.ACTIVE);
        if (active) {
            if (level.getBlockState(spikePos).isAir()) {
                level.setBlockAndUpdate(spikePos, BMBlocks.DUNGEON_SPIKES.block().get().defaultBlockState().setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING)));
                level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.3F, 0.6F);
            }
        } else if (level.getBlockState(spikePos).getBlock() == BMBlocks.DUNGEON_SPIKES.block().get()) {
            level.setBlockAndUpdate(spikePos, Blocks.AIR.defaultBlockState());
            level.playSound(null, pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.3F, 0.6F);
        }
    }
}
