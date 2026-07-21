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

import java.util.List;

/**
 * Port of 1.20.1's {@code RecipePotionIncreasePotency}: raises the matching effect's amplifier (and
 * its paired {@code ampDurationMod}) - a strict upgrade only (requires either a higher amplifier, or
 * the same amplifier with a higher {@code ampDurationMod} as a tiebreak). There's no separate code
 * path for "average" potency tiers - those are just different data (a higher amplifier bundled with
 * a larger {@code ampDurationMod}), see the datagen table in {@code BMRecipeProvider}.
 */
public class FlaskPotencyRecipe extends FlaskRecipe
{
	private final Holder<MobEffect> outputEffect;
	private final int amplifier;
	private final double ampDurationMod;

	public FlaskPotencyRecipe(List<Ingredient> inputs, int syphon, int ticks, int minimumTier, Holder<MobEffect> outputEffect, int amplifier, double ampDurationMod)
	{
		super(inputs, syphon, ticks, minimumTier);
		this.outputEffect = outputEffect;
		this.amplifier = amplifier;
		this.ampDurationMod = ampDurationMod;
	}

	public Holder<MobEffect> outputEffect()
	{
		return outputEffect;
	}

	public int amplifier()
	{
		return amplifier;
	}

	public double ampDurationMod()
	{
		return ampDurationMod;
	}

	private int matchIndex(FlaskEffects flaskEffects)
	{
		return flaskEffects.indexOfEffect(outputEffect);
	}

	@Override
	public boolean canModifyFlask(FlaskEffects flaskEffects)
	{
		int index = matchIndex(flaskEffects);
		if (index < 0)
		{
			return false;
		}
		FlaskEffectEntry current = flaskEffects.entries().get(index);
		return current.amplifier() < amplifier || (current.amplifier() == amplifier && current.ampDurationMod() < ampDurationMod);
	}

	@Override
	public int getPriority(FlaskEffects flaskEffects)
	{
		return matchIndex(flaskEffects) + 1;
	}

	@Override
	public ItemStack getOutput(ItemStack flaskStack, FlaskEffects flaskEffects)
	{
		int index = matchIndex(flaskEffects);
		ItemStack output = flaskStack.copy();
		FlaskEffectEntry updated = flaskEffects.entries().get(index).withPotency(amplifier, ampDurationMod);
		AlchemyFlaskItem.setFlaskEffects(output, flaskEffects.withReplacedAt(index, updated));
		return output;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return BMRecipes.FLASK_POTENCY_SERIALIZER.get();
	}
}
