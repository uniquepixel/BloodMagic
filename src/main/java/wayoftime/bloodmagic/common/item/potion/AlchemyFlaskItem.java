package wayoftime.bloodmagic.common.item.potion;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.FlaskEffectEntry;
import wayoftime.bloodmagic.common.datacomponent.FlaskEffects;

import java.util.ArrayList;
import java.util.List;

/**
 * Full-fidelity port of 1.20.1's {@code ItemAlchemyFlask}: a reusable (durability-gauged, not
 * breakable) drinkable flask whose potion effect is set/upgraded entirely through Alchemy Table
 * recipes ({@code common/recipe/flask/*}), not through crafting-grid ingredients. The old branch
 * stored its contents as a dual NBT pair ({@code "effectholder"} as source of truth, mirrored into
 * vanilla's {@code "CustomPotionEffects"} purely so vanilla tooltip/color code kept working); this
 * branch collapses that into a single {@link FlaskEffects} data component and computes vanilla
 * {@link MobEffectInstance}s on demand wherever they're actually needed (drink, throw, tooltip,
 * tint) - see {@link #getEffectiveMobEffects(ItemStack)}.
 */
public class AlchemyFlaskItem extends Item
{
	public AlchemyFlaskItem()
	{
		super(new Item.Properties().stacksTo(1).durability(8));
	}

	public static FlaskEffects getFlaskEffects(ItemStack stack)
	{
		return stack.getOrDefault(BMDataComponents.FLASK_EFFECTS, FlaskEffects.EMPTY);
	}

	public static void setFlaskEffects(ItemStack stack, FlaskEffects effects)
	{
		if (effects.isEmpty())
		{
			stack.remove(BMDataComponents.FLASK_EFFECTS);
		}
		else
		{
			stack.set(BMDataComponents.FLASK_EFFECTS, effects);
		}
	}

	/** 1.0 normally; overridden to 0.25 by the Lingering variant, matching vanilla's lingering potion. */
	public double getDurationModifier(ItemStack stack)
	{
		return 1.0;
	}

	public List<MobEffectInstance> getEffectiveMobEffects(ItemStack stack)
	{
		double modifier = getDurationModifier(stack);
		List<MobEffectInstance> list = new ArrayList<>();
		for (FlaskEffectEntry entry : getFlaskEffects(stack).entries())
		{
			list.add(entry.toMobEffectInstance(modifier));
		}
		return list;
	}

	public int getRemainingUses(ItemStack stack)
	{
		return stack.getMaxDamage() - stack.getDamageValue();
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack)
	{
		return UseAnim.DRINK;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity)
	{
		return 32;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if (getRemainingUses(stack) <= 0)
		{
			return InteractionResultHolder.pass(stack);
		}
		return ItemUtils.startUsingInstantly(level, player, hand);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity)
	{
		Player player = livingEntity instanceof Player p ? p : null;
		if (player instanceof ServerPlayer serverPlayer)
		{
			CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
		}

		if (!level.isClientSide)
		{
			for (MobEffectInstance instance : getEffectiveMobEffects(stack))
			{
				if (instance.getEffect().value().isInstantenous())
				{
					instance.getEffect().value().applyInstantenousEffect(player, player, livingEntity, instance.getAmplifier(), 1.0);
				}
				else
				{
					livingEntity.addEffect(instance);
				}
			}
		}

		if (player != null)
		{
			player.awardStat(Stats.ITEM_USED.get(this));
			// The flask's durability is a "remaining uses" gauge that gets reset to 0 by the
			// flask_fill Alchemy Table recipes, not a tool durability that destroys the item when
			// exhausted - so damage is hand-incremented instead of going through
			// ItemStack#hurtAndBreak, which would shrink/delete the stack at max damage.
			if (!player.getAbilities().instabuild)
			{
				stack.setDamageValue(Math.min(stack.getDamageValue() + 1, stack.getMaxDamage()));
			}
		}

		livingEntity.gameEvent(GameEvent.DRINK);
		return stack;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag)
	{
		tooltip.add(Component.translatable("tooltip.bloodmagic.arctool.uses", getRemainingUses(stack)).withStyle(ChatFormatting.GOLD));
		PotionContents.addPotionTooltip(getEffectiveMobEffects(stack), tooltip::add, 1.0F, context.tickRate());
	}
}
