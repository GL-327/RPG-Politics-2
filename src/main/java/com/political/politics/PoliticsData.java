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
}
