package wayoftime.bloodmagic.common.event;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.item.SentientArmorHelper;
import wayoftime.bloodmagic.common.item.SentientArmorItem;
import wayoftime.bloodmagic.common.item.SentientToolHelper;
import wayoftime.bloodmagic.common.will.WillHelper;

/**
 * Gameplay hooks for the Sentient Armour set that can't live on the item classes themselves - both
 * of these mirror behavior 1.12's {@code ItemSentientArmour} implemented through mechanisms that no
 * longer exist (see {@code SentientArmorItem}'s class javadoc for the full history/reasoning).
 */
public class SentientArmorEventHandler {
    /**
     * Corrosive chestplate: melee attacker gets Withered on hit, mirroring 1.12's
     * {@code onPlayerAttacked} (which used Poison; this branch already migrated the Sentient Sword's
     * matching Corrosive rider from Poison to Wither, so this follows suit for consistency - see
     * {@link SentientToolHelper#applyOnHitEffect}). Fires on {@link LivingIncomingDamageEvent} (the
     * closest hook to 1.12's "before the hit lands" timing) rather than gating on the full-set/gem
     * check the extra-protection bonus below uses - 1.12 applied this from the chestplate alone.
     */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player wearer) || event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || attacker == wearer) {
            return;
        }

        ItemStack chest = wearer.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof SentientArmorItem)) {
            return;
        }
        if (SentientToolHelper.currentType(chest) != EnumWillType.CORROSIVE || SentientArmorHelper.currentLevel(chest) < 0) {
            return;
        }

        attacker.addEffect(new MobEffectInstance(MobEffects.WITHER, SentientArmorHelper.CORROSIVE_REFLECT_DURATION, 0));
    }

    /**
     * The full-set extra-protection bonus. 1.12 ran this through Forge's since-removed
     * {@code ISpecialArmor}, which replaced vanilla's armor/toughness damage math outright; that
     * hook doesn't exist anymore, so this instead applies the same Will-scaled fraction as an
     * additional reduction *after* vanilla armor/toughness/enchantments have already run (this event
     * fires post-reduction, pre-health-change - see {@link LivingDamageEvent.Pre}'s javadoc), and
     * drains the wearer's Will per point of damage absorbed the same way 1.12's
     * {@code damageArmor}/{@code getCostModifier} upkeep did. 1.12 reverted the *entire equipped
     * set* back to the player's pre-conversion gear the instant Will ran out mid-fight; this port
     * just skips the bonus for that single hit instead (the pieces stay equipped either way - see
     * {@code SentientArmorItem}'s class javadoc for why the wrap/revert system doesn't apply here).
     */
    public static void onDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player wearer)) {
            return;
        }

        ItemStack helmet = wearer.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = wearer.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = wearer.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = wearer.getItemBySlot(EquipmentSlot.FEET);
        if (!(helmet.getItem() instanceof SentientArmorItem) || !(chest.getItem() instanceof SentientArmorItem)
                || !(legs.getItem() instanceof SentientArmorItem) || !(boots.getItem() instanceof SentientArmorItem)) {
            return;
        }

        EnumWillType type = SentientToolHelper.currentType(chest);
        int level = SentientArmorHelper.currentLevel(chest);
        if (level < 0) {
            return;
        }

        float damage = event.getNewDamage();
        if (damage <= 0) {
            return;
        }

        double drain = SentientArmorHelper.WILL_DRAIN_PER_DAMAGE[level] * damage;
        if (WillHelper.getTotalWill(type, wearer) < drain) {
            return;
        }
        WillHelper.consumeWill(wearer, type, drain);

        double protection = SentientArmorHelper.getExtraProtection(type, level);
        event.setNewDamage((float) (damage * (1 - protection)));
    }
}
