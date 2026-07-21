package wayoftime.bloodmagic.compat.jei.forge;

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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.datamap.BMDataMaps;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.common.item.SoulGemItem;
import wayoftime.bloodmagic.common.recipe.forge.ForgeRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class SoulForgeRecipeCategory implements IRecipeCategory<ForgeRecipe> {
    public static final RecipeType<ForgeRecipe> RECIPE_TYPE = RecipeType.create(BloodMagic.MODID, "soul_forge", ForgeRecipe.class);

    private static final List<DeferredHolder<Item, SoulGemItem>> GEMS = List.of(
            BMItems.SOUL_GEM_PETTY, BMItems.SOUL_GEM_LESSER, BMItems.SOUL_GEM_COMMON, BMItems.SOUL_GEM_GREATER, BMItems.SOUL_GEM_GRAND);

    @Nonnull
    private final IDrawable background;
    private final IDrawable icon;

    public SoulForgeRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(BMBlocks.HELLFIRE_FORGE));
        background = guiHelper.createDrawable(BloodMagic.rl("gui/jei/soulforge.png"), 0, 0, 100, 40);
    }

    @Override
    public RecipeType<ForgeRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public List<Component> getTooltipStrings(ForgeRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = Lists.newArrayList();
        if (mouseX >= 40 && mouseX <= 60 && mouseY >= 21 && mouseY <= 34) {
            tooltip.add(Component.translatable("jei.bloodmagic.recipe.minimumsouls", recipe.getMinWill()));
            tooltip.add(Component.translatable("jei.bloodmagic.recipe.soulsdrained", recipe.getDrain()));
        }
        return tooltip;
    }

    @Override
    public void draw(ForgeRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(45, 23, 0);
        poseStack.scale(0.5f, 0.5f, 1f);
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.bloodmagic.recipe.will"), 0, 0, 0x8b8b8b, false);
        poseStack.translate(-6, 15, 0);
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.bloodmagic.recipe.info"), 0, 0, 0x8b8b8b, false);
        poseStack.popPose();
    }

    @Nonnull
    @Override
    public Component getTitle() {
        return Component.translatable("jei.bloodmagic.recipe.soulforge");
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ForgeRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> validGems = Lists.newArrayList();
        EnumWillType requiredType = recipe.getWillType().orElse(EnumWillType.DEFAULT);
        for (DeferredHolder<Item, SoulGemItem> gemHolder : GEMS) {
            ItemStack gemStack = new ItemStack(gemHolder.get());
            Double maxWill = gemStack.getItemHolder().getData(BMDataMaps.TARTARIC_GEM_MAX_AMOUNTS);
            if (maxWill != null && maxWill >= recipe.getMinWill()) {
                ItemStack display = gemStack.copy();
                display.set(BMDataComponents.DEMON_WILL_AMOUNT, maxWill);
                display.set(BMDataComponents.DEMON_WILL_TYPE, requiredType);
                validGems.add(display);
            }
        }
        IRecipeSlotBuilder gems = builder.addSlot(RecipeIngredientRole.CATALYST, 43, 1);
        gems.addItemStacks(validGems);

        IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, 74, 14);
        output.addItemStack(recipe.getOutput());

        List<Ingredient> inputs = recipe.getCraftingIngredients();
        for (int index = 0; index < inputs.size(); index++) {
            int x = index % 2;
            int y = index / 2;
            IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, x * 18 + 1, y * 18 + 1);
            input.addIngredients(inputs.get(index));
        }
    }
}
