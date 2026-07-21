package wayoftime.bloodmagic.client.render.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Full-fidelity port of 1.20.1's {@code ModelMeteor}: a chunky "asteroid" made of seven overlapping
 * cubes (one big central cube plus six smaller ones jutting off it at odd offsets), all rendered as a
 * single static pose - meteors don't animate, they just fall and explode.
 */
@OnlyIn(Dist.CLIENT)
public class ModelMeteor<T extends Entity> extends EntityModel<T> {
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("shape1", CubeListBuilder.create().texOffs(0, 0).addBox(-8, -8, -8, 16, 16, 16).mirror(true), PartPose.offset(0, 0, 0));
        partdefinition.addOrReplaceChild("shape2", CubeListBuilder.create().texOffs(0, 32).addBox(3F, -10F, -1F, 12, 12, 12).mirror(true), PartPose.offset(0, 0, 0));
        partdefinition.addOrReplaceChild("shape3", CubeListBuilder.create().texOffs(0, 32).addBox(0F, 0F, -10F, 12, 12, 12).mirror(true), PartPose.offset(0, 0, 0));
        partdefinition.addOrReplaceChild("shape4", CubeListBuilder.create().texOffs(0, 32).addBox(1F, 2F, 2F, 12, 12, 12).mirror(true), PartPose.offset(0, 0, 0));
        partdefinition.addOrReplaceChild("shape5", CubeListBuilder.create().texOffs(0, 32).addBox(-12F, -5F, 0F, 12, 12, 12).mirror(true), PartPose.offset(0, 0, 0));
        partdefinition.addOrReplaceChild("shape6", CubeListBuilder.create().texOffs(0, 32).addBox(-13F, -2F, -11F, 12, 12, 12).mirror(true), PartPose.offset(0, 0, 0));
        partdefinition.addOrReplaceChild("shape7", CubeListBuilder.create().texOffs(0, 32).addBox(-6F, -14F, -9F, 12, 12, 12).mirror(true), PartPose.offset(0, 0, 0));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape3;
    private final ModelPart shape4;
    private final ModelPart shape5;
    private final ModelPart shape6;
    private final ModelPart shape7;

    public ModelMeteor(ModelPart model) {
        this.shape1 = model.getChild("shape1");
        this.shape2 = model.getChild("shape2");
        this.shape3 = model.getChild("shape3");
        this.shape4 = model.getChild("shape4");
        this.shape5 = model.getChild("shape5");
        this.shape6 = model.getChild("shape6");
        this.shape7 = model.getChild("shape7");
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        for (ModelPart shape : meteorParts()) {
            shape.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }

    private Iterable<ModelPart> meteorParts() {
        return ImmutableList.of(shape1, shape2, shape3, shape4, shape5, shape6, shape7);
    }

    @Override
    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Meteors don't animate - a single static pose is rendered every frame.
    }
}
