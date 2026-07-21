package wayoftime.bloodmagic.common.ritual.harvest;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Pluggable hook consulted by {@link wayoftime.bloodmagic.common.ritual.types.HarvestRitual} for
 * every block it scans in range. Ported from 1.20.1's {@code IHarvestHandler}: each registered
 * handler (see {@link HarvestHandlerRegistry}) is asked whether it recognizes a block as a ripe
 * "crop" and, if so, is responsible for both harvesting it (computing the drops) and leaving the
 * block in its "just planted"/regrowing state.
 * <p>
 * The original interface also threaded an owner {@code UUID} through for protection-mod checks
 * (firing {@code BlockEvent.BreakEvent}/{@code EntityPlaceEvent} via a {@code BlockProtectionHelper}).
 * That helper doesn't exist on this branch, and no other ritual here fires protection events either
 * (they all mutate the level directly - see {@code HarvestRitual}, {@code FellingRitual},
 * {@code CrushingRitual}), so handlers on this branch do the same and the parameter is dropped.
 */
public interface IHarvestHandler {

	/**
	 * Tests whether {@code state} at {@code pos} is ready to be harvested by this handler.
	 *
	 * @param level the level
	 * @param pos   the position of {@code state}
	 * @param state the block state being checked
	 * @return true if this handler can harvest the block
	 */
	boolean test(Level level, BlockPos pos, BlockState state);

	/**
	 * Harvests the block: breaks/resets it to its "just planted" state (or removes it, for handlers
	 * whose block regrows on its own, such as vines) and appends the resulting drops to
	 * {@code drops}. Only called after {@link #test} returned true for the same position/state.
	 *
	 * @param level the level
	 * @param pos   the position of {@code state}
	 * @param state the block state being harvested
	 * @param drops the list to append resulting item drops to
	 * @return true if the harvest succeeded and the ritual should count this block as harvested
	 */
	boolean harvest(Level level, BlockPos pos, BlockState state, List<ItemStack> drops);
}
