package wayoftime.bloodmagic.common.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.item.potion.AlchemyFlaskItem;
import wayoftime.bloodmagic.common.item.potion.AlchemyFlaskLingeringItem;
import wayoftime.bloodmagic.common.item.potion.AlchemyFlaskThrowableItem;

import java.util.function.Supplier;

public class BMItems {
    public static final DeferredRegister<Item> BASIC_ITEMS = DeferredRegister.createItems(BloodMagic.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(BloodMagic.MODID);
    public static final DeferredRegister<Item> WILL_ITEMS = DeferredRegister.createItems(BloodMagic.MODID);
    public static final DeferredRegister<Item> TAB_REQ = DeferredRegister.createItems(BloodMagic.MODID);

    // these go first for creative tab order
    public static final DeferredHolder<Item, ArmorItem> LIVING_HELMET = BASIC_ITEMS.register("living_helmet", makeLivingArmor(ArmorItem.Type.HELMET));
    public static final DeferredHolder<Item, LivingArmorItem> LIVING_PLATE = TAB_REQ.register("living_plate", LivingArmorItem::new);
    public static final DeferredHolder<Item, ArmorItem> LIVING_LEGGINGS = BASIC_ITEMS.register("living_leggings", makeLivingArmor(ArmorItem.Type.LEGGINGS));
    public static final DeferredHolder<Item, ArmorItem> LIVING_BOOTS = BASIC_ITEMS.register("living_boots", makeLivingArmor(ArmorItem.Type.BOOTS));
    public static final DeferredHolder<Item, UpgradeTomeItem> UPGRADE_TOME = TAB_REQ.register("upgrade_tome", UpgradeTomeItem::new);

    public static final DeferredHolder<Item, ScrapItem> UPGRADE_SCRAP = BASIC_ITEMS.register("upgrade_scrap", () -> new ScrapItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, ScrapItem> SYNTHETIC_POINT = BASIC_ITEMS.register("synthetic_point", () -> new ScrapItem(new Item.Properties().component(BMDataComponents.UPGRADE_SCRAP, 1)));

    public static final DeferredHolder<Item, TrainerItem> TRAINING_BRACELET = BASIC_ITEMS.register("training_bracelet", TrainerItem::new);

    public static final DeferredHolder<Item, SigilItem> SIGIL = TAB_REQ.register("sigil", SigilItem::new);
    public static final DeferredHolder<Item, SigilHoldingItem> SIGIL_HOLDING = TAB_REQ.register("sigil_holding", SigilHoldingItem::new);

    public static final DeferredHolder<Item, BloodOrbItem> ORB_WEAK = BASIC_ITEMS.register("blood_orb_weak", BloodOrbItem::new);
    public static final DeferredHolder<Item, BloodOrbItem> ORB_APPRENTICE = BASIC_ITEMS.register("blood_orb_apprentice", BloodOrbItem::new);
    public static final DeferredHolder<Item, BloodOrbItem> ORB_MAGICIAN = BASIC_ITEMS.register("blood_orb_magician", BloodOrbItem::new);
    public static final DeferredHolder<Item, BloodOrbItem> ORB_MASTER = BASIC_ITEMS.register("blood_orb_master", BloodOrbItem::new);
    public static final DeferredHolder<Item, BloodOrbItem> ORB_ARCHMAGE = BASIC_ITEMS.register("blood_orb_archmage", BloodOrbItem::new);
    public static final DeferredHolder<Item, BloodOrbItem> ORB_TRANSCENDENT = BASIC_ITEMS.register("blood_orb_transcendent", BloodOrbItem::new);

    private static Supplier<ArmorItem> makeLivingArmor(ArmorItem.Type type) {
        return () -> new ArmorItem(BMMaterialsAndTiers.LIVING_ARMOR_MATERIAL, type, new Item.Properties().durability(type.getDurability(33)));
    }

    // these are here because I was trying to use the blank slate model for missing SigilType and Im not gonna delete them now that I remembered about the black/purple missing model
    public static final DeferredHolder<Item, Item> SLATE_BLANK = BASIC_ITEMS.register("slate_blank", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SLATE_REINFORCED = BASIC_ITEMS.register("slate_reinforced", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SLATE_IMBUED = BASIC_ITEMS.register("slate_imbued", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SLATE_DEMONIC = BASIC_ITEMS.register("slate_demonic", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SLATE_ETHEREAL = BASIC_ITEMS.register("slate_ethereal", () -> new Item(new Item.Properties()));
    // Confirmed against upstream/1.20.1's BloodMagicItems.java: SLATE/REINFORCED_SLATE/IMBUED_SLATE/
    // DEMONIC_SLATE/ETHEREAL_SLATE are the full list there too - there was never a 6th (Tier 6) slate
    // on 1.20.1, so nothing is missing here.

    // Ore fragments (ARC ore-processing chain: raw material/ore -> fragment -> dust/nugget) and
    // Hellforged Parts (dropped by reverting a tier-2 rune back to its tier-1 form in the ARC),
    // ported from 1.20.1 using the old item ids as-is.
    public static final DeferredHolder<Item, Item> HELLFORGED_PARTS = BASIC_ITEMS.register("hellforgedparts", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> IRON_FRAGMENT = BASIC_ITEMS.register("ironfragment", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> GOLD_FRAGMENT = BASIC_ITEMS.register("goldfragment", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> COPPER_FRAGMENT = BASIC_ITEMS.register("copperfragment", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NETHERITE_SCRAP_FRAGMENT = BASIC_ITEMS.register("fragment_netherite_scrap", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> DEMONITE_FRAGMENT = BASIC_ITEMS.register("demonitefragment", () -> new Item(new Item.Properties()));

    // Ore gravel (ARC ore-processing chain, next tier after fragment: fragment -> gravel via the
    // "resonator" ARC tool tag) and Corrupted Dust (crafted from 9x Corrupted Tiny Dust, itself a
    // chance byproduct of the fragment -> gravel ARC step; feeding Corrupted Dust back into the
    // Alchemy Table fragment -> gravel recipes yields more gravel per fragment), ported from
    // 1.20.1 using the old item ids as-is.
    public static final DeferredHolder<Item, Item> IRON_GRAVEL = BASIC_ITEMS.register("irongravel", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> GOLD_GRAVEL = BASIC_ITEMS.register("goldgravel", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> COPPER_GRAVEL = BASIC_ITEMS.register("coppergravel", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> NETHERITE_SCRAP_GRAVEL = BASIC_ITEMS.register("gravel_netherite_scrap", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> DEMONITE_GRAVEL = BASIC_ITEMS.register("demonitegravel", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> CORRUPTED_DUST = BASIC_ITEMS.register("corrupted_dust", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> CORRUPTED_DUST_TINY = BASIC_ITEMS.register("corrupted_tinydust", () -> new Item(new Item.Properties()));

    // ARC tool items: durability-gated catalysts placed in the Alchemical Reaction Chamber's tool
    // slot. Ported from 1.20.1's ItemARCToolBase(maxDamage, craftingMultiplier[, additionalOutputChance]
    // [, type]) using the old item ids, tiers and stats as-is - ARC_SPEED/ARC_CHANCE default to 1 via
    // ARCTile's getOrDefault when omitted below, and DEMON_WILL_TYPE defaults to EnumWillType.DEFAULT,
    // matching the old constructor's own defaults. See BMItemTagProvider for the tag population that
    // makes these usable, and BMRecipeProvider for how each one is now obtainable.
    public static final DeferredHolder<Item, Item> SANGUINE_REVERTER = BASIC_ITEMS.register("sanguinereverter", () -> new Item(new Item.Properties().stacksTo(1).durability(32).component(BMDataComponents.ARC_SPEED, 2D).component(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.STEADFAST)));

    public static final DeferredHolder<Item, Item> EXPLOSIVE_POWDER = BASIC_ITEMS.register("explosivepowder", () -> new Item(new Item.Properties().stacksTo(1).durability(64).component(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.DESTRUCTIVE)));
    public static final DeferredHolder<Item, Item> PRIMITIVE_EXPLOSIVE_CELL = BASIC_ITEMS.register("primitive_explosive_cell", () -> new Item(new Item.Properties().stacksTo(1).durability(256).component(BMDataComponents.ARC_SPEED, 1.5D).component(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.DESTRUCTIVE)));
    public static final DeferredHolder<Item, Item> HELLFORGED_EXPLOSIVE_CELL = BASIC_ITEMS.register("hellforged_explosive_cell", () -> new Item(new Item.Properties().stacksTo(1).durability(1024).component(BMDataComponents.ARC_SPEED, 2D).component(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.DESTRUCTIVE)));

    public static final DeferredHolder<Item, Item> RESONATOR = BASIC_ITEMS.register("resonator", () -> new Item(new Item.Properties().stacksTo(1).durability(64).component(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.VENGEFUL)));
    public static final DeferredHolder<Item, Item> PRIMITIVE_CRYSTALLINE_RESONATOR = BASIC_ITEMS.register("primitive_crystalline_resonator", () -> new Item(new Item.Properties().stacksTo(1).durability(256).component(BMDataComponents.ARC_SPEED, 1.5D).component(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.VENGEFUL)));
    public static final DeferredHolder<Item, Item> HELLFORGED_RESONATOR = BASIC_ITEMS.register("hellforged_resonator", () -> new Item(new Item.Properties().stacksTo(1).durability(1024).component(BMDataComponents.ARC_SPEED, 2D).component(BMDataComponents.ARC_CHANCE, 2D).component(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.VENGEFUL)));

    public static final DeferredHolder<Item, Item> BASIC_CUTTING_FLUID = BASIC_ITEMS.register("basiccuttingfluid", () -> new Item(new Item.Properties().stacksTo(1).durability(64).component(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.CORROSIVE)));
    public static final DeferredHolder<Item, Item> INTERMEDIATE_CUTTING_FLUID = BASIC_ITEMS.register("intermediatecuttingfluid", () -> new Item(new Item.Properties().stacksTo(1).durability(256).component(BMDataComponents.ARC_SPEED, 1.5D).component(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.CORROSIVE)));
    public static final DeferredHolder<Item, Item> ADVANCED_CUTTING_FLUID = BASIC_ITEMS.register("advancedcuttingfluid", () -> new Item(new Item.Properties().stacksTo(1).durability(1024).component(BMDataComponents.ARC_SPEED, 2D).component(BMDataComponents.ARC_CHANCE, 2D).component(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.CORROSIVE)));

    public static final DeferredHolder<Item, Item> PRIMITIVE_HYDRATION_CELL = BASIC_ITEMS.register("primitive_hydration_cell", () -> new Item(new Item.Properties().stacksTo(1).durability(128).component(BMDataComponents.ARC_SPEED, 1.5D)));

    // Furnace-tier ARC tools go only into BMTags.Items.ARC_SMELTING (see BMItemTagProvider), not
    // ARC_BLASTING/ARC_SMOKING: 1.20.1's TileAlchemicalReactionChamber only ever queried
    // RecipeType.SMELTING for ARC_TOOL_FURNACE items - blasting/smoking are new subdivisions on this
    // branch with no historical item ever wired to them, so smelting-only preserves 1.20.1 behaviour.
    public static final DeferredHolder<Item, Item> PRIMITIVE_FURNACE_CELL = BASIC_ITEMS.register("furnacecell_primitive", () -> new Item(new Item.Properties().stacksTo(1).durability(128).component(BMDataComponents.ARC_SPEED, 3D)));
    // 1.20.1's ItemLavaCrystal was a bindable item (IBindable) that syphoned LP from its bound
    // player's Soul Network to place fire blocks on right-click and to fuel furnaces indefinitely -
    // it never implemented IARCTool, so it never got a speed/chance bonus in the ARC either way, it
    // was just a valid tag member. This branch doesn't have an item-side binding+syphon
    // implementation to port that onto yet (BloodOrbItem/SigilItem only use BMDataComponents.BINDING
    // for ownership display, not for gameplay syphoning), so this is simplified to a plain
    // durability-gated ARC tool like its siblings above; ARC behaviour is unaffected either way.
    public static final DeferredHolder<Item, Item> LAVA_CRYSTAL = BASIC_ITEMS.register("lavacrystal", () -> new Item(new Item.Properties().stacksTo(1).durability(256)));

    public static final DeferredHolder<Item, SacrificialDaggerItem> SACRIFICIAL_DAGGER = ITEMS.register("sacrificial_dagger", SacrificialDaggerItem::new);
    public static final DeferredHolder<Item, ThrowingDaggerItem> THROWING_DAGGER = ITEMS.register("throwing_dagger", ThrowingDaggerItem::new);
    // Syringe variant: faster/weaker dagger that harvests Slate Ampoules from a killing blow - see
    // ThrowingDaggerSyringeItem/ThrowingDaggerSyringeEntity.
    public static final DeferredHolder<Item, ThrowingDaggerSyringeItem> THROWING_DAGGER_SYRINGE = ITEMS.register("throwing_dagger_syringe", ThrowingDaggerSyringeItem::new);
    public static final DeferredHolder<Item, SoulSnareItem> SOUL_SNARE = ITEMS.register("soul_snare", SoulSnareItem::new);
    // Harvested by the Syringe Throwing Dagger; right-click near a Blood Altar to pour 500 LP into
    // it, ported from 1.20.1's ItemBloodProvider("slate", 500).
    public static final DeferredHolder<Item, SlateAmpouleItem> SLATE_AMPOULE = BASIC_ITEMS.register("slate_ampoule", SlateAmpouleItem::new);

    public static final DeferredHolder<Item, RawSoulItem> RAW_WILL = WILL_ITEMS.register("raw_will", RawSoulItem::new);

    public static final DeferredHolder<Item, SoulGemItem> SOUL_GEM_PETTY = WILL_ITEMS.register("soul_gem_petty", SoulGemItem::new);
    public static final DeferredHolder<Item, SoulGemItem> SOUL_GEM_LESSER = WILL_ITEMS.register("soul_gem_lesser", SoulGemItem::new);
    public static final DeferredHolder<Item, SoulGemItem> SOUL_GEM_COMMON = WILL_ITEMS.register("soul_gem_common", SoulGemItem::new);
    public static final DeferredHolder<Item, SoulGemItem> SOUL_GEM_GREATER = WILL_ITEMS.register("soul_gem_greater", SoulGemItem::new);
    public static final DeferredHolder<Item, SoulGemItem> SOUL_GEM_GRAND = WILL_ITEMS.register("soul_gem_grand", SoulGemItem::new);

    // Sentient tools (registered under WILL_ITEMS, not ITEMS, so they pick up the same
    // per-Will-type texture-override/ItemProperties wiring the Soul Gems already get for free -
    // see BMItemModelProvider and ClientModEventHandler#onClientSetup).
    public static final DeferredHolder<Item, SentientSwordItem> SENTIENT_SWORD = WILL_ITEMS.register("sentient_sword", SentientSwordItem::new);
    public static final DeferredHolder<Item, SentientAxeItem> SENTIENT_AXE = WILL_ITEMS.register("sentient_axe", SentientAxeItem::new);
    public static final DeferredHolder<Item, SentientPickaxeItem> SENTIENT_PICKAXE = WILL_ITEMS.register("sentient_pickaxe", SentientPickaxeItem::new);
    public static final DeferredHolder<Item, SentientShovelItem> SENTIENT_SHOVEL = WILL_ITEMS.register("sentient_shovel", SentientShovelItem::new);
    public static final DeferredHolder<Item, SentientScytheItem> SENTIENT_SCYTHE = WILL_ITEMS.register("sentient_scythe", SentientScytheItem::new);
    public static final DeferredHolder<Item, SentientBowItem> SENTIENT_BOW = WILL_ITEMS.register("sentient_bow", SentientBowItem::new);

    // Sentient Armour: 1.20.1 never shipped a Java implementation of this at all (only leftover
    // texture assets survived from 1.12 - see SentientArmorItem's class javadoc for the full
    // history), so this is a from-scratch reconstruction of 1.12's mechanics on top of this
    // branch's modern attribute/data-component tooling. Registered under WILL_ITEMS too, same
    // reason as the tools above - and it gets the gem its per-Will-type icon variants for free too.
    public static final DeferredHolder<Item, SentientArmorItem> SENTIENT_HELMET = WILL_ITEMS.register("sentient_helmet", () -> new SentientArmorItem(ArmorItem.Type.HELMET));
    public static final DeferredHolder<Item, SentientArmorItem> SENTIENT_PLATE = WILL_ITEMS.register("sentient_plate", () -> new SentientArmorItem(ArmorItem.Type.CHESTPLATE));
    public static final DeferredHolder<Item, SentientArmorItem> SENTIENT_LEGGINGS = WILL_ITEMS.register("sentient_leggings", () -> new SentientArmorItem(ArmorItem.Type.LEGGINGS));
    public static final DeferredHolder<Item, SentientArmorItem> SENTIENT_BOOTS = WILL_ITEMS.register("sentient_boots", () -> new SentientArmorItem(ArmorItem.Type.BOOTS));
    public static final DeferredHolder<Item, SentientArmorGemItem> SENTIENT_ARMOUR_GEM = WILL_ITEMS.register("sentient_armour_gem", SentientArmorGemItem::new);

    public static final DeferredHolder<Item, Item> REAGENT_BLOODLIGHT = BASIC_ITEMS.register("reagent_bloodlight", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> REAGENT_HOLDING = BASIC_ITEMS.register("reagent_holding", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> REAGENT_SUPPRESSION = BASIC_ITEMS.register("reagent_suppression", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> REAGENT_TELEPOSITION = BASIC_ITEMS.register("reagent_teleposition", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> REAGENT_FASTMINER = BASIC_ITEMS.register("reagent_fastminer", () -> new Item(new Item.Properties()));
    // Alchemy Table crafting component for the Living-armor Binding arrays - see
    // wayoftime.bloodmagic.common.alchemyarray.AlchemyArrayEffects#BINDING_IDS, which was already
    // wired up and waiting on this item to exist.
    public static final DeferredHolder<Item, Item> REAGENT_BINDING = BASIC_ITEMS.register("reagent_binding", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, TeleposerFocusItem> TELEPOSER_FOCUS = BASIC_ITEMS.register("teleposer_focus", TeleposerFocusItem::new);

    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_MELEE_DAMAGE = BASIC_ITEMS.register("anointment_melee_damage", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_MELEE_USES.get(), 20));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_LOOTING = BASIC_ITEMS.register("anointment_looting", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_LOOTING_USES.get(), 20));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_BOW_POWER = BASIC_ITEMS.register("anointment_bow_power", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_BOW_POWER_USES.get(), 20));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_BOW_VELOCITY = BASIC_ITEMS.register("anointment_bow_velocity", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_BOW_VELOCITY_USES.get(), 20));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_HIDDEN_KNOWLEDGE = BASIC_ITEMS.register("anointment_hidden_knowledge", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_HIDDEN_KNOWLEDGE_USES.get(), 20));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_HOLY_WATER = BASIC_ITEMS.register("anointment_holy_water", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_HOLY_WATER_USES.get(), 20));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_QUICK_DRAW = BASIC_ITEMS.register("anointment_quick_draw", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_QUICK_DRAW_USES.get(), 20));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_SILK_TOUCH = BASIC_ITEMS.register("anointment_silk_touch", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_SILK_TOUCH_USES.get(), 20));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_FORTUNE = BASIC_ITEMS.register("anointment_fortune", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_FORTUNE_USES.get(), 20));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_SMELTING = BASIC_ITEMS.register("anointment_smelting", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_SMELTING_USES.get(), 20));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_VOIDING = BASIC_ITEMS.register("anointment_voiding", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_VOIDING_USES.get(), 20));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_WEAPON_REPAIR = BASIC_ITEMS.register("anointment_weapon_repair", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_WEAPON_REPAIR_USES.get(), 20));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_WILL_POWER = BASIC_ITEMS.register("anointment_will_power", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_WILL_POWER_USES.get(), 20));

    // 1.20.1 sold anointments in three container sizes (base/"_L"/"_XL", 256/1024/4096 max-applications
    // in the original) for every family except Will Power (which never got size variants there
    // either); this branch's simplified AnointmentItem only has a single "uses" number (no separate
    // potency axis - see AnointmentItem's class javadoc), so L/XL are ported as the same 1:4:16 uses
    // ratio applied to each family's base 20-use count, reusing AnointmentItem as-is. 1.20.1's
    // potency-tier variants (_2/_3, same use-count but a stronger per-hit effect, plus the one-off
    // BOW_POWER_ANOINTMENT_STRONG) aren't ported: this branch's AnointmentItem has no potency
    // parameter at all, so that would need a new mechanic, not just "a different use-count argument".
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_MELEE_DAMAGE_L = BASIC_ITEMS.register("anointment_melee_damage_l", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_MELEE_USES.get(), 80));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_MELEE_DAMAGE_XL = BASIC_ITEMS.register("anointment_melee_damage_xl", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_MELEE_USES.get(), 320));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_LOOTING_L = BASIC_ITEMS.register("anointment_looting_l", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_LOOTING_USES.get(), 80));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_LOOTING_XL = BASIC_ITEMS.register("anointment_looting_xl", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_LOOTING_USES.get(), 320));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_BOW_POWER_L = BASIC_ITEMS.register("anointment_bow_power_l", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_BOW_POWER_USES.get(), 80));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_BOW_POWER_XL = BASIC_ITEMS.register("anointment_bow_power_xl", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_BOW_POWER_USES.get(), 320));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_BOW_VELOCITY_L = BASIC_ITEMS.register("anointment_bow_velocity_l", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_BOW_VELOCITY_USES.get(), 80));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_BOW_VELOCITY_XL = BASIC_ITEMS.register("anointment_bow_velocity_xl", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_BOW_VELOCITY_USES.get(), 320));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_HIDDEN_KNOWLEDGE_L = BASIC_ITEMS.register("anointment_hidden_knowledge_l", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_HIDDEN_KNOWLEDGE_USES.get(), 80));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_HIDDEN_KNOWLEDGE_XL = BASIC_ITEMS.register("anointment_hidden_knowledge_xl", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_HIDDEN_KNOWLEDGE_USES.get(), 320));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_HOLY_WATER_L = BASIC_ITEMS.register("anointment_holy_water_l", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_HOLY_WATER_USES.get(), 80));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_HOLY_WATER_XL = BASIC_ITEMS.register("anointment_holy_water_xl", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_HOLY_WATER_USES.get(), 320));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_QUICK_DRAW_L = BASIC_ITEMS.register("anointment_quick_draw_l", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_QUICK_DRAW_USES.get(), 80));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_QUICK_DRAW_XL = BASIC_ITEMS.register("anointment_quick_draw_xl", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_QUICK_DRAW_USES.get(), 320));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_SILK_TOUCH_L = BASIC_ITEMS.register("anointment_silk_touch_l", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_SILK_TOUCH_USES.get(), 80));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_SILK_TOUCH_XL = BASIC_ITEMS.register("anointment_silk_touch_xl", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_SILK_TOUCH_USES.get(), 320));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_FORTUNE_L = BASIC_ITEMS.register("anointment_fortune_l", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_FORTUNE_USES.get(), 80));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_FORTUNE_XL = BASIC_ITEMS.register("anointment_fortune_xl", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_FORTUNE_USES.get(), 320));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_SMELTING_L = BASIC_ITEMS.register("anointment_smelting_l", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_SMELTING_USES.get(), 80));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_SMELTING_XL = BASIC_ITEMS.register("anointment_smelting_xl", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_SMELTING_USES.get(), 320));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_VOIDING_L = BASIC_ITEMS.register("anointment_voiding_l", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_VOIDING_USES.get(), 80));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_VOIDING_XL = BASIC_ITEMS.register("anointment_voiding_xl", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_VOIDING_USES.get(), 320));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_WEAPON_REPAIR_L = BASIC_ITEMS.register("anointment_weapon_repair_l", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_WEAPON_REPAIR_USES.get(), 80));
    public static final DeferredHolder<Item, AnointmentItem> ANOINTMENT_WEAPON_REPAIR_XL = BASIC_ITEMS.register("anointment_weapon_repair_xl", () -> new AnointmentItem(BMDataComponents.ANOINTMENT_WEAPON_REPAIR_USES.get(), 320));

    public static final DeferredHolder<Item, ItemActivationCrystal> ACTIVATION_CRYSTAL_WEAK = BASIC_ITEMS.register("activation_crystal_weak", () -> new ItemActivationCrystal(ItemActivationCrystal.CrystalType.WEAK));
    public static final DeferredHolder<Item, ItemActivationCrystal> ACTIVATION_CRYSTAL_AWAKENED = BASIC_ITEMS.register("activation_crystal_awakened", () -> new ItemActivationCrystal(ItemActivationCrystal.CrystalType.AWAKENED));
    public static final DeferredHolder<Item, ItemActivationCrystal> ACTIVATION_CRYSTAL_CREATIVE = BASIC_ITEMS.register("activation_crystal_creative", () -> new ItemActivationCrystal(ItemActivationCrystal.CrystalType.CREATIVE));

    public static final DeferredHolder<Item, RitualDivinerItem> RITUAL_DIVINER = BASIC_ITEMS.register("ritual_diviner", () -> new RitualDivinerItem());
    public static final DeferredHolder<Item, RitualDivinerItem> RITUAL_DIVINER_DUSK = BASIC_ITEMS.register("ritual_diviner_dusk", () -> new RitualDivinerItem(1));
    public static final DeferredHolder<Item, RitualDivinerItem> RITUAL_DIVINER_DAWN = BASIC_ITEMS.register("ritual_diviner_dawn", () -> new RitualDivinerItem(2));
    public static final DeferredHolder<Item, DemonWillGaugeItem> DEMON_WILL_GAUGE = BASIC_ITEMS.register("demon_will_gauge", DemonWillGaugeItem::new);
    public static final DeferredHolder<Item, ExperienceBookItem> EXPERIENCE_BOOK = BASIC_ITEMS.register("experience_book", ExperienceBookItem::new);

    // Alchemical Potion Flask system, ported from 1.20.1's ItemAlchemyFlask/Throwable/Lingering.
    public static final DeferredHolder<Item, AlchemyFlaskItem> ALCHEMY_FLASK = ITEMS.register("alchemy_flask", AlchemyFlaskItem::new);
    public static final DeferredHolder<Item, AlchemyFlaskThrowableItem> ALCHEMY_FLASK_THROWABLE = ITEMS.register("alchemy_flask_throwable", AlchemyFlaskThrowableItem::new);
    public static final DeferredHolder<Item, AlchemyFlaskLingeringItem> ALCHEMY_FLASK_LINGERING = ITEMS.register("alchemy_flask_lingering", AlchemyFlaskLingeringItem::new);

    // Alchemy Table catalyst/filling-agent reagents consumed by the flask recipes above - plain
    // items with no logic of their own on 1.20.1 too (ItemBase), ported as-is.
    public static final DeferredHolder<Item, Item> SIMPLE_CATALYST = BASIC_ITEMS.register("simplecatalyst", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> MUNDANE_POWER_CATALYST = BASIC_ITEMS.register("mundanepowercatalyst", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> MUNDANE_LENGTHENING_CATALYST = BASIC_ITEMS.register("mundanelengtheningcatalyst", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> COMBINATIONAL_CATALYST = BASIC_ITEMS.register("combinationalcatalyst", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> WEAK_FILLING_AGENT = BASIC_ITEMS.register("weakfillingagent", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> CYCLING_CATALYST = BASIC_ITEMS.register("cyclingcatalyst", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> AVERAGE_POWER_CATALYST = BASIC_ITEMS.register("averagepowercatalyst", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> AVERAGE_LENGTHENING_CATALYST = BASIC_ITEMS.register("averagelengtheningcatalyst", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> AVERAGE_FILLING_AGENT = BASIC_ITEMS.register("standardfillingagent", () -> new Item(new Item.Properties()));

    public static void register(IEventBus modBus) {
        BASIC_ITEMS.register(modBus);
        ITEMS.register(modBus);
        WILL_ITEMS.register(modBus);
        TAB_REQ.register(modBus);
    }
}
