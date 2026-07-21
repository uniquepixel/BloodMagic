package wayoftime.bloodmagic.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.blockentity.AlchemyArrayTile;

/**
 * Renderer for the Alchemy Array.
 * <p>
 * 1.20.1 rendered a large stack of bespoke, per-recipe rotating circle textures on the ground
 * (movement/updraft/spike/day/night/bounce/binding each had their own art, driven by a family of
 * {@code *AlchemyCircleRenderer} classes doing custom quadrilateral geometry - see
 * {@code AlchemyArrayRendererRegistry} in 1.20.1). None of that per-array art
 * ({@code textures/models/alchemyarrays/*.png}) has been ported to this branch, so reproducing
 * that system would mean drawing bespoke circles with no matching texture. Instead, this
 * renderer follows the same pattern as {@link BloodAltarRenderer}: it floats the two placed
 * ingredient item stacks (base, then added) just above the block, slowly spinning - enough to
 * show at a glance what's queued/consuming without depending on the unported array art.
 */
public class AlchemyArrayRenderer implements BlockEntityRenderer<AlchemyArrayTile> {

    public AlchemyArrayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AlchemyArrayTile tile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderItem(tile.getInventory().getStackInSlot(AlchemyArrayTile.BASE_SLOT), 0.3, tile.getLevel(), poseStack, bufferSource, packedLight, packedOverlay);
        renderItem(tile.getInventory().getStackInSlot(AlchemyArrayTile.ADDED_SLOT), 0.55, tile.getLevel(), poseStack, bufferSource, packedLight, packedOverlay);
    }

    private void renderItem(ItemStack stack, double height, Level level, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (stack.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();

        poseStack.pushPose();
        poseStack.translate(0.5, height, 0.5);
        float rotation = (float) (720.0F * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.scale(0.4F, 0.4F, 0.4F);
        BakedModel bakedModel = itemRenderer.getModel(stack, level, (LivingEntity) null, 1);
        itemRenderer.render(stack, ItemDisplayContext.FIXED, true, poseStack, bufferSource, packedLight, packedOverlay, bakedModel);
        poseStack.popPose();
    }
}
