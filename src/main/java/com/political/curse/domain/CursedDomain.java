package com.political.curse.domain;

import com.political.vfx.VfxElement;

/**
 * Immutable definition of an original Domain Expansion (JJK overhaul, Workstream A): a timed, anchored
 * "sure-hit" zone that applies {@link #effect()} to every entity inside on a fixed interval. Names and
 * descriptions are original to this mod.
 *
 * @param id              stable registry id (snake_case)
 * @param displayName     English display name (mirrored as {@code domain.politicalserver.<id>})
 * @param description     short tooltip
 * @param element         {@link VfxElement} theme for the dome + pulse visuals
 * @param ceCost          cursed-energy cost to expand
 * @param requiredGrade   minimum sorcerer grade (1..5)
 * @param durationTicks   how long the domain persists
 * @param radius          sure-hit radius in blocks
 * @param applyInterval   ticks between sure-hit applications
 * @param effect          per-tick sure-hit body
 */
public record CursedDomain(String id, String displayName, String description, VfxElement element,
                           double ceCost, int requiredGrade, int durationTicks, double radius,
                           int applyInterval, DomainEffect effect) {

    public String nameKey() {
        return "domain.politicalserver." + id;
    }

    public String descKey() {
        return "domain.politicalserver." + id + ".desc";
    }
}
