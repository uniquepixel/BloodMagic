package wayoftime.bloodmagic.common.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Implemented by weapons whose kills should yield a custom Will drop instead of (or in addition
 * to) the flat-chance drop in {@code WillEventHandler#onLivingDrops}. Mirrors 1.20.1's
 * {@code IDemonWillWeapon#getRandomDemonWillDrop}, simplified to a single roll since this branch's
 * Will economy has no looting-level multiplier hook at the drops-event level yet.
 */
public interface IWillDropWeapon {
    /**
     * @return the Will stack to drop, or {@code null}/empty if this kill shouldn't drop anything
     * (e.g. a peaceful-difficulty non-hostile kill).
     */
    ItemStack rollWillDrop(LivingEntity killed, Player attacker, ItemStack weapon);
}
