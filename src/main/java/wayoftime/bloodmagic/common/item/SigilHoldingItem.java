package wayoftime.bloodmagic.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.menu.HoldingMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds up to {@link HoldingMenu#SLOTS} sigils, delegating interactions to whichever one occupies
 * the first non-empty slot. The GUI opens either via the "Open Sigil of Holding" keybind
 * ({@link #openFromKeybind}) or by sneak-clicking a block/entity - sneak+air-click is deliberately
 * left alone so it still reaches the held sigil's own toggle behavior (the only gesture where that
 * matters for an activatable sigil).
 */
public class SigilHoldingItem extends Item {
    public SigilHoldingItem() {
        super(new Properties().stacksTo(1));
    }

    private static ItemStack getActiveSigil(ItemStack holding) {
        ItemContainerContents contents = holding.getOrDefault(BMDataComponents.HOLDING_CONTENTS, ItemContainerContents.EMPTY);
        for (int i = 0; i < HoldingMenu.SLOTS; i++) {
            ItemStack stack = contents.getStackInSlot(i);
            if (!stack.isEmpty()) {
                return stack.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private static void writeBackActiveSigil(ItemStack holding, ItemStack mutated) {
        ItemContainerContents contents = holding.getOrDefault(BMDataComponents.HOLDING_CONTENTS, ItemContainerContents.EMPTY);
        List<ItemStack> list = new ArrayList<>(HoldingMenu.SLOTS);
        boolean written = false;
        for (int i = 0; i < HoldingMenu.SLOTS; i++) {
            ItemStack stack = contents.getStackInSlot(i);
            if (!written && !stack.isEmpty()) {
                list.add(mutated);
                written = true;
            } else {
                list.add(stack);
            }
        }
        holding.set(BMDataComponents.HOLDING_CONTENTS, ItemContainerContents.fromItems(list));
    }

    /**
     * Entry point for the "Open Sigil of Holding" keybind (see {@code wayoftime.bloodmagic.network}):
     * finds a Sigil of Holding in either hand and opens its contents GUI directly, without needing
     * the sneak-click-on-a-block/entity workaround the bare item interaction relies on.
     */
    public static void openFromKeybind(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof SigilHoldingItem) {
            openMenu(player, mainHand, InteractionHand.MAIN_HAND);
            return;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof SigilHoldingItem) {
            openMenu(player, offHand, InteractionHand.OFF_HAND);
        }
    }

    private static void openMenu(Player player, ItemStack holding, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int lockedHotbarSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : -1;
        serverPlayer.openMenu(new SimpleMenuProvider(
                (containerId, inv, p) -> new HoldingMenu(containerId, inv, holding, lockedHotbarSlot),
                Component.translatable("item.bloodmagic.sigil_holding")
        ), buf -> {});
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        ItemStack holding = context.getItemInHand();
        if (player.isShiftKeyDown()) {
            if (!context.getLevel().isClientSide) {
                openMenu(player, holding, context.getHand());
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        ItemStack active = getActiveSigil(holding);
        if (active.isEmpty()) {
            return InteractionResult.PASS;
        }

        InteractionResult result = SigilItem.useSigilOn(active, context, player);
        writeBackActiveSigil(holding, active);
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack holding = player.getItemInHand(usedHand);
        ItemStack active = getActiveSigil(holding);
        if (active.isEmpty()) {
            return InteractionResultHolder.pass(holding);
        }

        InteractionResultHolder<ItemStack> result = SigilItem.useSigil(active, level, player, usedHand);
        writeBackActiveSigil(holding, active);
        return new InteractionResultHolder<>(result.getResult(), holding);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (player.isShiftKeyDown()) {
            if (!player.level().isClientSide) {
                openMenu(player, stack, usedHand);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }

        ItemStack active = getActiveSigil(stack);
        if (active.isEmpty()) {
            return InteractionResult.PASS;
        }

        InteractionResult result = SigilItem.useSigilOnEntity(active, player, interactionTarget);
        writeBackActiveSigil(stack, active);
        return result;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }

        ItemStack active = getActiveSigil(stack);
        if (active.isEmpty()) {
            return;
        }

        SigilItem.tickSigil(active, level, player);
        writeBackActiveSigil(stack, active);
    }
}
