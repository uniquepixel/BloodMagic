package wayoftime.bloodmagic.common.sigil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.api.sigil.SigilEffect;

public record BonemealPulseEffect(int horizontalRange, int verticalRange, int chance, int upkeep) implements SigilEffect {
    public static final MapCodec<BonemealPulseEffect> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("horizontal_range").forGetter(BonemealPulseEffect::horizontalRange),
            Codec.INT.fieldOf("vertical_range").forGetter(BonemealPulseEffect::verticalRange),
            Codec.INT.fieldOf("chance").forGetter(BonemealPulseEffect::chance),
            Codec.INT.fieldOf("upkeep_cost").forGetter(BonemealPulseEffect::upkeep)
    ).apply(builder, BonemealPulseEffect::new));

    @Override
    public boolean isActivatable() {
        return true;
    }

    @Override
    public int activeTick(ItemStack sigil, Level level, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0;
        }

        BlockPos center = player.blockPosition();
        for (int x = -horizontalRange; x <= horizontalRange; x++) {
            for (int z = -horizontalRange; z <= horizontalRange; z++) {
                for (int y = -verticalRange; y <= verticalRange; y++) {
                    if (level.random.nextInt(chance) != 0) {
                        continue;
                    }

                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!(state.getBlock() instanceof BonemealableBlock growable) || state.is(Blocks.GRASS_BLOCK)) {
                        continue;
                    }

                    if (growable.isValidBonemealTarget(serverLevel, pos, state) && growable.isBonemealSuccess(level, level.random, pos, state)) {
                        growable.performBonemeal(serverLevel, level.random, pos, state);
                        level.levelEvent(2005, pos, 0);
                    }
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
