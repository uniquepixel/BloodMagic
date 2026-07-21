package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import wayoftime.bloodmagic.common.blockentity.TileMimic;

/**
 * Ported from 1.20.1's BlockMimic. A Mimic is not a monster in disguise (despite the name evoking
 * one) - it's a purely cosmetic "disguise" block that can take on the appearance of any other
 * block via its {@link TileMimic}'s dynamic baked model (see client/model/mimic). Sneak-placing
 * this item against an existing block (see MimicBlockItem) swaps that block for a Mimic wearing
 * its old appearance; right-clicking an already-placed, undisguised Mimic with a BlockItem in hand
 * does the same. 1.20.1's guidebook describes the intangible ETHEREAL_MIMIC variant (this same
 * class, registered a second time with noCollission()) as dungeon-generated camouflage for pitfall
 * traps and secret passages.
 */
public class BlockMimic extends Block implements EntityBlock {
	private static final VoxelShape SHAPE = Shapes.box(0.01, 0, 0.01, 0.99, 1, 0.99);

	public BlockMimic(Properties prop) {
		super(prop);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
		BlockEntity te = reader.getBlockEntity(pos);
		if (te instanceof TileMimic mimicTile) {
			BlockState mimic = mimicTile.getMimic();
			if (mimic != null && !(mimic.getBlock() instanceof BlockMimic)) {
				return mimic.getShape(reader, pos, context);
			}
		}
		return SHAPE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileMimic(pos, state);
	}

	// Only useItemOn is overridden (not useWithoutItem) so the full activation logic - which branches
	// internally on whether a held item is present - runs exactly once per right-click regardless of
	// hand contents; useItemOn is always tried first by vanilla's dispatch and only falls through to
	// useWithoutItem when it returns PASS_TO_DEFAULT_BLOCK_INTERACTION, which this never does.
	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof TileMimic mimic && mimic.onBlockActivated(world, pos, state, player, hand, stack, trace.getDirection())) {
			return ItemInteractionResult.sidedSuccess(world.isClientSide);
		}
		return ItemInteractionResult.FAIL;
	}
}
