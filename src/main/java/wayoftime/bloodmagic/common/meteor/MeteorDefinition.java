package wayoftime.bloodmagic.common.meteor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Replaces 1.20.1's {@code RecipeMeteor} - what a meteor payload actually is: the item that triggers
 * it, an LP syphon cost, an explosion radius, and the concentric {@link MeteorLayer}s it fills in on
 * impact. See {@link MeteorDefinitions} for why this is a fixed Java-defined list instead of a
 * datapack-driven recipe type like the original.
 * <p>
 * {@link #spawnMeteorInWorld(Level, BlockPos)} is a direct port of the original's method of the same
 * name: detonate (if the definition wants an explosion), then build every layer from smallest radius
 * to largest, each one leaving the previous (smaller) layer's sphere untouched so layers nest instead
 * of overwriting each other.
 */
public class MeteorDefinition {
    private final Ingredient input;
    private final int syphon;
    private final float explosionRadius;
    private final List<MeteorLayer> layerList;

    public MeteorDefinition(Ingredient input, int syphon, float explosionRadius, List<MeteorLayer> layerList) {
        this.input = input;
        this.syphon = syphon;
        this.explosionRadius = explosionRadius;
        this.layerList = layerList;
    }

    public boolean matches(ItemStack stack) {
        return input.test(stack);
    }

    public int getSyphon() {
        return syphon;
    }

    public void spawnMeteorInWorld(Level world, BlockPos centerPos) {
        if (explosionRadius > 0) {
            world.explode(null, centerPos.getX(), centerPos.getY(), centerPos.getZ(), explosionRadius, true, Level.ExplosionInteraction.TNT);
        }

        List<MeteorLayer> sortedLayers = new ArrayList<>(layerList);
        sortedLayers.sort(Comparator.comparingInt(layer -> layer.layerRadius));

        int prevRadius = -1;
        for (MeteorLayer layer : sortedLayers) {
            layer.buildLayer(world, centerPos, prevRadius);
            prevRadius = layer.layerRadius;
        }
    }
}
