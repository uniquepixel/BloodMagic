package wayoftime.bloodmagic.client.render.item;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import wayoftime.bloodmagic.common.item.potion.AlchemyFlaskItem;

import java.util.List;

/**
 * Full-fidelity port of 1.20.1's {@code FlaskColor}: tints the flask's liquid (layer 0) and
 * highlight overlay (layer 2) based on the average color of the flask's stored potion effect(s) -
 * same averaging vanilla potions use, via {@link PotionContents#getColor(Iterable)}. All three
 * flask items (base/throwable/lingering) are an {@link AlchemyFlaskItem}, so both layers are always
 * tinted for all three, matching the old branch's {@code instanceof ItemAlchemyFlask} check. An
 * empty flask falls back to plain water's color, exactly like the old branch's
 * {@code PotionUtils.getColor(Potions.WATER)} fallback.
 * <p>
 * Client-only by necessity ({@link ItemColor} is a client class) - registered from
 * {@code ClientModEventHandler}, never referenced from any {@code common/} class.
 */
public class FlaskColor implements ItemColor
{
	@Override
	public int getColor(ItemStack stack, int tintIndex)
	{
		if (!(stack.getItem() instanceof AlchemyFlaskItem flaskItem))
		{
			return 0xFFFFFF;
		}

		if (tintIndex != 0 && tintIndex != 2)
		{
			return 0xFFFFFF;
		}

		List<MobEffectInstance> effects = flaskItem.getEffectiveMobEffects(stack);
		return effects.isEmpty() ? PotionContents.getColor(new PotionContents(Potions.WATER).getAllEffects()) : PotionContents.getColor(effects);
	}
}
