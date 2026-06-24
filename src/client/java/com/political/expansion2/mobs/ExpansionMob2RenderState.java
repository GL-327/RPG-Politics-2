package com.political.expansion2.mobs;

import com.political.client.model.AnimExtras;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Render state for {@link ExpansionMob2}. Carries {@link AnimExtras} (attack strength, boss phase,
 * aggression) extracted from the entity so the shared archetype models can play state-driven attack
 * lunges and enraged boss poses.
 */
public class ExpansionMob2RenderState extends HumanoidRenderState implements AnimExtras {

    public float politicalAttack;
    public int politicalPhase = 1;
    public boolean politicalAggressive;

    @Override
    public float politicalAttackStrength() {
        return politicalAttack;
    }

    @Override
    public int politicalBossPhase() {
        return politicalPhase;
    }

    @Override
    public boolean politicalAggressive() {
        return politicalAggressive;
    }
}
