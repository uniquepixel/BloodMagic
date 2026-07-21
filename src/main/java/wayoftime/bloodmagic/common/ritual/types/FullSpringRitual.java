package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;

import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of the Full Spring ({@code RitualWater}): fills its water
 * range with source blocks up to what the network's LP can afford per tick, and additionally
 * drains ambient default Will to (in the original) top up a fluid tank in the range. This branch
 * has no fluid-tank/capability system for that chest slot yet, so the Will-draining sub-effect is
 * dropped for now - the core water-generation loop, its LP cost, and its rune pattern are ported
 * as-is.
 */
public class FullSpringRitual extends Ritual {
    public static final String WATER_RANGE = "waterRange";

    public FullSpringRitual() {
        super(RitualRegistry.rl("full_spring"), 0, 500, "ritual.bloodmagic.full_spring");
        addBlockRange(WATER_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-1, 0, -1), 1, 1, 1));
        setMaximumVolumeAndDistanceOfRange(WATER_RANGE, 9, 3, 3);
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

        int maxEffects = currentEssence / getRefreshCost();
        int totalEffects = 0;

        AreaDescriptor waterRange = masterRitualStone.getBlockRange(WATER_RANGE);
        for (BlockPos newPos : waterRange.getContainedPositions(pos)) {
            if (level.isEmptyBlock(newPos)) {
                level.setBlockAndUpdate(newPos, Blocks.WATER.defaultBlockState());
                totalEffects++;
            }

            if (totalEffects >= maxEffects) {
                break;
            }
        }

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost() * totalEffects));
    }

    @Override
    public int getRefreshTime() {
        return 1;
    }

    @Override
    public int getRefreshCost() {
        return 25;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addCornerRunes(components, 1, 0, EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() {
        return new FullSpringRitual();
    }
}
