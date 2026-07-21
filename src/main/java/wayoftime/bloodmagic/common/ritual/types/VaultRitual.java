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
 * Real Demon Dungeon entry point, replacing the single-fixed-room stand-in this class used to be
 * (see git history for that version's javadoc). The 1.20.1 original split this into two rituals -
 * {@code RitualStandardDungeon} and {@code RitualSimpleDungeon} - that generate a dungeon in the
 * dedicated void dungeon dimension (see {@code data/bloodmagic/dimension/dungeon.json}) via
 * {@link DungeonSynthesizer#generateInitialRoom} and then link the two dimensions with a pair of
 * physical, bidirectional {@code INVERSION_PILLAR} portal blocks a player walks through. This
 * branch only has one dungeon-opening ritual slot to reuse (porting a second ritual type would mean
 * touching {@code RitualRegistry}, which is out of scope for this pass) and doesn't port the
 * Inversion Pillar portal subsystem (a whole separate block+tile pair with its own bidirectional-
 * pairing NBT and command-based cross-dimension teleport) - so this instead:
 * <ul>
 * <li>generates a full {@code room_pools/entrances/standard_dungeon_entrances} dungeon (the
 * "standard" tier, matching upstream's {@code RitualStandardDungeon}) in the dungeon dimension via
 * the exact same {@link DungeonSynthesizer} every generated room afterwards also goes through;</li>
 * <li>directly teleports every player within range of the altar to the generated entrance room's
 * safe spawn point (grabbing "nearby players" rather than a specific one because {@link Ritual#performRitual}
 * isn't given a player reference - upstream's own dead/commented-out code in
 * {@code RitualStandardDungeon} shows this was the original pre-portal approach too, before the
 * Inversion Pillar system was built);</li>
 * <li>stashes each teleported player's return position in their persistent data under
 * {@link Constants.NBT#DUNGEON_EXIT} (same NBT shape upstream used for this), and gives the
 * dungeon's {@code TileDungeonController} block (which every generated dungeon has exactly one of,
 * at its entrance room) a right-click handler that teleports back to it - see
 * {@code BlockDungeonController}.</li>
 * </ul>
 * Once inside, the actual room-by-room exploration/expansion (players right-clicking
 * {@code TileDungeonSeal} blocks with a dungeon-key item to push the generator further) is
 * unchanged from upstream.
 */
public class VaultRitual extends Ritual {
    private static final double PLAYER_SEARCH_RADIUS = 8.0;

    public VaultRitual() {
        super(RitualRegistry.rl("vault"), 1, 200000, "ritual.bloodmagic.vault");
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
        // is down, mirroring how the old stand-in checked "does the loot chest already exist".
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
        ResourceLocation initialType = ModRoomPools.STANDARD_DUNGEON_ENTRANCES;
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
        addOffsetRunes(components, 1, 2, 0, EnumRuneType.DUSK);
        addCornerRunes(components, 1, 0, EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new VaultRitual();
    }
}
