package wayoftime.bloodmagic.common.recipe.flask;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class FlaskLengthSerializer implements RecipeSerializer<FlaskLengthRecipe>
{
	public static final MapCodec<FlaskLengthRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Ingredient.CODEC_NONEMPTY.listOf().fieldOf("input").forGetter(FlaskLengthRecipe::inputs),
			Codec.INT.fieldOf("syphon").forGetter(FlaskLengthRecipe::syphon),
			Codec.INT.fieldOf("ticks").forGetter(FlaskLengthRecipe::ticks),
			Codec.INT.fieldOf("minimum_tier").forGetter(FlaskLengthRecipe::minimumTier),
			MobEffect.CODEC.fieldOf("effect").forGetter(FlaskLengthRecipe::outputEffect),
			Codec.DOUBLE.fieldOf("length_duration_mod").forGetter(FlaskLengthRecipe::lengthDurationMod))
			.apply(builder, FlaskLengthRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FlaskLengthRecipe> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), FlaskLengthRecipe::inputs,
			ByteBufCodecs.INT, FlaskLengthRecipe::syphon,
			ByteBufCodecs.INT, FlaskLengthRecipe::ticks,
			ByteBufCodecs.INT, FlaskLengthRecipe::minimumTier,
			MobEffect.STREAM_CODEC, FlaskLengthRecipe::outputEffect,
			ByteBufCodecs.DOUBLE, FlaskLengthRecipe::lengthDurationMod,
			FlaskLengthRecipe::new);

	@Override
	public MapCodec<FlaskLengthRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, FlaskLengthRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}
}
