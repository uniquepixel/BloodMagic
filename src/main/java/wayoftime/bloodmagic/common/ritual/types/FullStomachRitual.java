package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;

import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of the Full Stomach ({@code RitualFullStomach}): consumes
 * one edible item from the chest in its chest range to build up a stored food/saturation pool,
 * then feeds nearby players from that pool each pulse until it's exhausted. Uses NeoForge's
 * item-handler capability in place of the original's Forge {@code IItemHandler}, and the modern
 * {@code DataComponents.FOOD} component in place of the removed {@code Item.isEdible()}/
 * {@code getFoodProperties()}.
 */
public class FullStomachRitual extends Ritual {
    public static final String FILL_RANGE = "fillRange";
    public static final String CHEST_RANGE = "chest";

    private int foodLevel = 0;
    private float storedSaturation = 0;

    public FullStomachRitual() {
        super(RitualRegistry.rl("full_stomach"), 0, 100000, "ritual.bloodmagic.full_stomach");
        addBlockRange(FILL_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-25, -25, -25), 51));
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

        setMaximumVolumeAndDistanceOfRange(FILL_RANGE, 0, 25, 25);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();
        BlockPos pos = masterRitualStone.getMasterBlockPos();

        if (getRefreshCost() > currentEssence) {
            masterRitualStone.causeNausea();
            return;
        }

        AreaDescriptor chestRange = masterRitualStone.getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(pos).get(0);
        IItemHandler inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, chestPos, null);
        if (inventory == null) {
            return;
        }

        AreaDescriptor fillingRange = masterRitualStone.getBlockRange(FILL_RANGE);
        List<Player> playerList = level.getEntitiesOfClass(Player.class, fillingRange.getAABB(pos));

        if (foodLevel <= 0) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack stack = inventory.extractItem(i, 1, true);
                FoodProperties food = stack.isEmpty() ? null : stack.get(DataComponents.FOOD);

                if (food != null) {
                    foodLevel = food.nutrition();
                    storedSaturation = food.saturation();
                    inventory.extractItem(i, 1, false);

                    masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost()));
                    break;
                }
            }

            if (foodLevel <= 0) {
                return;
            }
        }

        for (Player player : playerList) {
            FoodData foodStats = player.getFoodData();
            float satLevel = foodStats.getSaturationLevel();
            float saturationAmount = storedSaturation * 2.0f;

            while ((saturationAmount + satLevel <= 20 || satLevel < 5) && foodLevel > 0) {
                foodStats.eat(1, storedSaturation);
                satLevel = foodStats.getSaturationLevel();
                foodLevel--;
            }
        }
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        foodLevel = tag.getInt("foodLevel");
        storedSaturation = tag.getFloat("storedSaturation");
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putInt("foodLevel", foodLevel);
        tag.putFloat("storedSaturation", storedSaturation);
    }

    @Override
    public int getRefreshTime() {
        return 20;
    }

    @Override
    public int getRefreshCost() {
        return 100;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addParallelRunes(components, 3, 0, EnumRuneType.FIRE);
        addCornerRunes(components, 1, 0, EnumRuneType.AIR);
        addOffsetRunes(components, 1, 2, 0, EnumRuneType.AIR);
        addCornerRunes(components, 4, 0, EnumRuneType.WATER);
        addOffsetRunes(components, 4, 3, 0, EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new FullStomachRitual();
    }
}
