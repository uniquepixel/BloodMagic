package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Ported from 1.20.1's {@code TileShapedExplosive}: no floodfill/analysis phase - the fuse starts
 * immediately on placement and, once it reaches 100 ticks, digs a fixed {@code radius}-by-{@code
 * radius} square {@code depth} blocks deep into whatever it's attached to.
 */
public class ShapedChargeTile extends ExplosiveChargeTile {
    private int radius;
    private int depth;

    public ShapedChargeTile(BlockPos pos, BlockState state, int radius, int depth) {
        super(BMTiles.SHAPED_CHARGE_TYPE.get(), pos, state);
        this.radius = radius;
        this.depth = depth;
    }

    @Override
    protected void detonate(ServerLevel level, BlockPos pos, Direction chargeDirection) {
        Direction sweepDir1;
        Direction sweepDir2;
        switch (chargeDirection) {
            case EAST, WEST -> {
                sweepDir1 = Direction.NORTH;
                sweepDir2 = Direction.UP;
            }
            case NORTH, SOUTH -> {
                sweepDir1 = Direction.EAST;
                sweepDir2 = Direction.UP;
            }
            default -> {
                sweepDir1 = Direction.NORTH;
                sweepDir2 = Direction.EAST;
            }
        }

        List<BlockPos> positions = new ArrayList<>();
        for (int i = 1; i <= depth; i++) {
            for (int j = -radius; j <= radius; j++) {
                for (int k = -radius; k <= radius; k++) {
                    positions.add(pos.relative(chargeDirection, i).relative(sweepDir1, j).relative(sweepDir2, k));
                }
            }
        }

        ItemStack tool = getHarvestingTool();
        breakAndCollectDrops(level, positions, tool);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        radius = tag.getInt("radius");
        depth = tag.getInt("depth");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("radius", radius);
        tag.putInt("depth", depth);
    }
}
