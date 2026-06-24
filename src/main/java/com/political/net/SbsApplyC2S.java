package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client → server apply from the {@code /sbs} editor. Carries the full edited stat sheet, rarity,
 * prefix (variant), cursed grade, and prefix-bound ability ids. The server writes these to the
 * player's held item's custom data and refreshes stats.
 */
public record SbsApplyC2S(
        int health, int defense, int strength, int intelligence, int damage,
        int critChance, int critDamage, int ferocity, int speed, int attackSpeed,
        String rarity, String variant, int cursedGrade,
        String prefixPower, String prefixAbility) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SbsApplyC2S> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "sbs_apply"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SbsApplyC2S> CODEC = StreamCodec.of(
            (buf, v) -> {
                buf.writeVarInt(v.health);
                buf.writeVarInt(v.defense);
                buf.writeVarInt(v.strength);
                buf.writeVarInt(v.intelligence);
                buf.writeVarInt(v.damage);
                buf.writeVarInt(v.critChance);
                buf.writeVarInt(v.critDamage);
                buf.writeVarInt(v.ferocity);
                buf.writeVarInt(v.speed);
                buf.writeVarInt(v.attackSpeed);
                buf.writeUtf(v.rarity);
                buf.writeUtf(v.variant);
                buf.writeVarInt(v.cursedGrade);
                buf.writeUtf(v.prefixPower);
                buf.writeUtf(v.prefixAbility);
            },
            buf -> new SbsApplyC2S(
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readUtf(), buf.readUtf(), buf.readVarInt(),
                    buf.readUtf(), buf.readUtf()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
