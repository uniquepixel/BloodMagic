package wayoftime.bloodmagic.common.recipe.flask;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class FlaskCycleSerializer implements RecipeSerializer<FlaskCycleRecipe>
{
	public static final MapCodec<FlaskCycleRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Ingredient.CODEC_NONEMPTY.listOf().fieldOf("input").forGetter(FlaskCycleRecipe::inputs),
			Codec.INT.fieldOf("syphon").forGetter(FlaskCycleRecipe::syphon),
			Codec.INT.fieldOf("ticks").forGetter(FlaskCycleRecipe::ticks),
			Codec.INT.fieldOf("minimum_tier").forGetter(FlaskCycleRecipe::minimumTier),
			Codec.INT.fieldOf("count").forGetter(FlaskCycleRecipe::numCycles))
			.apply(builder, FlaskCycleRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FlaskCycleRecipe> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), FlaskCycleRecipe::inputs,
			ByteBufCodecs.INT, FlaskCycleRecipe::syphon,
			ByteBufCodecs.INT, FlaskCycleRecipe::ticks,
			ByteBufCodecs.INT, FlaskCycleRecipe::minimumTier,
			ByteBufCodecs.INT, FlaskCycleRecipe::numCycles,
			FlaskCycleRecipe::new);

	@Override
	public MapCodec<FlaskCycleRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, FlaskCycleRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}
}
