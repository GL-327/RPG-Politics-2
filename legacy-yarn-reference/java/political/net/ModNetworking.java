package com.political.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

/** Central registration + send helpers for all custom server-to-client payloads. */
public final class ModNetworking {

    private ModNetworking() {}

    /** Registers every server-to-client payload type. Must run on both sides during init. */
    public static void registerS2CTypes() {
        PayloadTypeRegistry.playS2C().register(StatSyncS2C.ID, StatSyncS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(CourtStartS2C.ID, CourtStartS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(CourtTimerS2C.ID, CourtTimerS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(CourtEndS2C.ID, CourtEndS2C.CODEC);
    }

    public static void send(ServerPlayerEntity player, CustomPayload payload) {
        if (player != null) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
