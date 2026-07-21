package wayoftime.bloodmagic.client.event;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.api.BMIdentifiers;
import wayoftime.bloodmagic.client.hud.ElementRegistry;
import wayoftime.bloodmagic.client.hud.Elements;
import wayoftime.bloodmagic.client.model.sigil.SigilLoader;
import wayoftime.bloodmagic.client.model.mimic.MimicColor;
import wayoftime.bloodmagic.client.model.mimic.MimicLoader;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.client.render.BMModelLayers;
import wayoftime.bloodmagic.client.render.entity.EntityMeteorRenderer;
import wayoftime.bloodmagic.client.render.item.FlaskColor;
import wayoftime.bloodmagic.client.render.model.ModelMeteor;
import wayoftime.bloodmagic.client.screen.AlchemyTableScreen;
import wayoftime.bloodmagic.client.screen.FilterScreen;
import wayoftime.bloodmagic.client.screen.HoldingScreen;
import wayoftime.bloodmagic.client.screen.TrainerScreen;
import wayoftime.bloodmagic.common.entity.BMEntities;
import wayoftime.bloodmagic.common.menu.BMMenus;
import wayoftime.bloodmagic.client.render.entity.layer.LivingElytraLayer;
import wayoftime.bloodmagic.client.screen.ARCScreen;
import wayoftime.bloodmagic.client.screen.LivingStationScreen;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.item.BMItems;

@EventBusSubscriber(value = Dist.CLIENT, modid = BloodMagic.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ClientModEventHandler {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BMItems.WILL_ITEMS.getEntries().forEach(item -> {
                ItemProperties.register(item.get(), BloodMagic.TYPE_PROPERTY,
                        (stack, level, player, seed) ->
                                stack.getOrDefault(BMDataComponents.DEMON_WILL_TYPE, EnumWillType.DEFAULT).ordinal());
            });

            ItemProperties.register(BMItems.SACRIFICIAL_DAGGER.get(), BloodMagic.INCENSE_PROPERTY,
                    (stack, level, entity, seed) ->
                            stack.getOrDefault(BMDataComponents.INCENSE, false) ? 1 : 0);

            ItemProperties.register(BMItems.SIGIL.get(), BMIdentifiers.ItemProperties.SIGIL_ACTIVE,
                    (stack, level, entity, seed) ->
                            stack.has(BMDataComponents.SIGIL_ACTIVE) ? 1 : 0);

            ItemProperties.register(BMItems.SENTIENT_ARMOUR_GEM.get(), BMIdentifiers.ItemProperties.SENTIENT_GEM_ACTIVE,
                    (stack, level, entity, seed) ->
                            stack.has(BMDataComponents.SENTIENT_ARMOUR_GEM_ACTIVE) ? 1 : 0);

            // HUD overlay elements (Demon Will aura, divination readouts, Sigil of Holding contents) and
            // their saved positions - see wayoftime.bloodmagic.client.hud.
            Elements.registerElements();
            ElementRegistry.readConfig();
        });
    }

    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(BMIdentifiers.ModelLoaders.SIGILS, new SigilLoader());
        event.register(BMIdentifiers.ModelLoaders.MIMIC, new MimicLoader(BloodMagic.rl("block/solidopaquemimic")));
        event.register(BMIdentifiers.ModelLoaders.MIMIC_ETHEREAL, new MimicLoader(BloodMagic.rl("block/etherealopaquemimic")));
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(BMIdentifiers.ModelLocations.DIVINATION);
        event.register(BMIdentifiers.ModelLocations.SEER);
        event.register(BMIdentifiers.ModelLocations.LAVA);
        event.register(BMIdentifiers.ModelLocations.WATER);
        event.register(BMIdentifiers.ModelLocations.VOID);
        event.register(BMIdentifiers.ModelLocations.MINER);
        event.register(BMIdentifiers.ModelLocations.AIR);
        event.register(BMIdentifiers.ModelLocations.ICE);
        event.register(BMIdentifiers.ModelLocations.GROWTH);
        event.register(BMIdentifiers.ModelLocations.MAGNETISM);
        event.register(BMIdentifiers.ModelLocations.BLOODLIGHT);
        event.register(BMIdentifiers.ModelLocations.SUPPRESSION);
        event.register(BMIdentifiers.ModelLocations.TELEPOSITION);
    }

    @SubscribeEvent
    public static void registerRenderLayer(EntityRenderersEvent.AddLayers event) {
        // TODO figure out if there is a better way to get a whatever renderer that has #addLayer on it
        PlayerRenderer skin = event.getSkin(PlayerSkin.Model.WIDE);
        skin.addLayer(new LivingElytraLayer<>(skin, event.getEntityModels()));

        skin = event.getSkin(PlayerSkin.Model.SLIM);
        skin.addLayer(new LivingElytraLayer<>(skin, event.getEntityModels()));
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BMModelLayers.METEOR, ModelMeteor::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BMEntities.THROWING_DAGGER.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(BMEntities.THROWING_DAGGER_SYRINGE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(BMEntities.METEOR.get(), EntityMeteorRenderer::new);
        event.registerEntityRenderer(BMEntities.SOUL_SNARE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(BMEntities.POTION_FLASK.get(), ThrownItemRenderer::new);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        FlaskColor flaskColor = new FlaskColor();
        event.register(flaskColor, BMItems.ALCHEMY_FLASK.get(), BMItems.ALCHEMY_FLASK_THROWABLE.get(), BMItems.ALCHEMY_FLASK_LINGERING.get());
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(new MimicColor(), BMBlocks.MIMIC.block().get(), BMBlocks.ETHEREAL_MIMIC.block().get());
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(BMMenus.ARC.get(), ARCScreen::new);
        event.register(BMMenus.LIVING_STATION.get(), LivingStationScreen::new);
        event.register(BMMenus.TRAINER.get(), TrainerScreen::new);
        event.register(BMMenus.ALCHEMY_TABLE.get(), AlchemyTableScreen::new);
        event.register(BMMenus.HOLDING.get(), HoldingScreen::new);
        event.register(BMMenus.FILTER.get(), FilterScreen::new);
    }
}