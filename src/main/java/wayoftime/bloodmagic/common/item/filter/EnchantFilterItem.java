package wayoftime.bloodmagic.common.item.filter;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import wayoftime.bloodmagic.common.item.routing.CollectionEnchantFilterKey;
import wayoftime.bloodmagic.common.item.routing.IFilterKey;
import wayoftime.bloodmagic.common.item.routing.INestableFilterProvider;
import wayoftime.bloodmagic.common.item.routing.NoEnchantsFilterKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ported from 1.20.1's {@code ItemEnchantFilterCore}: whitelists/blacklists by enchantment. Each
 * reference stack (typically an enchanted book, but any enchanted item works via
 * {@link EnchantmentHelper#getEnchantmentsForCrafting}) matches any stack sharing ANY of its
 * enchantments at any level ("fuzzy" matching) - the original let you pick one specific
 * enchantment+level via cycle buttons; this matches the whole set at once instead (see
 * {@link AbstractFilterItem}'s javadoc). A reference item with no enchantments matches unenchanted
 * stacks instead, mirroring the original's {@code NoEnchantsFilterKey} fallback.
 */
public class EnchantFilterItem extends AbstractFilterItem implements INestableFilterProvider {
    @Override
    public List<IFilterKey> getFilterKeys(ItemStack filterStack) {
        List<IFilterKey> keys = new ArrayList<>();
        for (ItemStack content : getContents(filterStack)) {
            if (content.isEmpty()) {
                continue;
            }

            ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(content);
            if (enchantments.isEmpty()) {
                keys.add(new NoEnchantsFilterKey(1));
                continue;
            }

            Map<Holder<Enchantment>, Integer> enchantMap = new HashMap<>();
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                enchantMap.put(entry.getKey(), entry.getIntValue());
            }
            keys.add(new CollectionEnchantFilterKey(enchantMap, true, false, 1));
        }
        return keys;
    }

    @Override
    protected String descriptionKey() {
        return "tooltip.bloodmagic.enchantfilter.desc";
    }
}
