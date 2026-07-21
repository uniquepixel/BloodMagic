package wayoftime.bloodmagic.common.datacomponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * The modern-data-component equivalent of 1.20.1's {@code EffectHolder}: a single potion effect
 * "slot" stored on an Alchemy Flask. Unlike a plain vanilla {@link MobEffectInstance}, potency
 * (amplifier) and length (duration) are tracked as independently-upgradeable multipliers on top of
 * a base duration - that's the whole point of the Alchemy Table's potency/length recipes, which
 * bump {@link #amplifier}/{@link #ampDurationMod} or {@link #lengthDurationMod} in isolation without
 * touching the other. The actual applied duration is only computed when the flask is drunk/thrown,
 * via {@link #toMobEffectInstance(double)}.
 */
public record FlaskEffectEntry(Holder<MobEffect> effect, int baseDuration, int amplifier, double ampDurationMod,
		double lengthDurationMod)
{
	public static final Codec<FlaskEffectEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			MobEffect.CODEC.fieldOf("effect").forGetter(FlaskEffectEntry::effect),
			Codec.INT.fieldOf("duration").forGetter(FlaskEffectEntry::baseDuration),
			Codec.INT.fieldOf("amplifier").forGetter(FlaskEffectEntry::amplifier),
			Codec.DOUBLE.fieldOf("amp_duration_mod").forGetter(FlaskEffectEntry::ampDurationMod),
			Codec.DOUBLE.fieldOf("length_duration_mod").forGetter(FlaskEffectEntry::lengthDurationMod))
			.apply(instance, FlaskEffectEntry::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FlaskEffectEntry> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT), FlaskEffectEntry::effect,
			ByteBufCodecs.VAR_INT, FlaskEffectEntry::baseDuration,
			ByteBufCodecs.VAR_INT, FlaskEffectEntry::amplifier,
			ByteBufCodecs.DOUBLE, FlaskEffectEntry::ampDurationMod,
			ByteBufCodecs.DOUBLE, FlaskEffectEntry::lengthDurationMod,
			FlaskEffectEntry::new);

	public FlaskEffectEntry(Holder<MobEffect> effect, int baseDuration)
	{
		this(effect, baseDuration, 0, 1, 1);
	}

	/**
	 * Mirrors old {@code EffectHolder#getEffectInstance}: final applied duration is the base
	 * duration scaled by both the potency and length multipliers, plus whatever external modifier
	 * the holding item applies (1.0 normally, 0.25 for a Lingering flask - see
	 * {@code AlchemyFlaskItem#getDurationModifier}).
	 */
	public MobEffectInstance toMobEffectInstance(double durationModifier)
	{
		int duration = (int) (baseDuration * ampDurationMod * lengthDurationMod * durationModifier);
		return new MobEffectInstance(effect, duration, amplifier, false, true);
	}

	public FlaskEffectEntry withPotency(int amplifier, double ampDurationMod)
	{
		return new FlaskEffectEntry(effect, baseDuration, amplifier, ampDurationMod, lengthDurationMod);
	}

	public FlaskEffectEntry withLengthDurationMod(double lengthDurationMod)
	{
		return new FlaskEffectEntry(effect, baseDuration, amplifier, ampDurationMod, lengthDurationMod);
	}

	public FlaskEffectEntry withBaseDuration(int baseDuration)
	{
		return new FlaskEffectEntry(effect, baseDuration, amplifier, ampDurationMod, lengthDurationMod);
	}
}
