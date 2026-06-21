package com.political.curse.energy;

import com.political.combat.StatManager;
import com.political.curse.SorcererGrade;
import com.political.net.CursedEnergySyncS2C;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-side gameplay API for cursed energy used by the JJK technique/domain systems.
 *
 * <p>Rather than duplicate state, this delegates the actual pool to
 * {@code com.political.combat.StatManager} (the single source of truth, already persisted via
 * {@code DataManager.storedCursedEnergy} and synced to the owner via {@code StatSyncS2C}). On top of
 * that it adds the JJK-flavoured helpers the techniques/domains need (spend-with-feedback, broadcast
 * of a player's cursed-energy snapshot to every client for perception/UI) without editing the shared
 * StatManager or networking registry.</p>
 */
public final class CursedEnergyManager {

    private CursedEnergyManager() {}

    public static double current(ServerPlayer player) {
        return StatManager.getCursedEnergy(player);
    }

    public static double max(ServerPlayer player) {
        return StatManager.getMaxCursedEnergy(player);
    }

    public static boolean has(ServerPlayer player, double cost) {
        return current(player) >= cost;
    }

    /** Spends cursed energy if the player can afford it; returns {@code false} (and spends nothing) otherwise. */
    public static boolean spend(ServerPlayer player, double cost) {
        if (cost <= 0) return true;
        boolean ok = StatManager.spendCursedEnergy(player, cost);
        if (ok) broadcast(player);
        return ok;
    }

    /** Adds cursed energy (clamped to max); returns the amount actually added. */
    public static double add(ServerPlayer player, double amount) {
        double added = StatManager.addCursedEnergy(player, amount);
        if (added != 0) broadcast(player);
        return added;
    }

    /**
     * Broadcasts {@code subject}'s cursed-energy snapshot to every player on the server, so all clients
     * can drive perception cues and the cursed-energy HUD even for other players. Cheap and safe to call
     * after any change.
     */
    public static void broadcast(ServerPlayer subject) {
        MinecraftServer server = subject.level().getServer();
        if (server == null) return;
        CursedEnergySyncS2C payload = snapshot(subject);
        for (ServerPlayer viewer : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(viewer, payload);
        }
    }

    /** Sends every online player's cursed-energy snapshot to {@code target} (used on login / periodic refresh). */
    public static void syncAllTo(ServerPlayer target) {
        MinecraftServer server = target.level().getServer();
        if (server == null) return;
        for (ServerPlayer subject : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(target, snapshot(subject));
        }
    }

    private static CursedEnergySyncS2C snapshot(ServerPlayer subject) {
        return new CursedEnergySyncS2C(
                subject.getId(),
                (float) current(subject),
                (float) max(subject),
                SorcererGrade.of(subject));
    }
}
