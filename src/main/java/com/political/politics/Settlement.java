package com.political.politics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Serializable record of a generated settlement and its local government. */
public class Settlement {
    public String id = "";
    public String name = "";
    public SettlementType type = SettlementType.TOWN;

    // Town-hall / castle-keep centre (where governance lives) and world position.
    public int x;
    public int y;
    public int z;
    public String dimension = "minecraft:overworld";

    // Governance: a non-sovereign settlement answers to the nearest sovereign one.
    public String governedBy = "";   // settlement id, "" if sovereign
    public String leader = "";       // player uuid, "" if vacant

    // Local election state (rank-gated candidacy -> vote).
    public boolean electionActive = false;
    public long electionEnd = 0L;
    public List<String> candidates = new ArrayList<>();
    public Map<String, Integer> votes = new HashMap<>();
    public List<String> voted = new ArrayList<>();

    public Settlement() {}

    public Settlement(String id, String name, SettlementType type, int x, int y, int z, String dimension) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
    }

    public boolean isSovereign() {
        return type != null && type.sovereign;
    }

    public double distSq(int ox, int oz) {
        double dx = x - ox;
        double dz = z - oz;
        return dx * dx + dz * dz;
    }
}
