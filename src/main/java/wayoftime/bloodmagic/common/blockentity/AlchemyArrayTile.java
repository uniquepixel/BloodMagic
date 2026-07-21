package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import wayoftime.bloodmagic.common.alchemyarray.AlchemyArrayEffect;
import wayoftime.bloodmagic.common.alchemyarray.AlchemyArrayEffects;
import wayoftime.bloodmagic.common.recipe.BMRecipes;
import wayoftime.bloodmagic.common.recipe.array.AlchemyArrayInput;
import wayoftime.bloodmagic.common.recipe.array.AlchemyArrayRecipe;

/**
 * Full-fidelity port of 1.20.1's {@code TileAlchemyArray}. Two input slots (base, then added)
 * get filled one at a time by right-clicking the array with an item; once both are filled and
 * they satisfy an {@code AlchemyArrayRecipe}, an {@link AlchemyArrayEffect} is resolved (see
 * {@link AlchemyArrayEffects}) and ticks every frame via {@link #attemptCraft()} until it
 * reports completion, at which point one of each ingredient is consumed and the array block
 * removes itself. Entities standing on top of the array get routed through the active effect's
 * {@code onEntityCollidedWithBlock} every tick (used by Movement/Updraft/Spike/Bounce).
 * <p>
 * Not restored across a save/load cycle: the live {@link AlchemyArrayEffect} instance itself
 * (only its {@code activeCounter} progress and the raw item slots persist - the effect object is
 * re-derived fresh from whatever recipe currently matches the persisted items). This mirrors
 * 1.20.1 exactly: its {@code deserialize} had the equivalent restoration call commented out, so
 * a freshly loaded array always re-resolves its effect via {@code AlchemyArrayRegistry} the same
 * way {@link #attemptCraft()} does here. For every effect except Day/Night (which additionally
 * cache a "starting time of day" the moment they pass tick 100) this produces identical behavior
 * to an uninterrupted run; Day/Night can shift their target time slightly if the world is
 * unloaded mid-effect, same as upstream.
 */
public class AlchemyArrayTile extends BaseTile {
    public static final int BASE_SLOT = 0;
    public static final int ADDED_SLOT = 1;

    private final ItemStackHandler inv = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public boolean isActive = false;
    public int activeCounter = 0;
    private Direction rotation = Direction.from2DDataValue(0);
    public int rotateCooldown = 0;
    private boolean doDropIngredients = true;

    public AlchemyArrayEffect arrayEffect;

    private final RecipeManager.CachedCheck<AlchemyArrayInput, AlchemyArrayRecipe> recipeCheck = RecipeManager.createCheck(BMRecipes.ALCHEMY_ARRAY_TYPE.get());

    public AlchemyArrayTile(BlockPos pos, BlockState state) {
        super(BMTiles.ALCHEMY_ARRAY_TYPE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AlchemyArrayTile tile) {
        if (tile.isActive && tile.attemptCraft()) {
            tile.activeCounter++;
        } else {
            tile.isActive = false;
            tile.doDropIngredients = true;
            tile.activeCounter = 0;
            tile.arrayEffect = null;
        }

        if (tile.rotateCooldown > 0) {
            tile.rotateCooldown--;
        }
    }

    /**
     * Advances the current (or newly resolved) effect by one tick.
     *
     * @return {@code true} if the array should keep going, {@code false} if it has finished (or
     * failed to find a matching recipe) and should be reset/removed.
     */
    public boolean attemptCraft() {
        if (level == null) {
            return false;
        }

        if (arrayEffect == null) {
            RecipeHolder<AlchemyArrayRecipe> match = findMatchingRecipe();
            if (match == null) {
                return false;
            }
            arrayEffect = AlchemyArrayEffects.getEffect(match);
        }

        isActive = true;
        if (arrayEffect.update(this, activeCounter)) {
            inv.extractItem(BASE_SLOT, 1, false);
            inv.extractItem(ADDED_SLOT, 1, false);
            level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());
            return false;
        }

        return true;
    }

    private RecipeHolder<AlchemyArrayRecipe> findMatchingRecipe() {
        AlchemyArrayInput input = new AlchemyArrayInput(inv.getStackInSlot(BASE_SLOT), inv.getStackInSlot(ADDED_SLOT));
        return recipeCheck.getRecipeFor(input, level).orElse(null);
    }

    public void onEntityCollidedWithBlock(BlockState state, Entity entity) {
        if (arrayEffect != null && level != null) {
            arrayEffect.onEntityCollidedWithBlock(this, level, worldPosition, state, entity);
        }
    }

    public ItemStackHandler getInventory() {
        return inv;
    }

    public Direction getRotation() {
        return rotation;
    }

    public void setRotation(Direction rotation) {
        this.rotation = rotation;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void doDropIngredients(boolean drop) {
        this.doDropIngredients = drop;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inv.deserializeNBT(registries, tag.getCompound("inv"));
        isActive = tag.getBoolean("isActive");
        activeCounter = tag.getInt("activeCounter");
        doDropIngredients = !tag.contains("doDropIngredients") || tag.getBoolean("doDropIngredients");
        rotation = Direction.from2DDataValue(tag.getInt("rotation"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inv", inv.serializeNBT(registries));
        tag.putBoolean("isActive", isActive);
        tag.putInt("activeCounter", activeCounter);
        tag.putBoolean("doDropIngredients", doDropIngredients);
        tag.putInt("rotation", rotation.get2DDataValue());
    }
}
