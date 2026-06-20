package com.political.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Low-level block-placement primitives for the procedural settlement generators. */
public final class Build {

    private static final int FLAGS = 2; // UPDATE_CLIENTS, no neighbour updates (fast)

    private Build() {}

    public static int groundY(ServerLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
    }

    public static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, FLAGS);
    }

    public static void set(ServerLevel level, int x, int y, int z, Block block) {
        set(level, x, y, z, block.defaultBlockState());
    }

    /** Solid filled box (inclusive bounds). */
    public static void cube(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1, Block block) {
        BlockState s = block.defaultBlockState();
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++)
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++)
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++)
                    set(level, x, y, z, s);
    }

    /** Clears a volume to air (used to carve out terrain/trees above a footprint). */
    public static void clear(ServerLevel level, int x0, int y0, int z0, int x1, int y1, int z1) {
        cube(level, x0, y0, z0, x1, y1, z1, Blocks.AIR);
    }

    /** Flat plane (single y) over an x/z rectangle. */
    public static void floor(ServerLevel level, int x0, int z0, int x1, int z1, int y, Block block) {
        cube(level, x0, y, z0, x1, y, z1, block);
    }

    /** Four vertical walls of a rectangle (no floor/ceiling), height tall starting at y0. */
    public static void walls(ServerLevel level, int x0, int z0, int x1, int z1, int y0, int height, Block block) {
        int minX = Math.min(x0, x1), maxX = Math.max(x0, x1);
        int minZ = Math.min(z0, z1), maxZ = Math.max(z0, z1);
        for (int y = y0; y < y0 + height; y++) {
            for (int x = minX; x <= maxX; x++) {
                set(level, x, y, minZ, block);
                set(level, x, y, maxZ, block);
            }
            for (int z = minZ; z <= maxZ; z++) {
                set(level, minX, y, z, block);
                set(level, maxX, y, z, block);
            }
        }
    }

    /** A crenellated parapet (alternating merlons) along the top edge of a rectangle. */
    public static void crenellate(ServerLevel level, int x0, int z0, int x1, int z1, int y, Block block) {
        int minX = Math.min(x0, x1), maxX = Math.max(x0, x1);
        int minZ = Math.min(z0, z1), maxZ = Math.max(z0, z1);
        for (int x = minX; x <= maxX; x++) {
            if (((x - minX) & 1) == 0) { set(level, x, y, minZ, block); set(level, x, y, maxZ, block); }
        }
        for (int z = minZ; z <= maxZ; z++) {
            if (((z - minZ) & 1) == 0) { set(level, minX, y, z, block); set(level, maxX, y, z, block); }
        }
    }

    /** Solid support pillar from the surface up to (and filling) the platform base. */
    public static void foundation(ServerLevel level, int x0, int z0, int x1, int z1, int baseY, Block block) {
        int minX = Math.min(x0, x1), maxX = Math.max(x0, x1);
        int minZ = Math.min(z0, z1), maxZ = Math.max(z0, z1);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int g = groundY(level, x, z);
                int from = Math.min(g, baseY - 1);
                for (int y = from; y <= baseY - 1; y++) set(level, x, y, z, block);
            }
        }
    }
}
