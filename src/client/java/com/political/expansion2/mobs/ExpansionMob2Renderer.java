package com.political.expansion2.mobs;

import com.political.client.model.Archetype;
import com.political.client.model.ArchetypeModels;
import com.political.client.model.GlowOverlayLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;

/**
 * Renders an {@link ExpansionMob2} with a shared {@link Archetype} model + per-type texture. Boss /
 * mini-boss tiers add a fullbright {@link GlowOverlayLayer}.
 */
public class ExpansionMob2Renderer
        extends HumanoidMobRenderer<ExpansionMob2, ExpansionMob2RenderState, HumanoidModel<ExpansionMob2RenderState>> {

    private final Identifier texture;

    public ExpansionMob2Renderer(EntityRendererProvider.Context context, Archetype archetype, Identifier texture, boolean glow) {
        super(context, ArchetypeModels.<ExpansionMob2RenderState>bake(archetype, context), 0.5f);
        this.texture = texture;
        if (glow) {
            this.addLayer(new GlowOverlayLayer<>(this, texture));
        }
    }

    @Override
    public ExpansionMob2RenderState createRenderState() {
        return new ExpansionMob2RenderState();
    }

    @Override
    public void extractRenderState(ExpansionMob2 entity, ExpansionMob2RenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.politicalAttack = entity.getAttackAnim(partialTick);
        state.politicalPhase = entity.getPhase();
        state.politicalAggressive = entity.isAggressive();
    }

    @Override
    public Identifier getTextureLocation(ExpansionMob2RenderState state) {
        return texture;
    }
}
