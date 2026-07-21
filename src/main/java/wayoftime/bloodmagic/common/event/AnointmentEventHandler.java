package wayoftime.bloodmagic.common.event;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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
import wayoftime.bloodmagic.common.will.WorldWillHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Full-fidelity port of 1.20.1's Anointment system's gameplay effects, adapted to this branch's
 * per-anointment "uses" data component architecture (see {@link wayoftime.bloodmagic.common.item.AnointmentItem})
 * rather than the original's data-driven {@code AnointmentHolder}/attribute-provider registry.
 * Silk Touch, Fortune, Smelting and Voiding recompute a block's drops (the same trick the
 * original's {@code IGlobalLootModifier}s used); the rest hook the closest matching vanilla event.
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
     * Silk Touch, Fortune, Smelting and Voiding all recompute what a block would have dropped
     * under a modified tool (or clear the drops outright), same as the original's global loot
     * modifiers.
     */
    public static void onBlockDrops(BlockDropsEvent event) {
        ItemStack tool = event.getTool();
        if (tool.isEmpty() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        int voidingUses = tool.getOrDefault(BMDataComponents.ANOINTMENT_VOIDING_USES, 0);
        if (voidingUses > 0) {
            event.getDrops().clear();
            decrement(tool, BMDataComponents.ANOINTMENT_VOIDING_USES.get(), voidingUses);
            return;
        }

        int silkTouchUses = tool.getOrDefault(BMDataComponents.ANOINTMENT_SILK_TOUCH_USES, 0);
        int fortuneUses = tool.getOrDefault(BMDataComponents.ANOINTMENT_FORTUNE_USES, 0);
        if (silkTouchUses > 0 || fortuneUses > 0) {
            BlockState state = event.getState();
            ItemStack fakeTool = tool.copy();

            if (silkTouchUses > 0) {
                setEnchantLevel(level, fakeTool, Enchantments.SILK_TOUCH, 1);
                decrement(tool, BMDataComponents.ANOINTMENT_SILK_TOUCH_USES.get(), silkTouchUses);
            } else {
                int baseFortune = getEnchantLevel(level, tool, Enchantments.FORTUNE);
                setEnchantLevel(level, fakeTool, Enchantments.FORTUNE, baseFortune + fortuneUses);
                decrement(tool, BMDataComponents.ANOINTMENT_FORTUNE_USES.get(), fortuneUses);
            }

            List<ItemStack> recomputed = Block.getDrops(state, level, event.getPos(), event.getBlockEntity(), event.getBreaker(), fakeTool);
            List<ItemEntity> newDrops = new ArrayList<>();
            double x = event.getPos().getX() + 0.5;
            double y = event.getPos().getY() + 0.5;
            double z = event.getPos().getZ() + 0.5;
            for (ItemStack stack : recomputed) {
                if (!stack.isEmpty()) {
                    newDrops.add(new ItemEntity(level, x, y, z, stack));
                }
            }

            event.getDrops().clear();
            event.getDrops().addAll(newDrops);
            return;
        }

        int hiddenKnowledgeUses = tool.getOrDefault(BMDataComponents.ANOINTMENT_HIDDEN_KNOWLEDGE_USES, 0);
        if (hiddenKnowledgeUses > 0) {
            event.setDroppedExperience(event.getDroppedExperience() + HIDDEN_KNOWLEDGE_BONUS_XP);
            decrement(tool, BMDataComponents.ANOINTMENT_HIDDEN_KNOWLEDGE_USES.get(), hiddenKnowledgeUses);
        }

        int smeltingUses = tool.getOrDefault(BMDataComponents.ANOINTMENT_SMELTING_USES, 0);
        if (smeltingUses > 0) {
            for (ItemEntity drop : event.getDrops()) {
                ItemStack stack = drop.getItem();
                Optional<RecipeHolder<SmeltingRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level);
                recipe.ifPresent(holder -> {
                    ItemStack result = holder.value().getResultItem(level.registryAccess()).copy();
                    result.setCount(stack.getCount() * result.getCount());
                    drop.setItem(result);
                });
            }
            decrement(tool, BMDataComponents.ANOINTMENT_SMELTING_USES.get(), smeltingUses);
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

    private static int getEnchantLevel(ServerLevel level, ItemStack stack, net.minecraft.resources.ResourceKey<Enchantment> key) {
        Holder<Enchantment> holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
        return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
    }

    private static void setEnchantLevel(ServerLevel level, ItemStack stack, net.minecraft.resources.ResourceKey<Enchantment> key, int enchantLevel) {
        Holder<Enchantment> holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(holder, enchantLevel));
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
