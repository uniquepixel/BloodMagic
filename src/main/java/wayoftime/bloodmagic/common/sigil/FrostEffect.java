package wayoftime.bloodmagic.common.sigil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import wayoftime.bloodmagic.api.sigil.SigilEffect;

/**
 * A simplified, self-contained take on vanilla Frost Walker (which was converted to a
 * data-driven enchantment effect component with no directly-callable Java API in 1.21.x):
 * freezes nearby water source blocks under the player's feet into frosted ice.
 */
public record FrostEffect(int upkeep) implements SigilEffect {
    public static final MapCodec<FrostEffect> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("upkeep_cost").forGetter(FrostEffect::upkeep)
    ).apply(builder, FrostEffect::new));

    @Override
    public boolean isActivatable() {
        return true;
    }

    @Override
    public int activeTick(ItemStack sigil, Level level, Player player) {
        if (level.isClientSide) {
            return 0;
        }

        BlockPos center = player.blockPosition();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x * x + z * z > 5) {
                    continue;
                }
                BlockPos pos = center.offset(x, -1, z);
                BlockState state = level.getBlockState(pos);
                if (state.getFluidState().is(Fluids.WATER) && state.getFluidState().isSource()
                        && state.getBlock() != Blocks.FROSTED_ICE
                        && level.getBlockState(pos.above()).isAir()) {
                    level.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
                }
            }
        }

        return upkeep;
    }

    @Override
    public MapCodec<? extends SigilEffect> codec() {
        return CODEC;
    }
}
