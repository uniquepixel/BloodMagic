package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.dimension.DungeonDimensionHelper;
import wayoftime.bloodmagic.common.dimension.DungeonSpawnAllocator;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.structures.DungeonSynthesizer;
import wayoftime.bloodmagic.structures.ModRoomPools;
import wayoftime.bloodmagic.util.Constants;

import java.util.List;
import java.util.function.Consumer;

/**
 * The cheaper of 1.20.1's two dungeon-opening rituals - {@code RitualSimpleDungeon} (80,000 LP,
 * crystalLevel 0, entrance pool {@code room_pools/entrances/mini_dungeon_entrances}) - as opposed
 * to {@link VaultRitual}, which restores the other one ({@code RitualStandardDungeon}, 150,000 LP
 * upstream / 200,000 LP on this branch, entrance pool {@code room_pools/entrances/standard_dungeon_entrances}).
 * <p>
 * Both 1.20.1 rituals shared identical {@code performRitual} bodies (same Inversion Pillar portal
 * spawning, same {@link DungeonSynthesizer#generateInitialRoom} call) and differed only in: LP cost,
 * pillar height, rune pattern, and - the one substantive difference - which entrance room pool they
 * hand to the synthesizer. {@code mini_dungeon_entrances} and {@code standard_dungeon_entrances} are
 * genuinely distinct datapack room pools (see {@code assets/bloodmagic/schematics/room_pools/entrances/}),
 * so this is a real "lesser tier" of dungeon, not just a reroll of the same content at a discount.
 * <p>
 * This class otherwise follows {@link VaultRitual}'s exact adaptation of the upstream body: direct
 * teleport of every nearby player (no Inversion Pillar subsystem on this branch - see that class's
 * javadoc for the full rationale) plus the same one-shot marker-block guard, reusing the same
 * {@link DungeonSpawnAllocator}-backed spawn slot allocation (tier-agnostic, so simple and standard
 * dungeons opened back-to-back never collide in the shared dungeon dimension).
 */
public class SimpleVaultRitual extends Ritual {
    private static final double PLAYER_SEARCH_RADIUS = 8.0;

    public SimpleVaultRitual() {
        super(RitualRegistry.rl("simple_vault"), 0, 80000, "ritual.bloodmagic.simple_vault");
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        BlockPos pos = masterRitualStone.getMasterBlockPos();

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // One-shot guard: this ritual only opens one dungeon per altar. Subsequent pulses (it keeps
        // ticking every getRefreshTime() while the ritual stays active) are a no-op once the marker
        // is down, mirroring VaultRitual's own guard.
        BlockPos markerPos = pos.above();
        if (level.getBlockState(markerPos).is(BMBlocks.DUNGEON_BRICK_ASSORTED.block().get())) {
            return;
        }

        ServerLevel dungeonWorld = DungeonDimensionHelper.getDungeonWorld(level);
        if (dungeonWorld == null) {
            return;
        }

        BlockPos dungeonSpawnLocation = DungeonSpawnAllocator.allocateSpawnPosition(dungeonWorld);
        DungeonSynthesizer dungeon = new DungeonSynthesizer();
        ResourceLocation initialType = ModRoomPools.MINI_DUNGEON_ENTRANCES;
        BlockPos[] positions = dungeon.generateInitialRoom(initialType, level.random, dungeonWorld, dungeonSpawnLocation);
        BlockPos safePlayerPosition = positions[0];

        List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(PLAYER_SEARCH_RADIUS));
        for (ServerPlayer player : nearbyPlayers) {
            CompoundTag exit = new CompoundTag();
            exit.putInt("xCoord", player.getBlockX());
            exit.putInt("yCoord", player.getBlockY());
            exit.putInt("zCoord", player.getBlockZ());
            exit.putString("dimension_key", player.level().dimension().location().toString());
            player.getPersistentData().put(Constants.NBT.DUNGEON_EXIT, exit);

            player.teleportTo(dungeonWorld, safePlayerPosition.getX() + 0.5, safePlayerPosition.getY(), safePlayerPosition.getZ() + 0.5, player.getYRot(), player.getXRot());
        }

        serverLevel.setBlockAndUpdate(markerPos, BMBlocks.DUNGEON_BRICK_ASSORTED.block().get().defaultBlockState());
    }

    @Override
    public int getRefreshCost() {
        return 0;
    }

    @Override
    public int getRefreshTime() {
        return 200;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addParallelRunes(components, 1, 0, EnumRuneType.EARTH);
        addCornerRunes(components, 1, 0, EnumRuneType.AIR);
    }

    @Override
    public Ritual getNewCopy() {
        return new SimpleVaultRitual();
    }
}
