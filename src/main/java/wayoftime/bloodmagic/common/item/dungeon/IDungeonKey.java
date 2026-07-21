package wayoftime.bloodmagic.common.item.dungeon;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.common.item.dungeon.IDungeonKey}.
 * Implemented by whatever item type can be used at a {@code TileDungeonSeal} - given the list of
 * room-pool ids that door is willing to connect to, returns which one (if any) this key is valid
 * for, or {@code null} if the key doesn't match any of them.
 */
public interface IDungeonKey {
    ResourceLocation getValidResourceLocation(List<ResourceLocation> list);
}
