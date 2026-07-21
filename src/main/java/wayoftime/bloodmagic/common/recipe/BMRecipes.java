package wayoftime.bloodmagic.common.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.recipe.alchemy_table.AlchemyTableRecipe;
import wayoftime.bloodmagic.common.recipe.alchemy_table.AlchemyTableSerializer;
import wayoftime.bloodmagic.common.recipe.arc.ARCRecipe;
import wayoftime.bloodmagic.common.recipe.arc.ARCSerializer;
import wayoftime.bloodmagic.common.recipe.array.AlchemyArrayRecipe;
import wayoftime.bloodmagic.common.recipe.array.AlchemyArraySerializer;
import wayoftime.bloodmagic.common.recipe.bloodaltar.BloodAltarRecipe;
import wayoftime.bloodmagic.common.recipe.bloodaltar.BloodAltarRecipeSerializer;
import wayoftime.bloodmagic.common.recipe.flask.FlaskCycleRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskCycleSerializer;
import wayoftime.bloodmagic.common.recipe.flask.FlaskEffectRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskEffectSerializer;
import wayoftime.bloodmagic.common.recipe.flask.FlaskFillRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskFillSerializer;
import wayoftime.bloodmagic.common.recipe.flask.FlaskItemTransformRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskItemTransformSerializer;
import wayoftime.bloodmagic.common.recipe.flask.FlaskLengthRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskLengthSerializer;
import wayoftime.bloodmagic.common.recipe.flask.FlaskPotencyRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskPotencySerializer;
import wayoftime.bloodmagic.common.recipe.flask.FlaskRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskTransformRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskTransformSerializer;
import wayoftime.bloodmagic.common.recipe.forge.ForgeRecipe;
import wayoftime.bloodmagic.common.recipe.forge.ForgeSerializer;
import wayoftime.bloodmagic.common.recipe.tiered.EnergyTieredRecipe;
import wayoftime.bloodmagic.common.recipe.tiered.EnergyTieredSerializer;
import wayoftime.bloodmagic.common.recipe.tiered.FluidTieredRecipe;
import wayoftime.bloodmagic.common.recipe.tiered.FluidTieredSerializer;

import javax.smartcardio.ATR;

public class BMRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, BloodMagic.MODID);
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, BloodMagic.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<ForgeRecipe>> SOUL_FORGE_TYPE = TYPES.register(ForgeRecipe.RECIPE_TYPE_NAME, () -> RecipeType.simple(bm(ForgeRecipe.RECIPE_TYPE_NAME)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ForgeRecipe>> SOUL_FORGE_SERIALIZER = SERIALIZERS.register(ForgeRecipe.RECIPE_TYPE_NAME, ForgeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<BloodAltarRecipe>> BLOOD_ALTAR_TYPE = TYPES.register(BloodAltarRecipe.RECIPE_TYPE_NAME, () -> RecipeType.simple(bm(BloodAltarRecipe.RECIPE_TYPE_NAME)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BloodAltarRecipe>> BLOOD_ALTAR_SERIALIZER = SERIALIZERS.register(BloodAltarRecipe.RECIPE_TYPE_NAME, BloodAltarRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<ARCRecipe>> ARC_TYPE = TYPES.register(ARCRecipe.RECIPE_TYPE_NAME, () -> RecipeType.simple(bm(ARCRecipe.RECIPE_TYPE_NAME)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ARCRecipe>> ARC_SERIALIZER = SERIALIZERS.register(ARCRecipe.RECIPE_TYPE_NAME, ARCSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<FluidTieredRecipe>> FLUID_TIERED_TYPE = TYPES.register(FluidTieredRecipe.NAME, () -> RecipeType.simple(bm(FluidTieredRecipe.NAME)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FluidTieredRecipe>> FLUID_TIERED_SERIALIZER = SERIALIZERS.register(FluidTieredRecipe.NAME, FluidTieredSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<EnergyTieredRecipe>> ENERGY_TIERED_TYPE = TYPES.register(EnergyTieredRecipe.NAME, () -> RecipeType.simple(bm(EnergyTieredRecipe.NAME)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EnergyTieredRecipe>> ENERGY_TIERED_SERIALIZER = SERIALIZERS.register(EnergyTieredRecipe.NAME, EnergyTieredSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<AlchemyTableRecipe>> ALCHEMY_TABLE_TYPE = TYPES.register(AlchemyTableRecipe.RECIPE_TYPE_NAME, () -> RecipeType.simple(bm(AlchemyTableRecipe.RECIPE_TYPE_NAME)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AlchemyTableRecipe>> ALCHEMY_TABLE_SERIALIZER = SERIALIZERS.register(AlchemyTableRecipe.RECIPE_TYPE_NAME, AlchemyTableSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<AlchemyArrayRecipe>> ALCHEMY_ARRAY_TYPE = TYPES.register(AlchemyArrayRecipe.RECIPE_TYPE_NAME, () -> RecipeType.simple(bm(AlchemyArrayRecipe.RECIPE_TYPE_NAME)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AlchemyArrayRecipe>> ALCHEMY_ARRAY_SERIALIZER = SERIALIZERS.register(AlchemyArrayRecipe.RECIPE_TYPE_NAME, AlchemyArraySerializer::new);

    // Alchemical Potion Flask system: all seven recipe subtypes share one RecipeType, matching
    // 1.20.1's single shared "bloodmagic:potionflask" type - see FlaskRecipe's javadoc.
    public static final DeferredHolder<RecipeType<?>, RecipeType<FlaskRecipe>> FLASK_TYPE = TYPES.register(FlaskRecipe.RECIPE_TYPE_NAME, () -> RecipeType.simple(bm(FlaskRecipe.RECIPE_TYPE_NAME)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FlaskFillRecipe>> FLASK_FILL_SERIALIZER = SERIALIZERS.register("flask_fill", FlaskFillSerializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FlaskEffectRecipe>> FLASK_EFFECT_SERIALIZER = SERIALIZERS.register("flask_effect", FlaskEffectSerializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FlaskLengthRecipe>> FLASK_LENGTH_SERIALIZER = SERIALIZERS.register("flask_length", FlaskLengthSerializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FlaskPotencyRecipe>> FLASK_POTENCY_SERIALIZER = SERIALIZERS.register("flask_potency", FlaskPotencySerializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FlaskTransformRecipe>> FLASK_TRANSFORM_SERIALIZER = SERIALIZERS.register("flask_transform", FlaskTransformSerializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FlaskCycleRecipe>> FLASK_CYCLE_SERIALIZER = SERIALIZERS.register("flask_cycle", FlaskCycleSerializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FlaskItemTransformRecipe>> FLASK_ITEM_TRANSFORM_SERIALIZER = SERIALIZERS.register("flask_item_transform", FlaskItemTransformSerializer::new);

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
        TYPES.register(modBus);
    }

    private static ResourceLocation bm(String path) {
        return ResourceLocation.fromNamespaceAndPath(BloodMagic.MODID, path);
    }
}
