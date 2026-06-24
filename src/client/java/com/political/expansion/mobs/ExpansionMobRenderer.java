package com.political.expansion.mobs;

import com.political.client.model.Archetype;
import com.political.client.model.ArchetypeModels;
import com.political.client.model.GlowOverlayLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;

/**
 * Renders an {@link ExpansionMob} with a shared {@link Archetype} model + per-type texture. One
 * renderer instance is registered per entity type (see {@code ExpansionMobsClient}), so the
 * archetype, texture and glow flag are captured in the constructor. Boss/mini-boss tiers add a
 * fullbright {@link GlowOverlayLayer}.
 */
public class ExpansionMobRenderer
        extends HumanoidMobRenderer<ExpansionMob, ExpansionMobRenderState, HumanoidModel<ExpansionMobRenderState>> {

    private final Identifier texture;

    public ExpansionMobRenderer(EntityRendererProvider.Context context, Archetype archetype, Identifier texture, boolean glow) {
        super(context, ArchetypeModels.<ExpansionMobRenderState>bake(archetype, context), 0.5f);
        this.texture = texture;
        if (glow) {
            this.addLayer(new GlowOverlayLayer<>(this, texture));
        }
    }

    @Override
    public ExpansionMobRenderState createRenderState() {
        return new ExpansionMobRenderState();
    }

    @Override
    public void extractRenderState(ExpansionMob entity, ExpansionMobRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.politicalAttack = entity.getAttackAnim(partialTick);
        state.politicalPhase = entity.getPhase();
        state.politicalAggressive = entity.isAggressive();
    }

    @Override
    public Identifier getTextureLocation(ExpansionMobRenderState state) {
        return texture;
    }
}
