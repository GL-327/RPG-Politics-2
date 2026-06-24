package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server -> client: limb bitmask, active preset, and full preset id list for the picker. */
public record JjkProfileS2C(String presetId, int limbMask, String presetIds)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<JjkProfileS2C> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "jjk_profile_s2c"));

    public static final StreamCodec<RegistryFriendlyByteBuf, JjkProfileS2C> CODEC = StreamCodec.of(
            (buf, v) -> {
                buf.writeUtf(v.presetId);
                buf.writeVarInt(v.limbMask);
                buf.writeUtf(v.presetIds);
            },
            buf -> new JjkProfileS2C(buf.readUtf(), buf.readVarInt(), buf.readUtf()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
