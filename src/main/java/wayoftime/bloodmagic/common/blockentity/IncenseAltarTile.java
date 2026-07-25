package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import wayoftime.bloodmagic.common.incense.IIncensePath;
import wayoftime.bloodmagic.common.incense.IncenseAltarHandler;
import wayoftime.bloodmagic.common.incense.IncenseTranquilityRegistry;
import wayoftime.bloodmagic.common.incense.TranquilityStack;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Full-fidelity port of 1.20.1's Incense Altar ({@code TileIncenseAltar}): periodically walks a
 * physical "road" of {@link IIncensePath} blocks outward from the altar ring-by-ring
 * ({@link #recheckConstruction}), tabulates tranquility "flavors" (plants, crops, trees, earth,
 * water, fire, lava - see {@link IncenseTranquilityRegistry}) only within the rings the road
 * actually reaches, combines them via diminishing (square-root) returns, then converts the result
 * (gated by both tranquility AND road length - see {@link IncenseAltarHandler}) into an LP-gain
 * bonus multiplier that builds up on any player standing nearby, up to that bonus's cap. Consumed
 * by {@link wayoftime.bloodmagic.common.ritual.types.FeatheredKnifeRitual} as an LP-gain
 * multiplier via the pre-existing {@code incense} attachment.
 * <p>
 * With zero road blocks placed, the tranquility scan never runs at all (the road-walk fails at its
 * very first ring, distance 2) - but a flat +20% bonus still applies regardless, matching 1.20.1
 * exactly (see {@link IncenseAltarHandler}'s javadoc: tier 0 requires neither tranquility nor road
 * length). Laying down {@code IncenseAltarPathBlock} road rings (wood/stone/worn stone/obsidian -
 * see {@code BMBlocks}) both extends how far the tranquility scan reaches AND raises the bonus
 * cap past that flat 20%.
 */
public class IncenseAltarTile extends BaseTile {
    private static final double RANGE = 5;
    // How far (in blocks, y-axis) one road ring may step up/down from the previous ring while the
    // walk searches for the next ring's height - matches 1.20.1's "next ring may not be more than 5
    // blocks higher/lower than the previous ring" (see the guidebook).
    private static final int MAX_CHECK_RANGE = 5;
    private static final int RECHECK_INTERVAL = 100;

    private double tranquility = 0;
    private double incenseBonus = 0;
    private int roadDistance = 0;

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
            tile.recheckConstruction(level, pos);
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
     * Full-fidelity port of 1.20.1's {@code TileIncenseAltar#recheckConstruction}. Walks the road
     * outward ring by ring, starting at distance 2 (the closest ring checked - two blocks
     * horizontally from the altar): for each ring, searches y-offsets within
     * {@link #MAX_CHECK_RANGE} of the previous ring's height for a full ring of
     * {@link IIncensePath} blocks whose {@code getLevelOfPath} is high enough to reach this
     * distance (level {@code >= currentDistance - 2}). If a ring succeeds, every block from the
     * altar out to (and including) that ring, from the ring's height up to 2 above it, is tabulated
     * for tranquility; the walk then tries the next ring out. The first ring that fails ends the
     * walk (matching upstream's own loop, which has no other exit) - {@code roadDistance} becomes
     * that failing ring's distance minus 2, i.e. the number of successful rings.
     * <p>
     * With zero path blocks anywhere, the very first ring (distance 2) fails immediately, so
     * {@code roadDistance == 0} and nothing is ever scanned for tranquility - the resulting bonus is
     * then whatever {@link IncenseAltarHandler} grants for zero tranquility and zero road (a flat
     * 20%, not zero).
     */
    private void recheckConstruction(Level level, BlockPos pos) {
        int yOffset = 0;
        Map<EnumTranquilityType, Double> newTranquilityMap = new EnumMap<>(EnumTranquilityType.class);
        int newRoadDistance;

        for (int currentDistance = 2; ; currentDistance++) {
            boolean canFormRoad = false;

            search:
            for (int i = -MAX_CHECK_RANGE + yOffset; i <= MAX_CHECK_RANGE + yOffset; i++) {
                BlockPos verticalPos = pos.offset(0, i, 0);

                canFormRoad = true;
                ring:
                for (int index = 0; index < 4; index++) {
                    Direction horizontalFacing = Direction.from2DDataValue(index);
                    BlockPos facingOffsetPos = verticalPos.relative(horizontalFacing, currentDistance);
                    for (int j = -1; j <= 1; j++) {
                        BlockPos offsetPos = facingOffsetPos.relative(horizontalFacing.getClockWise(), j);
                        BlockState state = level.getBlockState(offsetPos);
                        Block block = state.getBlock();
                        if (!(block instanceof IIncensePath path && path.getLevelOfPath(level, offsetPos, state) >= currentDistance - 2)) {
                            canFormRoad = false;
                            break ring;
                        }
                    }
                }

                if (canFormRoad) {
                    yOffset = i;
                    break search;
                }
            }

            if (canFormRoad) {
                for (int i = -currentDistance; i <= currentDistance; i++) {
                    for (int j = -currentDistance; j <= currentDistance; j++) {
                        if (Math.abs(i) != currentDistance && Math.abs(j) != currentDistance) {
                            continue;
                        }

                        for (int y = yOffset; y <= 2 + yOffset; y++) {
                            BlockPos offsetPos = pos.offset(i, y, j);
                            BlockState state = level.getBlockState(offsetPos);
                            Block block = state.getBlock();
                            TranquilityStack stack = IncenseTranquilityRegistry.getTranquilityOfBlock(level, offsetPos, block, state);
                            if (stack != null) {
                                newTranquilityMap.merge(stack.type(), stack.value(), Double::sum);
                            }
                        }
                    }
                }
            } else {
                newRoadDistance = currentDistance - 2;
                break;
            }
        }

        double totalTranquility = 0;
        for (double value : newTranquilityMap.values()) {
            totalTranquility += value;
        }

        if (totalTranquility < 0) {
            return;
        }

        double appliedTranquility = 0;
        for (double value : newTranquilityMap.values()) {
            appliedTranquility += Math.sqrt(value);
        }

        this.tranquility = appliedTranquility;
        this.roadDistance = newRoadDistance;
        this.incenseBonus = IncenseAltarHandler.getIncenseBonusFromComponents(level, pos, appliedTranquility, newRoadDistance);
        setChanged();
    }

    public double getTranquility() {
        return tranquility;
    }

    public double getIncenseBonus() {
        return incenseBonus;
    }

    public int getRoadDistance() {
        return roadDistance;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tranquility = tag.getDouble("tranquility");
        incenseBonus = tag.getDouble("incenseBonus");
        roadDistance = tag.getInt("roadDistance");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("tranquility", tranquility);
        tag.putDouble("incenseBonus", incenseBonus);
        tag.putInt("roadDistance", roadDistance);
    }
}
