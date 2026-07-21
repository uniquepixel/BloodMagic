package wayoftime.bloodmagic.compat.jei.alchemytable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.datacomponent.FlaskEffectEntry;
import wayoftime.bloodmagic.common.datacomponent.FlaskEffects;
import wayoftime.bloodmagic.common.datamap.BMDataMaps;
import wayoftime.bloodmagic.common.datamap.BloodOrb;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.common.item.BloodOrbItem;
import wayoftime.bloodmagic.common.item.potion.AlchemyFlaskItem;
import wayoftime.bloodmagic.common.recipe.flask.FlaskCycleRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskFillRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskLengthRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskPotencyRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskTransformRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * JEI category for the Alchemical Potion Flask system ({@link FlaskRecipe} and its seven
 * subtypes - fill/effect/length/potency/transform/cycle/item-transform). All seven share a single
 * {@link wayoftime.bloodmagic.common.recipe.BMRecipes#FLASK_TYPE} (see that field's javadoc), so -
 * exactly like 1.20.1's single {@code PotionRecipeCategory} handling every
 * {@code RecipePotionFlaskBase} subclass - one JEI category here covers all of them generically via
 * the shared {@link FlaskRecipe} base type rather than one category per subtype.
 * <p>
 * Ported from 1.20.1's {@code compat.jei.alchemytable.PotionRecipeCategory}, following this branch's
 * {@link AlchemyTableRecipeCategory} as the closest sibling (same Alchemy Table background texture
 * and layout - the Potion Flask system is also processed at the Alchemy Table). Since {@link FlaskRecipe}
 * has no 1.20.1-style {@code getExampleEffectList()} helper, the example flask contents shown in the
 * JEI preview are synthesized here per-subtype (see {@link #exampleEffects(FlaskRecipe)}) purely from
 * each subtype's existing public getters - no changes were made to any class under
 * {@code common/recipe/flask}.
 */
public class PotionRecipeCategory implements IRecipeCategory<FlaskRecipe>
{
	public static final RecipeType<FlaskRecipe> RECIPE_TYPE = RecipeType.create(BloodMagic.MODID, "potion_flask", FlaskRecipe.class);

	private static final List<DeferredHolder<Item, BloodOrbItem>> ORBS = List.of(
			BMItems.ORB_WEAK, BMItems.ORB_APPRENTICE, BMItems.ORB_MAGICIAN, BMItems.ORB_MASTER, BMItems.ORB_ARCHMAGE, BMItems.ORB_TRANSCENDENT);

	private static final List<DeferredHolder<Item, ? extends AlchemyFlaskItem>> FLASK_ITEMS = List.of(
			BMItems.ALCHEMY_FLASK, BMItems.ALCHEMY_FLASK_THROWABLE, BMItems.ALCHEMY_FLASK_LINGERING);

	@Nonnull
	private final IDrawable background;
	private final IDrawable icon;

	public PotionRecipeCategory(IGuiHelper guiHelper)
	{
		icon = guiHelper.createDrawableItemStack(new ItemStack(BMBlocks.ALCHEMY_TABLE));
		background = guiHelper.createDrawable(BloodMagic.rl("gui/jei/alchemytable.png"), 0, 0, 118, 40);
	}

	@Override
	public List<Component> getTooltipStrings(FlaskRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY)
	{
		List<Component> tooltip = Lists.newArrayList();

		if (mouseX >= 58 && mouseX <= 78 && mouseY >= 21 && mouseY <= 34)
		{
			tooltip.add(Component.translatable("tooltip.bloodmagic.tier", recipe.minimumTier()));
			tooltip.add(Component.translatable("jei.bloodmagic.recipe.lpDrained", recipe.syphon()));
			tooltip.add(Component.translatable("jei.bloodmagic.recipe.ticksRequired", recipe.ticks()));
		}

		return tooltip;
	}

	@Override
	public void draw(FlaskRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY)
	{
		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		poseStack.translate(64, 23, 0);
		poseStack.scale(0.5f, 0.5f, 1f);
		guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.bloodmagic.recipe.lp"), 0, 0, 0x8b8b8b, false);
		poseStack.translate(-8, 15, 0);
		guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.bloodmagic.recipe.info"), 0, 0, 0x8b8b8b, false);
		poseStack.popPose();
	}

	@Nonnull
	@Override
	public Component getTitle()
	{
		// NOTE: "jei.bloodmagic.recipe.potionflask" is not yet present in BMLanguageProvider - see
		// this task's final report for the requested lang entry (matches the string used by
		// 1.20.1's PotionRecipeCategory).
		return Component.translatable("jei.bloodmagic.recipe.potionflask");
	}

	@Nonnull
	@Override
	public IDrawable getBackground()
	{
		return background;
	}

	@Nullable
	@Override
	public IDrawable getIcon()
	{
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, FlaskRecipe recipe, IFocusGroup focuses)
	{
		List<ItemStack> validOrbs = new ArrayList<>();
		for (DeferredHolder<Item, BloodOrbItem> orbHolder : ORBS)
		{
			ItemStack stack = new ItemStack(orbHolder.get());
			BloodOrb orbData = stack.getItemHolder().getData(BMDataMaps.BLOOD_ORB_STATS);
			if (orbData != null && orbData.tier() >= recipe.minimumTier())
			{
				validOrbs.add(stack);
			}
		}
		IRecipeSlotBuilder orbs = builder.addSlot(RecipeIngredientRole.CATALYST, 61, 1);
		orbs.addItemStacks(validOrbs);

		FlaskEffects exampleEffects = exampleEffects(recipe);

		List<ItemStack> flaskStacks = new ArrayList<>();
		for (DeferredHolder<Item, ? extends AlchemyFlaskItem> flaskHolder : FLASK_ITEMS)
		{
			ItemStack flaskStack = new ItemStack(flaskHolder.get());
			AlchemyFlaskItem.setFlaskEffects(flaskStack, exampleEffects);
			flaskStacks.add(flaskStack);
		}
		IRecipeSlotBuilder flask = builder.addSlot(RecipeIngredientRole.INPUT, 1, 1);
		flask.addItemStacks(flaskStacks);

		List<Ingredient> inputs = recipe.inputs();
		for (int index = 0; index < inputs.size(); index++)
		{
			int x = (index + 1) % 3;
			int y = (index + 1) / 3;
			IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, x * 18 + 1, y * 18 + 1);
			input.addIngredients(inputs.get(index));
		}

		ItemStack exampleFlask = flaskStacks.get(0);
		ItemStack outputStack = recipe.getOutput(exampleFlask, exampleEffects);
		IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, 92, 14);
		output.addItemStack(outputStack);
	}

	@Override
	public RecipeType<FlaskRecipe> getRecipeType()
	{
		return RECIPE_TYPE;
	}

	/**
	 * Synthesizes a representative "before" {@link FlaskEffects} for the JEI preview, since (unlike
	 * 1.20.1's {@code RecipePotionFlaskBase#getExampleEffectList()}) {@link FlaskRecipe} exposes no
	 * such helper. Built entirely from each subtype's existing public getters:
	 * <ul>
	 * <li>{@link FlaskLengthRecipe}/{@link FlaskPotencyRecipe} need the flask to already hold their
	 * target effect, or {@code getOutput} would index a non-existent entry.</li>
	 * <li>{@link FlaskTransformRecipe} needs one entry per consumed input effect type.</li>
	 * <li>{@link FlaskCycleRecipe}/{@link FlaskFillRecipe} just need a non-trivial (2+) example list
	 * to visibly demonstrate the rotate/trim.</li>
	 * <li>Everything else (new-effect and item-transform recipes) starts from an empty flask.</li>
	 * </ul>
	 */
	private static FlaskEffects exampleEffects(FlaskRecipe recipe)
	{
		List<FlaskEffectEntry> entries = new ArrayList<>();

		if (recipe instanceof FlaskLengthRecipe lengthRecipe)
		{
			entries.add(new FlaskEffectEntry(lengthRecipe.outputEffect(), 3600, 0, 1, 1));
		}
		else if (recipe instanceof FlaskPotencyRecipe potencyRecipe)
		{
			entries.add(new FlaskEffectEntry(potencyRecipe.outputEffect(), 3600, 0, 1, 1));
		}
		else if (recipe instanceof FlaskTransformRecipe transformRecipe)
		{
			for (Holder<MobEffect> effect : transformRecipe.inputEffectList())
			{
				entries.add(new FlaskEffectEntry(effect, 3600, 0, 1, 1));
			}
		}
		else if (recipe instanceof FlaskCycleRecipe || recipe instanceof FlaskFillRecipe)
		{
			entries.add(new FlaskEffectEntry(MobEffects.MOVEMENT_SPEED, 3600, 0, 1, 1));
			entries.add(new FlaskEffectEntry(MobEffects.FIRE_RESISTANCE, 3600, 0, 1, 1));
			entries.add(new FlaskEffectEntry(MobEffects.WATER_BREATHING, 3600, 0, 1, 1));
		}

		return new FlaskEffects(entries);
	}
}
