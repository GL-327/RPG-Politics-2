package com.political.client;

import com.political.curse.CurseEntity;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

/**
 * Renders the custom {@link CurseEntity} with its own horned humanoid model + cursed texture.
 * Walk/attack/idle motion comes from the humanoid rig; per-grade size comes from the entity's
 * scale attribute (set server-side in {@code CurseManager.manifest}).
 */
public class CurseRenderer extends HumanoidMobRenderer<CurseEntity, ZombieRenderState, ZombieModel<ZombieRenderState>> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("politicalserver", "textures/entity/curse_spirit.png");

    public CurseRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieModel<>(context.bakeLayer(CurseModels.CURSE_LAYER)), 0.5f);
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return TEXTURE;
    }
}
