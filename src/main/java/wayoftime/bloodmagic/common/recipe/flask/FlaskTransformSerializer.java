package wayoftime.bloodmagic.common.recipe.flask;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class FlaskTransformSerializer implements RecipeSerializer<FlaskTransformRecipe>
{
	public static final MapCodec<FlaskTransformRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Ingredient.CODEC_NONEMPTY.listOf().fieldOf("input").forGetter(FlaskTransformRecipe::inputs),
			Codec.INT.fieldOf("syphon").forGetter(FlaskTransformRecipe::syphon),
			Codec.INT.fieldOf("ticks").forGetter(FlaskTransformRecipe::ticks),
			Codec.INT.fieldOf("minimum_tier").forGetter(FlaskTransformRecipe::minimumTier),
			FlaskEffectAmount.CODEC.listOf().fieldOf("output_effect").forGetter(FlaskTransformRecipe::outputEffectList),
			MobEffect.CODEC.listOf().fieldOf("input_effect").forGetter(FlaskTransformRecipe::inputEffectList))
			.apply(builder, FlaskTransformRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FlaskTransformRecipe> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), FlaskTransformRecipe::inputs,
			ByteBufCodecs.INT, FlaskTransformRecipe::syphon,
			ByteBufCodecs.INT, FlaskTransformRecipe::ticks,
			ByteBufCodecs.INT, FlaskTransformRecipe::minimumTier,
			FlaskEffectAmount.STREAM_CODEC.apply(ByteBufCodecs.list()), FlaskTransformRecipe::outputEffectList,
			ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT).apply(ByteBufCodecs.list()), FlaskTransformRecipe::inputEffectList,
			FlaskTransformRecipe::new);

	@Override
	public MapCodec<FlaskTransformRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, FlaskTransformRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}
}
