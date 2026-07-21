package wayoftime.bloodmagic.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import wayoftime.bloodmagic.client.hud.HudEditScreen;
import wayoftime.bloodmagic.network.OpenHoldingPayload;

public class BMKeyMappings {
    public static final KeyMapping OPEN_HOLDING = new KeyMapping(
            "key.bloodmagic.open_holding", InputConstants.UNKNOWN.getValue(), "key.categories.bloodmagic"
    );

    // Not sent to the server as a payload like OPEN_HOLDING - the HUD editor (wayoftime.bloodmagic.client.hud)
    // is 100% client-side (just repositioning overlay elements), so the keybind opens it directly.
    public static final KeyMapping OPEN_HUD_EDITOR = new KeyMapping(
            "key.bloodmagic.open_hud_editor", InputConstants.UNKNOWN.getValue(), "key.categories.bloodmagic"
    );

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_HOLDING);
        event.register(OPEN_HUD_EDITOR);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        while (OPEN_HOLDING.consumeClick()) {
            PacketDistributor.sendToServer(new OpenHoldingPayload());
        }
        while (OPEN_HUD_EDITOR.consumeClick()) {
            Minecraft.getInstance().setScreen(new HudEditScreen());
        }
    }
}
