package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.potion.BMPotions;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.common.will.WorldWillHelper;
import wayoftime.bloodmagic.network.SetVelocityPayload;

import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of Speed ({@code RitualSpeed}): launches living entities
 * in its range in the stone's facing direction, with default Will boosting speed, Steadfast Will
 * granting Soft Fall, and Corrosive/Destructive/Vengeful Will gating which of children/adults get
 * carried. Players need the velocity force-synced over the network (plain server-side
 * {@code setDeltaMovement} gets overridden by client movement prediction next tick), so this uses
 * {@link SetVelocityPayload}, the direct replacement for the original's custom sync packet.
 */
public class SpeedRitual extends Ritual {
    public static final String SPEED_RANGE = "sanicRange";

    public static final double VENGEFUL_WILL_DRAIN = 0.05;
    public static final double DESTRUCTIVE_WILL_DRAIN = 0.05;
    public static final double RAW_WILL_DRAIN = 0.1;
    public static final double STEADFAST_WILL_DRAIN = 0.05;
    public static final double CORROSIVE_WILL_DRAIN = 0.05;

    public SpeedRitual() {
        super(RitualRegistry.rl("speed"), 0, 1000, "ritual.bloodmagic.speed");
        addBlockRange(SPEED_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-2, 1, -2), new BlockPos(3, 5, 3)));
        setMaximumVolumeAndDistanceOfRange(SPEED_RANGE, 0, 4, 5);
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

        double corrosiveWill = this.getWillRespectingConfig(level, pos, EnumWillType.CORROSIVE, willConfig);
        double destructiveWill = this.getWillRespectingConfig(level, pos, EnumWillType.DESTRUCTIVE, willConfig);
        double rawWill = this.getWillRespectingConfig(level, pos, EnumWillType.DEFAULT, willConfig);
        double steadfastWill = this.getWillRespectingConfig(level, pos, EnumWillType.STEADFAST, willConfig);
        double vengefulWill = this.getWillRespectingConfig(level, pos, EnumWillType.VENGEFUL, willConfig);

        AreaDescriptor speedRange = masterRitualStone.getBlockRange(SPEED_RANGE);

        double vengefulDrain = 0;
        double destructiveDrain = 0;
        double rawDrain = 0;
        double steadfastDrain = 0;
        double corrosiveDrain = 0;

        if (rawWill < RAW_WILL_DRAIN) {
            rawWill = 0;
        }

        if (corrosiveWill < CORROSIVE_WILL_DRAIN) {
            corrosiveWill = 0;
        }

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, speedRange.getAABB(pos))) {
            if (entity.isShiftKeyDown()) {
                continue;
            }

            boolean transportChildren = destructiveWill < DESTRUCTIVE_WILL_DRAIN;
            boolean transportAdults = vengefulWill < VENGEFUL_WILL_DRAIN;

            if ((entity.isBaby() && !transportChildren) || (!entity.isBaby() && !transportAdults)) {
                continue;
            }

            if (entity instanceof Player && (transportChildren ^ transportAdults)) {
                continue;
            }

            if (!transportChildren) {
                destructiveWill -= DESTRUCTIVE_WILL_DRAIN;
                destructiveDrain += DESTRUCTIVE_WILL_DRAIN;
            }

            if (!transportAdults) {
                vengefulWill -= VENGEFUL_WILL_DRAIN;
                vengefulDrain += VENGEFUL_WILL_DRAIN;
            }

            double motionY = getVerticalSpeedForWill(rawWill);
            double speed = getHorizontalSpeedForWill(rawWill);
            Direction direction = masterRitualStone.getDirection();

            if (rawWill >= RAW_WILL_DRAIN) {
                rawWill -= RAW_WILL_DRAIN;
                rawDrain += RAW_WILL_DRAIN;
            }

            if (corrosiveWill >= CORROSIVE_WILL_DRAIN) {
                corrosiveWill -= CORROSIVE_WILL_DRAIN;
                corrosiveDrain += CORROSIVE_WILL_DRAIN;
                speed += getAdditionalHorizontalSpeedForWill(corrosiveWill);
            }

            Vec3 motion = entity.getDeltaMovement();
            double motionX = motion.x();
            double motionZ = motion.z();
            entity.fallDistance = 0;

            switch (direction) {
                case NORTH -> {
                    motionX = 0;
                    motionZ = -speed;
                }
                case SOUTH -> {
                    motionX = 0;
                    motionZ = speed;
                }
                case WEST -> {
                    motionX = -speed;
                    motionZ = 0;
                }
                case EAST -> {
                    motionX = speed;
                    motionZ = 0;
                }
                default -> {
                }
            }

            if (steadfastWill >= STEADFAST_WILL_DRAIN) {
                entity.addEffect(new MobEffectInstance(BMPotions.SOFT_FALL, 100, 0));
                steadfastWill -= STEADFAST_WILL_DRAIN;
                steadfastDrain += STEADFAST_WILL_DRAIN;
            }

            entity.setDeltaMovement(motionX, motionY, motionZ);
            if (entity instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new SetVelocityPayload(motionX, motionY, motionZ));
            }
        }

        if (rawDrain > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.DEFAULT, rawDrain);
        }

        if (vengefulDrain > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.VENGEFUL, vengefulDrain);
        }

        if (destructiveDrain > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.DESTRUCTIVE, destructiveDrain);
        }

        if (steadfastDrain > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.STEADFAST, steadfastDrain);
        }

        if (corrosiveDrain > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.CORROSIVE, corrosiveDrain);
        }
    }

    @Override
    public int getRefreshTime() {
        return 1;
    }

    @Override
    public int getRefreshCost() {
        return 5;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 0, 0, -2, EnumRuneType.DUSK);
        addRune(components, 1, 0, -1, EnumRuneType.AIR);
        addRune(components, -1, 0, -1, EnumRuneType.AIR);
        for (int i = 0; i < 3; i++) {
            addRune(components, 2, 0, i, EnumRuneType.AIR);
            addRune(components, -2, 0, i, EnumRuneType.AIR);
        }
    }

    @Override
    public Ritual getNewCopy() {
        return new SpeedRitual();
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

    public double getVerticalSpeedForWill(double rawWill) {
        return 1.2 + rawWill / 200;
    }

    public double getHorizontalSpeedForWill(double rawWill) {
        return 3 + rawWill / 40;
    }

    public double getAdditionalHorizontalSpeedForWill(double corrosiveWill) {
        return corrosiveWill / 40;
    }
}
