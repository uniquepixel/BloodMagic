package wayoftime.bloodmagic.compat.jei.tank;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.recipe.tiered.FluidTieredRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * JEI category for {@link FluidTieredRecipe}: the Blood Tank tier-up recipe (2 Blood Tanks of the
 * same tier + iron ingots + a glass block -> 1 Blood Tank of the next tier up). Unlike this
 * package's other categories, {@link FluidTieredRecipe} extends {@code CustomRecipe} (see
 * {@code BaseTieredRecipe#isSpecial()}, which returns {@code true}) and is registered under plain
 * vanilla {@code RecipeType.CRAFTING} rather than a Blood Magic {@code RecipeType} - matching
 * vanilla "special" crafting recipes (tipped arrows, firework stars, banner duplication, ...),
 * whose ingredients are dynamic/NBT-dependent and therefore never picked up by JEI's generic
 * crafting-table integration. That's why only the tier-up recipe needs a category here: the base
 * "craft a tier-1 tank" recipe in {@code BMRecipeProvider} is a completely ordinary
 * {@code ShapedRecipeBuilder} recipe (not a {@link FluidTieredRecipe}), so it already renders fine
 * in JEI's built-in Crafting Table category with zero extra code, exactly like any other datapack
 * shaped recipe.
 * <p>
 * There is only ever one {@link FluidTieredRecipe} instance registered (see
 * {@code BMRecipeProvider}'s "blood_tank_tier_up" recipe) since {@link FluidTieredRecipe#matches}
 * and {@link FluidTieredRecipe#assemble} key off the placed tanks' own {@code CONTAINER_TIER} data
 * component rather than a fixed tier baked into the recipe, covering all 15 upgrades (tier 1->2
 * through 15->16) generically. To make that generic behaviour legible in JEI, the primary/secondary
 * tank slots and the output slot each cycle through a full set of example tank stacks (tiers 1-15
 * in, 2-16 out) built here via the {@code CONTAINER_TIER} component, rather than showing only the
 * single static tier-1 {@link FluidTieredRecipe#getOutput()} stack the recipe itself carries.
 * <p>
 * The iron/glass ingredients aren't read off the recipe (unlike {@code AlchemyTableRecipe#inputs()}
 * for example, {@link FluidTieredRecipe} exposes no per-cell ingredient accessor - only
 * {@code pattern}/{@code primary}/{@code secondary}/{@code output}) and are instead declared here
 * directly against the same {@code c:ingots/iron} / {@code c:glass_blocks} tags
 * {@code BMRecipeProvider}'s "blood_tank_tier_up" recipe uses, mirroring how
 * {@code ARCFurnaceRecipeCategory} hardcodes its catalyst ingredient rather than reading it off the
 * {@code SmeltingRecipe} instance.
 */
public class BloodTankRecipeCategory implements IRecipeCategory<FluidTieredRecipe> {
    public static final RecipeType<FluidTieredRecipe> RECIPE_TYPE = RecipeType.create(BloodMagic.MODID, "blood_tank", FluidTieredRecipe.class);

    private static final int MAX_TIER = 16;

    private static final Ingredient IRON_INGREDIENT = Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron")));
    private static final Ingredient GLASS_INGREDIENT = Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:glass_blocks")));

    @Nonnull
    private final IDrawable background;
    private final IDrawable icon;

    private final List<ItemStack> inputTankExamples;
    private final List<ItemStack> outputTankExamples;

    public BloodTankRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(BMBlocks.BLOOD_TANK.item().get()));
        background = guiHelper.createDrawable(BloodMagic.rl("gui/jei/bloodtank.png"), 0, 0, 100, 58);

        List<ItemStack> inputs = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();
        for (int tier = 1; tier < MAX_TIER; tier++) {
            inputs.add(tankAtTier(tier));
            outputs.add(tankAtTier(tier + 1));
        }
        inputTankExamples = inputs;
        outputTankExamples = outputs;
    }

    private static ItemStack tankAtTier(int tier) {
        ItemStack stack = new ItemStack(BMBlocks.BLOOD_TANK.item().get());
        stack.set(BMDataComponents.CONTAINER_TIER, tier);
        return stack;
    }

    @Nonnull
    @Override
    public Component getTitle() {
        return Component.translatable("jei.bloodmagic.recipe.bloodtank");
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
    public void setRecipe(IRecipeLayoutBuilder builder, FluidTieredRecipe recipe, IFocusGroup focuses) {
        // Mirrors the actual 3x3 "ipi" / " g " / "isi" crafting-grid pattern from
        // BMRecipeProvider's "blood_tank_tier_up" recipe, so the JEI layout matches what a player
        // sees in the crafting grid itself.
        IRecipeSlotBuilder ironTopLeft = builder.addSlot(RecipeIngredientRole.INPUT, 1, 1);
        ironTopLeft.addIngredients(IRON_INGREDIENT);

        IRecipeSlotBuilder primaryTank = builder.addSlot(RecipeIngredientRole.INPUT, 19, 1);
        primaryTank.addItemStacks(inputTankExamples);

        IRecipeSlotBuilder ironTopRight = builder.addSlot(RecipeIngredientRole.INPUT, 37, 1);
        ironTopRight.addIngredients(IRON_INGREDIENT);

        IRecipeSlotBuilder glass = builder.addSlot(RecipeIngredientRole.INPUT, 19, 19);
        glass.addIngredients(GLASS_INGREDIENT);

        IRecipeSlotBuilder ironBottomLeft = builder.addSlot(RecipeIngredientRole.INPUT, 1, 37);
        ironBottomLeft.addIngredients(IRON_INGREDIENT);

        IRecipeSlotBuilder secondaryTank = builder.addSlot(RecipeIngredientRole.INPUT, 19, 37);
        secondaryTank.addItemStacks(inputTankExamples);

        IRecipeSlotBuilder ironBottomRight = builder.addSlot(RecipeIngredientRole.INPUT, 37, 37);
        ironBottomRight.addIngredients(IRON_INGREDIENT);

        IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, 74, 19);
        output.addItemStacks(outputTankExamples);
    }

    @Override
    public RecipeType<FluidTieredRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }
}
