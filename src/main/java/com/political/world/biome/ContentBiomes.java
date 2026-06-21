package com.political.world.biome;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Injects the content workstream's original worldgen into the overworld via the Fabric Biome
 * Modification API (no TerraBlender). Two things are wired here:
 *
 * <ol>
 *   <li>Decorative placed features — {@code auric_bloom} (golden flowers) and {@code gloomcap_cluster}
 *       (dim mushrooms) — added to thematically-matching vanilla biomes.</li>
 * </ol>
 *
 * <p>The five authored standalone biomes (datapack JSON under
 * {@code data/politicalserver/worldgen/biome/}) are valid and loadable. Making them <em>generate</em>
 * as first-class overworld biomes additionally requires adding them to a multi-noise parameter list
 * datapack (a documented follow-up — see the integration manifest), which is intentionally left out
 * of code to stay TerraBlender-free and non-invasive.
 *
 * <p>Creature natural spawns are injected separately in {@code ContentCreatures#register()}.
 */
public final class ContentBiomes {

    private static final String MOD_ID = "politicalserver";

    private static final ResourceKey<PlacedFeature> AURIC_BLOOM = placed("auric_bloom");
    private static final ResourceKey<PlacedFeature> GLOOMCAP_CLUSTER = placed("gloomcap_cluster");

    private ContentBiomes() {}

    public static void register() {
        // Golden blooms across open, sunlit overworld.
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(Biomes.PLAINS, Biomes.MEADOW, Biomes.SAVANNA,
                        Biomes.SUNFLOWER_PLAINS, Biomes.SNOWY_PLAINS),
                GenerationStep.Decoration.VEGETAL_DECORATION, AURIC_BLOOM);

        // Dim mushroom clusters across shaded forests and wetlands.
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_FOREST)
                        .or(BiomeSelectors.includeByKey(Biomes.SWAMP, Biomes.MANGROVE_SWAMP, Biomes.DARK_FOREST)),
                GenerationStep.Decoration.VEGETAL_DECORATION, GLOOMCAP_CLUSTER);

        // Also decorate our authored standalone biomes when they appear in worldgen.
        var custom = BiomeSelectors.includeByKey(
                ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "auric_steppe")),
                ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "ashen_barrens")),
                ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "frostpetal_tundra")),
                ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "gloamwood")),
                ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "mistveil_fen")));
        BiomeModifications.addFeature(custom, GenerationStep.Decoration.VEGETAL_DECORATION, AURIC_BLOOM);
        BiomeModifications.addFeature(custom, GenerationStep.Decoration.VEGETAL_DECORATION, GLOOMCAP_CLUSTER);
    }

    private static ResourceKey<PlacedFeature> placed(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(MOD_ID, path));
    }
}
