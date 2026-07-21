package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;

import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of the Zephyr ({@code RitualZephyr}): vacuums dropped item
 * entities in its range into the inventory in its chest range, one LP-charged insert per item per
 * pulse. Uses NeoForge's item-handler capability in place of the original's Forge one.
 */
public class ZephyrRitual extends Ritual {
    public static final String ZEPHYR_RANGE = "zephyrRange";
    public static final String CHEST_RANGE = "chest";

    public ZephyrRitual() {
        super(RitualRegistry.rl("zephyr"), 0, 1000, "ritual.bloodmagic.zephyr");
        addBlockRange(ZEPHYR_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-5, -5, -5), 11));
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

        setMaximumVolumeAndDistanceOfRange(ZEPHYR_RANGE, 0, 10, 10);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        if (level.isClientSide) {
            return;
        }

        int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();
        BlockPos masterPos = masterRitualStone.getMasterBlockPos();
        AreaDescriptor chestRange = masterRitualStone.getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(masterPos).get(0);

        IItemHandler inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, chestPos, null);
        if (inventory == null) {
            return;
        }

        if (currentEssence < getRefreshCost()) {
            masterRitualStone.causeNausea();
            return;
        }

        AreaDescriptor zephyrRange = masterRitualStone.getBlockRange(ZEPHYR_RANGE);
        List<ItemEntity> itemList = level.getEntitiesOfClass(ItemEntity.class, zephyrRange.getAABB(masterPos));
        int count = 0;

        for (ItemEntity entityItem : itemList) {
            if (!entityItem.isAlive()) {
                continue;
            }

            ItemStack copyStack = entityItem.getItem().copy();
            int originalAmount = copyStack.getCount();
            ItemStack remainder = ItemHandlerHelper.insertItem(inventory, copyStack, false);

            if (remainder.getCount() < originalAmount) {
                count++;
                entityItem.getItem().setCount(remainder.getCount());
                if (remainder.isEmpty()) {
                    entityItem.discard();
                }
            }
        }

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost() * Math.min(count, 100)));
    }

    @Override
    public int getRefreshTime() {
        return 1;
    }

    @Override
    public int getRefreshCost() {
        return 1;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addParallelRunes(components, 2, 0, EnumRuneType.AIR);
        addCornerRunes(components, 1, 1, EnumRuneType.AIR);
        addParallelRunes(components, 1, -1, EnumRuneType.AIR);
    }

    @Override
    public Ritual getNewCopy() {
        return new ZephyrRitual();
    }
}
