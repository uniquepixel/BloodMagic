package wayoftime.bloodmagic.datagen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.util.blockitem.BlockWithItemHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockGroups {
    public static List<ResourceKey<Block>> RUNE_T1 = List.of(
            BMBlocks.RUNE_BLANK.block().getKey(), BMBlocks.RUNE_EFFICIENCY.block().getKey(),
            BMBlocks.RUNE_SACRIFICE.block().getKey(), BMBlocks.RUNE_SELF_SACRIFICE.block().getKey(),
            BMBlocks.RUNE_SPEED.block().getKey(), BMBlocks.RUNE_ACCELERATION.block().getKey(), BMBlocks.RUNE_DISLOCATION.block().getKey(),
            BMBlocks.RUNE_CAPACITY.block().getKey(), BMBlocks.RUNE_CAPACITY_AUGMENTED.block().getKey(),
            BMBlocks.RUNE_ORB.block().getKey(), BMBlocks.RUNE_CHARGING.block().getKey()
    );

    public static List<ResourceKey<Block>> RUNE_T2 = List.of(
            BMBlocks.RUNE_2_EFFICIENCY.block().getKey(),
            BMBlocks.RUNE_2_SACRIFICE.block().getKey(), BMBlocks.RUNE_2_SELF_SACRIFICE.block().getKey(),
            BMBlocks.RUNE_2_SPEED.block().getKey(), BMBlocks.RUNE_2_ACCELERATION.block().getKey(), BMBlocks.RUNE_2_DISLOCATION.block().getKey(),
            BMBlocks.RUNE_2_CAPACITY.block().getKey(), BMBlocks.RUNE_2_CAPACITY_AUGMENTED.block().getKey(),
            BMBlocks.RUNE_2_ORB.block().getKey(), BMBlocks.RUNE_2_CHARGING.block().getKey()
    );

    public static List<ResourceKey<Block>> BLOODSTONE = List.of(
            BMBlocks.BLOODSTONE.block().getKey(), BMBlocks.BLOODSTONE_BRICK.block().getKey()
    );

    public static List<ResourceKey<Block>> HELLFORGED_BLOCK = List.of( // theres textures for the other types for it
            BMBlocks.HELLFORGED_BLOCK.block().getKey()
    );

    public static List<ResourceKey<Block>> CRYSTAL_CLUSTER = List.of(
            BMBlocks.CRYSTAL_CLUSTER.block().getKey(), BMBlocks.CRYSTAL_CLUSTER_BRICK.block().getKey()
    );

    @SafeVarargs
    private static List<ResourceKey<Block>> flattenDungeonFamilies(Map<String, ? extends BlockWithItemHolder<? extends Block, ? extends BlockItem>>... families) {
        List<ResourceKey<Block>> keys = new ArrayList<>();
        for (Map<String, ? extends BlockWithItemHolder<? extends Block, ? extends BlockItem>> family : families) {
            for (BlockWithItemHolder<? extends Block, ? extends BlockItem> holder : family.values()) {
                keys.add(holder.block().getKey());
            }
        }
        return keys;
    }

    // Demon Dungeon decorative block palette (see BMBlocks) - stone-tool tier (matches
    // dungeon_stone/RUNE_T1's strength(2,5)/SoundType.STONE Properties) and the separate iron-tool
    // tier for the dungeon_metal family (strength(5,6)/SoundType.METAL, same tier as HELLFORGED_BLOCK
    // above).
    public static List<ResourceKey<Block>> DUNGEON_PALETTE_STONE_TIER = flattenDungeonFamilies(
            BMBlocks.DUNGEON_BRICK_1, BMBlocks.DUNGEON_BRICK_2, BMBlocks.DUNGEON_BRICK_3,
            BMBlocks.DUNGEON_POLISHED, BMBlocks.DUNGEON_TILE_FAMILY, BMBlocks.DUNGEON_SMALLBRICK,
            BMBlocks.DUNGEON_EYE, BMBlocks.DUNGEON_STONE_FAMILY, BMBlocks.DUNGEON_TILE_SPECIAL_FAMILY,
            BMBlocks.DUNGEON_PILLAR_CENTER, BMBlocks.DUNGEON_PILLAR_SPECIAL, BMBlocks.DUNGEON_PILLAR_CAP,
            BMBlocks.DUNGEON_BRICK_STAIRS, BMBlocks.DUNGEON_POLISHED_STAIRS, BMBlocks.DUNGEON_STONE_STAIRS,
            BMBlocks.DUNGEON_BRICK_WALLS, BMBlocks.DUNGEON_TILE_WALLS, BMBlocks.DUNGEON_POLISHED_WALLS, BMBlocks.DUNGEON_STONE_WALLS,
            BMBlocks.DUNGEON_BRICK_GATES, BMBlocks.DUNGEON_POLISHED_GATES,
            BMBlocks.DUNGEON_BRICK_SLABS, BMBlocks.DUNGEON_TILE_SLABS, BMBlocks.DUNGEON_STONE_SLABS, BMBlocks.DUNGEON_POLISHED_SLABS
    );
    static {
        DUNGEON_PALETTE_STONE_TIER.add(BMBlocks.DUNGEON_EMITTER.block().getKey());
        DUNGEON_PALETTE_STONE_TIER.add(BMBlocks.DUNGEON_CRACKED_BRICK_1.block().getKey());
        DUNGEON_PALETTE_STONE_TIER.add(BMBlocks.DUNGEON_GLOWING_CRACKED_BRICK_1.block().getKey());
        // Dungeon puzzle/hazard blocks (Priority 3 flavor content, see BMBlocks) - same stone-tool tier.
        DUNGEON_PALETTE_STONE_TIER.add(BMBlocks.DUNGEON_ALTERNATOR.block().getKey());
        DUNGEON_PALETTE_STONE_TIER.add(BMBlocks.DUNGEON_SPIKE_TRAP.block().getKey());
        DUNGEON_PALETTE_STONE_TIER.add(BMBlocks.DUNGEON_SPIKES.block().getKey());
    }

    public static List<ResourceKey<Block>> DUNGEON_PALETTE_METAL_TIER = flattenDungeonFamilies(BMBlocks.DUNGEON_METAL);

    // T5 altar capstone eligibility (see BMBlockTagProvider) - the dungeon_metal family (base + all
    // 4 Will reskins), matching 1.20.1's t5_capstones tag which accepted all 5 of its equivalent
    // "hellforgedblock" variants. Kept separate from DUNGEON_PALETTE_METAL_TIER above even though
    // they're currently identical - that list is about mining-tool tier, this one's about altar-tier
    // semantics, and the two concerns shouldn't be coupled just because they happen to match today.
    public static List<ResourceKey<Block>> DUNGEON_METAL = flattenDungeonFamilies(BMBlocks.DUNGEON_METAL);
}
