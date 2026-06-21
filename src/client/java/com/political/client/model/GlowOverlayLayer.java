package com.political.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * A mixin-free, fullbright additive overlay for boss-tier entities. Re-renders the model once more
 * with {@link RenderType#eyes} (emissive, ignores world light), which makes the whole creature read
 * as glowing/menacing. Reuses the entity's own texture as the glow mask so no extra texture art is
 * required; the additive blend brightens lit areas into a halo while transparent regions stay dark.
 *
 * <p>Wired by the per-package renderers for {@code boss}/{@code miniboss}-tier creatures via
 * {@code addLayer(new GlowOverlayLayer<>(this, texture))}.
 */
public class GlowOverlayLayer<S extends LivingEntityRenderState, M extends EntityModel<S>> extends EyesLayer<S, M> {

    private final RenderType renderType;

    public GlowOverlayLayer(RenderLayerParent<S, M> parent, Identifier texture) {
        super(parent);
        this.renderType = RenderTypes.eyes(texture);
    }

    @Override
    public RenderType renderType() {
        return renderType;
    }
}
