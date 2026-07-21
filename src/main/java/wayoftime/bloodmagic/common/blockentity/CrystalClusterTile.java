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

        tile.progress += drained;
        if (tile.progress >= WILL_PER_GROWTH_STAGE) {
            tile.progress = 0;
            tile.age++;
            tile.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
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
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("crystalType", type.name());
        tag.putInt("age", age);
        tag.putDouble("progress", progress);
    }
}
