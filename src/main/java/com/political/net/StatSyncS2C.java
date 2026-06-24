package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> client sync of the RPG stats the client cannot derive on its own.
 * Health/max-health are read from the local player; only the RPG layers travel here.
 * Carries both resources (Mana + Cursed Energy) plus the offensive stats shown on the HUD,
 * and a compact JJK state bitfield for live aura indicators (Black Flash zone, RCT, flow, etc.).
 */
public record StatSyncS2C(float defense, float strength, float maxMana, float mana,
                          float maxCursedEnergy, float cursedEnergy,
                          float critChance, float ferocity, float speed,
                          int sorcererGrade, int jjkFlags) implements CustomPacketPayload {

    /** Bit flags mirrored by {@link com.political.curse.rules.JjkRules#packHudFlags}. */
    public static final int FLAG_BLACK_FLASH_ZONE = 1;
    public static final int FLAG_BLACK_FLASH_PRIMED = 2;
    public static final int FLAG_RCT = 4;
    public static final int FLAG_FLOW = 8;
    public static final int FLAG_SIMPLE_DOMAIN = 16;
    public static final int FLAG_FALLING_BLOSSOM = 32;
    public static final int FLAG_BINDING_VOW = 64;

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
                buf.writeVarInt(v.sorcererGrade);
                buf.writeVarInt(v.jjkFlags);
            },
            buf -> new StatSyncS2C(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
                    buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
                    buf.readVarInt(), buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
