package wayoftime.bloodmagic.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.common.block.BMBlocks;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.structures.StoneToOreProcessor}: a
 * {@link StructureProcessor} that randomly swaps placed {@code dungeon_stone} blocks for
 * {@code dungeon_ore} while a room's NBT template is being stamped down, at a rate controlled by
 * the room's {@code oreDensity}. Never actually round-tripped through {@link #CODEC} (constructed
 * directly in Java, same as upstream) - {@link #getType()} just needs to return SOME registered
 * {@link StructureProcessorType} for the abstract method contract, and upstream's choice of
 * reusing vanilla's unrelated {@code BLOCK_ROT} type (rather than registering a new custom type)
 * is preserved as-is.
 */
public class StoneToOreProcessor extends StructureProcessor {
    public static final Codec<StoneToOreProcessor> CODEC = Codec.FLOAT.fieldOf("integrity").orElse(1.0F)
            .xmap(StoneToOreProcessor::new, processor -> processor.integrity).codec();
    private final float integrity;

    public StoneToOreProcessor(float integrity) {
        this.integrity = integrity;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings) {
        if (relativeBlockInfo.state().getBlock() != BMBlocks.DUNGEON_STONE.block().get()) {
            return relativeBlockInfo;
        }
        RandomSource random = settings.getRandom(relativeBlockInfo.pos());
        return !(this.integrity >= 1.0F) && !(random.nextFloat() >= this.integrity)
                ? new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), BMBlocks.DUNGEON_ORE.block().get().defaultBlockState(), relativeBlockInfo.nbt())
                : relativeBlockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.BLOCK_ROT;
    }
}
