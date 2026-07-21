package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.blockentity.CrystalClusterTile;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;

import java.util.function.Consumer;

/**
 * Full-fidelity-in-spirit port of 1.20.1's Ritual of Crystal Harvesting ({@code RitualCrystalHarvest}):
 * once per pulse, finds one harvestable Will crystal cluster in its range and harvests it.
 * <p>
 * Adapted: 1.20.1's {@code TileDemonCrystal} kept a 0-7 stacked crystal count and
 * {@code dropSingleCrystal()} popped one crystal as a free-standing item entity, no player required.
 * This branch's crystal cluster ({@link CrystalClusterTile}) is a single generic block/tile with a
 * binary mature/immature state instead, and its only harvest entry point,
 * {@code CrystalClusterTile.harvest(Player)}, hands the Raw Will reward directly to a real player
 * (inventory-add or drop-at-feet) rather than spawning a world item entity - so this ritual harvests
 * on behalf of its owner when they're online, and simply skips the pulse if they're not (the same
 * adaptation used by this batch's crystal_split ritual).
 */
public class CrystalHarvestRitual extends Ritual {
    public static final String CRYSTAL_RANGE = "crystal";

    public CrystalHarvestRitual() {
        super(RitualRegistry.rl("crystal_harvest"), 0, 40000, "ritual.bloodmagic.crystal_harvest");
        addBlockRange(CRYSTAL_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-3, 2, -3), 7, 5, 7));

        setMaximumVolumeAndDistanceOfRange(CRYSTAL_RANGE, 250, 5, 7);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();
        BlockPos pos = masterRitualStone.getMasterBlockPos();

        if (currentEssence < getRefreshCost()) {
            masterRitualStone.causeNausea();
            return;
        }

        Player owner = level.getPlayerByUUID(masterRitualStone.getOwner());
        if (owner == null) {
            return;
        }

        int maxEffects = 1;
        int totalEffects = 0;

        AreaDescriptor crystalRange = masterRitualStone.getBlockRange(CRYSTAL_RANGE);

        for (BlockPos nextPos : crystalRange.getContainedPositions(pos)) {
            if (level.getBlockEntity(nextPos) instanceof CrystalClusterTile crystalTile && crystalTile.isMature()) {
                crystalTile.harvest(owner);
                totalEffects++;

                if (totalEffects >= maxEffects) {
                    break;
                }
            }
        }

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost() * totalEffects));
    }

    @Override
    public int getRefreshTime() {
        return 25;
    }

    @Override
    public int getRefreshCost() {
        return 50;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addCornerRunes(components, 1, 0, EnumRuneType.AIR);
        addParallelRunes(components, 1, 1, EnumRuneType.DUSK);
        addParallelRunes(components, 1, -1, EnumRuneType.FIRE);
        addParallelRunes(components, 2, -1, EnumRuneType.FIRE);
        addParallelRunes(components, 3, -1, EnumRuneType.FIRE);
        addOffsetRunes(components, 3, 1, -1, EnumRuneType.FIRE);
        addCornerRunes(components, 3, -1, EnumRuneType.EARTH);
        addCornerRunes(components, 3, 0, EnumRuneType.EARTH);
        addOffsetRunes(components, 3, 2, 0, EnumRuneType.DUSK);
    }

    @Override
    public Ritual getNewCopy() {
        return new CrystalHarvestRitual();
    }
}
