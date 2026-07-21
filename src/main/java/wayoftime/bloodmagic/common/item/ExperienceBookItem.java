package wayoftime.bloodmagic.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;

import java.util.List;

/**
 * Simplified take on the Experience Book: shift-right-click banks one experience level from the
 * player into the book; a plain right-click gives all banked experience back at once (rather than
 * one level at a time like the original).
 */
public class ExperienceBookItem extends Item {
    public ExperienceBookItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.add(Component.translatable("tooltip.bloodmagic.experience_book").withStyle(ChatFormatting.GRAY));
        int stored = stack.getOrDefault(BMDataComponents.STORED_EXPERIENCE, 0);
        if (stored > 0) {
            tooltip.add(Component.translatable("tooltip.bloodmagic.experience_book.stored", stored).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltip, tooltipFlag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        int stored = stack.getOrDefault(BMDataComponents.STORED_EXPERIENCE, 0);
        if (player.isShiftKeyDown()) {
            if (player.experienceLevel > 0 || player.experienceProgress > 0) {
                int xpForLevel = player.getXpNeededForNextLevel();
                player.giveExperienceLevels(-1);
                stack.set(BMDataComponents.STORED_EXPERIENCE, stored + xpForLevel);
            }
        } else if (stored > 0) {
            player.giveExperiencePoints(stored);
            stack.set(BMDataComponents.STORED_EXPERIENCE, 0);
        }

        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
