package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.common.blockentity.DeforesterChargeTile;

/** Ported from 1.20.1's {@code BlockDeforesterCharge}: floodfills and clears an entire attached tree (logs + leaves). */
public class DeforesterChargeBlock extends ExplosiveChargeBlock {
    private final int maxLogs;

    public DeforesterChargeBlock(int maxLogs, Properties properties) {
        super(properties);
        this.maxLogs = maxLogs;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeforesterChargeTile(pos, state, maxLogs);
    }
}
