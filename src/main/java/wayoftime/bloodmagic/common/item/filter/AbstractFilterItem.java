package wayoftime.bloodmagic.common.item.filter;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.item.routing.IFilterProvider;
import wayoftime.bloodmagic.common.menu.FilterMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Restores 1.20.1's Filter item system (its {@code ItemRouterFilter}/{@code ItemStandardFilter}
 * base classes) as a leaner but functionally equivalent base: a Filter item carries a small grid of
 * reference item slots (see {@link FilterMenu}, opened by right-clicking) plus a whitelist/blacklist
 * toggle, both stored as data components ({@link BMDataComponents#FILTER_CONTENTS}/
 * {@link BMDataComponents#FILTER_BLACKLIST}) so they persist and sync like any other item data.
 * <p>
 * Concrete subclasses only need to say how one reference stack turns into an {@link wayoftime.bloodmagic.common.item.routing.IFilterKey}
 * (see {@link #buildKey(ItemStack)}) - the whitelist/blacklist combination and empty-filter-matches-
 * everything behaviour are handled once here via {@link IFilterProvider#matches}.
 * <p>
 * Not ported from the original: per-slot cycle buttons for picking a specific tag/enchantment out of
 * several candidates on one reference item (see {@code ItemTagFilter}/{@code ItemEnchantFilterCore}
 * in 1.20.1) - {@link wayoftime.bloodmagic.common.item.filter.TagFilterItem}/{@link EnchantFilterItem}
 * instead match against ALL of the reference item's tags/enchantments at once, which is a strict
 * superset of "pick one" and needs no extra GUI chrome.
 */
public abstract class AbstractFilterItem extends Item implements MenuProvider, IFilterProvider {
    public static final int SLOTS = 9;

    public AbstractFilterItem() {
        super(new Item.Properties().stacksTo(1));
    }

    /** True only for {@link CompositeFilterItem}: restricts its slots to other nestable filter items. */
    public boolean isNestedSlots() {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            int heldSlot = player.getInventory().selected;
            boolean nestedSlots = isNestedSlots();
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, inv, p) -> new FilterMenu(containerId, inv, stack, nestedSlots, heldSlot),
                    stack.getHoverName()
            ), buf -> {
                buf.writeBoolean(nestedSlots);
                buf.writeInt(heldSlot);
            });
        }

        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide), stack);
    }

    @Override
    public Component getDisplayName() {
        return getDescription();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory inv, Player player) {
        // Only reached if something else (e.g. a future generic "open this item's menu" hook) calls
        // it directly; use() above is the normal entry point and already builds the real ItemStack-
        // backed menu, so this just mirrors it using the main-hand stack.
        ItemStack stack = player.getMainHandItem();
        return new FilterMenu(containerId, inv, stack, isNestedSlots(), player.getInventory().selected);
    }

    protected List<ItemStack> getContents(ItemStack filterStack) {
        ItemContainerContents contents = filterStack.getOrDefault(BMDataComponents.FILTER_CONTENTS, ItemContainerContents.EMPTY);
        List<ItemStack> list = new ArrayList<>(SLOTS);
        for (int i = 0; i < SLOTS; i++) {
            list.add(i < contents.getSlots() ? contents.getStackInSlot(i) : ItemStack.EMPTY);
        }
        return list;
    }

    @Override
    public boolean isBlacklist(ItemStack filterStack) {
        return filterStack.getOrDefault(BMDataComponents.FILTER_BLACKLIST, Boolean.FALSE);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(descriptionKey()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

        boolean blacklist = isBlacklist(stack);
        tooltip.add(Component.translatable(blacklist ? "tooltip.bloodmagic.filter.blacklist" : "tooltip.bloodmagic.filter.whitelist").withStyle(ChatFormatting.GRAY));

        for (ItemStack content : getContents(stack)) {
            if (!content.isEmpty()) {
                tooltip.add(content.getHoverName().copy().withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    protected abstract String descriptionKey();
}
