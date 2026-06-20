package com.political.politics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Plain serializable snapshot of all political/economic state (Gson-backed). */
public class PoliticsData {
    // Roles (UUID strings, "" = vacant)
    public String chair = "";
    public String viceChair = "";
    public String judge = "";
    public int chairTermCount = 0;

    // Player registry
    public Map<String, String> playerNames = new HashMap<>();

    // Elections
    public long termEndTime = 0L;
    public long electionEndTime = 0L;
    public boolean electionActive = false;
    public boolean electionSystemEnabled = false;
    public Map<String, Integer> votes = new HashMap<>();
    public Map<String, String> votedPlayers = new HashMap<>();
    public List<String> candidates = new ArrayList<>();

    // Impeachment
    public boolean impeachmentActive = false;
    public int impeachYes = 0;
    public int impeachNo = 0;
    public List<String> impeachVoted = new ArrayList<>();

    // Perks
    public List<String> activePerks = new ArrayList<>();
    public List<String> chairSelectedPerks = new ArrayList<>();
    public List<String> viceChairPerks = new ArrayList<>();
    public boolean chairPerksSetThisTerm = false;
    public boolean viceChairPerksSetThisTerm = false;
    public List<String> previousTermPerks = new ArrayList<>();

    // Justice
    public Map<String, Long> prisoners = new HashMap<>();
    public Map<String, String> prisonerLocations = new HashMap<>();

    // Tax / treasury / economy
    public boolean taxEnabled = false;
    public int taxPercent = 5;
    public long lastTaxTime = 0L;
    public Map<String, Integer> taxOwed = new HashMap<>();
    public int treasury = 0;
    public Map<String, Integer> coins = new HashMap<>();

    // Banking
    public Map<String, Integer> bank = new HashMap<>();
    public long lastInterestTime = 0L;

    // Bounty Hunt (slayer) progression: key = "uuid|TYPE" -> xp
    public Map<String, Integer> bountyXp = new HashMap<>();

    // Active slayer quests: uuid -> "TYPE|tier|killsDone|killsNeeded|bossSpawned(0/1)"
    public Map<String, String> activeQuests = new HashMap<>();

    // Dictatorship
    public boolean dictatorActive = false;
    public String dictator = "";
    public String previousJudge = "";

    // --- Credits (premium currency; 1000 coins == 1 credit) ---
    public Map<String, Integer> credits = new HashMap<>();

    // --- Homes & checkpoints ---
    // home: "x,y,z,yaw,pitch,dimension"
    public Map<String, String> homes = new HashMap<>();
    // checkpoints: uuid -> (name -> "x,y,z,yaw,pitch,dimension")
    public Map<String, Map<String, String>> checkpoints = new HashMap<>();

    // --- Server spawn ("x,y,z,yaw,pitch,dimension") ---
    public String serverSpawn = "";

    // --- Titles (chat/nametag prefix) uuid -> "title|COLOR" ---
    public Map<String, String> titles = new HashMap<>();

    // --- Permanent player buffs: uuid -> list of buff ids ---
    public Map<String, List<String>> playerBuffs = new HashMap<>();
    // Progress counters for auto-granting buffs: key = "uuid|COUNTER" -> value
    public Map<String, Integer> buffProgress = new HashMap<>();

    // --- Crypto / stock markets ---
    // Holdings: key = "uuid|SYMBOL" -> units (stored x1000 for fractional)
    public Map<String, Long> cryptoHoldings = new HashMap<>();
    public Map<String, Long> stockHoldings = new HashMap<>();
    // Live prices (coins per unit). Empty -> seeded from defaults on first tick.
    public Map<String, Double> cryptoPrices = new HashMap<>();
    public Map<String, Double> stockPrices = new HashMap<>();
    public long lastMarketTick = 0L;

    // --- World / misc tuning ---
    public double mobHealthScalingMultiplier = 1.0;
    public boolean healthScalingEnabled = true;

    // --- Powers (Compound V + JJK cursed techniques) ---
    // Known power ids per player.
    public Map<String, List<String>> knownPowers = new HashMap<>();
    // Currently selected power id per player.
    public Map<String, String> selectedPower = new HashMap<>();
    // Temp V powers expire: key = uuid -> epoch millis when temp powers are stripped (0 = none).
    public Map<String, Long> tempPowerExpiry = new HashMap<>();
    // Sorcerer grade per player (0 = not a sorcerer; 1..5 where 5 = Special Grade).
    public Map<String, Integer> sorcererGrade = new HashMap<>();
    // Lifetime curses exorcised (drives grade progression and bragging rights).
    public Map<String, Integer> cursesExorcised = new HashMap<>();
    public boolean curseSpawningEnabled = true;

    // --- Cursed energy (separate resource from Mana) ---
    // Innate aptitude per player (CursedTrait name). Rolled once on first join.
    public Map<String, String> cursedTrait = new HashMap<>();

    // --- Tunable config (editable via the Developer Menu) ---
    public double curseNaturalSpawnChance = 0.04;   // chance a hostile manifests as a curse
    public double cursedObjectLootChance = 0.01;    // chance eligible dungeon loot is cursed
    public int deathCurseThreshold = 25;            // deaths in an area before items can curse
    public double deathCurseChance = 0.05;          // chance an eligible item curses once threshold hit
    public double cursedObjectAttractChance = 0.02; // per-check chance a carried cursed object lures a curse
    public double manaRegenRate = 0.02;             // fraction of max mana restored per second
    public double powerCostMultiplier = 1.0;        // global multiplier on power energy costs

    // --- Settlements & political geography ---
    // All settlements, keyed by id.
    public Map<String, Settlement> settlements = new HashMap<>();
    // Player citizenship: uuid -> settlement id.
    public Map<String, String> citizenship = new HashMap<>();
    // Civic rank within the home settlement: uuid -> CivicRank ordinal.
    public Map<String, Integer> civicRank = new HashMap<>();
    // The world-spawn capital's settlement id ("" until generated).
    public String capitalId = "";
    // Grid cells already considered for settlement generation ("dim|cx|cz").
    public List<String> generatedCells = new ArrayList<>();

    // --- Worldgen tuning (editable via the Developer Menu) ---
    public boolean settlementGenEnabled = true;     // auto-scatter settlements as players explore
    public int settlementGridChunks = 48;           // size of each candidate grid cell, in chunks
    public double settlementSpawnChance = 0.55;      // chance a suitable grid cell receives a settlement
    public long settlementTermMillis = 7L * 24 * 60 * 60 * 1000; // local term length
}
