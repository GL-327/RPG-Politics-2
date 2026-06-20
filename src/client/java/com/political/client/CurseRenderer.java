package com.political.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

/** Renders the custom Curse using the humanoid (zombie) model but with a custom cursed texture. */
public class CurseRenderer extends ZombieRenderer {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("politicalserver", "textures/entity/curse_spirit.png");

    public CurseRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return TEXTURE;
    }
}
