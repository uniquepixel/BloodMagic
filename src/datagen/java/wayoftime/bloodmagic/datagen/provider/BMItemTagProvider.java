package wayoftime.bloodmagic.datagen.provider;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.api.BMTags;

import java.util.concurrent.CompletableFuture;

public class BMItemTagProvider extends ItemTagsProvider {
    public BMItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags) {
        super(output, lookupProvider, blockTags, BloodMagic.MODID, null);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        copy(BMTags.Blocks.STORAGE_BLOCKS_HELLFORGED, BMTags.Items.STORAGE_BLOCKS_HELLFORGED);

        tag(BMTags.Items.LIVING_SET)
                .add(BMItems.LIVING_HELMET.get(), BMItems.LIVING_PLATE.get(), BMItems.LIVING_LEGGINGS.get(), BMItems.LIVING_BOOTS.get());

        tag(ItemTags.HEAD_ARMOR)
                .add(BMItems.LIVING_HELMET.get(), BMItems.SENTIENT_HELMET.get());
        tag(ItemTags.CHEST_ARMOR)
                .add(BMItems.LIVING_PLATE.get(), BMItems.SENTIENT_PLATE.get());
        tag(ItemTags.LEG_ARMOR)
                .add(BMItems.LIVING_LEGGINGS.get(), BMItems.SENTIENT_LEGGINGS.get());
        tag(ItemTags.FOOT_ARMOR)
                .add(BMItems.LIVING_BOOTS.get(), BMItems.SENTIENT_BOOTS.get());

        tag(BMTags.Items.LIVING_UPGRADE_SET)
                .addTag(BMTags.Items.LIVING_SET);

        tag(BMTags.Items.REVERTER)
                .add(BMItems.SANGUINE_REVERTER.get());
        tag(BMTags.Items.EXPLOSIVES)
                .add(BMItems.EXPLOSIVE_POWDER.get(), BMItems.PRIMITIVE_EXPLOSIVE_CELL.get(), BMItems.HELLFORGED_EXPLOSIVE_CELL.get());
        tag(BMTags.Items.RESONATOR)
                .add(BMItems.RESONATOR.get(), BMItems.PRIMITIVE_CRYSTALLINE_RESONATOR.get(), BMItems.HELLFORGED_RESONATOR.get());
        tag(BMTags.Items.CUTTING_FLUIDS)
                .add(BMItems.BASIC_CUTTING_FLUID.get(), BMItems.INTERMEDIATE_CUTTING_FLUID.get(), BMItems.ADVANCED_CUTTING_FLUID.get());
        tag(BMTags.Items.HYDRATION)
                .add(BMItems.PRIMITIVE_HYDRATION_CELL.get());

        tag(BMTags.Items.ARC_BLASTING);
        tag(BMTags.Items.ARC_SMELTING)
                .add(BMItems.PRIMITIVE_FURNACE_CELL.get(), BMItems.LAVA_CRYSTAL.get());
        tag(BMTags.Items.ARC_SMOKING);

        tag(BMTags.Items.ARC_FURNACE)
                .addTag(BMTags.Items.ARC_BLASTING)
                .addTag(BMTags.Items.ARC_SMELTING)
                .addTag(BMTags.Items.ARC_SMOKING);

        tag(BMTags.Items.ARC_TOOL)
                .addTag(BMTags.Items.REVERTER)
                .addTag(BMTags.Items.EXPLOSIVES)
                .addTag(BMTags.Items.RESONATOR)
                .addTag(BMTags.Items.CUTTING_FLUIDS)
                .addTag(BMTags.Items.HYDRATION)
                .addTag(BMTags.Items.ARC_FURNACE);

        // Ported from 1.20.1's BloodMagicTags.CHARGES - see its javadoc in BMTags for why the
        // block-drop loot modifiers need to know a charge item when they see one.
        tag(BMTags.Items.CHARGES)
                .add(BMBlocks.SHAPED_CHARGE.item().get(), BMBlocks.DEFORESTER_CHARGE.item().get(), BMBlocks.VEINMINE_CHARGE.item().get(), BMBlocks.FUNGAL_CHARGE.item().get());
    }
}
