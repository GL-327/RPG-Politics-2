package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client -> server: set one Developer Menu config value (op/creative gated server-side). */
public record DevConfigSetC2S(String key, float value) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DevConfigSetC2S> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "dev_config_set"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DevConfigSetC2S> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, DevConfigSetC2S::key,
            ByteBufCodecs.FLOAT, DevConfigSetC2S::value,
            DevConfigSetC2S::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
