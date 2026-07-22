package wayoftime.bloodmagic.client.render.alchemyarray;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import wayoftime.bloodmagic.common.blockentity.AlchemyArrayTile;

/**
 * Ported from 1.20.1's {@code client.render.alchemyarray.AlchemyArrayRenderer} - renamed to avoid
 * colliding with this branch's {@code client.render.blockentity.AlchemyArrayRenderer} (the actual
 * {@code BlockEntityRenderer<AlchemyArrayTile>}, which dispatches to one of these per-recipe
 * circle renderers - see {@link AlchemyArrayRendererRegistry}).
 * <p>
 * Draws a single flat, textured, spinning ground quad ({@link #arrayResource}) driven purely by
 * {@code craftTime} timing curves ({@link #getRotation}, {@link #getSecondaryRotation},
 * {@link #getSizeModifier}, {@link #getVerticalOffset}) - no distinct 3D geometry. The 8
 * subclasses in this package override a subset of those curves (and, for
 * Day/Night/Binding, layer in additional quads of their own) to get the different array
 * "personalities" 1.20.1 had.
 */
public class AlchemyCircleRenderer {
    public final ResourceLocation arrayResource;

    public AlchemyCircleRenderer(ResourceLocation arrayResource) {
        this.arrayResource = arrayResource;
    }

    public float getRotation(float craftTime) {
        float offset = 2;
        if (craftTime >= offset) {
            return (float) Math.pow(craftTime - offset, 1.5) * 1f;
        }
        return 0;
    }

    public float getSecondaryRotation(float craftTime) {
        float offset = 50;
        if (craftTime >= offset) {
            return (float) Math.pow(craftTime - offset, 1.7) * 0.5f;
        }
        return 0;
    }

    public float getSizeModifier(float craftTime) {
        if (craftTime >= 150 && craftTime <= 250) {
            return (200 - craftTime) / 50f;
        }
        return 1.0f;
    }

    public float getVerticalOffset(float craftTime) {
        if (craftTime >= 5) {
            if (craftTime <= 40) {
                return (float) (-0.4 + 0.4 * Math.pow((craftTime - 5) / 35f, 3));
            }
            return 0;
        }
        return -0.4f;
    }

    public void renderAt(AlchemyArrayTile tile, float craftTime, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        float rot = getRotation(craftTime);
        float secondaryRot = getSecondaryRotation(craftTime);
        float size = getSizeModifier(craftTime);
        Direction rotation = tile.getRotation();

        poseStack.pushPose();
        poseStack.translate(0, getVerticalOffset(craftTime), 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotation.toYRot()));

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(rot));
        poseStack.mulPose(Axis.ZN.rotationDegrees(secondaryRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(secondaryRot * 0.45812f));

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(arrayResource));
        Model2D model = Model2D.unitSquare(arrayResource);

        poseStack.scale(size, size, size);

        RenderResizableQuadrilateral.INSTANCE.renderSquare(model, poseStack, buffer, 0xFFFFFFFF, 0x00F000F0, packedOverlay);

        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }
}
