package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server → client snapshot opening the {@code /sbs} Skyblock-stats editor: the held item's display
 * name plus every editable Skyblock stat, its rarity, its prefix (variant), the cursed grade, and
 * the prefix-bound ability ids. {@code hasItem} is {@code false} when the player holds nothing,
 * which the screen renders as an empty-hands notice.
 */
public record SbsOpenS2C(
        boolean hasItem,
        String itemName,
        int health, int defense, int strength, int intelligence, int damage,
        int critChance, int critDamage, int ferocity, int speed, int attackSpeed,
        String rarity, String variant, int cursedGrade,
        String prefixPower, String prefixAbility) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SbsOpenS2C> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "sbs_open"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SbsOpenS2C> CODEC = StreamCodec.of(
            (buf, v) -> {
                buf.writeBoolean(v.hasItem);
                buf.writeUtf(v.itemName);
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
            buf -> new SbsOpenS2C(
                    buf.readBoolean(),
                    buf.readUtf(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readUtf(), buf.readUtf(), buf.readVarInt(),
                    buf.readUtf(), buf.readUtf()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
