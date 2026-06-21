package com.political.curse.spirits;

import com.political.client.model.ArchetypeModels;
import com.political.client.model.GlowOverlayLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

/**
 * Renderer for every {@link SpiritSpecies}. Each registered {@code EntityType} binds an instance
 * carrying that species, so the correct shared archetype model (per {@link SpiritSpecies.ModelKind})
 * is baked and the matching {@code textures/entity/<id>.png} is used. Boss species additionally get a
 * fullbright {@link GlowOverlayLayer}. Walk/attack/idle motion comes from the humanoid rig +
 * archetype {@code setupAnim}; per-grade size comes from the entity SCALE attribute.
 */
public class SpiritRenderer extends HumanoidMobRenderer<CursedSpiritEntity, ZombieRenderState, HumanoidModel<ZombieRenderState>> {

    private final Identifier texture;

    public SpiritRenderer(EntityRendererProvider.Context context, SpiritSpecies species) {
        super(context,
                ArchetypeModels.<ZombieRenderState>bake(SpiritModels.archetypeFor(species.modelKind()), context),
                0.5f * Math.max(0.5f, species.baseScale()));
        this.texture = Identifier.fromNamespaceAndPath("politicalserver", "textures/entity/" + species.id() + ".png");
        if (species.boss()) {
            this.addLayer(new GlowOverlayLayer<>(this, this.texture));
        }
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return texture;
    }
}
