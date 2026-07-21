package wayoftime.bloodmagic.common.recipe.flask;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import wayoftime.bloodmagic.common.datacomponent.FlaskEffects;
import wayoftime.bloodmagic.common.recipe.BMRecipes;

import java.util.List;

/**
 * Port of 1.20.1's {@code RecipePotionFlaskTransform}: converts the flask ITEM ITSELF into a
 * different flask variant (e.g. base -&gt; Throwable, base -&gt; Lingering) while carrying over its
 * full current contents (stored effects and remaining uses/durability) verbatim. This is the recipe
 * family that actually turns a plain drinkable flask into a throwable one - {@code flask_splash} and
 * {@code flask_lingering} in the old datagen table. Lowest priority of all flask recipe types (0),
 * so it never shadows an effect-modifying recipe that also happens to match the same ingredients.
 */
public class FlaskItemTransformRecipe extends FlaskRecipe
{
	private final ItemStack output;

	public FlaskItemTransformRecipe(List<Ingredient> inputs, int syphon, int ticks, int minimumTier, ItemStack output)
	{
		super(inputs, syphon, ticks, minimumTier);
		this.output = output;
	}

	public ItemStack output()
	{
		return output;
	}

	@Override
	public boolean canModifyFlask(FlaskEffects flaskEffects)
	{
		// canModifyFlask only receives the effect list, but the actual gate ("not already this
		// variant") is on the flask's item type - handled in matches() below via the flask stack,
		// consistent with old RecipePotionFlaskTransform#canModifyFlask(flaskStack, ...).
		return true;
	}

	@Override
	public boolean matches(FlaskRecipeInput input, net.minecraft.world.level.Level level)
	{
		return input.flask().getItem() != output.getItem() && super.matches(input, level);
	}

	@Override
	public int getPriority(FlaskEffects flaskEffects)
	{
		return 0;
	}

	@Override
	public ItemStack getOutput(ItemStack flaskStack, FlaskEffects flaskEffects)
	{
		ItemStack result = output.copy();
		result.applyComponents(flaskStack.getComponentsPatch());
		result.setDamageValue(flaskStack.getDamageValue());
		return result;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return BMRecipes.FLASK_ITEM_TRANSFORM_SERIALIZER.get();
	}
}
