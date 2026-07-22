package wayoftime.bloodmagic.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.common.loot.BMLootTables;

import java.util.List;

/**
 * Restores 1.20.1's per-room-type dungeon chest loot. Room-type -&gt; loot-table selection isn't
 * driven by any Java-side room "type"/"category" field on {@link DungeonRoom} - that class only
 * tracks door-pool wiring, not loot. Instead, exactly like vanilla's own generated structures (e.g.
 * ancient cities), each of the 48 ported room NBT templates already bakes the correct {@code
 * bloodmagic:chests/<category>/<name>} {@code LootTable} string directly onto its chest block
 * entities - inherited unchanged from 1.20.1's assets pipeline when the binary {@code .nbt} files
 * were copied over. 44 of the 48 templates carry such a tag on at least one chest (verified by
 * decompressing every template and scanning for a {@code LootTable} tag); collectively they
 * reference all 25 of {@link BMLootTables}'s room-themed tables. Those 25 tables simply didn't exist
 * as loot_table JSON on this branch (see {@code DungeonChestLoot}), so every reference was dangling
 * and every chest generated empty - which is what the "collapsed to one table" symptom actually was,
 * <i>not</i> this processor stamping something onto every chest.
 * <p>
 * This processor still respects that baked-in tag first and foremost (never overwrites it). Only for
 * the rare chest with no baked-in tag at all (currently just the disused, pool-unreferenced root
 * {@code four_way_corridor_loot.nbt} - the loadable-but-never-placed duplicate of {@code
 * standard/four_way_corridor_loot.nbt}, which DOES carry tags) does it fall back to a
 * placement-seeded uniform-random pick across all 25 tables in {@link BMLootTables#DUNGEON_CHEST_TABLES},
 * so even an untagged chest still gets loot variety instead of one hard-coded table. {@link
 * BMLootTables#DEMON_VAULT} is intentionally never used here - it belongs to an unrelated structure.
 */
public class DungeonChestLootProcessor extends StructureProcessor {
    public static final Codec<DungeonChestLootProcessor> CODEC = Codec.unit(DungeonChestLootProcessor::new);

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings) {
        if (relativeBlockInfo.state().getBlock() != Blocks.CHEST) {
            return relativeBlockInfo;
        }

        CompoundTag tag = relativeBlockInfo.nbt() != null ? relativeBlockInfo.nbt().copy() : new CompoundTag();
        RandomSource random = settings.getRandom(relativeBlockInfo.pos());
        if (tag.contains("LootTable")) {
            // Already carries the correct room-specific table (baked into the room's NBT template) -
            // just make sure it rolls its own seed on each placement instead of reusing whatever (if
            // anything) was baked into the template, matching vanilla's per-placement behaviour.
            if (!tag.contains("LootTableSeed")) {
                tag.putLong("LootTableSeed", random.nextLong());
                return new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), relativeBlockInfo.state(), tag);
            }
            return relativeBlockInfo;
        }

        List<ResourceKey<LootTable>> tables = BMLootTables.DUNGEON_CHEST_TABLES;
        ResourceKey<LootTable> fallback = tables.get(random.nextInt(tables.size()));
        tag.putString("LootTable", fallback.location().toString());
        tag.putLong("LootTableSeed", random.nextLong());
        return new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), relativeBlockInfo.state(), tag);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.BLOCK_ROT;
    }
}
