package com.political.expansion.mobs;

/**
 * Behavioural archetype for an {@link ExpansionMob}. Controls which AI goals the mob receives
 * and whether it carries a boss bar / phase logic.
 */
public enum MobRole {
    /** Actively hunts players (and optionally villagers). */
    HOSTILE,
    /** Wanders peacefully; only fights back when struck. */
    NEUTRAL,
    /** Peaceful but flees from players (and fights back if cornered). */
    SKITTISH,
    /** Tougher rare hostile with a boss bar but no phase changes. */
    MINIBOSS,
    /** Large hostile with a boss bar and a phase-2 enrage. */
    BOSS;

    /** True when this role actively seeks out targets. */
    public boolean isAggressive() {
        return this == HOSTILE || this == MINIBOSS || this == BOSS;
    }

    /** True when this role shows a boss bar. */
    public boolean isBossLike() {
        return this == MINIBOSS || this == BOSS;
    }
}
