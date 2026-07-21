package wayoftime.bloodmagic.common.item.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.common.blockentity.TileMimic;

/**
 * Ported from 1.20.1's ItemBlockMimic. Sneak-placing this item against an existing block replaces
 * that block with a Mimic disguised as it (preserving the replaced block's item form and, for
 * containers, its saved NBT in tileTag - see TileMimic). A normal (non-sneak) placement just
 * places a plain, undisguised Mimic like any other block, to be dressed up later via right-click
 * (see BlockMimic#useItemOn / TileMimic#onBlockActivated).
 */
public class MimicBlockItem extends BlockItem {
	public MimicBlockItem(Block block, Properties prop) {
		super(block, prop);
	}

	@Override
	public InteractionResult place(BlockPlaceContext context) {
		Player player = context.getPlayer();

		// No player (e.g. a dispenser) or not sneaking: behave like a normal block item.
		if (player == null || !player.isShiftKeyDown()) {
			return super.place(context);
		}

		ItemStack stack = player.getItemInHand(context.getHand());
		BlockPos pos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
		Level world = context.getLevel();
		Direction direction = context.getClickedFace();

		if (!player.mayUseItemAt(pos, direction, stack)) {
			return InteractionResult.FAIL;
		}

		BlockState replacedBlockstate = world.getBlockState(pos);
		Block replacedBlock = replacedBlockstate.getBlock();
		ItemStack replacedStack = replacedBlock.getCloneItemStack(world, pos, replacedBlockstate);

		BlockState mimicBlockstate = this.getBlock().defaultBlockState();

		if (!canReplaceBlock(world, pos, replacedBlockstate)) {
			return super.place(context);
		}

		BlockEntity tileReplaced = world.getBlockEntity(pos);
		if (!canReplaceTile(tileReplaced)) {
			return InteractionResult.FAIL;
		}

		var tileTag = getTagFromTileEntity(world, tileReplaced);

		stack.shrink(1);

		world.setBlock(pos, mimicBlockstate, 3);
		SoundType soundtype = mimicBlockstate.getSoundType(world, pos, player);
		world.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

		BlockEntity tile = world.getBlockEntity(pos);
		if (tile instanceof TileMimic mimic) {
			mimic.tileTag = tileTag;
			mimic.setMimic(replacedBlockstate);
			mimic.setDisplayStack(replacedStack);

			if (player.isCreative()) {
				mimic.dropItemsOnBreak = false;
			}
		}
		return InteractionResult.SUCCESS;
	}

	public boolean canReplaceTile(BlockEntity tile) {
		if (tile instanceof ChestBlockEntity) {
			return true;
		}

		return tile == null;
	}

	public boolean canReplaceBlock(Level world, BlockPos pos, BlockState state) {
		return state.getDestroySpeed(world, pos) != -1.0F;
	}

	public net.minecraft.nbt.CompoundTag getTagFromTileEntity(Level world, BlockEntity tile) {
		if (tile != null) {
			return tile.saveWithoutMetadata(world.registryAccess());
		}

		return new net.minecraft.nbt.CompoundTag();
	}
}
