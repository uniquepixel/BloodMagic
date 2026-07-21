package wayoftime.bloodmagic.common.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.BloodMagic;

/**
 * Full-fidelity port of 1.20.1's
 * {@code wayoftime.bloodmagic.common.dimension.DungeonDimensionHelper}. The dungeon dimension
 * itself is entirely data-driven (see {@code data/bloodmagic/dimension/dungeon.json}) and is
 * already loaded by the server the moment the datapack registers it - this is just a lookup
 * helper, not a dimension-creation routine.
 */
public class DungeonDimensionHelper {
    private static final ResourceKey<Level> DUNGEON_KEY = ResourceKey.create(Registries.DIMENSION, BloodMagic.rl("dungeon"));

    public static ServerLevel getDungeonWorld(Level world) {
        return world.getServer().getLevel(DUNGEON_KEY);
    }
}
