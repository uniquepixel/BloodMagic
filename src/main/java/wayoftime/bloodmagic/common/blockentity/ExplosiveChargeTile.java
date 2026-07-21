package wayoftime.bloodmagic.common.blockentity;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.common.block.ExplosiveChargeBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared base for the whole Charge family, ported from 1.20.1's {@code TileExplosiveCharge}/
 * {@code TileShapedExplosive}'s common fuse timeline: once {@link #readyToDetonate} says so, it
 * runs an identical ~5-second fuse (flint-and-steel sound + flame at tick 20, TNT-primed sound at
 * tick 30, smoke puffs from tick 30 on, then at tick 100 the explosion sound/particles play and
 * {@link #detonate} is called to actually clear blocks) before the charge block removes itself.
 * Subclasses only need to decide WHAT counts as "ready" and WHICH positions to clear - see
 * {@link ShapedChargeTile} (immediately ready, clears a fixed cuboid) and
 * {@link VeinMineChargeTile}/{@link DeforesterChargeTile} (floodfill an area across multiple ticks
 * before becoming ready, then clear whatever the floodfill found).
 */
public abstract class ExplosiveChargeTile extends BaseTile {
    private double internalCounter = 0;

    protected ExplosiveChargeTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        Direction chargeDirection = getBlockState().getValue(ExplosiveChargeBlock.ATTACHED).getOpposite();

        if (!readyToDetonate(level, worldPosition, chargeDirection)) {
            return;
        }

        internalCounter++;
        if (internalCounter == 20) {
            level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                    SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F);
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.FLAME, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 5, 0.02, 0.03, 0.02, 0);
            }
        }

        if (internalCounter == 30) {
            level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                    SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (internalCounter < 30) {
            return;
        }

        if (level instanceof ServerLevel server && level.random.nextDouble() < 0.3) {
            server.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 1, 0.0D, 0.0D, 0.0D, 0);
        }

        if (internalCounter == 100 && level instanceof ServerLevel server) {
            level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                    SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);
            server.sendParticles(ParticleTypes.EXPLOSION, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 10, 1.0D, 1.0D, 1.0D, 0);

            detonate(server, worldPosition, chargeDirection);
            level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());
        }
    }

    /** Called every tick before the fuse starts counting; return false to keep waiting (e.g. floodfilling). */
    protected boolean readyToDetonate(Level level, BlockPos pos, Direction chargeDirection) {
        return true;
    }

    /** Called once, at the moment of detonation, to actually clear whatever this charge decided to clear. */
    protected abstract void detonate(ServerLevel level, BlockPos pos, Direction chargeDirection);

    public ItemStack getHarvestingTool() {
        return new ItemStack(Items.DIAMOND_PICKAXE);
    }

    public void dropSelf() {
        if (level == null) {
            return;
        }
        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), new ItemStack(getBlockState().getBlock()));
    }

    /**
     * Breaks every listed block, collecting its loot-table drops (merged by stack, matching the
     * original's {@code handleExplosionDrops}) instead of spawning an item entity per block - then
     * pops the merged drops once at the end. Skips positions whose destroy speed is -1 (unbreakable).
     */
    protected void breakAndCollectDrops(ServerLevel level, Iterable<BlockPos> positions, ItemStack toolStack) {
        List<Pair<ItemStack, BlockPos>> drops = new ArrayList<>();

        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.getDestroySpeed(level, pos) == -1.0F) {
                continue;
            }

            BlockPos immutablePos = pos.immutable();
            BlockEntity blockEntity = state.getBlock() instanceof EntityBlock ? level.getBlockEntity(pos) : null;
            LootParams.Builder lootParams = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .withParameter(LootContextParams.TOOL, toolStack)
                    .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);

            state.getDrops(lootParams).forEach(stack -> mergeDrop(drops, stack, immutablePos));
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }

        for (Pair<ItemStack, BlockPos> drop : drops) {
            Block.popResource(level, drop.getSecond(), drop.getFirst());
        }
    }

    private static void mergeDrop(List<Pair<ItemStack, BlockPos>> drops, ItemStack stack, BlockPos pos) {
        for (int i = 0; i < drops.size(); i++) {
            Pair<ItemStack, BlockPos> existing = drops.get(i);
            if (ItemEntity.areMergable(existing.getFirst(), stack)) {
                ItemStack merged = ItemEntity.merge(existing.getFirst(), stack, 16);
                drops.set(i, Pair.of(merged, existing.getSecond()));
                if (stack.isEmpty()) {
                    return;
                }
            }
        }
        drops.add(Pair.of(stack, pos));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        internalCounter = tag.getDouble("internalCounter");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("internalCounter", internalCounter);
    }
}
