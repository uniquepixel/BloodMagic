package wayoftime.bloodmagic.common.loot;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import wayoftime.bloodmagic.BloodMagic;

public class BMLootTables {
    public static final ResourceKey<LootTable> DEMON_VAULT = ResourceKey.create(Registries.LOOT_TABLE, BloodMagic.rl("chests/demon_vault"));
}
