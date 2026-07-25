package wayoftime.bloodmagic.common.incense;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

/**
 * Full-fidelity port of 1.20.1's {@code IncenseAltarComponent}: describes one block of a
 * physical "altar-tier" structure an addon could register via
 * {@link IncenseAltarHandler#registerIncenseComponent}, offset from and rotatable around the
 * altar. Nothing in 1.20.1 itself (nor this port) ever calls {@code registerIncenseComponent} -
 * the hook exists (matching upstream) but is unused, so
 * {@link IncenseAltarHandler#getMaxIncenseBonusFromComponents} always reports every tier
 * satisfied. Kept for parity/addon-compatibility rather than functional necessity - see
 * {@link IncenseAltarHandler}'s javadoc.
 */
public class IncenseAltarComponent {
    public final BlockPos offsetPos;
    public final Block block;

    public IncenseAltarComponent(BlockPos offsetPos, Block block) {
        this.offsetPos = offsetPos;
        this.block = block;
    }

    public boolean doesBlockMatch(Block block) {
        return this.block == block;
    }

    /**
     * Base rotation is north.
     */
    public BlockPos getOffset(Direction rotation) {
        return new BlockPos(this.getX(rotation), offsetPos.getY(), this.getZ(rotation));
    }

    public int getX(Direction direction) {
        return switch (direction) {
            case EAST -> -this.offsetPos.getZ();
            case SOUTH -> -this.offsetPos.getX();
            case WEST -> this.offsetPos.getZ();
            default -> this.offsetPos.getX();
        };
    }

    public int getZ(Direction direction) {
        return switch (direction) {
            case EAST -> this.offsetPos.getX();
            case SOUTH -> -this.offsetPos.getZ();
            case WEST -> -this.offsetPos.getX();
            default -> this.offsetPos.getZ();
        };
    }
}
