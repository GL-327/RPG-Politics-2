package com.political.world.biome;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;

/**
 * Injects decorative features into vanilla biomes. Custom biomes generate via
 * {@code data/minecraft/dimension/overworld.json} with vanilla-only feature lists to avoid order cycles.
 */
public final class ContentBiomes {

    private ContentBiomes() {}

    public static void register() {
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_SAVANNA),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.PLACED_FEATURE,
                        Identifier.fromNamespaceAndPath("politicalserver", "auric_bloom")));

        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_TAIGA),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.PLACED_FEATURE,
                        Identifier.fromNamespaceAndPath("politicalserver", "auric_bloom")));

        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_FOREST),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.PLACED_FEATURE,
                        Identifier.fromNamespaceAndPath("politicalserver", "gloomcap_cluster")));
    }
}
