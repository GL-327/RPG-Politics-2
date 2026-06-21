package com.political.content.creatures.client;

import com.political.content.creatures.ContentCreatures;
import com.political.content.creatures.CreatureSpecies;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

/**
 * Client binder for the original ambient creatures: registers each species' bespoke model layer and
 * a {@link CreatureRenderer} bound to its texture. Call {@link #registerClient()} once at client
 * init (see the integration manifest).
 */
public final class ContentCreaturesClient {

    private static final String MOD_ID = "politicalserver";

    public static final ModelLayerLocation STAG_LAYER = layer("meadow_stag");
    public static final ModelLayerLocation TORTOISE_LAYER = layer("ridgeback_tortoise");
    public static final ModelLayerLocation MOTH_LAYER = layer("glimmermoth");

    private ContentCreaturesClient() {}

    public static void registerClient() {
        ModelLayerRegistry.registerModelLayer(STAG_LAYER, StagModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(TORTOISE_LAYER, TortoiseModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MOTH_LAYER, MothModel::createBodyLayer);

        register(CreatureSpecies.MEADOW_STAG, STAG_LAYER, StagModel::new, 0.6f);
        register(CreatureSpecies.RIDGEBACK_TORTOISE, TORTOISE_LAYER, TortoiseModel::new, 0.6f);
        register(CreatureSpecies.GLIMMERMOTH, MOTH_LAYER, MothModel::new, 0.3f);
    }

    private static void register(CreatureSpecies species, ModelLayerLocation layer,
                                 java.util.function.Function<ModelPart, EntityModel<CreatureRenderState>> factory,
                                 float shadow) {
        Identifier texture = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/" + species.id + ".png");
        EntityRendererRegistry.register(ContentCreatures.type(species),
                context -> new CreatureRenderer(context, factory.apply(context.bakeLayer(layer)), texture, shadow));
    }

    private static ModelLayerLocation layer(String id) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "creature_" + id), "main");
    }
}
