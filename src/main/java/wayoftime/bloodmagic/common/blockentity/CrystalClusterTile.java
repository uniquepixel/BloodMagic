package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.common.will.WorldWillHelper;

/**
 * Full-fidelity-in-spirit port of 1.20.1's Demon Will crystal clusters ({@code BlockDemonCrystal}/
 * {@code TileDemonCrystal}): a placed cluster slowly grows by consuming ambient Will matching its
 * own type (picked from the strongest Will present at the chunk when placed), then can be
 * harvested once mature for a Raw Will item of that type - a renewable, in-place alternative to
 * mining Will directly out of the aura. Not ported: the original's 6-directional wall/ceiling
 * attachment with 42 hand-tuned voxel shapes (this cluster is a simple fixed shape regardless of
 * placement), its dedicated Crystallizer "planting" block, and its player-carried-Will empty-hand
 * harvest shortcut - harvesting here is a plain right-click once mature.
 */
public class CrystalClusterTile extends BaseTile {
    private static final int MAX_AGE = 4;
    private static final double WILL_PER_GROWTH_STAGE = 50;
    private static final double DRAIN_PER_TICK = 0.5;
    private static final double HARVEST_AMOUNT = 25;

    private EnumWillType type = EnumWillType.DEFAULT;
    private int age = 0;
    private double progress = 0;

    // Will Catalyst buffer (see wayoftime.bloodmagic.common.item.CrystalCatalystItem), ported from
    // 1.20.1's TileDemonCrystal#applyCatalyst/injectedWill/speedModifier. Adapted to this tile's
    // simpler growth model (a direct will-drained-per-tick -> progress accumulator, rather than
    // 1.20.1's separate conversion-rate/growth-per-second formula): while injectedWill > 0, the
    // Will drained each tick is multiplied by speedMultiplier before being added to progress, and
    // the buffer is spent down by that same bonus amount - so the boost lasts roughly
    // injectedWill / ((speedMultiplier - 1) * DRAIN_PER_TICK) ticks before reverting to normal
    // speed, the same "temporary boost that depletes as it's used" shape as the original.
    private double injectedWill = 0;
    private double speedMultiplier = 1;

    public CrystalClusterTile(BlockPos pos, BlockState state) {
        super(BMTiles.CRYSTAL_CLUSTER_TYPE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CrystalClusterTile tile) {
        if (level.isClientSide || tile.age >= MAX_AGE || level.getGameTime() % 20 != 0) {
            return;
        }

        double drained = WorldWillHelper.drainWill(level, pos, tile.type, DRAIN_PER_TICK);
        if (drained <= 0) {
            return;
        }

        double gained = drained;
        if (tile.injectedWill > 0 && tile.speedMultiplier > 1) {
            double bonus = drained * (tile.speedMultiplier - 1);
            gained += bonus;
            tile.injectedWill = Math.max(0, tile.injectedWill - bonus);
            if (tile.injectedWill <= 0) {
                tile.speedMultiplier = 1;
            }
        }

        tile.progress += gained;
        if (tile.progress >= WILL_PER_GROWTH_STAGE) {
            tile.progress = 0;
            tile.age++;
            tile.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    /**
     * Ported from 1.20.1's {@code ItemCrystalCatalyst#applyCatalyst}/{@code TileDemonCrystal#applyCatalyst}:
     * only applies if the catalyst's Will type matches this cluster's, and the buffer isn't already
     * full. On success, raises {@link #speedMultiplier} to at least {@code speedMultiplier} and adds
     * {@code addedWill} to the buffer (capped at {@code maxWill}).
     */
    public boolean applyCatalyst(EnumWillType catalystType, double addedWill, double speedMultiplier, double maxWill) {
        if (catalystType != type || injectedWill >= maxWill) {
            return false;
        }

        this.speedMultiplier = Math.max(this.speedMultiplier, speedMultiplier);
        this.injectedWill = Math.min(maxWill, this.injectedWill + addedWill);
        setChanged();
        return true;
    }

    public void onPlacedByWorld() {
        if (level == null) {
            return;
        }

        EnumWillType strongest = EnumWillType.DEFAULT;
        double best = 0;
        for (EnumWillType candidate : EnumWillType.values()) {
            double amount = WorldWillHelper.getWill(level, worldPosition, candidate);
            if (amount > best) {
                best = amount;
                strongest = candidate;
            }
        }

        this.type = strongest;
        setChanged();
    }

    public boolean isMature() {
        return age >= MAX_AGE;
    }

    public void harvest(Player player) {
        if (level == null || level.isClientSide || !isMature()) {
            return;
        }

        ItemStack willStack = new ItemStack(BMItems.RAW_WILL.get());
        willStack.set(BMDataComponents.DEMON_WILL_TYPE, type);
        willStack.set(BMDataComponents.DEMON_WILL_AMOUNT, HARVEST_AMOUNT);

        if (!player.getInventory().add(willStack)) {
            player.drop(willStack, false);
        }

        age = MAX_AGE - 2;
        progress = 0;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public EnumWillType getWillType() {
        return type;
    }

    public int getAge() {
        return age;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        try {
            type = EnumWillType.valueOf(tag.getString("crystalType"));
        } catch (IllegalArgumentException ignored) {
            type = EnumWillType.DEFAULT;
        }
        age = tag.getInt("age");
        progress = tag.getDouble("progress");
        injectedWill = tag.getDouble("injectedWill");
        speedMultiplier = tag.contains("speedMultiplier") ? tag.getDouble("speedMultiplier") : 1;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("crystalType", type.name());
        tag.putInt("age", age);
        tag.putDouble("progress", progress);
        tag.putDouble("injectedWill", injectedWill);
        tag.putDouble("speedMultiplier", speedMultiplier);
    }
}
