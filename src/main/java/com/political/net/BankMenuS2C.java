package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> client snapshot for the Bank GUI: the player's wallet coins, bank balance, credits,
 * combined net worth, and the daily interest rate (in tenths of a percent, e.g. 20 = 2.0%).
 */
public record BankMenuS2C(int wallet, int bank, int credits, long netWorth, int interestTenths)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BankMenuS2C> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "bank_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BankMenuS2C> CODEC = StreamCodec.of(
            (buf, v) -> {
                buf.writeVarInt(v.wallet);
                buf.writeVarInt(v.bank);
                buf.writeVarInt(v.credits);
                buf.writeVarLong(v.netWorth);
                buf.writeVarInt(v.interestTenths);
            },
            buf -> new BankMenuS2C(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarLong(), buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
