package com.political.curse;

import net.minecraft.ChatFormatting;

import java.util.Random;

/**
 * A player's innate aptitude for cursed energy, rolled once on first join.
 *
 * <p>Most people barely have any (LOW is by far the most common), and must replenish
 * it by consuming cursed objects. A rare few are born under a Heavenly Restriction:
 * they have <b>no</b> cursed energy at all, but their bodies are pushed far beyond
 * human limits and they can wield cursed tools freely. Those with only a sliver of
 * cursed energy can still channel it into cursed tools.
 */
public enum CursedTrait {
    HEAVENLY_RESTRICTION("Heavenly Restriction", ChatFormatting.GOLD, 0, 0.0, 0.04,
            14.0, 4.0, 0.20, 0.30),
    MINIMAL("Sliver of Cursed Energy", ChatFormatting.GRAY, 25, 0.5, 0.18,
            4.0, 1.0, 0.06, 0.10),
    LOW("Faint Cursed Energy", ChatFormatting.WHITE, 60, 1.0, 0.55,
            0.0, 0.0, 0.0, 0.0),
    MODERATE("Steady Cursed Energy", ChatFormatting.AQUA, 130, 1.3, 0.13,
            0.0, 0.0, 0.0, 0.0),
    HIGH("Vast Cursed Energy", ChatFormatting.DARK_PURPLE, 240, 1.6, 0.06,
            0.0, 0.0, 0.0, 0.0),
    SIX_EYES("Six Eyes", ChatFormatting.LIGHT_PURPLE, 420, 3.5, 0.02,
            0.0, 0.0, 0.0, 0.0);

    private static final Random RNG = new Random();

    public final String display;
    public final ChatFormatting color;
    public final double maxCursedEnergy;
    public final double regenMultiplier;
    public final double weight;
    /** Bonus max health (raw points), bonus strength stat, bonus speed %, bonus attack-speed %. */
    public final double bonusHealth;
    public final double bonusStrength;
    public final double bonusSpeedPct;
    public final double bonusAttackSpeedPct;

    CursedTrait(String display, ChatFormatting color, double maxCursedEnergy, double regenMultiplier, double weight,
                double bonusHealth, double bonusStrength, double bonusSpeedPct, double bonusAttackSpeedPct) {
        this.display = display;
        this.color = color;
        this.maxCursedEnergy = maxCursedEnergy;
        this.regenMultiplier = regenMultiplier;
        this.weight = weight;
        this.bonusHealth = bonusHealth;
        this.bonusStrength = bonusStrength;
        this.bonusSpeedPct = bonusSpeedPct;
        this.bonusAttackSpeedPct = bonusAttackSpeedPct;
    }

    /** Can this player learn and channel cursed techniques? */
    public boolean canUseTechniques() {
        return maxCursedEnergy > 0;
    }

    /** Heavenly Restriction and the Maki-like sliver excel with cursed tools. */
    public boolean isToolSpecialist() {
        return this == HEAVENLY_RESTRICTION || this == MINIMAL;
    }

    public static CursedTrait byId(String id) {
        if (id == null) return LOW;
        try {
            return valueOf(id.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return LOW;
        }
    }

    /** Weighted random trait for a newly-seen player. */
    public static CursedTrait roll() {
        double total = 0;
        for (CursedTrait t : values()) total += t.weight;
        double r = RNG.nextDouble() * total;
        for (CursedTrait t : values()) {
            r -= t.weight;
            if (r <= 0) return t;
        }
        return LOW;
    }
}
