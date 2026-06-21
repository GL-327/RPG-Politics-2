package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client -> server action from the Powers GUI.
 * {@code action} is one of {@code open} (request a fresh menu), {@code select} (set the active
 * power to {@code powerId}), or {@code activate} (cast the currently selected power).
 */
public record PowerActionC2S(String action, String powerId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PowerActionC2S> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "power_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PowerActionC2S> CODEC = StreamCodec.of(
            (buf, v) -> {
                buf.writeUtf(v.action);
                buf.writeUtf(v.powerId);
            },
            buf -> new PowerActionC2S(buf.readUtf(), buf.readUtf()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
