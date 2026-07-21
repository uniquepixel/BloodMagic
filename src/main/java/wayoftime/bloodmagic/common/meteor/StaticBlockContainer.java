package wayoftime.bloodmagic.common.meteor;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Full-fidelity port of 1.20.1's {@code StaticBlockContainer}: always resolves to the same fixed block.
 */
public class StaticBlockContainer extends RandomBlockContainer {
    private final Block block;

    public StaticBlockContainer(Block block) {
        this.block = block;
    }

    @Override
    public Block getRandomBlock(RandomSource rand, Level world) {
        return block;
    }
}
