package wayoftime.bloodmagic.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;

import java.util.List;
import java.util.Locale;

/**
 * Client-side tooltip renderer shared by all Sentient tools, ported from 1.20.1's
 * {@code SentientTooltipHelper} - same layout (flavour text, attuned type, shift-to-expand level
 * and per-type rider info), rebased onto this branch's {@link EnumWillType}/{@link WillHelper}.
 */
public final class SentientTooltipHelper {
    private SentientTooltipHelper() {
    }

    public static ChatFormatting colorFor(EnumWillType type) {
        return switch (type) {
            case CORROSIVE -> ChatFormatting.GREEN;
            case DESTRUCTIVE -> ChatFormatting.GOLD;
            case VENGEFUL -> ChatFormatting.RED;
            case STEADFAST -> ChatFormatting.LIGHT_PURPLE;
            case DEFAULT -> ChatFormatting.DARK_AQUA;
        };
    }

    public static void appendSentientTooltip(List<Component> tooltip, String flavourKey, EnumWillType type, int level, double bonusDamage, Double digSpeedBonus, double pool) {
        tooltip.add(Component.translatable(flavourKey).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

        ChatFormatting color = colorFor(type);
        Component typeName = Component.literal(type.toCapitalized()).withStyle(color);
        tooltip.add(Component.translatable("tooltip.bloodmagic.sentient.attuned", typeName).withStyle(ChatFormatting.GRAY));

        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.bloodmagic.extra_info").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        if (level < 0) {
            tooltip.add(Component.translatable("tooltip.bloodmagic.sentient.inactive").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltip.add(Component.translatable("tooltip.bloodmagic.sentient.level_pool", level + 1, format(pool)).withStyle(color));

        if (bonusDamage > 0) {
            tooltip.add(Component.translatable("tooltip.bloodmagic.sentient.bonus_damage", formatSigned(bonusDamage)).withStyle(ChatFormatting.GRAY));
        }

        switch (type) {
            case CORROSIVE -> tooltip.add(Component.translatable("tooltip.bloodmagic.sentient.rider.corrosive",
                    format(SentientToolHelper.POISON_TIME[level] / 20.0),
                    SentientToolHelper.POISON_LEVEL[level] + 1).withStyle(color));
            case STEADFAST -> tooltip.add(Component.translatable("tooltip.bloodmagic.sentient.rider.steadfast",
                    format(SentientToolHelper.ABSORPTION_TIME[level] / 20.0)).withStyle(color));
            case VENGEFUL -> tooltip.add(Component.translatable("tooltip.bloodmagic.sentient.rider.vengeful",
                    formatSigned(SentientToolHelper.MOVEMENT_SPEED[level])).withStyle(color));
            default -> {
            }
        }

        if (digSpeedBonus != null && digSpeedBonus > 0) {
            tooltip.add(Component.translatable("tooltip.bloodmagic.sentient.rider.dig_speed", formatSigned(digSpeedBonus)).withStyle(ChatFormatting.GRAY));
        }
    }

    /**
     * Armour equivalent of {@link #appendSentientTooltip} - kept separate rather than folded into
     * that method because the per-type "rider" line each one shows is semantically different for
     * armour than for a weapon (e.g. Steadfast means bonus Absorption-on-kill for a tool but flat
     * knockback resistance for the chestplate), and armour additionally needs to show whether the
     * Sentient Armour Gem is currently active and what the full-set bonus is - see
     * {@link SentientArmorHelper}/{@link SentientArmorItem}/{@link SentientArmorGemItem}.
     */
    public static void appendArmorTooltip(List<Component> tooltip, String flavourKey, EnumWillType type, int level, double pool, boolean chestPiece, boolean gemActive) {
        tooltip.add(Component.translatable(flavourKey).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

        ChatFormatting color = colorFor(type);
        Component typeName = Component.literal(type.toCapitalized()).withStyle(color);
        tooltip.add(Component.translatable("tooltip.bloodmagic.sentient.attuned", typeName).withStyle(ChatFormatting.GRAY));

        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.bloodmagic.extra_info").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        if (level < 0) {
            // Distinguishes "not enough Will yet" from "carrying no active Sentient Armour Gem" -
            // the caller only ever passes a negative level here when at least one of those is true
            // (see SentientArmorItem#appendHoverText), so if the raw pool already clears the first
            // Will bracket the only remaining reason is the missing/inactive gem.
            boolean poolWouldQualify = SentientArmorHelper.getLevel(pool) >= 0;
            tooltip.add(Component.translatable(poolWouldQualify && !gemActive
                    ? "tooltip.bloodmagic.sentient_armor.gem_required"
                    : "tooltip.bloodmagic.sentient.inactive").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltip.add(Component.translatable("tooltip.bloodmagic.sentient.level_pool", level + 1, format(pool)).withStyle(color));

        if (chestPiece) {
            switch (type) {
                case CORROSIVE -> {
                    tooltip.add(Component.translatable("tooltip.bloodmagic.sentient_armor.corrosive_reflect", format(SentientArmorHelper.CORROSIVE_REFLECT_DURATION / 20.0)).withStyle(color));
                    tooltip.add(Component.translatable("tooltip.bloodmagic.sentient_armor.corrosive_cleanse").withStyle(color));
                }
                case STEADFAST -> tooltip.add(Component.translatable("tooltip.bloodmagic.sentient_armor.knockback",
                        formatSigned(SentientArmorHelper.KNOCKBACK_RESISTANCE_BONUS[level] * 100) + "%").withStyle(color));
                case VENGEFUL -> tooltip.add(Component.translatable("tooltip.bloodmagic.sentient_armor.speed",
                        formatSigned(SentientArmorHelper.SPEED_BOOST[level] * 100) + "%").withStyle(color));
                case DESTRUCTIVE -> {
                    tooltip.add(Component.translatable("tooltip.bloodmagic.sentient_armor.damage",
                            formatSigned(SentientArmorHelper.DAMAGE_BOOST[level] * 100) + "%").withStyle(color));
                    tooltip.add(Component.translatable("tooltip.bloodmagic.sentient_armor.attack_speed",
                            formatSigned(SentientArmorHelper.ATTACK_SPEED_PENALTY[level] * 100) + "%").withStyle(color));
                }
                default -> {
                }
            }
        }

        double protection = SentientArmorHelper.getExtraProtection(type, level);
        if (protection > 0) {
            tooltip.add(Component.translatable("tooltip.bloodmagic.sentient_armor.full_set_protection", format(protection * 100) + "%").withStyle(ChatFormatting.GRAY));
        }
    }

    private static String format(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.format(Locale.ROOT, "%d", (long) value);
        }
        String s = String.format(Locale.ROOT, "%.2f", value);
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "");
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }

    private static String formatSigned(double value) {
        return (value >= 0 ? "+" : "") + format(value);
    }
}
