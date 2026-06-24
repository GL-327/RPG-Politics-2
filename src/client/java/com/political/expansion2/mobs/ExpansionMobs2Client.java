package com.political.expansion2.mobs;

import com.political.client.model.Archetype;
import com.political.client.model.ArchetypeMapper;
import com.political.client.model.ArchetypeModels;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.resources.Identifier;

/**
 * Client-side registrar for the phase-2 creature set. Integration must call {@link #registerClient}
 * from the client initializer (already wired in {@code PoliticalClient}). Each spec is mapped to its
 * best-fit shared {@link Archetype} from its name/brute/role; bosses + mini-bosses also get a glow
 * overlay.
 */
public final class ExpansionMobs2Client {

    private ExpansionMobs2Client() {}

    public static void registerClient() {
        ArchetypeModels.registerLayers(); // idempotent; shared across all packages

        for (MobSpec2 spec : ExpansionMobs2.SPECS) {
            if (spec.type == null) continue;
            boolean isBoss = spec.role == MobRole2.BOSS;
            boolean isMiniBoss = spec.role == MobRole2.MINIBOSS;
            Archetype archetype = ArchetypeMapper.forCreature(spec.name, spec.brute, isBoss, isMiniBoss);
            Identifier texture = Identifier.fromNamespaceAndPath(
                    ExpansionMobs2.MOD_ID, "textures/entity/" + spec.id + ".png");
            com.political.client.tex.ProceduralTextures.register(texture, archetype, spec.id);
            boolean glow = isBoss || isMiniBoss;
            EntityRendererRegistry.register(spec.type,
                    context -> new ExpansionMob2Renderer(context, archetype, texture, glow));
        }
    }
}
