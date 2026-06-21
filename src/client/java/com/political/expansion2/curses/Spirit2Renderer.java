package com.political.expansion2.curses;

import com.political.client.model.ArchetypeModels;
import com.political.client.model.GlowOverlayLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

/**
 * Renderer for every {@link SpiritSpecies2}. Bakes the shared archetype model for the species'
 * {@link SpiritSpecies2.ModelKind}, binds {@code textures/entity/<id>.png}, and adds a fullbright
 * {@link GlowOverlayLayer} for boss species.
 */
public class Spirit2Renderer extends HumanoidMobRenderer<CursedSpirit2Entity, ZombieRenderState, HumanoidModel<ZombieRenderState>> {

    private final Identifier texture;

    public Spirit2Renderer(EntityRendererProvider.Context context, SpiritSpecies2 species) {
        super(context,
                ArchetypeModels.<ZombieRenderState>bake(Spirit2Models.archetypeFor(species.modelKind()), context),
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
