package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * Server → client: a player just cast a cursed technique — play the native cast-pose animation.
 */
public record TechniqueCastS2C(UUID playerUuid) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TechniqueCastS2C> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "jjk_technique_cast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TechniqueCastS2C> CODEC = StreamCodec.of(
            (buf, v) -> buf.writeUUID(v.playerUuid()),
            buf -> new TechniqueCastS2C(buf.readUUID()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
