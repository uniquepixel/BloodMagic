package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.common.blockentity.ShapedChargeTile;

/** Ported from 1.20.1's {@code BlockShapedExplosive}: digs a plain cuboid volume out of whatever it's stuck to. */
public class ShapedChargeBlock extends ExplosiveChargeBlock {
    private final int radius;
    private final int depth;

    public ShapedChargeBlock(int radius, int depth, Properties properties) {
        super(properties);
        this.radius = radius;
        this.depth = depth;
    }

    public ShapedChargeBlock(int radius, Properties properties) {
        this(radius, radius * 2 + 1, properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShapedChargeTile(pos, state, radius, depth);
    }
}
