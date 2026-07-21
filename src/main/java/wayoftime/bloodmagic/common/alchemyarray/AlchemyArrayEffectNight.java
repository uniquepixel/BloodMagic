package wayoftime.bloodmagic.common.alchemyarray;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.blockentity.AlchemyArrayTile;
import wayoftime.bloodmagic.util.RitualUtil;

/**
 * Ported from 1.20.1's {@code AlchemyArrayEffectNight}: the {@link AlchemyArrayEffectDay}
 * mirror, fast-forwarding to the next midnight instead of the next sunrise.
 */
public class AlchemyArrayEffectNight extends AlchemyArrayEffect {
    private long startingTime = 0;

    @Override
    public boolean update(AlchemyArrayTile tile, int ticksActive) {
        Level world = tile.getLevel();
        if (world == null) {
            return false;
        }

        if (ticksActive == 100) {
            startingTime = world.getDayTime();
            tile.doDropIngredients(false);
        }

        if (ticksActive <= 100) {
            return false;
        }

        if (world.isClientSide && world instanceof ClientLevel clientLevel) {
            long finalTime = ((world.getDayTime() + 11000) / 24000) * 24000 + 13000;
            long time = (finalTime - startingTime) * (ticksActive - 100) / 100 + startingTime;

            clientLevel.getLevelData().setDayTime(time);
            return false;
        }

        if (world instanceof ServerLevel) {
            long finalTime = ((world.getDayTime() + 11000) / 24000) * 24000 + 13000;
            long time = (finalTime - startingTime) * (ticksActive - 100) / 100 + startingTime;
            for (ServerLevel serverLevel : world.getServer().getAllLevels()) {
                serverLevel.setDayTime(time);
            }

            if (ticksActive >= 200) {
                BlockPos pos = tile.getBlockPos();
                RitualUtil.spawnLightning((ServerLevel) world, pos, true);
                return true;
            }

            return false;
        }
        return false;
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
    }

    @Override
    public AlchemyArrayEffect getNewCopy() {
        return new AlchemyArrayEffectNight();
    }
}
