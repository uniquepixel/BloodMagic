package wayoftime.bloodmagic.api.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.will.WorldWillHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Full-fidelity port of the 1.20.1 abstract Ritual class: player-customizable named
 * {@link AreaDescriptor} ranges (with volume/radius caps a ritual can widen based on ambient
 * Will), a rune multiblock pattern via {@link #gatherComponents}, and Will-scaled costs/behavior.
 * <p>
 * Adapted from the original in one respect: where 1.20.1 snapshots ambient Will into a
 * per-position {@code DemonWillHolder} (types: default/corrosive/destructive/steadfast/vengeful,
 * fed by a dedicated demon-aura world-save), this queries {@link WorldWillHelper} directly (this
 * branch's per-chunk ambient-Will aura, using the same five {@link EnumWillType} values) instead
 * of introducing a second, redundant Will-storage system.
 */
public abstract class Ritual {
    protected final Map<String, AreaDescriptor> modableRangeMap = new HashMap<>();
    protected final Map<String, Integer> volumeRangeMap = new HashMap<>();
    protected final Map<String, Integer> horizontalRangeMap = new HashMap<>();
    protected final Map<String, Integer> verticalRangeMap = new HashMap<>();

    private final ResourceLocation id;
    private final int crystalLevel;
    private final int activationCost;
    private final String unlocalizedName;

    protected Ritual(ResourceLocation id, int crystalLevel, int activationCost, String unlocalizedName) {
        this.id = id;
        this.crystalLevel = crystalLevel;
        this.activationCost = activationCost;
        this.unlocalizedName = unlocalizedName;
    }

    public void readFromNBT(CompoundTag tag) {
        ListTag tags = tag.getList("areas", 10);
        if (tags.isEmpty()) {
            return;
        }

        for (int i = 0; i < tags.size(); i++) {
            CompoundTag newTag = tags.getCompound(i);
            String rangeKey = newTag.getString("key");

            CompoundTag storedTag = newTag.getCompound("area");
            AreaDescriptor desc = this.getBlockRange(rangeKey);
            if (desc != null) {
                desc.readFromNBT(storedTag);
            }
        }
    }

    public void writeToNBT(CompoundTag tag) {
        ListTag tags = new ListTag();

        for (Map.Entry<String, AreaDescriptor> entry : modableRangeMap.entrySet()) {
            CompoundTag newTag = new CompoundTag();
            newTag.putString("key", entry.getKey());
            CompoundTag storedTag = new CompoundTag();

            entry.getValue().writeToNBT(storedTag);

            newTag.put("area", storedTag);

            tags.add(newTag);
        }

        tag.put("areas", tags);
    }

    /**
     * Called when a player attempts to activate the ritual on a valid Master Ritual Stone.
     */
    public boolean activateRitual(IMasterRitualStone masterRitualStone, Player player, java.util.UUID owner) {
        return true;
    }

    /**
     * Called every {@link #getRefreshTime()} ticks while active.
     */
    public abstract void performRitual(IMasterRitualStone masterRitualStone);

    public void stopRitual(IMasterRitualStone masterRitualStone, BreakType breakType) {
    }

    public abstract int getRefreshCost();

    public int getRefreshTime() {
        return 20;
    }

    public void addBlockRange(String range, AreaDescriptor defaultRange) {
        modableRangeMap.put(range, defaultRange);
    }

    public AreaDescriptor getBlockRange(String range) {
        return modableRangeMap.get(range);
    }

    public List<String> getListOfRanges() {
        return new ArrayList<>(modableRangeMap.keySet());
    }

    public String getNextBlockRange(String range) {
        List<String> rangeList = getListOfRanges();

        if (rangeList.isEmpty()) {
            return "";
        }

        if (!rangeList.contains(range)) {
            return rangeList.get(0);
        }

        boolean hasMatch = false;
        for (String rangeCheck : rangeList) {
            if (hasMatch) {
                return rangeCheck;
            } else if (rangeCheck.equals(range)) {
                hasMatch = true;
            }
        }

        return rangeList.get(0);
    }

    public EnumReaderBoundaries canBlockRangeBeModified(String range, AreaDescriptor descriptor, IMasterRitualStone master, BlockPos offset1, BlockPos offset2) {
        List<EnumWillType> willConfig = master.getActiveWillConfig();
        int maxVolume = getMaxVolumeForRange(range, willConfig, master.getWorldObj(), master.getMasterBlockPos());
        int maxVertical = getMaxVerticalRadiusForRange(range, willConfig, master.getWorldObj(), master.getMasterBlockPos());
        int maxHorizontal = getMaxHorizontalRadiusForRange(range, willConfig, master.getWorldObj(), master.getMasterBlockPos());

        return (maxVolume <= 0 || descriptor.getVolumeForOffsets(offset1, offset2) <= maxVolume)
                ? descriptor.isWithinRange(offset1, offset2, maxVertical, maxHorizontal) ? EnumReaderBoundaries.SUCCESS : EnumReaderBoundaries.NOT_WITHIN_BOUNDARIES
                : EnumReaderBoundaries.VOLUME_TOO_LARGE;
    }

    protected void setMaximumVolumeAndDistanceOfRange(String range, int volume, int horizontalRadius, int verticalRadius) {
        volumeRangeMap.put(range, volume);
        horizontalRangeMap.put(range, horizontalRadius);
        verticalRangeMap.put(range, verticalRadius);
    }

    public int getMaxVolumeForRange(String range, List<EnumWillType> activeTypes, Level level, BlockPos pos) {
        return volumeRangeMap.getOrDefault(range, 0);
    }

    public int getMaxVerticalRadiusForRange(String range, List<EnumWillType> activeTypes, Level level, BlockPos pos) {
        return verticalRangeMap.getOrDefault(range, 0);
    }

    public int getMaxHorizontalRadiusForRange(String range, List<EnumWillType> activeTypes, Level level, BlockPos pos) {
        return horizontalRangeMap.getOrDefault(range, 0);
    }

    public Component getErrorForBlockRangeOnFail(Player player, String range, IMasterRitualStone master, BlockPos offset1, BlockPos offset2) {
        AreaDescriptor descriptor = this.getBlockRange(range);
        if (descriptor == null) {
            return Component.translatable("ritual.bloodmagic.blockRange.tooBig", "?");
        }

        List<EnumWillType> willConfig = master.getActiveWillConfig();
        int maxVolume = this.getMaxVolumeForRange(range, willConfig, master.getWorldObj(), master.getMasterBlockPos());
        int maxVertical = this.getMaxVerticalRadiusForRange(range, willConfig, master.getWorldObj(), master.getMasterBlockPos());
        int maxHorizontal = this.getMaxHorizontalRadiusForRange(range, willConfig, master.getWorldObj(), master.getMasterBlockPos());

        if (maxVolume > 0 && descriptor.getVolumeForOffsets(offset1, offset2) > maxVolume) {
            return Component.translatable("ritual.bloodmagic.blockRange.tooBig", maxVolume);
        } else {
            return Component.translatable("ritual.bloodmagic.blockRange.tooFar", maxVertical, maxHorizontal);
        }
    }

    public Component[] provideInformationOfRitualToPlayer(Player player) {
        return new Component[]{Component.translatable(this.getTranslationKey() + ".info")};
    }

    public Component provideInformationOfRangeToPlayer(Player player, String range) {
        if (getListOfRanges().contains(range)) {
            return Component.translatable(this.getTranslationKey() + "." + range + ".info");
        } else {
            return Component.translatable("ritual.bloodmagic.blockRange.noRange");
        }
    }

    public abstract void gatherComponents(Consumer<RitualComponent> components);

    protected final void addRune(Consumer<RitualComponent> components, int offset1, int y, int offset2, EnumRuneType rune) {
        components.accept(new RitualComponent(new BlockPos(offset1, y, offset2), rune));
    }

    protected final void addOffsetRunes(Consumer<RitualComponent> components, int offset1, int offset2, int y, EnumRuneType rune) {
        addRune(components, offset1, y, offset2, rune);
        addRune(components, offset2, y, offset1, rune);
        addRune(components, offset1, y, -offset2, rune);
        addRune(components, -offset2, y, offset1, rune);
        addRune(components, -offset1, y, offset2, rune);
        addRune(components, offset2, y, -offset1, rune);
        addRune(components, -offset1, y, -offset2, rune);
        addRune(components, -offset2, y, -offset1, rune);
    }

    protected final void addCornerRunes(Consumer<RitualComponent> components, int offset, int y, EnumRuneType rune) {
        addRune(components, offset, y, offset, rune);
        addRune(components, offset, y, -offset, rune);
        addRune(components, -offset, y, -offset, rune);
        addRune(components, -offset, y, offset, rune);
    }

    protected final void addParallelRunes(Consumer<RitualComponent> components, int offset, int y, EnumRuneType rune) {
        addRune(components, offset, y, 0, rune);
        addRune(components, -offset, y, 0, rune);
        addRune(components, 0, y, -offset, rune);
        addRune(components, 0, y, offset, rune);
    }

    public double getWillRespectingConfig(Level level, BlockPos pos, EnumWillType type, List<EnumWillType> willConfig) {
        return willConfig.contains(type) ? WorldWillHelper.getWill(level, pos, type) : 0;
    }

    public abstract Ritual getNewCopy();

    public ResourceLocation getId() {
        return id;
    }

    public int getCrystalLevel() {
        return crystalLevel;
    }

    public int getActivationCost() {
        return activationCost;
    }

    public String getTranslationKey() {
        return unlocalizedName;
    }

    public Component name() {
        return Component.translatable(getTranslationKey());
    }

    public Map<String, AreaDescriptor> getModableRangeMap() {
        return modableRangeMap;
    }

    public enum BreakType {
        REDSTONE,
        BREAK_MRS,
        BREAK_STONE,
        ACTIVATE,
        DEACTIVATE,
        EXPLOSION,
    }
}
