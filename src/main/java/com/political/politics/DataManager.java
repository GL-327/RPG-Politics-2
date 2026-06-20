package com.political.politics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.political.RpgPoliticsMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/** Loads/saves {@link PoliticsData} to {world}/political_data.json and exposes role helpers. */
public final class DataManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static PoliticsData data = new PoliticsData();

    private DataManager() {}

    public static PoliticsData data() {
        return data;
    }

    public static void load(MinecraftServer server) {
        Path file = server.getWorldPath(LevelResource.ROOT).resolve("political_data.json");
        if (Files.exists(file)) {
            try {
                PoliticsData loaded = GSON.fromJson(Files.readString(file), PoliticsData.class);
                if (loaded != null) data = loaded;
            } catch (IOException | RuntimeException e) {
                RpgPoliticsMod.LOGGER.error("Failed to load political_data.json", e);
            }
        }
    }

    public static void save(MinecraftServer server) {
        Path file = server.getWorldPath(LevelResource.ROOT).resolve("political_data.json");
        try {
            Files.writeString(file, GSON.toJson(data));
        } catch (IOException e) {
            RpgPoliticsMod.LOGGER.error("Failed to save political_data.json", e);
        }
    }

    // --- Player registry ---

    public static void registerPlayer(ServerPlayer player) {
        data.playerNames.put(player.getStringUUID(), player.getName().getString());
    }

    public static String nameOf(String uuid) {
        return data.playerNames.getOrDefault(uuid, uuid);
    }

    // --- Roles ---

    public static Role roleOf(String uuid) {
        if (uuid == null || uuid.isEmpty()) return Role.NONE;
        if (uuid.equals(data.dictator) && data.dictatorActive) return Role.DICTATOR;
        if (uuid.equals(data.chair)) return Role.CHAIR;
        if (uuid.equals(data.viceChair)) return Role.VICE_CHAIR;
        if (uuid.equals(data.judge)) return Role.JUDGE;
        return Role.NONE;
    }

    public static boolean isChair(UUID uuid) {
        return uuid.toString().equals(data.chair);
    }

    public static boolean isJudge(UUID uuid) {
        String s = uuid.toString();
        return s.equals(data.judge) || (data.dictatorActive && s.equals(data.dictator));
    }

    public static void setChair(String uuid) {
        if (!data.chair.isEmpty() && !data.chair.equals(uuid)) {
            PerkManager.clearAllPerks();
        }
        data.chair = uuid == null ? "" : uuid;
        data.chairPerksSetThisTerm = false;
    }

    public static void setViceChair(String uuid) {
        data.viceChair = uuid == null ? "" : uuid;
        data.viceChairPerksSetThisTerm = false;
    }

    public static void setJudge(String uuid) {
        data.judge = uuid == null ? "" : uuid;
    }

    // --- Coins (economy foundation) ---

    public static int getCoins(String uuid) {
        return data.coins.getOrDefault(uuid, 0);
    }

    public static void addCoins(String uuid, int amount) {
        data.coins.merge(uuid, amount, Integer::sum);
    }

    public static boolean removeCoins(String uuid, int amount) {
        int have = getCoins(uuid);
        if (have < amount) return false;
        data.coins.put(uuid, have - amount);
        return true;
    }

    // --- Treasury ---

    public static int getTreasury() {
        return data.treasury;
    }

    public static void addTreasury(int amount) {
        data.treasury += amount;
    }

    public static boolean removeTreasury(int amount) {
        if (data.treasury < amount) return false;
        data.treasury -= amount;
        return true;
    }

    // --- Credits (premium currency; 1000 coins == 1 credit) ---

    public static final int COINS_PER_CREDIT = 1000;

    public static int getCredits(String uuid) {
        return data.credits.getOrDefault(uuid, 0);
    }

    public static void addCredits(String uuid, int amount) {
        data.credits.merge(uuid, amount, Integer::sum);
    }

    public static boolean removeCredits(String uuid, int amount) {
        int have = getCredits(uuid);
        if (have < amount) return false;
        data.credits.put(uuid, have - amount);
        return true;
    }

    /** Combined net worth in coins (coins + bank + credits*1000). */
    public static long netWorth(String uuid) {
        return (long) getCoins(uuid)
                + data.bank.getOrDefault(uuid, 0)
                + (long) getCredits(uuid) * COINS_PER_CREDIT;
    }

    // --- Titles ---

    public static String titleOf(String uuid) {
        return data.titles.get(uuid);
    }
}
