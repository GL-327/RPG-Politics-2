package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> client sync of the RPG stats the client cannot derive on its own.
 * Health/max-health are read from the local player; only the RPG layers travel here.
 * Carries both resources: Mana (existing items + serum powers) and Cursed Energy.
 */
public record StatSyncS2C(float defense, float strength, float maxMana, float mana,
                          float maxCursedEnergy, float cursedEnergy) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StatSyncS2C> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "stat_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StatSyncS2C> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, StatSyncS2C::defense,
            ByteBufCodecs.FLOAT, StatSyncS2C::strength,
            ByteBufCodecs.FLOAT, StatSyncS2C::maxMana,
            ByteBufCodecs.FLOAT, StatSyncS2C::mana,
            ByteBufCodecs.FLOAT, StatSyncS2C::maxCursedEnergy,
            ByteBufCodecs.FLOAT, StatSyncS2C::cursedEnergy,
            StatSyncS2C::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
