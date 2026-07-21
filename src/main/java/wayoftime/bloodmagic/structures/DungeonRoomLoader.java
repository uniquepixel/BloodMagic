package wayoftime.bloodmagic.structures;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.common.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import wayoftime.bloodmagic.gson.Serializers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Port of 1.20.1's {@code wayoftime.bloodmagic.structures.DungeonRoomLoader} - loading half only.
 * The original also had a {@code saveDungeons()}/{@code saveNewDungeons()}/{@code saveSingleDungeon()}
 * dev-tooling half (writes {@code DungeonRoom}s back out to {@code config/BloodMagic/schematics/}
 * as JSON) that the original author used once to hand-author the room JSON now checked in under
 * {@code assets/bloodmagic/schematics/} - that authoring flow isn't ported, it's dead weight once
 * the data already exists.
 * <p>
 * Room JSON lives in asset space (not data space) and is loaded straight off the mod's own
 * classpath via {@link Class#getResource}, exactly as upstream did - it isn't a datapack-reloadable
 * resource, just static data bundled in the jar (the 48 {@code .nbt} structure templates it
 * references, however, ARE data-space and go through vanilla's reloadable
 * {@link net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager}).
 */
public class DungeonRoomLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void loadRoomPools() {
        for (ResourceLocation schematic : new ArrayList<>(DungeonRoomRegistry.unloadedDungeonRoomPools)) {
            try {
                URL roomPoolURL = DungeonRoomLoader.class.getResource(resLocToResourcePath(schematic));
                if (roomPoolURL == null) {
                    LOGGER.warn("Missing dungeon room pool json for {}", schematic);
                    continue;
                }

                List<String> roomPoolList = Serializers.GSON.fromJson(Resources.toString(roomPoolURL, Charsets.UTF_8), new TypeToken<List<String>>() {
                }.getType());

                List<Pair<ResourceLocation, Integer>> roomPool = new ArrayList<>();
                for (String roomEntryString : roomPoolList) {
                    Pair<ResourceLocation, Integer> roomEntry = parseRoomEntryString(roomEntryString);
                    if (roomEntry != null) {
                        roomPool.add(roomEntry);
                    }
                }

                DungeonRoomRegistry.registerDungeomRoomPool(schematic, roomPool);
            } catch (Exception e) {
                LOGGER.error("Failed to load dungeon room pool {}", schematic, e);
            }
        }
    }

    public static Pair<ResourceLocation, Integer> parseRoomEntryString(String str) {
        String[] splitString = str.split(";");
        if (splitString.length == 2) {
            try {
                Integer weight = Integer.parseInt(splitString[0]);
                ResourceLocation resLoc = ResourceLocation.parse(splitString[1]);
                return Pair.of(resLoc, weight);
            } catch (NumberFormatException ex) {
                LOGGER.error("Malformed dungeon room pool entry: {}", str, ex);
            }
        }

        return null;
    }

    public static void loadDungeons() {
        for (ResourceLocation schematic : new ArrayList<>(DungeonRoomRegistry.unloadedDungeonRooms)) {
            try {
                URL dungeonURL = DungeonRoomLoader.class.getResource(resLocToResourcePath(schematic));
                if (dungeonURL == null) {
                    LOGGER.warn("Missing dungeon room schematic json for {}", schematic);
                    continue;
                }

                DungeonRoom dungeonRoom = Serializers.GSON.fromJson(Resources.toString(dungeonURL, Charsets.UTF_8), DungeonRoom.class);
                DungeonRoomRegistry.registerDungeonRoom(schematic, dungeonRoom, Math.max(1, dungeonRoom.dungeonWeight));
            } catch (Exception e) {
                LOGGER.error("Failed to load dungeon room {}", schematic, e);
            }
        }

        LOGGER.info("Loaded {} demon dungeon room schematics", DungeonRoomRegistry.dungeonRoomMap.size());
    }

    public static String resLocToResourcePath(ResourceLocation resourceLocation) {
        return "/assets/" + resourceLocation.getNamespace() + "/schematics/" + resourceLocation.getPath() + ".json";
    }
}
