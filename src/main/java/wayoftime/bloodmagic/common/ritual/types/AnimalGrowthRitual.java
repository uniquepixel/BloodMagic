package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
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
 * Full-fidelity port of 1.20.1's Ritual of Animal Growth ({@code RitualAnimalGrowth}): ages up
 * babies instantly, Vengeful Will reduces adults' breeding cooldown, Steadfast Will auto-feeds/
 * breeds adults from the chest in its chest range, and Destructive Will marks unbred adults with
 * Sacrificial Lamb (a "kamikaze" mode that detonates them near hostile mobs).
 */
public class AnimalGrowthRitual extends Ritual {
    public static final double RAW_WILL_DRAIN = 0.05;
    public static final double VENGEFUL_WILL_DRAIN = 0.02;
    public static final double STEADFAST_WILL_DRAIN = 0.1;
    public static final double DESTRUCTIVE_WILL_DRAIN = 1;

    public static final String GROWTH_RANGE = "growing";
    public static final String CHEST_RANGE = "chest";
    public static final int DEFAULT_REFRESH_TIME = 20;

    private int refreshTime = DEFAULT_REFRESH_TIME;

    public AnimalGrowthRitual() {
        super(RitualRegistry.rl("animal_growth"), 0, 10000, "ritual.bloodmagic.animal_growth");
        addBlockRange(GROWTH_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-2, 1, -2), 5, 2, 5));
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

        setMaximumVolumeAndDistanceOfRange(GROWTH_RANGE, 0, 7, 7);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();

        if (currentEssence < getRefreshCost()) {
            masterRitualStone.causeNausea();
            return;
        }

        int maxGrowths = currentEssence / getRefreshCost();
        int totalGrowths = 0;
        BlockPos pos = masterRitualStone.getMasterBlockPos();

        AreaDescriptor chestRange = masterRitualStone.getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(pos).get(0);
        IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, chestPos, null);

        List<EnumWillType> willConfig = masterRitualStone.getActiveWillConfig();

        double rawWill = this.getWillRespectingConfig(level, pos, EnumWillType.DEFAULT, willConfig);
        double steadfastWill = this.getWillRespectingConfig(level, pos, EnumWillType.STEADFAST, willConfig);
        double vengefulWill = this.getWillRespectingConfig(level, pos, EnumWillType.VENGEFUL, willConfig);
        double destructiveWill = this.getWillRespectingConfig(level, pos, EnumWillType.DESTRUCTIVE, willConfig);

        refreshTime = getRefreshTimeForRawWill(rawWill);
        boolean consumeRawWill = rawWill >= RAW_WILL_DRAIN && refreshTime != DEFAULT_REFRESH_TIME;

        double vengefulDrain = 0;
        double steadfastDrain = 0;
        double destructiveDrain = 0;

        boolean decreaseBreedTimer = vengefulWill >= VENGEFUL_WILL_DRAIN;
        boolean breedAnimals = steadfastWill >= STEADFAST_WILL_DRAIN && itemHandler != null;
        boolean kamikaze = destructiveWill >= DESTRUCTIVE_WILL_DRAIN;

        AreaDescriptor growingRange = masterRitualStone.getBlockRange(GROWTH_RANGE);
        AABB axis = growingRange.getAABB(pos);
        List<Animal> animalList = level.getEntitiesOfClass(Animal.class, axis);

        boolean performedEffect = false;

        for (Animal animal : animalList) {
            if (animal.getAge() < 0) {
                animal.ageUp(5);
                totalGrowths++;
                performedEffect = true;
            } else if (animal.getAge() > 0) {
                if (decreaseBreedTimer) {
                    if (vengefulWill >= VENGEFUL_WILL_DRAIN) {
                        animal.setAge(Math.max(0, animal.getAge() - getBreedingDecreaseForWill(vengefulWill)));
                        vengefulDrain += VENGEFUL_WILL_DRAIN;
                        vengefulWill -= VENGEFUL_WILL_DRAIN;
                        performedEffect = true;
                    } else {
                        decreaseBreedTimer = false;
                    }
                }
            } else {
                if (kamikaze) {
                    if (destructiveWill >= DESTRUCTIVE_WILL_DRAIN) {
                        if (!animal.hasEffect(BMPotions.SACRIFICIAL_LAMB)) {
                            animal.addEffect(new MobEffectInstance(BMPotions.SACRIFICIAL_LAMB, 1200));
                            destructiveDrain += DESTRUCTIVE_WILL_DRAIN;
                            destructiveWill -= DESTRUCTIVE_WILL_DRAIN;
                            performedEffect = true;
                        }
                    } else {
                        kamikaze = false;
                    }
                }

                if (breedAnimals) {
                    if (steadfastWill >= STEADFAST_WILL_DRAIN) {
                        if (!animal.isInLove()) {
                            for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                                ItemStack foodStack = itemHandler.getStackInSlot(slot);
                                if (!foodStack.isEmpty() && animal.isFood(foodStack)) {
                                    animal.setInLove(null);
                                    itemHandler.extractItem(slot, 1, false);
                                    steadfastDrain += STEADFAST_WILL_DRAIN;
                                    steadfastWill -= STEADFAST_WILL_DRAIN;
                                    performedEffect = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        breedAnimals = false;
                    }
                }
            }

            if (totalGrowths >= maxGrowths) {
                break;
            }
        }

        if (performedEffect && consumeRawWill) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.DEFAULT, RAW_WILL_DRAIN);
        }

        if (vengefulDrain > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.VENGEFUL, vengefulDrain);
        }

        if (steadfastDrain > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.STEADFAST, steadfastDrain);
        }

        if (destructiveDrain > 0) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.DESTRUCTIVE, destructiveDrain);
        }

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(totalGrowths * getRefreshCost()));
    }

    @Override
    public int getRefreshCost() {
        return 2;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addParallelRunes(components, 2, 0, EnumRuneType.DUSK);
        addParallelRunes(components, 1, 0, EnumRuneType.WATER);
        addRune(components, 1, 0, 2, EnumRuneType.EARTH);
        addRune(components, 1, 0, -2, EnumRuneType.EARTH);
        addRune(components, -1, 0, 2, EnumRuneType.EARTH);
        addRune(components, -1, 0, -2, EnumRuneType.EARTH);
        addRune(components, 2, 0, 1, EnumRuneType.AIR);
        addRune(components, 2, 0, -1, EnumRuneType.AIR);
        addRune(components, -2, 0, 1, EnumRuneType.AIR);
        addRune(components, -2, 0, -1, EnumRuneType.AIR);
    }

    @Override
    public Ritual getNewCopy() {
        return new AnimalGrowthRitual();
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

    public int getBreedingDecreaseForWill(double vengefulWill) {
        return (int) (10 + vengefulWill / 5);
    }

    public int getRefreshTimeForRawWill(double rawWill) {
        if (rawWill >= RAW_WILL_DRAIN) {
            return (int) Math.max(DEFAULT_REFRESH_TIME - rawWill / 10, 1);
        }

        return DEFAULT_REFRESH_TIME;
    }

    @Override
    public int getRefreshTime() {
        return refreshTime;
    }
}
