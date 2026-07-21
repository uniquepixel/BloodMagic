package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.api.BMIdentifiers;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.blockentity.BloodAltarTile;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;

import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of the iconic Ritual of the Well of Suffering ({@code RitualWellOfSuffering}):
 * searches its altar range for a Blood Altar (caching the found offset so it doesn't rescan every
 * pulse), then chips away at non-player living entities in its damage range, feeding LP straight
 * into that altar for each successful hit. The original's per-entity sacrifice-value overrides and
 * sacrifice blacklist (both driven by a config/blacklist system this branch doesn't have) are
 * replaced with a flat value matching the original's own default (25 LP/HP).
 */
public class WellOfSufferingRitual extends Ritual {
    public static final String ALTAR_RANGE = "altar";
    public static final String DAMAGE_RANGE = "damage";
    public static final int SACRIFICE_AMOUNT = 25;

    private BlockPos altarOffsetPos = BlockPos.ZERO;

    public WellOfSufferingRitual() {
        super(RitualRegistry.rl("well_of_suffering"), 0, 40000, "ritual.bloodmagic.well_of_suffering");
        addBlockRange(ALTAR_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-5, -10, -5), 11, 21, 11));
        addBlockRange(DAMAGE_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-10, -10, -10), 21));

        setMaximumVolumeAndDistanceOfRange(ALTAR_RANGE, 0, 10, 15);
        setMaximumVolumeAndDistanceOfRange(DAMAGE_RANGE, 0, 15, 15);
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

        BlockPos altarPos = pos.offset(altarOffsetPos);
        AreaDescriptor altarRange = masterRitualStone.getBlockRange(ALTAR_RANGE);

        BloodAltarTile altar = level.getBlockEntity(altarPos) instanceof BloodAltarTile found ? found : null;

        if (!altarRange.isWithinArea(altarOffsetPos) || altar == null) {
            altar = null;
            for (BlockPos newPos : altarRange.getContainedPositions(pos)) {
                if (level.getBlockEntity(newPos) instanceof BloodAltarTile found) {
                    altar = found;
                    altarOffsetPos = newPos.subtract(pos);
                    altarRange.resetCache();
                    break;
                }
            }
        }

        if (altar != null) {
            AreaDescriptor damageRange = masterRitualStone.getBlockRange(DAMAGE_RANGE);
            AABB range = damageRange.getAABB(pos);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, range);

            for (LivingEntity entity : entities) {
                if (entity instanceof Player || !entity.isAlive()) {
                    continue;
                }

                int lifeEssenceRatio = SACRIFICE_AMOUNT;
                if (entity.hurt(level.damageSources().source(BMIdentifiers.DamageTypes.SACRIFICE, entity), 1)) {
                    if (entity.isBaby()) {
                        lifeEssenceRatio *= 0.5F;
                    }

                    altar.sacrificialDaggerCall(lifeEssenceRatio, true);
                    totalEffects++;

                    if (totalEffects >= maxEffects) {
                        break;
                    }
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
        return 2;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addCornerRunes(components, 1, 0, EnumRuneType.FIRE);
        addCornerRunes(components, 2, -1, EnumRuneType.FIRE);
        addParallelRunes(components, 2, -1, EnumRuneType.EARTH);
        addCornerRunes(components, -3, -1, EnumRuneType.DUSK);
        addOffsetRunes(components, 2, 4, -1, EnumRuneType.WATER);
        addOffsetRunes(components, 1, 4, 0, EnumRuneType.WATER);
        addParallelRunes(components, 4, 1, EnumRuneType.AIR);
    }

    @Override
    public Ritual getNewCopy() {
        return new WellOfSufferingRitual();
    }
}
