package com.political.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Shared dark, modern theme + drawing helpers for the mod's custom menus (Powers, Bank, Governance).
 * Built on the 26.2 render-state pipeline: panels are drawn in {@link #extractBackground} (behind
 * widgets) and foreground text in {@link #extractRenderState} (over widgets). No mixins, no textures.
 */
public abstract class RpgScreen extends Screen {

    // Palette.
    protected static final int COL_DIM        = 0xC8060A10; // full-screen darkening
    protected static final int COL_PANEL_BG   = 0xF0121822; // main panel fill
    protected static final int COL_PANEL_BG2  = 0xF00C1018; // inset / secondary fill
    protected static final int COL_BORDER     = 0xFF3B4D6B; // panel border
    protected static final int COL_HEADER_TOP = 0xFF20304F;
    protected static final int COL_HEADER_BOT = 0xFF131B2B;
    protected static final int COL_ACCENT     = 0xFF5AA9FF; // bright accent (titles/lines)
    protected static final int COL_TEXT       = 0xFFE6ECF5;
    protected static final int COL_TEXT_DIM   = 0xFF8C97A8;
    protected static final int COL_GOLD       = 0xFFFFC857;

    protected RpgScreen(Component title) {
        super(title);
    }

    // ---------------- Drawing helpers ----------------

    /** A bordered panel with a subtle header strip across the top. */
    protected void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, COL_BORDER);
        g.fill(x, y, x + w, y + h, COL_PANEL_BG);
    }

    protected void headerBar(GuiGraphicsExtractor g, int x, int y, int w, int hh) {
        g.fillGradient(x, y, x + w, y + hh, COL_HEADER_TOP, COL_HEADER_BOT);
        g.fill(x, y + hh, x + w, y + hh + 1, COL_ACCENT);
    }

    /** Inset sub-panel for grouping content. */
    protected void inset(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, COL_PANEL_BG2);
        g.outline(x, y, w, h, 0x40FFFFFF);
    }

    /**
     * A sleek labelled resource bar with a gradient fill. {@code frac} is clamped to [0,1].
     */
    protected void statBar(GuiGraphicsExtractor g, int x, int y, int w, int h, float frac,
                           int track, int fillTop, int fillBot, Component label, int labelColor) {
        frac = Math.max(0f, Math.min(1f, frac));
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
        g.fill(x, y, x + w, y + h, track);
        int filled = (int) (w * frac);
        if (filled > 0) g.fillGradient(x, y, x + filled, y + h, fillTop, fillBot);
        // glossy top highlight
        g.fill(x, y, x + filled, y + 1, 0x40FFFFFF);
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

    /** Draws "Key: value" with a dim key and bright value, returns the next y (y + 11). */
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
