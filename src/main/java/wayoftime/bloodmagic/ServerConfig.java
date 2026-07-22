package wayoftime.bloodmagic;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {

    public final ModConfigSpec.ConfigValue<Integer> SELF_SACRIFICE_CONVERSION;
    public final ModConfigSpec.ConfigValue<Integer> DEFAULT_UPGRADE_POINTS;
    public final ModConfigSpec.ConfigValue<Integer> EVOLVED_UPGRADE_POINTS;
    // Fallback LP-per-health ratio for wayoftime.bloodmagic.common.item.DaggerOfSacrificeItem when
    // an entity type has no wayoftime.bloodmagic.common.datamap.BMDataMaps#SACRIFICE_LP_RATIO entry
    // - ported from 1.20.1's ConfigManager.COMMON.entitySacrificeDefault (default 25).
    public final ModConfigSpec.ConfigValue<Integer> DEFAULT_ENTITY_SACRIFICE_RATIO;

    protected ServerConfig(ModConfigSpec.Builder builder) {
        SELF_SACRIFICE_CONVERSION = builder.define("self_sacrifice_conversion", 100);
        DEFAULT_UPGRADE_POINTS = builder.define("default_upgrade_points", 100);
        EVOLVED_UPGRADE_POINTS = builder.define("evolved_upgrade_points", 300);
        DEFAULT_ENTITY_SACRIFICE_RATIO = builder.defineInRange("default_entity_sacrifice_ratio", 25, 0, 10000);
    }
}
