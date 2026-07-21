package wayoftime.bloodmagic.structures;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.structures.DungeonRoomRegistry}: the
 * in-memory registry of loaded {@link DungeonRoom}s and room pools (weighted lists of room
 * ResourceLocations), plus the "unloaded" lists that {@link ModDungeons}/{@link ModRoomPools}
 * populate at mod-init time and {@link DungeonRoomLoader} then resolves from disk.
 */
public class DungeonRoomRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static Map<DungeonRoom, Integer> dungeonWeightMap = new HashMap<>();
    public static Map<String, List<DungeonRoom>> dungeonStartingRoomMap = new HashMap<>();
    private static int totalWeight = 0;

    public static Map<ResourceLocation, DungeonRoom> dungeonRoomMap = new HashMap<>();
    public static Map<ResourceLocation, List<Pair<ResourceLocation, Integer>>> roomPoolTable = new HashMap<>();
    private static Map<ResourceLocation, Integer> totalWeightMap = new HashMap<>();

    public static List<ResourceLocation> unloadedDungeonRooms = new ArrayList<>();
    public static List<ResourceLocation> unloadedDungeonRoomPools = new ArrayList<>();

    public static void registerDungeonRoom(ResourceLocation res, DungeonRoom room, int weight) {
        room.key = res;
        dungeonWeightMap.put(room, weight);
        totalWeight += weight;
        dungeonRoomMap.put(res, room);
    }

    public static void registerUnloadedDungeonRoom(ResourceLocation res) {
        unloadedDungeonRooms.add(res);
    }

    public static void registerUnloadedDungeonRoomPool(ResourceLocation res) {
        unloadedDungeonRoomPools.add(res);
    }

    public static void registerDungeomRoomPool(ResourceLocation poolRes, List<Pair<ResourceLocation, Integer>> pool) {
        roomPoolTable.put(poolRes, pool);
        int totalWeightOfPool = 0;
        for (Pair<ResourceLocation, Integer> room : pool) {
            totalWeightOfPool += room.getValue();
        }
        totalWeightMap.put(poolRes, totalWeightOfPool);
    }

    public static DungeonRoom getRandomDungeonRoom(ResourceLocation roomPoolName, RandomSource rand) {
        Integer maxWeight = totalWeightMap.get(roomPoolName);

        int wantedWeight = 0;
        if (maxWeight != null && maxWeight > 0) {
            wantedWeight = rand.nextInt(maxWeight);
        }
        List<Pair<ResourceLocation, Integer>> roomPool = roomPoolTable.get(roomPoolName);
        if (roomPool == null) {
            if (DungeonSynthesizer.displayDetailedInformation) {
                LOGGER.warn("No dungeon room pool registered for {}", roomPoolName);
            }
            return null;
        }

        for (Pair<ResourceLocation, Integer> entry : roomPool) {
            wantedWeight -= entry.getValue();

            if (wantedWeight < 0) {
                ResourceLocation dungeonName = entry.getKey();
                return dungeonRoomMap.get(dungeonName);
            }
        }

        return null;
    }

    public static DungeonRoom getDungeonRoom(ResourceLocation dungeonName) {
        return dungeonRoomMap.get(dungeonName);
    }

    public static void registerStarterDungeonRoom(DungeonRoom room, String key) {
        dungeonStartingRoomMap.computeIfAbsent(key, k -> new ArrayList<>()).add(room);
    }

    public static DungeonRoom getRandomDungeonRoom(RandomSource rand) {
        int wantedWeight = rand.nextInt(totalWeight);
        for (Entry<DungeonRoom, Integer> entry : dungeonWeightMap.entrySet()) {
            wantedWeight -= entry.getValue();
            if (wantedWeight < 0) {
                return entry.getKey();
            }
        }

        return null;
    }

    public static DungeonRoom getRandomStarterDungeonRoom(Random rand, String key) {
        if (dungeonStartingRoomMap.containsKey(key)) {
            List<DungeonRoom> roomList = dungeonStartingRoomMap.get(key);
            return roomList.get(rand.nextInt(roomList.size()));
        }

        return null;
    }
}
