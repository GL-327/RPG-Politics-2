package com.political.client;

import com.political.net.ActivatePowerC2S;
import com.political.net.DevMenuOpenS2C;
import com.political.net.StatSyncS2C;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Client entrypoint for 26.2. Receives synced RPG stats, hides the vanilla hearts
 * (health is shown Skyblock-style in the action bar) and draws an Energy bar.
 */
public class PoliticalClient implements ClientModInitializer {

    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;

    private static KeyMapping activatePowerKey;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(StatSyncS2C.TYPE, (payload, context) -> {
            ClientRpgState.defense = payload.defense();
            ClientRpgState.strength = payload.strength();
            ClientRpgState.maxMana = payload.maxMana();
            ClientRpgState.mana = payload.mana();
            ClientRpgState.maxCursedEnergy = payload.maxCursedEnergy();
            ClientRpgState.cursedEnergy = payload.cursedEnergy();
        });

        ClientPlayNetworking.registerGlobalReceiver(DevMenuOpenS2C.TYPE, (payload, context) -> {
            context.client().execute(() ->
                    context.client().setScreenAndShow(new DevMenuScreen(payload.values())));
        });

        HudElementRegistry.removeElement(VanillaHudElements.HEALTH_BAR);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("politicalserver", "energy_bars"),
                PoliticalClient::renderEnergyBars);

        EntityRendererRegistry.register(com.political.curse.ModEntities.CURSE_SPIRIT, CurseRenderer::new);

        activatePowerKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.politicalserver.activate_power",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (activatePowerKey.consumeClick()) {
                if (client.player != null) {
                    ClientPlayNetworking.send(new ActivatePowerC2S());
                }
            }
        });
    }

    private static void renderEnergyBars(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        int x = (screenW - BAR_WIDTH) / 2;
        int y = screenH - 48;

        // Mana bar (aqua).
        float manaFrac = Math.max(0f, Math.min(1f, ClientRpgState.mana / Math.max(1f, ClientRpgState.maxMana)));
        drawBar(graphics, x, y, manaFrac, 0xFF002B3D, 0xFF38C6FF);

        // Cursed Energy bar (purple), drawn just above mana when the player has any.
        if (ClientRpgState.maxCursedEnergy > 0f) {
            float ceFrac = Math.max(0f, Math.min(1f, ClientRpgState.cursedEnergy / ClientRpgState.maxCursedEnergy));
            drawBar(graphics, x, y - (BAR_HEIGHT + 3), ceFrac, 0xFF1A0026, 0xFFB03CFF);
        }
    }

    private static void drawBar(GuiGraphicsExtractor graphics, int x, int y, float frac, int bg, int fg) {
        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xC0000000);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, bg);
        int filled = (int) (BAR_WIDTH * frac);
        graphics.fill(x, y, x + filled, y + BAR_HEIGHT, fg);
    }
}
