package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.api.datacomponent.Binding;
import wayoftime.bloodmagic.api.helper.SoulNetworkHelper;
import wayoftime.bloodmagic.api.soulnetwork.SoulTicket;
import wayoftime.bloodmagic.common.block.AlchemyTableBlock;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.SoulNetwork;
import wayoftime.bloodmagic.common.datacomponent.FlaskEffects;
import wayoftime.bloodmagic.common.datamap.BMDataMaps;
import wayoftime.bloodmagic.common.datamap.BloodOrb;
import wayoftime.bloodmagic.common.item.potion.AlchemyFlaskItem;
import wayoftime.bloodmagic.common.menu.AlchemyTableMenu;
import wayoftime.bloodmagic.common.recipe.BMRecipes;
import wayoftime.bloodmagic.common.recipe.alchemy_table.AlchemyTableInput;
import wayoftime.bloodmagic.common.recipe.alchemy_table.AlchemyTableRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskRecipe;
import wayoftime.bloodmagic.common.recipe.flask.FlaskRecipeInput;
import wayoftime.bloodmagic.util.TablePart;

public class AlchemyTableTile extends BaseTile implements MenuProvider {

    public Direction FACING;
    public TablePart PART;

    private int stackLimit = 0;
    private int errorFlag = 0;
    private int progress = 0;
    public final SimpleContainerData data = new SimpleContainerData(DATA_COUNT) {
        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> stackLimit = value;
                case 1 -> errorFlag = value;
                case 2 -> progress = value;
            }
            setChanged();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), AlchemyTableBlock.UPDATE_ALL);
        }

        @Override
        public int get(int index) {
            return switch(index) {
                case 0 -> stackLimit;
                case 1 -> errorFlag;
                case 2 -> progress;

                default -> 0;
            };
        }
    };

    public static final int INPUT_COUNT = 6;

    public static final int INPUT_SLOT = 0;
    public static final int ORB_SLOT = INPUT_SLOT + INPUT_COUNT;
    public static final int OUTPUT_SLOT = ORB_SLOT + 1;

    public static final int STACK_LIMIT = 0;
    public static final int ERROR_FLAG = STACK_LIMIT + 1;
    public static final int PROGRESS = ERROR_FLAG + 1;

    public static final int SLOT_COUNT = OUTPUT_SLOT + 1;
    public static final int DATA_COUNT = PROGRESS + 1;

    public static final int ERR_ORB = 1;
    public static final int ERR_ESSENCE = 2;

    public ItemStackHandler inv;
    public int work = 0;

    public AlchemyTableTile(BlockPos pos, BlockState state) {
        super(BMTiles.ALCHEMY_TABLE_TYPE.get(), pos, state);
        FACING = state.getValue(AlchemyTableBlock.FACING);
        PART = state.getValue(AlchemyTableBlock.PART);
        if (PART == TablePart.RIGHT) {
            return;
        }

        inv = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public int getSlotLimit(int slot) {
                if (slot < ORB_SLOT) {
                    return data.get(STACK_LIMIT) == 0 ? 64 : 1;
                }

                return super.getSlotLimit(slot);
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if (slot == ORB_SLOT) {
                    return stack.getItemHolder().getData(BMDataMaps.BLOOD_ORB_STATS) != null;
                }

                return slot != OUTPUT_SLOT;
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), AlchemyTableBlock.UPDATE_ALL);
            }
        };
    }

    private RecipeManager.CachedCheck<AlchemyTableInput, AlchemyTableRecipe> recipeCheck = RecipeManager.createCheck(BMRecipes.ALCHEMY_TABLE_TYPE.get());

    public static void tick(Level level, BlockPos pos, BlockState state, AlchemyTableTile table) {
        if (table.PART == TablePart.RIGHT) {
            return;
        }

        AlchemyTableInput input = table.getInput();
        RecipeHolder<AlchemyTableRecipe> recipe = table.recipeCheck.getRecipeFor(input, level).orElse(null);
        if (recipe != null) {
            table.processCraft(level, pos, recipe.value().tier(), recipe.value().essence(), recipe.value().duration(),
                    recipe.value().assemble(input, level.registryAccess()));
            return;
        }

        // No plain (item-only) Alchemy Table recipe matched - fall back to the Potion Flask
        // recipe family, which reads/writes a flask's CURRENT stored effects instead of just
        // matching ingredients (see FlaskRecipe). Mirrors 1.20.1's TileAlchemyTable#tick(): simple
        // recipes are tried first, and only if none match does it look for a flask among the
        // input slots and try flask recipes against the REMAINING (non-flask) reagents.
        int flaskSlot = table.findFlaskSlot();
        if (flaskSlot < 0) {
            table.work = 0;
            return;
        }

        ItemStack flaskStack = table.inv.getStackInSlot(flaskSlot);
        NonNullList<ItemStack> reagents = NonNullList.create();
        for (int i = 0; i < INPUT_COUNT; i++) {
            if (i == flaskSlot) {
                continue;
            }
            ItemStack stack = table.inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                reagents.add(stack);
            }
        }

        FlaskRecipeInput flaskInput = new FlaskRecipeInput(flaskStack, reagents);
        RecipeHolder<FlaskRecipe> flaskRecipe = findBestFlaskRecipe(level, flaskInput);
        if (flaskRecipe == null) {
            table.work = 0;
            return;
        }

        table.processCraft(level, pos, flaskRecipe.value().minimumTier(), flaskRecipe.value().syphon(), flaskRecipe.value().ticks(),
                flaskRecipe.value().assemble(flaskInput, level.registryAccess()));
    }

    /**
     * Unlike {@link #recipeCheck} (a single {@link RecipeManager.CachedCheck} is fine there since any
     * structural match is unambiguous), flask recipes can't just take the first structural match:
     * several flask recipes can share the same reagents while disagreeing on whether they apply (e.g.
     * a length/potency upgrade and a lowest-priority item-transform recipe keyed to the same
     * ingredient) - see {@link FlaskRecipe}'s class javadoc. Mirroring 1.20.1's
     * {@code BloodMagicRecipeRegistrar#getPotionFlaskRecipe}, every recipe whose {@link FlaskRecipe#matches}
     * passes (ingredients AND {@code canModifyFlask}) is considered, and the one with the highest
     * {@link FlaskRecipe#getPriority(FlaskEffects)} wins.
     */
    private static RecipeHolder<FlaskRecipe> findBestFlaskRecipe(Level level, FlaskRecipeInput input) {
        FlaskEffects effects = AlchemyFlaskItem.getFlaskEffects(input.flask());
        RecipeHolder<FlaskRecipe> best = null;
        int bestPriority = Integer.MIN_VALUE;
        for (RecipeHolder<FlaskRecipe> holder : level.getRecipeManager().getAllRecipesFor(BMRecipes.FLASK_TYPE.get())) {
            FlaskRecipe candidate = holder.value();
            if (!candidate.matches(input, level)) {
                continue;
            }
            int priority = candidate.getPriority(effects);
            if (best == null || priority > bestPriority) {
                best = holder;
                bestPriority = priority;
            }
        }
        return best;
    }

    /** First non-empty input slot holding an {@link AlchemyFlaskItem}, or -1 if none (matches
     * 1.20.1's "first flask found wins, in slot order" behaviour). */
    private int findFlaskSlot() {
        for (int i = 0; i < INPUT_COUNT; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof AlchemyFlaskItem) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Shared progress/LP/consume cycle for both the plain Alchemy Table recipes and the Potion Flask
     * recipes - same tier/essence gate, same progress bar (0-90 over {@code duration} ticks), same
     * per-tick input consumption (crafting-remainder item if present, otherwise shrink by 1). Only the
     * source of {@code minTier}/{@code essenceCost}/{@code duration}/{@code output} differs between the
     * two recipe families.
     */
    private void processCraft(Level level, BlockPos pos, int minTier, int essenceCost, int duration, ItemStack output) {
        ItemStack orbStack = inv.getStackInSlot(ORB_SLOT);
        Binding binding = orbStack.getOrDefault(BMDataComponents.BINDING, Binding.EMPTY);
        BloodOrb orb = orbStack.getItemHolder().getData(BMDataMaps.BLOOD_ORB_STATS);
        if (orb == null || orb.tier() < minTier) {
            data.set(ERROR_FLAG, ERR_ORB);
            return;
        }
        if (binding.isEmpty()) {
            data.set(ERROR_FLAG, ERR_ORB);
            return;
        }
        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(binding);
        if (network.getCurrentEssence() < essenceCost) {
            data.set(ERROR_FLAG, ERR_ESSENCE);
            return;
        }

        ItemStack currentOutput = inv.getStackInSlot(OUTPUT_SLOT);
        if (!currentOutput.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(output, currentOutput)) {
                work = 0;
                return;
            }
        }
        if (++work >= duration) {
            if (currentOutput.isEmpty()) {
                inv.setStackInSlot(OUTPUT_SLOT, output);
            } else {
                currentOutput.grow(output.getCount());
            }
            work = 0;
            network.syphon(SoulTicket.block(level, pos, essenceCost));
            for (int i = 0; i < INPUT_COUNT; i++) {
                ItemStack inputStack = inv.getStackInSlot(i);
                if (inputStack.hasCraftingRemainingItem()) {
                    inv.setStackInSlot(i, inputStack.getCraftingRemainingItem());
                } else {
                    inputStack.shrink(1);
                }
                if (inputStack.isEmpty()) {
                    inv.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
            setChanged();
        }

        data.set(PROGRESS, (int) Math.clamp((double) work / (double) duration * 90, 0, 90));
    }

    /** Non-empty input slots only (compacted, gaps removed) - both {@link AlchemyTableRecipe} and
     * {@link FlaskRecipe} ingredient lists are variable-length (not padded to {@link #INPUT_COUNT}),
     * so a raw 1:1 copy of all 6 slots (including empties) would never match anything but a
     * 6-ingredient recipe. */
    public AlchemyTableInput getInput() {
        NonNullList<ItemStack> inputs = NonNullList.create();
        for (int i = 0; i < ORB_SLOT; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }

        return new AlchemyTableInput(inputs);
    }

    public IItemHandler getItemHandler(Direction direction) {
        if (PART == TablePart.RIGHT) {
            BlockEntity be = level.getBlockEntity(getBlockPos().relative(FACING.getCounterClockWise()));
            if (be instanceof AlchemyTableTile tile) {
                return tile.getItemHandler(direction);
            }

            return new ItemStackHandler(0);
        }

        if (direction == null) {
            return inv;
        }

        return switch (direction) {
            case UP -> new RangedWrapper(inv, ORB_SLOT, ORB_SLOT + 1);
            case DOWN -> new RangedWrapper(inv, OUTPUT_SLOT, OUTPUT_SLOT + 1);
            default -> new RangedWrapper(inv, INPUT_SLOT, INPUT_SLOT + INPUT_COUNT);
        };
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        PART = TablePart.valueOf(tag.getString("part").toUpperCase());
        FACING = Direction.valueOf(tag.getString("facing").toUpperCase());
        if (PART == TablePart.RIGHT) {
            return;
        }
        inv.deserializeNBT(registries, tag.getCompound("inv"));
        stackLimit = tag.getInt("stack_limit");
        work = tag.getInt("work");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("part", PART.getSerializedName());
        tag.putString("facing", FACING.getSerializedName());
        if (PART == TablePart.RIGHT) {
            return;
        }
        tag.put("inv", inv.serializeNBT(registries));
        tag.putInt("stack_limit", stackLimit);
        tag.putInt("work", work);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(BMBlocks.ALCHEMY_TABLE.block().get().getDescriptionId());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AlchemyTableMenu(containerId, playerInventory, inv, data);
    }
}
