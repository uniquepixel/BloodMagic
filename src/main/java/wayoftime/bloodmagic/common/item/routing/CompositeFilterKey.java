package wayoftime.bloodmagic.common.item.routing;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Ported from 1.20.1: an OR-combination of other keys, used by {@code CompositeFilterItem} to
 * bundle "everything one nested filter matches" into a single key. Composite filter's own AND/OR
 * toggle then combines these (one per nested filter) at a higher level - see
 * {@code CompositeFilterItem#matches}.
 */
public class CompositeFilterKey implements IFilterKey {
    private final List<IFilterKey> keyList = new ArrayList<>();
    private int count;

    public CompositeFilterKey(int count) {
        this.count = count;
    }

    public void addFilterKey(IFilterKey key) {
        if (!(key instanceof CompositeFilterKey)) {
            keyList.add(key);
        }
    }

    @Override
    public boolean doesStackMatch(ItemStack testStack) {
        if (testStack.isEmpty()) {
            return false;
        }

        for (IFilterKey key : keyList) {
            if (key.doesStackMatch(testStack)) {
                return true;
            }
        }

        return false;
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
