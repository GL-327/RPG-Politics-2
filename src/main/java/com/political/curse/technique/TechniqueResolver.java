package com.political.curse.technique;

/**
 * The server-side gameplay body of a {@link CursedTechnique}. Implementations apply the mechanical
 * effect (damage, status, movement, …) and trigger {@code VfxHelper} visuals.
 *
 * @return {@code true} if the technique fired and should consume cursed energy + start its cooldown;
 *         {@code false} to abort cleanly (e.g. no valid target) without charging the player.
 */
@FunctionalInterface
public interface TechniqueResolver {
    boolean resolve(TechniqueContext ctx);
}
