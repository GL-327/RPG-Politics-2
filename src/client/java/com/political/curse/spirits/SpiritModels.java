package com.political.curse.spirits;

import com.political.client.model.Archetype;

/**
 * Maps each phase-1 cursed-spirit {@link SpiritSpecies.ModelKind} to a shared
 * {@link com.political.client.model.Archetype}. The actual geometry/animation lives in the shared
 * {@code com.political.client.model} library; this is just the per-roster lookup table.
 */
public final class SpiritModels {

    private SpiritModels() {}

    public static Archetype archetypeFor(SpiritSpecies.ModelKind kind) {
        return switch (kind) {
            case GAUNT -> Archetype.GAUNT_HUMANOID;
            case SWARM_TINY -> Archetype.TINY_SWARM;
            case HULKING -> Archetype.HULKING_BRUTE;
            // Horned curses now render as faithful horned special-grade silhouettes.
            case HORNED -> Archetype.SPECIAL_GRADE;
        };
    }
}
