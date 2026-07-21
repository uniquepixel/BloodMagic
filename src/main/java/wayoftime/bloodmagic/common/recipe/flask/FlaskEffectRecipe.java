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
 * Port of 1.20.1's {@code RecipePotionEffect}: adds a brand-new potion effect slot to the flask
 * (amplifier 0, both duration multipliers at 1.0) - the ingredient is a reagent representing that
 * effect (e.g. sugar -> Speed). Unlike {@link FlaskFillRecipe}, this never touches durability and
 * requires the flask NOT already have this effect.
 */
public class FlaskEffectRecipe extends FlaskRecipe
{
	private final Holder<MobEffect> outputEffect;
	private final int baseDuration;

	public FlaskEffectRecipe(List<Ingredient> inputs, int syphon, int ticks, int minimumTier, Holder<MobEffect> outputEffect, int baseDuration)
	{
		super(inputs, syphon, ticks, minimumTier);
		this.outputEffect = outputEffect;
		this.baseDuration = baseDuration;
	}

	public Holder<MobEffect> outputEffect()
	{
		return outputEffect;
	}

	public int baseDuration()
	{
		return baseDuration;
	}

	@Override
	public boolean canModifyFlask(FlaskEffects flaskEffects)
	{
		return flaskEffects.indexOfEffect(outputEffect) < 0;
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
		AlchemyFlaskItem.setFlaskEffects(output, flaskEffects.withAdded(new FlaskEffectEntry(outputEffect, baseDuration)));
		return output;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return BMRecipes.FLASK_EFFECT_SERIALIZER.get();
	}
}
