package com.political.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side mirror of the JJK overhaul state synced from the server (Workstream A).
 *
 * <p>The local player's cursed-energy pool is already carried by {@code ClientRpgState} (via
 * {@code StatSyncS2C}); this adds the JJK-specific bits the technique screen / HUD need: the viewer's
 * sorcerer grade, the techniques + domains they have unlocked, the per-slot key bindings (kept purely
 * client-side), and an {@code entityId -> [current, max, grade]} table fed by {@code CursedEnergySyncS2C}
 * for other players.</p>
 */
public final class CursedClientState {

    public static volatile int grade;
    public static volatile float cursedEnergy;
    public static volatile float maxCursedEnergy;

    public static volatile List<String> known = new ArrayList<>();
    public static volatile List<String> domains = new ArrayList<>();

    /** Technique ids bound to the four cast keys (slots 1..4). Empty string = unbound. */
    public static final String[] bound = {"", "", "", ""};

    /** entityId -> [current, max, grade] for every player whose cursed energy has been broadcast. */
    public static final Map<Integer, float[]> ENERGY = new ConcurrentHashMap<>();

    private CursedClientState() {}
}
