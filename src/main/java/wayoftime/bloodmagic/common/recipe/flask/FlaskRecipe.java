package wayoftime.bloodmagic.common.recipe.flask;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.datacomponent.FlaskEffects;
import wayoftime.bloodmagic.common.item.potion.AlchemyFlaskItem;
import wayoftime.bloodmagic.common.recipe.BMRecipes;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared base of 1.20.1's {@code recipe/flask/RecipePotionFlaskBase} hierarchy: an Alchemy Table
 * recipe that reads/writes a flask's CURRENT stored {@link FlaskEffects} rather than just matching
 * plain item ingredients. All seven subtypes (fill/effect/length/potency/transform/cycle/item
 * -transform) share a single {@link RecipeType} ({@link BMRecipes#FLASK_TYPE}, matching the old
 * branch's single shared {@code "bloodmagic:potionflask"} type) so a crafting station can iterate
 * one recipe pool and use {@link #getPriority(FlaskEffects)} to disambiguate when more than one
 * recipe matches the same flask + ingredients.
 */
public abstract class FlaskRecipe implements Recipe<FlaskRecipeInput>
{
	public static final String RECIPE_TYPE_NAME = "potion_flask";
	public static final int MAX_INPUTS = 5;

	protected final List<Ingredient> inputs;
	protected final int syphon;
	protected final int ticks;
	protected final int minimumTier;

	protected FlaskRecipe(List<Ingredient> inputs, int syphon, int ticks, int minimumTier)
	{
		this.inputs = inputs;
		this.syphon = syphon;
		this.ticks = ticks;
		this.minimumTier = minimumTier;
	}

	public List<Ingredient> inputs()
	{
		return inputs;
	}

	public int syphon()
	{
		return syphon;
	}

	public int ticks()
	{
		return ticks;
	}

	public int minimumTier()
	{
		return minimumTier;
	}

	/** Gate check: can this recipe apply at all given the flask's current effect list? */
	public abstract boolean canModifyFlask(FlaskEffects flaskEffects);

	/** Higher wins when more than one flask recipe matches the same flask + ingredients. */
	public abstract int getPriority(FlaskEffects flaskEffects);

	/** Produces the resulting flask stack given the flask's CURRENT effect list. */
	public abstract ItemStack getOutput(ItemStack flaskStack, FlaskEffects flaskEffects);

	@Override
	public boolean matches(FlaskRecipeInput input, Level level)
	{
		if (!canModifyFlask(AlchemyFlaskItem.getFlaskEffects(input.flask())))
		{
			return false;
		}

		if (input.size() != inputs.size())
		{
			return false;
		}

		List<Ingredient> remaining = new ArrayList<>(inputs);
		for (int i = 0; i < input.size(); i++)
		{
			boolean matched = false;
			for (int j = 0; j < remaining.size(); j++)
			{
				if (remaining.get(j).test(input.getItem(i)))
				{
					matched = true;
					remaining.remove(j);
					break;
				}
			}
			if (!matched)
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public ItemStack assemble(FlaskRecipeInput input, HolderLookup.Provider registries)
	{
		return getOutput(input.flask(), AlchemyFlaskItem.getFlaskEffects(input.flask()));
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return true;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries)
	{
		// Not craftable through a plain grid - the true result depends on the flask's current
		// contents, only known at match/assemble time (mirrors old RecipePotionFlaskBase, which
		// likewise returns ItemStack.EMPTY here and is marked "special").
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}

	@Override
	public RecipeType<?> getType()
	{
		return BMRecipes.FLASK_TYPE.get();
	}
}
