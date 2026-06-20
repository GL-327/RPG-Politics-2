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

    // ---- Skyblock-style combat stats (summed from gear) ----
    /** Chance (percent) for a hit to critically strike. */
    public double critChance = 0.0;
    /** Extra damage (percent) dealt on a critical strike. Base 50%. */
    public double critDamage = 50.0;
    /** Chance (percent) for bonus strikes; 100 = one guaranteed extra hit. */
    public double ferocity = 0.0;
    /** Speed stat points (each adds a small movement-speed bonus). */
    public double speed = 0.0;
    /** Attack-speed stat points. */
    public double attackSpeed = 0.0;
}
