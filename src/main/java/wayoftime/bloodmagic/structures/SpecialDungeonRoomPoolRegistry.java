package wayoftime.bloodmagic.structures;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import wayoftime.bloodmagic.common.block.BMBlocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiPredicate;

/**
 * Full-fidelity port of 1.20.1's
 * {@code wayoftime.bloodmagic.structures.SpecialDungeonRoomPoolRegistry}: tracks which "special"
 * room pools (guaranteed-content rooms like a mine key or mine entrance) are eligible to be slotted
 * into the next placed door, based on how many rooms/how deep the dungeon has generated so far.
 */
public class SpecialDungeonRoomPoolRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static Map<ResourceLocation, BiPredicate<Integer, Integer>> predicateMap = new HashMap<>();
    public static Map<ResourceLocation, BlockState> stateMap = new HashMap<>();

    public static List<ResourceLocation> getSpecialRooms(int minimumRooms, int minimumDepth, Map<ResourceLocation, Integer> timeSincePlacement, List<ResourceLocation> bufferRoomPools) {
        List<ResourceLocation> specialRoomPools = new ArrayList<>();

        for (Entry<ResourceLocation, BiPredicate<Integer, Integer>> entry : predicateMap.entrySet()) {
            ResourceLocation roomPool = entry.getKey();
            if (bufferRoomPools.contains(roomPool) || timeSincePlacement.containsKey(roomPool)) {
                continue;
            }

            if (entry.getValue().test(minimumRooms, minimumDepth)) {
                if (DungeonSynthesizer.displayDetailedInformation) {
                    LOGGER.info("Added special dungeon room pool: {}", roomPool);
                }
                specialRoomPools.add(roomPool);
            }
        }

        return specialRoomPools;
    }

    public static void registerUniqueRoomPool(ResourceLocation roomPool, int minRooms, int minDepth) {
        predicateMap.put(roomPool, (x, y) -> x >= minRooms && y >= minDepth);
    }

    public static void registerUniqueRoomPool(ResourceLocation roomPool, int minRooms, int minDepth, BlockState placementState) {
        registerUniqueRoomPool(roomPool, minRooms, minDepth);
        stateMap.put(roomPool, placementState);
    }

    public static BlockState getSealBlockState(ResourceLocation roomPool) {
        if (stateMap.containsKey(roomPool)) {
            return stateMap.get(roomPool);
        }

        return BMBlocks.SPECIAL_DUNGEON_SEAL.get().defaultBlockState();
    }
}
