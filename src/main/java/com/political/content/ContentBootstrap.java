package com.political.content;

import com.political.RpgPoliticsMod;

/**
 * Single entry point for everything the CONTENT workstream adds (ability accessories, original
 * ambient creatures, datapack-biome feature injection, and the original surface-structure set).
 *
 * <p>This is intentionally <b>not</b> called from the shared mod entrypoints to avoid merge
 * conflicts with the other parallel workstreams. Integration is a two-line wiring job documented in
 * {@code docs/integration/handoff/B_content.md}:
 *
 * <pre>
 *   // in RpgPoliticsMod.onInitialize():
 *   com.political.content.ContentBootstrap.init();
 *
 *   // in PoliticalClient.onInitializeClient():
 *   com.political.content.client.ContentClientBootstrap.initClient();
 * </pre>
 *
 * Everything compiles and is registry-safe today; until the wiring lines above are added, the
 * content simply lies dormant (classes load, nothing is registered).
 *
 * <p>(Client renderer/model registration lives in {@code com.political.content.client.ContentClientBootstrap}
 * because Loom keeps the client source set separate from the common source set.)
 */
public final class ContentBootstrap {

    private static boolean initialized = false;

    private ContentBootstrap() {}

    /** Common/server registration. Call once from the common initializer. */
    public static void init() {
        if (initialized) return;
        initialized = true;

        com.political.expansion2.accessories.AbilityAccessories2.register();
        com.political.content.creatures.ContentCreatures.register();
        com.political.world.biome.ContentBiomes.register();

        RpgPoliticsMod.LOGGER.info("Content workstream initialized: {} ability accessories, {} creatures, biome features.",
                com.political.expansion2.accessories.AbilityAccessories2.items().size(),
                com.political.content.creatures.CreatureSpecies.values().length);
    }
}
