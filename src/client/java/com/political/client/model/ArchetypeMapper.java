package com.political.client.model;

import java.util.Locale;

/**
 * Maps a non-curse creature (which carries no explicit model kind) to its best-fit {@link Archetype}
 * using only client-readable spec data: display name keywords, the "brute" flag and boss tier. This
 * keeps all archetype selection on the client — no server roster data is changed.
 */
public final class ArchetypeMapper {

    private ArchetypeMapper() {}

    private static boolean any(String n, String... keys) {
        for (String k : keys) {
            if (n.contains(k)) return true;
        }
        return false;
    }

    /**
     * @param name        the creature's display name (e.g. "Shadow Wolf Beast")
     * @param brute       whether the spec requested the bulky body
     * @param isBoss      true for full bosses (phase logic / boss bar)
     * @param isMiniBoss  true for mini-bosses
     */
    public static Archetype forCreature(String name, boolean brute, boolean isBoss, boolean isMiniBoss) {
        String n = name.toLowerCase(Locale.ROOT);

        // Full bosses always take the imposing colossus silhouette (+ glow layer on the renderer).
        if (isBoss) return Archetype.BOSS_COLOSSUS;

        // Tiny pests.
        if (any(n, "imp", "sprite", "pixie", "wisp", "mote", "flea", "gnat", "larva", "wraithling", "shardling")) {
            return Archetype.TINY_SWARM;
        }
        // Fliers.
        if (any(n, "eagle", "vulture", "owl", "wing", "seraph", "dragon", "drake", "harpy", "phoenix", "angel", "wyvern")) {
            return Archetype.WINGED;
        }
        // Serpents.
        if (any(n, "serpent", "naga", "hydra", "wyrm", "leviathan", "eel", "snake", "viper")) {
            return Archetype.SERPENTINE;
        }
        // Four-legged beasts.
        if (any(n, "wolf", "hound", "panther", "bear", "fox", "buffalo", "ram", "goat", "steed",
                "camel", "turtle", "stag", "salamander", "lion", "tiger", "boar", "cat", "beast")) {
            return Archetype.QUADRUPED;
        }
        // Spectral / robed casters.
        if (any(n, "wraith", "shade", "phantom", "banshee", "ghost", "spectre", "specter", "revenant",
                "shadow", "soul", "haunt", "acolyte", "cultist", "prophet", "priest", "lich", "doom", "necromancer")) {
            return Archetype.CLOAKED_SPIRIT;
        }
        // Many-limbed machines / weapon hosts.
        if (any(n, "automaton", "clockwork", "golem", "construct", "siege", "marshal", "marauder")) {
            return Archetype.MULTI_ARMED;
        }
        // Low scuttlers.
        if (any(n, "crawler", "spider", "scuttler", "tick", "lurker")) {
            return Archetype.CRAWLER;
        }
        // Mini-bosses and brutes fall back to the hulking body.
        if (isMiniBoss || brute) return Archetype.HULKING_BRUTE;

        return Archetype.GAUNT_HUMANOID;
    }
}
