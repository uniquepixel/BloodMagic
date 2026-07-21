package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;
import wayoftime.bloodmagic.api.datacomponent.Binding;
import wayoftime.bloodmagic.api.helper.SoulNetworkHelper;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumReaderBoundaries;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.datacomponent.SoulNetwork;
import wayoftime.bloodmagic.common.item.ItemActivationCrystal;
import wayoftime.bloodmagic.common.ritual.RitualHelper;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.util.ChatUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Full-fidelity port of 1.20.1's {@code TileMasterRitualStone}: owns/tracks the ritual bound to
 * this stone, its player-customizable {@link AreaDescriptor} ranges, redstone activation state,
 * and the tick loop that re-validates the rune pattern and runs the ritual. The old
 * {@code RitualEvent} cancelable bus events (fired around activate/run/stop) aren't ported yet -
 * they're an addon-mod extensibility hook, not core behavior.
 */
public class MasterRitualStoneTile extends BaseTile implements IMasterRitualStone {
    private final Map<String, AreaDescriptor> modableRangeMap = new java.util.HashMap<>();
    private List<EnumWillType> currentActiveWillConfig = new ArrayList<>();

    @Nullable
    private UUID owner;
    @Nullable
    private Ritual currentRitual;
    private boolean active;
    private boolean redstoned;
    private int activeTime;
    private int cooldown;
    private Direction direction = Direction.NORTH;

    public MasterRitualStoneTile(BlockPos pos, BlockState state) {
        super(BMTiles.MASTER_RITUAL_STONE_TYPE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MasterRitualStoneTile tile) {
        if (level.isClientSide) {
            return;
        }

        if (tile.isPowered() && tile.isActive()) {
            tile.active = false;
            tile.redstoned = true;
            tile.stopRitual(Ritual.BreakType.REDSTONE);
            return;
        }

        if (!tile.isActive() && !tile.isPowered() && tile.redstoned && tile.currentRitual != null) {
            tile.active = true;
            ItemStack crystalStack = ItemActivationCrystal.CrystalType.getStack(tile.currentRitual.getCrystalLevel());
            if (tile.owner != null) {
                crystalStack.set(BMDataComponents.BINDING, new Binding(tile.owner, ""));
            }
            tile.activateRitual(crystalStack, null, tile.currentRitual);
            tile.redstoned = false;
        }

        if (tile.currentRitual != null && tile.active) {
            if (tile.activeTime % tile.currentRitual.getRefreshTime() == 0) {
                tile.performRitual(level, pos);
            }

            tile.activeTime++;
        }
    }

    public boolean isPowered() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    @Override
    public boolean activateRitual(ItemStack activationCrystal, @Nullable Player activator, Ritual ritual) {
        if (activator instanceof FakePlayer) {
            return false;
        }

        if (level == null || level.isClientSide || ritual == null || !(activationCrystal.getItem() instanceof ItemActivationCrystal crystalItem)) {
            return false;
        }

        Binding binding = activationCrystal.getOrDefault(BMDataComponents.BINDING, Binding.EMPTY);
        if (binding.isEmpty()) {
            if (activator != null) {
                activator.displayClientMessage(Component.translatable("chat.bloodmagic.ritual.notValid"), true);
            }
            return false;
        }

        if (!RitualHelper.canCrystalActivate(ritual, crystalItem.getCrystalLevel())) {
            return false;
        }

        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(binding);
        if (network == null) {
            return false;
        }

        if (!redstoned && network.getCurrentEssence() < ritual.getActivationCost() && activator != null && !activator.isCreative()) {
            activator.displayClientMessage(Component.translatable("chat.bloodmagic.ritual.weak"), true);
            return false;
        }

        if (currentRitual != null) {
            currentRitual.stopRitual(this, Ritual.BreakType.ACTIVATE);
        }

        if (ritual.activateRitual(this, activator, binding.uuid())) {
            if (!redstoned && activator != null && !activator.isCreative()) {
                network.syphon(ticket(ritual.getActivationCost()));
            }

            if (activator != null) {
                activator.displayClientMessage(Component.translatable("chat.bloodmagic.ritual.activate"), true);
            }

            this.active = true;
            this.owner = binding.uuid();
            this.currentRitual = ritual;

            if (!checkBlockRanges(ritual.getModableRangeMap())) {
                addBlockRanges(ritual.getModableRangeMap());
            }

            setChanged();
            return true;
        }

        return false;
    }

    @Override
    public void performRitual(Level level, BlockPos pos) {
        if (level.isClientSide || currentRitual == null) {
            return;
        }

        if (RitualHelper.checkValidRitual(level, pos, currentRitual, direction)) {
            if (!checkBlockRanges(currentRitual.getModableRangeMap())) {
                addBlockRanges(currentRitual.getModableRangeMap());
            }

            currentRitual.performRitual(this);
        } else {
            stopRitual(Ritual.BreakType.BREAK_STONE);
        }
    }

    @Override
    public void stopRitual(Ritual.BreakType breakType) {
        if (level == null || level.isClientSide || currentRitual == null) {
            return;
        }

        currentRitual.stopRitual(this, breakType);
        if (breakType != Ritual.BreakType.REDSTONE) {
            this.currentRitual = null;
            this.active = false;
            this.activeTime = 0;
        }

        setChanged();
    }

    @Override
    public int getCooldown() {
        return cooldown;
    }

    @Override
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
        setChanged();
    }

    @Override
    public int getRunningTime() {
        return activeTime;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    public SoulNetwork getOwnerNetwork() {
        return owner == null ? null : SoulNetworkHelper.getSoulNetwork(owner);
    }

    @Override
    public Level getWorldObj() {
        return level;
    }

    @Override
    public BlockPos getMasterBlockPos() {
        return worldPosition;
    }

    @Override
    public String getNextBlockRange(String range) {
        return currentRitual != null ? currentRitual.getNextBlockRange(range) : "";
    }

    @Override
    public void provideInformationOfRitualToPlayer(Player player) {
        if (currentRitual != null) {
            ChatUtil.sendChatNoSpam(player, List.of(currentRitual.provideInformationOfRitualToPlayer(player)));
        }
    }

    @Override
    public void provideInformationOfRangeToPlayer(Player player, String range) {
        if (currentRitual != null && currentRitual.getListOfRanges().contains(range)) {
            ChatUtil.sendChatNoSpam(player, List.of(currentRitual.provideInformationOfRangeToPlayer(player, range)));
        }
    }

    @Override
    public void provideInformationOfWillConfigToPlayer(Player player, List<EnumWillType> typeList) {
        if (!typeList.isEmpty()) {
            MutableComponent combined = Component.empty();
            for (int i = 0; i < typeList.size(); i++) {
                if (i > 0) {
                    combined.append(", ");
                }
                combined.append(Component.translatable("tooltip.bloodmagic.currentBaseType." + typeList.get(i).name().toLowerCase(Locale.ROOT)));
            }

            ChatUtil.sendChatNoSpam(player, List.of(Component.translatable("ritual.bloodmagic.willConfig.set", combined)));
        } else {
            ChatUtil.sendChatNoSpam(player, List.of(Component.translatable("ritual.bloodmagic.willConfig.void")));
        }
    }

    @Override
    public void setActiveWillConfig(Player player, List<EnumWillType> typeList) {
        this.currentActiveWillConfig = new ArrayList<>(typeList);
        setChanged();
    }

    @Override
    public EnumReaderBoundaries setBlockRangeByBounds(Player player, String range, BlockPos offset1, BlockPos offset2) {
        AreaDescriptor descriptor = this.getBlockRange(range);
        if (descriptor == null || currentRitual == null) {
            return EnumReaderBoundaries.NOT_WITHIN_BOUNDARIES;
        }

        EnumReaderBoundaries modificationType = currentRitual.canBlockRangeBeModified(range, descriptor, this, offset1, offset2);
        if (modificationType == EnumReaderBoundaries.SUCCESS) {
            descriptor.modifyAreaByBlockPositions(offset1, offset2);
        }

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        return modificationType;
    }

    @Override
    public List<EnumWillType> getActiveWillConfig() {
        return new ArrayList<>(currentActiveWillConfig);
    }

    @Override
    public AreaDescriptor getBlockRange(String range) {
        return modableRangeMap.get(range);
    }

    @Override
    public void addBlockRange(String range, AreaDescriptor defaultRange) {
        modableRangeMap.putIfAbsent(range, defaultRange.copy());
    }

    @Override
    public void addBlockRanges(Map<String, AreaDescriptor> blockRanges) {
        for (Map.Entry<String, AreaDescriptor> entry : blockRanges.entrySet()) {
            modableRangeMap.putIfAbsent(entry.getKey(), entry.getValue().copy());
        }
    }

    @Override
    public void setBlockRange(String range, AreaDescriptor defaultRange) {
        modableRangeMap.put(range, defaultRange.copy());
    }

    @Override
    public void setBlockRanges(Map<String, AreaDescriptor> blockRanges) {
        for (Map.Entry<String, AreaDescriptor> entry : blockRanges.entrySet()) {
            modableRangeMap.put(entry.getKey(), entry.getValue().copy());
        }
    }

    private boolean checkBlockRanges(Map<String, AreaDescriptor> blockRanges) {
        for (String key : blockRanges.keySet()) {
            if (modableRangeMap.get(key) == null) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Ritual getCurrentRitual() {
        return currentRitual;
    }

    public void setCurrentRitual(Ritual currentRitual) {
        this.currentRitual = currentRitual;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;

        ResourceLocation ritualId = tag.contains("ritual") ? ResourceLocation.parse(tag.getString("ritual")) : null;
        currentRitual = ritualId == null ? null : RitualRegistry.get(ritualId);

        modableRangeMap.clear();
        if (currentRitual != null) {
            addBlockRanges(currentRitual.getModableRangeMap());
            CompoundTag rangesTag = tag.getCompound("ranges");
            for (Map.Entry<String, AreaDescriptor> entry : modableRangeMap.entrySet()) {
                if (rangesTag.contains(entry.getKey())) {
                    entry.getValue().readFromNBT(rangesTag.getCompound(entry.getKey()));
                }
            }
        }

        active = tag.getBoolean("active");
        activeTime = tag.getInt("activeTime");
        direction = Direction.values()[tag.getInt("direction")];
        redstoned = tag.getBoolean("redstoned");
        cooldown = tag.getInt("cooldown");

        currentActiveWillConfig = new ArrayList<>();
        Tag willListTag = tag.get("activeWillConfig");
        if (willListTag instanceof net.minecraft.nbt.ListTag willList) {
            for (int i = 0; i < willList.size(); i++) {
                try {
                    currentActiveWillConfig.add(EnumWillType.valueOf(willList.getString(i)));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (owner != null) {
            tag.putUUID("owner", owner);
        }

        if (currentRitual != null) {
            tag.putString("ritual", currentRitual.getId().toString());

            CompoundTag rangesTag = new CompoundTag();
            for (Map.Entry<String, AreaDescriptor> entry : modableRangeMap.entrySet()) {
                CompoundTag descTag = new CompoundTag();
                entry.getValue().writeToNBT(descTag);
                rangesTag.put(entry.getKey(), descTag);
            }
            tag.put("ranges", rangesTag);
        }

        tag.putBoolean("active", active);
        tag.putInt("activeTime", activeTime);
        tag.putInt("direction", direction.get3DDataValue());
        tag.putBoolean("redstoned", redstoned);
        tag.putInt("cooldown", cooldown);

        net.minecraft.nbt.ListTag willListTag = new net.minecraft.nbt.ListTag();
        for (EnumWillType type : currentActiveWillConfig) {
            willListTag.add(net.minecraft.nbt.StringTag.valueOf(type.name()));
        }
        tag.put("activeWillConfig", willListTag);
    }
}
