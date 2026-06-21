package com.political.world.structures;

import com.political.content.ModBlocks;
import com.political.world.Build;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.Random;

/**
 * Additive decorative props that deepen the mod's procedural settlements with more variety —
 * market stalls, monuments, planters, benches and notice boards — <b>without</b> touching the
 * government overlay (no {@code CIVIC_MARKER} placement, no {@code Settlement} registration, no
 * civic geometry). Every method is a self-contained, terrain-following addition built on the
 * shared {@link Build} layer, so settlements gain texture while governance stays byte-for-byte
 * unchanged. Lives in the structures package; {@code SettlementGenerator} calls into it.
 */
public final class SettlementProps {

    private SettlementProps() {}

    private static int surf(ServerLevel level, int x, int z) {
        return Build.groundY(level, x, z);
    }

    /** A covered market stall: counter, awning on posts, a barrel and a crate of goods. */
    public static void marketStall(ServerLevel level, int cx, int cz, Random rng) {
        int y = surf(level, cx, cz);
        // Counter
        Build.cube(level, cx - 1, y, cz, cx + 1, y, cz, Blocks.SMOOTH_STONE_SLAB);
        // Posts
        Build.cube(level, cx - 1, y, cz - 1, cx - 1, y + 2, cz - 1, Blocks.SPRUCE_FENCE);
        Build.cube(level, cx + 1, y, cz - 1, cx + 1, y + 2, cz - 1, Blocks.SPRUCE_FENCE);
        // Striped awning
        var cloth = rng.nextBoolean() ? VanillaBlocks.RED_WOOL : VanillaBlocks.WHITE_WOOL;
        Build.cube(level, cx - 1, y + 3, cz - 1, cx + 1, y + 3, cz, cloth);
        // Goods
        Build.set(level, cx - 1, y + 1, cz, Blocks.BARREL);
        Build.set(level, cx + 1, y + 1, cz, rng.nextBoolean() ? Blocks.HAY_BLOCK : Blocks.PUMPKIN);
        Build.set(level, cx, y + 1, cz, Blocks.LANTERN);
    }

    /** A short row of market stalls along Z. */
    public static void marketRow(ServerLevel level, int cx, int cz, int count, Random rng) {
        for (int i = 0; i < count; i++) {
            marketStall(level, cx, cz + i * 4, rng);
        }
    }

    /** A commemorative monument: a stepped plinth, a pillar and a glowing crown. */
    public static void monument(ServerLevel level, int cx, int cz) {
        int y = surf(level, cx, cz);
        Build.floor(level, cx - 2, cz - 2, cx + 2, cz + 2, y, ModBlocks.COBBLE_STREET);
        Build.floor(level, cx - 1, cz - 1, cx + 1, cz + 1, y + 1, ModBlocks.CASTLE_BRICKS);
        Build.cube(level, cx, y + 1, cz, cx, y + 4, cz, ModBlocks.CASTLE_PILLAR);
        Build.set(level, cx, y + 5, cz, ModBlocks.STREET_LAMP);
        Build.set(level, cx - 2, y + 1, cz - 2, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, cx + 2, y + 1, cz + 2, ModBlocks.ROYAL_BANNER_BLOCK);
    }

    /** A planter box of flowers framed in logs. */
    public static void planter(ServerLevel level, int cx, int cz, Random rng) {
        int y = surf(level, cx, cz);
        Build.walls(level, cx - 1, cz - 1, cx + 1, cz + 1, y, 1, Blocks.SPRUCE_LOG);
        Build.floor(level, cx - 1, cz - 1, cx + 1, cz + 1, y, Blocks.DIRT);
        Build.garden(level, cx - 1, cz - 1, cx + 1, cz + 1, y + 1, rng);
    }

    /** A public bench (stairs back-to-back) with a lamp. */
    public static void bench(ServerLevel level, int cx, int cz) {
        int y = surf(level, cx, cz);
        Build.stair(level, cx, y, cz, Blocks.SPRUCE_STAIRS, Direction.NORTH);
        Build.stair(level, cx + 1, y, cz, Blocks.SPRUCE_STAIRS, Direction.NORTH);
    }

    /** A town notice board: a lectern atop a small plinth, flanked by lamps. */
    public static void noticeBoard(ServerLevel level, int cx, int cz) {
        int y = surf(level, cx, cz);
        Build.set(level, cx, y, cz, ModBlocks.CASTLE_BRICKS);
        Build.set(level, cx, y + 1, cz, Blocks.LECTERN);
        Build.set(level, cx - 1, y + 1, cz, ModBlocks.STREET_LAMP);
        Build.set(level, cx + 1, y + 1, cz, ModBlocks.STREET_LAMP);
    }

    /** A pair of decorative banners on posts framing an entrance/avenue. */
    public static void bannerGate(ServerLevel level, int cx, int cz, int spread) {
        int yL = surf(level, cx - spread, cz);
        int yR = surf(level, cx + spread, cz);
        Build.cube(level, cx - spread, yL, cz, cx - spread, yL + 3, cz, ModBlocks.CASTLE_PILLAR);
        Build.cube(level, cx + spread, yR, cz, cx + spread, yR + 3, cz, ModBlocks.CASTLE_PILLAR);
        Build.set(level, cx - spread, yL + 4, cz, ModBlocks.ROYAL_BANNER_BLOCK);
        Build.set(level, cx + spread, yR + 4, cz, ModBlocks.ROYAL_BANNER_BLOCK);
    }
}
