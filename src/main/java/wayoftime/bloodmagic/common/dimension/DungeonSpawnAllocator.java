package wayoftime.bloodmagic.common.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * New support class (no 1.20.1 equivalent): 1.20.1 spaced separate dungeon instances apart in the
 * shared void dungeon dimension via {@code NetworkHelper.getSpawnPositionOfDungeon()}/
 * {@code incrementDungeonCounter()}, a global counter this branch doesn't have a home for (that
 * whole networking/helper class isn't ported). This is a minimal drop-in replacement: a per-dungeon-
 * dimension {@link SavedData} holding just a counter, so every new dungeon a player generates gets
 * its own non-overlapping slot along the X axis (spaced far enough apart - 512 blocks - that no
 * dungeon's room graph can ever cross into its neighbor's).
 */
public class DungeonSpawnAllocator extends SavedData {
    private static final String ID = "bloodmagic_dungeon_spawns";
    private static final int SPACING = 512;
    private static final int FIXED_Y = 100;

    private int counter = 0;

    public static BlockPos allocateSpawnPosition(ServerLevel dungeonWorld) {
        DungeonSpawnAllocator data = dungeonWorld.getDataStorage().computeIfAbsent(new SavedData.Factory<>(DungeonSpawnAllocator::new, DungeonSpawnAllocator::load), ID);
        int slot = data.counter++;
        data.setDirty();
        return new BlockPos(slot * SPACING, FIXED_Y, 0);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("counter", counter);
        return tag;
    }

    public static DungeonSpawnAllocator load(CompoundTag tag, HolderLookup.Provider registries) {
        DungeonSpawnAllocator data = new DungeonSpawnAllocator();
        data.counter = tag.getInt("counter");
        return data;
    }
}
