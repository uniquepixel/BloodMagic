package wayoftime.bloodmagic.client.hud;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import wayoftime.bloodmagic.client.hud.element.HUDElement;

import javax.annotation.Nullable;
import java.awt.Point;
import java.util.Map;

/**
 * Draggable HUD element editor: click and drag any registered {@link HUDElement}'s outlined box to
 * a new spot on screen, then either persist the new layout ("Done") or throw the in-progress edits
 * away ("Cancel"). Ported from 1.20.1's {@code GuiEditHUD}.
 * <p>
 * Not ported: a "Toggle" button that existed in 1.20.1's version of this screen but was permanently
 * disabled there ({@code btn.active = false}, never wired to anything) - it was dead code, not a
 * working feature, so there was nothing to port. The remaining three buttons use vanilla's own
 * {@code gui.done}/{@code gui.cancel}/{@code controls.reset} translation keys rather than the
 * bespoke {@code gui.bloodmagic.save/cancel/default} keys 1.20.1 used, since those don't exist in
 * this branch's lang file and adding them means editing {@code BMLanguageProvider}, which is out
 * of scope for this pass (owned by another concurrent change this round).
 */
public class HudEditScreen extends Screen {
    private final Map<ResourceLocation, Vec2> currentOverrides = Maps.newHashMap();
    private HUDElement dragged;

    public HudEditScreen() {
        super(Component.literal("Blood Magic HUD Editor"));
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(Button.builder(Component.translatable("controls.reset"), b -> {
            currentOverrides.clear();
            ElementRegistry.resetPos();
        }).pos(width / 2 - 115, height - 30).size(70, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> {
            ElementRegistry.save(currentOverrides);
            onClose();
        }).pos(width / 2 - 35, height - 30).size(70, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> {
            currentOverrides.clear();
            onClose();
        }).pos(width / 2 + 45, height - 30).size(70, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        Window window = Minecraft.getInstance().getWindow();
        for (HUDElement element : ElementRegistry.getElements()) {
            if (dragged == element)
                continue;

            ResourceLocation key = ElementRegistry.getKey(element);
            Vec2 position = currentOverrides.getOrDefault(key, ElementRegistry.getPosition(key));
            int xPos = (int) (window.getGuiScaledWidth() * position.x);
            int yPos = (int) (window.getGuiScaledHeight() * position.y);

            drawWithBox(guiGraphics, element, partialTicks, xPos, yPos);
        }

        if (dragged != null) {
            Point bounded = getBoundedDrag(window, mouseX, mouseY);
            drawWithBox(guiGraphics, dragged, partialTicks, bounded.x, bounded.y);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragged == null && button == 0) {
            HUDElement element = getHoveredElement(mouseX, mouseY);
            if (element != null) {
                dragged = element;
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        if (dragged != null) {
            Window window = Minecraft.getInstance().getWindow();
            Point bounded = getBoundedDrag(window, mouseX, mouseY);
            float xPos = (((float) bounded.x) / window.getGuiScaledWidth());
            float yPos = (((float) bounded.y) / window.getGuiScaledHeight());

            currentOverrides.put(ElementRegistry.getKey(dragged), new Vec2(xPos, yPos));
            dragged = null;
        }

        return super.mouseReleased(mouseX, mouseY, state);
    }

    @Nullable
    public HUDElement getHoveredElement(double mouseX, double mouseY) {
        Window window = Minecraft.getInstance().getWindow();
        for (HUDElement element : ElementRegistry.getElements()) {
            ResourceLocation key = ElementRegistry.getKey(element);
            Vec2 position = currentOverrides.getOrDefault(key, ElementRegistry.getPosition(key));

            int xPos = (int) (window.getGuiScaledWidth() * position.x);
            int yPos = (int) (window.getGuiScaledHeight() * position.y);

            if (mouseX < xPos || mouseX > xPos + element.getWidth())
                continue;

            if (mouseY < yPos || mouseY > yPos + element.getHeight())
                continue;

            return element;
        }

        return null;
    }

    protected Point getBoundedDrag(Window window, double mouseX, double mouseY) {
        int drawX = (int) (mouseX - dragged.getWidth() / 2);
        if (drawX + dragged.getWidth() >= window.getGuiScaledWidth())
            drawX = window.getGuiScaledWidth() - dragged.getWidth();
        if (drawX < 0)
            drawX = 0;

        int drawY = (int) (mouseY - dragged.getHeight() / 2);
        if (drawY + dragged.getHeight() >= window.getGuiScaledHeight())
            drawY = window.getGuiScaledHeight() - dragged.getHeight();
        if (drawY < 0)
            drawY = 0;

        return new Point(drawX, drawY);
    }

    protected void drawWithBox(GuiGraphics guiGraphics, HUDElement element, float partialTicks, int drawX, int drawY) {
        int color = ElementRegistry.getColor(ElementRegistry.getKey(element));

        guiGraphics.vLine(drawX, drawY, drawY + element.getHeight() - 1, color);
        guiGraphics.vLine(drawX + element.getWidth() - 1, drawY, drawY + element.getHeight() - 1, color);
        guiGraphics.hLine(drawX, drawX + element.getWidth() - 1, drawY, color);
        guiGraphics.hLine(drawX, drawX + element.getWidth() - 1, drawY + element.getHeight() - 1, color);

        element.draw(guiGraphics, partialTicks, drawX, drawY);
    }
}
