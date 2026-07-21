package wayoftime.bloodmagic.common.datacomponent;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * The full list of {@link FlaskEffectEntry} slots stored on an Alchemy Flask ItemStack, i.e. the
 * modern replacement for 1.20.1's dual {@code "effectholder"}/{@code "CustomPotionEffects"} NBT
 * tags. This is the sole persistent source of truth for a flask's contents - unlike the old branch,
 * nothing here mirrors into vanilla's {@code DataComponents.POTION_CONTENTS}; tooltip/drink/throw
 * code all compute {@link net.minecraft.world.effect.MobEffectInstance}s on demand from this list
 * (see {@code AlchemyFlaskItem#getEffectiveMobEffects}), which sidesteps any risk of a cached
 * component going stale relative to the flask's current item variant (e.g. after a
 * {@code FlaskItemTransformRecipe} swaps a base flask for its Lingering variant, the 25% duration
 * modifier is picked up correctly because it's read fresh from the item every time).
 */
public record FlaskEffects(List<FlaskEffectEntry> entries)
{
	public static final FlaskEffects EMPTY = new FlaskEffects(List.of());

	public static final Codec<FlaskEffects> CODEC = FlaskEffectEntry.CODEC.listOf()
			.xmap(FlaskEffects::new, FlaskEffects::entries);

	public static final StreamCodec<RegistryFriendlyByteBuf, FlaskEffects> STREAM_CODEC = FlaskEffectEntry.STREAM_CODEC
			.apply(ByteBufCodecs.list())
			.map(FlaskEffects::new, FlaskEffects::entries);

	public boolean isEmpty()
	{
		return entries.isEmpty();
	}

	public int size()
	{
		return entries.size();
	}

	public int indexOfEffect(Holder<MobEffect> effect)
	{
		for (int i = 0; i < entries.size(); i++)
		{
			if (entries.get(i).effect().is(effect))
			{
				return i;
			}
		}
		return -1;
	}

	public FlaskEffects withReplacedAt(int index, FlaskEffectEntry replacement)
	{
		List<FlaskEffectEntry> copy = new ArrayList<>(entries);
		copy.set(index, replacement);
		return new FlaskEffects(copy);
	}

	public FlaskEffects withAdded(FlaskEffectEntry entry)
	{
		List<FlaskEffectEntry> copy = new ArrayList<>(entries);
		copy.add(entry);
		return new FlaskEffects(copy);
	}

	public FlaskEffects trimmedTo(int maxEntries)
	{
		return new FlaskEffects(new ArrayList<>(entries.subList(0, Math.min(entries.size(), maxEntries))));
	}

	/** Mirrors old {@code RecipePotionCycle}: rotates the effect list left by {@code numCycles}. */
	public FlaskEffects rotatedLeft(int numCycles)
	{
		if (entries.isEmpty())
		{
			return this;
		}
		List<FlaskEffectEntry> copy = new ArrayList<>(entries);
		for (int i = 0; i < numCycles; i++)
		{
			copy.add(copy.remove(0));
		}
		return new FlaskEffects(copy);
	}
}
