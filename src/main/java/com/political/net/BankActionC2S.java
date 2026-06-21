package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client -> server action from the Bank GUI. {@code action} is {@code open}, {@code deposit},
 * {@code withdraw}, {@code depositAll}, or {@code withdrawAll}; {@code amount} applies to the
 * deposit/withdraw variants.
 */
public record BankActionC2S(String action, int amount) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BankActionC2S> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "bank_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BankActionC2S> CODEC = StreamCodec.of(
            (buf, v) -> {
                buf.writeUtf(v.action);
                buf.writeVarInt(v.amount);
            },
            buf -> new BankActionC2S(buf.readUtf(), buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
