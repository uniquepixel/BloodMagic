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
 * Ported from 1.20.1's {@code DayAlchemyCircleRenderer}: layers three quads - the base array,
 * a set of spikes that grow outward, and a central circle that grows inward - used for Day's
 * array.
 */
public class DayAlchemyCircleRenderer extends AlchemyCircleRenderer {
    private final ResourceLocation spikesResource;
    private final ResourceLocation circleResource;

    public DayAlchemyCircleRenderer(ResourceLocation arrayResource, ResourceLocation spikesResource, ResourceLocation circleResource) {
        super(arrayResource);
        this.spikesResource = spikesResource;
        this.circleResource = circleResource;
    }

    @Override
    public float getRotation(float craftTime) {
        return 0;
    }

    @Override
    public float getSecondaryRotation(float craftTime) {
        float offset = 2;
        if (craftTime >= offset) {
            return (craftTime - offset) * (craftTime - offset) * 0.05f;
        }
        return 0;
    }

    @Override
    public float getVerticalOffset(float craftTime) {
        if (craftTime >= 40) {
            if (craftTime <= 100) {
                return (float) (-0.4 + 0.4 * Math.pow((craftTime - 40) / 60f, 3));
            }
            return 0;
        }
        return -0.4f;
    }

    @Override
    public float getSizeModifier(float craftTime) {
        return 1.0f;
    }

    public float getSecondarySizeModifier(float craftTime) {
        if (craftTime >= 40) {
            if (craftTime <= 160) {
                return (float) (2f * Math.pow((craftTime - 40) / 120f, 3));
            }
            return 2;
        }
        return 0;
    }

    public float getTertiarySizeModifier(float craftTime) {
        if (craftTime >= 40) {
            if (craftTime <= 100) {
                return (float) (1f * Math.pow((craftTime - 40) / 60f, 3));
            }
            return 1;
        }
        return 0;
    }

    public float getSpikeVerticalOffset(float craftTime) {
        if (craftTime >= 40) {
            if (craftTime <= 100) {
                return (float) (-0.4 + 0.4 * Math.pow((craftTime - 40) / 60f, 3));
            } else if (craftTime <= 140) {
                return -0.01f * (craftTime - 100);
            }
            return -0.4f;
        }
        return -0.4f;
    }

    public float getCentralCircleOffset(float craftTime) {
        if (craftTime >= 40) {
            if (craftTime <= 100) {
                return (float) (-0.4 + 0.4 * Math.pow((craftTime - 40) / 60f, 3));
            } else if (craftTime <= 140) {
                return 0.01f * (craftTime - 100);
            }
            return 0.4f;
        }
        return -0.4f;
    }

    @Override
    public void renderAt(AlchemyArrayTile tile, float craftTime, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        float rot = getRotation(craftTime);
        float secondaryRot = getSecondaryRotation(craftTime);
        float size = getSizeModifier(craftTime);
        Direction rotation = tile.getRotation();

        // Base array
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

        // Growing spikes
        poseStack.pushPose();
        poseStack.translate(0, getSpikeVerticalOffset(craftTime), 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotation.toYRot()));

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZN.rotationDegrees(rot));
        poseStack.mulPose(Axis.YP.rotationDegrees(-secondaryRot));

        buffer = bufferSource.getBuffer(RenderType.entityTranslucent(spikesResource));
        model = Model2D.unitSquare(spikesResource);

        float secondarySize = getSecondarySizeModifier(craftTime);
        poseStack.scale(secondarySize, secondarySize, secondarySize);

        RenderResizableQuadrilateral.INSTANCE.renderSquare(model, poseStack, buffer, 0xFFFFFFFF, 0x00F000F0, packedOverlay);

        poseStack.popPose();
        poseStack.popPose();

        // Central shrinking-in circle
        poseStack.pushPose();
        poseStack.translate(0, getCentralCircleOffset(craftTime), 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotation.toYRot()));

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZN.rotationDegrees(rot));
        poseStack.mulPose(Axis.YP.rotationDegrees(-secondaryRot));

        buffer = bufferSource.getBuffer(RenderType.entityTranslucent(circleResource));
        model = Model2D.unitSquare(circleResource);

        float tertiarySize = getTertiarySizeModifier(craftTime);
        poseStack.scale(tertiarySize, tertiarySize, tertiarySize);

        RenderResizableQuadrilateral.INSTANCE.renderSquare(model, poseStack, buffer, 0xFFFFFFFF, 0x00F000F0, packedOverlay);

        poseStack.popPose();
        poseStack.popPose();

        poseStack.popPose();
    }
}
