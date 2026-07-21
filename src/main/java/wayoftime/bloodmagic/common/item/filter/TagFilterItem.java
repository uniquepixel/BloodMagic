package wayoftime.bloodmagic.common.item.filter;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import wayoftime.bloodmagic.common.item.routing.CollectionTagFilterKey;
import wayoftime.bloodmagic.common.item.routing.IFilterKey;
import wayoftime.bloodmagic.common.item.routing.INestableFilterProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Ported from 1.20.1's {@code ItemTagFilter}: whitelists/blacklists by item tag. Each reference
 * stack matches any stack sharing ANY of that reference's item tags - the original let you cycle
 * through a reference item's tags one at a time via a button and match only that one; this matches
 * all of them at once instead (a strict superset, see {@link AbstractFilterItem}'s javadoc).
 */
public class TagFilterItem extends AbstractFilterItem implements INestableFilterProvider {
    @Override
    public List<IFilterKey> getFilterKeys(ItemStack filterStack) {
        List<IFilterKey> keys = new ArrayList<>();
        for (ItemStack content : getContents(filterStack)) {
            if (content.isEmpty()) {
                continue;
            }
            List<TagKey<Item>> tags = content.getTags().toList();
            if (!tags.isEmpty()) {
                keys.add(new CollectionTagFilterKey(tags, 1));
            }
        }
        return keys;
    }

    @Override
    protected String descriptionKey() {
        return "tooltip.bloodmagic.tagfilter.desc";
    }
}
