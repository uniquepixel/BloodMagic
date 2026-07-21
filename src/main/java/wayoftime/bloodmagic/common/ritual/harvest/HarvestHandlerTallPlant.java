package wayoftime.bloodmagic.common.ritual.harvest;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;

import java.util.List;
import java.util.Set;

/**
 * Harvest handler for column-growing plants that stack multiple blocks vertically (sugar cane,
 * cactus, bamboo). Ported from 1.20.1's {@code HarvestHandlerTall}: only matches a block whose state
 * is exactly one of the registered "freshly grown" base states (age/stage 0), and only harvests if
 * another block of the same type is stacked directly above it - in which case the block above is
 * broken and this position's own state supplies the drops, leaving this block as the new top of the
 * column so it can grow back.
 */
public class HarvestHandlerTallPlant implements IHarvestHandler {
	private static final ItemStack MOCK_HOE = new ItemStack(Items.DIAMOND_HOE);

	private static final Set<BlockState> TALL_PLANT_STATES = Set.of(
			Blocks.SUGAR_CANE.defaultBlockState().setValue(SugarCaneBlock.AGE, 0),
			Blocks.CACTUS.defaultBlockState().setValue(CactusBlock.AGE, 0),
			Blocks.BAMBOO.defaultBlockState()
					.setValue(BambooStalkBlock.STAGE, 0)
					.setValue(BambooStalkBlock.AGE, 1)
					.setValue(BambooStalkBlock.LEAVES, BambooLeaves.NONE));

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return TALL_PLANT_STATES.contains(state);
	}

	@Override
	public boolean harvest(Level level, BlockPos pos, BlockState state, List<ItemStack> drops) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return false;
		}

		BlockPos abovePos = pos.above();
		BlockState aboveState = level.getBlockState(abovePos);
		if (aboveState.getBlock() != state.getBlock()) {
			return false;
		}

		List<ItemStack> blockDrops = Block.getDrops(state, serverLevel, pos, level.getBlockEntity(pos), null, MOCK_HOE);
		level.destroyBlock(abovePos, false);
		drops.addAll(blockDrops);
		return true;
	}
}
