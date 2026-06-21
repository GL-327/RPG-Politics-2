package com.political.curse.spirits;

/**
 * Combat traits a {@link SpiritSpecies} can carry. They are evaluated mixin-free inside
 * {@link CursedSpiritEntity#tick()} (on the server) with per-trait cooldowns, layered on top of the
 * inherited vanilla zombie goals (movement / melee / target acquisition). A species may mix several.
 */
public enum Behavior {
    /** Permanent Speed buff + erratic movement: cheap fodder that swarms in numbers. */
    FAST_SWARM,
    /** Fires a hitscan beam of cursed energy at its target (purple particle lance + magic damage). */
    RANGED_BLAST,
    /** Hitscan blast that also ignites; pairs with {@link #FIRE_IMMUNE}. */
    FIRE_BLAST,
    /** Curses nearby players with Poison/Wither in a radius every second. */
    POISON_AURA,
    /** Chills nearby players with Slowness + Mining Fatigue ("roots") in a radius. */
    FROST_AURA,
    /** Melee hits and proximity inflict Wither (flesh-warping touch). */
    WITHER_TOUCH,
    /** Heals itself whenever its auras/touch wound a player (transfiguration / leech). */
    LIFE_DRAIN,
    /** Blinks: short hops to flank, or long teleports around its target. */
    TELEPORT,
    /** Periodically manifests weaker curses to fight alongside it. */
    SUMMONER,
    /** Releases an AoE cursed-energy shockwave (radial knockback + damage). */
    SHOCKWAVE,
    /** Below 30% HP gains Strength + Speed once (boss desperation). */
    ENRAGE,
    /** Passive self-regeneration. */
    REGEN,
    /** Strikes harder in melee and knocks the victim back. */
    MELEE_BRUISER,
    /** Immune to fire/lava (cleared each tick + fire-resistance aura). */
    FIRE_IMMUNE
}
