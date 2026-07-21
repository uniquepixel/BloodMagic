package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.common.will.WorldWillHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of the Yawning Void ({@code RitualYawningVoid}): a quarry
 * that scans its (purely vertical) quarry range from the bottom up. With Steadfast Will active it
 * relocates blocks into the placement range instead of destroying them; with Corrosive Will active
 * and a filter chest present, only blocks matching an item already stored in that chest get voided
 * (a direct item-equality check here, in place of the original's pluggable
 * {@code IItemFilterProvider} filter-item system, which belongs to the Item Routing subsystem this
 * branch hasn't ported in full). Default Will speeds up the pulse rate. Resumes its scan across
 * ticks (capped at 100 block checks per tick) via a cached offset, same as {@link MagnetismRitual}.
 */
public class YawningVoidRitual extends Ritual {
    public static final String PLACEMENT_RANGE = "placementRange";
    public static final String QUARRY_RANGE = "quarryRange";
    public static final String CHEST_RANGE = "chest";

    public static final double RAW_WILL_DRAIN = 0.05;
    public static final double STEADFAST_WILL_DRAIN = 0.05;
    public static final double CORROSIVE_WILL_DRAIN = 0.05;
    public static final int DEFAULT_REFRESH_TIME = 10;

    private int refreshTime = DEFAULT_REFRESH_TIME;

    @Nullable
    private BlockPos lastPos;

    public YawningVoidRitual() {
        super(RitualRegistry.rl("yawning_void"), 0, 5000, "ritual.bloodmagic.yawning_void");
        addBlockRange(PLACEMENT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-1, 1, -1), 3));
        addBlockRange(QUARRY_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-1, -3, -1), 3));
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

        setMaximumVolumeAndDistanceOfRange(PLACEMENT_RANGE, 50, 4, 4);
        setMaximumVolumeAndDistanceOfRange(QUARRY_RANGE, 0, 64, 32);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();

        if (currentEssence < getRefreshCost()) {
            masterRitualStone.causeNausea();
            return;
        }

        BlockPos pos = masterRitualStone.getMasterBlockPos();
        List<EnumWillType> willConfig = masterRitualStone.getActiveWillConfig();

        double rawWill = this.getWillRespectingConfig(level, pos, EnumWillType.DEFAULT, willConfig);
        double steadfastWill = this.getWillRespectingConfig(level, pos, EnumWillType.STEADFAST, willConfig);
        double corrosiveWill = this.getWillRespectingConfig(level, pos, EnumWillType.CORROSIVE, willConfig);

        refreshTime = getRefreshTimeForRawWill(rawWill);
        boolean consumeRawWill = rawWill >= RAW_WILL_DRAIN && refreshTime != DEFAULT_REFRESH_TIME;

        BlockPos replacement = pos;
        boolean replaceNonDestroyed = steadfastWill >= STEADFAST_WILL_DRAIN;
        boolean destroy = !replaceNonDestroyed;
        boolean tryFilter = corrosiveWill >= CORROSIVE_WILL_DRAIN;

        boolean consumeSteadfastWill = steadfastWill >= STEADFAST_WILL_DRAIN;
        boolean consumeCorrosiveWill = corrosiveWill >= CORROSIVE_WILL_DRAIN;

        if ((!consumeSteadfastWill && willConfig.contains(EnumWillType.STEADFAST)) || (!consumeCorrosiveWill && willConfig.contains(EnumWillType.CORROSIVE))) {
            return;
        }

        boolean replace = false;
        if (replaceNonDestroyed) {
            AreaDescriptor placementRange = masterRitualStone.getBlockRange(PLACEMENT_RANGE);
            for (BlockPos offset : placementRange.getContainedPositions(pos)) {
                if (level.isEmptyBlock(offset)) {
                    replacement = offset;
                    replace = true;
                    break;
                }
            }
        }

        int maxBlockChecks = 100;
        int checks = 0;

        AreaDescriptor.Rectangle quarryRange = (AreaDescriptor.Rectangle) masterRitualStone.getBlockRange(QUARRY_RANGE);
        BlockPos minOffset = quarryRange.getMinimumOffset();
        BlockPos maxOffset = quarryRange.getMaximumOffset().offset(-1, -1, -1);

        boolean isDone = false;

        IItemHandler inventory = null;
        boolean doFilter = false;

        if (tryFilter) {
            AreaDescriptor chestRange = masterRitualStone.getBlockRange(CHEST_RANGE);
            BlockPos chestPos = chestRange.getContainedPositions(pos).get(0);
            inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, chestPos, null);
            doFilter = inventory != null;
        }

        if (replace || destroy) {
            if (doFilter) {
                destroy = false;
            }

            int j = maxOffset.getY();
            int i = minOffset.getX();
            int k = minOffset.getZ();

            if (lastPos != null && !lastPos.equals(BlockPos.ZERO)) {
                j = lastPos.getY();
                i = Math.min(maxOffset.getX(), Math.max(i, lastPos.getX()));
                k = Math.min(maxOffset.getZ(), Math.max(k, lastPos.getZ()));
            }

            while (j >= minOffset.getY()) {
                while (i <= maxOffset.getX()) {
                    while (k <= maxOffset.getZ()) {
                        if (checks >= maxBlockChecks || isDone) {
                            this.lastPos = new BlockPos(i, j, k);
                            return;
                        }
                        checks++;

                        BlockPos newPos = pos.offset(i, j, k);
                        BlockState state = level.getBlockState(newPos);

                        if (!state.isAir()) {
                            boolean shouldDestroy = destroy;

                            if (doFilter) {
                                ItemStack checkStack = new ItemStack(state.getBlock());
                                for (int n = 0; n < inventory.getSlots(); n++) {
                                    ItemStack filterStack = inventory.getStackInSlot(n);
                                    if (filterStack.isEmpty()) {
                                        continue;
                                    }

                                    if (ItemStack.isSameItem(filterStack, checkStack)) {
                                        shouldDestroy = true;
                                    }
                                    break;
                                }
                            }

                            if (shouldDestroy) {
                                level.setBlockAndUpdate(newPos, Blocks.AIR.defaultBlockState());
                                masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost()));
                                k++;
                                this.lastPos = new BlockPos(i, j, k);
                                isDone = true;
                                consumeSteadfastWill = false;
                            } else if (replace) {
                                swapLocations(level, newPos, replacement);
                                masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost()));
                                k++;
                                this.lastPos = new BlockPos(i, j, k);
                                isDone = true;
                            } else {
                                k++;
                            }
                        } else {
                            k++;
                        }
                    }
                    i++;
                    k = minOffset.getZ();
                }
                j--;
                i = minOffset.getX();
            }

            this.lastPos = new BlockPos(i, maxOffset.getY(), k);
        }

        if (isDone) {
            if (consumeRawWill) {
                WorldWillHelper.drainWill(level, pos, EnumWillType.DEFAULT, RAW_WILL_DRAIN);
            }

            if (consumeCorrosiveWill) {
                WorldWillHelper.drainWill(level, pos, EnumWillType.CORROSIVE, CORROSIVE_WILL_DRAIN);
            }

            if (consumeSteadfastWill) {
                WorldWillHelper.drainWill(level, pos, EnumWillType.STEADFAST, STEADFAST_WILL_DRAIN);
            }
        }
    }

    private static void swapLocations(Level level, BlockPos a, BlockPos b) {
        BlockState stateA = level.getBlockState(a);
        BlockState stateB = level.getBlockState(b);
        level.setBlockAndUpdate(a, stateB);
        level.setBlockAndUpdate(b, stateA);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        if (tag.contains("lastX")) {
            lastPos = new BlockPos(tag.getInt("lastX"), tag.getInt("lastY"), tag.getInt("lastZ"));
        }
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (lastPos != null) {
            tag.putInt("lastX", lastPos.getX());
            tag.putInt("lastY", lastPos.getY());
            tag.putInt("lastZ", lastPos.getZ());
        }
    }

    @Override
    public Component[] provideInformationOfRitualToPlayer(Player player) {
        return new Component[]{
                Component.translatable(this.getTranslationKey() + ".info"),
                Component.translatable(this.getTranslationKey() + ".default.info"),
                Component.translatable(this.getTranslationKey() + ".corrosive.info"),
                Component.translatable(this.getTranslationKey() + ".steadfast.info")
        };
    }

    public int getRefreshTimeForRawWill(double rawWill) {
        if (rawWill >= RAW_WILL_DRAIN) {
            return Math.max(1, (int) (10 - rawWill / 10));
        }

        return DEFAULT_REFRESH_TIME;
    }

    @Override
    public int getRefreshTime() {
        return refreshTime;
    }

    @Override
    public int getRefreshCost() {
        return 10;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addParallelRunes(components, 1, 0, EnumRuneType.BLANK);
        addParallelRunes(components, 4, 0, EnumRuneType.EARTH);
        addOffsetRunes(components, 1, 3, 0, EnumRuneType.WATER);
        addOffsetRunes(components, 3, 1, 0, EnumRuneType.WATER);
        addCornerRunes(components, 2, 0, EnumRuneType.DUSK);
        addParallelRunes(components, 3, 1, EnumRuneType.AIR);
        addParallelRunes(components, 2, 1, EnumRuneType.EARTH);
        addParallelRunes(components, 2, 2, EnumRuneType.EARTH);
        addParallelRunes(components, 3, 3, EnumRuneType.AIR);
        addCornerRunes(components, 2, 3, EnumRuneType.FIRE);
        addOffsetRunes(components, 2, 1, 3, EnumRuneType.FIRE);
        addOffsetRunes(components, 1, 2, 3, EnumRuneType.FIRE);
    }

    @Override
    public Ritual getNewCopy() {
        return new YawningVoidRitual();
    }
}
