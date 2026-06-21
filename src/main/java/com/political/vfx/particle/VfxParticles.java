package com.political.vfx.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Original, mod-owned {@link SimpleParticleType} definitions for the
 * {@code com.political.vfx} effects library.
 *
 * <p>These are <em>simple</em> particle types (no extra packet data) created via
 * {@link FabricParticleTypes#simple()} and registered against
 * {@link BuiltInRegistries#PARTICLE_TYPE}. Each type is paired with an original
 * texture under {@code assets/politicalserver/textures/particle/} and a particle
 * definition under {@code assets/politicalserver/particles/} (authored by
 * {@code tools/gen-vfx-particles.js}), plus a client-side provider registered in
 * {@code com.political.vfx.client.VfxClientBootstrap}.</p>
 *
 * <h2>Wiring (see handoff manifest)</h2>
 * <p>Registration is NOT auto-wired into a shared entrypoint. Call
 * {@link com.political.vfx.VfxBootstrap#init()} from the common initializer (and
 * {@code VfxClientBootstrap.initClient()} from the client initializer) to make the
 * types live. Until then they are compile-only scaffolding and all
 * {@link com.political.vfx.VfxHelper} routines run on vanilla particles.</p>
 */
public final class VfxParticles {

    public static final String MOD_ID = "politicalserver";

    /** Rising cursed-energy ember (violet → black), for domains/curses. */
    public static SimpleParticleType CURSED_EMBER;
    /** Slow drifting void mote, for implosions and void powers. */
    public static SimpleParticleType VOID_MOTE;
    /** Bright electric arc fleck, for lightning/chain routines. */
    public static SimpleParticleType ARC_SPARK;
    /** Glowing rune fragment, for ground rune circles / sigils. */
    public static SimpleParticleType RUNE_GLYPH;
    /** Radiant holy mote, for smite/heal effects. */
    public static SimpleParticleType RADIANT_MOTE;
    /** Heavy ember/cinder, for meteors, slams and debris. */
    public static SimpleParticleType CINDER;

    private VfxParticles() {}

    /**
     * Registers every custom particle type. Idempotent guard via {@code CURSED_EMBER}.
     * Call once during common mod init (before registries freeze).
     */
    public static void register() {
        if (CURSED_EMBER != null) return;
        CURSED_EMBER = make("cursed_ember");
        VOID_MOTE = make("void_mote");
        ARC_SPARK = make("arc_spark");
        RUNE_GLYPH = make("rune_glyph");
        RADIANT_MOTE = make("radiant_mote");
        CINDER = make("cinder");
    }

    private static SimpleParticleType make(String name) {
        return Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(MOD_ID, name),
                FabricParticleTypes.simple());
    }
}
