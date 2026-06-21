package com.political.curse.domain;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * The "sure-hit" per-tick body of a {@link CursedDomain}: applied to every living entity caught inside
 * the domain on each application tick, regardless of line of sight.
 *
 * @param age the domain's age in ticks (for scaling/animation)
 */
@FunctionalInterface
public interface DomainEffect {
    void apply(ServerLevel level, ServerPlayer caster, LivingEntity victim, int age);
}
