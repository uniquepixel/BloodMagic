package wayoftime.bloodmagic.common.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.blockentity.HellfireForgeTile;

import static wayoftime.bloodmagic.common.blockentity.HellfireForgeTile.*;

public class HellfireForgeMenu extends AbstractContainerMenu {

    private static final int SLOT_COUNT = OUTPUT_SLOT + 1;

    public final HellfireForgeTile tile;
    public final ContainerData data;

    public HellfireForgeMenu(int containerId, Inventory playerInventory, HellfireForgeTile tile) {
        super(BMMenus.HELLFIRE_FORGE.get(), containerId);
        this.tile = tile;
        this.data = tile.data;
        addDataSlots(data);

        // The 4 directional input slots, laid out at the corners like the 1.20.1 Soul Forge GUI.
        addSlot(new SlotItemHandler(tile.inv, SOUTH, 8, 15));
        addSlot(new SlotItemHandler(tile.inv, WEST, 80, 15));
        addSlot(new SlotItemHandler(tile.inv, NORTH, 8, 87));
        addSlot(new SlotItemHandler(tile.inv, EAST, 80, 87));

        addSlot(new SlotItemHandler(tile.inv, GEM_SLOT, 152, 51));
        addSlot(new SlotItemHandler(tile.inv, OUTPUT_SLOT, 44, 51) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // player inv
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 123 + i * 18));
            }
        }

        // player hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 181));
        }
    }

    public HellfireForgeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, (HellfireForgeTile) playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public int getData(int id) {
        return this.data.get(id);
    }

    private int playerInvStart = SLOT_COUNT;
    private int playerInvEnd = playerInvStart + 27;
    private int hotbarStart = playerInvEnd + 1;
    private int hotbarEnd = hotbarStart + 8;

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack movedStack = ItemStack.EMPTY;
        Slot movedSlot = this.slots.get(index);

        if (movedSlot.hasItem()) {
            ItemStack rawStack = movedSlot.getItem();
            movedStack = rawStack.copy();

            if (index < playerInvStart) {
                if (!this.moveItemStackTo(rawStack, playerInvStart, hotbarEnd, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (index >= playerInvStart) {
                if (!this.moveItemStackTo(rawStack, 0, playerInvStart, false)) {
                    return ItemStack.EMPTY;
                }
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
        return AbstractContainerMenu.stillValid(ContainerLevelAccess.NULL, player, BMBlocks.HELLFIRE_FORGE.block().get());
    }
}
