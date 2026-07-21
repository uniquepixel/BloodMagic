package wayoftime.bloodmagic.structures;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.structures.DungeonStructure}. Thin
 * wrapper around vanilla's own {@link StructureTemplate}/{@link StructureTemplateManager} API -
 * the same system vanilla uses to place villages/strongholds - pointed at one of the 48 NBT files
 * under {@code data/bloodmagic/structures/}.
 */
public class DungeonStructure {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ResourceLocation resource;

    public DungeonStructure(ResourceLocation resource) {
        this.resource = resource;
    }

    public boolean placeStructureAtPosition(RandomSource rand, StructurePlaceSettings settings, ServerLevel world, BlockPos pos) {
        if (pos == null) {
            return false;
        }

        StructureTemplateManager templatemanager = world.getStructureManager();

        Optional<StructureTemplate> template = templatemanager.get(resource);

        if (template.isEmpty()) {
            LOGGER.warn("Invalid dungeon structure template for location: {}", resource);
            return false;
        }

        BlockPos offset = StructureTemplate.calculateRelativePosition(settings, new BlockPos(0, 0, 0));
        BlockPos finalPos = pos.offset(offset);

        template.get().placeInWorld(world, finalPos, finalPos, settings, rand, 2);

        return true;
    }

    public DungeonStructure copy() {
        return new DungeonStructure(resource);
    }
}
