package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.common.item.routing.IFilterProvider;
import wayoftime.bloodmagic.util.ChatUtil;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A node in the Item Routing network (see {@link MasterRoutingNodeTile}): marks one of its six
 * neighboring external inventories as a destination the network can push items into, gated by an
 * optional item whitelist/blacklist and ordered by priority (higher priority nodes are filled
 * first). Right-click with a plain item to toggle it in the built-in filter; right-click empty-handed
 * to cycle priority; sneak-right-click empty-handed to flip whitelist/blacklist mode.
 * <p>
 * Restores 1.20.1's Filter item plumbing on top of that built-in filter: right-click with a Filter
 * item (see {@link wayoftime.bloodmagic.common.item.filter.AbstractFilterItem}) in hand installs it
 * into a dedicated filter slot instead of toggling the built-in list, giving access to tag/mod
 * id/enchantment/composite matching (see {@link #accepts(ItemStack)}, which requires BOTH the
 * built-in filter and the installed Filter item - if present - to pass). Sneak-right-click
 * empty-handed removes an installed Filter item. Nodes with no Filter item installed behave exactly
 * as before.
 */
public class OutputRoutingNodeTile extends BaseTile {
    private static final int MAX_FILTER_SIZE = 9;

    private final Set<Item> filter = new LinkedHashSet<>();
    private boolean blacklist = false;
    private int priority = 0;
    private ItemStack filterItem = ItemStack.EMPTY;

    public OutputRoutingNodeTile(BlockPos pos, BlockState state) {
        super(BMTiles.OUTPUT_ROUTING_NODE_TYPE.get(), pos, state);
    }

    public int getPriority() {
        return priority;
    }

    public ItemStack getFilterItem() {
        return filterItem;
    }

    private boolean acceptsBuiltIn(Item item) {
        if (filter.isEmpty()) {
            return true;
        }
        boolean contains = filter.contains(item);
        return blacklist != contains;
    }

    public boolean accepts(ItemStack stack) {
        if (!acceptsBuiltIn(stack.getItem())) {
            return false;
        }
        if (!filterItem.isEmpty() && filterItem.getItem() instanceof IFilterProvider provider) {
            return provider.matches(filterItem, stack);
        }
        return true;
    }

    public void handleInteract(Player player, ItemStack held) {
        if (level == null || level.isClientSide) {
            return;
        }

        if (held.isEmpty()) {
            if (player.isShiftKeyDown()) {
                if (!filterItem.isEmpty()) {
                    ItemStack removed = filterItem;
                    filterItem = ItemStack.EMPTY;
                    ChatUtil.sendChat(player, List.of(Component.translatable("chat.bloodmagic.routing_node.filter_removed", removed.getHoverName())));
                    if (!player.getInventory().add(removed)) {
                        player.drop(removed, false);
                    }
                } else {
                    blacklist = !blacklist;
                    ChatUtil.sendChat(player, List.of(Component.translatable(blacklist ? "chat.bloodmagic.routing_node.blacklist" : "chat.bloodmagic.routing_node.whitelist")));
                }
            } else {
                priority = (priority + 1) % 4;
                ChatUtil.sendChat(player, List.of(Component.translatable("chat.bloodmagic.routing_node.priority", priority)));
            }
            setChanged();
            return;
        }

        if (held.getItem() instanceof IFilterProvider) {
            ItemStack newFilter = held.copyWithCount(1);
            ItemStack oldFilter = filterItem;
            filterItem = newFilter;
            held.shrink(1);
            ChatUtil.sendChat(player, List.of(Component.translatable("chat.bloodmagic.routing_node.filter_installed", newFilter.getHoverName())));
            if (!oldFilter.isEmpty() && !player.getInventory().add(oldFilter)) {
                player.drop(oldFilter, false);
            }
            setChanged();
            return;
        }

        Item item = held.getItem();
        if (filter.remove(item)) {
            ChatUtil.sendChat(player, List.of(Component.translatable("chat.bloodmagic.item_router.removed", held.getHoverName())));
        } else if (filter.size() < MAX_FILTER_SIZE) {
            filter.add(item);
            ChatUtil.sendChat(player, List.of(Component.translatable("chat.bloodmagic.item_router.added", held.getHoverName())));
        } else {
            ChatUtil.sendChat(player, List.of(Component.translatable("chat.bloodmagic.item_router.full")));
        }

        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        filter.clear();
        ListTag list = tag.getList("filter", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(list.getString(i)));
            if (item != null) {
                filter.add(item);
            }
        }
        blacklist = tag.getBoolean("blacklist");
        priority = tag.getInt("priority");
        filterItem = tag.contains("filterItem") ? ItemStack.parseOptional(registries, tag.getCompound("filterItem")) : ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (Item item : filter) {
            list.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
        }
        tag.put("filter", list);
        tag.putBoolean("blacklist", blacklist);
        tag.putInt("priority", priority);
        if (!filterItem.isEmpty()) {
            tag.put("filterItem", filterItem.save(registries));
        }
    }
}
