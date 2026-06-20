package com.political.net;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Server to client sync of the RPG stats the client cannot derive on its own.
 * Health and max health are read directly from the local player on the client,
 * so only defense, strength, mana and coins are transmitted here.
 */
public record StatSyncS2C(float defense, float strength, float maxMana, float mana, int coins)
        implements CustomPayload {

    public static final CustomPayload.Id<StatSyncS2C> ID =
            new CustomPayload.Id<>(Identifier.of("politicalserver", "stat_sync"));

    public static final PacketCodec<RegistryByteBuf, StatSyncS2C> CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, StatSyncS2C::defense,
            PacketCodecs.FLOAT, StatSyncS2C::strength,
            PacketCodecs.FLOAT, StatSyncS2C::maxMana,
            PacketCodecs.FLOAT, StatSyncS2C::mana,
            PacketCodecs.INTEGER, StatSyncS2C::coins,
            StatSyncS2C::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
