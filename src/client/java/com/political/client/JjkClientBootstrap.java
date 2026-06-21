package com.political.client;

import com.political.curse.domain.CursedDomain;
import com.political.curse.domain.DomainRegistry;
import com.political.curse.technique.CursedTechnique;
import com.political.curse.technique.TechniqueRegistry;
import com.political.net.DomainActionC2S;
import com.political.net.DomainSyncS2C;
import com.political.net.TechniqueActionC2S;
import com.political.net.TechniqueMenuS2C;
import com.political.politics.DataManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Self-contained client bootstrap for the JJK overhaul (Workstream A). Lives in the client source set
 * because it touches client-only types (key mappings, HUD, screens, {@code ClientRpgState}) that the
 * common {@code JjkBootstrap} cannot see.
 *
 * <p>Integration: add {@code com.political.client.JjkClientBootstrap.initClient();} to
 * {@code PoliticalClient#onInitializeClient}.</p>
 */
public final class JjkClientBootstrap {

    private static boolean initialized;

    private static KeyMapping openTechniquesKey;
    private static KeyMapping domainKey;
    private static final KeyMapping[] SLOT_KEYS = new KeyMapping[4];
    private static final int[] SLOT_DEFAULTS = {GLFW.GLFW_KEY_Z, GLFW.GLFW_KEY_X, GLFW.GLFW_KEY_C, GLFW.GLFW_KEY_B};

    private JjkClientBootstrap() {}

    public static void initClient() {
        if (initialized) return;
        initialized = true;

        // Ensure the registries are populated client-side even if common init order differs.
        com.political.curse.technique.CursedTechniques.bootstrap();
        com.political.curse.domain.Domains.bootstrap();

        // Teach the common perception facade how to read the local player's pool client-side.
        com.political.curse.energy.CursedEnergy.installClientProviders(
                viewer -> ClientRpgState.maxCursedEnergy,
                viewer -> ClientRpgState.cursedEnergy);

        TechniqueBindings.load();
        registerReceivers();
        registerKeys();
        registerHud();
        DomainOverlayRenderer.register();
    }

    private static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(com.political.net.CursedEnergySyncS2C.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    CursedClientState.ENERGY.put(payload.entityId(),
                            new float[]{payload.cursedEnergy(), payload.maxCursedEnergy(), payload.grade()});
                    if (context.client().player != null && payload.entityId() == context.client().player.getId()) {
                        CursedClientState.grade = payload.grade();
                        CursedClientState.cursedEnergy = payload.cursedEnergy();
                        CursedClientState.maxCursedEnergy = payload.maxCursedEnergy();
                    }
                }));

        ClientPlayNetworking.registerGlobalReceiver(TechniqueMenuS2C.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    CursedClientState.grade = payload.grade();
                    CursedClientState.cursedEnergy = payload.cursedEnergy();
                    CursedClientState.maxCursedEnergy = payload.maxCursedEnergy();
                    CursedClientState.known = split(payload.known());
                    CursedClientState.domains = split(payload.domains());
                    if (TechniqueScreen.OPEN != null) {
                        TechniqueScreen.OPEN.refresh();
                    } else {
                        context.client().setScreenAndShow(new TechniqueScreen());
                    }
                }));

        ClientPlayNetworking.registerGlobalReceiver(DomainSyncS2C.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    if (payload.domainId() == null || payload.domainId().isBlank()) {
                        DomainClientState.ACTIVE.remove(payload.casterEntityId());
                    } else {
                        DomainClientState.ACTIVE.put(payload.casterEntityId(),
                                new DomainClientState.ActiveDomain(
                                        payload.domainId(),
                                        payload.centerX(),
                                        payload.centerY(),
                                        payload.centerZ(),
                                        payload.radius(),
                                        payload.elementOrdinal(),
                                        payload.ticksLeft()));
                    }
                    if (context.client().player != null
                            && payload.casterEntityId() == context.client().player.getId()) {
                        DomainClientState.localActive = payload.domainId() == null || payload.domainId().isBlank()
                                ? null
                                : DomainClientState.ACTIVE.get(payload.casterEntityId());
                    }
                }));
    }

    private static void registerKeys() {
        openTechniquesKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.politicalserver.open_techniques", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G,
                KeyMapping.Category.MISC));
        domainKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.politicalserver.expand_domain", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V,
                KeyMapping.Category.MISC));
        for (int i = 0; i < 4; i++) {
            SLOT_KEYS[i] = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                    "key.politicalserver.cursed_technique_" + (i + 1), InputConstants.Type.KEYSYM,
                    SLOT_DEFAULTS[i], KeyMapping.Category.MISC));
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            while (openTechniquesKey.consumeClick()) {
                ClientPlayNetworking.send(new TechniqueActionC2S("open", "", 0));
            }
            while (domainKey.consumeClick()) {
                String domainId = TechniqueScreen.selectedDomainId;
                ClientPlayNetworking.send(new DomainActionC2S(domainId == null ? "" : domainId));
            }
            for (int i = 0; i < 4; i++) {
                while (SLOT_KEYS[i].consumeClick()) {
                    String id = CursedClientState.bound[i];
                    if (id != null && !id.isEmpty()) {
                        ClientPlayNetworking.send(new TechniqueActionC2S("cast_slot", id, i + 1));
                    }
                }
            }
        });
    }

    private static void registerHud() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("politicalserver", "jjk_hud"),
                DomainHud::render);
    }

    private static List<String> split(String csv) {
        List<String> out = new ArrayList<>();
        if (csv == null || csv.isBlank()) return out;
        for (String s : csv.split(",")) {
            if (!s.isBlank()) out.add(s.trim());
        }
        return out;
    }
}
