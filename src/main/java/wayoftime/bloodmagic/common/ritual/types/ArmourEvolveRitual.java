package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.living.LivingHelper;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.util.RitualUtil;

import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of Living Update ({@code RitualArmourEvolve}): scans its
 * fixed check range for a player wearing a full, un-evolved Living Armour set and flips it to its
 * evolved state, deactivating the ritual and striking a cosmetic lightning bolt on success.
 * <p>
 * Adapted: the old NBT-backed {@code LivingStats.isEvolved()}/{@code ILivingContainer.canLivingEvolve()}
 * pair is replaced by this branch's {@code BMDataComponents.IS_EVOLVED} boolean data component
 * (see {@code UpgradeHolderBase.getMaxUpgradePoints}), and {@code LivingHelper.hasFullSet} stands in
 * for the old full-set check. This branch has only one Living Armour chestpiece item (no separate
 * "base" tier that can never evolve), so the old {@code canLivingEvolve()} gate - which only ever
 * differed between that base tier and the real armour - is dropped as vacuous. Lightning is spawned
 * via the branch's existing {@code RitualUtil.spawnLightning} helper in place of the old raw
 * {@code EntityType.LIGHTNING_BOLT.create(world)} call.
 */
public class ArmourEvolveRitual extends Ritual {
    public static final String CHECK_RANGE = "fillRange";

    public ArmourEvolveRitual() {
        super(RitualRegistry.rl("armour_evolve"), 0, 50000, "ritual.bloodmagic.armour_evolve");
        addBlockRange(CHECK_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1, 2, 1));
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        if (level.isClientSide) {
            return;
        }

        BlockPos pos = masterRitualStone.getMasterBlockPos();
        AreaDescriptor checkRange = masterRitualStone.getBlockRange(CHECK_RANGE);

        List<Player> playerList = level.getEntitiesOfClass(Player.class, checkRange.getAABB(pos));

        for (Player player : playerList) {
            if (!LivingHelper.hasFullSet(player)) {
                continue;
            }

            ItemStack chestStack = LivingHelper.getChest(player);
            if (chestStack.getOrDefault(BMDataComponents.IS_EVOLVED, false)) {
                continue;
            }

            chestStack.set(BMDataComponents.IS_EVOLVED, true);

            masterRitualStone.setActive(false);

            if (level instanceof ServerLevel serverLevel) {
                RitualUtil.spawnLightning(serverLevel, pos, true);
            }
        }
    }

    @Override
    public int getRefreshTime() {
        return 1;
    }

    @Override
    public int getRefreshCost() {
        return 0;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addCornerRunes(components, 1, 0, EnumRuneType.DUSK);
        addCornerRunes(components, 2, 0, EnumRuneType.FIRE);
        addOffsetRunes(components, 1, 2, 0, EnumRuneType.FIRE);
        addCornerRunes(components, 1, 1, EnumRuneType.DUSK);
        addParallelRunes(components, 4, 0, EnumRuneType.EARTH);
        addCornerRunes(components, 1, 3, EnumRuneType.DUSK);
        addParallelRunes(components, 1, 4, EnumRuneType.EARTH);

        for (int i = 0; i < 4; i++) {
            addCornerRunes(components, 3, i, EnumRuneType.EARTH);
        }
    }

    @Override
    public Ritual getNewCopy() {
        return new ArmourEvolveRitual();
    }
}
