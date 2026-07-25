package wayoftime.bloodmagic;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import wayoftime.bloodmagic.common.menu.BMMenus;
import wayoftime.bloodmagic.common.attribute.BMAttributes;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.blockentity.BMTiles;
import wayoftime.bloodmagic.common.command.BMCommands;
import wayoftime.bloodmagic.common.creativetab.BMTabs;
import wayoftime.bloodmagic.common.dataattachment.BMDataAttachments;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datamap.BMDataMaps;
import wayoftime.bloodmagic.common.entity.BMEntities;
import wayoftime.bloodmagic.common.fluid.BMFluids;
import wayoftime.bloodmagic.common.incense.IncenseTranquilityRegistry;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.common.item.BMMaterialsAndTiers;
import wayoftime.bloodmagic.common.loot.BMLootItemFunctions;
import wayoftime.bloodmagic.common.loot.BMLootModifiers;
import wayoftime.bloodmagic.common.recipe.BMRecipes;
import wayoftime.bloodmagic.common.recipe.ingredient.BMIngredientTypes;
import wayoftime.bloodmagic.common.registry.BMRegistries;
import wayoftime.bloodmagic.common.event.AnointmentEventHandler;
import wayoftime.bloodmagic.common.event.SentientArmorEventHandler;
import wayoftime.bloodmagic.common.potion.BMPotionEventHandler;
import wayoftime.bloodmagic.common.potion.BMPotions;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.common.ritual.harvest.HarvestHandlerRegistry;
import wayoftime.bloodmagic.common.sigil.SuppressionEffect;
import wayoftime.bloodmagic.common.will.WillEventHandler;
import wayoftime.bloodmagic.compat.curios.CuriosCompat;
import wayoftime.bloodmagic.compat.modopedia.BookCompat;
import wayoftime.bloodmagic.network.BMNetworking;

@Mod(BloodMagic.MODID)
public class BloodMagic {
    public static final String MODID = "bloodmagic";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceLocation TYPE_PROPERTY = rl("will_type");
    public static final ResourceLocation INCENSE_PROPERTY = rl("incense_type");

    public static final ServerConfig SERVER_CONFIG;
    private static final ModConfigSpec SERVER_CONFIG_SPEC;

    static {
        Pair<ServerConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_CONFIG = pair.getLeft();
        SERVER_CONFIG_SPEC = pair.getRight();
    }

    public BloodMagic(IEventBus modBus, ModContainer container, Dist side) {
        BMRegistries.register(modBus);
        BMDataComponents.register(modBus);
        BMFluids.register(modBus);
        BMBlocks.register(modBus);
        BMTiles.register(modBus);
        BMEntities.register(modBus);
        BMMaterialsAndTiers.register(modBus);
        BMItems.register(modBus);
        modBus.addListener(BMDataMaps::register);
        BMDataAttachments.register(modBus);
        BMAttributes.register(modBus);
        BMRecipes.register(modBus);
        BMIngredientTypes.register(modBus);
        BMMenus.register(modBus);
        BMTabs.register(modBus);
        BMPotions.register(modBus);
        BMLootModifiers.register(modBus);
        BMLootItemFunctions.register(modBus);
        modBus.addListener(BMNetworking::register);
        RitualRegistry.bootstrap();
        HarvestHandlerRegistry.bootstrap();
        IncenseTranquilityRegistry.bootstrap();

        // Demon Dungeon system: registers every room/room-pool id and loads its JSON off the mod's
        // own classpath (see DungeonRoomLoader) - safe to do here since, like the bootstrap() calls
        // above, none of this touches a DeferredHolder#get() yet. ModRoomPools#registerSpecialRooms
        // DOES need block state lookups though, so it's deferred to FMLCommonSetupEvent below,
        // after registries have actually been populated.
        wayoftime.bloodmagic.structures.ModDungeons.init();
        wayoftime.bloodmagic.structures.ModRoomPools.init();
        modBus.addListener(BloodMagic::commonSetup);

        container.registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG_SPEC);

        NeoForge.EVENT_BUS.addListener(BMCommands::register);
        NeoForge.EVENT_BUS.addListener(SuppressionEffect::onMobSpawnPositionCheck);
        NeoForge.EVENT_BUS.addListener(WillEventHandler::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(WillEventHandler::onItemPickup);
        NeoForge.EVENT_BUS.addListener(AnointmentEventHandler::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(AnointmentEventHandler::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(AnointmentEventHandler::onBlockDrops);
        NeoForge.EVENT_BUS.addListener(AnointmentEventHandler::onArrowLoose);
        NeoForge.EVENT_BUS.addListener(AnointmentEventHandler::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(BMPotionEventHandler::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(BMPotionEventHandler::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(BMPotionEventHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(SentientArmorEventHandler::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(SentientArmorEventHandler::onDamagePre);

        if (ModList.get().isLoaded("curios")) {
            modBus.addListener(CuriosCompat::registerCapabilities);
        }

        if (side == Dist.CLIENT && ModList.get().isLoaded("modopedia")) {
            BookCompat.init();
        }

        if (side == Dist.CLIENT) {
            modBus.addListener(wayoftime.bloodmagic.client.BMKeyMappings::register);
            NeoForge.EVENT_BUS.addListener(wayoftime.bloodmagic.client.BMKeyMappings::onClientTick);
        }
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(wayoftime.bloodmagic.structures.ModRoomPools::registerSpecialRooms);
    }
}
