package com.political.client;

import com.political.net.BankActionC2S;
import com.political.net.BankMenuS2C;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Clean Bank GUI: balances, interest, and deposit/withdraw controls wired via payloads. */
public class BankScreen extends RpgScreen {

    private static final int PANEL_W = 300;
    private static final int PANEL_H = 214;

    /** The currently open instance, so a server resend refreshes it in place (no Minecraft.screen in 26.2). */
    public static BankScreen OPEN;

    private BankMenuS2C data;
    private int left, top;

    public BankScreen(BankMenuS2C payload) {
        super(Component.literal("Bank"));
        OPEN = this;
        this.data = payload;
    }

    @Override
    public void removed() {
        if (OPEN == this) OPEN = null;
        super.removed();
    }

    public void apply(BankMenuS2C payload) {
        this.data = payload;
    }

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;

        int colL = left + 16;
        int colR = left + PANEL_W - 16 - 124;
        int y = top + 104;
        int[] amts = {100, 1000, 10000};
        String[] lbl = {"100", "1,000", "10,000"};
        for (int i = 0; i < amts.length; i++) {
            final int amt = amts[i];
            addRenderableWidget(Button.builder(Component.literal("Deposit " + lbl[i]),
                            b -> send("deposit", amt))
                    .bounds(colL, y, 124, 18).build());
            addRenderableWidget(Button.builder(Component.literal("Withdraw " + lbl[i]),
                            b -> send("withdraw", amt))
                    .bounds(colR, y, 124, 18).build());
            y += 21;
        }
        addRenderableWidget(Button.builder(Component.literal("Deposit All"), b -> send("depositAll", 0))
                .bounds(colL, y, 124, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Withdraw All"), b -> send("withdrawAll", 0))
                .bounds(colR, y, 124, 18).build());

        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(left + PANEL_W / 2 - 40, top + PANEL_H - 24, 80, 20).build());
    }

    private void send(String action, int amount) {
        ClientPlayNetworking.send(new BankActionC2S(action, amount));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractBackground(g, mouseX, mouseY, pt);
        glassBackdrop(g);
        panel(g, left, top, PANEL_W, PANEL_H);
        headerBar(g, left, top, PANEL_W, 22);
        inset(g, left + 12, top + 28, PANEL_W - 24, 66);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractRenderState(g, mouseX, mouseY, pt);
        centered(g, Component.literal("\u2696 NATIONAL BANK").withStyle(ChatFormatting.BOLD), left + PANEL_W / 2, top + 7, COL_GOLD);

        int x = left + 20;
        int y = top + 34;
        // Big bank balance.
        label(g, "Bank Balance", x, y, COL_TEXT_DIM);
        label(g, Component.literal(format(data.bank()) + " coins").withStyle(ChatFormatting.BOLD), x, y + 11, COL_GOLD);

        int rx = left + PANEL_W / 2 + 6;
        kv(g, "Wallet:", format(data.wallet()) + " coins", rx, y, COL_TEXT);
        kv(g, "Credits:", format(data.credits()), rx, y + 11, 0xFF8AE0FF);
        kv(g, "Net Worth:", format(data.netWorth()) + " coins", rx, y + 22, COL_ACCENT);

        double pct = data.interestTenths() / 10.0;
        long daily = (long) Math.floor(data.bank() * (data.interestTenths() / 1000.0));
        label(g, String.format("Interest: %.1f%% / day  (\u2248 +%s coins next payout)", pct, format(daily)),
                x, y + 50, 0xFF7CE0A0);
    }

    private static String format(long v) {
        return String.format("%,d", v);
    }
}
