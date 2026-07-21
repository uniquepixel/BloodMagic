package wayoftime.bloodmagic.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.client.render.BMModelLayers;
import wayoftime.bloodmagic.client.render.model.ModelMeteor;

/**
 * Full-fidelity port of 1.20.1's {@code EntityMeteorRenderer}: bakes and draws the {@link ModelMeteor}
 * asteroid-cube model, textured with {@code textures/models/meteor.png}.
 */
@OnlyIn(Dist.CLIENT)
public class EntityMeteorRenderer extends EntityRenderer<Entity> {
    private static final ResourceLocation METEOR_LOCATION = BloodMagic.rl("textures/models/meteor.png");

    private final Model model;

    public EntityMeteorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.model = new ModelMeteor<>(context.bakeLayer(BMModelLayers.METEOR));
    }

    @Override
    public void render(Entity entityIn, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
        poseStack.pushPose();

        VertexConsumer vertexConsumer = bufferIn.getBuffer(this.model.renderType(this.getTextureLocation(entityIn)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
        super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return METEOR_LOCATION;
    }
}
