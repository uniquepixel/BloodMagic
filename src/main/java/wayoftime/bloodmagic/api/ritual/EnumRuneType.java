package wayoftime.bloodmagic.api.ritual;

import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * Rune types for the ritual multiblock pattern - distinct from {@link wayoftime.bloodmagic.api.altar.EnumRuneType},
 * which is for the Blood Altar's own rune system.
 */
public enum EnumRuneType implements StringRepresentable {
    BLANK(ChatFormatting.GRAY),
    WATER(ChatFormatting.AQUA),
    FIRE(ChatFormatting.RED),
    EARTH(ChatFormatting.GREEN),
    AIR(ChatFormatting.WHITE),
    DUSK(ChatFormatting.DARK_GRAY),
    DAWN(ChatFormatting.GOLD);

    public final ChatFormatting colorCode;

    EnumRuneType(ChatFormatting colorCode) {
        this.colorCode = colorCode;
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return this.toString();
    }

    public static EnumRuneType byMetadata(int meta) {
        if (meta < 0 || meta >= values().length) {
            meta = 0;
        }
        return values()[meta];
    }
}
