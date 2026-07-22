package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;
import wayoftime.bloodmagic.common.item.BMItems;

import java.util.List;

/**
 * Full-fidelity port of 1.20.1's {@code BlockTau} (2 tiers: weak/strong, selected by
 * {@link #isStrong}). Unlike a normal crop, Tau only grows when it damages a living entity
 * standing in its bounding box (cactus damage) on an eligible growth tick: a weak Tau has a 20%
 * chance per successful hit to transform into strong Tau instead of growing normally, while a
 * strong Tau always grows on a successful hit. Breaking an immature strong Tau (see its loot table)
 * yields weak Tau back, matching 1.20.1.
 * <p>
 * 1.20.1's {@code isBonemealSuccess} override used the wrong parameter type ({@code Random} instead
 * of {@code RandomSource}) and so never actually overrode anything - meaning bonemeal worked on Tau
 * in practice via {@link CropBlock}'s default {@code true}, despite the source code's apparent
 * intent to disable it. This port preserves that actual (bonemeal works) behaviour rather than the
 * apparently-intended-but-never-shipped one.
 */
public class TauBlock extends CropBlock {
    public final boolean isStrong;
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

    public static final double TRANSFORM_CHANCE = 0.2D;

    public TauBlock(BlockBehaviour.Properties properties, boolean isStrong) {
        super(properties);
        this.isStrong = isStrong;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return isStrong ? BMItems.STRONG_TAU_SEED.get() : BMItems.WEAK_TAU_SEED.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[this.getAge(state)];
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) {
            return;
        }
        if (level.getRawBrightness(pos, 0) < 9) {
            return;
        }

        int age = this.getAge(state);
        if (age >= this.getMaxAge()) {
            return;
        }

        float growthSpeed = getGrowthSpeed(state, level, pos);
        if (!CommonHooks.canCropGrow(level, pos, state, random.nextInt((int) (25.0F / growthSpeed) + 1) == 0)) {
            return;
        }

        if (tryGrow(level, pos, age, random)) {
            CommonHooks.fireCropGrowPost(level, pos, state);
        }
    }

    @Override
    public void growCrops(Level level, BlockPos pos, BlockState state) {
        int age = this.getAge(state);
        if (age >= this.getMaxAge()) {
            return;
        }

        int newAge = age + getBonemealAgeIncrease(level);
        if (tryGrow(level, pos, age, newAge, level.random)) {
            CommonHooks.fireCropGrowPost(level, pos, state);
        }
    }

    /** Random-tick growth: always advances by exactly 1 age. */
    private boolean tryGrow(Level level, BlockPos pos, int age, RandomSource random) {
        return tryGrow(level, pos, age, age + 1, random);
    }

    /**
     * Ported from 1.20.1's {@code BlockTau#randomTick}/{@code #growCrops}: only grows (to
     * {@code newAge}) if hitting a nearby living entity with cactus damage succeeds; weak Tau has a
     * chance to transform into strong Tau (at {@code newAge}) instead of growing normally, while
     * strong Tau always grows on a successful hit.
     */
    private boolean tryGrow(Level level, BlockPos pos, int age, int newAge, RandomSource random) {
        boolean doTransform = false;
        boolean doGrow = !isStrong;

        AABB box = new AABB(pos).inflate(1, 0, 1);
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, box);
        for (LivingEntity entity : nearby) {
            if (entity.hurt(entity.damageSources().cactus(), 2)) {
                if (isStrong) {
                    doGrow = true;
                    break;
                } else if (random.nextDouble() <= TRANSFORM_CHANCE) {
                    doTransform = true;
                    break;
                }
            }
        }

        if (!doGrow) {
            return false;
        }

        if (doTransform) {
            level.setBlock(pos, ((TauBlock) BMBlocks.STRONG_TAU.get()).getStateForAge(newAge), 2);
        } else {
            level.setBlock(pos, this.getStateForAge(newAge), 2);
        }
        return true;
    }

    @Override
    protected int getBonemealAgeIncrease(Level level) {
        return Mth.nextInt(level.random, 1, 1);
    }
}
