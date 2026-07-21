package wayoftime.bloodmagic.common.item;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Simplified take on the Anointment system: instead of the original's data-driven registry of a
 * dozen-plus stackable weapon coatings, each anointment here is its own item that writes a fixed
 * number of "uses" into the target weapon's own dedicated data component - applied by using the
 * anointment while the target weapon is in the other hand. Only one anointment of a given kind can
 * be active on an item at a time (a second application just refills the uses).
 */
public class AnointmentItem extends Item {
    private final DataComponentType<Integer> usesComponent;
    private final int uses;

    public AnointmentItem(DataComponentType<Integer> usesComponent, int uses) {
        super(new Properties());
        this.usesComponent = usesComponent;
        this.uses = uses;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack self = player.getItemInHand(usedHand);
        InteractionHand otherHand = usedHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack target = player.getItemInHand(otherHand);

        if (target.isEmpty() || target == self) {
            return InteractionResultHolder.pass(self);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(self, true);
        }

        target.set(usesComponent, uses);
        self.shrink(1);
        return InteractionResultHolder.sidedSuccess(self, false);
    }
}
