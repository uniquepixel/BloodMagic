package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import wayoftime.bloodmagic.common.blockentity.TileDungeonSeal;
import wayoftime.bloodmagic.common.item.dungeon.IDungeonKey;
import wayoftime.bloodmagic.util.ChatUtil;

import java.util.List;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.common.block.BlockDungeonSeal}. Right
 * clicking one holding a valid {@code IDungeonKey} item is the ONLY thing that advances the dungeon
 * generation state machine (see {@link TileDungeonSeal#requestRoomFromController}) - there's no
 * tick-based or proximity trigger.
 * <p>
 * Upstream's {@code use()} also had a shortcut here: if this seal's controller link was never set
 * (a broken/degenerate seal placed right next to the return {@code INVERSION_PILLAR}), right-clicking
 * it would instead trigger that pillar's teleport. That's tied to the Inversion Pillar portal system,
 * which isn't ported (see {@code VaultRitual} for the simplified entry/exit this branch uses instead),
 * so that shortcut is dropped here - it's dead code without a pillar block to find.
 */
public class BlockDungeonSeal extends Block implements EntityBlock {
    public BlockDungeonSeal() {
        super(Properties.of().strength(20.0F, 50.0F).requiresCorrectToolForDrops());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileDungeonSeal(pos, state);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hitResult) {
        return use(world, pos, player, player.getItemInHand(InteractionHand.MAIN_HAND));
    }

    @Override
    public net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult result = use(world, pos, player, stack);
        return result == InteractionResult.SUCCESS ? net.minecraft.world.ItemInteractionResult.SUCCESS : net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private InteractionResult use(Level world, BlockPos pos, Player player, ItemStack playerItem) {
        if (!(world.getBlockEntity(pos) instanceof TileDungeonSeal seal) || player.isShiftKeyDown()) {
            return InteractionResult.FAIL;
        }

        int result = seal.requestRoomFromController(player, playerItem);
        if (result == -1 && !playerItem.isEmpty() && playerItem.getItem() instanceof IDungeonKey) {
            // Key didn't work
            ChatUtil.sendChatNoSpam(player, List.of(Component.translatable("tooltip.bloodmagic.incorrectKey")));
            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, world.random.nextFloat() * 0.4F + 0.8F);
        }

        return InteractionResult.SUCCESS;
    }
}
