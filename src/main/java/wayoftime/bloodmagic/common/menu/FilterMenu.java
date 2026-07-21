package wayoftime.bloodmagic.common.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.item.filter.AbstractFilterItem;
import wayoftime.bloodmagic.common.item.routing.INestableFilterProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for configuring a Filter item (see {@link AbstractFilterItem}): a 3x3 grid of ghost reference
 * slots (built on the pre-existing {@link AbstractGhostMenu} infrastructure this branch already
 * uses for the Trainer's bracelet GUI, so ghost items here work exactly the same way - left-click
 * to place a "template" copy without consuming it, right-click to clear) plus a whitelist/blacklist
 * (or, for a Composite filter, any/all) toggle button.
 * <p>
 * Not ported from 1.20.1: per-filter-type extra buttons (tag/enchant cycling - see
 * {@code AbstractFilterItem}'s javadoc for why), and the JEI ghost-ingredient drag handler.
 */
public class FilterMenu extends AbstractGhostMenu<FilterMenu> {
    public static final int ROWS = 3;
    public static final int COLUMNS = 3;
    public static final int SLOT_COUNT = ROWS * COLUMNS;

    public static final int DATA_BLACKLIST = 1;

    private final ItemStack filterStack;
    private final boolean nestedSlots;

    // SERVER constructor
    public FilterMenu(int containerId, Inventory playerInv, ItemStack filterStack, boolean nestedSlots, int heldSlot) {
        super(BMMenus.FILTER.get(), containerId, playerInv,
                buildData(filterStack), new FilterGhostItemHandler(filterStack, nestedSlots),
                ROWS, COLUMNS, 44, 18, 95, heldSlot);
        this.filterStack = filterStack;
        this.nestedSlots = nestedSlots;
    }

    // CLIENT constructor
    public FilterMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInv, ItemStack.EMPTY, buf.readBoolean(), buf.readInt());
    }

    private static SimpleContainerData buildData(ItemStack filterStack) {
        SimpleContainerData data = new SimpleContainerData(2);
        data.set(DATA_BLACKLIST, filterStack.isEmpty() ? 0 : (filterStack.getOrDefault(BMDataComponents.FILTER_BLACKLIST, false) ? 1 : 0));
        return data;
    }

    public boolean isNestedSlots() {
        return nestedSlots;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            boolean newValue = getData(DATA_BLACKLIST) == 0;
            setData(DATA_BLACKLIST, newValue ? 1 : 0);
            if (!filterStack.isEmpty()) {
                filterStack.set(BMDataComponents.FILTER_BLACKLIST, newValue);
            }
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Ghost slots never accept quick-move (mirrors AbstractGhostMenu's own quickMoveStack, which
        // this overrides so player-inventory shift-clicks don't try to route into ghost slots).
        ItemStack movedStack = ItemStack.EMPTY;
        net.minecraft.world.inventory.Slot movedSlot = this.slots.get(index);
        if (index >= SLOT_COUNT && movedSlot.hasItem()) {
            movedStack = movedSlot.getItem().copy();
        }
        return movedStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /** Ghost handler whose contents mirror the filter item's stored reference items, writing back on change. */
    static class FilterGhostItemHandler extends GhostItemHandler {
        private final ItemStack filterStack;
        private final boolean nestedSlots;

        FilterGhostItemHandler(ItemStack filterStack, boolean nestedSlots) {
            super(SLOT_COUNT);
            this.filterStack = filterStack;
            this.nestedSlots = nestedSlots;

            if (!filterStack.isEmpty()) {
                ItemContainerContents contents = filterStack.getOrDefault(BMDataComponents.FILTER_CONTENTS, ItemContainerContents.EMPTY);
                for (int i = 0; i < SLOT_COUNT && i < contents.getSlots(); i++) {
                    setStackInSlot(i, contents.getStackInSlot(i));
                }
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (!nestedSlots) {
                return true;
            }
            return stack.getItem() instanceof INestableFilterProvider;
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (filterStack.isEmpty()) {
                return;
            }
            List<ItemStack> list = new ArrayList<>(SLOT_COUNT);
            for (int i = 0; i < SLOT_COUNT; i++) {
                list.add(getStackInSlot(i));
            }
            filterStack.set(BMDataComponents.FILTER_CONTENTS, ItemContainerContents.fromItems(list));
        }
    }
}
