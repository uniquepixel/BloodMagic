package wayoftime.bloodmagic.common.item.potion;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.entity.PotionFlaskEntity;

/**
 * Full-fidelity port of 1.20.1's {@code ItemAlchemyFlaskThrowable}: right-click to throw the flask
 * as a real projectile that splashes its stored effect(s) on impact, matching vanilla's splash
 * potion. Shares the same durability-as-remaining-uses gauge as the base flask - the thrown flask
 * is a fresh copy of the stack (so its stored effects travel with it), while the original stack
 * stays in hand, just down one use.
 */
public class AlchemyFlaskThrowableItem extends AlchemyFlaskItem
{
	@Override
	public UseAnim getUseAnimation(ItemStack stack)
	{
		return UseAnim.NONE;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if (getRemainingUses(stack) <= 0 || getFlaskEffects(stack).isEmpty())
		{
			return InteractionResultHolder.pass(stack);
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPLASH_POTION_THROW,
				SoundSource.PLAYERS, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));

		if (!level.isClientSide)
		{
			PotionFlaskEntity flask = new PotionFlaskEntity(level, player);
			flask.setItem(stack);
			flask.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.5F, 1.0F);
			level.addFreshEntity(flask);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		if (!player.getAbilities().instabuild)
		{
			stack.setDamageValue(Math.min(stack.getDamageValue() + 1, stack.getMaxDamage()));
		}

		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}
}
