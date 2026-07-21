package wayoftime.bloodmagic.common.item.routing;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Ported from 1.20.1: matches only stacks that carry no enchantments at all. */
public class NoEnchantsFilterKey implements IFilterKey {
    private int count;

    public NoEnchantsFilterKey(int count) {
        this.count = count;
    }

    @Override
    public boolean doesStackMatch(ItemStack testStack) {
        return EnchantmentHelper.getEnchantmentsForCrafting(testStack).isEmpty();
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
