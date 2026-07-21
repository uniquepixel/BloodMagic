package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.common.blockentity.AlchemyArrayTile;
import wayoftime.bloodmagic.common.blockentity.BMTiles;
import wayoftime.bloodmagic.util.BlockEntityHelper;

/**
 * Full-fidelity port of 1.20.1's {@code BlockAlchemyArray}. Ground decal that hosts an
 * {@link AlchemyArrayTile}: right-clicking with an item places it into the tile's base slot,
 * then the next right-click (with a different or matching item) places the added/catalyst item
 * and kicks off {@link AlchemyArrayTile#attemptCraft()}. Entities standing on the array are
 * routed through the active effect every tick (used by the Movement/Updraft/Spike/Bounce
 * effects); breaking the array (or it consuming itself on craft completion) drops whatever is
 * still sitting in its two slots.
 * <p>
 * Not ported: 1.20.1's "Arcane Ashes" item, the only way to rotate a placed array so Movement
 * pushes a different direction - it doesn't exist on this branch and adding it is out of scope
 * for this pass (would require touching {@code BMItems.java}). Arrays here always face the
 * tile's default rotation ({@code Direction.from2DDataValue(0)}), same as a freshly-placed,
 * never-rotated array in 1.20.1.
 */
public class AlchemyArrayBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 1, 16);

    public AlchemyArrayBlock() {
        super(
                BlockBehaviour.Properties.of()
                        .noCollission()
                        .instabreak()
                        .sound(net.minecraft.world.level.block.SoundType.STONE)
        );
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlchemyArrayTile(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return BlockEntityHelper.getTicker(type, BMTiles.ALCHEMY_ARRAY_TYPE.get(), AlchemyArrayTile::tick);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.getBlockEntity(pos) instanceof AlchemyArrayTile array) {
            array.onEntityCollidedWithBlock(state, entity);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown() || !(level.getBlockEntity(pos) instanceof AlchemyArrayTile)) {
            return InteractionResult.FAIL;
        }

        // Empty-hand click is intentionally a no-op here, matching 1.20.1: BlockAlchemyArray#use
        // only ever touched the array's inventory inside an "if (!playerItem.isEmpty())" guard,
        // so right-clicking with an empty hand never actually returned/cleared placed ingredients.
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isShiftKeyDown() || heldStack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!(level.getBlockEntity(pos) instanceof AlchemyArrayTile array)) {
            return ItemInteractionResult.FAIL;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.sidedSuccess(true);
        }

        ItemStackHandler inv = array.getInventory();
        if (inv.getStackInSlot(AlchemyArrayTile.BASE_SLOT).isEmpty()) {
            placeOne(inv, AlchemyArrayTile.BASE_SLOT, heldStack);
        } else {
            if (inv.getStackInSlot(AlchemyArrayTile.ADDED_SLOT).isEmpty()) {
                placeOne(inv, AlchemyArrayTile.ADDED_SLOT, heldStack);
            }
            array.attemptCraft();
        }

        level.sendBlockUpdated(pos, state, state, 3);
        return ItemInteractionResult.sidedSuccess(false);
    }

    private static void placeOne(ItemStackHandler inv, int slot, ItemStack heldStack) {
        inv.setStackInSlot(slot, heldStack.copyWithCount(1));
        heldStack.shrink(1);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof AlchemyArrayTile array) {
                BlockEntityHelper.dropContents(level, pos, array.getInventory());
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
