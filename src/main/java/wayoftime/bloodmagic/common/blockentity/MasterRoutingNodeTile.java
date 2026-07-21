package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Full-fidelity-in-spirit port of 1.20.1's Item Routing network: a Master node flood-fills through
 * adjacent {@link InputRoutingNodeTile}/{@link OutputRoutingNodeTile} blocks to discover its
 * network (mirroring the original's graph-of-connected-nodes design), then pulls items from each
 * input node's external inventories into whichever priority-ordered, filter-matching output node
 * will take them. Not ported: the original's per-face connection-toggle GUI (connections here are
 * automatic - any routing-node block touching another is linked), its dedicated node-network
 * screens/containers, and its custom in-world node model/beam renderer - this uses plain blocks.
 * The underlying network concept and item movement are real.
 * <p>
 * The 1.20.1 Filter item system (whitelist/blacklist by exact item/tag/mod id/enchantment, and AND/OR
 * composition of other filters) is now restored too - see
 * {@link wayoftime.bloodmagic.common.item.filter.AbstractFilterItem} and its subclasses, plugged
 * into a node via {@link InputRoutingNodeTile#accepts(ItemStack)}/{@link OutputRoutingNodeTile#accepts(ItemStack)}
 * below, which this class now consults on both ends of the transfer (only the output side was
 * filterable before this pass - input nodes had no filtering capability at all).
 */
public class MasterRoutingNodeTile extends BaseTile {
    private static final int MAX_NODES = 256;

    public MasterRoutingNodeTile(BlockPos pos, BlockState state) {
        super(BMTiles.MASTER_ROUTING_NODE_TYPE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MasterRoutingNodeTile tile) {
        if (level.isClientSide || level.getGameTime() % 10 != 0) {
            return;
        }

        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> inputs = new ArrayList<>();
        List<BlockPos> outputs = new ArrayList<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(pos);
        visited.add(pos);

        while (!queue.isEmpty() && visited.size() < MAX_NODES) {
            BlockPos current = queue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (visited.contains(neighbor)) {
                    continue;
                }

                BlockEntity neighborTile = level.getBlockEntity(neighbor);
                if (neighborTile instanceof InputRoutingNodeTile) {
                    inputs.add(neighbor);
                    visited.add(neighbor);
                    queue.add(neighbor);
                } else if (neighborTile instanceof OutputRoutingNodeTile) {
                    outputs.add(neighbor);
                    visited.add(neighbor);
                    queue.add(neighbor);
                } else if (neighborTile instanceof MasterRoutingNodeTile) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        if (inputs.isEmpty() || outputs.isEmpty()) {
            return;
        }

        outputs.sort(Comparator.comparingInt(p -> -((OutputRoutingNodeTile) level.getBlockEntity(p)).getPriority()));

        for (BlockPos inputPos : inputs) {
            if (moveOneItem(level, inputPos, outputs, visited)) {
                return;
            }
        }
    }

    private static boolean moveOneItem(Level level, BlockPos inputPos, List<BlockPos> outputs, Set<BlockPos> networkPositions) {
        InputRoutingNodeTile inputTile = level.getBlockEntity(inputPos) instanceof InputRoutingNodeTile t ? t : null;

        for (Direction side : Direction.values()) {
            BlockPos externalPos = inputPos.relative(side);
            if (networkPositions.contains(externalPos)) {
                continue;
            }

            IItemHandler source = level.getCapability(Capabilities.ItemHandler.BLOCK, externalPos, side.getOpposite());
            if (source == null) {
                continue;
            }

            for (int slot = 0; slot < source.getSlots(); slot++) {
                ItemStack peek = source.extractItem(slot, 1, true);
                if (peek.isEmpty()) {
                    continue;
                }

                if (inputTile != null && !inputTile.accepts(peek)) {
                    continue;
                }

                if (tryInsertIntoOutputs(level, peek, outputs, networkPositions, source, slot)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean tryInsertIntoOutputs(Level level, ItemStack peek, List<BlockPos> outputs, Set<BlockPos> networkPositions, IItemHandler source, int slot) {
        for (BlockPos outputPos : outputs) {
            if (!(level.getBlockEntity(outputPos) instanceof OutputRoutingNodeTile outputTile) || !outputTile.accepts(peek)) {
                continue;
            }

            for (Direction outSide : Direction.values()) {
                BlockPos outExternal = outputPos.relative(outSide);
                if (networkPositions.contains(outExternal)) {
                    continue;
                }

                IItemHandler target = level.getCapability(Capabilities.ItemHandler.BLOCK, outExternal, outSide.getOpposite());
                if (target == null) {
                    continue;
                }

                ItemStack simulated = ItemHandlerHelper.insertItem(target, peek, true);
                if (simulated.getCount() < peek.getCount()) {
                    ItemStack extracted = source.extractItem(slot, 1, false);
                    if (!extracted.isEmpty()) {
                        ItemHandlerHelper.insertItem(target, extracted, false);
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
