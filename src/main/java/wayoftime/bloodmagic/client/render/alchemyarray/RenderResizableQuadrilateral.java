package wayoftime.bloodmagic.client.render.alchemyarray;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Ported from 1.20.1's {@code RenderResizableQuadrilateral}: draws a single flat, textured quad
 * on the local X/Z ground plane (y=0), facing up. Used as the billboard for every Alchemy Array
 * circle on this branch.
 * <p>
 * Simplified from the original: 1.20.1's version supported an arbitrary {@code Model2D} size by
 * tiling the texture in a UV-repeat loop, one 1x1 tile at a time. Every actual call site in this
 * mod (all 8 {@code *AlchemyCircleRenderer} classes) only ever builds a fixed 1x1
 * {@link Model2D#unitSquare}, so that loop was collapsed into the single quad it always produced
 * in practice. Vertex order/UVs/the fixed "faux" lighting normal are otherwise ported
 * byte-for-byte from the original's face=UP case (the only case it ever exercised - the loop over
 * {@code Direction.values()} was already commented out in 1.20.1).
 * <p>
 * Rewritten onto the modern chainable {@link VertexConsumer} API (see
 * {@code wayoftime.bloodmagic.util.RenderHelper} for the same pattern used elsewhere in this
 * branch's renderers) in place of 1.20.1's {@code buffer.vertex(...).endVertex()} chain.
 */
public class RenderResizableQuadrilateral {
    public static final RenderResizableQuadrilateral INSTANCE = new RenderResizableQuadrilateral();

    // 1.20.1 computed this "faux" normal by taking the DOWN direction's normal (0,-1,0) - the
    // renderer always flips face=UP to its opposite before reading Direction#getNormal() - and
    // offsetting each component by a fixed 2.5 before normalizing. Not a physically meaningful
    // up-facing normal, but kept as-is for lighting parity with the original circle art.
    private static final Vector3f RAW_NORMAL = new Vector3f(2.5f, 1.5f, 2.5f).normalize();

    public void renderSquare(Model2D square, PoseStack poseStack, VertexConsumer buffer, int argb, int light, int overlay) {
        int alpha = (argb >>> 24) & 0xFF;
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;

        float minX = (float) square.minX;
        float maxX = (float) square.maxX;
        float minY = (float) square.minY;
        float maxY = (float) square.maxY;

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        Vector3f norm = new Vector3f();
        pose.transformNormal(RAW_NORMAL.x(), RAW_NORMAL.y(), RAW_NORMAL.z(), norm);

        addVertex(buffer, matrix, minX, 0, maxY, 0, 0, red, green, blue, alpha, light, overlay, norm);
        addVertex(buffer, matrix, minX, 0, minY, 0, 1, red, green, blue, alpha, light, overlay, norm);
        addVertex(buffer, matrix, maxX, 0, minY, 1, 1, red, green, blue, alpha, light, overlay, norm);
        addVertex(buffer, matrix, maxX, 0, maxY, 1, 0, red, green, blue, alpha, light, overlay, norm);
    }

    private static void addVertex(VertexConsumer buf, Matrix4f matrix, float x, float y, float z, float u, float v, int red, int green, int blue, int alpha, int light, int overlay, Vector3f norm) {
        buf.addVertex(matrix, x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(norm.x(), norm.y(), norm.z());
    }
}
