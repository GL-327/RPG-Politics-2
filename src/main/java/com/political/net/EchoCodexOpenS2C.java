package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Opens the Echo Archive lore screen on the client. */
public record EchoCodexOpenS2C(int chapter) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EchoCodexOpenS2C> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "echo_codex_open"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EchoCodexOpenS2C> CODEC = StreamCodec.of(
            (buf, v) -> buf.writeVarInt(v.chapter),
            buf -> new EchoCodexOpenS2C(buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
