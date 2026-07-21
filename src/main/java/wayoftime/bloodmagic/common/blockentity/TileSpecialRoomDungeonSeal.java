package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.structures.DungeonRoom;
import wayoftime.bloodmagic.structures.DungeonRoomRegistry;
import wayoftime.bloodmagic.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Full-fidelity port of 1.20.1's
 * {@code wayoftime.bloodmagic.common.tile.TileSpecialRoomDungeonSeal}: a {@link TileDungeonSeal}
 * that instead of picking a random room from a pool, has a single specific
 * {@link DungeonRoom}/rotation/location pre-chosen for it (see
 * {@link #acceptSpecificDoorInformation}, called from
 * {@code DungeonSynthesizer#spawnDoorBlock}/{@code checkRequiredRoom}) - used for things like
 * guaranteed mine-key/mine-entrance rooms.
 */
public class TileSpecialRoomDungeonSeal extends TileDungeonSeal {
    ResourceLocation chosenRoom = BloodMagic.rl("empty");
    BlockPos roomLocation = BlockPos.ZERO;
    Rotation rotation = Rotation.NONE;

    public TileSpecialRoomDungeonSeal(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileSpecialRoomDungeonSeal(BlockPos pos, BlockState state) {
        this(BMTiles.SPECIAL_DUNGEON_SEAL_TYPE.get(), pos, state);
    }

    public void acceptSpecificDoorInformation(ServerLevel world, BlockPos controllerPos, ResourceLocation specialRoomType, Direction doorFacing, BlockPos activatedDoorPos, String activatedDoorType, int roomDepth, int highestBranchRoomDepth, DungeonRoom room, Rotation rotation, BlockPos roomLocation) {
        this.chosenRoom = room.key;
        this.roomLocation = roomLocation;
        this.rotation = rotation;
        List<ResourceLocation> roomPools = new ArrayList<>();
        roomPools.add(specialRoomType);
        this.acceptDoorInformation(controllerPos, activatedDoorPos, doorFacing, activatedDoorType, roomDepth, highestBranchRoomDepth, roomPools);
    }

    @Override
    public int requestRoomFromController(Player player, ItemStack heldStack) {
        if (level != null && !level.isClientSide && !potentialRoomTypes.isEmpty()) {
            BlockEntity tile = level.getBlockEntity(controllerPos);
            if (tile instanceof TileDungeonController tileController) {
                DungeonRoom room = DungeonRoomRegistry.getDungeonRoom(chosenRoom);
                if (room == null) {
                    return -1;
                }

                int state = tileController.handleRequestForPredesignatedRoomPlacement(player, heldStack, doorPos, doorDirection, doorType, activatedRoomDepth, highestBranchRoomDepth, potentialRoomTypes, room, rotation, roomLocation);

                if (state == -1) {
                    return -1;
                }
            }
        }

        return 3;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        CompoundTag masterTag = tag.getCompound(Constants.NBT.ROOM_LOCATION);
        roomLocation = new BlockPos(masterTag.getInt(Constants.NBT.X_COORD), masterTag.getInt(Constants.NBT.Y_COORD), masterTag.getInt(Constants.NBT.Z_COORD));

        chosenRoom = ResourceLocation.parse(tag.getString(Constants.NBT.ROOM_NAME));

        rotation = Rotation.values()[Math.max(0, Math.min(3, tag.getInt(Constants.NBT.ROTATION)))];
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        CompoundTag roomTag = new CompoundTag();
        roomTag.putInt(Constants.NBT.X_COORD, roomLocation.getX());
        roomTag.putInt(Constants.NBT.Y_COORD, roomLocation.getY());
        roomTag.putInt(Constants.NBT.Z_COORD, roomLocation.getZ());
        tag.put(Constants.NBT.ROOM_LOCATION, roomTag);

        tag.putString(Constants.NBT.ROOM_NAME, chosenRoom.toString());

        tag.putInt(Constants.NBT.ROTATION, rotation.ordinal());
    }
}
