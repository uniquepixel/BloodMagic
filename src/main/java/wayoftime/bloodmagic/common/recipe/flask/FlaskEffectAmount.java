package wayoftime.bloodmagic.common.recipe.flask;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;

/** A single {@code {"effect": ..., "base_duration": ...}} entry in a {@link FlaskTransformRecipe}'s output list. */
public record FlaskEffectAmount(Holder<MobEffect> effect, int baseDuration)
{
	public static final Codec<FlaskEffectAmount> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			MobEffect.CODEC.fieldOf("effect").forGetter(FlaskEffectAmount::effect),
			Codec.INT.fieldOf("base_duration").forGetter(FlaskEffectAmount::baseDuration))
			.apply(instance, FlaskEffectAmount::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FlaskEffectAmount> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT), FlaskEffectAmount::effect,
			ByteBufCodecs.VAR_INT, FlaskEffectAmount::baseDuration,
			FlaskEffectAmount::new);
}
