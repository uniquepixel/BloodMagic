package wayoftime.bloodmagic.common.item.routing;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;

/**
 * Ported from 1.20.1, updated for the modern {@link Holder}&lt;{@link Enchantment}&gt; API (see
 * {@link EnchantFilterKey}'s javadoc). Matches against a whole reference set of enchantments taken
 * from one item (e.g. everything on an enchanted book): either ANY of them present (matchAll=false,
 * used as this port's default "match by enchantment" behaviour) or ALL of them present
 * (matchAll=true), each either fuzzy or at its exact level.
 */
public class CollectionEnchantFilterKey implements IFilterKey {
    private final Map<Holder<Enchantment>, Integer> enchantMap;
    private final boolean isFuzzy;
    private final boolean matchAll;
    private int count;

    public CollectionEnchantFilterKey(Map<Holder<Enchantment>, Integer> enchantMap, boolean isFuzzy, boolean matchAll, int count) {
        this.enchantMap = enchantMap;
        this.isFuzzy = isFuzzy;
        this.matchAll = matchAll;
        this.count = count;
    }

    @Override
    public boolean doesStackMatch(ItemStack testStack) {
        var testEnchants = net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentsForCrafting(testStack);

        for (Map.Entry<Holder<Enchantment>, Integer> entry : enchantMap.entrySet()) {
            int level = testEnchants.getLevel(entry.getKey());
            boolean matchedThis = isFuzzy ? level > 0 : level == entry.getValue();

            if (matchedThis) {
                if (!matchAll) {
                    return true;
                }
            } else if (matchAll) {
                return false;
            }
        }

        return matchAll;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public void shrink(int changeAmount) {
        this.count -= changeAmount;
    }

    @Override
    public void grow(int changeAmount) {
        this.count += changeAmount;
    }

    @Override
    public boolean isEmpty() {
        return count == 0;
    }
}
