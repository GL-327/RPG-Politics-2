package com.political.client;

import com.political.net.GovActionC2S;
import com.political.net.GovMenuS2C;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Governance GUI: officials, treasury, elections, tax, your citizenship/rank, and quick actions. */
public class GovScreen extends RpgScreen {

    private static final int PANEL_W = 340;
    private static final int PANEL_H = 236;

    /** The currently open instance, so a server resend refreshes it in place (no Minecraft.screen in 26.2). */
    public static GovScreen OPEN;

    private GovMenuS2C data;
    private int left, top;

    public GovScreen(GovMenuS2C payload) {
        super(Component.literal("Governance"));
        OPEN = this;
        this.data = payload;
    }

    @Override
    public void removed() {
        if (OPEN == this) OPEN = null;
        super.removed();
    }

    public void apply(GovMenuS2C payload) {
        this.data = payload;
    }

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;

        // Pay tax (the owed amount is shown in the "You" panel).
        addRenderableWidget(Button.builder(
                        Component.literal("Pay Tax"),
                        b -> ClientPlayNetworking.send(new GovActionC2S("paytax", "")))
                .bounds(left + 12, top + PANEL_H - 26, 120, 20).build());

        // Vote buttons (only while an election is active).
        if (!data.candidates().isEmpty()) {
            int vx = left + 140;
            int vy = top + PANEL_H - 26;
            int i = 0;
            for (String entry : data.candidates().split(";")) {
                String[] parts = entry.split("\\|", 2);
                if (parts.length < 2) continue;
                final String uuid = parts[0];
                String label = parts[1];
                if (i >= 3) break;
                addRenderableWidget(Button.builder(Component.literal("Vote: " + label),
                                b -> ClientPlayNetworking.send(new GovActionC2S("vote", uuid)))
                        .bounds(vx, vy, 92, 20).build());
                vx += 96;
                i++;
            }
        }

        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(left + PANEL_W - 60, top + 4, 52, 14).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractBackground(g, mouseX, mouseY, pt);
        g.fill(0, 0, width, height, COL_DIM);
        panel(g, left, top, PANEL_W, PANEL_H);
        headerBar(g, left, top, PANEL_W, 22);
        inset(g, left + 12, top + 28, PANEL_W / 2 - 18, 150);
        inset(g, left + PANEL_W / 2 + 6, top + 28, PANEL_W / 2 - 18, 150);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractRenderState(g, mouseX, mouseY, pt);
        centered(g, Component.literal("\u2696 GOVERNANCE").withStyle(ChatFormatting.BOLD), left + PANEL_W / 2, top + 7, COL_ACCENT);

        // Left column: the state.
        int lx = left + 18;
        int ly = top + 34;
        label(g, Component.literal("The Government").withStyle(ChatFormatting.BOLD), lx, ly, COL_GOLD);
        ly += 13;
        ly = kv(g, "Chair:", data.chair(), lx, ly, COL_TEXT);
        ly = kv(g, "Vice Chair:", data.vice(), lx, ly, COL_TEXT);
        ly = kv(g, "Judge:", data.judge(), lx, ly, COL_TEXT);
        ly = kv(g, "Dictator:", data.dictator(), lx, ly, "none".equals(data.dictator()) ? COL_TEXT_DIM : 0xFFE06A6A);
        ly += 4;
        ly = kv(g, "Treasury:", data.treasury() + " coins", lx, ly, COL_GOLD);
        ly = kv(g, "Election:", data.electionStatus(), lx, ly, COL_ACCENT);
        ly = kv(g, "Tax:", data.taxStatus(), lx, ly, 0xFF7CE0A0);

        // Right column: the player.
        int rx = left + PANEL_W / 2 + 12;
        int ry = top + 34;
        label(g, Component.literal("You").withStyle(ChatFormatting.BOLD), rx, ry, COL_GOLD);
        ry += 13;
        ry = kv(g, "Citizenship:", data.citizenship(), rx, ry, COL_TEXT);
        ry = kv(g, "Civic Rank:", data.rank(), rx, ry, 0xFF8AE0FF);
        ry = kv(g, "Coins:", data.coins() + "", rx, ry, COL_GOLD);
        ry = kv(g, "Tax Owed:", data.taxOwed() + " coins", rx, ry, data.taxOwed() > 0 ? 0xFFE06A6A : COL_TEXT_DIM);
        ry += 4;
        label(g, "Active Perks:", rx, ry, COL_TEXT_DIM);
        ry += 11;
        String perks = data.perks() == null || data.perks().isBlank() ? "None" : data.perks();
        for (String line : wrap(perks, PANEL_W / 2 - 26)) {
            label(g, line, rx, ry, 0xFFB6C2D4);
            ry += 10;
        }
    }

    private java.util.List<String> wrap(String text, int maxWidth) {
        java.util.List<String> out = new java.util.ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split("\\s+")) {
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
