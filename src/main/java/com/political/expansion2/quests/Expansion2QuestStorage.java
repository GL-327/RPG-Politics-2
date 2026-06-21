package com.political.expansion2.quests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.political.RpgPoliticsMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Expansion2QuestStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Expansion2QuestData data = new Expansion2QuestData();

    private Expansion2QuestStorage() {}

    public static Expansion2QuestData data() { return data; }

    public static void load(MinecraftServer server) {
        Path file = server.getWorldPath(LevelResource.ROOT).resolve("expansion2_quests.json");
        if (Files.exists(file)) {
            try {
                Expansion2QuestData loaded = GSON.fromJson(Files.readString(file), Expansion2QuestData.class);
                if (loaded != null) data = loaded;
            } catch (IOException | RuntimeException e) {
                RpgPoliticsMod.LOGGER.error("Failed to load expansion2_quests.json", e);
            }
        }
    }

    public static void save(MinecraftServer server) {
        Path file = server.getWorldPath(LevelResource.ROOT).resolve("expansion2_quests.json");
        try {
            Files.writeString(file, GSON.toJson(data));
        } catch (IOException e) {
            RpgPoliticsMod.LOGGER.error("Failed to save expansion2_quests.json", e);
        }
    }
}
