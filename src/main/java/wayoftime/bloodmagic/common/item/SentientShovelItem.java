package wayoftime.bloodmagic.common.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.will.WillHelper;

import java.util.List;

/**
 * Full-fidelity port of 1.20.1's {@code ItemSentientShovel} - see {@link SentientSwordItem} for
 * the overall design notes shared by all five Sentient tools.
 */
public class SentientShovelItem extends ShovelItem implements IWillDropWeapon {
    private static final double BASE_ATTACK_DAMAGE = 3;
    private static final double BASE_ATTACK_SPEED = -2.8;

    public SentientShovelItem() {
        super(BMItemTier.SENTIENT, new Item.Properties()
                .durability(520)
                .component(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.DEFAULT)
                .component(BMDataComponents.DEMON_WILL_AMOUNT, 0D)
                .component(DataComponents.ATTRIBUTE_MODIFIERS, attributesFor(EnumWillType.DEFAULT, -1)));
    }

    private static ItemAttributeModifiers attributesFor(EnumWillType type, int level) {
        double extraDamage = SentientToolHelper.getExtraDamage(type, level, false);
        double attackSpeed = SentientToolHelper.getAttackSpeed(type, level, false, BASE_ATTACK_SPEED);
        double damage = BASE_ATTACK_DAMAGE + extraDamage - BMItemTier.SENTIENT.getAttackDamageBonus();
        return DiggerItem.createAttributes(BMItemTier.SENTIENT, (float) damage, (float) attackSpeed);
    }

    private void recalculate(ItemStack stack, Player player) {
        EnumWillType type = WillHelper.getLargestWillType(player);
        double will = WillHelper.getTotalWill(type, player);
        if (SentientToolHelper.currentType(stack) == type && SentientToolHelper.currentWill(stack) == will) {
            return;
        }
        stack.set(BMDataComponents.DEMON_WILL_TYPE, type);
        stack.set(BMDataComponents.DEMON_WILL_AMOUNT, will);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attributesFor(type, SentientToolHelper.getLevel(will)));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float value = super.getDestroySpeed(stack, state);
        if (value > 1) {
            return (float) (value + SentientToolHelper.getDigSpeedBonus(SentientToolHelper.currentLevel(stack)));
        }
        return value;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (result && attacker instanceof Player player) {
            recalculate(stack, player);
            SentientToolHelper.applyOnHitEffect(SentientToolHelper.currentType(stack), SentientToolHelper.currentLevel(stack), target, attacker);
        }
        return result;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        recalculate(stack, player);
        EnumWillType type = SentientToolHelper.currentType(stack);
        double drain = SentientToolHelper.getDrainPerSwing(SentientToolHelper.currentLevel(stack));
        if (drain > 0) {
            if (drain > WillHelper.getTotalWill(type, player)) {
                return false;
            }
            WillHelper.consumeWill(player, type, drain);
        }
        return super.onLeftClickEntity(stack, player, entity);
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
        SentientTooltipHelper.appendSentientTooltip(tooltip, "tooltip.bloodmagic.sentient_shovel.desc", type, level, SentientToolHelper.getExtraDamage(type, level, false), SentientToolHelper.getDigSpeedBonus(level), will);
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
