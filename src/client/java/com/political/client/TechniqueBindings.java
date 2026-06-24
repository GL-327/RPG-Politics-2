package com.political.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persists client-side JJK bindings, limb toggles, and preset selection across restarts
 * ({@code config/politicalserver_jjk_bindings.json}).
 */
public final class TechniqueBindings {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance()
            .getConfigDir().resolve("politicalserver_jjk_bindings.json");

    private TechniqueBindings() {}

    public static void load() {
        if (!Files.isRegularFile(FILE)) return;
        try {
            String raw = Files.readString(FILE, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
            JsonArray slots = root.getAsJsonArray("slots");
            if (slots != null) {
                for (int i = 0; i < Math.min(4, slots.size()); i++) {
                    CursedClientState.bound[i] = slots.get(i).getAsString();
                }
            }
            if (root.has("limbMask")) {
                CursedClientState.limbMask = root.get("limbMask").getAsInt();
            }
            if (root.has("presetId")) {
                CursedClientState.presetId = root.get("presetId").getAsString();
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        JsonArray slots = new JsonArray();
        for (int i = 0; i < 4; i++) {
            slots.add(CursedClientState.bound[i] == null ? "" : CursedClientState.bound[i]);
        }
        root.add("slots", slots);
        root.addProperty("limbMask", CursedClientState.limbMask);
        root.addProperty("presetId", CursedClientState.presetId == null ? "" : CursedClientState.presetId);
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
