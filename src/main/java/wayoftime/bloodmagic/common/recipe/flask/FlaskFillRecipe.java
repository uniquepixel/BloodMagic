package wayoftime.bloodmagic.common.recipe.flask;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import wayoftime.bloodmagic.common.datacomponent.FlaskEffects;
import wayoftime.bloodmagic.common.item.potion.AlchemyFlaskItem;
import wayoftime.bloodmagic.common.recipe.BMRecipes;

import java.util.List;

/**
 * Port of 1.20.1's {@code RecipePotionFill}. Semantically this is NOT "add a potion to an empty
 * flask" - it's "refill/restore the flask's remaining uses (reset damage to 0) while keeping (up to
 * {@link #maxEffects} of) its current effects", i.e. the ingredient is a Filling Agent that repairs
 * durability and optionally trims the effect count down to the agent's cap. Requires the flask to
 * already hold at least one effect.
 */
public class FlaskFillRecipe extends FlaskRecipe
{
	private final int maxEffects;

	public FlaskFillRecipe(List<Ingredient> inputs, int syphon, int ticks, int minimumTier, int maxEffects)
	{
		super(inputs, syphon, ticks, minimumTier);
		this.maxEffects = maxEffects;
	}

	public int maxEffects()
	{
		return maxEffects;
	}

	@Override
	public boolean canModifyFlask(FlaskEffects flaskEffects)
	{
		return !flaskEffects.isEmpty();
	}

	@Override
	public int getPriority(FlaskEffects flaskEffects)
	{
		return 1;
	}

	@Override
	public ItemStack getOutput(ItemStack flaskStack, FlaskEffects flaskEffects)
	{
		ItemStack output = flaskStack.copy();
		AlchemyFlaskItem.setFlaskEffects(output, flaskEffects.trimmedTo(maxEffects));
		output.setDamageValue(0);
		return output;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return BMRecipes.FLASK_FILL_SERIALIZER.get();
	}
}
