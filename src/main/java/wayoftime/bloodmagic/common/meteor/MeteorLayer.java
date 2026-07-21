package wayoftime.bloodmagic.common.meteor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Full-fidelity port of 1.20.1's {@code MeteorLayer}: one concentric shell of a meteor impact -
 * a radius, a "fill" block that's used once nothing weighted beats it, an optional list of weighted
 * alternatives (ores etc. that have a chance to appear instead of the fill), an optional min weight
 * (padding out the odds so the fill block still shows up even with few competing entries), and an
 * optional distinct "shell" block used only on the outermost skin of the sphere.
 * <p>
 * {@link #buildLayer(Level, BlockPos, int)} carves a sphere (leaving an inner sphere of radius
 * {@code emptyRadius} untouched, so an outer layer doesn't overwrite an inner one already placed) and
 * fills every replaceable block in it, exactly like the original.
 * <p>
 * Dropped relative to the original: JSON (de)serialization and network read/write, since payloads are
 * defined directly in Java code here rather than loaded from a datapack recipe - see
 * {@link MeteorDefinitions}.
 */
public class MeteorLayer {
    public final int layerRadius;
    public int additionalTotalWeight;
    public int minWeight = 0;
    private int totalMaxWeight = 0;
    private final List<WeightedEntry> weightList;
    private final RandomBlockContainer fillBlock;
    private RandomBlockContainer shellBlock;

    public MeteorLayer(int layerRadius, int additionalMaxWeight, List<WeightedEntry> weightList, RandomBlockContainer fillBlock) {
        this.layerRadius = layerRadius;
        this.additionalTotalWeight = additionalMaxWeight;
        this.weightList = weightList;
        this.fillBlock = fillBlock;
    }

    public MeteorLayer(int layerRadius, int additionalMaxWeight, Block fillBlock) {
        this(layerRadius, additionalMaxWeight, new ArrayList<>(), new StaticBlockContainer(fillBlock));
    }

    public MeteorLayer(int layerRadius, int additionalMaxWeight, Fluid fillFluid) {
        this(layerRadius, additionalMaxWeight, new ArrayList<>(), new FluidBlockContainer(fillFluid));
    }

    public MeteorLayer(int layerRadius, int additionalMaxWeight, TagKey<Block> fillTag) {
        this(layerRadius, additionalMaxWeight, fillTag, -1);
    }

    public MeteorLayer(int layerRadius, int additionalMaxWeight, TagKey<Block> fillTag, int staticIndex) {
        this(layerRadius, additionalMaxWeight, new ArrayList<>(), new RandomBlockTagContainer(fillTag, staticIndex));
    }

    public MeteorLayer addShellBlock(RandomBlockContainer shellBlock) {
        this.shellBlock = shellBlock;
        return this;
    }

    public MeteorLayer addShellBlock(TagKey<Block> tag) {
        return addShellBlock(tag, -1);
    }

    public MeteorLayer addShellBlock(TagKey<Block> tag, int staticIndex) {
        return addShellBlock(new RandomBlockTagContainer(tag, staticIndex));
    }

    public MeteorLayer addShellBlock(Block block) {
        return addShellBlock(new StaticBlockContainer(block));
    }

    public MeteorLayer addShellBlock(Fluid fluid) {
        return addShellBlock(new FluidBlockContainer(fluid));
    }

    public MeteorLayer addWeightedTag(TagKey<Block> tag, int weight) {
        return addWeightedTag(tag, weight, -1);
    }

    public MeteorLayer addWeightedTag(TagKey<Block> tag, int weight, int staticIndex) {
        weightList.add(new WeightedEntry(new RandomBlockTagContainer(tag, staticIndex), weight));
        return this;
    }

    public MeteorLayer addWeightedBlock(Block block, int weight) {
        weightList.add(new WeightedEntry(new StaticBlockContainer(block), weight));
        return this;
    }

    public MeteorLayer addWeightedFluid(Fluid fluid, int weight) {
        weightList.add(new WeightedEntry(new FluidBlockContainer(fluid), weight));
        return this;
    }

    public MeteorLayer setMinWeight(int weight) {
        this.minWeight = weight;
        return this;
    }

    public void buildLayer(Level world, BlockPos centerPos, int emptyRadius) {
        recalculateMaxWeight(world.random, world);

        int radius = layerRadius;
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                for (int k = -radius; k <= radius; k++) {
                    if (emptyRadius >= 0 && checkIfSphere(emptyRadius, i, j, k)) {
                        continue;
                    }

                    if (checkIfSphere(radius, i, j, k)) {
                        BlockPos pos = centerPos.offset(i, j, k);
                        BlockState currentState = world.getBlockState(pos);
                        BlockPlaceContext ctx = new BlockPlaceContext(world, null, InteractionHand.MAIN_HAND, ItemStack.EMPTY, BlockHitResult.miss(new Vec3(0, 0, 0), Direction.UP, pos));
                        if (!currentState.canBeReplaced(ctx)) {
                            continue;
                        }
                        if (shellBlock != null && checkIfSphereShell(radius, i, j, k)) {
                            Block block = shellBlock.getRandomBlock(world.random, world);
                            if (block != null) {
                                world.setBlockAndUpdate(pos, block.defaultBlockState());
                            }
                        } else {
                            world.setBlockAndUpdate(pos, getRandomState(world.random, world));
                        }
                    }
                }
            }
        }
    }

    private void recalculateMaxWeight(RandomSource rand, Level world) {
        totalMaxWeight = additionalTotalWeight;

        Iterator<WeightedEntry> itr = weightList.iterator();
        while (itr.hasNext()) {
            WeightedEntry entry = itr.next();
            Block newBlock = entry.container().getRandomBlock(rand, world);
            if (newBlock == null) {
                itr.remove();
                continue;
            }

            totalMaxWeight += entry.weight();
        }

        totalMaxWeight = Math.max(minWeight, totalMaxWeight);
    }

    private BlockState getRandomState(RandomSource rand, Level world) {
        Block block = fillBlock.getRandomBlock(rand, world);
        if (totalMaxWeight > 0) {
            int randNum = rand.nextInt(totalMaxWeight);
            for (WeightedEntry entry : weightList) {
                randNum -= entry.weight();
                if (randNum < 0) {
                    Block newBlock = entry.container().getRandomBlock(rand, world);
                    if (newBlock != null) {
                        block = newBlock;
                    }
                    break;
                }
            }
        }

        return block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }

    private boolean checkIfSphereShell(int xR, int xOff, int yOff, int zOff) {
        if (!checkIfSphere(xR, xOff, yOff, zOff)) {
            return false;
        }

        return !((checkIfSphere(xR, xOff + 1, yOff, zOff) && checkIfSphere(xR, xOff - 1, yOff, zOff))
                && (checkIfSphere(xR, xOff, yOff + 1, zOff) && checkIfSphere(xR, xOff, yOff - 1, zOff))
                && (checkIfSphere(xR, xOff, yOff, zOff + 1) && checkIfSphere(xR, xOff, yOff, zOff - 1)));
    }

    private boolean checkIfSphere(float radius, float xOff, float yOff, float zOff) {
        float possOffset = 0.5f;
        return xOff * xOff + yOff * yOff + zOff * zOff <= (radius + possOffset) * (radius + possOffset);
    }

    /** A single weighted alternative block/fluid/tag entry within a layer. Replaces the original's use of commons-lang {@code Pair}. */
    public record WeightedEntry(RandomBlockContainer container, int weight) {
    }
}
