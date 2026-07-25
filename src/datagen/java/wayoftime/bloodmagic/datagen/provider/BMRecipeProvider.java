package wayoftime.bloodmagic.datagen.provider;

import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.api.BMIdentifiers;
import wayoftime.bloodmagic.api.BMIdentifiers.Upgrades;
import wayoftime.bloodmagic.api.BMTags;
import wayoftime.bloodmagic.api.sigil.SigilEffect;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.UpgradeTome;
import wayoftime.bloodmagic.common.fluid.BMFluids;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.common.living.LivingUpgrade;
import wayoftime.bloodmagic.common.potion.BMPotions;
import wayoftime.bloodmagic.common.recipe.flask.FlaskCycleRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskEffectAmount;
import wayoftime.bloodmagic.common.recipe.flask.FlaskEffectRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskFillRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskItemTransformRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskLengthRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskPotencyRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskTransformRecipe;
import wayoftime.bloodmagic.common.recipe.ingredient.BloodOrbIngredient;
import wayoftime.bloodmagic.datagen.builder.recipe.TieredRecipeBuilder;
import wayoftime.bloodmagic.common.recipe.alchemy_table.AlchemyTableRecipe;
import wayoftime.bloodmagic.common.recipe.arc.ARCRecipe;
import wayoftime.bloodmagic.common.recipe.array.AlchemyArrayRecipe;
import wayoftime.bloodmagic.common.recipe.bloodaltar.BloodAltarRecipe;
import wayoftime.bloodmagic.common.recipe.forge.ForgeRecipe;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Recipes ported from 1.20.1 wherever the referenced items/blocks already exist on this branch.
 * The vast majority of 1.20.1 recipes reference items that haven't been re-implemented yet
 * (only a fraction of the old item roster exists here), so this is not a complete recipe set -
 * it's everything that was mechanically portable given the current item/block registry.
 */
public class BMRecipeProvider extends RecipeProvider {

    public BMRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output, HolderLookup.Provider registries) {
        // ===== Blood Altar (9 ported from 1.20.1) =====
        altar(output, "apprenticebloodorb", Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:storage_blocks/redstone"))), new ItemStack(BMItems.ORB_APPRENTICE.get()), 1, 5000, 5, 5);
        altar(output, "bucket_life", Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:bucket"))), new ItemStack(BMFluids.LIFE_ESSENCE_BUCKET.get()), 0, 1000, 5, 0);
        altar(output, "demonicslate", Ingredient.of(BMItems.SLATE_IMBUED.get()), new ItemStack(BMItems.SLATE_DEMONIC.get()), 3, 15000, 20, 20);
        altar(output, "etherealslate", Ingredient.of(BMItems.SLATE_DEMONIC.get()), new ItemStack(BMItems.SLATE_ETHEREAL.get()), 4, 30000, 40, 100);
        altar(output, "imbuedslate", Ingredient.of(BMItems.SLATE_REINFORCED.get()), new ItemStack(BMItems.SLATE_IMBUED.get()), 2, 5000, 15, 10);
        altar(output, "magicianbloodorb", Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:storage_blocks/gold"))), new ItemStack(BMItems.ORB_MAGICIAN.get()), 2, 25000, 20, 20);
        altar(output, "reinforcedslate", Ingredient.of(BMItems.SLATE_BLANK.get()), new ItemStack(BMItems.SLATE_REINFORCED.get()), 1, 2000, 5, 5);
        altar(output, "slate", Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones"))), new ItemStack(BMItems.SLATE_BLANK.get()), 0, 1000, 5, 5);
        altar(output, "weakbloodorb", Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/diamond"))), new ItemStack(BMItems.ORB_WEAK.get()), 0, 2000, 5, 1);

        // ===== Blood Altar, batch 2 (2 more ported from 1.20.1 now that hellforged_block/archmage orb/teleposer_focus exist) =====
        // archmagebloodorb: 1.20.1's ingredient was its own dungeon_metal block (named
        // "hellforgedblock" there), which collides with this branch's unrelated pre-existing
        // "hellforged_block" placeholder machine block - see BMBlocks.DUNGEON_METAL's javadoc. Fixed
        // to accept the actual ported dungeon_metal family (base + all 4 Will reskins), matching
        // 1.20.1's block tag (which accepted all 5 variants too).
        altar(output, "archmagebloodorb", Ingredient.of(
                BMBlocks.DUNGEON_METAL.get("").item().get(),
                BMBlocks.DUNGEON_METAL.get("_corrosive").item().get(),
                BMBlocks.DUNGEON_METAL.get("_destructive").item().get(),
                BMBlocks.DUNGEON_METAL.get("_steadfast").item().get(),
                BMBlocks.DUNGEON_METAL.get("_vengeful").item().get()
        ), new ItemStack(BMItems.ORB_ARCHMAGE.get()), 4, 80000, 50, 100);
        altar(output, "teleposer_focus", Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ender_pearls"))), new ItemStack(BMItems.TELEPOSER_FOCUS.get()), 3, 2000, 10, 10);
        altar(output, "enhanced_teleposer_focus", Ingredient.of(BMItems.TELEPOSER_FOCUS.get()), new ItemStack(BMItems.ENHANCED_TELEPOSER_FOCUS.get()), 3, 10000, 20, 10);

        // ===== Blood Altar, batch 3 (1 more ported from 1.20.1 now that the Dagger of Sacrifice
        // exists) - NOTE: this recipe id ("daggerofsacrifice") previously (mis)pointed at
        // BMItems.SACRIFICIAL_DAGGER as a stand-in, since only the self-sacrifice dagger existed on
        // this branch at the time it was ported. 1.20.1's actual "daggerofsacrifice" altar recipe
        // (iron_sword -> ItemDaggerOfSacrifice, tier 1, 3000/5/5) always targeted the *other* dagger -
        // the Sacrificial Dagger has no altar recipe in 1.20.1 at all (only its crafting-table recipe,
        // see "sacrificial_dagger" below). Nothing else in this codebase referenced this recipe's
        // output by identity (only its id, for the auto-generated recipe-unlock advancement, which
        // regenerates safely), so repointing it here is a correction, not a behaviour-breaking change. =====
        altar(output, "daggerofsacrifice", Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:iron_sword"))), new ItemStack(BMItems.DAGGER_OF_SACRIFICE.get()), 1, 3000, 5, 5);

        // ===== Blood Altar, batch 4 (1 more ported from 1.20.1 now that the Soul Snare exists) =====
        altar(output, "soul_snare", Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:strings"))), new ItemStack(BMItems.SOUL_SNARE.get()), 0, 500, 5, 1);

        // ===== Blood Altar, batch 5 (Master Blood Orb, ported from 1.20.1 now that
        // BMItems.WEAK_BLOOD_SHARD exists - AltarTier.FOUR.ordinal() = 3, matching the tier param
        // convention already used by demonicslate/dusk_tool above) =====
        altar(output, "masterbloodorb", Ingredient.of(BMItems.WEAK_BLOOD_SHARD.get()), new ItemStack(BMItems.ORB_MASTER.get()), 3, 40000, 30, 50);

        // ===== ARC (48 ported from 1.20.1; no further ARC recipes were portable - the remaining
        // 49 old ARC recipes all depend on an ore-processing chain (sand/fragment/gravel items),
        // a rune-reversion chain (needs the "hellforgedparts" item), or other items/tags that don't
        // exist on this branch yet) =====
        arc(output, "clay_from_sand", BMTags.Items.HYDRATION, Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:sands"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:clay_ball")))), List.of(Pair.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:clay_ball"))), 0.5)), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "clay_from_terracotta", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:terracotta"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:clay")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "mossify_cobblestone", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:cobblestone"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:mossy_cobblestone")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "mossify_cobblestone_slab", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:cobblestone_slab"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:mossy_cobblestone_slab")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "mossify_cobblestone_stairs", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:cobblestone_stairs"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:mossy_cobblestone_stairs")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "mossify_cobblestone_wall", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:cobblestone_wall"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:mossy_cobblestone_wall")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "mossify_stone_bricks", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:stone_bricks"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:mossy_stone_bricks")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "mossify_stone_brick_slab", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:stone_brick_slab"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:mossy_stone_brick_slab")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "mossify_stone_brick_stairs", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:stone_brick_stairs"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:mossy_stone_brick_stairs")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "mossify_stone_brick_wall", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:stone_brick_wall"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:mossy_stone_brick_wall")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "mud_from_dirt", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:dirt"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:mud")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "copper_block_to_exposed_copper", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:copper_block"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:exposed_copper")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "cut_copper_slab_to_exposed_cut_copper_slab", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:cut_copper_slab"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:exposed_cut_copper_slab")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "cut_copper_stairs_to_exposed_cut_copper_stairs", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:cut_copper_stairs"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:exposed_cut_copper_stairs")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "cut_copper_to_exposed_cut_copper", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:cut_copper"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:exposed_cut_copper")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "exposed_copper_to_weathered_copper", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:exposed_copper"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:weathered_copper")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "exposed_cut_copper_slab_to_weathered_cut_copper_slab", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:exposed_cut_copper_slab"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:weathered_cut_copper_slab")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "exposed_cut_copper_stairs_to_weathered_cut_copper_stairs", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:exposed_cut_copper_stairs"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:weathered_cut_copper_stairs")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "exposed_cut_copper_to_weathered_cut_copper", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:exposed_cut_copper"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:weathered_cut_copper")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "weathered_copper_to_oxidized_copper", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:weathered_copper"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:oxidized_copper")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "weathered_cut_copper_slab_to_oxidized_cut_copper_slab", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:weathered_cut_copper_slab"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:oxidized_cut_copper_slab")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "weathered_cut_copper_stairs_to_oxidized_cut_copper_stairs", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:weathered_cut_copper_stairs"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:oxidized_cut_copper_stairs")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "weathered_cut_copper_to_oxidized_cut_copper", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:weathered_cut_copper"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:oxidized_cut_copper")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "apprentice_blood_orb", BMTags.Items.REVERTER, Ingredient.of(BMItems.ORB_APPRENTICE.get()), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:redstone_block")))), List.of(), null, null);
        arc(output, "magician_blood_orb", BMTags.Items.REVERTER, Ingredient.of(BMItems.ORB_MAGICIAN.get()), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:gold_block")))), List.of(), null, null);
        arc(output, "netherite_ingot", BMTags.Items.REVERTER, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:netherite_ingot"))), List.of(Util.make(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:netherite_scrap"))), s -> s.setCount(4)), Util.make(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:gold_ingot"))), s -> s.setCount(4))), List.of(), null, null);
        arc(output, "weak_blood_orb", BMTags.Items.REVERTER, Ingredient.of(BMItems.ORB_WEAK.get()), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:diamond")))), List.of(), null, null);
        arc(output, "solidify_black_concrete", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:black_concrete_powder"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:black_concrete")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "solidify_blue_concrete", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:blue_concrete_powder"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:blue_concrete")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "solidify_brown_concrete", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:brown_concrete_powder"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:brown_concrete")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "solidify_cyan_concrete", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:cyan_concrete_powder"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:cyan_concrete")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "solidify_gray_concrete", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:gray_concrete_powder"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:gray_concrete")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "solidify_green_concrete", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:green_concrete_powder"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:green_concrete")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "solidify_light_blue_concrete", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:light_blue_concrete_powder"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:light_blue_concrete")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "solidify_light_gray_concrete", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:light_gray_concrete_powder"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:light_gray_concrete")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "solidify_lime_concrete", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:lime_concrete_powder"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:lime_concrete")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "solidify_magenta_concrete", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:magenta_concrete_powder"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:magenta_concrete")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "solidify_orange_concrete", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:orange_concrete_powder"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:orange_concrete")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "solidify_pink_concrete", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:pink_concrete_powder"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:pink_concrete")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "solidify_purple_concrete", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:purple_concrete_powder"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:purple_concrete")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "solidify_red_concrete", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:red_concrete_powder"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:red_concrete")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "solidify_white_concrete", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:white_concrete_powder"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:white_concrete")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "solidify_yellow_concrete", BMTags.Items.HYDRATION, Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:yellow_concrete_powder"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:yellow_concrete")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 200), null);
        arc(output, "wash_bed", BMTags.Items.HYDRATION, Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:beds"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:white_bed")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 333), null);
        arc(output, "wash_carpet", BMTags.Items.HYDRATION, Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:wool_carpets"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:white_carpet")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 333), null);
        arc(output, "wash_glass", BMTags.Items.HYDRATION, Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:glass_blocks"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:glass")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 333), null);
        arc(output, "wash_glass_pane", BMTags.Items.HYDRATION, Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:glass_panes"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:glass_pane")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 333), null);
        arc(output, "wash_wool", BMTags.Items.HYDRATION, Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:wool"))), List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:white_wool")))), List.of(), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("minecraft:water")), 333), null);

        // ===== ARC, batch 2 (Weak Blood Shard, ported from 1.20.1 now that BMItems.WEAK_BLOOD_SHARD
        // exists) - reverting a Strong Tau plant with a REVERTER tool yields 1 guaranteed shard plus a
        // 20% chance of a 2nd, matching 1.20.1's ARCRecipeBuilder.addRandomOutput(shard, 0.2) exactly.
        // This unblocks the Master Blood Orb altar recipe, REINFORCED_TELEPOSER_FOCUS's crafting
        // recipe, and the Greater Tartaric Gem Soul Forge recipe below, all 3 of which need it. =====
        arc(output, "weakbloodshard_tau", BMTags.Items.REVERTER, Ingredient.of(BMItems.STRONG_TAU_SEED.get()), List.of(new ItemStack(BMItems.WEAK_BLOOD_SHARD.get())), List.of(Pair.of(new ItemStack(BMItems.WEAK_BLOOD_SHARD.get()), 0.2)), null, null);

        // ===== Alchemy Table (10 ported from 1.20.1) =====
        alchemyTable(output, "bread", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/wheat"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:sugar")))), new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:bread"))), 1, 100, 100);
        alchemyTable(output, "clay_from_sand", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:sands"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:sands"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:water_bucket")))), Util.make(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:clay_ball"))), s -> s.setCount(2)), 2, 50, 100);
        alchemyTable(output, "cobweb", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:strings"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:strings"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:strings")))), new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:cobweb"))), 1, 50, 50);
        alchemyTable(output, "flint_from_gravel", List.of(Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:gravel"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:flint")))), Util.make(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:flint"))), s -> s.setCount(2)), 0, 50, 20);
        alchemyTable(output, "gold_ore_from_gilded", List.of(Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:gilded_blackstone")))), Util.make(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:gold_nugget"))), s -> s.setCount(9)), 2, 200, 100);
        alchemyTable(output, "grass_block", List.of(Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:dirt"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:bone_meal"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:wheat_seeds")))), new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:grass_block"))), 1, 200, 200);
        alchemyTable(output, "gunpowder", List.of(Ingredient.of(Items.BLAZE_POWDER), Ingredient.of(Items.QUARTZ), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:coals")))), Util.make(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:gunpowder"))), s -> s.setCount(3)), 0, 0, 100);
        alchemyTable(output, "leather_from_flesh", List.of(Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:rotten_flesh"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:rotten_flesh"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:rotten_flesh"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:rotten_flesh"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:flint"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:water_bucket")))), Util.make(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:leather"))), s -> s.setCount(4)), 1, 100, 200);
        alchemyTable(output, "nether_wart_from_block", List.of(Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:nether_wart_block")))), new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:nether_wart"))), 1, 50, 40);
        alchemyTable(output, "string", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:wool"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:flint")))), Util.make(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:string"))), s -> s.setCount(4)), 0, 100, 100);

        // ===== Soul Forge (3 ported from 1.20.1) =====
        soulForge(output, "commontartaricgem", List.of(Ingredient.of(BMItems.SOUL_GEM_LESSER.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/diamond"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:storage_blocks/gold"))), Ingredient.of(BMItems.SLATE_IMBUED.get())), new ItemStack(BMItems.SOUL_GEM_COMMON.get()), 240, 50);
        soulForge(output, "lessertartaricgem", List.of(Ingredient.of(BMItems.SOUL_GEM_PETTY.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/diamond"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:storage_blocks/redstone"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:storage_blocks/lapis")))), new ItemStack(BMItems.SOUL_GEM_LESSER.get()), 60, 20);
        soulForge(output, "pettytartaricgem", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/gold"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:glass_blocks"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/lapis")))), new ItemStack(BMItems.SOUL_GEM_PETTY.get()), 1, 1);

        // ===== Soul Forge, batch 2 (Greater Tartaric Gem, ported from 1.20.1 now that
        // BMItems.WEAK_BLOOD_SHARD exists - 1.20.1's "greatertartaricgem" was
        // (COMMON_GEM, DEMONIC_SLATE, WEAK_BLOOD_SHARD, BloodMagicTags.CRYSTAL_DEMON); the 4th slot
        // (any Will Crystal Cluster drop) is dropped here since this branch hasn't ported any Will
        // Crystal item yet (CrystalClusterBlock/DemonCrystallizerBlock exist, but nothing drops a
        // crystal item to tag - a separate content gap, not a WEAK_BLOOD_SHARD one) - ForgeRecipe's
        // ingredient list is already variable-length elsewhere in this file (e.g. "resonator" below
        // uses 3), so a 3-input recipe here is not a mechanical problem, just a narrower one) =====
        soulForge(output, "greatertartaricgem", List.of(Ingredient.of(BMItems.SOUL_GEM_COMMON.get()), Ingredient.of(BMItems.SLATE_DEMONIC.get()), Ingredient.of(BMItems.WEAK_BLOOD_SHARD.get())), new ItemStack(BMItems.SOUL_GEM_GREATER.get()), 1000, 100);

        // ===== Alchemy Table reagents (for the Alchemy Array sigil recipes below) =====
        // reagent_holding ported from 1.20.1 (tag names renamed forge: -> c:); the other three
        // referenced items that don't exist on this branch yet (teleposer block, etc.), so those
        // use simplified invented ingredient lists instead.
        alchemyTable(output, "reagent_holding", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:chests"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:leathers"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:strings"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:strings")))), new ItemStack(BMItems.REAGENT_HOLDING.get()), 2, 2000, 200);
        alchemyTable(output, "reagent_bloodlight", List.of(Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:glowstone_dust"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:blaze_powder"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone")))), new ItemStack(BMItems.REAGENT_BLOODLIGHT.get()), 1, 500, 100);
        alchemyTable(output, "reagent_suppression", List.of(Ingredient.of(BMItems.SIGIL.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/gold"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:bucket")))), new ItemStack(BMItems.REAGENT_SUPPRESSION.get()), 4, 10000, 200);
        alchemyTable(output, "reagent_teleposition", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/gold"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:ender_pearl"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:chorus_fruit")))), new ItemStack(BMItems.REAGENT_TELEPOSITION.get()), 4, 10000, 200);
        // reagent_fastminer ported from 1.20.1 (tag name renamed forge:gunpowder -> c:gunpowder)
        alchemyTable(output, "reagent_fastminer", List.of(Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:iron_pickaxe"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:iron_axe"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:iron_shovel"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gunpowders")))), new ItemStack(BMItems.REAGENT_FASTMINER.get()), 2, 2000, 200);

        // ===== Alchemy Table reagents, batch 2 (8 more ported from 1.20.1, filling in the Water/
        // Lava/Void/Growth/Frost/Magnetism/Air/Seer sigils - the sigil effects/items already worked,
        // only this crafting chain was ever missing; tag names renamed forge: -> c: same as above).
        // reagent_sight's "DIVINATION_SIGIL" ingredient uses the same any-sigil substitution as
        // reagent_suppression's "VOID_SIGIL" above (Ingredient.of(BMItems.SIGIL.get())), since this
        // branch's sigils are all one generic BMItems.SIGIL item distinguished only by a data
        // component rather than separate items - there's no per-effect Ingredient to match on. =====
        alchemyTable(output, "reagent_water", List.of(Ingredient.of(Items.SUGAR), Ingredient.of(Items.WATER_BUCKET), Ingredient.of(Items.WATER_BUCKET)), new ItemStack(BMItems.REAGENT_WATER.get()), 1, 300, 200);
        alchemyTable(output, "reagent_lava", List.of(Ingredient.of(Items.LAVA_BUCKET), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:cobblestones"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:storage_blocks/coal")))), new ItemStack(BMItems.REAGENT_LAVA.get()), 1, 1000, 200);
        alchemyTable(output, "reagent_void", List.of(Ingredient.of(Items.BUCKET), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:strings"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:strings"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gunpowders")))), new ItemStack(BMItems.REAGENT_VOID.get()), 2, 1000, 200);
        alchemyTable(output, "reagent_growth", List.of(Ingredient.of(net.minecraft.tags.ItemTags.SAPLINGS), Ingredient.of(net.minecraft.tags.ItemTags.SAPLINGS), Ingredient.of(Items.SUGAR_CANE), Ingredient.of(Items.SUGAR)), new ItemStack(BMItems.REAGENT_GROWTH.get()), 2, 2000, 200);
        alchemyTable(output, "reagent_magnetism", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:strings"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/gold"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/gold"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:storage_blocks/iron")))), new ItemStack(BMItems.REAGENT_MAGNETISM.get()), 3, 1000, 200);
        alchemyTable(output, "reagent_air", List.of(Ingredient.of(Items.GHAST_TEAR), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:feathers"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:feathers")))), new ItemStack(BMItems.REAGENT_AIR.get()), 2, 2000, 200);
        alchemyTable(output, "reagent_frost", List.of(Ingredient.of(Items.PACKED_ICE), Ingredient.of(Items.SNOW_BLOCK), Ingredient.of(Items.SNOWBALL), Ingredient.of(Items.WATER_BUCKET)), new ItemStack(BMItems.REAGENT_FROST.get()), 3, 2000, 200);
        alchemyTable(output, "reagent_sight", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/glowstone"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:glass_blocks"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:glass_blocks"))), Ingredient.of(BMItems.SIGIL.get())), new ItemStack(BMItems.REAGENT_SIGHT.get()), 1, 500, 200);

        // ===== Alchemy Array (sigils from reagents, ported from 1.20.1) =====
        // Textures ported 1:1 from 1.20.1's AlchemyArrayRecipeProvider - note "holdingsigil" reused
        // sightsigil.png there (the same texture as the Seer sigil, which doesn't exist on this
        // branch), so that reuse is preserved here rather than guessed at.
        array(output, "bloodlightsigil", arrayTexture("lightsigil.png"), Ingredient.of(BMItems.REAGENT_BLOODLIGHT.get()), Ingredient.of(BMItems.SLATE_IMBUED.get()), sigil(BMIdentifiers.Sigils.BLOODLIGHT));
        array(output, "holdingsigil", arrayTexture("sightsigil.png"), Ingredient.of(BMItems.REAGENT_HOLDING.get()), Ingredient.of(BMItems.SLATE_IMBUED.get()), new ItemStack(BMItems.SIGIL_HOLDING.get()));
        array(output, "suppressionsigil", arrayTexture("suppressionsigil.png"), Ingredient.of(BMItems.REAGENT_SUPPRESSION.get()), Ingredient.of(BMItems.SLATE_DEMONIC.get()), sigil(BMIdentifiers.Sigils.SUPPRESSION));
        array(output, "telepositionsigil", arrayTexture("teleportation.png"), Ingredient.of(BMItems.REAGENT_TELEPOSITION.get()), Ingredient.of(BMItems.SLATE_DEMONIC.get()), sigil(BMIdentifiers.Sigils.TELEPOSITION));

        // ===== Alchemy Array, batch 2 (1 more ported from 1.20.1 - only needs vanilla redstone + a blank slate) =====
        array(output, "divinationsigil", arrayTexture("divinationsigil.png"), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:redstone"))), Ingredient.of(BMItems.SLATE_BLANK.get()), sigil(BMIdentifiers.Sigils.DIVINATION));

        // ===== Alchemy Array, batch 3 (1 more ported from 1.20.1 now that reagent_fastminer exists) =====
        array(output, "fastminersigil", arrayTexture("fastminersigil.png"), Ingredient.of(BMItems.REAGENT_FASTMINER.get()), Ingredient.of(BMItems.SLATE_REINFORCED.get()), sigil(BMIdentifiers.Sigils.MINER));

        // ===== Alchemy Array, sigils batch 2 (8 more ported from 1.20.1, pairing with the "Alchemy
        // Table reagents, batch 2" reagents above - fills in the sigils that had a working effect/
        // item but no recipe at all: Water/Lava/Void/Growth/Frost/Magnetism/Air/Seer. Slate tiers and
        // textures ported 1:1 from 1.20.1's AlchemyArrayRecipeProvider, including frostsigil's
        // "stupidarray.png" placeholder texture - see that file's own TODO comment, not a mistake
        // here. Not labeled "batch 5" to avoid colliding with the unrelated Binding batch below) =====
        array(output, "watersigil", arrayTexture("watersigil.png"), Ingredient.of(BMItems.REAGENT_WATER.get()), Ingredient.of(BMItems.SLATE_BLANK.get()), sigil(BMIdentifiers.Sigils.WATER));
        array(output, "lavasigil", arrayTexture("lavasigil.png"), Ingredient.of(BMItems.REAGENT_LAVA.get()), Ingredient.of(BMItems.SLATE_BLANK.get()), sigil(BMIdentifiers.Sigils.LAVA));
        array(output, "voidsigil", arrayTexture("voidsigil.png"), Ingredient.of(BMItems.REAGENT_VOID.get()), Ingredient.of(BMItems.SLATE_REINFORCED.get()), sigil(BMIdentifiers.Sigils.VOID));
        array(output, "growthsigil", arrayTexture("growthsigil.png"), Ingredient.of(BMItems.REAGENT_GROWTH.get()), Ingredient.of(BMItems.SLATE_REINFORCED.get()), sigil(BMIdentifiers.Sigils.GROWTH));
        array(output, "frostsigil", arrayTexture("stupidarray.png"), Ingredient.of(BMItems.REAGENT_FROST.get()), Ingredient.of(BMItems.SLATE_IMBUED.get()), sigil(BMIdentifiers.Sigils.ICE));
        array(output, "magnetismsigil", arrayTexture("magnetismsigil.png"), Ingredient.of(BMItems.REAGENT_MAGNETISM.get()), Ingredient.of(BMItems.SLATE_IMBUED.get()), sigil(BMIdentifiers.Sigils.MAGNETISM));
        array(output, "airsigil", arrayTexture("airsigil.png"), Ingredient.of(BMItems.REAGENT_AIR.get()), Ingredient.of(BMItems.SLATE_REINFORCED.get()), sigil(BMIdentifiers.Sigils.AIR));
        array(output, "seersigil", arrayTexture("sightsigil.png"), Ingredient.of(BMItems.REAGENT_SIGHT.get()), Ingredient.of(BMItems.SLATE_REINFORCED.get()), sigil(BMIdentifiers.Sigils.SEER));

        // ===== Alchemy Array, batch 4 (6 special-effect arrays ported from 1.20.1 - movement/
        // updraft/spike/day/night/bounce - folded in here from a short-lived separate provider
        // class, since RecipeProvider#getName() is final and a second RecipeProvider subclass
        // collides with this one under the same "Recipes" datagen provider name. Output is a
        // placeholder (minecraft:bedrock, never handed to the player) since AlchemyArrayEffects
        // routes these ids to dedicated continuous/triggered effect classes instead of the plain
        // crafting effect that would consume the result. Textures ported 1:1 from 1.20.1's
        // AlchemyArrayRecipeProvider; AlchemyArrayRendererRegistry additionally keys these same
        // recipe ids to their bespoke multi-layer circle renderers rather than the plain one) =====
        array(output, "movement", arrayTexture("movementarray.png"), Ingredient.of(Items.FEATHER), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone"))), new ItemStack(Items.BEDROCK));
        array(output, "updraft", arrayTexture("updraftarray.png"), Ingredient.of(Items.FEATHER), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/glowstone"))), new ItemStack(Items.BEDROCK));
        array(output, "spike", arrayTexture("spikearray.png"), Ingredient.of(Items.COBBLESTONE), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron"))), new ItemStack(Items.BEDROCK));
        array(output, "day", arrayTexture("sunarray.png"), Ingredient.of(Items.COAL), Ingredient.of(Items.COAL), new ItemStack(Items.BEDROCK));
        array(output, "night", arrayTexture("moonarray.png"), Ingredient.of(Items.LAPIS_LAZULI), Ingredient.of(Items.LAPIS_LAZULI), new ItemStack(Items.BEDROCK));
        array(output, "bounce", arrayTexture("bouncearray.png"), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:slimeballs"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone"))), new ItemStack(Items.BEDROCK));

        // ===== Alchemy Array, batch 5 (the 8th special effect, Binding, now that reagent_binding
        // exists - all 5 of 1.20.1's living_* binding arrays ported, including "living_trainer"
        // (1.20.1's ItemLivingTrainer), whose 1:1 equivalent on this branch is BMItems.TRAINING_BRACELET
        // (same whitelist/blacklist Living Armour XP-gating mechanic, same translated name "Living
        // Training Bracelet"). Texture ported from 1.20.1's AlchemyArrayRegistry.BINDING_ARRAY
        // constant, which all 5 of its living_* recipes shared) =====
        alchemyTable(output, "reagent_binding", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/glowstone"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gunpowders"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:nuggets/gold")))), new ItemStack(BMItems.REAGENT_BINDING.get()), 3, 1000, 200);
        array(output, "living_helmet", arrayTexture("bindingarray.png"), Ingredient.of(BMItems.REAGENT_BINDING.get()), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:iron_helmet"))), new ItemStack(BMItems.LIVING_HELMET.get()));
        array(output, "living_plate", arrayTexture("bindingarray.png"), Ingredient.of(BMItems.REAGENT_BINDING.get()), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:iron_chestplate"))), new ItemStack(BMItems.LIVING_PLATE.get()));
        array(output, "living_leggings", arrayTexture("bindingarray.png"), Ingredient.of(BMItems.REAGENT_BINDING.get()), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:iron_leggings"))), new ItemStack(BMItems.LIVING_LEGGINGS.get()));
        array(output, "living_boots", arrayTexture("bindingarray.png"), Ingredient.of(BMItems.REAGENT_BINDING.get()), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:iron_boots"))), new ItemStack(BMItems.LIVING_BOOTS.get()));
        array(output, "living_trainer", arrayTexture("bindingarray.png"), Ingredient.of(BMItems.REAGENT_BINDING.get()), Ingredient.of(Items.DIAMOND), new ItemStack(BMItems.TRAINING_BRACELET.get()));

        // ===== Anointments (13 ported from 1.20.1; the original's "slate_vial" ingredient doesn't
        // exist on this branch, substituted with the closest existing tier - Imbued Slate) =====
        alchemyTable(output, "anointment_melee_damage", List.of(Ingredient.of(BMItems.SLATE_IMBUED.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:blaze_powder"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/quartz")))), new ItemStack(BMItems.ANOINTMENT_MELEE_DAMAGE.get()), 1, 500, 100);
        alchemyTable(output, "anointment_looting", List.of(Ingredient.of(BMItems.SLATE_IMBUED.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/lapis"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:bones")))), new ItemStack(BMItems.ANOINTMENT_LOOTING.get()), 1, 500, 100);
        alchemyTable(output, "anointment_bow_power", List.of(Ingredient.of(BMItems.SLATE_IMBUED.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:bow")))), new ItemStack(BMItems.ANOINTMENT_BOW_POWER.get()), 1, 500, 100);
        alchemyTable(output, "anointment_bow_velocity", List.of(Ingredient.of(BMItems.SLATE_IMBUED.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:nuggets/gold"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:bow")))), new ItemStack(BMItems.ANOINTMENT_BOW_VELOCITY.get()), 1, 500, 100);
        alchemyTable(output, "anointment_hidden_knowledge", List.of(Ingredient.of(BMItems.SLATE_IMBUED.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:glass_bottle"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:enchanted_book")))), new ItemStack(BMItems.ANOINTMENT_HIDDEN_KNOWLEDGE.get()), 1, 500, 100);
        alchemyTable(output, "anointment_holy_water", List.of(Ingredient.of(BMItems.SLATE_IMBUED.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:glistering_melon_slice"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/quartz")))), new ItemStack(BMItems.ANOINTMENT_HOLY_WATER.get()), 1, 500, 100);
        alchemyTable(output, "anointment_quick_draw", List.of(Ingredient.of(BMItems.SLATE_IMBUED.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:strings"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:spectral_arrow")))), new ItemStack(BMItems.ANOINTMENT_QUICK_DRAW.get()), 1, 500, 100);
        alchemyTable(output, "anointment_silk_touch", List.of(Ingredient.of(BMItems.SLATE_IMBUED.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:cobweb"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:nuggets/gold")))), new ItemStack(BMItems.ANOINTMENT_SILK_TOUCH.get()), 1, 500, 100);
        alchemyTable(output, "anointment_fortune", List.of(Ingredient.of(BMItems.SLATE_IMBUED.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone"))), Ingredient.of(Items.COAL)), new ItemStack(BMItems.ANOINTMENT_FORTUNE.get()), 1, 500, 100);
        alchemyTable(output, "anointment_smelting", List.of(Ingredient.of(BMItems.SLATE_IMBUED.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:furnace"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:coal")))), new ItemStack(BMItems.ANOINTMENT_SMELTING.get()), 1, 500, 100);
        alchemyTable(output, "anointment_voiding", List.of(Ingredient.of(BMItems.SLATE_IMBUED.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:netherrack"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:cobbled_deepslate")))), new ItemStack(BMItems.ANOINTMENT_VOIDING.get()), 1, 500, 100);
        alchemyTable(output, "anointment_weapon_repair", List.of(Ingredient.of(BMItems.SLATE_IMBUED.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/copper"))), Ingredient.of(Items.GOLD_NUGGET)), new ItemStack(BMItems.ANOINTMENT_WEAPON_REPAIR.get()), 1, 500, 100);
        alchemyTable(output, "anointment_will_power", List.of(Ingredient.of(BMItems.SLATE_IMBUED.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BMItems.RAW_WILL.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:nuggets/gold")))), new ItemStack(BMItems.ANOINTMENT_WILL_POWER.get()), 1, 500, 100);

        // ===== Anointment L/XL tiers (ported from 1.20.1's "_L"/"_XL" container sizes - see BMItems
        // for the use-count reasoning). 1.20.1's own L/XL recipes upgraded the base-tier anointment
        // using "Tau Oil" and, for XL, "Hellforged Sand" + an Amethyst Shard - none of which exist on
        // this branch - so each recipe here instead upgrades the previous tier using the next Slate
        // in this mod's existing altar progression (Imbued -> Demonic -> Ethereal) alongside the same
        // family-specific reagents already used at tier 1, keeping the "consume the lower tier" shape
        // of the original recipes without requiring new items.) =====
        alchemyTable(output, "anointment_melee_damage_l", List.of(Ingredient.of(BMItems.ANOINTMENT_MELEE_DAMAGE.get()), Ingredient.of(BMItems.SLATE_DEMONIC.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:blaze_powder"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/quartz")))), new ItemStack(BMItems.ANOINTMENT_MELEE_DAMAGE_L.get()), 3, 1000, 100);
        alchemyTable(output, "anointment_melee_damage_xl", List.of(Ingredient.of(BMItems.ANOINTMENT_MELEE_DAMAGE_L.get()), Ingredient.of(BMItems.SLATE_ETHEREAL.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:blaze_powder"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/quartz")))), new ItemStack(BMItems.ANOINTMENT_MELEE_DAMAGE_XL.get()), 4, 2000, 100);
        alchemyTable(output, "anointment_looting_l", List.of(Ingredient.of(BMItems.ANOINTMENT_LOOTING.get()), Ingredient.of(BMItems.SLATE_DEMONIC.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/lapis"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:bones")))), new ItemStack(BMItems.ANOINTMENT_LOOTING_L.get()), 3, 1000, 100);
        alchemyTable(output, "anointment_looting_xl", List.of(Ingredient.of(BMItems.ANOINTMENT_LOOTING_L.get()), Ingredient.of(BMItems.SLATE_ETHEREAL.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/lapis"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:bones")))), new ItemStack(BMItems.ANOINTMENT_LOOTING_XL.get()), 4, 2000, 100);
        alchemyTable(output, "anointment_bow_power_l", List.of(Ingredient.of(BMItems.ANOINTMENT_BOW_POWER.get()), Ingredient.of(BMItems.SLATE_DEMONIC.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:bow")))), new ItemStack(BMItems.ANOINTMENT_BOW_POWER_L.get()), 3, 1000, 100);
        alchemyTable(output, "anointment_bow_power_xl", List.of(Ingredient.of(BMItems.ANOINTMENT_BOW_POWER_L.get()), Ingredient.of(BMItems.SLATE_ETHEREAL.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:bow")))), new ItemStack(BMItems.ANOINTMENT_BOW_POWER_XL.get()), 4, 2000, 100);
        alchemyTable(output, "anointment_bow_velocity_l", List.of(Ingredient.of(BMItems.ANOINTMENT_BOW_VELOCITY.get()), Ingredient.of(BMItems.SLATE_DEMONIC.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:nuggets/gold"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:bow")))), new ItemStack(BMItems.ANOINTMENT_BOW_VELOCITY_L.get()), 3, 1000, 100);
        alchemyTable(output, "anointment_bow_velocity_xl", List.of(Ingredient.of(BMItems.ANOINTMENT_BOW_VELOCITY_L.get()), Ingredient.of(BMItems.SLATE_ETHEREAL.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:nuggets/gold"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:bow")))), new ItemStack(BMItems.ANOINTMENT_BOW_VELOCITY_XL.get()), 4, 2000, 100);
        alchemyTable(output, "anointment_hidden_knowledge_l", List.of(Ingredient.of(BMItems.ANOINTMENT_HIDDEN_KNOWLEDGE.get()), Ingredient.of(BMItems.SLATE_DEMONIC.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:glass_bottle"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:enchanted_book")))), new ItemStack(BMItems.ANOINTMENT_HIDDEN_KNOWLEDGE_L.get()), 3, 1000, 100);
        alchemyTable(output, "anointment_hidden_knowledge_xl", List.of(Ingredient.of(BMItems.ANOINTMENT_HIDDEN_KNOWLEDGE_L.get()), Ingredient.of(BMItems.SLATE_ETHEREAL.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:glass_bottle"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:enchanted_book")))), new ItemStack(BMItems.ANOINTMENT_HIDDEN_KNOWLEDGE_XL.get()), 4, 2000, 100);
        alchemyTable(output, "anointment_holy_water_l", List.of(Ingredient.of(BMItems.ANOINTMENT_HOLY_WATER.get()), Ingredient.of(BMItems.SLATE_DEMONIC.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:glistering_melon_slice"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/quartz")))), new ItemStack(BMItems.ANOINTMENT_HOLY_WATER_L.get()), 3, 1000, 100);
        alchemyTable(output, "anointment_holy_water_xl", List.of(Ingredient.of(BMItems.ANOINTMENT_HOLY_WATER_L.get()), Ingredient.of(BMItems.SLATE_ETHEREAL.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:glistering_melon_slice"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/quartz")))), new ItemStack(BMItems.ANOINTMENT_HOLY_WATER_XL.get()), 4, 2000, 100);
        alchemyTable(output, "anointment_quick_draw_l", List.of(Ingredient.of(BMItems.ANOINTMENT_QUICK_DRAW.get()), Ingredient.of(BMItems.SLATE_DEMONIC.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:strings"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:spectral_arrow")))), new ItemStack(BMItems.ANOINTMENT_QUICK_DRAW_L.get()), 3, 1000, 100);
        alchemyTable(output, "anointment_quick_draw_xl", List.of(Ingredient.of(BMItems.ANOINTMENT_QUICK_DRAW_L.get()), Ingredient.of(BMItems.SLATE_ETHEREAL.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:strings"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:spectral_arrow")))), new ItemStack(BMItems.ANOINTMENT_QUICK_DRAW_XL.get()), 4, 2000, 100);
        alchemyTable(output, "anointment_silk_touch_l", List.of(Ingredient.of(BMItems.ANOINTMENT_SILK_TOUCH.get()), Ingredient.of(BMItems.SLATE_DEMONIC.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:cobweb"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:nuggets/gold")))), new ItemStack(BMItems.ANOINTMENT_SILK_TOUCH_L.get()), 3, 1000, 100);
        alchemyTable(output, "anointment_silk_touch_xl", List.of(Ingredient.of(BMItems.ANOINTMENT_SILK_TOUCH_L.get()), Ingredient.of(BMItems.SLATE_ETHEREAL.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:cobweb"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:nuggets/gold")))), new ItemStack(BMItems.ANOINTMENT_SILK_TOUCH_XL.get()), 4, 2000, 100);
        alchemyTable(output, "anointment_fortune_l", List.of(Ingredient.of(BMItems.ANOINTMENT_FORTUNE.get()), Ingredient.of(BMItems.SLATE_DEMONIC.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone"))), Ingredient.of(Items.COAL)), new ItemStack(BMItems.ANOINTMENT_FORTUNE_L.get()), 3, 1000, 100);
        alchemyTable(output, "anointment_fortune_xl", List.of(Ingredient.of(BMItems.ANOINTMENT_FORTUNE_L.get()), Ingredient.of(BMItems.SLATE_ETHEREAL.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone"))), Ingredient.of(Items.COAL)), new ItemStack(BMItems.ANOINTMENT_FORTUNE_XL.get()), 4, 2000, 100);
        alchemyTable(output, "anointment_smelting_l", List.of(Ingredient.of(BMItems.ANOINTMENT_SMELTING.get()), Ingredient.of(BMItems.SLATE_DEMONIC.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:furnace"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:coal")))), new ItemStack(BMItems.ANOINTMENT_SMELTING_L.get()), 3, 1000, 100);
        alchemyTable(output, "anointment_smelting_xl", List.of(Ingredient.of(BMItems.ANOINTMENT_SMELTING_L.get()), Ingredient.of(BMItems.SLATE_ETHEREAL.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:furnace"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:coal")))), new ItemStack(BMItems.ANOINTMENT_SMELTING_XL.get()), 4, 2000, 100);
        alchemyTable(output, "anointment_voiding_l", List.of(Ingredient.of(BMItems.ANOINTMENT_VOIDING.get()), Ingredient.of(BMItems.SLATE_DEMONIC.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:netherrack"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:cobbled_deepslate")))), new ItemStack(BMItems.ANOINTMENT_VOIDING_L.get()), 3, 1000, 100);
        alchemyTable(output, "anointment_voiding_xl", List.of(Ingredient.of(BMItems.ANOINTMENT_VOIDING_L.get()), Ingredient.of(BMItems.SLATE_ETHEREAL.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:netherrack"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:cobbled_deepslate")))), new ItemStack(BMItems.ANOINTMENT_VOIDING_XL.get()), 4, 2000, 100);
        alchemyTable(output, "anointment_weapon_repair_l", List.of(Ingredient.of(BMItems.ANOINTMENT_WEAPON_REPAIR.get()), Ingredient.of(BMItems.SLATE_DEMONIC.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/copper"))), Ingredient.of(Items.GOLD_NUGGET)), new ItemStack(BMItems.ANOINTMENT_WEAPON_REPAIR_L.get()), 3, 1000, 100);
        alchemyTable(output, "anointment_weapon_repair_xl", List.of(Ingredient.of(BMItems.ANOINTMENT_WEAPON_REPAIR_L.get()), Ingredient.of(BMItems.SLATE_ETHEREAL.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/copper"))), Ingredient.of(Items.GOLD_NUGGET)), new ItemStack(BMItems.ANOINTMENT_WEAPON_REPAIR_XL.get()), 4, 2000, 100);

        // ===== Throwing Dagger (ported from 1.20.1, soul forge, x16 output) =====
        soulForge(output, "throwing_dagger", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:strings")))), Util.make(new ItemStack(BMItems.THROWING_DAGGER.get()), s -> s.setCount(16)), 32, 5);
        // Syringe variant (ported from 1.20.1, soul forge, x8 output)
        soulForge(output, "throwing_dagger_syringe", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:glass_blocks")))), Util.make(new ItemStack(BMItems.THROWING_DAGGER_SYRINGE.get()), s -> s.setCount(8)), 10, 2);

        // ===== Sentient tools (5 ported 1:1 from 1.20.1 - all originally free, drain 0/minWill 0,
        // just a Petty Tartaric Gem + the matching vanilla iron tool. The Sentient Bow recipe is
        // new since 1.20.1 never shipped a Java item for the bow (only leftover texture/model
        // assets existed) - see SentientBowItem - so this extends the same free-craft pattern to
        // it using a vanilla bow as the base) =====
        soulForge(output, "sentientsword", List.of(Ingredient.of(BMItems.SOUL_GEM_PETTY.get()), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:iron_sword")))), new ItemStack(BMItems.SENTIENT_SWORD.get()), 0, 0);
        soulForge(output, "sentientaxe", List.of(Ingredient.of(BMItems.SOUL_GEM_PETTY.get()), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:iron_axe")))), new ItemStack(BMItems.SENTIENT_AXE.get()), 0, 0);
        soulForge(output, "sentientpickaxe", List.of(Ingredient.of(BMItems.SOUL_GEM_PETTY.get()), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:iron_pickaxe")))), new ItemStack(BMItems.SENTIENT_PICKAXE.get()), 0, 0);
        soulForge(output, "sentientshovel", List.of(Ingredient.of(BMItems.SOUL_GEM_PETTY.get()), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:iron_shovel")))), new ItemStack(BMItems.SENTIENT_SHOVEL.get()), 0, 0);
        soulForge(output, "sentientscythe", List.of(Ingredient.of(BMItems.SOUL_GEM_PETTY.get()), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:iron_hoe")))), new ItemStack(BMItems.SENTIENT_SCYTHE.get()), 0, 0);
        soulForge(output, "sentientbow", List.of(Ingredient.of(BMItems.SOUL_GEM_PETTY.get()), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:bow")))), new ItemStack(BMItems.SENTIENT_BOW.get()), 0, 0);

        // ===== Sentient Armour (new - 1.20.1 never shipped a Java implementation to port a recipe
        // from at all; see SentientArmorItem's class javadoc. 1.12's only recipe for this feature
        // was the Gem itself, a Tartaric Forge recipe consuming a Diamond Chestplate + a Soul Gem +
        // an Iron Block + Obsidian - mirrored below via soulForge. The four pieces themselves are
        // new recipes in the same spirit, one tier up from the free Petty-Gem tool recipes above
        // since armour is the stronger, later-game item: a Lesser Soul Gem plus the matching Diamond
        // armor piece each) =====
        soulForge(output, "sentienthelmet", List.of(Ingredient.of(BMItems.SOUL_GEM_LESSER.get()), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:diamond_helmet")))), new ItemStack(BMItems.SENTIENT_HELMET.get()), 200, 80);
        soulForge(output, "sentientplate", List.of(Ingredient.of(BMItems.SOUL_GEM_LESSER.get()), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:diamond_chestplate")))), new ItemStack(BMItems.SENTIENT_PLATE.get()), 200, 80);
        soulForge(output, "sentientleggings", List.of(Ingredient.of(BMItems.SOUL_GEM_LESSER.get()), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:diamond_leggings")))), new ItemStack(BMItems.SENTIENT_LEGGINGS.get()), 200, 80);
        soulForge(output, "sentientboots", List.of(Ingredient.of(BMItems.SOUL_GEM_LESSER.get()), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:diamond_boots")))), new ItemStack(BMItems.SENTIENT_BOOTS.get()), 200, 80);
        soulForge(output, "sentient_armour_gem", List.of(Ingredient.of(BMItems.SOUL_GEM_LESSER.get()), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:diamond_chestplate"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:storage_blocks/iron"))), Ingredient.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:obsidian")))), new ItemStack(BMItems.SENTIENT_ARMOUR_GEM.get()), 300, 150);

        // Living Station: like Sentient Armour above, this is new-to-this-branch content with no
        // 1.20.1 upstream to port a recipe from (see LIVING_STATION's own registration comment in
        // BMBlocks - "no 1.20.1 equivalent to port art from"). It's the GUI front-end for applying/
        // scrapping Living Armour UPGRADE_TOME items (see LivingStationTile), so it's a real,
        // player-facing furniture block with no plausible reason to be left unreachable. Costed as a
        // "furniture/station" tier crafting-table recipe - a Lesser Soul Gem ties it to the same
        // Will-magic tier as the Sentient Armour pieces it manages, Bookshelves represent the
        // "recorded training knowledge" the station tracks, and Imbued Slate is this file's
        // established general-purpose "magic activation" ingredient for utility blocks.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.LIVING_STATION.item().get())
                .define('i', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron")))
                .define('b', Items.BOOKSHELF)
                .define('g', BMItems.SOUL_GEM_LESSER.get())
                .define('s', BMItems.SLATE_IMBUED.get())
                .pattern("ibi")
                .pattern("bgb")
                .pattern("isi")
                .unlockedBy("has_lesser_gem", has(BMItems.SOUL_GEM_LESSER.get()))
                .save(output, BloodMagic.rl("living_station"));

        // ===== Item Routing network (ported from 1.20.1's 4-tier soulforge chain, collapsed to
        // 3 recipes since this branch has no intermediate "routing node" block - each recipe below
        // merges the original's base-node + upgrade-tier material lists) =====
        soulForge(output, "master_routing_node", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:storage_blocks/iron"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/diamond"))), Ingredient.of(BMItems.SLATE_IMBUED.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/glowstone")))), new ItemStack(BMBlocks.MASTER_ROUTING_NODE.item().get()), 400, 200);
        soulForge(output, "input_routing_node", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/gold"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/glowstone")))), new ItemStack(BMBlocks.INPUT_ROUTING_NODE.item().get()), 400, 25);
        soulForge(output, "output_routing_node", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/glowstone")))), new ItemStack(BMBlocks.OUTPUT_ROUTING_NODE.item().get()), 400, 25);

        // Item Router: the "standalone alternative to the full Routing Node network" (see
        // ItemRouterTile's javadoc) - an enhanced hopper with a built-in whitelist plus an optional
        // Filter item slot (see the Item Routing "Filter" system below). Also new-to-this-branch (no
        // 1.20.1 registration for this block exists at all - only its unrelated ITEM_ROUTER_FILTER
        // item did), and left with no recipe despite the 3 routing nodes it complements all getting
        // one above. Kept as a plain crafting-table recipe (unlike the Hellfire-Forge-gated routing
        // nodes) since it's explicitly the simpler, more accessible alternative to that network - a
        // vanilla Hopper upgraded with iron reinforcement and an Imbued Slate for the smart filtering.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.ITEM_ROUTER.item().get())
                .define('i', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron")))
                .define('h', Items.HOPPER)
                .define('s', BMItems.SLATE_IMBUED.get())
                .pattern("i i")
                .pattern("ihi")
                .pattern("isi")
                .unlockedBy("has_imbued_slate", has(BMItems.SLATE_IMBUED.get()))
                .save(output, BloodMagic.rl("item_router"));

        // ===== Ritual Diviner (crafting-table shaped, ported from 1.20.1's shape/tiering - the
        // original's dedicated "scribe tool" items don't exist on this branch, substituted with
        // vanilla materials of a similar tier; the Dawn tier has no 1.20.1 recipe to port from, so
        // this extends the same tiering pattern the Dusk recipe already establishes) =====
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BMItems.RITUAL_DIVINER.get())
                .define('d', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/diamond")))
                .define('s', Items.BLAZE_ROD)
                .define('i', BMItems.SLATE_IMBUED.get())
                .pattern("ddd")
                .pattern("sis")
                .pattern("ddd")
                .unlockedBy("has_imbued_slate", has(BMItems.SLATE_IMBUED.get()))
                .save(output, BloodMagic.rl("ritual_diviner"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BMItems.RITUAL_DIVINER_DUSK.get())
                .define('S', BMItems.SLATE_DEMONIC.get())
                .define('d', BMItems.RITUAL_DIVINER.get())
                .define('t', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:nuggets/gold")))
                .pattern(" S ")
                .pattern("tdt")
                .pattern(" S ")
                .unlockedBy("has_diviner", has(BMItems.RITUAL_DIVINER.get()))
                .save(output, BloodMagic.rl("ritual_diviner_dusk"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BMItems.RITUAL_DIVINER_DAWN.get())
                .define('S', BMItems.SLATE_ETHEREAL.get())
                .define('d', BMItems.RITUAL_DIVINER_DUSK.get())
                .define('t', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:nuggets/gold")))
                .pattern(" S ")
                .pattern("tdt")
                .pattern(" S ")
                .unlockedBy("has_dusk_diviner", has(BMItems.RITUAL_DIVINER_DUSK.get()))
                .save(output, BloodMagic.rl("ritual_diviner_dawn"));

        // ===== Core structure blocks (6 ported from 1.20.1's crafting-table recipes, now that
        // BLOOD_ALTAR/ALCHEMY_TABLE/HELLFIRE_FORGE/TELEPOSER exist; the original's Blood Altar/
        // Alchemy Table/Hellfire Forge recipe ids are suffixed with "_block" here to avoid a
        // filesystem clash with the "blood_altar/", "alchemy_table/" and "hellfire_forge/" recipe
        // subfolders the altar()/alchemyTable()/soulForge() helpers already write into) =====
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.BLOOD_ALTAR.item().get())
                .define('a', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones")))
                .define('b', Items.FURNACE)
                .define('c', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/gold")))
                .pattern("a a")
                .pattern("aba")
                .pattern("ccc")
                .unlockedBy("has_stone", has(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones"))))
                .save(output, BloodMagic.rl("blood_altar_block"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.ALCHEMY_TABLE.item().get())
                .define('b', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron")))
                .define('g', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/gold")))
                .define('o', BMItems.SLATE_BLANK.get())
                .define('s', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones")))
                .define('w', TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:planks")))
                .pattern("sss")
                .pattern("wbw")
                .pattern("gog")
                .unlockedBy("has_blank_slate", has(BMItems.SLATE_BLANK.get()))
                .save(output, BloodMagic.rl("alchemy_table_block"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.HELLFIRE_FORGE.item().get())
                .define('S', BMItems.SLATE_BLANK.get())
                .define('i', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron")))
                .define('o', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:storage_blocks/iron")))
                .define('s', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones")))
                .pattern("i i")
                .pattern("sSs")
                .pattern("sos")
                .unlockedBy("has_blank_slate", has(BMItems.SLATE_BLANK.get()))
                .save(output, BloodMagic.rl("hellfire_forge_block"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.TELEPOSER.item().get())
                .define('e', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ender_pearls")))
                .define('g', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/gold")))
                .define('t', BMItems.TELEPOSER_FOCUS.get())
                .pattern("ggg")
                .pattern("ete")
                .pattern("ggg")
                .unlockedBy("has_teleposer_focus", has(BMItems.TELEPOSER_FOCUS.get()))
                .save(output, BloodMagic.rl("teleposer"));

        // Reinforced Teleposer Focus, ported from 1.20.1 now that BMItems.WEAK_BLOOD_SHARD exists -
        // 1.20.1's id for this recipe was (confusingly) "enhanced_teleposer_focus" despite producing
        // the *reinforced* focus, kept as-is here for fidelity (it doesn't collide with the
        // "blood_altar/enhanced_teleposer_focus" altar recipe above - different recipe subfolder).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BMItems.REINFORCED_TELEPOSER_FOCUS.get())
                .requires(BMItems.ENHANCED_TELEPOSER_FOCUS.get())
                .requires(BMItems.WEAK_BLOOD_SHARD.get())
                .unlockedBy("has_shard", has(BMItems.WEAK_BLOOD_SHARD.get()))
                .save(output, BloodMagic.rl("enhanced_teleposer_focus"));

        // ===== Hellforged/Demonite ore chain (ported from 1.20.1 now that BMItems.DEMONITE_RAW/
        // HELLFORGED_INGOT and BMBlocks.RAW_HELLFORGED_BLOCK exist): dungeon_ore drops Raw Demonite
        // (see MineBlock), which smelts/blasts into a Hellforged Ingot, which assembles 9-at-a-time
        // into BMBlocks.DUNGEON_METAL - matching 1.20.1's GeneratorRecipes exactly, including the
        // 200/100-tick smelt/blast times shared by every other ore in this mod. The final two
        // (dungeon_metal <-> ingot) use direct item references rather than 1.20.1's
        // STORAGE_BLOCKS_HELLFORGED tag, for the same reason "archmagebloodorb" above does: that tag
        // (see BMBlockTagProvider) is currently populated with this branch's unrelated pre-existing
        // HELLFORGED_BLOCK placeholder machine block, not DUNGEON_METAL - fixing the tag mapping
        // itself is out of scope here (BMBlockTagProvider isn't owned by this pass). =====
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(BMItems.DEMONITE_RAW.get()), RecipeCategory.MISC, BMItems.HELLFORGED_INGOT.get(), 0, 200)
                .unlockedBy("has_raw_demonite", has(BMItems.DEMONITE_RAW.get()))
                .save(output, BloodMagic.rl("smelting/ingot_from_raw_hellforged"));
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(BMItems.DEMONITE_RAW.get()), RecipeCategory.MISC, BMItems.HELLFORGED_INGOT.get(), 0, 100)
                .unlockedBy("has_raw_demonite", has(BMItems.DEMONITE_RAW.get()))
                .save(output, BloodMagic.rl("smelting/blasting_ingot_from_raw_hellforged"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RAW_HELLFORGED_BLOCK.item().get())
                .define('s', BMItems.DEMONITE_RAW.get())
                .pattern("sss")
                .pattern("sss")
                .pattern("sss")
                .unlockedBy("has_raw_hellforged", has(BMItems.DEMONITE_RAW.get()))
                .save(output, BloodMagic.rl("raw_hellforged_block"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BMItems.DEMONITE_RAW.get(), 9)
                .requires(BMBlocks.RAW_HELLFORGED_BLOCK.item().get())
                .unlockedBy("has_raw_hellforged_block", has(BMBlocks.RAW_HELLFORGED_BLOCK.item().get()))
                .save(output, BloodMagic.rl("raw_hellforged_block_to_item"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.DUNGEON_METAL.get("").item().get())
                .define('s', BMItems.HELLFORGED_INGOT.get())
                .pattern("sss")
                .pattern("sss")
                .pattern("sss")
                .unlockedBy("has_hellforged", has(BMItems.HELLFORGED_INGOT.get()))
                .save(output, BloodMagic.rl("hellforged_block"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BMItems.HELLFORGED_INGOT.get(), 9)
                .requires(BMBlocks.DUNGEON_METAL.get("").item().get())
                .unlockedBy("has_hellforged_block", has(BMBlocks.DUNGEON_METAL.get("").item().get()))
                .save(output, BloodMagic.rl("hellforged_block_to_ingot"));

        // ===== Blood Tank (new to this branch - BLOOD_TANK/BloodTankTile/BloodTankRenderer all
        // exist and work, but never had a recipe of any kind, in either the base craft or the
        // tier-up path). BloodTankTile.CAPACITIES runs tier 1 (16 buckets) up to tier 16 (524288
        // buckets), and BMBlocks.BLOOD_TANK's BlockItem already bakes CONTAINER_TIER=1 into its
        // default item properties, so a plain crafting-table recipe is all tier 1 needs - the
        // FluidTieredRecipe/TieredRecipeBuilder machinery (see BaseTieredRecipe#matches, which reads
        // both input stacks' CONTAINER_TIER component and requires them equal and below the tier-16
        // cap) already exists for every tier beyond that, it just never had a recipe registered
        // either. One tier-up recipe below covers all 15 upgrades (1->2 through 15->16) generically,
        // since matches()/assemble() key off the placed tanks' own tier component rather than a
        // fixed item, not one recipe per tier.
        //
        // Base tier: styled after the other core structure blocks just above (iron frame + glass
        // basin), gated behind an Apprentice-tier orb - a small step past the orb-less core
        // structures (Altar/Table/Forge/Teleposer), matching this file's other "early player-quality-
        // of-life storage/utility" items (see e.g. reinforcedslate's altar tier). =====
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.BLOOD_TANK.item().get())
                .define('i', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron")))
                .define('g', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:glass_blocks")))
                .define('o', bloodOrb(1))
                .pattern("igi")
                .pattern("g g")
                .pattern("ioi")
                .unlockedBy("has_apprentice_orb", has(BMItems.ORB_APPRENTICE.get()))
                .save(output, BloodMagic.rl("blood_tank"));

        // Tier-up: 2 Blood Tanks of the same tier (their fluid contents merge into the result, or
        // carry over from whichever side isn't empty - see FluidTieredRecipe#assemble) plus an iron
        // reinforcement frame and a glass connector, into 1 tank of the next tier up. A full 3x3
        // pattern is used deliberately (rather than a smaller pattern NeoForge would otherwise slide
        // around the grid) so the "primary"/"secondary" slot indices BaseTieredRecipe reads from are
        // unambiguous - a shape smaller than the grid could match at more than one offset, and
        // primary/secondary are fixed absolute slot indices, not pattern-relative ones.
        TieredRecipeBuilder.fluid(RecipeCategory.MISC, BMBlocks.BLOOD_TANK.item().get())
                .primary(1)
                .secondary(7)
                .define('i', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron")))
                .define('g', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:glass_blocks")))
                .define('p', BMBlocks.BLOOD_TANK.item().get())
                .define('s', BMBlocks.BLOOD_TANK.item().get())
                .pattern("ipi")
                .pattern(" g ")
                .pattern("isi")
                .unlockedBy("has_blood_tank", has(BMBlocks.BLOOD_TANK.item().get()))
                .save(output, BloodMagic.rl("blood_tank_tier_up"));

        // Sacrificial Dagger: 1.20.1 only ever gave this a crafting-table recipe (no Blood Altar
        // recipe - see the "daggerofsacrifice" altar recipe's comment above for why that name
        // actually belongs to the unrelated Dagger of Sacrifice).
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BMItems.SACRIFICIAL_DAGGER.get())
                .define('G', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/gold")))
                .define('g', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:glass_blocks")))
                .define('i', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron")))
                .pattern("ggg")
                .pattern(" Gg")
                .pattern("i g")
                .unlockedBy("has_gold_ingot", has(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/gold"))))
                .save(output, BloodMagic.rl("sacrificial_dagger"));

        // Synthetic Point: the original's "f" key also accepted bloodmagic:strong_tau as an
        // alternate ingredient; that item doesn't exist on this branch, so it's omitted from the
        // ingredient list below (the other 11 vanilla alternatives are unaffected).
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMItems.SYNTHETIC_POINT.get(), 2)
                .define('f', Ingredient.of(Items.ROTTEN_FLESH, Items.PORKCHOP, Items.MUTTON, Items.BEEF, Items.TROPICAL_FISH, Items.COD, Items.PUFFERFISH, Items.SALMON, Items.CHICKEN, Items.SPIDER_EYE, Items.RABBIT))
                .define('i', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:nuggets/iron")))
                .define('r', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone")))
                .pattern("ifi")
                .pattern("frf")
                .pattern("ifi")
                .unlockedBy("has_redstone_dust", has(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone"))))
                .save(output, BloodMagic.rl("synthetic_point"));

        // ===== Blood Runes (2 ported from 1.20.1; the other 17 old blood_rune_* recipes use a
        // custom "bloodmagic:bloodorb" (minimum orb tier) ingredient type that hasn't been ported
        // to this branch, or require the "hellforgedparts" item (all T2 rune upgrades) - neither
        // is portable without inventing a substitute, so those were skipped) =====
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_CAPACITY.item().get())
                .define('a', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones")))
                .define('b', Items.BUCKET)
                .define('c', BMBlocks.RUNE_BLANK.item().get())
                .define('d', BMItems.SLATE_IMBUED.get())
                .pattern("aba")
                .pattern("bcb")
                .pattern("ada")
                .unlockedBy("has_blank_rune", has(BMBlocks.RUNE_BLANK.item().get()))
                .save(output, BloodMagic.rl("blood_rune_capacity"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_DISLOCATION.item().get())
                .define('a', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones")))
                .define('b', Items.WATER_BUCKET)
                .define('c', BMBlocks.RUNE_BLANK.item().get())
                .define('d', BMItems.SLATE_IMBUED.get())
                .pattern("aba")
                .pattern("bcb")
                .pattern("ada")
                .unlockedBy("has_blank_rune", has(BMBlocks.RUNE_BLANK.item().get()))
                .save(output, BloodMagic.rl("blood_rune_displacement"));

        // ===== Blood Runes/ritual stones/misc, batch 2 (12 more ported from 1.20.1 now that the
        // custom "bloodmagic:bloodorb" (minimum orb tier) Ingredient exists - see BloodOrbIngredient.
        // Still skipped: ritual_reader (superseded by the Ritual Diviner recipes above) and the 4
        // path/* recipes - none of those result/component items exist on this branch yet.
        // lava_crystal/primitive_furnace_cell/primitive_hydration_cell are no longer skipped now that
        // their items exist - see the ARC tool items section near the end of this method) =====
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_BLANK.item().get())
                .define('a', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones")))
                .define('o', bloodOrb(0))
                .define('s', BMItems.SLATE_BLANK.get())
                .pattern("asa")
                .pattern("aoa")
                .pattern("aaa")
                .unlockedBy("has_blank_slate", has(BMItems.SLATE_BLANK.get()))
                .save(output, BloodMagic.rl("blood_rune_blank"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_SPEED.item().get())
                .define('a', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones")))
                .define('b', BMItems.SLATE_BLANK.get())
                .define('c', Items.SUGAR)
                .define('d', BMBlocks.RUNE_BLANK.item().get())
                .pattern("aba")
                .pattern("cdc")
                .pattern("aba")
                .unlockedBy("has_blank_rune", has(BMBlocks.RUNE_BLANK.item().get()))
                .save(output, BloodMagic.rl("blood_rune_speed"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_ACCELERATION.item().get())
                .define('a', Items.BUCKET)
                .define('b', BMItems.SLATE_DEMONIC.get())
                .define('c', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/gold")))
                .define('d', BMBlocks.RUNE_SPEED.item().get())
                .define('e', bloodOrb(3))
                .pattern("aba")
                .pattern("cdc")
                .pattern("aea")
                .unlockedBy("has_speed_rune", has(BMBlocks.RUNE_SPEED.item().get()))
                .save(output, BloodMagic.rl("blood_rune_acceleration"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_CAPACITY_AUGMENTED.item().get())
                .define('a', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:obsidians")))
                .define('b', BMItems.SLATE_DEMONIC.get())
                .define('c', Items.BUCKET)
                .define('d', BMBlocks.RUNE_CAPACITY.item().get())
                .define('e', bloodOrb(3))
                .pattern("aba")
                .pattern("cdc")
                .pattern("aea")
                .unlockedBy("has_capacity_rune", has(BMBlocks.RUNE_CAPACITY.item().get()))
                .save(output, BloodMagic.rl("blood_rune_aug_capacity"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_CHARGING.item().get())
                .define('G', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/glowstone")))
                .define('R', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone")))
                .define('e', bloodOrb(3))
                .define('r', BMBlocks.RUNE_BLANK.item().get())
                .define('s', BMItems.SLATE_DEMONIC.get())
                .pattern("RsR")
                .pattern("GrG")
                .pattern("ReR")
                .unlockedBy("has_blank_rune", has(BMBlocks.RUNE_BLANK.item().get()))
                .save(output, BloodMagic.rl("blood_rune_charging"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_ORB.item().get())
                .define('a', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones")))
                .define('b', bloodOrb(0))
                .define('c', BMBlocks.RUNE_BLANK.item().get())
                .define('d', bloodOrb(3))
                .pattern("aba")
                .pattern("cdc")
                .pattern("aba")
                .unlockedBy("has_blank_rune", has(BMBlocks.RUNE_BLANK.item().get()))
                .save(output, BloodMagic.rl("blood_rune_orb"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_SACRIFICE.item().get())
                .define('a', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones")))
                .define('b', BMItems.SLATE_REINFORCED.get())
                .define('c', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/gold")))
                .define('d', BMBlocks.RUNE_BLANK.item().get())
                .define('e', bloodOrb(1))
                .pattern("aba")
                .pattern("cdc")
                .pattern("aea")
                .unlockedBy("has_blank_rune", has(BMBlocks.RUNE_BLANK.item().get()))
                .save(output, BloodMagic.rl("blood_rune_sacrifice"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_SELF_SACRIFICE.item().get())
                .define('a', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones")))
                .define('b', BMItems.SLATE_REINFORCED.get())
                .define('c', Items.GLOWSTONE_DUST)
                .define('d', BMBlocks.RUNE_BLANK.item().get())
                .define('e', bloodOrb(1))
                .pattern("aba")
                .pattern("cdc")
                .pattern("aea")
                .unlockedBy("has_blank_rune", has(BMBlocks.RUNE_BLANK.item().get()))
                .save(output, BloodMagic.rl("blood_rune_self_sacrifice"));

        // Efficiency Rune: unlike every other rune above, this one has no 1.20.1 art to port from -
        // BloodRuneType.EFFICIENCY existed there (see BloodAltar#efficiencyMultiplier) but 1.20.1
        // never actually registered a block/item for it, so it was dead enum code, not real content
        // (confirmed: no BLANK_RUNE-style registration for it anywhere in 1.20.1's
        // BloodMagicBlocks.java). RUNE_EFFICIENCY/RUNE_2_EFFICIENCY are new-to-this-branch content
        // (see BloodRuneData) that was otherwise fully wired up - data map entry, lang, blockstate -
        // and just missing recipes. Styled like the other direct-from-RUNE_BLANK runes above (same
        // stone-corners/RUNE_BLANK-center/Imbued-Slate-base shape as blood_rune_capacity/
        // blood_rune_displacement), with Amethyst Shard as the "precision/reduced-waste" flavor
        // ingredient (unused by any other rune recipe, fitting for a rune about cutting drain waste).
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_EFFICIENCY.item().get())
                .define('a', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones")))
                .define('b', Items.AMETHYST_SHARD)
                .define('c', BMBlocks.RUNE_BLANK.item().get())
                .define('d', BMItems.SLATE_IMBUED.get())
                .pattern("aba")
                .pattern("bcb")
                .pattern("ada")
                .unlockedBy("has_blank_rune", has(BMBlocks.RUNE_BLANK.item().get()))
                .save(output, BloodMagic.rl("blood_rune_efficiency"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RITUAL_STONE_BLANK.item().get(), 4)
                .define('a', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:obsidians")))
                .define('b', BMItems.SLATE_REINFORCED.get())
                .define('c', bloodOrb(1))
                .pattern("aba")
                .pattern("bcb")
                .pattern("aba")
                .unlockedBy("has_reinforced_slate", has(BMItems.SLATE_REINFORCED.get()))
                .save(output, BloodMagic.rl("ritual_stone_blank"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.MASTER_RITUAL_STONE.item().get())
                .define('a', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:obsidians")))
                .define('b', BMBlocks.RITUAL_STONE_BLANK.item().get())
                .define('c', bloodOrb(2))
                .pattern("aba")
                .pattern("bcb")
                .pattern("aba")
                .unlockedBy("has_ritual_stone", has(BMBlocks.RITUAL_STONE_BLANK.item().get()))
                .save(output, BloodMagic.rl("ritual_stone_master"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.ARC_BLOCK.item().get())
                .define('I', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:storage_blocks/iron")))
                .define('S', BMItems.SLATE_IMBUED.get())
                .define('f', Items.FURNACE)
                .define('o', bloodOrb(2))
                .define('s', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones")))
                .pattern("sss")
                .pattern("SoS")
                .pattern("IfI")
                .unlockedBy("has_imbued_slate", has(BMItems.SLATE_IMBUED.get()))
                .save(output, BloodMagic.rl("arc_block"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMItems.EXPERIENCE_BOOK.get())
                .define('b', Items.ENCHANTED_BOOK)
                .define('e', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:storage_blocks/lapis")))
                .define('g', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/gold")))
                .define('l', BMItems.SLATE_IMBUED.get())
                .define('o', bloodOrb(2))
                .define('s', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:strings")))
                .pattern("ses")
                .pattern("lbl")
                .pattern("gog")
                .unlockedBy("has_imbued_slate", has(BMItems.SLATE_IMBUED.get()))
                .save(output, BloodMagic.rl("experience_tome"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.INCENSE_ALTAR.item().get())
                .define('c', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:cobblestones")))
                .define('h', Items.CHARCOAL)
                .define('o', bloodOrb(0))
                .define('s', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones")))
                .pattern("s s")
                .pattern("shs")
                .pattern("coc")
                .unlockedBy("has_stone", has(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones"))))
                .save(output, BloodMagic.rl("incense_altar"));

        // ===== Incense Altar road/path blocks (ported from 1.20.1's GeneratorRecipes "path_*"
        // family - see BMBlocks.WOOD_BRICK_PATH etc/IncenseAltarPathBlock/IncenseAltarTile). Orb
        // tiers mirror upstream exactly: 1.20.1 numbered its orbs 1-5 (weak/apprentice/magician/
        // master/archmage), this branch numbers them 0-5 (weak=0..archmage=4, plus a new tier-5
        // transcendent not present in 1.20.1) - so upstream's orb_tier 2/3/4/5 requirements map to
        // bloodOrb(1)/bloodOrb(2)/bloodOrb(3)/bloodOrb(4) here (apprentice/magician/master/archmage
        // respectively, the same orbs by name). Ids/ingredient counts/results match 1.20.1's
        // recipes/path/path_*.json exactly (also referenced by the guidebook's incense_altar entry). =====
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BMBlocks.WOOD_BRICK_PATH.item().get(), 4)
                .requires(Ingredient.of(net.minecraft.tags.ItemTags.PLANKS), 4)
                .requires(bloodOrb(1))
                .unlockedBy("has_planks", has(net.minecraft.tags.ItemTags.PLANKS))
                .save(output, BloodMagic.rl("path/path_wood"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BMBlocks.WOOD_TILE_PATH.item().get(), 4)
                .requires(BMBlocks.WOOD_BRICK_PATH.item().get(), 4)
                .unlockedBy("has_wood_path", has(BMBlocks.WOOD_BRICK_PATH.item().get()))
                .save(output, BloodMagic.rl("path/path_woodtile"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BMBlocks.STONE_BRICK_PATH.item().get(), 4)
                .requires(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones"))), 4)
                .requires(bloodOrb(2))
                .unlockedBy("has_stone", has(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones"))))
                .save(output, BloodMagic.rl("path/path_stone"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BMBlocks.STONE_TILE_PATH.item().get(), 4)
                .requires(BMBlocks.STONE_BRICK_PATH.item().get(), 4)
                .unlockedBy("has_stone_path", has(BMBlocks.STONE_BRICK_PATH.item().get()))
                .save(output, BloodMagic.rl("path/path_stonetile"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BMBlocks.WORN_STONE_BRICK_PATH.item().get(), 4)
                .requires(BMBlocks.STONE_BRICK_PATH.item().get(), 4)
                .requires(bloodOrb(3))
                .unlockedBy("has_stone_path", has(BMBlocks.STONE_BRICK_PATH.item().get()))
                .save(output, BloodMagic.rl("path/path_wornstone"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BMBlocks.WORN_STONE_TILE_PATH.item().get(), 4)
                .requires(BMBlocks.WORN_STONE_BRICK_PATH.item().get(), 4)
                .unlockedBy("has_worn_stone_path", has(BMBlocks.WORN_STONE_BRICK_PATH.item().get()))
                .save(output, BloodMagic.rl("path/path_wornstonetile"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BMBlocks.OBSIDIAN_BRICK_PATH.item().get(), 4)
                .requires(Items.OBSIDIAN, 4)
                .requires(bloodOrb(4))
                .unlockedBy("has_obsidian", has(Items.OBSIDIAN))
                .save(output, BloodMagic.rl("path/path_obsidian"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BMBlocks.OBSIDIAN_TILE_PATH.item().get(), 4)
                .requires(BMBlocks.OBSIDIAN_BRICK_PATH.item().get(), 4)
                .unlockedBy("has_obsidian_path", has(BMBlocks.OBSIDIAN_BRICK_PATH.item().get()))
                .save(output, BloodMagic.rl("path/path_obsidiantile"));

        // ===== Ore fragments (7 ARC ore-processing recipes ported from 1.20.1, now that
        // ironfragment/goldfragment/copperfragment/fragment_netherite_scrap exist). The 8th old
        // recipe, "fragmentshellforged" (raw-hellforged-material -> demonitefragment), is skipped:
        // its input tag was populated by 1.20.1's "rawdemoniteblock"/"DEMONITE_RAW" raw-ore item,
        // which doesn't exist on this branch (BMBlocks.HELLFORGED_BLOCK is the *compressed*
        // ingot-block equivalent, not the raw-material one, so it isn't a faithful substitute) - so
        // demonitefragment is registered but has no way to be crafted yet =====
        arc(output, "fragmentsiron", BMTags.Items.EXPLOSIVES, Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:raw_materials/iron"))), List.of(Util.make(new ItemStack(BMItems.IRON_FRAGMENT.get()), s -> s.setCount(2))), List.of(Pair.of(new ItemStack(BMItems.IRON_FRAGMENT.get()), 0.25)), null, null);
        arc(output, "fragmentsgold", BMTags.Items.EXPLOSIVES, Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:raw_materials/gold"))), List.of(Util.make(new ItemStack(BMItems.GOLD_FRAGMENT.get()), s -> s.setCount(2))), List.of(Pair.of(new ItemStack(BMItems.GOLD_FRAGMENT.get()), 0.25)), null, null);
        arc(output, "fragmentscopper", BMTags.Items.EXPLOSIVES, Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:raw_materials/copper"))), List.of(Util.make(new ItemStack(BMItems.COPPER_FRAGMENT.get()), s -> s.setCount(2))), List.of(Pair.of(new ItemStack(BMItems.COPPER_FRAGMENT.get()), 0.25)), null, null);
        arc(output, "fragmentsfrom_ore_iron", BMTags.Items.EXPLOSIVES, Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ores/iron"))), List.of(Util.make(new ItemStack(BMItems.IRON_FRAGMENT.get()), s -> s.setCount(4))), List.of(Pair.of(new ItemStack(BMItems.IRON_FRAGMENT.get()), 0.5)), null, null);
        arc(output, "fragmentsfrom_ore_gold", BMTags.Items.EXPLOSIVES, Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ores/gold"))), List.of(Util.make(new ItemStack(BMItems.GOLD_FRAGMENT.get()), s -> s.setCount(4))), List.of(Pair.of(new ItemStack(BMItems.GOLD_FRAGMENT.get()), 0.5)), null, null);
        arc(output, "fragmentsfrom_ore_copper", BMTags.Items.EXPLOSIVES, Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ores/copper"))), List.of(Util.make(new ItemStack(BMItems.COPPER_FRAGMENT.get()), s -> s.setCount(4))), List.of(Pair.of(new ItemStack(BMItems.COPPER_FRAGMENT.get()), 0.5)), null, null);
        arc(output, "fragmentsnetherite_scrap", BMTags.Items.EXPLOSIVES, Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ores/netherite_scrap"))), List.of(Util.make(new ItemStack(BMItems.NETHERITE_SCRAP_FRAGMENT.get()), s -> s.setCount(3))), List.of(), null, null);

        // ===== Ore gravel (5 ARC ore-processing recipes ported from 1.20.1, now that
        // irongravel/goldgravel/coppergravel/demonitegravel/gravel_netherite_scrap exist) - the next
        // tier after fragment: fragment -> gravel via the "resonator" ARC tool tag, with a chance of
        // also dropping Tiny Corrupted Dust. 1.20.1's input was a "fragments/x" forge tag, but no
        // such tag exists on this branch (each fragment tier only ever has the one BM item backing
        // it), so these use the concrete fragment item directly instead. "gravelshellforged"
        // (demonitefragment -> demonitegravel) is ported too even though demonitefragment itself
        // has no crafting source yet (see the "fragmentshellforged" note above) - same situation as
        // demonitefragment, mirrored one tier up so the chain is ready once that gap is filled =====
        arc(output, "gravelsiron", BMTags.Items.RESONATOR, Ingredient.of(BMItems.IRON_FRAGMENT.get()), List.of(new ItemStack(BMItems.IRON_GRAVEL.get())), List.of(Pair.of(new ItemStack(BMItems.CORRUPTED_DUST_TINY.get()), 0.5)), null, null);
        arc(output, "gravelsgold", BMTags.Items.RESONATOR, Ingredient.of(BMItems.GOLD_FRAGMENT.get()), List.of(new ItemStack(BMItems.GOLD_GRAVEL.get())), List.of(Pair.of(new ItemStack(BMItems.CORRUPTED_DUST_TINY.get()), 0.5)), null, null);
        arc(output, "gravelscopper", BMTags.Items.RESONATOR, Ingredient.of(BMItems.COPPER_FRAGMENT.get()), List.of(new ItemStack(BMItems.COPPER_GRAVEL.get())), List.of(Pair.of(new ItemStack(BMItems.CORRUPTED_DUST_TINY.get()), 0.25)), null, null);
        arc(output, "gravelshellforged", BMTags.Items.RESONATOR, Ingredient.of(BMItems.DEMONITE_FRAGMENT.get()), List.of(new ItemStack(BMItems.DEMONITE_GRAVEL.get())), List.of(), null, null);
        arc(output, "gravelsnetherite_scrap", BMTags.Items.RESONATOR, Ingredient.of(BMItems.NETHERITE_SCRAP_FRAGMENT.get()), List.of(new ItemStack(BMItems.NETHERITE_SCRAP_GRAVEL.get())), List.of(Pair.of(new ItemStack(BMItems.CORRUPTED_DUST_TINY.get()), 0.5), Pair.of(new ItemStack(BMItems.CORRUPTED_DUST_TINY.get()), 0.5)), null, null);

        // ===== Corrupted Dust (1 crafting-table recipe + 4 Alchemy Table recipes ported from
        // 1.20.1, now that corrupted_dust/corrupted_tinydust exist). Corrupted Dust compresses from
        // 9x Tiny Corrupted Dust (itself the ARC gravel byproduct above), then feeds back into the
        // Alchemy Table to reprocess a fragment into gravel at a better yield than the plain ARC
        // route. The 5th old "corrupted_*" Alchemy Table recipe, "corrupted_coal" (coal dust +
        // corrupted_dust -> "coalsand"), is skipped: "coalsand" doesn't exist on this branch (no
        // ARC/Alchemy Table sand-processing chain has been ported yet) =====
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMItems.CORRUPTED_DUST.get())
                .define('s', BMItems.CORRUPTED_DUST_TINY.get())
                .pattern("sss")
                .pattern("sss")
                .pattern("sss")
                .unlockedBy("has_corrupted_tinydust", has(BMItems.CORRUPTED_DUST_TINY.get()))
                .save(output, BloodMagic.rl("corrupted_dust"));
        alchemyTable(output, "corrupted_iron", List.of(Ingredient.of(BMItems.IRON_FRAGMENT.get()), Ingredient.of(BMItems.CORRUPTED_DUST.get())), Util.make(new ItemStack(BMItems.IRON_GRAVEL.get()), s -> s.setCount(2)), 3, 100, 50);
        alchemyTable(output, "corrupted_gold", List.of(Ingredient.of(BMItems.GOLD_FRAGMENT.get()), Ingredient.of(BMItems.CORRUPTED_DUST.get())), Util.make(new ItemStack(BMItems.GOLD_GRAVEL.get()), s -> s.setCount(2)), 3, 300, 50);
        alchemyTable(output, "corrupted_copper", List.of(Ingredient.of(BMItems.COPPER_FRAGMENT.get()), Ingredient.of(BMItems.CORRUPTED_DUST.get())), Util.make(new ItemStack(BMItems.COPPER_GRAVEL.get()), s -> s.setCount(2)), 3, 50, 50);
        alchemyTable(output, "corrupted_netherite", List.of(Ingredient.of(BMItems.NETHERITE_SCRAP_FRAGMENT.get()), Ingredient.of(BMItems.CORRUPTED_DUST.get()), Ingredient.of(BMItems.CORRUPTED_DUST.get()), Ingredient.of(BMItems.CORRUPTED_DUST.get())), Util.make(new ItemStack(BMItems.NETHERITE_SCRAP_GRAVEL.get()), s -> s.setCount(2)), 3, 1000, 50);

        // ===== Hellforged Parts, part 1: ARC rune reversion (9 ported from 1.20.1, now that
        // hellforgedparts exists) - reverts a tier-2 rune back into its tier-1 form, dropping
        // Hellforged Parts and 4 Netherite Scrap in the process =====
        arc(output, "reversion/self_sac", BMTags.Items.REVERTER, Ingredient.of(BMBlocks.RUNE_2_SELF_SACRIFICE.item().get()), List.of(new ItemStack(BMBlocks.RUNE_SELF_SACRIFICE.item().get()), new ItemStack(BMItems.HELLFORGED_PARTS.get()), Util.make(new ItemStack(Items.NETHERITE_SCRAP), s -> s.setCount(4))), List.of(), null, null);
        arc(output, "reversion/acceleration", BMTags.Items.REVERTER, Ingredient.of(BMBlocks.RUNE_2_ACCELERATION.item().get()), List.of(new ItemStack(BMBlocks.RUNE_ACCELERATION.item().get()), new ItemStack(BMItems.HELLFORGED_PARTS.get()), Util.make(new ItemStack(Items.NETHERITE_SCRAP), s -> s.setCount(4))), List.of(), null, null);
        arc(output, "reversion/aug_capacity", BMTags.Items.REVERTER, Ingredient.of(BMBlocks.RUNE_2_CAPACITY_AUGMENTED.item().get()), List.of(new ItemStack(BMBlocks.RUNE_CAPACITY_AUGMENTED.item().get()), new ItemStack(BMItems.HELLFORGED_PARTS.get()), Util.make(new ItemStack(Items.NETHERITE_SCRAP), s -> s.setCount(4))), List.of(), null, null);
        arc(output, "reversion/capacity", BMTags.Items.REVERTER, Ingredient.of(BMBlocks.RUNE_2_CAPACITY.item().get()), List.of(new ItemStack(BMBlocks.RUNE_CAPACITY.item().get()), new ItemStack(BMItems.HELLFORGED_PARTS.get()), Util.make(new ItemStack(Items.NETHERITE_SCRAP), s -> s.setCount(4))), List.of(), null, null);
        arc(output, "reversion/charging", BMTags.Items.REVERTER, Ingredient.of(BMBlocks.RUNE_2_CHARGING.item().get()), List.of(new ItemStack(BMBlocks.RUNE_CHARGING.item().get()), new ItemStack(BMItems.HELLFORGED_PARTS.get()), Util.make(new ItemStack(Items.NETHERITE_SCRAP), s -> s.setCount(4))), List.of(), null, null);
        arc(output, "reversion/displacement", BMTags.Items.REVERTER, Ingredient.of(BMBlocks.RUNE_2_DISLOCATION.item().get()), List.of(new ItemStack(BMBlocks.RUNE_DISLOCATION.item().get()), new ItemStack(BMItems.HELLFORGED_PARTS.get()), Util.make(new ItemStack(Items.NETHERITE_SCRAP), s -> s.setCount(4))), List.of(), null, null);
        arc(output, "reversion/orb_rune", BMTags.Items.REVERTER, Ingredient.of(BMBlocks.RUNE_2_ORB.item().get()), List.of(new ItemStack(BMBlocks.RUNE_ORB.item().get()), new ItemStack(BMItems.HELLFORGED_PARTS.get()), Util.make(new ItemStack(Items.NETHERITE_SCRAP), s -> s.setCount(4))), List.of(), null, null);
        arc(output, "reversion/sac", BMTags.Items.REVERTER, Ingredient.of(BMBlocks.RUNE_2_SACRIFICE.item().get()), List.of(new ItemStack(BMBlocks.RUNE_SACRIFICE.item().get()), new ItemStack(BMItems.HELLFORGED_PARTS.get()), Util.make(new ItemStack(Items.NETHERITE_SCRAP), s -> s.setCount(4))), List.of(), null, null);
        arc(output, "reversion/speed", BMTags.Items.REVERTER, Ingredient.of(BMBlocks.RUNE_2_SPEED.item().get()), List.of(new ItemStack(BMBlocks.RUNE_SPEED.item().get()), new ItemStack(BMItems.HELLFORGED_PARTS.get()), Util.make(new ItemStack(Items.NETHERITE_SCRAP), s -> s.setCount(4))), List.of(), null, null);
        // Efficiency's reversion recipe - see the "blood_rune_efficiency"/"blood_rune_efficiency_2"
        // recipes below for why this rune (unlike its 9 siblings above) has no 1.20.1 precedent to
        // mirror; added here purely to keep it at parity with the other 9 tier-2 runes now that it
        // has a tier-2 form to revert.
        arc(output, "reversion/efficiency", BMTags.Items.REVERTER, Ingredient.of(BMBlocks.RUNE_2_EFFICIENCY.item().get()), List.of(new ItemStack(BMBlocks.RUNE_EFFICIENCY.item().get()), new ItemStack(BMItems.HELLFORGED_PARTS.get()), Util.make(new ItemStack(Items.NETHERITE_SCRAP), s -> s.setCount(4))), List.of(), null, null);

        // ===== Hellforged Parts, part 2: crafting-table tier-2 rune upgrades (9 ported from
        // 1.20.1) - the reverse of the reversion recipes above: consumes a tier-1 rune, Hellforged
        // Parts, 4 Netherite Scrap, 2 Bloodstone ("largebloodstonebrick" in 1.20.1 - renamed
        // "bloodstone" on this branch, see BMBlocks.BLOODSTONE) and 2 Ethereal Slate to produce the
        // tier-2 rune =====
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_2_ACCELERATION.item().get())
                .define('s', Items.NETHERITE_SCRAP)
                .define('p', BMItems.HELLFORGED_PARTS.get())
                .define('d', BMBlocks.BLOODSTONE.item().get())
                .define('r', BMBlocks.RUNE_ACCELERATION.item().get())
                .define('o', BMItems.SLATE_ETHEREAL.get())
                .pattern("sps")
                .pattern("drd")
                .pattern("sos")
                .unlockedBy("has_hellforged_parts", has(BMItems.HELLFORGED_PARTS.get()))
                .save(output, BloodMagic.rl("blood_rune_acceleration_2"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_2_CAPACITY_AUGMENTED.item().get())
                .define('s', Items.NETHERITE_SCRAP)
                .define('p', BMItems.HELLFORGED_PARTS.get())
                .define('d', BMBlocks.BLOODSTONE.item().get())
                .define('r', BMBlocks.RUNE_CAPACITY_AUGMENTED.item().get())
                .define('o', BMItems.SLATE_ETHEREAL.get())
                .pattern("sps")
                .pattern("drd")
                .pattern("sos")
                .unlockedBy("has_hellforged_parts", has(BMItems.HELLFORGED_PARTS.get()))
                .save(output, BloodMagic.rl("blood_rune_aug_capacity_2"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_2_CAPACITY.item().get())
                .define('s', Items.NETHERITE_SCRAP)
                .define('p', BMItems.HELLFORGED_PARTS.get())
                .define('d', BMBlocks.BLOODSTONE.item().get())
                .define('r', BMBlocks.RUNE_CAPACITY.item().get())
                .define('o', BMItems.SLATE_ETHEREAL.get())
                .pattern("sps")
                .pattern("drd")
                .pattern("sos")
                .unlockedBy("has_hellforged_parts", has(BMItems.HELLFORGED_PARTS.get()))
                .save(output, BloodMagic.rl("blood_rune_capacity_2"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_2_CHARGING.item().get())
                .define('s', Items.NETHERITE_SCRAP)
                .define('p', BMItems.HELLFORGED_PARTS.get())
                .define('d', BMBlocks.BLOODSTONE.item().get())
                .define('r', BMBlocks.RUNE_CHARGING.item().get())
                .define('o', BMItems.SLATE_ETHEREAL.get())
                .pattern("sps")
                .pattern("drd")
                .pattern("sos")
                .unlockedBy("has_hellforged_parts", has(BMItems.HELLFORGED_PARTS.get()))
                .save(output, BloodMagic.rl("blood_rune_charging_2"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_2_DISLOCATION.item().get())
                .define('s', Items.NETHERITE_SCRAP)
                .define('p', BMItems.HELLFORGED_PARTS.get())
                .define('d', BMBlocks.BLOODSTONE.item().get())
                .define('r', BMBlocks.RUNE_DISLOCATION.item().get())
                .define('o', BMItems.SLATE_ETHEREAL.get())
                .pattern("sps")
                .pattern("drd")
                .pattern("sos")
                .unlockedBy("has_hellforged_parts", has(BMItems.HELLFORGED_PARTS.get()))
                .save(output, BloodMagic.rl("blood_rune_displacement_2"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_2_ORB.item().get())
                .define('s', Items.NETHERITE_SCRAP)
                .define('p', BMItems.HELLFORGED_PARTS.get())
                .define('d', BMBlocks.BLOODSTONE.item().get())
                .define('r', BMBlocks.RUNE_ORB.item().get())
                .define('o', BMItems.SLATE_ETHEREAL.get())
                .pattern("sps")
                .pattern("drd")
                .pattern("sos")
                .unlockedBy("has_hellforged_parts", has(BMItems.HELLFORGED_PARTS.get()))
                .save(output, BloodMagic.rl("blood_rune_orb_2"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_2_SACRIFICE.item().get())
                .define('s', Items.NETHERITE_SCRAP)
                .define('p', BMItems.HELLFORGED_PARTS.get())
                .define('d', BMBlocks.BLOODSTONE.item().get())
                .define('r', BMBlocks.RUNE_SACRIFICE.item().get())
                .define('o', BMItems.SLATE_ETHEREAL.get())
                .pattern("sps")
                .pattern("drd")
                .pattern("sos")
                .unlockedBy("has_hellforged_parts", has(BMItems.HELLFORGED_PARTS.get()))
                .save(output, BloodMagic.rl("blood_rune_sac_2"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_2_SELF_SACRIFICE.item().get())
                .define('s', Items.NETHERITE_SCRAP)
                .define('p', BMItems.HELLFORGED_PARTS.get())
                .define('d', BMBlocks.BLOODSTONE.item().get())
                .define('r', BMBlocks.RUNE_SELF_SACRIFICE.item().get())
                .define('o', BMItems.SLATE_ETHEREAL.get())
                .pattern("sps")
                .pattern("drd")
                .pattern("sos")
                .unlockedBy("has_hellforged_parts", has(BMItems.HELLFORGED_PARTS.get()))
                .save(output, BloodMagic.rl("blood_rune_self_sac_2"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_2_SPEED.item().get())
                .define('s', Items.NETHERITE_SCRAP)
                .define('p', BMItems.HELLFORGED_PARTS.get())
                .define('d', BMBlocks.BLOODSTONE.item().get())
                .define('r', BMBlocks.RUNE_SPEED.item().get())
                .define('o', BMItems.SLATE_ETHEREAL.get())
                .pattern("sps")
                .pattern("drd")
                .pattern("sos")
                .unlockedBy("has_hellforged_parts", has(BMItems.HELLFORGED_PARTS.get()))
                .save(output, BloodMagic.rl("blood_rune_speed_2"));

        // Efficiency's tier-2 form - same shape/cost as the 9 tier-2 runes above, for the same
        // reason (see "blood_rune_efficiency" above): no 1.20.1 recipe exists to port, since 1.20.1
        // never shipped a real Efficiency Rune item at all.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.RUNE_2_EFFICIENCY.item().get())
                .define('s', Items.NETHERITE_SCRAP)
                .define('p', BMItems.HELLFORGED_PARTS.get())
                .define('d', BMBlocks.BLOODSTONE.item().get())
                .define('r', BMBlocks.RUNE_EFFICIENCY.item().get())
                .define('o', BMItems.SLATE_ETHEREAL.get())
                .pattern("sps")
                .pattern("drd")
                .pattern("sos")
                .unlockedBy("has_hellforged_parts", has(BMItems.HELLFORGED_PARTS.get()))
                .save(output, BloodMagic.rl("blood_rune_efficiency_2"));

        // ===== Flask catalysts (4 of 1.20.1's 9 Alchemy Table catalyst recipes ported - these feed
        // the entire Alchemical Potion Flask system below, which was restored in an earlier round but
        // left every one of its catalyst *sources* uncovered: SIMPLE_CATALYST/WEAK_FILLING_AGENT/
        // CYCLING_CATALYST/COMBINATIONAL_CATALYST were consumed by dozens of flask recipes below with
        // no recipe of their own anywhere, making the whole system unreachable from a fresh save
        // despite every individual flask recipe otherwise being valid. The other 5 old catalyst
        // recipes (mundane_power/mundane_lengthening/average_power/average_lengthening/
        // standard_filling) aren't ported here - they need either 1.20.1's harvested "Tau" crop
        // product (distinct from this branch's WEAK_TAU_SEED/STRONG_TAU_SEED, which only place the
        // crop) or the same missing Hellforged dust/STRENGTHENED_CATALYST chain called out at
        // "archmagebloodorb"/BMBlocks.HELLFORGED_BLOCK above - AVERAGE_FILLING_AGENT never had a
        // crafting recipe even in 1.20.1 (loot-table only there too). "combinational" below swaps
        // 1.20.1's BloodMagicTags.DUST_COAL ingredient for plain Items.COAL, the same substitution
        // used for every other now-fixed c:dusts/coal reference in this file. =====
        alchemyTable(output, "simple_catalyst", List.of(Ingredient.of(Items.SUGAR), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/glowstone"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gunpowders"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:crops/nether_wart")))), Util.make(new ItemStack(BMItems.SIMPLE_CATALYST.get()), s -> s.setCount(2)), 2, 200, 100);
        alchemyTable(output, "weak_filling", List.of(Ingredient.of(BMItems.SIMPLE_CATALYST.get()), Ingredient.of(Items.SUGAR_CANE), Ingredient.of(Items.CRIMSON_FUNGUS), Ingredient.of(Items.WARPED_FUNGUS)), new ItemStack(BMItems.WEAK_FILLING_AGENT.get()), 2, 2000, 100);
        alchemyTable(output, "cycling_catalyst", List.of(Ingredient.of(BMItems.SIMPLE_CATALYST.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/lapis"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/lapis"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dyes/green"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:sands")))), new ItemStack(BMItems.CYCLING_CATALYST.get()), 2, 1000, 100);
        alchemyTable(output, "combinational", List.of(Ingredient.of(BMItems.SIMPLE_CATALYST.get()), Ingredient.of(Items.BROWN_MUSHROOM), Ingredient.of(Items.RED_MUSHROOM), Ingredient.of(Items.SLIME_BALL), Ingredient.of(Items.COAL)), new ItemStack(BMItems.COMBINATIONAL_CATALYST.get()), 4, 2000, 100);

        // ===== Alchemical Potion Flask (all 110 ported from 1.20.1 - the 28 that reference
        // SPECTRAL_SIGHT/GRAVITY/GROUNDED/OBSIDIAN_CLOAK/HARD_CLOAK/BOUNCE were initially skipped
        // pending those potion effects existing; now that BMPotions has all 16 of 1.20.1's effects,
        // they're included below alongside the rest instead of being kept as a separate batch) =====

        Ingredient simpleCatalyst = Ingredient.of(BMItems.SIMPLE_CATALYST.get());
        Ingredient combinationalCatalyst = Ingredient.of(BMItems.COMBINATIONAL_CATALYST.get());
        Ingredient fermentedSpiderEye = Ingredient.of(Items.FERMENTED_SPIDER_EYE);

        // --- Fill (refill uses / trim effect count - flask must already hold an effect) ---
        flaskFill(output, "fill_weak", List.of(Ingredient.of(BMItems.WEAK_FILLING_AGENT.get())), 1000, 200, 0, 1);
        flaskFill(output, "fill_standard", List.of(Ingredient.of(BMItems.AVERAGE_FILLING_AGENT.get())), 3000, 200, 0, 3);

        // --- Effect (adds a brand-new effect slot to an empty/other flask) ---
        flaskEffect(output, "speed_boost", List.of(simpleCatalyst, Ingredient.of(Items.SUGAR)), 500, 200, 1, MobEffects.MOVEMENT_SPEED, 3600);
        flaskEffect(output, "jump_boost", List.of(simpleCatalyst, Ingredient.of(Items.RABBIT_FOOT)), 500, 200, 1, MobEffects.JUMP, 3600);
        flaskEffect(output, "fire_resist", List.of(simpleCatalyst, Ingredient.of(Items.MAGMA_CREAM)), 500, 200, 1, MobEffects.FIRE_RESISTANCE, 3600);
        flaskEffect(output, "strength", List.of(simpleCatalyst, Ingredient.of(Items.BLAZE_POWDER)), 500, 200, 1, MobEffects.DAMAGE_BOOST, 3600);
        flaskEffect(output, "regen", List.of(simpleCatalyst, Ingredient.of(Items.GHAST_TEAR)), 500, 200, 1, MobEffects.REGENERATION, 900);
        flaskEffect(output, "weakness", List.of(simpleCatalyst, fermentedSpiderEye), 500, 200, 1, MobEffects.WEAKNESS, 1800);
        flaskEffect(output, "poison", List.of(simpleCatalyst, Ingredient.of(Items.SPIDER_EYE)), 500, 200, 1, MobEffects.POISON, 900);
        flaskEffect(output, "water_breathing", List.of(simpleCatalyst, Ingredient.of(Items.PUFFERFISH)), 500, 200, 1, MobEffects.WATER_BREATHING, 3600);
        flaskEffect(output, "night_vision", List.of(simpleCatalyst, Ingredient.of(Items.GOLDEN_CARROT)), 500, 200, 1, MobEffects.NIGHT_VISION, 3600);
        flaskEffect(output, "slow_fall", List.of(simpleCatalyst, Ingredient.of(Items.PHANTOM_MEMBRANE)), 500, 200, 1, MobEffects.SLOW_FALLING, 1800);
        flaskEffect(output, "passivity", List.of(simpleCatalyst, Ingredient.of(Items.HONEYCOMB)), 500, 200, 1, BMPotions.PASSIVITY.getDelegate(), 3600);
        flaskEffect(output, "health", List.of(simpleCatalyst, Ingredient.of(Items.GLISTERING_MELON_SLICE)), 500, 200, 1, MobEffects.HEAL, 0);
        flaskEffect(output, "bounce", List.of(simpleCatalyst, Ingredient.of(Items.SLIME_BALL)), 500, 200, 1, BMPotions.BOUNCE.getDelegate(), 3600);
        flaskEffect(output, "hard_cloak", List.of(simpleCatalyst, Ingredient.of(Items.OBSIDIAN)), 500, 200, 1, BMPotions.HARD_CLOAK.getDelegate(), 3600);

        // --- Transform (swaps N held effect types for M different ones) ---
        flaskTransform(output, "speed_to_slow", List.of(fermentedSpiderEye), 500, 200, 1, MobEffects.MOVEMENT_SLOWDOWN, 1800, MobEffects.MOVEMENT_SPEED);
        flaskTransform(output, "jump_to_slow", List.of(fermentedSpiderEye), 500, 200, 1, MobEffects.MOVEMENT_SLOWDOWN, 1800, MobEffects.JUMP);
        flaskTransform(output, "health_to_harm", List.of(fermentedSpiderEye), 500, 200, 1, MobEffects.HARM, 0, MobEffects.HEAL);
        flaskTransform(output, "poison_to_harm", List.of(fermentedSpiderEye), 500, 200, 1, MobEffects.HARM, 0, MobEffects.POISON);
        flaskTransform(output, "night_to_invis", List.of(fermentedSpiderEye), 500, 200, 1, MobEffects.INVISIBILITY, 3600, MobEffects.NIGHT_VISION);
        flaskTransform(output, "fall_to_levitation", List.of(fermentedSpiderEye), 500, 200, 1, MobEffects.LEVITATION, 3600, MobEffects.SLOW_FALLING);
        flaskTransform(output, "suspended_to_flight", List.of(combinationalCatalyst), 1000, 200, 1,
                List.of(new FlaskEffectAmount(BMPotions.FLIGHT.getDelegate(), 3600)), List.of(BMPotions.SUSPENDED.getDelegate(), MobEffects.LEVITATION));
        flaskTransform(output, "night_to_spectral", List.of(Ingredient.of(Items.GLOWSTONE_DUST)), 500, 200, 1, BMPotions.SPECTRAL_SIGHT.getDelegate(), 3600, MobEffects.NIGHT_VISION);
        flaskTransform(output, "gravity_to_heart", List.of(combinationalCatalyst), 1000, 200, 1,
                List.of(new FlaskEffectAmount(BMPotions.HEAVY_HEART.getDelegate(), 1800)), List.of(BMPotions.GRAVITY.getDelegate(), MobEffects.HEAL));
        flaskTransform(output, "gravity", List.of(combinationalCatalyst), 1000, 200, 1,
                List.of(new FlaskEffectAmount(BMPotions.GRAVITY.getDelegate(), 1800)), List.of(BMPotions.GROUNDED.getDelegate(), MobEffects.SLOW_FALLING));
        flaskTransform(output, "gravity_to_suspended", List.of(fermentedSpiderEye), 1000, 200, 1, BMPotions.SUSPENDED.getDelegate(), 1800, BMPotions.GRAVITY.getDelegate());
        flaskTransform(output, "hard_to_obsidian", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/diamond"))), Ingredient.of(Items.CRYING_OBSIDIAN)), 1000, 200, 1, BMPotions.OBSIDIAN_CLOAK.getDelegate(), 3600, BMPotions.HARD_CLOAK.getDelegate());
        flaskTransform(output, "jump_to_grounded", List.of(Ingredient.of(Items.COBWEB)), 1000, 200, 1, BMPotions.GROUNDED.getDelegate(), 1800, MobEffects.JUMP);

        // --- Potency/Length modifier loop (10 of the old 14 effects; 40 recipes) ---
        flaskModifiers(output, MobEffects.MOVEMENT_SPEED, "speed_boost");
        flaskModifiers(output, MobEffects.JUMP, "jump_boost");
        flaskModifiers(output, MobEffects.DAMAGE_BOOST, "strength");
        flaskModifiers(output, MobEffects.WEAKNESS, "weakness");
        flaskModifiers(output, MobEffects.POISON, "poison");
        flaskModifiers(output, MobEffects.REGENERATION, "regen");
        flaskModifiers(output, MobEffects.LEVITATION, "levitation");
        flaskModifiers(output, MobEffects.MOVEMENT_SLOWDOWN, "slowness");
        flaskModifiers(output, BMPotions.HEAVY_HEART.getDelegate(), "heavy_heart");
        flaskModifiers(output, BMPotions.FLIGHT.getDelegate(), "flight");
        flaskModifiers(output, BMPotions.HARD_CLOAK.getDelegate(), "hard_cloak");
        flaskModifiers(output, BMPotions.OBSIDIAN_CLOAK.getDelegate(), "obsidian_cloak");
        flaskModifiers(output, BMPotions.GRAVITY.getDelegate(), "gravity");
        flaskModifiers(output, BMPotions.SPECTRAL_SIGHT.getDelegate(), "spectral_sight");

        // --- Explicit potency (HEAL/HARM aren't in the loop above - different tier for "average") ---
        Ingredient mundanePowerCatalyst = Ingredient.of(BMItems.MUNDANE_POWER_CATALYST.get());
        Ingredient averagePowerCatalyst = Ingredient.of(BMItems.AVERAGE_POWER_CATALYST.get());
        flaskPotency(output, "potency_health", List.of(mundanePowerCatalyst), 200, 100, 1, MobEffects.HEAL, 1, 0.5);
        flaskPotency(output, "potency_harm", List.of(mundanePowerCatalyst), 200, 100, 1, MobEffects.HARM, 1, 0.5);
        flaskPotency(output, "potency_average_health", List.of(averagePowerCatalyst), 500, 100, 3, MobEffects.HEAL, 2, 0.25);
        flaskPotency(output, "potency_average_harm", List.of(averagePowerCatalyst), 500, 100, 3, MobEffects.HARM, 2, 0.25);

        // --- Explicit length (effects not covered by the modifier loop; 14 of the old 18) ---
        Ingredient mundaneLengtheningCatalyst = Ingredient.of(BMItems.MUNDANE_LENGTHENING_CATALYST.get());
        Ingredient averageLengtheningCatalyst = Ingredient.of(BMItems.AVERAGE_LENGTHENING_CATALYST.get());
        flaskLength(output, "length_fire_resist", List.of(mundaneLengtheningCatalyst), 200, 100, 1, MobEffects.FIRE_RESISTANCE, 2.6667);
        flaskLength(output, "length_water_breathing", List.of(mundaneLengtheningCatalyst), 200, 100, 1, MobEffects.WATER_BREATHING, 2.6667);
        flaskLength(output, "length_night_vision", List.of(mundaneLengtheningCatalyst), 200, 100, 1, MobEffects.NIGHT_VISION, 2.6667);
        flaskLength(output, "length_invisibility", List.of(mundaneLengtheningCatalyst), 200, 100, 1, MobEffects.INVISIBILITY, 2.6667);
        flaskLength(output, "length_slow_fall", List.of(mundaneLengtheningCatalyst), 200, 100, 1, MobEffects.SLOW_FALLING, 2.6667);
        flaskLength(output, "length_passivity", List.of(mundaneLengtheningCatalyst), 200, 100, 1, BMPotions.PASSIVITY.getDelegate(), 2.6667);
        flaskLength(output, "length_suspended", List.of(mundaneLengtheningCatalyst), 200, 100, 1, BMPotions.SUSPENDED.getDelegate(), 2.6667);
        flaskLength(output, "length_average_fire_resist", List.of(averageLengtheningCatalyst), 500, 100, 4, MobEffects.FIRE_RESISTANCE, 7.1112);
        flaskLength(output, "length_average_water_breathing", List.of(averageLengtheningCatalyst), 500, 100, 4, MobEffects.WATER_BREATHING, 7.1112);
        flaskLength(output, "length_average_night_vision", List.of(averageLengtheningCatalyst), 500, 100, 4, MobEffects.NIGHT_VISION, 7.1112);
        flaskLength(output, "length_average_invisibility", List.of(averageLengtheningCatalyst), 500, 100, 4, MobEffects.INVISIBILITY, 7.1112);
        flaskLength(output, "length_average_slow_fall", List.of(averageLengtheningCatalyst), 500, 100, 4, MobEffects.SLOW_FALLING, 7.1112);
        flaskLength(output, "length_average_passivity", List.of(averageLengtheningCatalyst), 500, 100, 4, BMPotions.PASSIVITY.getDelegate(), 7.1112);
        flaskLength(output, "length_average_suspended", List.of(averageLengtheningCatalyst), 500, 100, 4, BMPotions.SUSPENDED.getDelegate(), 7.1112);
        flaskLength(output, "length_bounce", List.of(mundaneLengtheningCatalyst), 200, 100, 1, BMPotions.BOUNCE.getDelegate(), 2.6667);
        flaskLength(output, "length_average_bounce", List.of(averageLengtheningCatalyst), 500, 100, 4, BMPotions.BOUNCE.getDelegate(), 7.1112);
        flaskLength(output, "length_grounded", List.of(mundaneLengtheningCatalyst), 200, 100, 1, BMPotions.GROUNDED.getDelegate(), 2.6667);
        flaskLength(output, "length_average_grounded", List.of(averageLengtheningCatalyst), 500, 100, 4, BMPotions.GROUNDED.getDelegate(), 7.1112);

        // --- Flask item-transform (turns a drinkable flask into a throwable/lingering one) ---
        flaskItemTransform(output, "flask_splash", List.of(simpleCatalyst, Ingredient.of(Items.GUNPOWDER)), 1000, 200, 1, new ItemStack(BMItems.ALCHEMY_FLASK_THROWABLE.get()));
        flaskItemTransform(output, "flask_lingering", List.of(simpleCatalyst, Ingredient.of(Items.DRAGON_BREATH)), 1000, 200, 1, new ItemStack(BMItems.ALCHEMY_FLASK_LINGERING.get()));

        // --- Cycle (rotates which held effect is "first") ---
        flaskCycle(output, "cycle_basic", List.of(Ingredient.of(BMItems.CYCLING_CATALYST.get())), 500, 50, 1, 1);

        // ===== Demonic Will collection chain (3 ported from 1.20.1's soulforge recipes for
        // BlockDemonCrucible/BlockDemonCrystallizer/BlockDemonPylon; the original crystallizer
        // recipe referenced its own output item "bloodmagic:soulforge" as an ingredient, which
        // looks like a authoring mistake in the original data - substituted here with the Crucible
        // itself, i.e. you upgrade a Crucible into a Crystallizer) =====
        soulForge(output, "demon_crucible", List.of(Ingredient.of(Items.CAULDRON), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/lapis"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/diamond")))), new ItemStack(BMBlocks.DEMON_CRUCIBLE.item().get()), 400, 100);
        soulForge(output, "demon_crystallizer", List.of(Ingredient.of(BMBlocks.DEMON_CRUCIBLE.item().get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/lapis"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:glass_blocks")))), new ItemStack(BMBlocks.DEMON_CRYSTALLIZER.item().get()), 500, 100);
        soulForge(output, "demon_pylon", List.of(Ingredient.of(BMItems.RAW_WILL.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/lapis"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:storage_blocks/iron")))), new ItemStack(BMBlocks.DEMON_PYLON.item().get()), 400, 50);

        // Demon Will Gauge: ported from 1.20.1's soulforge recipe (gold ingot + redstone dust + glass
        // + "any Demon Crystal" -> demonwillgauge, minimumDrain 400/drain 50) - "any Demon Crystal"
        // (a 5-item list of the old per-color crystal items) becomes this branch's single consolidated
        // BMItems.RAW_WILL, same substitution already established by demon_pylon just above.
        soulForge(output, "demon_will_gauge", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/gold"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:glass_blocks"))), Ingredient.of(BMItems.RAW_WILL.get())), new ItemStack(BMItems.DEMON_WILL_GAUGE.get()), 400, 50);

        // ===== Explosive Charge family (4 ported from 1.20.1's soulforge recipes for
        // BlockShapedExplosive/BlockDeforesterCharge/BlockFungalCharge/BlockVeinMineCharge; the
        // original's "forge:sandstone"/"forge:mushrooms"/"minecraft:mushroom_hyphae" ingredient
        // tags aren't confirmed to exist on this branch, so those 2 recipes use plain vanilla items
        // instead - see BMBlocks for why only the base tier of each charge is ported, and why the
        // 20 anointment-combo soulforge recipes (fortune/silk touch/smelting/voiding variants)
        // aren't - no AnointmentHolder integration on these charges) =====
        soulForge(output, "shaped_charge", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:cobblestones"))), Ingredient.of(Items.CHARCOAL), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:sands"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones")))), Util.make(new ItemStack(BMBlocks.SHAPED_CHARGE.item().get()), s -> s.setCount(8)), 10, 0.5);
        soulForge(output, "deforester_charge", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:cobblestones"))), Ingredient.of(Items.CHARCOAL), Ingredient.of(net.minecraft.tags.ItemTags.LOGS), Ingredient.of(net.minecraft.tags.ItemTags.PLANKS)), Util.make(new ItemStack(BMBlocks.DEFORESTER_CHARGE.item().get()), s -> s.setCount(8)), 10, 0.5);
        soulForge(output, "fungal_charge", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:cobblestones"))), Ingredient.of(Items.CHARCOAL), Ingredient.of(Items.RED_MUSHROOM_BLOCK, Items.BROWN_MUSHROOM_BLOCK), Ingredient.of(Items.RED_MUSHROOM, Items.BROWN_MUSHROOM)), Util.make(new ItemStack(BMBlocks.FUNGAL_CHARGE.item().get()), s -> s.setCount(8)), 10, 0.5);
        soulForge(output, "veinmine_charge", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:cobblestones"))), Ingredient.of(Items.CHARCOAL), Ingredient.of(Items.SANDSTONE), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:sands")))), Util.make(new ItemStack(BMBlocks.VEINMINE_CHARGE.item().get()), s -> s.setCount(8)), 10, 0.5);

        // ===== ARC tool items (13 ported from 1.20.1: the durability-gated catalysts placed in the
        // Alchemical Reaction Chamber's tool slot). BMTags.Items.REVERTER/EXPLOSIVES/RESONATOR/
        // CUTTING_FLUIDS/HYDRATION/ARC_SMELTING were all empty until BMItems gained these
        // registrations, meaning every ARC recipe elsewhere in this file that gates on one of those
        // tags was unreachable - see BMItemTagProvider for the tag population.
        //
        // Tartaric Forge (1.20.1's BlockSoulForge/TileSoulForge - "Tartaric Forge" is just its
        // in-game name) doesn't exist as a block on this branch; only the differently-recipe-typed
        // Hellfire Forge (soulForge() below) does. So sanguinereverter/resonator/
        // primitive_crystalline_resonator/hellforged_resonator (all originally Tartaric Forge
        // recipes) are mirrored onto soulForge() instead, the same substitution already used above
        // for sentient_armour_gem. Their original "defaultcrystal"/"tauoil"/hellforged-ingot
        // ingredients don't exist on this branch either - defaultcrystal is a Demon Crystal drop
        // (out of scope, see common/block/Demon*.java) and tauoil/the hellforged ingot were never
        // ported - so those slots use a vanilla Quartz stand-in for "raw crystal", Corrupted (Tiny)
        // Dust for "tau oil", and BMItems.HELLFORGED_PARTS for the hellforged ingot.
        //
        // explosivepowder/*cuttingfluid were originally Alchemy Table recipes, and the Alchemy Table
        // does exist on this branch, so those are ported via alchemyTable() with the same
        // Corrupted (Tiny) Dust substitution for the not-yet-ported "tau"/plant-oil reagents.
        //
        // lavacrystal/furnacecell_primitive/primitive_hydration_cell were originally plain
        // crafting-table recipes and are ported as-is (see the updated skip comment further above).
        // lavacrystal loses 1.20.1's bindable/fire-starting/furnace-fuel behaviour (ItemLavaCrystal)
        // since there's no item-side Binding+Soul-Network-syphon implementation on this branch yet -
        // see BMItems.LAVA_CRYSTAL for details; it never implemented 1.20.1's IARCTool anyway, so
        // this doesn't change its ARC behaviour either way.
        //
        // Both furnace-tier items go only into BMTags.Items.ARC_SMELTING, not ARC_BLASTING/
        // ARC_SMOKING - see BMItems.PRIMITIVE_FURNACE_CELL for why. =====

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMItems.LAVA_CRYSTAL.get())
                .define('a', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:glass_blocks")))
                .define('b', Items.LAVA_BUCKET)
                .define('c', bloodOrb(0))
                .define('d', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:obsidians")))
                .define('e', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/diamond")))
                .pattern("aba")
                .pattern("bcb")
                .pattern("ded")
                .unlockedBy("has_weak_orb", has(BMItems.ORB_WEAK.get()))
                .save(output, BloodMagic.rl("lava_crystal"));

        // Weak Activation Crystal: ported from 1.20.1 (BloodAltarRecipeProvider's
        // "weak_activation_crystal", AltarTier.THREE.ordinal() == minTier 2 on this branch's 0-indexed
        // altar() helper - see the "demonicslate"/"teleposer_focus"/"archmagebloodorb" altar recipes
        // above for the same AltarTier-ordinal-to-minTier mapping already established in this file).
        // ACTIVATION_CRYSTAL_AWAKENED isn't ported alongside it: 1.20.1 registered that item too but
        // never gave it a crafting recipe of any kind either (no altar/table/forge/ritual reference
        // anywhere in that branch's source), so there's no precedent to port from - it's still
        // registered/obtainable via creative/commands, matching this file's existing
        // REINFORCED_TELEPOSER_FOCUS precedent. ACTIVATION_CRYSTAL_CREATIVE is intentionally excluded
        // (its own CrystalType.CREATIVE name and Integer.MAX_VALUE crystal level make it a
        // debug/creative-only item, not meant to be player-crafted).
        altar(output, "weak_activation_crystal", Ingredient.of(BMItems.LAVA_CRYSTAL.get()), new ItemStack(BMItems.ACTIVATION_CRYSTAL_WEAK.get()), 2, 10000, 20, 10);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMItems.PRIMITIVE_FURNACE_CELL.get())
                .define('c', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:cobblestones")))
                .define('f', Items.COAL_BLOCK)
                .define('s', BMItems.SLATE_BLANK.get())
                .define('o', bloodOrb(2))
                .pattern("csc")
                .pattern("cfc")
                .pattern("coc")
                .unlockedBy("has_magician_orb", has(BMItems.ORB_MAGICIAN.get()))
                .save(output, BloodMagic.rl("primitive_furnace_cell"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMItems.PRIMITIVE_HYDRATION_CELL.get())
                .define('B', Items.WATER_BUCKET)
                .define('c', TagKey.create(Registries.ITEM, ResourceLocation.parse("c:cobblestones")))
                .define('o', bloodOrb(2))
                .define('s', BMItems.SLATE_BLANK.get())
                .pattern("csc")
                .pattern("cBc")
                .pattern("coc")
                .unlockedBy("has_magician_orb", has(BMItems.ORB_MAGICIAN.get()))
                .save(output, BloodMagic.rl("primitive_hydration_cell"));

        alchemyTable(output, "explosive_powder", List.of(Ingredient.of(Items.GUNPOWDER), Ingredient.of(Items.GUNPOWDER), Ingredient.of(Items.COAL)), new ItemStack(BMItems.EXPLOSIVE_POWDER.get()), 1, 500, 200);
        alchemyTable(output, "primitive_explosive_cell", List.of(Ingredient.of(Items.GUNPOWDER), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone"))), Ingredient.of(BMItems.CORRUPTED_DUST_TINY.get()), Ingredient.of(Items.COAL)), new ItemStack(BMItems.PRIMITIVE_EXPLOSIVE_CELL.get()), 3, 1000, 200);
        alchemyTable(output, "hellforged_explosive_cell", List.of(Ingredient.of(Items.GUNPOWDER), Ingredient.of(Items.BLAZE_POWDER), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:gems/quartz"))), Ingredient.of(BMItems.CORRUPTED_DUST.get()), Ingredient.of(Items.COAL)), new ItemStack(BMItems.HELLFORGED_EXPLOSIVE_CELL.get()), 4, 4000, 200);

        alchemyTable(output, "basic_cutting_fluid", List.of(Ingredient.of(Items.SLIME_BALL), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/redstone"))), Ingredient.of(Items.GUNPOWDER), Ingredient.of(Items.SUGAR), Ingredient.of(Items.COAL), Ingredient.of(Items.WATER_BUCKET)), new ItemStack(BMItems.BASIC_CUTTING_FLUID.get()), 1, 1000, 200);
        alchemyTable(output, "intermediate_cutting_fluid", List.of(Ingredient.of(Items.SLIME_BALL), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts/glowstone"))), Ingredient.of(Items.GUNPOWDER), Ingredient.of(Items.SUGAR), Ingredient.of(Items.BLAZE_POWDER), Ingredient.of(Items.WATER_BUCKET)), new ItemStack(BMItems.INTERMEDIATE_CUTTING_FLUID.get()), 3, 2000, 200);
        alchemyTable(output, "advanced_cutting_fluid", List.of(Ingredient.of(Items.SLIME_BALL), Ingredient.of(BMItems.CORRUPTED_DUST.get()), Ingredient.of(Items.GLOW_BERRIES), Ingredient.of(Items.QUARTZ), Ingredient.of(Items.BLAZE_POWDER), Ingredient.of(Items.WATER_BUCKET)), new ItemStack(BMItems.ADVANCED_CUTTING_FLUID.get()), 4, 4000, 200);

        soulForge(output, "sanguine_reverter", List.of(Ingredient.of(Items.SHEARS), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones"))), Ingredient.of(BMItems.SLATE_IMBUED.get()), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron")))), new ItemStack(BMItems.SANGUINE_REVERTER.get()), 50, 10);
        soulForge(output, "resonator", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:stones"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/copper"))), Ingredient.of(Items.QUARTZ)), new ItemStack(BMItems.RESONATOR.get()), 50, 10);
        soulForge(output, "primitive_crystalline_resonator", List.of(Ingredient.of(Items.AMETHYST_SHARD), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots"))), Ingredient.of(Items.QUARTZ), Ingredient.of(BMItems.CORRUPTED_DUST_TINY.get())), new ItemStack(BMItems.PRIMITIVE_CRYSTALLINE_RESONATOR.get()), 150, 40);
        soulForge(output, "hellforged_resonator", List.of(Ingredient.of(Items.AMETHYST_SHARD), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/gold"))), Ingredient.of(Items.QUARTZ), Ingredient.of(BMItems.HELLFORGED_PARTS.get())), new ItemStack(BMItems.HELLFORGED_RESONATOR.get()), 300, 80);

        // ===== Demon Dungeon system (Priority 1/3 flavor content) - now that the Mimic and
        // dungeon_stone both exist, 1.20.1's ethereal_mimic recipe is portable as-is. Plain MIMIC
        // has no upstream crafting recipe either (dungeon/loot-generated only), matching 1.20.1. =====
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BMBlocks.ETHEREAL_MIMIC.item().get(), 8)
                .define('E', BMItems.SLATE_ETHEREAL.get())
                .define('S', BMBlocks.DUNGEON_STONE.item().get())
                .pattern("SSS")
                .pattern("SES")
                .pattern("SSS")
                .unlockedBy("has_ethereal_slate", has(BMItems.SLATE_ETHEREAL.get()))
                .save(output, BloodMagic.rl("ethereal_mimic"));

        // Dungeon Keys - upstream's "simple_key" hellfire/soul forge recipe is portable as-is
        // (redstone block + 2 iron ingots + infused slate, all real on this branch). Upstream's
        // LP-scale minimumDrain/drain (300.0/50.0) don't carry over meaningfully to this branch's
        // reworked Hellfire Forge (a Will-based minWill/drain pair, not raw LP) - 50/10 matches the
        // scale of this branch's other simple, low-tier forge recipes (sanguine_reverter, resonator).
        soulForge(output, "simple_key", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:storage_blocks/redstone"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron"))), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/iron"))), Ingredient.of(BMItems.SLATE_IMBUED.get())), new ItemStack(BMItems.DUNGEON_SIMPLE_KEY.get()), 50, 10);
        // "mine_key" was previously skipped since it needs a smeltable Hellforged Ingot, which
        // didn't exist on this branch at the time - it now does (BMItems.HELLFORGED_INGOT, added
        // alongside the rest of the Hellforged material chain). Upstream also required one of 5
        // per-Will-type "*_CRYSTAL" items (CORROSIVE/DESTRUCTIVE/VENGEFUL/RAW/STEADFAST_CRYSTAL as
        // alternatives in a single Ingredient slot); this branch never split the Will Crystal ore
        // drop into 5 typed items, so - matching the exact same substitution DungeonChestLoot.java's
        // javadoc already establishes for this concept - the matching *_CATALYST family
        // (BMItems#CORROSIVE_CATALYST/STEADFAST_CATALYST/VENGEFUL_CATALYST/DESTRUCTIVE_CATALYST/
        // RAW_CATALYST) is used as the 5 alternatives instead. minWill/drain (300/80) matches this
        // branch's other Hellforged-tier forge recipe, hellforged_resonator, since both consume a
        // rare late-game Hellforged material.
        soulForge(output, "mine_key", List.of(Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots/copper"))), Ingredient.of(BMItems.CORROSIVE_CATALYST.get(), BMItems.DESTRUCTIVE_CATALYST.get(), BMItems.VENGEFUL_CATALYST.get(), BMItems.RAW_CATALYST.get(), BMItems.STEADFAST_CATALYST.get()), Ingredient.of(BMItems.HELLFORGED_INGOT.get()), Ingredient.of(BMItems.SLATE_IMBUED.get())), new ItemStack(BMItems.DUNGEON_MINE_KEY.get()), 300, 80);

        // ===== Item Routing "Filter" system, restored from 1.20.1 (see BMItems and
        // wayoftime.bloodmagic.common.item.filter.AbstractFilterItem). Each filter is a paper "form"
        // inscribed with an Imbued Slate; the more specialised filters (tag/mod/enchant/composite)
        // are built on top of a Standard Filter rather than from scratch, mirroring how 1.20.1's
        // ItemTagFilter/ItemModFilter/etc. all extended ItemRouterFilter. =====
        soulForge(output, "standard_filter", List.of(Ingredient.of(Items.PAPER), Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:nuggets/iron"))), Ingredient.of(BMItems.SLATE_IMBUED.get())), new ItemStack(BMItems.STANDARD_FILTER.get()), 20, 5);
        soulForge(output, "tag_filter", List.of(Ingredient.of(BMItems.STANDARD_FILTER.get()), Ingredient.of(BMItems.SLATE_IMBUED.get())), new ItemStack(BMItems.TAG_FILTER.get()), 40, 10);
        soulForge(output, "mod_filter", List.of(Ingredient.of(BMItems.STANDARD_FILTER.get()), Ingredient.of(BMItems.SLATE_DEMONIC.get())), new ItemStack(BMItems.MOD_FILTER.get()), 40, 10);
        soulForge(output, "enchant_filter", List.of(Ingredient.of(BMItems.STANDARD_FILTER.get()), Ingredient.of(Items.BOOKSHELF)), new ItemStack(BMItems.ENCHANT_FILTER.get()), 60, 15);
        soulForge(output, "composite_filter", List.of(Ingredient.of(BMItems.STANDARD_FILTER.get()), Ingredient.of(BMItems.STANDARD_FILTER.get()), Ingredient.of(BMItems.SLATE_ETHEREAL.get())), new ItemStack(BMItems.COMPOSITE_FILTER.get()), 80, 20);

        // ===== Living Armour Downgrade Tomes =====
        // Fills a real gap: BMItems.UPGRADE_TOME had zero recipes of any kind, so the Ritual of the
        // Downgrade (LivingDowngradeRitual - see its javadoc) had no legitimate in-game item to
        // consume from its chest. 1.20.1's equivalent (LivingDowngradeRecipeProvider/
        // RecipeLivingDowngrade) was NOT a real crafting recipe at all: it just tagged one raw
        // vanilla item as the "key" RitualLivingDowngrade would recognise sitting in a chest, and
        // the STACK COUNT of that item determined how many downgrade levels got applied in one
        // ritual pulse (e.g. 5 rotten flesh -> Battle Hungry level 5 immediately). That whole points-
        // economy/stack-counting mechanic doesn't exist on this branch (see LivingDowngradeRitual's
        // javadoc) - downgrades are now regular UpgradeTome items carrying a fixed (upgrade, exp)
        // pair, consumed one at a time by the ritual via LivingHelper#applyExpToCap.
        //
        // Every one of this branch's 9 IS_DOWNGRADE upgrades (LivingUpgrades#downgrades) happens to
        // use exp==level 1:1 in its Levels map (e.g. BATTLE_HUNGRY needs exp>=1/2/3/4/5 for levels
        // 1-5), so a tome carrying exactly 1 exp reliably grants exactly 1 level per
        // LivingHelper#applyExp application - no partial/wasted exp, and no risk of overshooting a
        // level boundary. Crafting (and ritual-feeding) N tomes therefore reproduces 1.20.1's old
        // "N key items = N levels" progression tome-by-tome, and matches this branch's own
        // convention of incremental, re-craftable tiers (see the anointment_*_l/_xl chain above)
        // rather than an instant-max single craft. Ingredients mirror 1.20.1's per-downgrade "key
        // item" 1:1 (LivingDowngradeRecipeProvider); the Blood Altar tier/cost mirrors this branch's
        // other simple single-ingredient altar transforms (see "reinforcedslate" above).
        altar(output, "downgrade_tome_battle_hungry", Ingredient.of(Items.ROTTEN_FLESH), tome(registries, Upgrades.BATTLE_HUNGRY, 1), 1, 2000, 5, 5);
        altar(output, "downgrade_tome_melee_decrease", Ingredient.of(Items.STONE_SWORD), tome(registries, Upgrades.MELEE_DECREASE, 1), 1, 2000, 5, 5);
        altar(output, "downgrade_tome_quenched", Ingredient.of(Items.GLASS_BOTTLE), tome(registries, Upgrades.QUENCHED, 1), 1, 2000, 5, 5);
        altar(output, "downgrade_tome_storm_trooper", Ingredient.of(Items.ARROW), tome(registries, Upgrades.STORM_TROOPER, 1), 1, 2000, 5, 5);
        altar(output, "downgrade_tome_dig_slowdown", Ingredient.of(Items.STONE_PICKAXE), tome(registries, Upgrades.DIG_SLOWDOWN, 1), 1, 2000, 5, 5);
        altar(output, "downgrade_tome_slow_heal", Ingredient.of(Items.GHAST_TEAR), tome(registries, Upgrades.SLOW_HEAL, 1), 1, 2000, 5, 5);
        altar(output, "downgrade_tome_swim_decrease", Ingredient.of(Items.WATER_BUCKET), tome(registries, Upgrades.SWIM_DECREASE, 1), 1, 2000, 5, 5);
        altar(output, "downgrade_tome_speed_decrease", Ingredient.of(Items.SOUL_SAND), tome(registries, Upgrades.SPEED_DECREASE, 1), 1, 2000, 5, 5);
        altar(output, "downgrade_tome_crippled_arm", Ingredient.of(Items.SHIELD), tome(registries, Upgrades.CRIPPLED_ARM, 1), 1, 2000, 5, 5);
    }

    private static Ingredient bloodOrb(int minTier) {
        return new Ingredient(new BloodOrbIngredient(minTier));
    }

    private static ItemStack sigil(ResourceKey<SigilEffect> effect) {
        ItemStack stack = new ItemStack(BMItems.SIGIL.get());
        stack.set(BMDataComponents.SIGIL_EFFECT, effect);
        return stack;
    }

    /**
     * Builds an UPGRADE_TOME ItemStack pre-loaded with a specific {@code (Holder<LivingUpgrade>, exp)}
     * pair via the {@code BMDataComponents.UPGRADE_TOME_DATA} component - see
     * {@code wayoftime.bloodmagic.common.datacomponent.UpgradeTome}. {@code LivingUpgrade} is a
     * datapack registry (not a hardcoded one), so the real {@link Holder} backing it has to be
     * resolved from the {@code HolderLookup.Provider} handed to {@code buildRecipes} rather than
     * constructed directly; {@code ItemStack.CODEC} (used as-is by the Blood Altar/Alchemy
     * Table/Hellfire Forge recipe result field, same as the anointment/sigil results above) already
     * round-trips arbitrary data components including this one, so no custom recipe type is needed.
     */
    private static ItemStack tome(HolderLookup.Provider registries, ResourceKey<LivingUpgrade> upgrade, float exp) {
        ItemStack stack = new ItemStack(BMItems.UPGRADE_TOME.get());
        Holder<LivingUpgrade> holder = registries.lookupOrThrow(BMIdentifiers.RegistryKeys.LIVING_UPGRADES).getOrThrow(upgrade);
        stack.set(BMDataComponents.UPGRADE_TOME_DATA, new UpgradeTome(holder, exp));
        return stack;
    }

    private void array(RecipeOutput output, String id, Ingredient baseInput, Ingredient addedInput, ItemStack result) {
        array(output, id, AlchemyArrayRecipe.DEFAULT_TEXTURE, baseInput, addedInput, result);
    }

    /**
     * Overload carrying the per-recipe ground-circle texture forward - see {@code arrayTexture()}
     * and the call sites above for the real 1.20.1 texture mapping this was ported from
     * ({@code AlchemyArrayRecipeProvider}/{@code AlchemyArrayRecipeBuilder} on that branch).
     */
    private void array(RecipeOutput output, String id, ResourceLocation texture, Ingredient baseInput, Ingredient addedInput, ItemStack result) {
        ResourceLocation rl = BloodMagic.rl("array/" + id);
        output.accept(rl, new AlchemyArrayRecipe(baseInput, addedInput, result, texture), advancement(output, rl));
    }

    private static ResourceLocation arrayTexture(String fileName) {
        return BloodMagic.rl("textures/models/alchemyarrays/" + fileName);
    }

    private void altar(RecipeOutput output, String id, Ingredient input, ItemStack result, int minTier, int totalBlood, int craftSpeed, int drainSpeed) {
        ResourceLocation rl = BloodMagic.rl("blood_altar/" + id);
        output.accept(rl, new BloodAltarRecipe(input, result, minTier, totalBlood, craftSpeed, drainSpeed), advancement(output, rl));
    }

    private void arc(RecipeOutput output, String id, TagKey<Item> toolTag, Ingredient input, List<ItemStack> guaranteed, List<Pair<ItemStack, Double>> chanced, FluidStack inputFluid, FluidStack outputFluid) {
        ResourceLocation rl = BloodMagic.rl("arc/" + id);
        ARCRecipe recipe = new ARCRecipe(Ingredient.of(toolTag), input, guaranteed, chanced, Optional.ofNullable(inputFluid), Optional.ofNullable(outputFluid));
        output.accept(rl, recipe, advancement(output, rl));
    }

    private void alchemyTable(RecipeOutput output, String id, List<Ingredient> inputs, ItemStack result, int tier, int essence, int duration) {
        ResourceLocation rl = BloodMagic.rl("alchemy_table/" + id);
        output.accept(rl, new AlchemyTableRecipe(inputs, tier, essence, duration, result), advancement(output, rl));
    }

    private void soulForge(RecipeOutput output, String id, List<Ingredient> inputs, ItemStack result, double minWill, double drain) {
        ResourceLocation rl = BloodMagic.rl("hellfire_forge/" + id);
        output.accept(rl, new ForgeRecipe(minWill, drain, inputs, result, Optional.empty()), advancement(output, rl));
    }

    // ===== Alchemical Potion Flask (Alchemy Table recipes that read/write a flask's CURRENT
    // stored potion effect(s) rather than just matching item ingredients - see FlaskRecipe) =====

    private void flaskFill(RecipeOutput output, String id, List<Ingredient> inputs, int syphon, int ticks, int minimumTier, int maxEffects) {
        ResourceLocation rl = BloodMagic.rl("flask/" + id);
        output.accept(rl, new FlaskFillRecipe(inputs, syphon, ticks, minimumTier, maxEffects), advancement(output, rl));
    }

    private void flaskEffect(RecipeOutput output, String id, List<Ingredient> inputs, int syphon, int ticks, int minimumTier, Holder<MobEffect> effect, int baseDuration) {
        ResourceLocation rl = BloodMagic.rl("flask/" + id);
        output.accept(rl, new FlaskEffectRecipe(inputs, syphon, ticks, minimumTier, effect, baseDuration), advancement(output, rl));
    }

    private void flaskLength(RecipeOutput output, String id, List<Ingredient> inputs, int syphon, int ticks, int minimumTier, Holder<MobEffect> effect, double lengthDurationMod) {
        ResourceLocation rl = BloodMagic.rl("flask/" + id);
        output.accept(rl, new FlaskLengthRecipe(inputs, syphon, ticks, minimumTier, effect, lengthDurationMod), advancement(output, rl));
    }

    private void flaskPotency(RecipeOutput output, String id, List<Ingredient> inputs, int syphon, int ticks, int minimumTier, Holder<MobEffect> effect, int amplifier, double ampDurationMod) {
        ResourceLocation rl = BloodMagic.rl("flask/" + id);
        output.accept(rl, new FlaskPotencyRecipe(inputs, syphon, ticks, minimumTier, effect, amplifier, ampDurationMod), advancement(output, rl));
    }

    private void flaskTransform(RecipeOutput output, String id, List<Ingredient> inputs, int syphon, int ticks, int minimumTier, List<FlaskEffectAmount> outputEffects, List<Holder<MobEffect>> inputEffects) {
        ResourceLocation rl = BloodMagic.rl("flask/" + id);
        output.accept(rl, new FlaskTransformRecipe(inputs, syphon, ticks, minimumTier, outputEffects, inputEffects), advancement(output, rl));
    }

    /** Convenience overload for the common 1-effect-in/1-effect-out transform shape. */
    private void flaskTransform(RecipeOutput output, String id, List<Ingredient> inputs, int syphon, int ticks, int minimumTier, Holder<MobEffect> outputEffect, int outputBaseDuration, Holder<MobEffect> inputEffect) {
        flaskTransform(output, id, inputs, syphon, ticks, minimumTier, List.of(new FlaskEffectAmount(outputEffect, outputBaseDuration)), List.of(inputEffect));
    }

    private void flaskCycle(RecipeOutput output, String id, List<Ingredient> inputs, int syphon, int ticks, int minimumTier, int numCycles) {
        ResourceLocation rl = BloodMagic.rl("flask/" + id);
        output.accept(rl, new FlaskCycleRecipe(inputs, syphon, ticks, minimumTier, numCycles), advancement(output, rl));
    }

    private void flaskItemTransform(RecipeOutput output, String id, List<Ingredient> inputs, int syphon, int ticks, int minimumTier, ItemStack result) {
        ResourceLocation rl = BloodMagic.rl("flask/" + id);
        output.accept(rl, new FlaskItemTransformRecipe(inputs, syphon, ticks, minimumTier, result), advancement(output, rl));
    }

    /**
     * Reproduces old 1.20.1 PotionRecipeProvider's {@code addPotionModifiers} helper: for a given
     * effect, generates the standard 4-recipe potency/length family (weak potency, weak length,
     * average potency, average length). {@code namePrefix} matches the old id fragment
     * (e.g. "speed_boost" -> potency_speed_boost.json etc).
     */
    private void flaskModifiers(RecipeOutput output, Holder<MobEffect> effect, String namePrefix) {
        flaskPotency(output, "potency_" + namePrefix, List.of(Ingredient.of(BMItems.MUNDANE_POWER_CATALYST.get())), 200, 100, 1, effect, 1, 0.5);
        flaskLength(output, "length_" + namePrefix, List.of(Ingredient.of(BMItems.MUNDANE_LENGTHENING_CATALYST.get())), 200, 100, 1, effect, 2.6667);
        flaskPotency(output, "potency_average_" + namePrefix, List.of(Ingredient.of(BMItems.AVERAGE_POWER_CATALYST.get())), 500, 100, 4, effect, 2, 0.25);
        flaskLength(output, "length_average_" + namePrefix, List.of(Ingredient.of(BMItems.AVERAGE_LENGTHENING_CATALYST.get())), 500, 100, 4, effect, 7.1112);
    }

    private AdvancementHolder advancement(RecipeOutput output, ResourceLocation recipeId) {
        Advancement.Builder builder = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId))
                .rewards(AdvancementRewards.Builder.recipe(recipeId))
                .requirements(AdvancementRequirements.Strategy.OR);
        return builder.build(recipeId.withPrefix("recipes/bloodmagic/"));
    }
}
