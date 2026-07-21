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

public class FlaskEffectSerializer implements RecipeSerializer<FlaskEffectRecipe>
{
	public static final MapCodec<FlaskEffectRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Ingredient.CODEC_NONEMPTY.listOf().fieldOf("input").forGetter(FlaskEffectRecipe::inputs),
			Codec.INT.fieldOf("syphon").forGetter(FlaskEffectRecipe::syphon),
			Codec.INT.fieldOf("ticks").forGetter(FlaskEffectRecipe::ticks),
			Codec.INT.fieldOf("minimum_tier").forGetter(FlaskEffectRecipe::minimumTier),
			MobEffect.CODEC.fieldOf("effect").forGetter(FlaskEffectRecipe::outputEffect),
			Codec.INT.fieldOf("base_duration").forGetter(FlaskEffectRecipe::baseDuration))
			.apply(builder, FlaskEffectRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FlaskEffectRecipe> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), FlaskEffectRecipe::inputs,
			ByteBufCodecs.INT, FlaskEffectRecipe::syphon,
			ByteBufCodecs.INT, FlaskEffectRecipe::ticks,
			ByteBufCodecs.INT, FlaskEffectRecipe::minimumTier,
			MobEffect.STREAM_CODEC, FlaskEffectRecipe::outputEffect,
			ByteBufCodecs.INT, FlaskEffectRecipe::baseDuration,
			FlaskEffectRecipe::new);

	@Override
	public MapCodec<FlaskEffectRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, FlaskEffectRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}
}
