package com.political.expansion.ranged;

import com.political.combat.StatManager;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Self-contained RIGHT CLICK cast engine for the ranged &amp; magic expansion. Implemented
 * entirely with Fabric's {@link UseItemCallback} + vanilla mechanics (raycasts, cones,
 * area queries, lightning entities and particle "projectile" trails) — no mixins. Spell
 * power scales with the caster's Intelligence (their max Mana), satisfying the Skyblock
 * intelligence/mana scaling contract.
 *
 * <p>Cooldowns and Mana are tracked here independently of the shared item-ability engine,
 * so the two systems never collide.
 */
public final class RangedAbilityEngine {

    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private RangedAbilityEngine() {}

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            RangedCast cast = RangedItems.castOf(sp.getMainHandItem());
            if (cast == null) return InteractionResult.PASS;

            if (onCooldown(sp, cast)) {
                sp.sendSystemMessage(Component.literal(cast.displayName + " is recharging ("
                        + cooldownRemaining(sp, cast) + "s).").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            if (cast.manaCost > 0 && !StatManager.spendMana(sp, cast.manaCost)) {
                sp.sendSystemMessage(Component.literal("Not enough Mana (" + cast.manaCost + " needed).")
                        .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            ServerLevel level = (ServerLevel) world;
            boolean landed = cast(sp, level, cast);
            if (!landed) {
                if (cast.manaCost > 0) StatManager.addMana(sp, cast.manaCost); // refund whiffed target casts
                sp.sendSystemMessage(Component.literal("No target in sight for " + cast.displayName + ".")
                        .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            setCooldown(sp, cast);
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(), soundFor(cast), SoundSource.PLAYERS, 0.9f, 1.0f);
            return InteractionResult.SUCCESS;
        });
    }

    // ------------------------------------------------------------------
    // Cooldowns
    // ------------------------------------------------------------------

    private static boolean onCooldown(ServerPlayer p, RangedCast c) {
        Long ready = COOLDOWNS.get(p.getStringUUID() + "|" + c.name());
        return ready != null && System.currentTimeMillis() < ready;
    }

    private static long cooldownRemaining(ServerPlayer p, RangedCast c) {
        Long ready = COOLDOWNS.get(p.getStringUUID() + "|" + c.name());
        return ready == null ? 0 : Math.max(0, (ready - System.currentTimeMillis()) / 1000);
    }

    private static void setCooldown(ServerPlayer p, RangedCast c) {
        COOLDOWNS.put(p.getStringUUID() + "|" + c.name(),
                System.currentTimeMillis() + c.cooldownSeconds * 1000L);
    }

    // ------------------------------------------------------------------
    // Casting
    // ------------------------------------------------------------------

    /** Returns {@code false} only for strictly-targeted casts that found nothing (so Mana is refunded). */
    private static boolean cast(ServerPlayer p, ServerLevel level, RangedCast c) {
        float dmg = scaled(p, c.power);
        int r = c.range;
        switch (c) {
            case FIREBOLT -> {
                LivingEntity t = lookTarget(p, r);
                particleTrail(level, p, ParticleTypes.FLAME, r);
                if (t == null) return false;
                t.setRemainingFireTicks(120);
                hurt(level, p, t, dmg);
                level.sendParticles(ParticleTypes.LAVA, t.getX(), t.getY() + 1, t.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
            }
            case FLAME_WAVE -> {
                for (LivingEntity e : cone(p, r, 0.5)) {
                    e.setRemainingFireTicks(120);
                    hurt(level, p, e, dmg);
                }
                particleCone(level, p, ParticleTypes.FLAME);
            }
            case METEOR -> {
                Vec3 at = aimPoint(p, r);
                for (LivingEntity e : nearPoint(level, at, 4.5, p)) {
                    e.setRemainingFireTicks(140);
                    hurt(level, p, e, dmg);
                    launchAway(e, at, 1.0);
                }
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, at.x, at.y, at.z, 1, 0, 0, 0, 0.0);
                level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.5, at.z, 40, 2, 1, 2, 0.05);
            }
            case FROST_VOLLEY -> {
                for (LivingEntity e : cone(p, r, 0.5)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
                    hurt(level, p, e, dmg);
                }
                particleCone(level, p, ParticleTypes.SNOWFLAKE);
            }
            case BLIZZARD -> {
                Vec3 at = aimPoint(p, r);
                for (LivingEntity e : nearPoint(level, at, 5.0, p)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 3));
                    hurt(level, p, e, dmg);
                }
                level.sendParticles(ParticleTypes.SNOWFLAKE, at.x, at.y + 1, at.z, 80, 3, 1.5, 3, 0.05);
            }
            case CHAIN_LIGHTNING -> {
                LivingEntity t = lookTarget(p, r);
                if (t == null) return false;
                strike(level, t.position());
                hurt(level, p, t, dmg);
                for (LivingEntity e : nearPoint(level, t.position(), 5.0, p)) {
                    if (e == t) continue;
                    strike(level, e.position());
                    hurt(level, p, e, dmg * 0.6f);
                }
            }
            case STORM_FIELD -> {
                Vec3 at = aimPoint(p, r);
                List<LivingEntity> hit = nearPoint(level, at, 6.0, p);
                for (LivingEntity e : hit) {
                    strike(level, e.position());
                    hurt(level, p, e, dmg);
                }
                if (hit.isEmpty()) strike(level, at);
            }
            case SOUL_BEAM -> {
                float drained = 0f;
                for (LivingEntity e : cone(p, r, 0.7)) {
                    hurt(level, p, e, dmg);
                    drained += 2.5f;
                }
                p.heal(Math.min(14f, drained));
                particleTrail(level, p, ParticleTypes.SOUL, r);
            }
            case VOID_LANCE -> {
                for (LivingEntity e : cone(p, r, 0.85)) hurt(level, p, e, dmg);
                particleTrail(level, p, ParticleTypes.REVERSE_PORTAL, r);
            }
            case WIND_SHEAR -> {
                for (LivingEntity e : cone(p, r, 0.45)) {
                    hurt(level, p, e, dmg);
                    launchFrom(e, p, 2.0, 0.5);
                }
                particleCone(level, p, ParticleTypes.CLOUD);
            }
            case ARROW_VOLLEY -> {
                for (LivingEntity e : cone(p, r, 0.55)) hurt(level, p, e, dmg);
                particleCone(level, p, ParticleTypes.CRIT);
            }
            case EXPLOSIVE_SHOT -> {
                Vec3 at = aimPoint(p, r);
                for (LivingEntity e : nearPoint(level, at, 4.0, p)) {
                    hurt(level, p, e, dmg);
                    launchAway(e, at, 1.4);
                }
                level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y, at.z, 8, 1.5, 0.5, 1.5, 0.0);
            }
            case POISON_BARRAGE -> {
                for (LivingEntity e : cone(p, r, 0.5)) {
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
                    hurt(level, p, e, dmg);
                }
                particleCone(level, p, ParticleTypes.SNEEZE);
            }
            case KNIFE_FAN -> {
                for (LivingEntity e : cone(p, r, 0.4)) {
                    hurt(level, p, e, dmg);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
                }
                particleCone(level, p, ParticleTypes.CRIT);
            }
            case STAR_STORM -> {
                for (LivingEntity e : around(p, r)) {
                    hurt(level, p, e, dmg);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                }
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, p.getX(), p.getY() + 1, p.getZ(), 16, r * 0.4, 0.6, r * 0.4, 0.0);
            }
            case JAVELIN_PIERCE -> {
                LivingEntity t = lookTarget(p, r);
                particleTrail(level, p, ParticleTypes.ELECTRIC_SPARK, r);
                if (t == null) return false;
                hurt(level, p, t, dmg);
                launchFrom(t, p, 1.2, 0.3);
                strike(level, t.position());
            }
            case RICOCHET -> {
                for (LivingEntity e : around(p, r)) hurt(level, p, e, dmg);
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, p.getX(), p.getY() + 1, p.getZ(), 30, r * 0.4, 0.6, r * 0.4, 0.0);
            }
            case MAGIC_MISSILE -> {
                LivingEntity t = lookTarget(p, r);
                particleTrail(level, p, ParticleTypes.WITCH, r);
                if (t == null) return false;
                hurt(level, p, t, dmg);
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, t.getX(), t.getY() + 1, t.getZ(), 20, 0.3, 0.4, 0.3, 0.1);
            }
            case ARCANE_ORB -> {
                for (LivingEntity e : around(p, r)) hurt(level, p, e, dmg);
                level.sendParticles(ParticleTypes.WITCH, p.getX(), p.getY() + 1, p.getZ(), 50, r * 0.4, 1, r * 0.4, 0.05);
            }
            case SHADOW_BOLT -> {
                for (LivingEntity e : cone(p, r, 0.7)) {
                    hurt(level, p, e, dmg);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1));
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                }
                particleTrail(level, p, ParticleTypes.SQUID_INK, r);
            }
            case DECAY_FIELD -> {
                for (LivingEntity e : around(p, r)) {
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 2));
                    hurt(level, p, e, dmg);
                }
                level.sendParticles(ParticleTypes.SCULK_SOUL, p.getX(), p.getY() + 1, p.getZ(), 50, r * 0.4, 1, r * 0.4, 0.04);
            }
            case HEAL_BEAM -> {
                float heal = 6f + scaled(p, 8f);
                p.heal(heal);
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, true));
                for (Player ally : alliesInFront(p, r)) {
                    ally.heal(heal);
                    ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, true));
                }
                particleTrail(level, p, ParticleTypes.HEART, r);
            }
            case HOLY_NOVA -> {
                float heal = 6f + scaled(p, 6f);
                p.heal(heal);
                for (Player ally : alliesInFront(p, r + 2)) ally.heal(heal);
                for (LivingEntity e : around(p, r)) {
                    if (e instanceof Player) continue;
                    hurt(level, p, e, dmg);
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                }
                level.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 60, r * 0.4, 1, r * 0.4, 0.05);
            }
        }
        return true;
    }

    /** Multiplies a base value by the caster's Intelligence/Mana scaling (1.0 at 100 max Mana). */
    private static float scaled(ServerPlayer p, float base) {
        double maxMana = StatManager.getMaxMana(p);
        double mult = 1.0 + Math.max(0.0, (maxMana - 100.0) / 300.0);
        return (float) (base * mult);
    }

    private static SoundEvent soundFor(RangedCast c) {
        return switch (c) {
            case FIREBOLT, FLAME_WAVE, METEOR, EXPLOSIVE_SHOT -> SoundEvents.FIRECHARGE_USE;
            case CHAIN_LIGHTNING, STORM_FIELD, JAVELIN_PIERCE -> SoundEvents.WARDEN_SONIC_BOOM;
            case HEAL_BEAM, HOLY_NOVA -> SoundEvents.AMETHYST_BLOCK_CHIME;
            default -> SoundEvents.EXPERIENCE_ORB_PICKUP;
        };
    }

    // ------------------------------------------------------------------
    // Targeting / geometry helpers (mirror PowerManager / ItemActiveAbilityEngine)
    // ------------------------------------------------------------------

    private static void hurt(ServerLevel level, ServerPlayer p, LivingEntity e, float amount) {
        e.hurtServer(level, level.damageSources().playerAttack(p), amount);
    }

    private static LivingEntity lookTarget(ServerPlayer p, double range) {
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

    private static List<LivingEntity> cone(ServerPlayer p, double range, double tightness) {
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

    private static List<LivingEntity> around(ServerPlayer p, double r) {
        return p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(r), x -> x != p && x.isAlive());
    }

    private static List<LivingEntity> nearPoint(ServerLevel level, Vec3 c, double r, LivingEntity exclude) {
        AABB box = new AABB(c.x - r, c.y - r, c.z - r, c.x + r, c.y + r, c.z + r);
        return level.getEntitiesOfClass(LivingEntity.class, box, x -> x != exclude && x.isAlive());
    }

    private static List<Player> alliesInFront(ServerPlayer p, double range) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        List<Player> out = new ArrayList<>();
        AABB box = p.getBoundingBox().expandTowards(view.scale(range)).inflate(range * 0.5 + 1);
        for (Player e : p.level().getEntitiesOfClass(Player.class, box, x -> x != p && x.isAlive())) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double along = to.dot(view);
            if (along <= 0 || along > range) continue;
            if (along / to.length() >= 0.4) out.add(e);
        }
        return out;
    }

    private static Vec3 aimPoint(ServerPlayer p, double d) {
        return p.getEyePosition().add(p.getViewVector(1f).scale(d));
    }

    private static void launchAway(LivingEntity e, Vec3 from, double s) {
        Vec3 d = e.position().subtract(from);
        if (d.lengthSqr() < 1.0e-4) d = new Vec3(0, 1, 0);
        d = d.normalize().scale(s);
        e.push(d.x, 0.35, d.z);
        e.hurtMarked = true;
    }

    private static void launchFrom(LivingEntity e, ServerPlayer p, double s, double lift) {
        Vec3 d = e.position().subtract(p.position());
        if (d.lengthSqr() < 1.0e-4) d = p.getViewVector(1f);
        d = d.normalize().scale(s);
        e.push(d.x, lift, d.z);
        e.hurtMarked = true;
    }

    private static void strike(ServerLevel level, Vec3 at) {
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setPos(at.x, at.y, at.z);
            level.addFreshEntity(bolt);
        }
    }

    /** Particle "projectile" trail straight along the player's view. */
    private static void particleTrail(ServerLevel level, ServerPlayer p, ParticleOptions particle, double range) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        for (int i = 1; i <= range; i++) {
            Vec3 at = eye.add(view.scale(i));
            level.sendParticles(particle, at.x, at.y, at.z, 2, 0.05, 0.05, 0.05, 0.0);
        }
    }

    private static void particleCone(ServerLevel level, ServerPlayer p, ParticleOptions particle) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        for (int i = 1; i <= 8; i++) {
            Vec3 at = eye.add(view.scale(i * 1.2));
            level.sendParticles(particle, at.x, at.y, at.z, 4, 0.3, 0.3, 0.3, 0.02);
        }
    }
}
