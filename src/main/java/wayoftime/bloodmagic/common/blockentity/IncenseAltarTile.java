package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.common.dataattachment.BMDataAttachments;
import wayoftime.bloodmagic.common.incense.EnumTranquilityType;
import wayoftime.bloodmagic.common.incense.IncenseAltarHandler;
import wayoftime.bloodmagic.common.incense.IncenseTranquilityRegistry;
import wayoftime.bloodmagic.common.incense.TranquilityStack;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Full-fidelity-in-spirit port of 1.20.1's Incense Altar ({@code TileIncenseAltar}): periodically
 * scans the blocks around the altar for tranquility "flavors" (plants, crops, trees, earth, water,
 * fire, lava - see {@link IncenseTranquilityRegistry}), sums each flavor and combines them via
 * diminishing (square-root) returns, then converts the result into an LP-gain bonus multiplier
 * (see {@link IncenseAltarHandler}) that builds up on any player standing nearby, up to that
 * bonus's cap. Consumed by {@link wayoftime.bloodmagic.common.ritual.types.FeatheredKnifeRitual}
 * as an LP-gain multiplier via the pre-existing {@code incense} attachment.
 * <p>
 * Not ported: the original's "road construction quality" system, which required literally paving a
 * physical path of special road blocks ({@code IIncensePath}) outward from the altar before ANY
 * tranquility scan would even run, and which capped/gated the resulting bonus by how far that road
 * extended plus registered {@code IncenseAltarComponent} altar-tier structures. No road blocks or
 * altar-tier components exist on this branch, so the scan here always runs (over a fixed area
 * around the altar) and the bonus is capped purely by tranquility - see {@link IncenseAltarHandler}
 * for detail. This is what actually makes surrounding the altar with flowers, water and crops do
 * something, without first porting the entire road-block subsystem.
 */
public class IncenseAltarTile extends BaseTile {
    private static final double RANGE = 5;
    private static final int SCAN_RADIUS = 5;
    private static final int SCAN_DOWN = 2;
    private static final int SCAN_UP = 2;
    private static final int RECHECK_INTERVAL = 100;

    private double tranquility = 0;
    private double incenseBonus = 0;

    public IncenseAltarTile(BlockPos pos, BlockState state) {
        super(BMTiles.INCENSE_ALTAR_TYPE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, IncenseAltarTile tile) {
        if (level.isClientSide) {
            return;
        }

        AABB area = new AABB(pos).inflate(RANGE);
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        if (players.isEmpty()) {
            return;
        }

        if (level.getGameTime() % RECHECK_INTERVAL == 0) {
            tile.recheckTranquility(level, pos);
        }

        boolean hasPerformed = false;
        double cap = tile.incenseBonus;
        double increment = cap / 100D;

        for (Player player : players) {
            double current = player.getData(BMDataAttachments.INCENSE);
            if (current < cap) {
                player.setData(BMDataAttachments.INCENSE, Math.min(cap, current + increment));
                hasPerformed = true;
            }
        }

        if (hasPerformed && level instanceof ServerLevel server && level.random.nextInt(4) == 0) {
            server.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 1, 0.02, 0.03, 0.02, 0);
        }
    }

    /**
     * Scans the area around the altar for registered tranquility sources, sums each flavor
     * separately, then combines the per-flavor sums with diminishing (square-root) returns - a
     * garden with one dominant flavor plateaus quickly, while a varied garden of several flavors
     * keeps paying off. Mirrors 1.20.1's {@code TileIncenseAltar#recheckConstruction}.
     */
    private void recheckTranquility(Level level, BlockPos pos) {
        Map<EnumTranquilityType, Double> tranquilityMap = new EnumMap<>(EnumTranquilityType.class);

        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
            for (int y = -SCAN_DOWN; y <= SCAN_UP; y++) {
                for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                    BlockPos scanPos = pos.offset(x, y, z);
                    BlockState scanState = level.getBlockState(scanPos);
                    Block block = scanState.getBlock();

                    TranquilityStack stack = IncenseTranquilityRegistry.getTranquilityOfBlock(level, scanPos, block, scanState);
                    if (stack != null) {
                        tranquilityMap.merge(stack.type(), stack.value(), Double::sum);
                    }
                }
            }
        }

        double appliedTranquility = 0;
        for (double value : tranquilityMap.values()) {
            appliedTranquility += Math.sqrt(value);
        }

        this.tranquility = appliedTranquility;
        this.incenseBonus = IncenseAltarHandler.getIncenseBonusFromTranquility(appliedTranquility);
        setChanged();
    }

    public double getTranquility() {
        return tranquility;
    }

    public double getIncenseBonus() {
        return incenseBonus;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tranquility = tag.getDouble("tranquility");
        incenseBonus = tag.getDouble("incenseBonus");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("tranquility", tranquility);
        tag.putDouble("incenseBonus", incenseBonus);
    }
}
