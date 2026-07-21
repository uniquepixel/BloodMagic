package wayoftime.bloodmagic.common.will;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.datamap.BMDataMaps;
import wayoftime.bloodmagic.common.item.SoulGemItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for the Demon Will "soul economy": a player's total Will is simply whatever their Soul
 * Gems (Tartaric Gems) currently hold - there's no separate world-side pool for this part of the
 * system (that's the ambient chunk-based aura, a different, not-yet-ported mechanic).
 */
public class WillHelper {
    public static List<ItemStack> allSlots(Player player) {
        List<ItemStack> list = new ArrayList<>();
        list.addAll(player.getInventory().items);
        list.addAll(player.getInventory().armor);
        list.addAll(player.getInventory().offhand);
        return list;
    }

    public static double getTotalWill(EnumWillType type, Player player) {
        double total = 0;
        for (ItemStack stack : allSlots(player)) {
            total += getWillOf(stack, type);
        }
        return total;
    }

    /**
     * The Will type with the most Will currently held by the player (across all Soul Gems/raw
     * Will stacks), ignoring {@link EnumWillType#DEFAULT}. Used by the Sentient tools as the
     * "currently bound Will type" they scale off of - the same source of truth
     * {@link #getTotalWill} already reads from, mirroring 1.20.1's
     * {@code PlayerDemonWillHandler#getLargestWillType}. Returns {@code DEFAULT} if the player
     * isn't carrying any typed Will.
     */
    public static EnumWillType getLargestWillType(Player player) {
        EnumWillType best = EnumWillType.DEFAULT;
        double max = 0;
        for (EnumWillType type : EnumWillType.values()) {
            if (type == EnumWillType.DEFAULT) {
                continue;
            }
            double amount = getTotalWill(type, player);
            if (amount > max) {
                max = amount;
                best = type;
            }
        }
        return best;
    }

    /**
     * Drains up to {@code amount} of the given Will type from the player's Soul Gems/raw Will
     * stacks (whichever are found first), matching 1.20.1's
     * {@code PlayerDemonWillHandler#consumeDemonWill}. Returns the amount actually consumed.
     */
    public static double consumeWill(Player player, EnumWillType type, double amount) {
        double remaining = amount;
        for (ItemStack stack : allSlots(player)) {
            if (remaining <= 0) {
                break;
            }
            if (!stack.has(BMDataComponents.DEMON_WILL_TYPE) || !stack.has(BMDataComponents.DEMON_WILL_AMOUNT)) {
                continue;
            }
            if (stack.get(BMDataComponents.DEMON_WILL_TYPE) != type) {
                continue;
            }
            double current = stack.getOrDefault(BMDataComponents.DEMON_WILL_AMOUNT, 0D);
            double take = Math.min(current, remaining);
            if (take <= 0) {
                continue;
            }
            stack.set(BMDataComponents.DEMON_WILL_AMOUNT, current - take);
            remaining -= take;
        }
        return amount - remaining;
    }

    private static double getWillOf(ItemStack stack, EnumWillType type) {
        if (!stack.has(BMDataComponents.DEMON_WILL_TYPE) || !stack.has(BMDataComponents.DEMON_WILL_AMOUNT)) {
            return 0;
        }
        if (stack.get(BMDataComponents.DEMON_WILL_TYPE) != type) {
            return 0;
        }
        return stack.getOrDefault(BMDataComponents.DEMON_WILL_AMOUNT, 0D);
    }

    /**
     * Fills any Soul Gems in the player's inventory (other than {@code ignored}, if given) with
     * Will of the given type, up to each gem's capacity. Returns the amount actually absorbed.
     */
    public static double fillOtherGems(Player player, ItemStack ignored, EnumWillType type, double amount) {
        double remaining = amount;
        for (ItemStack stack : allSlots(player)) {
            if (remaining <= 0) {
                break;
            }
            if (stack == ignored || !(stack.getItem() instanceof SoulGemItem)) {
                continue;
            }
            remaining -= fillGem(stack, type, remaining);
        }
        return amount - remaining;
    }

    private static double fillGem(ItemStack gemStack, EnumWillType type, double amount) {
        Double max = gemStack.getItemHolder().getData(BMDataMaps.TARTARIC_GEM_MAX_AMOUNTS);
        if (max == null) {
            return 0;
        }

        double current = gemStack.getOrDefault(BMDataComponents.DEMON_WILL_AMOUNT, 0D);
        EnumWillType currentType = gemStack.getOrDefault(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.DEFAULT);
        if (current > 0 && currentType != type) {
            return 0;
        }

        double space = max - current;
        double toAdd = Math.min(space, amount);
        if (toAdd <= 0) {
            return 0;
        }

        gemStack.set(BMDataComponents.DEMON_WILL_TYPE, type);
        gemStack.set(BMDataComponents.DEMON_WILL_AMOUNT, current + toAdd);
        return toAdd;
    }
}
