package com.political.civics;

import net.minecraft.ChatFormatting;

/**
 * Elected municipal offices, distinct from the national roles (Chair/Vice/Judge)
 * in the politics package. Each office unlocks authority over one civic system.
 */
public enum CivicOffice {
    MAYOR("mayor", "Mayor", ChatFormatting.GOLD,
            "Directs public works and may commission civic projects."),
    JUDGE("judge", "Judge", ChatFormatting.DARK_RED,
            "Levies fines, posts wanted bounties, and sets bail."),
    TREASURER("treasurer", "Treasurer", ChatFormatting.GREEN,
            "Manages the sovereign wealth fund and treasury investments.");

    public final String id;
    public final String displayName;
    public final ChatFormatting color;
    public final String description;

    CivicOffice(String id, String displayName, ChatFormatting color, String description) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.description = description;
    }

    public static CivicOffice byId(String id) {
        if (id == null) return null;
        for (CivicOffice o : values()) {
            if (o.id.equalsIgnoreCase(id) || o.name().equalsIgnoreCase(id)) return o;
        }
        return null;
    }
}
