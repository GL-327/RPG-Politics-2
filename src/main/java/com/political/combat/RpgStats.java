package com.political.combat;

/** A computed RPG stat sheet for a player. */
public class RpgStats {
    public double maxHealth = StatManager.BASE_MAX_HEALTH;
    public double defense = StatManager.BASE_DEFENSE;
    public double strength = StatManager.BASE_STRENGTH;
    public double maxMana = StatManager.BASE_MAX_MANA;
    /** Maximum Cursed Energy, derived from the player's innate trait + powers + gear. */
    public double maxCursedEnergy = 0.0;
    /** Cursed-energy regeneration multiplier (0 for Heavenly Restriction). */
    public double cursedRegenMultiplier = 1.0;
    /** Physical bonuses (Heavenly Restriction / sliver users), applied as attributes. */
    public double bonusSpeedPct = 0.0;
    public double bonusAttackSpeedPct = 0.0;
}
