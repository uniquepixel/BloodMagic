package wayoftime.bloodmagic.common.item.filter;

import net.minecraft.world.item.ItemStack;
import wayoftime.bloodmagic.common.item.routing.CompositeFilterKey;
import wayoftime.bloodmagic.common.item.routing.IFilterKey;
import wayoftime.bloodmagic.common.item.routing.IFilterProvider;
import wayoftime.bloodmagic.common.item.routing.INestableFilterProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Ported from 1.20.1's {@code ItemCompositeFilter}: nests other Filter items (each an
 * {@link INestableFilterProvider}, so a composite can't hold another composite - avoids recursive
 * matching, matching the original's implied design) and combines their results. One
 * {@link CompositeFilterKey} is built per nested filter (OR across that filter's own keys, matching
 * {@code CompositeFilterKey}'s original semantics), and the whitelist/blacklist toggle is reused
 * here as an AND/OR toggle across nested filters (see {@link #matches}):
 * <ul>
 *     <li>OFF (default, "whitelist" slot in storage) - matches if ANY nested filter matches
 *     (this is the mode 1.20.1 didn't have, added here as a natural extension).</li>
 *     <li>ON ("blacklist" slot in storage) - matches only if ALL nested filters match, i.e. the
 *     original's {@code BasicCompositeFilter} AND-across-nested-filters behaviour.</li>
 * </ul>
 */
public class CompositeFilterItem extends AbstractFilterItem {
    @Override
    public boolean isNestedSlots() {
        return true;
    }

    @Override
    public List<IFilterKey> getFilterKeys(ItemStack filterStack) {
        List<IFilterKey> keys = new ArrayList<>();
        for (ItemStack nested : getContents(filterStack)) {
            if (nested.isEmpty() || !(nested.getItem() instanceof INestableFilterProvider provider)) {
                continue;
            }

            CompositeFilterKey key = new CompositeFilterKey(1);
            for (IFilterKey nestedKey : provider.getFilterKeys(nested)) {
                key.addFilterKey(nestedKey);
            }
            keys.add(key);
        }
        return keys;
    }

    @Override
    public boolean matches(ItemStack filterStack, ItemStack testStack) {
        if (testStack.isEmpty()) {
            return false;
        }

        List<IFilterKey> keys = getFilterKeys(filterStack);
        if (keys.isEmpty()) {
            return true;
        }

        boolean requireAll = isBlacklist(filterStack);
        if (requireAll) {
            for (IFilterKey key : keys) {
                if (!key.doesStackMatch(testStack)) {
                    return false;
                }
            }
            return true;
        }

        for (IFilterKey key : keys) {
            if (key.doesStackMatch(testStack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String descriptionKey() {
        return "tooltip.bloodmagic.compositefilter.desc";
    }
}
