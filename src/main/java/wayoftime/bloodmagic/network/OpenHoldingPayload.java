package wayoftime.bloodmagic.network;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import wayoftime.bloodmagic.BloodMagic;

/**
 * Sent client -> server when the player presses the "Open Sigil of Holding" key. Carries no data;
 * the server resolves which hand (if any) holds a Sigil of Holding from the sending player.
 */
public record OpenHoldingPayload() implements CustomPacketPayload {
    public static final Type<OpenHoldingPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BloodMagic.MODID, "open_holding"));
    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, OpenHoldingPayload> STREAM_CODEC = StreamCodec.unit(new OpenHoldingPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
