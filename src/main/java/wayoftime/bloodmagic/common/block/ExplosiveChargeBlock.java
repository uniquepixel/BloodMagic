package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.common.blockentity.ExplosiveChargeTile;

/**
 * Full-fidelity-in-spirit port of 1.20.1's {@code BlockShapedExplosive}: a directional charge that
 * sticks to whichever face the player was looking at when placed, then digs into the block in
 * front of it once its {@link ExplosiveChargeTile} decides it's ready. Shared by all 4 charge
 * types ({@link ShapedChargeBlock}, {@link DeforesterChargeBlock}, {@link VeinMineChargeBlock},
 * {@link FungalChargeBlock}) exactly like the original's class hierarchy.
 * <p>
 * Not ported: the original's owner-UUID tracking + {@code BlockProtectionHelper} claim check (that
 * helper class doesn't exist on this branch - there's no claims/protection mod integration to hook
 * into here). The {@code AnointmentHolder} tool-bonus on the loot-table "harvesting tool" IS ported
 * (see {@link #setPlacedBy} and {@link ExplosiveChargeTile#getHarvestingTool}), adapted to this
 * branch's per-anointment "uses" data-component system instead of the original's NBT-backed
 * {@code AnointmentHolder} class.
 */
public abstract class ExplosiveChargeBlock extends Block implements EntityBlock {
    private static final VoxelShape UP = Block.box(2, 0, 2, 14, 7, 14);
    private static final VoxelShape DOWN = Block.box(2, 9, 2, 14, 16, 14);
    private static final VoxelShape NORTH = Block.box(2, 2, 7, 14, 14, 16);
    private static final VoxelShape SOUTH = Block.box(2, 2, 0, 14, 14, 7);
    private static final VoxelShape EAST = Block.box(0, 2, 2, 7, 14, 14);
    private static final VoxelShape WEST = Block.box(9, 2, 2, 16, 14, 14);

    public static final EnumProperty<Direction> ATTACHED = EnumProperty.create("attached", Direction.class);

    protected ExplosiveChargeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ATTACHED, Direction.UP));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction.getOpposite() == state.getValue(ATTACHED) && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : state;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        for (Direction direction : context.getNearestLookingDirections()) {
            BlockState candidate = state.setValue(ATTACHED, direction.getOpposite());
            if (candidate.canSurvive(level, pos)) {
                return candidate;
            }
        }

        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ATTACHED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(ATTACHED)) {
            case DOWN -> DOWN;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            default -> UP;
        };
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ExplosiveChargeTile tile) {
            tile.dropSelf();
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Carries over whichever anointment (Fortune/Silk Touch/Smelting/Voiding) the placed
     * ItemStack was carrying - see {@link ExplosiveChargeTile#applyAnointmentsFrom} - so a charge
     * anointed before being placed (the same {@code AnointmentItem} "use in the other hand"
     * interaction as anointing any other tool) actually detonates with that bonus applied.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ExplosiveChargeTile tile) {
            tile.applyAnointmentsFrom(stack);
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> blockEntityType) {
        return (level1, pos, state1, tile) -> {
            if (tile instanceof ExplosiveChargeTile charge) {
                charge.tick();
            }
        };
    }
}
