package wayoftime.bloodmagic.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.structures.DungeonRoom}: the data
 * model for a single room. Holds the room's structure-name -&gt; origin offset map (a room can be
 * composed of more than one NBT structure stacked at the same spot, picked randomly - see
 * {@link #placeStructureAtPosition}), a per-door-type doors-per-facing map, bounding-volume
 * {@link AreaDescriptor.Rectangle}s, and the per-door-index room-pool wiring
 * ({@link #indexToDoorMap}/{@link #indexToRoomTypeMap}) that drives the procedural branching: place
 * a room, look at its open doors, for each door roll a weighted pick from that door's associated
 * room pool, place the next room aligned to that door, repeat.
 * <p>
 * Deserialized directly from the copied {@code assets/bloodmagic/schematics/*.json} files by
 * {@link wayoftime.bloodmagic.gson.Serializers#GSON} via reflection - field names here must match
 * those JSON files exactly (they do; this is a straight port).
 */
public class DungeonRoom {
    public ResourceLocation key;
    public int dungeonWeight = 1;
    public Map<String, BlockPos> structureMap = new TreeMap<>();

    // Map of doors. The Direction indicates what way this door faces.
    public Map<String, Map<Direction, List<BlockPos>>> doorMap = new TreeMap<>();
    public List<AreaDescriptor.Rectangle> descriptorList = new ArrayList<>();

    public float oreDensity = 0;
    public BlockPos spawnLocation = BlockPos.ZERO;
    public BlockPos controllerOffset = BlockPos.ZERO;
    public BlockPos portalOffset = BlockPos.ZERO;

    // Set of maps that contain the types of rooms that a given door (defined by its BlockPos) can
    // link to. Defined in this manner to reduce the file size of the DungeonRoom.
    public Map<Integer, List<BlockPos>> indexToDoorMap = new TreeMap<>();
    public Map<Integer, List<String>> indexToRoomTypeMap = new TreeMap<>();

    public Map<String, List<BlockPos>> requiredDoorMap = new TreeMap<>();
    public Map<Integer, AreaDescriptor.Rectangle> doorCoverMap = new TreeMap<>();

    public DungeonRoom(Map<String, BlockPos> structureMap, Map<String, Map<Direction, List<BlockPos>>> doorMap, List<AreaDescriptor.Rectangle> descriptorList) {
        this.structureMap = structureMap;
        this.doorMap = doorMap;
        this.descriptorList = descriptorList;
    }

    public DungeonRoom() {
        this(new TreeMap<>(), new TreeMap<>(), new ArrayList<>());
    }

    public DungeonRoom addStructure(String location, BlockPos pos) {
        structureMap.put(location, pos);
        return this;
    }

    public DungeonRoom addAreaDescriptor(AreaDescriptor.Rectangle descriptor) {
        descriptor.setDoCache(false);
        descriptorList.add(descriptor);
        return this;
    }

    public DungeonRoom addNonstandardDoor(BlockPos pos, Direction dir, String thisDoorType, int index, String wantedDoorType) {
        if (!requiredDoorMap.containsKey(wantedDoorType)) {
            requiredDoorMap.put(wantedDoorType, new ArrayList<>());
        }
        requiredDoorMap.get(wantedDoorType).add(pos);
        return addDoor(pos, dir, thisDoorType, index);
    }

    public DungeonRoom addDoor(BlockPos pos, Direction dir, String doorType, int index) {
        if (!doorMap.containsKey(doorType)) {
            doorMap.put(doorType, new TreeMap<>());
        }

        Map<Direction, List<BlockPos>> dirMap = doorMap.get(doorType);
        if (!dirMap.containsKey(dir)) {
            dirMap.put(dir, new ArrayList<>());
        }

        dirMap.get(dir).add(pos);

        if (!indexToDoorMap.containsKey(index)) {
            indexToDoorMap.put(index, new ArrayList<>());
        }

        indexToDoorMap.get(index).add(pos);

        return this;
    }

    public DungeonRoom addDoors(Direction dir, String doorType, int index, BlockPos... positions) {
        if (positions.length <= 0) {
            return this;
        }

        for (BlockPos position : positions) {
            addDoor(position, dir, doorType, index);
        }

        return this;
    }

    public DungeonRoom addNormalRoomPool(int index, ResourceLocation roomPool) {
        return this.addRoomPool(index, roomPool.toString());
    }

    public DungeonRoom addSpecialRoomPool(int index, ResourceLocation roomPool) {
        return this.addRoomPool(index, "#" + roomPool);
    }

    public DungeonRoom addDeadendRoomPool(int index, ResourceLocation roomPool) {
        return this.addRoomPool(index, "$" + roomPool);
    }

    public DungeonRoom addRoomPool(int index, String roomPool) {
        if (!indexToRoomTypeMap.containsKey(index)) {
            indexToRoomTypeMap.put(index, new ArrayList<>());
        }

        indexToRoomTypeMap.get(index).add(roomPool);

        return this;
    }

    public DungeonRoom setOreDensity(float oreDensity) {
        this.oreDensity = oreDensity;
        return this;
    }

    public BlockPos getOriginalBlockPos(BlockPos worldDoorPos, StructurePlaceSettings settings, BlockPos offset) {
        StructurePlaceSettings oppositeSettings = settings.copy();
        switch (settings.getRotation()) {
            case CLOCKWISE_90 -> oppositeSettings.setRotation(Rotation.COUNTERCLOCKWISE_90);
            case COUNTERCLOCKWISE_90 -> oppositeSettings.setRotation(Rotation.CLOCKWISE_90);
            default -> {
            }
        }

        return StructureTemplate.calculateRelativePosition(oppositeSettings, worldDoorPos).subtract(offset);
    }

    public int getIndexForDoor(BlockPos originalDoorPos) {
        for (Entry<Integer, List<BlockPos>> entry : indexToDoorMap.entrySet()) {
            if (entry.getValue().contains(originalDoorPos)) {
                return entry.getKey();
            }
        }

        return 1;
    }

    public DungeonRoom registerDoorFill(int index, AreaDescriptor.Rectangle desc) {
        doorCoverMap.put(index, desc);
        return this;
    }

    public AreaDescriptor getDoorFillDescriptor(BlockPos originalDoorPos) {
        int index = getIndexForDoor(originalDoorPos);
        if (doorCoverMap.containsKey(index)) {
            return doorCoverMap.get(index);
        }

        return new AreaDescriptor.Rectangle(new BlockPos(-1, -1, 0), 3, 3, 1);
    }

    public AreaDescriptor getDoorFillDescriptor(StructurePlaceSettings settings, BlockPos originalDoorPos, BlockPos newDoorPos, Direction dir) {
        StructurePlaceSettings rotatedSettings = settings.copy();
        rotatedSettings.setRotation(rotatedSettings.getRotation().getRotated(getRotationForDirectionFromNorth(dir)));
        AreaDescriptor desc = getDoorFillDescriptor(originalDoorPos);

        return desc.rotateDescriptor(rotatedSettings);
    }

    public Rotation getRotationForDirectionFromNorth(Direction dir) {
        return switch (dir) {
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    public List<DungeonDoor> getPotentialConnectedRoomTypes(StructurePlaceSettings settings, BlockPos offset) {
        // This DungeonDoor is stored in the door block.
        List<DungeonDoor> dungeonDoorList = new ArrayList<>();

        for (Entry<String, Map<Direction, List<BlockPos>>> entry : doorMap.entrySet()) {
            Map<Direction, List<BlockPos>> doorDirMap = entry.getValue();
            String doorType = entry.getKey();

            for (int i = 0; i < 4; i++) {
                Direction originalFacing = Direction.from2DDataValue(i);
                if (doorDirMap.containsKey(originalFacing)) {
                    Direction rotatedFacing = DungeonUtil.getFacingForSettings(settings, originalFacing);
                    List<BlockPos> doorList = doorDirMap.get(originalFacing);
                    if (indexToDoorMap == null || indexToDoorMap.isEmpty()) {
                        List<String> roomTypeList = new ArrayList<>();
                        for (BlockPos doorPos : doorList) {
                            BlockPos newDoorPos = StructureTemplate.calculateRelativePosition(settings, doorPos).offset(offset);
                            dungeonDoorList.add(new DungeonDoor(newDoorPos, rotatedFacing, doorType, roomTypeList, getDoorFillDescriptor(settings, doorPos, newDoorPos, originalFacing)));
                        }

                        continue;
                    }
                    for (Entry<Integer, List<BlockPos>> rotatedIndexEntry : indexToDoorMap.entrySet()) {
                        int index = rotatedIndexEntry.getKey();
                        List<String> roomTypeList = indexToRoomTypeMap.get(index);
                        List<BlockPos> indexedDoorList = rotatedIndexEntry.getValue();
                        for (BlockPos indexPos : indexedDoorList) {
                            // Check if we are on the right Type
                            if (doorList.contains(indexPos)) {
                                String requiredType = getRequiredDoorType(doorType, indexPos);

                                BlockPos newDoorPos = StructureTemplate.calculateRelativePosition(settings, indexPos).offset(offset);
                                dungeonDoorList.add(new DungeonDoor(newDoorPos, rotatedFacing, requiredType, roomTypeList, getDoorFillDescriptor(settings, indexPos, newDoorPos, originalFacing)));
                            }
                        }
                    }
                }
            }
        }

        return dungeonDoorList;
    }

    public String getRequiredDoorType(String type, BlockPos indexPos) {
        for (Entry<String, List<BlockPos>> entry : requiredDoorMap.entrySet()) {
            if (entry.getValue().contains(indexPos)) {
                return entry.getKey();
            }
        }

        return type;
    }

    public List<AreaDescriptor> getAreaDescriptors(StructurePlaceSettings settings, BlockPos offset) {
        List<AreaDescriptor> newList = new ArrayList<>();

        for (AreaDescriptor desc : descriptorList) {
            newList.add(desc.rotateDescriptor(settings).offset(offset));
        }

        return newList;
    }

    public BlockPos getPlayerSpawnLocationForPlacement(StructurePlaceSettings settings, BlockPos offset) {
        return StructureTemplate.calculateRelativePosition(settings, spawnLocation).offset(offset);
    }

    public BlockPos getPortalOffsetLocationForPlacement(StructurePlaceSettings settings, BlockPos offset) {
        return StructureTemplate.calculateRelativePosition(settings, portalOffset).offset(offset);
    }

    public BlockPos getInitialSpawnOffsetForControllerPos(StructurePlaceSettings settings, BlockPos controllerPos) {
        if (controllerOffset == null) {
            return controllerPos;
        }

        return controllerPos.subtract(StructureTemplate.calculateRelativePosition(settings, controllerOffset));
    }

    public List<BlockPos> getDoorOffsetsForFacing(StructurePlaceSettings settings, String doorType, Direction facing, BlockPos offset) {
        List<BlockPos> offsetList = new ArrayList<>();

        if (doorMap.containsKey(doorType)) {
            Map<Direction, List<BlockPos>> doorDirMap = doorMap.get(doorType);
            Direction originalFacing = DungeonUtil.reverseRotate(settings.getMirror(), settings.getRotation(), facing);
            if (doorDirMap.containsKey(originalFacing)) {
                List<BlockPos> doorList = doorDirMap.get(originalFacing);
                for (BlockPos doorPos : doorList) {
                    offsetList.add(StructureTemplate.calculateRelativePosition(settings, doorPos).offset(offset));
                }
            }
        }

        return offsetList;
    }

    public Map<String, List<BlockPos>> getAllDoorOffsetsForFacing(StructurePlaceSettings settings, Direction facing, BlockPos offset) {
        Map<String, List<BlockPos>> offsetMap = new TreeMap<>();

        for (String type : doorMap.keySet()) {
            offsetMap.put(type, getDoorOffsetsForFacing(settings, type, facing, offset));
        }

        return offsetMap;
    }

    public boolean placeStructureAtPosition(RandomSource rand, StructurePlaceSettings settings, ServerLevel world, BlockPos pos) {
        Map<BlockPos, List<String>> compositeMap = new TreeMap<>();
        for (Entry<String, BlockPos> entry : structureMap.entrySet()) {
            BlockPos key = entry.getValue();
            String structure = entry.getKey();

            compositeMap.computeIfAbsent(key, k -> new ArrayList<>()).add(structure);
        }

        for (Entry<BlockPos, List<String>> entry : compositeMap.entrySet()) {
            ResourceLocation location = ResourceLocation.parse(entry.getValue().get(rand.nextInt(entry.getValue().size())));
            BlockPos offsetPos = StructureTemplate.calculateRelativePosition(settings, entry.getKey());
            DungeonStructure structure = new DungeonStructure(location);

            structure.placeStructureAtPosition(rand, settings, world, pos.offset(offsetPos));
        }

        return true;
    }
}
