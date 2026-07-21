package wayoftime.bloodmagic.client.render;

import net.minecraft.client.model.geom.ModelLayerLocation;
import wayoftime.bloodmagic.BloodMagic;

/**
 * Holds this branch's custom {@link ModelLayerLocation}s, ported from 1.20.1's
 * {@code BloodMagicModelLayerLocations}. Only the meteor entity model exists so far, so this doesn't
 * need to be a bigger registry-style class yet.
 */
public class BMModelLayers {
    public static final ModelLayerLocation METEOR = new ModelLayerLocation(BloodMagic.rl("entity/meteor"), "main");

    private BMModelLayers() {
    }
}
