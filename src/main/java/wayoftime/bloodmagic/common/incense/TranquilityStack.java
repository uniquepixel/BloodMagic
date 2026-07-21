package wayoftime.bloodmagic.common.incense;

/**
 * Port of 1.20.1's {@code TranquilityStack}: holds the tranquility flavor and value contributed by
 * a single matched block during an Incense Altar scan.
 */
public record TranquilityStack(EnumTranquilityType type, double value) {
}
