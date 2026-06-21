package com.political.client;

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
 * Powers &amp; Serums menu: a skill-grid of every power (bright if awakened, dimmed if locked),
 * a live readout of grade / aptitude / Mana / Cursed Energy, a detail panel, and Activate.
 * Pure custom rendering on the 26.2 pipeline (no mixins, no textures).
 */
public class PowersScreen extends RpgScreen {

    private static final int PANEL_W = 380;
    private static final int PANEL_H = 236;
    private static final int CELL = 19;
    private static final int PITCH = 21;
    private static final int COLS = 8;

    /** The currently open instance, so a server resend refreshes it in place (no Minecraft.screen in 26.2). */
    public static PowersScreen OPEN;

    private PowerMenuS2C data;
    private Set<String> known = new HashSet<>();
    private String selectedId = "";
    private int tab = 0; // 0 = Compound V, 1 = Cursed Techniques

    private int left, top;
    private final List<Node> nodes = new ArrayList<>();
    private Node hovered;

    private record Node(Power power, int x, int y, boolean known, String abbr) {}

    public PowersScreen(PowerMenuS2C payload) {
        super(Component.literal("Powers & Serums"));
        OPEN = this;
        apply(payload);
    }

    @Override
    public void removed() {
        if (OPEN == this) OPEN = null;
        super.removed();
    }

    /** Refresh from a server resend without losing the open screen. */
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

        int tabY = top + 64;
        addRenderableWidget(Button.builder(Component.literal("Compound V"), b -> { tab = 0; layoutNodes(); })
                .bounds(left + 12, tabY, 110, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Cursed Techniques"), b -> { tab = 1; layoutNodes(); })
                .bounds(left + 126, tabY, 130, 18).build());

        addRenderableWidget(Button.builder(Component.literal("\u26A1 Activate"), b ->
                        ClientPlayNetworking.send(new PowerActionC2S("activate", selectedId)))
                .bounds(left + 12, top + PANEL_H - 26, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(left + PANEL_W - 92, top + PANEL_H - 26, 80, 20).build());

        layoutNodes();
    }

    private void layoutNodes() {
        nodes.clear();
        Power.Origin origin = tab == 0 ? Power.Origin.COMPOUND_V : Power.Origin.CURSED_TECHNIQUE;
        int gx = left + 12;
        int gy = top + 90;
        int i = 0;
        for (Power p : Power.ofOrigin(origin)) {
            int col = i % COLS;
            int row = i / COLS;
            nodes.add(new Node(p, gx + col * PITCH, gy + row * PITCH, known.contains(p.id()), abbr(p)));
            i++;
        }
    }

    private static String abbr(Power p) {
        String[] words = p.displayName.replaceAll("[^A-Za-z ]", "").trim().split("\\s+");
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

        // Info strip insets.
        inset(g, left + 12, top + 28, 168, 32);
        inset(g, left + 190, top + 28, PANEL_W - 202, 32);
        // Grid + detail backgrounds.
        inset(g, left + 8, top + 86, COLS * PITCH + 4, PANEL_H - 120);
        inset(g, left + 190, top + 86, PANEL_W - 202, PANEL_H - 120);

        // Power nodes (drawn in background so widgets/text overlay cleanly).
        hovered = null;
        for (Node n : nodes) {
            boolean sel = n.power.id().equals(selectedId);
            boolean hot = mouseX >= n.x && mouseX < n.x + CELL && mouseY >= n.y && mouseY < n.y + CELL;
            if (hot) hovered = n;
            int top2, bot;
            if (n.power.origin == Power.Origin.COMPOUND_V) {
                top2 = n.known ? 0xFFC0413F : 0xFF3A2020;
                bot = n.known ? 0xFF7C1F1E : 0xFF241414;
            } else {
                top2 = n.known ? 0xFF8A47D6 : 0xFF291E3A;
                bot = n.known ? 0xFF4C1F86 : 0xFF181024;
            }
            g.fillGradient(n.x, n.y, n.x + CELL, n.y + CELL, top2, bot);
            int border = sel ? COL_GOLD : (hot ? COL_ACCENT : 0xFF000000);
            g.outline(n.x, n.y, CELL, CELL, border);
            int tw = font.width(n.abbr);
            g.text(font, n.abbr, n.x + (CELL - tw) / 2, n.y + (CELL - 8) / 2,
                    n.known ? 0xFFFFFFFF : 0xFF6A6A6A, true);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractRenderState(g, mouseX, mouseY, pt);
        centered(g, Component.literal("POWERS & SERUMS").withStyle(ChatFormatting.BOLD), left + PANEL_W / 2, top + 7, COL_ACCENT);

        // --- Info strip ---
        int ix = left + 16;
        int iy = top + 31;
        iy = kv(g, "Grade:", DataManager.gradeLabel(data.grade()), ix, iy, COL_TEXT);
        iy = kv(g, "Aptitude:", data.trait(), ix, iy, 0xFFB07CF0);
        String selName = selectedId.isEmpty() ? "none" : displayOf(selectedId);
        kv(g, "Selected:", selName, ix, iy, COL_GOLD);

        // Mana + Cursed Energy bars.
        int bx = left + 196;
        int bw = PANEL_W - 208;
        float manaFrac = data.maxMana() <= 0 ? 0 : (float) data.mana() / data.maxMana();
        statBar(g, bx, top + 34, bw, 10, manaFrac, 0xFF06121C, 0xFF49C4FF, 0xFF1C7FD0,
                Component.literal("Mana " + data.mana() + "/" + data.maxMana()), 0xFFFFFFFF);
        if (data.maxCursedEnergy() > 0) {
            float ceFrac = (float) data.cursedEnergy() / data.maxCursedEnergy();
            statBar(g, bx, top + 48, bw, 10, ceFrac, 0xFF160622, 0xFFC065FF, 0xFF7A26C9,
                    Component.literal("Cursed Energy " + data.cursedEnergy() + "/" + data.maxCursedEnergy()), 0xFFFFFFFF);
        } else {
            label(g, "No Cursed Energy (Heavenly Restriction)", bx, top + 50, COL_TEXT_DIM);
        }

        // --- Detail panel (selected power) ---
        int dx = left + 196;
        int dy = top + 92;
        Power det = Power.byId(selectedId);
        if (det == null && hovered != null) det = hovered.power;
        if (det != null) {
            label(g, Component.literal(det.displayName).withStyle(ChatFormatting.BOLD), dx, dy, originColor(det.origin));
            dy += 12;
            label(g, det.origin.label, dx, dy, COL_TEXT_DIM);
            dy += 13;
            for (String line : wrap(det.description, PANEL_W - 214)) {
                label(g, line, dx, dy, COL_TEXT);
                dy += 10;
            }
            dy += 2;
            String resource = det.origin == Power.Origin.CURSED_TECHNIQUE ? " CE" : " Mana";
            kv(g, "Cost:", det.energyCost + resource, dx, dy, COL_ACCENT);
            kv(g, "Cooldown:", (det.cooldownTicks / 20) + "s", dx, dy + 11, 0xFF7CE0A0);
            kv(g, "Status:", known.contains(det.id()) ? "Awakened" : "Locked", dx, dy + 22,
                    known.contains(det.id()) ? 0xFF7CE0A0 : 0xFFE06A6A);
        } else {
            label(g, "Select a power node.", dx, dy, COL_TEXT_DIM);
        }

        // Hover tooltip for a node.
        if (hovered != null) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal(hovered.power.displayName).withStyle(hovered.power.origin.color, ChatFormatting.BOLD));
            tip.add(Component.literal(hovered.power.description).withStyle(ChatFormatting.GRAY));
            tip.add(Component.literal((hovered.known ? "Click to select" : "Locked")).withStyle(
                    hovered.known ? ChatFormatting.GREEN : ChatFormatting.DARK_RED));
            g.setComponentTooltipForNextFrame(font, tip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (event.button() == 0) {
            double mouseX = event.x();
            double mouseY = event.y();
            for (Node n : nodes) {
                if (mouseX >= n.x && mouseX < n.x + CELL && mouseY >= n.y && mouseY < n.y + CELL) {
                    if (n.known) {
                        selectedId = n.power.id();
                        ClientPlayNetworking.send(new PowerActionC2S("select", selectedId));
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubled);
    }

    private static int originColor(Power.Origin o) {
        return o == Power.Origin.COMPOUND_V ? 0xFFFF6B6B : 0xFFC065FF;
    }

    private String displayOf(String id) {
        Power p = Power.byId(id);
        return p == null ? id : p.displayName;
    }

    private List<String> wrap(String text, int maxWidth) {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String trial = line.length() == 0 ? word : line + " " + word;
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
