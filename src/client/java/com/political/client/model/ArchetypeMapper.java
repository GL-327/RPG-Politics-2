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

        // A strong themed family takes precedence even for bosses, so a "Dragon Lord" reads as a
        // dragon and a "Hero Captain" as a caped super — not a generic colossus.
        Archetype family = familyOf(n);
        if (family != null) return family;

        // Otherwise full bosses take the imposing crowned colossus silhouette (+ glow on the renderer).
        if (isBoss) return Archetype.BOSS_COLOSSUS;

        // Mini-bosses and brutes fall back to the hulking body.
        if (isMiniBoss || brute) return Archetype.HULKING_BRUTE;

        return Archetype.GAUNT_HUMANOID;
    }

    /** Keyword → themed family archetype, or {@code null} when nothing distinctive matches. */
    private static Archetype familyOf(String n) {
        // Caped super-humans (The Boys / Invincible / Viltrumite hero factions).
        if (any(n, "hero", "super", "viltrum", "compound", "cape", "captain", "sentry", "homelander",
                "paragon", "vigilant", "champion", "icon", "titan-", "supreme", "patriot", "crimson",
                "invincible", "omni", "guardian")) {
            return Archetype.HERO_CAPED;
        }
        // Demons / fiends / devils.
        if (any(n, "demon", "fiend", "devil", "hellspawn", "infernal", "abyssal", "balor", "imp-lord",
                "hellion", "diablo", "archfiend", "pit ", "brimstone")) {
            return Archetype.DEMON_FIEND;
        }
        // Constructs / golems / automatons / statues.
        if (any(n, "automaton", "clockwork", "golem", "construct", "siege", "sentinel", "statue",
                "colossus of", "warforged", "juggernaut", "machine", "engine")) {
            return Archetype.CONSTRUCT_GOLEM;
        }
        // Armoured knights / guards / paladins.
        if (any(n, "knight", "paladin", "guard", "templar", "legion", "soldier", "warden", "crusader",
                "cavalier", "vanguard", "myrmidon", "centurion", "man-at-arms", "dragoon", "marshal")) {
            return Archetype.ARMORED_KNIGHT;
        }
        // Undead husks.
        if (any(n, "undead", "skeleton", "skeletal", "zombie", "wight", "ghoul", "bone", "corpse",
                "risen", "draugr", "mummy", "rotting", "decayed", "graveborn", "ossuary")) {
            return Archetype.UNDEAD_HUSK;
        }
        // Elementals / calamity lineages.
        if (any(n, "elemental", "flame", "frost", "storm", "ember", "cinder", "blaze", "glacier",
                "inferno", "tempest", "magma", "volcano", "calamity", "tide", "flora", "ash ", "spark")) {
            return Archetype.ELEMENTAL_BEING;
        }
        // Tiny pests / swarm.
        if (any(n, "imp", "sprite", "pixie", "wisp", "mote", "flea", "gnat", "larva", "wraithling",
                "shardling", "fairy", "nymph")) {
            return Archetype.TINY_SWARM;
        }
        // Fliers / winged.
        if (any(n, "eagle", "vulture", "owl", "wing", "seraph", "dragon", "drake", "harpy", "phoenix",
                "angel", "wyvern", "roc", "griffin", "gryphon")) {
            return Archetype.WINGED;
        }
        // Serpents.
        if (any(n, "serpent", "naga", "hydra", "wyrm", "leviathan", "eel", "snake", "viper", "basilisk")) {
            return Archetype.SERPENTINE;
        }
        // Four-legged beasts.
        if (any(n, "wolf", "hound", "panther", "bear", "fox", "buffalo", "ram", "goat", "steed",
                "camel", "turtle", "stag", "salamander", "lion", "tiger", "boar", "cat", "beast",
                "direwolf", "chimera", "manticore")) {
            return Archetype.QUADRUPED;
        }
        // Amorphous masses / transfigured horrors.
        if (any(n, "ooze", "blob", "amorphous", "mass", "transfigured", "womb", "horde", "abomination",
                "mutant", "flesh", "slime", "morass")) {
            return Archetype.AMORPHOUS_CURSE;
        }
        // Spectral / robed casters.
        if (any(n, "wraith", "shade", "phantom", "banshee", "ghost", "spectre", "specter", "revenant",
                "shadow", "soul", "haunt", "acolyte", "cultist", "prophet", "priest", "lich",
                "doom", "necromancer", "warlock", "witch", "hex", "shaman", "seer")) {
            return Archetype.CLOAKED_SPIRIT;
        }
        // Many-limbed weapon hosts.
        if (any(n, "marauder", "weaver", "reaver", "tool", "armament", "blademaster")) {
            return Archetype.MULTI_ARMED;
        }
        // Low scuttlers.
        if (any(n, "crawler", "spider", "scuttler", "tick", "lurker", "scorpion", "broodling")) {
            return Archetype.CRAWLER;
        }
        // Named special-grade / archfiend humanoids.
        if (any(n, "special grade", "sovereign", "archon", "overlord", "emperor", "tyrant", "vessel",
                "deity", "god", "king vessel")) {
            return Archetype.SPECIAL_GRADE;
        }
        return null;
    }
}
