package wayoftime.bloodmagic.compat.jei.arc;

import com.mojang.datafixers.util.Pair;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.recipe.arc.ARCRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ARCRecipeCategory implements IRecipeCategory<ARCRecipe> {
    public static final RecipeType<ARCRecipe> RECIPE_TYPE = RecipeType.create(BloodMagic.MODID, "alchemical_reaction_chamber", ARCRecipe.class);

    @Nonnull
    private final IDrawable background;
    private final IDrawable icon;

    public ARCRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(BMBlocks.ARC_BLOCK));
        background = guiHelper.createDrawable(BloodMagic.rl("gui/jei/arc.png"), 0, 0, 157, 43);
    }

    @Nonnull
    @Override
    public Component getTitle() {
        return Component.translatable("jei.bloodmagic.recipe.arc");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ARCRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 6).addIngredients(recipe.getInput()).setSlotName("input");
        builder.addSlot(RecipeIngredientRole.INPUT, 22, 17).addIngredients(recipe.getTool()).setSlotName("tool");

        List<Pair<ItemStack, Double>> allOutputs = recipe.getAllListedOutputs();
        for (int i = 0; i < allOutputs.size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 54 + i * 22, 17).addItemStack(allOutputs.get(i).getFirst()).setSlotName("output");
        }

        recipe.getInputFluid().ifPresent(fluid ->
                builder.addSlot(RecipeIngredientRole.INPUT, 1, 26).addIngredient(NeoForgeTypes.FLUID_STACK, fluid).setSlotName("inputFluid"));
        recipe.getOutputFluid().ifPresent(fluid ->
                builder.addSlot(RecipeIngredientRole.OUTPUT, 140, 7).addIngredient(NeoForgeTypes.FLUID_STACK, fluid).setSlotName("outputFluid"));
    }

    @Override
    public RecipeType<ARCRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public void draw(ARCRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        List<Pair<ItemStack, Double>> allOutputs = recipe.getAllListedOutputs();

        for (int i = 0; i < allOutputs.size(); i++) {
            double chance = allOutputs.get(i).getSecond();
            String text;
            if (chance >= 1) {
                text = "";
            } else if (chance < 0.01) {
                text = "<1%";
            } else {
                text = Math.round(chance * 100) + "%";
            }
            guiGraphics.drawString(mc.font, text, 86 + 22 * i - mc.font.width(text) / 2, 5, 0xFFFFFF, true);
        }
    }

    @Override
    public List<Component> getTooltipStrings(ARCRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        return new ArrayList<>();
    }
}
