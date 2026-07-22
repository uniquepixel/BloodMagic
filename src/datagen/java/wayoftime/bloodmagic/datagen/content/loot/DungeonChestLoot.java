package wayoftime.bloodmagic.datagen.content.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction.Builder;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.common.loot.BMLootTables;

import java.util.function.BiConsumer;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.common.data.GeneratorLootTable}'s
 * {@code BMLootTables} sub-provider - the 25 room-themed dungeon chest loot tables (12
 * {@code simple_dungeon/*}, 8 {@code standard_dungeon/*}, 5 {@code mines/*}) that {@link
 * wayoftime.bloodmagic.structures.DungeonChestLootProcessor} attaches to placed chests. See {@link
 * BMLootTables} for the id list and {@link wayoftime.bloodmagic.structures.DungeonChestLootProcessor}'s
 * class javadoc for how the room-type -&gt; table mapping is actually determined (baked directly into
 * each room's structure NBT, not by any Java-side room "type" field).
 * <p>
 * Item substitutions from 1.20.1 (this branch doesn't have every item 1.20.1's loot tables
 * referenced yet - each substitution below picks the closest already-ported equivalent rather than
 * silently dropping the entry):
 * <ul>
 * <li>{@code MONSTER_SOUL_RAW} (+ a will-amount NBT function) -&gt; {@link BMItems#RAW_WILL} (plain
 * item, no will-amount function - this branch has no equivalent of 1.20.1's {@code SetWillRange})</li>
 * <li>{@code PETTY_GEM} (+ a will-fraction NBT function) -&gt; {@link BMItems#SOUL_GEM_PETTY} (plain
 * item, same tier rename as {@code MONSTER_SOUL_RAW} -&gt; {@code RAW_WILL})</li>
 * <li>{@code LIVING_TOME} (+ a specific Living Armor upgrade-type/XP-range NBT function) -&gt; {@link
 * BMItems#UPGRADE_TOME} (plain item - this branch's Living Armor upgrade system has no per-upgrade
 * "tome" registry to key off of, unlike 1.20.1's {@code LivingArmorRegistrar})</li>
 * <li>{@code CORROSIVE_CRYSTAL}/{@code STEADFAST_CRYSTAL}/{@code VENGEFUL_CRYSTAL}/{@code
 * DESTRUCTIVE_CRYSTAL}/{@code RAW_CRYSTAL} -&gt; the matching {@link BMItems#CORROSIVE_CATALYST}
 * family (this branch never split the Will Crystal ore drop into 5 typed items; the 5 Will Catalysts
 * are the closest existing 5-way Will-type split)</li>
 * <li>{@code IRON_SAND}/{@code GOLD_SAND} -&gt; {@link BMItems#IRON_FRAGMENT}/{@link
 * BMItems#GOLD_FRAGMENT} (this branch's ore-processing chain has no "sand" tier)</li>
 * <li>{@code COAL_SAND} -&gt; vanilla {@code minecraft:coal}</li>
 * <li>{@code NETHERITE_SCRAP_SAND} -&gt; vanilla {@code minecraft:netherite_scrap}</li>
 * <li>{@code HELLFORGED_SAND} -&gt; {@link BMItems#HELLFORGED_INGOT} ({@code bloodmagic:ingot_hellforged}),
 * one of the new items a parallel work item is adding this round - it landed before this table needed
 * to reference it, so it's used directly rather than via a dynamic registry-id lookup</li>
 * <li>{@code SULFUR} -&gt; vanilla {@code minecraft:gunpowder}</li>
 * <li>{@code TAU_OIL} -&gt; {@link BMItems#SIMPLE_CATALYST}, {@code PLANT_OIL} -&gt; {@link
 * BMItems#WEAK_FILLING_AGENT} (no oil-flavored items exist yet; picked two otherwise-unused Alchemy
 * Table reagents rather than triple-stacking the Tau seed items already in the same pools)</li>
 * <li>{@code STRENGTHENED_CATALYST} -&gt; {@link BMItems#CYCLING_CATALYST} (otherwise-unused
 * catalyst item)</li>
 * <li>{@code BLEEDING_EDGE_MUSIC} -&gt; vanilla {@code minecraft:music_disc_pigstep} (no BM music
 * discs exist on this branch)</li>
 * </ul>
 * Everything else (anointments across all three potency tiers, Sentient tools, Will Catalysts,
 * fragments, ARC tools, dungeon keys, {@code HELLFORGED_PARTS}, etc.) already exists under this
 * branch's registry ids and is ported 1:1.
 */
public class DungeonChestLoot implements LootTableSubProvider {
    private final HolderLookup.Provider registries;

    public DungeonChestLoot(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> acceptor) {
        Item[] baseAnointments = new Item[]{BMItems.ANOINTMENT_BOW_POWER.get(),
                BMItems.ANOINTMENT_FORTUNE.get(), BMItems.ANOINTMENT_HIDDEN_KNOWLEDGE.get(),
                BMItems.ANOINTMENT_HOLY_WATER.get(), BMItems.ANOINTMENT_LOOTING.get(),
                BMItems.ANOINTMENT_MELEE_DAMAGE.get(), BMItems.ANOINTMENT_QUICK_DRAW.get(),
                BMItems.ANOINTMENT_SILK_TOUCH.get(), BMItems.ANOINTMENT_SMELTING.get()};

        // 1.20.1's "_2"/empowered tier -> this branch's "_L" tier
        Item[] empoweredAnointments = new Item[]{BMItems.ANOINTMENT_BOW_POWER_L.get(),
                BMItems.ANOINTMENT_FORTUNE_L.get(), BMItems.ANOINTMENT_HIDDEN_KNOWLEDGE_L.get(),
                BMItems.ANOINTMENT_HOLY_WATER_L.get(), BMItems.ANOINTMENT_LOOTING_L.get(),
                BMItems.ANOINTMENT_MELEE_DAMAGE_L.get(), BMItems.ANOINTMENT_QUICK_DRAW_L.get(),
                BMItems.ANOINTMENT_SILK_TOUCH_L.get(), BMItems.ANOINTMENT_SMELTING_L.get()};

        Item[] empoweredWeaponAnointments = new Item[]{BMItems.ANOINTMENT_BOW_POWER_L.get(),
                BMItems.ANOINTMENT_HOLY_WATER_L.get(), BMItems.ANOINTMENT_LOOTING_L.get(),
                BMItems.ANOINTMENT_MELEE_DAMAGE_L.get(), BMItems.ANOINTMENT_QUICK_DRAW_L.get(),
                BMItems.ANOINTMENT_SMELTING_L.get(), BMItems.ANOINTMENT_BOW_VELOCITY_L.get()};

        // 1.20.1's "_3"/long tier -> this branch's "_XL" tier
        Item[] empoweredAnointments3 = new Item[]{BMItems.ANOINTMENT_BOW_POWER_XL.get(),
                BMItems.ANOINTMENT_FORTUNE_XL.get(), BMItems.ANOINTMENT_HIDDEN_KNOWLEDGE_XL.get(),
                BMItems.ANOINTMENT_HOLY_WATER_XL.get(), BMItems.ANOINTMENT_LOOTING_XL.get(),
                BMItems.ANOINTMENT_MELEE_DAMAGE_XL.get(), BMItems.ANOINTMENT_QUICK_DRAW_XL.get(),
                BMItems.ANOINTMENT_SILK_TOUCH_XL.get(), BMItems.ANOINTMENT_SMELTING_XL.get()};

        Item[] empoweredWeaponAnointments3 = new Item[]{BMItems.ANOINTMENT_BOW_POWER_XL.get(),
                BMItems.ANOINTMENT_HOLY_WATER_XL.get(), BMItems.ANOINTMENT_LOOTING_XL.get(),
                BMItems.ANOINTMENT_MELEE_DAMAGE_XL.get(), BMItems.ANOINTMENT_QUICK_DRAW_XL.get(),
                BMItems.ANOINTMENT_SMELTING_XL.get(), BMItems.ANOINTMENT_BOW_VELOCITY_XL.get()};

        // Identical content to empoweredAnointments3 in 1.20.1 too (a pre-existing upstream duplicate
        // array), kept as a separate reference only to mirror the original call sites 1:1.
        Item[] longAnointments3 = empoweredAnointments3;

        // ================= simple_dungeon/* =================
        // NOTE: entrance_chest/library/simple_blacksmith/food/farm_tools/farm_parts/bastion/nether/crypt
        // (9 of the 12 simple_dungeon tables) are NOT generated here - each nests a *vanilla* built-in
        // loot table (chests/simple_dungeon, chests/stronghold_library, etc.) as one of its pools, and
        // net.minecraft.data.loot.LootTableProvider's self-validation only resolves references within
        // the current provider's own generated batch - it has no access to vanilla's real loot table
        // registry, so any such reference unconditionally fails datagen's "Unknown loot table called"
        // check even though it resolves perfectly fine in the actual running game (vanilla's real
        // LootDataManager does have every built-in table loaded). This is a known NeoForge/vanilla
        // datagen limitation, not a defect in this port. Those 9 tables are instead hand-authored as
        // static JSON directly under data/bloodmagic/loot_table/chests/simple_dungeon/ - full content
        // parity with 1.20.1, just outside this generator. See DungeonChestLootProcessor's javadoc.

        LootPool.Builder potionChest = LootPool.lootPool().setRolls(UniformGenerator.between(5, 7))
                .add(LootItem.lootTableItem(Items.NETHER_WART).setWeight(40).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 7))))
                .add(LootItem.lootTableItem(Items.BLAZE_POWDER).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(7, 10))))
                .add(LootItem.lootTableItem(Items.BLAZE_POWDER).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 7))))
                .add(LootItem.lootTableItem(Items.POTION).setWeight(3).apply(SetPotionFunction.setPotion(Potions.WATER)))
                .add(LootItem.lootTableItem(Items.SLIME_BALL).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(10, 15))))
                .add(LootItem.lootTableItem(Items.MAGMA_CREAM).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(6, 10))))
                .add(LootItem.lootTableItem(Items.GUNPOWDER).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(6, 10))))
                .add(LootItem.lootTableItem(Items.REDSTONE).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(10, 20))))
                .add(LootItem.lootTableItem(Items.GLOWSTONE_DUST).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(20, 30))));
        potionChest = addMultipleItemsWithSameParams(potionChest, baseAnointments, 1, UniformGenerator.between(1, 3));

        LootPool.Builder armoryPool = LootPool.lootPool().setRolls(UniformGenerator.between(5, 7));
        armoryPool.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(25).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(7, 15))));
        armoryPool.add(LootItem.lootTableItem(Items.IRON_NUGGET).setWeight(20).setQuality(-4).apply(SetItemCountFunction.setCount(UniformGenerator.between(30, 50))));
        armoryPool.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(4).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        armoryPool.add(LootItem.lootTableItem(Items.LEATHER).setWeight(18).setQuality(-4).apply(SetItemCountFunction.setCount(UniformGenerator.between(10, 20))));
        armoryPool.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(8).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 7))));

        addMultipleItemsWithQualitySameParams(armoryPool, new Item[]{Items.LEATHER_BOOTS,
                Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET,
                Items.LEATHER_LEGGINGS}, 4, -3, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(15, 25)), SetItemDamageFunction.setDamage(UniformGenerator.between(0.3F, 0.9F)));
        addMultipleItemsWithSameParams(armoryPool, new Item[]{Items.LEATHER_BOOTS, Items.LEATHER_CHESTPLATE,
                Items.LEATHER_HELMET, Items.LEATHER_LEGGINGS}, 7, ConstantValue.exactly(1), SetItemDamageFunction.setDamage(UniformGenerator.between(0.8F, 1.0F)));
        addMultipleItemsWithSameParams(armoryPool, new Item[]{Items.IRON_BOOTS, Items.IRON_CHESTPLATE,
                Items.IRON_HELMET, Items.IRON_LEGGINGS}, 6, ConstantValue.exactly(1), SetItemDamageFunction.setDamage(UniformGenerator.between(0.2F, 0.5F)));
        addMultipleItemsWithQualitySameParams(armoryPool, new Item[]{Items.IRON_BOOTS, Items.IRON_CHESTPLATE,
                Items.IRON_LEGGINGS, Items.IRON_HELMET}, 4, -2, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(10, 20)), SetItemDamageFunction.setDamage(UniformGenerator.between(0.9F, 1.0F)));
        addMultipleItemsWithSameParams(armoryPool, new Item[]{Items.DIAMOND_BOOTS, Items.DIAMOND_CHESTPLATE,
                Items.DIAMOND_HELMET, Items.DIAMOND_LEGGINGS}, 2, ConstantValue.exactly(1), SetItemDamageFunction.setDamage(UniformGenerator.between(0.1F, 0.2F)));
        addMultipleItemsWithQualitySameParams(armoryPool, new Item[]{Items.DIAMOND_BOOTS,
                Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET,
                Items.DIAMOND_LEGGINGS}, 1, 2, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(20, 25)), SetItemDamageFunction.setDamage(UniformGenerator.between(0.4F, 1.0F)));

        LootPool.Builder potionIngredientsExtra = addMultipleItemsWithSameParams(LootPool.lootPool(), baseAnointments, 1, UniformGenerator.between(2, 4));
        acceptor.accept(BMLootTables.SIMPLE_DUNGEON_POTION_INGREDIENTS, LootTable.lootTable().withPool(potionChest).withPool(potionIngredientsExtra));
        acceptor.accept(BMLootTables.SIMPLE_DUNGEON_SIMPLE_ARMOURY, LootTable.lootTable().withPool(armoryPool));

        LootPool.Builder tartaricGemPool = LootPool.lootPool().setRolls(UniformGenerator.between(1, 2))
                .add(LootItem.lootTableItem(BMItems.SOUL_GEM_PETTY.get()).setWeight(5));
        LootPool.Builder tartaricSoulPool = LootPool.lootPool().setRolls(UniformGenerator.between(1, 2))
                .add(LootItem.lootTableItem(BMItems.RAW_WILL.get()).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));
        LootPool.Builder upgradePool = LootPool.lootPool().setRolls(UniformGenerator.between(1, 2))
                .add(LootItem.lootTableItem(BMItems.UPGRADE_TOME.get()).setWeight(3));
        acceptor.accept(BMLootTables.SIMPLE_DUNGEON_TEST_GEMS, LootTable.lootTable().withPool(tartaricGemPool).withPool(tartaricSoulPool).withPool(upgradePool));

        // ================= standard_dungeon/* =================

        LootPool.Builder decentLoot = LootPool.lootPool().setRolls(UniformGenerator.between(3, 6));
        decentLoot.add(LootItem.lootTableItem(Items.RAW_COPPER).setWeight(15).setQuality(-4).apply(SetItemCountFunction.setCount(UniformGenerator.between(8, 20))));
        decentLoot.add(LootItem.lootTableItem(Items.RAW_IRON).setWeight(20).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 8))));
        decentLoot.add(LootItem.lootTableItem(Items.RAW_GOLD).setWeight(25).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 7))));
        decentLoot.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(5).setQuality(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 6))));
        decentLoot.add(LootItem.lootTableItem(Items.EMERALD).setWeight(3).setQuality(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 8))));
        decentLoot.add(LootItem.lootTableItem(BMItems.STRONG_TAU_SEED.get()).setWeight(6).setQuality(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 6))));
        decentLoot = addMultipleItemsWithSameParams(decentLoot, empoweredAnointments, 1, UniformGenerator.between(2, 4));
        addMultipleItemsWithQualitySameParams(decentLoot, new Item[]{Items.DIAMOND_BOOTS,
                Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET,
                Items.DIAMOND_LEGGINGS}, 3, 2, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(20, 39)), SetItemDamageFunction.setDamage(UniformGenerator.between(0.7F, 1.0F)));
        addMultipleItemsWithQualitySameParams(decentLoot, new Item[]{Items.IRON_BOOTS, Items.IRON_CHESTPLATE,
                Items.IRON_HELMET, Items.IRON_LEGGINGS}, 4, 0, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(20, 39)), SetItemDamageFunction.setDamage(UniformGenerator.between(0.5F, 0.8F)));
        decentLoot.add(LootItem.lootTableItem(Items.NETHERITE_SCRAP).setWeight(4).setQuality(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))));
        decentLoot.add(LootItem.lootTableItem(BMItems.HELLFORGED_INGOT.get()).setWeight(3).setQuality(6).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));

        decentLoot.add(LootItem.lootTableItem(BMItems.RAW_WILL.get()).setWeight(8).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));
        decentLoot = addMultipleItemsWithSameParams(decentLoot, new Item[]{
                BMItems.CORROSIVE_CATALYST.get(), BMItems.STEADFAST_CATALYST.get(),
                BMItems.VENGEFUL_CATALYST.get(), BMItems.DESTRUCTIVE_CATALYST.get(),
                BMItems.RAW_CATALYST.get()}, 2, UniformGenerator.between(2, 5));
        decentLoot.add(LootItem.lootTableItem(BMItems.SYNTHETIC_POINT.get()).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(7, 12))));

        acceptor.accept(BMLootTables.STANDARD_DUNGEON_DECENT_LOOT, LootTable.lootTable().withPool(decentLoot));
        acceptor.accept(BMLootTables.STANDARD_DUNGEON_GREAT_LOOT, LootTable.lootTable().withPool(decentLoot).withPool(decentLoot));

        LootPool.Builder enchantingLoot = LootPool.lootPool().setRolls(UniformGenerator.between(3, 6));
        enchantingLoot.add(LootItem.lootTableItem(Items.PAPER).setWeight(30).setQuality(-4).apply(SetItemCountFunction.setCount(UniformGenerator.between(10, 24))));
        enchantingLoot.add(LootItem.lootTableItem(Items.BOOK).setWeight(15).setQuality(-2).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 10))));
        enchantingLoot.add(LootItem.lootTableItem(Items.BOOK).setWeight(12).setQuality(1).apply(enchantWithLevels(ConstantValue.exactly(30.0F))));
        enchantingLoot.add(LootItem.lootTableItem(Items.LAPIS_LAZULI).setWeight(18).setQuality(-2).apply(SetItemCountFunction.setCount(UniformGenerator.between(10, 22))));
        enchantingLoot.add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE).setWeight(10).setQuality(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))));
        addMultipleItemsWithQualitySameParams(enchantingLoot, new Item[]{Items.DIAMOND_BOOTS,
                Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET, Items.DIAMOND_LEGGINGS, Items.DIAMOND_PICKAXE,
                Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL,
                Items.DIAMOND_SWORD}, 1, 2, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(25, 39)));
        addMultipleItemsWithQualitySameParams(enchantingLoot, new Item[]{BMItems.SENTIENT_SWORD.get(),
                BMItems.SENTIENT_PICKAXE.get(), BMItems.SENTIENT_SCYTHE.get(),
                BMItems.SENTIENT_AXE.get(),
                BMItems.SENTIENT_SHOVEL.get()}, 1, 2, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(25, 39)));
        enchantingLoot.add(LootItem.lootTableItem(BMItems.UPGRADE_TOME.get()).setWeight(4));
        enchantingLoot.add(LootItem.lootTableItem(BMItems.WEAK_TAU_SEED.get()).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 7))));
        enchantingLoot.add(LootItem.lootTableItem(BMItems.STRONG_TAU_SEED.get()).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 7))));
        addMultipleItemsWithSameParams(enchantingLoot, new Item[]{BMItems.CORROSIVE_CATALYST.get(),
                BMItems.STEADFAST_CATALYST.get(), BMItems.VENGEFUL_CATALYST.get(),
                BMItems.DESTRUCTIVE_CATALYST.get(),
                BMItems.RAW_CATALYST.get()}, 1, UniformGenerator.between(2, 5));

        acceptor.accept(BMLootTables.STANDARD_DUNGEON_ENCHANTING_LOOT, LootTable.lootTable().withPool(enchantingLoot));

        LootPool.Builder poorLoot = LootPool.lootPool().setRolls(UniformGenerator.between(2, 4));
        poorLoot.add(LootItem.lootTableItem(BMItems.COPPER_FRAGMENT.get()).setWeight(25).setQuality(-4).apply(SetItemCountFunction.setCount(UniformGenerator.between(6, 15))));
        poorLoot.add(LootItem.lootTableItem(BMItems.IRON_FRAGMENT.get()).setWeight(20).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 8))));
        poorLoot.add(LootItem.lootTableItem(BMItems.GOLD_FRAGMENT.get()).setWeight(15).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 7))));
        poorLoot.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(3).setQuality(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))));
        poorLoot.add(LootItem.lootTableItem(Items.EMERALD).setWeight(1).setQuality(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 8))));
        poorLoot.add(LootItem.lootTableItem(Items.WHEAT).setWeight(8).setQuality(-2).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 8))));
        poorLoot.add(LootItem.lootTableItem(Items.FEATHER).setWeight(8).setQuality(-2).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 7))));
        poorLoot.add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(8).setQuality(-2).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 9))));
        poorLoot.add(LootItem.lootTableItem(Items.GUNPOWDER).setWeight(8).setQuality(-2).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 8))));
        poorLoot.add(LootItem.lootTableItem(Items.WARPED_STEM).setWeight(20).setQuality(-1).apply(SetItemCountFunction.setCount(UniformGenerator.between(8, 12))));
        poorLoot.add(LootItem.lootTableItem(Items.STICK).setWeight(15).setQuality(-1).apply(SetItemCountFunction.setCount(UniformGenerator.between(9, 15))));
        poorLoot.add(LootItem.lootTableItem(Items.SUGAR_CANE).setWeight(8).setQuality(-1).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 10))));
        poorLoot.add(LootItem.lootTableItem(Items.GUNPOWDER).setWeight(6).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 7))));
        poorLoot.add(LootItem.lootTableItem(BMItems.DUNGEON_SIMPLE_KEY.get()).setWeight(3).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));
        poorLoot.add(LootItem.lootTableItem(BMItems.STRONG_TAU_SEED.get()).setWeight(3).setQuality(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 6))));
        poorLoot.add(LootItem.lootTableItem(BMItems.WEAK_TAU_SEED.get()).setWeight(5).setQuality(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 6))));

        acceptor.accept(BMLootTables.STANDARD_DUNGEON_POOR_LOOT, LootTable.lootTable().withPool(poorLoot));

        LootPool.Builder potionsLoot = LootPool.lootPool().setRolls(UniformGenerator.between(3, 6));
        potionsLoot.add(LootItem.lootTableItem(BMItems.IRON_FRAGMENT.get()).setWeight(20).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 9))));
        potionsLoot.add(LootItem.lootTableItem(BMItems.GOLD_FRAGMENT.get()).setWeight(15).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 9))));
        potionsLoot.add(LootItem.lootTableItem(Items.COAL).setWeight(8).setQuality(-2).apply(SetItemCountFunction.setCount(UniformGenerator.between(7, 15))));
        potionsLoot.add(LootItem.lootTableItem(Items.REDSTONE).setWeight(10).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 9))));
        potionsLoot.add(LootItem.lootTableItem(Items.GLOWSTONE_DUST).setWeight(10).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 9))));
        potionsLoot.add(LootItem.lootTableItem(Items.GUNPOWDER).setWeight(8).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 9))));
        potionsLoot.add(LootItem.lootTableItem(Items.NETHER_WART).setWeight(10).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(6, 14))));
        potionsLoot.add(LootItem.lootTableItem(Items.GOLDEN_CARROT).setWeight(4).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        potionsLoot.add(LootItem.lootTableItem(Items.PHANTOM_MEMBRANE).setWeight(4).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        potionsLoot.add(LootItem.lootTableItem(Items.RABBIT_FOOT).setWeight(3).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        potionsLoot.add(LootItem.lootTableItem(BMItems.WEAK_FILLING_AGENT.get()).setWeight(6).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 9))));
        potionsLoot.add(LootItem.lootTableItem(BMItems.WEAK_TAU_SEED.get()).setWeight(8).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 9))));

        addMultipleItemsWithQualitySameParams(potionsLoot, new Item[]{Items.BLAZE_POWDER, Items.MAGMA_CREAM,
                Items.SLIME_BALL, Items.FERMENTED_SPIDER_EYE, Items.SPIDER_EYE, Items.SUGAR, Items.HONEYCOMB,
                Items.GLISTERING_MELON_SLICE}, 5, -1, UniformGenerator.between(2, 5));

        potionsLoot.add(LootItem.lootTableItem(BMItems.BASIC_CUTTING_FLUID.get()).setWeight(6).setQuality(4));
        potionsLoot.add(LootItem.lootTableItem(BMItems.MUNDANE_POWER_CATALYST.get()).setWeight(10).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));
        potionsLoot.add(LootItem.lootTableItem(BMItems.MUNDANE_LENGTHENING_CATALYST.get()).setWeight(10).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));
        potionsLoot.add(LootItem.lootTableItem(BMItems.COMBINATIONAL_CATALYST.get()).setWeight(7).setQuality(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));
        potionsLoot.add(LootItem.lootTableItem(BMItems.SIMPLE_CATALYST.get()).setWeight(10).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 7))));
        potionsLoot.add(LootItem.lootTableItem(Items.SPLASH_POTION).setWeight(5).apply(SetPotionFunction.setPotion(Potions.POISON)));

        acceptor.accept(BMLootTables.STANDARD_DUNGEON_DECENT_ALCHEMY, LootTable.lootTable().withPool(potionsLoot));

        LootPool.Builder strongPotionsLoot = LootPool.lootPool().setRolls(UniformGenerator.between(4, 6));
        strongPotionsLoot.add(LootItem.lootTableItem(BMItems.IRON_FRAGMENT.get()).setWeight(10).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 9))));
        strongPotionsLoot.add(LootItem.lootTableItem(BMItems.GOLD_FRAGMENT.get()).setWeight(8).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 9))));
        strongPotionsLoot.add(LootItem.lootTableItem(Items.COAL).setWeight(3).setQuality(-2).apply(SetItemCountFunction.setCount(UniformGenerator.between(7, 15))));
        strongPotionsLoot.add(LootItem.lootTableItem(Items.REDSTONE).setWeight(3).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 9))));
        strongPotionsLoot.add(LootItem.lootTableItem(Items.GLOWSTONE_DUST).setWeight(3).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 9))));
        strongPotionsLoot.add(LootItem.lootTableItem(Items.GUNPOWDER).setWeight(3).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 9))));
        strongPotionsLoot.add(LootItem.lootTableItem(Items.NETHER_WART).setWeight(4).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(6, 14))));
        strongPotionsLoot.add(LootItem.lootTableItem(Items.GOLDEN_CARROT).setWeight(2).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        strongPotionsLoot.add(LootItem.lootTableItem(Items.PHANTOM_MEMBRANE).setWeight(3).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        strongPotionsLoot.add(LootItem.lootTableItem(Items.RABBIT_FOOT).setWeight(2).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        strongPotionsLoot.add(LootItem.lootTableItem(Items.GHAST_TEAR).setWeight(5).setQuality(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        strongPotionsLoot.add(LootItem.lootTableItem(Items.GLOW_BERRIES).setWeight(5).setQuality(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        strongPotionsLoot.add(LootItem.lootTableItem(Items.AMETHYST_SHARD).setWeight(5).setQuality(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        strongPotionsLoot.add(LootItem.lootTableItem(BMItems.STRONG_TAU_SEED.get()).setWeight(8).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 9))));

        strongPotionsLoot.add(LootItem.lootTableItem(BMItems.MUNDANE_POWER_CATALYST.get()).setWeight(10).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        strongPotionsLoot.add(LootItem.lootTableItem(BMItems.MUNDANE_LENGTHENING_CATALYST.get()).setWeight(10).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        strongPotionsLoot.add(LootItem.lootTableItem(BMItems.COMBINATIONAL_CATALYST.get()).setWeight(4).setQuality(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        strongPotionsLoot.add(LootItem.lootTableItem(BMItems.AVERAGE_POWER_CATALYST.get()).setWeight(2).setQuality(5).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));
        strongPotionsLoot.add(LootItem.lootTableItem(BMItems.AVERAGE_LENGTHENING_CATALYST.get()).setWeight(2).setQuality(5).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));
        strongPotionsLoot.add(LootItem.lootTableItem(BMItems.CYCLING_CATALYST.get()).setWeight(6).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        strongPotionsLoot.add(LootItem.lootTableItem(BMItems.AVERAGE_FILLING_AGENT.get()).setWeight(4).setQuality(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        strongPotionsLoot.add(LootItem.lootTableItem(BMItems.INTERMEDIATE_CUTTING_FLUID.get()).setWeight(9).setQuality(4));
        strongPotionsLoot.add(LootItem.lootTableItem(Items.SPLASH_POTION).setWeight(3).apply(SetPotionFunction.setPotion(Potions.STRONG_HEALING)));
        strongPotionsLoot.add(LootItem.lootTableItem(Items.SPLASH_POTION).setWeight(5).apply(SetPotionFunction.setPotion(Potions.HEALING)));
        strongPotionsLoot.add(LootItem.lootTableItem(Items.SPLASH_POTION).setWeight(3).apply(SetPotionFunction.setPotion(Potions.STRONG_REGENERATION)));
        strongPotionsLoot.add(LootItem.lootTableItem(Items.SPLASH_POTION).setWeight(4).apply(SetPotionFunction.setPotion(Potions.LONG_REGENERATION)));

        addMultipleItemsWithQualitySameParams(strongPotionsLoot, new Item[]{
                BMItems.CORROSIVE_CATALYST.get(), BMItems.RAW_CATALYST.get(),
                BMItems.STEADFAST_CATALYST.get(), BMItems.VENGEFUL_CATALYST.get(),
                BMItems.DESTRUCTIVE_CATALYST.get()}, 2, 3, UniformGenerator.between(2, 5));

        acceptor.accept(BMLootTables.STANDARD_DUNGEON_STRONG_ALCHEMY, LootTable.lootTable().withPool(strongPotionsLoot));

        LootPool.Builder smithyLoot = LootPool.lootPool().setRolls(UniformGenerator.between(4, 7));
        smithyLoot.add(LootItem.lootTableItem(Items.RAW_COPPER).setWeight(15).setQuality(-4).apply(SetItemCountFunction.setCount(UniformGenerator.between(8, 20))));
        smithyLoot.add(LootItem.lootTableItem(Items.RAW_IRON).setWeight(20).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(8, 12))));
        smithyLoot.add(LootItem.lootTableItem(Items.RAW_GOLD).setWeight(16).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(5, 11))));
        smithyLoot.add(LootItem.lootTableItem(Items.COAL).setWeight(20).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(8, 14))));
        smithyLoot.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(7).setQuality(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 6))));
        smithyLoot.add(LootItem.lootTableItem(Items.EMERALD).setWeight(3).setQuality(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 8))));
        smithyLoot.add(LootItem.lootTableItem(Items.NETHERITE_SCRAP).setWeight(4).setQuality(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        smithyLoot.add(LootItem.lootTableItem(BMItems.HELLFORGED_INGOT.get()).setWeight(3).setQuality(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))));
        smithyLoot.add(LootItem.lootTableItem(BMItems.CORRUPTED_DUST.get()).setWeight(6).setQuality(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        smithyLoot.add(LootItem.lootTableItem(BMItems.STRONG_TAU_SEED.get()).setWeight(8).setQuality(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))));
        smithyLoot.add(LootItem.lootTableItem(BMItems.PRIMITIVE_EXPLOSIVE_CELL.get()).setWeight(10).setQuality(5));

        addMultipleItemsWithSameParams(smithyLoot, empoweredWeaponAnointments, 3, UniformGenerator.between(1, 4));
        smithyLoot.add(LootItem.lootTableItem(Items.IRON_NUGGET).setWeight(12).setQuality(-4).apply(SetItemCountFunction.setCount(UniformGenerator.between(20, 40))));
        smithyLoot.add(LootItem.lootTableItem(Items.LEATHER).setWeight(14).setQuality(-4).apply(SetItemCountFunction.setCount(UniformGenerator.between(10, 20))));
        smithyLoot.add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(6).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 7))));

        addMultipleItemsWithQualitySameParams(smithyLoot, new Item[]{Items.IRON_PICKAXE, Items.IRON_AXE,
                Items.IRON_SWORD, Items.IRON_SHOVEL, Items.IRON_HOE, Items.IRON_HELMET, Items.IRON_CHESTPLATE,
                Items.IRON_LEGGINGS,
                Items.IRON_BOOTS}, 3, -2, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(10, 20)), SetItemDamageFunction.setDamage(UniformGenerator.between(0.9F, 1.0F)));
        addMultipleItemsWithQualitySameParams(smithyLoot, new Item[]{Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE,
                Items.DIAMOND_SWORD, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.DIAMOND_HELMET,
                Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS,
                Items.DIAMOND_BOOTS}, 1, 2, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(20, 25)), SetItemDamageFunction.setDamage(UniformGenerator.between(0.7F, 0.9F)));

        smithyLoot.add(LootItem.lootTableItem(BMItems.UPGRADE_TOME.get()).setWeight(4));
        smithyLoot.add(LootItem.lootTableItem(BMItems.UPGRADE_TOME.get()).setWeight(4));
        addMultipleItemsWithQualitySameParams(smithyLoot, new Item[]{BMItems.CORROSIVE_CATALYST.get(),
                BMItems.RAW_CATALYST.get(), BMItems.STEADFAST_CATALYST.get(),
                BMItems.VENGEFUL_CATALYST.get(),
                BMItems.DESTRUCTIVE_CATALYST.get()}, 2, 3, UniformGenerator.between(1, 3));

        acceptor.accept(BMLootTables.STANDARD_DUNGEON_DECENT_SMITHY, LootTable.lootTable().withPool(smithyLoot));

        LootPool.Builder minesKeyLoot = LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(BMItems.DUNGEON_MINE_ENTRANCE_KEY.get()).setWeight(1).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));
        LootPool.Builder minesKeyExtraLoot = addMultipleItemsWithSameParams(LootPool.lootPool().setRolls(UniformGenerator.between(3, 6)), empoweredAnointments, 1, UniformGenerator.between(2, 4));

        acceptor.accept(BMLootTables.STANDARD_DUNGEON_MINES_KEY, LootTable.lootTable().withPool(minesKeyExtraLoot).withPool(minesKeyLoot));

        // ================= mines/* =================

        // Fragment loot table that can be a base for stronger loot
        LootPool.Builder fragmentLoot = LootPool.lootPool().setRolls(UniformGenerator.between(1, 2));
        fragmentLoot.add(LootItem.lootTableItem(BMItems.COPPER_FRAGMENT.get()).setWeight(25).setQuality(-4).apply(SetItemCountFunction.setCount(UniformGenerator.between(15, 22))));
        fragmentLoot.add(LootItem.lootTableItem(BMItems.IRON_FRAGMENT.get()).setWeight(20).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(8, 15))));
        fragmentLoot.add(LootItem.lootTableItem(BMItems.GOLD_FRAGMENT.get()).setWeight(15).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(8, 13))));
        fragmentLoot.add(LootItem.lootTableItem(BMItems.NETHERITE_SCRAP_FRAGMENT.get()).setWeight(3).setQuality(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));

        LootPool.Builder miningOreLoot = LootPool.lootPool().setRolls(UniformGenerator.between(3, 5));
        miningOreLoot.add(LootItem.lootTableItem(Items.RAW_COPPER).setWeight(15).setQuality(-4).apply(SetItemCountFunction.setCount(UniformGenerator.between(12, 26))));
        miningOreLoot.add(LootItem.lootTableItem(Items.RAW_IRON).setWeight(20).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(10, 15))));
        miningOreLoot.add(LootItem.lootTableItem(Items.RAW_GOLD).setWeight(16).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(7, 14))));
        miningOreLoot.add(LootItem.lootTableItem(Items.COAL).setWeight(20).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(10, 16))));
        miningOreLoot.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(7).setQuality(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 7))));
        miningOreLoot.add(LootItem.lootTableItem(Items.EMERALD).setWeight(4).setQuality(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 10))));
        miningOreLoot.add(LootItem.lootTableItem(Items.REDSTONE).setWeight(10).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(10, 15))));
        miningOreLoot.add(LootItem.lootTableItem(Items.GUNPOWDER).setWeight(4).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(6, 11))));
        miningOreLoot.add(LootItem.lootTableItem(Items.LAPIS_LAZULI).setWeight(10).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(10, 15))));
        miningOreLoot.add(LootItem.lootTableItem(BMItems.STRONG_TAU_SEED.get()).setWeight(6).setQuality(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 8))));
        miningOreLoot.add(LootItem.lootTableItem(BMItems.HELLFORGED_INGOT.get()).setWeight(12).setQuality(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(5, 7))));
        addMultipleItemsWithQualitySameParams(miningOreLoot, new Item[]{Items.IRON_PICKAXE,
                Items.IRON_SHOVEL}, 4, 1, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(20, 40)));
        addMultipleItemsWithQualitySameParams(miningOreLoot, new Item[]{Items.DIAMOND_PICKAXE,
                Items.DIAMOND_SHOVEL}, 3, 3, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(30, 40)));
        addMultipleItemsWithQualitySameParams(miningOreLoot, new Item[]{Items.NETHERITE_PICKAXE,
                Items.NETHERITE_SHOVEL,
                Items.NETHERITE_AXE}, 1, 6, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(30, 40)));

        addMultipleItemsWithQualitySameParams(miningOreLoot, new Item[]{BMItems.CORROSIVE_CATALYST.get(),
                BMItems.RAW_CATALYST.get(), BMItems.STEADFAST_CATALYST.get(),
                BMItems.VENGEFUL_CATALYST.get(),
                BMItems.DESTRUCTIVE_CATALYST.get()}, 2, 3, UniformGenerator.between(2, 5));
        miningOreLoot.add(LootItem.lootTableItem(BMItems.UPGRADE_TOME.get()).setWeight(4));
        addMultipleItemsWithQualitySameParams(miningOreLoot, empoweredWeaponAnointments, 3, 2, UniformGenerator.between(2, 5));
        miningOreLoot.add(LootItem.lootTableItem(BMItems.HELLFORGED_PARTS.get()).setWeight(1).setQuality(10).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));
        miningOreLoot.add(LootItem.lootTableItem(Items.MUSIC_DISC_PIGSTEP).setWeight(3).setQuality(5).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));

        acceptor.accept(BMLootTables.MINES_ORE_LOOT, LootTable.lootTable().withPool(fragmentLoot).withPool(miningOreLoot));

        LootPool.Builder minesSmithyLoot = LootPool.lootPool().setRolls(UniformGenerator.between(3, 5));
        minesSmithyLoot.add(LootItem.lootTableItem(Items.RAW_COPPER).setWeight(10).setQuality(-4).apply(SetItemCountFunction.setCount(UniformGenerator.between(12, 26))));
        minesSmithyLoot.add(LootItem.lootTableItem(Items.RAW_IRON).setWeight(16).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(10, 15))));
        minesSmithyLoot.add(LootItem.lootTableItem(Items.RAW_GOLD).setWeight(14).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(7, 14))));
        minesSmithyLoot.add(LootItem.lootTableItem(Items.COAL).setWeight(10).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(10, 16))));
        minesSmithyLoot.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(6).setQuality(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 7))));
        minesSmithyLoot.add(LootItem.lootTableItem(Items.ANCIENT_DEBRIS).setWeight(5).setQuality(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        minesSmithyLoot.add(LootItem.lootTableItem(BMItems.STRONG_TAU_SEED.get()).setWeight(4).setQuality(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 8))));
        minesSmithyLoot.add(LootItem.lootTableItem(BMItems.HELLFORGED_INGOT.get()).setWeight(12).setQuality(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(5, 7))));
        minesSmithyLoot.add(LootItem.lootTableItem(BMBlocks.DUNGEON_ORE).setWeight(8).setQuality(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 7))));

        addMultipleItemsWithQualitySameParams(minesSmithyLoot, new Item[]{Items.DIAMOND_PICKAXE,
                Items.DIAMOND_SHOVEL, Items.DIAMOND_AXE, Items.DIAMOND_HOE,
                Items.DIAMOND_SWORD}, 3, 3, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(30, 40)));
        addMultipleItemsWithQualitySameParams(minesSmithyLoot, new Item[]{Items.NETHERITE_PICKAXE,
                Items.NETHERITE_SHOVEL, Items.NETHERITE_AXE, Items.NETHERITE_SWORD,
                Items.NETHERITE_HOE}, 1, 6, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(30, 40)));
        addMultipleItemsWithQualitySameParams(minesSmithyLoot, new Item[]{Items.NETHERITE_HELMET,
                Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS,
                Items.NETHERITE_BOOTS}, 1, 6, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(25, 35)));

        addMultipleItemsWithQualitySameParams(minesSmithyLoot, new Item[]{BMItems.CORROSIVE_CATALYST.get(),
                BMItems.RAW_CATALYST.get(), BMItems.STEADFAST_CATALYST.get(),
                BMItems.VENGEFUL_CATALYST.get(),
                BMItems.DESTRUCTIVE_CATALYST.get()}, 2, 3, UniformGenerator.between(2, 5));

        minesSmithyLoot.add(LootItem.lootTableItem(BMItems.UPGRADE_TOME.get()).setWeight(4));
        minesSmithyLoot.add(LootItem.lootTableItem(BMItems.RAW_WILL.get()).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))));

        addMultipleItemsWithQualitySameParams(minesSmithyLoot, empoweredWeaponAnointments3, 2, 3, UniformGenerator.between(1, 3));
        minesSmithyLoot.add(LootItem.lootTableItem(Items.BOOK).setWeight(10).setQuality(3).apply(enchantWithLevels(UniformGenerator.between(25, 35))));

        minesSmithyLoot.add(LootItem.lootTableItem(BMItems.HELLFORGED_PARTS.get()).setWeight(2).setQuality(10).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));
        minesSmithyLoot.add(LootItem.lootTableItem(BMItems.PRIMITIVE_EXPLOSIVE_CELL.get()).setWeight(6).setQuality(3).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));
        minesSmithyLoot.add(LootItem.lootTableItem(BMItems.PRIMITIVE_CRYSTALLINE_RESONATOR.get()).setWeight(6).setQuality(3).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));
        minesSmithyLoot.add(LootItem.lootTableItem(BMItems.INTERMEDIATE_CUTTING_FLUID.get()).setWeight(6).setQuality(3).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));
        minesSmithyLoot.add(LootItem.lootTableItem(BMItems.HELLFORGED_EXPLOSIVE_CELL.get()).setWeight(4).setQuality(5).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))).apply(SetItemDamageFunction.setDamage(UniformGenerator.between(0.3F, 0.7F))));
        minesSmithyLoot.add(LootItem.lootTableItem(BMItems.HELLFORGED_RESONATOR.get()).setWeight(4).setQuality(5).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))).apply(SetItemDamageFunction.setDamage(UniformGenerator.between(0.3F, 0.7F))));
        minesSmithyLoot.add(LootItem.lootTableItem(BMItems.ADVANCED_CUTTING_FLUID.get()).setWeight(4).setQuality(5).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))).apply(SetItemDamageFunction.setDamage(UniformGenerator.between(0.3F, 0.7F))));
        minesSmithyLoot.add(LootItem.lootTableItem(Items.MUSIC_DISC_PIGSTEP).setWeight(3).setQuality(5).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));

        acceptor.accept(BMLootTables.MINES_SMITHY_LOOT, LootTable.lootTable().withPool(fragmentLoot).withPool(minesSmithyLoot));

        LootPool.Builder minesRawLoot = LootPool.lootPool().setRolls(UniformGenerator.between(1, 2));
        minesRawLoot.add(LootItem.lootTableItem(Items.RAW_COPPER).setWeight(20).setQuality(-4).apply(SetItemCountFunction.setCount(UniformGenerator.between(8, 20))));
        minesRawLoot.add(LootItem.lootTableItem(Items.RAW_IRON).setWeight(16).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 8))));
        minesRawLoot.add(LootItem.lootTableItem(Items.RAW_GOLD).setWeight(14).setQuality(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 7))));
        minesRawLoot.add(LootItem.lootTableItem(Items.NETHERITE_SCRAP).setWeight(3).setQuality(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        minesRawLoot.add(LootItem.lootTableItem(BMItems.CORRUPTED_DUST.get()).setWeight(8).setQuality(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));

        LootPool.Builder minesDecentLoot = LootPool.lootPool().setRolls(UniformGenerator.between(3, 5));
        minesDecentLoot.add(LootItem.lootTableItem(Items.COAL).setWeight(10).setQuality(-2).apply(SetItemCountFunction.setCount(UniformGenerator.between(12, 18))));
        minesDecentLoot.add(LootItem.lootTableItem(Items.AMETHYST_SHARD).setWeight(12).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(10, 17))));
        minesDecentLoot.add(LootItem.lootTableItem(Items.QUARTZ).setWeight(15).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(10, 17))));
        minesDecentLoot.add(LootItem.lootTableItem(Items.REDSTONE).setWeight(8).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(14, 20))));
        minesDecentLoot.add(LootItem.lootTableItem(Items.LAPIS_LAZULI).setWeight(8).setQuality(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(14, 20))));
        minesDecentLoot.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(5).setQuality(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 7))));
        minesDecentLoot.add(LootItem.lootTableItem(Items.EMERALD).setWeight(3).setQuality(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 8))));

        minesDecentLoot.add(LootItem.lootTableItem(BMItems.RAW_WILL.get()).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4))));
        minesDecentLoot.add(LootItem.lootTableItem(BMItems.STRONG_TAU_SEED.get()).setWeight(6).setQuality(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 6))));
        addMultipleItemsWithSameParams(minesDecentLoot, empoweredAnointments3, 2, UniformGenerator.between(2, 5));
        addMultipleItemsWithSameParams(minesDecentLoot, longAnointments3, 2, UniformGenerator.between(2, 5));

        addMultipleItemsWithQualitySameParams(minesDecentLoot, new Item[]{BMItems.CORROSIVE_CATALYST.get(),
                BMItems.RAW_CATALYST.get(), BMItems.STEADFAST_CATALYST.get(),
                BMItems.VENGEFUL_CATALYST.get(),
                BMItems.DESTRUCTIVE_CATALYST.get()}, 2, 3, UniformGenerator.between(2, 5));

        addMultipleItemsWithQualitySameParams(minesDecentLoot, new Item[]{Items.NETHERITE_PICKAXE,
                Items.NETHERITE_SHOVEL, Items.NETHERITE_AXE, Items.NETHERITE_SWORD,
                Items.NETHERITE_HOE}, 1, 6, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(30, 40)));
        addMultipleItemsWithQualitySameParams(minesDecentLoot, new Item[]{Items.NETHERITE_HELMET,
                Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS,
                Items.NETHERITE_BOOTS}, 1, 6, ConstantValue.exactly(1), enchantWithLevels(UniformGenerator.between(30, 35)));

        minesDecentLoot.add(LootItem.lootTableItem(BMItems.HELLFORGED_PARTS.get()).setWeight(6).setQuality(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));
        minesDecentLoot.add(LootItem.lootTableItem(BMItems.HELLFORGED_EXPLOSIVE_CELL.get()).setWeight(4).setQuality(5).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));
        minesDecentLoot.add(LootItem.lootTableItem(BMItems.HELLFORGED_RESONATOR.get()).setWeight(4).setQuality(5).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));
        minesDecentLoot.add(LootItem.lootTableItem(BMItems.ADVANCED_CUTTING_FLUID.get()).setWeight(4).setQuality(5).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));
        minesDecentLoot.add(LootItem.lootTableItem(Items.MUSIC_DISC_PIGSTEP).setWeight(3).setQuality(5).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));

        acceptor.accept(BMLootTables.MINES_DECENT_LOOT, LootTable.lootTable().withPool(minesRawLoot).withPool(minesDecentLoot));

        LootPool.Builder minesFoodLoot = LootPool.lootPool().setRolls(UniformGenerator.between(3, 5));
        addMultipleItemsWithSameParams(minesFoodLoot, new Item[]{Items.BEEF, Items.PORKCHOP, Items.CHICKEN,
                Items.EGG, Items.MUTTON, Items.RABBIT}, 10, UniformGenerator.between(5, 10));
        addMultipleItemsWithSameParams(minesFoodLoot, new Item[]{Items.FEATHER, Items.LEATHER, Items.WHITE_WOOL,
                Items.BLACK_WOOL, Items.RABBIT_HIDE}, 5, UniformGenerator.between(4, 15));
        addMultipleItemsWithSameParams(minesFoodLoot, new Item[]{Items.MELON_SEEDS, Items.PUMPKIN_SEEDS,
                Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS}, 5, UniformGenerator.between(5, 10));
        minesFoodLoot.add(LootItem.lootTableItem(Items.LEAD).setWeight(6).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))));
        minesFoodLoot.add(LootItem.lootTableItem(Items.RABBIT_FOOT).setWeight(2).setQuality(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));
        minesFoodLoot.add(LootItem.lootTableItem(Items.NAME_TAG).setWeight(3).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));
        minesFoodLoot.add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(6).apply(SetItemCountFunction.setCount(UniformGenerator.between(7, 11))));
        minesFoodLoot.add(LootItem.lootTableItem(Items.BONE).setWeight(6).apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 7))));
        minesFoodLoot.add(LootItem.lootTableItem(BMItems.WEAK_TAU_SEED.get()).setWeight(12).apply(SetItemCountFunction.setCount(UniformGenerator.between(6, 11))));
        minesFoodLoot.add(LootItem.lootTableItem(BMItems.STRONG_TAU_SEED.get()).setWeight(6).apply(SetItemCountFunction.setCount(UniformGenerator.between(6, 11))));
        minesFoodLoot.add(LootItem.lootTableItem(BMItems.WEAK_FILLING_AGENT.get()).setWeight(6).apply(SetItemCountFunction.setCount(UniformGenerator.between(6, 11))));
        minesFoodLoot.add(LootItem.lootTableItem(BMItems.ADVANCED_CUTTING_FLUID.get()).setWeight(3).setQuality(5).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));
        minesFoodLoot.add(LootItem.lootTableItem(BMItems.HELLFORGED_PARTS.get()).setWeight(3).setQuality(10).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))));

        acceptor.accept(BMLootTables.MINES_FOOD_LOOT, LootTable.lootTable().withPool(fragmentLoot).withPool(minesFoodLoot));

        LootPool.Builder mineKeyLoot = LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(BMItems.DUNGEON_MINE_KEY.get()).setWeight(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));

        acceptor.accept(BMLootTables.MINES_MINE_KEY_LOOT, LootTable.lootTable().withPool(minesRawLoot).withPool(minesDecentLoot).withPool(mineKeyLoot));
    }

    private Builder enchantWithLevels(NumberProvider levels) {
        return EnchantWithLevelsFunction.enchantWithLevels(registries, levels);
    }

    private static LootPool.Builder addMultipleItemsWithSameParams(LootPool.Builder pool, Item[] items, int basicWeight, NumberProvider basicRange, Builder... functions) {
        return addMultipleItemsWithQualitySameParams(pool, items, basicWeight, 0, basicRange, functions);
    }

    private static LootPool.Builder addMultipleItemsWithQualitySameParams(LootPool.Builder pool, Item[] items, int basicWeight, int quality, NumberProvider basicRange, Builder... functions) {
        if (basicWeight > 0) {
            for (Item item : items) {
                LootPoolSingletonContainer.Builder<?> entryBuilder = LootItem.lootTableItem(item).setWeight(basicWeight).setQuality(quality).apply(SetItemCountFunction.setCount(basicRange));
                for (Builder function : functions) {
                    entryBuilder = entryBuilder.apply(function);
                }

                pool = pool.add(entryBuilder);
            }
        }

        return pool;
    }
}
