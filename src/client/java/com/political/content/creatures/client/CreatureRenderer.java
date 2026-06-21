package com.political.content.creatures.client;

import com.political.content.creatures.ContentCreature;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/**
 * Generic renderer for every {@link ContentCreature}: pairs a bespoke {@link EntityModel} with a
 * per-species texture. Walk/idle animation is extracted from the entity into {@link CreatureRenderState}
 * automatically by the {@link MobRenderer} pipeline.
 */
public class CreatureRenderer extends MobRenderer<ContentCreature, CreatureRenderState, EntityModel<CreatureRenderState>> {

    private final Identifier texture;

    public CreatureRenderer(EntityRendererProvider.Context context, EntityModel<CreatureRenderState> model,
                            Identifier texture, float shadowRadius) {
        super(context, model, shadowRadius);
        this.texture = texture;
    }

    @Override
    public CreatureRenderState createRenderState() {
        return new CreatureRenderState();
    }

    @Override
    public Identifier getTextureLocation(CreatureRenderState state) {
        return texture;
    }
}
