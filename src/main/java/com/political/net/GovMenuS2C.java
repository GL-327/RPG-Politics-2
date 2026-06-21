package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> client snapshot for the Governance GUI. Carries the current officials, treasury,
 * election + tax status, the viewing player's citizenship/rank/owed tax, the active perk summary,
 * and (during an election) a {@code candidates} list of {@code uuid|name} entries separated by ';'.
 */
public record GovMenuS2C(String chair, String vice, String judge, String dictator,
                         String electionStatus, String taxStatus, String citizenship, String rank,
                         String perks, int treasury, int coins, int taxOwed, String candidates)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<GovMenuS2C> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "gov_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GovMenuS2C> CODEC = StreamCodec.of(
            (buf, v) -> {
                buf.writeUtf(v.chair);
                buf.writeUtf(v.vice);
                buf.writeUtf(v.judge);
                buf.writeUtf(v.dictator);
                buf.writeUtf(v.electionStatus);
                buf.writeUtf(v.taxStatus);
                buf.writeUtf(v.citizenship);
                buf.writeUtf(v.rank);
                buf.writeUtf(v.perks);
                buf.writeVarInt(v.treasury);
                buf.writeVarInt(v.coins);
                buf.writeVarInt(v.taxOwed);
                buf.writeUtf(v.candidates);
            },
            buf -> new GovMenuS2C(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(),
                    buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readUtf()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
