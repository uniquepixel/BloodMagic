package wayoftime.bloodmagic.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.client.render.alchemyarray.AlchemyArrayRendererRegistry;
import wayoftime.bloodmagic.client.render.alchemyarray.AlchemyCircleRenderer;
import wayoftime.bloodmagic.common.blockentity.AlchemyArrayTile;
import wayoftime.bloodmagic.common.recipe.BMRecipes;
import wayoftime.bloodmagic.common.recipe.array.AlchemyArrayInput;
import wayoftime.bloodmagic.common.recipe.array.AlchemyArrayRecipe;

/**
 * Renderer for the Alchemy Array.
 * <p>
 * Full-fidelity restoration of 1.20.1's approach (a flat, textured, spinning ground billboard
 * per recipe - see {@code AlchemyCircleRenderer} and its 8 timing-curve subclasses under
 * {@code client.render.alchemyarray}), replacing this branch's earlier placeholder of floating,
 * spinning item stacks (kept the two-item-stack renderer only until the array art and rendering
 * utilities it depends on - {@code RenderResizableQuadrilateral}, {@code Model2D} - were ported).
 * <p>
 * Ported from 1.20.1's {@code RenderAlchemyArray}: re-resolves which recipe currently matches the
 * array's contents every frame (mirroring {@link AlchemyArrayTile#attemptCraft()}'s own recipe
 * lookup) purely to pick the right {@link AlchemyCircleRenderer} - the actual craft/effect logic
 * stays entirely server-authoritative in the tile.
 */
public class AlchemyArrayRenderer implements BlockEntityRenderer<AlchemyArrayTile> {
    private final RecipeManager.CachedCheck<AlchemyArrayInput, AlchemyArrayRecipe> recipeCheck =
            RecipeManager.createCheck(BMRecipes.ALCHEMY_ARRAY_TYPE.get());

    public AlchemyArrayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AlchemyArrayTile tile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();
        if (level == null) {
            return;
        }

        ItemStack base = tile.getInventory().getStackInSlot(AlchemyArrayTile.BASE_SLOT);
        ItemStack added = tile.getInventory().getStackInSlot(AlchemyArrayTile.ADDED_SLOT);
        if (base.isEmpty() && added.isEmpty()) {
            return;
        }

        AlchemyArrayInput input = new AlchemyArrayInput(base, added);
        RecipeHolder<AlchemyArrayRecipe> match = recipeCheck.getRecipeFor(input, level).orElse(null);

        ResourceLocation recipeId = match != null ? match.id() : null;
        AlchemyArrayRecipe recipe = match != null ? match.value() : null;
        AlchemyCircleRenderer renderer = AlchemyArrayRendererRegistry.getRenderer(recipeId, recipe);

        float craftTime = tile.activeCounter + partialTick;
        renderer.renderAt(tile, craftTime, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
