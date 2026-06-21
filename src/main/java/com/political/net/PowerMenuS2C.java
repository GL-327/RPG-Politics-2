package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> client snapshot used to open/refresh the Powers &amp; Serums GUI. Carries the player's
 * known power ids (comma-separated), the selected power, sorcerer grade + aptitude, and the live
 * Mana / Cursed Energy values so the screen can render bars and node states without extra queries.
 */
public record PowerMenuS2C(String known, String selected, String trait, int grade,
                           int mana, int maxMana, int cursedEnergy, int maxCursedEnergy)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PowerMenuS2C> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "power_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PowerMenuS2C> CODEC = StreamCodec.of(
            (buf, v) -> {
                buf.writeUtf(v.known);
                buf.writeUtf(v.selected);
                buf.writeUtf(v.trait);
                buf.writeVarInt(v.grade);
                buf.writeVarInt(v.mana);
                buf.writeVarInt(v.maxMana);
                buf.writeVarInt(v.cursedEnergy);
                buf.writeVarInt(v.maxCursedEnergy);
            },
            buf -> new PowerMenuS2C(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
