package wayoftime.bloodmagic.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.FakePlayer;
import wayoftime.bloodmagic.common.blockentity.BloodAltarTile;
import wayoftime.bloodmagic.util.AltarUtil;

import java.util.List;

/**
 * Full-fidelity port of 1.20.1's Slate Ampoule ({@code ItemBloodProvider("slate", 500)}): right-click
 * near a Blood Altar to pour a fixed 500 LP straight into its main tank (no sacrifice/self-sacrifice
 * multiplier - see {@link BloodAltarTile#fillMainTank(int)}), consuming one Ampoule. Harvested from
 * killing blows dealt with the {@link ThrowingDaggerSyringeItem}'s dagger - see
 * {@code wayoftime.bloodmagic.common.entity.ThrowingDaggerSyringeEntity}.
 */
public class SlateAmpouleItem extends Item {
    public static final int LP_PROVIDED = 500;

    public SlateAmpouleItem() {
        super(new Item.Properties().stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof FakePlayer) {
            return super.use(level, player, hand);
        }

        BlockPos altarPos = AltarUtil.findAltar(level, player.blockPosition(), 2);
        if (altarPos == null) {
            return super.use(level, player, hand);
        }

        BlockEntity be = level.getBlockEntity(altarPos);
        if (!(be instanceof BloodAltarTile altar)) {
            return super.use(level, player, hand);
        }

        double posX = player.getX();
        double posY = player.getY();
        double posZ = player.getZ();
        level.playSound(null, posX, posY, posZ, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        for (int i = 0; i < 8; i++) {
            level.addParticle(DustParticleOptions.REDSTONE, posX + Math.random() - Math.random(), posY + Math.random() - Math.random(), posZ + Math.random() - Math.random(), 0, 0, 0);
        }

        altar.fillMainTank(LP_PROVIDED);
        if (!player.isCreative()) {
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.bloodmagic.blood_provider.slate.desc").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
