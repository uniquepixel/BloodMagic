package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.common.incense.IIncensePath;

/**
 * Full-fidelity port of 1.20.1's {@code BlockPath}: a plain decorative cube that also doubles as
 * an Incense Altar road block (see {@link IIncensePath} /
 * {@link wayoftime.bloodmagic.common.blockentity.IncenseAltarTile#recheckConstruction}).
 * {@code pathLevel} controls how many rings out from the altar this tier of path can extend the
 * road - 1.20.1 registered four tiers at levels 2/4/6/8 (wood/stone/worn stone/obsidian - see
 * {@code BMBlocks}), each reaching 2 rings further than the last (3/5/7/9 rings out respectively,
 * matching the guidebook - see {@code IncenseAltarHandler}).
 */
public class IncenseAltarPathBlock extends Block implements IIncensePath {
    private final int pathLevel;

    public IncenseAltarPathBlock(int pathLevel, BlockBehaviour.Properties properties) {
        super(properties);
        this.pathLevel = pathLevel;
    }

    @Override
    public int getLevelOfPath(Level world, BlockPos pos, BlockState state) {
        return pathLevel;
    }
}
