package com.political.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.Heightmap;

/** Low-level block-placement primitives for the procedural settlement generators. */
public final class Build {

    private static final int FLAGS = 2; // UPDATE_CLIENTS, no neighbour updates (fast)

    // When non-null, placements are captured into a buffer instead of placed immediately
    // (single-threaded server-side generation, so a plain static field is safe).
    private static BuildBuffer deferred = null;

    private Build() {}

    /** Begin capturing placements into {@code buffer} instead of writing to the world. */
    public static void beginDeferred(BuildBuffer buffer) {
        deferred = buffer;
    }

    public static void endDeferred() {
        deferred = null;
    }

    public static int groundY(ServerLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
    }

    public static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        if (deferred != null) {
            deferred.add(x, y, z, state);
            return;
        }
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

    /** Fills a {@code thickness}-wide border ring of a rectangle at a single y (e.g. a rampart walkway). */
    public static void borderRing(ServerLevel level, int x0, int z0, int x1, int z1, int y, int thickness, Block block) {
        int minX = Math.min(x0, x1), maxX = Math.max(x0, x1);
        int minZ = Math.min(z0, z1), maxZ = Math.max(z0, z1);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int edge = Math.min(Math.min(x - minX, maxX - x), Math.min(z - minZ, maxZ - z));
                if (edge < thickness) set(level, x, y, z, block);
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

    public static void stair(ServerLevel level, int x, int y, int z, Block stairBlock, Direction facing) {
        BlockState s = stairBlock.defaultBlockState();
        if (s.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            s = s.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        }
        if (s.hasProperty(BlockStateProperties.HALF)) {
            s = s.setValue(BlockStateProperties.HALF, Half.BOTTOM);
        }
        set(level, x, y, z, s);
    }

    /**
     * A pitched gable roof over a rectangle: slopes up along X from both long edges to a
     * central ridge, extruded along Z. {@code stairBlock} forms the slopes, {@code ridge}
     * the cap.
     */
    public static void gableRoof(ServerLevel level, int x0, int z0, int x1, int z1, int baseRoofY,
                                 Block stairBlock, Block ridge) {
        int minX = Math.min(x0, x1), maxX = Math.max(x0, x1);
        int minZ = Math.min(z0, z1), maxZ = Math.max(z0, z1);
        int i = 0;
        while (true) {
            int leftX = minX + i;
            int rightX = maxX - i;
            int y = baseRoofY + i;
            if (leftX > rightX) break;
            if (leftX == rightX) {
                for (int z = minZ; z <= maxZ; z++) set(level, leftX, y, z, ridge);
                break;
            }
            for (int z = minZ; z <= maxZ; z++) {
                stair(level, leftX, y, z, stairBlock, Direction.EAST);
                stair(level, rightX, y, z, stairBlock, Direction.WEST);
            }
            i++;
        }
    }

    /** Scatters a few flowers / grass tufts over a grass rectangle for gardens. */
    public static void garden(ServerLevel level, int x0, int z0, int x1, int z1, int y, java.util.Random rng) {
        Block[] flora = { Blocks.POPPY, Blocks.DANDELION, Blocks.CORNFLOWER, Blocks.OXEYE_DAISY,
                Blocks.AZURE_BLUET, Blocks.SHORT_GRASS };
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
            for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
                if (rng.nextInt(3) != 0) continue;
                set(level, x, y, z, flora[rng.nextInt(flora.length)]);
            }
        }
    }

    /** A small decorative tree (log trunk + leaf canopy). */
    public static void tree(ServerLevel level, int x, int y, int z) {
        int h = 4;
        for (int i = 0; i < h; i++) set(level, x, y + i, z, Blocks.OAK_LOG);
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++)
                for (int dy = h - 2; dy <= h; dy++) {
                    if (Math.abs(dx) + Math.abs(dz) + Math.max(0, dy - h + 1) <= 3) {
                        if (dx == 0 && dz == 0 && dy < h) continue;
                        set(level, x + dx, y + dy, z + dz, Blocks.OAK_LEAVES);
                    }
                }
        set(level, x, y + h, z, Blocks.OAK_LEAVES);
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
