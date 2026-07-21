package wayoftime.bloodmagic.common.recipe.flask;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class FlaskFillSerializer implements RecipeSerializer<FlaskFillRecipe>
{
	public static final MapCodec<FlaskFillRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Ingredient.CODEC_NONEMPTY.listOf().fieldOf("input").forGetter(FlaskFillRecipe::inputs),
			Codec.INT.fieldOf("syphon").forGetter(FlaskFillRecipe::syphon),
			Codec.INT.fieldOf("ticks").forGetter(FlaskFillRecipe::ticks),
			Codec.INT.fieldOf("minimum_tier").forGetter(FlaskFillRecipe::minimumTier),
			Codec.INT.fieldOf("max_effects").forGetter(FlaskFillRecipe::maxEffects))
			.apply(builder, FlaskFillRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FlaskFillRecipe> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), FlaskFillRecipe::inputs,
			ByteBufCodecs.INT, FlaskFillRecipe::syphon,
			ByteBufCodecs.INT, FlaskFillRecipe::ticks,
			ByteBufCodecs.INT, FlaskFillRecipe::minimumTier,
			ByteBufCodecs.INT, FlaskFillRecipe::maxEffects,
			FlaskFillRecipe::new);

	@Override
	public MapCodec<FlaskFillRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, FlaskFillRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}
}
