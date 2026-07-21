package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.entity.EntityMeteor;
import wayoftime.bloodmagic.common.meteor.MeteorDefinition;
import wayoftime.bloodmagic.common.meteor.MeteorDefinitions;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;

import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of the Meteor ({@code RitualMeteor}): scans its item range for
 * an item entity whose stack matches a known {@link MeteorDefinition}, syphons that definition's LP
 * cost, consumes one of the item, then throws a real {@link EntityMeteor} projectile high into the sky
 * above the stone carrying that one item - the entity falls back down under gravity and detonates the
 * definition's layered sphere of blocks into the world wherever it lands, exactly like the original.
 * <p>
 * The only simplification versus 1.20.1 is in what backs {@link MeteorDefinitions} (a fixed Java list
 * instead of a datapack recipe registry) - see that class's doc comment for why. The trigger scan,
 * LP cost, projectile toss, and full rune pattern are unchanged.
 */
public class MeteorRitual extends Ritual {
    public static final String CHECK_RANGE = "itemRange";

    public MeteorRitual() {
        super(RitualRegistry.rl("meteor"), 0, 250000, "ritual.bloodmagic.meteor");
        addBlockRange(CHECK_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1, 1, 1));
        setMaximumVolumeAndDistanceOfRange(CHECK_RANGE, 27, 10, 10);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        if (level.isClientSide) {
            return;
        }

        BlockPos pos = masterRitualStone.getMasterBlockPos();
        AreaDescriptor itemRange = masterRitualStone.getBlockRange(CHECK_RANGE);
        List<ItemEntity> itemList = level.getEntitiesOfClass(ItemEntity.class, itemRange.getAABB(pos));

        for (ItemEntity entityItem : itemList) {
            if (!entityItem.isAlive()) {
                continue;
            }

            ItemStack stack = entityItem.getItem();
            MeteorDefinition definition = MeteorDefinitions.pickFor(stack);
            if (definition == null) {
                continue;
            }

            int syphonAmount = definition.getSyphon();
            int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();

            if (currentEssence < syphonAmount) {
                return;
            }

            if (syphonAmount > 0) {
                masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(syphonAmount));
            }

            EntityMeteor meteor = new EntityMeteor(level, pos.getX() + 0.5, level.getMaxBuildHeight() + 10, pos.getZ() + 0.5);
            meteor.setDeltaMovement(0, -0.1, 0);
            meteor.setContainedStack(stack.split(1));
            level.addFreshEntity(meteor);

            if (stack.isEmpty()) {
                entityItem.discard();
            }

            return;
        }
    }

    @Override
    public int getRefreshTime() {
        return 20;
    }

    @Override
    public int getRefreshCost() {
        return 0;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 2, 0, 0, EnumRuneType.FIRE);
        addRune(components, -2, 0, 0, EnumRuneType.FIRE);
        addRune(components, 0, 0, 2, EnumRuneType.FIRE);
        addRune(components, 0, 0, -2, EnumRuneType.FIRE);
        addRune(components, 3, 0, 1, EnumRuneType.AIR);
        addRune(components, 3, 0, -1, EnumRuneType.AIR);
        addRune(components, -3, 0, 1, EnumRuneType.AIR);
        addRune(components, -3, 0, -1, EnumRuneType.AIR);
        addRune(components, 1, 0, 3, EnumRuneType.AIR);
        addRune(components, -1, 0, 3, EnumRuneType.AIR);
        addRune(components, 1, 0, -3, EnumRuneType.AIR);
        addRune(components, -1, 0, -3, EnumRuneType.AIR);
        addRune(components, 4, 0, 2, EnumRuneType.AIR);
        addRune(components, 4, 0, -2, EnumRuneType.AIR);
        addRune(components, -4, 0, 2, EnumRuneType.AIR);
        addRune(components, -4, 0, -2, EnumRuneType.AIR);
        addRune(components, 2, 0, 4, EnumRuneType.AIR);
        addRune(components, -2, 0, 4, EnumRuneType.AIR);
        addRune(components, 2, 0, -4, EnumRuneType.AIR);
        addRune(components, -2, 0, -4, EnumRuneType.AIR);
        addRune(components, 5, 0, 3, EnumRuneType.DUSK);
        addRune(components, 5, 0, -3, EnumRuneType.DUSK);
        addRune(components, -5, 0, 3, EnumRuneType.DUSK);
        addRune(components, -5, 0, -3, EnumRuneType.DUSK);
        addRune(components, 3, 0, 5, EnumRuneType.DUSK);
        addRune(components, -3, 0, 5, EnumRuneType.DUSK);
        addRune(components, 3, 0, -5, EnumRuneType.DUSK);
        addRune(components, -3, 0, -5, EnumRuneType.DUSK);
        addRune(components, -4, 0, -4, EnumRuneType.DUSK);
        addRune(components, -4, 0, 4, EnumRuneType.DUSK);
        addRune(components, 4, 0, 4, EnumRuneType.DUSK);
        addRune(components, 4, 0, -4, EnumRuneType.DUSK);

        for (int i = 4; i <= 6; i++) {
            addRune(components, i, 0, 0, EnumRuneType.EARTH);
            addRune(components, -i, 0, 0, EnumRuneType.EARTH);
            addRune(components, 0, 0, i, EnumRuneType.EARTH);
            addRune(components, 0, 0, -i, EnumRuneType.EARTH);
        }

        addRune(components, 8, 0, 0, EnumRuneType.EARTH);
        addRune(components, -8, 0, 0, EnumRuneType.EARTH);
        addRune(components, 0, 0, 8, EnumRuneType.EARTH);
        addRune(components, 0, 0, -8, EnumRuneType.EARTH);
        addRune(components, 8, 1, 0, EnumRuneType.EARTH);
        addRune(components, -8, 1, 0, EnumRuneType.EARTH);
        addRune(components, 0, 1, 8, EnumRuneType.EARTH);
        addRune(components, 0, 1, -8, EnumRuneType.EARTH);
        addRune(components, 7, 1, 0, EnumRuneType.EARTH);
        addRune(components, -7, 1, 0, EnumRuneType.EARTH);
        addRune(components, 0, 1, 7, EnumRuneType.EARTH);
        addRune(components, 0, 1, -7, EnumRuneType.EARTH);
        addRune(components, 7, 2, 0, EnumRuneType.FIRE);
        addRune(components, -7, 2, 0, EnumRuneType.FIRE);
        addRune(components, 0, 2, 7, EnumRuneType.FIRE);
        addRune(components, 0, 2, -7, EnumRuneType.FIRE);
        addRune(components, 6, 2, 0, EnumRuneType.FIRE);
        addRune(components, -6, 2, 0, EnumRuneType.FIRE);
        addRune(components, 0, 2, 6, EnumRuneType.FIRE);
        addRune(components, 0, 2, -6, EnumRuneType.FIRE);
        addRune(components, 6, 3, 0, EnumRuneType.WATER);
        addRune(components, -6, 3, 0, EnumRuneType.WATER);
        addRune(components, 0, 3, 6, EnumRuneType.WATER);
        addRune(components, 0, 3, -6, EnumRuneType.WATER);
        addRune(components, 5, 3, 0, EnumRuneType.WATER);
        addRune(components, -5, 3, 0, EnumRuneType.WATER);
        addRune(components, 0, 3, 5, EnumRuneType.WATER);
        addRune(components, 0, 3, -5, EnumRuneType.WATER);
        addRune(components, 5, 4, 0, EnumRuneType.AIR);
        addRune(components, -5, 4, 0, EnumRuneType.AIR);
        addRune(components, 0, 4, 5, EnumRuneType.AIR);
        addRune(components, 0, 4, -5, EnumRuneType.AIR);

        for (int i = -1; i <= 1; i++) {
            addRune(components, i, 4, 4, EnumRuneType.AIR);
            addRune(components, i, 4, -4, EnumRuneType.AIR);
            addRune(components, 4, 4, i, EnumRuneType.AIR);
            addRune(components, -4, 4, i, EnumRuneType.AIR);
        }

        addRune(components, 2, 4, 4, EnumRuneType.WATER);
        addRune(components, 4, 4, 2, EnumRuneType.WATER);
        addRune(components, 2, 4, -4, EnumRuneType.WATER);
        addRune(components, -4, 4, 2, EnumRuneType.WATER);
        addRune(components, -2, 4, 4, EnumRuneType.WATER);
        addRune(components, 4, 4, -2, EnumRuneType.WATER);
        addRune(components, -2, 4, -4, EnumRuneType.WATER);
        addRune(components, -4, 4, -2, EnumRuneType.WATER);
        addRune(components, 2, 4, 3, EnumRuneType.FIRE);
        addRune(components, 3, 4, 2, EnumRuneType.FIRE);
        addRune(components, 3, 4, 3, EnumRuneType.FIRE);
        addRune(components, -2, 4, 3, EnumRuneType.FIRE);
        addRune(components, 3, 4, -2, EnumRuneType.FIRE);
        addRune(components, 3, 4, -3, EnumRuneType.FIRE);
        addRune(components, 2, 4, -3, EnumRuneType.FIRE);
        addRune(components, -3, 4, 2, EnumRuneType.FIRE);
        addRune(components, -3, 4, 3, EnumRuneType.FIRE);
        addRune(components, -2, 4, -3, EnumRuneType.FIRE);
        addRune(components, -3, 4, -2, EnumRuneType.FIRE);
        addRune(components, -3, 4, -3, EnumRuneType.FIRE);
    }

    @Override
    public Ritual getNewCopy() {
        return new MeteorRitual();
    }
}
