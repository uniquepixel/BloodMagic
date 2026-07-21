package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.common.will.WorldWillHelper;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of Regeneration ({@code RitualRegeneration}): heals
 * living entities in its heal range, and - fueled by ambient Corrosive Will - can siphon health
 * from non-player entities in its vampire range to heal players directly. The old ritual's
 * absorption sub-effect is dead code in the original too (the flag that would enable it is never
 * set), so it's dropped here as well rather than faithfully porting unreachable logic.
 */
public class RegenerationRitual extends Ritual {
    public static final String HEAL_RANGE = "heal";
    public static final String VAMPIRE_RANGE = "vampire";

    public static final int SACRIFICE_AMOUNT = 100;
    public static final double CORROSIVE_WILL_DRAIN = 0.04;

    public RegenerationRitual() {
        super(RitualRegistry.rl("regeneration"), 0, 25000, "ritual.bloodmagic.regeneration");
        addBlockRange(HEAL_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-15, -15, -15), 31));
        addBlockRange(VAMPIRE_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-15, -15, -15), 31));

        setMaximumVolumeAndDistanceOfRange(HEAL_RANGE, 0, 20, 20);
        setMaximumVolumeAndDistanceOfRange(VAMPIRE_RANGE, 0, 20, 20);
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
        int totalCost = 0;

        List<EnumWillType> willConfig = masterRitualStone.getActiveWillConfig();
        double corrosiveWill = this.getWillRespectingConfig(level, pos, EnumWillType.CORROSIVE, willConfig);
        double corrosiveDrain = 0;

        boolean syphonHealth = corrosiveWill >= CORROSIVE_WILL_DRAIN;

        AreaDescriptor healArea = masterRitualStone.getBlockRange(HEAL_RANGE);
        AABB healRange = healArea.getAABB(pos);

        AreaDescriptor damageArea = masterRitualStone.getBlockRange(VAMPIRE_RANGE);
        AABB damageRange = damageArea.getAABB(pos);

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, healRange);
        List<Player> players = level.getEntitiesOfClass(Player.class, healRange);
        List<LivingEntity> damagedEntities = level.getEntitiesOfClass(LivingEntity.class, damageRange);

        if (syphonHealth) {
            for (Player player : players) {
                if (player.getHealth() <= player.getMaxHealth() - 1) {
                    float syphonedHealthAmount = getSyphonAmountForWill(corrosiveWill);
                    Collections.shuffle(damagedEntities);
                    for (LivingEntity damagedEntity : damagedEntities) {
                        if (damagedEntity instanceof Player) {
                            continue;
                        }

                        float currentHealth = damagedEntity.getHealth();
                        damagedEntity.hurt(level.damageSources().source(BMIdentifiers.DamageTypes.SACRIFICE, damagedEntity), Math.min(player.getMaxHealth() - player.getHealth(), syphonedHealthAmount));

                        float healthDifference = currentHealth - damagedEntity.getHealth();
                        if (healthDifference > 0) {
                            corrosiveDrain += CORROSIVE_WILL_DRAIN;
                            corrosiveWill -= CORROSIVE_WILL_DRAIN;
                            player.heal(healthDifference);
                        }

                        break;
                    }
                }
            }
        }

        for (LivingEntity entity : entities) {
            float health = entity.getHealth();
            if (health <= entity.getMaxHealth() - 1 && entity.canBeAffected(new MobEffectInstance(MobEffects.REGENERATION))) {
                if (entity instanceof Player) {
                    totalCost += getRefreshCost();
                    currentEssence -= getRefreshCost();
                } else {
                    totalCost += getRefreshCost() / 10;
                    currentEssence -= getRefreshCost() / 10;
                }

                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50, 0, false, false));
                totalEffects++;

                if (totalEffects >= maxEffects) {
                    break;
                }
            }
        }

        if (corrosiveDrain > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.CORROSIVE, corrosiveDrain);
        }

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(totalCost));
    }

    @Override
    public int getRefreshTime() {
        return 50;
    }

    @Override
    public int getRefreshCost() {
        return SACRIFICE_AMOUNT;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 4, 0, 0, EnumRuneType.AIR);
        addRune(components, 5, 0, -1, EnumRuneType.AIR);
        addRune(components, 5, 0, 1, EnumRuneType.AIR);
        addRune(components, -4, 0, 0, EnumRuneType.AIR);
        addRune(components, -5, 0, -1, EnumRuneType.AIR);
        addRune(components, -5, 0, 1, EnumRuneType.AIR);
        addRune(components, 0, 0, 4, EnumRuneType.FIRE);
        addRune(components, 1, 0, 5, EnumRuneType.FIRE);
        addRune(components, -1, 0, 5, EnumRuneType.FIRE);
        addRune(components, 0, 0, -4, EnumRuneType.FIRE);
        addRune(components, 1, 0, -5, EnumRuneType.FIRE);
        addRune(components, -1, 0, -5, EnumRuneType.FIRE);
        addOffsetRunes(components, 3, 5, 0, EnumRuneType.WATER);
        addCornerRunes(components, 3, 0, EnumRuneType.DUSK);
        addOffsetRunes(components, 4, 5, 0, EnumRuneType.EARTH);
        addOffsetRunes(components, 4, 5, -1, EnumRuneType.EARTH);
        addCornerRunes(components, 5, 0, EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new RegenerationRitual();
    }

    public float getSyphonAmountForWill(double corrosiveWill) {
        return 1;
    }
}
