package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Ported from 1.20.1's {@code TileVeinMineCharge}: while the attached block can be broken at all
 * (destroy speed != -1), floodfills outward through the 6 cardinal + 12 diagonal-edge neighbors
 * collecting every connected block of that exact same type (an ore vein, most usefully), budgeted
 * by total block count, one BFS layer per tick. {@link FungalChargeTile} overrides the validity
 * checks below to instead chase a connected huge-mushroom growth, reusing this floodfill verbatim -
 * mirroring the original's own inheritance relationship between the two.
 */
public class VeinMineChargeTile extends ExplosiveChargeTile {
    private static final Vec3i[] DIAGONALS = new Vec3i[]{
            new Vec3i(0, 1, 1), new Vec3i(0, 1, -1), new Vec3i(0, -1, 1), new Vec3i(0, -1, -1),
            new Vec3i(1, 0, 1), new Vec3i(-1, 0, 1), new Vec3i(1, 0, -1), new Vec3i(-1, 0, -1),
            new Vec3i(1, 1, 0), new Vec3i(-1, 1, 0), new Vec3i(1, -1, 0), new Vec3i(-1, -1, 0)
    };

    private int maxBlocks;
    private int currentBlocks = 0;

    private Map<BlockPos, Boolean> veinParts;
    private List<BlockPos> veinPartsCache;

    public VeinMineChargeTile(BlockPos pos, BlockState state, int maxBlocks) {
        this(BMTiles.VEIN_MINE_CHARGE_TYPE.get(), pos, state, maxBlocks);
    }

    protected VeinMineChargeTile(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxBlocks) {
        super(type, pos, state);
        this.maxBlocks = maxBlocks;
    }

    /** Whether {@code candidate} is part of the same structure as {@code original} (the attached starting block). */
    protected boolean isValidBlock(BlockState original, BlockState candidate) {
        return original.getBlock() == candidate.getBlock();
    }

    /** Whether the attached block is a valid place to start the floodfill from at all. */
    protected boolean isValidStartingBlock(Level level, BlockPos pos, BlockState state) {
        return state.getDestroySpeed(level, pos) != -1.0F;
    }

    protected boolean checkDiagonals() {
        return true;
    }

    @Override
    protected boolean readyToDetonate(Level level, BlockPos pos, Direction chargeDirection) {
        BlockPos startPos = pos.relative(chargeDirection);
        BlockState startState = level.getBlockState(startPos);
        if (!isValidStartingBlock(level, startPos, startState)) {
            return false;
        }

        if (veinParts == null) {
            veinParts = new HashMap<>();
            veinPartsCache = new LinkedList<>();
            veinParts.put(startPos, false);
            veinPartsCache.add(startPos);
            currentBlocks = 1;
        }

        boolean foundNew = false;
        List<BlockPos> newPositions = new LinkedList<>();
        for (BlockPos currentPos : veinPartsCache) {
            if (veinParts.getOrDefault(currentPos, false)) {
                continue;
            }

            for (Direction dir : Direction.values()) {
                foundNew |= tryAdd(level, startState, currentPos.relative(dir), newPositions);
            }

            if (checkDiagonals()) {
                for (Vec3i diag : DIAGONALS) {
                    foundNew |= tryAdd(level, startState, currentPos.offset(diag), newPositions);
                }
            }

            veinParts.put(currentPos, true);
            if (currentBlocks >= maxBlocks) {
                break;
            }
        }
        veinPartsCache.addAll(newPositions);

        return !foundNew;
    }

    private boolean tryAdd(Level level, BlockState startState, BlockPos checkPos, List<BlockPos> newPositions) {
        if (veinParts.containsKey(checkPos) || currentBlocks >= maxBlocks) {
            return false;
        }

        BlockState checkState = level.getBlockState(checkPos);
        if (!isValidBlock(startState, checkState)) {
            return false;
        }

        currentBlocks++;
        veinParts.put(checkPos, false);
        newPositions.add(checkPos);
        return true;
    }

    @Override
    protected void detonate(ServerLevel level, BlockPos pos, Direction chargeDirection) {
        ItemStack tool = getHarvestingTool();
        breakAndCollectDrops(level, veinPartsCache == null ? List.of() : veinPartsCache, tool);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        maxBlocks = tag.getInt("maxBlocks");
        currentBlocks = tag.getInt("currentBlocks");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("maxBlocks", maxBlocks);
        tag.putInt("currentBlocks", currentBlocks);
    }
}
