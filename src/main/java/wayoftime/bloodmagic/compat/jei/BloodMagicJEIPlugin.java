package wayoftime.bloodmagic.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.recipe.BMRecipes;
import wayoftime.bloodmagic.common.recipe.tiered.FluidTieredRecipe;
import wayoftime.bloodmagic.compat.jei.alchemytable.AlchemyTableRecipeCategory;
import wayoftime.bloodmagic.compat.jei.alchemytable.PotionRecipeCategory;
import wayoftime.bloodmagic.compat.jei.altar.BloodAltarRecipeCategory;
import wayoftime.bloodmagic.compat.jei.arc.ARCFurnaceRecipeCategory;
import wayoftime.bloodmagic.compat.jei.arc.ARCRecipeCategory;
import wayoftime.bloodmagic.compat.jei.array.AlchemyArrayRecipeCategory;
import wayoftime.bloodmagic.compat.jei.forge.SoulForgeRecipeCategory;
import wayoftime.bloodmagic.compat.jei.tank.BloodTankRecipeCategory;

import java.util.Objects;

@JeiPlugin
public class BloodMagicJEIPlugin implements IModPlugin {

    private static final ResourceLocation ID = BloodMagic.rl("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new AlchemyTableRecipeCategory(guiHelper));
        registration.addRecipeCategories(new PotionRecipeCategory(guiHelper));
        registration.addRecipeCategories(new ARCRecipeCategory(guiHelper));
        registration.addRecipeCategories(new ARCFurnaceRecipeCategory(guiHelper));
        registration.addRecipeCategories(new BloodAltarRecipeCategory(guiHelper));
        registration.addRecipeCategories(new SoulForgeRecipeCategory(guiHelper));
        registration.addRecipeCategories(new AlchemyArrayRecipeCategory(guiHelper));
        registration.addRecipeCategories(new BloodTankRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientLevel level = Objects.requireNonNull(Minecraft.getInstance().level);

        registration.addRecipes(AlchemyTableRecipeCategory.RECIPE_TYPE,
                level.getRecipeManager().getAllRecipesFor(BMRecipes.ALCHEMY_TABLE_TYPE.get()).stream().map(RecipeHolder::value).toList());
        registration.addRecipes(PotionRecipeCategory.RECIPE_TYPE,
                level.getRecipeManager().getAllRecipesFor(BMRecipes.FLASK_TYPE.get()).stream().map(RecipeHolder::value).toList());
        registration.addRecipes(ARCRecipeCategory.RECIPE_TYPE,
                level.getRecipeManager().getAllRecipesFor(BMRecipes.ARC_TYPE.get()).stream().map(RecipeHolder::value).toList());
        registration.addRecipes(BloodAltarRecipeCategory.RECIPE_TYPE,
                level.getRecipeManager().getAllRecipesFor(BMRecipes.BLOOD_ALTAR_TYPE.get()).stream().map(RecipeHolder::value).toList());
        registration.addRecipes(SoulForgeRecipeCategory.RECIPE_TYPE,
                level.getRecipeManager().getAllRecipesFor(BMRecipes.SOUL_FORGE_TYPE.get()).stream().map(RecipeHolder::value).toList());
        registration.addRecipes(ARCFurnaceRecipeCategory.RECIPE_TYPE,
                level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING).stream()
                        .map(RecipeHolder::value)
                        .filter(recipe -> recipe instanceof SmeltingRecipe)
                        .map(recipe -> (SmeltingRecipe) recipe)
                        .toList());
        registration.addRecipes(AlchemyArrayRecipeCategory.RECIPE_TYPE,
                level.getRecipeManager().getAllRecipesFor(BMRecipes.ALCHEMY_ARRAY_TYPE.get()).stream().map(RecipeHolder::value).toList());
        // FluidTieredRecipe (the Blood Tank tier-up) is a "special" CraftingRecipe registered
        // under plain RecipeType.CRAFTING - not BMRecipes.FLUID_TIERED_TYPE, which nothing ever
        // assigns as a recipe's actual getType() - so it has to be found the same way
        // ARCFurnaceRecipeCategory finds its SmeltingRecipes above: pull every RecipeType.CRAFTING
        // recipe and filter down by instanceof.
        registration.addRecipes(BloodTankRecipeCategory.RECIPE_TYPE,
                level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream()
                        .map(RecipeHolder::value)
                        .filter(recipe -> recipe instanceof FluidTieredRecipe)
                        .map(recipe -> (FluidTieredRecipe) recipe)
                        .toList());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(BMBlocks.ALCHEMY_TABLE), AlchemyTableRecipeCategory.RECIPE_TYPE, PotionRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BMBlocks.ARC_BLOCK), ARCRecipeCategory.RECIPE_TYPE, ARCFurnaceRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BMBlocks.BLOOD_ALTAR), BloodAltarRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BMBlocks.HELLFIRE_FORGE), SoulForgeRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BMBlocks.ALCHEMY_ARRAY), AlchemyArrayRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Items.CRAFTING_TABLE), BloodTankRecipeCategory.RECIPE_TYPE);
    }
}
