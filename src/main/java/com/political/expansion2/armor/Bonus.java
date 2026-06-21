package com.political.expansion2.armor;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

final class Bonus {
    private static final int DURATION = 100;
    private Bonus() {}
    static void effect(ServerPlayer player, Holder<MobEffect> effect, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, DURATION, amplifier, true, false));
    }
    static void heal(ServerPlayer player, float amount) {
        if (player.getHealth() < player.getMaxHealth()) player.heal(amount);
    }
    static void igniteNearby(ServerPlayer player, double radius, int fireTicks) {
        for (Monster m : player.level().getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(radius))) {
            m.setRemainingFireTicks(fireTicks);
        }
    }
    static void afflictNearby(ServerPlayer player, Holder<MobEffect> effect, double radius, int duration, int amplifier) {
        for (Monster m : player.level().getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(radius))) {
            m.addEffect(new MobEffectInstance(effect, duration, amplifier, true, true));
        }
    }
}
