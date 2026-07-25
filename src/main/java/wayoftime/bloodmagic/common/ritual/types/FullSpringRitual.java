package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.common.will.WorldWillHelper;

import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of the Full Spring ({@code RitualWater}): fills its water
 * range with source blocks up to what the network's LP can afford per tick, and additionally
 * drains ambient default Will to top up a fluid tank sitting in a second, independently linked
 * single-block range (the original's {@code WATER_TANK_RANGE} - despite the original naming a
 * local variable {@code chestRange}, it is not a chest/item lookup: it fetches whatever block
 * entity sits at that linked position and asks it directly for a fluid-handler capability, then
 * fills it with vanilla water). This ports that lookup onto this branch's block-entity fluid
 * capability ({@code Capabilities.FluidHandler.BLOCK}, the same registration point used by
 * {@code BloodTankTile}), and the ambient Will drain onto {@link WorldWillHelper} (this branch's
 * equivalent of 1.20.1's {@code WorldDemonWillHandler}).
 */
public class FullSpringRitual extends Ritual {
    public static final String WATER_RANGE = "waterRange";
    public static final String WATER_TANK_RANGE = "waterTank";

    public FullSpringRitual() {
        super(RitualRegistry.rl("full_spring"), 0, 500, "ritual.bloodmagic.full_spring");
        addBlockRange(WATER_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-1, 0, -1), 1, 1, 1));
        addBlockRange(WATER_TANK_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

        setMaximumVolumeAndDistanceOfRange(WATER_RANGE, 9, 3, 3);
        setMaximumVolumeAndDistanceOfRange(WATER_TANK_RANGE, 1, 10, 10);
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
        List<EnumWillType> willConfig = masterRitualStone.getActiveWillConfig();

        int maxEffects = currentEssence / getRefreshCost();
        int totalEffects = 0;
        int lpDrain = 0;

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

        lpDrain += getRefreshCost() * totalEffects;

        double rawWill = this.getWillRespectingConfig(level, pos, EnumWillType.DEFAULT, willConfig);
        double rawDrained = 0;

        if (rawWill > 0) {
            AreaDescriptor tankRange = masterRitualStone.getBlockRange(WATER_TANK_RANGE);
            BlockPos tankPos = tankRange.getContainedPositions(pos).get(0);
            double drain = getWillCostForRawWill(rawWill);
            int lpCost = getLPCostForRawWill(rawWill);

            if (rawWill >= drain && currentEssence >= lpCost) {
                IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, tankPos, null);
                if (handler != null) {
                    int filled = handler.fill(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
                    double ratio = filled / (double) FluidType.BUCKET_VOLUME;

                    rawWill -= drain * ratio;
                    rawDrained += drain * ratio;

                    int lpCostFilled = (int) Math.ceil(lpCost * ratio);
                    currentEssence -= lpCostFilled;
                    lpDrain += lpCostFilled;
                }
            }
        }

        if (rawDrained > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.DEFAULT, rawDrained);
        }

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(lpDrain));
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

    public int getLPCostForRawWill(double raw) {
        return Math.max((int) (20 - raw / 10), 0);
    }

    public double getWillCostForRawWill(double raw) {
        return Math.min(1, raw / 1000);
    }
}
