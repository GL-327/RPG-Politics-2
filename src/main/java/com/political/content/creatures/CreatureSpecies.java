package com.political.content.creatures;

import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;

import java.util.function.Predicate;

/**
 * The original ambient creatures introduced by the content workstream. Each species is a peaceful
 * or stoic overworld animal driven by vanilla AI goals ({@link ContentCreature}) and rendered with
 * its own bespoke {@code EntityModel} on the client. AI/behaviour shape is studied from the
 * MIT-licensed Naturalist mod, but all stats, names and art are original.
 */
public enum CreatureSpecies {

    /** A skittish grazing deer of meadows and steppes; flees from threats. */
    MEADOW_STAG("meadow_stag", "Meadow Stag", Role.PREY, ModelKind.STAG,
            18.0, 0.30, 1.0f, 0.9f, 1.4f, 6, 2, 3),

    /** A slow, heavily-shelled reptile of fens and shores; stoic and hard to budge. */
    RIDGEBACK_TORTOISE("ridgeback_tortoise", "Ridgeback Tortoise", Role.STOIC, ModelKind.TORTOISE,
            30.0, 0.12, 1.0f, 1.0f, 0.7f, 4, 1, 2),

    /** A glimmering night-moth of gloamwoods; flutters and drifts, easily startled. */
    GLIMMERMOTH("glimmermoth", "Glimmermoth", Role.PREY, ModelKind.MOTH,
            8.0, 0.26, 1.0f, 0.6f, 0.6f, 5, 2, 4);

    public enum Role { PREY, STOIC }

    public enum ModelKind { STAG, TORTOISE, MOTH }

    public final String id;
    public final String displayName;
    public final Role role;
    public final ModelKind modelKind;
    public final double maxHealth;
    public final double speed;
    public final float scale;
    public final float width;
    public final float height;
    public final int spawnWeight;
    public final int minGroup;
    public final int maxGroup;

    CreatureSpecies(String id, String displayName, Role role, ModelKind modelKind,
                    double maxHealth, double speed, float scale, float width, float height,
                    int spawnWeight, int minGroup, int maxGroup) {
        this.id = id;
        this.displayName = displayName;
        this.role = role;
        this.modelKind = modelKind;
        this.maxHealth = maxHealth;
        this.speed = speed;
        this.scale = scale;
        this.width = width;
        this.height = height;
        this.spawnWeight = spawnWeight;
        this.minGroup = minGroup;
        this.maxGroup = maxGroup;
    }

    /** Biomes this species naturally spawns in (overworld subsets, kept conservative). */
    public Predicate<BiomeSelectionContext> biomeSelector() {
        return switch (this) {
            case MEADOW_STAG -> BiomeSelectors.tag(net.minecraft.tags.BiomeTags.IS_FOREST)
                    .or(BiomeSelectors.includeByKey(net.minecraft.world.level.biome.Biomes.MEADOW,
                            net.minecraft.world.level.biome.Biomes.PLAINS));
            case RIDGEBACK_TORTOISE -> BiomeSelectors.includeByKey(net.minecraft.world.level.biome.Biomes.SWAMP,
                    net.minecraft.world.level.biome.Biomes.MANGROVE_SWAMP,
                    net.minecraft.world.level.biome.Biomes.BEACH);
            case GLIMMERMOTH -> BiomeSelectors.includeByKey(net.minecraft.world.level.biome.Biomes.DARK_FOREST,
                    net.minecraft.world.level.biome.Biomes.OLD_GROWTH_PINE_TAIGA,
                    net.minecraft.world.level.biome.Biomes.TAIGA);
        };
    }
}
