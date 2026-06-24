package com.political.curse.rules;

import com.political.combat.StatManager;
import com.political.config.PoliticalConfig;
import com.political.curse.SorcererGrade;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The cohesive cursed-energy <b>rules engine</b> at the heart of the JJK overhaul. Everything that is a
 * cross-cutting jujutsu rule — rather than a single technique's effect — lives here so the rest of the
 * code (techniques, domains, the power roster, the combat engine) can defer to one authority:
 *
 * <ul>
 *   <li><b>Binding Vows</b> — persistent risk/reward contracts ({@link BindingVow}) that scale a
 *       sorcerer's output and cursed-energy cost and levy a body toll.</li>
 *   <li><b>Cursed-energy output / reinforcement (flow)</b> — a toggle that drains cursed energy to
 *       reinforce the body (damage reduction) and amplify technique output.</li>
 *   <li><b>Reverse Cursed Technique</b> — a toggle that converts cursed energy into rapid healing.</li>
 *   <li><b>Black Flash</b> — a distortion crit that can be primed (the technique) or proc on any melee
 *       hit, granting the "in the zone" buff on success.</li>
 *   <li><b>Domain wards</b> — Simple Domain and Falling Blossom Emotion, the canonical counters that
 *       neutralise a domain's sure-hit (see {@code DomainManager}).</li>
 * </ul>
 *
 * <p>All windows are tracked in wall-clock millis (consistent with {@code PowerManager} cooldowns); the
 * per-tick upkeep (draining/healing) is driven from {@code JjkBootstrap}'s server-tick hook via
 * {@link #tick(MinecraftServer)} — no mixins.</p>
 */
public final class JjkRules {

    private static final Map<UUID, Long> OUTPUT_UNTIL = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> RCT_UNTIL = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> BLACK_FLASH_PRIMED = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> BLACK_FLASH_ZONE = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> SIMPLE_DOMAIN_UNTIL = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> FALLING_BLOSSOM_UNTIL = new ConcurrentHashMap<>();

    private static final java.util.Random RNG = new java.util.Random();

    private static int tickCounter;

    private JjkRules() {}

    // ------------------------------------------------------------------
    // Output multipliers (consumed by techniques / domains / powers)
    // ------------------------------------------------------------------

    /**
     * The combined cursed-energy output multiplier for {@code player}: binding vows, active cursed-energy
     * output (flow), and the Black Flash "in the zone" buff all fold in here. Technique and domain damage
     * is scaled by this so the whole roster honours the rules engine through a single call.
     */
    public static double outputMultiplier(ServerPlayer player) {
        if (player == null) return 1.0;
        UUID id = player.getUUID();
        PoliticalConfig cfg = PoliticalConfig.get();
        double m = vowOutputMultiplier(player.getStringUUID());
        if (isOutputActive(id)) m *= 1.0 + Math.max(0.0, cfg.jjkOutputBoostPct);
        if (isInBlackFlashZone(id)) m *= 1.0 + Math.max(0.0, cfg.jjkBlackFlashZoneBoostPct);
        return m;
    }

    /** Cursed-energy cost multiplier from sworn binding vows (clamped so a cast always costs something). */
    public static double ceCostMultiplier(ServerPlayer player) {
        if (player == null) return 1.0;
        double m = 1.0;
        for (BindingVow vow : activeVows(player.getStringUUID())) m *= vow.ceCostMultiplier;
        return Math.max(0.25, m);
    }

    /** Flat maximum-health delta (negative = penalty) owed by a player's sworn vows. Read by StatManager. */
    public static double flatHealthDelta(String uuid) {
        double sum = 0.0;
        for (BindingVow vow : activeVows(uuid)) sum -= vow.healthPenalty;
        return sum;
    }

    private static double vowOutputMultiplier(String uuid) {
        double m = 1.0;
        for (BindingVow vow : activeVows(uuid)) m *= vow.outputMultiplier;
        return m;
    }

    // ------------------------------------------------------------------
    // Binding Vows
    // ------------------------------------------------------------------

    public static List<BindingVow> activeVows(String uuid) {
        List<BindingVow> out = new java.util.ArrayList<>();
        for (String id : DataManager.activeBindingVows(uuid)) {
            BindingVow vow = BindingVow.byId(id);
            if (vow != null) out.add(vow);
        }
        return out;
    }

    public static boolean hasVow(String uuid, BindingVow vow) {
        return DataManager.activeBindingVows(uuid).contains(vow.id());
    }

    /** Swears or revokes a binding vow. Returns a feedback component (never throws). */
    public static Component toggleVow(ServerPlayer player, BindingVow vow) {
        String uuid = player.getStringUUID();
        int grade = SorcererGrade.of(player);
        if (hasVow(uuid, vow)) {
            DataManager.setBindingVow(uuid, vow.id(), false);
            StatManager.apply(player);
            StatManager.sync(player);
            return Component.literal("You release the " + vow.display + " binding vow.")
                    .withStyle(ChatFormatting.GRAY);
        }
        if (!SorcererGrade.meets(grade, vow.requiredGrade)) {
            return Component.literal("You are not strong enough to swear the " + vow.display + " (requires "
                    + DataManager.gradeLabel(vow.requiredGrade) + ").").withStyle(ChatFormatting.RED);
        }
        DataManager.setBindingVow(uuid, vow.id(), true);
        StatManager.apply(player);
        StatManager.sync(player);
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.SCULK_SOUL, player.getX(), player.getY() + 1, player.getZ(),
                    40, 0.4, 0.8, 0.4, 0.02);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 0.9f, 1.1f);
        }
        return Component.literal("You swear the " + vow.display + " binding vow. " + vow.description)
                .withStyle(vow.color);
    }

    // ------------------------------------------------------------------
    // Cursed-energy output / reinforcement (flow)
    // ------------------------------------------------------------------

    public static boolean isOutputActive(UUID uuid) {
        Long until = OUTPUT_UNTIL.get(uuid);
        return until != null && System.currentTimeMillis() < until;
    }

    public static void activateOutput(ServerPlayer player, long durationMs) {
        OUTPUT_UNTIL.put(player.getUUID(), System.currentTimeMillis() + durationMs);
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, (int) (durationMs / 50) + 40, 1, false, true));
    }

    /** Toggles sustained cursed-energy output/reinforcement (flow). Returns feedback; never throws. */
    public static Component toggleOutput(ServerPlayer player) {
        UUID id = player.getUUID();
        if (isOutputActive(id)) {
            OUTPUT_UNTIL.remove(id);
            StatManager.sync(player);
            return Component.literal("You let your cursed energy settle.").withStyle(ChatFormatting.GRAY);
        }
        if (StatManager.getMaxCursedEnergy(player) <= 0) {
            return Component.literal("You have no cursed energy to flow.").withStyle(ChatFormatting.RED);
        }
        activateOutput(player, 60_000L);
        StatManager.sync(player);
        return Component.literal("Cursed energy flows \u2014 your body is reinforced and your output sharpened.")
                .withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    // ------------------------------------------------------------------
    // Reverse Cursed Technique
    // ------------------------------------------------------------------

    public static boolean isReverseCursedActive(UUID uuid) {
        Long until = RCT_UNTIL.get(uuid);
        return until != null && System.currentTimeMillis() < until;
    }

    /** Toggles Reverse Cursed Technique. Returns feedback; never throws. */
    public static Component toggleReverseCursed(ServerPlayer player) {
        UUID id = player.getUUID();
        if (isReverseCursedActive(id)) {
            RCT_UNTIL.remove(id);
            StatManager.sync(player);
            return Component.literal("Reverse Cursed Technique fades.").withStyle(ChatFormatting.GRAY);
        }
        if (StatManager.getMaxCursedEnergy(player) <= 0) {
            return Component.literal("You channel no cursed energy to reverse.").withStyle(ChatFormatting.RED);
        }
        RCT_UNTIL.put(id, System.currentTimeMillis() + 30_000L);
        StatManager.sync(player);
        return Component.literal("Reverse Cursed Technique flows \u2014 your wounds knit as cursed energy burns.")
                .withStyle(ChatFormatting.GREEN);
    }

    // ------------------------------------------------------------------
    // Black Flash
    // ------------------------------------------------------------------

    /** Primes a Black Flash window: the next melee hit within the window is guaranteed to distort. */
    public static void primeBlackFlash(ServerPlayer player) {
        BLACK_FLASH_PRIMED.put(player.getUUID(), System.currentTimeMillis() + 6_000L);
        StatManager.sync(player);
    }

    public static boolean isInBlackFlashZone(UUID uuid) {
        Long until = BLACK_FLASH_ZONE.get(uuid);
        return until != null && System.currentTimeMillis() < until;
    }

    public static boolean isBlackFlashPrimed(UUID uuid) {
        Long until = BLACK_FLASH_PRIMED.get(uuid);
        return until != null && System.currentTimeMillis() < until;
    }

    /** Compact HUD bitfield for {@link com.political.net.StatSyncS2C}. */
    public static int packHudFlags(ServerPlayer player) {
        if (player == null) return 0;
        UUID id = player.getUUID();
        int flags = 0;
        if (isInBlackFlashZone(id)) flags |= com.political.net.StatSyncS2C.FLAG_BLACK_FLASH_ZONE;
        if (isBlackFlashPrimed(id)) flags |= com.political.net.StatSyncS2C.FLAG_BLACK_FLASH_PRIMED;
        if (isReverseCursedActive(id)) flags |= com.political.net.StatSyncS2C.FLAG_RCT;
        if (isOutputActive(id)) flags |= com.political.net.StatSyncS2C.FLAG_FLOW;
        if (hasSimpleDomain(id)) flags |= com.political.net.StatSyncS2C.FLAG_SIMPLE_DOMAIN;
        if (hasFallingBlossom(id)) flags |= com.political.net.StatSyncS2C.FLAG_FALLING_BLOSSOM;
        if (!activeVows(player.getStringUUID()).isEmpty()) {
            flags |= com.political.net.StatSyncS2C.FLAG_BINDING_VOW;
        }
        return flags;
    }

    /**
     * Resolves Black Flash on a melee hit. A primed window guarantees it; otherwise a sorcerer with
     * cursed energy has a small innate chance to flash on any strike. On success the attacker enters
     * "the zone" (amplified output + cursed-energy surge) and the strike lands distorted bonus damage.
     */
    public static boolean tryBlackFlash(ServerPlayer attacker, LivingEntity target, ServerLevel level) {
        UUID id = attacker.getUUID();
        PoliticalConfig cfg = PoliticalConfig.get();
        long now = System.currentTimeMillis();

        Long primedUntil = BLACK_FLASH_PRIMED.get(id);
        boolean primed = primedUntil != null && now < primedUntil;

        boolean proc = primed;
        if (!proc) {
            // Innate chance: only sorcerers actively channelling cursed energy can flash.
            int grade = SorcererGrade.of(attacker);
            if (grade > 0 && StatManager.getCursedEnergy(attacker) > 5) {
                double chance = cfg.jjkBlackFlashChance * (1.0 + grade * 0.15);
                proc = RNG.nextDouble() < chance;
            }
        }
        if (!proc) return false;

        BLACK_FLASH_PRIMED.remove(id);
        int grade = SorcererGrade.of(attacker);
        double bonus = (cfg.jjkBlackFlashBase + grade * 5.0) * outputMultiplier(attacker);
        target.invulnerableTime = 0;
        target.hurtServer(level, level.damageSources().playerAttack(attacker), (float) bonus);

        // "In the zone": amplified output, a cursed-energy surge, and a brief edge.
        BLACK_FLASH_ZONE.put(id, now + 8_000L);
        StatManager.addCursedEnergy(attacker, 12 + grade * 4);
        attacker.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 160, 0, false, true));

        level.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + target.getBbHeight() * 0.6,
                target.getZ(), 2, 0.1, 0.1, 0.1, 0.0);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1, target.getZ(),
                36, 0.4, 0.6, 0.4, 0.3);
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.4f, 0.6f);
        attacker.sendSystemMessage(Component.literal("\u26a1 BLACK FLASH!").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), true);
        StatManager.sync(attacker);
        return true;
    }

    // ------------------------------------------------------------------
    // Domain wards (Simple Domain / Falling Blossom Emotion)
    // ------------------------------------------------------------------

    public static void raiseSimpleDomain(ServerPlayer player, long durationMs) {
        SIMPLE_DOMAIN_UNTIL.put(player.getUUID(), System.currentTimeMillis() + durationMs);
        StatManager.sync(player);
    }

    public static void raiseFallingBlossom(ServerPlayer player, long durationMs) {
        FALLING_BLOSSOM_UNTIL.put(player.getUUID(), System.currentTimeMillis() + durationMs);
        StatManager.sync(player);
    }

    public static boolean hasSimpleDomain(UUID uuid) {
        Long until = SIMPLE_DOMAIN_UNTIL.get(uuid);
        return until != null && System.currentTimeMillis() < until;
    }

    public static boolean hasFallingBlossom(UUID uuid) {
        Long until = FALLING_BLOSSOM_UNTIL.get(uuid);
        return until != null && System.currentTimeMillis() < until;
    }

    /** Whether {@code victim} is currently warded against a domain's sure-hit (Simple Domain / Falling Blossom). */
    public static boolean wardsAgainstDomain(LivingEntity victim) {
        return victim instanceof ServerPlayer sp
                && (hasSimpleDomain(sp.getUUID()) || hasFallingBlossom(sp.getUUID()));
    }

    // ------------------------------------------------------------------
    // Per-tick upkeep
    // ------------------------------------------------------------------

    public static void tick(MinecraftServer server) {
        if (++tickCounter % 10 != 0) return; // twice per second
        PoliticalConfig cfg = PoliticalConfig.get();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();

            if (isReverseCursedActive(id)) {
                double cost = cfg.jjkReverseCursedCePerSecond * 0.5;
                if (player.getHealth() >= player.getMaxHealth()) {
                    // Fully healed — let it idle cheaply rather than waste cursed energy.
                } else if (StatManager.spendCursedEnergy(player, cost)) {
                    player.heal((float) (cfg.jjkReverseCursedHealPerSecond * 0.5));
                    if (player.level() instanceof ServerLevel level) {
                        level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.2, player.getZ(),
                                2, 0.3, 0.4, 0.3, 0.01);
                    }
                } else {
                    RCT_UNTIL.remove(id);
                    player.sendSystemMessage(Component.literal("Reverse Cursed Technique sputters out \u2014 no cursed energy.")
                            .withStyle(ChatFormatting.GRAY), true);
                    StatManager.sync(player);
                }
            }

            if (isOutputActive(id)) {
                double cost = cfg.jjkOutputCePerSecond * 0.5;
                if (StatManager.spendCursedEnergy(player, cost)) {
                    player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20, 1, false, false));
                } else {
                    OUTPUT_UNTIL.remove(id);
                    StatManager.sync(player);
                }
            }
        }
    }

    public static void clear(UUID uuid) {
        OUTPUT_UNTIL.remove(uuid);
        RCT_UNTIL.remove(uuid);
        BLACK_FLASH_PRIMED.remove(uuid);
        BLACK_FLASH_ZONE.remove(uuid);
        SIMPLE_DOMAIN_UNTIL.remove(uuid);
        FALLING_BLOSSOM_UNTIL.remove(uuid);
    }
}
