package wayoftime.bloodmagic.client.render.alchemyarray;

import net.minecraft.resources.ResourceLocation;
import wayoftime.bloodmagic.BloodMagic;

/**
 * Ported from 1.20.1's {@code LowStaticAlchemyCircleRenderer}: a low, non-spinning quad (rotation
 * fixed at 0) with a secondary rotation that ramps briefly then holds - used for Spike and
 * Bounce's arrays.
 */
public class LowStaticAlchemyCircleRenderer extends LowAlchemyCircleRenderer {
    public LowStaticAlchemyCircleRenderer() {
        this(BloodMagic.rl("textures/models/alchemyarrays/skeletonturret1.png"));
    }

    public LowStaticAlchemyCircleRenderer(ResourceLocation arrayResource) {
        super(arrayResource);
    }

    @Override
    public float getRotation(float craftTime) {
        return 0;
    }

    @Override
    public float getSecondaryRotation(float craftTime) {
        float offset = 2;
        float duration = 180;
        if (craftTime >= offset && craftTime < offset + duration) {
            return (craftTime - offset) * 2f;
        }
        return 0;
    }
}
