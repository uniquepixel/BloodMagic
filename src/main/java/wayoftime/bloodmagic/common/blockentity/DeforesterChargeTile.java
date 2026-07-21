package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Ported from 1.20.1's {@code TileDeforesterCharge}: while the attached block is a log or leaf,
 * floodfills outward through the 6 cardinal neighbors collecting every connected log/leaf block
 * (budgeted by log count only - leaves ride along for free, matching the original exactly), one BFS
 * layer per tick. Once the floodfill stabilizes (no new blocks found that tick), the fuse begins;
 * at detonation every collected block is cleared.
 */
public class DeforesterChargeTile extends ExplosiveChargeTile {
    private int maxLogs;
    private int currentLogs = 0;

    private Map<BlockPos, Boolean> treeParts;
    private List<BlockPos> treePartsCache;

    public DeforesterChargeTile(BlockPos pos, BlockState state, int maxLogs) {
        super(BMTiles.DEFORESTER_CHARGE_TYPE.get(), pos, state);
        this.maxLogs = maxLogs;
    }

    @Override
    protected boolean readyToDetonate(Level level, BlockPos pos, Direction chargeDirection) {
        BlockState attached = level.getBlockState(pos.relative(chargeDirection));
        if (!attached.is(BlockTags.LOGS) && !attached.is(BlockTags.LEAVES)) {
            return false;
        }

        if (treeParts == null) {
            treeParts = new HashMap<>();
            treePartsCache = new LinkedList<>();
            BlockPos start = pos.relative(chargeDirection);
            treeParts.put(start, false);
            treePartsCache.add(start);
        }

        boolean foundNew = false;
        List<BlockPos> newPositions = new LinkedList<>();
        for (BlockPos currentPos : treePartsCache) {
            if (treeParts.getOrDefault(currentPos, false)) {
                continue;
            }

            for (Direction dir : Direction.values()) {
                BlockPos checkPos = currentPos.relative(dir);
                if (treeParts.containsKey(checkPos) || currentLogs >= maxLogs) {
                    continue;
                }

                BlockState checkState = level.getBlockState(checkPos);
                boolean isTree = false;
                if (checkState.is(BlockTags.LOGS)) {
                    currentLogs++;
                    isTree = true;
                } else if (checkState.is(BlockTags.LEAVES)) {
                    isTree = true;
                }

                if (isTree) {
                    treeParts.put(checkPos, false);
                    newPositions.add(checkPos);
                    foundNew = true;
                }
            }

            treeParts.put(currentPos, true);
            if (currentLogs >= maxLogs) {
                break;
            }
        }
        treePartsCache.addAll(newPositions);

        return !foundNew;
    }

    @Override
    protected void detonate(ServerLevel level, BlockPos pos, Direction chargeDirection) {
        ItemStack tool = getHarvestingTool();
        breakAndCollectDrops(level, treePartsCache == null ? List.of() : treePartsCache, tool);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        maxLogs = tag.getInt("maxLogs");
        currentLogs = tag.getInt("currentLogs");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("maxLogs", maxLogs);
        tag.putInt("currentLogs", currentLogs);
    }
}
