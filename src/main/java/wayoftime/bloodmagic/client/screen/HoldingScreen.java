package wayoftime.bloodmagic.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import wayoftime.bloodmagic.common.menu.HoldingMenu;

/**
 * Reuses the vanilla shulker box background (single row of slots + standard player inventory) so
 * this doesn't need a bespoke texture asset - only the first 4 slots of the top row are wired up
 * to real slots, the rest of that row renders as inert background.
 */
public class HoldingScreen extends AbstractContainerScreen<HoldingMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/container/shulker_box.png");

    public HoldingScreen(HoldingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
