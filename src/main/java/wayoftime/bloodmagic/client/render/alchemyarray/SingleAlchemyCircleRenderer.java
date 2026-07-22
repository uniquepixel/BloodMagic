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
 * Ported from 1.20.1's {@code SingleAlchemyCircleRenderer}: spins steadily around Y with no
 * secondary tilt, at a constant size.
 */
public class SingleAlchemyCircleRenderer extends AlchemyCircleRenderer {
    public SingleAlchemyCircleRenderer(ResourceLocation arrayResource) {
        super(arrayResource);
    }

    @Override
    public float getRotation(float craftTime) {
        float offset = 2;
        if (craftTime >= offset) {
            return (craftTime - offset) * 2f;
        }
        return 0;
    }

    @Override
    public float getSecondaryRotation(float craftTime) {
        return 0;
    }

    @Override
    public float getSizeModifier(float craftTime) {
        return 1.0f;
    }

    @Override
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
        poseStack.mulPose(Axis.ZN.rotationDegrees(rot));
        poseStack.mulPose(Axis.YP.rotationDegrees(secondaryRot));

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(arrayResource));
        Model2D model = Model2D.unitSquare(arrayResource);

        poseStack.scale(size, size, size);

        RenderResizableQuadrilateral.INSTANCE.renderSquare(model, poseStack, buffer, 0xFFFFFFFF, 0x00F000F0, packedOverlay);

        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }
}
