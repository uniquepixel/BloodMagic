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

public class FlaskPotencySerializer implements RecipeSerializer<FlaskPotencyRecipe>
{
	public static final MapCodec<FlaskPotencyRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Ingredient.CODEC_NONEMPTY.listOf().fieldOf("input").forGetter(FlaskPotencyRecipe::inputs),
			Codec.INT.fieldOf("syphon").forGetter(FlaskPotencyRecipe::syphon),
			Codec.INT.fieldOf("ticks").forGetter(FlaskPotencyRecipe::ticks),
			Codec.INT.fieldOf("minimum_tier").forGetter(FlaskPotencyRecipe::minimumTier),
			MobEffect.CODEC.fieldOf("effect").forGetter(FlaskPotencyRecipe::outputEffect),
			Codec.INT.fieldOf("amplifier").forGetter(FlaskPotencyRecipe::amplifier),
			Codec.DOUBLE.fieldOf("amp_duration_mod").forGetter(FlaskPotencyRecipe::ampDurationMod))
			.apply(builder, FlaskPotencyRecipe::new));

	// amplifier + ampDurationMod are packed into one transient pair here purely because
	// StreamCodec#composite tops out at 6 field/getter pairs and this recipe has 7 fields; the
	// recipe class itself keeps them as two flat fields (matching the JSON shape 1:1).
	private record PotencyData(int amplifier, double ampDurationMod)
	{
	}

	private static final StreamCodec<RegistryFriendlyByteBuf, PotencyData> POTENCY_DATA_STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, PotencyData::amplifier,
			ByteBufCodecs.DOUBLE, PotencyData::ampDurationMod,
			PotencyData::new);

	public static final StreamCodec<RegistryFriendlyByteBuf, FlaskPotencyRecipe> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), FlaskPotencyRecipe::inputs,
			ByteBufCodecs.INT, FlaskPotencyRecipe::syphon,
			ByteBufCodecs.INT, FlaskPotencyRecipe::ticks,
			ByteBufCodecs.INT, FlaskPotencyRecipe::minimumTier,
			MobEffect.STREAM_CODEC, FlaskPotencyRecipe::outputEffect,
			POTENCY_DATA_STREAM_CODEC, recipe -> new PotencyData(recipe.amplifier(), recipe.ampDurationMod()),
			(inputs, syphon, ticks, minimumTier, effect, potency) -> new FlaskPotencyRecipe(inputs, syphon, ticks, minimumTier, effect, potency.amplifier(), potency.ampDurationMod()));

	@Override
	public MapCodec<FlaskPotencyRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, FlaskPotencyRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}
}
