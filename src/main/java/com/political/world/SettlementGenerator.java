package com.political.world;

import com.political.content.ModBlocks;
import com.political.politics.DataManager;
import com.political.politics.Settlement;
import com.political.politics.SettlementType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
        buildCastle(level, cx, cz, baseY, sizeFor(SettlementType.CAPITAL, rng), rng);

        Settlement s = new Settlement("cap_" + cx + "_" + cz, name, SettlementType.CAPITAL,
                cx, baseY, cz, dimId(level));
        DataManager.addSettlement(s);
        return s;
    }

    /**
     * Plans a settlement into {@code buffer} (no blocks placed yet) and registers it
     * immediately so governance/citizenship work while the structure streams in.
     */
    public static Settlement planInto(BuildBuffer buffer, ServerLevel level, int cx, int cz,
                                      SettlementType type, String name) {
        Build.beginDeferred(buffer);
        try {
            return generate(level, cx, cz, type, name);
        } finally {
            Build.endDeferred();
        }
    }

    /** Builds a settlement of a chosen tier at a centre and registers it. */
    public static Settlement generate(ServerLevel level, int cx, int cz, SettlementType type, String name) {
        Random rng = new Random(((long) cx << 32) ^ cz ^ type.ordinal());
        int baseY = Math.max(level.getMinY() + 6, Build.groundY(level, cx, cz));
        // Per-instance size: most settlements are large, a few are small, and the biggest
        // capitals are genuinely enormous mega-builds.
        int half = sizeFor(type, rng);
        switch (type) {
            case CAPITAL -> buildCastle(level, cx, cz, baseY, half, rng);
            case CITY -> buildCity(level, cx, cz, baseY, half, rng);
            case TOWN -> buildTown(level, cx, cz, baseY, half, rng);
            case VILLAGE -> buildVillage(level, cx, cz, baseY, half, rng);
        }
        Settlement s = new Settlement(type.name().toLowerCase() + "_" + cx + "_" + cz, name, type,
                cx, baseY, cz, dimId(level));
        DataManager.addSettlement(s);
        return s;
    }

    /** Half-extent (radius in blocks) for a tier. Skewed so most are big; capitals can be huge. */
    public static int sizeFor(SettlementType type, Random rng) {
        double roll = rng.nextDouble();
        return switch (type) {
            // ~20% modest, ~60% large, ~20% colossal (up to ~220 blocks across).
            case CAPITAL -> roll < 0.2 ? 48 + rng.nextInt(16) : roll < 0.8 ? 64 + rng.nextInt(32) : 96 + rng.nextInt(16);
            case CITY    -> roll < 0.25 ? 36 + rng.nextInt(12) : roll < 0.85 ? 48 + rng.nextInt(24) : 72 + rng.nextInt(12);
            case TOWN    -> roll < 0.3 ? 18 + rng.nextInt(8) : 26 + rng.nextInt(16);
            case VILLAGE -> roll < 0.35 ? 9 + rng.nextInt(5) : 14 + rng.nextInt(8);
        };
    }

    // ------------------------------------------------------------------
    // Capital castle — a HUGE fortified town inside curtain walls
    // ------------------------------------------------------------------

    private static void buildCastle(ServerLevel level, int cx, int cz, int baseY, int half, Random rng) {
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        int palaceHalf = Math.max(9, Math.min(24, half / 5));
        int wallH = 10 + half / 40;

        // Clear the build volume and lay the bailey ground + foundation.
        Build.clear(level, x0 - 2, baseY, z0 - 2, x1 + 2, baseY + wallH + 24, z1 + 2);
        Build.foundation(level, x0 - 1, z0 - 1, x1 + 1, z1 + 1, baseY, ModBlocks.CASTLE_BRICKS);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, ModBlocks.COBBLE_STREET);

        // Double-thick curtain wall with a walkable rampart + crenellations.
        Build.walls(level, x0, z0, x1, z1, baseY, wallH, ModBlocks.CASTLE_BRICKS);
        Build.walls(level, x0 + 1, z0 + 1, x1 - 1, z1 - 1, baseY, wallH, ModBlocks.CASTLE_BRICKS);
        Build.borderRing(level, x0, z0, x1, z1, baseY + wallH - 1, 2, ModBlocks.CASTLE_BRICKS);
        Build.crenellate(level, x0, z0, x1, z1, baseY + wallH, ModBlocks.CASTLE_BRICKS);
        // Wall torches along the rampart.
        for (int x = x0 + 4; x <= x1 - 4; x += 8) {
            Build.set(level, x, baseY + wallH, z0 + 1, ModBlocks.STREET_LAMP);
            Build.set(level, x, baseY + wallH, z1 - 1, ModBlocks.STREET_LAMP);
        }

        // Four grand corner towers + mid-wall towers on long walls of big castles.
        cornerTower(level, x0 - 1, z0 - 1, baseY, wallH + 6);
        cornerTower(level, x1 - 5, z0 - 1, baseY, wallH + 6);
        cornerTower(level, x0 - 1, z1 - 5, baseY, wallH + 6);
        cornerTower(level, x1 - 5, z1 - 5, baseY, wallH + 6);
        if (half >= 70) {
            gateTower(level, x0 - 1, cz - 2, baseY, wallH + 3);
            gateTower(level, x1 - 3, cz - 2, baseY, wallH + 3);
            gateTower(level, cx - 2, z0 - 1, baseY, wallH + 3);
        }

        // South gatehouse: two flanking towers + a 5-wide gate.
        gateTower(level, cx - 6, z1 - 4, baseY, wallH + 4);
        gateTower(level, cx + 2, z1 - 4, baseY, wallH + 4);
        Build.clear(level, cx - 2, baseY, z1 - 1, cx + 2, baseY + 4, z1 + 1);
        Build.set(level, cx - 3, baseY + wallH + 1, z1, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, cx + 3, baseY + wallH + 1, z1, ModBlocks.ROYAL_BANNER_BLOCK);

        // Packed inner districts (medieval houses on a street grid), reserving the centre
        // for the royal palace and the central avenue.
        int m = Math.max(6, half / 12);
        districtFill(level, x0 + m, z0 + m, x1 - m, z1 - m, baseY, 12, cx, cz, palaceHalf + 2,
                rng, ModBlocks.COBBLE_STREET, false);

        // Grand processional avenue from the gate to the palace steps.
        Build.clear(level, cx - 2, baseY, cz + palaceHalf, cx + 2, baseY + 7, z1 - 1);
        Build.floor(level, cx - 2, cz + palaceHalf, cx + 2, z1 - 1, baseY - 1, ModBlocks.PAVED_ROAD);
        for (int z = cz + palaceHalf + 2; z <= z1 - 3; z += 6) {
            lampPost(level, cx - 3, baseY, z);
            lampPost(level, cx + 3, baseY, z);
        }

        // The royal palace — governance node — at the heart of the bailey.
        buildKeep(level, cx, cz, baseY, palaceHalf, 18 + palaceHalf, rng);

        // Formal gardens flanking the palace approach.
        Build.garden(level, cx - palaceHalf + 2, cz + 4, cx - 4, cz + palaceHalf - 1, baseY, rng);
        Build.garden(level, cx + 4, cz + 4, cx + palaceHalf - 2, cz + palaceHalf - 1, baseY, rng);
        Build.tree(level, cx - palaceHalf, baseY, cz + palaceHalf - 2);
        Build.tree(level, cx + palaceHalf, baseY, cz + palaceHalf - 2);
        Build.tree(level, cx - palaceHalf, baseY, cz - palaceHalf + 2);
        Build.tree(level, cx + palaceHalf, baseY, cz - palaceHalf + 2);
    }

    /** A grand multi-storey palace/keep used as the capital governance node. */
    private static void buildKeep(ServerLevel level, int cx, int cz, int baseY, int half, int height, Random rng) {
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        Build.clear(level, x0, baseY, z0, x1, baseY + height + 8, z1);
        Build.foundation(level, x0, z0, x1, z1, baseY, ModBlocks.CASTLE_BRICKS);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, ModBlocks.CASTLE_BRICKS);
        Build.walls(level, x0, z0, x1, z1, baseY, height, ModBlocks.CASTLE_BRICKS);

        // Pillared corners running the full height.
        Build.cube(level, x0, baseY, z0, x0, baseY + height, z0, ModBlocks.CASTLE_PILLAR);
        Build.cube(level, x1, baseY, z0, x1, baseY + height, z0, ModBlocks.CASTLE_PILLAR);
        Build.cube(level, x0, baseY, z1, x0, baseY + height, z1, ModBlocks.CASTLE_PILLAR);
        Build.cube(level, x1, baseY, z1, x1, baseY + height, z1, ModBlocks.CASTLE_PILLAR);

        // Interior floors every 5 blocks.
        for (int fy = baseY + 5; fy < baseY + height; fy += 5) {
            Build.floor(level, x0 + 1, z0 + 1, x1 - 1, z1 - 1, fy, ModBlocks.CASTLE_BRICKS);
        }
        // Window bands.
        for (int fy = baseY + 2; fy < baseY + height; fy += 4) {
            for (int x = x0 + 2; x < x1; x += 3) {
                Build.set(level, x, fy, z0, Blocks.GLASS);
                Build.set(level, x, fy, z1, Blocks.GLASS);
            }
            for (int z = z0 + 2; z < z1; z += 3) {
                Build.set(level, x0, fy, z, Blocks.GLASS);
                Build.set(level, x1, fy, z, Blocks.GLASS);
            }
        }
        // Battlemented roof + corner banners.
        Build.floor(level, x0, z0, x1, z1, baseY + height, ModBlocks.CASTLE_BRICKS);
        Build.crenellate(level, x0, z0, x1, z1, baseY + height + 1, ModBlocks.CASTLE_PILLAR);
        Build.set(level, x0 + 1, baseY + height + 2, z0 + 1, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, x1 - 1, baseY + height + 2, z0 + 1, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, x0 + 1, baseY + height + 2, z1 - 1, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, x1 - 1, baseY + height + 2, z1 - 1, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, cx, baseY + height + 3, cz, ModBlocks.ROYAL_BANNER_BLOCK);

        // Grand south entrance with steps.
        Build.clear(level, cx - 2, baseY, z1, cx + 2, baseY + 4, z1);
        for (int dx = -2; dx <= 2; dx++) Build.stair(level, cx + dx, baseY - 1, z1 + 1, Blocks.DARK_OAK_STAIRS, Direction.NORTH);

        // Throne room: the governance node, carpeted in royal red.
        Build.floor(level, cx - 3, cz - 3, cx + 3, cz + 3, baseY, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, cx, baseY + 1, cz, ModBlocks.CIVIC_MARKER);
        Build.set(level, cx - 4, baseY + 1, cz - 4, ModBlocks.STREET_LAMP);
        Build.set(level, cx + 4, baseY + 1, cz - 4, ModBlocks.STREET_LAMP);
        Build.set(level, cx - 4, baseY + 1, cz + 4, ModBlocks.STREET_LAMP);
        Build.set(level, cx + 4, baseY + 1, cz + 4, ModBlocks.STREET_LAMP);
    }

    private static void cornerTower(ServerLevel level, int x, int z, int baseY, int h) {
        int s = 6;
        Build.foundation(level, x, z, x + s, z + s, baseY, ModBlocks.CASTLE_PILLAR);
        Build.walls(level, x, z, x + s, z + s, baseY, h, ModBlocks.CASTLE_PILLAR);
        Build.crenellate(level, x, z, x + s, z + s, baseY + h, ModBlocks.CASTLE_PILLAR);
        for (int fy = baseY + 3; fy < baseY + h; fy += 4) {
            Build.set(level, x + s / 2, fy, z, Blocks.GLASS);
            Build.set(level, x, fy, z + s / 2, Blocks.GLASS);
        }
        Build.set(level, x + s / 2, baseY + h, z + s / 2, ModBlocks.STREET_LAMP);
        Build.set(level, x + s / 2, baseY + h + 1, z + s / 2, ModBlocks.ROYAL_BANNER_BLOCK);
    }

    private static void gateTower(ServerLevel level, int x, int z, int baseY, int h) {
        int s = 4;
        Build.foundation(level, x, z, x + s, z + s, baseY, ModBlocks.CASTLE_BRICKS);
        Build.walls(level, x, z, x + s, z + s, baseY, h, ModBlocks.CASTLE_BRICKS);
        Build.crenellate(level, x, z, x + s, z + s, baseY + h, ModBlocks.CASTLE_BRICKS);
        Build.set(level, x + s / 2, baseY + h, z + s / 2, ModBlocks.STREET_LAMP);
    }

    // ------------------------------------------------------------------
    // Modern suburb city
    // ------------------------------------------------------------------

    private static void buildCity(ServerLevel level, int cx, int cz, int baseY, int half, Random rng) {
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        int plaza = Math.max(11, Math.min(24, half / 4));

        Build.clear(level, x0, baseY, z0, x1, baseY + 40, z1);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, ModBlocks.PAVED_ROAD);

        // Varied modern blocks on a 14-block street grid, central plaza reserved.
        districtFill(level, x0, z0, x1, z1, baseY, 14, cx, cz, plaza, rng, ModBlocks.PAVED_ROAD, true);

        // Central civic plaza: park, fountain, and the town hall.
        Build.floor(level, cx - plaza, cz - plaza, cx + plaza, cz + plaza, baseY - 1, ModBlocks.PAVED_ROAD);
        Build.garden(level, cx - plaza + 1, cz + 5, cx - 5, cz + plaza - 1, baseY, rng);
        Build.garden(level, cx + 5, cz + 5, cx + plaza - 1, cz + plaza - 1, baseY, rng);
        Build.tree(level, cx - plaza + 2, baseY, cz + plaza - 3);
        Build.tree(level, cx + plaza - 2, baseY, cz + plaza - 3);
        Build.tree(level, cx - plaza + 2, baseY, cz - plaza + 2);
        Build.tree(level, cx + plaza - 2, baseY, cz - plaza + 2);
        fountain(level, cx, cz - plaza + 2, baseY);
        for (int a = -plaza; a <= plaza; a += plaza) {
            lampPost(level, cx + a, baseY, cz - plaza);
            lampPost(level, cx + a, baseY, cz + plaza);
        }
        buildTownHall(level, cx, cz, baseY, rng);
    }

    private static void buildModernBuilding(ServerLevel level, int x, int z, int baseY, int w, int d, int floors, Random rng) {
        int h = floors * 3;
        Block facade = rng.nextInt(4) == 0 ? ModBlocks.TOWN_HALL_BRICKS : ModBlocks.MODERN_FACADE;
        Build.clear(level, x, baseY, z, x + w - 1, baseY + h + 2, z + d - 1);
        Build.foundation(level, x, z, x + w - 1, z + d - 1, baseY, facade);
        Build.walls(level, x, z, x + w - 1, z + d - 1, baseY, h, facade);
        Build.floor(level, x, z, x + w - 1, z + d - 1, baseY - 1, facade);
        Build.floor(level, x, z, x + w - 1, z + d - 1, baseY + h, facade);
        // Window bands on each floor.
        for (int f = 0; f < floors; f++) {
            int wy = baseY + 1 + f * 3;
            for (int wx = x + 1; wx < x + w - 1; wx++) {
                Build.set(level, wx, wy, z, ModBlocks.MODERN_WINDOW);
                Build.set(level, wx, wy, z + d - 1, ModBlocks.MODERN_WINDOW);
                Build.set(level, wx, wy + 1, z, ModBlocks.MODERN_WINDOW);
                Build.set(level, wx, wy + 1, z + d - 1, ModBlocks.MODERN_WINDOW);
            }
            for (int wz = z + 1; wz < z + d - 1; wz++) {
                Build.set(level, x, wy, wz, ModBlocks.MODERN_WINDOW);
                Build.set(level, x + w - 1, wy, wz, ModBlocks.MODERN_WINDOW);
                Build.set(level, x, wy + 1, wz, ModBlocks.MODERN_WINDOW);
                Build.set(level, x + w - 1, wy + 1, wz, ModBlocks.MODERN_WINDOW);
            }
        }
        // Ground-floor glass entrance + rooftop beacon.
        Build.set(level, x + w / 2, baseY, z, Blocks.GLASS);
        Build.clear(level, x + w / 2, baseY, z, x + w / 2, baseY + 1, z);
        Build.set(level, x + w / 2, baseY + h + 1, z + d / 2, ModBlocks.STREET_LAMP);
    }

    private static void buildTownHall(ServerLevel level, int cx, int cz, int baseY, Random rng) {
        int half = 8;
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        Build.clear(level, x0, baseY, z0, x1, baseY + 16, z1);
        Build.foundation(level, x0, z0, x1, z1, baseY, ModBlocks.TOWN_HALL_BRICKS);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, ModBlocks.TOWN_HALL_BRICKS);
        Build.walls(level, x0, z0, x1, z1, baseY, 11, ModBlocks.TOWN_HALL_BRICKS);
        Build.floor(level, x0, z0, x1, z1, baseY + 11, ModBlocks.TOWN_HALL_BRICKS);
        Build.crenellate(level, x0, z0, x1, z1, baseY + 12, ModBlocks.CASTLE_PILLAR);
        // Pillared portico across the front, with a clock tower above the door.
        for (int x = x0; x <= x1; x += 2) Build.cube(level, x, baseY, z1, x, baseY + 8, z1, ModBlocks.CASTLE_PILLAR);
        Build.clear(level, cx - 1, baseY, z1, cx + 1, baseY + 4, z1);
        Build.cube(level, cx - 1, baseY + 12, cz, cx + 1, baseY + 16, cz, ModBlocks.TOWN_HALL_BRICKS);
        Build.set(level, cx, baseY + 17, cz, ModBlocks.STREET_LAMP);
        // Window bands.
        for (int x = x0 + 2; x < x1; x += 2) {
            Build.set(level, x, baseY + 3, z0, ModBlocks.MODERN_WINDOW);
            Build.set(level, x, baseY + 6, z0, ModBlocks.MODERN_WINDOW);
        }
        // Governance node + banners.
        Build.set(level, cx, baseY + 1, cz, ModBlocks.CIVIC_MARKER);
        Build.set(level, cx, baseY + 10, cz, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, cx - 3, baseY + 1, cz, ModBlocks.STREET_LAMP);
        Build.set(level, cx + 3, baseY + 1, cz, ModBlocks.STREET_LAMP);
    }

    // ------------------------------------------------------------------
    // Medieval town & village
    // ------------------------------------------------------------------

    private static void buildTown(ServerLevel level, int cx, int cz, int baseY, int half, Random rng) {
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        int sq = Math.max(8, half / 4);
        Build.clear(level, x0, baseY, z0, x1, baseY + 20, z1);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, Blocks.GRASS_BLOCK);

        districtFill(level, x0, z0, x1, z1, baseY, 12, cx, cz, sq + 2, rng, Blocks.GRASS_BLOCK, false);

        // Central market square with the town hall.
        Build.floor(level, cx - sq, cz - sq, cx + sq, cz + sq, baseY - 1, ModBlocks.COBBLE_STREET);
        buildTownHall(level, cx, cz, baseY, rng);
        well(level, cx + sq - 2, cz + sq - 2, baseY);
        Build.tree(level, cx - sq + 1, baseY, cz + sq - 1);
        Build.tree(level, cx - sq + 1, baseY, cz - sq + 1);
        for (int a = -sq; a <= sq; a += sq) {
            lampPost(level, cx + a, baseY, cz - sq);
            lampPost(level, cx + a, baseY, cz + sq);
        }
    }

    private static void buildVillage(ServerLevel level, int cx, int cz, int baseY, int half, Random rng) {
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        Build.clear(level, x0, baseY, z0, x1, baseY + 14, z1);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, Blocks.GRASS_BLOCK);

        districtFill(level, x0, z0, x1, z1, baseY, 10, cx, cz, 5, rng, Blocks.GRASS_BLOCK, false);

        // Village green: civic plinth, well, gardens.
        Build.floor(level, cx - 4, cz - 4, cx + 4, cz + 4, baseY - 1, Blocks.GRASS_BLOCK);
        Build.cube(level, cx - 1, baseY, cz - 1, cx + 1, baseY, cz + 1, ModBlocks.TOWN_HALL_BRICKS);
        Build.set(level, cx, baseY + 1, cz, ModBlocks.CIVIC_MARKER);
        well(level, cx + 3, cz + 3, baseY);
        Build.garden(level, cx - 4, cz - 4, cx - 1, cz - 1, baseY, rng);
        Build.tree(level, cx - 3, baseY, cz + 3);
        lampPost(level, cx - 3, baseY, cz - 3);
        lampPost(level, cx + 3, baseY, cz - 3);
    }

    // ------------------------------------------------------------------
    // District filler — street grid + plots of buildings
    // ------------------------------------------------------------------

    /**
     * Lays a cobbled street grid over a rectangle and fills each plot with a building,
     * skipping any plot that overlaps the reserved central box (centre {@code rcx,rcz},
     * half-extent {@code rHalf}). {@code modern} chooses glassy towers vs. timbered houses.
     */
    private static void districtFill(ServerLevel level, int x0, int z0, int x1, int z1, int baseY,
                                     int step, int rcx, int rcz, int rHalf, Random rng,
                                     Block ground, boolean modern) {
        Build.floor(level, x0, z0, x1, z1, baseY - 1, ground);
        Block street = modern ? ModBlocks.PAVED_ROAD : ModBlocks.COBBLE_STREET;
        // Two-wide streets read more clearly as roads than single lines.
        for (int gx = x0; gx <= x1; gx += step) {
            Build.cube(level, gx, baseY - 1, z0, gx, baseY - 1, z1, street);
            if (gx + 1 <= x1) Build.cube(level, gx + 1, baseY - 1, z0, gx + 1, baseY - 1, z1, street);
        }
        for (int gz = z0; gz <= z1; gz += step) {
            Build.cube(level, x0, baseY - 1, gz, x1, baseY - 1, gz, street);
            if (gz + 1 <= z1) Build.cube(level, x0, baseY - 1, gz + 1, x1, baseY - 1, gz + 1, street);
        }

        for (int px = x0 + 1; px + step - 2 <= x1; px += step) {
            for (int pz = z0 + 1; pz + step - 2 <= z1; pz += step) {
                int hx0 = px + 1, hz0 = pz + 1;
                int w = step - 3, d = step - 3;
                boolean overlapX = hx0 <= rcx + rHalf && (hx0 + w) >= rcx - rHalf;
                boolean overlapZ = hz0 <= rcz + rHalf && (hz0 + d) >= rcz - rHalf;
                if (overlapX && overlapZ) continue;

                if (modern) {
                    int floors = 2 + rng.nextInt(7);
                    buildModernBuilding(level, hx0, hz0, baseY, w, d, floors, rng);
                    lampPost(level, px, baseY, pz);
                } else if (rng.nextInt(7) == 0) {
                    Build.garden(level, hx0, hz0, hx0 + w - 1, hz0 + d - 1, baseY, rng);
                    Build.tree(level, hx0 + w / 2, baseY, hz0 + d / 2);
                } else {
                    buildHouse(level, hx0, hz0, baseY, w, d, rng);
                    lampPost(level, px, baseY, pz);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Shared pieces
    // ------------------------------------------------------------------

    private static final Block[] HOUSE_WALLS = {
            ModBlocks.TOWN_HALL_BRICKS, ModBlocks.CASTLE_BRICKS, Blocks.OAK_PLANKS, ModBlocks.MODERN_FACADE
    };

    private static void buildHouse(ServerLevel level, int x, int z, int baseY, int w, int d, Random rng) {
        Block wall = HOUSE_WALLS[rng.nextInt(HOUSE_WALLS.length)];
        boolean twoStorey = w >= 7 && d >= 7 && rng.nextInt(3) == 0;
        int wallH = twoStorey ? 7 : 4;
        int x1 = x + w - 1, z1 = z + d - 1;
        int roofRise = w / 2 + 1;

        Build.clear(level, x, baseY, z, x1, baseY + wallH + roofRise + 2, z1);
        Build.foundation(level, x, z, x1, z1, baseY, ModBlocks.CASTLE_BRICKS);
        Build.floor(level, x, z, x1, z1, baseY - 1, Blocks.OAK_PLANKS);
        Build.walls(level, x, z, x1, z1, baseY, wallH, wall);
        if (twoStorey) Build.floor(level, x + 1, z + 1, x1 - 1, z1 - 1, baseY + 3, Blocks.OAK_PLANKS);
        Build.floor(level, x, z, x1, z1, baseY + wallH, Blocks.OAK_PLANKS);
        Build.gableRoof(level, x, z, x1, z1, baseY + wallH, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_PLANKS);

        // South door + glazed windows on each storey/wall.
        Build.clear(level, x + w / 2, baseY, z, x + w / 2, baseY + 1, z);
        houseWindows(level, x, z, x1, z1, baseY + 1);
        if (twoStorey) houseWindows(level, x, z, x1, z1, baseY + 4);

        // Interior lighting + a doorstep slab.
        Build.set(level, x + 1, baseY + wallH - 1, z + 1, ModBlocks.STREET_LAMP);
        if (twoStorey) Build.set(level, x + 1, baseY + 2, z + 1, ModBlocks.STREET_LAMP);
        Build.set(level, x + w / 2, baseY - 1, z - 1, ModBlocks.COBBLE_STREET);
    }

    private static void houseWindows(ServerLevel level, int x0, int z0, int x1, int z1, int y) {
        for (int x = x0 + 1; x < x1; x += 2) {
            Build.set(level, x, y, z0, Blocks.GLASS);
            Build.set(level, x, y, z1, Blocks.GLASS);
        }
        for (int z = z0 + 1; z < z1; z += 2) {
            Build.set(level, x0, y, z, Blocks.GLASS);
            Build.set(level, x1, y, z, Blocks.GLASS);
        }
    }

    private static void well(ServerLevel level, int cx, int cz, int baseY) {
        Build.walls(level, cx - 1, cz - 1, cx + 1, cz + 1, baseY, 1, ModBlocks.COBBLE_STREET);
        Build.set(level, cx, baseY, cz, Blocks.WATER);
        Build.cube(level, cx - 1, baseY + 1, cz - 1, cx - 1, baseY + 3, cz - 1, Blocks.OAK_LOG);
        Build.cube(level, cx + 1, baseY + 1, cz + 1, cx + 1, baseY + 3, cz + 1, Blocks.OAK_LOG);
        Build.cube(level, cx - 1, baseY + 1, cz + 1, cx - 1, baseY + 3, cz + 1, Blocks.OAK_LOG);
        Build.cube(level, cx + 1, baseY + 1, cz - 1, cx + 1, baseY + 3, cz - 1, Blocks.OAK_LOG);
        Build.floor(level, cx - 1, cz - 1, cx + 1, cz + 1, baseY + 4, Blocks.DARK_OAK_PLANKS);
        Build.set(level, cx, baseY + 3, cz, ModBlocks.STREET_LAMP);
    }

    private static void fountain(ServerLevel level, int cx, int cz, int baseY) {
        Build.walls(level, cx - 2, cz - 2, cx + 2, cz + 2, baseY, 1, ModBlocks.CASTLE_PILLAR);
        Build.floor(level, cx - 1, cz - 1, cx + 1, cz + 1, baseY, Blocks.WATER);
        Build.cube(level, cx, baseY, cz, cx, baseY + 2, cz, ModBlocks.CASTLE_PILLAR);
        Build.set(level, cx, baseY + 3, cz, ModBlocks.STREET_LAMP);
    }

    private static void lampPost(ServerLevel level, int x, int baseY, int z) {
        Build.set(level, x, baseY, z, ModBlocks.CASTLE_PILLAR);
        Build.set(level, x, baseY + 1, z, ModBlocks.CASTLE_PILLAR);
        Build.set(level, x, baseY + 2, z, ModBlocks.CASTLE_PILLAR);
        Build.set(level, x, baseY + 3, z, ModBlocks.STREET_LAMP);
    }
}
