package wayoftime.bloodmagic.common.item.dungeon;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.common.item.dungeon.ItemDungeonKey}:
 * matches a door's potential room-pool ids by substring against a fixed set of keywords (e.g. a
 * "simple key" matching any pool whose id contains "tier1" or "standard"), picking randomly among
 * matches. See {@code BMItems} for the concrete key items registered with this class.
 */
public class ItemDungeonKey extends ItemDungeonKeyBase {
    private final String[] resourceKeys;

    public ItemDungeonKey(Properties properties, String... resourceKeys) {
        super(properties);
        this.resourceKeys = resourceKeys;
    }

    @Override
    public ResourceLocation getValidResourceLocation(List<ResourceLocation> list) {
        List<ResourceLocation> subList = new ArrayList<>();
        for (ResourceLocation testLocation : list) {
            for (String key : resourceKeys) {
                if (testLocation.toString().contains(key)) {
                    subList.add(testLocation);
                }
            }
        }

        if (subList.isEmpty()) {
            return null;
        }

        Collections.shuffle(subList);
        return subList.get(0);
    }
}
