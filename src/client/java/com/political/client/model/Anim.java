package com.political.client.model;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Tiny shared animation helpers used by the archetype models so attack/aggression poses are derived
 * consistently. Falls back to plain {@link HumanoidRenderState} fields when the state does not carry
 * the richer {@link AnimExtras} data, so every model animates whether or not its render state was
 * extended.
 */
public final class Anim {

    private Anim() {}

    /** Attack swing strength in {@code [0,1]} (prefers {@link AnimExtras}, else vanilla attackTime). */
    public static float attack(HumanoidRenderState state) {
        if (state instanceof AnimExtras x) {
            float s = x.politicalAttackStrength();
            if (s > 0f) return s;
        }
        return state.attackTime;
    }

    /** Boss phase from {@link AnimExtras}, or 1 if not carried. */
    public static int phase(HumanoidRenderState state) {
        return state instanceof AnimExtras x ? Math.max(1, x.politicalBossPhase()) : 1;
    }

    /** Whether the creature is hunting (prefers {@link AnimExtras}, else true while moving). */
    public static boolean aggressive(HumanoidRenderState state) {
        if (state instanceof AnimExtras x) return x.politicalAggressive();
        return state.walkAnimationSpeed > 0.1f;
    }
}
