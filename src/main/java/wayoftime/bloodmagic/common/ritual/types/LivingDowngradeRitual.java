package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import wayoftime.bloodmagic.api.BMTags;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.UpgradeTome;
import wayoftime.bloodmagic.common.living.LivingHelper;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.util.RitualUtil;

import java.util.function.Consumer;

/**
 * Scoped-down port of 1.20.1's Ritual of Living Downgrade ({@code RitualLivingDowngrade}). The
 * original's mechanic was a generic point-budget economy: it read every item in a chest through
 * {@code ILivingUpgradePointsProvider}, summed "upgrade points" across arbitrary items, cross-
 * referenced a priority-ordered slot list and a {@code RecipeLivingDowngrade}/
 * {@code LivingArmorRegistrar} lookup table to figure out which of the old enum-based
 * {@code LivingUpgrade}s to apply, and juggled leftover points into "Living Tome Scrap" items - an
 * entire subsystem that no longer exists on this branch, which replaced enum upgrades with a
 * data-driven {@code Holder<LivingUpgrade>} registry and replaced the old points economy with
 * {@code UpgradeTome} items that each carry a specific upgrade + exp value directly
 * ({@code wayoftime.bloodmagic.common.datacomponent.UpgradeTome}, see {@link LivingHelper#applyExp}).
 * <p>
 * This port keeps the ritual's real range, activation/refresh cost and rune pattern verbatim, and
 * implements one clear, working core mechanic on top of the branch's actual upgrade-tome system:
 * each pulse, it looks for a non-crouching player wearing a full Living Armour set inside the
 * containment range (as the original did via {@code LivingUtil.hasFullSet}/this branch's
 * {@link LivingHelper#hasFullSet}), reads the chest two blocks along the stone's facing direction
 * (same offset the original used), and feeds the first tome it finds there whose upgrade is tagged
 * {@link BMTags.Living#IS_DOWNGRADE} straight into that player via
 * {@link LivingHelper#applyExpToCap}, consuming the tome and the LP cost. When the chest runs out of
 * downgrade tomes, the ritual deactivates itself with a cosmetic lightning bolt (via this branch's
 * {@code RitualUtil.spawnLightning}), mirroring the original's "downgrade complete" flourish. The
 * old priority-slot ordering, scrap-item change-making, training-bracelet block check and item-frame
 * fallback are all dropped as part of this scope-down - they were entirely about the removed points
 * economy.
 */
public class LivingDowngradeRitual extends Ritual {
    public static final String DOWNGRADE_RANGE = "containmentRange";

    public LivingDowngradeRitual() {
        super(RitualRegistry.rl("downgrade"), 0, 10000, "ritual.bloodmagic.downgrade");
        addBlockRange(DOWNGRADE_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-1, 0, -1), 3));
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos masterPos = masterRitualStone.getMasterBlockPos();
        Direction direction = masterRitualStone.getDirection();
        AreaDescriptor downgradeRange = masterRitualStone.getBlockRange(DOWNGRADE_RANGE);

        Player selectedPlayer = null;
        for (Player player : level.getEntitiesOfClass(Player.class, downgradeRange.getAABB(masterPos))) {
            if (!player.isCrouching() && LivingHelper.hasFullSet(player)) {
                selectedPlayer = player;
                break;
            }
        }

        if (selectedPlayer == null) {
            return;
        }

        BlockPos chestOffset = new BlockPos(0, 1, 0).relative(direction, 2);
        BlockPos chestPos = masterPos.offset(chestOffset);

        IItemHandler inv = level.getCapability(Capabilities.ItemHandler.BLOCK, chestPos, Direction.DOWN);
        if (inv == null) {
            selectedPlayer.displayClientMessage(Component.translatable("chat.bloodmagic.ritualLivingDowngrade.missingInventory", chestPos.getX(), chestPos.getY(), chestPos.getZ()), true);
            return;
        }

        for (int slot = 0; slot < inv.getSlots(); slot++) {
            ItemStack stack = inv.getStackInSlot(slot);
            UpgradeTome tome = stack.get(BMDataComponents.UPGRADE_TOME_DATA);
            if (tome == null || !tome.upgrade().is(BMTags.Living.IS_DOWNGRADE)) {
                continue;
            }

            float applied = LivingHelper.applyExpToCap(selectedPlayer, tome.upgrade(), tome.exp(), true);
            if (applied <= 0) {
                continue;
            }

            inv.extractItem(slot, 1, false);
            masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost()));

            if (!anyDowngradeTomeLeft(inv)) {
                masterRitualStone.setActive(false);
                RitualUtil.spawnLightning(serverLevel, masterPos, true);
            }

            return;
        }
    }

    private static boolean anyDowngradeTomeLeft(IItemHandler inv) {
        for (int i = 0; i < inv.getSlots(); i++) {
            UpgradeTome tome = inv.getStackInSlot(i).get(BMDataComponents.UPGRADE_TOME_DATA);
            if (tome != null && tome.upgrade().is(BMTags.Living.IS_DOWNGRADE)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getRefreshCost() {
        return 10;
    }

    @Override
    public int getRefreshTime() {
        return 1;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 0, 0, -1, EnumRuneType.AIR);
        addRune(components, 0, 0, -2, EnumRuneType.DUSK);
        addRune(components, 0, 1, -3, EnumRuneType.DUSK);
        addRune(components, 0, 2, -3, EnumRuneType.BLANK);
        addRune(components, 0, 3, -3, EnumRuneType.BLANK);
        addRune(components, 0, 1, -4, EnumRuneType.FIRE);

        for (int i = 1; i <= 3; i++) {
            addRune(components, 0, 0, i, EnumRuneType.AIR);
        }

        for (int sgn = -1; sgn <= 1; sgn += 2) {
            addRune(components, sgn, 0, 4, EnumRuneType.AIR);
            addRune(components, sgn * 2, 0, 2, EnumRuneType.AIR);
            addRune(components, sgn * 3, 0, 2, EnumRuneType.AIR);
            addRune(components, sgn * 3, 0, 3, EnumRuneType.AIR);
            addRune(components, sgn, 0, 0, EnumRuneType.EARTH);
            addRune(components, sgn, 0, 1, EnumRuneType.EARTH);
            addRune(components, sgn * 2, 0, -1, EnumRuneType.FIRE);
            addRune(components, sgn * 2, 0, -2, EnumRuneType.FIRE);
            addRune(components, sgn * 3, 0, -2, EnumRuneType.FIRE);
            addRune(components, sgn * 3, 0, -3, EnumRuneType.FIRE);
            addRune(components, sgn * 3, 0, -4, EnumRuneType.FIRE);
            addRune(components, sgn, 1, -1, EnumRuneType.AIR);
            addRune(components, sgn, 1, -2, EnumRuneType.AIR);
            addRune(components, sgn, 1, -4, EnumRuneType.FIRE);
            addRune(components, sgn * 2, 1, -4, EnumRuneType.FIRE);
            addRune(components, sgn, 0, -3, EnumRuneType.EARTH);
            addRune(components, sgn, 0, -4, EnumRuneType.EARTH);
            addRune(components, sgn, 0, -5, EnumRuneType.EARTH);
            addRune(components, sgn, 1, -5, EnumRuneType.EARTH);
            addRune(components, sgn, 2, -5, EnumRuneType.EARTH);
            addRune(components, sgn, 3, -5, EnumRuneType.EARTH);
            addRune(components, sgn, 3, -4, EnumRuneType.EARTH);
        }
    }

    @Override
    public Ritual getNewCopy() {
        return new LivingDowngradeRitual();
    }
}
