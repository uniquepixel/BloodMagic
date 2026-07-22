package wayoftime.bloodmagic.client.render.alchemyarray;

import net.minecraft.resources.ResourceLocation;
import wayoftime.bloodmagic.BloodMagic;

/**
 * Ported from 1.20.1's {@code LowAlchemyCircleRenderer}: identical to
 * {@link SingleAlchemyCircleRenderer} except the quad stays pinned to its lowest vertical offset
 * (-0.4, i.e. just below the block's top face) instead of rising as {@code craftTime} increases.
 */
public class LowAlchemyCircleRenderer extends SingleAlchemyCircleRenderer {
    public LowAlchemyCircleRenderer() {
        this(BloodMagic.rl("textures/models/alchemyarrays/skeletonturret1.png"));
    }

    public LowAlchemyCircleRenderer(ResourceLocation arrayResource) {
        super(arrayResource);
    }

    @Override
    public float getVerticalOffset(float craftTime) {
        return -0.4f;
    }
}
