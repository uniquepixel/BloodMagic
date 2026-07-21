package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ported from 1.20.1's {@code TileFungalCharge}: identical floodfill to {@link VeinMineChargeTile},
 * just chasing huge-mushroom cap/stem blocks specifically rather than "same block as whatever
 * started it" - the original achieved this the same way, by extending its vein-mine tile and only
 * overriding the two validity checks.
 * <p>
 * Not ported: the original's {@code BloodMagicTags.Blocks.MUSHROOM_HYPHAE}/{@code MUSHROOM_STEM}
 * tags (grouping red/brown mushroom blocks together) - this branch has no such tags yet, so the 3
 * vanilla mushroom-growth blocks are checked directly instead.
 */
public class FungalChargeTile extends VeinMineChargeTile {
    public FungalChargeTile(BlockPos pos, BlockState state, int maxBlocks) {
        super(BMTiles.FUNGAL_CHARGE_TYPE.get(), pos, state, maxBlocks);
    }

    private static boolean isMushroomGrowth(BlockState state) {
        return state.is(Blocks.RED_MUSHROOM_BLOCK) || state.is(Blocks.BROWN_MUSHROOM_BLOCK) || state.is(Blocks.MUSHROOM_STEM);
    }

    @Override
    protected boolean isValidBlock(BlockState original, BlockState candidate) {
        return isMushroomGrowth(candidate);
    }

    @Override
    protected boolean isValidStartingBlock(Level level, BlockPos pos, BlockState state) {
        return isMushroomGrowth(state);
    }

    @Override
    protected boolean checkDiagonals() {
        return true;
    }
}
