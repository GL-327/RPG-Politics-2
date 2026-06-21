package com.political.expansion.mobs;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Render state for {@link ExpansionMob}. A thin concrete subtype of {@link HumanoidRenderState} so
 * the shared humanoid rig drives walk/attack/idle animation; the per-creature texture is fixed on
 * the renderer instance (one renderer is registered per entity type).
 */
public class ExpansionMobRenderState extends HumanoidRenderState {
}
