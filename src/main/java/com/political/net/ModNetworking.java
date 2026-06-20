package com.political.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/** Registration + send helpers for clientbound payloads (26.2 / Mojang mappings). */
public final class ModNetworking {

    private ModNetworking() {}

    public static void registerS2CTypes() {
        PayloadTypeRegistry.clientboundPlay().register(StatSyncS2C.TYPE, StatSyncS2C.CODEC);
    }

    public static void send(ServerPlayer player, CustomPacketPayload payload) {
        if (player != null) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
