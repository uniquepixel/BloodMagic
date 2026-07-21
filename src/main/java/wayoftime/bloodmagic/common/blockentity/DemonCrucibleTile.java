package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.datamap.BMDataMaps;
import wayoftime.bloodmagic.common.will.WorldWillHelper;
import wayoftime.bloodmagic.util.BlockEntityHelper;

/**
 * Full-fidelity-in-spirit port of 1.20.1's Demonic Crucible ({@code BlockDemonCrucible}/
 * {@code TileDemonCrucible}): a single-slot block that exchanges Will between a held Will-bearing
 * item (a Raw Will item or Soul Gem - anything carrying the {@code DEMON_WILL_TYPE}/
 * {@code DEMON_WILL_AMOUNT} data components) and the ambient chunk aura, with the transfer
 * direction flipped by a redstone signal exactly like the original: unpowered (the default state)
 * releases the held item's Will into the world aura, powered instead withdraws ambient Will from
 * the world into the held item.
 * <p>
 * Not ported: the original's {@code IDemonWillConduit} network (a separate, conduit-only Will
 * buffer that other conduit blocks - including the original Crucible/Crystallizer/Pylon
 * themselves - could push/pull between each other independently of the world aura). This branch's
 * {@link WorldWillHelper} backend has no such network concept, only the flat per-chunk ambient
 * pool, so the Crucible (like the Crystallizer and Pylon below) talks to that pool directly - the
 * original's powered branch actually drained its own conduit buffer (fed externally by e.g. a
 * Pylon) rather than the world aura directly, but since nothing in that buffer could ever be
 * topped up without the network this substitutes the world aura as the source, preserving the
 * redstone-toggle UX (and the original unpowered-branch behavior, which already targeted the world
 * aura directly) without requiring the conduit network to exist.
 */
public class DemonCrucibleTile extends BaseTile {
    // 1.20.1's TileDemonCrucible had no data map for item capacity - it only ever dealt with a
    // hard 100-per-type internal buffer. Soul Gems here have a real per-item cap via
    // BMDataMaps.TARTARIC_GEM_MAX_AMOUNTS; Raw Will items don't, so this is their fallback cap.
    private static final double RAW_WILL_ITEM_CAP = 100;
    // Mirrors 1.20.1's TileDemonCrucible#gemDrainRate (10/tick, uncapped ticking); throttled to
    // once/second here to match this branch's slower-paced Will systems (see CrystalClusterTile).
    private static final double TRANSFER_RATE = 5;

    private final ItemStackHandler inv = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.has(BMDataComponents.DEMON_WILL_TYPE) && stack.has(BMDataComponents.DEMON_WILL_AMOUNT);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    };

    public DemonCrucibleTile(BlockPos pos, BlockState state) {
        super(BMTiles.DEMON_CRUCIBLE_TYPE.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inv;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DemonCrucibleTile tile) {
        if (level.isClientSide || level.getGameTime() % 20 != 0) {
            return;
        }

        ItemStack stack = tile.inv.getStackInSlot(0);
        if (stack.isEmpty()) {
            return;
        }

        EnumWillType type = stack.getOrDefault(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.DEFAULT);
        double amount = stack.getOrDefault(BMDataComponents.DEMON_WILL_AMOUNT, 0D);

        if (level.hasNeighborSignal(pos)) {
            // Powered: withdraw ambient Will from this chunk into the held item.
            Double cap = stack.getItemHolder().getData(BMDataMaps.TARTARIC_GEM_MAX_AMOUNTS);
            double max = cap == null ? RAW_WILL_ITEM_CAP : cap;
            double space = max - amount;
            if (space <= 0) {
                return;
            }

            double drained = WorldWillHelper.drainWill(level, pos, type, Math.min(space, TRANSFER_RATE));
            if (drained > 0) {
                stack.set(BMDataComponents.DEMON_WILL_AMOUNT, amount + drained);
                tile.setChanged();
            }
        } else {
            // Unpowered (default): release the held item's Will into this chunk's ambient aura -
            // matches 1.20.1's unpowered branch exactly.
            if (amount <= 0) {
                return;
            }

            double toRelease = Math.min(amount, TRANSFER_RATE);
            WorldWillHelper.addWill(level, pos, type, toRelease);
            stack.set(BMDataComponents.DEMON_WILL_AMOUNT, amount - toRelease);
            tile.setChanged();
        }
    }

    public void dropItems() {
        if (level == null) {
            return;
        }
        BlockEntityHelper.dropContents(level, worldPosition, inv);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inv.deserializeNBT(registries, tag.getCompound("inventory"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inv.serializeNBT(registries));
    }
}
