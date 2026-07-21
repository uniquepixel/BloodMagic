package wayoftime.bloodmagic.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import org.apache.commons.lang3.tuple.Pair;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.structures.Dungeon}: the original,
 * all-at-once door-stitching generator (place a room, then for 100 iterations pick a random open
 * door and try to fit a random room against it). Superseded in actual play by
 * {@link DungeonSynthesizer}'s lazy, player-driven expansion (see {@code TileDungeonController}/
 * {@code TileDungeonSeal}), but kept as-is since it's what {@code DungeonTester}'s debug command
 * exercises, matching upstream.
 */
public class Dungeon {
    public static boolean placeStructureAtPosition(RandomSource rand, ServerLevel world, BlockPos pos) {
        String initialDoorName = "default";

        // Map of doors, keyed by door type name. The Direction indicates what way this door faces.
        Map<String, Map<Direction, List<BlockPos>>> availableDoorMasterMap = new HashMap<>();
        List<AreaDescriptor> descriptorList = new ArrayList<>();
        Map<BlockPos, Pair<DungeonRoom, StructurePlaceSettings>> roomMap = new HashMap<>();

        StructurePlaceSettings settings = new StructurePlaceSettings();
        settings.setMirror(Mirror.NONE);
        settings.setRotation(Rotation.NONE);
        settings.setIgnoreEntities(true);
        settings.addProcessor(new StoneToOreProcessor(0.0f));
        settings.setKnownShape(true);

        DungeonRoom room = getRandomRoom(rand);
        roomMap.put(pos, Pair.of(room, settings.copy()));
        descriptorList.addAll(room.getAreaDescriptors(settings, pos));

        Map<Direction, List<BlockPos>> availableDoorMap = new HashMap<>();
        availableDoorMasterMap.put(initialDoorName, availableDoorMap);
        for (Direction facing : Direction.values()) {
            List<BlockPos> doorList = availableDoorMap.computeIfAbsent(facing, f -> new ArrayList<>());
            doorList.addAll(room.getDoorOffsetsForFacing(settings, initialDoorName, facing, pos));
        }

        // Initial AreaDescriptors and door positions are initialized. Time for fun!
        for (int i = 0; i < 100; i++) {
            List<String> typeList = new ArrayList<>(availableDoorMasterMap.keySet());
            String doorName = typeList.get(rand.nextInt(typeList.size()));

            availableDoorMap = availableDoorMasterMap.computeIfAbsent(doorName, k -> new HashMap<>());

            // Get which facing of doors are available.
            List<Direction> facingList = new ArrayList<>();
            for (Entry<Direction, List<BlockPos>> entry : availableDoorMap.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    facingList.add(entry.getKey());
                }
            }

            Collections.shuffle(facingList); // Shuffle the list so that it is random what is chosen

            Pair<Direction, BlockPos> removedDoor1 = null;
            Pair<Direction, BlockPos> removedDoor2 = null;
            BlockPos roomLocation = null;

            testDirection:
            for (Direction doorFacing : facingList) {
                Direction oppositeDoorFacing = doorFacing.getOpposite();
                List<BlockPos> availableDoorList = availableDoorMap.get(doorFacing);
                Collections.shuffle(availableDoorList);

                Rotation randRotation = Rotation.values()[rand.nextInt(Rotation.values().length)];
                settings.setRotation(randRotation);
                DungeonRoom testingRoom = getRandomRoom(rand);

                List<BlockPos> otherDoorList = testingRoom.getDoorOffsetsForFacing(settings, doorName, oppositeDoorFacing, BlockPos.ZERO);
                if (otherDoorList != null && !otherDoorList.isEmpty()) {
                    // See if one of these doors works.
                    Collections.shuffle(otherDoorList);
                    BlockPos testDoor = otherDoorList.get(0);
                    testDoor:
                    for (BlockPos availableDoor : availableDoorList) {
                        roomLocation = availableDoor.subtract(testDoor).offset(doorFacing.getNormal());

                        List<AreaDescriptor> descriptors = testingRoom.getAreaDescriptors(settings, roomLocation);
                        for (AreaDescriptor testDesc : descriptors) {
                            for (AreaDescriptor currentDesc : descriptorList) {
                                if (testDesc.intersects(currentDesc)) {
                                    break testDoor;
                                }
                            }
                        }

                        settings.clearProcessors();
                        settings.addProcessor(new StoneToOreProcessor(testingRoom.oreDensity));

                        roomMap.put(roomLocation, Pair.of(testingRoom, settings.copy()));
                        descriptorList.addAll(descriptors);
                        removedDoor1 = Pair.of(doorFacing, availableDoor);
                        removedDoor2 = Pair.of(oppositeDoorFacing, testDoor.offset(roomLocation));

                        room = testingRoom;

                        break testDirection;
                    }

                    break;
                }
            }

            if (removedDoor1 != null) {
                for (String doorType : room.doorMap.keySet()) {
                    availableDoorMap = availableDoorMasterMap.computeIfAbsent(doorType, k -> new HashMap<>());

                    for (Direction facing : Direction.values()) {
                        List<BlockPos> doorList = availableDoorMap.computeIfAbsent(facing, f -> new ArrayList<>());
                        doorList.addAll(room.getDoorOffsetsForFacing(settings, doorType, facing, roomLocation));
                    }

                    Direction face = removedDoor1.getKey();
                    if (availableDoorMap.containsKey(face)) {
                        availableDoorMap.get(face).remove(removedDoor1.getRight());
                    }
                }
            }

            if (removedDoor2 != null) {
                Direction face = removedDoor2.getKey();
                for (Entry<String, Map<Direction, List<BlockPos>>> entry : availableDoorMasterMap.entrySet()) {
                    Map<Direction, List<BlockPos>> doorMap = entry.getValue();
                    if (doorMap.containsKey(face)) {
                        doorMap.get(face).remove(removedDoor2.getRight());
                    }
                }
            }
        }

        // Building what I've got
        for (Entry<BlockPos, Pair<DungeonRoom, StructurePlaceSettings>> entry : roomMap.entrySet()) {
            BlockPos placementPos = entry.getKey();
            DungeonRoom placedRoom = entry.getValue().getKey();
            StructurePlaceSettings placementSettings = entry.getValue().getValue();

            placedRoom.placeStructureAtPosition(rand, placementSettings, world, placementPos);
        }

        return false;
    }

    public static DungeonRoom getRandomRoom(RandomSource rand) {
        return DungeonRoomRegistry.getRandomDungeonRoom(rand);
    }
}
