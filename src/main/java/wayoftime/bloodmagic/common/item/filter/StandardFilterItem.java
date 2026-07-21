package wayoftime.bloodmagic.common.item.filter;

import net.minecraft.world.item.ItemStack;
import wayoftime.bloodmagic.common.item.routing.BasicFilterKey;
import wayoftime.bloodmagic.common.item.routing.IFilterKey;
import wayoftime.bloodmagic.common.item.routing.INestableFilterProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Ported from 1.20.1's {@code ItemStandardFilter}: whitelists/blacklists by exact item (each
 * configured reference stack matches any stack of the same {@link net.minecraft.world.item.Item}).
 */
public class StandardFilterItem extends AbstractFilterItem implements INestableFilterProvider {
    @Override
    public List<IFilterKey> getFilterKeys(ItemStack filterStack) {
        List<IFilterKey> keys = new ArrayList<>();
        for (ItemStack content : getContents(filterStack)) {
            if (!content.isEmpty()) {
                keys.add(new BasicFilterKey(content, 1));
            }
        }
        return keys;
    }

    @Override
    protected String descriptionKey() {
        return "tooltip.bloodmagic.basicfilter.desc";
    }
}
