package com.political.curse;

import com.political.combat.StatManager;
import com.political.curse.domain.DomainManager;
import com.political.curse.domain.Domains;
import com.political.curse.energy.CursedEnergy;
import com.political.curse.energy.CursedEnergyManager;
import com.political.curse.technique.CursedTechniques;
import com.political.curse.technique.TechniqueManager;
import com.political.net.JjkNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

/**
 * Self-contained common-side bootstrap for the JJK overhaul (Workstream A).
 *
 * <p>This file owns <i>all</i> the server-side wiring the overhaul needs so the integrator only has to
 * add a single call. It deliberately does not touch any shared entrypoint; see
 * {@code docs/integration/handoff/A_jjk.md} for the exact one-liners.</p>
 *
 * <p>Integration: add {@code com.political.curse.JjkBootstrap.init();} to
 * {@code RpgPoliticsMod#onInitialize}. (The client half lives in the client source set —
 * {@code com.political.client.JjkClientBootstrap.initClient()} — because Fabric's client classes are
 * not visible from the common source set.)</p>
 */
public final class JjkBootstrap {

    private static boolean initialized;

    private JjkBootstrap() {}

    public static void init() {
        if (initialized) return;
        initialized = true;

        // 1. Data: techniques + domains.
        CursedTechniques.bootstrap();
        Domains.bootstrap();

        // 2. Perception: teach the common CursedEnergy facade how to read server-side pools.
        CursedEnergy.installServerProviders(
                viewer -> viewer instanceof ServerPlayer sp ? StatManager.getMaxCursedEnergy(sp) : 0.0,
                viewer -> viewer instanceof ServerPlayer sp ? StatManager.getCursedEnergy(sp) : 0.0);

        // 3. Networking (payload types + serverbound receivers). Self-contained; do not also wire this
        //    into ModNetworking or types will register twice.
        JjkNetworking.registerA();

        // 4. Server tick: drive active domains and keep every client's cursed-energy view fresh.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            DomainManager.tick(server);
            if (server.getTickCount() % 40 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    CursedEnergyManager.broadcast(player);
                }
            }
        });

        // 5. Tidy per-player state on disconnect.
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.player;
            if (player != null) {
                TechniqueManager.clear(player.getUUID());
                DomainManager.clear(player.getUUID());
            }
        });
    }
}
