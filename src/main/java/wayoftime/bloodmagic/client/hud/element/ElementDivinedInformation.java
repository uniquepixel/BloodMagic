package wayoftime.bloodmagic.client.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import wayoftime.bloodmagic.api.BMIdentifiers;
import wayoftime.bloodmagic.api.sigil.SigilEffect;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.item.SigilHoldingItem;
import wayoftime.bloodmagic.common.item.SigilItem;
import wayoftime.bloodmagic.common.menu.HoldingMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ElementTileInformation} that additionally only renders while the looked-at block entity
 * matches AND the player is carrying a Divination or Seer Sigil (bare, or tucked inside a Sigil of
 * Holding) - the "point a divination sigil at an altar and get a live readout" HUD element.
 * <p>
 * Ported from 1.20.1's {@code ElementDivinedInformation}, adapted for this branch's sigil rework:
 * 1.20.1 had separate {@code ItemSigilDivination}/seer item classes to check {@code getItem() ==}
 * against; this branch has a single generic {@link SigilItem} whose behavior is selected by the
 * {@link BMDataComponents#SIGIL_EFFECT} data component instead, so the gating below compares that
 * component's {@link ResourceKey} to {@link BMIdentifiers.Sigils#DIVINATION}/{@code SEER} instead
 * of comparing {@code Item} instances.
 * <p>
 * 1.20.1 additionally had a "simple" (Divination-only, short) vs "advanced" (Seer-only, longer -
 * altar crafting progress, consumption rate, total charge) variant of the Blood Altar readout.
 * {@link wayoftime.bloodmagic.common.blockentity.BloodAltarTile} on this branch doesn't expose
 * public getters for that crafting progress/consumption-rate/total-charge state (they're private
 * fields with no accessor), and adding one would mean touching {@code common/blockentity}, which
 * is out of scope for this client-only HUD port - so that distinction is collapsed here: every
 * registration of this element (in {@link wayoftime.bloodmagic.client.hud.Elements}) shows the
 * same compact readout for either sigil type, matching 1.20.1's own Incense Altar element (which
 * never had a separate advanced variant to begin with).
 */
public abstract class ElementDivinedInformation<T extends BlockEntity> extends ElementTileInformation<T> {
    public ElementDivinedInformation(int lines, Class<T> tileClass) {
        super(100, lines, tileClass);
    }

    @Override
    public boolean shouldRender(Minecraft minecraft) {
        HitResult trace = minecraft.hitResult;
        if (trace == null || trace.getType() != HitResult.Type.BLOCK)
            return false;

        BlockEntity tile = minecraft.level.getBlockEntity(((BlockHitResult) trace).getBlockPos());
        if (tile == null || !tileClass.isAssignableFrom(tile.getClass()))
            return false;

        Player player = minecraft.player;
        return hasSigil(player, BMIdentifiers.Sigils.DIVINATION) || hasSigil(player, BMIdentifiers.Sigils.SEER);
    }

    private static boolean hasSigil(Player player, ResourceKey<SigilEffect> effect) {
        for (ItemStack stack : allSlots(player)) {
            if (matches(stack, effect)) {
                return true;
            }
            if (stack.getItem() instanceof SigilHoldingItem) {
                ItemContainerContents contents = stack.getOrDefault(BMDataComponents.HOLDING_CONTENTS, ItemContainerContents.EMPTY);
                for (int i = 0; i < HoldingMenu.SLOTS; i++) {
                    if (matches(contents.getStackInSlot(i), effect)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean matches(ItemStack stack, ResourceKey<SigilEffect> effect) {
        if (!(stack.getItem() instanceof SigilItem)) {
            return false;
        }
        return stack.getOrDefault(BMDataComponents.SIGIL_EFFECT, BMIdentifiers.Sigils.DIVINATION).equals(effect);
    }

    // Mirrors wayoftime.bloodmagic.common.will.WillHelper#allSlots - main inventory, armor and
    // offhand. No dedicated shared helper for this exists yet on this branch (the 1.20.1
    // InventoryHelper#getActiveInventories this was ported from, which also scanned Curios slots,
    // has no equivalent here).
    private static List<ItemStack> allSlots(Player player) {
        List<ItemStack> list = new ArrayList<>();
        list.addAll(player.getInventory().items);
        list.addAll(player.getInventory().armor);
        list.addAll(player.getInventory().offhand);
        return list;
    }
}
