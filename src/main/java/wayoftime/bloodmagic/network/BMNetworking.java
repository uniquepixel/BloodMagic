package wayoftime.bloodmagic.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.item.SigilHoldingItem;

public class BMNetworking {
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(BloodMagic.MODID);
        registrar.playToServer(OpenHoldingPayload.TYPE, OpenHoldingPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> SigilHoldingItem.openFromKeybind(context.player())));

        registrar.playToClient(SetVelocityPayload.TYPE, SetVelocityPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() != null) {
                        context.player().setDeltaMovement(payload.x(), payload.y(), payload.z());
                    }
                }));
    }
}
