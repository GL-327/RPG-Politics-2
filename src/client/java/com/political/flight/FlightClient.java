package com.political.flight;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side driver for Viltrumite Flight (reimplementation of viltrumitecore's flight, mixin-free).
 *
 * <p>The server (via {@link FlightManager}) grants the vanilla {@code mayfly} ability whenever any
 * flight source is active. Entering flight uses the ordinary vanilla gesture (double-tap jump) or
 * the dedicated toggle key below. While flying, this tick handler overrides the local player's
 * velocity to produce momentum flight: holding the forward key ramps a 0..1 <b>throttle</b>, and the
 * player accelerates along their look direction up to a high cruising speed. Look up/down to climb or
 * dive at speed; release forward to coast; jump / sneak give fine vertical control while hovering.
 *
 * <p>The throttle is streamed to the server each tick so it can zero fall damage and ram entities at
 * boost speed. Creative flight is left entirely to vanilla (detected via {@code abilities.instabuild}).
 */
public final class FlightClient {

    private static KeyMapping flightToggleKey;

    private static float throttle = 0f;
    private static boolean wasFlying = false;
    private static boolean wasBoosting = false;

    private FlightClient() {}

    /** Client init. Integration must call this once from the client initializer. */
    public static void registerClient() {
        flightToggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.politicalserver.flight_toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(FlightClient::clientTick);
    }

    private static void clientTick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p == null) {
            throttle = 0f;
            wasFlying = false;
            wasBoosting = false;
            return;
        }

        Abilities ab = p.getAbilities();
        boolean creative = ab.instabuild; // vanilla creative flight stays untouched
        boolean eligible = ab.mayfly && !creative && !p.isSpectator();

        // Dedicated toggle: start/stop flying without the double-tap gesture.
        while (flightToggleKey.consumeClick()) {
            if (eligible) {
                ab.flying = !ab.flying;
                p.onUpdateAbilities();
            }
        }

        if (!eligible) {
            throttle = Math.max(0f, throttle - 0.1f);
            wasFlying = false;
            wasBoosting = false;
            if (p != null) p.noPhysics = false;
            return;
        }

        boolean flying = ab.flying;

        // Takeoff cue.
        if (flying && !wasFlying) {
            p.level().playLocalSound(p.getX(), p.getY(), p.getZ(),
                    SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.7f, 1.2f, false);
        }
        wasFlying = flying;

        if (flying) {
            applyMomentum(mc, p);
        } else {
            throttle = Math.max(0f, throttle - 0.1f);
            wasBoosting = false;
            p.noPhysics = false;
        }

        // Stream state to the server (drives fall immunity + ram knockback).
        if (flying || throttle > 0f) {
            ClientPlayNetworking.send(new FlightInputC2S(throttle, flying));
        }
    }

    private static void applyMomentum(Minecraft mc, LocalPlayer p) {
        Options o = mc.options;
        boolean fwd = o.keyUp.isDown();
        boolean back = o.keyDown.isDown();
        boolean left = o.keyLeft.isDown();
        boolean right = o.keyRight.isDown();
        boolean up = o.keyJump.isDown();
        boolean down = o.keyShift.isDown();

        // Throttle ramps while driving forward, bleeds off otherwise.
        if (fwd) throttle = Math.min(1f, throttle + 0.03f);
        else throttle = Math.max(0f, throttle - 0.06f);

        final double MIN_SPEED = 0.35;
        final double MAX_SPEED = 2.4;
        double ease = throttle * throttle; // ease-in: slow start, fast top end
        double speed = MIN_SPEED + (MAX_SPEED - MIN_SPEED) * ease;

        Vec3 look = p.getViewVector(1f);
        Vec3 sideways = new Vec3(-look.z, 0, look.x);
        if (sideways.lengthSqr() > 1.0e-4) sideways = sideways.normalize();
        double strafe = (right ? 1 : 0) - (left ? 1 : 0);

        Vec3 target;
        if (fwd) {
            // Fly where you look; pitch controls climb/dive at speed.
            target = look.scale(speed).add(sideways.scale(strafe * 0.3));
        } else {
            // Hover: gentle manual control, jump/sneak for vertical.
            Vec3 flat = new Vec3(look.x, 0, look.z);
            if (flat.lengthSqr() > 1.0e-4) flat = flat.normalize();
            double fb = back ? -1 : 0;
            double vy = (up ? 0.4 : 0) - (down ? 0.4 : 0);
            target = flat.scale(fb * 0.3).add(sideways.scale(strafe * 0.3));
            target = new Vec3(target.x, vy, target.z);
        }

        // Smooth acceleration toward the target velocity (momentum feel).
        Vec3 cur = p.getDeltaMovement();
        Vec3 next = cur.add(target.subtract(cur).scale(0.22));
        p.setDeltaMovement(next);
        p.fallDistance = 0;
        // Client-side boost collision bypass (mirrors viltrumitecore flight at high throttle).
        p.noPhysics = throttle >= FlightManager.BOOST_THRESHOLD;

        spawnFx(mc, p);
    }

    private static void spawnFx(Minecraft mc, LocalPlayer p) {
        boolean boosting = throttle >= FlightManager.BOOST_THRESHOLD;

        if (boosting && !wasBoosting) {
            p.level().playLocalSound(p.getX(), p.getY(), p.getZ(),
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.4f, 1.8f, false);
        }
        wasBoosting = boosting;

        if (mc.level == null) return;
        Vec3 back = p.getDeltaMovement().lengthSqr() > 1.0e-4
                ? p.getDeltaMovement().normalize().reverse() : p.getViewVector(1f).reverse();
        double bx = p.getX() + back.x * 0.7;
        double by = p.getY() + 0.9 + back.y * 0.7;
        double bz = p.getZ() + back.z * 0.7;

        if (throttle > 0.2f) {
            mc.level.addParticle(ParticleTypes.CLOUD, bx, by, bz, back.x * 0.1, back.y * 0.1, back.z * 0.1);
        }
        if (boosting) {
            mc.level.addParticle(ParticleTypes.END_ROD, bx, by, bz, back.x * 0.3, back.y * 0.3, back.z * 0.3);
        }
    }
}
