package com.political.expansion2.ranged;

import com.political.combat.StatManager;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RangedAbilityEngine2 {

    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private RangedAbilityEngine2() {}

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            RangedCast2 cast = RangedItems2.castOf(sp.getMainHandItem());
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
            boolean landed = execute(sp, level, cast);
            if (!landed) {
                if (cast.manaCost > 0) StatManager.addMana(sp, cast.manaCost);
                sp.sendSystemMessage(Component.literal("No target in sight for " + cast.displayName + ".")
                        .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            setCooldown(sp, cast);
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(), soundFor(cast), SoundSource.PLAYERS, 0.9f, 1.0f);
            return InteractionResult.SUCCESS;
        });
    }

    private static boolean onCooldown(ServerPlayer p, RangedCast2 c) {
        Long ready = COOLDOWNS.get(p.getStringUUID() + "|2|" + c.name());
        return ready != null && System.currentTimeMillis() < ready;
    }

    private static long cooldownRemaining(ServerPlayer p, RangedCast2 c) {
        Long ready = COOLDOWNS.get(p.getStringUUID() + "|2|" + c.name());
        return ready == null ? 0 : Math.max(0, (ready - System.currentTimeMillis()) / 1000);
    }

    private static void setCooldown(ServerPlayer p, RangedCast2 c) {
        COOLDOWNS.put(p.getStringUUID() + "|2|" + c.name(),
                System.currentTimeMillis() + c.cooldownSeconds * 1000L);
    }

    private static boolean execute(ServerPlayer p, ServerLevel level, RangedCast2 c) {
        float dmg = scaled(p, c.power);
        CastEffects fx = c.effects;
        int r = c.range;
        float drained = 0f;

        if (fx.selfHealthCost() > 0) {
            if (p.getHealth() <= fx.selfHealthCost() + 1) return false;
            p.hurt(level.damageSources().magic(), fx.selfHealthCost());
        }

        switch (c.pattern) {
            case BEAM -> {
                LivingEntity t = lookTarget(p, r);
                particleTrail(level, p, fx.particle().particle, r);
                if (t == null) return false;
                applyEffects(t, fx);
                hurt(level, p, t, dmg);
                drained = fx.drainPerHit();
            }
            case CONE -> {
                for (LivingEntity e : cone(p, r, fx.coneTightness())) {
                    applyEffects(e, fx);
                    hurt(level, p, e, dmg);
                    if (fx.launchFrom()) launchFrom(e, p, 2.0, 0.5);
                    drained += fx.drainPerHit();
                }
                particleCone(level, p, fx.particle().particle);
            }
            case AROUND -> {
                for (LivingEntity e : around(p, r)) {
                    applyEffects(e, fx);
                    hurt(level, p, e, dmg);
                    drained += fx.drainPerHit();
                }
                burstParticles(level, p, fx.particle().particle, r);
            }
            case AIM, DOMAIN -> {
                Vec3 at = aimPoint(p, r);
                double radius = fx.aimRadius() > 0 ? fx.aimRadius() : 5.0;
                for (LivingEntity e : nearPoint(level, at, radius, p)) {
                    applyEffects(e, fx);
                    hurt(level, p, e, dmg);
                    if (fx.launchAway()) launchAway(e, at, 1.2);
                    drained += fx.drainPerHit();
                }
                domainParticles(level, at, fx, c.pattern == CastPattern.DOMAIN);
            }
            case CHAIN -> {
                LivingEntity t = lookTarget(p, r);
                if (t == null) return false;
                if (fx.lightning()) strike(level, t.position());
                applyEffects(t, fx);
                hurt(level, p, t, dmg);
                drained += fx.drainPerHit();
                for (LivingEntity e : nearPoint(level, t.position(), fx.chainRadius(), p)) {
                    if (e == t) continue;
                    if (fx.lightning()) strike(level, e.position());
                    applyEffects(e, fx);
                    hurt(level, p, e, dmg * fx.chainMult());
                    drained += fx.drainPerHit() * 0.5f;
                }
                particleTrail(level, p, fx.particle().particle, r);
            }
            case PULL -> {
                Vec3 at = aimPoint(p, r);
                double radius = fx.aimRadius() > 0 ? fx.aimRadius() : 5.0;
                for (LivingEntity e : nearPoint(level, at, radius, p)) {
                    pullToward(e, at, 0.8);
                    applyEffects(e, fx);
                    hurt(level, p, e, dmg);
                }
                level.sendParticles(ParticleKind.VOID.particle, at.x, at.y, at.z, 40, radius * 0.3, 0.5, radius * 0.3, 0.02);
            }
            case HEAL -> {
                float heal = fx.healSelf() + scaled(p, fx.healAllies());
                p.heal(heal);
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, true));
                for (Player ally : alliesInFront(p, r)) {
                    ally.heal(heal);
                    ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, true));
                }
                particleTrail(level, p, ParticleKind.HEART.particle, r);
            }
            case HOLY_NOVA -> {
                float heal = fx.healSelf() + scaled(p, fx.healAllies());
                p.heal(heal);
                for (Player ally : alliesInFront(p, r + 2)) ally.heal(heal);
                for (LivingEntity e : around(p, r)) {
                    if (e instanceof Player) continue;
                    applyEffects(e, fx);
                    hurt(level, p, e, dmg);
                }
                burstParticles(level, p, ParticleKind.HOLY.particle, r);
            }
            case SANCTUM -> {
                Vec3 at = aimPoint(p, r);
                double radius = fx.aimRadius() > 0 ? fx.aimRadius() : 5.5;
                for (Player ally : nearPlayers(level, at, radius)) {
                    ally.heal(fx.healAllies() + scaled(p, 4f));
                    ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1, false, true));
                    ally.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 0, false, true));
                }
                for (LivingEntity e : nearPoint(level, at, radius, p)) {
                    if (e instanceof Player) continue;
                    applyEffects(e, fx);
                    hurt(level, p, e, dmg);
                }
                level.sendParticles(ParticleKind.HOLY.particle, at.x, at.y + 0.5, at.z, 50, radius * 0.35, 0.5, radius * 0.35, 0.03);
            }
            case SUMMON -> summonAt(p, level, c, aimPoint(p, r));
        }

        if (fx.healSelf() > 0 && c.pattern != CastPattern.HEAL && c.pattern != CastPattern.HOLY_NOVA) {
            p.heal(Math.min(16f, fx.healSelf() + drained));
        } else if (drained > 0) {
            p.heal(Math.min(18f, drained));
        }
        return true;
    }

    private static void summonAt(ServerPlayer p, ServerLevel level, RangedCast2 c, Vec3 at) {
        CastEffects fx = c.effects;
        int count = Math.max(1, fx.summonCount());
        Monster focus = nearestMonster(p, level, c.range);
        for (int i = 0; i < count; i++) {
            double ox = (i - count / 2.0) * 1.2;
            switch (fx.summon()) {
                case WOLF -> {
                    Mob wolf = EntityTypes.WOLF.create(level, EntitySpawnReason.MOB_SUMMONED);
                    if (wolf != null) {
                        wolf.setPos(at.x + ox, at.y, at.z);
                        wolf.setCustomName(Component.literal("Spirit Wolf").withStyle(ChatFormatting.GREEN));
                        if (focus != null) wolf.setTarget(focus);
                        level.addFreshEntity(wolf);
                    }
                }
                case GOLEM -> {
                    Mob golem = EntityTypes.IRON_GOLEM.create(level, EntitySpawnReason.MOB_SUMMONED);
                    if (golem != null) {
                        golem.setPos(at.x, at.y, at.z);
                        golem.setCustomName(Component.literal("Arc Golem").withStyle(ChatFormatting.GRAY));
                        level.addFreshEntity(golem);
                    }
                }
                case VEX -> {
                    Vex vex = EntityTypes.VEX.create(level, EntitySpawnReason.MOB_SUMMONED);
                    if (vex != null) {
                        vex.setPos(at.x + ox, at.y + 1, at.z);
                        level.addFreshEntity(vex);
                    }
                }
                default -> { }
            }
        }
        level.sendParticles(ParticleKind.SOUL.particle, at.x, at.y + 1, at.z, 30, 1.5, 0.5, 1.5, 0.02);
    }

    private static Monster nearestMonster(ServerPlayer p, ServerLevel level, double range) {
        Monster best = null;
        double bestD = range * range;
        for (Monster m : level.getEntitiesOfClass(Monster.class, p.getBoundingBox().inflate(range), Monster::isAlive)) {
            double d = p.distanceToSqr(m);
            if (d < bestD) { bestD = d; best = m; }
        }
        return best;
    }

    private static void applyEffects(LivingEntity e, CastEffects fx) {
        if (fx.fireTicks() > 0) e.setRemainingFireTicks(fx.fireTicks());
        if (fx.slowTicks() > 0) e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, fx.slowTicks(), fx.slowAmp()));
        if (fx.poisonTicks() > 0) e.addEffect(new MobEffectInstance(MobEffects.POISON, fx.poisonTicks(), fx.poisonAmp()));
        if (fx.witherTicks() > 0) e.addEffect(new MobEffectInstance(MobEffects.WITHER, fx.witherTicks(), fx.witherAmp()));
        if (fx.weaknessTicks() > 0) e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, fx.weaknessTicks(), fx.weaknessAmp()));
        if (fx.blindnessTicks() > 0) e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, fx.blindnessTicks(), 0));
    }

    private static float scaled(ServerPlayer p, float base) {
        double maxMana = StatManager.getMaxMana(p);
        double mult = 1.0 + Math.max(0.0, (maxMana - 100.0) / 300.0);
        return (float) (base * mult);
    }

    private static SoundEvent soundFor(RangedCast2 c) {
        return switch (c.pattern) {
            case BEAM, AIM, CHAIN -> c.effects.lightning() ? SoundEvents.WARDEN_SONIC_BOOM : SoundEvents.EXPERIENCE_ORB_PICKUP;
            case HEAL, HOLY_NOVA, SANCTUM -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case SUMMON -> SoundEvents.EVOKER_PREPARE_SUMMON;
            default -> c.effects.fireTicks() > 0 ? SoundEvents.FIRECHARGE_USE : SoundEvents.EXPERIENCE_ORB_PICKUP;
        };
    }

    private static void hurt(ServerLevel level, ServerPlayer p, LivingEntity e, float amount) {
        if (amount <= 0) return;
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

    private static List<Player> nearPlayers(ServerLevel level, Vec3 c, double r) {
        AABB box = new AABB(c.x - r, c.y - r, c.z - r, c.x + r, c.y + r, c.z + r);
        return level.getEntitiesOfClass(Player.class, box, Player::isAlive);
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

    private static void pullToward(LivingEntity e, Vec3 at, double s) {
        Vec3 d = at.subtract(e.position()).normalize().scale(s);
        e.push(d.x, 0.15, d.z);
        e.hurtMarked = true;
    }

    private static void strike(ServerLevel level, Vec3 at) {
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setPos(at.x, at.y, at.z);
            level.addFreshEntity(bolt);
        }
    }

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

    private static void burstParticles(ServerLevel level, ServerPlayer p, ParticleOptions particle, double r) {
        level.sendParticles(particle, p.getX(), p.getY() + 1, p.getZ(), 40, r * 0.4, 0.8, r * 0.4, 0.04);
    }

    private static void domainParticles(ServerLevel level, Vec3 at, CastEffects fx, boolean lingering) {
        ParticleOptions p = fx.particle().particle;
        int count = lingering ? 70 : 35;
        level.sendParticles(p, at.x, at.y + 0.5, at.z, count, fx.aimRadius() * 0.35, 0.6, fx.aimRadius() * 0.35, 0.03);
        if (fx.fireTicks() > 0) level.sendParticles(ParticleKind.FLAME.particle, at.x, at.y, at.z, 20, 1, 0.3, 1, 0.02);
    }
}
