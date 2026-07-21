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

        this.tag(BMTags.Blocks.T5_CAPSTONES)
                .addAll(BlockGroups.HELLFORGED_BLOCK);

        this.tag(BMTags.Blocks.T6_CAPSTONES)
                .addAll(BlockGroups.CRYSTAL_CLUSTER);

        this.tag(BMTags.Blocks.PILLARS)
                .add(Blocks.STONE_BRICKS); // TODO implement empty pillar tag = 1.20 behaviour-ish. perhaps isFaceSturdy shenanigans can help here too

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
    }
}
