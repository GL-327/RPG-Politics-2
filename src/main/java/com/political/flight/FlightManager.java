package com.political.flight;

import com.political.politics.DataManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The single authority for <b>all non-creative flight</b> in the mod (Viltrumite Flight).
 *
 * <p>This reimplements the flight mechanic from <i>viltrumitecore</i> / <i>viltrumiteflight</i>
 * (Fabric 1.20.1) in mixin-free 26.2: a momentum / throttle based flight where holding the
 * forward key while airborne ramps a 0..1 throttle, and the player accelerates in their look
 * direction up to a high cruising speed (far faster than vanilla creative flight). The original
 * mod tracked this through a {@code ViltrumiteFlightPlayer.getFlightThrottle()} float and, once
 * throttle crossed {@code 0.6}, cancelled the player's flight collision so they could barrel
 * through. We mirror that: throttle, the {@code 0.6} "boost" threshold, the look-driven steering,
 * and a high-speed ramming knockback.
 *
 * <p><b>Design (mixin-free):</b> the server grants the vanilla {@code abilities.mayfly} flag so the
 * client is allowed to fly; the heavy lifting (acceleration, steering, particles, sounds) happens
 * client-side in {@link FlightClient} where the local player is position-authoritative. The client
 * streams its {@link FlightInputC2S} throttle to the server, which applies the shared, authoritative
 * effects (fall-damage immunity + ram knockback). Creative-mode flight is never touched here.
 *
 * <p>Flight is reason-counted: several systems can request flight for the same player (a worn
 * armour ability, an active power, a known Compound&nbsp;V flight power). The player can fly while
 * at least one reason is active; when the last reason clears, {@code mayfly} is revoked.
 */
public final class FlightManager {

    /** Throttle at/above which flight "boosts": collision is ignored and ramming kicks in. */
    public static final float BOOST_THRESHOLD = 0.6f;

    public static final String REASON_TIMED = "timed";       // PowerManager FLIGHT / STAR_POWER (timed)
    public static final String REASON_POWER = "power";        // knowing a Compound V flight power (passive)
    public static final String REASON_ARMOR = "armor";        // worn gear with the Flight ability

    private static final Map<UUID, Set<String>> REASONS = new HashMap<>();
    private static final Map<UUID, Long> TIMED_UNTIL = new HashMap<>();
    private static final Set<UUID> GRANTED = new HashSet<>();

    // Latest client-reported flight state (server effects only).
    private static final Map<UUID, Float> THROTTLE = new HashMap<>();
    private static final Map<UUID, Long> THROTTLE_AT = new HashMap<>();

    private FlightManager() {}

    /** Common init. Integration must call this once during mod setup (e.g. in the main initializer). */
    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(FlightInputC2S.TYPE, FlightInputC2S.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FlightInputC2S.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> onClientInput(player, payload));
        });
        ServerTickEvents.END_SERVER_TICK.register(FlightManager::serverTick);
    }

    // ---------------- Public grant API (used by every flight source) ----------------

    /** Grant flight to the player for the given reason until {@link #disable} is called. */
    public static void enable(ServerPlayer player, String reason) {
        if (player == null) return;
        REASONS.computeIfAbsent(player.getUUID(), k -> new HashSet<>()).add(reason);
        applyAbility(player);
    }

    /** Grant flight for {@code durationMs}; refreshes the timer if already flying (timed reason). */
    public static void enableTimed(ServerPlayer player, long durationMs) {
        if (player == null || player.isCreative() || player.isSpectator()) return;
        TIMED_UNTIL.put(player.getUUID(), System.currentTimeMillis() + durationMs);
        enable(player, REASON_TIMED);
    }

    /** Remove a single flight reason; revokes {@code mayfly} once no reasons remain. */
    public static void disable(ServerPlayer player, String reason) {
        if (player == null) return;
        Set<String> set = REASONS.get(player.getUUID());
        if (set != null) {
            set.remove(reason);
            if (REASON_TIMED.equals(reason)) TIMED_UNTIL.remove(player.getUUID());
            if (set.isEmpty()) REASONS.remove(player.getUUID());
        }
        applyAbility(player);
    }

    /** Convenience for the per-tick armour check in {@link com.political.combat.AbilityEngine}. */
    public static void setArmorFlight(ServerPlayer player, boolean shouldFly) {
        if (shouldFly) enable(player, REASON_ARMOR);
        else disable(player, REASON_ARMOR);
    }

    public static boolean wantsFlight(ServerPlayer player) {
        if (!com.political.config.PoliticalConfig.get().flightEnabled) return false;
        Set<String> set = REASONS.get(player.getUUID());
        if (set == null || set.isEmpty()) return false;
        if (set.contains(REASON_TIMED)) {
            Long until = TIMED_UNTIL.get(player.getUUID());
            if (until != null && System.currentTimeMillis() >= until) return set.size() > 1;
        }
        return true;
    }

    public static void onPlayerRemoved(UUID uuid) {
        REASONS.remove(uuid);
        TIMED_UNTIL.remove(uuid);
        GRANTED.remove(uuid);
        THROTTLE.remove(uuid);
        THROTTLE_AT.remove(uuid);
    }

    // ---------------- Server-side per-tick maintenance + effects ----------------

    private static void serverTick(MinecraftServer server) {
        long now = System.currentTimeMillis();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();

            // Creative / spectator: leave vanilla flight completely alone.
            if (player.isCreative() || player.isSpectator()) {
                GRANTED.remove(id);
                continue;
            }

            // Expire timed flight.
            Long until = TIMED_UNTIL.get(id);
            if (until != null && now >= until) {
                TIMED_UNTIL.remove(id);
                Set<String> set = REASONS.get(id);
                if (set != null) {
                    set.remove(REASON_TIMED);
                    if (set.isEmpty()) REASONS.remove(id);
                }
            }

            // Passive: knowing a Compound V flight power keeps viltrumite flight permanently available.
            String suid = player.getStringUUID();
            boolean knowsFlightPower = DataManager.hasPower(suid, "flight")
                    || DataManager.hasPower(suid, "star_power")
                    || DataManager.hasPower(suid, "icarus_dive");
            if (knowsFlightPower) {
                REASONS.computeIfAbsent(id, k -> new HashSet<>()).add(REASON_POWER);
            } else {
                Set<String> set = REASONS.get(id);
                if (set != null) {
                    set.remove(REASON_POWER);
                    if (set.isEmpty()) REASONS.remove(id);
                }
            }

            applyAbility(player);

            // High-speed flight effects, gated on the client's reported throttle.
            if (GRANTED.contains(id) && player.getAbilities().flying) {
                Float thr = recentThrottle(id, now);
                if (thr != null && thr > 0.05f) {
                    player.fallDistance = 0;
                    // Viltrumite boost: phase through collision like viltrumitecore's EntityCollisionMixin.
                    player.noPhysics = thr >= BOOST_THRESHOLD;
                    if (thr >= BOOST_THRESHOLD && player.level() instanceof ServerLevel level) {
                        ram(player, level, thr);
                    }
                } else {
                    player.noPhysics = false;
                }
            } else {
                player.noPhysics = false;
            }
        }
    }

    /** Brings {@code mayfly} in line with whether the player currently wants flight. */
    private static void applyAbility(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return;
        UUID id = player.getUUID();
        boolean want = wantsFlight(player);
        if (want) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
            GRANTED.add(id);
        } else if (GRANTED.remove(id)) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    private static Float recentThrottle(UUID id, long now) {
        Long at = THROTTLE_AT.get(id);
        if (at == null || now - at > 500L) return null; // stale (client stopped reporting)
        return THROTTLE.get(id);
    }

    private static void onClientInput(ServerPlayer player, FlightInputC2S payload) {
        if (player == null) return;
        UUID id = player.getUUID();
        // Only meaningful for players we actually granted flight to (anti-spoof).
        if (!GRANTED.contains(id) || player.isCreative() || player.isSpectator()) {
            THROTTLE.remove(id);
            return;
        }
        THROTTLE.put(id, Math.max(0f, Math.min(1f, payload.throttle())));
        THROTTLE_AT.put(id, System.currentTimeMillis());
    }

    /** Barrel through anything in the flight path at boost speed: knock it aside and bruise it. */
    private static void ram(ServerPlayer player, ServerLevel level, float throttle) {
        Vec3 vel = player.getDeltaMovement();
        if (vel.lengthSqr() < 0.04) return;
        Vec3 dir = vel.normalize();
        AABB box = player.getBoundingBox().expandTowards(dir.scale(2.0)).inflate(0.6);
        float damage = 4.0f + 6.0f * throttle;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, x -> x != player && x.isAlive())) {
            e.hurtServer(level, level.damageSources().playerAttack(player), damage);
            Vec3 push = dir.scale(1.2 + throttle).add(0, 0.25, 0);
            e.push(push.x, push.y, push.z);
            e.hurtMarked = true;
        }
        if (player.tickCount % 2 == 0) {
            Vec3 behind = player.position().subtract(dir.scale(0.8));
            level.sendParticles(ParticleTypes.CLOUD, behind.x, behind.y + 0.9, behind.z, 4, 0.2, 0.2, 0.2, 0.01);
        }
        if (throttle >= 0.95f && player.tickCount % 12 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.6f, 1.6f);
        }
    }
}
