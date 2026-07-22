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
 * Ported from 1.20.1's {@code BeaconAlchemyCircleRenderer}: spins quickly around Y while its
 * secondary tilt ramps from flat to a 90-degree pitch over its first 150 ticks - used for
 * Updraft's array.
 */
public class BeaconAlchemyCircleRenderer extends AlchemyCircleRenderer {
    public BeaconAlchemyCircleRenderer(ResourceLocation arrayResource) {
        super(arrayResource);
    }

    @Override
    public float getRotation(float craftTime) {
        float offset = 2;
        if (craftTime >= offset) {
            return (craftTime - offset) * 5f;
        }
        return 0;
    }

    @Override
    public float getSecondaryRotation(float craftTime) {
        float offset = 50;
        float secondaryOffset = 150;
        if (craftTime >= offset) {
            if (craftTime < secondaryOffset) {
                return 90 * (craftTime - offset) / (secondaryOffset - offset);
            }
            return 90;
        }
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
        poseStack.mulPose(Axis.YP.rotationDegrees(rot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-secondaryRot));

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(arrayResource));
        Model2D model = Model2D.unitSquare(arrayResource);

        poseStack.scale(size, size, size);

        RenderResizableQuadrilateral.INSTANCE.renderSquare(model, poseStack, buffer, 0xFFFFFFFF, 0x00F000F0, packedOverlay);

        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();
    }
}
