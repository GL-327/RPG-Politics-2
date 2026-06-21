package com.political.expansion2.powers;

import com.political.client.RpgScreen;
import com.political.net.PowerActionC2S;
import com.political.net.PowerMenuS2C;
import com.political.politics.DataManager;
import com.political.power.Power;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extended Powers GUI including Expansion 2 categories. Integration agent may swap this in
 * for {@link com.political.client.PowersScreen} or merge its tab logic.
 */
public class PowersScreen2 extends RpgScreen {

    public static PowersScreen2 OPEN;

    private static final int PANEL_W = 400;
    private static final int PANEL_H = 250;
    private static final int CELL = 18;
    private static final int PITCH = 20;
    private static final int COLS = 9;

    private PowerMenuS2C data;
    private Set<String> known = new HashSet<>();
    private String selectedId = "";
    /** 0–1 base Power tabs; 2–7 Power2 categories. */
    private int tab = 0;

    private int left, top;
    private final List<Node> nodes = new ArrayList<>();
    private Node hovered;

    private record Node(String id, String displayName, String abbr, int colorTop, int colorBot, int x, int y, boolean known) {}

    public PowersScreen2(PowerMenuS2C payload) {
        super(Component.literal("Powers & Serums II"));
        OPEN = this;
        apply(payload);
    }

    public static void registerClient() {
        // Hook point: integration agent may register PowerMenuS2C receiver to open PowersScreen2 instead of PowersScreen.
    }

    @Override
    public void removed() {
        if (OPEN == this) OPEN = null;
        super.removed();
    }

    public void apply(PowerMenuS2C payload) {
        this.data = payload;
        this.selectedId = payload.selected();
        this.known = new HashSet<>();
        if (!payload.known().isEmpty()) {
            for (String s : payload.known().split(",")) if (!s.isBlank()) known.add(s.trim());
        }
        if (this.minecraft != null) layoutNodes();
    }

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
        int tabY = top + 62;
        String[] labels = {"Compound V", "Techniques", "Viltrumite", "Heroes", "JJK II", "Domains", "Ultimates", "Passives"};
        for (int i = 0; i < labels.length; i++) {
            int t = i;
            addRenderableWidget(Button.builder(Component.literal(labels[i]), b -> { tab = t; layoutNodes(); })
                    .bounds(left + 8 + (i % 4) * 96, tabY + (i / 4) * 20, 92, 18).build());
        }
        addRenderableWidget(Button.builder(Component.literal("\u26A1 Activate"), b ->
                        ClientPlayNetworking.send(new PowerActionC2S("activate", selectedId)))
                .bounds(left + 12, top + PANEL_H - 26, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(left + PANEL_W - 92, top + PANEL_H - 26, 80, 20).build());
        layoutNodes();
    }

    private void layoutNodes() {
        nodes.clear();
        int gx = left + 10;
        int gy = top + 108;
        int i = 0;
        if (tab == 0) {
            for (Power p : Power.ofOrigin(Power.Origin.COMPOUND_V)) addNode(p.id(), p.displayName, p.origin == Power.Origin.COMPOUND_V, gx, gy, i++);
        } else if (tab == 1) {
            for (Power p : Power.ofOrigin(Power.Origin.CURSED_TECHNIQUE)) addNode(p.id(), p.displayName, false, gx, gy, i++);
        } else {
            Power2.Category cat = Power2.Category.values()[tab - 2];
            for (Power2 p : Power2.ofCategory(cat)) addNode2(p, cat, gx, gy, i++);
        }
    }

    private void addNode(String id, String name, boolean compound, int gx, int gy, int i) {
        int col = i % COLS, row = i / COLS;
        int top2 = compound ? 0xFFC0413F : 0xFF8A47D6;
        int bot = compound ? 0xFF7C1F1E : 0xFF4C1F86;
        nodes.add(new Node(id, name, abbr(name), top2, bot, gx + col * PITCH, gy + row * PITCH, known.contains(id)));
    }

    private void addNode2(Power2 p, Power2.Category cat, int gx, int gy, int i) {
        int col = i % COLS, row = i / COLS;
        int top2 = switch (cat) {
            case VILTRUMITE -> 0xFFC0413F;
            case HERO -> 0xFFD4A017;
            case JJK_TECHNIQUE -> 0xFF8A47D6;
            case DOMAIN -> 0xFF6B2FA0;
            case ULTIMATE -> 0xFFE6C200;
            case PASSIVE -> 0xFF2E8B57;
        };
        int bot = top2 & 0xFF000000 | ((top2 & 0xFEFEFE) >> 1);
        nodes.add(new Node(p.id(), p.displayName, abbr(p.displayName), top2, bot, gx + col * PITCH, gy + row * PITCH, known.contains(p.id())));
    }

    private static String abbr(String name) {
        String[] words = name.replaceAll("[^A-Za-z ]", "").trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        if (words.length == 1) sb.append(words[0], 0, Math.min(3, words[0].length()));
        else for (String w : words) {
            if (w.isEmpty()) continue;
            sb.append(Character.toUpperCase(w.charAt(0)));
            if (sb.length() >= 3) break;
        }
        return sb.toString().toUpperCase();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractBackground(g, mouseX, mouseY, pt);
        g.fill(0, 0, width, height, COL_DIM);
        panel(g, left, top, PANEL_W, PANEL_H);
        headerBar(g, left, top, PANEL_W, 22);
        inset(g, left + 8, top + 104, COLS * PITCH + 4, PANEL_H - 138);
        inset(g, left + 200, top + 28, PANEL_W - 212, 72);
        hovered = null;
        for (Node n : nodes) {
            boolean sel = n.id.equals(selectedId);
            boolean hot = mouseX >= n.x && mouseX < n.x + CELL && mouseY >= n.y && mouseY < n.y + CELL;
            if (hot) hovered = n;
            g.fillGradient(n.x, n.y, n.x + CELL, n.y + CELL, n.known ? n.colorTop : (n.colorTop & 0xFF000000 | 0x202020), n.known ? n.colorBot : 0xFF181818);
            g.outline(n.x, n.y, CELL, CELL, sel ? COL_GOLD : (hot ? COL_ACCENT : 0xFF000000));
            int tw = font.width(n.abbr);
            g.text(font, n.abbr, n.x + (CELL - tw) / 2, n.y + (CELL - 8) / 2, n.known ? 0xFFFFFFFF : 0xFF6A6A6A, true);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractRenderState(g, mouseX, mouseY, pt);
        centered(g, Component.literal("POWERS & SERUMS II").withStyle(ChatFormatting.BOLD), left + PANEL_W / 2, top + 7, COL_ACCENT);
        int dx = left + 206;
        int dy = top + 34;
        kv(g, "Grade:", DataManager.gradeLabel(data.grade()), dx, dy, COL_TEXT);
        kv(g, "Selected:", displayOf(selectedId), dx, dy + 11, COL_GOLD);
        statBar(g, left + 206, top + 52, PANEL_W - 220, 10,
                data.maxMana() <= 0 ? 0 : (float) data.mana() / data.maxMana(),
                0xFF06121C, 0xFF49C4FF, 0xFF1C7FD0,
                Component.literal("Mana " + data.mana() + "/" + data.maxMana()), 0xFFFFFFFF);
        if (data.maxCursedEnergy() > 0) {
            statBar(g, left + 206, top + 66, PANEL_W - 220, 10,
                    (float) data.cursedEnergy() / data.maxCursedEnergy(),
                    0xFF160622, 0xFFC065FF, 0xFF7A26C9,
                    Component.literal("CE " + data.cursedEnergy() + "/" + data.maxCursedEnergy()), 0xFFFFFFFF);
        }
        Node det = hovered;
        if (det != null) {
            dy = top + 112;
            label(g, Component.literal(det.displayName).withStyle(ChatFormatting.BOLD), left + 206, dy, COL_ACCENT);
            Power2 p2 = Power2.byId(det.id);
            Power p1 = Power.byId(det.id);
            String desc = p2 != null ? p2.description : (p1 != null ? p1.description : "");
            for (String line : wrap(desc, PANEL_W - 220)) {
                dy += 10;
                label(g, line, left + 206, dy, COL_TEXT);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (event.button() == 0) {
            for (Node n : nodes) {
                if (event.x() >= n.x && event.x() < n.x + CELL && event.y() >= n.y && event.y() < n.y + CELL) {
                    if (n.known) {
                        selectedId = n.id;
                        ClientPlayNetworking.send(new PowerActionC2S("select", selectedId));
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubled);
    }

    private String displayOf(String id) {
        Power2 p2 = Power2.byId(id);
        if (p2 != null) return p2.displayName;
        Power p1 = Power.byId(id);
        return p1 == null ? (id.isEmpty() ? "none" : id) : p1.displayName;
    }

    private List<String> wrap(String text, int maxWidth) {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String trial = line.isEmpty() ? word : line + " " + word;
            if (font.width(trial) > maxWidth && !line.isEmpty()) {
                out.add(line.toString());
                line = new StringBuilder(word);
            } else line = new StringBuilder(trial);
        }
        if (!line.isEmpty()) out.add(line.toString());
        return out;
    }
}
