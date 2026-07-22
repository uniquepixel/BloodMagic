package wayoftime.bloodmagic.client.render.alchemyarray;

import net.minecraft.resources.ResourceLocation;

/**
 * Ported from 1.20.1's {@code BloodMagicRenderer.Model2D}: a minimal flat-quad descriptor (a
 * square on the local X/Z plane, textured with {@link #resource}) consumed by
 * {@link RenderResizableQuadrilateral}. Only the fields this branch's Alchemy Array renderers
 * actually use were carried over - the original's {@code Model3D} sibling class (used by other,
 * unrelated 1.20.1 renderers) was not.
 */
public class Model2D {
    public double minX, minY;
    public double maxX, maxY;

    public ResourceLocation resource;

    public double sizeX() {
        return maxX - minX;
    }

    public double sizeY() {
        return maxY - minY;
    }

    /**
     * Every Alchemy Array circle in 1.20.1 was rendered as a fixed 1x1 unit square centered on the
     * block (-0.5..+0.5 on both axes), scaled afterward via {@code PoseStack.scale}. All 8 renderer
     * subclasses on this branch build their {@link Model2D} this way too.
     */
    public static Model2D unitSquare(ResourceLocation resource) {
        Model2D model = new Model2D();
        model.minX = -0.5;
        model.maxX = 0.5;
        model.minY = -0.5;
        model.maxY = 0.5;
        model.resource = resource;
        return model;
    }
}
