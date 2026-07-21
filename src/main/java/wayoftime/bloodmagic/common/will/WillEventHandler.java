package wayoftime.bloodmagic.common.will;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.common.item.IWillDropWeapon;
import wayoftime.bloodmagic.common.item.RawSoulItem;

/**
 * Wires up the Demon Will soul loop:
 * killing a monster with a Sentient tool ({@link IWillDropWeapon}) yields that weapon's own
 * Will-scaled drop (see {@code SentientToolHelper#rollWillDrop}); any other kill instead has a
 * flat chance to drop some raw Will. Either way, picked-up Will goes straight into a Soul Gem
 * instead of an inventory slot, and every kill always seeds a little ambient Will into the world
 * aura at the kill position, which {@link WorldWillHelper} tracks per-chunk.
 * <p>
 * The flat-chance path is a stand-in for 1.20.1's dedicated "Soul Snare" potion effect (now ported
 * separately - see {@code BMPotionEventHandler#onLivingDrops}) - it exists so the gem/soul loop is
 * reachable even without a Sentient tool or Soul Snare in play.
 */
public class WillEventHandler {
    private static final float DROP_CHANCE = 0.35F;

    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity attacked = event.getEntity();
        if (!(attacked instanceof Monster) || attacked.level().isClientSide) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        WorldWillHelper.addWill(attacked.level(), attacked.blockPosition(), EnumWillType.DEFAULT, attacked.getMaxHealth() / 8.0);

        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof IWillDropWeapon willDropWeapon) {
            ItemStack drop = willDropWeapon.rollWillDrop(attacked, player, held);
            if (drop != null && !drop.isEmpty()) {
                event.getDrops().add(new ItemEntity(attacked.level(), attacked.getX(), attacked.getY(), attacked.getZ(), drop));
            }
            return;
        }

        if (attacked.getRandom().nextFloat() > DROP_CHANCE) {
            return;
        }

        double amount = 1 + attacked.getRandom().nextDouble() * (attacked.getMaxHealth() / 4.0);
        ItemStack soulStack = new ItemStack(BMItems.RAW_WILL.get());
        soulStack.set(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.DEFAULT);
        soulStack.set(BMDataComponents.DEMON_WILL_AMOUNT, amount);

        event.getDrops().add(new ItemEntity(attacked.level(), attacked.getX(), attacked.getY(), attacked.getZ(), soulStack));
    }

    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide) {
            return;
        }

        ItemStack stack = event.getItemEntity().getItem();
        if (!(stack.getItem() instanceof RawSoulItem)) {
            return;
        }
        if (!stack.has(BMDataComponents.DEMON_WILL_TYPE) || !stack.has(BMDataComponents.DEMON_WILL_AMOUNT)) {
            return;
        }

        EnumWillType type = stack.get(BMDataComponents.DEMON_WILL_TYPE);
        double amount = stack.getOrDefault(BMDataComponents.DEMON_WILL_AMOUNT, 0D);
        if (amount <= 0) {
            return;
        }

        double absorbed = WillHelper.fillOtherGems(player, ItemStack.EMPTY, type, amount);
        if (absorbed <= 0) {
            return;
        }

        double remaining = amount - absorbed;
        if (remaining <= 0.0001) {
            event.setCanPickup(TriState.FALSE);
            event.getItemEntity().discard();
        } else {
            stack.set(BMDataComponents.DEMON_WILL_AMOUNT, remaining);
        }
    }
}
