package com.political.expansion2.curses;

import com.political.client.model.ArchetypeModels;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/** Client binder for Phase-2 cursed spirits. Call {@link #registerClient()} once at client init. */
public final class Spirit2Client {

    private Spirit2Client() {}

    public static void registerClient() {
        CurseSpirits2.register();

        ArchetypeModels.registerLayers(); // idempotent; shared across all packages

        for (SpiritSpecies2 species : SpiritSpecies2.values()) {
            EntityRendererRegistry.register(CurseSpirits2.typeFor(species),
                    context -> new Spirit2Renderer(context, species));
        }
    }
}
