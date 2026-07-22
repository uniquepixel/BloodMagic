package wayoftime.bloodmagic.common.recipe.array;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.recipe.BMRecipes;

/**
 * Ported from 1.20.1's {@code RecipeAlchemyArray}, including the per-recipe ground-circle
 * {@link #texture} field that drives which art {@code AlchemyArrayRendererRegistry} (client-side)
 * picks for this recipe - see that class's javadoc for how the id/texture -> renderer mapping
 * works. The texture is purely cosmetic data here (a {@link ResourceLocation} value, not a
 * rendering call), so it's safe to carry on this common-side recipe like any other field.
 */
public class AlchemyArrayRecipe implements Recipe<AlchemyArrayInput> {
    public static final String RECIPE_TYPE_NAME = "array";

    /**
     * Matches 1.20.1's {@code AlchemyArrayRendererRegistry.DEFAULT_RENDERER} texture, used for any
     * recipe that doesn't specify one explicitly.
     */
    public static final ResourceLocation DEFAULT_TEXTURE = BloodMagic.rl("textures/models/alchemyarrays/basearray.png");

    private final Ingredient baseInput;
    private final Ingredient addedInput;
    private final ItemStack output;
    private final ResourceLocation texture;

    public AlchemyArrayRecipe(Ingredient baseInput, Ingredient addedInput, ItemStack output, ResourceLocation texture) {
        this.baseInput = baseInput;
        this.addedInput = addedInput;
        this.output = output;
        this.texture = texture;
    }

    public AlchemyArrayRecipe(Ingredient baseInput, Ingredient addedInput, ItemStack output) {
        this(baseInput, addedInput, output, DEFAULT_TEXTURE);
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

    public ResourceLocation getTexture() {
        return texture;
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
