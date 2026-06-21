package com.political.content.creatures;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.EnumMap;
import java.util.Map;

/**
 * Registers the original ambient creatures (see {@link CreatureSpecies}). Builds one
 * {@link EntityType} per species with attributes + ground spawn placement, and injects natural
 * spawns into their biomes via the Fabric Biome Modification API (no TerraBlender).
 *
 * <p>Call {@link #register()} from the common initializer; client renderers live in
 * {@code com.political.content.creatures.client.ContentCreaturesClient}.
 */
public final class ContentCreatures {

    public static final String MOD_ID = "politicalserver";

    private static final Map<CreatureSpecies, EntityType<ContentCreature>> TYPES = new EnumMap<>(CreatureSpecies.class);
    private static final Map<EntityType<?>, CreatureSpecies> BY_TYPE = new java.util.HashMap<>();

    private ContentCreatures() {}

    public static EntityType<ContentCreature> type(CreatureSpecies species) {
        return TYPES.get(species);
    }

    public static CreatureSpecies speciesForType(EntityType<?> type) {
        return BY_TYPE.get(type);
    }

    public static void register() {
        for (CreatureSpecies species : CreatureSpecies.values()) {
            registerType(species);
        }
        registerSpawns();
    }

    public static ContentCreature spawnById(ServerLevel level, BlockPos pos, CreatureSpecies species) {
        EntityType<ContentCreature> type = TYPES.get(species);
        if (type == null) return null;
        ContentCreature creature = type.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (creature == null) return null;
        creature.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        level.addFreshEntity(creature);
        return creature;
    }

    private static void registerType(CreatureSpecies species) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(MOD_ID, species.id));
        EntityType<ContentCreature> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
                EntityType.Builder.<ContentCreature>of(ContentCreature::new, MobCategory.CREATURE)
                        .sized(species.width, species.height)
                        .build(key));
        FabricDefaultAttributeRegistry.register(type, ContentCreature.createAttributes(species));
        TYPES.put(species, type);
        BY_TYPE.put(type, species);
    }

    private static void registerSpawns() {
        for (CreatureSpecies species : CreatureSpecies.values()) {
            EntityType<ContentCreature> type = TYPES.get(species);
            if (type == null) continue;
            SpawnPlacements.register(type, SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
            if (species.spawnWeight > 0) {
                BiomeModifications.addSpawn(species.biomeSelector(), MobCategory.CREATURE, type,
                        species.spawnWeight, species.minGroup, species.maxGroup);
            }
        }
    }
}
