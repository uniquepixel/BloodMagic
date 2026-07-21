package wayoftime.bloodmagic.common.ritual.harvest;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Harvest handler for {@link NetherWartBlock} at max age. Full-fidelity port of 1.20.1's
 * {@code HarvestHandlerNetherWart}: same seed-consuming replant logic as {@link HarvestHandlerCrop}
 * (nether wart isn't a {@link net.minecraft.world.level.block.CropBlock}, so it gets its own
 * handler), just resetting to the block's default state (age 0) instead of using
 * {@code CropBlock#getStateForAge}.
 */
public class HarvestHandlerNetherWart implements IHarvestHandler {
	private static final ItemStack MOCK_HOE = new ItemStack(Items.DIAMOND_HOE);

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return state.getBlock() instanceof NetherWartBlock && state.getValue(NetherWartBlock.AGE) >= 3;
	}

	@Override
	public boolean harvest(Level level, BlockPos pos, BlockState state, List<ItemStack> drops) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return false;
		}

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

		level.setBlockAndUpdate(pos, state.getBlock().defaultBlockState());
		level.levelEvent(2001, pos, Block.getId(state));
		drops.addAll(blockDrops);
		return true;
	}
}
