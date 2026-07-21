package wayoftime.bloodmagic.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import wayoftime.bloodmagic.BloodMagic;

/**
 * Sent server -> client to force a player's velocity, e.g. by the Ritual of Speed. Plain
 * server-side {@code Entity.setDeltaMovement} isn't enough for the controlling player - their own
 * client-side movement prediction overrides it next tick - so the server has to push it over the
 * wire, matching 1.20.1's {@code SetClientVelocityPacket}.
 */
public record SetVelocityPayload(double x, double y, double z) implements CustomPacketPayload {
    public static final Type<SetVelocityPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BloodMagic.MODID, "set_velocity"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetVelocityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, SetVelocityPayload::x,
            ByteBufCodecs.DOUBLE, SetVelocityPayload::y,
            ByteBufCodecs.DOUBLE, SetVelocityPayload::z,
            SetVelocityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
