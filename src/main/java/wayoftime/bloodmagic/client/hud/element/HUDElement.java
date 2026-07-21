package wayoftime.bloodmagic.client.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import wayoftime.bloodmagic.client.hud.ElementRegistry;

/**
 * Base type for a single draggable HUD overlay element. Ported from 1.20.1 unchanged.
 */
@OnlyIn(Dist.CLIENT)
public abstract class HUDElement {
    private final int width;
    private final int height;

    public HUDElement(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean shouldRender(Minecraft minecraft) {
        return true;
    }

    public abstract void draw(GuiGraphics guiGraphics, float partialTicks, int drawX, int drawY);

    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        Vec2 point = ElementRegistry.getPosition(ElementRegistry.getKey(this));
        return ElementRegistry.getKey(this) + "@" + point.x + "," + point.y;
    }
}
