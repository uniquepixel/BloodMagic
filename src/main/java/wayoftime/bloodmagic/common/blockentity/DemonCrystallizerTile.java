package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.will.WorldWillHelper;

/**
 * Full-fidelity-in-spirit port of 1.20.1's Demonic Crystallizer ({@code BlockDemonCrystallizer}/
 * {@code TileDemonCrystallizer}): as long as the block directly above stays empty and this chunk's
 * ambient aura holds at least {@link #WILL_TO_FORM_CRYSTAL} of some Will type, it accumulates
 * formation progress once per tick; once that progress reaches {@link #TOTAL_FORMATION_TIME} it
 * drains the aura and places a {@link CrystalClusterBlock} above itself - exactly mirroring the
 * original's {@code formCrystal}, except that 1.20.1 had 5 separate per-type crystal blocks where
 * this branch has a single {@link CrystalClusterTile} that self-selects its type from the
 * strongest ambient Will at its position (see {@link CrystalClusterTile#onPlacedByWorld}), which is
 * guaranteed to match the type this tile just drained since nothing else changes the aura in
 * between. From there, growth/harvesting is entirely the Crystal Cluster's own job - this block's
 * only responsibility is "planting" one.
 * <p>
 * Not ported: the original's {@code IDemonWillConduit} interface (see {@link DemonCrucibleTile}'s
 * javadoc for why - this branch has no conduit network, only the flat per-chunk aura, which is
 * already exactly what this tile reads growth Will from).
 */
public class DemonCrystallizerTile extends BaseTile {
    private static final double WILL_TO_FORM_CRYSTAL = 99;
    private static final double TOTAL_FORMATION_TIME = 1000;
    private static final double FORMATION_RATE = 1;

    private double internalCounter = 0;

    public DemonCrystallizerTile(BlockPos pos, BlockState state) {
        super(BMTiles.DEMON_CRYSTALLIZER_TYPE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DemonCrystallizerTile tile) {
        if (level.isClientSide) {
            return;
        }

        BlockPos above = pos.above();
        if (!level.isEmptyBlock(above)) {
            return;
        }

        EnumWillType highest = EnumWillType.DEFAULT;
        double best = 0;
        for (EnumWillType candidate : EnumWillType.values()) {
            double amount = WorldWillHelper.getWill(level, pos, candidate);
            if (amount > best) {
                best = amount;
                highest = candidate;
            }
        }

        if (best < WILL_TO_FORM_CRYSTAL) {
            return;
        }

        tile.internalCounter += FORMATION_RATE;
        if (tile.internalCounter < TOTAL_FORMATION_TIME) {
            tile.setChanged();
            return;
        }

        double drained = WorldWillHelper.drainWill(level, pos, highest, WILL_TO_FORM_CRYSTAL);
        if (drained >= WILL_TO_FORM_CRYSTAL) {
            level.setBlockAndUpdate(above, BMBlocks.CRYSTAL_CLUSTER.block().get().defaultBlockState());
            if (level.getBlockEntity(above) instanceof CrystalClusterTile clusterTile) {
                clusterTile.onPlacedByWorld();
            }
            tile.internalCounter = 0;
        }

        tile.setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        internalCounter = tag.getDouble("internalCounter");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("internalCounter", internalCounter);
    }
}
