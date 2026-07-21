package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Placed by the Bloodlight sigil; not obtainable as an item, matching the 1.20.1 original
 * (a permanent light source with no collision that you punch away like a torch).
 */
public class BloodlightBlock extends Block {
    private static final VoxelShape SHAPE = box(7, 7, 7, 9, 9, 9);

    public BloodlightBlock() {
        super(
                BlockBehaviour.Properties.of()
                        .noCollission()
                        .noOcclusion()
                        .lightLevel(state -> 15)
                        .instabreak()
                        .ignitedByLava()
        );
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
