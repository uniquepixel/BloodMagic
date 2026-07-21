package wayoftime.bloodmagic.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import wayoftime.bloodmagic.api.datacomponent.Binding;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;

import java.util.List;
import java.util.Locale;

/**
 * Ported from 1.20.1: bound to a player (via the shared {@link Binding} data component, same
 * mechanism as Blood Orbs), used to activate a Master Ritual Stone. The stone only activates a
 * ritual whose {@link wayoftime.bloodmagic.api.ritual.Ritual#getCrystalLevel()} is at or below
 * this crystal's level.
 */
public class ItemActivationCrystal extends Item {
    private final CrystalType type;

    public ItemActivationCrystal(CrystalType type) {
        super(new Item.Properties().stacksTo(1).component(BMDataComponents.BINDING, Binding.EMPTY));
        this.type = type;
    }

    public CrystalType getType() {
        return type;
    }

    public int getCrystalLevel() {
        return type == CrystalType.CREATIVE ? Integer.MAX_VALUE : type.ordinal() + 1;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.bloodmagic.activation_crystal." + type.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.GRAY));

        Binding binding = stack.get(BMDataComponents.BINDING);
        if (binding != null && !binding.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.bloodmagic.current_owner", binding.name()).withStyle(ChatFormatting.GRAY));
        }

        super.appendHoverText(stack, context, tooltip, flag);
    }

    public enum CrystalType {
        WEAK,
        AWAKENED,
        CREATIVE;

        public static ItemStack getStack(int level) {
            if (level < 0) {
                level = 0;
            }
            return switch (level) {
                case 0 -> new ItemStack(BMItems.ACTIVATION_CRYSTAL_WEAK.get());
                case 1 -> new ItemStack(BMItems.ACTIVATION_CRYSTAL_AWAKENED.get());
                default -> new ItemStack(BMItems.ACTIVATION_CRYSTAL_CREATIVE.get());
            };
        }
    }
}
