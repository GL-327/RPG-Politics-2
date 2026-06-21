package com.political.world.structures;

import com.political.RpgPoliticsMod;
import com.political.world.Build;
import com.political.world.BuildBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Random;

/**
 * Builder + placer for the content workstream's original surface structures
 * ({@link ContentStructureKind}). Each layout is authored from the shared {@link Build} /
 * {@link SurfaceGenUtil} primitives, captured into a {@link com.political.world.BuildBuffer}, then
 * streamed into the world — exactly the deferred build pipeline used by the settlement, dungeon and
 * surface-structure systems. Post-placement loot/mob hooks reuse {@link StructureLoot}.
 *
 * <p>This class is self-contained: it does not modify {@link StructureManager} or
 * {@link StructureGenerator}. To hook it into auto-scatter, see the integration manifest.
 */
public final class ContentStructures {

    private ContentStructures() {}

    /**
     * Generates {@code kind} centred on {@code (cx, cz)}, drains the build buffer into {@code level}
     * immediately and applies its loot/population hooks. Returns the recorded site.
     */
    public static StructureSite placeNow(ServerLevel level, int cx, int cz, ContentStructureKind kind) {
        Random rng = new Random(kind.id.hashCode() ^ ((long) cx << 32) ^ cz);
        BuildBuffer buffer = new BuildBuffer();
        StructurePlan plan = planInto(buffer, level, cx, cz, kind, rng);

        // Stream the captured buffer into the world (same op shape as StructureManager.drainPending).
        for (BuildBuffer.Op op : plan.buffer.ops) {
            level.setBlock(new BlockPos(op.x, op.y, op.z), op.state, 2);
        }
        StructureLoot.applyPostOps(level, plan);

        RpgPoliticsMod.LOGGER.info("Content structure {} placed at {}, {}, {} ({} blocks).",
                kind.id, plan.site.x, plan.site.y, plan.site.z, plan.buffer.size());
        return plan.site;
    }

    /** Captures {@code kind}'s layout into {@code buffer} and returns its plan (deferred build). */
    public static StructurePlan planInto(BuildBuffer buffer, ServerLevel level, int cx, int cz,
                                         ContentStructureKind kind, Random rng) {
        int floorY = Math.max(level.getSeaLevel() - 1, Build.medianGround(level, cx - 8, cz - 8, cx + 8, cz + 8));
        String dim = level.dimension().identifier().toString();
        StructureSite site = new StructureSite(dim, cx, floorY, cz, kind.loot, System.currentTimeMillis());
        StructurePlan plan = new StructurePlan(buffer, site);

        Build.beginDeferred(buffer);
        try {
            switch (kind) {
                case WAYSTONE_SHRINE -> waystoneShrine(plan, level, cx, floorY, cz, kind, rng);
                case RANGER_OUTPOST -> rangerOutpost(plan, level, cx, floorY, cz, kind, rng);
                case LEYLINE_NEXUS -> leylineNexus(plan, level, cx, floorY, cz, kind, rng);
            }
        } finally {
            Build.endDeferred();
        }
        return plan;
    }

    // ------------------------------------------------------------------
    // Waystone shrine — a 5x5 stepped plinth, a runed pillar and a lantern.
    // ------------------------------------------------------------------

    private static void waystoneShrine(StructurePlan plan, ServerLevel level, int cx, int baseY, int cz,
                                       ContentStructureKind kind, Random rng) {
        int x0 = cx - 2, z0 = cz - 2, x1 = cx + 2, z1 = cz + 2;
        SurfaceGenUtil.prep(level, x0, z0, x1, z1, baseY, 6, Blocks.STONE_BRICKS);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, Blocks.STONE_BRICKS);
        Build.floor(level, x0 + 1, z0 + 1, x1 - 1, z1 - 1, baseY, Blocks.CHISELED_STONE_BRICKS);

        // Central runed waystone column.
        SurfaceGenUtil.pillar(level, cx, baseY + 1, cz, 3, StructureBlocks.RUNED_STONE);
        Build.set(level, cx, baseY + 4, cz, Blocks.LANTERN);
        Build.set(level, cx, baseY + 1, cz - 1, Blocks.DECORATED_POT);

        // Four corner candles + a guiding banner.
        Build.set(level, x0, baseY, z0, Blocks.CANDLE);
        Build.set(level, x1, baseY, z0, Blocks.CANDLE);
        Build.set(level, x0, baseY, z1, Blocks.CANDLE);
        Build.set(level, x1, baseY, z1, Blocks.CANDLE);
        SurfaceGenUtil.bannerPost(level, x1 + 1, baseY, z1 + 1, 3, StructureBlocks.WAR_BANNER);

        SurfaceGenUtil.chest(plan, level, cx + 1, baseY, cz + 1, kind.loot.lootTable);
    }

    // ------------------------------------------------------------------
    // Ranger outpost — a small log cabin with a gable roof + a friendly ranger.
    // ------------------------------------------------------------------

    private static void rangerOutpost(StructurePlan plan, ServerLevel level, int cx, int baseY, int cz,
                                      ContentStructureKind kind, Random rng) {
        int x0 = cx - 3, z0 = cz - 3, x1 = cx + 3, z1 = cz + 3;
        Block wall = Blocks.SPRUCE_LOG, floor = Blocks.SPRUCE_PLANKS;

        SurfaceGenUtil.prep(level, x0, z0, x1, z1, baseY, 7, Blocks.COBBLESTONE);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, floor);
        Build.walls(level, x0, z0, x1, z1, baseY, 4, wall);
        Build.floor(level, x0, z0, x1, z1, baseY + 4, floor);
        Build.gableRoof(level, x0, z0, x1, z1, baseY + 4, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_PLANKS);
        SurfaceGenUtil.doorway(level, cx, baseY, z1, Blocks.SPRUCE_DOOR, true);

        // Windows.
        Build.set(level, x0, baseY + 2, cz, Blocks.GLASS_PANE);
        Build.set(level, x1, baseY + 2, cz, Blocks.GLASS_PANE);

        // Furnishings: bed, crafting table, fletching table, a hearth out front.
        Build.set(level, x0 + 1, baseY, z0 + 1, VanillaBlocks.RED_BED);
        Build.set(level, x1 - 1, baseY, z0 + 1, Blocks.CRAFTING_TABLE);
        Build.set(level, x1 - 1, baseY, z0 + 2, Blocks.FLETCHING_TABLE);
        SurfaceGenUtil.campfire(level, cx + 2, baseY, z1 + 2);
        SurfaceGenUtil.bannerPost(level, x0 - 1, baseY, z1 + 1, 3, StructureBlocks.WAR_BANNER);

        SurfaceGenUtil.chest(plan, level, x0 + 1, baseY, z1 - 1, kind.loot.lootTable);
        SurfaceGenUtil.villager(plan, cx, baseY + 1, cz);
    }

    // ------------------------------------------------------------------
    // Leyline nexus — a ring of arcane stone pillars crowned with amethyst.
    // ------------------------------------------------------------------

    private static void leylineNexus(StructurePlan plan, ServerLevel level, int cx, int baseY, int cz,
                                     ContentStructureKind kind, Random rng) {
        int x0 = cx - 3, z0 = cz - 3, x1 = cx + 3, z1 = cz + 3;
        Block stone = StructureBlocks.RUNED_STONE;

        SurfaceGenUtil.prep(level, x0, z0, x1, z1, baseY, 7, Blocks.POLISHED_DEEPSLATE);
        Build.floor(level, x0, z0, x1, z1, baseY - 1, Blocks.POLISHED_DEEPSLATE);
        Build.floor(level, x0 + 1, z0 + 1, x1 - 1, z1 - 1, baseY, Blocks.DEEPSLATE_TILES);

        // Four corner obelisk pillars topped with amethyst.
        int[][] corners = { {x0, z0}, {x1, z0}, {x0, z1}, {x1, z1} };
        for (int[] c : corners) {
            SurfaceGenUtil.pillar(level, c[0], baseY, c[1], 5, stone);
            Build.set(level, c[0], baseY + 5, c[1], Blocks.AMETHYST_BLOCK);
            Build.set(level, c[0], baseY + 6, c[1], Blocks.AMETHYST_CLUSTER);
        }

        // Central focus: a budding amethyst altar over a small dais.
        Build.set(level, cx, baseY, cz, Blocks.ENCHANTING_TABLE);
        Build.set(level, cx, baseY + 1, cz, Blocks.BUDDING_AMETHYST);
        Build.set(level, cx, baseY + 2, cz, Blocks.AMETHYST_CLUSTER);
        Build.set(level, cx - 2, baseY, cz, Blocks.AMETHYST_BLOCK);
        Build.set(level, cx + 2, baseY, cz, Blocks.AMETHYST_BLOCK);

        SurfaceGenUtil.chest(plan, level, cx + 1, baseY, cz + 1, kind.loot.lootTable);
        SurfaceGenUtil.cursedSpirit(plan, cx, baseY + 1, cz - 2, 1);
    }
}
