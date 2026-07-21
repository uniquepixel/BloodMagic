package wayoftime.bloodmagic.api.ritual;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum EnumReaderBoundaries implements StringRepresentable {
    SUCCESS, VOLUME_TOO_LARGE, NOT_WITHIN_BOUNDARIES;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return toString();
    }
}
