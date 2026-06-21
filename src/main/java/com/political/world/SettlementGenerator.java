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
import net.minecraft.world.level.block.Rotation;

import java.util.List;
import java.util.Random;

/**
 * Builds the mod's "neo-medieval modern" settlements out of the custom {@link ModBlocks}
 * palette: huge fortified capitals, modern suburb cities, medieval towns and small villages.
 *
 * <p>Generation is <b>terrain-adaptive</b>: every building samples the median ground height
 * under its own footprint and rests there with a foundation skirt down to the surrounding
 * surface (no settlement-wide flatten), and streets follow the terrain — exactly how vanilla
 * villages settle onto the landscape. If the operator has captured real builds with
 * {@code /settlement capture}, those templates are spliced in instead of code-drawn houses.
 */
public final class SettlementGenerator {

    private static final String[] NAMES = {
            "Oldswinford", "Stourbridge", "Dudley", "Wollaston", "Amblecote", "Kinver",
            "Halesowen", "Cradley", "Wordsley", "Brierley", "Pedmore", "Norton",
            "Lye", "Quarry Bank", "Hagley", "Clent", "Sedgley", "Gornal",
            "Wombourne", "Kingswinford", "Tipton", "Netherton", "Bilston", "Wednesbury"
    };

    /** Captured templates the generator will use for plots, refreshed at settlement start. */
    private static List<String> houseTemplates = List.of();

    private SettlementGenerator() {}

    public static String pickName(Random rng) {
        return NAMES[rng.nextInt(NAMES.length)];
    }

    private static String dimId(ServerLevel level) {
        return level.dimension().identifier().toString();
    }

    private static int surf(ServerLevel level, int x, int z) {
        return Build.groundY(level, x, z);
    }

    // ------------------------------------------------------------------
    // Public entry points
    // ------------------------------------------------------------------

    /** Builds a capital castle at the given centre and registers it. Returns the settlement. */
    public static Settlement generateCapital(ServerLevel level, int cx, int cz, String name) {
        return generate(level, cx, cz, SettlementType.CAPITAL, name);
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
        // Pick up any operator-captured building templates for this run.
        houseTemplates = StructureIO.list();

        int half = sizeFor(type, rng);
        // Settlement reference height: median ground at the heart, for the record + civic centre.
        int baseY = Math.max(level.getMinY() + 6, Build.medianGround(level, cx - 8, cz - 8, cx + 8, cz + 8));
        switch (type) {
            case CAPITAL -> buildCastle(level, cx, cz, baseY, half, rng);
            case CITY -> buildCity(level, cx, cz, half, rng);
            case TOWN -> buildTown(level, cx, cz, half, rng);
            case VILLAGE -> buildVillage(level, cx, cz, half, rng);
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
            case CAPITAL -> roll < 0.2 ? 48 + rng.nextInt(16) : roll < 0.8 ? 64 + rng.nextInt(32) : 96 + rng.nextInt(16);
            case CITY    -> roll < 0.25 ? 36 + rng.nextInt(12) : roll < 0.85 ? 48 + rng.nextInt(24) : 72 + rng.nextInt(12);
            case TOWN    -> roll < 0.3 ? 18 + rng.nextInt(8) : 26 + rng.nextInt(16);
            case VILLAGE -> roll < 0.35 ? 9 + rng.nextInt(5) : 14 + rng.nextInt(8);
        };
    }

    // ------------------------------------------------------------------
    // Capital castle — a HUGE fortified town on a fitted platform
    // ------------------------------------------------------------------

    private static void buildCastle(ServerLevel level, int cx, int cz, int baseY, int half, Random rng) {
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        int palaceHalf = Math.max(9, Math.min(24, half / 5));
        int wallH = 10 + half / 40;

        // Fit a fortified platform to the terrain: foundation skirt down to ground on every edge
        // (a natural-looking retaining base), interior cleared, courtyard paved at platform level.
        Build.prepFootprint(level, x0 - 1, z0 - 1, x1 + 1, z1 + 1, baseY, wallH + 24, ModBlocks.CASTLE_BRICKS);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, ModBlocks.COBBLE_STREET);

        // Double-thick curtain wall with a walkable rampart + crenellations.
        Build.walls(level, x0, z0, x1, z1, baseY, wallH, ModBlocks.CASTLE_BRICKS);
        Build.walls(level, x0 + 1, z0 + 1, x1 - 1, z1 - 1, baseY, wallH, ModBlocks.CASTLE_BRICKS);
        Build.borderRing(level, x0, z0, x1, z1, baseY + wallH - 1, 2, ModBlocks.CASTLE_BRICKS);
        Build.crenellate(level, x0, z0, x1, z1, baseY + wallH, ModBlocks.CASTLE_BRICKS);
        for (int x = x0 + 4; x <= x1 - 4; x += 8) {
            Build.set(level, x, baseY + wallH, z0 + 1, ModBlocks.STREET_LAMP);
            Build.set(level, x, baseY + wallH, z1 - 1, ModBlocks.STREET_LAMP);
        }

        // Corner towers + mid-wall towers on big castles.
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
        // A causeway from the gate down to the terrain so you can walk in.
        Build.surfaceRoad(level, cx - 2, z1 + 1, cx + 2, z1 + 10, ModBlocks.PAVED_ROAD);

        // Packed inner districts on the platform, reserving the centre for the palace.
        int m = Math.max(6, half / 12);
        districtFill(level, x0 + m, z0 + m, x1 - m, z1 - m, baseY, 12, cx, cz, palaceHalf + 2,
                rng, ModBlocks.COBBLE_STREET, false, true);

        // Grand processional avenue from the gate to the palace steps.
        Build.floor(level, cx - 2, cz + palaceHalf, cx + 2, z1 - 1, baseY - 1, ModBlocks.PAVED_ROAD);
        Build.clear(level, cx - 2, baseY, cz + palaceHalf, cx + 2, baseY + 7, z1 - 1);
        for (int z = cz + palaceHalf + 2; z <= z1 - 3; z += 6) {
            lampPost(level, cx - 3, baseY, z);
            lampPost(level, cx + 3, baseY, z);
        }

        // The royal palace — governance node — at the heart of the bailey.
        buildKeep(level, cx, cz, baseY, palaceHalf, 18 + palaceHalf, rng);

        Build.garden(level, cx - palaceHalf + 2, cz + 4, cx - 4, cz + palaceHalf - 1, baseY, rng);
        Build.garden(level, cx + 4, cz + 4, cx + palaceHalf - 2, cz + palaceHalf - 1, baseY, rng);
    }

    /** A grand multi-storey palace/keep used as the capital governance node. */
    private static void buildKeep(ServerLevel level, int cx, int cz, int baseY, int half, int height, Random rng) {
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        Build.prepFootprint(level, x0, z0, x1, z1, baseY, height + 8, ModBlocks.CASTLE_BRICKS);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, ModBlocks.CASTLE_BRICKS);
        Build.walls(level, x0, z0, x1, z1, baseY, height, ModBlocks.CASTLE_BRICKS);

        Build.cube(level, x0, baseY, z0, x0, baseY + height, z0, ModBlocks.CASTLE_PILLAR);
        Build.cube(level, x1, baseY, z0, x1, baseY + height, z0, ModBlocks.CASTLE_PILLAR);
        Build.cube(level, x0, baseY, z1, x0, baseY + height, z1, ModBlocks.CASTLE_PILLAR);
        Build.cube(level, x1, baseY, z1, x1, baseY + height, z1, ModBlocks.CASTLE_PILLAR);

        for (int fy = baseY + 5; fy < baseY + height; fy += 5) {
            Build.floor(level, x0 + 1, z0 + 1, x1 - 1, z1 - 1, fy, ModBlocks.CASTLE_BRICKS);
        }
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
        Build.floor(level, x0, z0, x1, z1, baseY + height, ModBlocks.CASTLE_BRICKS);
        Build.crenellate(level, x0, z0, x1, z1, baseY + height + 1, ModBlocks.CASTLE_PILLAR);
        Build.set(level, x0 + 1, baseY + height + 2, z0 + 1, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, x1 - 1, baseY + height + 2, z0 + 1, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, x0 + 1, baseY + height + 2, z1 - 1, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, x1 - 1, baseY + height + 2, z1 - 1, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, cx, baseY + height + 3, cz, ModBlocks.ROYAL_BANNER_BLOCK);

        Build.clear(level, cx - 2, baseY, z1, cx + 2, baseY + 4, z1);
        for (int dx = -2; dx <= 2; dx++) Build.stair(level, cx + dx, baseY - 1, z1 + 1, Blocks.DARK_OAK_STAIRS, Direction.NORTH);

        Build.floor(level, cx - 3, cz - 3, cx + 3, cz + 3, baseY, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, cx, baseY + 1, cz, ModBlocks.CIVIC_MARKER);
        Build.set(level, cx - 4, baseY + 1, cz - 4, ModBlocks.STREET_LAMP);
        Build.set(level, cx + 4, baseY + 1, cz - 4, ModBlocks.STREET_LAMP);
        Build.set(level, cx - 4, baseY + 1, cz + 4, ModBlocks.STREET_LAMP);
        Build.set(level, cx + 4, baseY + 1, cz + 4, ModBlocks.STREET_LAMP);
    }

    private static void cornerTower(ServerLevel level, int x, int z, int baseY, int h) {
        int s = 6;
        Build.prepFootprint(level, x, z, x + s, z + s, baseY, h + 2, ModBlocks.CASTLE_PILLAR);
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
        Build.prepFootprint(level, x, z, x + s, z + s, baseY, h + 1, ModBlocks.CASTLE_BRICKS);
        Build.walls(level, x, z, x + s, z + s, baseY, h, ModBlocks.CASTLE_BRICKS);
        Build.crenellate(level, x, z, x + s, z + s, baseY + h, ModBlocks.CASTLE_BRICKS);
        Build.set(level, x + s / 2, baseY + h, z + s / 2, ModBlocks.STREET_LAMP);
    }

    // ------------------------------------------------------------------
    // Modern suburb city
    // ------------------------------------------------------------------

    private static void buildCity(ServerLevel level, int cx, int cz, int half, Random rng) {
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        int plaza = Math.max(11, Math.min(24, half / 4));

        // Varied modern blocks on a 14-block street grid that follows the terrain.
        districtFill(level, x0, z0, x1, z1, 0, 14, cx, cz, plaza, rng, ModBlocks.PAVED_ROAD, true, false);

        // Central civic plaza on a fitted pad: park, fountain and the town hall.
        int plazaY = Math.max(level.getMinY() + 6, Build.medianGround(level, cx - plaza, cz - plaza, cx + plaza, cz + plaza));
        Build.prepFootprint(level, cx - plaza, cz - plaza, cx + plaza, cz + plaza, plazaY, 6, ModBlocks.PAVED_ROAD);
        Build.floor(level, cx - plaza, cz - plaza, cx + plaza, cz + plaza, plazaY - 1, ModBlocks.PAVED_ROAD);
        Build.garden(level, cx - plaza + 1, cz + 5, cx - 5, cz + plaza - 1, plazaY, rng);
        Build.garden(level, cx + 5, cz + 5, cx + plaza - 1, cz + plaza - 1, plazaY, rng);
        Build.tree(level, cx - plaza + 2, plazaY, cz + plaza - 3);
        Build.tree(level, cx + plaza - 2, plazaY, cz + plaza - 3);
        fountain(level, cx, cz - plaza + 2, plazaY);
        buildTownHall(level, cx, cz, plazaY, rng);
    }

    private static void buildModernBuilding(ServerLevel level, int x, int z, int w, int d, int floors, Random rng) {
        int x1 = x + w - 1, z1 = z + d - 1;
        int baseY = Build.medianGround(level, x, z, x1, z1);
        int h = floors * 3;
        Block facade = rng.nextInt(4) == 0 ? ModBlocks.TOWN_HALL_BRICKS : ModBlocks.MODERN_FACADE;
        Build.prepFootprint(level, x, z, x1, z1, baseY, h + 2, facade);
        Build.walls(level, x, z, x1, z1, baseY, h, facade);
        Build.floor(level, x, z, x1, z1, baseY - 1, facade);
        Build.floor(level, x, z, x1, z1, baseY + h, facade);
        for (int f = 0; f < floors; f++) {
            int wy = baseY + 1 + f * 3;
            for (int wx = x + 1; wx < x1; wx++) {
                Build.set(level, wx, wy, z, ModBlocks.MODERN_WINDOW);
                Build.set(level, wx, wy, z1, ModBlocks.MODERN_WINDOW);
                Build.set(level, wx, wy + 1, z, ModBlocks.MODERN_WINDOW);
                Build.set(level, wx, wy + 1, z1, ModBlocks.MODERN_WINDOW);
            }
            for (int wz = z + 1; wz < z1; wz++) {
                Build.set(level, x, wy, wz, ModBlocks.MODERN_WINDOW);
                Build.set(level, x1, wy, wz, ModBlocks.MODERN_WINDOW);
                Build.set(level, x, wy + 1, wz, ModBlocks.MODERN_WINDOW);
                Build.set(level, x1, wy + 1, wz, ModBlocks.MODERN_WINDOW);
            }
        }
        Build.clear(level, x + w / 2, baseY, z, x + w / 2, baseY + 1, z);
        Build.set(level, x + w / 2, baseY + h + 1, z + d / 2, ModBlocks.STREET_LAMP);
    }

    private static void buildTownHall(ServerLevel level, int cx, int cz, int baseY, Random rng) {
        int half = 8;
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        Build.prepFootprint(level, x0, z0, x1, z1, baseY, 16, ModBlocks.TOWN_HALL_BRICKS);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, ModBlocks.TOWN_HALL_BRICKS);
        Build.walls(level, x0, z0, x1, z1, baseY, 11, ModBlocks.TOWN_HALL_BRICKS);
        Build.floor(level, x0, z0, x1, z1, baseY + 11, ModBlocks.TOWN_HALL_BRICKS);
        Build.crenellate(level, x0, z0, x1, z1, baseY + 12, ModBlocks.CASTLE_PILLAR);
        for (int x = x0; x <= x1; x += 2) Build.cube(level, x, baseY, z1, x, baseY + 8, z1, ModBlocks.CASTLE_PILLAR);
        Build.clear(level, cx - 1, baseY, z1, cx + 1, baseY + 4, z1);
        Build.cube(level, cx - 1, baseY + 12, cz, cx + 1, baseY + 16, cz, ModBlocks.TOWN_HALL_BRICKS);
        Build.set(level, cx, baseY + 17, cz, ModBlocks.STREET_LAMP);
        for (int x = x0 + 2; x < x1; x += 2) {
            Build.set(level, x, baseY + 3, z0, ModBlocks.MODERN_WINDOW);
            Build.set(level, x, baseY + 6, z0, ModBlocks.MODERN_WINDOW);
        }
        Build.set(level, cx, baseY + 1, cz, ModBlocks.CIVIC_MARKER);
        Build.set(level, cx, baseY + 10, cz, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, cx - 3, baseY + 1, cz, ModBlocks.STREET_LAMP);
        Build.set(level, cx + 3, baseY + 1, cz, ModBlocks.STREET_LAMP);
    }

    // ------------------------------------------------------------------
    // Medieval town & village
    // ------------------------------------------------------------------

    private static void buildTown(ServerLevel level, int cx, int cz, int half, Random rng) {
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        int sq = Math.max(8, half / 4);
        districtFill(level, x0, z0, x1, z1, 0, 12, cx, cz, sq + 2, rng, Blocks.GRASS_BLOCK, false, false);

        int sqY = Math.max(level.getMinY() + 6, Build.medianGround(level, cx - sq, cz - sq, cx + sq, cz + sq));
        Build.prepFootprint(level, cx - sq, cz - sq, cx + sq, cz + sq, sqY, 4, ModBlocks.COBBLE_STREET);
        Build.floor(level, cx - sq, cz - sq, cx + sq, cz + sq, sqY - 1, ModBlocks.COBBLE_STREET);
        buildTownHall(level, cx, cz, sqY, rng);
        well(level, cx + sq - 2, cz + sq - 2, sqY);
        Build.tree(level, cx - sq + 1, sqY, cz + sq - 1);
        Build.tree(level, cx - sq + 1, sqY, cz - sq + 1);
    }

    private static void buildVillage(ServerLevel level, int cx, int cz, int half, Random rng) {
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        districtFill(level, x0, z0, x1, z1, 0, 10, cx, cz, 5, rng, Blocks.GRASS_BLOCK, false, false);

        int gY = Math.max(level.getMinY() + 6, Build.medianGround(level, cx - 4, cz - 4, cx + 4, cz + 4));
        Build.prepFootprint(level, cx - 4, cz - 4, cx + 4, cz + 4, gY, 3, ModBlocks.COBBLE_STREET);
        Build.floor(level, cx - 4, cz - 4, cx + 4, cz + 4, gY - 1, ModBlocks.COBBLE_STREET);
        Build.cube(level, cx - 1, gY, cz - 1, cx + 1, gY, cz + 1, ModBlocks.TOWN_HALL_BRICKS);
        Build.set(level, cx, gY + 1, cz, ModBlocks.CIVIC_MARKER);
        well(level, cx + 3, cz + 3, gY);
        Build.garden(level, cx - 4, cz - 4, cx - 1, cz - 1, gY, rng);
        Build.tree(level, cx - 3, gY, cz + 3);
        lampPost(level, cx - 3, gY, cz - 3);
        lampPost(level, cx + 3, gY, cz - 3);
    }

    // ------------------------------------------------------------------
    // District filler — terrain-following street grid + plots of buildings
    // ------------------------------------------------------------------

    /**
     * Lays a terrain-following street grid over a rectangle and fills each plot with a building,
     * skipping any plot that overlaps the reserved central box. {@code modern} chooses glassy
     * towers vs. timbered houses; {@code platform} (castle baileys) keeps streets on a flat pad.
     */
    private static void districtFill(ServerLevel level, int x0, int z0, int x1, int z1, int platformY,
                                     int step, int rcx, int rcz, int rHalf, Random rng,
                                     Block ground, boolean modern, boolean platform) {
        Block street = modern ? ModBlocks.PAVED_ROAD : ModBlocks.COBBLE_STREET;
        // Two-wide streets read clearly as roads. On a castle platform they sit at platformY;
        // out in the world they follow the surface.
        for (int gx = x0; gx <= x1; gx += step) {
            if (platform) {
                Build.cube(level, gx, platformY - 1, z0, Math.min(gx + 1, x1), platformY - 1, z1, street);
            } else {
                Build.surfaceRoad(level, gx, z0, Math.min(gx + 1, x1), z1, street);
            }
        }
        for (int gz = z0; gz <= z1; gz += step) {
            if (platform) {
                Build.cube(level, x0, platformY - 1, gz, x1, platformY - 1, Math.min(gz + 1, z1), street);
            } else {
                Build.surfaceRoad(level, x0, gz, x1, Math.min(gz + 1, z1), street);
            }
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
                    buildModernBuilding(level, hx0, hz0, w, d, floors, rng);
                    lampPost(level, px, 0, pz);
                } else if (rng.nextInt(7) == 0) {
                    int gY = surf(level, hx0 + w / 2, hz0 + d / 2);
                    Build.garden(level, hx0, hz0, hx0 + w - 1, hz0 + d - 1, gY, rng);
                    Build.tree(level, hx0 + w / 2, gY, hz0 + d / 2);
                } else if (!plotFromTemplate(level, hx0, hz0, w, d, rng)) {
                    buildHouse(level, hx0, hz0, w, d, rng);
                    lampPost(level, px, 0, pz);
                }
            }
        }
    }

    /** Splices a captured template into a plot if one fits; returns true if it placed one. */
    private static boolean plotFromTemplate(ServerLevel level, int x, int z, int w, int d, Random rng) {
        if (houseTemplates.isEmpty() || rng.nextInt(2) != 0) return false;
        String name = houseTemplates.get(rng.nextInt(houseTemplates.size()));
        var template = StructureIO.load(level, name);
        if (template == null) return false;
        var dim = template.getSize();
        if (dim.getX() > w + 2 || dim.getZ() > d + 2) return false; // doesn't fit this plot
        int baseY = Build.medianGround(level, x, z, x + dim.getX(), z + dim.getZ());
        Build.prepFootprint(level, x, z, x + dim.getX() - 1, z + dim.getZ() - 1, baseY, dim.getY() + 1, ModBlocks.CASTLE_BRICKS);
        Rotation rot = switch (rng.nextInt(4)) {
            case 1 -> Rotation.CLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
        StructureIO.place(level, template, new BlockPos(x, baseY - 1, z), rot);
        return true;
    }

    // ------------------------------------------------------------------
    // Shared pieces
    // ------------------------------------------------------------------

    private static final Block[] HOUSE_WALLS = {
            ModBlocks.TOWN_HALL_BRICKS, ModBlocks.CASTLE_BRICKS, Blocks.OAK_PLANKS, ModBlocks.MODERN_FACADE
    };

    private static void buildHouse(ServerLevel level, int x, int z, int w, int d, Random rng) {
        Block wall = HOUSE_WALLS[rng.nextInt(HOUSE_WALLS.length)];
        boolean twoStorey = w >= 7 && d >= 7 && rng.nextInt(3) == 0;
        int wallH = twoStorey ? 7 : 4;
        int x1 = x + w - 1, z1 = z + d - 1;
        int roofRise = w / 2 + 1;
        int baseY = Build.medianGround(level, x, z, x1, z1);

        Build.prepFootprint(level, x, z, x1, z1, baseY, wallH + roofRise + 2, ModBlocks.CASTLE_BRICKS);
        Build.floor(level, x, z, x1, z1, baseY - 1, Blocks.OAK_PLANKS);
        Build.walls(level, x, z, x1, z1, baseY, wallH, wall);
        if (twoStorey) Build.floor(level, x + 1, z + 1, x1 - 1, z1 - 1, baseY + 3, Blocks.OAK_PLANKS);
        Build.floor(level, x, z, x1, z1, baseY + wallH, Blocks.OAK_PLANKS);
        Build.gableRoof(level, x, z, x1, z1, baseY + wallH, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_PLANKS);

        Build.clear(level, x + w / 2, baseY, z, x + w / 2, baseY + 1, z);
        houseWindows(level, x, z, x1, z1, baseY + 1);
        if (twoStorey) houseWindows(level, x, z, x1, z1, baseY + 4);

        Build.set(level, x + 1, baseY + wallH - 1, z + 1, ModBlocks.STREET_LAMP);
        if (twoStorey) Build.set(level, x + 1, baseY + 2, z + 1, ModBlocks.STREET_LAMP);
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

    /** A lamp post that stands on the local surface (terrain-following). */
    private static void lampPost(ServerLevel level, int x, int baseYHint, int z) {
        int baseY = baseYHint > 0 ? baseYHint : surf(level, x, z);
        Build.set(level, x, baseY, z, ModBlocks.CASTLE_PILLAR);
        Build.set(level, x, baseY + 1, z, ModBlocks.CASTLE_PILLAR);
        Build.set(level, x, baseY + 2, z, ModBlocks.CASTLE_PILLAR);
        Build.set(level, x, baseY + 3, z, ModBlocks.STREET_LAMP);
    }
}
