package com.political.expansion.armor;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

/**
 * Tiny effect helpers for full-set bonuses. Everything is applied through vanilla mob effects /
 * mechanics (no mixins, no shared-manager edits), mirroring how
 * {@link com.political.combat.AbilityEngine} applies its armour passives.
 */
final class Bonus {

    /** Refreshed every set-bonus tick; longer than the tick interval so the effect never flickers. */
    private static final int DURATION = 100;

    private Bonus() {}

    /** Applies a hidden, refreshing potion effect to the wearer. */
    static void effect(ServerPlayer player, Holder<MobEffect> effect, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, DURATION, amplifier, true, false));
    }

    /** Heals the wearer (set bonuses like Bloodmoon's lifedraw). */
    static void heal(ServerPlayer player, float amount) {
        if (player.getHealth() < player.getMaxHealth()) {
            player.heal(amount);
        }
    }

    /** Sets nearby hostile mobs on fire (Emberforge). */
    static void igniteNearby(ServerPlayer player, double radius, int fireTicks) {
        AABB box = player.getBoundingBox().inflate(radius);
        for (Monster m : player.level().getEntitiesOfClass(Monster.class, box)) {
            m.setRemainingFireTicks(fireTicks);
        }
    }

    /** Afflicts nearby hostile mobs with a debuff (Frostguard slow, Wraith wither). */
    static void afflictNearby(ServerPlayer player, Holder<MobEffect> effect, double radius, int duration, int amplifier) {
        AABB box = player.getBoundingBox().inflate(radius);
        for (Monster m : player.level().getEntitiesOfClass(Monster.class, box)) {
            m.addEffect(new MobEffectInstance(effect, duration, amplifier, true, true));
        }
    }
}
