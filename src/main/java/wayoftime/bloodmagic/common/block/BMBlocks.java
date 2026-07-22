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
import wayoftime.bloodmagic.common.item.block.MimicBlockItem;

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

    // Demon crop blocks, ported from 1.20.1's BlockNetherrackSoil/BlockGrowingDoubt/BlockTau (see
    // HarvestHandlerCrop's javadoc for why these were missing). NETHER_SOIL gets a BlockItem (via
    // BLOCK_REG, matching 1.20.1's separately-registered "nether_soil" item); the 3 crop blocks
    // don't (matching 1.20.1: they're only ever placed by planting their seed item on eligible
    // soil, same as vanilla wheat/Blocks.WHEAT has no BlockItem of its own).
    private static final BlockBehaviour.Properties nether_soil_properties = BlockBehaviour.Properties.of().strength(0.4F, 0.4F).sound(SoundType.NETHERRACK).randomTicks();
    public static final BlockWithItemHolder<NetherSoilBlock, BlockItem> NETHER_SOIL = BLOCK_REG.register("nether_soil", () -> new NetherSoilBlock(nether_soil_properties));

    private static final BlockBehaviour.Properties demon_crop_properties = BlockBehaviour.Properties.of().noCollission().randomTicks().instabreak().sound(SoundType.CROP);
    public static final DeferredHolder<Block, GrowingDoubtBlock> GROWING_DOUBT = BLOCKS.register("creeping_doubt", () -> new GrowingDoubtBlock(demon_crop_properties));
    public static final DeferredHolder<Block, TauBlock> WEAK_TAU = BLOCKS.register("weak_tau", () -> new TauBlock(demon_crop_properties, false));
    public static final DeferredHolder<Block, TauBlock> STRONG_TAU = BLOCKS.register("strong_tau", () -> new TauBlock(demon_crop_properties, true));

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

    // Demon Dungeon system, ported from 1.20.1's structures/ package + associated blocks. Only the
    // handful of blocks actually referenced by Java code (the generator's mechanical needs) are
    // ported this round - the ~130-block decorative palette (numbered dungeon_brick1-3/tile/
    // polished/pillars/stairs/slabs/walls/gates, plus the 4 Will-corrupted "corrosive/destructive/
    // steadfast/vengeful" reskins of all of those) is a separate content task for a follow-up round.
    // Until then, any of the copied NBT structure templates that reference those unregistered block
    // ids will place as air (vanilla's structure loader silently substitutes air for unknown block
    // ids) - the room-stitching/generation logic itself is unaffected, this is a visual/content gap
    // only. dungeon_stone is the block StoneToOreProcessor swaps for dungeon_ore while placing a
    // room's NBT, at a rate controlled by that room's oreDensity - it's the block actually used for
    // "these walls sometimes have valuable ore" progression, so it (and the ore it becomes) are
    // ported now rather than deferred with the rest of the decorative set.
    private static final BlockBehaviour.Properties dungeon_properties = BlockBehaviour.Properties.of().strength(2.0F, 5.0F).sound(SoundType.STONE).requiresCorrectToolForDrops();
    public static final BlockWithItemHolder<Block, BlockItem> DUNGEON_STONE = BLOCK_REG.register("dungeon_stone", dungeon_properties, new Item.Properties());
    public static final BlockWithItemHolder<Block, BlockItem> DUNGEON_ORE = BLOCK_REG.register("dungeon_ore", BlockBehaviour.Properties.of().strength(3.0F, 3.0F).sound(SoundType.STONE).requiresCorrectToolForDrops(), new Item.Properties());
    // Filler material used to cap doorways that never got a room placed against them, and to plug
    // the gap between adjacent rooms - this is what most of a generated dungeon's walls are made of.
    public static final BlockWithItemHolder<Block, BlockItem> DUNGEON_BRICK_ASSORTED = BLOCK_REG.register("dungeon_brick_assorted", BlockBehaviour.Properties.of().strength(20.0F, 50.0F).sound(SoundType.STONE).requiresCorrectToolForDrops(), new Item.Properties());
    // Replaces a TileDungeonSeal that got permanently blocked (a room couldn't be placed and the
    // door has nowhere left to go).
    public static final BlockWithItemHolder<Block, BlockItem> DUNGEON_TILE_SPECIAL = BLOCK_REG.register("dungeon_tilespecial", dungeon_properties, new Item.Properties());

    // Structural/internal only - never player-placed, so (matching the BLOOD_LIGHT precedent above)
    // these have no BlockItem.
    public static final DeferredHolder<Block, wayoftime.bloodmagic.common.block.BlockDungeonController> DUNGEON_CONTROLLER = BLOCKS.register("dungeon_controller", wayoftime.bloodmagic.common.block.BlockDungeonController::new);
    public static final DeferredHolder<Block, wayoftime.bloodmagic.common.block.BlockDungeonSeal> DUNGEON_SEAL = BLOCKS.register("dungeon_seal", wayoftime.bloodmagic.common.block.BlockDungeonSeal::new);
    public static final DeferredHolder<Block, wayoftime.bloodmagic.common.block.BlockSpecialDungeonSeal> SPECIAL_DUNGEON_SEAL = BLOCKS.register("special_dungeon_seal", wayoftime.bloodmagic.common.block.BlockSpecialDungeonSeal::new);

    // =========================================================================================
    // Demon Dungeon decorative block palette (follow-up round to the above).
    // =========================================================================================
    // Ported from 1.20.1's DUNGEONBLOCKS/DECORATIVE_STAIR/DECORATIVE_WALL/DECORATIVE_GATE/
    // DECORATIVE_SLAB families in BloodMagicBlocks.java. These are the plain decorative blocks the
    // 48 NBT dungeon room templates (ported last round, see structures/) reference by id - until now
    // those ids resolved to nothing registered, so vanilla's structure-template loader silently
    // placed them as air. DUNGEON_STONE/DUNGEON_ORE/DUNGEON_BRICK_ASSORTED/DUNGEON_TILE_SPECIAL/
    // DUNGEON_CONTROLLER/DUNGEON_SEAL/SPECIAL_DUNGEON_SEAL above were already ported last round and
    // are NOT touched here. DUNGEON_ALTERNATOR and DUNGEON_SPIKE_TRAP need working BlockEntities, so
    // they're registered separately elsewhere alongside their tiles, not as part of this family.
    //
    // Every family below (except the two "reskins only" ones, which reuse an already-registered base
    // block above) has 5 Will-corruption variants: an uncorrupted base plus corrosive/destructive/
    // steadfast/vengeful texture reskins - structurally identical Block/RotatedPillarBlock/StairBlock/
    // etc, only the texture differs. Nothing outside this file needs to address an individual variant
    // by name (unlike DUNGEON_STONE/DUNGEON_ORE above, these are never referenced from Java, only
    // from NBT by id string), so they're kept in suffix-keyed Maps here instead of ~130 separate named
    // fields. Shape-block (stair/wall/gate/slab) ids are hand-listed in String[]s rather than derived
    // by concatenation because 1.20.1's own ids are irregular (e.g. the *base* brick stair is
    // "dungeon_brick_stairs" but every one of its 4 reskins, and every other brick-based shape block
    // at every variant, is "dungeon_brick1_...") - the ported NBT templates hardcode 1.20.1's exact
    // id strings, so matching those exactly matters more than a tidy naming scheme here.
    //
    // 1.20.1 named its dungeon_metal blocks HELLFORGED_BLOCK, which collides with this branch's
    // unrelated pre-existing "hellforged_block" placeholder-textured machine block (see above) - so
    // this uses DUNGEON_METAL instead to avoid the clash.
    private static final String[] WILL_VARIANTS = {"", "_corrosive", "_destructive", "_steadfast", "_vengeful"};

    private static Map<String, BlockWithItemHolder<Block, BlockItem>> dungeonCubeFamily(String idPrefix, BlockBehaviour.Properties properties) {
        Map<String, BlockWithItemHolder<Block, BlockItem>> variants = new HashMap<>();
        for (String suffix : WILL_VARIANTS) {
            variants.put(suffix, BASIC_REG.register(idPrefix + suffix, properties, new Item.Properties()));
        }
        return variants;
    }

    // Like dungeonCubeFamily, but the "" (base) variant is an already-registered block from last
    // round (DUNGEON_STONE/DUNGEON_TILE_SPECIAL above) instead of a new registration.
    private static Map<String, BlockWithItemHolder<Block, BlockItem>> dungeonCubeReskinsOnly(BlockWithItemHolder<Block, BlockItem> base, String idPrefix, BlockBehaviour.Properties properties) {
        Map<String, BlockWithItemHolder<Block, BlockItem>> variants = new HashMap<>();
        variants.put("", base);
        for (String suffix : WILL_VARIANTS) {
            if (!suffix.isEmpty()) {
                variants.put(suffix, BASIC_REG.register(idPrefix + suffix, properties, new Item.Properties()));
            }
        }
        return variants;
    }

    public static final Map<String, BlockWithItemHolder<Block, BlockItem>> DUNGEON_BRICK_1 = dungeonCubeFamily("dungeon_brick1", dungeon_properties);
    public static final Map<String, BlockWithItemHolder<Block, BlockItem>> DUNGEON_BRICK_2 = dungeonCubeFamily("dungeon_brick2", dungeon_properties);
    public static final Map<String, BlockWithItemHolder<Block, BlockItem>> DUNGEON_BRICK_3 = dungeonCubeFamily("dungeon_brick3", dungeon_properties);
    public static final Map<String, BlockWithItemHolder<Block, BlockItem>> DUNGEON_POLISHED = dungeonCubeFamily("dungeon_polished", dungeon_properties);
    public static final Map<String, BlockWithItemHolder<Block, BlockItem>> DUNGEON_TILE_FAMILY = dungeonCubeFamily("dungeon_tile", dungeon_properties);
    public static final Map<String, BlockWithItemHolder<Block, BlockItem>> DUNGEON_SMALLBRICK = dungeonCubeFamily("dungeon_smallbrick", dungeon_properties);

    private static final BlockBehaviour.Properties dungeon_metal_properties = BlockBehaviour.Properties.of().strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    public static final Map<String, BlockWithItemHolder<Block, BlockItem>> DUNGEON_METAL = dungeonCubeFamily("dungeon_metal", dungeon_metal_properties);

    private static final BlockBehaviour.Properties dungeon_eye_properties = BlockBehaviour.Properties.of().strength(2.0F, 5.0F).sound(SoundType.STONE).requiresCorrectToolForDrops().lightLevel(state -> 15);
    public static final Map<String, BlockWithItemHolder<Block, BlockItem>> DUNGEON_EYE = dungeonCubeFamily("dungeon_eye", dungeon_eye_properties);

    public static final Map<String, BlockWithItemHolder<Block, BlockItem>> DUNGEON_STONE_FAMILY = dungeonCubeReskinsOnly(DUNGEON_STONE, "dungeon_stone", dungeon_properties);
    public static final Map<String, BlockWithItemHolder<Block, BlockItem>> DUNGEON_TILE_SPECIAL_FAMILY = dungeonCubeReskinsOnly(DUNGEON_TILE_SPECIAL, "dungeon_tilespecial", dungeon_properties);

    // Standalone plain cubes - no Will reskins of these existed in 1.20.1 either.
    private static final BlockBehaviour.Properties dungeon_emitter_properties = BlockBehaviour.Properties.of().strength(2.0F, 5.0F).sound(SoundType.STONE).requiresCorrectToolForDrops().lightLevel(state -> 8);
    // PoweredBlock is vanilla's Block of Redstone class (always-active redstone power source) - reused
    // here (as 1.20.1 did) purely for its emitter behaviour, with a custom light level on top.
    public static final BlockWithItemHolder<net.minecraft.world.level.block.PoweredBlock, BlockItem> DUNGEON_EMITTER = BASIC_REG.register("dungeon_emitter", () -> new net.minecraft.world.level.block.PoweredBlock(dungeon_emitter_properties), new Item.Properties());
    public static final BlockWithItemHolder<Block, BlockItem> DUNGEON_CRACKED_BRICK_1 = BASIC_REG.register("dungeon_regular_cracked_brick1", dungeon_properties, new Item.Properties());
    public static final BlockWithItemHolder<Block, BlockItem> DUNGEON_GLOWING_CRACKED_BRICK_1 = BASIC_REG.register("dungeon_cracked_brick1", dungeon_properties, new Item.Properties());

    // Pillars: RotatedPillarBlock (center/special) and the custom directional BlockPillarCap.
    private static Map<String, BlockWithItemHolder<net.minecraft.world.level.block.RotatedPillarBlock, BlockItem>> dungeonPillarFamily(String idPrefix) {
        Map<String, BlockWithItemHolder<net.minecraft.world.level.block.RotatedPillarBlock, BlockItem>> variants = new HashMap<>();
        for (String suffix : WILL_VARIANTS) {
            variants.put(suffix, BLOCK_REG.register(idPrefix + suffix, () -> new net.minecraft.world.level.block.RotatedPillarBlock(dungeon_properties)));
        }
        return variants;
    }

    public static final Map<String, BlockWithItemHolder<net.minecraft.world.level.block.RotatedPillarBlock, BlockItem>> DUNGEON_PILLAR_CENTER = dungeonPillarFamily("dungeon_pillar_center");
    public static final Map<String, BlockWithItemHolder<net.minecraft.world.level.block.RotatedPillarBlock, BlockItem>> DUNGEON_PILLAR_SPECIAL = dungeonPillarFamily("dungeon_pillar_special");

    private static Map<String, BlockWithItemHolder<BlockPillarCap, BlockItem>> dungeonPillarCapFamily() {
        Map<String, BlockWithItemHolder<BlockPillarCap, BlockItem>> variants = new HashMap<>();
        for (String suffix : WILL_VARIANTS) {
            variants.put(suffix, BLOCK_REG.register("dungeon_pillar_cap" + suffix, () -> new BlockPillarCap(dungeon_properties)));
        }
        return variants;
    }

    public static final Map<String, BlockWithItemHolder<BlockPillarCap, BlockItem>> DUNGEON_PILLAR_CAP = dungeonPillarCapFamily();

    // Shape blocks (stairs/walls/gates/slabs) built from the cube families above. Ids below are
    // 1.20.1's exact registry names, in WILL_VARIANTS order (base, corrosive, destructive, steadfast,
    // vengeful) - see the big comment at the top of this section for why the base-brick-stairs id is
    // irregular ("dungeon_brick_stairs", not "dungeon_brick1_stairs").
    private static final String[] DUNGEON_BRICK_STAIR_IDS = {"dungeon_brick_stairs", "dungeon_brick1_stairs_corrosive", "dungeon_brick1_stairs_destructive", "dungeon_brick1_stairs_steadfast", "dungeon_brick1_stairs_vengeful"};
    private static final String[] DUNGEON_POLISHED_STAIR_IDS = {"dungeon_polished_stairs", "dungeon_polished_stairs_corrosive", "dungeon_polished_stairs_destructive", "dungeon_polished_stairs_steadfast", "dungeon_polished_stairs_vengeful"};
    private static final String[] DUNGEON_STONE_STAIR_IDS = {"dungeon_stone_stairs", "dungeon_stone_stairs_corrosive", "dungeon_stone_stairs_destructive", "dungeon_stone_stairs_steadfast", "dungeon_stone_stairs_vengeful"};

    private static final String[] DUNGEON_BRICK_WALL_IDS = {"dungeon_brick1_wall", "dungeon_brick1_wall_corrosive", "dungeon_brick1_wall_destructive", "dungeon_brick1_wall_steadfast", "dungeon_brick1_wall_vengeful"};
    private static final String[] DUNGEON_TILE_WALL_IDS = {"dungeon_tile_wall", "dungeon_tile_wall_corrosive", "dungeon_tile_wall_destructive", "dungeon_tile_wall_steadfast", "dungeon_tile_wall_vengeful"};
    private static final String[] DUNGEON_POLISHED_WALL_IDS = {"dungeon_polished_wall", "dungeon_polished_wall_corrosive", "dungeon_polished_wall_destructive", "dungeon_polished_wall_steadfast", "dungeon_polished_wall_vengeful"};
    private static final String[] DUNGEON_STONE_WALL_IDS = {"dungeon_stone_wall", "dungeon_stone_wall_corrosive", "dungeon_stone_wall_destructive", "dungeon_stone_wall_steadfast", "dungeon_stone_wall_vengeful"};

    private static final String[] DUNGEON_BRICK_GATE_IDS = {"dungeon_brick1_gate", "dungeon_brick1_gate_corrosive", "dungeon_brick1_gate_destructive", "dungeon_brick1_gate_steadfast", "dungeon_brick1_gate_vengeful"};
    private static final String[] DUNGEON_POLISHED_GATE_IDS = {"dungeon_polished_gate", "dungeon_polished_gate_corrosive", "dungeon_polished_gate_destructive", "dungeon_polished_gate_steadfast", "dungeon_polished_gate_vengeful"};

    private static final String[] DUNGEON_BRICK_SLAB_IDS = {"dungeon_brick1_slab", "dungeon_brick1_slab_corrosive", "dungeon_brick1_slab_destructive", "dungeon_brick1_slab_steadfast", "dungeon_brick1_slab_vengeful"};
    private static final String[] DUNGEON_TILE_SLAB_IDS = {"dungeon_tile_slab", "dungeon_tile_slab_corrosive", "dungeon_tile_slab_destructive", "dungeon_tile_slab_steadfast", "dungeon_tile_slab_vengeful"};
    private static final String[] DUNGEON_STONE_SLAB_IDS = {"dungeon_stone_slab", "dungeon_stone_slab_corrosive", "dungeon_stone_slab_destructive", "dungeon_stone_slab_steadfast", "dungeon_stone_slab_vengeful"};
    private static final String[] DUNGEON_POLISHED_SLAB_IDS = {"dungeon_polished_slab", "dungeon_polished_slab_corrosive", "dungeon_polished_slab_destructive", "dungeon_polished_slab_steadfast", "dungeon_polished_slab_vengeful"};

    private static Map<String, BlockWithItemHolder<net.minecraft.world.level.block.StairBlock, BlockItem>> dungeonStairFamily(String[] ids, Map<String, BlockWithItemHolder<Block, BlockItem>> source) {
        Map<String, BlockWithItemHolder<net.minecraft.world.level.block.StairBlock, BlockItem>> variants = new HashMap<>();
        for (int i = 0; i < WILL_VARIANTS.length; i++) {
            String suffix = WILL_VARIANTS[i];
            BlockWithItemHolder<Block, BlockItem> src = source.get(suffix);
            variants.put(suffix, BLOCK_REG.register(ids[i], () -> new net.minecraft.world.level.block.StairBlock(src.block().get().defaultBlockState(), dungeon_properties)));
        }
        return variants;
    }

    private static Map<String, BlockWithItemHolder<net.minecraft.world.level.block.WallBlock, BlockItem>> dungeonWallFamily(String[] ids) {
        Map<String, BlockWithItemHolder<net.minecraft.world.level.block.WallBlock, BlockItem>> variants = new HashMap<>();
        for (int i = 0; i < WILL_VARIANTS.length; i++) {
            variants.put(WILL_VARIANTS[i], BLOCK_REG.register(ids[i], () -> new net.minecraft.world.level.block.WallBlock(dungeon_properties)));
        }
        return variants;
    }

    private static Map<String, BlockWithItemHolder<net.minecraft.world.level.block.FenceGateBlock, BlockItem>> dungeonGateFamily(String[] ids) {
        Map<String, BlockWithItemHolder<net.minecraft.world.level.block.FenceGateBlock, BlockItem>> variants = new HashMap<>();
        for (int i = 0; i < WILL_VARIANTS.length; i++) {
            variants.put(WILL_VARIANTS[i], BLOCK_REG.register(ids[i], () -> new net.minecraft.world.level.block.FenceGateBlock(net.minecraft.world.level.block.state.properties.WoodType.OAK, dungeon_properties)));
        }
        return variants;
    }

    private static Map<String, BlockWithItemHolder<net.minecraft.world.level.block.SlabBlock, BlockItem>> dungeonSlabFamily(String[] ids) {
        Map<String, BlockWithItemHolder<net.minecraft.world.level.block.SlabBlock, BlockItem>> variants = new HashMap<>();
        for (int i = 0; i < WILL_VARIANTS.length; i++) {
            variants.put(WILL_VARIANTS[i], BLOCK_REG.register(ids[i], () -> new net.minecraft.world.level.block.SlabBlock(dungeon_properties)));
        }
        return variants;
    }

    public static final Map<String, BlockWithItemHolder<net.minecraft.world.level.block.StairBlock, BlockItem>> DUNGEON_BRICK_STAIRS = dungeonStairFamily(DUNGEON_BRICK_STAIR_IDS, DUNGEON_BRICK_1);
    public static final Map<String, BlockWithItemHolder<net.minecraft.world.level.block.StairBlock, BlockItem>> DUNGEON_POLISHED_STAIRS = dungeonStairFamily(DUNGEON_POLISHED_STAIR_IDS, DUNGEON_POLISHED);
    public static final Map<String, BlockWithItemHolder<net.minecraft.world.level.block.StairBlock, BlockItem>> DUNGEON_STONE_STAIRS = dungeonStairFamily(DUNGEON_STONE_STAIR_IDS, DUNGEON_STONE_FAMILY);

    public static final Map<String, BlockWithItemHolder<net.minecraft.world.level.block.WallBlock, BlockItem>> DUNGEON_BRICK_WALLS = dungeonWallFamily(DUNGEON_BRICK_WALL_IDS);
    public static final Map<String, BlockWithItemHolder<net.minecraft.world.level.block.WallBlock, BlockItem>> DUNGEON_TILE_WALLS = dungeonWallFamily(DUNGEON_TILE_WALL_IDS);
    public static final Map<String, BlockWithItemHolder<net.minecraft.world.level.block.WallBlock, BlockItem>> DUNGEON_POLISHED_WALLS = dungeonWallFamily(DUNGEON_POLISHED_WALL_IDS);
    public static final Map<String, BlockWithItemHolder<net.minecraft.world.level.block.WallBlock, BlockItem>> DUNGEON_STONE_WALLS = dungeonWallFamily(DUNGEON_STONE_WALL_IDS);

    public static final Map<String, BlockWithItemHolder<net.minecraft.world.level.block.FenceGateBlock, BlockItem>> DUNGEON_BRICK_GATES = dungeonGateFamily(DUNGEON_BRICK_GATE_IDS);
    public static final Map<String, BlockWithItemHolder<net.minecraft.world.level.block.FenceGateBlock, BlockItem>> DUNGEON_POLISHED_GATES = dungeonGateFamily(DUNGEON_POLISHED_GATE_IDS);

    public static final Map<String, BlockWithItemHolder<net.minecraft.world.level.block.SlabBlock, BlockItem>> DUNGEON_BRICK_SLABS = dungeonSlabFamily(DUNGEON_BRICK_SLAB_IDS);
    public static final Map<String, BlockWithItemHolder<net.minecraft.world.level.block.SlabBlock, BlockItem>> DUNGEON_TILE_SLABS = dungeonSlabFamily(DUNGEON_TILE_SLAB_IDS);
    public static final Map<String, BlockWithItemHolder<net.minecraft.world.level.block.SlabBlock, BlockItem>> DUNGEON_STONE_SLABS = dungeonSlabFamily(DUNGEON_STONE_SLAB_IDS);
    public static final Map<String, BlockWithItemHolder<net.minecraft.world.level.block.SlabBlock, BlockItem>> DUNGEON_POLISHED_SLABS = dungeonSlabFamily(DUNGEON_POLISHED_SLAB_IDS);

    // Dungeon puzzle/hazard blocks (Priority 3 flavor content) - ported alongside their BlockEntities,
    // not registered as part of the plain-texture-family loops above.
    // dungeon_alternator: plain single-texture cube (its ACTIVE property has no visual effect in
    // 1.20.1 either - the block's animated texture strip cycles regardless of state), so it goes
    // through BASIC_REG for the free auto blockstate/model/loot treatment like the rest of the palette.
    public static final BlockWithItemHolder<BlockAlternator, BlockItem> DUNGEON_ALTERNATOR = BASIC_REG.register("dungeon_alternator", () -> new BlockAlternator(dungeon_properties), new Item.Properties());
    // dungeon_spike_trap and spikes both use hand-authored (not datagen'd) blockstates/models matching
    // 1.20.1's exactly (see src/main/resources/assets/bloodmagic/blockstates - orientable trigger plate
    // and a multipart "cross" hazard respectively) - kept on BLOCK_REG so BMBlockstateProvider's
    // BASIC_BLOCKS auto-loop doesn't also try to generate a conflicting cubeAll blockstate for them.
    public static final BlockWithItemHolder<BlockSpikeTrap, BlockItem> DUNGEON_SPIKE_TRAP = BLOCK_REG.register("dungeon_spike_trap", () -> new BlockSpikeTrap(dungeon_properties));
    private static final BlockBehaviour.Properties dungeon_spikes_properties = BlockBehaviour.Properties.of().strength(2.0F, 5.0F).sound(SoundType.CHAIN).noOcclusion().noCollission().requiresCorrectToolForDrops();
    public static final BlockWithItemHolder<BlockSpikes, BlockItem> DUNGEON_SPIKES = BLOCK_REG.register("spikes", () -> new BlockSpikes(dungeon_spikes_properties));

    // The Mimic: a purely cosmetic "disguise" block (not a monster - see BlockMimic), ported from
    // 1.20.1's BloodMagicBlocks MIMIC/ETHEREAL_MIMIC. Its dynamic baked model (client/model/mimic)
    // needs a custom loader, so (like DUNGEON_SPIKE_TRAP/DUNGEON_SPIKES above) it's kept on
    // BLOCK_REG with hand-authored blockstate/model json rather than BASIC_REG's auto-loop.
    // ETHEREAL_MIMIC additionally has noCollission() - it's the intangible variant 1.20.1's
    // guidebook says the dungeon generator hides pitfall traps and secret passages behind.
    public static final BlockWithItemHolder<BlockMimic, MimicBlockItem> MIMIC = BLOCK_REG.register("mimic",
            () -> new BlockMimic(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().sound(SoundType.METAL).strength(2.0f)
                    .isRedstoneConductor((state, level, pos) -> false).isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false).noOcclusion()),
            MimicBlockItem::new, new Item.Properties());
    public static final BlockWithItemHolder<BlockMimic, MimicBlockItem> ETHEREAL_MIMIC = BLOCK_REG.register("ethereal_mimic",
            () -> new BlockMimic(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().sound(SoundType.METAL).strength(2.0f)
                    .isRedstoneConductor((state, level, pos) -> false).isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false).noOcclusion().noCollission()),
            MimicBlockItem::new, new Item.Properties());

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
