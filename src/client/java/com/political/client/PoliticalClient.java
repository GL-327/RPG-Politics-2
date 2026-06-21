package com.political.client;

import com.political.net.ActivatePowerC2S;
import com.political.net.BankMenuS2C;
import com.political.net.DevMenuOpenS2C;
import com.political.net.DialogueOpenS2C;
import com.political.net.GovMenuS2C;
import com.political.net.PowerActionC2S;
import com.political.net.PowerMenuS2C;
import com.political.net.StatSyncS2C;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Client entrypoint for 26.2. Receives synced RPG stats, hides the vanilla hearts, and draws a
 * compact, modern HUD above the hotbar: sleek Health / Mana / Cursed-Energy bars plus a tidy
 * stat-chip strip (Defense, Strength, Crit, Ferocity, Speed). No mixins, no textures.
 */
public class PoliticalClient implements ClientModInitializer {

    private static KeyMapping activatePowerKey;
    private static KeyMapping openPowersKey;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(StatSyncS2C.TYPE, (payload, context) -> {
            ClientRpgState.defense = payload.defense();
            ClientRpgState.strength = payload.strength();
            ClientRpgState.maxMana = payload.maxMana();
            ClientRpgState.mana = payload.mana();
            ClientRpgState.maxCursedEnergy = payload.maxCursedEnergy();
            ClientRpgState.cursedEnergy = payload.cursedEnergy();
            ClientRpgState.critChance = payload.critChance();
            ClientRpgState.ferocity = payload.ferocity();
            ClientRpgState.speed = payload.speed();
        });

        ClientPlayNetworking.registerGlobalReceiver(DialogueOpenS2C.TYPE, (payload, context) -> {
            DialogueOpenS2C dialogue = (DialogueOpenS2C) payload;
            context.client().execute(() ->
                    context.client().setScreenAndShow(new VillagerDialogueScreen(dialogue)));
        });

        ClientPlayNetworking.registerGlobalReceiver(DevMenuOpenS2C.TYPE, (payload, context) -> {
            context.client().execute(() ->
                    context.client().setScreenAndShow(new DevMenuScreen(payload.values())));
        });

        ClientPlayNetworking.registerGlobalReceiver(PowerMenuS2C.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (com.political.expansion2.powers.PowersScreen2.OPEN != null) {
                    com.political.expansion2.powers.PowersScreen2.OPEN.apply(payload);
                } else {
                    context.client().setScreenAndShow(new com.political.expansion2.powers.PowersScreen2(payload));
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(BankMenuS2C.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (BankScreen.OPEN != null) BankScreen.OPEN.apply(payload);
                else context.client().setScreenAndShow(new BankScreen(payload));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(GovMenuS2C.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (GovScreen.OPEN != null) GovScreen.OPEN.apply(payload);
                else context.client().setScreenAndShow(new GovScreen(payload));
            });
        });

        HudElementRegistry.removeElement(VanillaHudElements.HEALTH_BAR);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("politicalserver", "rpg_hud"),
                PoliticalClient::renderHud);

        ModelLayerRegistry.registerModelLayer(CurseModels.CURSE_LAYER, CurseModels::createBodyLayer);
        EntityRendererRegistry.register(com.political.curse.ModEntities.CURSE_SPIRIT, CurseRenderer::new);
        com.political.curse.spirits.SpiritClient.registerClient();
        com.political.expansion2.curses.Spirit2Client.registerClient();
        com.political.expansion.mobs.ExpansionMobsClient.registerClient();
        com.political.expansion2.mobs.ExpansionMobs2Client.registerClient();
        com.political.expansion2.powers.PowersScreen2.registerClient();
        com.political.flight.FlightClient.registerClient();
        com.political.guide.GuideClient.registerClient();
        com.political.vfx.client.VfxClientBootstrap.initClient();
        com.political.content.client.ContentClientBootstrap.initClient();
        com.political.client.JjkClientBootstrap.initClient();

        ItemTooltips.register();

        activatePowerKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.politicalserver.activate_power",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KeyMapping.Category.MISC));

        openPowersKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.politicalserver.open_powers",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (activatePowerKey.consumeClick()) {
                if (client.player != null) {
                    ClientPlayNetworking.send(new ActivatePowerC2S());
                }
            }
            while (openPowersKey.consumeClick()) {
                if (client.player != null) {
                    ClientPlayNetworking.send(new PowerActionC2S("open", ""));
                }
            }
        });
    }

    private static final int BAR_W = 100;
    private static final int BAR_H = 7;

    private static void renderHud(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        net.minecraft.client.gui.Font font = mc.font;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        int center = sw / 2;
        int barX = center - BAR_W / 2;

        float hp = mc.player.getHealth();
        float maxHp = Math.max(1f, mc.player.getMaxHealth());
        boolean hasCe = ClientRpgState.maxCursedEnergy > 0f;

        // Stack the bars upward, well above the vanilla armour/hunger rows.
        int manaY = sh - 50;
        int healthY = manaY - (BAR_H + 3);
        int ceY = hasCe ? healthY - (BAR_H + 3) : healthY;
        int chipsY = (hasCe ? ceY : healthY) - 12;

        if (hasCe) {
            float ceFrac = ClientRpgState.cursedEnergy / Math.max(1f, ClientRpgState.maxCursedEnergy);
            drawBar(g, font, barX, ceY, ceFrac, 0xFF15071F, 0xFFCB7BFF, 0xFF7A26C9,
                    "\u2620", 0xFFD9A6FF, (int) ClientRpgState.cursedEnergy + "/" + (int) ClientRpgState.maxCursedEnergy);
        }
        drawBar(g, font, barX, healthY, hp / maxHp, 0xFF2A0A0A, 0xFFFF6B6B, 0xFFB22222,
                "\u2764", 0xFFFF8A8A, (int) Math.ceil(hp) + "/" + (int) maxHp);
        drawBar(g, font, barX, manaY, ClientRpgState.mana / Math.max(1f, ClientRpgState.maxMana),
                0xFF06212F, 0xFF5AD2FF, 0xFF1C7FD0,
                "\u2742", 0xFF9CE6FF, (int) ClientRpgState.mana + "/" + (int) ClientRpgState.maxMana);

        drawStatChips(g, font, center, chipsY);
    }

    /** A sleek labelled bar with gradient fill, glossy top, an icon to the left and a centered value. */
    private static void drawBar(GuiGraphicsExtractor g, net.minecraft.client.gui.Font font,
                                int x, int y, float frac, int track, int fillTop, int fillBot,
                                String icon, int iconColor, String value) {
        frac = Math.max(0f, Math.min(1f, frac));
        g.fill(x - 1, y - 1, x + BAR_W + 1, y + BAR_H + 1, 0xD0000000);
        g.fill(x, y, x + BAR_W, y + BAR_H, track);
        int filled = (int) (BAR_W * frac);
        if (filled > 0) {
            g.fillGradient(x, y, x + filled, y + BAR_H, fillTop, fillBot);
            g.fill(x, y, x + filled, y + 1, 0x44FFFFFF);
        }
        g.text(font, icon, x - 10, y - 1, iconColor, true);
        int vw = font.width(value);
        g.text(font, value, x + (BAR_W - vw) / 2, y - 1, 0xFFFFFFFF, true);
    }

    /** A tidy, centered row of stat chips with a subtle backing pill. */
    private static void drawStatChips(GuiGraphicsExtractor g, net.minecraft.client.gui.Font font, int center, int y) {
        java.util.List<int[]> colors = new java.util.ArrayList<>();
        java.util.List<String> chips = new java.util.ArrayList<>();
        chips.add("\u2748 " + (int) ClientRpgState.defense);   colors.add(new int[]{0xFF7CE0A0});
        chips.add("\u2726 " + (int) ClientRpgState.strength);  colors.add(new int[]{0xFFFFD24A});
        if (ClientRpgState.critChance > 0) { chips.add("\u2741 " + (int) ClientRpgState.critChance + "%"); colors.add(new int[]{0xFF6FB7FF}); }
        if (ClientRpgState.ferocity > 0)   { chips.add("\u2694 " + (int) ClientRpgState.ferocity);          colors.add(new int[]{0xFFFF7A7A}); }
        if (ClientRpgState.speed > 0)      { chips.add("\u27A4 " + (int) ClientRpgState.speed);              colors.add(new int[]{0xFFE6ECF5}); }

        int pad = 6;
        int total = 0;
        for (String c : chips) total += font.width(c) + pad;
        total += pad;
        int x = center - total / 2;
        g.fill(x - 2, y - 2, x + total, y + 10, 0xB0050A12);
        g.fill(x - 2, y - 2, x + total, y - 1, 0xFF5AA9FF);
        int cx = x + pad;
        for (int i = 0; i < chips.size(); i++) {
            g.text(font, chips.get(i), cx, y, colors.get(i)[0], true);
            cx += font.width(chips.get(i)) + pad;
        }
    }
}
