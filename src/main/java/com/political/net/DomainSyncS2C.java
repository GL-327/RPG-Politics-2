package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Clientbound snapshot of one active domain expansion (JJK overhaul). Sent when a domain opens,
 * ticks down while active, and once more with {@code domainId = ""} when it closes.
 */
public record DomainSyncS2C(
        int casterEntityId,
        String domainId,
        double centerX,
        double centerY,
        double centerZ,
        float radius,
        int elementOrdinal,
        int ticksLeft)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DomainSyncS2C> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "jjk_domain_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DomainSyncS2C> CODEC = StreamCodec.of(
            (buf, v) -> {
                buf.writeVarInt(v.casterEntityId());
                buf.writeUtf(v.domainId());
                buf.writeDouble(v.centerX());
                buf.writeDouble(v.centerY());
                buf.writeDouble(v.centerZ());
                buf.writeFloat(v.radius());
                buf.writeVarInt(v.elementOrdinal());
                buf.writeVarInt(v.ticksLeft());
            },
            buf -> new DomainSyncS2C(
                    buf.readVarInt(),
                    buf.readUtf(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readFloat(),
                    buf.readVarInt(),
                    buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
