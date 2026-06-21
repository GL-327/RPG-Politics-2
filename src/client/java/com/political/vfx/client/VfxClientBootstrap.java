package com.political.vfx.client;

import com.political.vfx.particle.VfxParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;

/**
 * Client-side bootstrap for the Workstream-C custom particle types.
 *
 * <p>Registers a {@link VfxRisingParticle.Provider} for each type declared in
 * {@link VfxParticles}. Like {@link com.political.vfx.VfxBootstrap}, this is NOT
 * auto-wired into a shared entrypoint to avoid concurrent edits — the integration
 * agent calls {@link #initClient()} from the client initializer (after the common
 * {@code VfxBootstrap.init()} has registered the types). See the handoff manifest.</p>
 */
public final class VfxClientBootstrap {

    private VfxClientBootstrap() {}

    /**
     * Registers particle providers for all custom VFX particle types. Safe to call
     * once after {@link VfxParticles#register()} has run; a no-op guard prevents
     * NPEs if the types were never registered.
     */
    public static void initClient() {
        if (VfxParticles.CURSED_EMBER == null) {
            // Types not registered (VfxBootstrap.init() was not wired) — nothing to bind.
            return;
        }
        ParticleProviderRegistry reg = ParticleProviderRegistry.getInstance();
        // tint (0xRRGGBB), scale, lifetime(ticks), gravity, rises?
        reg.register(VfxParticles.CURSED_EMBER,
                s -> new VfxRisingParticle.Provider(s, 0x7A2BD6, 0.9F, 28, -0.004F, true));
        reg.register(VfxParticles.VOID_MOTE,
                s -> new VfxRisingParticle.Provider(s, 0x35106B, 0.8F, 40, 0.0F, false));
        reg.register(VfxParticles.ARC_SPARK,
                s -> new VfxRisingParticle.Provider(s, 0xFFF066, 0.7F, 10, 0.02F, false));
        reg.register(VfxParticles.RUNE_GLYPH,
                s -> new VfxRisingParticle.Provider(s, 0xC06CFF, 1.0F, 22, 0.0F, false));
        reg.register(VfxParticles.RADIANT_MOTE,
                s -> new VfxRisingParticle.Provider(s, 0xFFF4C2, 0.8F, 30, -0.006F, true));
        reg.register(VfxParticles.CINDER,
                s -> new VfxRisingParticle.Provider(s, 0xFF6A1A, 1.0F, 24, 0.03F, false));
    }
}
