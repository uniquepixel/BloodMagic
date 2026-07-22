package wayoftime.bloodmagic.datagen.content.datamap;

import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import wayoftime.bloodmagic.common.datamap.BMDataMaps;

import java.util.function.Function;

/**
 * Ported from 1.20.1's default {@code sacrificialValues} config list ("villager;100", "slime;15",
 * etc.) - see {@link BMDataMaps#SACRIFICE_LP_RATIO}'s javadoc for why this is a data map here
 * instead of a config list. The original's "dummmmmmy:target_dummy;0" entry (a debug-mod entity not
 * present on this branch) is omitted.
 */
public class SacrificeLpRatioData {
    public static void bootstrap(Function<DataMapType<EntityType<?>, Integer>, DataMapProvider.Builder<Integer, EntityType<?>>> setup) {
        setup.apply(BMDataMaps.SACRIFICE_LP_RATIO)
                .add(EntityType.VILLAGER.builtInRegistryHolder(), 100, false)
                .add(EntityType.SLIME.builtInRegistryHolder(), 15, false)
                .add(EntityType.ENDERMAN.builtInRegistryHolder(), 10, false)
                .add(EntityType.COW.builtInRegistryHolder(), 100, false)
                .add(EntityType.CHICKEN.builtInRegistryHolder(), 100, false)
                .add(EntityType.HORSE.builtInRegistryHolder(), 100, false)
                .add(EntityType.SHEEP.builtInRegistryHolder(), 100, false)
                .add(EntityType.WOLF.builtInRegistryHolder(), 100, false)
                .add(EntityType.OCELOT.builtInRegistryHolder(), 100, false)
                .add(EntityType.PIG.builtInRegistryHolder(), 100, false)
                .add(EntityType.RABBIT.builtInRegistryHolder(), 100, false)
                .build();
    }
}
