package com.political.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Broadcast snapshot of one player's cursed-energy pool (JJK overhaul, Workstream A).
 *
 * <p>Unlike {@code StatSyncS2C} (owner-only), this is sent to <i>every</i> client so they can drive
 * curse-perception cues and the cursed-energy HUD for any player, keyed by {@link #entityId()}.</p>
 */
public record CursedEnergySyncS2C(int entityId, float cursedEnergy, float maxCursedEnergy, int grade)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CursedEnergySyncS2C> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("politicalserver", "jjk_ce_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CursedEnergySyncS2C> CODEC = StreamCodec.of(
            (buf, v) -> {
                buf.writeVarInt(v.entityId);
                buf.writeFloat(v.cursedEnergy);
                buf.writeFloat(v.maxCursedEnergy);
                buf.writeVarInt(v.grade);
            },
            buf -> new CursedEnergySyncS2C(buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
