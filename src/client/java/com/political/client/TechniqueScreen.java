package com.political.client;

import com.political.curse.domain.CursedDomain;
import com.political.curse.domain.DomainRegistry;
import com.political.curse.jjk.JjkPreset;
import com.political.curse.jjk.JjkPreset;
import com.political.curse.jjk.JjkPresetRegistry;
import com.political.curse.limb.CursedLimb;
import com.political.curse.limb.LimbTechniqueMap;
import com.political.curse.technique.CursedTechnique;
import com.political.curse.technique.TechniqueRegistry;
import com.political.net.DomainActionC2S;
import com.political.net.JjkProfileC2S;
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
 * Cursed-technique select / bind screen with a People Playground–style limb pathway picker and JJP
 * energy preset selector.
 */
public class TechniqueScreen extends RpgScreen {

    public static TechniqueScreen OPEN;
    public static volatile String selectedDomainId = "";

    private enum Tab { TECHNIQUES, LIMBS }

    private static final int PANEL_W = 420;
    private static final int PANEL_H = 268;
    private static final int CELL = 20;
    private static final int PITCH = 22;
    private static final int COLS = 8;
    private static final String[] KEY_LABELS = {"Z", "X", "C", "B"};

    private int left, top;
    private Tab tab = Tab.TECHNIQUES;
    private String selectedId = "";
    private final List<Node> nodes = new ArrayList<>();
    private final List<DomainNode> domainNodes = new ArrayList<>();
    private Node hovered;
    private DomainNode domainHovered;
    private CursedLimb limbHovered;

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
        addRenderableWidget(Button.builder(Component.literal("Techniques"), b -> switchTab(Tab.TECHNIQUES))
                .bounds(left + 8, top + 24, 96, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Limbs & Preset"), b -> switchTab(Tab.LIMBS))
                .bounds(left + 108, top + 24, 110, 18).build());

        if (tab == Tab.TECHNIQUES) {
            int bindY = top + PANEL_H - 58;
            for (int i = 0; i < 4; i++) {
                int slot = i;
                String bound = CursedClientState.bound[i];
                boolean hot = bound != null && bound.equals(selectedId);
                addRenderableWidget(Button.builder(Component.literal(KEY_LABELS[i] + (hot ? " \u2713" : "")),
                                b -> bindSelected(slot))
                        .bounds(left + 10 + i * 48, bindY, 46, 18).build());
            }
            int actionY = top + PANEL_H - 34;
            addRenderableWidget(Button.builder(Component.literal("\u2620 Cast Selected"), b -> castSelected())
                    .bounds(left + 10, actionY, 112, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Expand Domain"), b -> expandDomain())
                    .bounds(left + 126, actionY, 118, 20).build());
        } else {
            addRenderableWidget(Button.builder(Component.literal("Cycle JJP Preset"), b -> cyclePreset())
                    .bounds(left + 10, top + PANEL_H - 58, 130, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Enable All Limbs"), b -> setAllLimbs(true))
                    .bounds(left + 146, top + PANEL_H - 58, 120, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Seal All"), b -> setAllLimbs(false))
                    .bounds(left + 272, top + PANEL_H - 58, 90, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(left + PANEL_W - 84, top + PANEL_H - 34, 74, 20).build());
    }

    private void switchTab(Tab next) {
        tab = next;
        relayoutWidgets();
    }

    private void layoutNodes() {
        nodes.clear();
        domainNodes.clear();
        int gx = left + 12;
        int gy = top + 112;
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

        int dy = top + 48;
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

    private void cyclePreset() {
        List<String> ids = new ArrayList<>(CursedClientState.presetIds);
        if (ids.isEmpty()) {
            JjkPresetRegistry.bootstrap();
            for (JjkPreset p : JjkPresetRegistry.all()) ids.add(p.id());
        }
        if (ids.isEmpty()) return;
        int idx = ids.indexOf(CursedClientState.presetId == null ? "" : CursedClientState.presetId);
        String next;
        if (idx < 0) next = ids.get(0);
        else if (idx >= ids.size() - 1) next = "";
        else next = ids.get(idx + 1);
        CursedClientState.presetId = next;
        TechniqueBindings.save();
        ClientPlayNetworking.send(new JjkProfileC2S("set_preset", next, 0, true));
    }

    private void setAllLimbs(boolean enabled) {
        int mask = enabled ? CursedLimb.ALL_ENABLED_MASK : 0;
        CursedClientState.limbMask = mask;
        TechniqueBindings.save();
        for (CursedLimb limb : CursedLimb.values()) {
            ClientPlayNetworking.send(new JjkProfileC2S("toggle_limb", "", limb.ordinal(), enabled));
        }
    }

    private void toggleLimb(CursedLimb limb) {
        boolean next = !CursedLimb.isEnabled(CursedClientState.limbMask, limb);
        CursedClientState.limbMask = CursedLimb.toggle(CursedClientState.limbMask, limb, next);
        TechniqueBindings.save();
        ClientPlayNetworking.send(new JjkProfileC2S("toggle_limb", "", limb.ordinal(), next));
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
        glassBackdrop(g);
        panel(g, left, top, PANEL_W, PANEL_H);
        headerBar(g, left, top, PANEL_W, 22);

        if (tab == Tab.TECHNIQUES) {
            inset(g, left + 8, top + 108, COLS * PITCH + 4, 72);
            inset(g, left + 208, top + 44, PANEL_W - 218, 136);
            inset(g, left + 8, top + 186, PANEL_W - 16, 36);

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
        } else {
            inset(g, left + 8, top + 48, PANEL_W - 16, PANEL_H - 110);
            drawLimbGrid(g, mouseX, mouseY);
        }
    }

    private void drawLimbGrid(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        limbHovered = null;
        int gx = left + 24;
        int gy = top + 58;
        int colW = 88;
        int rowH = 22;
        int cols = 2;
        for (int i = 0; i < CursedLimb.values().length; i++) {
            CursedLimb limb = CursedLimb.values()[i];
            int col = i % cols;
            int row = i / cols;
            int x = gx + col * (colW + 8);
            int y = gy + row * rowH;
            int w = colW;
            int h = 18;
            boolean enabled = CursedLimb.isEnabled(CursedClientState.limbMask, limb);
            boolean hot = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
            if (hot) limbHovered = limb;
            g.fill(x, y, x + w, y + h, enabled ? 0xCC1A2838 : 0xAA0A0E14);
            g.outline(x, y, w, h, enabled ? (hot ? COL_ACCENT : COL_GOLD) : 0xFF553344);
            String status = enabled ? "\u2713" : "\u2717";
            g.text(font, status + " " + limb.label, x + 4, y + 5, enabled ? 0xFFE6ECF5 : 0xFF777F8E, true);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractRenderState(g, mouseX, mouseY, pt);
        centered(g, Component.literal("CURSED TECHNIQUES").withStyle(ChatFormatting.BOLD),
                left + PANEL_W / 2, top + 7, COL_ACCENT);

        int dx = left + 214;
        int dy = top + 49;
        kv(g, "Grade:", DataManager.gradeLabel(CursedClientState.grade), dx, dy, COL_TEXT);
        float max = Math.max(1f, CursedClientState.maxCursedEnergy);
        statBar(g, dx, dy + 12, PANEL_W - 228, 10, CursedClientState.cursedEnergy / max,
                0xFF160622, 0xFFC065FF, 0xFF7A26C9,
                Component.literal("CE " + (int) CursedClientState.cursedEnergy + "/" + (int) CursedClientState.maxCursedEnergy),
                0xFFFFFFFF);

        if (tab == Tab.TECHNIQUES) {
            renderTechniqueDetail(g, dx);
        } else {
            renderLimbDetail(g);
        }
    }

    private void renderTechniqueDetail(GuiGraphicsExtractor g, int dx) {
        String detailId = hovered != null ? hovered.id : selectedId;
        CursedTechnique t = TechniqueRegistry.byId(detailId);
        int ty = top + 112;
        if (t != null) {
            label(g, Component.literal(t.displayName()).withStyle(ChatFormatting.BOLD), dx, ty, COL_ACCENT);
            ty += 11;
            CursedLimb limb = LimbTechniqueMap.limbFor(t.id());
            label(g, t.element().name() + "  \u2022  CE " + (int) t.ceCost()
                    + "  \u2022  Path: " + limb.label, dx, ty, COL_GOLD);
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

        label(g, "Domains (click to select, V or button to expand):", left + 14, top + 192, COL_TEXT_DIM);
        String domainDetailId = domainHovered != null ? domainHovered.id : selectedDomainId;
        CursedDomain dom = DomainRegistry.byId(domainDetailId);
        if (dom != null) {
            label(g, dom.displayName() + "  \u2022  CE " + (int) dom.ceCost()
                    + "  \u2022  r=" + (int) dom.radius(), left + 14, top + 204, COL_GOLD);
        } else if (CursedClientState.domains.isEmpty()) {
            label(g, "No domains unlocked yet.", left + 14, top + 204, COL_TEXT_DIM);
        }

        label(g, "Bind selected technique to Z / X / C / B, then cast from the HUD or hotkeys.",
                left + 12, top + PANEL_H - 72, COL_TEXT_DIM);
    }

    private void renderLimbDetail(GuiGraphicsExtractor g) {
        int tx = left + 220;
        int ty = top + 58;
        label(g, "Limb Pathways", tx, ty, COL_ACCENT);
        ty += 12;
        label(g, "Seal a limb to block every technique routed through it.", tx, ty, COL_TEXT_DIM);
        ty += 14;
        if (limbHovered != null) {
            boolean on = CursedLimb.isEnabled(CursedClientState.limbMask, limbHovered);
            label(g, limbHovered.label + " — " + (on ? "OPEN" : "SEALED"), tx, ty, on ? COL_GOLD : 0xFFFF7A7A);
            ty += 12;
            var linked = LimbTechniqueMap.techniquesFor(limbHovered);
            if (linked.isEmpty()) {
                label(g, "No techniques mapped.", tx, ty, COL_TEXT_DIM);
            } else {
                label(g, "Techniques: " + String.join(", ", linked.stream()
                        .map(id -> {
                            CursedTechnique t = TechniqueRegistry.byId(id);
                            return t == null ? id : t.displayName();
                        }).limit(4).toList()), tx, ty, COL_TEXT);
            }
        }
        ty = top + 170;
        label(g, "JJP Energy Preset", tx, ty, COL_ACCENT);
        ty += 12;
        String presetLabel = "None (grade scaling only)";
        if (CursedClientState.presetId != null && !CursedClientState.presetId.isBlank()) {
            JjkPreset p = JjkPresetRegistry.byId(CursedClientState.presetId);
            presetLabel = p == null ? CursedClientState.presetId : p.displayName();
        }
        label(g, "Active: " + presetLabel, tx, ty, COL_GOLD);
        ty += 11;
        label(g, "Cycle preset to apply PP-style CE pool + regen curves.", tx, ty, COL_TEXT_DIM);
    }

    private String boundSlotsFor(String id) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CursedClientState.bound.length; i++) {
            if (id.equals(CursedClientState.bound[i])) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(KEY_LABELS[i]);
            }
        }
        return sb.isEmpty() ? "none" : sb.toString();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (event.button() == 0) {
            if (tab == Tab.TECHNIQUES) {
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
            } else if (limbHovered != null) {
                toggleLimb(limbHovered);
                return true;
            }
        }
        return super.mouseClicked(event, doubled);
    }

    private List<String> wrap(String text, int maxWidth) {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String trial = line.isEmpty() ? word : line + " " + word;
            if (font.width(trial) > maxWidth && !line.isEmpty()) {
                out.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(trial);
            }
        }
        if (!line.isEmpty()) out.add(line.toString());
        return out;
    }
}
