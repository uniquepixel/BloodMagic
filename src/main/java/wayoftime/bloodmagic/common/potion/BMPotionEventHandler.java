package wayoftime.bloodmagic.common.potion;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.item.BMItems;

public class BMPotionEventHandler {
    private static final int SCAN_INTERVAL = 10;

    /**
     * 1.20.1's Spectral Sight made nearby entities glow through walls via a client-only Mixin on
     * {@code LivingEntity#isCurrentlyGlowing}, scoped per-viewer. This branch has no Mixin
     * framework, so the same "see mobs through walls" gameplay value is reproduced by directly
     * applying vanilla's own {@link MobEffects#GLOWING} (ambient, no particles/icon) to nearby
     * entities while a player has the effect - one real behavioral difference from the original:
     * the glow is a synced entity flag, so it's visible to every nearby player, not just the
     * Spectral Sight wearer.
     */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || player.tickCount % SCAN_INTERVAL != 0) {
            return;
        }

        MobEffectInstance spectralSight = player.getEffect(BMPotions.SPECTRAL_SIGHT);
        if (spectralSight == null) {
            return;
        }

        double range = spectralSight.getAmplifier() * 32 + 24;
        AABB area = player.getBoundingBox().inflate(range);
        for (LivingEntity nearby : player.level().getEntitiesOfClass(LivingEntity.class, area)) {
            if (player.distanceToSqr(nearby) <= range * range) {
                nearby.addEffect(new MobEffectInstance(MobEffects.GLOWING, SCAN_INTERVAL + 5, 0, true, false, false));
            }
        }
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (event.getSource().is(DamageTypeTags.IS_FALL) && entity.hasEffect(BMPotions.SOFT_FALL)) {
            event.setAmount(0);
        }

        if (event.getSource().is(DamageTypeTags.IS_FALL) && entity.hasEffect(BMPotions.HEAVY_HEART)) {
            int amplifier = entity.getEffect(BMPotions.HEAVY_HEART).getAmplifier();
            event.setAmount(event.getAmount() * (1 + (amplifier + 1) * 0.5F));
        }
    }

    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity attacked = event.getEntity();
        if (!attacked.hasEffect(BMPotions.SOUL_SNARE) || attacked.level().isClientSide) {
            return;
        }

        int level = attacked.getEffect(BMPotions.SOUL_SNARE).getAmplifier();
        double amount = attacked.getRandom().nextDouble() * (level + 1) * (level + 1) * 4 + 1;

        ItemStack soulStack = new ItemStack(BMItems.RAW_WILL.get());
        soulStack.set(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.DEFAULT);
        soulStack.set(BMDataComponents.DEMON_WILL_AMOUNT, amount);

        event.getDrops().add(new ItemEntity(attacked.level(), attacked.getX(), attacked.getY(), attacked.getZ(), soulStack));
    }
}
