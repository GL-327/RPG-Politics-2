package com.political.expansion2.curses;

/**
 * Combat traits for Phase-2 cursed spirits. Evaluated mixin-free inside
 * {@link CursedSpirit2Entity#tick()} with per-trait cooldowns on top of vanilla zombie goals.
 */
public enum Behavior2 {
    FAST_SWARM,
    RANGED_BLAST,
    FIRE_BLAST,
    POISON_AURA,
    FROST_AURA,
    WITHER_TOUCH,
    LIFE_DRAIN,
    TELEPORT,
    SUMMONER,
    SHOCKWAVE,
    ENRAGE,
    REGEN,
    MELEE_BRUISER,
    FIRE_IMMUNE,
    DOMAIN_FIELD,
    CURSED_BOLT,
    BLINDNESS_CURSE,
    CURSE_SEAL,
    SWARM_REPLICATE,
    CHAIN_CURSE,
    GRAVITY_PULL,
    DISASTER_ERUPT,
    MAXIMUM_BURST,
    SHIKIGAMI_CALL,
    ARMAMENT_FORM,
    NECROMANCY_RISE,
    VOICE_CURSE,
    RAINBOW_BEAM
}
