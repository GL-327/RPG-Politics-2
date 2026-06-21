package com.political.client;

import com.political.vfx.VfxElement;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side domain boundary feedback: element-tinted particle rings at synced domain perimeters.
 * Uses the same mixin-free client-tick approach as {@code FlightClient} because MC 26.2 moved
 * world-render callbacks to a new package with a different buffer pipeline.
 */
public final class DomainOverlayRenderer {

    private static boolean registered;
    private static int tick;

    private DomainOverlayRenderer() {}

    public static void register() {
        if (registered) return;
        registered = true;
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) return;
            if (DomainClientState.ACTIVE.isEmpty()) return;
            if (++tick % 2 != 0) return;

            Vec3 cam = client.player.getEyePosition();
            for (DomainClientState.ActiveDomain d : DomainClientState.ACTIVE.values()) {
                Vec3 center = new Vec3(d.centerX(), d.centerY(), d.centerZ());
                if (cam.distanceToSqr(center) > (d.radius() + 24) * (d.radius() + 24)) continue;
                spawnRing(client, d, center);
            }
        });
    }

    private static void spawnRing(Minecraft client, DomainClientState.ActiveDomain d, Vec3 center) {
        ParticleOptions core = particleFor(d.elementOrdinal());
        ParticleOptions trail = ParticleTypes.END_ROD;
        double phase = (client.level.getGameTime() + (long) (d.centerX() * 31 + d.centerZ())) * 0.07;
        int segments = Math.max(16, (int) (d.radius() * 3));
        double y = center.y + 0.15;

        for (int i = 0; i < segments; i++) {
            double ang = (Math.PI * 2 * i) / segments + phase;
            double x = center.x + Math.cos(ang) * d.radius();
            double z = center.z + Math.sin(ang) * d.radius();
            client.level.addParticle(core, x, y, z, 0, 0.01, 0);
            if (i % 4 == 0) {
                client.level.addParticle(trail, x, y + d.radius() * 0.35, z, 0, 0.02, 0);
            }
        }

        // Vertical wisps at the dome apex.
        client.level.addParticle(core, center.x, center.y + d.radius() * 0.5, center.z, 0, 0.03, 0);
    }

    private static ParticleOptions particleFor(int ordinal) {
        VfxElement[] values = VfxElement.values();
        if (ordinal < 0 || ordinal >= values.length) return ParticleTypes.WITCH;
        return switch (values[ordinal]) {
            case FIRE -> ParticleTypes.FLAME;
            case FROST -> ParticleTypes.SNOWFLAKE;
            case VOID -> ParticleTypes.REVERSE_PORTAL;
            case LIGHTNING -> ParticleTypes.ELECTRIC_SPARK;
            case NATURE -> ParticleTypes.SPORE_BLOSSOM_AIR;
            case HOLY -> ParticleTypes.END_ROD;
            case BLOOD -> ParticleTypes.CRIMSON_SPORE;
            case ARCANE -> ParticleTypes.ENCHANT;
        };
    }

    /** Whether the local player is standing inside their active domain (for HUD tint). */
    public static boolean isLocalPlayerInsideDomain(Minecraft mc) {
        DomainClientState.ActiveDomain local = DomainClientState.localActive;
        if (local == null || mc.player == null) return false;
        Vec3 p = mc.player.position();
        Vec3 c = new Vec3(local.centerX(), local.centerY(), local.centerZ());
        return p.distanceToSqr(c) <= local.radius() * local.radius();
    }
}
