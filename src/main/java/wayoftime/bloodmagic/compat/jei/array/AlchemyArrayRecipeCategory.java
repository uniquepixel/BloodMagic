package wayoftime.bloodmagic.compat.jei.array;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.recipe.array.AlchemyArrayRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * JEI category for {@link AlchemyArrayRecipe}: the two-item Alchemy Array recipes consumed by
 * {@code AlchemyArrayTile#attemptCraft()} (base item placed first via right-click, then the
 * added/catalyst item; together they resolve an {@code AlchemyArrayEffect} which produces the
 * result once it finishes running). Ported from 1.20.1's {@code AlchemyArrayCraftingCategory},
 * following this branch's established {@code IRecipeCategory} shape (see
 * {@code BloodAltarRecipeCategory} for the closest sibling: a plain two-input/one-output layout).
 * <p>
 * 1.20.1's icon/catalyst was the "Arcane Ashes" item used to place the array in-world; that item
 * doesn't exist on this branch (see {@code AlchemyArrayBlock}'s class javadoc - placing the array
 * was redesigned around placing the block directly), so the icon/catalyst here is the Alchemy
 * Array block itself instead.
 */
public class AlchemyArrayRecipeCategory implements IRecipeCategory<AlchemyArrayRecipe> {
    public static final RecipeType<AlchemyArrayRecipe> RECIPE_TYPE = RecipeType.create(BloodMagic.MODID, "alchemy_array", AlchemyArrayRecipe.class);

    @Nonnull
    private final IDrawable background;
    private final IDrawable icon;

    public AlchemyArrayRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(BMBlocks.ALCHEMY_ARRAY));
        background = guiHelper.createDrawable(BloodMagic.rl("gui/jei/binding.png"), 0, 0, 100, 30);
    }

    @Nonnull
    @Override
    public Component getTitle() {
        return Component.translatable("jei.bloodmagic.recipe.alchemyarray");
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
    public void setRecipe(IRecipeLayoutBuilder builder, AlchemyArrayRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, 74, 6);
        output.addItemStack(recipe.getOutput());

        IRecipeSlotBuilder catalyst = builder.addSlot(RecipeIngredientRole.INPUT, 30, 4);
        catalyst.addIngredients(recipe.getAddedInput());

        IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, 1, 6);
        input.addIngredients(recipe.getBaseInput());
    }

    @Override
    public RecipeType<AlchemyArrayRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }
}
