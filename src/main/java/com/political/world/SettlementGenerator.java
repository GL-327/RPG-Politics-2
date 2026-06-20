package com.political.world;

import com.political.content.ModBlocks;
import com.political.politics.DataManager;
import com.political.politics.Settlement;
import com.political.politics.SettlementType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Random;

/**
 * Builds the mod's procedural "neo-medieval modern" settlements out of the custom
 * {@link ModBlocks} palette: huge capital castles, modern suburb cities, medieval
 * towns and small villages. Generation is synchronous block placement (no mixins,
 * no datapack structures), triggered at world spawn and as players explore.
 */
public final class SettlementGenerator {

    private static final String[] NAMES = {
            "Oldswinford", "Stourbridge", "Dudley", "Wollaston", "Amblecote", "Kinver",
            "Halesowen", "Cradley", "Wordsley", "Brierley", "Pedmore", "Norton",
            "Lye", "Quarry Bank", "Hagley", "Clent", "Sedgley", "Gornal",
            "Wombourne", "Kingswinford", "Tipton", "Netherton", "Bilston", "Wednesbury"
    };

    private SettlementGenerator() {}

    public static String pickName(Random rng) {
        return NAMES[rng.nextInt(NAMES.length)];
    }

    private static String dimId(ServerLevel level) {
        return level.dimension().identifier().toString();
    }

    // ------------------------------------------------------------------
    // Public entry points
    // ------------------------------------------------------------------

    /** Builds a capital castle at the given centre and registers it. Returns the settlement. */
    public static Settlement generateCapital(ServerLevel level, int cx, int cz, String name) {
        Random rng = new Random(((long) cx << 32) ^ cz ^ 0xCA571E);
        int baseY = Math.max(level.getMinY() + 6, Build.groundY(level, cx, cz));
        buildCastle(level, cx, cz, baseY, rng);

        Settlement s = new Settlement("cap_" + cx + "_" + cz, name, SettlementType.CAPITAL,
                cx, baseY, cz, dimId(level));
        DataManager.addSettlement(s);
        return s;
    }

    /** Builds a settlement of a chosen tier at a centre and registers it. */
    public static Settlement generate(ServerLevel level, int cx, int cz, SettlementType type, String name) {
        Random rng = new Random(((long) cx << 32) ^ cz ^ type.ordinal());
        int baseY = Math.max(level.getMinY() + 6, Build.groundY(level, cx, cz));
        switch (type) {
            case CAPITAL -> buildCastle(level, cx, cz, baseY, rng);
            case CITY -> buildCity(level, cx, cz, baseY, rng);
            case TOWN -> buildTown(level, cx, cz, baseY, rng);
            case VILLAGE -> buildVillage(level, cx, cz, baseY, rng);
        }
        Settlement s = new Settlement(type.name().toLowerCase() + "_" + cx + "_" + cz, name, type,
                cx, baseY, cz, dimId(level));
        DataManager.addSettlement(s);
        return s;
    }

    // ------------------------------------------------------------------
    // Capital castle
    // ------------------------------------------------------------------

    private static void buildCastle(ServerLevel level, int cx, int cz, int baseY, Random rng) {
        int half = 24;
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;

        // Clear the build volume and lay a courtyard platform.
        Build.clear(level, x0 - 1, baseY, z0 - 1, x1 + 1, baseY + 28, z1 + 1);
        Build.foundation(level, x0, z0, x1, z1, baseY, ModBlocks.CASTLE_BRICKS);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, ModBlocks.COBBLE_STREET);

        // Outer curtain wall with crenellations.
        int wallH = 7;
        Build.walls(level, x0, z0, x1, z1, baseY, wallH, ModBlocks.CASTLE_BRICKS);
        Build.crenellate(level, x0, z0, x1, z1, baseY + wallH, ModBlocks.CASTLE_BRICKS);

        // Corner towers.
        cornerTower(level, x0, z0, baseY, ModBlocks.CASTLE_PILLAR);
        cornerTower(level, x1 - 4, z0, baseY, ModBlocks.CASTLE_PILLAR);
        cornerTower(level, x0, z1 - 4, baseY, ModBlocks.CASTLE_PILLAR);
        cornerTower(level, x1 - 4, z1 - 4, baseY, ModBlocks.CASTLE_PILLAR);

        // Gatehouse: carve a 3-wide opening in the south wall.
        Build.clear(level, cx - 1, baseY, z1, cx + 1, baseY + 3, z1);
        Build.set(level, cx - 2, baseY + wallH + 1, z1, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, cx + 2, baseY + wallH + 1, z1, ModBlocks.ROYAL_BANNER_BLOCK);
        // Road from the gate to the keep.
        Build.floor(level, cx - 1, cz, cx + 1, z1, baseY - 1, ModBlocks.PAVED_ROAD);

        // Central keep.
        int kh = 7; // keep half-width (14x14)
        int keepH = 18;
        int kx0 = cx - kh, kz0 = cz - kh, kx1 = cx + kh, kz1 = cz + kh;
        Build.walls(level, kx0, kz0, kx1, kz1, baseY, keepH, ModBlocks.CASTLE_BRICKS);
        Build.floor(level, kx0, kz0, kx1, kz1, baseY - 1, ModBlocks.CASTLE_BRICKS);
        // Floors every 5 blocks.
        for (int fy = baseY + 5; fy < baseY + keepH; fy += 5) {
            Build.floor(level, kx0 + 1, kz0 + 1, kx1 - 1, kz1 - 1, fy, ModBlocks.CASTLE_BRICKS);
        }
        // Roof + crenellations + banners.
        Build.floor(level, kx0, kz0, kx1, kz1, baseY + keepH, ModBlocks.CASTLE_BRICKS);
        Build.crenellate(level, kx0, kz0, kx1, kz1, baseY + keepH + 1, ModBlocks.CASTLE_PILLAR);
        Build.set(level, cx, baseY + keepH + 2, cz, ModBlocks.ROYAL_BANNER_BLOCK);
        // Windows up the keep.
        for (int fy = baseY + 2; fy < baseY + keepH; fy += 3) {
            Build.set(level, kx0, fy, cz, Blocks.GLASS);
            Build.set(level, kx1, fy, cz, Blocks.GLASS);
            Build.set(level, cx, fy, kz0, Blocks.GLASS);
        }
        // Keep doorway facing the gate.
        Build.clear(level, cx - 1, baseY, kz1, cx + 1, baseY + 2, kz1);

        // Throne room: the governance node.
        Build.floor(level, cx - 2, cz - 2, cx + 2, cz + 2, baseY, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, cx, baseY + 1, cz, ModBlocks.CIVIC_MARKER);
        Build.set(level, cx - 3, baseY + 1, cz - 3, ModBlocks.STREET_LAMP);
        Build.set(level, cx + 3, baseY + 1, cz - 3, ModBlocks.STREET_LAMP);

        // A ring of street lamps along the courtyard and a few houses.
        for (int x = x0 + 4; x <= x1 - 4; x += 8) {
            lampPost(level, x, baseY, z0 + 3);
            lampPost(level, x, baseY, z1 - 3);
        }
        buildHouse(level, x0 + 4, z0 + 4, baseY, 7, 6, 4, ModBlocks.TOWN_HALL_BRICKS, ModBlocks.CASTLE_BRICKS, rng);
        buildHouse(level, x1 - 11, z0 + 4, baseY, 7, 6, 4, ModBlocks.TOWN_HALL_BRICKS, ModBlocks.CASTLE_BRICKS, rng);
        buildHouse(level, x0 + 4, z1 - 10, baseY, 7, 6, 4, ModBlocks.TOWN_HALL_BRICKS, ModBlocks.CASTLE_BRICKS, rng);
    }

    private static void cornerTower(ServerLevel level, int x, int z, int baseY, Block block) {
        int h = 12;
        Build.walls(level, x, z, x + 4, z + 4, baseY, h, block);
        Build.crenellate(level, x, z, x + 4, z + 4, baseY + h, block);
        Build.set(level, x + 2, baseY + h, z + 2, ModBlocks.STREET_LAMP);
    }

    // ------------------------------------------------------------------
    // Modern suburb city
    // ------------------------------------------------------------------

    private static void buildCity(ServerLevel level, int cx, int cz, int baseY, Random rng) {
        int half = 32;
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;

        Build.clear(level, x0, baseY, z0, x1, baseY + 24, z1);
        Build.foundation(level, x0, z0, x1, z1, baseY, ModBlocks.MODERN_FACADE);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, ModBlocks.PAVED_ROAD);

        // Street grid every 12 blocks -> 8x8 building plots between.
        int step = 12;
        for (int gx = x0; gx <= x1; gx += step) Build.cube(level, gx, baseY - 1, z0, gx + 1, baseY - 1, z1, ModBlocks.COBBLE_STREET);
        for (int gz = z0; gz <= z1; gz += step) Build.cube(level, x0, baseY - 1, gz, x1, baseY - 1, gz + 1, ModBlocks.COBBLE_STREET);

        for (int bx = x0 + 2; bx + 8 <= x1 - 2; bx += step) {
            for (int bz = z0 + 2; bz + 8 <= z1 - 2; bz += step) {
                if (Math.abs(bx - cx) < 8 && Math.abs(bz - cz) < 8) continue; // leave room for town hall
                int floors = 2 + rng.nextInt(5);
                buildModernBuilding(level, bx, bz, baseY, 8, 8, floors, rng);
                lampPost(level, bx - 1, baseY, bz - 1);
            }
        }

        // Central town hall = governance node.
        buildTownHall(level, cx, cz, baseY, rng);
    }

    private static void buildModernBuilding(ServerLevel level, int x, int z, int baseY, int w, int d, int floors, Random rng) {
        int h = floors * 3;
        Build.walls(level, x, z, x + w - 1, z + d - 1, baseY, h, ModBlocks.MODERN_FACADE);
        Build.floor(level, x, z, x + w - 1, z + d - 1, baseY - 1, ModBlocks.MODERN_FACADE);
        Build.floor(level, x, z, x + w - 1, z + d - 1, baseY + h, ModBlocks.MODERN_FACADE);
        // Window bands on each floor.
        for (int f = 0; f < floors; f++) {
            int wy = baseY + 1 + f * 3;
            for (int wx = x + 1; wx < x + w - 1; wx += 2) {
                Build.set(level, wx, wy, z, ModBlocks.MODERN_WINDOW);
                Build.set(level, wx, wy, z + d - 1, ModBlocks.MODERN_WINDOW);
            }
            for (int wz = z + 1; wz < z + d - 1; wz += 2) {
                Build.set(level, x, wy, wz, ModBlocks.MODERN_WINDOW);
                Build.set(level, x + w - 1, wy, wz, ModBlocks.MODERN_WINDOW);
            }
        }
        // Ground-floor entrance.
        Build.clear(level, x + w / 2, baseY, z, x + w / 2, baseY + 1, z);
        Build.set(level, x + w / 2, baseY + h + 1, z + d / 2, ModBlocks.STREET_LAMP);
    }

    private static void buildTownHall(ServerLevel level, int cx, int cz, int baseY, Random rng) {
        int half = 6;
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        Build.clear(level, x0, baseY, z0, x1, baseY + 12, z1);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, ModBlocks.TOWN_HALL_BRICKS);
        Build.walls(level, x0, z0, x1, z1, baseY, 8, ModBlocks.TOWN_HALL_BRICKS);
        Build.floor(level, x0, z0, x1, z1, baseY + 8, ModBlocks.TOWN_HALL_BRICKS);
        Build.crenellate(level, x0, z0, x1, z1, baseY + 9, ModBlocks.CASTLE_PILLAR);
        // Columned facade + entrance.
        for (int x = x0; x <= x1; x += 2) Build.cube(level, x, baseY, z1, x, baseY + 6, z1, ModBlocks.CASTLE_PILLAR);
        Build.clear(level, cx - 1, baseY, z1, cx + 1, baseY + 3, z1);
        // Windows.
        for (int x = x0 + 1; x < x1; x += 2) {
            Build.set(level, x, baseY + 3, z0, ModBlocks.MODERN_WINDOW);
        }
        // Governance node.
        Build.set(level, cx, baseY + 1, cz, ModBlocks.CIVIC_MARKER);
        Build.set(level, cx, baseY + 7, cz, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, cx - 2, baseY + 1, cz, ModBlocks.STREET_LAMP);
        Build.set(level, cx + 2, baseY + 1, cz, ModBlocks.STREET_LAMP);
    }

    // ------------------------------------------------------------------
    // Medieval town & village
    // ------------------------------------------------------------------

    private static void buildTown(ServerLevel level, int cx, int cz, int baseY, Random rng) {
        int half = 16;
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        Build.clear(level, x0, baseY, z0, x1, baseY + 14, z1);
        Build.foundation(level, x0, z0, x1, z1, baseY, ModBlocks.COBBLE_STREET);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, Blocks.GRASS_BLOCK);
        // Cross streets.
        Build.cube(level, cx - 1, baseY - 1, z0, cx + 1, baseY - 1, z1, ModBlocks.COBBLE_STREET);
        Build.cube(level, x0, baseY - 1, cz - 1, x1, baseY - 1, cz + 1, ModBlocks.COBBLE_STREET);

        buildTownHall(level, cx, cz, baseY, rng);

        // Ring of houses.
        int[][] spots = {
                {x0 + 3, z0 + 3}, {x1 - 10, z0 + 3}, {x0 + 3, z1 - 9}, {x1 - 10, z1 - 9},
                {cx - 4, z0 + 3}, {cx - 4, z1 - 9}
        };
        for (int[] sp : spots) {
            buildHouse(level, sp[0], sp[1], baseY, 7, 6, 4, ModBlocks.TOWN_HALL_BRICKS, ModBlocks.CASTLE_BRICKS, rng);
        }
        for (int x = x0 + 4; x <= x1 - 4; x += 10) {
            lampPost(level, x, baseY, cz - 2);
            lampPost(level, x, baseY, cz + 2);
        }
    }

    private static void buildVillage(ServerLevel level, int cx, int cz, int baseY, Random rng) {
        int half = 9;
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        Build.clear(level, x0, baseY, z0, x1, baseY + 10, z1);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, Blocks.GRASS_BLOCK);
        Build.cube(level, cx - 1, baseY - 1, z0, cx + 1, baseY - 1, z1, ModBlocks.COBBLE_STREET);

        // Small civic plinth (governance node) at the centre.
        Build.cube(level, cx - 1, baseY, cz - 1, cx + 1, baseY, cz + 1, ModBlocks.TOWN_HALL_BRICKS);
        Build.set(level, cx, baseY + 1, cz, ModBlocks.CIVIC_MARKER);
        lampPost(level, cx - 2, baseY, cz - 2);
        lampPost(level, cx + 2, baseY, cz + 2);

        buildHouse(level, x0 + 2, z0 + 2, baseY, 6, 5, 3, ModBlocks.TOWN_HALL_BRICKS, ModBlocks.CASTLE_BRICKS, rng);
        buildHouse(level, x1 - 8, z0 + 2, baseY, 6, 5, 3, ModBlocks.TOWN_HALL_BRICKS, ModBlocks.CASTLE_BRICKS, rng);
        buildHouse(level, x0 + 2, z1 - 7, baseY, 6, 5, 3, ModBlocks.TOWN_HALL_BRICKS, ModBlocks.CASTLE_BRICKS, rng);
    }

    // ------------------------------------------------------------------
    // Shared pieces
    // ------------------------------------------------------------------

    private static void buildHouse(ServerLevel level, int x, int z, int baseY, int w, int d, int wallH,
                                   Block wall, Block roof, Random rng) {
        Build.clear(level, x, baseY, z, x + w - 1, baseY + wallH + 2, z + d - 1);
        Build.floor(level, x, z, x + w - 1, z + d - 1, baseY - 1, wall);
        Build.walls(level, x, z, x + w - 1, z + d - 1, baseY, wallH, wall);
        // Flat roof with a small parapet.
        Build.floor(level, x, z, x + w - 1, z + d - 1, baseY + wallH, roof);
        // Door (south centre) + a couple of windows.
        Build.clear(level, x + w / 2, baseY, z, x + w / 2, baseY + 1, z);
        Build.set(level, x + 1, baseY + 1, z, Blocks.GLASS);
        Build.set(level, x + w - 2, baseY + 1, z, Blocks.GLASS);
        Build.set(level, x, baseY + 1, z + d / 2, Blocks.GLASS);
        Build.set(level, x + w - 1, baseY + 1, z + d / 2, Blocks.GLASS);
    }

    private static void lampPost(ServerLevel level, int x, int baseY, int z) {
        Build.set(level, x, baseY, z, ModBlocks.CASTLE_PILLAR);
        Build.set(level, x, baseY + 1, z, ModBlocks.CASTLE_PILLAR);
        Build.set(level, x, baseY + 2, z, ModBlocks.CASTLE_PILLAR);
        Build.set(level, x, baseY + 3, z, ModBlocks.STREET_LAMP);
    }
}
