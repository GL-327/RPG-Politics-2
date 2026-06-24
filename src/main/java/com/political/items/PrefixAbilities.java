package com.political.items;

import com.political.combat.StatManager;
import com.political.curse.rules.JjkRules;
import com.political.power.Power;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The ability layer for item <b>prefixes</b> (see {@link Variant}). A prefix decorates an item's
 * name and can grant an ability that fires through the existing trigger paths:
 *
 * <ul>
 *   <li><b>Cursed</b> ({@link Variant#CURSED}) — binds a {@link Power} of
 *       {@link Power.Origin#CURSED_TECHNIQUE}. On right-click it channels Cursed Energy and casts a
 *       compact technique effect, routed through {@link JjkRules} (output multiplier, Black Flash,
 *       Reverse Cursed, Simple Domain). Cursed weapons also bite for bonus on-hit damage.</li>
 *   <li><b>Unique</b> ({@link Variant#UNIQUE}) — binds an {@link ItemActiveAbility} (a normal active
 *       ability), cast on right-click by {@link com.political.items.ItemActiveAbilityEngine}.</li>
 * </ul>
 *
 * <p>Plain items (no prefix) carry only stats/rarity and have no ability. This class only ever
 * <em>calls</em> the public APIs of {@link StatManager} and {@link JjkRules}; it never mutates the
 * curse-rules engine or the power roster.
 */
public final class PrefixAbilities {

    private static final Random RNG = new Random();
    /** Per-player, per-power cooldown gate (millis), so cursed gear can't be spammed. */
    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private PrefixAbilities() {}

    // ------------------------------------------------------------------
    // Resolution
    // ------------------------------------------------------------------

    /** The cursed technique granted by a stack's {@code Cursed} prefix, or {@code null}. */
    public static Power cursedPowerOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        if (ItemStats.variantOf(stack) != Variant.CURSED) return null;
        Power p = Power.byId(ItemStats.prefixPowerId(stack));
        return (p != null && p.origin == Power.Origin.CURSED_TECHNIQUE) ? p : null;
    }

    /** The active ability granted by a stack's {@code Unique} prefix, or {@code null}. */
    public static ItemActiveAbility uniqueAbilityOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        if (ItemStats.variantOf(stack) != Variant.UNIQUE) return null;
        return ItemActiveAbility.byId(ItemStats.prefixAbilityId(stack));
    }

    // ------------------------------------------------------------------
    // On-hit (called by AbilityEngine)
    // ------------------------------------------------------------------

    /** Flat bonus damage a Cursed-prefixed weapon adds per swing (the curse bites independently). */
    public static float onHitBonus(ServerPlayer attacker, ItemStack weapon) {
        if (cursedPowerOf(weapon) == null) return 0f;
        int grade = Math.max(1, ItemStats.cursedGradeOf(weapon));
        double ce = StatManager.getMaxCursedEnergy(attacker);
        double bonus = grade * 4.0 + ce * 0.05;
        return (float) (bonus * JjkRules.outputMultiplier(attacker));
    }

    /** A Cursed-prefixed weapon occasionally primes a Black Flash for the wielder's next strike. */
    public static void tryWeaponBlackFlash(ServerPlayer attacker, ItemStack weapon,
                                           LivingEntity target, ServerLevel level) {
        if (cursedPowerOf(weapon) == null) return;
        if (StatManager.getMaxCursedEnergy(attacker) <= 0) return;
        int grade = Math.max(1, ItemStats.cursedGradeOf(weapon));
        if (RNG.nextDouble() < 0.04 * grade) {
            JjkRules.primeBlackFlash(attacker);
        }
    }

    // ------------------------------------------------------------------
    // Right-click cast (called by ItemActiveAbilityEngine)
    // ------------------------------------------------------------------

    /**
     * Attempts a Cursed-prefix cast for the held item. Returns {@code null} when the item is not a
     * Cursed-prefixed item (so the caller falls through to other handlers); otherwise returns a
     * resolved {@link InteractionResult}.
     */
    public static InteractionResult tryCastCursed(ServerPlayer p, ServerLevel level, ItemStack held) {
        Power power = cursedPowerOf(held);
        if (power == null) return null;

        if (!com.political.config.PoliticalConfig.get().powersEnabled) {
            return fail(p, "Cursed techniques are disabled on this server.");
        }
        String key = p.getStringUUID() + "|" + power.id();
        Long ready = COOLDOWNS.get(key);
        long now = System.currentTimeMillis();
        if (ready != null && now < ready) {
            return fail(p, power.displayName + " is recharging (" + ((ready - now) / 1000 + 1) + "s).");
        }
        if (StatManager.getMaxCursedEnergy(p) <= 0) {
            return fail(p, "You have no Cursed Energy to channel this curse.");
        }
        int cost = (int) Math.max(1, Math.round(power.energyCost * JjkRules.ceCostMultiplier(p)));
        if (!StatManager.spendCursedEnergy(p, cost)) {
            return fail(p, "Not enough Cursed Energy (" + cost + " needed).");
        }

        if (!castCursed(p, level, power)) {
            StatManager.addCursedEnergy(p, cost); // refund a strictly-targeted miss
            return fail(p, "No target in sight for " + power.displayName + ".");
        }

        COOLDOWNS.put(key, now + power.cooldownTicks * 50L);
        level.playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 0.7f);
        p.sendSystemMessage(Component.literal("\u2620 " + power.displayName)
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), true);
        return InteractionResult.SUCCESS;
    }

    /**
     * Compact, self-contained cursed-technique effects scaled by {@link JjkRules#outputMultiplier}.
     * Returns {@code false} only when a strictly-targeted technique found nothing (so energy refunds).
     */
    private static boolean castCursed(ServerPlayer p, ServerLevel level, Power power) {
        double out = JjkRules.outputMultiplier(p);
        switch (power) {
            case BLACK_FLASH -> {
                JjkRules.primeBlackFlash(p);
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 120, 0, false, true));
            }
            case REVERSE_CURSED, HEALING -> {
                p.sendSystemMessage(JjkRules.toggleReverseCursed(p), true);
                p.removeEffect(MobEffects.POISON);
                p.removeEffect(MobEffects.WITHER);
            }
            case INFINITY, SIMPLE_DOMAIN -> {
                JjkRules.raiseSimpleDomain(p, 8_000L);
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 160, 3, false, true));
            }
            case SIX_EYES -> StatManager.refillCursedEnergy(p);
            case LIMITLESS_BLUE, GRAVITY_WELL -> {
                for (LivingEntity e : around(p, 8)) {
                    pull(e, p, 1.4);
                    e.hurtServer(level, level.damageSources().playerAttack(p), (float) (6.0 * out));
                }
                level.sendParticles(ParticleTypes.PORTAL, p.getX(), p.getY() + 1, p.getZ(), 80, 2, 1, 2, 0.4);
            }
            case LIMITLESS_RED, SHOCKWAVE, GROUND_POUND -> {
                for (LivingEntity e : around(p, 7)) {
                    launchAway(e, p, 1.8);
                    e.hurtServer(level, level.damageSources().playerAttack(p), (float) (10.0 * out));
                }
                level.sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY() + 1, p.getZ(), 8, 1, 1, 1, 0.0);
            }
            case HOLLOW_PURPLE, WORLD_CUTTING_SLASH, MAXIMUM_METEOR -> {
                for (LivingEntity e : cone(p, 16, 0.5)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), (float) (22.0 * out));
                    launchAway(e, p, 1.0);
                }
                particleLine(level, p, ParticleTypes.REVERSE_PORTAL, 16);
            }
            case DOMAIN_EXPANSION, MALEVOLENT_SHRINE, SELF_EMBODIMENT -> {
                for (LivingEntity e : around(p, 9)) {
                    if (JjkRules.wardsAgainstDomain(e)) continue;
                    e.hurtServer(level, level.damageSources().playerAttack(p), (float) (14.0 * out));
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                }
                level.sendParticles(ParticleTypes.SCULK_SOUL, p.getX(), p.getY() + 1, p.getZ(), 120, 4, 2, 4, 0.05);
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.1f, 0.7f);
            }
            case DISMANTLE, CLEAVE, FALLING_BLOSSOM -> {
                for (LivingEntity e : cone(p, 8, 0.55)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), (float) (12.0 * out));
                }
                particleCone(level, p, ParticleTypes.SWEEP_ATTACK);
            }
            case DISASTER_FLAMES, FIRE_ARROW_FUGA -> {
                for (LivingEntity e : cone(p, 12, 0.45)) {
                    e.setRemainingFireTicks(160);
                    e.hurtServer(level, level.damageSources().playerAttack(p), (float) (10.0 * out));
                }
                particleCone(level, p, ParticleTypes.FLAME);
            }
            case CURSED_SPEECH, CURSED_RESTRAINT -> {
                for (LivingEntity e : cone(p, 12, 0.3)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 6));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 4));
                    e.hurtServer(level, level.damageSources().playerAttack(p), (float) (5.0 * out));
                }
            }
            case NUE_STRIKE, STORMFRONT -> {
                LivingEntity t = lookTarget(p, 24);
                if (t == null) return false;
                strike(level, t.position());
                t.hurtServer(level, level.damageSources().playerAttack(p), (float) (12.0 * out));
            }
            case SOUL_DRAIN, LIFEDRAIN -> {
                boolean hit = false;
                for (LivingEntity e : around(p, 6)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), (float) (6.0 * out));
                    p.heal(2.5f);
                    hit = true;
                }
                if (!hit) return false;
            }
            default -> {
                // Generic cursed blast for any technique without a bespoke effect.
                for (LivingEntity e : cone(p, 10, 0.5)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), (float) (9.0 * out));
                }
                particleCone(level, p, ParticleTypes.SOUL);
            }
        }
        return true;
    }

    private static InteractionResult fail(ServerPlayer p, String msg) {
        p.sendSystemMessage(Component.literal(msg).withStyle(ChatFormatting.RED), true);
        return InteractionResult.FAIL;
    }

    // ------------------------------------------------------------------
    // Targeting helpers (mirror PowerManager / ItemActiveAbilityEngine patterns)
    // ------------------------------------------------------------------

    private static List<LivingEntity> around(ServerPlayer p, double r) {
        return p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(r),
                x -> x != p && x.isAlive());
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

    private static void launchAway(LivingEntity e, ServerPlayer p, double s) {
        Vec3 d = e.position().subtract(p.position()).normalize().scale(s);
        e.push(d.x, 0.35, d.z);
        e.hurtMarked = true;
    }

    private static void pull(LivingEntity e, ServerPlayer p, double s) {
        Vec3 d = p.position().subtract(e.position()).normalize().scale(s);
        e.push(d.x, 0.2, d.z);
        e.hurtMarked = true;
    }

    private static void strike(ServerLevel level, Vec3 at) {
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setPos(at.x, at.y, at.z);
            level.addFreshEntity(bolt);
        }
    }

    private static void particleCone(ServerLevel level, ServerPlayer p, net.minecraft.core.particles.SimpleParticleType pt) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        for (int i = 1; i <= 8; i++) {
            Vec3 at = eye.add(view.scale(i * 1.2));
            level.sendParticles(pt, at.x, at.y, at.z, 4, 0.3, 0.3, 0.3, 0.02);
        }
    }

    private static void particleLine(ServerLevel level, ServerPlayer p, net.minecraft.core.particles.SimpleParticleType pt, int len) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        for (int i = 1; i <= len; i++) {
            Vec3 at = eye.add(view.scale(i));
            level.sendParticles(pt, at.x, at.y, at.z, 6, 0.2, 0.2, 0.2, 0.02);
        }
    }
}
