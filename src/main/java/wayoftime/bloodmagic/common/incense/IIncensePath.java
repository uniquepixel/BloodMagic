package wayoftime.bloodmagic.common.incense;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Full-fidelity port of 1.20.1's {@code IIncensePath} (from {@code api.compat}): any block
 * implementing this is a valid "road" block for an Incense Altar - see
 * {@link wayoftime.bloodmagic.common.blockentity.IncenseAltarTile#recheckConstruction} for how the
 * road is walked ring-by-ring outward from the altar, and {@link IncenseAltarHandler} for how the
 * furthest reachable ring caps the tranquility bonus.
 */
public interface IIncensePath {
    /**
     * Goes from 0 to however far this path block can be from the altar while still functioning. 0
     * represents a block that can work when it is two blocks horizontally away from the altar (ring
     * "distance 2" - the closest ring the road-walk ever checks).
     */
    int getLevelOfPath(Level world, BlockPos pos, BlockState state);
}
