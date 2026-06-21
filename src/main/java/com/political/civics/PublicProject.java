package com.political.civics;

import net.minecraft.ChatFormatting;

/**
 * Civic infrastructure funded from the treasury. Once a project's investment
 * reaches its cost it is completed and confers a permanent, server-wide benefit
 * applied to online citizens by {@link TreasuryFund}.
 */
public enum PublicProject {
    ROADS("roads", "Royal Roads", 12000, ChatFormatting.GRAY,
            "Paved highways speed all citizens (Speed I)."),
    GRANARY("granary", "Public Granary", 10000, ChatFormatting.GREEN,
            "Stored grain keeps citizens fed (Saturation)."),
    AQUEDUCT("aqueduct", "Grand Aqueduct", 14000, ChatFormatting.AQUA,
            "Clean water bolsters health (Health Boost)."),
    HOSPITAL("hospital", "City Hospital", 16000, ChatFormatting.RED,
            "Medical care grants gentle Regeneration."),
    UNIVERSITY("university", "Royal University", 20000, ChatFormatting.LIGHT_PURPLE,
            "Schooling speeds tool work (Haste)."),
    WALLS("walls", "City Walls", 18000, ChatFormatting.DARK_GRAY,
            "Fortifications harden citizens (Resistance).");

    public final String id;
    public final String displayName;
    public final int cost;
    public final ChatFormatting color;
    public final String description;

    PublicProject(String id, String displayName, int cost, ChatFormatting color, String description) {
        this.id = id;
        this.displayName = displayName;
        this.cost = cost;
        this.color = color;
        this.description = description;
    }

    public static PublicProject byId(String id) {
        if (id == null) return null;
        for (PublicProject p : values()) {
            if (p.id.equalsIgnoreCase(id) || p.name().equalsIgnoreCase(id)) return p;
        }
        return null;
    }
}
