package wayoftime.bloodmagic.structures;

import com.google.common.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.blockentity.TileDungeonController;
import wayoftime.bloodmagic.common.blockentity.TileDungeonSeal;
import wayoftime.bloodmagic.common.blockentity.TileSpecialRoomDungeonSeal;
import wayoftime.bloodmagic.gson.Serializers;
import wayoftime.bloodmagic.structures.rooms.DungeonRoomPlacement;
import wayoftime.bloodmagic.util.ChatUtil;
import wayoftime.bloodmagic.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.structures.DungeonSynthesizer} - the
 * heart of the dungeon system. Unlike {@link Dungeon}'s all-at-once generator, this one builds
 * lazily: {@link #generateInitialRoom} stamps down one starting room and a {@code TileDungeonSeal}
 * at every one of its open doors; each seal just sits there holding NBT until a player right-clicks
 * it with a valid dungeon-key item, at which point {@link #addNewRoomToExistingDungeon} rolls a
 * weighted pick from that door's room pool, tries up to 10 random rotations/rooms to find one that
 * fits without overlapping any previously-placed room's {@link AreaDescriptor}, and on success
 * recurses the same door-sealing step for the new room's own open doors. If no ordinary room fits
 * after 10 tries, it falls back to trying a room from the generic {@code room_pools/connective_corridors}
 * pool with {@code extendCorriDoors=true} (which re-seals with the SAME activated door type instead
 * of consuming a room-pool slot, letting corridors chain indefinitely to route around a dead end).
 */
public class DungeonSynthesizer {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static boolean displayDetailedInformation = false;

    // Map of doors, keyed by door type name. The Direction indicates what way this door faces.
    public Map<String, Map<Direction, List<BlockPos>>> availableDoorMasterMap = new HashMap<>();

    public List<AreaDescriptor> descriptorList = new ArrayList<>();

    private int activatedDoors = 0;

    private List<ResourceLocation> specialRoomBuffer = new ArrayList<>();
    private Map<ResourceLocation, Integer> placementsSinceLastSpecial = new HashMap<>();

    public TileDungeonController tile;

    public void setDungeonController(TileDungeonController tile) {
        this.tile = tile;
    }

    public boolean isAreaDescriptorInBounds(Level level, AreaDescriptor desc) {
        if (desc instanceof AreaDescriptor.Rectangle rectangle) {
            BlockPos maxOffset = rectangle.getMaximumOffset();
            BlockPos minOffset = rectangle.getMinimumOffset();

            return maxOffset.getY() < level.getMaxBuildHeight() && minOffset.getY() >= level.getMinBuildHeight();
        }

        return true;
    }

    public void writeToNBT(CompoundTag tag) {
        String json = Serializers.GSON.toJson(availableDoorMasterMap);
        tag.putString(Constants.NBT.DOOR_MAP, json);

        ListTag listnbt = new ListTag();
        for (AreaDescriptor desc : descriptorList) {
            CompoundTag compoundnbt = new CompoundTag();
            desc.writeToNBT(compoundnbt);
            listnbt.add(compoundnbt);
        }

        if (!listnbt.isEmpty()) {
            tag.put(Constants.NBT.AREA_DESCRIPTORS, listnbt);
        }

        ListTag bufferNBTList = new ListTag();
        for (ResourceLocation resourceLocation : specialRoomBuffer) {
            CompoundTag compoundnbt = new CompoundTag();
            compoundnbt.putString(Constants.NBT.ROOM_POOL, resourceLocation.toString());
            bufferNBTList.add(compoundnbt);
        }

        if (!bufferNBTList.isEmpty()) {
            tag.put(Constants.NBT.ROOM_POOL_BUFFER, bufferNBTList);
        }

        ListTag placementNBTList = new ListTag();
        for (Entry<ResourceLocation, Integer> entry : placementsSinceLastSpecial.entrySet()) {
            CompoundTag compoundnbt = new CompoundTag();
            compoundnbt.putString(Constants.NBT.ROOM_POOL, entry.getKey().toString());
            compoundnbt.putInt(Constants.NBT.VALUE, entry.getValue());
            placementNBTList.add(compoundnbt);
        }

        if (!placementNBTList.isEmpty()) {
            tag.put(Constants.NBT.ROOM_POOL_TRACKER, placementNBTList);
        }
    }

    public void readFromNBT(CompoundTag tag) {
        String testJson = tag.getString(Constants.NBT.DOOR_MAP);
        if (!testJson.isEmpty()) {
            Map<String, Map<Direction, List<BlockPos>>> parsed = Serializers.GSON.fromJson(testJson, new TypeToken<Map<String, Map<Direction, List<BlockPos>>>>() {
            }.getType());
            availableDoorMasterMap = parsed != null ? parsed : new HashMap<>();
        }

        ListTag listnbt = tag.getList(Constants.NBT.AREA_DESCRIPTORS, 10);
        for (int i = 0; i < listnbt.size(); i++) {
            CompoundTag compoundnbt = listnbt.getCompound(i);
            AreaDescriptor.Rectangle rec = new AreaDescriptor.Rectangle(BlockPos.ZERO, 0);
            rec.readFromNBT(compoundnbt);
            descriptorList.add(rec);
        }

        ListTag bufferNBTList = tag.getList(Constants.NBT.ROOM_POOL_BUFFER, 10);
        for (int i = 0; i < bufferNBTList.size(); i++) {
            CompoundTag compoundnbt = bufferNBTList.getCompound(i);
            specialRoomBuffer.add(ResourceLocation.parse(compoundnbt.getString(Constants.NBT.ROOM_POOL)));
        }

        ListTag trackerNBTList = tag.getList(Constants.NBT.ROOM_POOL_TRACKER, 10);
        for (int i = 0; i < trackerNBTList.size(); i++) {
            CompoundTag compoundnbt = trackerNBTList.getCompound(i);
            placementsSinceLastSpecial.put(ResourceLocation.parse(compoundnbt.getString(Constants.NBT.ROOM_POOL)), compoundnbt.getInt(Constants.NBT.VALUE));
        }
    }

    public BlockPos[] generateInitialRoom(ResourceLocation initialType, RandomSource rand, ServerLevel world, BlockPos spawningPosition) {
        StructurePlaceSettings settings = new StructurePlaceSettings();
        settings.setMirror(Mirror.NONE);
        settings.setRotation(Rotation.NONE);
        settings.setIgnoreEntities(true);
        settings.setKnownShape(true);

        DungeonRoom initialRoom = DungeonRoomRegistry.getRandomDungeonRoom(initialType, rand);
        if (initialRoom == null) {
            LOGGER.error("No dungeon room available in initial room pool {} - is the datapack missing?", initialType);
            return new BlockPos[]{spawningPosition, spawningPosition};
        }

        BlockPos roomPlacementPosition = initialRoom.getInitialSpawnOffsetForControllerPos(settings, spawningPosition);

        descriptorList.addAll(initialRoom.getAreaDescriptors(settings, roomPlacementPosition));

        for (Direction facing : Direction.values()) {
            Map<String, List<BlockPos>> doorTypeMap = initialRoom.getAllDoorOffsetsForFacing(settings, facing, roomPlacementPosition);
            for (Entry<String, List<BlockPos>> entry : doorTypeMap.entrySet()) {
                Map<Direction, List<BlockPos>> doorDirectionMap = availableDoorMasterMap.computeIfAbsent(entry.getKey(), k -> new HashMap<>());
                doorDirectionMap.computeIfAbsent(facing, f -> new ArrayList<>()).addAll(entry.getValue());
            }
        }

        initialRoom.placeStructureAtPosition(rand, settings, world, roomPlacementPosition);

        addNewControllerBlock(world, spawningPosition);

        List<DungeonDoor> doorTypeMap = initialRoom.getPotentialConnectedRoomTypes(settings, roomPlacementPosition);
        for (DungeonDoor dungeonDoor : doorTypeMap) {
            this.addNewDoorBlock(null, dungeonDoor, world, spawningPosition, dungeonDoor.doorPos, dungeonDoor.doorDir, dungeonDoor.doorType, 0, 0, dungeonDoor.getRoomList(), dungeonDoor.getSpecialRoomList());
        }

        BlockPos playerPos = initialRoom.getPlayerSpawnLocationForPlacement(settings, roomPlacementPosition);
        BlockPos portalLocation = initialRoom.getPortalOffsetLocationForPlacement(settings, roomPlacementPosition);

        return new BlockPos[]{playerPos, portalLocation};
    }

    public void addNewControllerBlock(ServerLevel world, BlockPos controllerPos) {
        world.setBlock(controllerPos, BMBlocks.DUNGEON_CONTROLLER.get().defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        BlockEntity tile = world.getBlockEntity(controllerPos);
        if (tile instanceof TileDungeonController controller) {
            controller.setDungeonSynthesizer(this);
        } else {
            LOGGER.warn("Failed to find dungeon controller block entity at {}", controllerPos);
        }
    }

    public boolean isBlockInDescriptor(BlockPos blockPos) {
        for (AreaDescriptor descriptor : this.descriptorList) {
            if (descriptor.isWithinArea(blockPos)) {
                return true;
            }
        }

        return false;
    }

    public boolean isAnyBlockInDescriptor(List<BlockPos> posList) {
        for (BlockPos pos : posList) {
            if (isBlockInDescriptor(pos)) {
                return true;
            }
        }

        return false;
    }

    public boolean doesDescriptorIntersect(AreaDescriptor desc) {
        for (AreaDescriptor descriptor : this.descriptorList) {
            if (descriptor.intersects(desc)) {
                return true;
            }
        }

        return false;
    }

    public boolean addNewDoorBlock(Player player, DungeonDoor door, ServerLevel world, BlockPos controllerPos, BlockPos doorBlockPos, Direction doorFacing, String doorType, int newRoomDepth, int highestBranchRoomDepth, List<ResourceLocation> potentialRoomTypes, List<ResourceLocation> specialRoomTypes) {
        if (highestBranchRoomDepth < newRoomDepth) {
            highestBranchRoomDepth = newRoomDepth;
        }

        BlockPos doorBlockOffsetPos = doorBlockPos.relative(doorFacing).relative(Direction.UP, 2);

        AreaDescriptor desc = door.descriptor;
        List<BlockPos> fillerList = desc.getContainedPositions(doorBlockOffsetPos);

        boolean doPlaceDoor = !doesDescriptorIntersect(desc);
        if (!doPlaceDoor) {
            // Don't place a door right next to where a room already exists - seal the gap instead.
            for (BlockPos fillerPos : fillerList) {
                world.setBlockAndUpdate(fillerPos.relative(doorFacing.getOpposite()), BMBlocks.DUNGEON_BRICK_ASSORTED.block().get().defaultBlockState());
            }

            world.setBlockAndUpdate(doorBlockOffsetPos.relative(doorFacing.getOpposite()), BMBlocks.DUNGEON_BRICK_ASSORTED.block().get().defaultBlockState());

            return false;
        } else {
            for (BlockPos fillerPos : fillerList) {
                world.setBlockAndUpdate(fillerPos, BMBlocks.DUNGEON_BRICK_ASSORTED.block().get().defaultBlockState());
            }
        }

        ResourceLocation specialRoomType = getSpecialRoom(newRoomDepth, specialRoomTypes);
        if (specialRoomType != null) {
            DungeonRoom randomRoom = getRandomRoom(specialRoomType, world.random);
            if (randomRoom != null) {
                if (checkRequiredRoom(world, controllerPos, specialRoomType, doorBlockOffsetPos, randomRoom, world.random, doorBlockPos, doorFacing, doorType, newRoomDepth, highestBranchRoomDepth)) {
                    removeSpecialRoom(specialRoomType);

                    if (player != null) {
                        ChatUtil.sendChatNoSpam(player, List.of(Component.translatable("tooltip.bloodmagic.specialspawn")));
                    }

                    return true;
                }
            } else if (displayDetailedInformation) {
                LOGGER.info("No random room found for special room type {}", specialRoomType);
            }
        }

        potentialRoomTypes = modifyRoomTypes(potentialRoomTypes);

        world.setBlock(doorBlockOffsetPos, BMBlocks.DUNGEON_SEAL.get().defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        BlockEntity tile = world.getBlockEntity(doorBlockOffsetPos);
        if (tile instanceof TileDungeonSeal seal) {
            seal.acceptDoorInformation(controllerPos, doorBlockPos, doorFacing, doorType, newRoomDepth, highestBranchRoomDepth, potentialRoomTypes);
        } else {
            LOGGER.warn("Failed to find dungeon seal block entity at {}", doorBlockOffsetPos);
        }

        return true;
    }

    public List<ResourceLocation> modifyRoomTypes(List<ResourceLocation> potentialRoomTypes) {
        return new ArrayList<>(potentialRoomTypes);
    }

    public ResourceLocation getSpecialRoom(int currentRoomDepth, List<ResourceLocation> potentialSpecialRoomTypes) {
        if (potentialSpecialRoomTypes.isEmpty() || specialRoomBuffer.isEmpty()) {
            return null;
        }

        for (ResourceLocation resource : potentialSpecialRoomTypes) {
            if (specialRoomBuffer.contains(resource)) {
                return resource;
            }
        }

        return potentialSpecialRoomTypes.get(0);
    }

    public void removeSpecialRoom(ResourceLocation resource) {
        specialRoomBuffer.remove(resource);

        placementsSinceLastSpecial.put(resource, 0);
        if (tile != null) {
            tile.setChanged();
        }
    }

    public boolean checkRequiredRoom(ServerLevel world, BlockPos controllerPos, ResourceLocation specialRoomType, BlockPos doorBlockOffsetPos, DungeonRoom room, RandomSource rand, BlockPos activatedDoorPos, Direction doorFacing, String activatedDoorType, int newRoomDepth, int highestBranchRoomDepth) {
        StructurePlaceSettings settings = new StructurePlaceSettings();
        settings.setMirror(Mirror.NONE);
        settings.setRotation(Rotation.NONE);
        settings.setIgnoreEntities(false);
        settings.setKnownShape(true);

        DungeonRoom placedRoom = null;
        BlockPos roomLocation = null;

        Direction oppositeDoorFacing = doorFacing.getOpposite();

        List<Rotation> rotationList = Rotation.getShuffled(rand);
        Rotation finalRotation = null;

        rotationCheck:
        for (Rotation initialRotation : rotationList) {
            settings.setRotation(initialRotation);

            List<BlockPos> otherDoorList = room.getDoorOffsetsForFacing(settings, activatedDoorType, oppositeDoorFacing, BlockPos.ZERO);
            if (otherDoorList != null && !otherDoorList.isEmpty()) {
                int doorIndex = rand.nextInt(otherDoorList.size());
                BlockPos testDoor = otherDoorList.get(doorIndex);

                roomLocation = activatedDoorPos.subtract(testDoor).offset(doorFacing.getNormal());

                List<AreaDescriptor> descriptors = room.getAreaDescriptors(settings, roomLocation);
                for (AreaDescriptor testDesc : descriptors) {
                    if (!this.isAreaDescriptorInBounds(world, testDesc)) {
                        break rotationCheck;
                    }

                    for (AreaDescriptor currentDesc : descriptorList) {
                        if (testDesc.intersects(currentDesc)) {
                            break rotationCheck;
                        }
                    }
                }

                descriptorList.addAll(descriptors);

                placedRoom = room;
                finalRotation = initialRotation;

                break;
            }
        }

        if (placedRoom == null) {
            return false;
        }

        spawnDoorBlock(world, controllerPos, specialRoomType, doorBlockOffsetPos, doorFacing, activatedDoorPos, activatedDoorType, newRoomDepth, highestBranchRoomDepth, room, finalRotation, roomLocation);

        if (tile != null) {
            tile.setChanged();
        }

        return true;
    }

    public void spawnDoorBlock(ServerLevel world, BlockPos controllerPos, ResourceLocation specialRoomType, BlockPos doorBlockOffsetPos, Direction doorFacing, BlockPos activatedDoorPos, String activatedDoorType, int roomDepth, int highestBranchRoomDepth, DungeonRoom room, Rotation rotation, BlockPos roomLocation) {
        world.setBlock(doorBlockOffsetPos, SpecialDungeonRoomPoolRegistry.getSealBlockState(specialRoomType), 3);
        BlockEntity tile = world.getBlockEntity(doorBlockOffsetPos);
        if (tile instanceof TileSpecialRoomDungeonSeal specialSeal) {
            specialSeal.acceptSpecificDoorInformation(world, controllerPos, specialRoomType, doorFacing, activatedDoorPos, activatedDoorType, roomDepth, highestBranchRoomDepth, room, rotation, roomLocation);
        }
    }

    /**
     * Returns how successful the placement of the room was: 0 = placed an ordinary room, 1 = fell
     * back to a connective corridor, 2 = failed entirely (door left blocked).
     */
    public int addNewRoomToExistingDungeon(Player player, ServerLevel world, BlockPos controllerPos, ResourceLocation roomType, RandomSource rand, BlockPos activatedDoorPos, Direction doorFacing, String activatedDoorType, List<ResourceLocation> potentialRooms, int activatedRoomDepth, int highestBranchRoomDepth) {
        for (int i = 0; i < 10; i++) {
            boolean testPlacement = attemptPlacementOfRandomRoom(player, world, controllerPos, roomType, rand, activatedDoorPos, doorFacing, activatedDoorType, activatedRoomDepth, highestBranchRoomDepth, potentialRooms, false);
            if (testPlacement) {
                if (tile != null) {
                    tile.setChanged();
                }
                return 0;
            }
        }

        ResourceLocation pathPool = ResourceLocation.parse("bloodmagic:room_pools/connective_corridors");
        if (attemptPlacementOfRandomRoom(player, world, controllerPos, pathPool, rand, activatedDoorPos, doorFacing, activatedDoorType, activatedRoomDepth, highestBranchRoomDepth, potentialRooms, true)) {
            if (tile != null) {
                tile.setChanged();
            }
            return 1;
        }

        return 2;
    }

    public boolean forcePlacementOfRoom(Player player, ServerLevel world, BlockPos controllerPos, Direction doorFacing, BlockPos activatedDoorPos, String activatedDoorType, int previousRoomDepth, int previousMaxDepth, DungeonRoom room, Rotation rotation, BlockPos roomLocation) {
        if (room == null) {
            return false;
        }

        StructurePlaceSettings settings = new StructurePlaceSettings();
        settings.setMirror(Mirror.NONE);
        settings.setRotation(rotation);
        settings.setIgnoreEntities(false);
        settings.setKnownShape(true);

        DungeonRoom placedRoom = room;
        Pair<Direction, BlockPos> activatedDoor = Pair.of(doorFacing, activatedDoorPos);

        Direction oppositeDoorFacing = doorFacing.getOpposite();
        Pair<Direction, BlockPos> addedDoor = Pair.of(oppositeDoorFacing, activatedDoorPos.relative(doorFacing));

        settings.clearProcessors();
        settings.addProcessor(new StoneToOreProcessor(room.oreDensity));

        placedRoom.placeStructureAtPosition(world.random, settings, world, roomLocation);
        for (String doorType : placedRoom.doorMap.keySet()) {
            Map<Direction, List<BlockPos>> availableDoorMap = availableDoorMasterMap.computeIfAbsent(doorType, k -> new HashMap<>());
            for (Direction facing : Direction.values()) {
                List<BlockPos> doorList = availableDoorMap.computeIfAbsent(facing, f -> new ArrayList<>());
                doorList.addAll(placedRoom.getDoorOffsetsForFacing(settings, doorType, facing, roomLocation));
            }

            if (doorType.equals(activatedDoorType)) {
                Direction activatedDoorFace = activatedDoor.getKey();
                if (availableDoorMap.containsKey(activatedDoorFace)) {
                    availableDoorMap.get(activatedDoorFace).remove(activatedDoor.getRight());
                }

                Direction addedDoorFace = addedDoor.getKey();
                if (availableDoorMap.containsKey(addedDoorFace)) {
                    availableDoorMap.get(addedDoorFace).remove(addedDoor.getRight());
                }
            }
        }

        List<DungeonDoor> doorTypeMap = placedRoom.getPotentialConnectedRoomTypes(settings, roomLocation);
        Collections.shuffle(doorTypeMap);
        boolean addedHigherPath = false;

        for (DungeonDoor dungeonDoor : doorTypeMap) {
            if (addedDoor.getKey().equals(dungeonDoor.doorDir) && addedDoor.getRight().equals(dungeonDoor.doorPos)) {
                continue;
            }

            int newRoomDepth = previousRoomDepth + (addedHigherPath ? world.random.nextInt(2) * 2 - 1 : 1);
            addedHigherPath = true;
            this.addNewDoorBlock(player, dungeonDoor, world, controllerPos, dungeonDoor.doorPos, dungeonDoor.doorDir, dungeonDoor.doorType, newRoomDepth, previousMaxDepth, dungeonDoor.getRoomList(), dungeonDoor.getSpecialRoomList());
        }

        if (tile != null) {
            tile.setChanged();
        }

        return true;
    }

    public DungeonRoomPlacement getRandomPlacement(ServerLevel world, BlockPos controllerPos, ResourceLocation roomType, RandomSource rand, BlockPos activatedDoorPos, Direction doorFacing, String activatedDoorType, int previousRoomDepth, int previousMaxDepth, List<ResourceLocation> potentialRooms, boolean extendCorriDoors) {
        StructurePlaceSettings settings = new StructurePlaceSettings();
        settings.setMirror(Mirror.NONE);
        settings.setRotation(Rotation.NONE);
        settings.setIgnoreEntities(false);
        settings.setKnownShape(true);

        Direction oppositeDoorFacing = doorFacing.getOpposite();
        DungeonRoom testingRoom = getRandomRoom(roomType, rand);
        if (testingRoom == null) {
            return null;
        }

        List<Rotation> rotationList = Rotation.getShuffled(rand);

        rotationCheck:
        for (Rotation initialRotation : rotationList) {
            settings.setRotation(initialRotation);

            List<BlockPos> otherDoorList = testingRoom.getDoorOffsetsForFacing(settings, activatedDoorType, oppositeDoorFacing, BlockPos.ZERO);
            if (otherDoorList != null && !otherDoorList.isEmpty()) {
                int doorIndex = rand.nextInt(otherDoorList.size());
                BlockPos testDoor = otherDoorList.get(doorIndex);

                BlockPos roomLocation = activatedDoorPos.subtract(testDoor).offset(doorFacing.getNormal());

                List<AreaDescriptor> descriptors = testingRoom.getAreaDescriptors(settings, roomLocation);
                for (AreaDescriptor testDesc : descriptors) {
                    if (!this.isAreaDescriptorInBounds(world, testDesc)) {
                        break rotationCheck;
                    }

                    for (AreaDescriptor currentDesc : descriptorList) {
                        if (testDesc.intersects(currentDesc)) {
                            break rotationCheck;
                        }
                    }
                }

                settings.clearProcessors();
                settings.addProcessor(new StoneToOreProcessor(testingRoom.oreDensity));

                Pair<Direction, BlockPos> addedDoor = Pair.of(oppositeDoorFacing, testDoor.offset(roomLocation));

                return new DungeonRoomPlacement(testingRoom, world, settings, roomLocation, addedDoor);
            }
        }

        return null;
    }

    public boolean attemptPlacementOfRandomRoom(Player player, ServerLevel world, BlockPos controllerPos, ResourceLocation roomType, RandomSource rand, BlockPos activatedDoorPos, Direction doorFacing, String activatedDoorType, int previousRoomDepth, int previousMaxDepth, List<ResourceLocation> potentialRooms, boolean extendCorriDoors) {
        Pair<Direction, BlockPos> activatedDoor = Pair.of(doorFacing, activatedDoorPos);

        DungeonRoomPlacement placement = this.getRandomPlacement(world, controllerPos, roomType, rand, activatedDoorPos, doorFacing, activatedDoorType, previousRoomDepth, previousMaxDepth, potentialRooms, extendCorriDoors);
        if (placement == null) {
            return false;
        }

        Pair<Direction, BlockPos> addedDoor = placement.getEntrance();

        descriptorList.addAll(placement.getAreaDescriptors());

        placement.placeStructure();

        activatedDoors++;
        checkSpecialRoomRequirements(previousRoomDepth);

        for (String doorType : placement.getAllRoomTypes()) {
            Map<Direction, List<BlockPos>> availableDoorMap = availableDoorMasterMap.computeIfAbsent(doorType, k -> new HashMap<>());
            for (Direction facing : Direction.values()) {
                List<BlockPos> doorList = availableDoorMap.computeIfAbsent(facing, f -> new ArrayList<>());
                doorList.addAll(placement.getDoorOffsetsForFacing(doorType, facing));
            }

            if (doorType.equals(activatedDoorType)) {
                Direction activatedDoorFace = activatedDoor.getKey();
                if (availableDoorMap.containsKey(activatedDoorFace)) {
                    availableDoorMap.get(activatedDoorFace).remove(activatedDoor.getRight());
                }

                Direction addedDoorFace = addedDoor.getKey();
                if (availableDoorMap.containsKey(addedDoorFace)) {
                    availableDoorMap.get(addedDoorFace).remove(addedDoor.getRight());
                }
            }
        }

        List<DungeonDoor> doorTypeMap = placement.getPotentialConnectedRoomTypes();

        Collections.shuffle(doorTypeMap);
        boolean addedHigherPath = false;

        for (DungeonDoor dungeonDoor : doorTypeMap) {
            if (addedDoor.getKey().equals(dungeonDoor.doorDir) && addedDoor.getRight().equals(dungeonDoor.doorPos)) {
                continue;
            }

            if (extendCorriDoors) {
                this.addNewDoorBlock(player, dungeonDoor, world, controllerPos, dungeonDoor.doorPos, dungeonDoor.doorDir, activatedDoorType, previousRoomDepth, previousMaxDepth, potentialRooms, new ArrayList<>());
            } else {
                int newRoomDepth = previousRoomDepth + (addedHigherPath ? world.random.nextInt(2) * 2 - 1 : 1);

                List<ResourceLocation> roomList = dungeonDoor.isDeadend(newRoomDepth, previousMaxDepth)
                        ? dungeonDoor.getDeadendRoomList()
                        : dungeonDoor.getRoomList();

                if (this.addNewDoorBlock(player, dungeonDoor, world, controllerPos, dungeonDoor.doorPos, dungeonDoor.doorDir, dungeonDoor.doorType, newRoomDepth, previousMaxDepth, roomList, dungeonDoor.getSpecialRoomList())) {
                    addedHigherPath = true;
                }
            }
        }

        return true;
    }

    public void checkSpecialRoomRequirements(int currentRoomDepth) {
        for (ResourceLocation res : new ArrayList<>(this.placementsSinceLastSpecial.keySet())) {
            placementsSinceLastSpecial.put(res, placementsSinceLastSpecial.get(res) + 1);
        }

        List<ResourceLocation> newSpecialPools = SpecialDungeonRoomPoolRegistry.getSpecialRooms(activatedDoors, currentRoomDepth, placementsSinceLastSpecial, specialRoomBuffer);

        for (ResourceLocation newSpecialPool : newSpecialPools) {
            if (!specialRoomBuffer.contains(newSpecialPool)) {
                specialRoomBuffer.add(newSpecialPool);
                if (tile != null) {
                    tile.setChanged();
                }
            }
        }
    }

    public static DungeonRoom getRandomRoom(ResourceLocation roomType, RandomSource rand) {
        return DungeonRoomRegistry.getRandomDungeonRoom(roomType, rand);
    }

    public static DungeonRoom getDungeonRoom(ResourceLocation dungeonName) {
        return DungeonRoomRegistry.getDungeonRoom(dungeonName);
    }
}
