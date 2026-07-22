package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.api.BMTags;
import wayoftime.bloodmagic.api.helper.SoulNetworkHelper;
import wayoftime.bloodmagic.api.soulnetwork.SoulTicket;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.SoulNetwork;
import wayoftime.bloodmagic.common.item.TeleposerFocusItem;
import wayoftime.bloodmagic.util.ChatUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Full-fidelity-in-spirit port of 1.20.1's {@code TileTeleposer}: swaps both blocks (with their
 * block entities' full NBT/data-component state) AND entities between this Teleposer's position
 * and whatever position its inserted Focus is bound to, when powered by redstone.
 * <p>
 * The swapped region is a cuboid sized by the inserted Focus's tier ({@link TeleposerFocusItem#range},
 * base/Enhanced/Reinforced = 0/1/2) - see {@link #entityBox}/{@link #blockOffsets}, ported from
 * 1.20.1's {@code ItemTeleposerFocus#getEntityRangeOffset}/{@code #getBlockListOffset}. Blocks tagged
 * {@link BMTags.Blocks#TELEPOSE_BLOCK_BLACKLIST} (portals/doors/beds/pistons/bedrock/the Alchemy
 * Table) and entities tagged {@link BMTags.EntityTypes#TELEPOSE_BLACKLIST} are skipped, matching
 * the original's guards.
 * <p>
 * Adapted: entities can only cross dimensions if they're a {@link ServerPlayer} (matching the
 * previous, entity-only version of this tile) - 1.20.1 routed every entity through a command-based
 * dimension teleport, which this branch doesn't have plumbing for outside the player case. Swapped
 * blocks always cross dimensions fine either way, since block state/data doesn't need an entity
 * teleport path. Scheduled block-tick transfer (1.20.1 re-scheduled a swapped block's pending tick,
 * e.g. a mid-flip lever, on the other side) is not ported - a minor cosmetic edge case.
 */
public class TeleposerTile extends BaseTile {
    private static final int MAX_UNIT_COST = 1000;
    private static final int MAX_TOTAL_COST = 10000;

    private ItemStack storedFocus = ItemStack.EMPTY;
    @Nullable
    private UUID owner;
    private boolean previouslyPowered;

    public TeleposerTile(BlockPos pos, BlockState state) {
        super(BMTiles.TELEPOSER_TYPE.get(), pos, state);
    }

    public boolean hasFocus() {
        return !storedFocus.isEmpty();
    }

    public void handleInteract(Player player) {
        if (level == null || level.isClientSide) {
            return;
        }

        if (owner == null) {
            owner = player.getUUID();
        }

        ItemStack held = player.getMainHandItem();
        if (storedFocus.isEmpty() && held.getItem() instanceof TeleposerFocusItem && held.has(BMDataComponents.TELEPOSITION_BINDING)) {
            storedFocus = held.split(1);
            setChanged();
            ChatUtil.sendChat(player, List.of(Component.translatable("chat.bloodmagic.teleposer.linked")));
            return;
        }

        if (!storedFocus.isEmpty() && held.isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, storedFocus);
            storedFocus = ItemStack.EMPTY;
            setChanged();
            ChatUtil.sendChat(player, List.of(Component.translatable("chat.bloodmagic.teleposer.unlinked")));
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TeleposerTile tile) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean powered = level.hasNeighborSignal(pos);
        if (powered && !tile.previouslyPowered) {
            tile.initiateTeleport(serverLevel, pos);
        }
        tile.previouslyPowered = powered;
    }

    private void initiateTeleport(ServerLevel level, BlockPos pos) {
        if (storedFocus.isEmpty() || owner == null) {
            return;
        }

        if (!(storedFocus.getItem() instanceof TeleposerFocusItem focusItem)) {
            return;
        }

        GlobalPos bound = storedFocus.get(BMDataComponents.TELEPOSITION_BINDING);
        if (bound == null) {
            return;
        }

        ServerLevel targetLevel = level.getServer().getLevel(bound.dimension());
        if (targetLevel == null) {
            return;
        }

        BlockPos targetPos = bound.pos();
        if (targetLevel == level && targetPos.equals(pos)) {
            return;
        }
        if (!(targetLevel.getBlockEntity(targetPos) instanceof TeleposerTile)) {
            return;
        }

        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(owner);
        if (network == null) {
            return;
        }

        int range = focusItem.range;
        AABB hereBox = entityBox(pos, range);
        AABB thereBox = entityBox(targetPos, range);

        List<Entity> hereEntities = level.getEntitiesOfClass(Entity.class, hereBox, e -> !e.getType().is(BMTags.EntityTypes.TELEPOSE_BLACKLIST));
        List<Entity> thereEntities = targetLevel.getEntitiesOfClass(Entity.class, thereBox, e -> !e.getType().is(BMTags.EntityTypes.TELEPOSE_BLACKLIST));
        List<BlockPos> offsets = blockOffsets(range);

        // 1.20.1 priced this per-swap (blocks + entities) using the actual number of successful
        // swaps; this pays upfront for the theoretical maximum (every offset in the region plus
        // every entity found), matching the pre-existing convention on this branch (see the
        // entity-only version this replaces) rather than needing a refund path.
        int maxUses = hereEntities.size() + thereEntities.size() + offsets.size();
        if (maxUses == 0) {
            return;
        }

        boolean crossDimension = targetLevel != level;
        int unitCost = crossDimension ? MAX_UNIT_COST : Math.min((int) (0.5 * Math.sqrt(targetPos.distSqr(pos))), MAX_UNIT_COST);
        int totalCost = Math.min(unitCost * maxUses, MAX_TOTAL_COST);

        if (!network.syphon(SoulTicket.block(level, pos, totalCost))) {
            return;
        }

        List<Entity> toSwapHere = new ArrayList<>(hereEntities);
        List<Entity> toSwapThere = new ArrayList<>(thereEntities);

        for (Entity entity : toSwapHere) {
            moveEntity(entity, level, pos, targetLevel, targetPos);
        }
        for (Entity entity : toSwapThere) {
            moveEntity(entity, targetLevel, targetPos, level, pos);
        }

        for (BlockPos offset : offsets) {
            swapBlockAndEntity(level, pos.offset(offset), targetLevel, targetPos.offset(offset));
        }

        level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
        targetLevel.playSound(null, targetPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * Ported from 1.20.1's {@code ItemTeleposerFocus#getEntityRangeOffset}: a range-0 focus covers
     * only the single block directly above the Teleposer (y+1); range N covers a (2N+1) x (2N+1)
     * footprint from y+1 to y+2N+1.
     */
    private static AABB entityBox(BlockPos origin, int range) {
        return new AABB(
                origin.getX() - range, origin.getY() + 1, origin.getZ() - range,
                origin.getX() + range + 1, origin.getY() + 2 * range + 2, origin.getZ() + range + 1
        );
    }

    /** Ported from 1.20.1's {@code ItemTeleposerFocus#getBlockListOffset}. */
    private static List<BlockPos> blockOffsets(int range) {
        List<BlockPos> offsets = new ArrayList<>();
        for (int x = -range; x <= range; x++) {
            for (int y = 1; y <= 2 * range + 1; y++) {
                for (int z = -range; z <= range; z++) {
                    offsets.add(new BlockPos(x, y, z));
                }
            }
        }
        return offsets;
    }

    /**
     * Ported from 1.20.1's {@code Utils#swapLocations}: snapshots each side's blockstate + block
     * entity data, clears both block entities, swaps the blockstates, then restores each snapshot
     * onto the other side's now-relocated block entity. Modernized to this branch's data-component-
     * aware block entity persistence ({@code saveWithFullMetadata}/{@code loadWithComponents}, the
     * same pair {@code TileMimic} already uses for its own state snapshotting) rather than 1.20.1's
     * raw {@code saveWithFullMetadata()}/{@code load(tag)} NBT calls.
     */
    private static boolean swapBlockAndEntity(ServerLevel levelA, BlockPos posA, ServerLevel levelB, BlockPos posB) {
        BlockState stateA = levelA.getBlockState(posA);
        BlockState stateB = levelB.getBlockState(posB);

        if ((stateA.isAir() && stateB.isAir())
                || stateA.is(BMTags.Blocks.TELEPOSE_BLOCK_BLACKLIST)
                || stateB.is(BMTags.Blocks.TELEPOSE_BLOCK_BLACKLIST)) {
            return false;
        }

        BlockEntity tileA = levelA.getBlockEntity(posA);
        BlockEntity tileB = levelB.getBlockEntity(posB);
        CompoundTag tagA = tileA != null ? tileA.saveWithFullMetadata(levelA.registryAccess()) : null;
        CompoundTag tagB = tileB != null ? tileB.saveWithFullMetadata(levelB.registryAccess()) : null;

        // Clear both block entities before swapping states, so setBlock doesn't try to keep an
        // incompatible one alive.
        if (stateB.getBlock() instanceof EntityBlock) {
            levelB.removeBlockEntity(posB);
        }
        if (stateA.getBlock() instanceof EntityBlock) {
            levelA.removeBlockEntity(posA);
        }

        levelB.setBlock(posB, stateA, 3);
        levelA.setBlock(posA, stateB, 3);

        if (tagA != null) {
            BlockEntity newTileAtB = levelB.getBlockEntity(posB);
            if (newTileAtB != null) {
                newTileAtB.loadWithComponents(tagA, levelB.registryAccess());
                newTileAtB.setChanged();
            }
        }

        if (tagB != null) {
            BlockEntity newTileAtA = levelA.getBlockEntity(posA);
            if (newTileAtA != null) {
                newTileAtA.loadWithComponents(tagB, levelA.registryAccess());
                newTileAtA.setChanged();
            }
        }

        levelA.updateNeighborsAt(posA, stateB.getBlock());
        levelB.updateNeighborsAt(posB, stateA.getBlock());

        return true;
    }

    private static void moveEntity(Entity entity, ServerLevel from, BlockPos fromPos, ServerLevel to, BlockPos toPos) {
        if (!entity.isAlive()) {
            return;
        }

        Vec3 offset = entity.position().subtract(fromPos.getX(), fromPos.getY(), fromPos.getZ());
        Vec3 newPos = offset.add(toPos.getX(), toPos.getY(), toPos.getZ());

        if (entity instanceof ServerPlayer player) {
            if (from == to) {
                player.teleportTo(newPos.x, newPos.y, newPos.z);
            } else {
                player.teleportTo(to, newPos.x, newPos.y, newPos.z, player.getYRot(), player.getXRot());
            }
        } else if (from == to) {
            entity.teleportTo(newPos.x, newPos.y, newPos.z);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        if (tag.contains("focus")) {
            storedFocus = ItemStack.parseOptional(registries, tag.getCompound("focus"));
        } else {
            storedFocus = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
        if (!storedFocus.isEmpty()) {
            tag.put("focus", storedFocus.save(registries));
        }
    }
}
