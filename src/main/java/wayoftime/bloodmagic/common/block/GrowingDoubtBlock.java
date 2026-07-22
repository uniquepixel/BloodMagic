package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import wayoftime.bloodmagic.common.item.BMItems;

/**
 * Full-fidelity port of 1.20.1's {@code BlockGrowingDoubt} ("Creeping Doubt"): a Blood-Magic-only
 * crop that, unlike vanilla crops, requires {@link NetherSoilBlock} rather than farmland to be
 * planted on. Ported alongside {@link TauBlock}; see {@link wayoftime.bloodmagic.common.ritual.harvest.HarvestHandlerCrop}
 * for why both were previously missing (the Gathering of the Zephyr / Crack of the Fractured Crystal
 * plumbing that harvests any max-age {@link CropBlock} generically already works on these once
 * registered - no harvest-handler changes needed).
 */
public class GrowingDoubtBlock extends CropBlock {
    private static final VoxelShape[] SHAPES = new VoxelShape[]{
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 9.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 11.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)
    };

    public GrowingDoubtBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BMBlocks.NETHER_SOIL.block().get());
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return BMItems.GROWING_DOUBT_SEED.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[this.getAge(state)];
    }
}
