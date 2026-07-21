package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.common.blockentity.BMTiles;
import wayoftime.bloodmagic.common.blockentity.TileDungeonAlternator;
import wayoftime.bloodmagic.util.BlockEntityHelper;

/**
 * Ported from 1.20.1's common/block/BlockAlternator.java - a decorative Demon Dungeon puzzle/switch
 * block (dungeon_alternator, see BMBlocks) that acts as a built-in redstone clock, flipping its ACTIVE
 * state (and therefore its output signal) every 40 ticks via TileDungeonAlternator. Unrelated to
 * DemonPylonTile/other "Demon"-prefixed content elsewhere in this branch - this is purely a dungeon
 * decoration/puzzle piece.
 * <p>
 * 1.20.1's updateShape/setPlacedBy called worldIn.scheduleTick(pos, this, ...) but never overrode
 * Block#tick, so those scheduled ticks were no-ops there too (vanilla's default Block#tick does
 * nothing) - the actual toggling has always come entirely from the block entity ticker below, so
 * those dead scheduleTick calls aren't ported.
 */
public class BlockAlternator extends Block implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public BlockAlternator(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(ACTIVE, false));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(ACTIVE, false);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileDungeonAlternator(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return BlockEntityHelper.getTicker(type, BMTiles.DUNGEON_ALTERNATOR_TYPE.get(), TileDungeonAlternator::tick);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(ACTIVE) ? 15 : 0;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(ACTIVE) ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }
}
