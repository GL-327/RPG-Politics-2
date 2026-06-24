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
            ClientRpgState.sorcererGrade = payload.sorcererGrade();
            ClientRpgState.jjkFlags = payload.jjkFlags();
            CursedClientState.grade = payload.sorcererGrade();
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

        ClientPlayNetworking.registerGlobalReceiver(com.political.net.SbsOpenS2C.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (SbsScreen.OPEN != null) SbsScreen.OPEN.apply(payload);
                else context.client().setScreenAndShow(new SbsScreen(payload));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(com.political.net.EchoCodexOpenS2C.TYPE, (payload, context) ->
                context.client().execute(() ->
                        context.client().setScreenAndShow(new com.political.client.echo.EchoLoreScreen(payload.chapter()))));

        HudElementRegistry.removeElement(VanillaHudElements.HEALTH_BAR);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("politicalserver", "rpg_hud"),
                PoliticalClient::renderHud);

        ModelLayerRegistry.registerModelLayer(CurseModels.CURSE_LAYER, CurseModels::createBodyLayer);
        EntityRendererRegistry.register(com.political.curse.ModEntities.CURSE_SPIRIT, CurseRenderer::new);
        com.political.client.tex.ProceduralTextures.register(
                Identifier.fromNamespaceAndPath("politicalserver", "textures/entity/curse_spirit.png"),
                com.political.client.model.Archetype.SPECIAL_GRADE, "curse_spirit");
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
                PoliticalKeyCategories.RPG));

        openPowersKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.politicalserver.open_powers",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                PoliticalKeyCategories.RPG));

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
            // Subtle client-side resonance while in the Black Flash zone (purely visual).
            if (client.player != null && client.level != null
                    && ClientRpgState.jjk(StatSyncS2C.FLAG_BLACK_FLASH_ZONE)
                    && client.level.getGameTime() % 6 == 0) {
                var rng = client.player.getRandom();
                client.level.addParticle(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                        client.player.getX() + (rng.nextDouble() - 0.5) * 0.6,
                        client.player.getY() + 1.0 + rng.nextDouble() * 0.4,
                        client.player.getZ() + (rng.nextDouble() - 0.5) * 0.6,
                        0, 0.02, 0);
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
        boolean bfZone = ClientRpgState.jjk(StatSyncS2C.FLAG_BLACK_FLASH_ZONE);
        boolean bfPrimed = ClientRpgState.jjk(StatSyncS2C.FLAG_BLACK_FLASH_PRIMED);
        float pulse = 0.5f + 0.5f * (float) Math.sin(mc.level.getGameTime() * 0.18);

        int manaY = sh - 50;
        int healthY = manaY - (BAR_H + 3);
        int ceY = hasCe ? healthY - (BAR_H + 3) : healthY;
        int auraY = (hasCe ? ceY : healthY) - 10;
        int chipsY = auraY - 12;

        if (hasCe) {
            float ceFrac = ClientRpgState.cursedEnergy / Math.max(1f, ClientRpgState.maxCursedEnergy);
            int ceTop = bfZone ? HudGlass.lerpColor(0xFFCB7BFF, 0xFFFFFFFF, pulse * 0.35f) : 0xFFCB7BFF;
            int ceBot = bfZone ? HudGlass.lerpColor(0xFF7A26C9, 0xFFE040FB, pulse * 0.25f) : 0xFF7A26C9;
            HudGlass.bar(g, font, barX, ceY, BAR_W, BAR_H, ceFrac, 0xFF15071F, ceTop, ceBot,
                    "\u2620", bfZone ? 0xFFFFFFFF : 0xFFD9A6FF,
                    (int) ClientRpgState.cursedEnergy + "/" + (int) ClientRpgState.maxCursedEnergy);
            if (ClientRpgState.sorcererGrade > 0) {
                String grade = com.political.politics.DataManager.gradeLabel(ClientRpgState.sorcererGrade);
                g.text(font, grade, barX + BAR_W + 4, ceY - 1, 0xFFC065FF, true);
            }
        }
        HudGlass.bar(g, font, barX, healthY, BAR_W, BAR_H, hp / maxHp, 0xFF2A0A0A, 0xFFFF6B6B, 0xFFB22222,
                "\u2764", 0xFFFF8A8A, (int) Math.ceil(hp) + "/" + (int) maxHp);
        HudGlass.bar(g, font, barX, manaY, BAR_W, BAR_H,
                ClientRpgState.mana / Math.max(1f, ClientRpgState.maxMana),
                0xFF06212F, 0xFF5AD2FF, 0xFF1C7FD0,
                "\u2742", 0xFF9CE6FF, (int) ClientRpgState.mana + "/" + (int) ClientRpgState.maxMana);

        drawJjkAuras(g, font, center, auraY, bfZone, bfPrimed, pulse);
        drawStatChips(g, font, center, chipsY);
    }

    /** Live JJK state pills — Black Flash zone, primed window, RCT, flow, domain wards, vows. */
    private static void drawJjkAuras(GuiGraphicsExtractor g, net.minecraft.client.gui.Font font,
                                     int center, int y, boolean bfZone, boolean bfPrimed, float pulse) {
        java.util.List<String> tags = new java.util.ArrayList<>();
        java.util.List<Integer> colors = new java.util.ArrayList<>();
        if (bfZone) { tags.add("\u26a1 ZONE"); colors.add(0xFFE040FB); }
        else if (bfPrimed) { tags.add("\u26a1 PRIMED"); colors.add(0xFFCB7BFF); }
        if (ClientRpgState.jjk(StatSyncS2C.FLAG_RCT)) { tags.add("RCT"); colors.add(0xFF7CE0A0); }
        if (ClientRpgState.jjk(StatSyncS2C.FLAG_FLOW)) { tags.add("FLOW"); colors.add(0xFFC065FF); }
        if (ClientRpgState.jjk(StatSyncS2C.FLAG_SIMPLE_DOMAIN)) { tags.add("SD"); colors.add(0xFF6FB7FF); }
        if (ClientRpgState.jjk(StatSyncS2C.FLAG_FALLING_BLOSSOM)) { tags.add("FB"); colors.add(0xFFFFA857); }
        if (ClientRpgState.jjk(StatSyncS2C.FLAG_BINDING_VOW)) { tags.add("VOW"); colors.add(0xFFFFD24A); }
        if (tags.isEmpty()) return;

        int pad = 4;
        int total = pad;
        for (String t : tags) total += font.width(t) + pad;
        int x = center - total / 2;
        int glow = bfZone ? (int) (0x30 + pulse * 0x20) : 0x28;
        HudGlass.panel(g, x - 2, y - 1, total + 2, 10);
        HudGlass.accentTop(g, x - 2, y - 1, total + 2, bfZone ? 0xFFE040FB : 0xFFC065FF);
        int cx = x + pad;
        for (int i = 0; i < tags.size(); i++) {
            g.text(font, tags.get(i), cx, y, colors.get(i), true);
            cx += font.width(tags.get(i)) + pad;
        }
    }

    /** A sleek labelled bar with gradient fill, glossy top, an icon to the left and a centered value. */
    private static void drawBar(GuiGraphicsExtractor g, net.minecraft.client.gui.Font font,
                                int x, int y, float frac, int track, int fillTop, int fillBot,
                                String icon, int iconColor, String value) {
        HudGlass.bar(g, font, x, y, BAR_W, BAR_H, frac, track, fillTop, fillBot, icon, iconColor, value);
    }

    /** @deprecated use {@link HudGlass#lerpColor} */
    private static int lerpColor(int from, int to, float t) {
        return HudGlass.lerpColor(from, to, t);
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
        HudGlass.panel(g, x - 2, y - 2, total + 2, 12);
        HudGlass.accentTopDefault(g, x - 2, y - 2, total + 2);
        int cx = x + pad;
        for (int i = 0; i < chips.size(); i++) {
            g.text(font, chips.get(i), cx, y, colors.get(i)[0], true);
            cx += font.width(chips.get(i)) + pad;
        }
    }
}
