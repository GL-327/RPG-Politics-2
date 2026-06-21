package com.political.curse.spirits;

import com.political.client.model.ArchetypeModels;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * Client-side binder for the whole cursed-spirit roster. <b>Integration must call
 * {@link #registerClient()} once from {@code PoliticalClient.onInitializeClient()}</b> (alongside the
 * existing {@code CurseModels}/{@code CurseRenderer} registration). It registers the shared archetype
 * model layers and a {@link SpiritRenderer} for every species' {@code EntityType}.
 */
public final class SpiritClient {

    private SpiritClient() {}

    public static void registerClient() {
        ModSpirits.registerAll(); // ensure types exist if client init runs before server registration

        ArchetypeModels.registerLayers(); // idempotent; shared across all packages

        for (SpiritSpecies species : SpiritSpecies.values()) {
            EntityRendererRegistry.register(ModSpirits.typeFor(species),
                    context -> new SpiritRenderer(context, species));
        }
    }
}
