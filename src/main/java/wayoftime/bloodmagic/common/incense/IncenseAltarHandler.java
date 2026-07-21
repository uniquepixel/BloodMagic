package wayoftime.bloodmagic.common.incense;

/**
 * Port of 1.20.1's {@code IncenseAltarHandler}: converts an Incense Altar's summed tranquility
 * into the LP-gain bonus multiplier applied to nearby self-sacrifice (see
 * {@link wayoftime.bloodmagic.common.blockentity.IncenseAltarTile}), via the same piecewise-linear
 * tier curve the original used.
 * <p>
 * Not ported: the original also gated this bonus by two additional factors that don't exist on
 * this branch - {@code IncenseAltarComponent}/{@code getMaxIncenseBonusFromComponents} (physical
 * altar-tier structures registered per addon) and {@code getMaxIncenseBonusFromRoads}/
 * {@code roadsRequired} (a road-construction-quality system driven by {@code IIncensePath}, which
 * required literally paving a path out from the altar with special road blocks before ANY
 * tranquility from the surroundings would even be scanned - see 1.20.1's
 * {@code TileIncenseAltar#recheckConstruction}). Since no road-path blocks or altar-tier components
 * exist on this branch, the bonus here is driven purely by tranquility - functionally equivalent to
 * the original with roads/components always maxed out, and it's what lets a garden of flowers,
 * water and crops actually do something without first porting the entire road-block subsystem.
 */
public class IncenseAltarHandler {
    // Incense bonus maximum applied for the tier of blocks.
    public static final double[] INCENSE_BONUSES = new double[] { 0.2, 0.6, 1.2, 2, 3, 4.5 };
    public static final double[] TRANQUILITY_REQUIRED = new double[] { 0, 6, 14.14, 28, 44.09, 83.14 };

    public static double getIncenseBonusFromTranquility(double tranquility) {
        double possibleBonus = 0;

        for (int i = 0; i < INCENSE_BONUSES.length; i++) {
            if (tranquility >= TRANQUILITY_REQUIRED[i]) {
                possibleBonus = INCENSE_BONUSES[i];
            } else if (i >= 1) {
                possibleBonus += (INCENSE_BONUSES[i] - possibleBonus) * (tranquility - TRANQUILITY_REQUIRED[i - 1]) / (TRANQUILITY_REQUIRED[i] - TRANQUILITY_REQUIRED[i - 1]);
                break;
            }
        }

        return possibleBonus;
    }
}
