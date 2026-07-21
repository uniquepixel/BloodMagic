package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.common.blockentity.BMTiles;
import wayoftime.bloodmagic.common.blockentity.DemonCrucibleTile;
import wayoftime.bloodmagic.util.BlockEntityHelper;

/**
 * Ported from 1.20.1's {@code BlockDemonCrucible}. Right-click with an item to insert it (if it's
 * empty and the item carries Will), right-click empty-handed to withdraw whatever's inside -
 * mirrors {@link wayoftime.bloodmagic.common.block.BloodAltarBlock}'s single-slot click-to-swap
 * interaction, which the original Crucible's own {@code Utils.insertItemToTile} helper predates
 * on this branch.
 */
public class DemonCrucibleBlock extends Block implements EntityBlock {
    public DemonCrucibleBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(2.0F, 5.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DemonCrucibleTile(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return BlockEntityHelper.getTicker(blockEntityType, BMTiles.DEMON_CRUCIBLE_TYPE.get(), DemonCrucibleTile::tick);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof DemonCrucibleTile tile) {
                tile.dropItems();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!(level.getBlockEntity(pos) instanceof DemonCrucibleTile tile)) {
            return ItemInteractionResult.FAIL;
        }

        ItemStackHandler inv = tile.getInventory();
        ItemStack current = inv.getStackInSlot(0);

        if (current.isEmpty() && !stack.isEmpty() && inv.isItemValid(0, stack)) {
            inv.setStackInSlot(0, stack.copy());
            player.setItemInHand(hand, ItemStack.EMPTY);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        } else if (!current.isEmpty() && stack.isEmpty()) {
            player.setItemInHand(hand, current.copy());
            inv.setStackInSlot(0, ItemStack.EMPTY);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
