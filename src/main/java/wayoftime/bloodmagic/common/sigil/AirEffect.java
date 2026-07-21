package wayoftime.bloodmagic.common.sigil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.api.sigil.SigilEffect;

public record AirEffect(int cost) implements SigilEffect {
    public static final MapCodec<AirEffect> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("cost").forGetter(AirEffect::cost)
    ).apply(builder, AirEffect::new));

    @Override
    public int useOnAir(ItemStack sigil, Player player, InteractionHand usedHand) {
        Vec3 look = player.getLookAngle();
        double velocity = 1.7;
        player.setDeltaMovement(look.x * velocity, look.y * velocity, look.z * velocity);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS, 0.5F, 2.6F + (player.level().random.nextFloat() - player.level().random.nextFloat()) * 0.8F);
        return cost;
    }

    @Override
    public MapCodec<? extends SigilEffect> codec() {
        return CODEC;
    }
}
