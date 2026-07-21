package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.api.datacomponent.Binding;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.common.blockentity.BMTiles;
import wayoftime.bloodmagic.common.blockentity.MasterRitualStoneTile;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.item.ItemActivationCrystal;
import wayoftime.bloodmagic.common.item.RitualDivinerItem;
import wayoftime.bloodmagic.common.ritual.RitualHelper;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.util.BlockEntityHelper;

public class MasterRitualStoneBlock extends Block implements EntityBlock {
    public MasterRitualStoneBlock() {
        super(
                BlockBehaviour.Properties.of()
                        .requiresCorrectToolForDrops()
                        .strength(2, 5)
                        .sound(SoundType.STONE)
        );
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof MasterRitualStoneTile tile)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (heldItem.getItem() instanceof ItemActivationCrystal) {
            if (!level.isClientSide) {
                activateWithCrystal(heldItem, level, pos, player, tile);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (heldItem.getItem() instanceof RitualDivinerItem diviner) {
            if (!level.isClientSide) {
                diviner.handleUseOnMasterRitualStone(heldItem, level, pos, player, tile);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private void activateWithCrystal(ItemStack heldItem, Level level, BlockPos pos, Player player, MasterRitualStoneTile tile) {
        Binding binding = heldItem.getOrDefault(BMDataComponents.BINDING, Binding.EMPTY);
        if (binding.isEmpty()) {
            binding = new Binding(player.getUUID(), player.getGameProfile().getName());
            heldItem.set(BMDataComponents.BINDING, binding);
        }

        ResourceLocation ritualId = RitualHelper.getValidRitual(level, pos);
        Ritual ritual = ritualId == null ? null : RitualRegistry.get(ritualId);
        if (ritual == null) {
            player.displayClientMessage(Component.translatable("chat.bloodmagic.ritual.notValid"), true);
            return;
        }

        Direction direction = RitualHelper.getDirectionOfRitual(level, pos, ritual);
        if (direction == null) {
            player.displayClientMessage(Component.translatable("chat.bloodmagic.ritual.notValid"), true);
            return;
        }

        if (tile.activateRitual(heldItem, player, ritual)) {
            tile.setDirection(direction);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof MasterRitualStoneTile tile) {
                tile.stopRitual(Ritual.BreakType.BREAK_MRS);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (level.getBlockEntity(pos) instanceof MasterRitualStoneTile tile) {
            tile.stopRitual(Ritual.BreakType.EXPLOSION);
        }
        super.wasExploded(level, pos, explosion);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MasterRitualStoneTile(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return BlockEntityHelper.getTicker(blockEntityType, BMTiles.MASTER_RITUAL_STONE_TYPE.get(), MasterRitualStoneTile::tick);
    }
}
