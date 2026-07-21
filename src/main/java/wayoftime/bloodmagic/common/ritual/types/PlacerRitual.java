package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;

import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of the Satiated Placer ({@code RitualPlacer}): pulls a
 * placeable block from an adjacent chest and places it at the first replaceable position in its
 * placer range, once per pulse.
 * <p>
 * Adapted: the old Forge item-handler capability + {@code Utils.getInventory} lookup is replaced by
 * this branch's NeoForge equivalent ({@code Capabilities.ItemHandler.BLOCK}), and the old
 * protection-mod compatibility layer ({@code BlockProtectionHelper.tryPlaceBlock}) - which this
 * branch has no analogue for - is replaced by a direct {@code Level#setBlockAndUpdate} call.
 */
public class PlacerRitual extends Ritual {
    public static final String PLACER_RANGE = "placerRange";
    public static final String CHEST_RANGE = "chest";

    public PlacerRitual() {
        super(RitualRegistry.rl("placer"), 0, 5000, "ritual.bloodmagic.placer");
        addBlockRange(PLACER_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-2, 0, -2), 5, 1, 5));
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

        setMaximumVolumeAndDistanceOfRange(PLACER_RANGE, 300, 7, 7);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        BlockPos masterPos = masterRitualStone.getMasterBlockPos();

        AreaDescriptor chestRange = masterRitualStone.getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(masterPos).get(0);
        BlockEntity chestTile = level.getBlockEntity(chestPos);

        int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();

        if (currentEssence < getRefreshCost()) {
            masterRitualStone.causeNausea();
            return;
        }

        AreaDescriptor placerRange = masterRitualStone.getBlockRange(PLACER_RANGE);

        if (chestTile == null) {
            return;
        }

        IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, chestPos, null);
        if (itemHandler == null) {
            return;
        }

        posLoop:
        for (BlockPos blockPos : placerRange.getContainedPositions(masterPos)) {
            BlockPlaceContext ctx = new BlockPlaceContext(level, null, InteractionHand.MAIN_HAND, ItemStack.EMPTY, BlockHitResult.miss(new Vec3(0, 0, 0), Direction.UP, blockPos));
            if (!level.getBlockState(blockPos).canBeReplaced(ctx)) {
                continue;
            }

            for (int invSlot = 0; invSlot < itemHandler.getSlots(); invSlot++) {
                ItemStack stack = itemHandler.extractItem(invSlot, 1, true);
                if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) {
                    continue;
                }

                Block blockToPlace = Block.byItem(stack.getItem());
                level.setBlockAndUpdate(blockPos, blockToPlace.defaultBlockState());

                itemHandler.extractItem(invSlot, 1, false);
                chestTile.setChanged();
                masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost()));
                break posLoop; // Break instead of return in case we add things later
            }
        }
    }

    @Override
    public int getRefreshCost() {
        return 50;
    }

    @Override
    public int getRefreshTime() {
        return 5;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 3, 0, 3, EnumRuneType.EARTH);
        addRune(components, 3, 0, -3, EnumRuneType.EARTH);
        addRune(components, -3, 0, 3, EnumRuneType.EARTH);
        addRune(components, -3, 0, -3, EnumRuneType.EARTH);

        addRune(components, 3, 0, 2, EnumRuneType.WATER);
        addRune(components, 3, 0, -2, EnumRuneType.WATER);
        addRune(components, 2, 0, 3, EnumRuneType.WATER);
        addRune(components, 2, 0, -3, EnumRuneType.WATER);
        addRune(components, -2, 0, 3, EnumRuneType.WATER);
        addRune(components, -2, 0, -3, EnumRuneType.WATER);
        addRune(components, -3, 0, 2, EnumRuneType.WATER);
        addRune(components, -3, 0, -2, EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() {
        return new PlacerRitual();
    }
}
