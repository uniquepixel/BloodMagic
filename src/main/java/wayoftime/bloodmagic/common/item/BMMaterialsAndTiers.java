package wayoftime.bloodmagic.common.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import wayoftime.bloodmagic.BloodMagic;

import java.util.List;
import java.util.Map;

public class BMMaterialsAndTiers {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(BuiltInRegistries.ARMOR_MATERIAL, BloodMagic.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> LIVING_ARMOR_MATERIAL = ARMOR_MATERIALS.register("living", () -> new ArmorMaterial(
            ArmorMaterials.IRON.value().defense(), ArmorMaterials.IRON.value().enchantmentValue(),
            ArmorMaterials.IRON.value().equipSound(), () -> Ingredient.of(BMItems.RAW_WILL.get()),
            List.of(new ArmorMaterial.Layer(bm("living"))), 0, 0
    ));

    /**
     * Port of 1.12's {@code ItemSentientArmour} base stats (that branch is the last one with a real
     * Java implementation - see {@code SentientArmorItem}'s class javadoc for the full history).
     * 1.12 bypassed vanilla armor entirely via Forge's since-removed {@code ISpecialArmor}, tuned to
     * land around Iron-equivalent by default and Diamond-and-beyond with a full activated set; this
     * port instead gives the material itself a flat Diamond-equivalent baseline (defense values and
     * toughness identical to {@link ArmorMaterials#DIAMOND}) and layers the Will-scaled bonus on top
     * as cached attribute modifiers/a post-armor damage event - see {@code SentientArmorItem} and
     * {@code SentientArmorEventHandler}. The single {@link ArmorMaterial.Layer} here only supplies
     * the DEFAULT-Will-type texture; {@code SentientArmorItem#getArmorTexture} swaps in the other
     * four Will-type variants per-stack.
     */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SENTIENT_ARMOR_MATERIAL = ARMOR_MATERIALS.register("sentient", () -> new ArmorMaterial(
            Map.of(
                    ArmorItem.Type.BOOTS, 3,
                    ArmorItem.Type.LEGGINGS, 6,
                    ArmorItem.Type.CHESTPLATE, 8,
                    ArmorItem.Type.HELMET, 3
            ),
            ArmorMaterials.DIAMOND.value().enchantmentValue(),
            ArmorMaterials.DIAMOND.value().equipSound(),
            () -> Ingredient.of(BMItems.RAW_WILL.get()),
            List.of(new ArmorMaterial.Layer(bm("sentientarmour"))),
            ArmorMaterials.DIAMOND.value().toughness(), 0
    ));

    private static ResourceLocation bm(String path) {
        return ResourceLocation.fromNamespaceAndPath(BloodMagic.MODID, path);
    }

    public static void register(IEventBus modBus) {
        ARMOR_MATERIALS.register(modBus);
    }
}
