package wayoftime.bloodmagic.compat.jei.arc;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.api.BMTags;
import wayoftime.bloodmagic.common.block.BMBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ARCFurnaceRecipeCategory implements IRecipeCategory<SmeltingRecipe> {
    public static final RecipeType<SmeltingRecipe> RECIPE_TYPE = RecipeType.create(BloodMagic.MODID, "alchemical_reaction_chamber_furnace", SmeltingRecipe.class);

    @Nonnull
    private final IDrawable background;
    private final IDrawable icon;

    public ARCFurnaceRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(BMBlocks.ARC_BLOCK));
        background = guiHelper.createDrawable(BloodMagic.rl("gui/jei/arc.png"), 0, 0, 157, 43);
    }

    @Nonnull
    @Override
    public Component getTitle() {
        return Component.translatable("jei.bloodmagic.recipe.arcfurnace");
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SmeltingRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, 54, 17);
        output.addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));

        IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 1, 6);
        Ingredient ingredient = recipe.getIngredients().get(0);
        input.addIngredients(ingredient);

        IRecipeSlotBuilder catalyst = builder.addSlot(RecipeIngredientRole.CATALYST, 22, 17);
        catalyst.addIngredients(Ingredient.of(BMTags.Items.ARC_FURNACE));
    }

    @Override
    public RecipeType<SmeltingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }
}
