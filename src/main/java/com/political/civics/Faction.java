package com.political.civics;

import java.util.ArrayList;
import java.util.List;

/**
 * Serializable record of a political party / faction players may found and join.
 * Stored in {@code PoliticsData.factions} (Gson-backed, so plain public fields).
 */
public class Faction {
    public String id = "";
    public String name = "";
    public String tag = "";          // short chat tag, e.g. "[LIB]"
    public String founder = "";      // player uuid
    public String motto = "";
    public String ideology = "CENTRIST"; // FactionIdeology.name()
    public List<String> members = new ArrayList<>();
    public int treasury = 0;          // party war-chest (coins)
    public int influence = 0;         // earned by winning offices / elections
    public long foundedAt = 0L;

    public Faction() {}

    public Faction(String id, String name, String tag, String founder, String ideology) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.founder = founder;
        this.ideology = ideology;
        this.members.add(founder);
        this.foundedAt = System.currentTimeMillis();
    }

    public int size() {
        return members.size();
    }
}
