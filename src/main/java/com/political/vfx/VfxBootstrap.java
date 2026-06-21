package com.political.vfx;

import com.political.sound.VfxSounds;
import com.political.vfx.particle.VfxParticles;

/**
 * Optional, self-contained bootstrap for the Workstream-C VFX library.
 *
 * <p>Registering custom particle types and sound events normally requires a hook
 * from a shared entrypoint. To avoid editing shared files concurrently with other
 * workstreams, that wiring is deliberately <b>not</b> performed here automatically.
 * Instead, the integration agent adds two one-liners (see
 * {@code docs/integration/handoff/C_vfx.md}):</p>
 *
 * <pre>
 *   // in RpgPoliticsMod#onInitialize() (common):
 *   com.political.vfx.VfxBootstrap.init();
 *
 *   // in PoliticalClient#onInitializeClient() (client):
 *   com.political.vfx.client.VfxClientBootstrap.initClient();
 * </pre>
 *
 * <p>Both calls are safe no-ops if invoked twice (the registries themselves guard
 * against double registration via the {@code register()} idempotency checks).</p>
 */
public final class VfxBootstrap {

    private VfxBootstrap() {}

    /**
     * Common-side bootstrap: registers custom {@link VfxParticles} particle types
     * and {@link VfxSounds} sound events. Call from the common initializer.
     */
    public static void init() {
        VfxParticles.register();
        VfxSounds.register();
    }
}
