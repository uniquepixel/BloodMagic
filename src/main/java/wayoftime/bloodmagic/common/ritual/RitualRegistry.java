package wayoftime.bloodmagic.common.ritual;

import net.minecraft.resources.ResourceLocation;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.common.ritual.types.AnimalGrowthRitual;
import wayoftime.bloodmagic.common.ritual.types.ArmourEvolveRitual;
import wayoftime.bloodmagic.common.ritual.types.CondorRitual;
import wayoftime.bloodmagic.common.ritual.types.CraftingRitual;
import wayoftime.bloodmagic.common.ritual.types.CrushingRitual;
import wayoftime.bloodmagic.common.ritual.types.CrystalHarvestRitual;
import wayoftime.bloodmagic.common.ritual.types.CrystalSplitRitual;
import wayoftime.bloodmagic.common.ritual.types.EllipsoidRitual;
import wayoftime.bloodmagic.common.ritual.types.FeatheredKnifeRitual;
import wayoftime.bloodmagic.common.ritual.types.FellingRitual;
import wayoftime.bloodmagic.common.ritual.types.ForsakenSoulRitual;
import wayoftime.bloodmagic.common.ritual.types.FullSpringRitual;
import wayoftime.bloodmagic.common.ritual.types.FullStomachRitual;
import wayoftime.bloodmagic.common.ritual.types.GeodeRitual;
import wayoftime.bloodmagic.common.ritual.types.GreenGroveRitual;
import wayoftime.bloodmagic.common.ritual.types.GroundingRitual;
import wayoftime.bloodmagic.common.ritual.types.HarvestRitual;
import wayoftime.bloodmagic.common.ritual.types.JumpingRitual;
import wayoftime.bloodmagic.common.ritual.types.LavaRitual;
import wayoftime.bloodmagic.common.ritual.types.LivingDowngradeRitual;
import wayoftime.bloodmagic.common.ritual.types.MagnetismRitual;
import wayoftime.bloodmagic.common.ritual.types.MeteorRitual;
import wayoftime.bloodmagic.common.ritual.types.PlacerRitual;
import wayoftime.bloodmagic.common.ritual.types.RegenerationRitual;
import wayoftime.bloodmagic.common.ritual.types.SphereCreateRitual;
import wayoftime.bloodmagic.common.ritual.types.SpeedRitual;
import wayoftime.bloodmagic.common.ritual.types.UpgradeRemoveRitual;
import wayoftime.bloodmagic.common.ritual.types.VaultRitual;
import wayoftime.bloodmagic.common.ritual.types.WellOfSufferingRitual;
import wayoftime.bloodmagic.common.ritual.types.YawningVoidRitual;
import wayoftime.bloodmagic.common.ritual.types.ZephyrRitual;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RitualRegistry {
    private static final Map<ResourceLocation, Ritual> RITUALS = new LinkedHashMap<>();

    public static void bootstrap() {
        register(new FullSpringRitual());
        register(new RegenerationRitual());
        register(new SpeedRitual());
        register(new LavaRitual());
        register(new MagnetismRitual());
        register(new JumpingRitual());
        register(new FellingRitual());
        register(new GreenGroveRitual());
        register(new FullStomachRitual());
        register(new ZephyrRitual());
        register(new YawningVoidRitual());
        register(new GroundingRitual());
        register(new CondorRitual());
        register(new AnimalGrowthRitual());
        register(new WellOfSufferingRitual());
        register(new FeatheredKnifeRitual());
        register(new SphereCreateRitual());
        register(new HarvestRitual());
        register(new MeteorRitual());
        register(new VaultRitual());
        register(new ArmourEvolveRitual());
        register(new CrystalHarvestRitual());
        register(new PlacerRitual());
        register(new ForsakenSoulRitual());
        register(new UpgradeRemoveRitual());
        register(new CrystalSplitRitual());
        register(new EllipsoidRitual());
        register(new GeodeRitual());
        register(new CrushingRitual());
        register(new LivingDowngradeRitual());
        register(new CraftingRitual());
    }

    private static void register(Ritual ritual) {
        RITUALS.put(ritual.getId(), ritual);
    }

    public static Ritual get(ResourceLocation id) {
        return RITUALS.get(id);
    }

    public static List<Ritual> all() {
        return List.copyOf(RITUALS.values());
    }

    public static ResourceLocation nextAfter(ResourceLocation current) {
        List<ResourceLocation> ids = List.copyOf(RITUALS.keySet());
        if (ids.isEmpty()) {
            return null;
        }
        int index = ids.indexOf(current);
        return ids.get((index + 1) % ids.size());
    }

    public static ResourceLocation rl(String path) {
        return BloodMagic.rl(path);
    }
}
