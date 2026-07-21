package wayoftime.bloodmagic.common.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.caps.BMCaps;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datamap.BMDataMaps;
import wayoftime.bloodmagic.common.datamap.BloodRune;
import wayoftime.bloodmagic.api.altar.EnumRuneType;
import wayoftime.bloodmagic.util.BlockEntityHelper;
import wayoftime.bloodmagic.util.blockitem.BlockWithItemHolder;
import wayoftime.bloodmagic.util.blockitem.BlockWithItemRegister;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BMBlocks {
    public static final DeferredRegister<Block> BASIC_BLOCKS = DeferredRegister.createBlocks(BloodMagic.MODID);
    public static final DeferredRegister<Item> BASIC_BLOCK_ITEMS = DeferredRegister.createItems(BloodMagic.MODID);
    public static final BlockWithItemRegister BASIC_REG = new BlockWithItemRegister(BASIC_BLOCKS, BASIC_BLOCK_ITEMS);

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(BloodMagic.MODID);
    public static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.createItems(BloodMagic.MODID);
    public static final BlockWithItemRegister BLOCK_REG = new BlockWithItemRegister(BLOCKS, BLOCK_ITEMS);

    public static final BlockWithItemHolder<BloodAltarBlock, BlockItem> BLOOD_ALTAR = BLOCK_REG.register("blood_altar", BloodAltarBlock::new);
    public static final BlockWithItemHolder<BloodTankBlock, BlockItem> BLOOD_TANK = BLOCK_REG.register("blood_tank", BloodTankBlock::new, block -> new BlockItem(block, new Item.Properties().component(BMDataComponents.CONTAINER_TIER, 1)));
    public static final BlockWithItemHolder<HellfireForgeBlock, BlockItem> HELLFIRE_FORGE = BLOCK_REG.register("hellfire_forge", HellfireForgeBlock::new);
    public static final BlockWithItemHolder<ARCBlock, BlockItem> ARC_BLOCK = BLOCK_REG.register("arc", ARCBlock::new);
    public static final BlockWithItemHolder<AlchemyTableBlock, BlockItem> ALCHEMY_TABLE = BLOCK_REG.register("alchemy_table", AlchemyTableBlock::new);

    // Living Station has no 1.20.1 equivalent to port art from (it's new to this branch, built for
    // this branch's from-scratch Living Armour upgrade/training system - see LivingStationTile), so
    // there's no upstream texture to copy. It stays on BLOCK_REG (not BASIC_REG) and reuses the
    // "hellforged_block" placeholder cube texture in BMBlockstateProvider, the same convention
    // already used for the other mechanically-complete-but-textureless machine blocks (ITEM_ROUTER,
    // DEMON_CRUCIBLE, the routing nodes, etc.) - see BMBlockstateProvider#registerStatesAndModels.
    public static final BlockWithItemHolder<LivingStationBlock, BlockItem> LIVING_STATION = BLOCK_REG.register("living_station", LivingStationBlock::new);

    public static final BlockWithItemHolder<ImperfectRitualBlock, BlockItem> IMPERFECT_RITUAL_BLOCK = BASIC_REG.register("ritual_stone_imperfect", ImperfectRitualBlock::new);

    public static final BlockWithItemHolder<MasterRitualStoneBlock, BlockItem> MASTER_RITUAL_STONE = BLOCK_REG.register("ritual_stone_master", MasterRitualStoneBlock::new);

    // No item form - only ever placed by the Bloodlight sigil, matching the 1.20.1 original.
    public static final DeferredHolder<Block, BloodlightBlock> BLOOD_LIGHT = BLOCKS.register("bloodlight", BloodlightBlock::new);

    public static final BlockWithItemHolder<AlchemyArrayBlock, BlockItem> ALCHEMY_ARRAY = BASIC_REG.register("alchemy_array", AlchemyArrayBlock::new);

    public static final BlockWithItemHolder<TeleposerBlock, BlockItem> TELEPOSER = BLOCK_REG.register("teleposer", TeleposerBlock::new);

    public static final BlockWithItemHolder<IncenseAltarBlock, BlockItem> INCENSE_ALTAR = BLOCK_REG.register("incense_altar", IncenseAltarBlock::new);

    public static final BlockWithItemHolder<ItemRouterBlock, BlockItem> ITEM_ROUTER = BLOCK_REG.register("item_router", ItemRouterBlock::new);

    public static final BlockWithItemHolder<MasterRoutingNodeBlock, BlockItem> MASTER_ROUTING_NODE = BLOCK_REG.register("master_routing_node", MasterRoutingNodeBlock::new);
    public static final BlockWithItemHolder<InputRoutingNodeBlock, BlockItem> INPUT_ROUTING_NODE = BLOCK_REG.register("input_routing_node", InputRoutingNodeBlock::new);
    public static final BlockWithItemHolder<OutputRoutingNodeBlock, BlockItem> OUTPUT_ROUTING_NODE = BLOCK_REG.register("output_routing_node", OutputRoutingNodeBlock::new);

    public static final BlockWithItemHolder<RitualStoneBlock, BlockItem> RITUAL_STONE_BLANK = BASIC_REG.register("ritual_stone_blank", () -> new RitualStoneBlock(wayoftime.bloodmagic.api.ritual.EnumRuneType.BLANK));
    public static final BlockWithItemHolder<RitualStoneBlock, BlockItem> RITUAL_STONE_WATER = BASIC_REG.register("ritual_stone_water", () -> new RitualStoneBlock(wayoftime.bloodmagic.api.ritual.EnumRuneType.WATER));
    public static final BlockWithItemHolder<RitualStoneBlock, BlockItem> RITUAL_STONE_FIRE = BASIC_REG.register("ritual_stone_fire", () -> new RitualStoneBlock(wayoftime.bloodmagic.api.ritual.EnumRuneType.FIRE));
    public static final BlockWithItemHolder<RitualStoneBlock, BlockItem> RITUAL_STONE_EARTH = BASIC_REG.register("ritual_stone_earth", () -> new RitualStoneBlock(wayoftime.bloodmagic.api.ritual.EnumRuneType.EARTH));
    public static final BlockWithItemHolder<RitualStoneBlock, BlockItem> RITUAL_STONE_AIR = BASIC_REG.register("ritual_stone_air", () -> new RitualStoneBlock(wayoftime.bloodmagic.api.ritual.EnumRuneType.AIR));
    public static final BlockWithItemHolder<RitualStoneBlock, BlockItem> RITUAL_STONE_DUSK = BASIC_REG.register("ritual_stone_dusk", () -> new RitualStoneBlock(wayoftime.bloodmagic.api.ritual.EnumRuneType.DUSK));
    public static final BlockWithItemHolder<RitualStoneBlock, BlockItem> RITUAL_STONE_DAWN = BASIC_REG.register("ritual_stone_dawn", () -> new RitualStoneBlock(wayoftime.bloodmagic.api.ritual.EnumRuneType.DAWN));

    private static final BlockBehaviour.Properties rune_properties = BlockBehaviour.Properties.of().strength(2.0F, 5.0F).sound(SoundType.STONE).requiresCorrectToolForDrops();
    private static final ItemLore safe_decoration = new ItemLore(List.of(BlockEntityHelper.translatableHover("tooltip.bloodmagic.safe_for_decoration").withStyle(ChatFormatting.ITALIC)));
    private static final Item.Properties decoration_item_properties = new Item.Properties().component(DataComponents.LORE, safe_decoration);

    public static final BlockWithItemHolder<Block, BlockItem> RUNE_BLANK = BASIC_REG.register("rune_blank", rune_properties, decoration_item_properties);

    public static final BlockWithItemHolder<Block, BlockItem> RUNE_SACRIFICE = BASIC_REG.register("rune_sacrifice", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_SELF_SACRIFICE = BASIC_REG.register("rune_sacrifice_self", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_CAPACITY = BASIC_REG.register("rune_capacity", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_CAPACITY_AUGMENTED = BASIC_REG.register("rune_capacity_augmented", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_CHARGING = BASIC_REG.register("rune_charging", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_SPEED = BASIC_REG.register("rune_speed", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_ACCELERATION = BASIC_REG.register("rune_acceleration", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_DISLOCATION = BASIC_REG.register("rune_dislocation", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_ORB = BASIC_REG.register("rune_orb", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_EFFICIENCY = BASIC_REG.register("rune_efficiency", rune_properties, decoration_item_properties);

    public static final BlockWithItemHolder<Block, BlockItem> RUNE_2_SACRIFICE = BASIC_REG.register("rune_2_sacrifice", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_2_SELF_SACRIFICE = BASIC_REG.register("rune_2_sacrifice_self", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_2_CAPACITY = BASIC_REG.register("rune_2_capacity", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_2_CAPACITY_AUGMENTED = BASIC_REG.register("rune_2_capacity_augmented", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_2_CHARGING = BASIC_REG.register("rune_2_charging", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_2_SPEED = BASIC_REG.register("rune_2_speed", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_2_ACCELERATION = BASIC_REG.register("rune_2_acceleration", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_2_DISLOCATION = BASIC_REG.register("rune_2_dislocation", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_2_ORB = BASIC_REG.register("rune_2_orb", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> RUNE_2_EFFICIENCY = BASIC_REG.register("rune_2_efficiency", rune_properties, decoration_item_properties);

    public static final BlockWithItemHolder<Block, BlockItem> BLOODSTONE = BASIC_REG.register("bloodstone", rune_properties, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> BLOODSTONE_BRICK = BASIC_REG.register("bloodstone_brick", rune_properties, decoration_item_properties);

    public static final BlockWithItemHolder<Block, BlockItem> HELLFORGED_BLOCK = BASIC_REG.register("hellforged_block", BlockBehaviour.Properties.of().strength(5, 6).sound(SoundType.METAL).requiresCorrectToolForDrops(), new Item.Properties());

    public static final BlockWithItemHolder<CrystalClusterBlock, BlockItem> CRYSTAL_CLUSTER = BASIC_REG.register("crystal_cluster", CrystalClusterBlock::new, rune_properties, BlockItem::new, decoration_item_properties);
    public static final BlockWithItemHolder<Block, BlockItem> CRYSTAL_CLUSTER_BRICK = BASIC_REG.register("crystal_cluster_brick", rune_properties, decoration_item_properties);

    // Demonic Will collection chain, ported from 1.20.1's BlockDemonCrucible/BlockDemonCrystallizer/BlockDemonPylon.
    public static final BlockWithItemHolder<DemonCrucibleBlock, BlockItem> DEMON_CRUCIBLE = BLOCK_REG.register("demon_crucible", DemonCrucibleBlock::new);
    public static final BlockWithItemHolder<DemonCrystallizerBlock, BlockItem> DEMON_CRYSTALLIZER = BLOCK_REG.register("demon_crystallizer", DemonCrystallizerBlock::new);
    public static final BlockWithItemHolder<DemonPylonBlock, BlockItem> DEMON_PYLON = BLOCK_REG.register("demon_pylon", DemonPylonBlock::new);

    // Explosive Charge family, ported from 1.20.1's BlockShapedExplosive/BlockDeforesterCharge/
    // BlockFungalCharge/BlockVeinMineCharge. Only the base tier of each is ported (the original's
    // "augmented"/"deep" tiers - AUG_SHAPED_CHARGE, DEFORESTER_CHARGE_2, VEINMINE_CHARGE_2,
    // FUNGAL_CHARGE_2, SHAPED_CHARGE_DEEP - are the same blocks at bigger radius/budget values and
    // aren't ported, for scope).
    private static final BlockBehaviour.Properties charge_properties = BlockBehaviour.Properties.of().strength(2.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    public static final BlockWithItemHolder<ShapedChargeBlock, BlockItem> SHAPED_CHARGE = BLOCK_REG.register("shaped_charge", () -> new ShapedChargeBlock(2, charge_properties));
    public static final BlockWithItemHolder<DeforesterChargeBlock, BlockItem> DEFORESTER_CHARGE = BLOCK_REG.register("deforester_charge", () -> new DeforesterChargeBlock(128, charge_properties));
    public static final BlockWithItemHolder<VeinMineChargeBlock, BlockItem> VEINMINE_CHARGE = BLOCK_REG.register("veinmine_charge", () -> new VeinMineChargeBlock(128, charge_properties));
    public static final BlockWithItemHolder<FungalChargeBlock, BlockItem> FUNGAL_CHARGE = BLOCK_REG.register("fungal_charge", () -> new FungalChargeBlock(128, charge_properties));

    private static void registerBlockCapability(RegisterCapabilitiesEvent event) {
        event.registerBlock(
                BMCaps.RUNE_POWERS,
                (level, pos, state, blockEntity, context) -> () -> {
                    List<BloodRune> runes = state.getBlockHolder().getData(BMDataMaps.BLOOD_RUNES);
                    Map<EnumRuneType, Integer> upgrades = new HashMap<>();
                    if (runes == null) {
                        return upgrades;
                    }

                    for (BloodRune rune : runes) {
                        upgrades.compute(rune.type(), (k, v) -> v == null ? rune.amount() : v + rune.amount());
                    }
                    return upgrades;
                },
                RUNE_ACCELERATION.block().get(), RUNE_SPEED.block().get(), RUNE_CHARGING.block().get(),
                RUNE_SACRIFICE.block().get(), RUNE_SELF_SACRIFICE.block().get(), RUNE_ORB.block().get(),
                RUNE_CAPACITY.block().get(), RUNE_CAPACITY_AUGMENTED.block().get(), RUNE_DISLOCATION.block().get(),
                RUNE_EFFICIENCY.block().get(),
                RUNE_2_ACCELERATION.block().get(), RUNE_2_SPEED.block().get(), RUNE_2_CHARGING.block().get(),
                RUNE_2_SACRIFICE.block().get(), RUNE_2_SELF_SACRIFICE.block().get(), RUNE_2_ORB.block().get(),
                RUNE_2_CAPACITY.block().get(), RUNE_2_CAPACITY_AUGMENTED.block().get(), RUNE_2_DISLOCATION.block().get(),
                RUNE_2_EFFICIENCY.block().get()
        );
    }

    public static void register(IEventBus modBus) {
        BASIC_BLOCKS.register(modBus);
        BASIC_BLOCK_ITEMS.register(modBus);
        BLOCKS.register(modBus);
        BLOCK_ITEMS.register(modBus);
        modBus.addListener(BMBlocks::registerBlockCapability);
    }
}
