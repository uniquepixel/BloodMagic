package wayoftime.bloodmagic.common.ritual.types;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.api.datacomponent.LivingStats;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.UpgradeTome;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.common.living.LivingHelper;
import wayoftime.bloodmagic.common.living.LivingUpgrade;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.util.RitualUtil;

import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of Living Reversion ({@code RitualUpgradeRemove}): scans its
 * fixed check range for a full-set Living Armour wearer, pops each of their leveled-up upgrades out
 * into a tome item and wipes all upgrade experience, then deactivates and strikes a cosmetic
 * lightning bolt if anything was actually removed.
 * <p>
 * Adapted: the old flat NBT {@code LivingStats}/{@code LivingUpgrade} exp map and
 * {@code ItemLivingContainer.updateLivingStats}-driven "living tome" stack are replaced by this
 * branch's {@code BMDataComponents.UPGRADES} exp map and {@link UpgradeTome} data component /
 * {@code BMItems.UPGRADE_TOME} item, which serve the exact same role. After wiping the upgrade map,
 * {@code LivingHelper.recalcPoints} is called to keep this branch's separate
 * {@code CURRENT_UPGRADE_POINTS} bookkeeping (which didn't exist in 1.20.1) in sync.
 */
public class UpgradeRemoveRitual extends Ritual {
    public static final String CHECK_RANGE = "fillRange";

    public UpgradeRemoveRitual() {
        super(RitualRegistry.rl("upgrade_remove"), 0, 25000, "ritual.bloodmagic.upgrade_remove");
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
            LivingStats stats = chestStack.getOrDefault(BMDataComponents.UPGRADES, LivingStats.EMPTY);

            boolean removedUpgrade = false;
            for (Object2FloatMap.Entry<Holder<LivingUpgrade>> entry : stats.object2FloatEntrySet()) {
                Holder<LivingUpgrade> upgrade = entry.getKey();
                float exp = entry.getFloatValue();

                if (LivingHelper.getLevelFromXp(upgrade, exp) < 1) {
                    continue;
                }

                ItemStack tomeStack = new ItemStack(BMItems.UPGRADE_TOME.get());
                tomeStack.set(BMDataComponents.UPGRADE_TOME_DATA, new UpgradeTome(upgrade, exp));

                ItemEntity item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), tomeStack);
                level.addFreshEntity(item);
                removedUpgrade = true;
            }

            if (!stats.upgrades().isEmpty()) {
                chestStack.set(BMDataComponents.UPGRADES, LivingStats.EMPTY);
                LivingHelper.recalcPoints(player);
            }

            if (removedUpgrade) {
                masterRitualStone.setActive(false);

                if (level instanceof ServerLevel serverLevel) {
                    RitualUtil.spawnLightning(serverLevel, pos, true);
                }
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
        addCornerRunes(components, 1, 1, EnumRuneType.WATER);
        addParallelRunes(components, 4, 0, EnumRuneType.EARTH);
        addCornerRunes(components, 1, 3, EnumRuneType.WATER);
        addParallelRunes(components, 1, 4, EnumRuneType.AIR);

        for (int i = 0; i < 4; i++) {
            addCornerRunes(components, 3, i, EnumRuneType.EARTH);
        }
    }

    @Override
    public Ritual getNewCopy() {
        return new UpgradeRemoveRitual();
    }
}
