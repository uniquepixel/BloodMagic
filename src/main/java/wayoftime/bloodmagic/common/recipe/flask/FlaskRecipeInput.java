package wayoftime.bloodmagic.common.recipe.flask;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Input container for the flask recipe family. Unlike a normal crafting-grid recipe, a flask
 * recipe's "input" is really two things: the flask itself (whose CURRENT stored
 * {@code FlaskEffects} the recipe reads and transforms - see {@code FlaskRecipe#canModifyFlask}) and
 * the other reagent ingredients placed alongside it in the Alchemy Table. {@link #flask} is kept
 * separate from the {@link RecipeInput} grid contract (which only covers {@link #ingredients}) since
 * it never participates in the plain ingredient-matching pass.
 */
public record FlaskRecipeInput(ItemStack flask, NonNullList<ItemStack> ingredients) implements RecipeInput
{
	@Override
	public ItemStack getItem(int index)
	{
		return ingredients.get(index);
	}

	@Override
	public int size()
	{
		return ingredients.size();
	}
}
