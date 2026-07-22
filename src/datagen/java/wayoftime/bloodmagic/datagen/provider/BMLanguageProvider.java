package wayoftime.bloodmagic.datagen.provider;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.fluid.BMFluids;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.datagen.content.LivingUpgrades;
import wayoftime.bloodmagic.datagen.content.SigilData;
import wayoftime.bloodmagic.util.blockitem.BlockWithItemHolder;

public class BMLanguageProvider extends LanguageProvider {

    public BMLanguageProvider(PackOutput output) {
        super(output, BloodMagic.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        //Fluids - Life Essence
        add(BMFluids.LIFE_ESSENCE_TYPE.get().getDescriptionId(), "Life Essence");
        add(BMFluids.LIFE_ESSENCE_BUCKET.get(), "Bucket of Life");
        add(BMFluids.LIFE_ESSENCE_BLOCK.get(), "Life Essence");

        //Fluids - Liquid Doubt
        add(BMFluids.DOUBT_TYPE.get().getDescriptionId(), "Liquid Doubt");
        add(BMFluids.DOUBT_BUCKET.get(), "Doubt Bucket");
        add(BMFluids.DOUBT_BLOCK.get(), "Liquid Doubt");

        //Orbs
        add(BMItems.ORB_WEAK.get(), "Weak Blood Orb");
        add(BMItems.ORB_APPRENTICE.get(), "Apprentice Blood Orb");
        add(BMItems.ORB_MAGICIAN.get(), "Magician Blood Orb");
        add(BMItems.ORB_MASTER.get(), "Master Blood Orb");
        add(BMItems.ORB_ARCHMAGE.get(), "Archmage Blood Orb");
        add(BMItems.ORB_TRANSCENDENT.get(), "Transcendent Blood Orb");

        //Binding Info
        addTooltip("current_owner", "Current Owner: %s");
        addTooltip("no_owner", "Not bound yet");

        add(BMBlocks.BLOOD_ALTAR, "Blood Altar");
        add(BMItems.SACRIFICIAL_DAGGER.get(), "Sacrificial Dagger");
        add(BMItems.THROWING_DAGGER.get(), "Throwing Dagger");
        add("tooltip.bloodmagic.throwing_dagger.desc", "Throw for quick, if unremarkable, damage.");
        add(BMItems.THROWING_DAGGER_SYRINGE.get(), "Syringe Throwing Dagger");
        add(BMItems.SLATE_AMPOULE.get(), "Slate Ampoule");
        add("tooltip.bloodmagic.blood_provider.slate.desc", "A simple ampoule containing 500LP.");
        add(BMItems.SOUL_SNARE.get(), "Soul Snare");
        addTooltip("soul_snare.desc", "Throw at a monster and then kill them to obtain their demonic will.");

        //Sentient tools
        add(BMItems.SENTIENT_SWORD.get(), "Sentient Sword");
        add(BMItems.SENTIENT_AXE.get(), "Sentient Axe");
        add(BMItems.SENTIENT_PICKAXE.get(), "Sentient Pickaxe");
        add(BMItems.SENTIENT_SHOVEL.get(), "Sentient Shovel");
        add(BMItems.SENTIENT_SCYTHE.get(), "Sentient Scythe");
        add(BMItems.SENTIENT_BOW.get(), "Sentient Bow");
        addTooltip("sentient_sword.desc", "Uses demon will to unleash its full potential.");
        addTooltip("sentient_axe.desc", "Uses demon will to unleash its full potential.");
        addTooltip("sentient_pickaxe.desc", "Uses demon will to unleash its full potential.");
        addTooltip("sentient_shovel.desc", "Uses demon will to unleash its full potential.");
        addTooltip("sentient_scythe.desc", "Uses demon will to unleash its full potential.");
        addTooltip("sentient_bow.desc", "Uses demon will to unleash its full potential.");
        addTooltip("extra_info", "-Hold shift for more info-");
        add("tooltip.bloodmagic.sentient.attuned", "Attuned: %s");
        add("tooltip.bloodmagic.sentient.level_pool", "Level %s (%s Will)");
        add("tooltip.bloodmagic.sentient.inactive", "Inactive (gather more Will)");
        add("tooltip.bloodmagic.sentient.bonus_damage", "%s bonus damage");
        add("tooltip.bloodmagic.sentient.rider.corrosive", "On hit: Wither %ss (Lv %s)");
        add("tooltip.bloodmagic.sentient.rider.steadfast", "On kill: Absorption %ss");
        add("tooltip.bloodmagic.sentient.rider.vengeful", "Movement Speed: %s");
        add("tooltip.bloodmagic.sentient.rider.dig_speed", "Mining Speed: %s");

        //Sentient Armour - see SentientArmorItem's class javadoc: 1.20.1 never shipped a Java
        //implementation of this at all, so it's reconstructed here from 1.12's mechanics.
        add(BMItems.SENTIENT_HELMET.get(), "Sentient Helmet");
        add(BMItems.SENTIENT_PLATE.get(), "Sentient Plate");
        add(BMItems.SENTIENT_LEGGINGS.get(), "Sentient Leggings");
        add(BMItems.SENTIENT_BOOTS.get(), "Sentient Boots");
        add(BMItems.SENTIENT_ARMOUR_GEM.get(), "Sentient Armour Gem");
        addTooltip("sentient_helmet.desc", "Uses demon will to unleash its full potential.");
        addTooltip("sentient_plate.desc", "Uses demon will to unleash its full potential.");
        addTooltip("sentient_leggings.desc", "Uses demon will to unleash its full potential.");
        addTooltip("sentient_boots.desc", "Uses demon will to unleash its full potential.");
        addTooltip("sentient_armour_gem.desc", "Carry this while wearing a full Sentient Armour set and activate it to unleash the set's true potential.");
        add("tooltip.bloodmagic.sentient_armour_gem.active", "Activated");
        add("tooltip.bloodmagic.sentient_armour_gem.inactive", "Deactivated (right click to activate)");
        add("tooltip.bloodmagic.sentient_armor.knockback", "Knockback Resistance: %s");
        add("tooltip.bloodmagic.sentient_armor.speed", "Movement Speed: %s");
        add("tooltip.bloodmagic.sentient_armor.damage", "Attack Damage: %s");
        add("tooltip.bloodmagic.sentient_armor.attack_speed", "Attack Speed: %s");
        add("tooltip.bloodmagic.sentient_armor.corrosive_reflect", "On hit: attacker is Withered %ss");
        add("tooltip.bloodmagic.sentient_armor.corrosive_cleanse", "Immune to Poison and Wither");
        add("tooltip.bloodmagic.sentient_armor.full_set_protection", "Full Set Bonus: %s extra damage reduction");
        add("tooltip.bloodmagic.sentient_armor.gem_required", "Inactive (carry an activated Sentient Armour Gem)");

        //Runes - Blank
        add(BMBlocks.RUNE_BLANK, "Blank Rune");

        //Runes - Basic
        add(BMBlocks.RUNE_SACRIFICE, "Rune of Sacrifice");
        add(BMBlocks.RUNE_SELF_SACRIFICE, "Rune of Self Sacrifice");
        add(BMBlocks.RUNE_SPEED, "Speed Rune");
        add(BMBlocks.RUNE_ACCELERATION, "Acceleration Rune");
        add(BMBlocks.RUNE_DISLOCATION, "Displacement Rune");
        add(BMBlocks.RUNE_CAPACITY, "Capacity Rune");
        add(BMBlocks.RUNE_CAPACITY_AUGMENTED, "Augmented Capacity Rune");
        add(BMBlocks.RUNE_CHARGING, "Charging Rune");
        add(BMBlocks.RUNE_ORB, "Rune of the Orb");
        add(BMBlocks.RUNE_EFFICIENCY, "Rune of Efficiency");

        //Runes - Reinforced
        add(BMBlocks.RUNE_2_SACRIFICE, "Reinforced Rune of Sacrifice");
        add(BMBlocks.RUNE_2_SELF_SACRIFICE, "Reinforced Rune of Self Sacrifice");
        add(BMBlocks.RUNE_2_SPEED, "Reinforced Speed Rune");
        add(BMBlocks.RUNE_2_ACCELERATION, "Reinforced Acceleration Rune");
        add(BMBlocks.RUNE_2_DISLOCATION, "Reinforced Displacement Rune");
        add(BMBlocks.RUNE_2_CAPACITY, "Reinforced Capacity Rune");
        add(BMBlocks.RUNE_2_CAPACITY_AUGMENTED, "Reinforced Augmented Capacity Rune");
        add(BMBlocks.RUNE_2_CHARGING, "Reinforced Charging Rune");
        add(BMBlocks.RUNE_2_ORB, "Reinforced Rune of the Orb");
        add(BMBlocks.RUNE_2_EFFICIENCY, "Reinforced Rune of Efficiency");

        add(BMBlocks.BLOODSTONE, "Polished Bloodstone");
        add(BMBlocks.BLOODSTONE_BRICK, "Bloodstone Brick");

        add(BMBlocks.HELLFORGED_BLOCK, "Hellforged Block");

        //Ore fragments + Hellforged Parts
        add(BMItems.HELLFORGED_PARTS.get(), "Intricate Hellforged Parts");
        addTooltip("hellforgedparts", "These parts are currently beyond your crafting capabilities...");
        add(BMItems.IRON_FRAGMENT.get(), "Iron Fragment");
        add(BMItems.GOLD_FRAGMENT.get(), "Gold Fragment");
        add(BMItems.COPPER_FRAGMENT.get(), "Copper Fragment");
        add(BMItems.NETHERITE_SCRAP_FRAGMENT.get(), "Ancient Debris Fragment");
        add(BMItems.DEMONITE_FRAGMENT.get(), "Demonite Fragment");

        //Ore gravel + Corrupted Dust
        add(BMItems.IRON_GRAVEL.get(), "Iron Gravel");
        add(BMItems.GOLD_GRAVEL.get(), "Gold Gravel");
        add(BMItems.COPPER_GRAVEL.get(), "Copper Gravel");
        add(BMItems.NETHERITE_SCRAP_GRAVEL.get(), "Ancient Debris Gravel");
        add(BMItems.DEMONITE_GRAVEL.get(), "Demonite Gravel");
        add(BMItems.CORRUPTED_DUST.get(), "Corrupted Dust");
        add(BMItems.CORRUPTED_DUST_TINY.get(), "Tiny Corrupted Dust");

        //ARC tool items
        add(BMItems.SANGUINE_REVERTER.get(), "Sanguine Reverter");
        add(BMItems.RESONATOR.get(), "Crystal Resonator");
        add(BMItems.PRIMITIVE_CRYSTALLINE_RESONATOR.get(), "Reinforced Resonator");
        add(BMItems.HELLFORGED_RESONATOR.get(), "Hellforged Resonator");
        add(BMItems.EXPLOSIVE_POWDER.get(), "Explosive Powder");
        add(BMItems.PRIMITIVE_EXPLOSIVE_CELL.get(), "Reinforced Explosive Cell");
        add(BMItems.HELLFORGED_EXPLOSIVE_CELL.get(), "Hellforged Explosive Cell");
        add(BMItems.BASIC_CUTTING_FLUID.get(), "Basic Cutting Fluid");
        add(BMItems.INTERMEDIATE_CUTTING_FLUID.get(), "Intermediate Cutting Fluid");
        add(BMItems.ADVANCED_CUTTING_FLUID.get(), "Advanced Cutting Fluid");
        add(BMItems.PRIMITIVE_HYDRATION_CELL.get(), "Primitive Hydration Cell");
        add(BMItems.PRIMITIVE_FURNACE_CELL.get(), "Primitive Fuel Cell");
        add(BMItems.LAVA_CRYSTAL.get(), "Lava Crystal");

        add(BMBlocks.CRYSTAL_CLUSTER, "Crystal Cluster");
        add(BMBlocks.CRYSTAL_CLUSTER_BRICK, "Crystal Cluster Brick");

        addTooltip("safe_for_decoration", "Safe for Decoration");

        add(BMBlocks.ARC_BLOCK, "Alchemical Reaction Chamber");

        add(BMBlocks.BLOOD_TANK, "Blood Tank");
        addTooltip("container_tier_missing", "No Tier found!");
        addTooltip("container_tier", "Current Tier: %s");
        addTooltip("fluid_content_empty", "Empty");
        addTooltip("fluid_content", "Contains: %smB of %s");

        add(BMBlocks.IMPERFECT_RITUAL_BLOCK, "Imperfect Ritual Stone");

        add(BMBlocks.MASTER_RITUAL_STONE, "Master Ritual Stone");

        add(BMBlocks.RITUAL_STONE_BLANK, "Ritual Stone");
        add(BMBlocks.RITUAL_STONE_WATER, "Water Ritual Stone");
        add(BMBlocks.RITUAL_STONE_FIRE, "Fire Ritual Stone");
        add(BMBlocks.RITUAL_STONE_EARTH, "Earth Ritual Stone");
        add(BMBlocks.RITUAL_STONE_AIR, "Air Ritual Stone");
        add(BMBlocks.RITUAL_STONE_DUSK, "Dusk Ritual Stone");
        add(BMBlocks.RITUAL_STONE_DAWN, "Dawn Ritual Stone");

        add("ritual.bloodmagic.full_spring", "Ritual of the Full Spring");
        add("ritual.bloodmagic.regeneration", "Ritual of Regeneration");
        add("ritual.bloodmagic.speed", "Ritual of Speed");
        add("ritual.bloodmagic.lava", "Ritual of the Crucible");
        add("ritual.bloodmagic.magnetism", "Ritual of Magnetism");
        add("ritual.bloodmagic.jumping", "Ritual of the High Jump");
        add("ritual.bloodmagic.felling", "Ritual of the Feller");
        add("ritual.bloodmagic.green_grove", "Ritual of the Green Grove");
        add("ritual.bloodmagic.full_stomach", "Ritual of the Full Stomach");
        add("ritual.bloodmagic.zephyr", "Ritual of the Zephyr");
        add("ritual.bloodmagic.yawning_void", "Ritual of the Yawning Void");
        add("ritual.bloodmagic.grounding", "Ritual of Grounding");
        add("ritual.bloodmagic.condor", "Ritual of the Condor");
        add("ritual.bloodmagic.animal_growth", "Ritual of Animal Growth");
        add("ritual.bloodmagic.well_of_suffering", "Ritual of the Well of Suffering");
        add("ritual.bloodmagic.feathered_knife", "Ritual of the Feathered Knife");
        add("ritual.bloodmagic.sphere_create", "Ritual of the Sphere Creation");
        add("ritual.bloodmagic.harvest", "Ritual of the Harvest");
        add("ritual.bloodmagic.meteor", "Ritual of the Meteor");
        add("ritual.bloodmagic.vault", "Ritual of the Vault");
        add("chat.bloodmagic.ritual.bound", "You have bound this ritual stone.");
        add("chat.bloodmagic.ritual.notOwner", "You do not own this ritual stone.");
        add("chat.bloodmagic.ritual.none", "The ritual stone is now inactive.");
        add("chat.bloodmagic.ritual.selected", "Selected: %s");
        add("chat.bloodmagic.ritual.activated", "The ritual has been activated.");
        add("chat.bloodmagic.ritual.deactivated", "The ritual has been deactivated.");

        add(BMBlocks.HELLFIRE_FORGE, "Hellfire Forge");
        add(BMItems.SIGIL_HOLDING.get(), "Sigil of Holding");
        addTooltip("sigil_holding.desc", "Sigil-ception");

        add(BMBlocks.ALCHEMY_ARRAY, "Alchemy Array");
        add(BMItems.REAGENT_BLOODLIGHT.get(), "Blood Lamp Reagent");
        add(BMItems.REAGENT_HOLDING.get(), "Holding Reagent");
        add(BMItems.REAGENT_SUPPRESSION.get(), "Suppression Reagent");
        add(BMItems.REAGENT_TELEPOSITION.get(), "Teleposition Reagent");
        add(BMItems.REAGENT_FASTMINER.get(), "Fast Miner Reagent");
        add(BMItems.REAGENT_BINDING.get(), "Binding Reagent");

        add(BMBlocks.TELEPOSER, "Teleposer");
        add(BMItems.TELEPOSER_FOCUS.get(), "Teleposition Focus");
        add(BMItems.ENHANCED_TELEPOSER_FOCUS.get(), "Enhanced Teleposition Focus");
        add(BMItems.REINFORCED_TELEPOSER_FOCUS.get(), "Reinforced Teleposition Focus");
        addTooltip("teleposerfocus.coords", "Current coordinates: (%d, %d, %d).");
        add("chat.bloodmagic.teleposer.linked", "Teleposer linked.");
        add("chat.bloodmagic.teleposer.unlinked", "Teleposer unlinked.");

        // Dagger of Sacrifice, ported from 1.20.1 (distinct from the self-sacrifice Sacrificial
        // Dagger above).
        add(BMItems.DAGGER_OF_SACRIFICE.get(), "Dagger of Sacrifice");

        // Will Catalysts, ported from 1.20.1.
        add(BMItems.RAW_CATALYST.get(), "Raw Will Catalyst");
        add(BMItems.CORROSIVE_CATALYST.get(), "Corrosive Will Catalyst");
        add(BMItems.DESTRUCTIVE_CATALYST.get(), "Destructive Will Catalyst");
        add(BMItems.STEADFAST_CATALYST.get(), "Steadfast Will Catalyst");
        add(BMItems.VENGEFUL_CATALYST.get(), "Vengeful Will Catalyst");
        addTooltip("crystalCatalyst", "Accelerates the growth of will.");

        // Demon crop blocks, ported from 1.20.1 - block names drop the original's "(NYI)" ("not yet
        // implemented") suffix on creeping_doubt/nether_soil now that they actually work here. No
        // separate seed-item translation keys are needed: BlockItem#getDescriptionId() delegates to
        // the placed block's key (vanilla behaviour, confirmed against BlockItem.java), so
        // GROWING_DOUBT_SEED/WEAK_TAU_SEED/STRONG_TAU_SEED automatically pick up the block names
        // below - adding them separately throws "Duplicate translation key" since it's the same key.
        add(BMBlocks.GROWING_DOUBT.get(), "Seeds of Doubt");
        add(BMBlocks.WEAK_TAU.get(), "Tau Fruit");
        add(BMBlocks.STRONG_TAU.get(), "Saturated Tau");
        add(BMBlocks.NETHER_SOIL, "Nether Soil");

        add(BMItems.ANOINTMENT_MELEE_DAMAGE.get(), "Anointment of Melee Damage");
        addTooltip("anointment_melee_damage.desc", "Coats a weapon in a corrosive edge, dealing bonus damage for a limited number of hits.");

        add(BMItems.ANOINTMENT_LOOTING.get(), "Anointment of Looting");
        addTooltip("anointment_looting.desc", "Coats a weapon so that its kills yield double the spoils, for a limited number of hits.");

        add(BMItems.ANOINTMENT_BOW_POWER.get(), "Anointment of Bow Power");
        addTooltip("anointment_bow_power.desc", "Coats a bow so its arrows strike harder, for a limited number of shots.");

        add(BMItems.ANOINTMENT_BOW_VELOCITY.get(), "Anointment of Bow Velocity");
        addTooltip("anointment_bow_velocity.desc", "Coats a bow so its arrows fly faster, for a limited number of shots.");

        add(BMItems.ANOINTMENT_HIDDEN_KNOWLEDGE.get(), "Anointment of Hidden Knowledge");
        addTooltip("anointment_hidden_knowledge.desc", "Coats a tool so mined blocks yield bonus experience, for a limited number of blocks.");

        add(BMItems.ANOINTMENT_HOLY_WATER.get(), "Anointment of Holy Water");
        addTooltip("anointment_holy_water.desc", "Coats a weapon with holy water, dealing severe bonus damage to the undead, for a limited number of hits.");

        add(BMItems.ANOINTMENT_QUICK_DRAW.get(), "Anointment of Quick Draw");
        addTooltip("anointment_quick_draw.desc", "Coats a bow so it fires at full power even on a rushed draw, for a limited number of shots.");

        add(BMItems.ANOINTMENT_SILK_TOUCH.get(), "Anointment of Silk Touch");
        addTooltip("anointment_silk_touch.desc", "Coats a tool so mined blocks drop themselves intact, for a limited number of blocks.");

        add(BMItems.ANOINTMENT_FORTUNE.get(), "Anointment of Fortune");
        addTooltip("anointment_fortune.desc", "Coats a tool so mined blocks drop extra loot, for a limited number of blocks.");

        add(BMItems.ANOINTMENT_SMELTING.get(), "Anointment of Smelting");
        addTooltip("anointment_smelting.desc", "Coats a tool so mined blocks drop their smelted form, for a limited number of blocks.");

        add(BMItems.ANOINTMENT_VOIDING.get(), "Anointment of Voiding");
        addTooltip("anointment_voiding.desc", "Coats a tool so mined blocks drop nothing at all, for a limited number of blocks.");

        add(BMItems.ANOINTMENT_WEAPON_REPAIR.get(), "Anointment of Weapon Repair");
        addTooltip("anointment_weapon_repair.desc", "Coats a weapon so it mends a little with every hit, for a limited number of hits.");

        add(BMItems.ANOINTMENT_WILL_POWER.get(), "Anointment of Will Power");
        addTooltip("anointment_will_power.desc", "Coats a weapon so its hits siphon a little Demon Will into your inventory, for a limited number of hits.");

        // Larger-capacity tiers of the anointments above (ported from 1.20.1's "_L"/"_XL" container
        // sizes - see BMItems for why Will Power has no tiers, matching 1.20.1).
        add(BMItems.ANOINTMENT_MELEE_DAMAGE_L.get(), "Anointment of Melee Damage L");
        add(BMItems.ANOINTMENT_MELEE_DAMAGE_XL.get(), "Anointment of Melee Damage XL");
        add(BMItems.ANOINTMENT_LOOTING_L.get(), "Anointment of Looting L");
        add(BMItems.ANOINTMENT_LOOTING_XL.get(), "Anointment of Looting XL");
        add(BMItems.ANOINTMENT_BOW_POWER_L.get(), "Anointment of Bow Power L");
        add(BMItems.ANOINTMENT_BOW_POWER_XL.get(), "Anointment of Bow Power XL");
        add(BMItems.ANOINTMENT_BOW_VELOCITY_L.get(), "Anointment of Bow Velocity L");
        add(BMItems.ANOINTMENT_BOW_VELOCITY_XL.get(), "Anointment of Bow Velocity XL");
        add(BMItems.ANOINTMENT_HIDDEN_KNOWLEDGE_L.get(), "Anointment of Hidden Knowledge L");
        add(BMItems.ANOINTMENT_HIDDEN_KNOWLEDGE_XL.get(), "Anointment of Hidden Knowledge XL");
        add(BMItems.ANOINTMENT_HOLY_WATER_L.get(), "Anointment of Holy Water L");
        add(BMItems.ANOINTMENT_HOLY_WATER_XL.get(), "Anointment of Holy Water XL");
        add(BMItems.ANOINTMENT_QUICK_DRAW_L.get(), "Anointment of Quick Draw L");
        add(BMItems.ANOINTMENT_QUICK_DRAW_XL.get(), "Anointment of Quick Draw XL");
        add(BMItems.ANOINTMENT_SILK_TOUCH_L.get(), "Anointment of Silk Touch L");
        add(BMItems.ANOINTMENT_SILK_TOUCH_XL.get(), "Anointment of Silk Touch XL");
        add(BMItems.ANOINTMENT_FORTUNE_L.get(), "Anointment of Fortune L");
        add(BMItems.ANOINTMENT_FORTUNE_XL.get(), "Anointment of Fortune XL");
        add(BMItems.ANOINTMENT_SMELTING_L.get(), "Anointment of Smelting L");
        add(BMItems.ANOINTMENT_SMELTING_XL.get(), "Anointment of Smelting XL");
        add(BMItems.ANOINTMENT_VOIDING_L.get(), "Anointment of Voiding L");
        add(BMItems.ANOINTMENT_VOIDING_XL.get(), "Anointment of Voiding XL");
        add(BMItems.ANOINTMENT_WEAPON_REPAIR_L.get(), "Anointment of Weapon Repair L");
        add(BMItems.ANOINTMENT_WEAPON_REPAIR_XL.get(), "Anointment of Weapon Repair XL");

        add(BMBlocks.INCENSE_ALTAR, "Incense Altar");

        add("effect.bloodmagic.soul_snare", "Soul Snare");
        add("effect.bloodmagic.soft_fall", "Soft Fall");
        add("effect.bloodmagic.fire_fuse", "Fire Fuse");
        add("effect.bloodmagic.suspended", "Suspended");
        add("effect.bloodmagic.flight", "Flight");
        add("effect.bloodmagic.heavy_heart", "Heavy Heart");
        add("effect.bloodmagic.passivity", "Passivity");
        add("effect.bloodmagic.plant_leech", "Plant Leech");
        add("effect.bloodmagic.sacrificial_lamb", "Sacrificial Lamb");
        add("effect.bloodmagic.spectral_sight", "Spectral Sight");
        add("effect.bloodmagic.gravity", "Gravity");
        add("effect.bloodmagic.grounded", "Grounded");
        add("effect.bloodmagic.obsidian_cloak", "Obsidian Cloak");
        add("effect.bloodmagic.hard_cloak", "Hard Cloak");
        add("effect.bloodmagic.bounce", "Bounce");
        add("effect.bloodmagic.soul_fray", "Soul Fray");

        add(BMItems.RITUAL_DIVINER.get(), "Ritual Diviner");
        add(BMItems.RITUAL_DIVINER_DUSK.get(), "Dusk Ritual Diviner");
        add(BMItems.RITUAL_DIVINER_DAWN.get(), "Dawn Ritual Diviner");
        add("chat.bloodmagic.diviner.none", "This Master Ritual Stone has no ritual bound.");
        add("chat.bloodmagic.diviner.info", "Ritual: %s | Active: %s | Cost: %s LP every %s ticks");
        add("tooltip.bloodmagic.diviner.currentRitual", "Current Ritual: %s");
        add("tooltip.bloodmagic.diviner.currentDirection", "Direction: %s");
        add("tooltip.bloodmagic.diviner.totalRune", "Total Runes: %s");

        add(BMItems.ACTIVATION_CRYSTAL_WEAK.get(), "Weak Activation Crystal");
        add(BMItems.ACTIVATION_CRYSTAL_AWAKENED.get(), "Awakened Activation Crystal");
        add(BMItems.ACTIVATION_CRYSTAL_CREATIVE.get(), "Creative Activation Crystal");
        add("tooltip.bloodmagic.activation_crystal.weak", "Can activate low-tier rituals.");
        add("tooltip.bloodmagic.activation_crystal.awakened", "Can activate mid-tier rituals.");
        add("tooltip.bloodmagic.activation_crystal.creative", "Can activate any ritual.");

        add(BMItems.DEMON_WILL_GAUGE.get(), "Demon Will Gauge");
        add("chat.bloodmagic.willgauge.line", "%s Will: %s");
        add("chat.bloodmagic.willgauge.empty", "You aren't carrying any Will.");
        add("chat.bloodmagic.willgauge.aura_line", "Ambient %s Will: %s");
        add("chat.bloodmagic.willgauge.aura_empty", "There is no ambient Will here.");

        add("key.categories.bloodmagic", "Blood Magic");
        add("key.bloodmagic.open_holding", "Open Sigil of Holding");
        add("key.bloodmagic.open_hud_editor", "Open HUD Editor");

        add(BMBlocks.ITEM_ROUTER, "Item Router");

        add(BMBlocks.MASTER_ROUTING_NODE, "Master Routing Node");
        add(BMBlocks.INPUT_ROUTING_NODE, "Input Routing Node");
        add(BMBlocks.OUTPUT_ROUTING_NODE, "Output Routing Node");
        add("chat.bloodmagic.routing_node.whitelist", "Filter mode: Whitelist");
        add("chat.bloodmagic.routing_node.blacklist", "Filter mode: Blacklist");
        add("chat.bloodmagic.routing_node.priority", "Priority: %s");
        add("chat.bloodmagic.item_router.added", "Added %s to the filter.");
        add("chat.bloodmagic.item_router.removed", "Removed %s from the filter.");
        add("chat.bloodmagic.item_router.full", "The filter is full.");

        add(BMItems.EXPERIENCE_BOOK.get(), "Experience Book");
        addTooltip("experience_book", "Stores banked knowledge for later.");
        addTooltip("experience_book.stored", "Stored Experience: %s");
        add(BMItems.RAW_WILL.get(), "Raw Will");

        //Soul Gems
        add(BMItems.SOUL_GEM_PETTY.get(), "Petty Tartaric Gem");
        add(BMItems.SOUL_GEM_LESSER.get(), "Lesser Tartaric Gem");
        add(BMItems.SOUL_GEM_COMMON.get(), "Common Tartaric Gem");
        add(BMItems.SOUL_GEM_GREATER.get(), "Greater Tartaric Gem");
        add(BMItems.SOUL_GEM_GRAND.get(), "Grand Tartaric Gem");
        addGemDesc(BMItems.SOUL_GEM_PETTY, "a little");
        addGemDesc(BMItems.SOUL_GEM_LESSER, "some");
        addGemDesc(BMItems.SOUL_GEM_COMMON, "more");
        addGemDesc(BMItems.SOUL_GEM_GREATER, "a greater amount of");
        addGemDesc(BMItems.SOUL_GEM_GRAND, "a large amount of");

        addTooltip("will", "Will Quality: %s");
        for (EnumWillType type : EnumWillType.values()) {
            addTooltip("current_type." + type.getSerializedName(), String.format("Contains: %s Will", type.toCapitalized()));
        }

        add(BMBlocks.ALCHEMY_TABLE, "Alchemy Table");
        addTooltip("alchemy_table.orb_error.title", "Orb Error");
        addTooltip("alchemy_table.orb_error.text", "Blood Orb not bound or missing");
        addTooltip("alchemy_table.essence_error.title", "Life Essence Error");
        addTooltip("alchemy_table.essence_error.text", "Not enough Life Essence in Soul Network");
        addTooltip("alchemy_table.stack_limit_toggle", "Toggle Max Stack Amount for Input Slots");

        //Living Armour and upgrades
        add("item_group.bloodmagic.main", "Blood Magic");
        add("item_group.bloodmagic.tomes", "Blood Magic Upgrade Tomes");
        add("item_group.bloodmagic.trainers", "Blood Magic Trainer Tomes");

        add(BMItems.LIVING_HELMET.get(), "Living Helmet");
        add(BMItems.LIVING_PLATE.get(), "Living Plate");
        add(BMItems.LIVING_LEGGINGS.get(), "Living Leggings");
        add(BMItems.LIVING_BOOTS.get(), "Living Boots");
        add(BMItems.UPGRADE_TOME.get(), "Upgrade Tome");

        add(BMBlocks.LIVING_STATION, "Living Upgrade Station");
        add(BMItems.UPGRADE_SCRAP.get(), "Upgrade Tome Scrap");
        add(BMItems.SYNTHETIC_POINT.get(), "Synthetic Upgrade Points");
        addTooltip("scrap", "Contained Upgrade Points: %s");

        add(BMItems.TRAINING_BRACELET.get(), "Living Training Bracelet");
        add("trainer.bloodmagic.allow_others", "Allow Others");
        add("trainer.bloodmagic.deny_others", "Deny Others");
        add("trainer.bloodmagic.save", "Save");

        addCommand("upgrade.get", "%s has the following upgrades:\n");
        addCommand("upgrade.set", "Set %s to %s exp for %s");
        addCommand("upgrade.no_armour", "The chestplate %s is wearing does not have an entry in the 'Living Armour Data' data map. Upgrades cannot take effect like this");
        addCommand("evolve.success", "Set evolved state to %s");
        addCommand("recalc.success", "Upgrades use up %s points");
        addCommand("limit.get", "%s is in '%s' mode and has the following limits:\n");
        addCommand("limit.set", "Set limit of %s to %s exp for %s");
        addCommand("limit.mode.allow", "allow others");
        addCommand("limit.mode.deny", "deny others");

        addTooltip("upgrade_points", "Upgrade Points: %s/%s");
        add("chat.bloodmagic.living_upgrade.level_up", "%s has levelled up to %s!");

        LivingUpgrades.translations(this::add);
        SigilData.translations(this::add);

        // JEI recipe category lang-keys
        add("jei.bloodmagic.recipe.alchemyarray", "Alchemy Array");
        add("jei.bloodmagic.recipe.alchemytable", "Alchemy Table");
        add("jei.bloodmagic.recipe.altar", "Blood Altar");
        add("jei.bloodmagic.recipe.arc", "ARC Recipe");
        add("jei.bloodmagic.recipe.arcfurnace", "ARC Furnace Recipe");
        add("jei.bloodmagic.recipe.consumptionrate", "Consumption: %s LP/t");
        add("jei.bloodmagic.recipe.drainrate", "Drain: %s LP/t");
        add("jei.bloodmagic.recipe.info", "Info");
        add("jei.bloodmagic.recipe.lp", "LP");
        add("jei.bloodmagic.recipe.lpDrained", "Drained: %s LP");
        add("jei.bloodmagic.recipe.minimumsouls", "Minimum: %s Will");
        add("jei.bloodmagic.recipe.potionflask", "Potion Flask");
        add("jei.bloodmagic.recipe.requiredlp", "LP: %,d");
        add("jei.bloodmagic.recipe.requiredtier", "Tier: %d");
        add("jei.bloodmagic.recipe.soulforge", "Hellfire Forge");
        add("jei.bloodmagic.recipe.soulsdrained", "Drained: %s Will");
        add("jei.bloodmagic.recipe.ticksRequired", "Time: %sTicks");
        add("jei.bloodmagic.recipe.will", "Will");
        add("tooltip.bloodmagic.tier", "Tier %d");

        //Alchemical Potion Flasks
        add(BMItems.ALCHEMY_FLASK.get(), "Alchemy Flask");
        add(BMItems.ALCHEMY_FLASK_THROWABLE.get(), "Throwable Alchemy Flask");
        add(BMItems.ALCHEMY_FLASK_LINGERING.get(), "Lingering Alchemy Flask");
        addTooltip("arctool.uses", "Uses: %d");

        //Alchemy Table catalysts/filling agents
        add(BMItems.SIMPLE_CATALYST.get(), "Simple Catalyst");
        add(BMItems.MUNDANE_POWER_CATALYST.get(), "Mundane Power Catalyst");
        add(BMItems.MUNDANE_LENGTHENING_CATALYST.get(), "Mundane Lengthening Catalyst");
        add(BMItems.COMBINATIONAL_CATALYST.get(), "Combinational Catalyst");
        add(BMItems.WEAK_FILLING_AGENT.get(), "Weak Filling Agent");
        add(BMItems.CYCLING_CATALYST.get(), "Cycling Catalyst");
        add(BMItems.AVERAGE_POWER_CATALYST.get(), "Average Power Catalyst");
        add(BMItems.AVERAGE_LENGTHENING_CATALYST.get(), "Average Lengthening Catalyst");
        add(BMItems.AVERAGE_FILLING_AGENT.get(), "Average Filling Agent");

        add(BMBlocks.DEMON_CRUCIBLE, "Demonic Crucible");
        addTooltip("demon_crucible.desc", "Exchanges Will between a held Raw Will item or Soul Gem and the ambient aura. Unpowered releases Will into the aura; a redstone signal reverses it to withdraw Will instead.");
        add(BMBlocks.DEMON_CRYSTALLIZER, "Demonic Crystallizer");
        addTooltip("demon_crystallizer.desc", "Slowly condenses a chunk's ambient Will into a growing Crystal Cluster placed above it.");
        add(BMBlocks.DEMON_PYLON, "Demonic Pylon");
        addTooltip("demon_pylon.desc", "Draws ambient Will from the 4 neighboring chunks into its own, slowly concentrating Will over an area.");

        add(BMBlocks.SHAPED_CHARGE, "Shaped Charge");
        addTooltip("shaped_charge.desc", "Sticks to a surface and, a few seconds after being placed, digs a small tunnel into whatever it's attached to.");
        add(BMBlocks.DEFORESTER_CHARGE, "Deforester Charge");
        addTooltip("deforester_charge.desc", "Sticks to a tree and clears the whole connected trunk and canopy a few seconds after being placed.");
        add(BMBlocks.VEINMINE_CHARGE, "Vein Charge");
        addTooltip("veinmine_charge.desc", "Sticks to a block and clears the whole connected vein of that block a few seconds after being placed.");
        add(BMBlocks.FUNGAL_CHARGE, "Fungal Charge");
        addTooltip("fungal_charge.desc", "Sticks to a huge mushroom and clears the whole connected growth a few seconds after being placed.");

        // Demon Dungeon system
        add(wayoftime.bloodmagic.common.item.BMItems.DUNGEON_SIMPLE_KEY.get(), "Simple Dungeon Key");
        add(wayoftime.bloodmagic.common.item.BMItems.DUNGEON_MINE_ENTRANCE_KEY.get(), "Mine Entrance Key");
        add(wayoftime.bloodmagic.common.item.BMItems.DUNGEON_MINE_KEY.get(), "Mine Key");
        add(wayoftime.bloodmagic.common.item.BMItems.DUNGEON_TESTER.get(), "Dungeon Tester (Debug)");
        add(BMBlocks.DUNGEON_STONE, "Dungeon Stone");
        add(BMBlocks.DUNGEON_ORE, "Dungeon Ore");
        add(BMBlocks.DUNGEON_BRICK_ASSORTED, "Assorted Dungeon Brick");
        add(BMBlocks.DUNGEON_TILE_SPECIAL, "Blocked Dungeon Seal");
        add(BMBlocks.DUNGEON_CONTROLLER.get(), "Dungeon Controller");
        add(BMBlocks.DUNGEON_SEAL.get(), "Dungeon Seal");
        add(BMBlocks.SPECIAL_DUNGEON_SEAL.get(), "Special Dungeon Seal");
        addTooltip("specialspawn", "A special room has spawned nearby!");
        addTooltip("blockeddoor", "This door leads nowhere...");
        addTooltip("incorrectKey", "This key doesn't fit this door.");

        // Demon Dungeon decorative block palette (see BMBlocks) - each family below has 5
        // Will-corruption variants (base + corrosive/destructive/steadfast/vengeful reskins).
        addDungeonFamily(BMBlocks.DUNGEON_BRICK_1, "Dungeon Brick 1");
        addDungeonFamily(BMBlocks.DUNGEON_BRICK_2, "Dungeon Brick 2");
        addDungeonFamily(BMBlocks.DUNGEON_BRICK_3, "Dungeon Brick 3");
        addDungeonFamily(BMBlocks.DUNGEON_POLISHED, "Polished Dungeon Stone");
        addDungeonFamily(BMBlocks.DUNGEON_TILE_FAMILY, "Dungeon Tile");
        addDungeonFamily(BMBlocks.DUNGEON_SMALLBRICK, "Small Dungeon Brick");
        addDungeonFamily(BMBlocks.DUNGEON_METAL, "Dungeon Metal");
        addDungeonFamily(BMBlocks.DUNGEON_EYE, "Dungeon Eye");
        // Base ("") variant already has a lang entry above (from the previous round) - only the 4
        // Will reskins are new here, so these two use the reskins-only variant of the helper.
        addDungeonFamilyReskinsOnly(BMBlocks.DUNGEON_STONE_FAMILY, "Dungeon Stone");
        addDungeonFamilyReskinsOnly(BMBlocks.DUNGEON_TILE_SPECIAL_FAMILY, "Blocked Dungeon Seal");
        addDungeonFamily(BMBlocks.DUNGEON_PILLAR_CENTER, "Dungeon Pillar");
        addDungeonFamily(BMBlocks.DUNGEON_PILLAR_SPECIAL, "Special Dungeon Pillar");
        addDungeonFamily(BMBlocks.DUNGEON_PILLAR_CAP, "Dungeon Pillar Cap");
        addDungeonFamily(BMBlocks.DUNGEON_BRICK_STAIRS, "Dungeon Brick Stairs");
        addDungeonFamily(BMBlocks.DUNGEON_POLISHED_STAIRS, "Polished Dungeon Stone Stairs");
        addDungeonFamily(BMBlocks.DUNGEON_STONE_STAIRS, "Dungeon Stone Stairs");
        addDungeonFamily(BMBlocks.DUNGEON_BRICK_WALLS, "Dungeon Brick Wall");
        addDungeonFamily(BMBlocks.DUNGEON_TILE_WALLS, "Dungeon Tile Wall");
        addDungeonFamily(BMBlocks.DUNGEON_POLISHED_WALLS, "Polished Dungeon Stone Wall");
        addDungeonFamily(BMBlocks.DUNGEON_STONE_WALLS, "Dungeon Stone Wall");
        addDungeonFamily(BMBlocks.DUNGEON_BRICK_GATES, "Dungeon Brick Fence Gate");
        addDungeonFamily(BMBlocks.DUNGEON_POLISHED_GATES, "Polished Dungeon Stone Fence Gate");
        addDungeonFamily(BMBlocks.DUNGEON_BRICK_SLABS, "Dungeon Brick Slab");
        addDungeonFamily(BMBlocks.DUNGEON_TILE_SLABS, "Dungeon Tile Slab");
        addDungeonFamily(BMBlocks.DUNGEON_STONE_SLABS, "Dungeon Stone Slab");
        addDungeonFamily(BMBlocks.DUNGEON_POLISHED_SLABS, "Polished Dungeon Stone Slab");
        add(BMBlocks.DUNGEON_EMITTER, "Dungeon Light Emitter");
        add(BMBlocks.DUNGEON_CRACKED_BRICK_1, "Cracked Dungeon Brick");
        add(BMBlocks.DUNGEON_GLOWING_CRACKED_BRICK_1, "Glowing Cracked Dungeon Brick");
        // Dungeon puzzle/hazard blocks (Priority 3 flavor content, see BMBlocks).
        add(BMBlocks.DUNGEON_ALTERNATOR, "Dungeon Alternator");
        add(BMBlocks.DUNGEON_SPIKE_TRAP, "Dungeon Spike Trap");
        add(BMBlocks.DUNGEON_SPIKES, "Spikes");
        add(BMBlocks.MIMIC, "Opaque Mimic");
        add(BMBlocks.ETHEREAL_MIMIC, "Ethereal Mimic");
        add("chat.bloodmagic.mimic.potionSet", "Potion effect set.");
        add("chat.bloodmagic.mimic.potionSpawnRadius.up", "Potion spawn radius increased to %s.");
        add("chat.bloodmagic.mimic.potionSpawnRadius.down", "Potion spawn radius decreased to %s.");
        add("chat.bloodmagic.mimic.detectRadius.up", "Detection radius increased to %s.");
        add("chat.bloodmagic.mimic.detectRadius.down", "Detection radius decreased to %s.");
        add("chat.bloodmagic.mimic.potionInterval.up", "Potion interval increased to %s.");
        add("chat.bloodmagic.mimic.potionInterval.down", "Potion interval decreased to %s.");

        //Modopedia Guidebook lang-keys (was Patchouli)
        addBook("name", "Sanguine Scientiem");
        addBook("landing_text", "Welcome to $(blood)Blood Magic$()! \n\n$(bmentry:utility/nyi)A lot of stuff$() isn't yet implemented, so please excuse our dust. \n\nClick $(bmentry:utility/getting_started)HERE$() to get started. If you find any bugs, please report them on our $(l:https://github.com/WayofTime/BloodMagic/issues)Github$().");
        addBook("subtitle", "Alchemical Wizardry");

        // Item Routing "Filter" system, restored from 1.20.1 (see BMItems and
        // wayoftime.bloodmagic.common.item.filter.AbstractFilterItem).
        add(BMItems.STANDARD_FILTER.get(), "Standard Filter");
        add(BMItems.TAG_FILTER.get(), "Tag Filter");
        add(BMItems.MOD_FILTER.get(), "Mod Filter");
        add(BMItems.ENCHANT_FILTER.get(), "Enchantment Filter");
        add(BMItems.COMPOSITE_FILTER.get(), "Composite Filter");
        addTooltip("basicfilter.desc", "Matches an exact item.");
        addTooltip("tagfilter.desc", "Matches any of an item's tags.");
        addTooltip("modfilter.desc", "Matches by the mod an item comes from.");
        addTooltip("enchantfilter.desc", "Matches any of an item's enchantments.");
        addTooltip("compositefilter.desc", "Combines other Filters with AND/OR.");
        addTooltip("filter.whitelist", "Whitelist");
        addTooltip("filter.blacklist", "Blacklist");
        add("chat.bloodmagic.routing_node.filter_installed", "Installed filter: %s");
        add("chat.bloodmagic.routing_node.filter_removed", "Removed filter: %s");
        add("filter.bloodmagic.blackwhitelist", "Whitelist/Blacklist");
        add("filter.bloodmagic.whitelist", "Mode: Whitelist");
        add("filter.bloodmagic.blacklist", "Mode: Blacklist");
        add("filter.bloodmagic.matchany", "Mode: Match Any (OR)");
        add("filter.bloodmagic.matchall", "Mode: Match All (AND)");
    }

    public void addBook(String key, String value) {
        add("guide.bloodmagic." + key, value);
    }

    public void addCommand(String key, String value) {
        add("commands.bloodmagic." + key, value);
    }

    public void addGemDesc(DeferredHolder holder, String desc) {
        addTooltip("soul_gem." + holder.getId().getPath(), String.format("A gem used to contain %s will.", desc));
    }

    public void add(BlockWithItemHolder<? extends Block, ? extends BlockItem> block, String name) {
        add(block.block().get().getDescriptionId(), name);
    }

    public void addTooltip(String name, String value) {
        add("tooltip.bloodmagic." + name, value);
    }

    // Demon Dungeon decorative block palette helper (see BMBlocks) - families are keyed by the same
    // "" / "_corrosive" / "_destructive" / "_steadfast" / "_vengeful" suffixes used to register them.
    private static final String[] DUNGEON_WILL_SUFFIXES = {"", "_corrosive", "_destructive", "_steadfast", "_vengeful"};
    private static final String[] DUNGEON_WILL_LABEL_PREFIXES = {"", "Corrosive ", "Destructive ", "Steadfast ", "Vengeful "};

    private void addDungeonFamily(java.util.Map<String, ? extends BlockWithItemHolder<? extends Block, ? extends BlockItem>> family, String baseName) {
        for (int i = 0; i < DUNGEON_WILL_SUFFIXES.length; i++) {
            BlockWithItemHolder<? extends Block, ? extends BlockItem> holder = family.get(DUNGEON_WILL_SUFFIXES[i]);
            if (holder != null) {
                add(holder, DUNGEON_WILL_LABEL_PREFIXES[i] + baseName);
            }
        }
    }

    // Like addDungeonFamily, but skips the "" (base) variant - used for families whose base block was
    // already given a lang entry in a previous round (see call sites above).
    private void addDungeonFamilyReskinsOnly(java.util.Map<String, ? extends BlockWithItemHolder<? extends Block, ? extends BlockItem>> family, String baseName) {
        for (int i = 1; i < DUNGEON_WILL_SUFFIXES.length; i++) {
            BlockWithItemHolder<? extends Block, ? extends BlockItem> holder = family.get(DUNGEON_WILL_SUFFIXES[i]);
            if (holder != null) {
                add(holder, DUNGEON_WILL_LABEL_PREFIXES[i] + baseName);
            }
        }
    }
}
