package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> client sync of the RPG stats the client cannot derive on its own.
 * Health/max-health are read from the local player; only the RPG layers travel here.
 * Carries both resources (Mana + Cursed Energy) plus the offensive stats shown on the HUD.
 */
public record StatSyncS2C(float defense, float strength, float maxMana, float mana,
                          float maxCursedEnergy, float cursedEnergy,
                          float critChance, float ferocity, float speed) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StatSyncS2C> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "stat_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StatSyncS2C> CODEC = StreamCodec.of(
            (buf, v) -> {
                buf.writeFloat(v.defense);
                buf.writeFloat(v.strength);
                buf.writeFloat(v.maxMana);
                buf.writeFloat(v.mana);
                buf.writeFloat(v.maxCursedEnergy);
                buf.writeFloat(v.cursedEnergy);
                buf.writeFloat(v.critChance);
                buf.writeFloat(v.ferocity);
                buf.writeFloat(v.speed);
            },
            buf -> new StatSyncS2C(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
                    buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
