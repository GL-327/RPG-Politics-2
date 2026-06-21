package com.political.civics;

import net.minecraft.ChatFormatting;

/**
 * Decrees the government may enact. Each carries a one-time treasury cost and an
 * ongoing effect applied by {@link LawManager}. Laws are mutually compatible
 * unless noted; most simply toggle a flag other systems read.
 */
public enum CivicLaw {
    TAX_HOLIDAY("tax_holiday", "Tax Holiday", 5000, ChatFormatting.GREEN,
            "Suspends daily tax collection while in force."),
    FREE_MARKET("free_market", "Free Market Act", 4000, ChatFormatting.GOLD,
            "Waives auction listing fees and halves market trade fees."),
    STIMULUS("stimulus", "Economic Stimulus", 8000, ChatFormatting.YELLOW,
            "Boosts all job wages by 25% and grants workers Haste."),
    PUBLIC_HEALTHCARE("healthcare", "Public Healthcare", 6000, ChatFormatting.RED,
            "Grants Regeneration to online citizens; drains the treasury upkeep."),
    MARTIAL_LAW("martial_law", "Martial Law", 7000, ChatFormatting.DARK_RED,
            "Office holders & guards gain Resistance; wanted criminals suffer Slowness."),
    WELFARE("welfare", "Welfare Programme", 5000, ChatFormatting.AQUA,
            "Pays a daily stipend to the poorest citizens from the treasury."),
    CONSCRIPTION("conscription", "Conscription", 6000, ChatFormatting.DARK_PURPLE,
            "All citizens gain Strength but move a little slower (war footing).");

    public final String id;
    public final String displayName;
    public final int cost;
    public final ChatFormatting color;
    public final String description;

    CivicLaw(String id, String displayName, int cost, ChatFormatting color, String description) {
        this.id = id;
        this.displayName = displayName;
        this.cost = cost;
        this.color = color;
        this.description = description;
    }

    public static CivicLaw byId(String id) {
        if (id == null) return null;
        for (CivicLaw l : values()) {
            if (l.id.equalsIgnoreCase(id) || l.name().equalsIgnoreCase(id)) return l;
        }
        return null;
    }
}
