package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A node in the Item Routing network (see {@link MasterRoutingNodeTile}): marks one of its six
 * neighboring external inventories (any side not itself facing another routing node) as a source
 * the network can pull items from.
 */
public class InputRoutingNodeTile extends BaseTile {
    public InputRoutingNodeTile(BlockPos pos, BlockState state) {
        super(BMTiles.INPUT_ROUTING_NODE_TYPE.get(), pos, state);
    }
}
