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
 * Port of 1.20.1's {@code RecipePotionIncreaseLength}: raises the matching effect's
 * {@code lengthDurationMod} - a strict upgrade only (requires the flask already hold
 * {@link #outputEffect} with a lower length multiplier than {@link #lengthDurationMod}).
 * Amplifier/potency is untouched.
 */
public class FlaskLengthRecipe extends FlaskRecipe
{
	private final Holder<MobEffect> outputEffect;
	private final double lengthDurationMod;

	public FlaskLengthRecipe(List<Ingredient> inputs, int syphon, int ticks, int minimumTier, Holder<MobEffect> outputEffect, double lengthDurationMod)
	{
		super(inputs, syphon, ticks, minimumTier);
		this.outputEffect = outputEffect;
		this.lengthDurationMod = lengthDurationMod;
	}

	public Holder<MobEffect> outputEffect()
	{
		return outputEffect;
	}

	public double lengthDurationMod()
	{
		return lengthDurationMod;
	}

	private int matchIndex(FlaskEffects flaskEffects)
	{
		return flaskEffects.indexOfEffect(outputEffect);
	}

	@Override
	public boolean canModifyFlask(FlaskEffects flaskEffects)
	{
		int index = matchIndex(flaskEffects);
		return index >= 0 && flaskEffects.entries().get(index).lengthDurationMod() < lengthDurationMod;
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
		FlaskEffectEntry updated = flaskEffects.entries().get(index).withLengthDurationMod(lengthDurationMod);
		AlchemyFlaskItem.setFlaskEffects(output, flaskEffects.withReplacedAt(index, updated));
		return output;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return BMRecipes.FLASK_LENGTH_SERIALIZER.get();
	}
}
