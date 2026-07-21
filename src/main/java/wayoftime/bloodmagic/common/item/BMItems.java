package wayoftime.bloodmagic.common.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
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
    // TODO I dont think there ever was a T6 slate? if there was we should add it here as well

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

    public static final DeferredHolder<Item, SacrificialDaggerItem> SACRIFICIAL_DAGGER = ITEMS.register("sacrificial_dagger", SacrificialDaggerItem::new);
    public static final DeferredHolder<Item, ThrowingDaggerItem> THROWING_DAGGER = ITEMS.register("throwing_dagger", ThrowingDaggerItem::new);
    public static final DeferredHolder<Item, SoulSnareItem> SOUL_SNARE = ITEMS.register("soul_snare", SoulSnareItem::new);

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
