package wayoftime.bloodmagic.common.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.item.SigilItem;

import java.util.ArrayList;
import java.util.List;

public class HoldingMenu extends AbstractContainerMenu {
    public static final int SLOTS = 4;

    private final ItemStackHandler handler;

    // CLIENT constructor - real contents arrive shortly after via the vanilla container sync packet
    public HoldingMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInv, ItemStack.EMPTY, -1);
    }

    // SERVER constructor
    public HoldingMenu(int containerId, Inventory playerInv, ItemStack holdingStack, int lockedHotbarSlot) {
        super(BMMenus.HOLDING.get(), containerId);

        this.handler = new ItemStackHandler(SLOTS) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.getItem() instanceof SigilItem;
            }

            @Override
            protected void onContentsChanged(int slot) {
                if (holdingStack.isEmpty()) {
                    return;
                }
                List<ItemStack> contents = new ArrayList<>(SLOTS);
                for (int i = 0; i < SLOTS; i++) {
                    contents.add(getStackInSlot(i));
                }
                holdingStack.set(BMDataComponents.HOLDING_CONTENTS, ItemContainerContents.fromItems(contents));
            }
        };

        ItemContainerContents contents = holdingStack.getOrDefault(BMDataComponents.HOLDING_CONTENTS, ItemContainerContents.EMPTY);
        for (int i = 0; i < SLOTS; i++) {
            handler.setStackInSlot(i, contents.getStackInSlot(i));
        }

        for (int i = 0; i < SLOTS; i++) {
            this.addSlot(new SlotItemHandler(handler, i, 62 + i * 18, 20));
        }

        // player inv
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 51 + i * 18));
            }
        }

        // player hotbar
        for (int i = 0; i < 9; i++) {
            if (i == lockedHotbarSlot) {
                this.addSlot(new LockedSlot(playerInv, i, 8 + i * 18, 109));
            } else {
                this.addSlot(new Slot(playerInv, i, 8 + i * 18, 109));
            }
        }
    }

    public IItemHandler getHandler() {
        return handler;
    }

    private final int playerInvStart = SLOTS;
    private final int playerInvEnd = playerInvStart + 27;
    private final int hotbarEnd = playerInvEnd + 9;

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack movedStack = ItemStack.EMPTY;
        Slot movedSlot = this.slots.get(index);

        if (movedSlot.hasItem()) {
            ItemStack rawStack = movedSlot.getItem();
            movedStack = rawStack.copy();

            if (index < playerInvStart) {
                if (!this.moveItemStackTo(rawStack, playerInvStart, hotbarEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(rawStack, 0, playerInvStart, false)) {
                return ItemStack.EMPTY;
            }

            if (rawStack.isEmpty()) {
                movedSlot.set(ItemStack.EMPTY);
            } else {
                movedSlot.setChanged();
            }
        }

        return movedStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private static class LockedSlot extends Slot {
        public LockedSlot(net.minecraft.world.Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
