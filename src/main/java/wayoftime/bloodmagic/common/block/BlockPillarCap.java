package wayoftime.bloodmagic.common.block;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Ported from 1.20.1's common/block/base/BlockPillarCap.java (this branch has no "base" subpackage
 * for blocks - see BMBlocks for the other dungeon blocks living directly under common/block/, so this
 * follows that same flatter convention). A purely decorative pillar-capital block that orients itself
 * to whichever face it was placed against, used by the Demon Dungeon decorative palette
 * (dungeon_pillar_cap and its 4 Will-corrupted reskins - see BMBlocks). Datagen gives it two distinct
 * models (an "outward-pointing" one for up/east/south/west and a "downward-pointing" one for
 * down/north/west... see BMBlockstateProvider for the exact per-facing model/rotation table, which
 * matches 1.20.1's hand-authored dungeon_pillar_cap.json blockstate exactly) since the cap isn't
 * texture-symmetric front-to-back.
 */
public class BlockPillarCap extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public BlockPillarCap(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
