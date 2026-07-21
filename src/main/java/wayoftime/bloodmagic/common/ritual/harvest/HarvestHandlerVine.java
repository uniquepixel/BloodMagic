package wayoftime.bloodmagic.common.ritual.harvest;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Harvest handler for the classic hanging {@link Blocks#VINE} block (the kind found dangling off
 * jungle leaves), as opposed to the newer stacking {@link net.minecraft.world.level.block.GrowingPlantHeadBlock}
 * family handled by {@link HarvestHandlerGrowingPlant}. Ported from 1.20.1's
 * {@code HarvestHandlerJungleVines}: only harvests the bottom-most vine of a hanging strand (one with
 * another vine block directly above it), shearing it off so the strand can regrow downward again.
 */
public class HarvestHandlerVine implements IHarvestHandler {
	private static final ItemStack MOCK_SHEARS = new ItemStack(Items.SHEARS);

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return state.is(Blocks.VINE) && level.getBlockState(pos.above()).is(Blocks.VINE);
	}

	@Override
	public boolean harvest(Level level, BlockPos pos, BlockState state, List<ItemStack> drops) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return false;
		}

		List<ItemStack> blockDrops = Block.getDrops(state, serverLevel, pos, level.getBlockEntity(pos), null, MOCK_SHEARS);
		level.destroyBlock(pos, false);
		drops.addAll(blockDrops);
		return true;
	}
}
