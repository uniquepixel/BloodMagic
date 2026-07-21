package wayoftime.bloodmagic.common.ritual.harvest;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Harvest handler for the "growing tip" of stacking plants that regrow on their own - cave vines
 * (with or without glow berries), weeping vines, twisting vines, and kelp all share the vanilla
 * {@link GrowingPlantHeadBlock}/{@link GrowingPlantBodyBlock} pair. Ported from 1.20.1's
 * {@code HarvestHandlerGrowingPlant}: breaks the head once it has at least one body segment attached,
 * letting the body regrow a new tip over time.
 * <p>
 * The original read the head's growth direction off {@code GrowingPlantBlock#growthDirection}
 * directly (a protected vanilla field); that's not exposed as public API on this branch/modloader
 * (no access-transformer entry widens it), so instead this checks both the block above and below for
 * a body segment - equivalent in practice, since a head block only ever has a body neighbor on the
 * side it grew from (up for kelp/twisting vines, down for cave vines/weeping vines).
 */
public class HarvestHandlerGrowingPlant implements IHarvestHandler {
	private static final ItemStack MOCK_HOE = new ItemStack(Items.DIAMOND_HOE);

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		if (!(state.getBlock() instanceof GrowingPlantHeadBlock)) {
			return false;
		}

		return level.getBlockState(pos.above()).getBlock() instanceof GrowingPlantBodyBlock
				|| level.getBlockState(pos.below()).getBlock() instanceof GrowingPlantBodyBlock;
	}

	@Override
	public boolean harvest(Level level, BlockPos pos, BlockState state, List<ItemStack> drops) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return false;
		}

		List<ItemStack> blockDrops = Block.getDrops(state, serverLevel, pos, level.getBlockEntity(pos), null, MOCK_HOE);
		level.destroyBlock(pos, false);
		drops.addAll(blockDrops);
		return true;
	}
}
