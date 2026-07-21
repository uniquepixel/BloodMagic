package wayoftime.bloodmagic.common.item;

import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import net.minecraft.world.item.ItemStack;

/**
 * Shared stat tables for the four Sentient Armour pieces, ported from 1.12's
 * {@code WayofTime.bloodmagic.item.armour.ItemSentientArmour} - the last branch with a real Java
 * implementation of this feature. Everything from 1.14.4 onward only carried the leftover texture
 * assets forward (see {@code SentientArmorItem}'s class javadoc), so 1.20.1 had nothing to port
 * from directly for the mechanics themselves.
 * <p>
 * Uses its own Will-bracket table (8 brackets) rather than {@link SentientToolHelper}'s
 * ({@code SOUL_BRACKET}, 7 brackets) - the two systems had different bracket curves in every branch
 * that had both, so this keeps that distinction rather than collapsing them together. Cached
 * type/pool are still read via {@link SentientToolHelper#currentType}/{@code currentWill} - those
 * use the same {@code DEMON_WILL_TYPE}/{@code DEMON_WILL_AMOUNT} components regardless of bracket
 * table, so there's no need to duplicate that plumbing here.
 */
public final class SentientArmorHelper {
    private SentientArmorHelper() {
    }

    public static final double[] WILL_BRACKET = {30, 200, 600, 1500, 4000, 6000, 8000, 16000};

    /** Will drained per point of damage absorbed while the full 4-piece set's bonus is active. */
    public static final double[] WILL_DRAIN_PER_DAMAGE = {0.1, 0.12, 0.15, 0.2, 0.3, 0.35, 0.4, 0.5};

    /** Extra post-armor damage reduction fraction, full-set-only - see SentientArmorEventHandler. */
    public static final double[] EXTRA_PROTECTION = {0, 0.25, 0.5, 0.6, 0.7, 0.75, 0.85, 0.9};
    public static final double[] STEADFAST_PROTECTION = {0.25, 0.5, 0.6, 0.7, 0.75, 0.85, 0.9, 0.95};

    // Chestplate-only attribute bonuses (1.12 only ever granted these via the chest slot's
    // getAttributeModifiers override, regardless of what the other three pieces were).
    public static final double[] KNOCKBACK_RESISTANCE_BONUS = {0.2, 0.4, 0.6, 0.8, 1, 1, 1, 1};
    public static final double[] DAMAGE_BOOST = {0.03, 0.06, 0.09, 0.12, 0.15, 0.18, 0.22, 0.25};
    public static final double[] ATTACK_SPEED_PENALTY = {-0.02, -0.04, -0.06, -0.08, -0.1, -0.12, -0.14, -0.16};
    public static final double[] SPEED_BOOST = {0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4};

    /**
     * Corrosive chest's on-hurt reflect (Wither on the attacker, matching this branch's existing
     * Corrosive Poison-&gt;Wither migration on the Sentient Sword - see
     * {@link SentientToolHelper#applyOnHitEffect}). 1.12's version left this at a flat 100 ticks
     * with a source comment reading "TODO: customize duration" - kept flat here too rather than
     * inventing a bracket curve the original never actually shipped.
     */
    public static final int CORROSIVE_REFLECT_DURATION = 100;

    public static int getLevel(double will) {
        int lvl = -1;
        for (int i = 0; i < WILL_BRACKET.length; i++) {
            if (will >= WILL_BRACKET[i]) {
                lvl = i;
            }
        }
        return lvl;
    }

    public static int currentLevel(ItemStack stack) {
        return getLevel(SentientToolHelper.currentWill(stack));
    }

    public static double getExtraProtection(EnumWillType type, int level) {
        if (level < 0) {
            return 0;
        }
        return type == EnumWillType.STEADFAST ? STEADFAST_PROTECTION[level] : EXTRA_PROTECTION[level];
    }
}
