package wayoftime.bloodmagic.common.block.type;

import net.minecraft.util.StringRepresentable;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.common.block.type.SpecialSealType}.
 */
public enum SpecialSealType implements StringRepresentable {
    STANDARD("standard"),
    MINE_ENTRANCE("mine_entrance"),
    MINE_KEY("mine_key");

    private final String name;

    SpecialSealType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
