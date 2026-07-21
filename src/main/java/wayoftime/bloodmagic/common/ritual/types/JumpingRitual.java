package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.network.SetVelocityPayload;

import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of the High Jump ({@code RitualJumping}): launches living
 * entities in its jump range upward, with jump height controlled by how tall the player has built
 * the (purely vertical) jumpPower range. Players need their velocity force-synced, same as
 * {@link SpeedRitual} - see {@link SetVelocityPayload}.
 */
public class JumpingRitual extends Ritual {
    public static final String JUMP_RANGE = "jumpRange";
    public static final String JUMP_POWER = "jumpPower";

    public JumpingRitual() {
        super(RitualRegistry.rl("jumping"), 0, 5000, "ritual.bloodmagic.jumping");
        addBlockRange(JUMP_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-1, 1, -1), 3, 1, 3));
        setMaximumVolumeAndDistanceOfRange(JUMP_RANGE, 0, 5, 5);
        addBlockRange(JUMP_POWER, new AreaDescriptor.Rectangle(new BlockPos(0, 0, 0), 1, 5, 1));
        setMaximumVolumeAndDistanceOfRange(JUMP_POWER, 0, 1, 100);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();

        if (currentEssence < getRefreshCost()) {
            masterRitualStone.causeNausea();
            return;
        }

        int maxEffects = currentEssence / getRefreshCost();
        int totalEffects = 0;

        AreaDescriptor jumpRange = masterRitualStone.getBlockRange(JUMP_RANGE);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, jumpRange.getAABB(masterRitualStone.getMasterBlockPos()));

        for (LivingEntity entity : entities) {
            if (totalEffects >= maxEffects) {
                break;
            }

            double motionY = masterRitualStone.getBlockRange(JUMP_POWER).getHeight() * 0.3;

            entity.fallDistance = 0;
            if (entity.isShiftKeyDown()) {
                continue;
            }

            Vec3 motion = entity.getDeltaMovement();
            double motionX = motion.x();
            double motionZ = motion.z();

            totalEffects++;

            entity.setDeltaMovement(motionX, motionY, motionZ);
            if (entity instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new SetVelocityPayload(motionX, motionY, motionZ));
            }
        }

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost() * totalEffects));
    }

    @Override
    public int getRefreshTime() {
        return 1;
    }

    @Override
    public int getRefreshCost() {
        return getBlockRange(JUMP_POWER).getHeight();
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        for (int i = -1; i <= 1; i++) {
            addCornerRunes(components, 1, i, EnumRuneType.AIR);
        }
    }

    @Override
    public Ritual getNewCopy() {
        return new JumpingRitual();
    }
}
