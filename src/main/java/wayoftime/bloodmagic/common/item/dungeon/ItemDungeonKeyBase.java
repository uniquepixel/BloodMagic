package wayoftime.bloodmagic.common.item.dungeon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * Port of 1.20.1's {@code wayoftime.bloodmagic.common.item.dungeon.ItemDungeonKeyBase}. Upstream
 * extended a mod-wide {@code ItemBase(String desc)} convenience class that doesn't exist on this
 * branch (items here just take {@link net.minecraft.world.item.Item.Properties} directly, see
 * {@code BMItems}) - extends {@link Item} directly instead, otherwise unchanged.
 */
public abstract class ItemDungeonKeyBase extends Item implements IDungeonKey {
    public ItemDungeonKeyBase(Properties properties) {
        super(properties);
    }

    @Override
    public abstract ResourceLocation getValidResourceLocation(List<ResourceLocation> list);
}
