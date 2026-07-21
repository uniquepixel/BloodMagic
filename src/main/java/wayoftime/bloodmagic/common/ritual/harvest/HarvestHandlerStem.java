package wayoftime.bloodmagic.common.ritual.harvest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Harvest handler for stem-and-fruit crops (pumpkin/melon). Ported from 1.20.1's
 * {@code HarvestHandlerStem}: instead of matching against a registered stem-to-fruit
 * {@code BlockState} multimap (needed there so addon crops could register their own stem/fruit
 * pairs), this checks the two vanilla pairs directly, since there's no addon-facing registry on this
 * branch. Breaks the fruit block the {@link AttachedStemBlock} is pointing at; the stem reverts to an
 * unattached {@code StemBlock} on its own via vanilla's neighbor-update handling once the fruit is
 * gone, then regrows a new one over time.
 */
public class HarvestHandlerStem implements IHarvestHandler {
	private static final ItemStack MOCK_HOE = new ItemStack(Items.DIAMOND_HOE);

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return state.getBlock() instanceof AttachedStemBlock;
	}

	@Override
	public boolean harvest(Level level, BlockPos pos, BlockState state, List<ItemStack> drops) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return false;
		}

		Block stem = state.getBlock();
		Block expectedFruit = stem == Blocks.ATTACHED_PUMPKIN_STEM ? Blocks.PUMPKIN
				: stem == Blocks.ATTACHED_MELON_STEM ? Blocks.MELON
				: null;
		if (expectedFruit == null) {
			return false;
		}

		Direction facing = state.getValue(AttachedStemBlock.FACING);
		BlockPos fruitPos = pos.relative(facing);
		BlockState fruitState = level.getBlockState(fruitPos);
		if (fruitState.getBlock() != expectedFruit) {
			return false;
		}

		List<ItemStack> blockDrops = Block.getDrops(fruitState, serverLevel, fruitPos, level.getBlockEntity(fruitPos), null, MOCK_HOE);
		level.destroyBlock(fruitPos, false);
		drops.addAll(blockDrops);
		return true;
	}
}
