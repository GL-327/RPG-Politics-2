package com.political.curse.technique;

import com.political.curse.SorcererGrade;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-cast scratch state handed to a {@link TechniqueResolver}: the caster, their level, eye position
 * and view vector, plus a handful of targeting helpers mirroring the geometry used elsewhere in the
 * mod (see {@code Power2Effects}). Resolvers use these to find victims and deal player-attributed
 * damage; the visual layer is left to {@code VfxHelper}.
 */
public final class TechniqueContext {

    public final ServerPlayer caster;
    public final ServerLevel level;
    public final Vec3 eye;
    public final Vec3 view;

    public TechniqueContext(ServerPlayer caster) {
        this.caster = caster;
        this.level = caster.level();
        this.eye = caster.getEyePosition();
        this.view = caster.getViewVector(1f);
    }

    public int grade() {
        return SorcererGrade.of(caster);
    }

    /** Entities inside a forward cone. {@code tightness} is the minimum cos(angle) (1 = laser, 0.5 ≈ 60°). */
    public List<LivingEntity> cone(double range, double tightness) {
        List<LivingEntity> out = new ArrayList<>();
        AABB box = caster.getBoundingBox().expandTowards(view.scale(range)).inflate(range * 0.5 + 1);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, x -> x != caster && x.isAlive())) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double along = to.dot(view);
            if (along <= 0 || along > range) continue;
            if (along / to.length() >= tightness) out.add(e);
        }
        return out;
    }

    /** Living entities around the caster within {@code radius}. */
    public List<LivingEntity> around(double radius) {
        return level.getEntitiesOfClass(LivingEntity.class,
                caster.getBoundingBox().inflate(radius), x -> x != caster && x.isAlive());
    }

    /** The closest entity the caster is looking at within {@code range}, or {@code null}. */
    public LivingEntity lookTarget(double range) {
        LivingEntity best = null;
        double bestAlong = range;
        AABB box = caster.getBoundingBox().expandTowards(view.scale(range)).inflate(2.0);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, x -> x != caster && x.isAlive())) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double along = to.dot(view);
            if (along <= 0 || along > range) continue;
            if (to.subtract(view.scale(along)).lengthSqr() <= 2.5 && along < bestAlong) {
                best = e;
                bestAlong = along;
            }
        }
        return best;
    }

    /** The nearest living entity within {@code radius} (excludes the caster), or {@code null}. */
    public LivingEntity nearest(double radius) {
        LivingEntity best = null;
        double bestSqr = radius * radius;
        for (LivingEntity e : around(radius)) {
            double d = e.distanceToSqr(caster);
            if (d < bestSqr) {
                best = e;
                bestSqr = d;
            }
        }
        return best;
    }

    public Vec3 aim(double dist) {
        return eye.add(view.scale(dist));
    }

    /** Deals player-attributed magic damage so kills credit the caster (exorcism rewards/progression). */
    public void hurt(LivingEntity victim, float damage) {
        victim.hurtServer(level, level.damageSources().playerAttack(caster), damage);
    }
}
