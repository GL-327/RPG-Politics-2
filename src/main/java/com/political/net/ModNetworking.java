package com.political.net;

import com.political.expansion2.powers.PowerBridge;
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
        PayloadTypeRegistry.clientboundPlay().register(DevMenuOpenS2C.TYPE, DevMenuOpenS2C.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DialogueOpenS2C.TYPE, DialogueOpenS2C.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PowerMenuS2C.TYPE, PowerMenuS2C.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BankMenuS2C.TYPE, BankMenuS2C.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GovMenuS2C.TYPE, GovMenuS2C.CODEC);
    }

    /** Serverbound payload types + receivers. Call during common init. */
    public static void registerC2STypes() {
        PayloadTypeRegistry.serverboundPlay().register(ActivatePowerC2S.TYPE, ActivatePowerC2S.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(DialogueChooseC2S.TYPE, DialogueChooseC2S.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DialogueChooseC2S.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() ->
                    com.political.npc.DialogueManager.handleChoice(player, payload.choiceId(), payload.action()));
        });

        ServerPlayNetworking.registerGlobalReceiver(ActivatePowerC2S.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                Component result = PowerBridge.activateSelected(player);
                if (result != null) player.sendSystemMessage(result, true);
            });
        });

        PayloadTypeRegistry.serverboundPlay().register(PowerActionC2S.TYPE, PowerActionC2S.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PowerActionC2S.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> PowerBridge.handleAction(player, payload.action(), payload.powerId()));
        });

        PayloadTypeRegistry.serverboundPlay().register(BankActionC2S.TYPE, BankActionC2S.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BankActionC2S.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() ->
                    com.political.economy.BankManager.handleAction(player, payload.action(), payload.amount()));
        });

        PayloadTypeRegistry.serverboundPlay().register(GovActionC2S.TYPE, GovActionC2S.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(GovActionC2S.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() ->
                    com.political.politics.GovMenu.handleAction(player, payload.action(), payload.arg()));
        });

        PayloadTypeRegistry.serverboundPlay().register(DevConfigSetC2S.TYPE, DevConfigSetC2S.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DevConfigSetC2S.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                if (!com.political.dev.DevMenuItem.isDev(player)) return;
                com.political.dev.DevConfigKey key = com.political.dev.DevConfigKey.byName(payload.key());
                if (key != null) com.political.dev.DevConfig.set(key, payload.value());
            });
        });
    }

    public static void send(ServerPlayer player, CustomPacketPayload payload) {
        if (player != null) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
