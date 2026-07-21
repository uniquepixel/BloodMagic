package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import wayoftime.bloodmagic.common.item.routing.IFilterProvider;
import wayoftime.bloodmagic.util.ChatUtil;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Standalone alternative to the full Routing Node network: an enhanced hopper that pulls from the
 * four horizontal sides and above into whatever's below it, gated by an optional small built-in
 * item whitelist (right-click with a plain item to toggle it in/out; an empty filter allows
 * everything, matching a hopper).
 * <p>
 * Restores 1.20.1's Filter item plumbing (this block corresponds to the original's use of
 * {@code ItemRouterFilter}-derived items): right-click with a Filter item (see
 * {@link wayoftime.bloodmagic.common.item.filter.AbstractFilterItem}) in hand installs it into a
 * dedicated filter slot, adding tag/mod id/enchantment/composite matching on top of the built-in
 * whitelist (both must pass - see {@link #accepts(ItemStack)}). Sneak-right-click empty-handed
 * removes an installed Filter item.
 */
public class ItemRouterTile extends BaseTile {
    private static final int MAX_FILTER_SIZE = 4;
    private static final Direction[] INPUT_SIDES = {Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private final Set<Item> filter = new LinkedHashSet<>();
    private ItemStack filterItem = ItemStack.EMPTY;

    public ItemRouterTile(BlockPos pos, BlockState state) {
        super(BMTiles.ITEM_ROUTER_TYPE.get(), pos, state);
    }

    public ItemStack getFilterItem() {
        return filterItem;
    }

    public boolean accepts(ItemStack stack) {
        if (!filter.isEmpty() && !filter.contains(stack.getItem())) {
            return false;
        }
        if (!filterItem.isEmpty() && filterItem.getItem() instanceof IFilterProvider provider) {
            return provider.matches(filterItem, stack);
        }
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ItemRouterTile tile) {
        if (level.isClientSide || level.getGameTime() % 4 != 0) {
            return;
        }

        IItemHandler output = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.below(), Direction.UP);
        if (output == null) {
            return;
        }

        for (Direction side : INPUT_SIDES) {
            IItemHandler source = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(side), side.getOpposite());
            if (source == null) {
                continue;
            }

            for (int slot = 0; slot < source.getSlots(); slot++) {
                ItemStack peek = source.extractItem(slot, 1, true);
                if (peek.isEmpty() || !tile.accepts(peek)) {
                    continue;
                }

                ItemStack remainder = ItemHandlerHelper.insertItem(output, peek, true);
                if (!remainder.isEmpty()) {
                    continue;
                }

                ItemStack extracted = source.extractItem(slot, 1, false);
                if (!extracted.isEmpty()) {
                    ItemHandlerHelper.insertItem(output, extracted, false);
                    return;
                }
            }
        }
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
        if (!filterItem.isEmpty()) {
            tag.put("filterItem", filterItem.save(registries));
        }
    }
}
