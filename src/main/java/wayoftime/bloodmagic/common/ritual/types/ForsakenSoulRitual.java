package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.blockentity.CrystalClusterTile;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.item.RawSoulItem;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.common.will.WorldWillHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity-in-spirit port of 1.20.1's Ritual of the Forsaken Soul ({@code RitualForsakenSoul}):
 * consumes items from an adjacent chest to hasten the growth of Will crystal clusters in range.
 * <p>
 * Adapted: the original fed dedicated {@code ItemCrystalCatalyst} stacks into
 * {@code TileDemonCrystal.applyCatalyst}, a whole item + tile-mutator pairing this branch doesn't
 * have. In its place, this pulls {@link RawSoulItem} (Raw Will) stacks - which already carry a
 * {@code EnumWillType}/amount pair via data components - out of the chest and injects that Will
 * directly into the ambient aura at a randomly chosen immature {@link CrystalClusterTile} in range,
 * which that cluster's own natural growth tick then draws down over subsequent seconds. This keeps
 * the ritual's "feed stored soul essence to the crystal grove to speed it up" mechanic real and
 * working rather than dropping it, while running entirely on items and helpers this branch already
 * has.
 */
public class ForsakenSoulRitual extends Ritual {
    public static final String CRYSTAL_RANGE = "crystal";
    public static final String CHEST_RANGE = "chest";

    public ForsakenSoulRitual() {
        super(RitualRegistry.rl("forsaken_soul"), 0, 40000, "ritual.bloodmagic.forsaken_soul");
        addBlockRange(CRYSTAL_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-3, -7, -3), 7, 5, 7));
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 2, 0), 1));

        setMaximumVolumeAndDistanceOfRange(CRYSTAL_RANGE, 250, 5, 7);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
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

        int maxEffects = Math.min(100, currentEssence / getRefreshCost());
        int totalEffects = 0;

        AreaDescriptor chestRange = masterRitualStone.getBlockRange(CHEST_RANGE);
        List<BlockPos> chestList = chestRange.getContainedPositions(pos);
        if (chestList.isEmpty()) {
            return;
        }

        IItemHandler inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, chestList.get(0), null);
        if (inventory == null) {
            return;
        }

        List<CrystalClusterTile> crystalList = new ArrayList<>();

        AreaDescriptor crystalRange = masterRitualStone.getBlockRange(CRYSTAL_RANGE);
        for (BlockPos nextPos : crystalRange.getContainedPositions(pos)) {
            if (level.getBlockEntity(nextPos) instanceof CrystalClusterTile crystalTile && !crystalTile.isMature()) {
                crystalList.add(crystalTile);
            }
        }

        if (crystalList.isEmpty()) {
            return;
        }

        Collections.shuffle(crystalList);

        int crystalIndex = 0;
        for (int i = 0; i < inventory.getSlots() && crystalIndex < crystalList.size() && totalEffects < maxEffects; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof RawSoulItem)) {
                continue;
            }

            EnumWillType willType = stack.getOrDefault(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.DEFAULT);
            double amount = stack.getOrDefault(BMDataComponents.DEMON_WILL_AMOUNT, 0D);
            if (amount <= 0) {
                continue;
            }

            CrystalClusterTile crystalTile = crystalList.get(crystalIndex);
            WorldWillHelper.addWill(level, crystalTile.getBlockPos(), willType, amount);
            inventory.extractItem(i, 1, false);

            crystalIndex++;
            totalEffects++;
        }

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost() * totalEffects));
    }

    @Override
    public int getRefreshTime() {
        return 25;
    }

    @Override
    public int getRefreshCost() {
        return 2;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addCornerRunes(components, 1, 0, EnumRuneType.AIR);
        addParallelRunes(components, 1, -1, EnumRuneType.DUSK);
        addParallelRunes(components, 1, 1, EnumRuneType.FIRE);
        addParallelRunes(components, 2, 1, EnumRuneType.FIRE);
        addParallelRunes(components, 3, 1, EnumRuneType.FIRE);
        addOffsetRunes(components, 3, 1, 1, EnumRuneType.FIRE);
        addCornerRunes(components, 3, 1, EnumRuneType.EARTH);
        addCornerRunes(components, 3, 0, EnumRuneType.EARTH);
        addOffsetRunes(components, 3, 2, 0, EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new ForsakenSoulRitual();
    }
}
