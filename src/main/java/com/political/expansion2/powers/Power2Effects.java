package com.political.expansion2.powers;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Shared geometry + VFX helpers for {@link PowerManager2}. */
final class Power2Effects {

    private Power2Effects() {}

    static List<LivingEntity> cone(ServerPlayer p, double range, double tightness) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        List<LivingEntity> out = new ArrayList<>();
        AABB box = p.getBoundingBox().expandTowards(view.scale(range)).inflate(range * 0.5 + 1);
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, box, x -> x != p && x.isAlive())) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double along = to.dot(view);
            if (along <= 0 || along > range) continue;
            if (along / to.length() >= tightness) out.add(e);
        }
        return out;
    }

    static List<LivingEntity> around(ServerPlayer p, double radius) {
        return p.level().getEntitiesOfClass(LivingEntity.class,
                p.getBoundingBox().inflate(radius), x -> x != p && x.isAlive());
    }

    static List<LivingEntity> nearPoint(ServerLevel level, Vec3 c, double r, LivingEntity exclude) {
        AABB box = new AABB(c.x - r, c.y - r, c.z - r, c.x + r, c.y + r, c.z + r);
        return level.getEntitiesOfClass(LivingEntity.class, box, x -> x != exclude && x.isAlive());
    }

    static LivingEntity lookTarget(ServerPlayer p, double range) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        LivingEntity best = null;
        double bestAlong = range;
        AABB box = p.getBoundingBox().expandTowards(view.scale(range)).inflate(2.0);
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, box, x -> x != p && x.isAlive())) {
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

    static Vec3 aimPoint(ServerPlayer p, double dist) {
        return p.getEyePosition().add(p.getViewVector(1f).scale(dist));
    }

    static void launchEntity(LivingEntity e, ServerPlayer p, double strength, double lift) {
        Vec3 dir = p.position().subtract(e.position());
        if (dir.lengthSqr() < 1.0e-4) dir = p.getViewVector(1f).reverse();
        dir = dir.normalize().scale(strength);
        e.push(dir.x, lift, dir.z);
        e.hurtMarked = true;
    }

    static void launchSelf(ServerPlayer p, Vec3 velocity) {
        p.setDeltaMovement(velocity);
        p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(p));
    }

    static void blink(ServerPlayer p, double dist) {
        Vec3 view = p.getViewVector(1f);
        p.teleportTo(p.getX() + view.x * dist, p.getY() + Math.max(0, view.y * dist), p.getZ() + view.z * dist);
        p.fallDistance = 0;
        if (p.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.PORTAL, p.getX(), p.getY() + 1, p.getZ(), 40, 0.5, 1, 0.5, 0.3);
        }
    }

    static void beam(ServerPlayer p, ServerLevel level, double range, float damage, boolean fire, boolean explosive) {
        for (LivingEntity e : cone(p, range, 0.75)) {
            e.hurtServer(level, level.damageSources().playerAttack(p), damage);
            if (fire) e.setRemainingFireTicks(80);
        }
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        for (int i = 1; i <= range; i++) {
            Vec3 at = eye.add(view.scale(i));
            level.sendParticles(explosive ? ParticleTypes.SONIC_BOOM : ParticleTypes.END_ROD,
                    at.x, at.y, at.z, explosive ? 1 : 3, 0.05, 0.05, 0.05, 0.0);
        }
        level.playSound(null, p.getX(), p.getY(), p.getZ(),
                explosive ? SoundEvents.GENERIC_EXPLODE.value() : SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 1.2f, 1.0f);
    }

    static void strike(ServerLevel level, Vec3 at) {
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setPos(at.x, at.y, at.z);
            level.addFreshEntity(bolt);
        }
    }

    static void particleCone(ServerLevel level, ServerPlayer p, net.minecraft.core.particles.SimpleParticleType particle) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        for (int i = 1; i <= 8; i++) {
            Vec3 at = eye.add(view.scale(i * 1.2));
            level.sendParticles(particle, at.x, at.y, at.z, 4, 0.3, 0.3, 0.3, 0.02);
        }
    }

    static void domainRing(ServerLevel level, ServerPlayer p, double radius,
                           net.minecraft.core.particles.SimpleParticleType particle, int count) {
        double cx = p.getX(), cy = p.getY() + 0.5, cz = p.getZ();
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2 * i) / count;
            level.sendParticles(particle, cx + Math.cos(angle) * radius, cy, cz + Math.sin(angle) * radius,
                    2, 0.1, 0.5, 0.1, 0.01);
        }
        level.sendParticles(particle, cx, cy + 1, cz, 80, radius * 0.4, 1.5, radius * 0.4, 0.02);
        level.playSound(null, cx, cy, cz, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.8f, 0.55f);
    }

    static void summonShadows(ServerPlayer p, ServerLevel level, int count) {
        Monster focus = null;
        double bestD = 576;
        for (Monster m : p.level().getEntitiesOfClass(Monster.class, p.getBoundingBox().inflate(24))) {
            if (!m.isAlive()) continue;
            double d = m.distanceToSqr(p);
            if (d < bestD) { focus = m; bestD = d; }
        }
        for (int i = 0; i < count; i++) {
            Mob wolf = EntityTypes.WOLF.create(level, EntitySpawnReason.TRIGGERED);
            if (wolf == null) continue;
            wolf.setPos(p.getX() + (i - 1), p.getY(), p.getZ());
            wolf.setCustomName(Component.literal("Shadow").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
            if (focus != null) wolf.setTarget(focus);
            level.addFreshEntity(wolf);
        }
    }

    static void grantFlight(ServerPlayer p, long durationMs) {
        com.political.flight.FlightManager.enableTimed(p, durationMs);
    }
}
