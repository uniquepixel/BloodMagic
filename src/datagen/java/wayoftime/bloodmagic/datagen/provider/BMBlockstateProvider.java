package wayoftime.bloodmagic.datagen.provider;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.block.ARCBlock;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;

public class BMBlockstateProvider extends BlockStateProvider {
    public BMBlockstateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, BloodMagic.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        BMBlocks.BASIC_BLOCKS.getEntries().forEach(block -> {
            simpleBlockWithItem(block.get(), cubeAll(block.get()));
        });

        simpleBlockWithItem(BMBlocks.MASTER_RITUAL_STONE.block().get(), cubeAll(BMBlocks.MASTER_RITUAL_STONE.block().get()));

        simpleBlock(BMBlocks.BLOOD_LIGHT.get(), cubeAll(BMBlocks.BLOOD_LIGHT.get()));

        simpleBlockWithItem(BMBlocks.TELEPOSER.block().get(), models().cubeBottomTop("teleposer",
                bm("block/teleposer_side"), bm("block/teleposer_side"), bm("block/teleposer_top")));

        simpleBlockWithItem(BMBlocks.INCENSE_ALTAR.block().get(), cubeAll(BMBlocks.INCENSE_ALTAR.block().get()));

        simpleBlockWithItem(BMBlocks.ITEM_ROUTER.block().get(), models().cubeAll("item_router", bm("block/hellforged_block")));

        simpleBlockWithItem(BMBlocks.MASTER_ROUTING_NODE.block().get(), models().cubeAll("master_routing_node", bm("block/hellforged_block")));
        simpleBlockWithItem(BMBlocks.INPUT_ROUTING_NODE.block().get(), models().cubeAll("input_routing_node", bm("block/hellforged_block")));
        simpleBlockWithItem(BMBlocks.OUTPUT_ROUTING_NODE.block().get(), models().cubeAll("output_routing_node", bm("block/hellforged_block")));

        // Living Station: new to this branch, no 1.20.1 art to port - see BMBlocks for why this
        // reuses the same placeholder cube texture as the other textureless machine blocks above.
        simpleBlockWithItem(BMBlocks.LIVING_STATION.block().get(), models().cubeAll("living_station", bm("block/hellforged_block")));

        // Demonic Will collection chain - no dedicated textures/models ported yet (1.20.1 used
        // custom .obj models for all three), so these reuse placeholder cube textures the same way
        // the routing nodes above do.
        simpleBlockWithItem(BMBlocks.DEMON_CRUCIBLE.block().get(), models().cubeAll("demon_crucible", bm("block/hellforged_block")));
        simpleBlockWithItem(BMBlocks.DEMON_CRYSTALLIZER.block().get(), models().cubeAll("demon_crystallizer", bm("block/hellforged_block")));
        simpleBlockWithItem(BMBlocks.DEMON_PYLON.block().get(), models().cubeAll("demon_pylon", bm("block/hellforged_block")));

        // Explosive Charge family - directional ATTACHED property, so (unlike the plain cubes
        // above) every Direction value needs its own variant entry; all 6 point at the same
        // placeholder cube model regardless of direction since there's no oriented texture yet.
        chargeBlock(BMBlocks.SHAPED_CHARGE.block().get(), "shaped_charge");
        chargeBlock(BMBlocks.DEFORESTER_CHARGE.block().get(), "deforester_charge");
        chargeBlock(BMBlocks.VEINMINE_CHARGE.block().get(), "veinmine_charge");
        chargeBlock(BMBlocks.FUNGAL_CHARGE.block().get(), "fungal_charge");

        // Demon Dungeon system - no dedicated textures ported yet (see BMBlocks for why only this
        // handful of dungeon blocks is registered this round), so these reuse the same placeholder
        // cube texture as the other textureless machine blocks above.
        simpleBlockWithItem(BMBlocks.DUNGEON_STONE.block().get(), models().cubeAll("dungeon_stone", bm("block/hellforged_block")));
        simpleBlockWithItem(BMBlocks.DUNGEON_ORE.block().get(), models().cubeAll("dungeon_ore", bm("block/hellforged_block")));
        simpleBlockWithItem(BMBlocks.DUNGEON_BRICK_ASSORTED.block().get(), models().cubeAll("dungeon_brick_assorted", bm("block/hellforged_block")));
        simpleBlockWithItem(BMBlocks.DUNGEON_TILE_SPECIAL.block().get(), models().cubeAll("dungeon_tilespecial", bm("block/hellforged_block")));
        simpleBlock(BMBlocks.DUNGEON_CONTROLLER.get(), models().cubeAll("dungeon_controller", bm("block/hellforged_block")));
        simpleBlock(BMBlocks.DUNGEON_SEAL.get(), models().cubeAll("dungeon_seal", bm("block/hellforged_block")));
        // SPECIAL_DUNGEON_SEAL has a SEAL enum property (standard/mine_entrance/mine_key) but no
        // per-variant art yet, so every value points at the same placeholder model.
        ModelFile specialSealModel = models().cubeAll("special_dungeon_seal", bm("block/hellforged_block"));
        VariantBlockStateBuilder specialSealBuilder = getVariantBuilder(BMBlocks.SPECIAL_DUNGEON_SEAL.get());
        for (wayoftime.bloodmagic.common.block.type.SpecialSealType type : wayoftime.bloodmagic.common.block.type.SpecialSealType.values()) {
            specialSealBuilder.partialState().with(wayoftime.bloodmagic.common.block.BlockSpecialDungeonSeal.SEAL, type).modelForState().modelFile(specialSealModel).addModel();
        }

        VariantBlockStateBuilder builder = getVariantBuilder(BMBlocks.ARC_BLOCK.block().get());
        String bottom = "block/arc_bottom";
        String lit = "_lit";
        for (EnumWillType type : EnumWillType.values()) {
            String willName = type.getSerializedName();
            String side = "block/arc_side_" + willName;
            String front = "block/arc_front_" + willName;
            String top = "block/arc_top_" + willName;
            ModelFile on = models().orientableWithBottom("alchemical_reaction_chamber_" + willName + "_lit", bm(side + lit), bm(front + lit), bm(bottom), bm(top));
            ModelFile off = models().orientableWithBottom("alchemical_reaction_chamber_" + willName, bm(side), bm(front), bm(bottom), bm(top));
            if (type == EnumWillType.DEFAULT) {
                simpleBlockItem(BMBlocks.ARC_BLOCK.block().get(), off);
            }

            for (Direction facing : Direction.Plane.HORIZONTAL) {
                builder.partialState().with(ARCBlock.LIT, false).with(ARCBlock.FACING, facing).with(ARCBlock.TYPE, type).modelForState().modelFile(off).rotationY((int) facing.getOpposite().toYRot()).addModel();
                builder.partialState().with(ARCBlock.LIT, true).with(ARCBlock.FACING, facing).with(ARCBlock.TYPE, type).modelForState().modelFile(on).rotationY((int) facing.getOpposite().toYRot()).addModel();
            }
        }
    }

    private void chargeBlock(wayoftime.bloodmagic.common.block.ExplosiveChargeBlock block, String name) {
        ModelFile model = models().cubeAll(name, bm("block/hellforged_block"));
        VariantBlockStateBuilder chargeBuilder = getVariantBuilder(block);
        for (Direction dir : Direction.values()) {
            chargeBuilder.partialState().with(wayoftime.bloodmagic.common.block.ExplosiveChargeBlock.ATTACHED, dir).modelForState().modelFile(model).addModel();
        }
        simpleBlockItem(block, model);
    }

    private static ResourceLocation bm(String path) {
        return ResourceLocation.fromNamespaceAndPath(BloodMagic.MODID, path);
    }
}
