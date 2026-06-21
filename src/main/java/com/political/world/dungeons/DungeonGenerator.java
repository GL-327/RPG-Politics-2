package com.political.world.dungeons;

import com.political.world.Build;
import com.political.world.BuildBuffer;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.Random;

/**
 * Orchestrates per-type dungeon layouts: entrance, branching rooms, corridors,
 * trap halls, spawner wings, and a tier-appropriate boss chamber.
 */
public final class DungeonGenerator {

    private DungeonGenerator() {}

    public static DungeonPlan planInto(BuildBuffer buffer, ServerLevel level, int cx, int cz,
                                         DungeonType type, Random rng) {
        int floorY = DungeonGenUtil.findFloorY(level, cx, cz, type, rng);
        String dim = level.dimension().identifier().toString();
        DungeonSite site = new DungeonSite(dim, cx, floorY, cz, type, System.currentTimeMillis());
        DungeonPlan plan = new DungeonPlan(buffer, site);

        Build.beginDeferred(buffer);
        try {
            generateLayout(plan, level, cx, floorY, cz, type, rng);
        } finally {
            Build.endDeferred();
        }
        return plan;
    }

    private static void generateLayout(DungeonPlan plan, ServerLevel level, int cx, int floorY, int cz,
                                       DungeonType type, Random rng) {
        // Core lobby
        DungeonGenUtil.room(plan, level, cx - 4, floorY, cz - 4, 9, 5, 9, type);
        DungeonGenUtil.carveEntrance(plan, level, cx, floorY, cz - 4, type.wallBlock(), rng);

        switch (type) {
            case CURSED_CRYPT -> layoutCrypt(plan, level, cx, floorY, cz, type, rng);
            case BANDIT_HIDEOUT -> layoutHideout(plan, level, cx, floorY, cz, type, rng);
            case ANCIENT_RUINS -> layoutRuins(plan, level, cx, floorY, cz, type, rng);
            case VILTRUMITE_LAB -> layoutLab(plan, level, cx, floorY, cz, type, rng);
            case SORCERER_SANCTUM -> layoutSanctum(plan, level, cx, floorY, cz, type, rng);
            case DRAGONS_VAULT -> layoutVault(plan, level, cx, floorY, cz, type, rng);
            case FLOODED_TEMPLE -> layoutTemple(plan, level, cx, floorY, cz, type, rng);
            case NETHERITE_VAULT -> layoutNetherite(plan, level, cx, floorY, cz, type, rng);
            case OVERGROWN_CATACOMBS -> layoutCatacombs(plan, level, cx, floorY, cz, type, rng);
            case CRYSTAL_CAVERNS -> layoutCrystal(plan, level, cx, floorY, cz, type, rng);
        }

        // Boss wing always at +X
        int bossX = cx + 18;
        DungeonGenUtil.corridor(plan, level, cx + 4, floorY, cz, bossX - 2, cz, type);
        DungeonGenUtil.bossChamber(plan, level, bossX, floorY, cz - 5, type, rng);

        // Marker block at entrance for locate
        Build.set(level, cx, floorY + 1, cz - 3, DungeonBlocks.SOUL_LANTERN);
    }

    // ---- Per-type wings (each adds 2–4 unique rooms) ----

    private static void layoutCrypt(DungeonPlan plan, ServerLevel level, int cx, int y, int cz,
                                    DungeonType type, Random rng) {
        sideRoom(plan, level, cx - 14, y, cz - 2, 7, 5, 7, type, DungeonTier.UNCOMMON, rng);
        sideRoom(plan, level, cx - 14, y, cz + 6, 6, 4, 6, type, DungeonTier.COMMON, rng);
        DungeonGenUtil.corridor(plan, level, cx - 4, y, cz, cx - 14, cz, type);
        DungeonGenUtil.soulFireTrap(plan, level, cx - 8, y + 1, cz + 1);
        DungeonGenUtil.spawnerRoom(plan, level, cx - 11, y + 1, cz, type.mobCommon, rng);
        plan.postOps.add(DungeonPlan.PostOp.mob(cx - 12, y + 1, cz + 2, type.mobElite));
    }

    private static void layoutHideout(DungeonPlan plan, ServerLevel level, int cx, int y, int cz,
                                      DungeonType type, Random rng) {
        sideRoom(plan, level, cx, y, cz + 10, 8, 4, 8, type, DungeonTier.COMMON, rng);
        sideRoom(plan, level, cx - 10, y, cz + 10, 7, 4, 6, type, DungeonTier.UNCOMMON, rng);
        DungeonGenUtil.corridor(plan, level, cx, y, cz + 4, cx, cz + 10, type);
        DungeonGenUtil.corridor(plan, level, cx - 4, y, cz + 13, cx - 10, cz + 13, type);
        DungeonGenUtil.oakDoor(plan, level, cx, y + 1, cz + 9);
        DungeonGenUtil.arrowTrap(plan, level, cx - 6, y + 2, cz + 12, Direction.SOUTH);
        DungeonGenUtil.spawnerRoom(plan, level, cx + 3, y + 1, cz + 14, type.mobCommon, rng);
        plan.postOps.add(DungeonPlan.PostOp.mob(cx - 8, y + 1, cz + 12, type.mobCommon));
    }

    private static void layoutRuins(DungeonPlan plan, ServerLevel level, int cx, int y, int cz,
                                    DungeonType type, Random rng) {
        sideRoom(plan, level, cx, y, cz - 14, 8, 5, 8, type, DungeonTier.RARE, rng);
        sideRoom(plan, level, cx + 10, y, cz - 12, 6, 4, 6, type, DungeonTier.UNCOMMON, rng);
        DungeonGenUtil.corridor(plan, level, cx, y, cz - 4, cx, cz - 14, type);
        DungeonGenUtil.corridor(plan, level, cx + 4, y, cz - 10, cx + 10, cz - 10, type);
        DungeonGenUtil.arrowTrap(plan, level, cx + 1, y + 2, cz - 8, Direction.NORTH);
        DungeonGenUtil.arrowTrap(plan, level, cx + 12, y + 2, cz - 11, Direction.WEST);
        DungeonGenUtil.soulFireTrap(plan, level, cx + 2, y + 1, cz - 12);
        DungeonGenUtil.chest(plan, level, cx + 13, y + 1, cz - 11, DungeonTier.RARE);
    }

    private static void layoutLab(DungeonPlan plan, ServerLevel level, int cx, int y, int cz,
                                  DungeonType type, Random rng) {
        sideRoom(plan, level, cx - 12, y, cz + 8, 9, 5, 7, type, DungeonTier.RARE, rng);
        sideRoom(plan, level, cx + 8, y, cz, 7, 4, 9, type, DungeonTier.UNCOMMON, rng);
        DungeonGenUtil.corridor(plan, level, cx - 4, y, cz + 2, cx - 12, cz + 11, type);
        DungeonGenUtil.corridor(plan, level, cx + 4, y, cz, cx + 8, cz, type);
        Build.cube(level, cx - 10, y + 1, cz + 10, cx - 8, y + 2, cz + 12, type.accentBlock());
        Build.cube(level, cx + 10, y + 1, cz + 2, cx + 12, y + 2, cz + 4, net.minecraft.world.level.block.Blocks.GLASS);
        DungeonGenUtil.ironBars(plan, level, cx + 9, y + 1, cz + 5, 3);
        DungeonGenUtil.spawnerRoom(plan, level, cx + 11, y + 1, cz + 2, type.mobElite, rng);
    }

    private static void layoutSanctum(DungeonPlan plan, ServerLevel level, int cx, int y, int cz,
                                      DungeonType type, Random rng) {
        sideRoom(plan, level, cx, y, cz + 12, 9, 6, 9, type, DungeonTier.EPIC, rng);
        sideRoom(plan, level, cx - 12, y, cz, 7, 5, 7, type, DungeonTier.RARE, rng);
        DungeonGenUtil.corridor(plan, level, cx, y, cz + 4, cx, cz + 12, type);
        DungeonGenUtil.corridor(plan, level, cx - 4, y, cz, cx - 12, cz, type);
        Build.set(level, cx, y + 1, cz + 16, type.accentBlock());
        Build.set(level, cx - 9, y + 2, cz, net.minecraft.world.level.block.Blocks.ENCHANTING_TABLE);
        DungeonGenUtil.soulFireTrap(plan, level, cx + 2, y + 1, cz + 14);
        DungeonGenUtil.spawnerRoom(plan, level, cx - 9, y + 1, cz + 2, type.mobCommon, rng);
    }

    private static void layoutVault(DungeonPlan plan, ServerLevel level, int cx, int y, int cz,
                                    DungeonType type, Random rng) {
        sideRoom(plan, level, cx, y, cz - 12, 10, 6, 10, type, DungeonTier.EPIC, rng);
        sideRoom(plan, level, cx - 12, y, cz - 4, 8, 5, 8, type, DungeonTier.RARE, rng);
        DungeonGenUtil.corridor(plan, level, cx, y, cz - 4, cx, cz - 12, type);
        DungeonGenUtil.corridor(plan, level, cx - 4, y, cz - 4, cx - 12, cz - 4, type);
        DungeonGenUtil.arrowTrap(plan, level, cx + 2, y + 2, cz - 8, Direction.NORTH);
        DungeonGenUtil.arrowTrap(plan, level, cx - 8, y + 2, cz - 6, Direction.EAST);
        Build.cube(level, cx - 2, y + 1, cz - 10, cx + 2, y + 3, cz - 8, type.accentBlock());
        DungeonGenUtil.spawnerRoom(plan, level, cx - 10, y + 1, cz - 2, type.mobElite, rng);
    }

    private static void layoutTemple(DungeonPlan plan, ServerLevel level, int cx, int y, int cz,
                                     DungeonType type, Random rng) {
        sideRoom(plan, level, cx - 10, y, cz + 10, 9, 5, 9, type, DungeonTier.UNCOMMON, rng);
        sideRoom(plan, level, cx + 8, y, cz + 8, 7, 4, 7, type, DungeonTier.COMMON, rng);
        DungeonGenUtil.corridor(plan, level, cx - 4, y, cz + 4, cx - 10, cz + 14, type);
        DungeonGenUtil.corridor(plan, level, cx + 4, y, cz + 2, cx + 8, cz + 11, type);
        DungeonGenUtil.floodedBasin(plan, level, cx - 8, y, cz + 12, 5, 5);
        DungeonGenUtil.floodedBasin(plan, level, cx + 9, y, cz + 9, 4, 4);
        Build.set(level, cx - 6, y + 1, cz + 14, net.minecraft.world.level.block.Blocks.SEA_LANTERN);
    }

    private static void layoutNetherite(DungeonPlan plan, ServerLevel level, int cx, int y, int cz,
                                        DungeonType type, Random rng) {
        sideRoom(plan, level, cx, y, cz + 12, 8, 5, 8, type, DungeonTier.EPIC, rng);
        sideRoom(plan, level, cx - 14, y, cz + 2, 7, 4, 7, type, DungeonTier.RARE, rng);
        DungeonGenUtil.corridor(plan, level, cx, y, cz + 4, cx, cz + 12, type);
        DungeonGenUtil.corridor(plan, level, cx - 4, y, cz + 2, cx - 14, cz + 5, type);
        DungeonGenUtil.ironBars(plan, level, cx + 2, y + 1, cz + 14, 4);
        DungeonGenUtil.arrowTrap(plan, level, cx - 11, y + 2, cz + 4, Direction.WEST);
        DungeonGenUtil.chest(plan, level, cx + 3, y + 1, cz + 15, DungeonTier.EPIC);
        DungeonGenUtil.spawnerRoom(plan, level, cx - 11, y + 1, cz + 3, type.mobElite, rng);
    }

    private static void layoutCatacombs(DungeonPlan plan, ServerLevel level, int cx, int y, int cz,
                                        DungeonType type, Random rng) {
        sideRoom(plan, level, cx - 14, y, cz - 6, 8, 5, 8, type, DungeonTier.UNCOMMON, rng);
        sideRoom(plan, level, cx - 14, y, cz + 4, 7, 4, 6, type, DungeonTier.COMMON, rng);
        DungeonGenUtil.corridor(plan, level, cx - 4, y, cz - 2, cx - 14, cz - 2, type);
        DungeonGenUtil.corridor(plan, level, cx - 14, y, cz - 2, cx - 14, cz + 4, type);
        Build.set(level, cx - 11, y + 1, cz - 4, net.minecraft.world.level.block.Blocks.VINE);
        Build.set(level, cx - 12, y + 1, cz + 6, net.minecraft.world.level.block.Blocks.MOSS_BLOCK);
        DungeonGenUtil.soulFireTrap(plan, level, cx - 10, y + 1, cz);
        DungeonGenUtil.spawnerRoom(plan, level, cx - 12, y + 1, cz + 2, type.mobCommon, rng);
        plan.postOps.add(DungeonPlan.PostOp.mob(cx - 11, y + 1, cz - 5, type.mobElite));
    }

    private static void layoutCrystal(DungeonPlan plan, ServerLevel level, int cx, int y, int cz,
                                      DungeonType type, Random rng) {
        sideRoom(plan, level, cx + 10, y, cz - 8, 9, 6, 9, type, DungeonTier.RARE, rng);
        sideRoom(plan, level, cx + 10, y, cz + 6, 7, 5, 7, type, DungeonTier.UNCOMMON, rng);
        DungeonGenUtil.corridor(plan, level, cx + 4, y, cz - 4, cx + 10, cz - 4, type);
        DungeonGenUtil.corridor(plan, level, cx + 10, y, cz - 4, cx + 10, cz + 6, type);
        Build.set(level, cx + 14, y + 1, cz - 4, net.minecraft.world.level.block.Blocks.AMETHYST_CLUSTER);
        Build.set(level, cx + 13, y + 1, cz + 9, net.minecraft.world.level.block.Blocks.AMETHYST_CLUSTER);
        DungeonGenUtil.chest(plan, level, cx + 14, y + 1, cz - 6, DungeonTier.RARE);
        DungeonGenUtil.spawnerRoom(plan, level, cx + 13, y + 1, cz + 8, type.mobCommon, rng);
    }

    private static void sideRoom(DungeonPlan plan, ServerLevel level, int x, int y, int z,
                                 int w, int h, int d, DungeonType type, DungeonTier chestTier, Random rng) {
        DungeonGenUtil.room(plan, level, x, y, z, w, h, d, type);
        DungeonGenUtil.chest(plan, level, x + w / 2, y + 1, z + d / 2, chestTier);
        if (rng.nextFloat() < 0.55f) {
            DungeonGenUtil.spawnerRoom(plan, level, x + 1, y + 1, z + 1, type.mobCommon, rng);
        }
        if (rng.nextFloat() < 0.35f) {
            DungeonGenUtil.arrowTrap(plan, level, x + w - 2, y + 2, z + d / 2,
                    rng.nextBoolean() ? Direction.EAST : Direction.WEST);
        }
    }
}
