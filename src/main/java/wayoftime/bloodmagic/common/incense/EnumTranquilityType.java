package wayoftime.bloodmagic.common.incense;

/**
 * Full-fidelity port of 1.20.1's {@code EnumTranquilityType}: the "flavor" of tranquility a block
 * contributes to an Incense Altar's scan (plants, crops, trees, earth, water, fire and lava all
 * count separately - see {@link IncenseAltarHandler} for how the flavors are combined).
 */
public enum EnumTranquilityType {
    PLANT,
    CROP,
    TREE,
    EARTHEN,
    WATER,
    FIRE,
    LAVA;

    public static EnumTranquilityType getType(String type) {
        for (EnumTranquilityType t : values()) {
            if (t.name().equalsIgnoreCase(type)) {
                return t;
            }
        }

        return null;
    }
}
