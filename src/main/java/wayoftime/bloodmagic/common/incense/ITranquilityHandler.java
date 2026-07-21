package wayoftime.bloodmagic.common.incense;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of 1.20.1's {@code ITranquilityHandler}: a functional interface returning the
 * {@link TranquilityStack} (if any) contributed by a given block during an Incense Altar scan.
 */
public interface ITranquilityHandler {
    TranquilityStack getTranquilityOfBlock(Level world, BlockPos pos, Block block, BlockState state);
}
