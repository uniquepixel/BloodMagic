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
 * Ported from 1.20.1's {@code NightAlchemyCircleRenderer}: layers the base array, a central
 * circle that grows in, and a "moon" ({@code symbolResource}) that arcs over the top of it -
 * used for Night's array. 1.20.1's own symbol-layer block (separate from the moon) was already
 * commented out in the source being ported from, so it's omitted here too.
 */
public class NightAlchemyCircleRenderer extends AlchemyCircleRenderer {
    private final ResourceLocation symbolResource;
    private final ResourceLocation circleResource;

    public NightAlchemyCircleRenderer(ResourceLocation arrayResource, ResourceLocation symbolResource, ResourceLocation circleResource) {
        super(arrayResource);
        this.symbolResource = symbolResource;
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

    public float getTertiaryRotation(float craftTime) {
        float offset = 60;
        if (craftTime >= offset) {
            return (craftTime - offset) * (craftTime - offset) * 0.15f;
        }
        return 0;
    }

    @Override
    public float getVerticalOffset(float craftTime) {
        return -0.4f;
    }

    @Override
    public float getSizeModifier(float craftTime) {
        return 1.0f;
    }

    public float getTertiarySizeModifier(float craftTime) {
        return 1;
    }

    public float getCentralCircleOffset(float craftTime) {
        if (craftTime >= 40) {
            if (craftTime <= 100) {
                return (float) (-0.4 + 0.4 * Math.pow((craftTime - 40) / 60f, 3));
            }
            return 0;
        }
        return -0.4f;
    }

    public float getSymbolPitch(float craftTime) {
        if (craftTime > 70) {
            if (craftTime <= 100) {
                return 90 * (craftTime - 70) / 30f;
            }
            return 90;
        }
        return 0;
    }

    public float getCentralCirclePitch(float craftTime) {
        if (craftTime > 70) {
            if (craftTime <= 150) {
                return 360 * (craftTime - 70) / 80f;
            }
            return 360;
        }
        return 0;
    }

    public float moonDisplacement(float craftTime) {
        if (craftTime > 40) {
            if (craftTime <= 100) {
                return (float) (2 * Math.pow((craftTime - 40) / 60f, 3));
            }
            return 2;
        }
        return 0;
    }

    public float moonArc(float craftTime) {
        if (craftTime > 100) {
            if (craftTime <= 200) {
                return 180 * (craftTime - 100) / 100f;
            }
            return 180;
        }
        return 0;
    }

    @Override
    public void renderAt(AlchemyArrayTile tile, float craftTime, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        float rot = getRotation(craftTime);
        float secondaryRot = getSecondaryRotation(craftTime);
        float tertiaryRot = getTertiaryRotation(craftTime);
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

        // Central circle growing in
        poseStack.pushPose();
        poseStack.translate(0, getCentralCircleOffset(craftTime), 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotation.toYRot()));

        poseStack.pushPose();
        float pitch = getCentralCirclePitch(craftTime);
        poseStack.mulPose(Axis.XN.rotationDegrees(pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(-secondaryRot));

        buffer = bufferSource.getBuffer(RenderType.entityTranslucent(circleResource));
        model = Model2D.unitSquare(circleResource);

        float tertiarySize = getTertiarySizeModifier(craftTime);
        poseStack.scale(tertiarySize, tertiarySize, tertiarySize);
        RenderResizableQuadrilateral.INSTANCE.renderSquare(model, poseStack, buffer, 0xFFFFFFFF, 0x00F000F0, packedOverlay);

        poseStack.popPose();
        poseStack.popPose();

        // Moon arcing over the array
        poseStack.pushPose();
        poseStack.translate(0, getCentralCircleOffset(craftTime), 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotation.toYRot()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(moonArc(craftTime)));
        poseStack.translate(moonDisplacement(craftTime), 0, 0);

        poseStack.pushPose();
        pitch = getSymbolPitch(craftTime);
        poseStack.mulPose(Axis.XN.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZN.rotationDegrees(tertiaryRot));

        buffer = bufferSource.getBuffer(RenderType.entityTranslucent(symbolResource));
        model = Model2D.unitSquare(symbolResource);

        poseStack.scale(1.0F, 1.0F, 1.0F);
        RenderResizableQuadrilateral.INSTANCE.renderSquare(model, poseStack, buffer, 0xFFFFFFFF, 0x00F000F0, packedOverlay);

        poseStack.popPose();
        poseStack.popPose();

        poseStack.popPose();
    }
}
