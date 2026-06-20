package com.political.net;

import com.political.power.PowerManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/** Registration + send helpers for mod payloads (26.2 / Mojang mappings). */
public final class ModNetworking {

    private ModNetworking() {}

    /** Clientbound payload types. Call during common init. */
    public static void registerS2CTypes() {
        PayloadTypeRegistry.clientboundPlay().register(StatSyncS2C.TYPE, StatSyncS2C.CODEC);
    }

    /** Serverbound payload types + receivers. Call during common init. */
    public static void registerC2STypes() {
        PayloadTypeRegistry.serverboundPlay().register(ActivatePowerC2S.TYPE, ActivatePowerC2S.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ActivatePowerC2S.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                Component result = PowerManager.activateSelected(player);
                player.sendSystemMessage(result, true);
            });
        });
    }

    public static void send(ServerPlayer player, CustomPacketPayload payload) {
        if (player != null) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
