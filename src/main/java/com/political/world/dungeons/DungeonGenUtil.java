package com.political.world.dungeons;

import com.political.world.Build;
import com.political.world.BuildBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Random;

/** Shared room/corridor/trap primitives for all dungeon archetypes. */
public final class DungeonGenUtil {

    private DungeonGenUtil() {}

    public static int findFloorY(ServerLevel level, int x, int z, DungeonType type, Random rng) {
        int surface = Build.groundY(level, x, z);
        if (!type.underground) {
            return Math.max(level.getSeaLevel() - 2, surface - 1);
        }
        int target = surface - 10 - rng.nextInt(18);
        target = Math.max(level.getMinY() + 8, Math.min(target, surface - 4));
        // Snap to solid stone column
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos(x, target, z);
        for (int y = target; y > level.getMinY() + 4; y--) {
            m.setY(y);
            if (level.getBlockState(m).isSolidRender()) return y + 1;
        }
        return Math.max(level.getMinY() + 8, target);
    }

    /** Hidden surface entrance shaft leading to the dungeon lobby. */
    public static void carveEntrance(DungeonPlan plan, ServerLevel level, int x, int floorY, int z,
                                     Block wall, Random rng) {
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        for (int y = surface; y >= floorY + 3; y--) {
            Build.set(level, x, y, z, Blocks.AIR);
            if (y > floorY + 3) {
                Build.set(level, x + 1, y, z, Blocks.AIR);
                Build.set(level, x - 1, y, z, Blocks.AIR);
                Build.set(level, x, y, z + 1, Blocks.AIR);
                Build.set(level, x, y, z - 1, Blocks.AIR);
            }
        }
        // Collapsed stone cap — looks natural until dug into
        Build.set(level, x, surface, z, Blocks.MOSSY_COBBLESTONE);
        if (rng.nextBoolean()) Build.set(level, x, surface, z + 1, Blocks.STONE);
    }

    /** Hollow rectangular room with floor, walls, ceiling, and torch/lantern corners. */
    public static void room(DungeonPlan plan, ServerLevel level, int x0, int y, int z0,
                            int w, int h, int d, DungeonType type) {
        Block wall = type.wallBlock();
        Block floor = type.floorBlock();
        Block accent = type.accentBlock();
        int x1 = x0 + w - 1;
        int z1 = z0 + d - 1;
        Build.clear(level, x0, y, z0, x1, y + h, z1);
        Build.floor(level, x0, z0, x1, z1, y, floor);
        Build.cube(level, x0, y + h, z0, x1, y + h, z1, wall);
        Build.walls(level, x0, z0, x1, z1, y + 1, h - 1, wall);
        // Accent pillars
        Build.set(level, x0 + 1, y + 1, z0 + 1, accent);
        Build.set(level, x1 - 1, y + 1, z1 - 1, accent);
        if (type.cursed) {
            Build.set(level, (x0 + x1) / 2, y + 1, (z0 + z1) / 2, Blocks.SOUL_FIRE);
        }
    }

    /** 3-wide corridor connecting two points at the same Y. */
    public static void corridor(DungeonPlan plan, ServerLevel level, int x0, int y, int z0,
                                int x1, int z1, DungeonType type) {
        Block floor = type.floorBlock();
        Block wall = type.wallBlock();
        int stepX = Integer.compare(x1, x0);
        int stepZ = Integer.compare(z1, z0);
        int x = x0;
        int z = z0;
        while (x != x1 || z != z1) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Build.clear(level, x + dx, y + 1, z + dz, x + dx, y + 3, z + dz);
                    Build.set(level, x + dx, y, z + dz, floor);
                    if (Math.abs(dx) == 1 && Math.abs(dz) == 1) continue;
                    if (dx == 0 && dz == 0) continue;
                    Build.set(level, x + dx, y + 1, z + dz, wall);
                    Build.set(level, x + dx, y + 4, z + dz, wall);
                }
            }
            if (x != x1) x += stepX;
            else if (z != z1) z += stepZ;
        }
    }

    public static void chest(DungeonPlan plan, ServerLevel level, int x, int y, int z, DungeonTier tier) {
        Build.set(level, x, y, z, Blocks.CHEST.defaultBlockState());
        plan.postOps.add(switch (tier) {
            case COMMON -> DungeonPlan.PostOp.of(DungeonPlan.PostKind.CHEST_COMMON, x, y, z);
            case UNCOMMON -> DungeonPlan.PostOp.of(DungeonPlan.PostKind.CHEST_UNCOMMON, x, y, z);
            case RARE -> DungeonPlan.PostOp.of(DungeonPlan.PostKind.CHEST_RARE, x, y, z);
            case EPIC -> DungeonPlan.PostOp.of(DungeonPlan.PostKind.CHEST_EPIC, x, y, z);
        });
    }

    public static void bossChest(DungeonPlan plan, ServerLevel level, int x, int y, int z) {
        Build.set(level, x, y, z, Blocks.CHEST.defaultBlockState());
        plan.postOps.add(DungeonPlan.PostOp.of(DungeonPlan.PostKind.CHEST_BOSS, x, y, z));
    }

    public static void spawnerRoom(DungeonPlan plan, ServerLevel level, int x, int y, int z,
                                   String mobId, Random rng) {
        Build.set(level, x, y, z, Blocks.SPAWNER.defaultBlockState());
        plan.postOps.add(DungeonPlan.PostOp.spawner(x, y, z, mobId));
    }

    public static void arrowTrap(DungeonPlan plan, ServerLevel level, int x, int y, int z, Direction facing) {
        Build.set(level, x, y, z, Blocks.DISPENSER.defaultBlockState()
                .setValue(BlockStateProperties.FACING, facing));
        Build.set(level, x, y - 1, z, Blocks.STONE_PRESSURE_PLATE.defaultBlockState());
        plan.postOps.add(DungeonPlan.PostOp.of(DungeonPlan.PostKind.ARROW_TRAP, x, y, z));
    }

    public static void soulFireTrap(DungeonPlan plan, ServerLevel level, int x, int y, int z) {
        Build.set(level, x, y - 1, z, Blocks.SOUL_SOIL.defaultBlockState());
        Build.set(level, x, y, z, Blocks.SOUL_FIRE.defaultBlockState());
        plan.postOps.add(DungeonPlan.PostOp.of(DungeonPlan.PostKind.SOUL_FIRE_TRAP, x, y, z));
    }

    public static void bossChamber(DungeonPlan plan, ServerLevel level, int x0, int y, int z0,
                                   DungeonType type, Random rng) {
        int w = 11, h = 6, d = 11;
        room(plan, level, x0, y, z0, w, h, d, type);
        int cx = x0 + w / 2;
        int cz = z0 + d / 2;
        Build.set(level, cx, y + 1, cz, DungeonBlocks.BOSS_ALTAR);
        DungeonGenUtil.bossChest(plan, level, cx + 2, y + 1, cz);
        DungeonGenUtil.bossChest(plan, level, cx - 2, y + 1, cz);
        plan.postOps.add(DungeonPlan.PostOp.mob(cx, y + 1, cz + 2, type.mobBoss));
        // Boss room spawners at corners
        spawnerRoom(plan, level, x0 + 2, y + 1, z0 + 2, type.mobElite, rng);
        spawnerRoom(plan, level, x0 + w - 3, y + 1, z0 + d - 3, type.mobElite, rng);
    }

    public static void floodedBasin(DungeonPlan plan, ServerLevel level, int x0, int y, int z0, int w, int d) {
        int x1 = x0 + w - 1;
        int z1 = z0 + d - 1;
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                Build.set(level, x, y, z, Blocks.WATER.defaultBlockState());
                plan.postOps.add(DungeonPlan.PostOp.of(DungeonPlan.PostKind.WATER_FILL, x, y, z));
            }
        }
    }

    public static void ironBars(DungeonPlan plan, ServerLevel level, int x, int y, int z, int h) {
        for (int dy = 0; dy < h; dy++) {
            Build.set(level, x, y + dy, z, Blocks.IRON_BARS.defaultBlockState());
        }
    }

    public static void oakDoor(DungeonPlan plan, ServerLevel level, int x, int y, int z) {
        Build.set(level, x, y, z, Blocks.OAK_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
        Build.set(level, x, y + 1, z, Blocks.OAK_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }
}
