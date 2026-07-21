package wayoftime.bloodmagic.compat.curios;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.item.BMItems;

/**
 * All Curios-referencing code lives in this class specifically so that it is only ever
 * classloaded when Curios is confirmed present (callers must guard with
 * {@code ModList.get().isLoaded("curios")} before touching anything here).
 */
public class CuriosCompat {

    private static final String LIVING_ARMOUR_SOCKET = "bloodmagic:living_armour_socket";

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(CuriosCapability.ITEM, (stack, ctx) -> new ICurio() {
            @Override
            public ItemStack getStack() {
                return stack;
            }
        }, BMItems.SIGIL.get());
    }

    /**
     * Grants bonus instances of the "living_armour_socket" curio slot based on the player's
     * invested level in the Curios Socket living armour upgrade.
     */
    public static ItemAttributeModifiers withBonusSlots(ItemAttributeModifiers current, int level) {
        if (level <= 0) {
            return current;
        }
        ResourceLocation id = BloodMagic.rl("living_armour_socket_bonus");
        return CuriosApi.withSlotModifier(current, LIVING_ARMOUR_SOCKET, id, level,
                AttributeModifier.Operation.ADD_VALUE, EquipmentSlotGroup.CHEST);
    }
}
