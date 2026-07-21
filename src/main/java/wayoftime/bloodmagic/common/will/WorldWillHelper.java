package wayoftime.bloodmagic.common.will;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;

import javax.annotation.Nullable;

/**
 * Per-dimension ambient Will aura, stored per-chunk. Separate from {@link WillHelper}, which
 * tracks Will a player is physically carrying in Soul Gems - this is the passive world-side pool
 * the 1.20.1 branch called WillWorld/WillChunk.
 */
public class WorldWillHelper {
    public static double getWill(Level level, BlockPos pos, EnumWillType type) {
        WillWorldData data = getData(level);
        return data == null ? 0 : data.getWill(new ChunkPos(pos), type);
    }

    public static double addWill(Level level, BlockPos pos, EnumWillType type, double amount) {
        WillWorldData data = getData(level);
        return data == null ? 0 : data.addWill(new ChunkPos(pos), type, amount);
    }

    public static double drainWill(Level level, BlockPos pos, EnumWillType type, double amount) {
        WillWorldData data = getData(level);
        return data == null ? 0 : data.drainWill(new ChunkPos(pos), type, amount);
    }

    @Nullable
    private static WillWorldData getData(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getDataStorage().computeIfAbsent(new SavedData.Factory<>(WillWorldData::new, WillWorldData::load), WillWorldData.ID);
    }
}
