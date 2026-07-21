package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.common.blockentity.VeinMineChargeTile;

/** Ported from 1.20.1's {@code BlockVeinMineCharge}: floodfills and clears an entire connected vein of one block type - e.g. an ore vein in one go. */
public class VeinMineChargeBlock extends ExplosiveChargeBlock {
    private final int maxBlocks;

    public VeinMineChargeBlock(int maxBlocks, Properties properties) {
        super(properties);
        this.maxBlocks = maxBlocks;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VeinMineChargeTile(pos, state, maxBlocks);
    }
}
