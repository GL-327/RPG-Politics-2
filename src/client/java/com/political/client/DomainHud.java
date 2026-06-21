package com.political.client;

import com.political.curse.domain.CursedDomain;
import com.political.curse.domain.DomainRegistry;
import com.political.curse.technique.CursedTechnique;
import com.political.curse.technique.TechniqueRegistry;
import com.political.politics.DataManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Compact JJK HUD overlay (Workstream A): a left-edge panel showing the player's sorcerer grade and
 * the four key-bound cursed techniques (with their themed colours and cast keys). Drawn mixin-free via
 * the Fabric HUD element pipeline (registered in {@link JjkClientBootstrap}). The cursed-energy bar
 * itself is already drawn by the main {@code PoliticalClient} HUD, so this complements rather than
 * duplicates it.
 */
public final class DomainHud {

    private static final String[] KEY_LABELS = {"Z", "X", "C", "B"};

    private DomainHud() {}

    static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (CursedClientState.maxCursedEnergy <= 0f && CursedClientState.grade <= 0) return;

        Font font = mc.font;
        int x = 6;
        int y = mc.getWindow().getGuiScaledHeight() / 2 - 42;
        int w = 128;
        int rows = 4;
        int extra = DomainClientState.localActive != null ? 14 : 0;
        int h = 16 + rows * 11 + extra;

        g.fill(x - 2, y - 2, x + w, y + h, 0xB0050A12);
        g.fill(x - 2, y - 2, x + w, y - 1, 0xFFC065FF);

        if (DomainOverlayRenderer.isLocalPlayerInsideDomain(mc)) {
            int sw = mc.getWindow().getGuiScaledWidth();
            int sh = mc.getWindow().getGuiScaledHeight();
            g.fill(0, 0, sw, 6, 0x604A148C);
            g.fill(0, sh - 6, sw, sh, 0x604A148C);
        }

        String grade = DataManager.gradeLabel(CursedClientState.grade);
        g.text(font, "\u2620 " + grade, x + 2, y + 2, 0xFFD9A6FF, true);

        if (DomainClientState.localActive != null) {
            CursedDomain d = DomainRegistry.byId(DomainClientState.localActive.domainId());
            String name = d == null ? "Domain active" : d.displayName();
            g.text(font, "\u25C9 " + name, x + 2, y + 13, 0xFFFFA857, true);
        }

        int rowY = y + 15 + extra;
        for (int i = 0; i < rows; i++) {
            String id = CursedClientState.bound[i];
            CursedTechnique t = (id == null || id.isEmpty()) ? null : TechniqueRegistry.byId(id);
            String name = t == null ? "\u2014 unbound" : t.displayName();
            int color = t == null ? 0xFF555F6E : (0xFF000000 | t.element().tintRgb());
            g.text(font, "[" + KEY_LABELS[i] + "] " + name, x + 2, rowY + i * 11, color, true);
        }
    }
}
