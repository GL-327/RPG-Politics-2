package com.political.politics;

/**
 * Government perks. The signed {@link #points} drive the Chair's zero-sum budget
 * (selected perks must sum to exactly 0). {@link #applied} marks perks whose
 * gameplay effect is wired into the combat/attribute system in this build; the
 * remainder are recognised for selection but their world/cosmetic effects are
 * deferred.
 */
public enum Perk {
    // Positive
    DOUBLE_HEALTH("Doubled Vitality", 3, true),
    DOUBLE_DAMAGE("Martial Decree", 3, true),
    INCREASED_ARMOUR("Standing Army", 2, true),
    SOFTER_LANDING("Safe Streets", 1, false),
    LOOT_GALORE("Bountiful Spoils", 2, false),
    PUBLIC_WORKS("Public Works", 2, true),
    GOLDEN_AGE("Golden Age", 3, true),
    NATIONAL_UNITY("National Unity", 2, true),
    XP_TAX_CUTS("Scholarship Act", 2, false),
    FORTIFIED_SHIELDS("Fortified Shields", 2, true),
    SWIFT_HARVEST("Swift Harvest", 1, true),
    BATTLE_HARDENED("Battle Hardened", 2, true),
    NIGHTVISION_DECREE("Nightwatch Decree", 1, true),
    PROSPERITY_SURGE("Prosperity Surge", 2, false),
    TREASURE_HUNTER("Treasure Hunter", 2, false),
    DIPLOMATIC_IMMUNITY("Diplomatic Immunity", 3, true),
    BOUNTY_HUNTER_SURGE("Bounty Hunter Surge", 2, false),

    // Neutral
    ETERNAL_DAWN("Eternal Dawn", 0, false),
    CHAOS_LOTTERY("Chaos Lottery", 0, false),
    SILENT_WORLD("Silent World", 0, false),
    CULTURAL_FESTIVAL("Cultural Festival", 0, false),

    // Negative
    CIVIL_UNREST("Civil Unrest", -2, false),
    CRIME_WAVE("Crime Wave", -2, false),
    INFRASTRUCTURE_NEGLECT("Infrastructure Neglect", -1, true),
    MINOR_CORRUPTION("Minor Corruption", -1, false),
    GLASS_CANNON("Glass Cannon", -2, true),
    FAMINE("Famine", -1, false),
    VOID_TOUCHED("Void Touched", -2, true),
    WITHERING_ECONOMY("Withering Economy", -1, false),
    BOUNTY_TAX("Bounty Tax", -1, false);

    public final String displayName;
    public final int points;
    public final boolean applied;

    Perk(String displayName, int points, boolean applied) {
        this.displayName = displayName;
        this.points = points;
        this.applied = applied;
    }

    public static Perk byId(String id) {
        try {
            return valueOf(id.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
