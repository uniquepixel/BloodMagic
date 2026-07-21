package wayoftime.bloodmagic.common.alchemyarray;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import wayoftime.bloodmagic.common.blockentity.AlchemyArrayTile;

/**
 * Ported from 1.20.1's {@code AlchemyArrayEffectCrafting}: the default effect used by every
 * array recipe that isn't one of the seven special-cased ones. Simply counts up to
 * {@link #tickLimit} ticks, then pops the recipe's output out as a dropped item entity.
 */
public class AlchemyArrayEffectCrafting extends AlchemyArrayEffect {
    public final ItemStack outputStack;
    public int tickLimit;

    public AlchemyArrayEffectCrafting(ItemStack outputStack) {
        this(outputStack, 200);
    }

    public AlchemyArrayEffectCrafting(ItemStack outputStack, int tickLimit) {
        this.outputStack = outputStack;
        this.tickLimit = tickLimit;
    }

    @Override
    public boolean update(AlchemyArrayTile tile, int ticksActive) {
        if (tile.getLevel() == null || tile.getLevel().isClientSide) {
            return false;
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

    @Override
    public void writeToNBT(CompoundTag tag) {
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
    }

    @Override
    public AlchemyArrayEffect getNewCopy() {
        return new AlchemyArrayEffectCrafting(outputStack, tickLimit);
    }
}
