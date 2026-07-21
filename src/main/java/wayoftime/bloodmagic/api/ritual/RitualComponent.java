package wayoftime.bloodmagic.api.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Binds an {@link EnumRuneType} to an offset from the Master Ritual Stone, for use in a ritual's
 * multiblock rune pattern (see {@link Ritual#gatherComponents}).
 */
public class RitualComponent {
    private final BlockPos offset;
    private final EnumRuneType runeType;

    public RitualComponent(BlockPos offset, EnumRuneType runeType) {
        this.offset = offset;
        this.runeType = runeType;
    }

    public int getX(Direction direction) {
        return switch (direction) {
            case EAST -> -this.getOffset().getZ();
            case SOUTH -> -this.getOffset().getX();
            case WEST -> this.getOffset().getZ();
            default -> this.getOffset().getX();
        };
    }

    public int getZ(Direction direction) {
        return switch (direction) {
            case EAST -> this.getOffset().getX();
            case SOUTH -> -this.getOffset().getZ();
            case WEST -> -this.getOffset().getX();
            default -> this.getOffset().getZ();
        };
    }

    public BlockPos getOffset(Direction direction) {
        return new BlockPos(getX(direction), offset.getY(), getZ(direction));
    }

    public BlockPos getOffset() {
        return offset;
    }

    public EnumRuneType getRuneType() {
        return runeType;
    }
}
