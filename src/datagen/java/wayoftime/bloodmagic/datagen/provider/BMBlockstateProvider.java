package wayoftime.bloodmagic.datagen.provider;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.block.ARCBlock;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.block.NetherSoilBlock;
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

        // Demon Dungeon decorative block palette - everything registered via BASIC_REG in BMBlocks
        // (bricks 1/2/3, the stone/tilespecial Will reskins, eye, polished, tile, smallbrick, metal,
        // emitter, both cracked bricks - ~56 plain single-texture cubes) is already handled for free
        // by the BASIC_BLOCKS.getEntries() loop above: cubeAll keys off the registry name, which
        // matches the flat texture files copied from 1.20.1's textures/block/dungeon/ folder 1:1
        // (flattened - see BMBlocks). Only the blocks registered via BLOCK_REG below (pillars, and the
        // stair/wall/gate/slab shapes) need explicit treatment here, since none of those are simple
        // same-name cubes.
        dungeonPillarFamily(BMBlocks.DUNGEON_PILLAR_CENTER, "dungeon_pillar", "dungeon_pillarheart");
        dungeonPillarFamily(BMBlocks.DUNGEON_PILLAR_SPECIAL, "dungeon_pillarspecial", "dungeon_pillarheart");
        dungeonPillarCapFamily(BMBlocks.DUNGEON_PILLAR_CAP);

        dungeonStairFamily(BMBlocks.DUNGEON_BRICK_STAIRS, "dungeon_brick1");
        dungeonStairFamily(BMBlocks.DUNGEON_POLISHED_STAIRS, "dungeon_polished");
        dungeonStairFamily(BMBlocks.DUNGEON_STONE_STAIRS, "dungeon_stone");

        dungeonWallFamily(BMBlocks.DUNGEON_BRICK_WALLS, "dungeon_brick1");
        dungeonWallFamily(BMBlocks.DUNGEON_TILE_WALLS, "dungeon_tile");
        dungeonWallFamily(BMBlocks.DUNGEON_POLISHED_WALLS, "dungeon_polished");
        dungeonWallFamily(BMBlocks.DUNGEON_STONE_WALLS, "dungeon_stone");

        dungeonGateFamily(BMBlocks.DUNGEON_BRICK_GATES, "dungeon_brick1");
        dungeonGateFamily(BMBlocks.DUNGEON_POLISHED_GATES, "dungeon_polished");

        dungeonSlabFamily(BMBlocks.DUNGEON_BRICK_SLABS, "dungeon_brick1");
        dungeonSlabFamily(BMBlocks.DUNGEON_TILE_SLABS, "dungeon_tile");
        dungeonSlabFamily(BMBlocks.DUNGEON_STONE_SLABS, "dungeon_stone");
        dungeonSlabFamily(BMBlocks.DUNGEON_POLISHED_SLABS, "dungeon_polished");

        // Demon crop blocks (see BMBlocks/NetherSoilBlock/GrowingDoubtBlock/TauBlock) - textures
        // copied 1:1 from 1.20.1.
        ModelFile netherSoilModel = models().withExistingParent("nether_soil", mcLoc("block/template_farmland"))
                .texture("dirt", mcLoc("block/netherrack"))
                .texture("top", bm("block/nether_soil"));
        VariantBlockStateBuilder netherSoilBuilder = getVariantBuilder(BMBlocks.NETHER_SOIL.block().get());
        for (int moisture = 0; moisture <= 7; moisture++) {
            netherSoilBuilder.partialState().with(NetherSoilBlock.MOISTURE, moisture).modelForState().modelFile(netherSoilModel).addModel();
        }
        simpleBlockItem(BMBlocks.NETHER_SOIL.block().get(), netherSoilModel);

        cropAgeBlock(BMBlocks.GROWING_DOUBT.get(), "block/crop", "crop", "creeping_doubt", null);
        cropAgeBlock(BMBlocks.WEAK_TAU.get(), "block/cross", "cross", "weak_tau", null);
        // strong_tau age 0 is unreachable in practice (strong Tau only ever appears via a >=age-1
        // transform from weak Tau - see TauBlock) and 1.20.1 never shipped a strong_tau_1 texture,
        // instead reusing weak_tau_1 for that unreachable variant - matched here for fidelity.
        cropAgeBlock(BMBlocks.STRONG_TAU.get(), "block/cross", "cross", "strong_tau", "weak_tau_1");
    }

    private void cropAgeBlock(CropBlock block, String parentPath, String textureKey, String texturePrefix, String age0OverrideTexture) {
        String name = path(block);
        VariantBlockStateBuilder builder = getVariantBuilder(block);
        for (int age = 0; age <= 7; age++) {
            ResourceLocation texture = (age == 0 && age0OverrideTexture != null)
                    ? bm("block/" + age0OverrideTexture)
                    : bm("block/" + texturePrefix + "_" + (age + 1));
            ModelFile model = models().withExistingParent(name + "_" + (age + 1), mcLoc(parentPath)).texture(textureKey, texture);
            builder.partialState().with(CropBlock.AGE, age).modelForState().modelFile(model).addModel();
        }
    }

    private static final String[] DUNGEON_WILL_VARIANTS = {"", "_corrosive", "_destructive", "_steadfast", "_vengeful"};

    private static String path(net.minecraft.world.level.block.Block block) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

    private void dungeonPillarFamily(java.util.Map<String, wayoftime.bloodmagic.util.blockitem.BlockWithItemHolder<net.minecraft.world.level.block.RotatedPillarBlock, net.minecraft.world.item.BlockItem>> family, String sideBase, String endBase) {
        for (String suffix : DUNGEON_WILL_VARIANTS) {
            net.minecraft.world.level.block.RotatedPillarBlock block = family.get(suffix).block().get();
            String name = path(block);
            ResourceLocation side = bm("block/" + sideBase + suffix);
            ResourceLocation end = bm("block/" + endBase + suffix);
            ModelFile vertical = models().cubeColumn(name, side, end);
            ModelFile horizontal = models().cubeColumnHorizontal(name + "_horizontal", side, end);
            axisBlock(block, vertical, horizontal);
            simpleBlockItem(block, vertical);
        }
    }

    // Matches 1.20.1's hand-authored dungeon_pillar_cap.json blockstate exactly (see wayoftime.bloodmagic.common.block.BlockPillarCap) -
    // the cap has two distinct models (an "outward" one used for up/east/south and a "downward" one
    // used for down/north/west) since it isn't texture-symmetric front-to-back, so this can't use the
    // generic directionalBlock() helper (which only ever rotates a single model).
    private void dungeonPillarCapFamily(java.util.Map<String, wayoftime.bloodmagic.util.blockitem.BlockWithItemHolder<wayoftime.bloodmagic.common.block.BlockPillarCap, net.minecraft.world.item.BlockItem>> family) {
        for (String suffix : DUNGEON_WILL_VARIANTS) {
            wayoftime.bloodmagic.common.block.BlockPillarCap block = family.get(suffix).block().get();
            String name = path(block);
            ResourceLocation heart = bm("block/dungeon_pillarheart" + suffix);
            ResourceLocation top = bm("block/dungeon_pillartop" + suffix);
            ResourceLocation bottom = bm("block/dungeon_pillarbottom" + suffix);
            ModelFile up = models().cubeBottomTop(name, top, heart, heart);
            ModelFile down = models().cubeBottomTop(name + "_down", bottom, heart, heart);
            getVariantBuilder(block)
                    .partialState().with(wayoftime.bloodmagic.common.block.BlockPillarCap.FACING, Direction.DOWN).modelForState().modelFile(down).addModel()
                    .partialState().with(wayoftime.bloodmagic.common.block.BlockPillarCap.FACING, Direction.EAST).modelForState().modelFile(up).rotationX(90).rotationY(90).addModel()
                    .partialState().with(wayoftime.bloodmagic.common.block.BlockPillarCap.FACING, Direction.NORTH).modelForState().modelFile(down).rotationX(270).addModel()
                    .partialState().with(wayoftime.bloodmagic.common.block.BlockPillarCap.FACING, Direction.SOUTH).modelForState().modelFile(up).rotationX(270).addModel()
                    .partialState().with(wayoftime.bloodmagic.common.block.BlockPillarCap.FACING, Direction.UP).modelForState().modelFile(up).addModel()
                    .partialState().with(wayoftime.bloodmagic.common.block.BlockPillarCap.FACING, Direction.WEST).modelForState().modelFile(down).rotationX(90).rotationY(90).addModel();
            simpleBlockItem(block, up);
        }
    }

    private void dungeonStairFamily(java.util.Map<String, wayoftime.bloodmagic.util.blockitem.BlockWithItemHolder<net.minecraft.world.level.block.StairBlock, net.minecraft.world.item.BlockItem>> family, String textureBase) {
        for (String suffix : DUNGEON_WILL_VARIANTS) {
            net.minecraft.world.level.block.StairBlock block = family.get(suffix).block().get();
            ResourceLocation texture = bm("block/" + textureBase + suffix);
            stairsBlock(block, texture);
            simpleBlockItem(block, models().stairs(path(block), texture, texture, texture));
        }
    }

    private void dungeonWallFamily(java.util.Map<String, wayoftime.bloodmagic.util.blockitem.BlockWithItemHolder<net.minecraft.world.level.block.WallBlock, net.minecraft.world.item.BlockItem>> family, String textureBase) {
        for (String suffix : DUNGEON_WILL_VARIANTS) {
            net.minecraft.world.level.block.WallBlock block = family.get(suffix).block().get();
            ResourceLocation texture = bm("block/" + textureBase + suffix);
            wallBlock(block, texture);
            simpleBlockItem(block, models().wallInventory(path(block) + "_inventory", texture));
        }
    }

    private void dungeonGateFamily(java.util.Map<String, wayoftime.bloodmagic.util.blockitem.BlockWithItemHolder<net.minecraft.world.level.block.FenceGateBlock, net.minecraft.world.item.BlockItem>> family, String textureBase) {
        for (String suffix : DUNGEON_WILL_VARIANTS) {
            net.minecraft.world.level.block.FenceGateBlock block = family.get(suffix).block().get();
            ResourceLocation texture = bm("block/" + textureBase + suffix);
            fenceGateBlock(block, texture);
            simpleBlockItem(block, models().fenceGate(path(block), texture));
        }
    }

    private void dungeonSlabFamily(java.util.Map<String, wayoftime.bloodmagic.util.blockitem.BlockWithItemHolder<net.minecraft.world.level.block.SlabBlock, net.minecraft.world.item.BlockItem>> family, String textureBase) {
        for (String suffix : DUNGEON_WILL_VARIANTS) {
            net.minecraft.world.level.block.SlabBlock block = family.get(suffix).block().get();
            ResourceLocation texture = bm("block/" + textureBase + suffix);
            slabBlock(block, texture, texture);
            simpleBlockItem(block, models().slab(path(block), texture, texture, texture));
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
