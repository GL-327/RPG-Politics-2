package com.political.world;

import com.political.content.ModBlocks;
import com.political.politics.SettlementType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Dev-only tool: plans a settlement into a {@link BuildBuffer} and renders an accurate
 * isometric PNG of the <em>final</em> voxel state — i.e. exactly what the generator
 * places in-world. Replays every op (air clears, solids set), face-culls hidden faces,
 * and draws the result with a 2:1 isometric projection and per-face shading.
 *
 * <p>Triggered on server start when the {@code SETTLEMENT_PREVIEW} env var is set.
 */
public final class SettlementPreview {

    private static final Map<Block, Integer> COLORS = new HashMap<>();
    static {
        COLORS.put(ModBlocks.CASTLE_BRICKS, rgb(135, 135, 143));
        COLORS.put(ModBlocks.CASTLE_PILLAR, rgb(158, 158, 166));
        COLORS.put(ModBlocks.ROYAL_BANNER_BLOCK, rgb(170, 40, 46));
        COLORS.put(ModBlocks.TOWN_HALL_BRICKS, rgb(150, 92, 58));
        COLORS.put(ModBlocks.PAVED_ROAD, rgb(52, 52, 58));
        COLORS.put(ModBlocks.COBBLE_STREET, rgb(120, 114, 106));
        COLORS.put(ModBlocks.STREET_LAMP, rgb(255, 226, 140));
        COLORS.put(ModBlocks.MODERN_FACADE, rgb(222, 222, 216));
        COLORS.put(ModBlocks.MODERN_WINDOW, rgb(96, 162, 210));
        COLORS.put(ModBlocks.CIVIC_MARKER, rgb(168, 80, 196));
        COLORS.put(Blocks.GLASS, rgb(196, 226, 234));
        COLORS.put(Blocks.OAK_PLANKS, rgb(192, 156, 100));
        COLORS.put(Blocks.DARK_OAK_STAIRS, rgb(78, 56, 36));
        COLORS.put(Blocks.DARK_OAK_PLANKS, rgb(86, 64, 42));
        COLORS.put(Blocks.OAK_LOG, rgb(112, 86, 56));
        COLORS.put(Blocks.OAK_LEAVES, rgb(58, 118, 50));
        COLORS.put(Blocks.GRASS_BLOCK, rgb(98, 152, 72));
        COLORS.put(Blocks.WATER, rgb(64, 110, 200));
    }

    private SettlementPreview() {}

    private static int rgb(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }

    public static void renderAll(ServerLevel level) {
        try {
            String mode = System.getenv("SETTLEMENT_PREVIEW");
            if (mode != null && !mode.isBlank() && !mode.equals("1") && !mode.equals("true")) {
                // e.g. SETTLEMENT_PREVIEW=city — render exactly one tier.
                SettlementType type = SettlementType.valueOf(mode.trim().toUpperCase());
                renderOne(level, type, 512, 512, "settlement_" + type.name().toLowerCase());
                return;
            }
            // Default: one accurate showcase image (large modern city).
            renderOne(level, SettlementType.CITY, 512, 512, "settlement_showcase");
        } catch (Exception e) {
            com.political.RpgPoliticsMod.LOGGER.error("Preview render failed", e);
        }
    }

    /** Plans and renders a single settlement to previews/{name}.png */
    public static void renderOne(ServerLevel level, SettlementType type, int cx, int cz, String name) throws Exception {
        BuildBuffer buf = new BuildBuffer();
        SettlementGenerator.planInto(buf, level, cx, cz, type, "Preview");
        com.political.RpgPoliticsMod.LOGGER.info("Preview {} captured {} block ops", name, buf.size());
        render(buf, name);
        com.political.RpgPoliticsMod.LOGGER.info("Settlement preview rendered to {}",
                new File("previews", name + ".png").getAbsolutePath());
    }

    // ------------------------------------------------------------------
    // Voxel resolution
    // ------------------------------------------------------------------

    private static long key(int x, int y, int z) {
        // Offsets keep coordinates positive across the mod's build range.
        return (((long) (x + 4096)) << 42) | (((long) (y + 1024)) << 21) | (z + 4096);
    }

    public static void render(BuildBuffer buffer, String name) throws Exception {
        if (buffer.ops.isEmpty()) return;

        // Replay ops in order: air clears, solids set. Last write wins -> final state.
        Map<Long, Integer> voxels = new HashMap<>(buffer.ops.size());
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BuildBuffer.Op op : buffer.ops) {
            long k = key(op.x, op.y, op.z);
            if (op.state.isAir()) {
                voxels.remove(k);
            } else {
                voxels.put(k, COLORS.getOrDefault(op.state.getBlock(), rgb(150, 150, 150)));
                minX = Math.min(minX, op.x); maxX = Math.max(maxX, op.x);
                minY = Math.min(minY, op.y); maxY = Math.max(maxY, op.y);
                minZ = Math.min(minZ, op.z); maxZ = Math.max(maxZ, op.z);
            }
        }
        if (voxels.isEmpty()) return;

        // Clip away buried foundations: find the dominant ground slab (the y-layer with the
        // most blocks) and only render from just below it upward, as you'd see it in-world.
        Map<Integer, Integer> yHist = new HashMap<>();
        for (long k : voxels.keySet()) {
            int y = (int) ((k >> 21) & 0x1FFFFF) - 1024;
            yHist.merge(y, 1, Integer::sum);
        }
        int groundY = minY;
        int best = -1;
        for (Map.Entry<Integer, Integer> e : yHist.entrySet()) {
            if (e.getValue() > best) { best = e.getValue(); groundY = e.getKey(); }
        }
        minY = Math.max(minY, groundY - 2);

        // Tile size chosen so each image lands around ~1000px wide regardless of scale.
        int spanSum = (maxX - minX) + (maxZ - minZ) + 2;
        int hw = clamp(1000 / Math.max(1, spanSum), 2, 14);
        int hh = Math.max(1, hw / 2);
        int vh = Math.max(3, hw + 1);

        int diffMin = minX - maxZ, diffMax = maxX - minZ;
        int sumMin = minX + minZ, sumMax = maxX + maxZ;
        int margin = 8;
        int sxMin = diffMin * hw - hw;
        int sxMax = diffMax * hw + hw;
        int syMin = sumMin * hh - maxY * vh;
        int syMax = sumMax * hh - minY * vh + 2 * hh + vh;
        int originX = -sxMin + margin;
        int originY = -syMin + margin;
        int imgW = (sxMax - sxMin) + 2 * margin + 1;
        int imgH = (syMax - syMin) + 2 * margin + 1;

        BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
        fillBackground(img, rgb(238, 242, 247));

        // Painter's order for a camera looking toward -x/-z: far (low x,z) first, bottom up.
        int[] xs = new int[4], ys = new int[4];
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    Integer col = voxels.get(key(x, y, z));
                    if (col == null) continue;
                    boolean top = !voxels.containsKey(key(x, y + 1, z));
                    boolean right = !voxels.containsKey(key(x + 1, y, z));
                    boolean left = !voxels.containsKey(key(x, y, z + 1));
                    if (!top && !right && !left) continue;

                    int tx = originX + (x - z) * hw;
                    int ty = originY + (x + z) * hh - y * vh;
                    // Diamond vertices of the top face.
                    int Tx = tx,      Ty = ty;
                    int Rx = tx + hw, Ry = ty + hh;
                    int Bx = tx,      By = ty + 2 * hh;
                    int Lx = tx - hw, Ly = ty + hh;

                    if (right) { // face toward +x
                        xs[0] = Bx; ys[0] = By;
                        xs[1] = Rx; ys[1] = Ry;
                        xs[2] = Rx; ys[2] = Ry + vh;
                        xs[3] = Bx; ys[3] = By + vh;
                        fillQuad(img, xs, ys, shade(col, 0.78));
                    }
                    if (left) { // face toward +z
                        xs[0] = Lx; ys[0] = Ly;
                        xs[1] = Bx; ys[1] = By;
                        xs[2] = Bx; ys[2] = By + vh;
                        xs[3] = Lx; ys[3] = Ly + vh;
                        fillQuad(img, xs, ys, shade(col, 0.60));
                    }
                    if (top) {
                        xs[0] = Tx; ys[0] = Ty;
                        xs[1] = Rx; ys[1] = Ry;
                        xs[2] = Bx; ys[2] = By;
                        xs[3] = Lx; ys[3] = Ly;
                        fillQuad(img, xs, ys, shade(col, 1.0));
                    }
                }
            }
        }

        File dir = new File("previews");
        dir.mkdirs();
        ImageIO.write(img, "png", new File(dir, name + ".png"));
    }

    // ------------------------------------------------------------------
    // Raster helpers
    // ------------------------------------------------------------------

    private static void fillBackground(BufferedImage img, int color) {
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
                img.setRGB(x, y, color);
    }

    /** Scanline-fills a convex quad given as 4 ordered vertices. */
    private static void fillQuad(BufferedImage img, int[] xs, int[] ys, int color) {
        int minY = Math.min(Math.min(ys[0], ys[1]), Math.min(ys[2], ys[3]));
        int maxY = Math.max(Math.max(ys[0], ys[1]), Math.max(ys[2], ys[3]));
        int H = img.getHeight(), W = img.getWidth();
        for (int y = Math.max(0, minY); y <= Math.min(H - 1, maxY); y++) {
            int xMin = Integer.MAX_VALUE, xMax = Integer.MIN_VALUE;
            for (int i = 0; i < 4; i++) {
                int j = (i + 1) % 4;
                int y0 = ys[i], y1 = ys[j];
                if (y0 == y1) continue;
                if (y < Math.min(y0, y1) || y > Math.max(y0, y1)) continue;
                double t = (double) (y - y0) / (y1 - y0);
                int x = (int) Math.round(xs[i] + t * (xs[j] - xs[i]));
                xMin = Math.min(xMin, x);
                xMax = Math.max(xMax, x);
            }
            if (xMin > xMax) continue;
            for (int x = Math.max(0, xMin); x <= Math.min(W - 1, xMax); x++) {
                img.setRGB(x, y, color);
            }
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static int shade(int color, double factor) {
        int r = (int) Math.min(255, ((color >> 16) & 0xFF) * factor);
        int g = (int) Math.min(255, ((color >> 8) & 0xFF) * factor);
        int b = (int) Math.min(255, (color & 0xFF) * factor);
        return rgb(r, g, b);
    }
}
