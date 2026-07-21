package wayoftime.bloodmagic.common.item.routing;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Ported from 1.20.1, updated for 1.21.1's data-driven enchantments ({@link Holder}&lt;{@link Enchantment}&gt;
 * instead of a plain object, and {@link EnchantmentHelper#getEnchantmentsForCrafting(ItemStack)}
 * instead of the old {@code EnchantmentHelper.getEnchantments(stack)}/special-cased enchanted-book
 * check - the modern helper already reads the correct data component for either case).
 * Matches a single enchantment, either at an exact level or "fuzzy" (any level greater than zero).
 */
public class EnchantFilterKey implements IFilterKey {
    private final Holder<Enchantment> enchantment;
    private final int enchantLevel;
    private final boolean isFuzzy;
    private int count;

    public EnchantFilterKey(Holder<Enchantment> enchantment, int enchantLevel, boolean isFuzzy, int count) {
        this.enchantment = enchantment;
        this.enchantLevel = enchantLevel;
        this.isFuzzy = isFuzzy;
        this.count = count;
    }

    @Override
    public boolean doesStackMatch(ItemStack testStack) {
        int level = EnchantmentHelper.getEnchantmentsForCrafting(testStack).getLevel(enchantment);
        return isFuzzy ? level > 0 : level == enchantLevel;
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
