package wayoftime.bloodmagic.common.item.routing;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Ported from 1.20.1: matches if the test stack carries ANY of a list of tags. */
public class CollectionTagFilterKey implements IFilterKey {
    private final List<TagKey<Item>> itemTags;
    private int count;

    public CollectionTagFilterKey(List<TagKey<Item>> tagList, int count) {
        this.itemTags = tagList;
        this.count = count;
    }

    @Override
    public boolean doesStackMatch(ItemStack testStack) {
        for (TagKey<Item> tag : itemTags) {
            if (testStack.is(tag)) {
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
