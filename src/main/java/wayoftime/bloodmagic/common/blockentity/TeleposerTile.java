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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
 * Simplified take on the Teleposer: swaps entities (rather than both entities and blocks, like
 * the original) between this block and whatever position its inserted Focus is bound to, when
 * powered by redstone. Players can cross dimensions; other entities only swap within the same
 * level, since moving arbitrary entities between dimensions safely needs more plumbing than this
 * pass covers.
 */
public class TeleposerTile extends BaseTile {
    private static final double RANGE = 1.5;
    private static final int MAX_UNIT_COST = 1000;

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

        GlobalPos bound = storedFocus.get(BMDataComponents.TELEPOSITION_BINDING);
        if (bound == null) {
            return;
        }

        ServerLevel targetLevel = level.getServer().getLevel(bound.dimension());
        if (targetLevel == null) {
            return;
        }

        BlockPos targetPos = bound.pos();
        if (!(targetLevel.getBlockEntity(targetPos) instanceof TeleposerTile)) {
            return;
        }

        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(owner);
        if (network == null) {
            return;
        }

        AABB hereBox = new AABB(pos).inflate(RANGE);
        AABB thereBox = new AABB(targetPos).inflate(RANGE);

        List<Entity> hereEntities = level.getEntitiesOfClass(Entity.class, hereBox);
        List<Entity> thereEntities = targetLevel.getEntitiesOfClass(Entity.class, thereBox);

        int maxUses = hereEntities.size() + thereEntities.size();
        if (maxUses == 0) {
            return;
        }

        boolean crossDimension = targetLevel != level;
        int unitCost = crossDimension ? MAX_UNIT_COST : Math.min((int) (0.5 * Math.sqrt(targetPos.distSqr(pos))), MAX_UNIT_COST);
        int totalCost = Math.min(unitCost * maxUses, 10000);

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

        level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
        targetLevel.playSound(null, targetPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
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
