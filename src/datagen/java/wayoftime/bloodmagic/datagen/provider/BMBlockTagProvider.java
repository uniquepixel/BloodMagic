package wayoftime.bloodmagic.datagen.provider;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.api.BMTags;
import wayoftime.bloodmagic.datagen.BlockGroups;

import java.util.concurrent.CompletableFuture;

public class BMBlockTagProvider extends BlockTagsProvider {
    public BMBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, BloodMagic.MODID, null);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BMTags.Blocks.RUNES)
                .addAll(BlockGroups.RUNE_T1)
                .addAll(BlockGroups.RUNE_T2);

        this.tag(BMTags.Blocks.T3_CAPSTONES)
                .add(Blocks.GLOWSTONE, Blocks.SHROOMLIGHT, Blocks.SEA_LANTERN)
                .add(Blocks.OCHRE_FROGLIGHT, Blocks.PEARLESCENT_FROGLIGHT, Blocks.VERDANT_FROGLIGHT);

        this.tag(BMTags.Blocks.T4_CAPSTONES)
                .addAll(BlockGroups.BLOODSTONE);

        // Fixed from BlockGroups.HELLFORGED_BLOCK: that's this branch's unrelated pre-existing
        // "hellforged_block" placeholder machine block, not 1.20.1's t5-capstone block (which was
        // *also* named "hellforgedblock" there, but got ported here as DUNGEON_METAL to avoid the
        // naming collision - see BMBlocks.DUNGEON_METAL's javadoc). 1.20.1's t5_capstones tag
        // accepted all 5 of its variants (base + 4 Will reskins), matched here by DUNGEON_METAL.
        this.tag(BMTags.Blocks.T5_CAPSTONES)
                .addAll(BlockGroups.DUNGEON_METAL);

        this.tag(BMTags.Blocks.T6_CAPSTONES)
                .addAll(BlockGroups.CRYSTAL_CLUSTER);

        // Deliberately left empty to match 1.20.1's ComponentType.NOTAIR pillar gate (any solid,
        // non-air, non-liquid block - not just a fixed list) - AltarUtil#getTier's pillar check
        // already falls back to an isFaceSturdy(UP)/isFaceSturdy(DOWN) test (true for essentially any
        // solid full block, false for air and fluids, matching NOTAIR's intent exactly) whenever this
        // tag has zero entries, so populating it with a hand-picked block list (previously just
        // minecraft:stone_bricks) would only make this narrower than 1.20.1, not broader.
        this.tag(BMTags.Blocks.PILLARS);

        this.tag(BMTags.Blocks.SOUL_NETWORK_COMPARATOR)
                .addAll(BlockGroups.BLOODSTONE);

        this.tag(BMTags.Blocks.PULSE_ON_CRAFTING)
                .add(Blocks.REDSTONE_LAMP, Blocks.NOTE_BLOCK);

        this.tag(BMTags.Blocks.STORAGE_BLOCKS_HELLFORGED)
                .addAll(BlockGroups.HELLFORGED_BLOCK);

        this.tag(BlockTags.BEACON_BASE_BLOCKS)
                .addAll(BlockGroups.HELLFORGED_BLOCK);

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(BMBlocks.BLOOD_ALTAR.block().getKey(), BMBlocks.BLOOD_TANK.block().getKey(), BMBlocks.ALCHEMY_TABLE.block().getKey(), BMBlocks.TELEPOSER.block().getKey(), BMBlocks.INCENSE_ALTAR.block().getKey(), BMBlocks.ITEM_ROUTER.block().getKey(), BMBlocks.MASTER_ROUTING_NODE.block().getKey(), BMBlocks.INPUT_ROUTING_NODE.block().getKey(), BMBlocks.OUTPUT_ROUTING_NODE.block().getKey(), BMBlocks.DEMON_CRUCIBLE.block().getKey(), BMBlocks.DEMON_CRYSTALLIZER.block().getKey(), BMBlocks.DEMON_PYLON.block().getKey(), BMBlocks.SHAPED_CHARGE.block().getKey(), BMBlocks.DEFORESTER_CHARGE.block().getKey(), BMBlocks.VEINMINE_CHARGE.block().getKey(), BMBlocks.FUNGAL_CHARGE.block().getKey());

        this.tag(BlockTags.NEEDS_STONE_TOOL)
                .add(BMBlocks.BLOOD_ALTAR.block().getKey(), BMBlocks.BLOOD_TANK.block().getKey(), BMBlocks.ALCHEMY_TABLE.block().getKey(), BMBlocks.TELEPOSER.block().getKey(), BMBlocks.INCENSE_ALTAR.block().getKey(), BMBlocks.ITEM_ROUTER.block().getKey(), BMBlocks.MASTER_ROUTING_NODE.block().getKey(), BMBlocks.INPUT_ROUTING_NODE.block().getKey(), BMBlocks.OUTPUT_ROUTING_NODE.block().getKey(), BMBlocks.DEMON_CRUCIBLE.block().getKey(), BMBlocks.DEMON_CRYSTALLIZER.block().getKey(), BMBlocks.DEMON_PYLON.block().getKey(), BMBlocks.SHAPED_CHARGE.block().getKey(), BMBlocks.DEFORESTER_CHARGE.block().getKey(), BMBlocks.VEINMINE_CHARGE.block().getKey(), BMBlocks.FUNGAL_CHARGE.block().getKey());

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(BMBlocks.IMPERFECT_RITUAL_BLOCK.block().getKey())
                .add(BMBlocks.MASTER_RITUAL_STONE.block().getKey())
                .add(BMBlocks.RITUAL_STONE_BLANK.block().getKey())
                .add(BMBlocks.RITUAL_STONE_WATER.block().getKey())
                .add(BMBlocks.RITUAL_STONE_FIRE.block().getKey())
                .add(BMBlocks.RITUAL_STONE_EARTH.block().getKey())
                .add(BMBlocks.RITUAL_STONE_AIR.block().getKey())
                .add(BMBlocks.RITUAL_STONE_DUSK.block().getKey())
                .add(BMBlocks.RITUAL_STONE_DAWN.block().getKey())
                .addAll(BlockGroups.BLOODSTONE)
                .addAll(BlockGroups.HELLFORGED_BLOCK)
                .addAll(BlockGroups.CRYSTAL_CLUSTER)
                .addAll(BlockGroups.RUNE_T1)
                .addAll(BlockGroups.RUNE_T2);

        this.tag(BlockTags.NEEDS_STONE_TOOL)
                .add(BMBlocks.IMPERFECT_RITUAL_BLOCK.block().getKey())
                .add(BMBlocks.MASTER_RITUAL_STONE.block().getKey())
                .add(BMBlocks.RITUAL_STONE_BLANK.block().getKey())
                .add(BMBlocks.RITUAL_STONE_WATER.block().getKey())
                .add(BMBlocks.RITUAL_STONE_FIRE.block().getKey())
                .add(BMBlocks.RITUAL_STONE_EARTH.block().getKey())
                .add(BMBlocks.RITUAL_STONE_AIR.block().getKey())
                .add(BMBlocks.RITUAL_STONE_DUSK.block().getKey())
                .add(BMBlocks.RITUAL_STONE_DAWN.block().getKey())
                .addAll(BlockGroups.BLOODSTONE)
                .addAll(BlockGroups.CRYSTAL_CLUSTER)
                .addAll(BlockGroups.RUNE_T1);

        this.tag(BlockTags.NEEDS_IRON_TOOL)
                .addAll(BlockGroups.HELLFORGED_BLOCK);

        this.tag(Tags.Blocks.NEEDS_NETHERITE_TOOL)
                .addAll(BlockGroups.RUNE_T2);

        // Demon Dungeon decorative block palette (see BMBlocks) - stone-tool tier matches
        // dungeon_stone/dungeon_properties (strength(2,5)/SoundType.STONE); the dungeon_metal family
        // is strength(5,6)/SoundType.METAL, same tier as HELLFORGED_BLOCK above, so it needs iron
        // instead.
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .addAll(BlockGroups.DUNGEON_PALETTE_STONE_TIER)
                .addAll(BlockGroups.DUNGEON_PALETTE_METAL_TIER);

        this.tag(BlockTags.NEEDS_STONE_TOOL)
                .addAll(BlockGroups.DUNGEON_PALETTE_STONE_TIER);

        this.tag(BlockTags.NEEDS_IRON_TOOL)
                .addAll(BlockGroups.DUNGEON_PALETTE_METAL_TIER);

        // The Mimic (Priority 1 flavor content, see BMBlocks) - stone-tool tier matches upstream's
        // strength(2.0f)/SoundType.METAL, same tags upstream gives both variants.
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(BMBlocks.MIMIC.block().getKey(), BMBlocks.ETHEREAL_MIMIC.block().getKey());

        this.tag(BlockTags.NEEDS_STONE_TOOL)
                .add(BMBlocks.MIMIC.block().getKey(), BMBlocks.ETHEREAL_MIMIC.block().getKey());

        // Ported from 1.20.1's data/bloodmagic/tags/blocks/mundane_block.json (#forge:cobblestone,
        // #forge:stone, #minecraft:sand, #minecraft:dirt, minecraft:gravel, minecraft:netherrack) -
        // see BMTags.Blocks.MUNDANE_BLOCK's javadoc for why this exists (gates the Voiding
        // anointment). Listed directly rather than via the "c:stones"/"c:cobblestones"/"c:sands"
        // common tags: those are only populated by NeoForge's own (already-built) data at runtime,
        // not by anything a datagen run in this project can see, so referencing them here fails
        // TagsProvider's "missing reference" validation during runData.
        this.tag(BMTags.Blocks.MUNDANE_BLOCK)
                .add(Blocks.STONE, Blocks.COBBLESTONE, Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE)
                .add(Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE)
                .add(Blocks.SAND, Blocks.RED_SAND)
                .add(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.ROOTED_DIRT, Blocks.GRASS_BLOCK, Blocks.MYCELIUM)
                .add(Blocks.GRAVEL, Blocks.NETHERRACK);

        // Teleposer block-swap guard, ported from 1.20.1's data/bloodmagic/tags/blocks/
        // telepose_blacklist.json (#minecraft:portals/doors/beds expanded to their individual
        // blocks, and #forge:immovable/#forge:relocation_not_supported omitted - both "required":
        // false in 1.20.1 - since none of these vanilla/cross-mod tags are referenceable from local
        // datagen validation here; see the MUNDANE_BLOCK comment above for the same constraint).
        this.tag(BMTags.Blocks.TELEPOSE_BLOCK_BLACKLIST)
                .add(Blocks.BEDROCK, Blocks.END_PORTAL_FRAME, Blocks.PISTON_HEAD, Blocks.MOVING_PISTON)
                .add(BMBlocks.ALCHEMY_TABLE.block().get())
                .add(Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.END_GATEWAY)
                .add(Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.BIRCH_DOOR, Blocks.JUNGLE_DOOR, Blocks.ACACIA_DOOR, Blocks.DARK_OAK_DOOR)
                .add(Blocks.MANGROVE_DOOR, Blocks.CHERRY_DOOR, Blocks.BAMBOO_DOOR, Blocks.CRIMSON_DOOR, Blocks.WARPED_DOOR, Blocks.IRON_DOOR)
                .add(Blocks.WHITE_BED, Blocks.ORANGE_BED, Blocks.MAGENTA_BED, Blocks.LIGHT_BLUE_BED, Blocks.YELLOW_BED, Blocks.LIME_BED, Blocks.PINK_BED, Blocks.GRAY_BED)
                .add(Blocks.LIGHT_GRAY_BED, Blocks.CYAN_BED, Blocks.PURPLE_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.GREEN_BED, Blocks.RED_BED, Blocks.BLACK_BED);
    }
}
