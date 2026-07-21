package wayoftime.bloodmagic.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.will.WillHelper;
import wayoftime.bloodmagic.common.will.WorldWillHelper;
import wayoftime.bloodmagic.util.ChatUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Right-click on air reports how much Will of each type the player is carrying across their Soul
 * Gems; right-click on a block reports the ambient Will aura in that block's chunk instead.
 */
public class DemonWillGaugeItem extends Item {
    public DemonWillGaugeItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        List<Component> lines = new ArrayList<>();
        for (EnumWillType type : EnumWillType.values()) {
            double amount = WillHelper.getTotalWill(type, player);
            if (amount > 0) {
                lines.add(Component.translatable("chat.bloodmagic.willgauge.line", type.toCapitalized(), ChatUtil.DECIMAL_FORMAT.format(amount)));
            }
        }
        if (lines.isEmpty()) {
            lines.add(Component.translatable("chat.bloodmagic.willgauge.empty"));
        }

        ChatUtil.sendChat(player, lines);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }

        List<Component> lines = new ArrayList<>();
        for (EnumWillType type : EnumWillType.values()) {
            double amount = WorldWillHelper.getWill(context.getLevel(), context.getClickedPos(), type);
            if (amount > 0) {
                lines.add(Component.translatable("chat.bloodmagic.willgauge.aura_line", type.toCapitalized(), ChatUtil.DECIMAL_FORMAT.format(amount)));
            }
        }
        if (lines.isEmpty()) {
            lines.add(Component.translatable("chat.bloodmagic.willgauge.aura_empty"));
        }

        ChatUtil.sendChat(context.getPlayer(), lines);
        return InteractionResult.sidedSuccess(false);
    }
}
