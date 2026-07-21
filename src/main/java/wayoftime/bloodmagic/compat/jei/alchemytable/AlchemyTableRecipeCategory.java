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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.datamap.BMDataMaps;
import wayoftime.bloodmagic.common.datamap.BloodOrb;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.common.item.BloodOrbItem;
import wayoftime.bloodmagic.common.recipe.alchemy_table.AlchemyTableRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AlchemyTableRecipeCategory implements IRecipeCategory<AlchemyTableRecipe> {
    public static final RecipeType<AlchemyTableRecipe> RECIPE_TYPE = RecipeType.create(BloodMagic.MODID, "alchemy_table", AlchemyTableRecipe.class);

    private static final List<DeferredHolder<net.minecraft.world.item.Item, BloodOrbItem>> ORBS = List.of(
            BMItems.ORB_WEAK, BMItems.ORB_APPRENTICE, BMItems.ORB_MAGICIAN, BMItems.ORB_MASTER, BMItems.ORB_ARCHMAGE, BMItems.ORB_TRANSCENDENT);

    @Nonnull
    private final IDrawable background;
    private final IDrawable icon;

    public AlchemyTableRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(BMBlocks.ALCHEMY_TABLE));
        background = guiHelper.createDrawable(BloodMagic.rl("gui/jei/alchemytable.png"), 0, 0, 118, 40);
    }

    @Override
    public List<Component> getTooltipStrings(AlchemyTableRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> tooltip = Lists.newArrayList();

        if (mouseX >= 58 && mouseX <= 78 && mouseY >= 21 && mouseY <= 34) {
            tooltip.add(Component.translatable("tooltip.bloodmagic.tier", recipe.tier()));
            tooltip.add(Component.translatable("jei.bloodmagic.recipe.lpDrained", recipe.essence()));
            tooltip.add(Component.translatable("jei.bloodmagic.recipe.ticksRequired", recipe.duration()));
        }

        return tooltip;
    }

    @Override
    public void draw(AlchemyTableRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
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
    public Component getTitle() {
        return Component.translatable("jei.bloodmagic.recipe.alchemytable");
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
    public void setRecipe(IRecipeLayoutBuilder builder, AlchemyTableRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, 92, 14);
        output.addItemStack(recipe.output());

        List<ItemStack> validOrbs = new ArrayList<>();
        for (DeferredHolder<net.minecraft.world.item.Item, BloodOrbItem> orbHolder : ORBS) {
            ItemStack stack = new ItemStack(orbHolder.get());
            BloodOrb orbData = stack.getItemHolder().getData(BMDataMaps.BLOOD_ORB_STATS);
            if (orbData != null && orbData.tier() >= recipe.tier()) {
                validOrbs.add(stack);
            }
        }
        IRecipeSlotBuilder orb = builder.addSlot(RecipeIngredientRole.CATALYST, 61, 1);
        orb.addItemStacks(validOrbs);

        List<Ingredient> inputs = recipe.inputs();
        for (int index = 0; index < inputs.size(); index++) {
            int x = index % 3;
            int y = index / 3;
            IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, x * 18 + 1, y * 18 + 1);
            input.addIngredients(inputs.get(index));
        }
    }

    @Override
    public RecipeType<AlchemyTableRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }
}
