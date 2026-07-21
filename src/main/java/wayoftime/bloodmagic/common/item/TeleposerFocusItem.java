package wayoftime.bloodmagic.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import wayoftime.bloodmagic.common.blockentity.TeleposerTile;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;

import java.util.List;

/**
 * Binds to whichever Teleposer block it's used on; carrying it to a different Teleposer and
 * inserting it there (right-click) links that Teleposer to the bound position.
 */
public class TeleposerFocusItem extends Item {
    public TeleposerFocusItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof TeleposerTile) {
            ItemStack stack = context.getItemInHand();
            stack.set(BMDataComponents.TELEPOSITION_BINDING, GlobalPos.of(context.getLevel().dimension(), context.getClickedPos()));
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        GlobalPos bound = stack.get(BMDataComponents.TELEPOSITION_BINDING);
        if (bound != null) {
            tooltip.add(Component.translatable("tooltip.bloodmagic.teleposerfocus.coords",
                    bound.pos().getX(), bound.pos().getY(), bound.pos().getZ()).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltip, tooltipFlag);
    }
}
