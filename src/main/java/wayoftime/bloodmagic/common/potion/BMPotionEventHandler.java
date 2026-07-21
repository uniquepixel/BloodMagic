package wayoftime.bloodmagic.common.potion;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.item.BMItems;

public class BMPotionEventHandler {
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
