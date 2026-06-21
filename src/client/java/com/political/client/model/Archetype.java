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
    TINY_SWARM("swarm");

    private final String id;

    Archetype(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
