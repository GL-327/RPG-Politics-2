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
 * Persists the four client-side technique slot bindings across restarts
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
            if (slots == null) return;
            for (int i = 0; i < Math.min(4, slots.size()); i++) {
                CursedClientState.bound[i] = slots.get(i).getAsString();
            }
        } catch (Exception ignored) {
            // Corrupt config — start fresh.
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        JsonArray slots = new JsonArray();
        for (int i = 0; i < 4; i++) {
            slots.add(CursedClientState.bound[i] == null ? "" : CursedClientState.bound[i]);
        }
        root.add("slots", slots);
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
