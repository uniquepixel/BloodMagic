package wayoftime.bloodmagic.common.meteor;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Port of 1.20.1's {@code RandomBlockContainer}: a small strategy interface for "what block should
 * actually get placed here" used by {@link MeteorLayer} - a single fixed block ({@link StaticBlockContainer}),
 * a fluid's legacy placement block ({@link FluidBlockContainer}), or a random/indexed pick out of a
 * block tag ({@link RandomBlockTagContainer}).
 * <p>
 * The original also carried JSON (de)serialization and network read/write so a container could be
 * described in a datapack {@code RecipeMeteor} and synced to the client. Since this branch drives
 * meteor payloads from a fixed, code-defined list instead of a datapack recipe type (see
 * {@link MeteorDefinitions} for why), that serialization plumbing has no caller here and was dropped -
 * only the part that matters at impact time, picking an actual block, was kept.
 */
public abstract class RandomBlockContainer {
    public abstract Block getRandomBlock(RandomSource rand, Level world);
}
