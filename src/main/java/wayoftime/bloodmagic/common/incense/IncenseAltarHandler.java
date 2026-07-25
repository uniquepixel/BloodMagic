package wayoftime.bloodmagic.common.incense;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Full-fidelity port of 1.20.1's {@code IncenseAltarHandler}: converts an Incense Altar's summed
 * tranquility, road length and (theoretical, see below) altar-tier component structure into the
 * LP-gain bonus multiplier applied to nearby self-sacrifice - see
 * {@link wayoftime.bloodmagic.common.blockentity.IncenseAltarTile#recheckConstruction}.
 * <p>
 * Three factors gate the final bonus, each capping it independently (the applied bonus is the
 * minimum of all three ceilings):
 * <ul>
 * <li>{@code tranquility} - the (square-root-combined) sum of "flavors" of blocks tabulated
 * around the altar (see {@link IncenseTranquilityRegistry}). This is a smooth piecewise-linear
 * ramp between {@link #TRANQUILITY_REQUIRED} tiers.</li>
 * <li>{@code roads} - how many rings of {@link IIncensePath} blocks have been physically laid out
 * from the altar (see {@link #ROADS_REQUIRED}) - a hard step function, not a ramp: the bonus
 * cannot exceed the tier unlocked by the furthest complete ring. With ZERO road blocks, this
 * still permits tier-0 (20%, {@code roadsRequired[0] == 0}) - matching 1.20.1's own design, an
 * Incense Altar sitting completely alone still gives a flat +20% (see the "Tier 1 setup is the
 * Altar itself" line in the guidebook) even though no surrounding terrain is scanned at all
 * without a road ring reaching out to it.</li>
 * <li>{@code components} - registered {@link IncenseAltarComponent} altar-tier structures (see
 * {@link #registerIncenseComponent}). 1.20.1 shipped this hook but never actually populated it
 * (no vanilla or addon code ever calls {@code registerIncenseComponent}), so
 * {@link #getMaxIncenseBonusFromComponents} always reports every tier satisfied in practice - kept
 * here for exact parity/addon-compatibility, not because it does anything.</li>
 * </ul>
 * Because the road-required array only goes up to a road distance of 10 (the furthest any
 * registered path block - Obsidian, level 8 - can reach; see {@code IncenseAltarPathBlock}), the
 * top {@link #INCENSE_BONUSES} tier (4.5 / +450%) is permanently unreachable with the current path
 * block roster, exactly matching 1.20.1 (whose own guidebook only ever documents caps up to
 * Obsidian's 300%, never mentioning a 450% tier) - the extra {@code roadsRequired} entry (12) is
 * vestigial, left over for a higher-tier path block upstream never shipped.
 */
public class IncenseAltarHandler {
    public static final Map<Integer, List<IncenseAltarComponent>> INCENSE_COMPONENT_MAP = new TreeMap<>();

    // Incense bonus maximum applied for the tier of blocks.
    public static final double[] INCENSE_BONUSES = new double[] { 0.2, 0.6, 1.2, 2, 3, 4.5 };
    public static final double[] TRANQUILITY_REQUIRED = new double[] { 0, 6, 14.14, 28, 44.09, 83.14 };
    // Number of successful road rings (currentDistance - 2) needed to unlock each tier. The trailing
    // 12 is unused (only indices 0-5 are ever read, matching INCENSE_BONUSES.length) - vestigial,
    // matching upstream's own "TODO: Change for when the roads are fully implemented" comment.
    public static final int[] ROADS_REQUIRED = new int[] { 0, 1, 4, 6, 8, 10, 12 };

    public static void registerIncenseComponent(int altarLevel, IncenseAltarComponent component) {
        INCENSE_COMPONENT_MAP.computeIfAbsent(altarLevel, k -> new ArrayList<>()).add(component);
    }

    public static void registerIncenseComponent(int altarLevel, BlockPos offsetPos, Block block, BlockState state) {
        registerIncenseComponent(altarLevel, new IncenseAltarComponent(offsetPos, block));
    }

    public static double getMaxIncenseBonusFromComponents(Level world, BlockPos pos) {
        double accumulatedBonus = 0;
        for (int i = 0; i < INCENSE_BONUSES.length; i++) {
            double previousBonus = (i <= 0 ? 0 : INCENSE_BONUSES[i - 1]);
            double nextBonus = INCENSE_BONUSES[i];
            if (!INCENSE_COMPONENT_MAP.containsKey(i)) {
                accumulatedBonus += (nextBonus - previousBonus);
            } else {
                boolean hasAllComponentsThisTier = true;
                for (IncenseAltarComponent component : INCENSE_COMPONENT_MAP.get(i)) {
                    BlockPos offsetPos = pos.offset(component.getOffset(Direction.NORTH));
                    BlockState state = world.getBlockState(offsetPos);
                    Block block = state.getBlock();
                    if (component.doesBlockMatch(block)) {
                        hasAllComponentsThisTier = false;
                    } else {
                        accumulatedBonus += (nextBonus - previousBonus) / INCENSE_COMPONENT_MAP.get(i).size();
                    }
                }

                if (!hasAllComponentsThisTier) {
                    break;
                }
            }
        }

        return accumulatedBonus;
    }

    public static double getMaxIncenseBonusFromRoads(int roads) {
        double previousBonus = 0;
        for (int i = 0; i < INCENSE_BONUSES.length; i++) {
            if (roads >= ROADS_REQUIRED[i]) {
                previousBonus = INCENSE_BONUSES[i];
            } else {
                return previousBonus;
            }
        }

        return previousBonus;
    }

    public static double getIncenseBonusFromComponents(Level world, BlockPos pos, double tranquility, int roads) {
        double maxBonus = Math.min(getMaxIncenseBonusFromComponents(world, pos), getMaxIncenseBonusFromRoads(roads));
        double possibleBonus = 0;

        for (int i = 0; i < INCENSE_BONUSES.length; i++) {
            if (tranquility >= TRANQUILITY_REQUIRED[i]) {
                possibleBonus = INCENSE_BONUSES[i];
            } else if (i >= 1) {
                possibleBonus += (INCENSE_BONUSES[i] - possibleBonus) * (tranquility - TRANQUILITY_REQUIRED[i - 1]) / (TRANQUILITY_REQUIRED[i] - TRANQUILITY_REQUIRED[i - 1]);
                break;
            }
        }

        return Math.min(maxBonus, possibleBonus);
    }
}
