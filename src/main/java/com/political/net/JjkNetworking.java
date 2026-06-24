package com.political.net;

import com.political.curse.domain.DomainManager;
import com.political.curse.technique.TechniqueManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Self-contained networking registration for the JJK overhaul (Workstream A).
 *
 * <p>This does <b>not</b> touch {@code ModNetworking}'s own registry. The integrator should call
 * {@link #registerA()} exactly once during common init (it is already invoked from
 * {@code com.political.curse.JjkBootstrap#init()}, so wiring {@code JjkBootstrap.init()} is enough —
 * do not also call this directly or payload types will be registered twice).</p>
 */
public final class JjkNetworking {

    private static boolean registered;

    private JjkNetworking() {}

    /** Registers all JJK payload types (S2C + C2S) and the serverbound receivers. Idempotent. */
    public static void registerA() {
        if (registered) return;
        registered = true;

        // Clientbound types.
        PayloadTypeRegistry.clientboundPlay().register(CursedEnergySyncS2C.TYPE, CursedEnergySyncS2C.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TechniqueMenuS2C.TYPE, TechniqueMenuS2C.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DomainSyncS2C.TYPE, DomainSyncS2C.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TechniqueCastS2C.TYPE, TechniqueCastS2C.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(JjkProfileS2C.TYPE, JjkProfileS2C.CODEC);

        // Serverbound types + receivers.
        PayloadTypeRegistry.serverboundPlay().register(TechniqueActionC2S.TYPE, TechniqueActionC2S.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TechniqueActionC2S.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                switch (payload.action()) {
                    case "open" -> TechniqueManager.openMenu(player);
                    case "cast", "cast_slot" -> {
                        Component result = TechniqueManager.cast(player, payload.techniqueId());
                        if (result != null) player.sendSystemMessage(result, true);
                    }
                    default -> { }
                }
            });
        });

        PayloadTypeRegistry.serverboundPlay().register(DomainActionC2S.TYPE, DomainActionC2S.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DomainActionC2S.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                Component result = DomainManager.expand(player, payload.domainId());
                if (result != null) player.sendSystemMessage(result, true);
            });
        });

        PayloadTypeRegistry.serverboundPlay().register(JjkProfileC2S.TYPE, JjkProfileC2S.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(JjkProfileC2S.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleProfile(player, payload));
        });
    }

    private static void handleProfile(ServerPlayer player, JjkProfileC2S payload) {
        switch (payload.action()) {
            case "toggle_limb" -> {
                if (payload.limbOrdinal() >= 0 && payload.limbOrdinal() < com.political.curse.limb.CursedLimb.values().length) {
                    com.political.curse.limb.LimbStateManager.toggleLimb(
                            player, com.political.curse.limb.CursedLimb.values()[payload.limbOrdinal()], payload.enabled());
                }
            }
            case "set_preset" -> com.political.curse.limb.LimbStateManager.setPreset(player, payload.presetId());
            default -> com.political.curse.limb.LimbStateManager.sync(player);
        }
    }
}
