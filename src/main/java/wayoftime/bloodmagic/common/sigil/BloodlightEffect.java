package wayoftime.bloodmagic.common.sigil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.api.sigil.SigilEffect;
import wayoftime.bloodmagic.common.block.BMBlocks;

public record BloodlightEffect(int cost) implements SigilEffect {
    public static final MapCodec<BloodlightEffect> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("cost").forGetter(BloodlightEffect::cost)
    ).apply(builder, BloodlightEffect::new));

    @Override
    public int useOnBlock(ItemStack sigil, Player player, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos target = context.getClickedPos().relative(context.getClickedFace());
        if (!level.getBlockState(target).isAir()) {
            return 0;
        }

        level.setBlockAndUpdate(target, BMBlocks.BLOOD_LIGHT.get().defaultBlockState());
        return cost;
    }

    @Override
    public MapCodec<? extends SigilEffect> codec() {
        return CODEC;
    }
}
