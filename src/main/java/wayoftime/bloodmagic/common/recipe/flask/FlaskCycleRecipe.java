package wayoftime.bloodmagic.common.recipe.flask;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import wayoftime.bloodmagic.common.datacomponent.FlaskEffects;
import wayoftime.bloodmagic.common.item.potion.AlchemyFlaskItem;
import wayoftime.bloodmagic.common.recipe.BMRecipes;

import java.util.List;

/**
 * Port of 1.20.1's {@code RecipePotionCycle}: rotates the flask's effect list left by
 * {@link #numCycles}. Since {@link FlaskLengthRecipe}/{@link FlaskPotencyRecipe} target an effect by
 * its position in that list (see their {@code getPriority}), cycling changes which effect index-
 * sensitive recipes/UI treat as "first" without altering the effects themselves. Requires at least
 * two effects (cycling one effect is a no-op).
 */
public class FlaskCycleRecipe extends FlaskRecipe
{
	private final int numCycles;

	public FlaskCycleRecipe(List<Ingredient> inputs, int syphon, int ticks, int minimumTier, int numCycles)
	{
		super(inputs, syphon, ticks, minimumTier);
		this.numCycles = numCycles;
	}

	public int numCycles()
	{
		return numCycles;
	}

	@Override
	public boolean canModifyFlask(FlaskEffects flaskEffects)
	{
		return flaskEffects.size() >= 2;
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
		AlchemyFlaskItem.setFlaskEffects(output, flaskEffects.rotatedLeft(numCycles));
		return output;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return BMRecipes.FLASK_CYCLE_SERIALIZER.get();
	}
}
