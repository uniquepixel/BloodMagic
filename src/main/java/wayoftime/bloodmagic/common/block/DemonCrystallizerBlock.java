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
import wayoftime.bloodmagic.common.blockentity.DemonCrystallizerTile;
import wayoftime.bloodmagic.util.BlockEntityHelper;

/** Ported from 1.20.1's {@code BlockDemonCrystallizer}. No player interaction of its own - see {@link DemonCrystallizerTile}. */
public class DemonCrystallizerBlock extends Block implements EntityBlock {
    public DemonCrystallizerBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(2.0F, 5.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DemonCrystallizerTile(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return BlockEntityHelper.getTicker(blockEntityType, BMTiles.DEMON_CRYSTALLIZER_TYPE.get(), DemonCrystallizerTile::tick);
    }
}
