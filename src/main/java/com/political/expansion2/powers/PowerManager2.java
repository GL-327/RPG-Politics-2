package com.political.expansion2.powers;

import com.political.combat.StatManager;
import com.political.politics.DataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Runs Expansion 2 power activations alongside {@link com.political.power.PowerManager}. */
public final class PowerManager2 {

    private static final Map<String, Long> COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Set<String>> PASSIVE_ON = new HashMap<>();
    private static final Map<UUID, ActiveDomain> DOMAINS = new HashMap<>();
    private static int tickCounter;

    private record ActiveDomain(Power2 power, long expiresAtMs) {}

    private PowerManager2() {}

    public static void register() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(PowerManager2::serverTick);
    }

    private static void serverTick(MinecraftServer server) {
        if (++tickCounter % 20 != 0) return;
        long now = System.currentTimeMillis();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            tickPassives(p);
            tickDomains(p, now);
        }
    }

    private static void tickPassives(ServerPlayer p) {
        Set<String> on = PASSIVE_ON.get(p.getUUID());
        if (on == null || on.isEmpty()) return;
        if (on.contains(Power2.PASSIVE_REGEN_AURA.id()) && p.getHealth() < p.getMaxHealth()) p.heal(0.5f);
        if (on.contains(Power2.PASSIVE_BATTLE_FRENZY.id()) && p.getHealth() <= p.getMaxHealth() * 0.5f) {
            p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 0, false, false, false));
        }
        if (on.contains(Power2.PASSIVE_GRADE_PRESSURE.id())) {
            int grade = DataManager.sorcererGrade(p.getStringUUID());
            for (LivingEntity e : Power2Effects.around(p, 10)) {
                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, Math.min(2, Math.max(0, grade - 1)), false, false, false));
            }
        }
        if (on.contains(Power2.PASSIVE_CURSE_RESIST.id())) {
            p.removeEffect(MobEffects.POISON);
            p.removeEffect(MobEffects.WITHER);
            p.removeEffect(MobEffects.WEAKNESS);
        }
        if (on.contains(Power2.PASSIVE_CE_EFFICIENCY.id()) && StatManager.getMaxCursedEnergy(p) > 0) {
            StatManager.addCursedEnergy(p, 0.5);
        }
        if (on.contains(Power2.PASSIVE_BLACK_FLASH_MASTERY.id())) {
            p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 0, false, false, false));
        }
    }

    private static void tickDomains(ServerPlayer p, long now) {
        ActiveDomain dom = DOMAINS.get(p.getUUID());
        if (dom == null || now >= dom.expiresAtMs()) {
            if (dom != null) DOMAINS.remove(p.getUUID());
            return;
        }
        if (!(p.level() instanceof ServerLevel sl)) return;
        for (LivingEntity e : Power2Effects.around(p, 14)) {
            if (e != p) applyDomainTick(sl, p, e, dom.power());
        }
        Power2Effects.domainRing(sl, p, 12, ParticleTypes.REVERSE_PORTAL, 24);
    }

    private static void applyDomainTick(ServerLevel sl, ServerPlayer owner, LivingEntity e, Power2 domain) {
        switch (domain) {
            case DOMAIN_INFINITE_VOID -> {
                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false, false));
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2, false, false, false));
                e.hurtServer(sl, sl.damageSources().magic(), 3.0f);
            }
            case DOMAIN_TIME_CELL -> e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 4, false, false, false));
            case DOMAIN_HANDMADE -> {
                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 2, false, false, false));
                e.hurtServer(sl, sl.damageSources().magic(), 4.0f);
            }
            case DOMAIN_MUTUAL_LOVE -> e.hurtServer(sl, sl.damageSources().playerAttack(owner), 6.0f);
            case DOMAIN_DEADLY_SENTENCING -> e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 3, false, false, false));
            case DOMAIN_CEREBRAL -> {
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3, false, false, false));
                e.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 60, 2, false, false, false));
            }
            case DOMAIN_IDLE_GAMBLE -> {
                if (owner.getRandom().nextBoolean()) e.hurtServer(sl, sl.damageSources().magic(), 8.0f);
                else owner.heal(1.0f);
            }
            case DOMAIN_WOMB -> {
                e.push(0, 0.4, 0);
                e.hurtMarked = true;
                e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 0, false, false, false));
            }
            case DOMAIN_THUNDER_GAAIS -> {
                e.hurtServer(sl, sl.damageSources().magic(), 5.0f);
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, false, false));
            }
            case DOMAIN_HORIZON -> {
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3, false, false, false));
                e.hurtServer(sl, sl.damageSources().magic(), 4.0f);
            }
            default -> { }
        }
    }

    public static boolean isPassiveOn(ServerPlayer p, Power2 passive) {
        Set<String> on = PASSIVE_ON.get(p.getUUID());
        return on != null && on.contains(passive.id());
    }

    public static boolean isPower2(String id) {
        return Power2.byId(id) != null;
    }

    public static Component activateSelected(ServerPlayer player) {
        Power2 power = Power2.byId(DataManager.selectedPower(player.getStringUUID()));
        return power == null ? null : activate(player, power);
    }

    public static Component activate(ServerPlayer player, Power2 power) {
        if (!DataManager.hasPower(player.getStringUUID(), power.id())) return err("You have not awakened that power.");
        if (onCooldown(player, power)) return err(power.displayName + " is recharging (" + cooldownRemaining(player, power) + "s).");
        int grade = DataManager.sorcererGrade(player.getStringUUID());
        if (power.minGrade > 0 && grade < power.minGrade) {
            return err(power.displayName + " requires " + DataManager.gradeLabel(power.minGrade) + ".");
        }
        if (power.isPassive) return togglePassive(player, power);

        int cost = scaledCost(player, power);
        if (power.usesCursedEnergy()) {
            if (StatManager.getMaxCursedEnergy(player) <= 0) return err("You have no cursed energy to channel.");
            if (!StatManager.spendCursedEnergy(player, cost)) return err("Not enough Cursed Energy (" + cost + " needed).");
        } else if (!StatManager.spendMana(player, cost)) {
            return err("Not enough Mana (" + cost + " needed).");
        }

        ServerLevel level = player.level();
        if (!cast(player, level, power)) {
            if (power.usesCursedEnergy()) StatManager.addCursedEnergy(player, cost);
            else StatManager.addMana(player, cost);
            return err("No valid target for " + power.displayName + ".");
        }
        setCooldown(player, power);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.2f);
        return Component.literal("Used " + power.displayName + ".").withStyle(power.category.color);
    }

    private static Component togglePassive(ServerPlayer player, Power2 power) {
        Set<String> on = PASSIVE_ON.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
        if (on.remove(power.id())) return Component.literal("Deactivated " + power.displayName + ".").withStyle(ChatFormatting.GRAY);
        on.add(power.id());
        return Component.literal("Activated " + power.displayName + ".").withStyle(ChatFormatting.GREEN);
    }

    private static int scaledCost(ServerPlayer player, Power2 power) {
        double mult = DataManager.data().powerCostMultiplier;
        if (isPassiveOn(player, Power2.PASSIVE_CE_EFFICIENCY) && power.usesCursedEnergy()) mult *= 0.85;
        return (int) Math.round(power.energyCost * mult);
    }

    private static boolean cast(ServerPlayer p, ServerLevel level, Power2 power) {
        return switch (power) {
            case VILTRUMITE_DASH -> { Power2Effects.blink(p, 12); yield true; }
            case VILTRUMITE_MEGA_PUNCH -> {
                LivingEntity t = Power2Effects.lookTarget(p, 6);
                if (t == null) yield false;
                t.hurtServer(level, level.damageSources().playerAttack(p), 22.0f);
                Power2Effects.launchEntity(t, p, -2.5, 0.8);
                yield true;
            }
            case VILTRUMITE_BLOCK -> {
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 160, 4, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 2, false, true));
                yield true;
            }
            case VILTRUMITE_GRAB_SLAM -> {
                LivingEntity t = Power2Effects.lookTarget(p, 8);
                if (t == null) yield false;
                t.hurtServer(level, level.damageSources().playerAttack(p), 16.0f);
                t.push(0, -0.8, 0);
                t.hurtMarked = true;
                yield true;
            }
            case VILTRUMITE_ORBITAL_STRIKE -> {
                LivingEntity t = Power2Effects.lookTarget(p, 30);
                Vec3 at = t != null ? t.position() : Power2Effects.aimPoint(p, 18);
                Power2Effects.grantFlight(p, 15_000L);
                p.teleportTo(at.x, at.y + 8, at.z);
                for (LivingEntity e : Power2Effects.nearPoint(level, at, 6, p)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), 14.0f);
                    Power2Effects.launchEntity(e, p, -2.0, 0.6);
                }
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, at.x, at.y, at.z, 1, 0, 0, 0, 0);
                yield true;
            }
            case VILTRUMITE_IMPACT_CRATER, VILTRUMITE_AERIAL_BOMBARDMENT -> {
                for (LivingEntity e : Power2Effects.around(p, 9)) {
                    Power2Effects.launchEntity(e, p, -2.8, 0.7);
                    e.hurtServer(level, level.damageSources().playerAttack(p), 8.0f);
                }
                level.sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY(), p.getZ(), 8, 2.5, 0.2, 2.5, 0);
                yield true;
            }
            case VILTRUMITE_RAGE_SURGE -> {
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 300, 3, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 300, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 300, 2, false, true));
                yield true;
            }
            case VILTRUMITE_THUNDERCLAP -> {
                for (LivingEntity e : Power2Effects.cone(p, 14, 0.35)) {
                    Power2Effects.launchEntity(e, p, -2.5, 0.4);
                    e.hurtServer(level, level.damageSources().playerAttack(p), 5.0f);
                }
                Power2Effects.particleCone(level, p, ParticleTypes.CLOUD);
                yield true;
            }
            case VILTRUMITE_SKULL_CRUSH -> {
                LivingEntity t = Power2Effects.lookTarget(p, 10);
                if (t == null) yield false;
                t.hurtServer(level, level.damageSources().playerAttack(p), 20.0f);
                t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 3));
                yield true;
            }
            case VILTRUMITE_SUBORBITAL_DIVE -> {
                Power2Effects.grantFlight(p, 20_000L);
                Power2Effects.launchSelf(p, p.getViewVector(1f).scale(2.5).add(0, 1.5, 0));
                yield true;
            }
            case VILTRUMITE_WAR_CRY -> {
                for (LivingEntity e : Power2Effects.around(p, 12)) {
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 2));
                }
                level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.5f, 0.7f);
                yield true;
            }
            case VILTRUMITE_BONE_SHATTER -> {
                for (LivingEntity e : Power2Effects.cone(p, 10, 0.5)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), 10.0f);
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2));
                }
                yield true;
            }
            case VILTRUMITE_MOMENTUM_RAM -> {
                Power2Effects.launchSelf(p, p.getViewVector(1f).scale(2.0));
                for (LivingEntity e : Power2Effects.cone(p, 8, 0.2)) {
                    e.hurtServer(level, level.damageSources().playerAttack(p), 9.0f);
                    Power2Effects.launchEntity(e, p, -1.5, 0.5);
                }
                yield true;
            }
            case VILTRUMITE_SUPERSONIC -> {
                Power2Effects.grantFlight(p, 25_000L);
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 240, 4, false, true));
                yield true;
            }
            case HOMELANDER_BEAM -> { Power2Effects.beam(p, level, 28, 12.0f, true, true); yield true; }
            case ATOM_EVE_SHIFT -> {
                p.heal(6.0f);
                for (LivingEntity e : Power2Effects.around(p, 8)) {
                    if (e != p) e.hurtServer(level, level.damageSources().magic(), 7.0f);
                }
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 1, p.getZ(), 40, 2, 1, 2, 0.05);
                yield true;
            }
            case A_TRAIN_BLUR -> {
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 160, 5, false, true));
                Power2Effects.blink(p, 10);
                yield true;
            }
            case QUEEN_MAEVES_COUNTER -> {
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 3, false, true));
                LivingEntity t = Power2Effects.lookTarget(p, 6);
                if (t != null) t.hurtServer(level, level.damageSources().playerAttack(p), 14.0f);
                yield true;
            }
            case TRANSLUCENT_VANISH -> { p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 240, 0, false, false)); yield true; }
            case STARLIGHT_BOLT -> { Power2Effects.beam(p, level, 22, 10.0f, false, false); yield true; }
            case BLACK_NOIR_STRIKE -> {
                LivingEntity t = Power2Effects.lookTarget(p, 12);
                if (t == null) yield false;
                t.hurtServer(level, level.damageSources().playerAttack(p), 24.0f);
                level.sendParticles(ParticleTypes.SQUID_INK, t.getX(), t.getEyeY(), t.getZ(), 30, 0.3, 0.4, 0.3, 0.02);
                yield true;
            }
            case SOLDIER_BOY_CHARGE -> {
                Power2Effects.launchSelf(p, p.getViewVector(1f).scale(1.8));
                for (LivingEntity e : Power2Effects.cone(p, 10, 0.25)) {
                    e.setRemainingFireTicks(100);
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
                    e.hurtServer(level, level.damageSources().playerAttack(p), 10.0f);
                }
                yield true;
            }
            case DEEP_TIDAL_CRUSH -> {
                for (LivingEntity e : Power2Effects.around(p, 10)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 3));
                    e.hurtServer(level, level.damageSources().magic(), 6.0f);
                }
                level.sendParticles(ParticleTypes.BUBBLE, p.getX(), p.getY() + 1, p.getZ(), 80, 4, 2, 4, 0.05);
                yield true;
            }
            case MM_SUPPRESS -> {
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 2, false, true));
                p.removeEffect(MobEffects.POISON);
                for (LivingEntity e : Power2Effects.cone(p, 8, 0.45)) e.hurtServer(level, level.damageSources().playerAttack(p), 7.0f);
                yield true;
            }
            case FRENCHIE_ARSENAL -> {
                Vec3 at = Power2Effects.aimPoint(p, 14);
                for (LivingEntity e : Power2Effects.nearPoint(level, at, 5, p)) {
                    e.hurtServer(level, level.damageSources().magic(), 12.0f);
                    Power2Effects.launchEntity(e, p, -1.8, 0.5);
                }
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, at.x, at.y, at.z, 2, 0, 0, 0, 0);
                yield true;
            }
            case KIMIKO_BLADE_STORM -> {
                for (LivingEntity e : Power2Effects.around(p, 6)) e.hurtServer(level, level.damageSources().playerAttack(p), 8.0f);
                Power2Effects.particleCone(level, p, ParticleTypes.SWEEP_ATTACK);
                yield true;
            }
            case BUTCHER_BERSERK -> {
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 240, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, 1, false, true));
                yield true;
            }
            case RYAN_OUTBURST -> {
                for (LivingEntity e : Power2Effects.around(p, 14)) {
                    e.hurtServer(level, level.damageSources().magic(), 15.0f);
                    Power2Effects.launchEntity(e, p, -2.0, 0.8);
                }
                level.sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY() + 1, p.getZ(), 12, 4, 2, 4, 0);
                yield true;
            }
            case NEO_NOIR_ECHO -> {
                LivingEntity t = Power2Effects.lookTarget(p, 14);
                if (t == null) yield false;
                t.hurtServer(level, level.damageSources().playerAttack(p), 12.0f);
                t.hurtServer(level, level.damageSources().playerAttack(p), 12.0f);
                yield true;
            }
            case STRAW_DOLL -> {
                LivingEntity t = Power2Effects.lookTarget(p, 24);
                if (t == null) yield false;
                t.hurtServer(level, level.damageSources().magic(), 14.0f);
                level.sendParticles(ParticleTypes.CRIT, t.getX(), t.getEyeY(), t.getZ(), 25, 0.3, 0.4, 0.3, 0.1);
                yield true;
            }
            case HAIRPIN -> {
                for (LivingEntity e : Power2Effects.cone(p, 12, 0.4)) e.hurtServer(level, level.damageSources().magic(), 7.0f);
                Power2Effects.particleCone(level, p, ParticleTypes.CRIT);
                yield true;
            }
            case BOOGIE_WOOGIE -> {
                LivingEntity t = Power2Effects.lookTarget(p, 24);
                if (t == null) yield false;
                double px = p.getX(), py = p.getY(), pz = p.getZ();
                p.teleportTo(t.getX(), t.getY(), t.getZ());
                t.teleportTo(px, py, pz);
                yield true;
            }
            case COPY_TECHNIQUE -> {
                for (LivingEntity e : Power2Effects.around(p, 8)) e.hurtServer(level, level.damageSources().magic(), 10.0f);
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, p.getX(), p.getY() + 1, p.getZ(), 50, 2, 1, 2, 0);
                yield true;
            }
            case RIKA_MANIFEST -> {
                Power2Effects.summonShadows(p, level, 2);
                for (LivingEntity e : Power2Effects.around(p, 8)) e.hurtServer(level, level.damageSources().magic(), 12.0f);
                yield true;
            }
            case STAR_RAGE -> {
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 3, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 1, false, true));
                yield true;
            }
            case JUDGEMAN -> {
                for (LivingEntity e : Power2Effects.cone(p, 14, 0.35)) {
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 3));
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 2));
                }
                yield true;
            }
            case CONFISCATION -> {
                LivingEntity t = Power2Effects.lookTarget(p, 16);
                if (t == null) yield false;
                t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 4));
                t.removeEffect(MobEffects.RESISTANCE);
                t.removeEffect(MobEffects.ABSORPTION);
                yield true;
            }
            case CURSED_SPIRIT_MANIPULATION -> { Power2Effects.summonShadows(p, level, 4); yield true; }
            case DISASTER_PLANTS -> {
                for (LivingEntity e : Power2Effects.around(p, 9)) {
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 2));
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 160, 2));
                    e.hurtServer(level, level.damageSources().magic(), 6.0f);
                }
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, p.getX(), p.getY() + 1, p.getZ(), 60, 3, 1, 3, 0.02);
                yield true;
            }
            case FLOWING_RED_SCALE -> {
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 220, 3, false, true));
                for (LivingEntity e : Power2Effects.around(p, 4)) e.hurtServer(level, level.damageSources().magic(), 4.0f);
                yield true;
            }
            case ANTIGRAVITY_SYSTEM -> {
                for (LivingEntity e : Power2Effects.around(p, 12)) {
                    e.push(0, 1.5, 0);
                    e.hurtMarked = true;
                    e.hurtServer(level, level.damageSources().magic(), 7.0f);
                }
                yield true;
            }
            case BIRD_STRIKE -> { Power2Effects.beam(p, level, 24, 9.0f, false, false); yield true; }
            case ICE_FORMATION -> {
                for (LivingEntity e : Power2Effects.cone(p, 11, 0.45)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 140, 4));
                    e.hurtServer(level, level.damageSources().magic(), 8.0f);
                }
                level.sendParticles(ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 1, p.getZ(), 60, 3, 1, 3, 0.05);
                yield true;
            }
            case THUNDER_INSPECTION -> {
                LivingEntity t = Power2Effects.lookTarget(p, 28);
                if (t != null) {
                    Power2Effects.strike(level, t.position());
                    t.hurtServer(level, level.damageSources().magic(), 10.0f);
                }
                for (LivingEntity e : Power2Effects.around(p, 8)) {
                    if (e != t) e.hurtServer(level, level.damageSources().magic(), 5.0f);
                }
                yield true;
            }
            case SOUL_MULTIPLICITY -> {
                for (LivingEntity e : Power2Effects.around(p, 10)) {
                    e.hurtServer(level, level.damageSources().magic(), 6.0f);
                    e.hurtServer(level, level.damageSources().magic(), 6.0f);
                }
                yield true;
            }
            case BLOOD_BOILING -> {
                p.hurtServer(level, level.damageSources().magic(), 3.0f);
                for (LivingEntity e : Power2Effects.cone(p, 10, 0.5)) {
                    e.setRemainingFireTicks(100);
                    e.hurtServer(level, level.damageSources().magic(), 9.0f);
                }
                yield true;
            }
            case CURSE_COLLAGE -> {
                p.heal(8.0f);
                StatManager.addCursedEnergy(p, 20);
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2, false, true));
                yield true;
            }
            case DEATH_SWARMING -> {
                for (LivingEntity e : Power2Effects.around(p, 9)) {
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1));
                    e.hurtServer(level, level.damageSources().magic(), 7.0f);
                }
                level.sendParticles(ParticleTypes.SCULK_SOUL, p.getX(), p.getY() + 1, p.getZ(), 50, 3, 1, 3, 0.03);
                yield true;
            }
            case GAMMA_RAY -> { Power2Effects.beam(p, level, 35, 22.0f, false, true); yield true; }
            case DOMAIN_INFINITE_VOID, DOMAIN_TIME_CELL, DOMAIN_HANDMADE, DOMAIN_MUTUAL_LOVE,
                 DOMAIN_DEADLY_SENTENCING, DOMAIN_CEREBRAL, DOMAIN_IDLE_GAMBLE, DOMAIN_WOMB,
                 DOMAIN_THUNDER_GAAIS, DOMAIN_HORIZON -> { castDomain(p, level, power); yield true; }
            case ULTIMATE_PURPLE_STORM -> {
                Power2Effects.beam(p, level, 36, 28.0f, false, true);
                for (LivingEntity e : Power2Effects.around(p, 10)) e.hurtServer(level, level.damageSources().magic(), 12.0f);
                yield true;
            }
            case ULTIMATE_MAXIMUM_UZUMAKI -> {
                Vec3 at = Power2Effects.aimPoint(p, 16);
                for (LivingEntity e : Power2Effects.nearPoint(level, at, 7, p)) {
                    e.hurtServer(level, level.damageSources().magic(), 28.0f);
                    Power2Effects.launchEntity(e, p, -2.5, 0.6);
                }
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, at.x, at.y, at.z, 100, 3, 2, 3, 0.1);
                yield true;
            }
            case ULTIMATE_MERGED_BEAST -> {
                Power2Effects.summonShadows(p, level, 3);
                for (LivingEntity e : Power2Effects.cone(p, 14, 0.35)) e.hurtServer(level, level.damageSources().playerAttack(p), 16.0f);
                yield true;
            }
            case ULTIMATE_METEOR_STORM -> {
                for (int i = 0; i < 3; i++) {
                    Vec3 at = Power2Effects.aimPoint(p, 12 + i * 4);
                    for (LivingEntity e : Power2Effects.nearPoint(level, at, 5, p)) {
                        e.setRemainingFireTicks(120);
                        e.hurtServer(level, level.damageSources().magic(), 18.0f);
                    }
                }
                yield true;
            }
            case ULTIMATE_OPEN_SHRINE -> {
                for (LivingEntity e : Power2Effects.cone(p, 20, 0.25)) e.hurtServer(level, level.damageSources().playerAttack(p), 22.0f);
                for (LivingEntity e : Power2Effects.around(p, 10)) e.hurtServer(level, level.damageSources().magic(), 14.0f);
                Power2Effects.particleCone(level, p, ParticleTypes.SWEEP_ATTACK);
                yield true;
            }
            case ULTIMATE_STAR_FALL -> {
                for (LivingEntity e : Power2Effects.around(p, 14)) {
                    Power2Effects.launchEntity(e, p, 2.5, -0.3);
                    e.hurtServer(level, level.damageSources().magic(), 16.0f);
                }
                level.sendParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 2, p.getZ(), 100, 5, 3, 5, 0.05);
                yield true;
            }
            case ULTIMATE_VILTRUMITE_APOCALYPSE -> {
                Power2Effects.grantFlight(p, 30_000L);
                Power2Effects.beam(p, level, 30, 20.0f, true, true);
                for (LivingEntity e : Power2Effects.around(p, 12)) {
                    Power2Effects.launchEntity(e, p, -3.0, 0.8);
                    e.hurtServer(level, level.damageSources().playerAttack(p), 12.0f);
                }
                yield true;
            }
            case ULTIMATE_HERO_SQUAD -> {
                p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 300, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 300, 2, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1, false, true));
                for (LivingEntity e : Power2Effects.around(p, 10)) e.hurtServer(level, level.damageSources().playerAttack(p), 10.0f);
                yield true;
            }
            default -> power.isPassive;
        };
    }

    private static void castDomain(ServerPlayer p, ServerLevel level, Power2 domain) {
        p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 1, false, true));
        p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 2, false, true));
        for (LivingEntity e : Power2Effects.around(p, 14)) {
            if (e != p) {
                e.hurtServer(level, level.damageSources().magic(), 14.0f);
                applyDomainTick(level, p, e, domain);
            }
        }
        Power2Effects.domainRing(level, p, 13, ParticleTypes.REVERSE_PORTAL, 32);
        DOMAINS.put(p.getUUID(), new ActiveDomain(domain, System.currentTimeMillis() + 8000L));
    }

    private static boolean onCooldown(ServerPlayer p, Power2 power) {
        Long ready = COOLDOWNS.get(p.getStringUUID() + "|" + power.name());
        return ready != null && System.currentTimeMillis() < ready;
    }

    private static long cooldownRemaining(ServerPlayer p, Power2 power) {
        Long ready = COOLDOWNS.get(p.getStringUUID() + "|" + power.name());
        return ready == null ? 0 : Math.max(0, (ready - System.currentTimeMillis()) / 1000);
    }

    private static void setCooldown(ServerPlayer p, Power2 power) {
        int cd = power.cooldownTicks;
        if (isPassiveOn(p, Power2.PASSIVE_SHADOW_AFFINITY)
                && (power.category == Power2.Category.JJK_TECHNIQUE || power.isDomain)) {
            cd = (int) (cd * 0.85);
        }
        COOLDOWNS.put(p.getStringUUID() + "|" + power.name(), System.currentTimeMillis() + cd * 50L);
    }

    public static void onPlayerRemoved(UUID uuid) {
        PASSIVE_ON.remove(uuid);
        DOMAINS.remove(uuid);
        COOLDOWNS.keySet().removeIf(k -> k.startsWith(uuid.toString()));
    }

    public static void handleAction(ServerPlayer player, String action, String powerId) {
        switch (action) {
            case "select" -> {
                Power2 p = Power2.byId(powerId);
                if (p != null && DataManager.hasPower(player.getStringUUID(), p.id())) {
                    DataManager.setSelectedPower(player.getStringUUID(), p.id());
                } else player.sendSystemMessage(err("You have not awakened that power."), true);
            }
            case "activate" -> {
                Component result = activateSelected(player);
                if (result != null) player.sendSystemMessage(result, true);
            }
            default -> { }
        }
        com.political.power.PowerManager.sendMenu(player);
    }

    private static Component err(String msg) {
        return Component.literal(msg).withStyle(ChatFormatting.RED);
    }
}
