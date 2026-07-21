package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.blockentity.CrystalClusterTile;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.common.will.WorldWillHelper;

import java.util.function.Consumer;

/**
 * Full-fidelity-in-spirit port of 1.20.1's Ritual of Crystal Splitting ({@code RitualCrystalSplit}):
 * consumes a mature raw (Default-Will) crystal cluster above the Master Ritual Stone and seeds four
 * typed clusters - Vengeful, Corrosive, Steadfast, Destructive - at the four positions rotated around
 * the ritual's facing, aborting entirely (no cost, no effect) if any of the four target spots is
 * blocked by something that isn't air or a matching, still-growing cluster.
 * <p>
 * Adapted for this branch's crystal-cluster reality: 1.20.1 had five separate typed
 * {@code BlockDemonCrystal}s and a {@code TileDemonCrystal} with a 0-7 stacked crystal count that
 * this ritual directly read/wrote ({@code getCrystalCount}/{@code setCrystalCount}) to move progress
 * from the raw crystal into its four typed children. This branch instead has one generic
 * {@link CrystalClusterTile} that self-picks its Will type from the strongest ambient Will present
 * at placement (via its public {@code onPlacedByWorld()}) and has no count to read or write
 * directly. So here, each target spot is "seeded" by injecting a large burst of the desired
 * {@link EnumWillType} into the ambient aura at that position via {@code WorldWillHelper.addWill}
 * (biasing/accelerating whatever cluster ends up there, new or already-growing, toward that type),
 * then - for empty spots only - placing a fresh cluster block and immediately calling its
 * {@code onPlacedByWorld()} so it picks up that freshly-injected Will as its type. Consuming the raw
 * cluster is likewise adapted: since the tile exposes no direct "decrement progress" mutator, only
 * {@code CrystalClusterTile.harvest(Player)} (which requires a live player and grants them a Raw
 * Will item as a reward, rather than 1.20.1's silent server-side count decrement), the ritual
 * harvests the raw cluster on behalf of its owner when they're online and skips the pulse
 * otherwise - the same online-owner adaptation used by this batch's crystal_harvest ritual.
 */
public class CrystalSplitRitual extends Ritual {
    private static final double SEED_WILL_AMOUNT = 100;

    public CrystalSplitRitual() {
        super(RitualRegistry.rl("crystal_split"), 0, 20000, "ritual.bloodmagic.crystal_split");
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();

        if (currentEssence < getRefreshCost()) {
            masterRitualStone.causeNausea();
            return;
        }

        BlockPos pos = masterRitualStone.getMasterBlockPos();
        Direction direction = masterRitualStone.getDirection();
        BlockPos rawPos = pos.above(2);

        if (!(level.getBlockEntity(rawPos) instanceof CrystalClusterTile rawTile) || rawTile.getWillType() != EnumWillType.DEFAULT || !rawTile.isMature()) {
            return;
        }

        Player owner = level.getPlayerByUUID(masterRitualStone.getOwner());
        if (owner == null) {
            return;
        }

        BlockPos vengefulPos = pos.relative(rotateFacing(Direction.NORTH, direction)).above();
        BlockPos corrosivePos = pos.relative(rotateFacing(Direction.EAST, direction)).above();
        BlockPos steadfastPos = pos.relative(rotateFacing(Direction.SOUTH, direction)).above();
        BlockPos destructivePos = pos.relative(rotateFacing(Direction.WEST, direction)).above();

        if (!canSeed(level, vengefulPos, EnumWillType.VENGEFUL)
                || !canSeed(level, corrosivePos, EnumWillType.CORROSIVE)
                || !canSeed(level, steadfastPos, EnumWillType.STEADFAST)
                || !canSeed(level, destructivePos, EnumWillType.DESTRUCTIVE)) {
            return;
        }

        rawTile.harvest(owner);

        seedCrystal(level, vengefulPos, EnumWillType.VENGEFUL);
        seedCrystal(level, corrosivePos, EnumWillType.CORROSIVE);
        seedCrystal(level, steadfastPos, EnumWillType.STEADFAST);
        seedCrystal(level, destructivePos, EnumWillType.DESTRUCTIVE);

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost()));
    }

    private boolean canSeed(Level level, BlockPos pos, EnumWillType type) {
        if (level.getBlockEntity(pos) instanceof CrystalClusterTile tile) {
            return tile.getWillType() == type && !tile.isMature();
        }

        return level.isEmptyBlock(pos);
    }

    private void seedCrystal(Level level, BlockPos pos, EnumWillType type) {
        WorldWillHelper.addWill(level, pos, type, SEED_WILL_AMOUNT);

        if (level.getBlockEntity(pos) instanceof CrystalClusterTile) {
            // Already a matching, still-growing cluster here - the burst above accelerates its
            // own natural-growth tick, standing in for the original's crystal-count increment.
            return;
        }

        level.setBlockAndUpdate(pos, BMBlocks.CRYSTAL_CLUSTER.block().get().defaultBlockState());
        if (level.getBlockEntity(pos) instanceof CrystalClusterTile newTile) {
            newTile.onPlacedByWorld();
        }
    }

    public Direction rotateFacing(Direction facing, Direction rotation) {
        return switch (rotation) {
            case EAST -> facing.getClockWise();
            case SOUTH -> facing.getClockWise().getClockWise();
            case WEST -> facing.getCounterClockWise();
            default -> facing;
        };
    }

    @Override
    public int getRefreshTime() {
        return 20;
    }

    @Override
    public int getRefreshCost() {
        return 1000;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 0, 0, -1, EnumRuneType.FIRE);
        addRune(components, 1, 0, 0, EnumRuneType.EARTH);
        addRune(components, 0, 0, 1, EnumRuneType.WATER);
        addRune(components, -1, 0, 0, EnumRuneType.AIR);

        addOffsetRunes(components, 1, 2, -1, EnumRuneType.DUSK);
        addCornerRunes(components, 1, 0, EnumRuneType.BLANK);
        addParallelRunes(components, 2, 0, EnumRuneType.DUSK);
    }

    @Override
    public Ritual getNewCopy() {
        return new CrystalSplitRitual();
    }

    @Override
    public Component[] provideInformationOfRitualToPlayer(Player player) {
        return new Component[]{Component.translatable(this.getTranslationKey() + ".info")};
    }
}
