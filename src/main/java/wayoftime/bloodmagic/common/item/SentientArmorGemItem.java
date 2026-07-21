package wayoftime.bloodmagic.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.will.WillHelper;

import java.util.List;

/**
 * The Sentient Armour Gem. On 1.12 (the last branch with a real implementation - see
 * {@link SentientArmorItem}'s class javadoc) right-clicking this wrapped whatever armor the player
 * was wearing into/out of Sentient Armour; its own icon toggled between an "activated"/"deactivated"
 * variant based on whether the player currently had any Sentient Armour equipped, resolved through
 * a client-only {@code ItemMeshDefinition} that reached into {@code Minecraft.getMinecraft().player}
 * - exactly the kind of live-client-reference tooltip/model hook this session had to strip out of
 * the Sentient tools to stop it crashing dedicated servers.
 * <p>
 * Since the armor pieces are now directly equippable items in their own right (see
 * {@link SentientArmorItem}), the gem's job changes to a plain carried toggle: right-click flips a
 * persisted {@code SENTIENT_ARMOUR_GEM_ACTIVE} marker (the same Unit-presence pattern
 * {@link SigilItem} already uses for its own activatable sigils), and
 * {@link SentientArmorItem#recalculate} checks {@link #hasActiveGem} each tick to decide whether the
 * worn armour's Will-scaled bonuses are currently switched on. The activated/deactivated *texture*
 * swap is driven the same data-driven way the sword/bow variants are - see
 * {@code BMItemModelProvider}/{@code ClientModEventHandler} - rather than a client-only mesh hook.
 */
public class SentientArmorGemItem extends Item {
    public SentientArmorGemItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .component(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.DEFAULT)
                .component(BMDataComponents.DEMON_WILL_AMOUNT, 0D));
    }

    /** Scans the player's inventory (main+armor+offhand) for an activated gem - see WillHelper#allSlots. */
    public static boolean hasActiveGem(Player player) {
        for (ItemStack stack : WillHelper.allSlots(player)) {
            if (stack.getItem() instanceof SentientArmorGemItem && stack.has(BMDataComponents.SENTIENT_ARMOUR_GEM_ACTIVE)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (stack.has(BMDataComponents.SENTIENT_ARMOUR_GEM_ACTIVE)) {
                stack.remove(BMDataComponents.SENTIENT_ARMOUR_GEM_ACTIVE);
            } else {
                stack.set(BMDataComponents.SENTIENT_ARMOUR_GEM_ACTIVE, Unit.INSTANCE);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }

        EnumWillType type = WillHelper.getLargestWillType(player);
        double will = WillHelper.getTotalWill(type, player);
        if (SentientToolHelper.currentType(stack) != type || SentientToolHelper.currentWill(stack) != will) {
            stack.set(BMDataComponents.DEMON_WILL_TYPE, type);
            stack.set(BMDataComponents.DEMON_WILL_AMOUNT, will);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        boolean active = stack.has(BMDataComponents.SENTIENT_ARMOUR_GEM_ACTIVE);
        tooltip.add(Component.translatable("tooltip.bloodmagic.sentient_armour_gem.desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable(active ? "tooltip.bloodmagic.sentient_armour_gem.active" : "tooltip.bloodmagic.sentient_armour_gem.inactive")
                .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
