package wayoftime.bloodmagic.common.recipe.flask;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class FlaskItemTransformSerializer implements RecipeSerializer<FlaskItemTransformRecipe>
{
	public static final MapCodec<FlaskItemTransformRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Ingredient.CODEC_NONEMPTY.listOf().fieldOf("input").forGetter(FlaskItemTransformRecipe::inputs),
			Codec.INT.fieldOf("syphon").forGetter(FlaskItemTransformRecipe::syphon),
			Codec.INT.fieldOf("ticks").forGetter(FlaskItemTransformRecipe::ticks),
			Codec.INT.fieldOf("minimum_tier").forGetter(FlaskItemTransformRecipe::minimumTier),
			ItemStack.CODEC.fieldOf("output").forGetter(FlaskItemTransformRecipe::output))
			.apply(builder, FlaskItemTransformRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FlaskItemTransformRecipe> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), FlaskItemTransformRecipe::inputs,
			ByteBufCodecs.INT, FlaskItemTransformRecipe::syphon,
			ByteBufCodecs.INT, FlaskItemTransformRecipe::ticks,
			ByteBufCodecs.INT, FlaskItemTransformRecipe::minimumTier,
			ItemStack.STREAM_CODEC, FlaskItemTransformRecipe::output,
			FlaskItemTransformRecipe::new);

	@Override
	public MapCodec<FlaskItemTransformRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, FlaskItemTransformRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}
}
