package wayoftime.bloodmagic.common.ritual.harvest;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Harvest handler for {@link SweetBerryBushBlock} at max age. Full-fidelity port of 1.20.1's
 * {@code HarvestHandlerBerryBush}, including its quirk: berries are popped directly at the bush's
 * position via {@link Block#popResource} rather than appended to the {@code drops} list, so (same as
 * upstream) they bypass the ritual's above-controller inventory-insertion step that every other
 * handler's drops go through.
 */
public class HarvestHandlerBerryBush implements IHarvestHandler {

	@Override
	public boolean test(Level level, BlockPos pos, BlockState state) {
		return state.getBlock() instanceof SweetBerryBushBlock && state.getValue(SweetBerryBushBlock.AGE) >= 3;
	}

	@Override
	public boolean harvest(Level level, BlockPos pos, BlockState state, List<ItemStack> drops) {
		BlockState newState = state.setValue(SweetBerryBushBlock.AGE, 1);
		level.setBlockAndUpdate(pos, newState);

		int berries = 2 + level.random.nextInt(2);
		Block.popResource(level, pos, new ItemStack(Items.SWEET_BERRIES, berries));
		level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);

		return true;
	}
}
