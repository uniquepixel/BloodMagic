package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.common.item.dungeon.IDungeonKey;
import wayoftime.bloodmagic.structures.DungeonRoom;
import wayoftime.bloodmagic.structures.DungeonSynthesizer;
import wayoftime.bloodmagic.util.Constants;

import java.util.List;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.common.tile.TileDungeonController}:
 * every generated dungeon has exactly one of these, at the spawn/entrance room. It owns the
 * {@link DungeonSynthesizer} instance driving that dungeon's lazy, player-triggered room expansion
 * (see {@link TileDungeonSeal#requestRoomFromController}) and persists its whole door-graph state
 * to NBT. Adapted from upstream's custom {@code TileBase#serialize}/{@code deserialize} wrapper to
 * this branch's vanilla {@code saveAdditional}/{@code loadAdditional} convention (see
 * {@code BaseTile}) - the NBT shape itself (a single nested {@code dungeon_controller} compound
 * holding the synthesizer's own tag) is otherwise unchanged.
 */
public class TileDungeonController extends BaseTile {
    public DungeonSynthesizer dungeon = null;

    public TileDungeonController(BlockPos pos, BlockState state) {
        super(BMTiles.DUNGEON_CONTROLLER_TYPE.get(), pos, state);
    }

    public void setDungeonSynthesizer(DungeonSynthesizer dungeon) {
        this.dungeon = dungeon;
        this.dungeon.setDungeonController(this);
        setChanged();
    }

    public int handleRequestForRoomPlacement(Player player, ItemStack keyStack, BlockPos activatedDoorPos, net.minecraft.core.Direction doorFacing, String activatedDoorType, int activatedRoomDepth, int highestBranchRoomDepth, List<ResourceLocation> potentialRooms) {
        if (level != null && !level.isClientSide && level instanceof ServerLevel) {
            if (!keyStack.isEmpty() && keyStack.getItem() instanceof IDungeonKey dungeonKey) {
                ResourceLocation roomType = dungeonKey.getValidResourceLocation(potentialRooms);
                if (roomType == null) {
                    return -1;
                }
                int placementState = dungeon.addNewRoomToExistingDungeon(player, (ServerLevel) level, this.getBlockPos(), roomType, level.random, activatedDoorPos, doorFacing, activatedDoorType, potentialRooms, activatedRoomDepth, highestBranchRoomDepth);
                if (placementState == 0) {
                    // Consume the key!
                    keyStack.shrink(1);
                    spawnCosmeticLightning(activatedDoorPos);
                }

                return placementState;
            }
        }
        return -1;
    }

    public int handleRequestForPredesignatedRoomPlacement(Player player, ItemStack keyStack, BlockPos activatedDoorPos, net.minecraft.core.Direction doorFacing, String activatedDoorType, int activatedRoomDepth, int highestBranchRoomDepth, List<ResourceLocation> potentialRooms, DungeonRoom room, Rotation rotation, BlockPos roomLocation) {
        if (level != null && !level.isClientSide && level instanceof ServerLevel) {
            if (!keyStack.isEmpty() && keyStack.getItem() instanceof IDungeonKey dungeonKey) {
                ResourceLocation roomType = dungeonKey.getValidResourceLocation(potentialRooms);
                if (roomType == null) {
                    return -1;
                }
                boolean didPlace = dungeon.forcePlacementOfRoom(player, (ServerLevel) level, this.getBlockPos(), doorFacing, activatedDoorPos, activatedDoorType, activatedRoomDepth, highestBranchRoomDepth, room, rotation, roomLocation);
                if (didPlace) {
                    // Consume the key!
                    keyStack.shrink(1);
                    spawnCosmeticLightning(activatedDoorPos);
                }
            }
        }

        return 1;
    }

    private void spawnCosmeticLightning(BlockPos pos) {
        if (level == null) {
            return;
        }
        LightningBolt lightningboltentity = EntityType.LIGHTNING_BOLT.create(level);
        if (lightningboltentity == null) {
            return;
        }
        lightningboltentity.setPos(pos.getX(), pos.getY(), pos.getZ());
        lightningboltentity.setVisualOnly(true);
        level.addFreshEntity(lightningboltentity);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(Constants.NBT.DUNGEON_CONTROLLER)) {
            CompoundTag synthesizerTag = tag.getCompound(Constants.NBT.DUNGEON_CONTROLLER);
            dungeon = new DungeonSynthesizer();
            dungeon.readFromNBT(synthesizerTag);
        } else {
            dungeon = new DungeonSynthesizer();
        }

        dungeon.setDungeonController(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (dungeon != null) {
            CompoundTag synthesizerTag = new CompoundTag();
            dungeon.writeToNBT(synthesizerTag);
            tag.put(Constants.NBT.DUNGEON_CONTROLLER, synthesizerTag);
        }
    }
}
