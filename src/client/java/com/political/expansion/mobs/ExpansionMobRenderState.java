package com.political.expansion.mobs;

import com.political.client.model.AnimExtras;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Render state for {@link ExpansionMob}. A concrete {@link HumanoidRenderState} so the shared
 * humanoid rig drives walk/attack/idle animation; it also carries {@link AnimExtras} (attack
 * strength, boss phase, aggression) extracted from the entity so the archetype models can play
 * state-driven attack lunges and enraged boss poses. The per-creature texture is fixed on the
 * renderer instance (one renderer is registered per entity type).
 */
public class ExpansionMobRenderState extends HumanoidRenderState implements AnimExtras {

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
