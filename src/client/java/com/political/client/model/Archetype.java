package com.political.client.model;

/**
 * The shared catalogue of entity-model archetypes used by every custom creature/spirit in the mod.
 *
 * <p>Each archetype is a distinct, mixin-free {@link net.minecraft.client.model.HumanoidModel}
 * subclass (so it inherits robust vanilla head-tracking / limb-swing / attack motion) with its own
 * silhouette and bolted-on, animated appendages (horns, wings, tails, extra arms, antennae …). A
 * species/mob is mapped to its best-fit archetype on the client only — no server data changes.
 *
 * <p>The integer order is stable; the lower-case {@link #id()} is used to build the per-archetype
 * {@link net.minecraft.client.model.geom.ModelLayerLocation} so all four content packages share one
 * baked layer per archetype.
 */
public enum Archetype {
    /** Thin, slightly hunched humanoid with long clawed arms — imps, stalkers, wisps, casters. */
    GAUNT_HUMANOID("gaunt"),
    /** Bulky, broad-shouldered brute with shoulder spikes — ogres, knights, brutes. */
    HULKING_BRUTE("brute"),
    /** Floating upper torso with a long segmented swaying tail and no legs — serpents, nagas. */
    SERPENTINE("serpent"),
    /** Humanoid with a second, lower pair of arms — tool revenants, asura-curses, automatons. */
    MULTI_ARMED("multiarm"),
    /** Humanoid with large flapping membrane wings — owls, eagles, dragons, seraphs. */
    WINGED("winged"),
    /** Hunched four-legged beast with a forward neck — wolves, panthers, bears, salamanders. */
    QUADRUPED("quadruped"),
    /** Low, splay-legged six-limbed crawler — spiders, larvae, constructs. */
    CRAWLER("crawler"),
    /** Hooded, robe-skirted spectral curse with horns and no visible legs — wraiths, shades. */
    CLOAKED_SPIRIT("cloaked"),
    /** Oversized, crowned, heavily-built boss silhouette — slow, imposing, glow-eligible. */
    BOSS_COLOSSUS("colossus"),
    /** Tiny big-headed antennaed swarm body with fast, jittery motion — motes, fleas, gnats. */
    TINY_SWARM("swarm"),

    // ───────────────────────── Source-faithful creature families (added) ─────────────────────────
    /** Boneless, many-mouthed/eyed blob curse on a writhing mass — JJK special-grade amorphous. */
    AMORPHOUS_CURSE("amorphous"),
    /** Tall horned humanoid curse with a head-crest, extra eyes and clawed hands — JJK special grade. */
    SPECIAL_GRADE("special"),
    /** Heroic, caped super-human with a chest emblem and flight-ready lean — Compound V / Viltrumite. */
    HERO_CAPED("hero"),
    /** Plated, crested, tasseted knight with a weapon-ready stance — guards, paladins, men-at-arms. */
    ARMORED_KNIGHT("knight"),
    /** Emaciated skeletal husk with ribcage, dropped jaw and a broken shamble — undead. */
    UNDEAD_HUSK("undead"),
    /** Horned, bat-winged, tailed fiend with taloned swipes — demons, devils, hellspawn. */
    DEMON_FIEND("demon"),
    /** Blocky, core-lit golem with slab arms and a stomping slam — constructs, automatons, statues. */
    CONSTRUCT_GOLEM("construct"),
    /** Floating core ringed by orbiting shards with wisp-arms — flame / frost / storm elementals. */
    ELEMENTAL_BEING("elemental");

    private final String id;

    Archetype(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
