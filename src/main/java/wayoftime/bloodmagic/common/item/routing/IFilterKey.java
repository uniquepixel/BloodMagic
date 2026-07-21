package wayoftime.bloodmagic.common.item.routing;

import net.minecraft.world.item.ItemStack;

/**
 * Ported from 1.20.1's {@code wayoftime.bloodmagic.common.item.routing.IFilterKey}: the "what does
 * this filter actually match" abstraction that a filter item (see {@link IFilterProvider}) builds
 * one-or-more of from its configured reference items. Each concrete implementation below matches
 * stacks a different way (exact item, tag, mod id, enchantment, or a combination of other keys).
 * <p>
 * The count/grow/shrink bookkeeping is carried over from the original for fidelity and potential
 * future use (e.g. quantity-limited routing), but the routing node matching added in this pass
 * (see {@code InputRoutingNodeTile}/{@code OutputRoutingNodeTile}) only calls
 * {@link #doesStackMatch(ItemStack)} - the original's batched, count-throttled transfer bookkeeping
 * ({@code BasicItemFilter}/{@code BlacklistItemFilter}) was not carried over, since this port's
 * Master node already moves a single item per tick and doesn't need per-key request budgets.
 */
public interface IFilterKey {
    boolean doesStackMatch(ItemStack testStack);

    int getCount();

    void setCount(int count);

    void grow(int changeAmount);

    boolean isEmpty();

    void shrink(int changeAmount);
}
