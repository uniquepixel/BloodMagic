package wayoftime.bloodmagic.util;

/**
 * NBT key constants used by the ported Demon Dungeon system (see {@code structures/} and
 * {@code common.blockentity.TileDungeonController}/{@code TileDungeonSeal}). 1.20.1 had a single
 * sprawling {@code Constants} class shared by the whole mod; nothing else on this branch depends
 * on a shared constants holder yet, so this is trimmed to just the dungeon-relevant keys instead
 * of porting the entire original file wholesale. Extend this (don't replace it) if a later port
 * needs more of the original's keys.
 */
public class Constants {
    public static class NBT {
        public static final String X_COORD = "xCoord";
        public static final String Y_COORD = "yCoord";
        public static final String Z_COORD = "zCoord";
        public static final String DIRECTION = "direction";

        public static final String DUNGEON_CONTROLLER = "dungeon_controller";
        public static final String DUNGEON_DOOR = "dungeon_door";
        public static final String DUNGEON_TELEPORT_POS = "dungeon_teleport_pos";
        public static final String DUNGEON_TELEPORT_KEY = "dungeon_teleport_key";

        public static final String DOOR_MAP = "dungeon_door_map";
        public static final String AREA_DESCRIPTORS = "area_descriptors";
        public static final String DOOR_TYPES = "door_types";
        public static final String DOOR = "door";
        public static final String TYPE = "type";
        public static final String DEPTH = "room_depth";
        public static final String MAX_DEPTH = "max_room_depth";
        public static final String ROOM_POOL_BUFFER = "room_pool_buffer";
        public static final String ROOM_POOL_TRACKER = "room_pool_tracker";
        public static final String ROOM_POOL = "room_pool";

        public static final String ROOM_LOCATION = "room_location";
        public static final String ROOM_NAME = "room_name";
        public static final String ROTATION = "rotation";

        public static final String VALUE = "value";

        public static final String DUNGEON_EXIT = "dungeon_exit";
    }
}
