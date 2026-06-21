package com.political.expansion.melee;

import com.political.combat.StatManager;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Self-contained RIGHT CLICK ability handler for the melee weapon expansion. Mirrors the
 * behaviour of {@code com.political.items.ItemActiveAbilityEngine} but resolves and casts only
 * this expansion's {@link MeleeAbility}s, so it never touches the shared enums/engine.
 *
 * <p>Registered automatically by {@link MeleeWeapons#register()}. It is harmless to run alongside
 * the shared engine: for any non-melee-expansion item it simply returns {@link InteractionResult#PASS}.
 */
public final class MeleeAbilityEngine {

    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private MeleeAbilityEngine() {}

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            MeleeWeapon weapon = MeleeWeapons.byStack(sp.getMainHandItem());
            if (weapon == null) return InteractionResult.PASS;
            MeleeAbility ability = weapon.ability;

            if (onCooldown(sp, ability)) {
                sp.sendSystemMessage(Component.literal(ability.displayName + " is on cooldown.")
                        .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            if (ability.manaCost > 0 && !StatManager.spendMana(sp, ability.manaCost)) {
                sp.sendSystemMessage(Component.literal("Not enough Mana.").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            if (!cast(sp, (ServerLevel) world, ability)) return InteractionResult.PASS;
            setCooldown(sp, ability);
            return InteractionResult.SUCCESS;
        });
    }

    private static boolean onCooldown(ServerPlayer p, MeleeAbility a) {
        Long ready = COOLDOWNS.get(p.getStringUUID() + "|" + a.name());
        return ready != null && System.currentTimeMillis() < ready;
    }

    private static void setCooldown(ServerPlayer p, MeleeAbility a) {
        COOLDOWNS.put(p.getStringUUID() + "|" + a.name(),
                System.currentTimeMillis() + a.cooldownSeconds * 1000L);
    }

    private static boolean cast(ServerPlayer p, ServerLevel level, MeleeAbility a) {
        switch (a) {
            // ---------------- Common ----------------
            case CLEAVING_SLASH -> {
                for (LivingEntity e : cone(p, 6, 0.5)) hurt(level, p, e, 8f);
                particleCone(level, p, ParticleTypes.SWEEP_ATTACK);
            }
            case QUICK_JAB -> {
                blink(p, 3);
                LivingEntity t = lookTarget(p, 5);
                if (t != null) hurt(level, p, t, 7f);
            }
            case CRUSHING_BLOW -> {
                LivingEntity t = lookTarget(p, 5);
                if (t == null) return false;
                hurt(level, p, t, 10f);
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
                level.sendParticles(ParticleTypes.CRIT, t.getX(), t.getY() + 1, t.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
            }
            case TIMBER_HACK -> {
                for (LivingEntity e : cone(p, 5, 0.6)) hurt(level, p, e, 9f);
                particleCone(level, p, ParticleTypes.CRIT);
            }
            case BRACED_LUNGE -> {
                blink(p, 2);
                LivingEntity t = lookTarget(p, 5);
                if (t == null) return false;
                hurt(level, p, t, 8f);
                launchAway(t, p, 1.3);
            }

            // ---------------- Uncommon ----------------
            case RIPOSTE -> {
                for (LivingEntity e : cone(p, 5, 0.5)) hurt(level, p, e, 11f);
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 120, 1, false, true));
                particleCone(level, p, ParticleTypes.SWEEP_ATTACK);
            }
            case BLEEDING_STAB -> {
                LivingEntity t = lookTarget(p, 6);
                if (t == null) return false;
                hurt(level, p, t, 9f);
                t.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 2));
            }
            case REND -> {
                for (LivingEntity e : cone(p, 6, 0.45)) {
                    hurt(level, p, e, 10f);
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 1));
                }
                particleCone(level, p, ParticleTypes.CRIT);
            }
            case SKEWER -> {
                LivingEntity t = lookTarget(p, 9);
                if (t == null) return false;
                hurt(level, p, t, 12f);
                Vec3 to = t.position().subtract(p.position()).normalize().scale(0.8);
                p.push(to.x, 0.1, to.z);
                p.hurtMarked = true;
            }
            case CONCUSSION -> {
                for (LivingEntity e : around(p, 5)) {
                    hurt(level, p, e, 8f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 3));
                }
                level.sendParticles(ParticleTypes.CRIT, p.getX(), p.getY() + 1, p.getZ(), 30, 2, 0.5, 2, 0.05);
            }

            // ---------------- Rare ----------------
            case VALIANT_SWEEP -> {
                for (LivingEntity e : cone(p, 7, 0.4)) {
                    hurt(level, p, e, 13f);
                    launchAway(e, p, 1.1);
                }
                particleCone(level, p, ParticleTypes.SWEEP_ATTACK);
            }
            case SHADOW_FLICKER -> {
                LivingEntity t = lookTarget(p, 18);
                if (t == null) return false;
                Vec3 behind = t.position().subtract(t.getViewVector(1f).scale(1.5));
                p.teleportTo(behind.x, behind.y, behind.z);
                hurt(level, p, t, 18f);
                level.sendParticles(ParticleTypes.SMOKE, p.getX(), p.getY() + 1, p.getZ(), 30, 0.3, 0.5, 0.3, 0.02);
            }
            case STATIC_REACH -> {
                for (LivingEntity e : cone(p, 12, 0.65)) {
                    hurt(level, p, e, 11f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
                }
                particleCone(level, p, ParticleTypes.ELECTRIC_SPARK);
            }
            case IAIDO_DRAW -> {
                for (LivingEntity e : cone(p, 8, 0.55)) {
                    hurt(level, p, e, 12f);
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
                }
                particleCone(level, p, ParticleTypes.SWEEP_ATTACK);
            }
            case REAPING_ARC -> {
                for (LivingEntity e : cone(p, 7, 0.45)) {
                    hurt(level, p, e, 12f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                }
                particleCone(level, p, ParticleTypes.SOUL);
            }

            // ---------------- Epic ----------------
            case EARTHSHATTER -> {
                for (LivingEntity e : around(p, 6)) {
                    hurt(level, p, e, 16f);
                    launchAway(e, p, 1.4);
                }
                level.sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY(), p.getZ(), 6, 1, 0.2, 1, 0.0);
            }
            case RENDING_FRENZY -> {
                for (LivingEntity e : cone(p, 6, 0.5)) {
                    hurt(level, p, e, 9f);
                    hurt(level, p, e, 9f);
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 2));
                }
                particleCone(level, p, ParticleTypes.CRIT);
            }
            case PERMAFROST_CHOP -> {
                for (LivingEntity e : around(p, 7)) {
                    hurt(level, p, e, 14f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 3));
                }
                level.sendParticles(ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 1, p.getZ(), 60, 3, 1, 3, 0.05);
            }
            case MAGMA_CLEAVE -> {
                for (LivingEntity e : cone(p, 8, 0.45)) {
                    hurt(level, p, e, 14f);
                    e.setRemainingFireTicks(120);
                }
                particleCone(level, p, ParticleTypes.FLAME);
            }
            case LASHING_COIL -> {
                for (LivingEntity e : cone(p, 12, 0.6)) {
                    hurt(level, p, e, 11f);
                    Vec3 d = p.position().subtract(e.position()).normalize().scale(1.2);
                    e.push(d.x, 0.2, d.z);
                    e.hurtMarked = true;
                }
                particleCone(level, p, ParticleTypes.CRIT);
            }
            case LEVIN_THROW -> {
                LivingEntity t = lookTarget(p, 18);
                if (t == null) return false;
                hurt(level, p, t, 16f);
                strike(level, t.position());
            }

            // ---------------- Legendary ----------------
            case COMET_SMASH -> {
                for (LivingEntity e : around(p, 8)) {
                    hurt(level, p, e, 24f);
                    launchAway(e, p, 1.2);
                }
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, p.getX(), p.getY(), p.getZ(), 2, 0.5, 0.2, 0.5, 0.0);
                level.sendParticles(ParticleTypes.FIREWORK, p.getX(), p.getY(), p.getZ(), 30, 2, 1, 2, 0.15);
            }
            case CRESCENT_ECLIPSE -> {
                for (LivingEntity e : cone(p, 9, 0.4)) {
                    hurt(level, p, e, 22f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
                }
                particleCone(level, p, ParticleTypes.SWEEP_ATTACK);
                level.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 30, 2, 1, 2, 0.05);
            }
            case SOUL_HARVEST -> {
                float healed = 0f;
                for (LivingEntity e : around(p, 8)) {
                    hurt(level, p, e, 18f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1));
                    healed += 2f;
                }
                if (healed > 0) p.heal(Math.min(healed, 12f));
                level.sendParticles(ParticleTypes.SOUL, p.getX(), p.getY() + 1, p.getZ(), 50, 3, 1, 3, 0.04);
            }
            case SWEEPING_VORTEX -> {
                for (LivingEntity e : cone(p, 9, 0.4)) {
                    hurt(level, p, e, 20f);
                    Vec3 d = p.position().subtract(e.position()).normalize().scale(0.9);
                    e.push(d.x, 0.2, d.z);
                    e.hurtMarked = true;
                }
                particleCone(level, p, ParticleTypes.SWEEP_ATTACK);
            }
            case SEISMIC_SLAM -> {
                for (LivingEntity e : around(p, 8)) {
                    hurt(level, p, e, 26f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3));
                    launchAway(e, p, 0.8);
                }
                level.sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY(), p.getZ(), 8, 2, 0.2, 2, 0.0);
            }
            case THOUSAND_CUTS -> {
                List<LivingEntity> targets = around(p, 10);
                for (LivingEntity e : targets) {
                    Vec3 behind = e.position().subtract(e.getViewVector(1f).scale(1.2));
                    p.teleportTo(behind.x, behind.y, behind.z);
                    hurt(level, p, e, 16f);
                    level.sendParticles(ParticleTypes.CRIT, e.getX(), e.getY() + 1, e.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
                }
                if (targets.isEmpty()) return false;
            }

            // ---------------- Mythic ----------------
            case DIVINE_EXECUTION -> {
                LivingEntity t = lookTarget(p, 10);
                if (t == null) return false;
                float dmg = t.getHealth() < t.getMaxHealth() * 0.4f ? 60f : 30f;
                hurt(level, p, t, dmg);
                for (LivingEntity e : around(p, 6)) {
                    hurt(level, p, e, 14f);
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                }
                level.sendParticles(ParticleTypes.END_ROD, t.getX(), t.getY() + 1, t.getZ(), 60, 1, 1, 1, 0.1);
            }
            case OBLIVION_RIFT -> {
                for (LivingEntity e : around(p, 9)) {
                    hurt(level, p, e, 30f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 140, 2));
                    Vec3 d = p.position().subtract(e.position()).normalize().scale(1.0);
                    e.push(d.x, 0.1, d.z);
                    e.hurtMarked = true;
                }
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 1, p.getZ(), 120, 3, 1.5, 3, 0.15);
            }
            case CATACLYSM -> {
                for (LivingEntity e : around(p, 9)) {
                    hurt(level, p, e, 32f);
                    e.setRemainingFireTicks(160);
                    launchAway(e, p, 1.6);
                }
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, p.getX(), p.getY(), p.getZ(), 3, 1, 0.3, 1, 0.0);
                level.sendParticles(ParticleTypes.LAVA, p.getX(), p.getY() + 1, p.getZ(), 40, 3, 1, 3, 0.1);
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1f, 0.8f);
            }
            case REAP_ETERNAL -> {
                float healed = 0f;
                for (LivingEntity e : around(p, 9)) {
                    hurt(level, p, e, 30f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 2));
                    healed += 3f;
                }
                if (healed > 0) p.heal(Math.min(healed, 20f));
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.getX(), p.getY() + 1, p.getZ(), 70, 3, 1.5, 3, 0.04);
            }
        }
        level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1f, 1.2f);
        return true;
    }

    // --- helpers (mirror ItemActiveAbilityEngine / PowerManager) ---

    private static void hurt(ServerLevel level, ServerPlayer p, LivingEntity e, float dmg) {
        e.hurtServer(level, level.damageSources().playerAttack(p), dmg);
    }

    private static LivingEntity lookTarget(ServerPlayer p, double range) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        LivingEntity best = null;
        double bestAlong = range;
        AABB box = p.getBoundingBox().expandTowards(view.scale(range)).inflate(2);
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

    private static void blink(ServerPlayer p, double dist) {
        Vec3 v = p.getViewVector(1f).scale(dist);
        p.teleportTo(p.getX() + v.x, p.getY() + Math.max(0, v.y), p.getZ() + v.z);
        p.fallDistance = 0;
    }

    private static void launchAway(LivingEntity e, ServerPlayer p, double s) {
        Vec3 d = e.position().subtract(p.position()).normalize().scale(s);
        e.push(d.x, 0.3, d.z);
        e.hurtMarked = true;
    }

    private static void strike(ServerLevel level, Vec3 at) {
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setPos(at.x, at.y, at.z);
            level.addFreshEntity(bolt);
        }
    }

    private static void particleCone(ServerLevel level, ServerPlayer p, SimpleParticleType pt) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        for (int i = 1; i <= 8; i++) {
            Vec3 at = eye.add(view.scale(i * 1.2));
            level.sendParticles(pt, at.x, at.y, at.z, 4, 0.3, 0.3, 0.3, 0.02);
        }
    }
}
