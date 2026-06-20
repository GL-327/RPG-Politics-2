package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/** Server -> client: open the Developer Menu pre-filled with the current config values. */
public record DevMenuOpenS2C(List<Float> values) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DevMenuOpenS2C> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "dev_menu_open"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DevMenuOpenS2C> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), DevMenuOpenS2C::values,
            DevMenuOpenS2C::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
