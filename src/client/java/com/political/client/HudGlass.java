package com.political.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Shared liquid-glass HUD primitives — frosted panels and gradient bars. */
public final class HudGlass {

    private static final int COL_PANEL   = 0xB8182430;
    private static final int COL_EDGE    = 0x55FFFFFF;
    private static final int COL_SHINE   = 0x28FFFFFF;
    private static final int COL_BORDER  = 0x664A6A8C;
    private static final int COL_ACCENT  = 0xFF7EC8FF;

    private HudGlass() {}

    /** Frosted panel with glass rim and top shine. */
    public static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, COL_BORDER);
        g.fill(x, y, x + w, y + h, COL_PANEL);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, COL_SHINE);
        g.fill(x + 1, y + 1, x + 2, y + h - 1, COL_EDGE);
        g.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, 0x22FFFFFF);
    }

    /** Accent hairline along the top edge of a panel. */
    public static void accentTop(GuiGraphicsExtractor g, int x, int y, int w, int color) {
        g.fill(x, y, x + w, y + 1, color);
    }

    public static void accentTopDefault(GuiGraphicsExtractor g, int x, int y, int w) {
        accentTop(g, x, y, w, COL_ACCENT);
    }

    /** Labelled gradient bar with optional icon left and centered value. */
    public static void bar(GuiGraphicsExtractor g, Font font, int x, int y, int w, int h,
                           float frac, int track, int fillTop, int fillBot,
                           String icon, int iconColor, String value) {
        frac = Math.max(0f, Math.min(1f, frac));
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x66000000);
        g.fill(x, y, x + w, y + h, track);
        int filled = (int) (w * frac);
        if (filled > 0) {
            g.fillGradient(x, y, x + filled, y + h, fillTop, fillBot);
            g.fill(x, y, x + filled, y + 1, 0x55FFFFFF);
        }
        if (icon != null && !icon.isEmpty()) {
            g.text(font, icon, x - 10, y - 1, iconColor, true);
        }
        if (value != null && !value.isEmpty()) {
            int vw = font.width(value);
            g.text(font, value, x + (w - vw) / 2, y - 1, 0xFFFFFFFF, true);
        }
    }

    public static int lerpColor(int from, int to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int fa = (from >> 24) & 0xFF, fr = (from >> 16) & 0xFF, fg = (from >> 8) & 0xFF, fb = from & 0xFF;
        int ta = (to >> 24) & 0xFF, tr = (to >> 16) & 0xFF, tg = (to >> 8) & 0xFF, tb = to & 0xFF;
        int a = (int) (fa + (ta - fa) * t);
        int r = (int) (fr + (tr - fr) * t);
        int gv = (int) (fg + (tg - fg) * t);
        int b = (int) (fb + (tb - fb) * t);
        return (a << 24) | (r << 16) | (gv << 8) | b;
    }
}
