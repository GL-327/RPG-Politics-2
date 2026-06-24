package com.political.curse.limb;

/**
 * Fourteen cursed-energy pathways mapped to body regions (People Playground JJP limb model).
 * Each limb can be toggled off to seal techniques routed through that pathway.
 */
public enum CursedLimb {
    HEAD("Head", "HD", 0xFFC065FF),
    EYES("Eyes", "EY", 0xFF6FB7FF),
    TORSO("Torso", "TR", 0xFF9CE6FF),
    HEART("Heart", "HT", 0xFFFF7A7A),
    UPPER_ARM_L("L Upper Arm", "UA", 0xFFFFD24A),
    UPPER_ARM_R("R Upper Arm", "UB", 0xFFFFD24A),
    LOWER_ARM_L("L Forearm", "LA", 0xFF7CE0A0),
    LOWER_ARM_R("R Forearm", "LB", 0xFF7CE0A0),
    HANDS("Hands", "HN", 0xFFE6ECF5),
    LOWER_BODY("Lower Body", "LBd", 0xFFCB7BFF),
    UPPER_LEG_L("L Thigh", "TL", 0xFF4DA3E8),
    UPPER_LEG_R("R Thigh", "TR", 0xFF4DA3E8),
    LOWER_LEG_L("L Shin", "SL", 0xFF5AA9FF),
    LOWER_LEG_R("R Shin", "SR", 0xFF5AA9FF);

    public final String label;
    public final String abbr;
    public final int tint;

    CursedLimb(String label, String abbr, int tint) {
        this.label = label;
        this.abbr = abbr;
        this.tint = tint;
    }

    public int bit() {
        return 1 << ordinal();
    }

    public static final int ALL_ENABLED_MASK = (1 << values().length) - 1;

    public static boolean isEnabled(int mask, CursedLimb limb) {
        return (mask & limb.bit()) != 0;
    }

    public static int toggle(int mask, CursedLimb limb, boolean enabled) {
        if (enabled) return mask | limb.bit();
        return mask & ~limb.bit();
    }
}
