package wayoftime.bloodmagic.common.will;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;

import java.util.HashMap;
import java.util.Map;

/**
 * Simplified take on the 1.20.1 WillWorld/WillChunk ambient aura system: a flat per-chunk Will
 * amount per type, with no "base" regeneration value or dirty-chunk-tracking optimization - just
 * a straightforward per-dimension SavedData map, following the exact pattern already used for
 * Soul Networks in {@link wayoftime.bloodmagic.common.world.BMSavedData}.
 */
public class WillWorldData extends SavedData {
    public static final String ID = "bloodmagic_will";

    private final Map<Long, Map<EnumWillType, Double>> chunkWill = new HashMap<>();

    public double getWill(ChunkPos pos, EnumWillType type) {
        Map<EnumWillType, Double> map = chunkWill.get(pos.toLong());
        return map == null ? 0 : map.getOrDefault(type, 0D);
    }

    public double addWill(ChunkPos pos, EnumWillType type, double amount) {
        Map<EnumWillType, Double> map = chunkWill.computeIfAbsent(pos.toLong(), k -> new HashMap<>());
        double newAmount = map.getOrDefault(type, 0D) + amount;
        map.put(type, newAmount);
        setDirty();
        return newAmount;
    }

    public double drainWill(ChunkPos pos, EnumWillType type, double amount) {
        Map<EnumWillType, Double> map = chunkWill.get(pos.toLong());
        if (map == null) {
            return 0;
        }
        double current = map.getOrDefault(type, 0D);
        double drained = Math.min(current, amount);
        map.put(type, current - drained);
        setDirty();
        return drained;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<Long, Map<EnumWillType, Double>> entry : chunkWill.entrySet()) {
            CompoundTag chunkTag = new CompoundTag();
            chunkTag.putLong("pos", entry.getKey());

            CompoundTag willTag = new CompoundTag();
            for (Map.Entry<EnumWillType, Double> willEntry : entry.getValue().entrySet()) {
                willTag.putDouble(willEntry.getKey().getSerializedName(), willEntry.getValue());
            }
            chunkTag.put("will", willTag);

            list.add(chunkTag);
        }
        tag.put("chunks", list);
        return tag;
    }

    public static WillWorldData load(CompoundTag tag, HolderLookup.Provider registries) {
        WillWorldData data = new WillWorldData();
        ListTag list = tag.getList("chunks", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag chunkTag = list.getCompound(i);
            long pos = chunkTag.getLong("pos");
            CompoundTag willTag = chunkTag.getCompound("will");

            Map<EnumWillType, Double> map = new HashMap<>();
            for (EnumWillType type : EnumWillType.values()) {
                if (willTag.contains(type.getSerializedName())) {
                    map.put(type, willTag.getDouble(type.getSerializedName()));
                }
            }
            data.chunkWill.put(pos, map);
        }

        return data;
    }
}
