package wayoftime.bloodmagic.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.item.BMMaterialsAndTiers;
import wayoftime.bloodmagic.common.living.LivingUpgrade;
import wayoftime.bloodmagic.api.altar.AltarTier;

public class BMTags {
    public static class Items {
        public static final TagKey<Item> SOUL_GEM = tag(bm("soul_gems"));

        public static final TagKey<Item> STORAGE_BLOCKS_HELLFORGED = fromBlock(Blocks.STORAGE_BLOCKS_HELLFORGED);

        // The 4 Explosive Charge family items (see BMBlocks' SHAPED_CHARGE/DEFORESTER_CHARGE/
        // VEINMINE_CHARGE/FUNGAL_CHARGE) - used by the anointment-driven block-drop loot modifiers
        // (see common.loot.BMLootModifiers) to make sure a charge held/used as a "tool" never itself
        // triggers anointment bonuses meant for its own detonation-time synthetic harvesting tool,
        // matching 1.20.1's BloodMagicTags.CHARGES guard in GlobalLootModifier.
        public static final TagKey<Item> CHARGES = tag(bm("charges"));

        public static final TagKey<Item> ARC_TOOL = tag(bm("arc_tool"));

        public static final TagKey<Item> REVERTER = withParent(ARC_TOOL, bm("reverter"));
        public static final TagKey<Item> RESONATOR = withParent(ARC_TOOL, bm("resonator"));
        public static final TagKey<Item> EXPLOSIVES = withParent(ARC_TOOL, bm("explosives"));
        public static final TagKey<Item> CUTTING_FLUIDS = withParent(ARC_TOOL, bm("cutting_fluids"));
        public static final TagKey<Item> HYDRATION = withParent(ARC_TOOL, bm("hydration"));

        public static final TagKey<Item> ARC_FURNACE = withParent(ARC_TOOL, bm("furnace"));
        public static final TagKey<Item> ARC_BLASTING = withParent(ARC_FURNACE, bm("blasting"));
        public static final TagKey<Item> ARC_SMELTING = withParent(ARC_FURNACE, bm("smelting"));
        public static final TagKey<Item> ARC_SMOKING = withParent(ARC_FURNACE, bm("smoking"));

        public static final TagKey<Item> LIVING_UPGRADE_SET = tag(bm("living_upgrade_set"));
        public static final TagKey<Item> LIVING_SET = withParent(LIVING_UPGRADE_SET, BMMaterialsAndTiers.LIVING_ARMOR_MATERIAL.getId());

        private static TagKey<Item> fromBlock(TagKey<Block> input) {
            return tag(input.location());
        }

        private static TagKey<Item> withParent(TagKey<Item> parent, ResourceLocation location) {
            return TagKey.create(Registries.ITEM, location.withPrefix(parent.location().getPath()+"/"));
        }

        private static TagKey<Item> tag(ResourceLocation id) {
            return TagKey.create(Registries.ITEM, id);
        }
    }

    public static class Blocks {
        public static final TagKey<Block> RUNES = tag(bm("altar/runes"));
        public static final TagKey<Block> PILLARS = tag(bm("altar/pillars"));
        public static final TagKey<Block> T3_CAPSTONES = tag(bm("altar/t3_capstones"));
        public static final TagKey<Block> T4_CAPSTONES = tag(bm("altar/t4_capstones"));
        public static final TagKey<Block> T5_CAPSTONES = tag(bm("altar/t5_capstones"));
        public static final TagKey<Block> T6_CAPSTONES = tag(bm("altar/t6_capstones"));

        public static final TagKey<Block> PULSE_ON_CRAFTING = tag(bm("altar/pulse_on_crafting"));
        public static final TagKey<Block> SOUL_NETWORK_COMPARATOR = tag(bm("altar/soul_network_comparator"));

        public static final TagKey<Block> STORAGE_BLOCKS_HELLFORGED = tag(c("storage_blocks/hellforged"));

        // Ported from 1.20.1's BloodMagicTags.Blocks.MUNDANE_BLOCK - gates the Voiding anointment's
        // loot modifier (see common.loot.BMLootModifiers) so it only discards drops from "junk"
        // blocks (stone/cobblestone/sand/dirt/gravel/netherrack) instead of every block a Voiding
        // charge/tool touches.
        public static final TagKey<Block> MUNDANE_BLOCK = tag(bm("mundane_block"));

        // Teleposer block-swap guard, ported from 1.20.1's BloodMagicTags.TELEPOSE_BLOCK_BLACKLIST -
        // blocks in this tag are never swapped by the Teleposer (see TeleposerTile), matching the
        // original's "don't teleposition portals/doors/beds/pistons/bedrock/the Alchemy Table" list.
        public static final TagKey<Block> TELEPOSE_BLOCK_BLACKLIST = tag(bm("telepose_blacklist"));

        private static TagKey<Block> tag(ResourceLocation id) {
            return TagKey.create(Registries.BLOCK, id);
        }
    }

    public static class EntityTypes {
        // Teleposer entity-swap guard, ported from 1.20.1's BloodMagicTags.TELEPOSE_BLACKLIST - empty
        // by default there too (a hook point for datapacks to exclude specific entity types from
        // Teleposer swaps), so no entries are added by this branch's datagen either.
        public static final TagKey<EntityType<?>> TELEPOSE_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, bm("telepose_blacklist"));
    }

    public static class DamageTypes {
        public static final TagKey<DamageType> SELF_SACRIFICE = TagKey.create(Registries.DAMAGE_TYPE, bm("self_sacrifice"));
        public static final TagKey<DamageType> TOUGH_IGNORED = TagKey.create(Registries.DAMAGE_TYPE, bm("tough_ignored"));
    }

    public static class Tiers {
        public static final TagKey<AltarTier> VALID_TIERS = TagKey.create(BMIdentifiers.RegistryKeys.ALTAR_TIER_KEY, bm("valid_tiers"));
    }

    public static class Living {
        public static final TagKey<LivingUpgrade> TOOLTIP_ORDER = tag(bm("tooltip_order"));
        public static final TagKey<LivingUpgrade> TOOLTIP_HIDE = tag(bm("tooltip_hide"));
        public static final TagKey<LivingUpgrade> IS_DOWNGRADE = tag(bm("is_downgrade"));
        public static final TagKey<LivingUpgrade> LIVING_START = tag(bm("living_start"));
        public static final TagKey<LivingUpgrade> TRAINERS = tag(bm("trainer"));
        public static final TagKey<LivingUpgrade> IS_SCRAPPABLE = tag(bm("is_scrappable"));

        public static final TagKey<LivingUpgrade> LIVING_BLACKLIST = tag(bm("empty"));

        private static TagKey<LivingUpgrade> tag(ResourceLocation id) {
            return TagKey.create(BMIdentifiers.RegistryKeys.LIVING_UPGRADES, id);
        }
    }

    private static ResourceLocation bm(String path) {
        return ResourceLocation.fromNamespaceAndPath(BloodMagic.MODID, path);
    }

    private static ResourceLocation c(String path) {
        return ResourceLocation.fromNamespaceAndPath("c", path);
    }
}
