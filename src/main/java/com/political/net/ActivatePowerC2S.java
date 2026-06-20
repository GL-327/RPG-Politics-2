package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client -> server signal that the player pressed the "activate power" key. */
public record ActivatePowerC2S() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ActivatePowerC2S> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "activate_power"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ActivatePowerC2S> CODEC =
            StreamCodec.unit(new ActivatePowerC2S());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
