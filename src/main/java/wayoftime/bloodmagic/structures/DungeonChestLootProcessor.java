package wayoftime.bloodmagic.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.common.loot.BMLootTables;

/**
 * The 48 ported dungeon room NBT templates place plain {@code minecraft:chest} blocks for loot, but
 * none of them carry a baked-in {@code LootTable} reference (unlike vanilla's own structures, e.g.
 * ancient cities, which embed the loot table id directly in the chest's exported block-entity NBT).
 * {@link wayoftime.bloodmagic.common.loot.BMLootTables#DEMON_VAULT} was defined for exactly this but
 * was never actually wired into placement - every dungeon chest generated empty. This processor
 * stamps the loot table (and a placement-seeded {@code LootTableSeed}) onto every chest as it's
 * placed, the same way vanilla structures do it.
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
        if (tag.contains("LootTable")) {
            return relativeBlockInfo;
        }

        tag.putString("LootTable", BMLootTables.DEMON_VAULT.location().toString());
        tag.putLong("LootTableSeed", settings.getRandom(relativeBlockInfo.pos()).nextLong());
        return new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), relativeBlockInfo.state(), tag);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.BLOCK_ROT;
    }
}
