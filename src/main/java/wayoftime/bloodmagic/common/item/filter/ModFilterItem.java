package wayoftime.bloodmagic.common.item.filter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import wayoftime.bloodmagic.common.item.routing.IFilterKey;
import wayoftime.bloodmagic.common.item.routing.INestableFilterProvider;
import wayoftime.bloodmagic.common.item.routing.ModFilterKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Ported from 1.20.1's {@code ItemModFilter}: whitelists/blacklists by the mod (namespace) that
 * registered the reference item.
 */
public class ModFilterItem extends AbstractFilterItem implements INestableFilterProvider {
    @Override
    public List<IFilterKey> getFilterKeys(ItemStack filterStack) {
        List<IFilterKey> keys = new ArrayList<>();
        for (ItemStack content : getContents(filterStack)) {
            if (!content.isEmpty()) {
                String namespace = BuiltInRegistries.ITEM.getKey(content.getItem()).getNamespace();
                keys.add(new ModFilterKey(namespace, 1));
            }
        }
        return keys;
    }

    @Override
    protected String descriptionKey() {
        return "tooltip.bloodmagic.modfilter.desc";
    }
}
