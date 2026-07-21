package wayoftime.bloodmagic.common.item.routing;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A leaner replacement for 1.20.1's {@code IItemFilterProvider}: implemented by a Filter item (see
 * {@code wayoftime.bloodmagic.common.item.filter.AbstractFilterItem} and its subclasses) to say
 * what ItemStacks it matches. The original's per-button texture-position/tooltip-hover state
 * machine (used to drive its ghost-slot GUI's button rendering) was not carried over - this port's
 * {@code FilterMenu}/{@code FilterScreen} instead use vanilla container buttons/ghost slots
 * directly - but the actual matching capability (whitelist/blacklist, exact item/tag/mod
 * id/enchantment, and AND/OR composition of other filters) is fully restored.
 * <p>
 * An empty filter (no reference items configured yet) always matches everything, mirroring both
 * the 1.20.1 original and this branch's pre-existing simple routing-node whitelist.
 */
public interface IFilterProvider {
    /**
     * Builds the list of {@link IFilterKey}s this filter stack currently represents, one per
     * configured reference item (or, for a composite filter, one {@link CompositeFilterKey} per
     * nested filter).
     */
    List<IFilterKey> getFilterKeys(ItemStack filterStack);

    /** Whitelist (false, default) matches only configured keys; blacklist (true) matches everything else. */
    boolean isBlacklist(ItemStack filterStack);

    default boolean matches(ItemStack filterStack, ItemStack testStack) {
        if (testStack.isEmpty()) {
            return false;
        }

        List<IFilterKey> keys = getFilterKeys(filterStack);
        if (keys.isEmpty()) {
            return true;
        }

        boolean anyMatch = false;
        for (IFilterKey key : keys) {
            if (key.doesStackMatch(testStack)) {
                anyMatch = true;
                break;
            }
        }

        return isBlacklist(filterStack) != anyMatch;
    }
}
