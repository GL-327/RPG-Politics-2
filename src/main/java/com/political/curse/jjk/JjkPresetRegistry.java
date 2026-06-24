package com.political.curse.jjk;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.political.RpgPoliticsMod;
import net.minecraft.util.StringUtil;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads {@code data/politicalserver/jjk/presets.json} (ported from People Playground JJP) and exposes
 * scaled cursed-energy profiles for the limb / technique screen.
 */
public final class JjkPresetRegistry {

    /** PP RealMaxEnergy is orders of magnitude above our pool — divide to fit StatManager. */
    private static final double CE_SCALE = 350.0;

    private static final Map<String, JjkPreset> BY_ID = new LinkedHashMap<>();
    private static boolean loaded;

    private JjkPresetRegistry() {}

    public static void bootstrap() {
        if (loaded) return;
        loaded = true;
        BY_ID.clear();
        try (var stream = JjkPresetRegistry.class.getClassLoader()
                .getResourceAsStream("data/politicalserver/jjk/presets.json")) {
            if (stream == null) {
                RpgPoliticsMod.LOGGER.warn("JJP presets.json missing from classpath");
                return;
            }
            JsonArray root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonArray();
            for (JsonElement el : root) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();
                String name = obj.get("name").getAsString();
                double recovery = obj.has("RecoveryEnergyMultiplier")
                        ? obj.get("RecoveryEnergyMultiplier").getAsDouble() : 1.0;
                double realMax = obj.has("RealMaxEnergy")
                        ? obj.get("RealMaxEnergy").getAsDouble() : 0.0;
                String id = slug(name);
                if (id.isEmpty() || BY_ID.containsKey(id)) continue;
                double scaled = realMax <= 0 ? 0 : Math.min(500, realMax / CE_SCALE);
                BY_ID.put(id, new JjkPreset(id, cleanName(name), recovery, scaled));
            }
            RpgPoliticsMod.LOGGER.info("Loaded {} JJP energy presets", BY_ID.size());
        } catch (Exception e) {
            RpgPoliticsMod.LOGGER.error("Failed to load JJP presets", e);
        }
    }

    public static JjkPreset byId(String id) {
        if (id == null || id.isBlank()) return null;
        return BY_ID.get(id);
    }

    public static List<JjkPreset> all() {
        return Collections.unmodifiableList(new ArrayList<>(BY_ID.values()));
    }

    public static String idListCsv() {
        return String.join(",", BY_ID.keySet());
    }

    public static double bonusMaxCe(String presetId) {
        JjkPreset p = byId(presetId);
        return p == null ? 0.0 : p.scaledMaxCe();
    }

    public static double regenMultiplier(String presetId) {
        JjkPreset p = byId(presetId);
        if (p == null || p.recoveryMultiplier() <= 0) return 1.0;
        return Math.min(5.0, p.recoveryMultiplier());
    }

    private static String slug(String name) {
        String cleaned = name.replace("[JJP]", "").trim().toLowerCase(Locale.ROOT);
        cleaned = cleaned.replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
        return cleaned;
    }

    private static String cleanName(String name) {
        return StringUtil.stripColor(name.replace("[JJP]", "").trim());
    }
}
