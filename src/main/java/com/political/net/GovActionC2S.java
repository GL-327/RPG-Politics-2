package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client -> server action from the Governance GUI. {@code action} is {@code open}, {@code paytax}
 * (pay all owed), or {@code vote} (with {@code arg} = the candidate's UUID).
 */
public record GovActionC2S(String action, String arg) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<GovActionC2S> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "gov_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GovActionC2S> CODEC = StreamCodec.of(
            (buf, v) -> {
                buf.writeUtf(v.action);
                buf.writeUtf(v.arg);
            },
            buf -> new GovActionC2S(buf.readUtf(), buf.readUtf()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
