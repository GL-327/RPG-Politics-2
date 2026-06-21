package com.political.civics;

import net.minecraft.ChatFormatting;

/**
 * Citizen professions. Each job pays a base daily wage (scaled by job level) and
 * a smaller on-demand {@code /job work} payout. Wages are drawn from the treasury
 * when funds allow, otherwise minted (kept small to stay non-inflationary).
 */
public enum Job {
    MINER("miner", "Miner", 120, ChatFormatting.GRAY,
            "Delve the deeps; paid for hazardous underground labour."),
    FARMER("farmer", "Farmer", 100, ChatFormatting.GREEN,
            "Feed the nation; steady, reliable wages."),
    HUNTER("hunter", "Hunter", 135, ChatFormatting.DARK_GREEN,
            "Cull beasts for the realm; bounty kills grant bonus job XP."),
    GUARD("guard", "Guard", 145, ChatFormatting.RED,
            "Keep the peace; capturing wanted criminals grants bonus job XP."),
    MERCHANT("merchant", "Merchant", 150, ChatFormatting.GOLD,
            "Move goods through the markets; thrives under Free Market law."),
    BANKER("banker", "Banker", 160, ChatFormatting.YELLOW,
            "Steward the coin; higher wages but heavier taxation."),
    BUILDER("builder", "Builder", 115, ChatFormatting.AQUA,
            "Raise public works; contributes to civic projects."),
    DIPLOMAT("diplomat", "Diplomat", 170, ChatFormatting.LIGHT_PURPLE,
            "Broker accords between factions; the best-paid profession.");

    public final String id;
    public final String displayName;
    public final int baseWage;
    public final ChatFormatting color;
    public final String description;

    Job(String id, String displayName, int baseWage, ChatFormatting color, String description) {
        this.id = id;
        this.displayName = displayName;
        this.baseWage = baseWage;
        this.color = color;
        this.description = description;
    }

    public static Job byId(String id) {
        if (id == null) return null;
        for (Job j : values()) {
            if (j.id.equalsIgnoreCase(id) || j.name().equalsIgnoreCase(id)) return j;
        }
        return null;
    }
}
