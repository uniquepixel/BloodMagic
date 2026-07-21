package wayoftime.bloodmagic.client.hud.element;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.client.Sprite;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.item.SigilHoldingItem;
import wayoftime.bloodmagic.common.menu.HoldingMenu;

/**
 * Sigil of Holding contents readout: a small strip showing the sigils currently tucked into a held
 * Sigil of Holding, without needing to open its full inventory GUI.
 * <p>
 * Ported from 1.20.1's {@code ElementHolding}, re-wired to this branch's redesigned Sigil of
 * Holding: 1.20.1 stored its 4 sigils directly in item NBT with a separate "current item ordinal"
 * (cycled with dedicated keybinds) marking which one was active. This branch stores them as an
 * {@link ItemContainerContents} data component ({@link BMDataComponents#HOLDING_CONTENTS}, see
 * {@link HoldingMenu}) and has no cycle keybind or stored "selected slot" concept any more -
 * {@link SigilHoldingItem} always delegates to whichever slot has the first non-empty stack - so
 * the selected-slot highlight box here just tracks that same "first non-empty slot" rule instead
 * of a persisted selection.
 */
public class ElementHolding extends HUDElement {
    private static final Sprite HOLDING_BAR = new Sprite(BloodMagic.rl("textures/gui/widgets.png"), 0, 0, 102, 22);
    private static final Sprite SELECTED_OVERLAY = new Sprite(BloodMagic.rl("textures/gui/widgets.png"), 0, 22, 24, 24);

    public ElementHolding() {
        super(HOLDING_BAR.getTextureWidth(), HOLDING_BAR.getTextureHeight());
    }

    @Override
    public void draw(GuiGraphics guiGraphics, float partialTicks, int drawX, int drawY) {
        HOLDING_BAR.draw(guiGraphics, drawX, drawY);

        Minecraft minecraft = Minecraft.getInstance();
        ItemStack holding = getHoldingStack(minecraft.player);
        if (holding.isEmpty()) {
            return;
        }

        ItemContainerContents contents = holding.getOrDefault(BMDataComponents.HOLDING_CONTENTS, ItemContainerContents.EMPTY);

        int activeSlot = -1;
        for (int i = 0; i < HoldingMenu.SLOTS; i++) {
            if (!contents.getStackInSlot(i).isEmpty()) {
                activeSlot = i;
                break;
            }
        }
        if (activeSlot >= 0) {
            SELECTED_OVERLAY.draw(guiGraphics, drawX - 1 + (activeSlot * 20), drawY - 1);
        }

        Lighting.setupForFlatItems();
        int xOffset = 0;
        for (int i = 0; i < HoldingMenu.SLOTS; i++) {
            renderHotbarItem(guiGraphics, drawX + 3 + xOffset, drawY + 3, partialTicks, minecraft.player, contents.getStackInSlot(i));
            xOffset += 20;
        }
    }

    @Override
    public boolean shouldRender(Minecraft minecraft) {
        return !getHoldingStack(minecraft.player).isEmpty();
    }

    private static ItemStack getHoldingStack(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof SigilHoldingItem) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof SigilHoldingItem) {
            return offHand;
        }
        return ItemStack.EMPTY;
    }

    protected void renderHotbarItem(GuiGraphics guiGraphics, int x, int y, float partialTicks, Player player, ItemStack stack) {
        if (!stack.isEmpty()) {
            float animation = (float) stack.getPopTime() - partialTicks;

            PoseStack poseStack = guiGraphics.pose();
            if (animation > 0.0F) {
                poseStack.pushPose();
                float f1 = 1.0F + animation / 5.0F;
                poseStack.translate((float) (x + 8), (float) (y + 12), 0.0F);
                poseStack.scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                poseStack.translate((float) (-(x + 8)), (float) (-(y + 12)), 0.0F);
            }
            guiGraphics.renderItem(player, stack, x, y, 1);

            if (animation > 0.0F)
                poseStack.popPose();

            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, x, y);
        }
    }
}
