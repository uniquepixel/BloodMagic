package wayoftime.bloodmagic.common.item.routing;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Ported from 1.20.1: matches any stack carrying the given item tag. */
public class TagFilterKey implements IFilterKey {
    private final TagKey<Item> itemTag;
    private int count;

    public TagFilterKey(TagKey<Item> tag, int count) {
        this.itemTag = tag;
        this.count = count;
    }

    @Override
    public boolean doesStackMatch(ItemStack testStack) {
        return testStack.is(itemTag);
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
