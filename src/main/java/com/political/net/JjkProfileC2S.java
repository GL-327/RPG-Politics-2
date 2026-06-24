package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client -> server: toggle a limb or select a JJP energy preset. */
public record JjkProfileC2S(String action, String presetId, int limbOrdinal, boolean enabled)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<JjkProfileC2S> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "jjk_profile_c2s"));

    public static final StreamCodec<RegistryFriendlyByteBuf, JjkProfileC2S> CODEC = StreamCodec.of(
            (buf, v) -> {
                buf.writeUtf(v.action);
                buf.writeUtf(v.presetId);
                buf.writeVarInt(v.limbOrdinal);
                buf.writeBoolean(v.enabled);
            },
            buf -> new JjkProfileC2S(buf.readUtf(), buf.readUtf(), buf.readVarInt(), buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
