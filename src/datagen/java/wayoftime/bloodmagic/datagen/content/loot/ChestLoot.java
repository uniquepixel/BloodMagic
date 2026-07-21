package wayoftime.bloodmagic.datagen.content.loot;

import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.common.loot.BMLootTables;

import java.util.function.BiConsumer;

public class ChestLoot implements LootTableSubProvider {
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(BMLootTables.DEMON_VAULT, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(2, 4))
                        .add(LootItem.lootTableItem(BMItems.RAW_WILL.get()))
                        .add(LootItem.lootTableItem(BMItems.SOUL_GEM_LESSER.get()))
                        .add(LootItem.lootTableItem(BMItems.SOUL_GEM_COMMON.get()))
                        .add(LootItem.lootTableItem(BMItems.ORB_APPRENTICE.get()))
                        .add(LootItem.lootTableItem(BMItems.SLATE_REINFORCED.get()))
                        .add(LootItem.lootTableItem(BMItems.EXPERIENCE_BOOK.get()))
                )
        );
    }
}
