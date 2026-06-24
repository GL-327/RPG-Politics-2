package com.political.curse.limb;

import com.political.combat.StatManager;
import com.political.curse.jjk.JjkPresetRegistry;
import com.political.net.JjkProfileS2C;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Per-player limb toggles and selected JJP energy preset (People Playground port). */
public final class LimbStateManager {

    private record Profile(int limbMask, String presetId) {}

    private static final Map<UUID, Profile> PROFILES = new ConcurrentHashMap<>();

    private LimbStateManager() {}

    public static int limbMask(ServerPlayer player) {
        return profile(player).limbMask;
    }

    public static String presetId(ServerPlayer player) {
        return profile(player).presetId;
    }

    public static boolean limbEnabled(ServerPlayer player, CursedLimb limb) {
        return CursedLimb.isEnabled(limbMask(player), limb);
    }

    public static boolean canCast(ServerPlayer player, String techniqueId) {
        CursedLimb limb = LimbTechniqueMap.limbFor(techniqueId);
        return limbEnabled(player, limb);
    }

    public static void toggleLimb(ServerPlayer player, CursedLimb limb, boolean enabled) {
        Profile cur = profile(player);
        int next = CursedLimb.toggle(cur.limbMask, limb, enabled);
        PROFILES.put(player.getUUID(), new Profile(next, cur.presetId));
        sync(player);
    }

    public static void setPreset(ServerPlayer player, String presetId) {
        Profile cur = profile(player);
        String id = presetId == null ? "" : presetId.trim();
        if (!id.isEmpty() && JjkPresetRegistry.byId(id) == null) return;
        PROFILES.put(player.getUUID(), new Profile(cur.limbMask, id));
        StatManager.apply(player);
        sync(player);
    }

    public static void sync(ServerPlayer player) {
        Profile p = profile(player);
        ServerPlayNetworking.send(player, new JjkProfileS2C(
                p.presetId,
                p.limbMask,
                JjkPresetRegistry.idListCsv()));
    }

    public static void clear(UUID uuid) {
        PROFILES.remove(uuid);
    }

    private static Profile profile(ServerPlayer player) {
        return PROFILES.computeIfAbsent(player.getUUID(),
                u -> new Profile(CursedLimb.ALL_ENABLED_MASK, ""));
    }
}
