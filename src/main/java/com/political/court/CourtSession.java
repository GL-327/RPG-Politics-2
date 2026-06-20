package com.political.court;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/** A single active Court Domain instance. */
public class CourtSession {
    public final UUID judge;
    public final UUID accused;
    public final ServerLevel level;
    public final Vec3 center;
    public final double radius;
    public final double height;
    public final long endTimeMillis;

    public CourtSession(UUID judge, UUID accused, ServerLevel level, Vec3 center,
                        double radius, double height, long endTimeMillis) {
        this.judge = judge;
        this.accused = accused;
        this.level = level;
        this.center = center;
        this.radius = radius;
        this.height = height;
        this.endTimeMillis = endTimeMillis;
    }

    public boolean contains(Vec3 pos) {
        double dx = pos.x - center.x;
        double dz = pos.z - center.z;
        double dy = pos.y - center.y;
        return (dx * dx + dz * dz) <= radius * radius && dy >= -2.0 && dy <= height;
    }

    public int remainingSeconds() {
        return Math.max(0, (int) ((endTimeMillis - System.currentTimeMillis()) / 1000));
    }
}
