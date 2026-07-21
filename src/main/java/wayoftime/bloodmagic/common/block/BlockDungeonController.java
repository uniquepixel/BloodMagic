package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import wayoftime.bloodmagic.common.blockentity.TileDungeonController;
import wayoftime.bloodmagic.util.Constants;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.common.block.BlockDungeonController},
 * plus one addition: upstream's return trip out of a dungeon was via a second, paired
 * {@code INVERSION_PILLAR} portal block placed back at the entrance room (see
 * {@code TileInversionPillar}) - that whole bidirectional-portal subsystem isn't ported (see
 * {@code VaultRitual}). Right-clicking the controller block itself (which every dungeon has exactly
 * one of, at its entrance room) now serves as the simplified return trip instead, teleporting the
 * player back to the position {@code VaultRitual} stashed in their persistent data before sending
 * them in.
 */
public class BlockDungeonController extends Block implements EntityBlock {
    public BlockDungeonController() {
        super(Properties.of().strength(20.0F, 50.0F).requiresCorrectToolForDrops());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileDungeonController(pos, state);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer) || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(Constants.NBT.DUNGEON_EXIT)) {
            return InteractionResult.PASS;
        }

        CompoundTag exit = persistentData.getCompound(Constants.NBT.DUNGEON_EXIT);
        ResourceKey<Level> dimension = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, ResourceLocation.parse(exit.getString("dimension_key")));
        ServerLevel destination = serverPlayer.server.getLevel(dimension);
        if (destination == null) {
            return InteractionResult.PASS;
        }

        persistentData.remove(Constants.NBT.DUNGEON_EXIT);
        serverPlayer.teleportTo(destination, exit.getInt("xCoord") + 0.5, exit.getInt("yCoord"), exit.getInt("zCoord") + 0.5, serverPlayer.getYRot(), serverPlayer.getXRot());

        return InteractionResult.SUCCESS;
    }
}
