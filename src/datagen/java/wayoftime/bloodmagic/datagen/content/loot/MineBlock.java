package wayoftime.bloodmagic.datagen.content.loot;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import wayoftime.bloodmagic.common.block.AlchemyTableBlock;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.util.TablePart;
import wayoftime.bloodmagic.util.blockitem.BlockWithItemHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MineBlock extends BlockLootSubProvider {
    public MineBlock(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
        BMBlocks.BASIC_BLOCKS.getEntries().forEach(holder -> dropSelfList.add(holder.get()));
        addDropSelf(BMBlocks.ARC_BLOCK); // TODO maybe let it keep fluids?
        addDropSelf(BMBlocks.BLOOD_ALTAR);
        addDropSelf(BMBlocks.HELLFIRE_FORGE);
        addDropSelf(BMBlocks.MASTER_RITUAL_STONE);
        addDropSelf(BMBlocks.TELEPOSER);
        addDropSelf(BMBlocks.INCENSE_ALTAR);
        addDropSelf(BMBlocks.ITEM_ROUTER);
        addDropSelf(BMBlocks.MASTER_ROUTING_NODE);
        addDropSelf(BMBlocks.INPUT_ROUTING_NODE);
        addDropSelf(BMBlocks.OUTPUT_ROUTING_NODE);
        addDropSelf(BMBlocks.DEMON_CRUCIBLE);
        addDropSelf(BMBlocks.DEMON_CRYSTALLIZER);
        addDropSelf(BMBlocks.DEMON_PYLON);

        // Demon Dungeon system - DUNGEON_CONTROLLER/DUNGEON_SEAL/SPECIAL_DUNGEON_SEAL are
        // structural/internal only (no BlockItem, see BMBlocks) so intentionally have no loot table.
        addDropSelf(BMBlocks.DUNGEON_STONE);
        addDropSelf(BMBlocks.DUNGEON_ORE);
        addDropSelf(BMBlocks.DUNGEON_BRICK_ASSORTED);
        addDropSelf(BMBlocks.DUNGEON_TILE_SPECIAL);

        // Demon Dungeon decorative block palette (see BMBlocks) - the plain single-texture cubes
        // (bricks, stone/tilespecial reskins, eye, polished, tile, smallbrick, metal, emitter, both
        // cracked bricks) are registered via BASIC_REG and already covered by the BASIC_BLOCKS loop
        // above. Pillars/stairs/walls/gates are BLOCK_REG (dropSelf, added below); slabs need the
        // special "double slab drops 2" table instead of a plain dropSelf, so they're kept in their
        // own list and handled in generate().
        addDropSelfFamilies(BMBlocks.DUNGEON_PILLAR_CENTER, BMBlocks.DUNGEON_PILLAR_SPECIAL, BMBlocks.DUNGEON_PILLAR_CAP,
                BMBlocks.DUNGEON_BRICK_STAIRS, BMBlocks.DUNGEON_POLISHED_STAIRS, BMBlocks.DUNGEON_STONE_STAIRS,
                BMBlocks.DUNGEON_BRICK_WALLS, BMBlocks.DUNGEON_TILE_WALLS, BMBlocks.DUNGEON_POLISHED_WALLS, BMBlocks.DUNGEON_STONE_WALLS,
                BMBlocks.DUNGEON_BRICK_GATES, BMBlocks.DUNGEON_POLISHED_GATES);
        addSlabFamilies(BMBlocks.DUNGEON_BRICK_SLABS, BMBlocks.DUNGEON_TILE_SLABS, BMBlocks.DUNGEON_STONE_SLABS, BMBlocks.DUNGEON_POLISHED_SLABS);

        // Dungeon puzzle/hazard blocks (Priority 3 flavor content, see BMBlocks) - DUNGEON_ALTERNATOR
        // is BASIC_REG (already covered by the BASIC_BLOCKS loop above); these two are BLOCK_REG.
        addDropSelf(BMBlocks.DUNGEON_SPIKE_TRAP);
        addDropSelf(BMBlocks.DUNGEON_SPIKES);

        // The Mimic (Priority 1 flavor content, see BMBlocks) - plain dropSelf matching upstream's
        // loot table exactly; TileMimic never actually drops its disguise/tag contents (see
        // TileMimic's class javadoc), so no component-copying table is needed here.
        addDropSelf(BMBlocks.MIMIC);
        addDropSelf(BMBlocks.ETHEREAL_MIMIC);

        // Demon crop blocks (see BMBlocks/NetherSoilBlock/GrowingDoubtBlock/TauBlock) - NETHER_SOIL
        // drops netherrack (matching vanilla farmland dropping dirt); the 3 crop blocks need custom
        // per-age tables, built in generate() below.
        cropBlockList.add(BMBlocks.NETHER_SOIL.block().get());
        cropBlockList.add(BMBlocks.GROWING_DOUBT.get());
        cropBlockList.add(BMBlocks.WEAK_TAU.get());
        cropBlockList.add(BMBlocks.STRONG_TAU.get());
    }

    private final List<Block> cropBlockList = new ArrayList<>();

    @SafeVarargs
    private final void addDropSelfFamilies(java.util.Map<String, ? extends BlockWithItemHolder<? extends Block, ? extends BlockItem>>... families) {
        for (java.util.Map<String, ? extends BlockWithItemHolder<? extends Block, ? extends BlockItem>> family : families) {
            for (BlockWithItemHolder<? extends Block, ? extends BlockItem> holder : family.values()) {
                dropSelfList.add(holder.block().get());
            }
        }
    }

    private final List<Block> dungeonSlabList = new ArrayList<>();

    @SafeVarargs
    private final void addSlabFamilies(java.util.Map<String, ? extends BlockWithItemHolder<? extends Block, ? extends BlockItem>>... families) {
        for (java.util.Map<String, ? extends BlockWithItemHolder<? extends Block, ? extends BlockItem>> family : families) {
            for (BlockWithItemHolder<? extends Block, ? extends BlockItem> holder : family.values()) {
                dungeonSlabList.add(holder.block().get());
            }
        }
    }

    private void addDropSelf(BlockWithItemHolder<? extends Block, ? extends BlockItem> toAdd) {
        dropSelfList.add(toAdd.block().get());
    }

    // Explosive Charges are never in the standard loot table - like BLOOD_LIGHT, they drop (or
    // don't) entirely through their own tile's playerWillDestroy -> dropSelf() override instead
    // (see ExplosiveChargeBlock/ExplosiveChargeTile), so a normal dropSelf loot entry here would
    // double the item.
    private final List<Block> chargeBlocks = List.of(BMBlocks.SHAPED_CHARGE.block().get(), BMBlocks.DEFORESTER_CHARGE.block().get(), BMBlocks.VEINMINE_CHARGE.block().get(), BMBlocks.FUNGAL_CHARGE.block().get());

    private final List<Block> specialDropList = List.of(BMBlocks.BLOOD_TANK.block().get(), BMBlocks.LIVING_STATION.block().get(), BMBlocks.ALCHEMY_TABLE.block().get(), BMBlocks.BLOOD_LIGHT.get());
    private List<Block> dropSelfList = new ArrayList<>();

    @Override
    protected Iterable<Block> getKnownBlocks() {
        List<Block> list = new ArrayList<>();
        list.addAll(specialDropList);
        list.addAll(dropSelfList);
        list.addAll(chargeBlocks);
        list.addAll(dungeonSlabList);
        list.addAll(cropBlockList);
        return list;
    }

    @Override
    protected void generate() {
        dropSelfList.forEach(this::dropSelf);
        copyComponents(BMBlocks.BLOOD_TANK);
        copyComponents(BMBlocks.LIVING_STATION);

        add(BMBlocks.ALCHEMY_TABLE.block().get(), block -> createSinglePropConditionTable(block, AlchemyTableBlock.PART, TablePart.LEFT));

        add(BMBlocks.BLOOD_LIGHT.get(), noDrop());
        chargeBlocks.forEach(block -> add(block, noDrop()));

        // Demon Dungeon decorative palette slabs need the double-slab-drops-2 table, not plain dropSelf.
        dungeonSlabList.forEach(block -> add(block, this::createSlabItemTable));

        // Demon crop blocks - ported 1:1 from 1.20.1's creeping_doubt.json/weak_tau.json/
        // strong_tau.json/nether_soil.json (see BMBlocks).
        add(BMBlocks.NETHER_SOIL.block().get(), block -> createSingleItemTable(Items.NETHERRACK));

        Block growingDoubt = BMBlocks.GROWING_DOUBT.get();
        Item growingDoubtSeed = BMItems.GROWING_DOUBT_SEED.get();
        add(growingDoubt, createCropDrops(growingDoubt, growingDoubtSeed, growingDoubtSeed, matureCondition(growingDoubt)));

        Block weakTau = BMBlocks.WEAK_TAU.get();
        Item weakTauItem = BMItems.WEAK_TAU_SEED.get();
        add(weakTau, createCropDrops(weakTau, weakTauItem, weakTauItem, matureCondition(weakTau)));

        // Strong Tau: breaking it immature yields weak Tau back (matching 1.20.1's 3-pool table -
        // see BMBlocks/TauBlock) rather than the plain createCropDrops shape above.
        Block strongTau = BMBlocks.STRONG_TAU.get();
        Item strongTauItem = BMItems.STRONG_TAU_SEED.get();
        LootItemCondition.Builder strongTauMature = matureCondition(strongTau);
        var enchantmentLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        add(strongTau, this.applyExplosionDecay(strongTau, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(weakTauItem))
                        .when(InvertedLootItemCondition.invert(strongTauMature)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(strongTauItem))
                        .when(strongTauMature))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(strongTauItem)
                                .apply(ApplyBonusCount.addBonusBinomialDistributionCount(enchantmentLookup.getOrThrow(Enchantments.FORTUNE), 0.5714286F, 3)))
                        .when(strongTauMature))
        ));
    }

    private LootItemCondition.Builder matureCondition(Block cropBlock) {
        return LootItemBlockStatePropertyCondition.hasBlockStateProperties(cropBlock)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, 7));
    }

    private void copyComponents(BlockWithItemHolder<? extends Block, ? extends BlockItem> holder) {
        add(
                holder.block().get(),
                LootTable.lootTable().withPool(
                        this.applyExplosionCondition(holder.block().get(), LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(holder)
                                        .apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY))
                                )
                        )
                )
        );
    }
}
