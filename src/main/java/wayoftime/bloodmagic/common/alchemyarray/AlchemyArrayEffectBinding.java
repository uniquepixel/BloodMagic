package wayoftime.bloodmagic.common.alchemyarray;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import wayoftime.bloodmagic.common.blockentity.AlchemyArrayTile;
import wayoftime.bloodmagic.util.RitualUtil;

/**
 * Ported from 1.20.1's {@code AlchemyArrayEffectBinding}: a slower crafting effect (300 ticks
 * instead of the base 200) used for the Living-armor "binding" arrays, with periodic
 * visual-only lightning strikes while it charges.
 * <p>
 * The original timed those strikes to a 5-point-star sweep animation driven by a dedicated
 * {@code BindingAlchemyCircleRenderer} (custom quadrilateral geometry keyed to unported
 * {@code bindingarray.png}/{@code bindinglightningarray.png} textures - see the class doc on
 * {@link wayoftime.bloodmagic.client.render.blockentity.AlchemyArrayRenderer} for what this
 * branch's array renderer does instead). Since that render system doesn't exist here, the
 * lightning below instead orbits the array at a fixed radius once every 50 ticks - it keeps the
 * "charging up" spectacle without depending on geometry built for a texture set that isn't
 * ported.
 */
public class AlchemyArrayEffectBinding extends AlchemyArrayEffectCrafting {
    public AlchemyArrayEffectBinding(ItemStack outputStack, int tickLimit) {
        super(outputStack, tickLimit);
    }

    public AlchemyArrayEffectBinding(ItemStack outputStack) {
        this(outputStack, 300);
    }

    @Override
    public boolean update(AlchemyArrayTile tile, int ticksActive) {
        if (tile.getLevel() == null || tile.getLevel().isClientSide) {
            return false;
        }

        if (ticksActive >= 50 && ticksActive <= 250) {
            spawnLightningOnCircle(tile, ticksActive);
        }

        if (ticksActive >= tickLimit) {
            BlockPos pos = tile.getBlockPos();
            ItemStack output = outputStack.copy();
            ItemEntity outputEntity = new ItemEntity(tile.getLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, output);
            tile.getLevel().addFreshEntity(outputEntity);
            return true;
        }

        return false;
    }

    private void spawnLightningOnCircle(AlchemyArrayTile tile, int ticksActive) {
        if (ticksActive % 50 != 0 || !(tile.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        int circle = ticksActive / 50 - 1;
        double distance = 1.5D;
        double angle = circle * 2 * Math.PI / 5D;

        double dispX = distance * Math.sin(angle);
        double dispZ = -distance * Math.cos(angle);

        BlockPos strikePos = tile.getBlockPos().offset((int) Math.round(dispX), 0, (int) Math.round(dispZ));
        RitualUtil.spawnLightning(serverLevel, strikePos, true);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
    }

    @Override
    public AlchemyArrayEffect getNewCopy() {
        return new AlchemyArrayEffectBinding(outputStack, tickLimit);
    }
}
