package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.client.render.blockentity.AlchemyArrayRenderer;
import wayoftime.bloodmagic.client.render.blockentity.BloodAltarRenderer;
import wayoftime.bloodmagic.client.render.blockentity.BloodTankRenderer;
import wayoftime.bloodmagic.client.render.blockentity.HellfireForgeRenderer;
import wayoftime.bloodmagic.common.block.BMBlocks;

import java.util.Set;

public class BMTiles {
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BloodMagic.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HellfireForgeTile>> HELLFIRE_FORGE_TYPE = TILES.register("hellfire_forge",
            () -> new BlockEntityType<>(HellfireForgeTile::new, Set.of(BMBlocks.HELLFIRE_FORGE.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BloodAltarTile>> BLOOD_ALTAR_TYPE = TILES.register("blood_altar",
            () -> new BlockEntityType<>(BloodAltarTile::new, Set.of(BMBlocks.BLOOD_ALTAR.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ARCTile>> ARC_TYPE = TILES.register("arc",
            () -> new BlockEntityType<>(ARCTile::new, Set.of(BMBlocks.ARC_BLOCK.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BloodTankTile>> BLOOD_TANK_TYPE = TILES.register("blood_tank",
            () -> new BlockEntityType<>(BloodTankTile::new, Set.of(BMBlocks.BLOOD_TANK.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LivingStationTile>> LIVING_STATION_TYPE = TILES.register("living_station",
            () -> new BlockEntityType<>(LivingStationTile::new, Set.of(BMBlocks.LIVING_STATION.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlchemyTableTile>> ALCHEMY_TABLE_TYPE = TILES.register("alchemy_table",
            () -> new BlockEntityType<>(AlchemyTableTile::new, Set.of(BMBlocks.ALCHEMY_TABLE.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MasterRitualStoneTile>> MASTER_RITUAL_STONE_TYPE = TILES.register("ritual_stone_master",
            () -> new BlockEntityType<>(MasterRitualStoneTile::new, Set.of(BMBlocks.MASTER_RITUAL_STONE.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TeleposerTile>> TELEPOSER_TYPE = TILES.register("teleposer",
            () -> new BlockEntityType<>(TeleposerTile::new, Set.of(BMBlocks.TELEPOSER.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IncenseAltarTile>> INCENSE_ALTAR_TYPE = TILES.register("incense_altar",
            () -> new BlockEntityType<>(IncenseAltarTile::new, Set.of(BMBlocks.INCENSE_ALTAR.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemRouterTile>> ITEM_ROUTER_TYPE = TILES.register("item_router",
            () -> new BlockEntityType<>(ItemRouterTile::new, Set.of(BMBlocks.ITEM_ROUTER.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MasterRoutingNodeTile>> MASTER_ROUTING_NODE_TYPE = TILES.register("master_routing_node",
            () -> new BlockEntityType<>(MasterRoutingNodeTile::new, Set.of(BMBlocks.MASTER_ROUTING_NODE.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InputRoutingNodeTile>> INPUT_ROUTING_NODE_TYPE = TILES.register("input_routing_node",
            () -> new BlockEntityType<>(InputRoutingNodeTile::new, Set.of(BMBlocks.INPUT_ROUTING_NODE.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OutputRoutingNodeTile>> OUTPUT_ROUTING_NODE_TYPE = TILES.register("output_routing_node",
            () -> new BlockEntityType<>(OutputRoutingNodeTile::new, Set.of(BMBlocks.OUTPUT_ROUTING_NODE.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrystalClusterTile>> CRYSTAL_CLUSTER_TYPE = TILES.register("crystal_cluster",
            () -> new BlockEntityType<>(CrystalClusterTile::new, Set.of(BMBlocks.CRYSTAL_CLUSTER.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlchemyArrayTile>> ALCHEMY_ARRAY_TYPE = TILES.register("alchemy_array",
            () -> new BlockEntityType<>(AlchemyArrayTile::new, Set.of(BMBlocks.ALCHEMY_ARRAY.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DemonCrucibleTile>> DEMON_CRUCIBLE_TYPE = TILES.register("demon_crucible",
            () -> new BlockEntityType<>(DemonCrucibleTile::new, Set.of(BMBlocks.DEMON_CRUCIBLE.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DemonCrystallizerTile>> DEMON_CRYSTALLIZER_TYPE = TILES.register("demon_crystallizer",
            () -> new BlockEntityType<>(DemonCrystallizerTile::new, Set.of(BMBlocks.DEMON_CRYSTALLIZER.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DemonPylonTile>> DEMON_PYLON_TYPE = TILES.register("demon_pylon",
            () -> new BlockEntityType<>(DemonPylonTile::new, Set.of(BMBlocks.DEMON_PYLON.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShapedChargeTile>> SHAPED_CHARGE_TYPE = TILES.register("shaped_charge",
            () -> new BlockEntityType<>((pos, state) -> new ShapedChargeTile(pos, state, 2, 5), Set.of(BMBlocks.SHAPED_CHARGE.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeforesterChargeTile>> DEFORESTER_CHARGE_TYPE = TILES.register("deforester_charge",
            () -> new BlockEntityType<>((pos, state) -> new DeforesterChargeTile(pos, state, 128), Set.of(BMBlocks.DEFORESTER_CHARGE.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VeinMineChargeTile>> VEIN_MINE_CHARGE_TYPE = TILES.register("veinmine_charge",
            () -> new BlockEntityType<>((pos, state) -> new VeinMineChargeTile(pos, state, 128), Set.of(BMBlocks.VEINMINE_CHARGE.block().get()), null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FungalChargeTile>> FUNGAL_CHARGE_TYPE = TILES.register("fungal_charge",
            () -> new BlockEntityType<>((pos, state) -> new FungalChargeTile(pos, state, 128), Set.of(BMBlocks.FUNGAL_CHARGE.block().get()), null));

    // Demon Dungeon system - see BMBlocks for why only this handful of dungeon blocks are ported
    // this round.
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileDungeonController>> DUNGEON_CONTROLLER_TYPE = TILES.register("dungeon_controller",
            () -> new BlockEntityType<>(TileDungeonController::new, Set.of(BMBlocks.DUNGEON_CONTROLLER.get()), null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileDungeonSeal>> DUNGEON_SEAL_TYPE = TILES.register("dungeon_seal",
            () -> new BlockEntityType<>(TileDungeonSeal::new, Set.of(BMBlocks.DUNGEON_SEAL.get()), null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileSpecialRoomDungeonSeal>> SPECIAL_DUNGEON_SEAL_TYPE = TILES.register("special_dungeon_seal",
            () -> new BlockEntityType<>(TileSpecialRoomDungeonSeal::new, Set.of(BMBlocks.SPECIAL_DUNGEON_SEAL.get()), null));

    private static void registerTileCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                HELLFIRE_FORGE_TYPE.get(),
                HellfireForgeTile::getInventory
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BLOOD_ALTAR_TYPE.get(),
                (tile, side) -> tile.getInventory()
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                BLOOD_ALTAR_TYPE.get(),
                (tile, side) -> side == null ? null : tile
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ARC_TYPE.get(),
                ARCTile::getItemHandler
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ARC_TYPE.get(),
                ARCTile::getFluidHandler
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                BLOOD_TANK_TYPE.get(),
                BloodTankTile::getFluidHandler
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                LIVING_STATION_TYPE.get(),
                (tile, side) -> {
                    int start = 0;
                    int end = 1;
                    if (side == Direction.UP) { // top = scrap
                        start++;
                        end++;
                    }
                    return new RangedWrapper(tile.itemCap, start, end);
                }
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ALCHEMY_TABLE_TYPE.get(),
                AlchemyTableTile::getItemHandler
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ALCHEMY_ARRAY_TYPE.get(),
                (tile, side) -> tile.getInventory()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                DEMON_CRUCIBLE_TYPE.get(),
                (tile, side) -> tile.getInventory()
        );
    }

    private static void registerBlockEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(HELLFIRE_FORGE_TYPE.get(), HellfireForgeRenderer::new);
        event.registerBlockEntityRenderer(BLOOD_ALTAR_TYPE.get(), BloodAltarRenderer::new);
        event.registerBlockEntityRenderer(BLOOD_TANK_TYPE.get(), BloodTankRenderer::new);
        event.registerBlockEntityRenderer(ALCHEMY_ARRAY_TYPE.get(), AlchemyArrayRenderer::new);
    }

    public static void register(IEventBus modBus) {
        TILES.register(modBus);
        modBus.addListener(BMTiles::registerTileCapabilities);
        modBus.addListener(BMTiles::registerBlockEntityRenderer);
    }
}