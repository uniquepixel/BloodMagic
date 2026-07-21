package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.common.blockentity.FungalChargeTile;

/** Ported from 1.20.1's {@code BlockFungalCharge}: floodfills and clears an entire connected huge-mushroom growth (cap + stem blocks). */
public class FungalChargeBlock extends ExplosiveChargeBlock {
    private final int maxBlocks;

    public FungalChargeBlock(int maxBlocks, Properties properties) {
        super(properties);
        this.maxBlocks = maxBlocks;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FungalChargeTile(pos, state, maxBlocks);
    }
}
