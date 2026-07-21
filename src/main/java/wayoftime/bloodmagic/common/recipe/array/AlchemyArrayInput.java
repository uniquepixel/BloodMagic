package wayoftime.bloodmagic.common.recipe.array;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class AlchemyArrayInput implements RecipeInput {
    private final ItemStack baseStack;
    private final ItemStack addedStack;

    public AlchemyArrayInput(ItemStack baseStack, ItemStack addedStack) {
        this.baseStack = baseStack;
        this.addedStack = addedStack;
    }

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? baseStack : addedStack;
    }

    @Override
    public int size() {
        return 2;
    }
}
