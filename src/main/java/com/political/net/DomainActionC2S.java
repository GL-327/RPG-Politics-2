package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client -> server request to expand a domain by id (JJK overhaul). An empty {@code domainId} asks the
 * server to expand the player's best available domain.
 */
public record DomainActionC2S(String domainId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DomainActionC2S> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "jjk_domain_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DomainActionC2S> CODEC = StreamCodec.of(
            (buf, v) -> buf.writeUtf(v.domainId),
            buf -> new DomainActionC2S(buf.readUtf()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
