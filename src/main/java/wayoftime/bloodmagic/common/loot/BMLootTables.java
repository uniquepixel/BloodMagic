package wayoftime.bloodmagic.common.loot;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import wayoftime.bloodmagic.BloodMagic;

import java.util.List;

/**
 * Registry of every {@code bloodmagic:chests/*} loot table id. {@link #DEMON_VAULT} is unrelated to
 * the Demon Dungeon room system (it's used elsewhere); {@link #DUNGEON_CHEST_TABLES} is the full
 * restored 1.20.1 set of 25 room-themed dungeon chest tables (12 {@code simple_dungeon/*}, 8
 * {@code standard_dungeon/*}, 5 {@code mines/*}), full-fidelity ported from 1.20.1's
 * {@code GeneratorLootTable}. See {@link wayoftime.bloodmagic.structures.DungeonChestLootProcessor}
 * for how these get attached to placed chests, and
 * {@code wayoftime.bloodmagic.datagen.content.loot.DungeonChestLoot} for their actual pool contents.
 */
public class BMLootTables {
    public static final ResourceKey<LootTable> DEMON_VAULT = ResourceKey.create(Registries.LOOT_TABLE, BloodMagic.rl("chests/demon_vault"));

    // simple_dungeon/* - ported from 1.20.1's chests/simple_dungeon/* (mini_dungeon-tier rooms)
    public static final ResourceKey<LootTable> SIMPLE_DUNGEON_ENTRANCE_CHEST = table("chests/simple_dungeon/entrance_chest");
    public static final ResourceKey<LootTable> SIMPLE_DUNGEON_LIBRARY = table("chests/simple_dungeon/library");
    public static final ResourceKey<LootTable> SIMPLE_DUNGEON_POTION_INGREDIENTS = table("chests/simple_dungeon/potion_ingredients");
    public static final ResourceKey<LootTable> SIMPLE_DUNGEON_SIMPLE_ARMOURY = table("chests/simple_dungeon/simple_armoury");
    public static final ResourceKey<LootTable> SIMPLE_DUNGEON_SIMPLE_BLACKSMITH = table("chests/simple_dungeon/simple_blacksmith");
    public static final ResourceKey<LootTable> SIMPLE_DUNGEON_TEST_GEMS = table("chests/simple_dungeon/test_gems");
    public static final ResourceKey<LootTable> SIMPLE_DUNGEON_FOOD = table("chests/simple_dungeon/food");
    public static final ResourceKey<LootTable> SIMPLE_DUNGEON_FARM_TOOLS = table("chests/simple_dungeon/farm_tools");
    public static final ResourceKey<LootTable> SIMPLE_DUNGEON_FARM_PARTS = table("chests/simple_dungeon/farm_parts");
    public static final ResourceKey<LootTable> SIMPLE_DUNGEON_BASTION = table("chests/simple_dungeon/bastion");
    public static final ResourceKey<LootTable> SIMPLE_DUNGEON_NETHER = table("chests/simple_dungeon/nether");
    public static final ResourceKey<LootTable> SIMPLE_DUNGEON_CRYPT = table("chests/simple_dungeon/crypt");

    // standard_dungeon/* - ported from 1.20.1's chests/standard_dungeon/* (standard-tier rooms)
    public static final ResourceKey<LootTable> STANDARD_DUNGEON_DECENT_LOOT = table("chests/standard_dungeon/decent_loot");
    public static final ResourceKey<LootTable> STANDARD_DUNGEON_GREAT_LOOT = table("chests/standard_dungeon/great_loot");
    public static final ResourceKey<LootTable> STANDARD_DUNGEON_ENCHANTING_LOOT = table("chests/standard_dungeon/enchanting_loot");
    public static final ResourceKey<LootTable> STANDARD_DUNGEON_POOR_LOOT = table("chests/standard_dungeon/poor_loot");
    public static final ResourceKey<LootTable> STANDARD_DUNGEON_DECENT_ALCHEMY = table("chests/standard_dungeon/decent_alchemy");
    public static final ResourceKey<LootTable> STANDARD_DUNGEON_STRONG_ALCHEMY = table("chests/standard_dungeon/strong_alchemy");
    public static final ResourceKey<LootTable> STANDARD_DUNGEON_DECENT_SMITHY = table("chests/standard_dungeon/decent_smithy");
    public static final ResourceKey<LootTable> STANDARD_DUNGEON_MINES_KEY = table("chests/standard_dungeon/mines_key");

    // mines/* - ported from 1.20.1's chests/mines/* (mine shaft rooms)
    public static final ResourceKey<LootTable> MINES_ORE_LOOT = table("chests/mines/ore_loot");
    public static final ResourceKey<LootTable> MINES_SMITHY_LOOT = table("chests/mines/smithy_loot");
    public static final ResourceKey<LootTable> MINES_DECENT_LOOT = table("chests/mines/decent_loot");
    public static final ResourceKey<LootTable> MINES_FOOD_LOOT = table("chests/mines/food_loot");
    public static final ResourceKey<LootTable> MINES_MINE_KEY_LOOT = table("chests/mines/mine_key_loot");

    /**
     * All 25 dungeon-room chest tables, used by {@link wayoftime.bloodmagic.structures.DungeonChestLootProcessor}
     * as a weighted-random fallback for the rare chest whose placed structure NBT carries no baked-in
     * {@code LootTable} tag of its own (every one of the 48 ported room templates that actually places
     * a chest already bakes in the correct one of these - see that processor's class javadoc).
     * Deliberately excludes {@link #DEMON_VAULT}, which belongs to an unrelated structure.
     */
    public static final List<ResourceKey<LootTable>> DUNGEON_CHEST_TABLES = List.of(
            SIMPLE_DUNGEON_ENTRANCE_CHEST, SIMPLE_DUNGEON_LIBRARY, SIMPLE_DUNGEON_POTION_INGREDIENTS,
            SIMPLE_DUNGEON_SIMPLE_ARMOURY, SIMPLE_DUNGEON_SIMPLE_BLACKSMITH, SIMPLE_DUNGEON_TEST_GEMS,
            SIMPLE_DUNGEON_FOOD, SIMPLE_DUNGEON_FARM_TOOLS, SIMPLE_DUNGEON_FARM_PARTS,
            SIMPLE_DUNGEON_BASTION, SIMPLE_DUNGEON_NETHER, SIMPLE_DUNGEON_CRYPT,
            STANDARD_DUNGEON_DECENT_LOOT, STANDARD_DUNGEON_GREAT_LOOT, STANDARD_DUNGEON_ENCHANTING_LOOT,
            STANDARD_DUNGEON_POOR_LOOT, STANDARD_DUNGEON_DECENT_ALCHEMY, STANDARD_DUNGEON_STRONG_ALCHEMY,
            STANDARD_DUNGEON_DECENT_SMITHY, STANDARD_DUNGEON_MINES_KEY,
            MINES_ORE_LOOT, MINES_SMITHY_LOOT, MINES_DECENT_LOOT, MINES_FOOD_LOOT, MINES_MINE_KEY_LOOT
    );

    private static ResourceKey<LootTable> table(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, BloodMagic.rl(path));
    }
}
