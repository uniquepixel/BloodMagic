package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of Magnetism ({@code RitualMagnetic}): scans a growing box
 * beneath the stone (radius set by the block directly below it - plain stone/dirt gives a small
 * radius, Iron/Gold/Diamond Blocks give progressively larger ones) for ore blocks and teleports the
 * first one found into an empty spot in the placement range, resuming from where it left off across
 * ticks (capped at 100 block checks per tick) rather than rescanning from scratch. The original's
 * fake-player-based pick-block lookup was already dead code upstream (commented out in favor of a
 * plain `new ItemStack(block)` ore-tag check), so it isn't ported either.
 */
public class MagnetismRitual extends Ritual {
    public static final String PLACEMENT_RANGE = "placementRange";

    @Nullable
    private BlockPos lastPos;

    public MagnetismRitual() {
        super(RitualRegistry.rl("magnetism"), 0, 5000, "ritual.bloodmagic.magnetism");
        addBlockRange(PLACEMENT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-1, 1, -1), 3));
        setMaximumVolumeAndDistanceOfRange(PLACEMENT_RANGE, 50, 4, 4);
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
        AreaDescriptor placementRange = masterRitualStone.getBlockRange(PLACEMENT_RANGE);

        BlockPos replacement = pos;
        boolean replace = false;

        for (BlockPos offset : placementRange.getContainedPositions(pos)) {
            if (level.isEmptyBlock(offset)) {
                replacement = offset;
                replace = true;
                break;
            }
        }

        if (!replace) {
            return;
        }

        BlockState downState = level.getBlockState(pos.below());
        int radius = getRadius(downState.getBlock());

        int maxBlockChecks = 100;
        int checks = 0;

        int j = -1;
        int i = -radius;
        int k = -radius;

        if (lastPos != null && !lastPos.equals(BlockPos.ZERO)) {
            j = lastPos.getY();
            i = Math.min(radius, Math.max(-radius, lastPos.getX()));
            k = Math.min(radius, Math.max(-radius, lastPos.getZ()));
        }

        while (j + pos.getY() >= level.getMinBuildHeight()) {
            while (i <= radius) {
                while (k <= radius) {
                    if (checks >= maxBlockChecks) {
                        this.lastPos = new BlockPos(i, j, k);
                        return;
                    }
                    checks++;

                    BlockPos newPos = pos.offset(i, j, k);
                    BlockState state = level.getBlockState(newPos);
                    ItemStack checkStack = new ItemStack(state.getBlock());
                    if (isBlockOre(checkStack)) {
                        swapLocations(level, newPos, replacement);
                        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost()));
                        k++;
                        this.lastPos = new BlockPos(i, j, k);
                        return;
                    }

                    k++;
                }
                i++;
                k = -radius;
            }
            j--;
            i = -radius;
        }

        this.lastPos = new BlockPos(i, -1, k);
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

    public int getRadius(Block block) {
        if (block == Blocks.IRON_BLOCK) {
            return 7;
        }

        if (block == Blocks.GOLD_BLOCK) {
            return 15;
        }

        if (block == Blocks.DIAMOND_BLOCK) {
            return 31;
        }

        return 3;
    }

    @Override
    public int getRefreshTime() {
        return 40;
    }

    @Override
    public int getRefreshCost() {
        return 50;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addCornerRunes(components, 1, 0, EnumRuneType.EARTH);
        addParallelRunes(components, 2, 1, EnumRuneType.EARTH);
        addCornerRunes(components, 2, 1, EnumRuneType.AIR);
        addParallelRunes(components, 2, 2, EnumRuneType.FIRE);
    }

    @Override
    public Ritual getNewCopy() {
        return new MagnetismRitual();
    }

    public static boolean isBlockOre(ItemStack stack) {
        return !stack.isEmpty() && stack.is(Tags.Items.ORES);
    }
}
