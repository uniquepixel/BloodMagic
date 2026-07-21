package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.potion.BMPotions;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;

import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of the Condor ({@code RitualCondor}): grants every player
 * in its flight range real creative-style flight via {@link BMPotions#FLIGHT}, charged per player
 * per pulse.
 */
public class CondorRitual extends Ritual {
    public static final String FLIGHT_RANGE = "flightRange";

    public CondorRitual() {
        super(RitualRegistry.rl("condor"), 0, 1000000, "ritual.bloodmagic.condor");
        addBlockRange(FLIGHT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-10, 0, -10), new BlockPos(10, 30, 10)));
        setMaximumVolumeAndDistanceOfRange(FLIGHT_RANGE, 0, 100, 200);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        AABB aabb = masterRitualStone.getBlockRange(FLIGHT_RANGE).getAABB(masterRitualStone.getMasterBlockPos());
        Level level = masterRitualStone.getWorldObj();

        int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();

        List<Player> players = level.getEntitiesOfClass(Player.class, aabb);
        int entityCount = players.size();

        if (currentEssence < getRefreshCost() * entityCount) {
            masterRitualStone.causeNausea();
            return;
        }

        for (Player player : players) {
            player.addEffect(new MobEffectInstance(BMPotions.FLIGHT, 20, 0, true, false));
        }

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost() * entityCount));
    }

    @Override
    public int getRefreshTime() {
        return 10;
    }

    @Override
    public int getRefreshCost() {
        return 5;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addParallelRunes(components, 1, 0, EnumRuneType.DUSK);
        addCornerRunes(components, 2, 0, EnumRuneType.AIR);
        addOffsetRunes(components, 1, 3, 0, EnumRuneType.EARTH);
        addParallelRunes(components, 3, 0, EnumRuneType.EARTH);
        addOffsetRunes(components, 3, 4, 0, EnumRuneType.WATER);
        addParallelRunes(components, 1, 1, EnumRuneType.FIRE);
        addParallelRunes(components, 2, 1, EnumRuneType.BLANK);
        addParallelRunes(components, 4, 1, EnumRuneType.BLANK);
        addParallelRunes(components, 5, 1, EnumRuneType.AIR);
        addParallelRunes(components, 5, 0, EnumRuneType.DUSK);

        for (int i = 2; i <= 4; i++) {
            addParallelRunes(components, i, 2, EnumRuneType.EARTH);
        }

        addOffsetRunes(components, 2, 1, 4, EnumRuneType.FIRE);
        addCornerRunes(components, 2, 4, EnumRuneType.AIR);
        addCornerRunes(components, 4, 2, EnumRuneType.FIRE);

        for (int i = -1; i <= 1; i++) {
            addOffsetRunes(components, 3, i, 4, EnumRuneType.EARTH);
        }
    }

    @Override
    public Ritual getNewCopy() {
        return new CondorRitual();
    }
}
