package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.util.ChatUtil;
import wayoftime.bloodmagic.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.common.tile.TileDungeonSeal}: a dumb
 * NBT holder placed at every generated door, populated once via {@link #acceptDoorInformation} when
 * the door is created. The state machine only advances when a player right-clicks the seal holding
 * a valid {@code IDungeonKey} item (see {@code BlockDungeonSeal#use}) - there's no tick or proximity
 * trigger. Adapted to this branch's {@code saveAdditional}/{@code loadAdditional} convention.
 */
public class TileDungeonSeal extends BaseTile {
    public BlockPos controllerPos = BlockPos.ZERO;
    public BlockPos doorPos = BlockPos.ZERO;
    public Direction doorDirection = Direction.NORTH;
    public String doorType = "";
    public int activatedRoomDepth;
    public int highestBranchRoomDepth;

    public List<ResourceLocation> potentialRoomTypes = new ArrayList<>();

    public TileDungeonSeal(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileDungeonSeal(BlockPos pos, BlockState state) {
        this(BMTiles.DUNGEON_SEAL_TYPE.get(), pos, state);
    }

    public int requestRoomFromController(Player player, ItemStack heldStack) {
        if (level != null && !level.isClientSide && !potentialRoomTypes.isEmpty()) {
            BlockEntity tile = level.getBlockEntity(controllerPos);
            if (tile instanceof TileDungeonController tileController) {
                int state = tileController.handleRequestForRoomPlacement(player, heldStack, doorPos, doorDirection, doorType, activatedRoomDepth, highestBranchRoomDepth, potentialRoomTypes);

                if (state == -1) {
                    return -1;
                }

                if (state == 2 && player != null) {
                    ChatUtil.sendChatNoSpam(player, List.of(Component.translatable("tooltip.bloodmagic.blockeddoor")));
                    level.setBlock(worldPosition, BMBlocks.DUNGEON_TILE_SPECIAL.block().get().defaultBlockState(), 3);
                    level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F);
                }
            }
        }

        return 3;
    }

    public void acceptDoorInformation(BlockPos controllerPos, BlockPos doorPos, Direction doorDirection, String doorType, int activatedRoomDepth, int highestBranchRoomDepth, List<ResourceLocation> potentialRoomTypes) {
        this.controllerPos = controllerPos;
        this.doorPos = doorPos;
        this.doorDirection = doorDirection;
        this.doorType = doorType;
        this.potentialRoomTypes = potentialRoomTypes;
        this.activatedRoomDepth = activatedRoomDepth;
        this.highestBranchRoomDepth = highestBranchRoomDepth;
        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        CompoundTag masterTag = tag.getCompound(Constants.NBT.DUNGEON_CONTROLLER);
        controllerPos = new BlockPos(masterTag.getInt(Constants.NBT.X_COORD), masterTag.getInt(Constants.NBT.Y_COORD), masterTag.getInt(Constants.NBT.Z_COORD));

        CompoundTag doorTag = tag.getCompound(Constants.NBT.DUNGEON_DOOR);
        doorPos = new BlockPos(doorTag.getInt(Constants.NBT.X_COORD), doorTag.getInt(Constants.NBT.Y_COORD), doorTag.getInt(Constants.NBT.Z_COORD));

        doorDirection = Direction.values()[tag.getInt(Constants.NBT.DIRECTION)];

        potentialRoomTypes = new ArrayList<>();
        ListTag listnbt = tag.getList(Constants.NBT.DOOR_TYPES, 10);
        for (int i = 0; i < listnbt.size(); ++i) {
            CompoundTag compoundnbt = listnbt.getCompound(i);
            String str = compoundnbt.getString(Constants.NBT.DOOR);
            potentialRoomTypes.add(ResourceLocation.parse(str));
        }

        this.doorType = tag.getString(Constants.NBT.TYPE);
        activatedRoomDepth = tag.getInt(Constants.NBT.DEPTH);
        highestBranchRoomDepth = tag.getInt(Constants.NBT.MAX_DEPTH);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        CompoundTag masterTag = new CompoundTag();
        masterTag.putInt(Constants.NBT.X_COORD, controllerPos.getX());
        masterTag.putInt(Constants.NBT.Y_COORD, controllerPos.getY());
        masterTag.putInt(Constants.NBT.Z_COORD, controllerPos.getZ());
        tag.put(Constants.NBT.DUNGEON_CONTROLLER, masterTag);

        CompoundTag doorTag = new CompoundTag();
        doorTag.putInt(Constants.NBT.X_COORD, doorPos.getX());
        doorTag.putInt(Constants.NBT.Y_COORD, doorPos.getY());
        doorTag.putInt(Constants.NBT.Z_COORD, doorPos.getZ());
        tag.put(Constants.NBT.DUNGEON_DOOR, doorTag);

        tag.putInt(Constants.NBT.DIRECTION, doorDirection.get3DDataValue());

        ListTag listnbt = new ListTag();
        for (ResourceLocation potentialRoomType : potentialRoomTypes) {
            String str = potentialRoomType.toString();
            CompoundTag compoundnbt = new CompoundTag();
            compoundnbt.putString(Constants.NBT.DOOR, str);
            listnbt.add(compoundnbt);
        }

        if (!listnbt.isEmpty()) {
            tag.put(Constants.NBT.DOOR_TYPES, listnbt);
        }

        tag.putString(Constants.NBT.TYPE, doorType);

        tag.putInt(Constants.NBT.DEPTH, activatedRoomDepth);
        tag.putInt(Constants.NBT.MAX_DEPTH, highestBranchRoomDepth);
    }
}
