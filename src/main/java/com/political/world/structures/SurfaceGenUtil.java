package com.political.world.structures;

import com.political.world.Build;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.Random;

/**
 * Shared above-ground building primitives for the surface-structure generators: footprint
 * prep, ruined walls, tents, graves, campfires, props and the post-op registrations that turn
 * placed marker blocks into loot/spawners/mobs after the structure streams in. Built entirely
 * on the mixin-free {@link Build} placement layer used by the settlement & dungeon systems.
 */
public final class SurfaceGenUtil {

    private SurfaceGenUtil() {}

    /** Median surface height across a footprint (delegates to the shared {@link Build} helper). */
    public static int groundY(ServerLevel level, int x0, int z0, int x1, int z1) {
        return Build.medianGround(level, x0, z0, x1, z1);
    }

    /** Rests a building footprint on the terrain at {@code floorY} with a foundation skirt + cleared air. */
    public static void prep(ServerLevel level, int x0, int z0, int x1, int z1, int floorY, int clearH, Block foundation) {
        Build.prepFootprint(level, x0, z0, x1, z1, floorY, clearH, foundation);
    }

    // ------------------------------------------------------------------
    // Walls & ruins
    // ------------------------------------------------------------------

    /** Solid walls of a rectangle. */
    public static void walls(ServerLevel level, int x0, int z0, int x1, int z1, int y0, int h, Block block) {
        Build.walls(level, x0, z0, x1, z1, y0, h, block);
    }

    /** Walls with random missing blocks + creeping decay near the top — a weathered ruin look. */
    public static void ruinedWalls(ServerLevel level, int x0, int z0, int x1, int z1, int y0, int h,
                                   Block block, Block decay, Random rng) {
        int minX = Math.min(x0, x1), maxX = Math.max(x0, x1);
        int minZ = Math.min(z0, z1), maxZ = Math.max(z0, z1);
        for (int y = y0; y < y0 + h; y++) {
            int frac = (y - y0);
            for (int x = minX; x <= maxX; x++) {
                placeRuin(level, x, y, minZ, frac, h, block, decay, rng);
                placeRuin(level, x, y, maxZ, frac, h, block, decay, rng);
            }
            for (int z = minZ; z <= maxZ; z++) {
                placeRuin(level, minX, y, z, frac, h, block, decay, rng);
                placeRuin(level, maxX, y, z, frac, h, block, decay, rng);
            }
        }
    }

    private static void placeRuin(ServerLevel level, int x, int y, int z, int frac, int h,
                                  Block block, Block decay, Random rng) {
        // Higher courses are more likely to have crumbled away.
        float gap = 0.08f + 0.5f * frac / Math.max(1, h);
        if (rng.nextFloat() < gap) return;
        Build.set(level, x, y, z, rng.nextInt(4) == 0 ? decay : block);
    }

    /** A solid vertical pillar. */
    public static void pillar(ServerLevel level, int x, int baseY, int z, int h, Block block) {
        Build.cube(level, x, baseY, z, x, baseY + h - 1, z, block);
    }

    // ------------------------------------------------------------------
    // Camp / battlefield props
    // ------------------------------------------------------------------

    /** A small canvas tent (wool roof on log corners) sized {@code half} from a centre. */
    public static void tent(ServerLevel level, int cx, int baseY, int cz, int half, Block cloth, Random rng) {
        int x0 = cx - half, x1 = cx + half, z0 = cz - half, z1 = cz + half;
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                Build.set(level, x, baseY, z, StructureBlocks.CAMP_GROUND);
            }
        }
        // Corner posts
        Build.cube(level, x0, baseY, z0, x0, baseY + 2, z0, Blocks.OAK_LOG);
        Build.cube(level, x1, baseY, z0, x1, baseY + 2, z0, Blocks.OAK_LOG);
        Build.cube(level, x0, baseY, z1, x0, baseY + 2, z1, Blocks.OAK_LOG);
        Build.cube(level, x1, baseY, z1, x1, baseY + 2, z1, Blocks.OAK_LOG);
        // Ridge pole + sloped cloth roof
        int ridgeY = baseY + 4;
        Build.cube(level, x0, ridgeY, cz, x1, ridgeY, cz, Blocks.OAK_LOG);
        for (int x = x0; x <= x1; x++) {
            for (int dz = 0; dz <= half; dz++) {
                int ry = ridgeY - dz;
                if (ry <= baseY + 2) break;
                Build.set(level, x, ry, cz - dz, cloth);
                Build.set(level, x, ry, cz + dz, cloth);
            }
        }
    }

    /** A lit campfire surrounded by a stone ring. */
    public static void campfire(ServerLevel level, int x, int y, int z) {
        Build.set(level, x, y, z, Blocks.CAMPFIRE);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            Build.set(level, x + d.getStepX(), y, z + d.getStepZ(), Blocks.COBBLESTONE);
        }
    }

    /** A grave: a small dirt mound with a fence-post headstone and optional cobweb. */
    public static void grave(ServerLevel level, int x, int baseY, int z, Random rng) {
        Build.set(level, x, baseY, z, Blocks.PODZOL);
        Build.set(level, x, baseY + 1, z - 1, Blocks.COBBLESTONE_WALL);
        if (rng.nextInt(3) == 0) Build.set(level, x, baseY + 2, z - 1, Blocks.COBWEB);
        if (rng.nextInt(4) == 0) Build.set(level, x, baseY + 1, z, Blocks.SOUL_TORCH);
    }

    /** A statue/monument column with an accent crown — used for heroes, obelisks, civic ruins. */
    public static void statue(ServerLevel level, int x, int baseY, int z, int h, Block body, Block crown) {
        Build.cube(level, x, baseY, z, x, baseY + h - 1, z, body);
        Build.set(level, x, baseY + h, z, crown);
    }

    /** A faction banner on a post (war banner / royal banner styling). */
    public static void bannerPost(ServerLevel level, int x, int baseY, int z, int h, Block banner) {
        Build.cube(level, x, baseY, z, x, baseY + h - 1, z, Blocks.OAK_FENCE);
        Build.set(level, x, baseY + h, z, banner);
    }

    // ------------------------------------------------------------------
    // Doors & openings
    // ------------------------------------------------------------------

    public static void doorway(ServerLevel level, int x, int y, int z, Block door, boolean placeDoor) {
        Build.set(level, x, y, z, Blocks.AIR);
        Build.set(level, x, y + 1, z, Blocks.AIR);
        if (placeDoor) {
            Build.set(level, x, y, z, door.defaultBlockState()
                    .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
            Build.set(level, x, y + 1, z, door.defaultBlockState()
                    .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
        }
    }

    // ------------------------------------------------------------------
    // Post-op registrations (loot / population)
    // ------------------------------------------------------------------

    /** Places a chest (facing south) and queues it to be filled from {@code lootTable}. */
    public static void chest(StructurePlan plan, ServerLevel level, int x, int y, int z, String lootTable) {
        BlockState chest = Blocks.CHEST.defaultBlockState();
        if (chest.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            chest = chest.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
        }
        Build.set(level, x, y, z, chest);
        plan.postOps.add(StructurePlan.PostOp.chest(x, y, z, lootTable));
    }

    /** Places a mob spawner block and queues it to be configured with {@code mobId}. */
    public static void spawner(StructurePlan plan, ServerLevel level, int x, int y, int z, String mobId) {
        Build.set(level, x, y, z, Blocks.SPAWNER.defaultBlockState());
        plan.postOps.add(StructurePlan.PostOp.spawner(x, y, z, mobId));
    }

    public static void mob(StructurePlan plan, int x, int y, int z, String mobId) {
        if (mobId == null) return;
        plan.postOps.add(StructurePlan.PostOp.mob(x, y, z, mobId));
    }

    public static void villager(StructurePlan plan, int x, int y, int z) {
        plan.postOps.add(StructurePlan.PostOp.villager(x, y, z));
    }

    public static void trader(StructurePlan plan, int x, int y, int z) {
        plan.postOps.add(StructurePlan.PostOp.trader(x, y, z));
    }

    public static void cursedSpirit(StructurePlan plan, int x, int y, int z, int grade) {
        plan.postOps.add(StructurePlan.PostOp.cursedSpirit(x, y, z, grade));
    }
}
