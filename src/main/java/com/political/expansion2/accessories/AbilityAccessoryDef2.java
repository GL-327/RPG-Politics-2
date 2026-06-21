package com.political.expansion2.accessories;

import com.political.items.Rarity;

/**
 * Definition of a single artifacts-inspired ability accessory ({@code acc2_ab_*}). Pairs an
 * {@link AccessoryAbility2} with a tuning magnitude (radius / strength / interval, ability-specific)
 * and the standard cosmetic metadata used by the tooltip.
 */
public final class AbilityAccessoryDef2 {

    public final String id;
    public final String displayName;
    public final AccessoryDef2.Type type;
    public final Rarity rarity;
    public final String flavor;
    public final AccessoryAbility2 ability;
    /** Ability-specific magnitude (e.g. magnet radius in blocks, step height in blocks). */
    public final double magnitude;
    /** Short human label describing the granted power, shown in the tooltip. */
    public final String powerLabel;

    public AbilityAccessoryDef2(String id, String displayName, AccessoryDef2.Type type, Rarity rarity,
                                String flavor, AccessoryAbility2 ability, double magnitude, String powerLabel) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.rarity = rarity;
        this.flavor = flavor;
        this.ability = ability;
        this.magnitude = magnitude;
        this.powerLabel = powerLabel;
    }
}
