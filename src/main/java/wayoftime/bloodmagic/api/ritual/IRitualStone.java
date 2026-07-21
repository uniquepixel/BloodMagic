package wayoftime.bloodmagic.api.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Implemented by a rune-stone block that can report/change which {@link EnumRuneType} it
 * represents for the Master Ritual Stone's multiblock pattern check.
 */
public interface IRitualStone {
    boolean isRuneType(Level level, BlockPos pos, EnumRuneType runeType);

    void setRuneType(Level level, BlockPos pos, EnumRuneType runeType);
}
