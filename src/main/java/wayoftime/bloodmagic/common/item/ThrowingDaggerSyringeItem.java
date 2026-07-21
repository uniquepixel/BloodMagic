package wayoftime.bloodmagic.common.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.entity.ThrowingDaggerSyringeEntity;

/**
 * Full-fidelity port of 1.20.1's Syringe Throwing Dagger ({@code ItemThrowingDaggerSyringe}): a
 * variant of {@link ThrowingDaggerItem} that throws a faster, slightly weaker dagger which harvests
 * Slate Ampoules from its killing blows (see {@link ThrowingDaggerSyringeEntity}). 1.20.1 didn't
 * override the tooltip for this variant either, so it inherits the same "quick, unremarkable damage"
 * blurb as the base dagger from {@link ThrowingDaggerItem#appendHoverText}.
 */
public class ThrowingDaggerSyringeItem extends ThrowingDaggerItem {
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, 10);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide) {
            ThrowingDaggerSyringeEntity dagger = new ThrowingDaggerSyringeEntity(level, player);
            dagger.setItem(new ItemStack(this));
            dagger.setDamage(8);
            dagger.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 0.5F);
            level.addFreshEntity(dagger);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
