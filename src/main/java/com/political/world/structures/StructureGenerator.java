package com.political.world.structures;

import com.political.world.Build;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Random;

/**
 * Orchestrates the per-archetype above-ground layouts. Each builder rests on the terrain via
 * the shared {@link Build} primitives (no settlement-wide flatten — a foundation skirt + cleared
 * air per footprint, exactly like the settlement & dungeon generators) and registers loot /
 * population hooks onto the {@link StructurePlan} for {@link StructureLoot} to apply after the
 * build streams in.
 */
public final class StructureGenerator {

    private StructureGenerator() {}

    private static void maybeBoss(StructurePlan plan, int x, int y, int z, StructureType type, Random rng) {
        if (type.mobBoss == null) return;
        if (rng.nextDouble() >= com.political.config.PoliticalConfig.get().structureBossSpawnChance) return;
        SurfaceGenUtil.mob(plan, x, y, z, type.mobBoss);
    }

    public static StructurePlan planInto(com.political.world.BuildBuffer buffer, ServerLevel level,
                                         int cx, int cz, StructureType type, Random rng) {
        int floorY = Math.max(level.getSeaLevel() - 1, Build.medianGround(level, cx - 8, cz - 8, cx + 8, cz + 8));
        String dim = level.dimension().identifier().toString();
        StructureSite site = new StructureSite(dim, cx, floorY, cz, type, System.currentTimeMillis());
        StructurePlan plan = new StructurePlan(buffer, site);

        Build.beginDeferred(buffer);
        try {
            switch (type) {
                case SORCERER_WATCHTOWER -> watchtower(plan, level, cx, floorY, cz, type, rng, 14);
                case MAGE_TOWER -> mageTower(plan, level, cx, floorY, cz, type, rng);
                case HERO_OUTPOST -> heroOutpost(plan, level, cx, floorY, cz, type, rng);
                case BANDIT_CAMP -> banditCamp(plan, level, cx, floorY, cz, type, rng);
                case CURSED_SHRINE -> cursedShrine(plan, level, cx, floorY, cz, type, rng);
                case ABANDONED_MANOR -> abandonedManor(plan, level, cx, floorY, cz, type, rng);
                case TRADING_POST -> tradingPost(plan, level, cx, floorY, cz, type, rng);
                case ELECTION_HALL_RUIN -> electionHallRuin(plan, level, cx, floorY, cz, type, rng);
                case BATTLEFIELD -> battlefield(plan, level, cx, floorY, cz, type, rng);
                case OBELISK -> obelisk(plan, level, cx, floorY, cz, type, rng);
                case WANDERING_MERCHANT -> wanderingMerchant(plan, level, cx, floorY, cz, type, rng);
            }
        } finally {
            Build.endDeferred();
        }
        return plan;
    }

    // ------------------------------------------------------------------
    // Arcane towers
    // ------------------------------------------------------------------

    private static void watchtower(StructurePlan plan, ServerLevel level, int cx, int baseY, int cz,
                                   StructureType type, Random rng, int height) {
        int half = 3;
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        Block wall = type.wallBlock(), floor = type.floorBlock(), accent = type.accentBlock();

        SurfaceGenUtil.prep(level, x0, z0, x1, z1, baseY, height + 4, wall);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, floor);
        Build.walls(level, x0, z0, x1, z1, baseY, height, wall);

        // Interior floors + windows + lantern columns
        for (int fy = baseY + 4; fy < baseY + height; fy += 4) {
            Build.floor(level, x0 + 1, z0 + 1, x1 - 1, z1 - 1, fy, floor);
            for (int x = x0 + 1; x < x1; x += 2) {
                Build.set(level, x, fy + 1, z0, Blocks.GLASS_PANE);
                Build.set(level, x, fy + 1, z1, Blocks.GLASS_PANE);
            }
        }
        SurfaceGenUtil.doorway(level, cx, baseY, z1, Blocks.SPRUCE_DOOR, true);

        // Battlements + arcane crown
        Build.floor(level, x0, z0, x1, z1, baseY + height, wall);
        Build.crenellate(level, x0, z0, x1, z1, baseY + height + 1, wall);
        Build.set(level, cx, baseY + height + 1, cz, accent);
        Build.set(level, cx, baseY + height + 2, cz, Blocks.AMETHYST_CLUSTER);

        // Loot + ambient sorcerers
        SurfaceGenUtil.chest(plan, level, x0 + 1, baseY + 1, z0 + 1, type.lootTable);
        SurfaceGenUtil.chest(plan, level, x1 - 1, baseY + 4 + 1, z1 - 1, type.lootTable);
        SurfaceGenUtil.spawner(plan, level, cx, baseY + 1, cz, type.mobCommon);
        SurfaceGenUtil.mob(plan, cx, baseY + height + 2, cz, type.mobElite);
        SurfaceGenUtil.bannerPost(level, x0 - 1, baseY, z0 - 1, 4, StructureBlocks.WAR_BANNER);
    }

    private static void mageTower(StructurePlan plan, ServerLevel level, int cx, int baseY, int cz,
                                  StructureType type, Random rng) {
        watchtower(plan, level, cx, baseY, cz, type, rng, 20);
        Block floor = type.floorBlock();
        // Arcane study at the base: enchanting table + bookshelves + brewing ambience
        Build.set(level, cx, baseY + 1, cz - 2, Blocks.ENCHANTING_TABLE);
        Build.set(level, cx - 2, baseY + 1, cz - 2, Blocks.BOOKSHELF);
        Build.set(level, cx + 2, baseY + 1, cz - 2, Blocks.BOOKSHELF);
        Build.set(level, cx - 2, baseY + 1, cz + 2, Blocks.BREWING_STAND);
        Build.set(level, cx, baseY + 2, cz, Blocks.AIR);
        Build.floor(level, cx - 1, cz - 1, cx + 1, cz + 1, baseY - 1, floor);
        // The archmage broods at the summit (boss-grade, rare flavour)
        if (type.mobBoss != null) maybeBoss(plan, cx, baseY + 21, cz, type, rng);
        SurfaceGenUtil.chest(plan, level, cx + 1, baseY + 1, cz - 2, type.lootTable);
    }

    // ------------------------------------------------------------------
    // Order: Hero outpost
    // ------------------------------------------------------------------

    private static void heroOutpost(StructurePlan plan, ServerLevel level, int cx, int baseY, int cz,
                                    StructureType type, Random rng) {
        int half = 6;
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        Block wall = type.wallBlock(), floor = type.floorBlock();

        // Palisade courtyard
        SurfaceGenUtil.prep(level, x0, z0, x1, z1, baseY, 8, wall);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, floor);
        Build.walls(level, x0, z0, x1, z1, baseY, 4, wall);
        Build.crenellate(level, x0, z0, x1, z1, baseY + 4, wall);
        SurfaceGenUtil.doorway(level, cx, baseY, z1, Blocks.OAK_DOOR, false);

        // Corner watch posts with banners
        SurfaceGenUtil.bannerPost(level, x0, baseY + 4, z0, 2, StructureBlocks.WAR_BANNER);
        SurfaceGenUtil.bannerPost(level, x1, baseY + 4, z0, 2, StructureBlocks.WAR_BANNER);
        SurfaceGenUtil.bannerPost(level, x0, baseY + 4, z1, 2, StructureBlocks.WAR_BANNER);
        SurfaceGenUtil.bannerPost(level, x1, baseY + 4, z1, 2, StructureBlocks.WAR_BANNER);

        // Barracks building inside
        int bx0 = cx - 3, bz0 = cz - 3, bx1 = cx + 1, bz1 = cz + 1;
        Build.floor(level, bx0, bz0, bx1, bz1, baseY - 1, Blocks.OAK_PLANKS);
        Build.walls(level, bx0, bz0, bx1, bz1, baseY, 3, wall);
        Build.floor(level, bx0, bz0, bx1, bz1, baseY + 3, Blocks.OAK_PLANKS);
        Build.gableRoof(level, bx0, bz0, bx1, bz1, baseY + 3, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_PLANKS);
        SurfaceGenUtil.doorway(level, cx - 1, baseY, bz1, Blocks.OAK_DOOR, true);
        Build.set(level, bx0 + 1, baseY, bz0 + 1, VanillaBlocks.RED_BED);

        // Training props: target dummy + weapon rack
        SurfaceGenUtil.statue(level, cx + 4, baseY, cz - 4, 2, Blocks.HAY_BLOCK, Blocks.CARVED_PUMPKIN);
        Build.set(level, cx + 4, baseY, cz + 4, Blocks.SMITHING_TABLE);
        Build.set(level, cx + 3, baseY, cz + 4, Blocks.GRINDSTONE);
        SurfaceGenUtil.campfire(level, cx + 3, baseY, cz - 3);

        // Loot + friendly garrison (no hostiles)
        SurfaceGenUtil.chest(plan, level, bx0 + 1, baseY, bz1 - 1, type.lootTable);
        SurfaceGenUtil.chest(plan, level, bx1 - 1, baseY, bz0 + 1, type.lootTable);
        for (int i = 0; i < type.villagers; i++) {
            SurfaceGenUtil.villager(plan, cx + rng.nextInt(5) - 2, baseY + 1, cz + rng.nextInt(5) - 2);
        }
    }

    // ------------------------------------------------------------------
    // Bandit camp
    // ------------------------------------------------------------------

    private static void banditCamp(StructurePlan plan, ServerLevel level, int cx, int baseY, int cz,
                                   StructureType type, Random rng) {
        // Stockade ring of oak fence
        int r = 8;
        for (int x = cx - r; x <= cx + r; x++) {
            for (int z = cz - r; z <= cz + r; z++) {
                boolean edge = (x == cx - r || x == cx + r || z == cz - r || z == cz + r);
                if (edge && rng.nextInt(4) != 0) {
                    int g = Build.groundY(level, x, z);
                    Build.set(level, x, g, z, Blocks.OAK_FENCE);
                    if (rng.nextInt(3) == 0) Build.set(level, x, g + 1, z, Blocks.OAK_FENCE);
                }
            }
        }
        SurfaceGenUtil.campfire(level, cx, baseY, cz);
        Build.set(level, cx, baseY + 1, cz, Blocks.AIR);

        // Three tents around the fire
        Block[] cloths = { VanillaBlocks.WHITE_WOOL, VanillaBlocks.BROWN_WOOL, VanillaBlocks.GRAY_WOOL };
        int[][] spots = { {cx - 5, cz - 4}, {cx + 5, cz - 3}, {cx - 1, cz + 5} };
        for (int i = 0; i < spots.length; i++) {
            int tx = spots[i][0], tz = spots[i][1];
            int ty = Build.groundY(level, tx, tz);
            SurfaceGenUtil.tent(level, tx, ty, tz, 2, cloths[i % cloths.length], rng);
            SurfaceGenUtil.chest(plan, level, tx, ty + 1, tz, type.lootTable);
            SurfaceGenUtil.spawner(plan, level, tx + 1, ty + 1, tz, type.mobCommon);
        }
        SurfaceGenUtil.bannerPost(level, cx + 1, baseY, cz + 1, 3, StructureBlocks.WAR_BANNER);

        // Outlaws + their brutes, and the bandit king lording over the loot pile
        SurfaceGenUtil.mob(plan, cx, baseY + 1, cz, type.mobElite);
        SurfaceGenUtil.mob(plan, cx + 2, baseY + 1, cz - 2, type.mobCommon);
        SurfaceGenUtil.mob(plan, cx - 2, baseY + 1, cz + 2, type.mobCommon);
        maybeBoss(plan, cx, baseY + 1, cz - 1, type, rng);
    }

    // ------------------------------------------------------------------
    // Cursed shrine
    // ------------------------------------------------------------------

    private static void cursedShrine(StructurePlan plan, ServerLevel level, int cx, int baseY, int cz,
                                     StructureType type, Random rng) {
        int half = 4;
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        Block wall = type.wallBlock(), floor = type.floorBlock(), accent = type.accentBlock();

        // Raised altar platform
        SurfaceGenUtil.prep(level, x0, z0, x1, z1, baseY, 6, Blocks.POLISHED_BLACKSTONE);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, floor);
        Build.floor(level, x0, z0, x1, z1, baseY, Blocks.POLISHED_BLACKSTONE);

        // Four pillars + soul lanterns
        SurfaceGenUtil.pillar(level, x0, baseY + 1, z0, 4, wall);
        SurfaceGenUtil.pillar(level, x1, baseY + 1, z0, 4, wall);
        SurfaceGenUtil.pillar(level, x0, baseY + 1, z1, 4, wall);
        SurfaceGenUtil.pillar(level, x1, baseY + 1, z1, 4, wall);
        Build.set(level, x0, baseY + 5, z0, accent);
        Build.set(level, x1, baseY + 5, z0, accent);
        Build.set(level, x0, baseY + 5, z1, accent);
        Build.set(level, x1, baseY + 5, z1, accent);

        // Central cursed altar with soul fire
        Build.set(level, cx, baseY + 1, cz, StructureBlocks.CURSED_ALTAR);
        Build.set(level, cx, baseY + 2, cz, Blocks.SOUL_FIRE);
        Build.set(level, cx - 1, baseY + 1, cz, Blocks.CANDLE);
        Build.set(level, cx + 1, baseY + 1, cz, Blocks.CANDLE);

        SurfaceGenUtil.chest(plan, level, cx, baseY + 1, cz + 2, type.lootTable);
        SurfaceGenUtil.mob(plan, cx + 2, baseY + 1, cz - 2, type.mobCommon);
        SurfaceGenUtil.mob(plan, cx - 2, baseY + 1, cz + 2, type.mobElite);
        SurfaceGenUtil.cursedSpirit(plan, cx, baseY + 1, cz - 2, 1);
    }

    // ------------------------------------------------------------------
    // Abandoned manor
    // ------------------------------------------------------------------

    private static void abandonedManor(StructurePlan plan, ServerLevel level, int cx, int baseY, int cz,
                                       StructureType type, Random rng) {
        int half = 6;
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        Block wall = type.wallBlock(), floor = type.floorBlock();

        SurfaceGenUtil.prep(level, x0, z0, x1, z1, baseY, 12, Blocks.COBBLESTONE);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, floor);
        // Two crumbling storeys
        SurfaceGenUtil.ruinedWalls(level, x0, z0, x1, z1, baseY, 4, wall, Blocks.COBWEB, rng);
        Build.floor(level, x0 + 1, z0 + 1, x1 - 1, z1 - 1, baseY + 4, Blocks.SPRUCE_PLANKS);
        SurfaceGenUtil.ruinedWalls(level, x0, z0, x1, z1, baseY + 5, 4, wall, Blocks.COBWEB, rng);
        Build.gableRoof(level, x0, z0, x1, z1, baseY + 9, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_PLANKS);
        SurfaceGenUtil.doorway(level, cx, baseY, z1, Blocks.DARK_OAK_DOOR, false);

        // Decayed furnishings
        Build.set(level, x0 + 1, baseY, z0 + 1, Blocks.COBWEB);
        Build.set(level, x1 - 1, baseY + 4, z1 - 1, Blocks.COBWEB);
        Build.set(level, cx, baseY, cz, Blocks.DEAD_BUSH);
        Build.set(level, x0 + 1, baseY, cz, Blocks.CRAFTING_TABLE);
        Build.set(level, x1 - 1, baseY, cz, Blocks.FLETCHING_TABLE);
        Build.set(level, cx - 2, baseY + 4, cz - 2, VanillaBlocks.RED_BED);

        SurfaceGenUtil.chest(plan, level, x0 + 1, baseY, z0 + 1, type.lootTable);
        SurfaceGenUtil.chest(plan, level, x1 - 1, baseY + 4, z0 + 1, type.lootTable);
        SurfaceGenUtil.mob(plan, cx, baseY + 1, cz, type.mobCommon);
        SurfaceGenUtil.mob(plan, x1 - 2, baseY + 5, z1 - 2, type.mobElite);
    }

    // ------------------------------------------------------------------
    // Trading post
    // ------------------------------------------------------------------

    private static void tradingPost(StructurePlan plan, ServerLevel level, int cx, int baseY, int cz,
                                    StructureType type, Random rng) {
        int half = 5;
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        Block wall = type.wallBlock(), floor = type.floorBlock();

        SurfaceGenUtil.prep(level, x0, z0, x1, z1, baseY, 6, Blocks.COBBLESTONE);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, floor);
        Build.walls(level, x0, z0, x1, z1, baseY, 4, wall);
        Build.floor(level, x0, z0, x1, z1, baseY + 4, Blocks.SPRUCE_PLANKS);
        Build.gableRoof(level, x0, z0, x1, z1, baseY + 4, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_PLANKS);
        SurfaceGenUtil.doorway(level, cx, baseY, z1, Blocks.SPRUCE_DOOR, true);

        // Market counter + barrels of goods
        Build.cube(level, x0 + 1, baseY, cz, x1 - 1, baseY, cz, Blocks.SMOOTH_STONE_SLAB);
        Build.set(level, x0 + 1, baseY, z0 + 1, Blocks.BARREL);
        Build.set(level, x0 + 2, baseY, z0 + 1, Blocks.BARREL);
        Build.set(level, x1 - 1, baseY, z0 + 1, Blocks.FLETCHING_TABLE);
        Build.set(level, cx, baseY, z0 + 1, Blocks.LECTERN);
        SurfaceGenUtil.bannerPost(level, x1 + 1, baseY, z1 + 1, 3, StructureBlocks.WAR_BANNER);
        SurfaceGenUtil.campfire(level, x1 + 2, baseY, cz);

        SurfaceGenUtil.chest(plan, level, x0 + 1, baseY, z0 + 1, type.lootTable);
        SurfaceGenUtil.chest(plan, level, x1 - 1, baseY, z1 - 1, type.lootTable);
        for (int i = 0; i < type.villagers; i++) {
            SurfaceGenUtil.villager(plan, cx + rng.nextInt(5) - 2, baseY + 1, cz + rng.nextInt(5) - 2);
        }
    }

    // ------------------------------------------------------------------
    // Election hall ruin
    // ------------------------------------------------------------------

    private static void electionHallRuin(StructurePlan plan, ServerLevel level, int cx, int baseY, int cz,
                                         StructureType type, Random rng) {
        int half = 6;
        int x0 = cx - half, z0 = cz - half, x1 = cx + half, z1 = cz + half;
        Block wall = type.wallBlock(), floor = type.floorBlock();

        SurfaceGenUtil.prep(level, x0, z0, x1, z1, baseY, 8, Blocks.STONE_BRICKS);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, floor);
        // Colonnade of broken marble columns
        for (int x = x0; x <= x1; x += 2) {
            int hL = 4 - rng.nextInt(2);
            int hR = 4 - rng.nextInt(2);
            SurfaceGenUtil.pillar(level, x, baseY, z0, hL, wall);
            SurfaceGenUtil.pillar(level, x, baseY, z1, hR, wall);
        }
        SurfaceGenUtil.ruinedWalls(level, x0, z0, x1, z1, baseY, 5, wall, Blocks.MOSSY_STONE_BRICKS, rng);

        // Speaker's podium + toppled ballot urn + civic banner
        Build.set(level, cx, baseY, cz, Blocks.LECTERN);
        Build.set(level, cx, baseY - 1, cz, wall);
        Build.set(level, cx - 2, baseY, cz, Blocks.DECORATED_POT);
        Build.set(level, cx + 2, baseY, cz, VanillaBlocks.IRON_CHAIN);
        SurfaceGenUtil.statue(level, cx, baseY, cz - 4, 3, wall, type.accentBlock());

        SurfaceGenUtil.chest(plan, level, cx + 1, baseY, cz + 1, type.lootTable);
        SurfaceGenUtil.chest(plan, level, cx - 2, baseY, cz - 2, type.lootTable);
        SurfaceGenUtil.mob(plan, x1 - 1, baseY + 1, z1 - 1, type.mobCommon);
        for (int i = 0; i < type.villagers; i++) {
            SurfaceGenUtil.villager(plan, cx + rng.nextInt(3) - 1, baseY + 1, cz + 2);
        }
    }

    // ------------------------------------------------------------------
    // Battlefield graveyard
    // ------------------------------------------------------------------

    private static void battlefield(StructurePlan plan, ServerLevel level, int cx, int baseY, int cz,
                                    StructureType type, Random rng) {
        int r = 9;
        // Scatter graves, broken spears (fences) and abandoned shields
        for (int i = 0; i < 16; i++) {
            int gx = cx + rng.nextInt(r * 2 + 1) - r;
            int gz = cz + rng.nextInt(r * 2 + 1) - r;
            int gy = Build.groundY(level, gx, gz);
            SurfaceGenUtil.grave(level, gx, gy, gz, rng);
            if (rng.nextInt(3) == 0) {
                Build.set(level, gx + 1, gy, gz, Blocks.OAK_FENCE);
                Build.set(level, gx + 1, gy + 1, gz, Blocks.OAK_FENCE);
            }
        }
        // Central war monument
        SurfaceGenUtil.statue(level, cx, baseY, cz, 4, type.wallBlock(), type.accentBlock());
        Build.set(level, cx, baseY + 5, cz, Blocks.SOUL_LANTERN);
        SurfaceGenUtil.bannerPost(level, cx + 2, baseY, cz + 2, 3, StructureBlocks.WAR_BANNER);
        SurfaceGenUtil.campfire(level, cx - 2, baseY, cz - 2);

        SurfaceGenUtil.chest(plan, level, cx + 1, baseY, cz, type.lootTable);
        SurfaceGenUtil.spawner(plan, level, cx - 3, baseY, cz + 3, type.mobCommon);
        SurfaceGenUtil.mob(plan, cx + 3, baseY + 1, cz - 3, type.mobElite);
        SurfaceGenUtil.cursedSpirit(plan, cx, baseY + 1, cz + 3, 1);
    }

    // ------------------------------------------------------------------
    // Obelisk
    // ------------------------------------------------------------------

    private static void obelisk(StructurePlan plan, ServerLevel level, int cx, int baseY, int cz,
                                StructureType type, Random rng) {
        Block stone = type.wallBlock();
        // Stepped base
        Build.floor(level, cx - 3, cz - 3, cx + 3, cz + 3, baseY - 1, stone);
        Build.floor(level, cx - 2, cz - 2, cx + 2, cz + 2, baseY, stone);
        // Tapering shaft
        int h = 12;
        for (int y = 0; y < h; y++) {
            int taper = y / 5;
            Build.cube(level, cx - 1 + taper, baseY + 1 + y, cz - 1 + taper,
                    cx + 1 - taper, baseY + 1 + y, cz + 1 - taper, stone);
        }
        Build.set(level, cx, baseY + 1 + h, cz, type.accentBlock());
        Build.set(level, cx, baseY + 2 + h, cz, Blocks.AMETHYST_CLUSTER);
        // Glyph accents
        Build.set(level, cx, baseY + 4, cz - 2, type.accentBlock());
        Build.set(level, cx, baseY + 4, cz + 2, type.accentBlock());

        // Buried offering cache at the base
        SurfaceGenUtil.chest(plan, level, cx + 2, baseY, cz + 2, type.lootTable);
        SurfaceGenUtil.mob(plan, cx - 2, baseY + 1, cz - 2, type.mobCommon);
    }

    // ------------------------------------------------------------------
    // Wandering merchant camp
    // ------------------------------------------------------------------

    private static void wanderingMerchant(StructurePlan plan, ServerLevel level, int cx, int baseY, int cz,
                                          StructureType type, Random rng) {
        SurfaceGenUtil.tent(level, cx, baseY, cz, 2, VanillaBlocks.LIGHT_BLUE_WOOL, rng);
        SurfaceGenUtil.campfire(level, cx + 3, baseY, cz);
        Build.set(level, cx - 2, baseY, cz - 2, Blocks.BARREL);
        Build.set(level, cx - 2, baseY, cz + 2, Blocks.BARREL);
        Build.set(level, cx + 2, baseY, cz - 2, Blocks.HAY_BLOCK);
        SurfaceGenUtil.bannerPost(level, cx - 3, baseY, cz - 3, 3, StructureBlocks.WAR_BANNER);

        SurfaceGenUtil.chest(plan, level, cx, baseY + 1, cz, type.lootTable);
        SurfaceGenUtil.trader(plan, cx + 1, baseY + 1, cz + 1);
        for (int i = 0; i < type.villagers; i++) {
            SurfaceGenUtil.villager(plan, cx + rng.nextInt(3) - 1, baseY + 1, cz + rng.nextInt(3) - 1);
        }
    }
}
