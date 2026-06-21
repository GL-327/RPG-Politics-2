package com.political.world.structures;

/** A placed surface-structure instance, tracked for locate/list/scatter bookkeeping. */
public final class StructureSite {

    public final String dimension;
    public final int x;
    public final int y;
    public final int z;
    public final StructureType type;
    public final long placedAt;

    public StructureSite(String dimension, int x, int y, int z, StructureType type, long placedAt) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.placedAt = placedAt;
    }

    public double distSq(double px, double pz) {
        double dx = x - px;
        double dz = z - pz;
        return dx * dx + dz * dz;
    }
}
