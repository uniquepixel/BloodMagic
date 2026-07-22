package wayoftime.bloodmagic.client.render.alchemyarray;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.blockentity.AlchemyArrayTile;

/**
 * Ported from 1.20.1's {@code BindingAlchemyCircleRenderer}: a large central circle plus 5
 * smaller "lightning" circles that sweep inward from a distance over 5 timed passes before
 * collapsing into the center - used for the Living armor Binding arrays.
 * <p>
 * Note this class only overloads {@code getRotation}/{@code getSecondaryRotation}/
 * {@code getVerticalOffset} with an extra {@code circle} parameter; it deliberately does not
 * override the single-argument versions inherited from {@link AlchemyCircleRenderer} (this
 * matches 1.20.1 exactly - the outer circle's rotation/vertical offset and the base secondary
 * rotation genuinely fall back to the parent class's plain timing curves).
 */
public class BindingAlchemyCircleRenderer extends AlchemyCircleRenderer {
    public static final int numberOfSweeps = 5;
    public static final int startTime = 50;
    public static final int sweepTime = 40;
    public static final int inwardRotationTime = 50;
    public static final float arcLength = (float) Math.sqrt(2 * (2 * 2) - 2 * 2 * 2 * Math.cos(2 * Math.PI * 2 / 5));
    public static final float theta2 = (float) (18f * Math.PI / 180f);
    public static final int endTime = 300;

    public final ResourceLocation[] arraysResources;

    public BindingAlchemyCircleRenderer() {
        super(BloodMagic.rl("textures/models/alchemyarrays/bindingarray.png"));
        arraysResources = new ResourceLocation[5];
        for (int i = 0; i < arraysResources.length; i++) {
            arraysResources[i] = BloodMagic.rl("textures/models/alchemyarrays/bindinglightningarray.png");
        }
    }

    public float getRotation(int circle, float craftTime) {
        float offset = 2;
        if (circle == -1 && craftTime >= offset) {
            return (craftTime - offset) * 360 * 2 / 5 / sweepTime;
        }
        if (craftTime >= offset) {
            return (float) Math.pow(craftTime - offset, 1.5) * 0.5f;
        }
        return 0;
    }

    public float getVerticalOffset(int circle, float craftTime) {
        if (craftTime >= 5) {
            if (craftTime <= 40) {
                return (float) (0.4 * Math.pow((craftTime - 5) / 35f, 3));
            }
            return 0.4f;
        }
        return 0;
    }

    public float getInwardRotation(int circle, float craftTime) {
        float offset = startTime + numberOfSweeps * sweepTime;
        if (craftTime >= offset) {
            if (craftTime <= offset + inwardRotationTime) {
                return 90f / inwardRotationTime * (craftTime - offset);
            }
            return 90;
        }
        return 0;
    }

    @Override
    public void renderAt(AlchemyArrayTile tile, float craftTime, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        float rot = getRotation(-1, craftTime);
        float size = 3;
        Direction dirRotation = tile.getRotation();

        poseStack.pushPose();
        poseStack.translate(0, getVerticalOffset(craftTime), 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-dirRotation.toYRot()));

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(rot));

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(arrayResource));
        Model2D model = Model2D.unitSquare(arrayResource);

        poseStack.scale(size, size, size);
        RenderResizableQuadrilateral.INSTANCE.renderSquare(model, poseStack, buffer, 0xFFFFFFFF, packedLight, packedOverlay);

        poseStack.popPose();

        for (int i = 0; i < 5; i++) {
            poseStack.pushPose();

            float newSize = 1;
            float distance = getDistanceOfCircle(i, craftTime);
            float angle = getAngleOfCircle(i, craftTime);
            float rotation = getRotation(i, craftTime);

            poseStack.translate(distance * Math.sin(angle), getVerticalOffset(i, craftTime), -distance * Math.cos(angle));
            poseStack.mulPose(Axis.YP.rotationDegrees(i * 360 / 5));
            poseStack.mulPose(Axis.ZN.rotationDegrees(getInwardRotation(i, craftTime)));
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

            buffer = bufferSource.getBuffer(RenderType.entityTranslucent(arraysResources[i]));
            model = Model2D.unitSquare(arraysResources[i]);

            poseStack.scale(newSize, newSize, newSize);
            RenderResizableQuadrilateral.INSTANCE.renderSquare(model, poseStack, buffer, 0xFFFFFFFF, packedLight, packedOverlay);

            poseStack.popPose();
        }

        poseStack.popPose();
        poseStack.popPose();
    }

    public static float getAngleOfCircle(int circle, float craftTime) {
        if (circle < 0 || circle > 4) {
            return 0;
        }

        float originalAngle = (float) (circle * 2 * Math.PI / 5d);

        double sweep = (craftTime - startTime) / sweepTime;
        if (sweep >= 0 && sweep < numberOfSweeps) {
            float offset = ((int) sweep) * sweepTime + startTime;
            originalAngle += 2 * Math.PI * 2 / 5 * (int) sweep + getAngle(craftTime - offset, (int) sweep);
        } else if (sweep >= numberOfSweeps) {
            originalAngle += 2 * Math.PI * 2 / 5 * numberOfSweeps + (craftTime - 5 * sweepTime - startTime) * 2 * Math.PI * 2 / 5 / sweepTime;
        }

        return originalAngle;
    }

    public static float getAngle(float craftTime, int sweep) {
        float rDP = craftTime / sweepTime * arcLength;
        float rEnd = (float) Math.sqrt(rDP * rDP + 2 * 2 - 2 * rDP * 2 * Math.cos(theta2));
        return (float) Math.acos((2 * 2 + rEnd * rEnd - rDP * rDP) / (2 * rEnd * 2));
    }

    /**
     * Returns the center-to-center distance of this circle.
     */
    public static float getDistanceOfCircle(int circle, float craftTime) {
        double sweep = (craftTime - startTime) / sweepTime;
        if (sweep >= 0 && sweep < numberOfSweeps) {
            float offset = ((int) sweep) * sweepTime + startTime;
            float angle = getAngle(craftTime - offset, (int) sweep);
            float thetaPrime = (float) (Math.PI - theta2 - angle);
            return (float) (2 * Math.sin(theta2) / Math.sin(thetaPrime));
        } else if (sweep >= numberOfSweeps && craftTime < endTime) {
            return 2 - 2 * (craftTime - startTime - numberOfSweeps * sweepTime) / (endTime - startTime - numberOfSweeps * sweepTime);
        } else if (craftTime >= endTime) {
            return 0;
        }

        return 2;
    }
}
