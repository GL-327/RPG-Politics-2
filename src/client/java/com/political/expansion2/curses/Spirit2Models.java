package com.political.expansion2.curses;

import com.political.client.model.Archetype;

/**
 * Maps each phase-2 cursed-spirit {@link SpiritSpecies2.ModelKind} to a shared
 * {@link com.political.client.model.Archetype}. Geometry/animation lives in the shared
 * {@code com.political.client.model} library; this is just the per-roster lookup table.
 */
public final class Spirit2Models {

    private Spirit2Models() {}

    public static Archetype archetypeFor(SpiritSpecies2.ModelKind kind) {
        return switch (kind) {
            case GAUNT -> Archetype.GAUNT_HUMANOID;
            case SWARM_TINY -> Archetype.TINY_SWARM;
            case HULKING -> Archetype.HULKING_BRUTE;
            case HORNED -> Archetype.CLOAKED_SPIRIT;
            case WINGED -> Archetype.WINGED;
            case SERPENT -> Archetype.SERPENTINE;
            case TOOL -> Archetype.MULTI_ARMED;
            case CORPSE -> Archetype.CRAWLER;
        };
    }
}
