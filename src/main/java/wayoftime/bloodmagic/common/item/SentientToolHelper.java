package wayoftime.bloodmagic.common.item;

import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;

/**
 * Shared stat tables and behavior for the five Sentient tools (sword/axe/pickaxe/shovel/scythe),
 * ported from 1.20.1's near-identical {@code ItemSentientSword}/{@code ItemSentientAxe}/
 * {@code ItemSentientPickaxe}/{@code ItemSentientShovel}/{@code ItemSentientScythe} - those five
 * classes were ~450 lines each of copy-pasted logic differing only in a handful of constants, so
 * this branch consolidates the shared tables/math here and lets each tool item supply just its own
 * base damage/speed (and, for the sword, its slightly different damage/speed curves - see the
 * {@code SWORD_*} tables below).
 * <p>
 * Where 1.20.1 cached "current type"/"current level" via a pile of NBT doubles recalculated on
 * every hit/right-click, this branch computes everything live off
 * {@link wayoftime.bloodmagic.common.will.WillHelper#getLargestWillType} whenever it's needed -
 * there's no correctness difference since the old code recalculated just as often anyway, and it
 * removes an entire layer of stale-cache bookkeeping.
 */
public final class SentientToolHelper {
    private SentientToolHelper() {
    }

    public static final int[] SOUL_BRACKET = {16, 60, 200, 400, 1000, 2000, 4000};

    // Shared by axe/pickaxe/shovel/scythe. The sword uses its own (slightly lower) curves - see
    // the SWORD_* tables below.
    public static final double[] DEFAULT_DAMAGE_ADDED = {1, 2, 3, 3.5, 4, 4.5, 5};
    public static final double[] DESTRUCTIVE_DAMAGE_ADDED = {2, 3, 4, 5, 6, 7, 8};
    public static final double[] VENGEFUL_DAMAGE_ADDED = {0, 0.5, 1, 1.5, 2, 2.5, 3};
    public static final double[] STEADFAST_DAMAGE_ADDED = {0, 0.5, 1, 1.5, 2, 2.5, 3};
    public static final double[] VENGEFUL_ATTACK_SPEED = {-3, -2.8, -2.7, -2.6, -2.5, -2.4, -2.3};
    public static final double[] DESTRUCTIVE_ATTACK_SPEED = {-3.1, -3.1, -3.2, -3.3, -3.3, -3.3, -3.3};
    public static final double DEFAULT_ATTACK_SPEED_FALLBACK = -2.9;

    public static final double[] SWORD_DEFAULT_DAMAGE_ADDED = {1, 1.5, 2, 2.5, 3, 3.5, 4};
    public static final double[] SWORD_VENGEFUL_DAMAGE_ADDED = {0, 0.5, 1, 1.5, 2, 2.25, 2.5};
    public static final double[] SWORD_STEADFAST_DAMAGE_ADDED = {0, 0.5, 1, 1.5, 2, 2.25, 2.5};
    public static final double[] SWORD_VENGEFUL_ATTACK_SPEED = {-2.1, -2, -1.8, -1.7, -1.6, -1.6, -1.5};
    public static final double[] SWORD_DESTRUCTIVE_ATTACK_SPEED = {-2.6, -2.7, -2.8, -2.9, -3, -3, -3};
    public static final double SWORD_DEFAULT_ATTACK_SPEED_FALLBACK = -2.4;

    public static final double[] DIG_SPEED_ADDED = {1, 1.5, 2, 3, 4, 5, 6};
    public static final double[] SOUL_DRAIN_PER_SWING = {0.05, 0.1, 0.2, 0.4, 0.75, 1, 1.25};
    public static final double[] SOUL_DROP = {2, 4, 7, 10, 13, 15, 18};
    public static final double[] STATIC_DROP = {1, 1, 2, 3, 3, 4, 4};

    public static final int[] ABSORPTION_TIME = {200, 300, 400, 500, 600, 700, 800};
    public static final double MAX_ABSORPTION_HEARTS = 10;

    public static final int[] POISON_TIME = {25, 50, 60, 80, 100, 120, 150};
    public static final int[] POISON_LEVEL = {0, 0, 0, 1, 1, 1, 1};

    public static final double[] MOVEMENT_SPEED = {0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.4};

    /**
     * The type/amount a Sentient tool is currently attuned to is cached on the stack itself (via
     * the same {@code DEMON_WILL_TYPE}/{@code DEMON_WILL_AMOUNT} components Raw Will/Soul Gem
     * stacks already use) rather than recomputed from the player on every single query - most
     * critically, {@code Item#getDestroySpeed} has no player parameter to recompute from at all,
     * so the tool's last-recalculated state has to live somewhere reachable from the stack alone.
     */
    public static EnumWillType currentType(ItemStack stack) {
        return stack.getOrDefault(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.DEFAULT);
    }

    public static double currentWill(ItemStack stack) {
        return stack.getOrDefault(BMDataComponents.DEMON_WILL_AMOUNT, 0D);
    }

    public static int currentLevel(ItemStack stack) {
        return getLevel(currentWill(stack));
    }

    /** Bracket index for a given Will amount, or -1 if below the first bracket ("inactive"). */
    public static int getLevel(double will) {
        int lvl = -1;
        for (int i = 0; i < SOUL_BRACKET.length; i++) {
            if (will >= SOUL_BRACKET[i]) {
                lvl = i;
            }
        }
        return lvl;
    }

    public static double getExtraDamage(EnumWillType type, int level, boolean sword) {
        if (level < 0) {
            return 0;
        }
        return switch (type) {
            case CORROSIVE, DEFAULT -> sword ? SWORD_DEFAULT_DAMAGE_ADDED[level] : DEFAULT_DAMAGE_ADDED[level];
            case DESTRUCTIVE -> DESTRUCTIVE_DAMAGE_ADDED[level];
            case VENGEFUL -> sword ? SWORD_VENGEFUL_DAMAGE_ADDED[level] : VENGEFUL_DAMAGE_ADDED[level];
            case STEADFAST -> sword ? SWORD_STEADFAST_DAMAGE_ADDED[level] : STEADFAST_DAMAGE_ADDED[level];
        };
    }

    public static double getAttackSpeed(EnumWillType type, int level, boolean sword, double baseAttackSpeed) {
        if (level < 0) {
            return baseAttackSpeed;
        }
        return switch (type) {
            case VENGEFUL -> sword ? SWORD_VENGEFUL_ATTACK_SPEED[level] : VENGEFUL_ATTACK_SPEED[level];
            case DESTRUCTIVE -> sword ? SWORD_DESTRUCTIVE_ATTACK_SPEED[level] : DESTRUCTIVE_ATTACK_SPEED[level];
            default -> sword ? SWORD_DEFAULT_ATTACK_SPEED_FALLBACK : DEFAULT_ATTACK_SPEED_FALLBACK;
        };
    }

    /** Digging speed bonus (axe/pickaxe/shovel only) - flat regardless of type, matching 1.20.1. */
    public static double getDigSpeedBonus(int level) {
        return level >= 0 ? DIG_SPEED_ADDED[level] : 0;
    }

    public static double getDrainPerSwing(int level) {
        return level >= 0 ? SOUL_DRAIN_PER_SWING[level] : 0;
    }

    /** Corrosive Wither / Steadfast Absorption on-hit riders, ported 1:1 from 1.20.1. */
    public static void applyOnHitEffect(EnumWillType type, int level, LivingEntity target, LivingEntity attacker) {
        if (level < 0) {
            return;
        }
        switch (type) {
            case CORROSIVE -> target.addEffect(new MobEffectInstance(MobEffects.WITHER, POISON_TIME[level], POISON_LEVEL[level]));
            case STEADFAST -> {
                if (!target.isAlive()) {
                    float absorption = attacker.getAbsorptionAmount();
                    attacker.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, ABSORPTION_TIME[level], 127, false, false));
                    attacker.setAbsorptionAmount((float) Math.min(absorption + target.getMaxHealth() * 0.05F, MAX_ABSORPTION_HEARTS));
                }
            }
            default -> {
            }
        }
    }

    /**
     * Ported from {@code IDemonWillWeapon#getRandomDemonWillDrop}: rolls a single Will drop off a
     * kill, sized by the tool's current level and the kill's max health. Simplified to a single
     * roll rather than 1.20.1's looting-scaled multi-roll loop - NeoForge's current
     * {@code LivingDropsEvent} no longer exposes a looting level at all (loot-table enchantments
     * apply earlier in the pipeline now), so there's nothing left to scale by. Returns
     * {@code null} if the kill doesn't qualify (a non-hostile mob killed outside Peaceful
     * difficulty, matching 1.20.1's rule) or the tool isn't active.
     */
    public static ItemStack rollWillDrop(LivingEntity killed, EnumWillType type, int level) {
        boolean qualifies = killed.level().getDifficulty() == Difficulty.PEACEFUL || killed instanceof Enemy;
        if (!qualifies || level < 0) {
            return null;
        }

        double willModifier = killed instanceof Slime ? 0.67 : 1;
        double amount = willModifier * (SOUL_DROP[level] * killed.getRandom().nextDouble() + STATIC_DROP[level]) * killed.getMaxHealth() / 20.0;
        if (amount <= 0) {
            return null;
        }

        ItemStack soulStack = new ItemStack(BMItems.RAW_WILL.get());
        soulStack.set(BMDataComponents.DEMON_WILL_TYPE, type);
        soulStack.set(BMDataComponents.DEMON_WILL_AMOUNT, amount);
        return soulStack;
    }
}
