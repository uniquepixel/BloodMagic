package wayoftime.bloodmagic.common.datamap;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import wayoftime.bloodmagic.BloodMagic;

import java.util.List;

public class BMDataMaps {
    public static final DataMapType<Item, Double> TARTARIC_GEM_MAX_AMOUNTS = DataMapType.builder(
            BloodMagic.rl("tartaric_gem_max"),
            Registries.ITEM,
            Codec.DOUBLE
    ).synced(Codec.DOUBLE, true).build();

    public static final DataMapType<Item, BloodOrb> BLOOD_ORB_STATS = DataMapType.builder(
            BloodMagic.rl("blood_orb_stats"),
            Registries.ITEM,
            BloodOrb.CODEC
    ).synced(BloodOrb.CODEC, true).build();


    public static final DataMapType<Block, List<BloodRune>> BLOOD_RUNES = DataMapType.builder(
            BloodMagic.rl("blood_runes"),
            Registries.BLOCK,
            BloodRune.CODEC.listOf()
    ).synced(BloodRune.CODEC.listOf(), true).build();

    public static final DataMapType<Item, LivingArmorData> LIVING_ARMOUR_DATA = DataMapType.builder(
            BloodMagic.rl("armour_data"),
            Registries.ITEM,
            LivingArmorData.CODEC
    ).synced(LivingArmorData.CODEC, true).build();

    public static final DataMapType<Block, ResourceLocation> IMPERFECT_RITUAL_CATALYST = DataMapType.builder(
            BloodMagic.rl("imperfect_ritual_catalysts"),
            Registries.BLOCK,
            ResourceLocation.CODEC
    ).synced(ResourceLocation.CODEC, true).build();

    // wayoftime.bloodmagic.common.item.DaggerOfSacrificeItem's per-entity-type LP-per-health ratio,
    // ported from 1.20.1's ConfigManager.COMMON.sacrificialValues (a config-file string list of
    // "entityid;ratio" pairs, e.g. "villager;100", parsed into BloodMagicValueManager#sacrificial).
    // A data map fits this branch's architecture better than a parsed config list - see e.g.
    // BLOOD_ORB_STATS above for the established precedent - and datapacks can extend/override it the
    // normal data-map way. Entity types with no entry here fall back to
    // BloodMagic.SERVER_CONFIG.DEFAULT_ENTITY_SACRIFICE_RATIO (ported from 1.20.1's
    // entitySacrificeDefault, which stayed a plain config value since it's a single scalar, not a
    // per-entity table).
    public static final DataMapType<EntityType<?>, Integer> SACRIFICE_LP_RATIO = DataMapType.builder(
            BloodMagic.rl("sacrifice_lp_ratio"),
            Registries.ENTITY_TYPE,
            Codec.INT
    ).synced(Codec.INT, true).build();

    public static void register(RegisterDataMapTypesEvent event) {
        event.register(TARTARIC_GEM_MAX_AMOUNTS);
        event.register(BLOOD_ORB_STATS);
        event.register(BLOOD_RUNES);
        event.register(LIVING_ARMOUR_DATA);

        event.register(IMPERFECT_RITUAL_CATALYST);
        event.register(SACRIFICE_LP_RATIO);
    }
}
