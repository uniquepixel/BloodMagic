package wayoftime.bloodmagic.common.recipe.array;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class AlchemyArraySerializer implements RecipeSerializer<AlchemyArrayRecipe> {
    public static final MapCodec<AlchemyArrayRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("base_input").forGetter(AlchemyArrayRecipe::getBaseInput),
            Ingredient.CODEC_NONEMPTY.fieldOf("added_input").forGetter(AlchemyArrayRecipe::getAddedInput),
            ItemStack.CODEC.fieldOf("output").forGetter(AlchemyArrayRecipe::getOutput),
            ResourceLocation.CODEC.optionalFieldOf("texture", AlchemyArrayRecipe.DEFAULT_TEXTURE).forGetter(AlchemyArrayRecipe::getTexture)
    ).apply(instance, AlchemyArrayRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyArrayRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, AlchemyArrayRecipe::getBaseInput,
            Ingredient.CONTENTS_STREAM_CODEC, AlchemyArrayRecipe::getAddedInput,
            ItemStack.STREAM_CODEC, AlchemyArrayRecipe::getOutput,
            ResourceLocation.STREAM_CODEC, AlchemyArrayRecipe::getTexture,
            AlchemyArrayRecipe::new
    );

    @Override
    public MapCodec<AlchemyArrayRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AlchemyArrayRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
