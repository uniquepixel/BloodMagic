package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.common.blockentity.BMTiles;
import wayoftime.bloodmagic.common.blockentity.MasterRoutingNodeTile;
import wayoftime.bloodmagic.util.BlockEntityHelper;

public class MasterRoutingNodeBlock extends Block implements EntityBlock {
    public MasterRoutingNodeBlock() {
        super(
                BlockBehaviour.Properties.of()
                        .requiresCorrectToolForDrops()
                        .strength(3, 5)
                        .sound(SoundType.METAL)
        );
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MasterRoutingNodeTile(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return BlockEntityHelper.getTicker(blockEntityType, BMTiles.MASTER_ROUTING_NODE_TYPE.get(), MasterRoutingNodeTile::tick);
    }
}
