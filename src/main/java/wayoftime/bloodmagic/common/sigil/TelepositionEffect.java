package wayoftime.bloodmagic.common.sigil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.api.sigil.SigilEffect;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.util.ChatUtil;

import java.util.List;

/**
 * Simplified take on the Sigil of Teleposition: rather than requiring a bound Teleposer block on
 * the other end and swapping the two locations, this binds to any block right-clicked and warps
 * the player back to that stored point on the next air-use. No cross-block-swap, no dedicated
 * Teleposer block ecosystem.
 */
public record TelepositionEffect(int cost) implements SigilEffect {
    public static final MapCodec<TelepositionEffect> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("cost").forGetter(TelepositionEffect::cost)
    ).apply(builder, TelepositionEffect::new));

    @Override
    public int useOnBlock(ItemStack sigil, Player player, UseOnContext context) {
        GlobalPos bound = GlobalPos.of(context.getLevel().dimension(), context.getClickedPos());
        sigil.set(BMDataComponents.TELEPOSITION_BINDING, bound);
        ChatUtil.sendChat(player, List.of(Component.translatable("chat.bloodmagic.teleposition.bound",
                bound.pos().getX(), bound.pos().getY(), bound.pos().getZ())));
        return 0;
    }

    @Override
    public int useOnAir(ItemStack sigil, Player player, InteractionHand usedHand) {
        GlobalPos bound = sigil.get(BMDataComponents.TELEPOSITION_BINDING);
        if (bound == null || !(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }

        ServerLevel targetLevel = serverPlayer.server.getLevel(bound.dimension());
        if (targetLevel == null) {
            return 0;
        }

        BlockPos pos = bound.pos();
        Level originLevel = player.level();
        originLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        serverPlayer.teleportTo(targetLevel, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, player.getYRot(), player.getXRot());
        targetLevel.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        return cost;
    }

    @Override
    public MapCodec<? extends SigilEffect> codec() {
        return CODEC;
    }
}
