package com.political.client;

import com.political.curse.domain.CursedDomain;
import com.political.curse.domain.DomainRegistry;
import com.political.curse.technique.CursedTechnique;
import com.political.curse.technique.TechniqueRegistry;
import com.political.net.DomainActionC2S;
import com.political.net.TechniqueActionC2S;
import com.political.politics.DataManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Cursed-technique select / bind screen (JJK overhaul, Workstream A), in the shared {@link RpgScreen}
 * style. Lists the techniques the player has unlocked (by grade), lets them inspect each one, bind any
 * of them to the four cast keys ({@code Z/X/C/B}), cast the selected one immediately, and expand their
 * chosen domain. Bindings persist via {@link TechniqueBindings}.
 */
public class TechniqueScreen extends RpgScreen {

    public static TechniqueScreen OPEN;
    /** Last domain chosen for expansion (also used by the V keybind). */
    public static volatile String selectedDomainId = "";

    private static final int PANEL_W = 400;
    private static final int PANEL_H = 248;
    private static final int CELL = 20;
    private static final int PITCH = 22;
    private static final int COLS = 8;
    private static final String[] KEY_LABELS = {"Z", "X", "C", "B"};

    private int left, top;
    private String selectedId = "";
    private final List<Node> nodes = new ArrayList<>();
    private final List<DomainNode> domainNodes = new ArrayList<>();
    private Node hovered;
    private DomainNode domainHovered;

    private record Node(String id, String abbr, int colorTop, int colorBot, int x, int y) {}

    private record DomainNode(String id, String abbr, int color, int x, int y, int w, int h) {}

    public TechniqueScreen() {
        super(Component.literal("Cursed Techniques"));
        OPEN = this;
        if (!CursedClientState.known.isEmpty()) selectedId = CursedClientState.known.get(0);
        if (selectedDomainId.isEmpty() && !CursedClientState.domains.isEmpty()) {
            selectedDomainId = CursedClientState.domains.get(0);
        }
    }

    public void refresh() {
        if (selectedId.isEmpty() && !CursedClientState.known.isEmpty()) {
            selectedId = CursedClientState.known.get(0);
        }
        if ((selectedDomainId == null || selectedDomainId.isEmpty()) && !CursedClientState.domains.isEmpty()) {
            selectedDomainId = CursedClientState.domains.get(0);
        }
        if (minecraft != null) {
            layoutNodes();
            relayoutWidgets();
        }
    }

    @Override
    public void removed() {
        if (OPEN == this) OPEN = null;
        super.removed();
    }

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
        layoutNodes();
        relayoutWidgets();
    }

    private void relayoutWidgets() {
        clearWidgets();
        int bindY = top + PANEL_H - 58;
        for (int i = 0; i < 4; i++) {
            int slot = i;
            String bound = CursedClientState.bound[i];
            boolean hot = bound != null && bound.equals(selectedId);
            String label = KEY_LABELS[i] + (hot ? " \u2713" : "");
            addRenderableWidget(Button.builder(Component.literal(label), b -> bindSelected(slot))
                    .bounds(left + 10 + i * 48, bindY, 46, 18).build());
        }

        int actionY = top + PANEL_H - 34;
        addRenderableWidget(Button.builder(Component.literal("\u2620 Cast Selected"), b -> castSelected())
                .bounds(left + 10, actionY, 112, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Expand Domain"), b -> expandDomain())
                .bounds(left + 126, actionY, 118, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(left + PANEL_W - 84, actionY, 74, 20).build());
    }

    private void layoutNodes() {
        nodes.clear();
        domainNodes.clear();
        int gx = left + 12;
        int gy = top + 96;
        int i = 0;
        for (String id : CursedClientState.known) {
            CursedTechnique t = TechniqueRegistry.byId(id);
            if (t == null) continue;
            int col = i % COLS;
            int row = i / COLS;
            int argb = 0xFF000000 | t.element().tintRgb();
            int bot = argb & 0xFF000000 | ((argb & 0xFEFEFE) >> 1);
            nodes.add(new Node(id, abbr(t.displayName()), argb, bot, gx + col * PITCH, gy + row * PITCH));
            i++;
        }

        int dy = top + 28;
        int dx = left + 12;
        for (String id : CursedClientState.domains) {
            CursedDomain d = DomainRegistry.byId(id);
            if (d == null) continue;
            int w = Math.min(118, font.width(d.displayName()) + 14);
            domainNodes.add(new DomainNode(id, abbr(d.displayName()), 0xFF000000 | d.element().tintRgb(),
                    dx, dy, w, 16));
            dx += w + 4;
        }
    }

    private void bindSelected(int slot) {
        if (!selectedId.isEmpty()) {
            CursedClientState.bound[slot] = selectedId;
            TechniqueBindings.save();
            relayoutWidgets();
        }
    }

    private void castSelected() {
        if (!selectedId.isEmpty()) ClientPlayNetworking.send(new TechniqueActionC2S("cast", selectedId, 0));
    }

    private void expandDomain() {
        ClientPlayNetworking.send(new DomainActionC2S(selectedDomainId == null ? "" : selectedDomainId));
    }

    private static String abbr(String name) {
        String[] words = name.replaceAll("[^A-Za-z ]", "").trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        if (words.length == 1) {
            sb.append(words[0], 0, Math.min(3, words[0].length()));
        } else {
            for (String w : words) {
                if (w.isEmpty()) continue;
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (sb.length() >= 3) break;
            }
        }
        return sb.toString().toUpperCase();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractBackground(g, mouseX, mouseY, pt);
        g.fill(0, 0, width, height, COL_DIM);
        panel(g, left, top, PANEL_W, PANEL_H);
        headerBar(g, left, top, PANEL_W, 22);
        inset(g, left + 8, top + 92, COLS * PITCH + 4, 72);
        inset(g, left + 208, top + 28, PANEL_W - 218, 136);
        inset(g, left + 8, top + 170, PANEL_W - 16, 36);

        hovered = null;
        for (Node n : nodes) {
            boolean sel = n.id.equals(selectedId);
            boolean hot = mouseX >= n.x && mouseX < n.x + CELL && mouseY >= n.y && mouseY < n.y + CELL;
            if (hot) hovered = n;
            g.fillGradient(n.x, n.y, n.x + CELL, n.y + CELL, n.colorTop, n.colorBot);
            g.outline(n.x, n.y, CELL, CELL, sel ? COL_GOLD : (hot ? COL_ACCENT : 0xFF000000));
            int tw = font.width(n.abbr);
            g.text(font, n.abbr, n.x + (CELL - tw) / 2, n.y + (CELL - 8) / 2, 0xFFFFFFFF, true);
        }

        domainHovered = null;
        for (DomainNode dn : domainNodes) {
            boolean sel = dn.id.equals(selectedDomainId);
            boolean hot = mouseX >= dn.x && mouseX < dn.x + dn.w && mouseY >= dn.y && mouseY < dn.y + dn.h;
            if (hot) domainHovered = dn;
            g.fill(dn.x, dn.y, dn.x + dn.w, dn.y + dn.h, sel ? (dn.color | 0xFF000000) : 0xF0101418);
            g.outline(dn.x, dn.y, dn.w, dn.h, sel ? COL_GOLD : (hot ? COL_ACCENT : 0xFF2A3344));
            g.text(font, dn.abbr, dn.x + 4, dn.y + 4, 0xFFFFFFFF, true);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractRenderState(g, mouseX, mouseY, pt);
        centered(g, Component.literal("CURSED TECHNIQUES").withStyle(ChatFormatting.BOLD),
                left + PANEL_W / 2, top + 7, COL_ACCENT);

        int dx = left + 214;
        int dy = top + 33;
        kv(g, "Grade:", DataManager.gradeLabel(CursedClientState.grade), dx, dy, COL_TEXT);
        float max = Math.max(1f, CursedClientState.maxCursedEnergy);
        statBar(g, dx, dy + 12, PANEL_W - 228, 10, CursedClientState.cursedEnergy / max,
                0xFF160622, 0xFFC065FF, 0xFF7A26C9,
                Component.literal("CE " + (int) CursedClientState.cursedEnergy + "/" + (int) CursedClientState.maxCursedEnergy),
                0xFFFFFFFF);

        String detailId = hovered != null ? hovered.id : selectedId;
        CursedTechnique t = TechniqueRegistry.byId(detailId);
        int ty = top + 96;
        if (t != null) {
            label(g, Component.literal(t.displayName()).withStyle(ChatFormatting.BOLD), dx, ty, COL_ACCENT);
            ty += 11;
            label(g, t.element().name() + "  \u2022  CE " + (int) t.ceCost()
                    + "  \u2022  CD " + String.format("%.1fs", t.cooldownTicks() / 20.0), dx, ty, COL_GOLD);
            ty += 11;
            label(g, "Requires: " + DataManager.gradeLabel(t.requiredGrade()), dx, ty, COL_TEXT_DIM);
            ty += 12;
            for (String line : wrap(t.description(), PANEL_W - 228)) {
                label(g, line, dx, ty, COL_TEXT);
                ty += 10;
            }
            ty += 4;
            label(g, "Bound to: " + boundSlotsFor(detailId), dx, ty, COL_TEXT_DIM);
        }

        label(g, "Domains (click to select, V or button to expand):", left + 14, top + 176, COL_TEXT_DIM);
        String domainDetailId = domainHovered != null ? domainHovered.id : selectedDomainId;
        CursedDomain dom = DomainRegistry.byId(domainDetailId);
        if (dom != null) {
            label(g, dom.displayName() + "  \u2022  CE " + (int) dom.ceCost()
                    + "  \u2022  r=" + (int) dom.radius(), left + 14, top + 188, COL_GOLD);
        } else if (CursedClientState.domains.isEmpty()) {
            label(g, "No domains unlocked yet.", left + 14, top + 188, COL_TEXT_DIM);
        }

        label(g, "Bind selected technique to Z / X / C / B, then cast from the HUD or hotkeys.",
                left + 12, top + PANEL_H - 72, COL_TEXT_DIM);
    }

    private String boundSlotsFor(String id) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CursedClientState.bound.length; i++) {
            if (id.equals(CursedClientState.bound[i])) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(KEY_LABELS[i]);
            }
        }
        return sb.length() == 0 ? "none" : sb.toString();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (event.button() == 0) {
            for (Node n : nodes) {
                if (event.x() >= n.x && event.x() < n.x + CELL && event.y() >= n.y && event.y() < n.y + CELL) {
                    selectedId = n.id;
                    return true;
                }
            }
            for (DomainNode dn : domainNodes) {
                if (event.x() >= dn.x && event.x() < dn.x + dn.w
                        && event.y() >= dn.y && event.y() < dn.y + dn.h) {
                    selectedDomainId = dn.id;
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubled);
    }

    private List<String> wrap(String text, int maxWidth) {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String trial = line.isEmpty() ? word : line + " " + word;
            if (font.width(trial) > maxWidth && line.length() > 0) {
                out.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(trial);
            }
        }
        if (line.length() > 0) out.add(line.toString());
        return out;
    }
}
