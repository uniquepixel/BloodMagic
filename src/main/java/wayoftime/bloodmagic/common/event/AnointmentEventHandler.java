package wayoftime.bloodmagic.common.event;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.common.item.SentientBowItem;
import wayoftime.bloodmagic.common.item.SentientToolHelper;
import wayoftime.bloodmagic.common.will.WillHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Full-fidelity port of 1.20.1's Anointment system's gameplay effects, adapted to this branch's
 * per-anointment "uses" data component architecture (see {@link wayoftime.bloodmagic.common.item.AnointmentItem})
 * rather than the original's data-driven {@code AnointmentHolder}/attribute-provider registry.
 * Silk Touch, Fortune, Smelting and Voiding used to be handled here too, but now live in
 * {@code wayoftime.bloodmagic.common.loot.BMLootModifiers} as real global loot modifiers (see
 * {@link #onBlockDrops}'s javadoc for why); the rest hook the closest matching vanilla event.
 */
public class AnointmentEventHandler {
    private static final float MELEE_ANOINTMENT_DAMAGE = 1.0F;
    private static final float HOLY_WATER_DAMAGE = 5.0F;
    private static final int HIDDEN_KNOWLEDGE_BONUS_XP = 2;
    private static final float BOW_POWER_MULTIPLIER = 1.25F;
    private static final float BOW_VELOCITY_MULTIPLIER = 1.5F;
    private static final int QUICK_DRAW_BONUS_CHARGE_TICKS = 10;
    private static final double WILL_POWER_AMOUNT = 5;

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();

        int meleeUses = weapon.getOrDefault(BMDataComponents.ANOINTMENT_MELEE_USES, 0);
        if (meleeUses > 0) {
            event.setAmount(event.getAmount() + MELEE_ANOINTMENT_DAMAGE);
            decrement(weapon, BMDataComponents.ANOINTMENT_MELEE_USES.get(), meleeUses);
        }

        int holyWaterUses = weapon.getOrDefault(BMDataComponents.ANOINTMENT_HOLY_WATER_USES, 0);
        if (holyWaterUses > 0 && event.getEntity().getType().is(EntityTypeTags.UNDEAD)) {
            event.setAmount(event.getAmount() + HOLY_WATER_DAMAGE);
            decrement(weapon, BMDataComponents.ANOINTMENT_HOLY_WATER_USES.get(), holyWaterUses);
        }

        int repairUses = weapon.getOrDefault(BMDataComponents.ANOINTMENT_WEAPON_REPAIR_USES, 0);
        if (repairUses > 0 && weapon.isDamaged()) {
            weapon.setDamageValue(weapon.getDamageValue() - 1);
            decrement(weapon, BMDataComponents.ANOINTMENT_WEAPON_REPAIR_USES.get(), repairUses);
        }

        int willPowerUses = weapon.getOrDefault(BMDataComponents.ANOINTMENT_WILL_POWER_USES, 0);
        if (willPowerUses > 0) {
            ItemStack willStack = new ItemStack(BMItems.RAW_WILL.get());
            willStack.set(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.DEFAULT);
            willStack.set(BMDataComponents.DEMON_WILL_AMOUNT, WILL_POWER_AMOUNT);
            if (!attacker.getInventory().add(willStack)) {
                attacker.drop(willStack, false);
            }
            decrement(weapon, BMDataComponents.ANOINTMENT_WILL_POWER_USES.get(), willPowerUses);
        }
    }

    /**
     * Duplicates whatever drops the kill already produced (a simplified stand-in for a real
     * looting-level bonus, which would need to hook the loot table itself rather than the
     * finalized drop list).
     */
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        int uses = weapon.getOrDefault(BMDataComponents.ANOINTMENT_LOOTING_USES, 0);
        if (uses <= 0 || event.getDrops().isEmpty()) {
            return;
        }

        List<ItemEntity> extra = new ArrayList<>();
        for (ItemEntity drop : event.getDrops()) {
            ItemEntity duplicate = new ItemEntity(drop.level(), drop.getX(), drop.getY(), drop.getZ(), drop.getItem().copy());
            extra.add(duplicate);
        }
        event.getDrops().addAll(extra);

        decrement(weapon, BMDataComponents.ANOINTMENT_LOOTING_USES.get(), uses);
    }

    /**
     * Only Hidden Knowledge is left here - Silk Touch, Fortune, Smelting and Voiding used to be
     * handled in this method (recomputing what a block would have dropped under a modified tool,
     * or clearing the drops outright), but that only ever ran for normal player mining: this method
     * hooks NeoForge's {@code BlockDropsEvent}, which is fired from {@code Block#dropResources} and
     * therefore never reached by {@code ExplosiveChargeTile#breakAndCollectDrops} (Charges collect
     * their drops straight from {@code BlockState#getDrops}, bypassing dropResources entirely). That
     * logic now lives in {@code wayoftime.bloodmagic.common.loot.BMLootModifiers} as real global
     * loot modifiers, which run inside the loot-table resolution both paths share - see that
     * class's javadoc. Hidden Knowledge has no loot-modifier equivalent (it only touches XP, which
     * loot modifiers can't see), so it stays here.
     */
    public static void onBlockDrops(BlockDropsEvent event) {
        ItemStack tool = event.getTool();
        if (tool.isEmpty()) {
            return;
        }

        int hiddenKnowledgeUses = tool.getOrDefault(BMDataComponents.ANOINTMENT_HIDDEN_KNOWLEDGE_USES, 0);
        if (hiddenKnowledgeUses > 0) {
            event.setDroppedExperience(event.getDroppedExperience() + HIDDEN_KNOWLEDGE_BONUS_XP);
            decrement(tool, BMDataComponents.ANOINTMENT_HIDDEN_KNOWLEDGE_USES.get(), hiddenKnowledgeUses);
        }
    }

    /**
     * Fakes a fuller draw so the shot fires at full power/velocity even if released early.
     */
    public static void onArrowLoose(ArrowLooseEvent event) {
        ItemStack bow = event.getBow();
        int uses = bow.getOrDefault(BMDataComponents.ANOINTMENT_QUICK_DRAW_USES, 0);
        if (uses <= 0) {
            return;
        }

        event.setCharge(event.getCharge() + QUICK_DRAW_BONUS_CHARGE_TICKS);
        decrement(bow, BMDataComponents.ANOINTMENT_QUICK_DRAW_USES.get(), uses);
    }

    /**
     * Boosts a freshly-fired arrow's damage/velocity if the bow that shot it carries Bow Power/
     * Bow Velocity charges. Runs on entity join since the fired arrow entity isn't available from
     * {@link ArrowLooseEvent} itself.
     */
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof AbstractArrow arrow) || !(arrow.getOwner() instanceof Player shooter)) {
            return;
        }

        ItemStack bow = shooter.getMainHandItem().is(net.minecraft.world.item.Items.BOW) || shooter.getMainHandItem().is(net.minecraft.world.item.Items.CROSSBOW)
                ? shooter.getMainHandItem()
                : shooter.getOffhandItem();

        int powerUses = bow.getOrDefault(BMDataComponents.ANOINTMENT_BOW_POWER_USES, 0);
        if (powerUses > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() * BOW_POWER_MULTIPLIER);
            decrement(bow, BMDataComponents.ANOINTMENT_BOW_POWER_USES.get(), powerUses);
        }

        int velocityUses = bow.getOrDefault(BMDataComponents.ANOINTMENT_BOW_VELOCITY_USES, 0);
        if (velocityUses > 0) {
            arrow.setDeltaMovement(arrow.getDeltaMovement().scale(BOW_VELOCITY_MULTIPLIER));
            decrement(bow, BMDataComponents.ANOINTMENT_BOW_VELOCITY_USES.get(), velocityUses);
        }

        if (bow.getItem() instanceof SentientBowItem) {
            applySentientBowBonus(bow, arrow, shooter);
        }
    }

    /**
     * The Sentient Bow's Will-scaled shot bonus (see {@link SentientBowItem}) - half of the
     * matching melee tools' bonus damage, a small velocity bump, and a Will drain per shot at half
     * the melee "drain per swing" rate.
     */
    private static void applySentientBowBonus(ItemStack bow, AbstractArrow arrow, Player shooter) {
        EnumWillType type = WillHelper.getLargestWillType(shooter);
        double will = WillHelper.getTotalWill(type, shooter);
        int level = SentientToolHelper.getLevel(will);
        if (level < 0) {
            return;
        }

        double bonusDamage = SentientToolHelper.getExtraDamage(type, level, false) * 0.5;
        arrow.setBaseDamage(arrow.getBaseDamage() + bonusDamage);
        arrow.setDeltaMovement(arrow.getDeltaMovement().scale(1 + 0.04 * (level + 1)));

        double drain = SentientToolHelper.getDrainPerSwing(level) * 0.5;
        if (drain > 0) {
            WillHelper.consumeWill(shooter, type, drain);
        }
    }

    private static void decrement(ItemStack weapon, DataComponentType<Integer> component, int uses) {
        uses--;
        if (uses <= 0) {
            weapon.remove(component);
        } else {
            weapon.set(component, uses);
        }
    }
}
