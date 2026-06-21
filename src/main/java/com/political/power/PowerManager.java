package com.political.power;

import com.political.combat.StatManager;
import com.political.net.ModNetworking;
import com.political.net.PowerMenuS2C;
import com.political.politics.DataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
    private static final Map<UUID, Long> BLACK_FLASH_UNTIL = new HashMap<>();
    private static int tickCounter = 0;

    private PowerManager() {}

    public static void register() {
        // Infinity (passive): knowing the technique lets cursed energy auto-negate incoming hits.
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayer sp)) return true;
            String uuid = sp.getStringUUID();
            if (!DataManager.hasPower(uuid, Power.INFINITY.id())) return true;
            if (StatManager.getCursedEnergy(sp) < 6) return true;
            int grade = DataManager.sorcererGrade(uuid);
            if (RNG.nextFloat() < 0.20f + 0.05f * grade) {
                StatManager.spendCursedEnergy(sp, 6);
                if (sp.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1, sp.getZ(), 14, 0.4, 0.6, 0.4, 0.02);
                }
                return false; // hit folded into "infinity" — negated
            }
            return true;
        });
    }

    private static final java.util.Random RNG = new java.util.Random();

    /**
     * Consumes a pending Black Flash on the attacker (set by activating the technique), dealing
     * bonus distorted damage on this hit. Called from {@link com.political.combat.AbilityEngine}
     * because that engine consumes the attack event first (returning SUCCESS), which would
     * otherwise prevent a second {@code AttackEntityCallback} listener from ever running.
     */
    public static void tryConsumeBlackFlash(ServerPlayer sp, LivingEntity target, ServerLevel sl) {
        Long until = BLACK_FLASH_UNTIL.get(sp.getUUID());
        if (until == null || System.currentTimeMillis() >= until) return;
        BLACK_FLASH_UNTIL.remove(sp.getUUID());
        target.hurtServer(sl, sl.damageSources().playerAttack(sp), 12.0f);
        sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 40, 0.4, 0.6, 0.4, 0.4);
        sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2f, 0.7f);
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
        if (!com.political.config.PoliticalConfig.get().powersEnabled)
            return err("Powers are disabled on this server.");
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
        boolean landed = cast(player, level, power);
        if (!landed) {
            // Strictly-targeted power with nothing in sight: refund energy and tell the player.
            if (power.origin == Power.Origin.CURSED_TECHNIQUE) StatManager.addCursedEnergy(player, cost);
            else StatManager.addMana(player, cost);
            return err("No target in sight for " + power.displayName + ".");
        }
        setCooldown(player, power);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.2f);
        return Component.literal("Used " + power.displayName + ".").withStyle(power.origin.color);
    }

    /** Returns {@code false} if a strictly-targeted power found no target (so energy is refunded). */
    private static boolean cast(ServerPlayer p, ServerLevel level, Power power) {
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
                if (t == null) return false;
                launchEntity(t, p, -2.0, 0.6); // negative = away from player
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
                if (t == null) return false;
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 6));
                t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 4));
                t.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 160, 4));
            }
            case MIND_CONTROL -> {
                LivingEntity t = lookTarget(p, 16);
                if (!(t instanceof Mob mob)) return false;
                Monster victim = nearestMonster(p, mob, 24);
                if (victim != null) mob.setTarget(victim);
                mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
            }
            case HEAD_POP -> {
                LivingEntity t = lookTarget(p, 18);
                if (t == null) return false;
                t.hurtServer(level, level.damageSources().playerAttack(p), 35.0f);
                level.sendParticles(ParticleTypes.CRIMSON_SPORE, t.getX(), t.getEyeY(), t.getZ(), 60, 0.3, 0.3, 0.3, 0.2);
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
                if (t == null) return false;
                t.hurtServer(level, level.damageSources().playerAttack(p), 20.0f);
            }
            case DIVERGENT_FIST -> {
                LivingEntity t = lookTarget(p, 6);
                if (t == null) return false;
                t.hurtServer(level, level.damageSources().playerAttack(p), 8.0f);
                launchEntity(t, p, -1.6, 0.5);
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

            // ---- New Compound V powers ----
            case ELECTROKINESIS -> {
                LivingEntity t = lookTarget(p, 30);
                if (t != null) {
                    strike(level, t.position());
                    t.hurtServer(level, level.damageSources().magic(), 6.0f);
                }
                for (LivingEntity e : around(p, 8)) {
                    if (e == p) continue;
                    e.hurtServer(level, level.damageSources().magic(), 5.0f);
                }
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY() + 1, p.getZ(), 60, 4, 2, 4, 0.2);
            }
            case ACID_SPRAY -> {
                for (LivingEntity e : cone(p, 10, 0.55)) {
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 1));
                    e.hurtServer(level, level.damageSources().magic(), 5.0f);
                }
                particleCone(level, p, ParticleTypes.SNEEZE);
            }
            case PHASE_SHIFT -> {
                p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0, false, false));
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 1, false, true));
            }
            case TERRAKINESIS -> {
                for (LivingEntity e : around(p, 7)) {
                    if (e == p) continue;
                    e.push(0, 1.2, 0);
                    e.hurtMarked = true;
                    e.hurtServer(level, level.damageSources().magic(), 5.0f);
                }
                level.sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY(), p.getZ(), 6, 3, 0.1, 3, 0.0);
            }
            case ELASTIC_REACH -> {
                for (LivingEntity e : cone(p, 9, 0.3)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), 7.0f);
                    launchEntity(e, p, -1.0, 0.3);
                }
                particleCone(level, p, ParticleTypes.CRIT);
            }
            case BLOODLUST -> {
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 300, 3, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 300, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.HASTE, 300, 2, false, true));
            }
            case GRAVITY_CRUSH -> {
                for (LivingEntity e : around(p, 12)) {
                    if (e == p) continue;
                    launchEntity(e, p, 3.0, -0.2);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 4));
                    e.hurtServer(level, level.damageSources().magic(), 7.0f);
                }
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 1, p.getZ(), 80, 4, 2, 4, 0.05);
            }
            case MAGNETISM -> {
                for (LivingEntity e : around(p, 14)) launchEntity(e, p, 3.0, 0.1);
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.0);
            }
            case LIGHT_FLARE -> {
                for (LivingEntity e : cone(p, 12, 0.2)) {
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 2));
                }
                level.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getEyeY(), p.getZ(), 80, 1, 1, 1, 0.1);
            }
            case VENOM_CLOUD -> {
                for (LivingEntity e : around(p, 8)) {
                    if (e == p) continue;
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1));
                }
                level.sendParticles(ParticleTypes.SNEEZE, p.getX(), p.getY() + 1, p.getZ(), 60, 4, 2, 4, 0.02);
            }
            case THERMAL_LANCE -> beam(p, level, 22, 18.0f, true, true);
            case KINETIC_ABSORPTION -> {
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 3, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 400, 1, false, true));
            }

            // ---- New cursed techniques ----
            case SHADOW_SERPENT -> summonShadows(p, level, 4);
            case CURSED_CLONE -> summonShadows(p, level, 2);
            case BLOOD_EDGE -> {
                p.hurtServer(level, level.damageSources().magic(), 4.0f);
                for (LivingEntity e : cone(p, 12, 0.4)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), 9.0f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
                }
                particleCone(level, p, ParticleTypes.CRIT);
            }
            case PIERCING_BLOOD -> {
                p.hurtServer(level, level.damageSources().magic(), 4.0f);
                beam(p, level, 30, 16.0f, false, false);
            }
            case DECAY -> {
                for (LivingEntity e : around(p, 8)) {
                    if (e == p) continue;
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 2));
                    e.hurtServer(level, level.damageSources().magic(), 6.0f);
                }
                level.sendParticles(ParticleTypes.SCULK_SOUL, p.getX(), p.getY() + 1, p.getZ(), 40, 3, 1, 3, 0.02);
            }
            case IDLE_TRANSFIGURATION -> {
                LivingEntity t = lookTarget(p, 16);
                if (t == null) return false;
                t.hurtServer(level, level.damageSources().magic(), 14.0f);
                t.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 2));
                t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 3));
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 3));
            }
            case DISASTER_FLAMES -> {
                for (LivingEntity e : cone(p, 12, 0.45)) {
                    e.setRemainingFireTicks(160);
                    e.hurtServer(level, level.damageSources().playerAttack(p), 12.0f);
                }
                particleCone(level, p, ParticleTypes.FLAME);
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.4f, 0.7f);
            }
            case PROJECTION_SORCERY -> {
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 4, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.HASTE, 200, 3, false, true));
                for (LivingEntity e : cone(p, 12, 0.3)) e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 6));
            }
            case SWAP_PLACES -> {
                LivingEntity t = lookTarget(p, 30);
                if (t == null) return false;
                double px = p.getX(), py = p.getY(), pz = p.getZ();
                p.teleportTo(t.getX(), t.getY(), t.getZ());
                t.teleportTo(px, py, pz);
                level.sendParticles(ParticleTypes.PORTAL, px, py + 1, pz, 40, 0.5, 1, 0.5, 0.3);
            }
            case CURSED_BOMB -> {
                Vec3 at = aimPoint(p, 14);
                for (LivingEntity e : nearPoint(level, at, 5, p)) {
                    e.hurtServer(level, level.damageSources().magic(), 14.0f);
                    launchEntity(e, p, -2.0, 0.5);
                }
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, at.x, at.y, at.z, 1, 0, 0, 0, 0.0);
                level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4f, 0.9f);
            }
            case FALLING_BLOSSOM -> {
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 3, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2, false, true));
                for (LivingEntity e : around(p, 5)) {
                    if (e == p) continue;
                    e.hurtServer(level, level.damageSources().magic(), 7.0f);
                    launchEntity(e, p, -1.8, 0.4);
                }
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, p.getX(), p.getY() + 1, p.getZ(), 60, 3, 1, 3, 0.0);
            }
            case WHEEL_ADAPTATION -> {
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 600, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 600, 3, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 4, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0, false, true));
            }
            case NOVA_BURST -> {
                for (LivingEntity e : around(p, 9)) {
                    if (e == p) continue;
                    e.setRemainingFireTicks(100);
                    launchEntity(e, p, -2.6, 0.6);
                    e.hurtServer(level, level.damageSources().playerAttack(p), 9.0f);
                }
                level.sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY() + 1, p.getZ(), 10, 3, 1, 3, 0.0);
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.3f, 1.1f);
            }
            case CURSED_TIDE -> {
                for (LivingEntity e : around(p, 10)) {
                    if (e == p) continue;
                    launchEntity(e, p, -2.2, 0.4);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 2));
                    e.hurtServer(level, level.damageSources().magic(), 9.0f);
                }
                level.sendParticles(ParticleTypes.SCULK_SOUL, p.getX(), p.getY() + 1, p.getZ(), 60, 4, 1, 4, 0.04);
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.2f, 0.8f);
            }

            // ---- New Compound V powers (heroes / Viltrumite) ----
            case ICARUS_DIVE -> {
                grantFlight(p, 30_000L);
                launchSelf(p, p.getViewVector(1f).scale(2.2).add(0, 0.2, 0));
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.2f, 0.8f);
            }
            case HEAT_VISION_OVERLOAD -> beam(p, level, 30, 22.0f, true, true);
            case GROUND_POUND -> {
                for (LivingEntity e : around(p, 9)) {
                    if (e == p) continue;
                    launchEntity(e, p, -3.0, 0.7);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
                    e.hurtServer(level, level.damageSources().playerAttack(p), 9.0f);
                }
                level.sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY(), p.getZ(), 8, 2.5, 0.2, 2.5, 0.0);
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2f, 0.7f);
            }
            case SHOCK_PULSE -> {
                LivingEntity t = lookTarget(p, 30);
                if (t != null) {
                    strike(level, t.position());
                    t.hurtServer(level, level.damageSources().magic(), 6.0f);
                }
                for (LivingEntity e : around(p, 9)) {
                    if (e == p) continue;
                    e.hurtServer(level, level.damageSources().magic(), 5.0f);
                }
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.getX(), p.getY() + 1, p.getZ(), 70, 4, 2, 4, 0.2);
            }
            case TITAN_GRIP -> {
                LivingEntity t = lookTarget(p, 8);
                if (t == null) return false;
                t.hurtServer(level, level.damageSources().playerAttack(p), 18.0f);
                t.push(0, -0.6, 0);
                t.hurtMarked = true;
                level.sendParticles(ParticleTypes.CRIT, t.getX(), t.getY() + 1, t.getZ(), 30, 0.4, 0.6, 0.4, 0.3);
            }
            case AFTERIMAGE -> {
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 4, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 200, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));
                level.sendParticles(ParticleTypes.CLOUD, p.getX(), p.getY() + 1, p.getZ(), 30, 0.4, 0.8, 0.4, 0.02);
            }
            case REGEN_SURGE -> {
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, 3, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 240, 3, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 240, 0, false, true));
                p.removeEffect(MobEffects.POISON);
                p.removeEffect(MobEffects.WITHER);
                p.heal(6.0f);
            }
            case WIND_BLAST -> {
                for (LivingEntity e : cone(p, 14, 0.3)) {
                    launchEntity(e, p, -3.0, 0.4);
                    e.hurtServer(level, level.damageSources().playerAttack(p), 4.0f);
                }
                particleCone(level, p, ParticleTypes.CLOUD);
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 1.0f);
            }
            case METEOR_DROP -> {
                Vec3 at = aimPoint(p, 18);
                for (LivingEntity e : nearPoint(level, at, 6, p)) {
                    e.setRemainingFireTicks(120);
                    e.hurtServer(level, level.damageSources().playerAttack(p), 16.0f);
                    launchEntity(e, p, -1.5, 0.5);
                }
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, at.x, at.y, at.z, 1, 0, 0, 0, 0.0);
                level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5f, 0.6f);
            }
            case SEISMIC_SLAM -> {
                for (LivingEntity e : around(p, 8)) {
                    if (e == p) continue;
                    e.push(0, 1.3, 0);
                    e.hurtMarked = true;
                    e.hurtServer(level, level.damageSources().magic(), 7.0f);
                }
                level.sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY(), p.getZ(), 6, 3, 0.1, 3, 0.0);
            }

            // ---- New cursed techniques (JJK) ----
            case MAXIMUM_METEOR -> {
                Vec3 at = aimPoint(p, 20);
                for (LivingEntity e : nearPoint(level, at, 8, p)) {
                    e.setRemainingFireTicks(160);
                    e.hurtServer(level, level.damageSources().magic(), 30.0f);
                    launchEntity(e, p, -2.0, 0.6);
                }
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, at.x, at.y, at.z, 1, 0, 0, 0, 0.0);
                level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 120, 4, 3, 4, 0.1);
                level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.6f, 0.5f);
            }
            case FIRE_ARROW_FUGA -> beam(p, level, 28, 18.0f, true, true);
            case COFFIN_IRON_MOUNTAIN -> {
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 300, 4, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 3, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 300, 0, false, true));
                for (LivingEntity e : around(p, 8)) {
                    if (e == p) continue;
                    launchEntity(e, p, -2.0, 0.4);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 3));
                    e.hurtServer(level, level.damageSources().magic(), 10.0f);
                }
                level.sendParticles(ParticleTypes.BUBBLE, p.getX(), p.getY() + 1, p.getZ(), 120, 5, 2, 5, 0.1);
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.4f, 0.7f);
            }
            case SELF_EMBODIMENT -> {
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 1, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 2, false, true));
                for (LivingEntity e : around(p, 12)) {
                    if (e == p) continue;
                    e.hurtServer(level, level.damageSources().magic(), 20.0f);
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 3));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 3));
                }
                level.sendParticles(ParticleTypes.SCULK_SOUL, p.getX(), p.getY() + 1, p.getZ(), 200, 12, 6, 12, 0.05);
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.0f, 0.5f);
            }
            case SOUL_TRANSFIGURATION -> {
                LivingEntity t = lookTarget(p, 16);
                if (t == null) return false;
                t.hurtServer(level, level.damageSources().magic(), 16.0f);
                t.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 2));
                t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 3));
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 3));
                level.sendParticles(ParticleTypes.SCULK_SOUL, t.getX(), t.getEyeY(), t.getZ(), 30, 0.4, 0.6, 0.4, 0.04);
            }
            case CHIMERA_SHADOW_GARDEN -> {
                summonShadows(p, level, 6);
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 300, 1, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 300, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 300, 1, false, true));
                level.sendParticles(ParticleTypes.SQUID_INK, p.getX(), p.getY() + 1, p.getZ(), 120, 6, 2, 6, 0.02);
            }
            case NUE_STRIKE -> {
                LivingEntity t = lookTarget(p, 30);
                Vec3 at = t != null ? t.position() : aimPoint(p, 20);
                strike(level, at);
                for (LivingEntity e : nearPoint(level, at, 5, p)) {
                    e.hurtServer(level, level.damageSources().magic(), 7.0f);
                }
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y + 1, at.z, 60, 2, 2, 2, 0.2);
            }
            case GRAVITY_WELL -> {
                for (LivingEntity e : around(p, 14)) {
                    if (e == p) continue;
                    launchEntity(e, p, 3.5, -0.1);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 4));
                    e.hurtServer(level, level.damageSources().magic(), 10.0f);
                }
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 1, p.getZ(), 120, 5, 3, 5, 0.05);
            }
            case MALEVOLENT_SHRINE -> {
                for (LivingEntity e : cone(p, 18, 0.3)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), 20.0f);
                }
                for (LivingEntity e : around(p, 8)) {
                    if (e == p) continue;
                    launchEntity(e, p, -1.5, 0.3);
                    e.hurtServer(level, level.damageSources().magic(), 12.0f);
                }
                particleCone(level, p, ParticleTypes.SWEEP_ATTACK);
                level.sendParticles(ParticleTypes.SCULK_SOUL, p.getX(), p.getY() + 1, p.getZ(), 80, 6, 3, 6, 0.05);
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.0f, 0.6f);
            }
        }
        return true;
    }

    private static List<LivingEntity> nearPoint(ServerLevel level, Vec3 c, double r, LivingEntity exclude) {
        AABB box = new AABB(c.x - r, c.y - r, c.z - r, c.x + r, c.y + r, c.z + r);
        return level.getEntitiesOfClass(LivingEntity.class, box, x -> x != exclude && x.isAlive());
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
        // All non-creative flight is unified through the Viltrumite FlightManager.
        com.political.flight.FlightManager.enableTimed(p, durationMs);
    }

    // ---------------- Tick & cleanup ----------------

    public static void tick(MinecraftServer server) {
        if (++tickCounter % 20 != 0) return;
        long now = System.currentTimeMillis();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            // Expire temp powers (Temp V) — strips ONLY the temp-granted power, never earned ones.
            String uuid = p.getStringUUID();
            Long exp = DataManager.data().tempPowerExpiry.get(uuid);
            if (exp != null && exp != 0 && now >= exp) {
                String tempId = DataManager.data().tempPowerId.remove(uuid);
                DataManager.data().tempPowerExpiry.remove(uuid);
                String prev = DataManager.data().tempPowerPrevSelected.remove(uuid);
                if (tempId != null) {
                    DataManager.revokePower(uuid, tempId);
                    // Restore the previously selected power if the player still knows it.
                    if (prev != null && DataManager.hasPower(uuid, prev)) {
                        DataManager.setSelectedPower(uuid, prev);
                    }
                }
                p.sendSystemMessage(Component.literal("The Temp V wears off; the borrowed power fades.").withStyle(ChatFormatting.GRAY));
            }
            // Timed flight expiry is owned by FlightManager (see com.political.flight).
        }
    }

    public static void onPlayerRemoved(UUID uuid) {
        BLACK_FLASH_UNTIL.remove(uuid);
        com.political.flight.FlightManager.onPlayerRemoved(uuid);
    }

    private static Component err(String msg) {
        return Component.literal(msg).withStyle(ChatFormatting.RED);
    }

    // ---------------- Powers GUI ----------------

    /** Builds and sends a fresh Powers &amp; Serums menu snapshot to the player's client. */
    public static void sendMenu(ServerPlayer player) {
        String uuid = player.getStringUUID();
        String knownCsv = String.join(",", DataManager.knownPowers(uuid));
        String selected = DataManager.selectedPower(uuid);
        var trait = DataManager.cursedTrait(uuid);
        ModNetworking.send(player, new PowerMenuS2C(
                knownCsv,
                selected == null ? "" : selected,
                trait.display,
                DataManager.sorcererGrade(uuid),
                (int) Math.floor(StatManager.getMana(player)),
                (int) Math.floor(StatManager.getMaxMana(player)),
                (int) Math.floor(StatManager.getCursedEnergy(player)),
                (int) Math.floor(StatManager.getMaxCursedEnergy(player))));
    }

    /** Handles a {@link com.political.net.PowerActionC2S} from the GUI, then resends the menu. */
    public static void handleAction(ServerPlayer player, String action, String powerId) {
        switch (action) {
            case "select" -> {
                Power p = Power.byId(powerId);
                if (p != null && DataManager.hasPower(player.getStringUUID(), p.id())) {
                    DataManager.setSelectedPower(player.getStringUUID(), p.id());
                } else {
                    player.sendSystemMessage(err("You have not awakened that power."), true);
                }
            }
            case "activate" -> player.sendSystemMessage(activateSelected(player), true);
            default -> { }
        }
        sendMenu(player);
    }
}
