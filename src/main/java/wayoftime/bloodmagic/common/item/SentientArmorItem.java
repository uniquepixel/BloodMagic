package wayoftime.bloodmagic.common.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.util.Unit;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.will.WillHelper;

import java.util.List;

/**
 * Full-fidelity(-in-spirit) port of the Sentient Armour set. 1.20.1 never actually implemented
 * this - the java class only ever existed on 1.12 ({@code WayofTime.bloodmagic.item.armour.
 * ItemSentientArmour}); every branch from 1.14.4 onward (1.16.3, 1.18.2, 1.20.1) carried forward
 * only the leftover texture/model assets with no Java behind them at all (confirmed via
 * {@code git log --diff-filter=A} across the whole repo history - the class was last touched in
 * 1.12's history under {@code item/armour/ItemSentientArmour.java}).
 * <p>
 * 1.12's version worked by completely bypassing vanilla's armor system: right-clicking the
 * Sentient Armour Gem (see {@link SentientArmorGemItem}) would swap out whatever armor the player
 * was wearing for a wrapped "Sentient Armour" stack (storing the original inside NBT to revert to
 * later), and all damage reduction ran through Forge's since-removed {@code ISpecialArmor} hook -
 * a flat per-piece damage-absorption fraction, boosted (checked via an all-four-pieces-present
 * check on the chestplate) by the wearer's current Will type/pool, with the boost's upkeep cost
 * continually draining that Will pool per hit and the whole set reverting back to the player's
 * original gear if it ran dry.
 * <p>
 * Neither of those two mechanisms exist anymore: {@code ISpecialArmor} was removed when armor moved
 * to the generic attribute system pre-1.9, and wrap/revert-on-empty-Will was always a workaround
 * for not being able to make the item directly craftable. This port instead:
 * <ul>
 * <li>makes the four pieces directly Soul Forge-craftable equippable armor (see the
 * {@code sentienthelmet}/{@code sentientplate}/{@code sentientleggings}/{@code sentientboots}
 * recipes) with a flat Diamond-equivalent baseline (see {@link BMMaterialsAndTiers#SENTIENT_ARMOR_MATERIAL}),
 * <li>reapplies 1.12's chest-only Will-typed attribute bonuses (Steadfast knockback resistance /
 * Vengeful movement speed / Destructive damage+attack speed) as cached
 * {@code DataComponents.ATTRIBUTE_MODIFIERS}, recalculated the same way the Sentient tools cache
 * their damage/speed (see {@link SentientToolHelper}), and
 * <li>reapplies the old ISpecialArmor extra-protection-fraction (still full-set-and-Will-gated, see
 * {@link wayoftime.bloodmagic.common.event.SentientArmorEventHandler}) as a post-armor damage
 * reduction, draining Will per hit the same way instead of reverting the whole set.
 * </ul>
 * The Sentient Armour Gem's role changes to match: rather than swapping items in and out, it's now
 * a carried (not worn) toggle that gates whether the worn Sentient Armour's Will-scaled bonuses
 * (chest attributes + the full-set extra protection) are active at all - equipping the pieces alone
 * still gets you their flat Diamond-equivalent baseline, matching the spirit of "the gem activates
 * the armour's true potential" from the original tooltip flavour.
 */
public class SentientArmorItem extends ArmorItem {
    private static final float BASE_TOUGHNESS = 2.0F;

    public SentientArmorItem(Type type) {
        super(BMMaterialsAndTiers.SENTIENT_ARMOR_MATERIAL, type, new Item.Properties()
                .durability(type.getDurability(31))
                .component(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.DEFAULT)
                .component(BMDataComponents.DEMON_WILL_AMOUNT, 0D)
                .component(DataComponents.ATTRIBUTE_MODIFIERS, attributesFor(type, EnumWillType.DEFAULT, -1)));
    }

    // Base defense is hardcoded here (matching what's registered on SENTIENT_ARMOR_MATERIAL) rather
    // than read off the material's DeferredHolder - this constructor runs while the item registry
    // event is still populating entries, and the armor material registry may not have finished
    // binding yet (see the "bootstrap eagerly resolving an unbound DeferredHolder" trap called out
    // for this whole feature).
    private static int baseDefense(Type type) {
        return switch (type) {
            case HELMET, BOOTS -> 3;
            case LEGGINGS -> 6;
            case CHESTPLATE -> 8;
            default -> 0;
        };
    }

    private static ItemAttributeModifiers attributesFor(Type type, EnumWillType willType, int level) {
        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(type.getSlot());
        ResourceLocation id = BloodMagic.rl("sentient_armor." + type.getSerializedName());

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(id, baseDefense(type), AttributeModifier.Operation.ADD_VALUE), group)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, BASE_TOUGHNESS, AttributeModifier.Operation.ADD_VALUE), group);

        if (type == Type.CHESTPLATE && level >= 0) {
            switch (willType) {
                case STEADFAST -> builder.add(Attributes.KNOCKBACK_RESISTANCE,
                        new AttributeModifier(id, SentientArmorHelper.KNOCKBACK_RESISTANCE_BONUS[level], AttributeModifier.Operation.ADD_VALUE), group);
                case VENGEFUL -> builder.add(Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(id, SentientArmorHelper.SPEED_BOOST[level], AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), group);
                case DESTRUCTIVE -> {
                    builder.add(Attributes.ATTACK_DAMAGE,
                            new AttributeModifier(id, SentientArmorHelper.DAMAGE_BOOST[level], AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), group);
                    builder.add(Attributes.ATTACK_SPEED,
                            new AttributeModifier(id, SentientArmorHelper.ATTACK_SPEED_PENALTY[level], AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), group);
                }
                default -> {
                }
            }
        }

        return builder.build();
    }

    private void recalculate(ItemStack stack, Player player) {
        EnumWillType type = WillHelper.getLargestWillType(player);
        double will = WillHelper.getTotalWill(type, player);
        boolean gemActive = SentientArmorGemItem.hasActiveGem(player);
        if (SentientToolHelper.currentType(stack) == type && SentientToolHelper.currentWill(stack) == will
                && stack.has(BMDataComponents.SENTIENT_ARMOUR_GEM_ACTIVE) == gemActive) {
            return;
        }
        stack.set(BMDataComponents.DEMON_WILL_TYPE, type);
        stack.set(BMDataComponents.DEMON_WILL_AMOUNT, will);
        // Cached on the armour stack itself (not just read live off the gem) so appendHoverText and
        // getArmorTexture - neither of which have a Player to consult - can agree with what
        // attributesFor actually granted, rather than recomputing gem state a second, possibly
        // stale, way.
        if (gemActive) {
            stack.set(BMDataComponents.SENTIENT_ARMOUR_GEM_ACTIVE, Unit.INSTANCE);
        } else {
            stack.remove(BMDataComponents.SENTIENT_ARMOUR_GEM_ACTIVE);
        }
        int level = gemActive ? SentientArmorHelper.getLevel(will) : -1;
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attributesFor(getType(), type, level));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide || !(entity instanceof Player player) || player.getItemBySlot(getType().getSlot()) != stack) {
            return;
        }

        recalculate(stack, player);

        // Corrosive chestplate: passive immunity to Poison/Wither while worn (1.12's onArmorTick,
        // chest-only). Not gated behind the gem/Will-level - 1.12 applied this unconditionally
        // whenever the chest was wearing Corrosive Sentient Armour, active-set-bonus or not.
        if (getType() == Type.CHESTPLATE && SentientToolHelper.currentType(stack) == EnumWillType.CORROSIVE) {
            player.removeEffect(MobEffects.POISON);
            player.removeEffect(MobEffects.WITHER);
        }
    }

    /**
     * Per-Will-type equipped-body-layer texture swap. Safe to implement directly here (rather than
     * under {@code client/}) - {@code IItemExtension#getArmorTexture} only deals in common types
     * (ItemStack/Entity/EquipmentSlot/ArmorMaterial.Layer/ResourceLocation) and is only ever
     * *invoked* from the client-side {@code HumanoidArmorLayer}, so overriding it here never touches
     * {@code Minecraft}/{@code LocalPlayer}/any {@code net.minecraft.client} type - unlike the
     * {@code Minecraft.getInstance().player} mistake this session already hit and fixed elsewhere,
     * this hook never needs a live client reference at all; it reads the same cached
     * {@code DEMON_WILL_TYPE} component the tools' tooltips do.
     */
    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        // Gated on the cached gem-active marker, not just Will type - an un-activated piece (no
        // active Sentient Armour Gem carried) shows the plain DEFAULT texture even if the wearer is
        // sitting on a pile of typed Will, matching "the gem unlocks the armour's true potential".
        if (!stack.has(BMDataComponents.SENTIENT_ARMOUR_GEM_ACTIVE)) {
            return null;
        }
        EnumWillType type = SentientToolHelper.currentType(stack);
        if (type == EnumWillType.DEFAULT) {
            return null;
        }
        return BloodMagic.rl("textures/models/armor/sentientarmour_" + type.getSerializedName() + "_layer_" + (innerModel ? 2 : 1) + ".png");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        EnumWillType type = SentientToolHelper.currentType(stack);
        double will = SentientToolHelper.currentWill(stack);
        boolean gemActive = stack.has(BMDataComponents.SENTIENT_ARMOUR_GEM_ACTIVE);
        // Mirrors recalculate()'s gating so the tooltip can never claim a bonus that isn't actually
        // reflected in the stack's cached ATTRIBUTE_MODIFIERS.
        int level = gemActive ? SentientArmorHelper.currentLevel(stack) : -1;
        boolean chest = getType() == Type.CHESTPLATE;
        SentientTooltipHelper.appendArmorTooltip(tooltip, "tooltip.bloodmagic.sentient_" + pieceName() + ".desc", type, level, will, chest, gemActive);
        super.appendHoverText(stack, context, tooltip, flag);
    }

    /**
     * Matches this branch's item id/asset naming ("sentient_plate", not "sentient_chestplate") -
     * {@link Type#getSerializedName()} returns vanilla's own "chestplate", which only agrees with
     * ours for the other three pieces.
     */
    private String pieceName() {
        return getType() == Type.CHESTPLATE ? "plate" : getType().getSerializedName();
    }
}
