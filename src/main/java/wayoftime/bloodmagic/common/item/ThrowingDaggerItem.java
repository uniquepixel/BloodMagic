package wayoftime.bloodmagic.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.entity.ThrowingDaggerEntity;

import java.util.List;

/**
 * Full-fidelity port of 1.20.1's Throwing Dagger ({@code ItemThrowingDagger}): right-click to
 * throw one from the stack as a real projectile.
 */
public class ThrowingDaggerItem extends Item {
    public ThrowingDaggerItem() {
        super(new Item.Properties().stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, 10);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide) {
            ThrowingDaggerEntity dagger = new ThrowingDaggerEntity(level, player);
            dagger.setItem(new ItemStack(this));
            dagger.setDamage(10);
            dagger.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 0.5F);
            level.addFreshEntity(dagger);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.bloodmagic.throwing_dagger.desc").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
