package wayoftime.bloodmagic.common.ritual.harvest;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Harvest handler for vanilla {@link CropBlock}s at max age (wheat, carrots, potatoes, beetroot).
 * Ported from 1.20.1's {@code HarvestHandlerPlantable}: computes the block's drops with a mock hoe,
 * and only actually harvests if one of those drops is a seed for the crop itself (a {@link BlockItem}
 * placing the same block) - consuming one of that seed to replant at age 0, matching vanilla's
 * "you need a seed in hand to replant" expectation and preserving the crop untouched on the rare
 * unlucky pulse where no seed drops.
 * <p>
 * The original handler also reflectively registered crops from HarvestCraft, Actually Additions,
 * Extra Utilities 2, Roots, and Mystical Agriculture, plus Blood Magic's own creeping-doubt/tau crop
 * blocks. None of those mods are dependencies of this branch, and the Blood Magic crop blocks haven't
 * been ported here yet, so only the four vanilla crops are registered.
 */
public class HarvestHandlerCrop implements IHarvestHandler {
	private static final ItemStack MOCK_HOE = new ItemStack(Items.DIAMOND_HOE);

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state);
	}

	@Override
	public boolean harvest(Level level, BlockPos pos, BlockState state, List<ItemStack> drops) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return false;
		}

		CropBlock crop = (CropBlock) state.getBlock();
		List<ItemStack> blockDrops = Block.getDrops(state, serverLevel, pos, level.getBlockEntity(pos), null, MOCK_HOE);

		boolean foundSeed = false;
		for (ItemStack stack : blockDrops) {
			if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == state.getBlock()) {
				stack.shrink(1);
				foundSeed = true;
				break;
			}
		}

		if (!foundSeed) {
			return false;
		}

		level.setBlockAndUpdate(pos, crop.getStateForAge(0));
		level.levelEvent(2001, pos, Block.getId(state));
		drops.addAll(blockDrops);
		return true;
	}
}
