package wayoftime.bloodmagic.common.item.potion;

import net.minecraft.world.item.ItemStack;

/**
 * Full-fidelity port of 1.20.1's {@code ItemAlchemyFlaskLingering}: thrown flask leaves a lingering
 * cloud (see {@code PotionFlaskEntity#isLingering}) instead of splashing instantly, and - matching
 * vanilla's own lingering potion - only applies 25% of the stored duration. Which behavior a thrown
 * flask gets is determined purely by the item class of the stack it's carrying
 * ({@code PotionFlaskEntity#isLingering}), so no separate "prep" hook is needed here.
 */
public class AlchemyFlaskLingeringItem extends AlchemyFlaskThrowableItem
{
	@Override
	public double getDurationModifier(ItemStack stack)
	{
		return 0.25;
	}
}
