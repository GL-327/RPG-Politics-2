package com.political.client.model;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

import java.util.EnumMap;
import java.util.Map;

/**
 * Central registry + factory for the shared {@link Archetype} model library.
 *
 * <p>Every archetype owns exactly one baked {@link ModelLayerLocation} (id
 * {@code politicalserver:archetype_<id>}) shared across all four content packages. Call
 * {@link #registerLayers()} once at client init (it is idempotent, so each package's
 * {@code registerClient()} may safely call it). Then build the right {@link HumanoidModel} for an
 * entity with {@link #bake(Archetype, EntityRendererProvider.Context)}.
 *
 * <p>All archetype models subclass {@link HumanoidModel}, so they slot into the existing
 * {@code HumanoidMobRenderer} pipeline unchanged and inherit vanilla head-tracking / limb-swing /
 * attack animation, while each adds its own distinct geometry and appendage motion.
 */
public final class ArchetypeModels {

    private static final Map<Archetype, ModelLayerLocation> LAYERS = new EnumMap<>(Archetype.class);

    static {
        for (Archetype a : Archetype.values()) {
            LAYERS.put(a, new ModelLayerLocation(
                    Identifier.fromNamespaceAndPath("politicalserver", "archetype_" + a.id()), "main"));
        }
    }

    private static boolean layersRegistered = false;

    private ArchetypeModels() {}

    public static ModelLayerLocation layerOf(Archetype archetype) {
        return LAYERS.get(archetype);
    }

    /** Registers every archetype's baked layer. Idempotent — safe to call from each package. */
    public static void registerLayers() {
        if (layersRegistered) return;
        layersRegistered = true;
        ModelLayerRegistry.registerModelLayer(LAYERS.get(Archetype.GAUNT_HUMANOID), GauntModel::createLayer);
        ModelLayerRegistry.registerModelLayer(LAYERS.get(Archetype.HULKING_BRUTE), BruteModel::createLayer);
        ModelLayerRegistry.registerModelLayer(LAYERS.get(Archetype.SERPENTINE), SerpentModel::createLayer);
        ModelLayerRegistry.registerModelLayer(LAYERS.get(Archetype.MULTI_ARMED), MultiArmModel::createLayer);
        ModelLayerRegistry.registerModelLayer(LAYERS.get(Archetype.WINGED), WingedModel::createLayer);
        ModelLayerRegistry.registerModelLayer(LAYERS.get(Archetype.QUADRUPED), QuadrupedModel::createLayer);
        ModelLayerRegistry.registerModelLayer(LAYERS.get(Archetype.CRAWLER), CrawlerModel::createLayer);
        ModelLayerRegistry.registerModelLayer(LAYERS.get(Archetype.CLOAKED_SPIRIT), CloakedModel::createLayer);
        ModelLayerRegistry.registerModelLayer(LAYERS.get(Archetype.BOSS_COLOSSUS), ColossusModel::createLayer);
        ModelLayerRegistry.registerModelLayer(LAYERS.get(Archetype.TINY_SWARM), SwarmModel::createLayer);
    }

    /** Bakes the archetype's layer in the given render context and wraps it in the matching model. */
    public static <S extends HumanoidRenderState> HumanoidModel<S> bake(
            Archetype archetype, EntityRendererProvider.Context context) {
        return create(archetype, context.bakeLayer(layerOf(archetype)));
    }

    /** Constructs the model for an archetype around an already-baked root part. */
    public static <S extends HumanoidRenderState> HumanoidModel<S> create(Archetype archetype, ModelPart root) {
        return switch (archetype) {
            case GAUNT_HUMANOID -> new GauntModel<>(root);
            case HULKING_BRUTE -> new BruteModel<>(root);
            case SERPENTINE -> new SerpentModel<>(root);
            case MULTI_ARMED -> new MultiArmModel<>(root);
            case WINGED -> new WingedModel<>(root);
            case QUADRUPED -> new QuadrupedModel<>(root);
            case CRAWLER -> new CrawlerModel<>(root);
            case CLOAKED_SPIRIT -> new CloakedModel<>(root);
            case BOSS_COLOSSUS -> new ColossusModel<>(root);
            case TINY_SWARM -> new SwarmModel<>(root);
        };
    }
}
