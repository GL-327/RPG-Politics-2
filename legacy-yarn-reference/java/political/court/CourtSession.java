package com.political.court;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

/** A single active Court Domain instance. */
public class CourtSession {
    public final UUID judge;
    public final UUID accused;
    public final ServerWorld world;
    public final Vec3d center;
    public final double radius;
    public final double height;
    public final long endTimeMillis;

    public CourtSession(UUID judge, UUID accused, ServerWorld world, Vec3d center,
                        double radius, double height, long endTimeMillis) {
        this.judge = judge;
        this.accused = accused;
        this.world = world;
        this.center = center;
        this.radius = radius;
        this.height = height;
        this.endTimeMillis = endTimeMillis;
    }

    public boolean contains(Vec3d pos) {
        double dx = pos.x - center.x;
        double dz = pos.z - center.z;
        double dy = pos.y - center.y;
        return (dx * dx + dz * dz) <= radius * radius && dy >= -2.0 && dy <= height;
    }

    public int remainingSeconds() {
        return Math.max(0, (int) ((endTimeMillis - System.currentTimeMillis()) / 1000));
    }
}
