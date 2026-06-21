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

    // --- Powers ---

    public static java.util.List<String> knownPowers(String uuid) {
        return data.knownPowers.computeIfAbsent(uuid, k -> new java.util.ArrayList<>());
    }

    public static boolean hasPower(String uuid, String powerId) {
        return knownPowers(uuid).contains(powerId);
    }

    public static boolean grantPower(String uuid, String powerId) {
        java.util.List<String> list = knownPowers(uuid);
        if (list.contains(powerId)) return false;
        list.add(powerId);
        if (data.selectedPower.get(uuid) == null) data.selectedPower.put(uuid, powerId);
        return true;
    }

    public static void revokeAllPowers(String uuid) {
        knownPowers(uuid).clear();
        data.selectedPower.remove(uuid);
        data.tempPowerExpiry.remove(uuid);
        data.tempPowerId.remove(uuid);
        data.tempPowerPrevSelected.remove(uuid);
    }

    /** Removes a single power; resets the selection to another known power if needed. */
    public static void revokePower(String uuid, String powerId) {
        java.util.List<String> list = knownPowers(uuid);
        list.remove(powerId);
        if (powerId.equals(data.selectedPower.get(uuid))) {
            if (list.isEmpty()) data.selectedPower.remove(uuid);
            else data.selectedPower.put(uuid, list.get(0));
        }
    }

    public static String selectedPower(String uuid) {
        return data.selectedPower.get(uuid);
    }

    public static void setSelectedPower(String uuid, String powerId) {
        data.selectedPower.put(uuid, powerId);
    }

    // --- Sorcerer grade ---

    public static int sorcererGrade(String uuid) {
        return data.sorcererGrade.getOrDefault(uuid, 0);
    }

    public static void setSorcererGrade(String uuid, int grade) {
        data.sorcererGrade.put(uuid, Math.max(0, Math.min(5, grade)));
    }

    // --- Cursed energy (current value, persisted across relog) ---

    public static double storedCursedEnergy(String uuid) {
        return data.cursedEnergyStored.getOrDefault(uuid, 0.0);
    }

    public static void setStoredCursedEnergy(String uuid, double value) {
        data.cursedEnergyStored.put(uuid, Math.max(0.0, value));
    }

    /** Grade label: 0 none, 1 Grade 4 ... 5 Special Grade (JJK-style, inverted scale). */
    public static String gradeLabel(int grade) {
        return switch (grade) {
            case 1 -> "Grade 4 Sorcerer";
            case 2 -> "Grade 3 Sorcerer";
            case 3 -> "Grade 2 Sorcerer";
            case 4 -> "Grade 1 Sorcerer";
            case 5 -> "Special Grade Sorcerer";
            default -> "Non-sorcerer";
        };
    }

    public static int addExorcism(String uuid) {
        int v = data.cursesExorcised.merge(uuid, 1, Integer::sum);
        // Auto-promote grade as curses are exorcised.
        int[] thresholds = {3, 10, 25, 60, 120};
        int grade = 0;
        for (int t : thresholds) if (v >= t) grade++;
        if (grade > sorcererGrade(uuid)) setSorcererGrade(uuid, grade);
        return v;
    }

    // --- Cursed energy aptitude (trait) ---

    public static com.political.curse.CursedTrait cursedTrait(String uuid) {
        return com.political.curse.CursedTrait.byId(data.cursedTrait.get(uuid));
    }

    public static void setCursedTrait(String uuid, com.political.curse.CursedTrait trait) {
        data.cursedTrait.put(uuid, trait.name());
    }

    /** Rolls a trait for a player the first time we see them; returns the (possibly existing) trait. */
    public static com.political.curse.CursedTrait ensureTrait(String uuid) {
        String existing = data.cursedTrait.get(uuid);
        if (existing == null) {
            com.political.curse.CursedTrait rolled = com.political.curse.CursedTrait.roll();
            data.cursedTrait.put(uuid, rolled.name());
            return rolled;
        }
        return com.political.curse.CursedTrait.byId(existing);
    }

    // --- Settlements & political geography ---

    public static java.util.Map<String, Settlement> settlements() {
        return data.settlements;
    }

    public static Settlement settlement(String id) {
        return id == null ? null : data.settlements.get(id);
    }

    public static void addSettlement(Settlement s) {
        data.settlements.put(s.id, s);
        // Non-sovereign settlements fall under the nearest sovereign city/capital.
        if (!s.isSovereign()) {
            Settlement ruler = nearestSovereign(s.dimension, s.x, s.z, s.id);
            s.governedBy = ruler == null ? "" : ruler.id;
        }
    }

    /** Nearest sovereign (Capital/City) settlement in a dimension, excluding {@code excludeId}. */
    public static Settlement nearestSovereign(String dimension, int x, int z, String excludeId) {
        Settlement best = null;
        double bestSq = Double.MAX_VALUE;
        for (Settlement s : data.settlements.values()) {
            if (!s.isSovereign() || !s.dimension.equals(dimension)) continue;
            if (s.id.equals(excludeId)) continue;
            double dsq = s.distSq(x, z);
            if (dsq < bestSq) { bestSq = dsq; best = s; }
        }
        return best;
    }

    /** Nearest settlement of any tier in a dimension (used to enrol citizens). */
    public static Settlement nearestSettlement(String dimension, int x, int z) {
        Settlement best = null;
        double bestSq = Double.MAX_VALUE;
        for (Settlement s : data.settlements.values()) {
            if (!s.dimension.equals(dimension)) continue;
            double dsq = s.distSq(x, z);
            if (dsq < bestSq) { bestSq = dsq; best = s; }
        }
        return best;
    }

    // --- Citizenship & civic rank ---

    public static String citizenshipOf(String uuid) {
        return data.citizenship.get(uuid);
    }

    public static void setCitizenship(String uuid, String settlementId) {
        if (settlementId == null) data.citizenship.remove(uuid);
        else data.citizenship.put(uuid, settlementId);
    }

    public static CivicRank civicRank(String uuid) {
        return CivicRank.byOrdinal(data.civicRank.getOrDefault(uuid, 0));
    }

    public static void setCivicRank(String uuid, CivicRank rank) {
        data.civicRank.put(uuid, rank.ordinal());
    }

    /** Promotes one step; returns the new rank (capped at the top climbable rank). */
    public static CivicRank promote(String uuid) {
        int next = Math.min(CivicRank.values().length - 1, data.civicRank.getOrDefault(uuid, 0) + 1);
        data.civicRank.put(uuid, next);
        return CivicRank.byOrdinal(next);
    }

    public static CivicRank demote(String uuid) {
        int next = Math.max(0, data.civicRank.getOrDefault(uuid, 0) - 1);
        data.civicRank.put(uuid, next);
        return CivicRank.byOrdinal(next);
    }

    public static boolean canStandForElection(String uuid) {
        return civicRank(uuid).ordinal() >= CivicRank.CANDIDATE_THRESHOLD.ordinal();
    }

    /** True if the player currently leads any settlement. */
    public static boolean isSettlementLeader(String uuid) {
        for (Settlement s : data.settlements.values()) {
            if (uuid.equals(s.leader)) return true;
        }
        return false;
    }
}
