package wayoftime.bloodmagic.common.recipe.array;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.recipe.BMRecipes;

public class AlchemyArrayRecipe implements Recipe<AlchemyArrayInput> {
    public static final String RECIPE_TYPE_NAME = "array";

    private final Ingredient baseInput;
    private final Ingredient addedInput;
    private final ItemStack output;

    public AlchemyArrayRecipe(Ingredient baseInput, Ingredient addedInput, ItemStack output) {
        this.baseInput = baseInput;
        this.addedInput = addedInput;
        this.output = output;
    }

    public Ingredient getBaseInput() {
        return baseInput;
    }

    public Ingredient getAddedInput() {
        return addedInput;
    }

    public ItemStack getOutput() {
        return output;
    }

    @Override
    public boolean matches(AlchemyArrayInput input, Level level) {
        return baseInput.test(input.getItem(0)) && addedInput.test(input.getItem(1));
    }

    @Override
    public ItemStack assemble(AlchemyArrayInput input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BMRecipes.ALCHEMY_ARRAY_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return BMRecipes.ALCHEMY_ARRAY_TYPE.get();
    }
}
