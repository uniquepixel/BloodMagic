package wayoftime.bloodmagic.datagen.content.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import wayoftime.bloodmagic.common.block.AlchemyTableBlock;
import wayoftime.bloodmagic.common.block.BMBlocks;
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
        addDropSelf(BMBlocks.DEMON_CRUCIBLE);
        addDropSelf(BMBlocks.DEMON_CRYSTALLIZER);
        addDropSelf(BMBlocks.DEMON_PYLON);
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
