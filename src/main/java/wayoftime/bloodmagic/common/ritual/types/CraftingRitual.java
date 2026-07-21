package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Scoped-down port of 1.20.1's Ritual of Crafting ({@code RitualCrafting}). The original was a
 * three-mode auto-crafting-station emulator: mode 0 read a filter item out of an item frame (via
 * {@code IItemFilterProvider}/{@code ItemRouterFilter}) to drive a vanilla 3x3 crafting match, mode 1
 * fed a Hellfire Forge through the same filter system for Tartaric Forge recipes, and mode 2 did the
 * same for an Alchemy Table - all switched by which Will type was configured, and all leaning on the
 * old item-routing filter framework ({@code IItemFilter}/{@code IFilterKey}) for both ingredient
 * selection and output-count limiting.
 * <p>
 * Per the scope-down agreed for this ritual: only mode 0 (vanilla 3x3 crafting) is ported, and the
 * item-frame/filter-provider indirection is replaced with something simpler but just as real - the
 * recipe pattern is read directly from the first 9 slots of the chest in the recipe range (whatever
 * item sits in each slot names the ingredient type wanted in that grid position), matched against
 * the input chest's inventory by item type, assembled with 1.21's {@link CraftingInput} recipe API
 * (replacing the old {@code TransientCraftingContainer} boilerplate), and the result is pushed to the
 * output chest (or dropped as an {@link ItemEntity} if it's missing/full). Crafting-remainder items
 * (e.g. empty buckets) are still honored via {@link CraftingRecipe#getRemainingItems}. The
 * Hellforge/Alchemy-Table modes, the output-count-limiting filter, and the whole item-filter-provider
 * integration are dropped entirely, as agreed - none of that framework is ported.
 */
public class CraftingRitual extends Ritual {
    public static final String OUTPUT_CHEST_RANGE = "outputRange";
    public static final String INPUT_CHEST_RANGE = "inputRange";
    public static final String RECIPE_CHEST_RANGE = "recipeRange";

    public CraftingRitual() {
        super(RitualRegistry.rl("crafting"), 0, 15000, "ritual.bloodmagic.crafting");
        addBlockRange(OUTPUT_CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));
        addBlockRange(INPUT_CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));
        addBlockRange(RECIPE_CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 2, 0), 1));

        setMaximumVolumeAndDistanceOfRange(OUTPUT_CHEST_RANGE, 1, 7, 7);
        setMaximumVolumeAndDistanceOfRange(INPUT_CHEST_RANGE, 1, 7, 7);
        setMaximumVolumeAndDistanceOfRange(RECIPE_CHEST_RANGE, 1, 7, 7);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();

        if (currentEssence < getRefreshCost()) {
            masterRitualStone.causeNausea();
            return;
        }

        BlockPos pos = masterRitualStone.getMasterBlockPos();

        IItemHandler inputInv = inventoryFor(level, masterRitualStone, pos, INPUT_CHEST_RANGE);
        IItemHandler recipeInv = inventoryFor(level, masterRitualStone, pos, RECIPE_CHEST_RANGE);
        if (inputInv == null || recipeInv == null) {
            return;
        }

        List<ItemStack> pattern = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            pattern.add(i < recipeInv.getSlots() ? recipeInv.getStackInSlot(i) : ItemStack.EMPTY);
        }

        CraftingInput patternInput = CraftingInput.of(3, 3, pattern);
        if (patternInput.isEmpty()) {
            return;
        }

        List<RecipeHolder<CraftingRecipe>> matches = level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, patternInput, level);
        if (matches.isEmpty()) {
            return;
        }

        CraftingRecipe recipe = matches.get(0).value();

        // Pull one matching ingredient (by item type) per non-empty pattern slot from the input
        // inventory, remembering which physical slot it came from for crafting-remainder handling.
        int[] sourceSlot = new int[9];
        Map<Integer, Integer> claimed = new HashMap<>();
        List<ItemStack> craftGrid = new ArrayList<>(9);

        for (int i = 0; i < 9; i++) {
            ItemStack key = pattern.get(i);
            if (key.isEmpty()) {
                craftGrid.add(ItemStack.EMPTY);
                sourceSlot[i] = -1;
                continue;
            }

            int foundSlot = -1;
            for (int invSlot = 0; invSlot < inputInv.getSlots(); invSlot++) {
                ItemStack invStack = inputInv.getStackInSlot(invSlot);
                int already = claimed.getOrDefault(invSlot, 0);
                if (invStack.isEmpty() || invStack.getCount() <= already || !ItemStack.isSameItem(invStack, key)) {
                    continue;
                }

                foundSlot = invSlot;
                claimed.merge(invSlot, 1, Integer::sum);
                craftGrid.add(invStack.copyWithCount(1));
                break;
            }

            if (foundSlot == -1) {
                // Missing an ingredient - nothing has been extracted for real yet, so just bail.
                return;
            }
            sourceSlot[i] = foundSlot;
        }

        CraftingInput actualInput = CraftingInput.of(3, 3, craftGrid);
        if (!recipe.matches(actualInput, level)) {
            return;
        }

        ItemStack result = recipe.assemble(actualInput, level.registryAccess());
        if (result.isEmpty()) {
            return;
        }

        BlockPos outputPos = firstPos(masterRitualStone, pos, OUTPUT_CHEST_RANGE);
        IItemHandler outputInv = outputPos == null ? null : level.getCapability(Capabilities.ItemHandler.BLOCK, outputPos, null);
        if (outputPos == null) {
            outputPos = pos.above(2);
        }

        if (outputInv != null && !ItemHandlerHelper.insertItem(outputInv, result.copy(), true).isEmpty()) {
            // Output can't fully fit - don't consume ingredients for a partial craft.
            return;
        }

        for (Map.Entry<Integer, Integer> entry : claimed.entrySet()) {
            inputInv.extractItem(entry.getKey(), entry.getValue(), false);
        }

        NonNullList<ItemStack> remaining = recipe.getRemainingItems(actualInput);
        for (int i = 0; i < 9; i++) {
            ItemStack rem = remaining.get(i);
            if (rem.isEmpty() || sourceSlot[i] < 0) {
                continue;
            }
            ItemStack leftover = inputInv.insertItem(sourceSlot[i], rem, false);
            if (!leftover.isEmpty()) {
                dropAt(level, outputPos, leftover);
            }
        }

        ItemStack insertRemainder = outputInv == null ? result.copy() : ItemHandlerHelper.insertItem(outputInv, result.copy(), false);
        if (!insertRemainder.isEmpty()) {
            dropAt(level, outputPos, insertRemainder);
        }

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost()));
    }

    private static BlockPos firstPos(IMasterRitualStone masterRitualStone, BlockPos masterPos, String range) {
        List<BlockPos> positions = masterRitualStone.getBlockRange(range).getContainedPositions(masterPos);
        return positions.isEmpty() ? null : positions.get(0);
    }

    private static IItemHandler inventoryFor(Level level, IMasterRitualStone masterRitualStone, BlockPos masterPos, String range) {
        BlockPos target = firstPos(masterRitualStone, masterPos, range);
        return target == null ? null : level.getCapability(Capabilities.ItemHandler.BLOCK, target, null);
    }

    private static void dropAt(Level level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, stack));
    }

    @Override
    public int getRefreshTime() {
        return 4;
    }

    @Override
    public int getRefreshCost() {
        return 10;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addCornerRunes(components, 1, 1, EnumRuneType.EARTH);

        addOffsetRunes(components, 1, 2, -1, EnumRuneType.FIRE);
        addCornerRunes(components, 1, -1, EnumRuneType.FIRE);
        addRune(components, -1, -1, 0, EnumRuneType.EARTH);
        addRune(components, 1, -1, 0, EnumRuneType.EARTH);
        addRune(components, 0, -1, -1, EnumRuneType.EARTH);
        addRune(components, 0, -1, 1, EnumRuneType.WATER);
        addRune(components, -1, 1, 0, EnumRuneType.EARTH);
        addRune(components, 1, 1, 0, EnumRuneType.EARTH);
        addRune(components, 0, 1, -1, EnumRuneType.EARTH);
        addRune(components, 0, 0, 1, EnumRuneType.EARTH);

        addRune(components, 0, 2, -1, EnumRuneType.DUSK);
    }

    @Override
    public Ritual getNewCopy() {
        return new CraftingRitual();
    }
}
