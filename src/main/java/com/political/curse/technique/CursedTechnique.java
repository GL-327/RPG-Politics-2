package com.political.curse.technique;

import com.political.vfx.VfxElement;

/**
 * Immutable definition of an original cursed technique (JJK overhaul, Workstream A).
 *
 * <p>All technique names/descriptions here are original to RPG&nbsp;Politics&nbsp;2 — they port the
 * <i>mechanics</i> of the cursed-technique genre, not any trademarked names or assets.</p>
 *
 * @param id            stable registry id (snake_case)
 * @param displayName   English display name (also mirrored as a lang key {@code technique.politicalserver.<id>})
 * @param description   short tooltip describing the effect
 * @param element       {@link VfxElement} theme used for all of this technique's visuals
 * @param ceCost        cursed-energy cost to cast
 * @param cooldownTicks per-player cooldown in ticks
 * @param requiredGrade minimum sorcerer grade (1..5) needed to unlock it
 * @param resolver      server-side gameplay body
 */
public record CursedTechnique(String id, String displayName, String description, VfxElement element,
                              double ceCost, int cooldownTicks, int requiredGrade, TechniqueResolver resolver) {

    public String nameKey() {
        return "technique.politicalserver." + id;
    }

    public String descKey() {
        return "technique.politicalserver." + id + ".desc";
    }

    public boolean resolve(TechniqueContext ctx) {
        return resolver.resolve(ctx);
    }
}
