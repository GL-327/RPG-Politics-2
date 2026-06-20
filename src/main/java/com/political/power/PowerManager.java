package com.political.power;

import com.political.combat.StatManager;
import com.political.politics.DataManager;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Runs power activations (Compound&nbsp;V + cursed techniques) using Fabric events and
 * vanilla mechanics only. Tracks per-player cooldowns, timed flight, and the empowered
 * "Black Flash" next-hit buff.
 */
public final class PowerManager {

    private static final Map<String, Long> COOLDOWNS = new HashMap<>(); // "uuid|POWER" -> ready-at ms
    private static final Map<UUID, Long> FLIGHT_UNTIL = new HashMap<>();
    private static final Map<UUID, Long> BLACK_FLASH_UNTIL = new HashMap<>();
    private static int tickCounter = 0;

    private PowerManager() {}

    public static void register() {
        // Black Flash: the next melee hit after activation deals bonus distorted damage.
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (level.isClientSide()) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            if (!(entity instanceof LivingEntity target)) return InteractionResult.PASS;
            Long until = BLACK_FLASH_UNTIL.get(sp.getUUID());
            if (until != null && System.currentTimeMillis() < until) {
                BLACK_FLASH_UNTIL.remove(sp.getUUID());
                ServerLevel sl = (ServerLevel) level;
                target.hurtServer(sl, sl.damageSources().playerAttack(sp), 12.0f);
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 40, 0.4, 0.6, 0.4, 0.4);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2f, 0.7f);
            }
            return InteractionResult.PASS;
        });
    }

    // ---------------- Cooldowns ----------------

    private static boolean onCooldown(ServerPlayer p, Power power) {
        Long ready = COOLDOWNS.get(p.getStringUUID() + "|" + power.name());
        return ready != null && System.currentTimeMillis() < ready;
    }

    private static long cooldownRemaining(ServerPlayer p, Power power) {
        Long ready = COOLDOWNS.get(p.getStringUUID() + "|" + power.name());
        return ready == null ? 0 : Math.max(0, (ready - System.currentTimeMillis()) / 1000);
    }

    private static void setCooldown(ServerPlayer p, Power power) {
        COOLDOWNS.put(p.getStringUUID() + "|" + power.name(),
                System.currentTimeMillis() + power.cooldownTicks * 50L);
    }

    // ---------------- Activation ----------------

    /** Activates the player's currently selected power. Returns a status component. */
    public static Component activateSelected(ServerPlayer player) {
        String sel = DataManager.selectedPower(player.getStringUUID());
        Power power = Power.byId(sel);
        if (power == null) return err("You have no power selected. Use /power select <id>.");
        if (!DataManager.hasPower(player.getStringUUID(), power.id())) return err("You have not awakened that power.");
        if (onCooldown(player, power)) return err(power.displayName + " is recharging (" + cooldownRemaining(player, power) + "s).");

        int cost = (int) Math.round(power.energyCost * DataManager.data().powerCostMultiplier);
        if (power.origin == Power.Origin.CURSED_TECHNIQUE) {
            if (StatManager.getMaxCursedEnergy(player) <= 0)
                return err("You have no cursed energy to channel.");
            if (!StatManager.spendCursedEnergy(player, cost))
                return err("Not enough Cursed Energy (" + cost + " needed).");
        } else {
            if (!StatManager.spendMana(player, cost))
                return err("Not enough Mana (" + cost + " needed).");
        }

        ServerLevel level = player.level();
        cast(player, level, power);
        setCooldown(player, power);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.2f);
        return Component.literal("Used " + power.displayName + ".").withStyle(power.origin.color);
    }

    private static void cast(ServerPlayer p, ServerLevel level, Power power) {
        switch (power) {
            case LASER_EYES -> beam(p, level, 24, 8.0f, true, false);
            case CHEST_BLAST -> beam(p, level, 18, 14.0f, false, true);
            case PYROKINESIS -> {
                for (LivingEntity e : cone(p, 10, 0.6)) {
                    e.setRemainingFireTicks(120);
                    e.hurtServer(level, level.damageSources().playerAttack(p), 6.0f);
                }
                particleCone(level, p, ParticleTypes.FLAME);
            }
            case CRYOKINESIS -> {
                for (LivingEntity e : around(p, 8)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 3));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                    e.hurtServer(level, level.damageSources().playerAttack(p), 4.0f);
                }
                level.sendParticles(ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 1, p.getZ(), 80, 4, 2, 4, 0.1);
            }
            case SONIC_SCREAM -> {
                for (LivingEntity e : cone(p, 12, 0.4)) {
                    launchEntity(e, p, 1.6, 0.5);
                    e.hurtServer(level, level.damageSources().playerAttack(p), 5.0f);
                }
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.5f, 1.0f);
            }
            case SUPER_LEAP -> launchSelf(p, p.getViewVector(1f).scale(0.8).add(0, 1.3, 0));
            case SUPER_STRENGTH -> {
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 1, false, true));
            }
            case SPEEDSTER -> {
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 3, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.HASTE, 200, 2, false, true));
            }
            case FLIGHT -> grantFlight(p, 30_000L);
            case STAR_POWER -> {
                grantFlight(p, 20_000L);
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 400, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 400, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0, false, true));
            }
            case INVISIBILITY -> p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 300, 0, false, false));
            case TELEKINESIS -> {
                LivingEntity t = lookTarget(p, 20);
                if (t != null) launchEntity(t, p, -2.0, 0.6); // negative = away from player
            }
            case TELEPORT -> blink(p, 14);
            case LIFEDRAIN -> {
                for (LivingEntity e : around(p, 6)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), 5.0f);
                    p.heal(2.5f);
                }
            }
            case HEALING, REVERSE_CURSED -> {
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2, false, true));
                p.removeEffect(MobEffects.POISON);
                p.removeEffect(MobEffects.WITHER);
            }
            case STORMFRONT -> {
                LivingEntity t = lookTarget(p, 30);
                Vec3 at = t != null ? t.position() : aimPoint(p, 20);
                strike(level, at);
            }
            case FORCEFIELD -> {
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 300, 3, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 3, false, true));
            }
            case PETRIFYING_GAZE -> {
                LivingEntity t = lookTarget(p, 20);
                if (t != null) {
                    t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 6));
                    t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 4));
                    t.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 160, 4));
                }
            }
            case MIND_CONTROL -> {
                LivingEntity t = lookTarget(p, 16);
                if (t instanceof Mob mob) {
                    Monster victim = nearestMonster(p, mob, 24);
                    if (victim != null) mob.setTarget(victim);
                    mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                }
            }
            case HEAD_POP -> {
                LivingEntity t = lookTarget(p, 18);
                if (t != null) {
                    t.hurtServer(level, level.damageSources().playerAttack(p), 35.0f);
                    level.sendParticles(ParticleTypes.CRIMSON_SPORE, t.getX(), t.getEyeY(), t.getZ(), 60, 0.3, 0.3, 0.3, 0.2);
                }
            }
            case LIMITLESS_BLUE -> {
                for (LivingEntity e : around(p, 14)) launchEntity(e, p, 2.0, 0.2); // pull toward
            }
            case LIMITLESS_RED -> {
                for (LivingEntity e : around(p, 12)) {
                    launchEntity(e, p, -3.0, 0.7);
                    e.hurtServer(level, level.damageSources().playerAttack(p), 8.0f);
                }
                level.sendParticles(ParticleTypes.LARGE_SMOKE, p.getX(), p.getY() + 1, p.getZ(), 60, 3, 2, 3, 0.1);
            }
            case HOLLOW_PURPLE -> beam(p, level, 40, 40.0f, true, true);
            case DISMANTLE -> {
                for (LivingEntity e : cone(p, 14, 0.5)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), 9.0f);
                }
                particleCone(level, p, ParticleTypes.SWEEP_ATTACK);
            }
            case CLEAVE -> {
                LivingEntity t = lookTarget(p, 8);
                if (t != null) t.hurtServer(level, level.damageSources().playerAttack(p), 20.0f);
            }
            case DIVERGENT_FIST -> {
                LivingEntity t = lookTarget(p, 6);
                if (t != null) {
                    t.hurtServer(level, level.damageSources().playerAttack(p), 8.0f);
                    launchEntity(t, p, -1.6, 0.5);
                }
            }
            case BLACK_FLASH -> BLACK_FLASH_UNTIL.put(p.getUUID(), System.currentTimeMillis() + 5000L);
            case TEN_SHADOWS -> summonShadows(p, level, 3);
            case CURSED_SPEECH -> {
                for (LivingEntity e : cone(p, 12, 0.4)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 4));
                    e.hurtServer(level, level.damageSources().playerAttack(p), 6.0f);
                }
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.4f, 1.0f);
            }
            case DOMAIN_EXPANSION -> {
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 1, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 2, false, true));
                for (LivingEntity e : around(p, 12)) {
                    if (e == p) continue;
                    e.hurtServer(level, level.damageSources().magic(), 18.0f);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 2));
                }
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 1, p.getZ(), 200, 12, 6, 12, 0.2);
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.0f, 0.6f);
            }
            case INFINITY -> {
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 160, 4, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 3, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 160, 0, false, true));
                for (LivingEntity e : around(p, 4)) launchEntity(e, p, -2.0, 0.4);
                level.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 60, 1.2, 1.2, 1.2, 0.02);
            }
            case SIX_EYES -> {
                StatManager.addCursedEnergy(p, StatManager.getMaxCursedEnergy(p));
                p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0, false, false));
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 1, false, true));
                level.sendParticles(ParticleTypes.GLOW, p.getX(), p.getEyeY(), p.getZ(), 40, 0.4, 0.4, 0.4, 0.05);
            }
            case SIMPLE_DOMAIN -> {
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1, false, true));
                for (LivingEntity e : around(p, 5)) {
                    launchEntity(e, p, -1.5, 0.4);
                    e.hurtServer(level, level.damageSources().magic(), 4.0f);
                }
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, p.getX(), p.getY() + 1, p.getZ(), 60, 3, 1, 3, 0.0);
            }
            case WORLD_CUTTING_SLASH -> {
                for (LivingEntity e : cone(p, 16, 0.35)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), 16.0f);
                }
                particleCone(level, p, ParticleTypes.SWEEP_ATTACK);
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.4f, 0.6f);
            }
            case SHOCKWAVE -> {
                for (LivingEntity e : around(p, 8)) {
                    if (e == p) continue;
                    launchEntity(e, p, -2.4, 0.6);
                    e.hurtServer(level, level.damageSources().playerAttack(p), 5.0f);
                }
                level.sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY(), p.getZ(), 8, 2, 0.2, 2, 0.0);
            }
            case IRON_SKIN -> {
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 240, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 240, 2, false, true));
            }
            case SOUL_DRAIN -> {
                float drained = 0f;
                for (LivingEntity e : cone(p, 10, 0.45)) {
                    e.hurtServer(level, level.damageSources().magic(), 6.0f);
                    drained += 2f;
                }
                p.heal(Math.min(12f, drained));
                if (drained > 0) StatManager.addCursedEnergy(p, Math.min(15, drained));
                level.sendParticles(ParticleTypes.SCULK_SOUL, p.getX(), p.getEyeY(), p.getZ(), 30, 1.5, 0.6, 1.5, 0.02);
            }
            case SHADOW_STEP -> {
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 3, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));
                p.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 120, 2, false, true));
                level.sendParticles(ParticleTypes.SQUID_INK, p.getX(), p.getY() + 1, p.getZ(), 40, 0.4, 0.8, 0.4, 0.02);
            }
            case CURSED_RESTRAINT -> {
                for (LivingEntity e : around(p, 8)) {
                    if (e == p) continue;
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 5));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 3));
                    e.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 160, 3));
                }
                level.sendParticles(ParticleTypes.WITCH, p.getX(), p.getY() + 1, p.getZ(), 40, 3, 1, 3, 0.0);
            }
            case ADAPTIVE_BIOLOGY -> {
                p.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 2400, 1, false, true));
                StatManager.addCursedEnergy(p, 20);
            }
            case REGENERATIVE_CODE -> {
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 3, false, true));
                StatManager.addCursedEnergy(p, 15);
            }
        }
    }

    // ---------------- Effect helpers ----------------

    private static void beam(ServerPlayer p, ServerLevel level, double range, float damage, boolean fire, boolean explosive) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        for (LivingEntity e : cone(p, range, 0.75)) {
            e.hurtServer(level, level.damageSources().playerAttack(p), damage);
            if (fire) e.setRemainingFireTicks(80);
        }
        for (int i = 1; i <= range; i++) {
            Vec3 at = eye.add(view.scale(i));
            level.sendParticles(explosive ? ParticleTypes.SONIC_BOOM : ParticleTypes.END_ROD,
                    at.x, at.y, at.z, explosive ? 1 : 3, 0.05, 0.05, 0.05, 0.0);
        }
        level.playSound(null, p.getX(), p.getY(), p.getZ(),
                explosive ? SoundEvents.GENERIC_EXPLODE.value() : SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 1.2f, 1.0f);
    }

    private static void strike(ServerLevel level, Vec3 at) {
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setPos(at.x, at.y, at.z);
            level.addFreshEntity(bolt);
        }
    }

    private static void summonShadows(ServerPlayer p, ServerLevel level, int count) {
        Monster focus = nearestMonster(p, null, 24);
        for (int i = 0; i < count; i++) {
            Mob wolf = EntityTypes.WOLF.create(level, EntitySpawnReason.TRIGGERED);
            if (wolf == null) continue;
            wolf.setPos(p.getX() + (i - 1), p.getY(), p.getZ());
            wolf.setCustomName(Component.literal("Shadow").withStyle(ChatFormatting.DARK_PURPLE));
            if (focus != null) wolf.setTarget(focus);
            level.addFreshEntity(wolf);
        }
    }

    /** Entities in a forward cone: {@code tightness} closer to 1 = narrower. */
    private static List<LivingEntity> cone(ServerPlayer p, double range, double tightness) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        List<LivingEntity> out = new ArrayList<>();
        AABB box = p.getBoundingBox().expandTowards(view.scale(range)).inflate(range * 0.5 + 1);
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, box, x -> x != p && x.isAlive())) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double along = to.dot(view);
            if (along <= 0 || along > range) continue;
            double cos = along / to.length();
            if (cos >= tightness) out.add(e);
        }
        return out;
    }

    private static List<LivingEntity> around(ServerPlayer p, double radius) {
        return p.level().getEntitiesOfClass(LivingEntity.class,
                p.getBoundingBox().inflate(radius), x -> x != p && x.isAlive());
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
            Vec3 perp = to.subtract(view.scale(along));
            if (perp.lengthSqr() <= 2.5 && along < bestAlong) {
                best = e;
                bestAlong = along;
            }
        }
        return best;
    }

    private static Monster nearestMonster(ServerPlayer p, LivingEntity exclude, double radius) {
        Monster best = null;
        double bestD = radius * radius;
        for (Monster m : p.level().getEntitiesOfClass(Monster.class, p.getBoundingBox().inflate(radius))) {
            if (m == exclude || !m.isAlive()) continue;
            double d = m.distanceToSqr(p);
            if (d < bestD) {
                best = m;
                bestD = d;
            }
        }
        return best;
    }

    private static Vec3 aimPoint(ServerPlayer p, double dist) {
        return p.getEyePosition().add(p.getViewVector(1f).scale(dist));
    }

    /** Push an entity; positive strength = toward the player (pull), negative = away. */
    private static void launchEntity(LivingEntity e, ServerPlayer p, double strength, double lift) {
        Vec3 dir = p.position().subtract(e.position());
        if (dir.lengthSqr() < 1.0e-4) dir = p.getViewVector(1f).reverse();
        dir = dir.normalize().scale(strength);
        e.push(dir.x, lift, dir.z);
        e.hurtMarked = true;
    }

    private static void launchSelf(ServerPlayer p, Vec3 velocity) {
        p.setDeltaMovement(velocity);
        p.connection.send(new ClientboundSetEntityMotionPacket(p));
    }

    private static void blink(ServerPlayer p, double dist) {
        Vec3 view = p.getViewVector(1f);
        double tx = p.getX() + view.x * dist;
        double ty = p.getY() + Math.max(0, view.y * dist);
        double tz = p.getZ() + view.z * dist;
        p.teleportTo(tx, ty, tz);
        p.fallDistance = 0;
        ((ServerLevel) p.level()).sendParticles(ParticleTypes.PORTAL, tx, ty + 1, tz, 40, 0.5, 1, 0.5, 0.3);
    }

    private static void particleCone(ServerLevel level, ServerPlayer p, net.minecraft.core.particles.SimpleParticleType particle) {
        Vec3 eye = p.getEyePosition();
        Vec3 view = p.getViewVector(1f);
        for (int i = 1; i <= 8; i++) {
            Vec3 at = eye.add(view.scale(i * 1.2));
            level.sendParticles(particle, at.x, at.y, at.z, 4, 0.3, 0.3, 0.3, 0.02);
        }
    }

    private static void grantFlight(ServerPlayer p, long durationMs) {
        if (p.isCreative() || p.isSpectator()) return;
        FLIGHT_UNTIL.put(p.getUUID(), System.currentTimeMillis() + durationMs);
        if (!p.getAbilities().mayfly) {
            p.getAbilities().mayfly = true;
            p.onUpdateAbilities();
        }
    }

    // ---------------- Tick & cleanup ----------------

    public static void tick(MinecraftServer server) {
        if (++tickCounter % 20 != 0) return;
        long now = System.currentTimeMillis();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            // Expire temp powers (Temp V).
            Long exp = DataManager.data().tempPowerExpiry.get(p.getStringUUID());
            if (exp != null && exp != 0 && now >= exp) {
                DataManager.revokeAllPowers(p.getStringUUID());
                DataManager.data().tempPowerExpiry.remove(p.getStringUUID());
                p.sendSystemMessage(Component.literal("The Temp V wears off; your power fades.").withStyle(ChatFormatting.GRAY));
            }
            // Expire timed flight.
            Long until = FLIGHT_UNTIL.get(p.getUUID());
            if (until != null && now >= until && !p.isCreative() && !p.isSpectator()) {
                FLIGHT_UNTIL.remove(p.getUUID());
                p.getAbilities().mayfly = false;
                p.getAbilities().flying = false;
                p.onUpdateAbilities();
            }
        }
    }

    public static void onPlayerRemoved(UUID uuid) {
        FLIGHT_UNTIL.remove(uuid);
        BLACK_FLASH_UNTIL.remove(uuid);
    }

    private static Component err(String msg) {
        return Component.literal(msg).withStyle(ChatFormatting.RED);
    }
}
