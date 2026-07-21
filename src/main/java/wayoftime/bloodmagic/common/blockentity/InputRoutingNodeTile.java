package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.common.item.routing.IFilterProvider;
import wayoftime.bloodmagic.util.ChatUtil;

import java.util.List;

/**
 * A node in the Item Routing network (see {@link MasterRoutingNodeTile}): marks one of its six
 * neighboring external inventories (any side not itself facing another routing node) as a source
 * the network can pull items from.
 * <p>
 * Restores 1.20.1's Filter item plumbing ({@code TileFilteredRoutingNode#getFilterStack}): holds an
 * optional Filter item (see {@link wayoftime.bloodmagic.common.item.filter.AbstractFilterItem}) that,
 * when present, gates which items this node will pull - install one by right-clicking the node with
 * the filter item in hand (sneak-right-click empty-handed to remove it again). With no filter
 * installed, behaviour is unchanged from before (pulls anything), so existing setups aren't affected.
 * Simplified from the original: one filter slot for the whole node rather than one ghost slot per
 * face, since this port's node already treats all six neighbouring faces uniformly (no per-face
 * connection toggles - see {@link MasterRoutingNodeTile}'s javadoc).
 */
public class InputRoutingNodeTile extends BaseTile {
    private ItemStack filterItem = ItemStack.EMPTY;

    public InputRoutingNodeTile(BlockPos pos, BlockState state) {
        super(BMTiles.INPUT_ROUTING_NODE_TYPE.get(), pos, state);
    }

    public ItemStack getFilterItem() {
        return filterItem;
    }

    public boolean accepts(ItemStack stack) {
        if (filterItem.isEmpty() || !(filterItem.getItem() instanceof IFilterProvider provider)) {
            return true;
        }
        return provider.matches(filterItem, stack);
    }

    public void handleInteract(Player player, ItemStack held) {
        if (level == null || level.isClientSide) {
            return;
        }

        if (held.isEmpty()) {
            if (player.isShiftKeyDown() && !filterItem.isEmpty()) {
                ItemStack removed = filterItem;
                filterItem = ItemStack.EMPTY;
                ChatUtil.sendChat(player, List.of(Component.translatable("chat.bloodmagic.routing_node.filter_removed", removed.getHoverName())));
                if (!player.getInventory().add(removed)) {
                    player.drop(removed, false);
                }
                setChanged();
            }
            return;
        }

        if (!(held.getItem() instanceof IFilterProvider)) {
            return;
        }

        ItemStack newFilter = held.copyWithCount(1);
        ItemStack oldFilter = filterItem;
        filterItem = newFilter;
        held.shrink(1);
        ChatUtil.sendChat(player, List.of(Component.translatable("chat.bloodmagic.routing_node.filter_installed", newFilter.getHoverName())));

        if (!oldFilter.isEmpty() && !player.getInventory().add(oldFilter)) {
            player.drop(oldFilter, false);
        }

        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        filterItem = tag.contains("filterItem") ? ItemStack.parseOptional(registries, tag.getCompound("filterItem")) : ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!filterItem.isEmpty()) {
            tag.put("filterItem", filterItem.save(registries));
        }
    }
}
