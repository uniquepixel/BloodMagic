package wayoftime.bloodmagic.structures;

import net.minecraft.resources.ResourceLocation;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.block.BlockSpecialDungeonSeal;
import wayoftime.bloodmagic.common.block.type.SpecialSealType;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.structures.ModRoomPools}.
 */
public class ModRoomPools {
    public static final ResourceLocation CONNECTIVE_CORRIDORS = BloodMagic.rl("room_pools/connective_corridors");
    public static final ResourceLocation MINI_DUNGEON_ENTRANCES = BloodMagic.rl("room_pools/entrances/mini_dungeon_entrances");
    public static final ResourceLocation STANDARD_DUNGEON_ENTRANCES = BloodMagic.rl("room_pools/entrances/standard_dungeon_entrances");
    public static final ResourceLocation MINI_DUNGEON = BloodMagic.rl("room_pools/tier1/mini_dungeon");

    public static final ResourceLocation STANDARD_ROOMS = BloodMagic.rl("room_pools/standard/standard_rooms");
    public static final ResourceLocation MINE_ENTRANCES = BloodMagic.rl("room_pools/special/mine_entrances");
    public static final ResourceLocation MINE_KEY = BloodMagic.rl("room_pools/standard/mine_key");
    public static final ResourceLocation STANDARD_DEADEND = BloodMagic.rl("room_pools/standard/standard_deadend");

    public static final ResourceLocation MINE_ROOMS = BloodMagic.rl("room_pools/mines/mine_rooms");
    public static final ResourceLocation MINE_CORRIDORS = BloodMagic.rl("room_pools/mines/mine_corridors");

    public static final ResourceLocation DEFAULT_DEADEND = STANDARD_DEADEND;
    public static final ResourceLocation MINE_DEADEND = BloodMagic.rl("room_pools/mines/mine_deadend");

    public static void init() {
        registerDungeonRooms();
        DungeonRoomLoader.loadRoomPools();
    }

    public static void registerDungeonRooms() {
        DungeonRoomRegistry.registerUnloadedDungeonRoomPool(CONNECTIVE_CORRIDORS);
        DungeonRoomRegistry.registerUnloadedDungeonRoomPool(MINI_DUNGEON_ENTRANCES);
        DungeonRoomRegistry.registerUnloadedDungeonRoomPool(STANDARD_DUNGEON_ENTRANCES);
        DungeonRoomRegistry.registerUnloadedDungeonRoomPool(MINI_DUNGEON);
        DungeonRoomRegistry.registerUnloadedDungeonRoomPool(STANDARD_ROOMS);
        DungeonRoomRegistry.registerUnloadedDungeonRoomPool(MINE_ENTRANCES);

        DungeonRoomRegistry.registerUnloadedDungeonRoomPool(MINE_ROOMS);
        DungeonRoomRegistry.registerUnloadedDungeonRoomPool(MINE_CORRIDORS);
        DungeonRoomRegistry.registerUnloadedDungeonRoomPool(MINE_KEY);

        DungeonRoomRegistry.registerUnloadedDungeonRoomPool(DEFAULT_DEADEND);
        DungeonRoomRegistry.registerUnloadedDungeonRoomPool(MINE_DEADEND);
    }

    public static void registerSpecialRooms() {
        SpecialDungeonRoomPoolRegistry.registerUniqueRoomPool(MINE_ENTRANCES, 1, 4, BMBlocks.SPECIAL_DUNGEON_SEAL.get().defaultBlockState().setValue(BlockSpecialDungeonSeal.SEAL, SpecialSealType.MINE_ENTRANCE));
        SpecialDungeonRoomPoolRegistry.registerUniqueRoomPool(MINE_KEY, 1, 6, BMBlocks.SPECIAL_DUNGEON_SEAL.get().defaultBlockState().setValue(BlockSpecialDungeonSeal.SEAL, SpecialSealType.MINE_KEY));
    }
}
