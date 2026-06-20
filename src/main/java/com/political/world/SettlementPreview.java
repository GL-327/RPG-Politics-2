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
 * Dev-only tool: plans a settlement into a {@link BuildBuffer} and renders a top-down
 * PNG (highest block wins, shaded by height) so the layout can be inspected without the
 * game client. Triggered on server start when the {@code SETTLEMENT_PREVIEW} env var is set.
 */
public final class SettlementPreview {

    private static final Map<Block, Integer> COLORS = new HashMap<>();
    static {
        COLORS.put(ModBlocks.CASTLE_BRICKS, rgb(132, 132, 140));
        COLORS.put(ModBlocks.CASTLE_PILLAR, rgb(154, 154, 162));
        COLORS.put(ModBlocks.ROYAL_BANNER_BLOCK, rgb(168, 42, 46));
        COLORS.put(ModBlocks.TOWN_HALL_BRICKS, rgb(152, 96, 60));
        COLORS.put(ModBlocks.PAVED_ROAD, rgb(48, 48, 54));
        COLORS.put(ModBlocks.COBBLE_STREET, rgb(112, 106, 100));
        COLORS.put(ModBlocks.STREET_LAMP, rgb(255, 232, 150));
        COLORS.put(ModBlocks.MODERN_FACADE, rgb(216, 216, 210));
        COLORS.put(ModBlocks.MODERN_WINDOW, rgb(96, 156, 204));
        COLORS.put(ModBlocks.CIVIC_MARKER, rgb(156, 74, 184));
        COLORS.put(Blocks.GLASS, rgb(202, 230, 236));
        COLORS.put(Blocks.OAK_PLANKS, rgb(190, 155, 100));
        COLORS.put(Blocks.DARK_OAK_STAIRS, rgb(74, 54, 34));
        COLORS.put(Blocks.DARK_OAK_PLANKS, rgb(84, 62, 40));
        COLORS.put(Blocks.OAK_LOG, rgb(110, 85, 55));
        COLORS.put(Blocks.OAK_LEAVES, rgb(60, 120, 50));
        COLORS.put(Blocks.GRASS_BLOCK, rgb(96, 150, 70));
    }

    private SettlementPreview() {}

    private static int rgb(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }

    public static void renderAll(ServerLevel level) {
        try {
            for (SettlementType type : SettlementType.values()) {
                BuildBuffer buf = new BuildBuffer();
                SettlementGenerator.planInto(buf, level, 0, 0, type, "Preview");
                com.political.RpgPoliticsMod.LOGGER.info("Preview {} captured {} block ops", type, buf.size());
                render(buf, type.name().toLowerCase());
            }
            com.political.RpgPoliticsMod.LOGGER.info("Settlement previews rendered to {}",
                    new File("previews").getAbsolutePath());
        } catch (Exception e) {
            com.political.RpgPoliticsMod.LOGGER.error("Preview render failed", e);
        }
    }

    public static void render(BuildBuffer buffer, String name) throws Exception {
        if (buffer.ops.isEmpty()) return;
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BuildBuffer.Op op : buffer.ops) {
            minX = Math.min(minX, op.x); maxX = Math.max(maxX, op.x);
            minZ = Math.min(minZ, op.z); maxZ = Math.max(maxZ, op.z);
        }
        int w = maxX - minX + 1;
        int h = maxZ - minZ + 1;
        int[] topY = new int[w * h];
        int[] topColor = new int[w * h];
        java.util.Arrays.fill(topY, Integer.MIN_VALUE);

        for (BuildBuffer.Op op : buffer.ops) {
            if (op.state.isAir()) continue;
            int ix = op.x - minX, iz = op.z - minZ;
            int idx = iz * w + ix;
            if (op.y >= topY[idx]) {
                topY[idx] = op.y;
                topColor[idx] = COLORS.getOrDefault(op.state.getBlock(), rgb(140, 140, 140));
            }
        }

        int minTop = Integer.MAX_VALUE, maxTop = Integer.MIN_VALUE;
        for (int v : topY) if (v != Integer.MIN_VALUE) { minTop = Math.min(minTop, v); maxTop = Math.max(maxTop, v); }
        int range = Math.max(1, maxTop - minTop);

        int scale = 4;
        BufferedImage img = new BufferedImage(w * scale, h * scale, BufferedImage.TYPE_INT_RGB);
        for (int iz = 0; iz < h; iz++) {
            for (int ix = 0; ix < w; ix++) {
                int idx = iz * w + ix;
                int color;
                if (topY[idx] == Integer.MIN_VALUE) {
                    color = rgb(28, 30, 34); // empty
                } else {
                    double shade = 0.55 + 0.45 * (topY[idx] - minTop) / range; // taller = brighter
                    color = shade(topColor[idx], shade);
                }
                for (int dy = 0; dy < scale; dy++)
                    for (int dx = 0; dx < scale; dx++)
                        img.setRGB(ix * scale + dx, iz * scale + dy, color);
            }
        }

        File dir = new File("previews");
        dir.mkdirs();
        ImageIO.write(img, "png", new File(dir, name + ".png"));
    }

    private static int shade(int color, double factor) {
        int r = (int) Math.min(255, ((color >> 16) & 0xFF) * factor);
        int g = (int) Math.min(255, ((color >> 8) & 0xFF) * factor);
        int b = (int) Math.min(255, (color & 0xFF) * factor);
        return rgb(r, g, b);
    }
}
