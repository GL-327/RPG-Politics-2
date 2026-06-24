package com.political.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Shared liquid-glass theme for mod menus — frosted translucent panels, soft highlights, and
 * Apple-inspired depth without texture assets.
 */
public abstract class RpgScreen extends Screen {

    protected static final int COL_DIM        = 0x90000812;
    protected static final int COL_PANEL_BG   = 0xB8182430;
    protected static final int COL_PANEL_BG2  = 0xA0101820;
    protected static final int COL_GLASS_EDGE = 0x55FFFFFF;
    protected static final int COL_GLASS_SHINE = 0x28FFFFFF;
    protected static final int COL_BORDER     = 0x664A6A8C;
    protected static final int COL_HEADER_TOP = 0xCC2A3A58;
    protected static final int COL_HEADER_BOT = 0xCC141C2A;
    protected static final int COL_ACCENT     = 0xFF7EC8FF;
    protected static final int COL_ACCENT_SOFT = 0xFF4DA3E8;
    protected static final int COL_TEXT       = 0xFFF4F8FF;
    protected static final int COL_TEXT_DIM   = 0xFFB0BBC8;
    protected static final int COL_GOLD       = 0xFFFFD98A;

    protected RpgScreen(Component title) {
        super(title);
    }

    /** Full-screen frosted dim layer. */
    protected void glassBackdrop(GuiGraphicsExtractor g) {
        g.fill(0, 0, width, height, COL_DIM);
        g.fillGradient(0, 0, width, height / 3, 0x18FFFFFF, 0x00000000);
    }

    /** Primary frosted panel with glass rim + top shine. */
    protected void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, COL_BORDER);
        g.fill(x, y, x + w, y + h, COL_PANEL_BG);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, COL_GLASS_SHINE);
        g.fill(x + 1, y + 1, x + 2, y + h - 1, COL_GLASS_EDGE);
        g.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, 0x22FFFFFF);
    }

    protected void headerBar(GuiGraphicsExtractor g, int x, int y, int w, int hh) {
        g.fillGradient(x, y, x + w, y + hh, COL_HEADER_TOP, COL_HEADER_BOT);
        g.fill(x, y, x + w, y + 1, COL_GLASS_SHINE);
        g.fill(x, y + hh, x + w, y + hh + 1, COL_ACCENT_SOFT);
    }

    protected void inset(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, COL_PANEL_BG2);
        g.outline(x, y, w, h, COL_GLASS_EDGE);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0x18FFFFFF);
    }

    protected void statBar(GuiGraphicsExtractor g, int x, int y, int w, int h, float frac,
                           int track, int fillTop, int fillBot, Component label, int labelColor) {
        frac = Math.max(0f, Math.min(1f, frac));
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x66000000);
        g.fill(x, y, x + w, y + h, track);
        int filled = (int) (w * frac);
        if (filled > 0) {
            g.fillGradient(x, y, x + filled, y + h, fillTop, fillBot);
            g.fill(x, y, x + filled, y + 1, 0x55FFFFFF);
        }
        if (label != null) {
            int tw = font.width(label);
            g.text(font, label, x + (w - tw) / 2, y + (h - 8) / 2, labelColor, true);
        }
    }

    protected void label(GuiGraphicsExtractor g, String text, int x, int y, int color) {
        g.text(font, text, x, y, color, true);
    }

    protected void label(GuiGraphicsExtractor g, Component text, int x, int y, int color) {
        g.text(font, text, x, y, color, true);
    }

    protected int kv(GuiGraphicsExtractor g, String key, String value, int x, int y, int valueColor) {
        g.text(font, key, x, y, COL_TEXT_DIM, true);
        int kw = font.width(key + " ");
        g.text(font, value, x + kw, y, valueColor, true);
        return y + 11;
    }

    protected void centered(GuiGraphicsExtractor g, Component text, int cx, int y, int color) {
        g.centeredText(font, text, cx, y, color);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
