package wayoftime.bloodmagic.common.meteor;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/**
 * Full-fidelity port of 1.20.1's {@code FluidBlockContainer}: resolves to a fluid's legacy placement
 * block (e.g. water -> {@code minecraft:water}), so a {@link MeteorLayer} can fill/shell with a fluid
 * the same way it can with a solid block.
 */
public class FluidBlockContainer extends RandomBlockContainer {
    private final Fluid fluid;

    public FluidBlockContainer(Fluid fluid) {
        this.fluid = fluid;
    }

    @Override
    public Block getRandomBlock(RandomSource rand, Level world) {
        BlockState state = fluid.defaultFluidState().createLegacyBlock();
        return state == null ? null : state.getBlock();
    }
}
