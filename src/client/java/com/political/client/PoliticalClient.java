package com.political.client;

import com.political.net.StatSyncS2C;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/**
 * Client entrypoint for 26.2. Receives synced RPG stats, hides the vanilla hearts
 * (health is shown Skyblock-style in the action bar) and draws a mana bar.
 */
public class PoliticalClient implements ClientModInitializer {

    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(StatSyncS2C.TYPE, (payload, context) -> {
            ClientRpgState.defense = payload.defense();
            ClientRpgState.strength = payload.strength();
            ClientRpgState.maxMana = payload.maxMana();
            ClientRpgState.mana = payload.mana();
        });

        HudElementRegistry.removeElement(VanillaHudElements.HEALTH_BAR);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("politicalserver", "mana_bar"),
                PoliticalClient::renderManaBar);
    }

    private static void renderManaBar(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        float max = Math.max(1f, ClientRpgState.maxMana);
        float frac = Math.max(0f, Math.min(1f, ClientRpgState.mana / max));

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        int x = (screenW - BAR_WIDTH) / 2;
        int y = screenH - 48;

        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xC0000000);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF002B3D);
        int filled = (int) (BAR_WIDTH * frac);
        graphics.fill(x, y, x + filled, y + BAR_HEIGHT, 0xFF38C6FF);
    }
}
