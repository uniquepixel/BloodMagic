package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.will.WorldWillHelper;

/**
 * Full-fidelity port of 1.20.1's Demonic Pylon ({@code BlockDemonPylon}/{@code TileDemonPylon}):
 * every tick, for each Will type, compares this chunk's ambient aura against the aura 16 blocks
 * (one chunk) away in each of the 4 cardinal directions, and if a neighbor holds more than here,
 * pulls half the difference (capped at {@link #TRANSFER_RATE}/tick) from there to here - the exact
 * algorithm 1.20.1 used, letting a field of Pylons slowly concentrate Will from a wide area into
 * one chunk (e.g. one holding a Crystallizer farm).
 * <p>
 * Not ported: the original's {@code IDemonWillConduit} interface - see {@link DemonCrucibleTile}'s
 * javadoc for why. Notably, the original Pylon's own conduit buffer was never actually read or
 * written by its own tick logic (only by other conduits pushing/pulling through the network), so
 * dropping the interface loses nothing this tile's own behavior depended on.
 */
public class DemonPylonTile extends BaseTile {
    private static final double TRANSFER_RATE = 1;

    public DemonPylonTile(BlockPos pos, BlockState state) {
        super(BMTiles.DEMON_PYLON_TYPE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DemonPylonTile tile) {
        if (level.isClientSide) {
            return;
        }

        for (EnumWillType type : EnumWillType.values()) {
            double current = WorldWillHelper.getWill(level, pos, type);

            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos neighborPos = pos.relative(side, 16);
                double neighborAmount = WorldWillHelper.getWill(level, neighborPos, type);
                if (neighborAmount > current) {
                    double moveAmount = Math.min((neighborAmount - current) / 2, TRANSFER_RATE);
                    double drained = WorldWillHelper.drainWill(level, neighborPos, type, moveAmount);
                    WorldWillHelper.addWill(level, pos, type, drained);
                }
            }
        }
    }
}
