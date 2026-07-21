package wayoftime.bloodmagic.client.hud;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.client.hud.element.HUDElement;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Registry of all draggable HUD overlay elements, their positions (as a fraction of the screen)
 * and their editor box colors, plus the position-persistence (a small JSON file under the mod's
 * config directory) and the actual per-frame overlay render hook.
 * <p>
 * Ported from 1.20.1's {@code ElementRegistry}. The only real change is the render hook: 1.20.1's
 * Forge fired {@code RenderGuiOverlayEvent.Pre} once per registered vanilla overlay layer, and the
 * old code piggybacked on the {@code CHAT_PANEL} layer purely as a "fire once per frame, late in
 * the HUD pass" anchor. NeoForge 21.1 replaced that with {@link RenderGuiEvent}, which already
 * fires exactly once per frame, immediately after every vanilla HUD layer has rendered (and before
 * any open {@link net.minecraft.client.gui.screens.Screen} draws on top) - a strictly better fit
 * for this than the old anchor-layer trick, so {@link RenderGuiEvent.Post} is used directly.
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = BloodMagic.MODID)
public class ElementRegistry {
    private static final File CONFIG = FMLPaths.CONFIGDIR.get().resolve(BloodMagic.MODID).resolve("hud_elements.json").toFile();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<ResourceLocation, HUDElement> HUD_ELEMENTS = Maps.newLinkedHashMap();
    private static final Map<HUDElement, ResourceLocation> REVERSE = Maps.newHashMap();
    private static final Map<ResourceLocation, ElementInfo> ELEMENT_INFO = Maps.newHashMap();

    public static void registerHandler(ResourceLocation key, HUDElement element, Vec2 defaultPosition) {
        HUD_ELEMENTS.put(key, element);
        REVERSE.put(element, key);

        ELEMENT_INFO.put(key, new ElementInfo(defaultPosition, getRandomColor()));
    }

    public static void resetPos() {
        ELEMENT_INFO.values().forEach(ElementInfo::resetPosition);
    }

    public static List<HUDElement> getElements() {
        return ImmutableList.copyOf(HUD_ELEMENTS.values());
    }

    public static ResourceLocation getKey(HUDElement element) {
        return REVERSE.get(element);
    }

    public static int getColor(ResourceLocation element) {
        return ELEMENT_INFO.getOrDefault(element, ElementInfo.DUMMY).getBoxColor();
    }

    public static Vec2 getPosition(ResourceLocation element) {
        return ELEMENT_INFO.get(element).getPosition();
    }

    public static void setPosition(ResourceLocation element, Vec2 point) {
        ELEMENT_INFO.compute(element, (resourceLocation, elementInfo) -> {
            if (elementInfo == null)
                return new ElementInfo(point, getRandomColor());

            elementInfo.setPosition(point);
            return elementInfo;
        });
    }

    public static void save(Map<ResourceLocation, Vec2> newLocations) {
        newLocations.forEach((k, v) -> {
            ElementInfo info = ELEMENT_INFO.get(k);
            if (info != null)
                info.setPosition(v);
        });

        Map<String, Vec2> toWrite = Maps.newHashMap();
        for (Map.Entry<ResourceLocation, ElementInfo> entry : ELEMENT_INFO.entrySet())
            toWrite.put(entry.getKey().toString(), entry.getValue().getPosition());

        String json = GSON.toJson(toWrite);
        FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve(BloodMagic.MODID));
        try (FileWriter writer = new FileWriter(CONFIG)) {
            writer.write(json);
        } catch (Exception e) {
            BloodMagic.LOGGER.error("Failed to save HUD element positions", e);
        }
    }

    public static void readConfig() {
        if (!CONFIG.exists())
            return;

        try (FileReader reader = new FileReader(CONFIG)) {
            Map<String, Vec2> toLoad = GSON.fromJson(reader, new TypeToken<Map<String, Vec2>>() {
            }.getType());
            for (Map.Entry<String, Vec2> entry : toLoad.entrySet()) {
                ElementInfo info = ELEMENT_INFO.get(ResourceLocation.parse(entry.getKey()));
                if (info != null)
                    info.setPosition(entry.getValue());
            }
        } catch (Exception e) {
            BloodMagic.LOGGER.error("Failed to load HUD element positions", e);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        Window window = minecraft.getWindow();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        for (HUDElement element : HUD_ELEMENTS.values()) {
            if (!element.shouldRender(minecraft))
                continue;

            Vec2 position = ELEMENT_INFO.get(getKey(element)).getPosition();
            int xPos = (int) (window.getGuiScaledWidth() * position.x);
            int yPos = (int) (window.getGuiScaledHeight() * position.y);
            element.draw(event.getGuiGraphics(), partialTick, xPos, yPos);
        }
    }

    public static int getRandomColor() {
        Random rand = new Random();
        float r = rand.nextFloat() / 2F + 0.5F;
        float g = rand.nextFloat() / 2F + 0.5F;
        float b = rand.nextFloat() / 2F + 0.5F;
        float a = 0.5F;
        return new Color(r, g, b, a).getRGB();
    }
}
