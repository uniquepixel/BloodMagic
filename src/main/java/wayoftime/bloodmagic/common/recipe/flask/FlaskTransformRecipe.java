package wayoftime.bloodmagic.common.recipe.flask;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import wayoftime.bloodmagic.common.datacomponent.FlaskEffectEntry;
import wayoftime.bloodmagic.common.datacomponent.FlaskEffects;
import wayoftime.bloodmagic.common.item.potion.AlchemyFlaskItem;
import wayoftime.bloodmagic.common.recipe.BMRecipes;

import java.util.ArrayList;
import java.util.List;

/**
 * Port of 1.20.1's {@code RecipePotionTransform}: consumes N of the flask's current effect TYPES
 * ({@link #inputEffectList}, a multiset match against the flask's held effects - order-independent)
 * and turns them into M output effects ({@link #outputEffectList}, each an effect + minimum base
 * duration). Used for things like Poison -> Harm, Night Vision -> Invisibility, etc.
 * <p>
 * 1:1 transforms (exactly one input effect type, one output) carry the input's amplifier/duration
 * multipliers over to the output ({@code savePotencies}); anything else resets the output to
 * amplifier 0 / both multipliers 1.0. If an output effect type is already present after removing the
 * consumed inputs, its base duration is only bumped up (never duplicated, never shortened).
 */
public class FlaskTransformRecipe extends FlaskRecipe
{
	private final List<FlaskEffectAmount> outputEffectList;
	private final List<Holder<MobEffect>> inputEffectList;

	public FlaskTransformRecipe(List<Ingredient> inputs, int syphon, int ticks, int minimumTier,
			List<FlaskEffectAmount> outputEffectList, List<Holder<MobEffect>> inputEffectList)
	{
		super(inputs, syphon, ticks, minimumTier);
		this.outputEffectList = outputEffectList;
		this.inputEffectList = inputEffectList;
	}

	public List<FlaskEffectAmount> outputEffectList()
	{
		return outputEffectList;
	}

	public List<Holder<MobEffect>> inputEffectList()
	{
		return inputEffectList;
	}

	private int getDuplicateEffects(FlaskEffects flaskEffects)
	{
		int count = 0;
		for (FlaskEffectAmount output : outputEffectList)
		{
			for (FlaskEffectEntry entry : flaskEffects.entries())
			{
				if (entry.effect().is(output.effect()) && output.baseDuration() <= entry.baseDuration())
				{
					count++;
					break;
				}
			}
		}
		return count;
	}

	@Override
	public boolean canModifyFlask(FlaskEffects flaskEffects)
	{
		if (flaskEffects.size() < inputEffectList.size())
		{
			return false;
		}
		if (getDuplicateEffects(flaskEffects) >= outputEffectList.size())
		{
			// Already fully satisfied - don't let the recipe re-run on an already-transformed flask.
			return false;
		}

		List<FlaskEffectEntry> remaining = new ArrayList<>(flaskEffects.entries());
		for (Holder<MobEffect> required : inputEffectList)
		{
			boolean matched = false;
			for (int i = 0; i < remaining.size(); i++)
			{
				if (remaining.get(i).effect().is(required))
				{
					matched = true;
					remaining.remove(i);
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
	public int getPriority(FlaskEffects flaskEffects)
	{
		int priority = 0;
		List<FlaskEffectEntry> entries = flaskEffects.entries();
		for (int i = 0; i < entries.size(); i++)
		{
			for (Holder<MobEffect> required : inputEffectList)
			{
				if (entries.get(i).effect().is(required))
				{
					priority += i + 1;
					break;
				}
			}
		}
		return priority;
	}

	@Override
	public ItemStack getOutput(ItemStack flaskStack, FlaskEffects flaskEffects)
	{
		boolean savePotencies = outputEffectList.size() == 1 && inputEffectList.size() == 1;

		int amplifier = 0;
		double ampDurationMod = 1;
		double lengthDurationMod = 1;

		List<FlaskEffectEntry> remaining = new ArrayList<>(flaskEffects.entries());
		for (Holder<MobEffect> required : inputEffectList)
		{
			for (int i = 0; i < remaining.size(); i++)
			{
				FlaskEffectEntry entry = remaining.get(i);
				if (entry.effect().is(required))
				{
					if (savePotencies)
					{
						amplifier = entry.amplifier();
						ampDurationMod = entry.ampDurationMod();
						lengthDurationMod = entry.lengthDurationMod();
					}
					remaining.remove(i);
					break;
				}
			}
		}

		for (FlaskEffectAmount output : outputEffectList)
		{
			int existingIndex = -1;
			for (int i = 0; i < remaining.size(); i++)
			{
				if (remaining.get(i).effect().is(output.effect()))
				{
					existingIndex = i;
					break;
				}
			}

			if (existingIndex >= 0)
			{
				FlaskEffectEntry existing = remaining.get(existingIndex);
				if (existing.baseDuration() < output.baseDuration())
				{
					remaining.set(existingIndex, existing.withBaseDuration(output.baseDuration()));
				}
			}
			else
			{
				remaining.add(new FlaskEffectEntry(output.effect(), output.baseDuration(), amplifier, ampDurationMod, lengthDurationMod));
			}
		}

		ItemStack result = flaskStack.copy();
		AlchemyFlaskItem.setFlaskEffects(result, new FlaskEffects(remaining));
		return result;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return BMRecipes.FLASK_TRANSFORM_SERIALIZER.get();
	}
}
