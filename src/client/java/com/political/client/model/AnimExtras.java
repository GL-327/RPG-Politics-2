package com.political.client.model;

/**
 * Optional render-state extension read by the archetype models for richer, state-driven animation.
 *
 * <p>The shared archetype models are generic over {@link net.minecraft.client.renderer.entity.state.HumanoidRenderState},
 * so they cannot see package-specific fields directly. Render states that carry combat/boss data
 * implement this interface, and the models read it via {@code instanceof} — keeping the model library
 * decoupled while still letting attack ferocity and boss phase drive special poses.
 */
public interface AnimExtras {

    /** Smoothed melee attack progress in {@code [0,1]}; {@code 0} when not mid-swing. */
    float politicalAttackStrength();

    /** Boss phase (1 = normal, 2 = enraged). Non-boss creatures report {@code 1}. */
    int politicalBossPhase();

    /** True while the creature is actively hunting a target. */
    boolean politicalAggressive();
}
