package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.common.ritual.harvest.HarvestHandlerRegistry;
import wayoftime.bloodmagic.common.ritual.harvest.IHarvestHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of the Harvest ({@code RitualHarvest}): dispatches every
 * block in its harvest range through the pluggable {@link IHarvestHandler} registry (see
 * {@link HarvestHandlerRegistry}) instead of checking vanilla crop types directly. Drops are inserted
 * into an item handler in the block above the master ritual stone if one is present (falling back to
 * dropping them at the harvested block's position otherwise), matching the original's behavior of
 * checking for an inventory above the controller.
 */
public class HarvestRitual extends Ritual {
    public static final String HARVEST_RANGE = "harvestRange";

    public HarvestRitual() {
        super(RitualRegistry.rl("harvest"), 0, 20000, "ritual.bloodmagic.harvest");
        addBlockRange(HARVEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-4, 1, -4), 9, 5, 9));
        setMaximumVolumeAndDistanceOfRange(HARVEST_RANGE, 0, 15, 15);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        BlockPos pos = masterRitualStone.getMasterBlockPos();

        if (masterRitualStone.getOwnerNetwork().getCurrentEssence() < getRefreshCost()) {
            masterRitualStone.causeNausea();
            return;
        }

        int harvested = 0;
        AreaDescriptor harvestArea = masterRitualStone.getBlockRange(HARVEST_RANGE);

        IItemHandler inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.above(), Direction.DOWN);

        for (BlockPos target : harvestArea.getContainedPositions(pos)) {
            if (harvestBlock(level, target, inventory)) {
                harvested++;
            }
        }

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost() * harvested));
    }

    public static boolean harvestBlock(Level level, BlockPos target, @Nullable IItemHandler inventory) {
        BlockState state = level.getBlockState(target);

        for (IHarvestHandler handler : HarvestHandlerRegistry.getHandlers()) {
            if (!handler.test(level, target, state)) {
                continue;
            }

            List<ItemStack> drops = new ArrayList<>();
            if (handler.harvest(level, target, state, drops)) {
                for (ItemStack stack : drops) {
                    if (stack.isEmpty()) {
                        continue;
                    }

                    ItemStack remainder = inventory == null ? stack : ItemHandlerHelper.insertItem(inventory, stack, false);
                    if (!remainder.isEmpty()) {
                        Containers.dropItemStack(level, target.getX(), target.getY(), target.getZ(), remainder);
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public int getRefreshCost() {
        return 20;
    }

    @Override
    public int getRefreshTime() {
        return 5;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addCornerRunes(components, 1, 0, EnumRuneType.DUSK);
        addParallelRunes(components, 2, 0, EnumRuneType.EARTH);
        addOffsetRunes(components, 3, 1, 0, EnumRuneType.EARTH);
        addOffsetRunes(components, 3, 2, 0, EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() {
        return new HarvestRitual();
    }
}
