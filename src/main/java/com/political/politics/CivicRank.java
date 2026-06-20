package com.political.politics;

import net.minecraft.ChatFormatting;

/**
 * The ladder a citizen climbs within their settlement. Only citizens at
 * {@link #COUNCILOR} (the top climbable rank) may stand as candidates for Leader;
 * the elected Leader holds the settlement's {@code leader} office on top of this.
 */
public enum CivicRank {
    CITIZEN("Citizen", ChatFormatting.GRAY),
    OFFICER("Officer", ChatFormatting.GREEN),
    COUNCILOR("Councilor", ChatFormatting.AQUA);

    public final String display;
    public final ChatFormatting color;

    CivicRank(String display, ChatFormatting color) {
        this.display = display;
        this.color = color;
    }

    public static CivicRank byOrdinal(int o) {
        CivicRank[] v = values();
        if (o < 0) o = 0;
        if (o >= v.length) o = v.length - 1;
        return v[o];
    }

    /** Lowest rank that may stand for election as settlement Leader. */
    public static final CivicRank CANDIDATE_THRESHOLD = COUNCILOR;
}
