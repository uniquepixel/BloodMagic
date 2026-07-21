package wayoftime.bloodmagic.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.will.WillHelper;

import java.util.List;

/**
 * The Sentient Bow: unlike the other five Sentient tools, 1.20.1 never actually shipped a Java
 * class for this - only leftover texture/model assets exist on that branch (per-Will-type icons
 * plus a 3-frame pulling animation per type), suggesting it was planned but abandoned. This is a
 * new implementation in the same spirit as its siblings: it scales bonus arrow damage/velocity off
 * the wielder's currently-dominant Will type/pool (via {@link SentientToolHelper}'s existing
 * tables, at half strength to account for arrows already dealing full damage independently) and
 * drains a little of that Will per shot, applied in
 * {@link wayoftime.bloodmagic.common.event.AnointmentEventHandler#onEntityJoin} alongside the
 * existing Bow Power/Bow Velocity anointment hooks since that's already the hook point this branch
 * uses to modify a freshly-fired arrow.
 * <p>
 * Simplified from the leftover 1.20.1 assets: this branch reuses the plain "handheld" bow model
 * with a single icon per Will type (matching the other Sentient tools) rather than porting the
 * 4-variant x 3-frame pulling animation texture setup - a fifteen-texture custom bow model
 * pipeline for a mechanic that never had a working implementation to match wasn't a good use of
 * time here; the per-type icon still swaps correctly, just without frame-by-frame draw animation.
 */
public class SentientBowItem extends BowItem implements IWillDropWeapon {
    public SentientBowItem() {
        super(new Item.Properties()
                .durability(384)
                .component(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.DEFAULT)
                .component(BMDataComponents.DEMON_WILL_AMOUNT, 0D));
    }

    private void recalculate(ItemStack stack, Player player) {
        EnumWillType type = WillHelper.getLargestWillType(player);
        double will = WillHelper.getTotalWill(type, player);
        if (SentientToolHelper.currentType(stack) != type || SentientToolHelper.currentWill(stack) != will) {
            stack.set(BMDataComponents.DEMON_WILL_TYPE, type);
            stack.set(BMDataComponents.DEMON_WILL_AMOUNT, will);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        recalculate(player.getItemInHand(hand), player);
        return super.use(level, player, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide && entity instanceof Player player && player.getMainHandItem() == stack) {
            recalculate(stack, player);
        }
    }

    @Override
    public ItemStack rollWillDrop(LivingEntity killed, Player attacker, ItemStack weapon) {
        return SentientToolHelper.rollWillDrop(killed, SentientToolHelper.currentType(weapon), SentientToolHelper.currentLevel(weapon));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        EnumWillType type = SentientToolHelper.currentType(stack);
        double will = SentientToolHelper.currentWill(stack);
        int level = SentientToolHelper.currentLevel(stack);
        SentientTooltipHelper.appendSentientTooltip(tooltip, "tooltip.bloodmagic.sentient_bow.desc", type, level, SentientToolHelper.getExtraDamage(type, level, false) * 0.5, null, will);
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
