package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.potion.BMPotions;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.common.will.WorldWillHelper;

import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of Grounding ({@code RitualGrounding}): every entity in
 * range gets a Will-gated status - Corrosive Will applies Suspended (no gravity), Vengeful Will
 * applies Levitation, Destructive Will additionally applies Heavy Heart (more fall damage), and
 * bosses immune to dimension travel (Wither/Ender Dragon) get nudged downward under Steadfast
 * Will. The original's default fallback (neither Corrosive nor Vengeful active) applied two
 * potions - Grounded and Gravity - that haven't been ported; this substitutes their evident intent
 * directly (zeroing vertical velocity so the entity can't gain height) rather than skip the
 * fallback branch entirely.
 */
public class GroundingRitual extends Ritual {
    public static final String GROUNDING_RANGE = "groundingRange";
    public static final double WILL_DRAIN = 0.1;

    public GroundingRitual() {
        super(RitualRegistry.rl("grounding"), 0, 5000, "ritual.bloodmagic.grounding");
        addBlockRange(GROUNDING_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-10, 0, -10), 21, 30, 21));
        setMaximumVolumeAndDistanceOfRange(GROUNDING_RANGE, 0, 200, 200);
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

        int maxEffects = currentEssence / getRefreshCost();
        int totalEffects = 0;

        List<EnumWillType> willConfig = masterRitualStone.getActiveWillConfig();

        double rawWill = this.getWillRespectingConfig(level, pos, EnumWillType.DEFAULT, willConfig);
        double corrosiveWill = this.getWillRespectingConfig(level, pos, EnumWillType.CORROSIVE, willConfig);
        double destructiveWill = this.getWillRespectingConfig(level, pos, EnumWillType.DESTRUCTIVE, willConfig);
        double steadfastWill = this.getWillRespectingConfig(level, pos, EnumWillType.STEADFAST, willConfig);
        double vengefulWill = this.getWillRespectingConfig(level, pos, EnumWillType.VENGEFUL, willConfig);

        double[] drain = {0, 0, 0, 0, 0}; // raw, corrosive, destructive, steadfast, vengeful

        AreaDescriptor groundingRange = masterRitualStone.getBlockRange(GROUNDING_RANGE);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, groundingRange.getAABB(pos));

        for (LivingEntity entity : entities) {
            if (totalEffects >= maxEffects) {
                break;
            }

            if (entity instanceof Player player && player.isCreative()) {
                continue;
            }

            totalEffects++;

            if (entity instanceof Player) {
                if (level.getGameTime() % 10 == 0 && rawWill >= WILL_DRAIN) {
                    drain[0] += WILL_DRAIN;
                    applySharedWillEffects(entity, corrosiveWill, destructiveWill, vengefulWill, drain);
                }
            } else if (entity.canChangeDimensions(level, level)) {
                if (level.getGameTime() % 10 == 0) {
                    applySharedWillEffects(entity, corrosiveWill, destructiveWill, vengefulWill, drain);
                }
            } else if (steadfastWill >= WILL_DRAIN) {
                if (entity instanceof WitherBoss || entity instanceof EnderDragon) {
                    entity.move(MoverType.SELF, new Vec3(0, -0.05, 0));
                }

                drain[3] += WILL_DRAIN / 10f;
                applySharedWillEffects(entity, corrosiveWill, destructiveWill, vengefulWill, drain);
            }
        }

        if (drain[0] > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.DEFAULT, drain[0]);
        }
        if (drain[1] > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.CORROSIVE, drain[1]);
        }
        if (drain[2] > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.DESTRUCTIVE, drain[2]);
        }
        if (drain[3] > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.STEADFAST, drain[3]);
        }
        if (drain[4] > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.VENGEFUL, drain[4]);
        }

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost() * totalEffects));
    }

    /**
     * @param drain corrosive/destructive/vengeful drains accumulate into indices 1/2/4 respectively.
     */
    private void applySharedWillEffects(LivingEntity entity, double corrosiveWill, double destructiveWill, double vengefulWill, double[] drain) {
        if (corrosiveWill >= WILL_DRAIN) {
            entity.addEffect(new MobEffectInstance(BMPotions.SUSPENDED, 20, 0));
            drain[1] += WILL_DRAIN;
        } else if (vengefulWill >= WILL_DRAIN) {
            drain[4] += WILL_DRAIN;
            entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 10));
        } else {
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x(), 0, motion.z());
        }

        if (destructiveWill >= WILL_DRAIN) {
            drain[2] += WILL_DRAIN;
            entity.addEffect(new MobEffectInstance(BMPotions.HEAVY_HEART, 100, 1));
        }
    }

    @Override
    public int getRefreshTime() {
        return 1;
    }

    @Override
    public int getRefreshCost() {
        return Math.max(1, getBlockRange(GROUNDING_RANGE).getVolume() / 10000);
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addParallelRunes(components, 1, 0, EnumRuneType.DUSK);
        addCornerRunes(components, 2, 2, EnumRuneType.EARTH);
        addCornerRunes(components, 3, 3, EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new GroundingRitual();
    }

    @Override
    public Component[] provideInformationOfRitualToPlayer(Player player) {
        return new Component[]{
                Component.translatable(this.getTranslationKey() + ".info"),
                Component.translatable(this.getTranslationKey() + ".default.info"),
                Component.translatable(this.getTranslationKey() + ".corrosive.info"),
                Component.translatable(this.getTranslationKey() + ".steadfast.info"),
                Component.translatable(this.getTranslationKey() + ".destructive.info"),
                Component.translatable(this.getTranslationKey() + ".vengeful.info")
        };
    }
}
