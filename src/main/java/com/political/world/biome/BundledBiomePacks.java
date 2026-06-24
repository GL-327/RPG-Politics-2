package com.political.world.biome;

import com.political.RpgPoliticsMod;

/**
 * Documents bundled biome datapacks imported into {@code src/main/resources/data/}.
 * <ul>
 *   <li><b>Terralith</b> (MIT) — full worldgen datapack (~96 biomes, structures, features).</li>
 *   <li><b>Biomes O' Plenty</b> — 68 biome climates + lang; vanilla feature templates (no BOP Java blocks).</li>
 *   <li><b>Oh The Biomes We've Gone</b> — 55 biome climates + lang; vanilla feature templates.</li>
 *   <li><b>politicalserver</b> — 14 original biomes merged into Terralith's {@code minecraft:dimension/overworld.json}.</li>
 * </ul>
 * Re-import: {@code node tools/import-biome-packs.mjs}
 */
public final class BundledBiomePacks {

    private BundledBiomePacks() {}

    public static void logLoaded() {
        RpgPoliticsMod.LOGGER.info(
                "Bundled biome packs active: Terralith + BOP + BWG + politicalserver (see BundledBiomePacks javadoc).");
    }
}
