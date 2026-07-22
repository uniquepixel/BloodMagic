package wayoftime.bloodmagic.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.blockentity.HellfireForgeTile;
import wayoftime.bloodmagic.common.menu.HellfireForgeMenu;

public class HellfireForgeScreen extends AbstractContainerScreen<HellfireForgeMenu> {

    // Reuses the 1.20.1 "Tartaric Forge"/Soul Forge GUI art (ported as-is): a 176x205 panel with
    // the progress-arrow graphic baked into the same texture just past x=176.
    private static final ResourceLocation BACKGROUND = BloodMagic.rl("textures/gui/container/hellfire_forge.png");

    public HellfireForgeScreen(HellfireForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 205;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int progress = getProgressScaled(90);
        guiGraphics.blit(BACKGROUND, leftPos + 115, topPos + 14 + 90 - progress, 176, 90 - progress, 18, progress);
    }

    private int getProgressScaled(int scale) {
        int progress = menu.getData(HellfireForgeTile.PROGRESS);
        return progress * scale / HellfireForgeTile.MAX_PROGRESS;
    }
}
