package com.political.combat;

/**
 * The computed RPG stat sheet for a player. Health is tracked through the
 * vanilla max-health attribute (so existing combat keeps working); the other
 * fields are RPG layers applied by {@link CombatEngine}.
 */
public class RpgStats {
    public double maxHealth = StatManager.BASE_MAX_HEALTH;
    public double defense = StatManager.BASE_DEFENSE;
    public double strength = StatManager.BASE_STRENGTH;
    public double critChance = StatManager.BASE_CRIT_CHANCE;
    public double critDamage = StatManager.BASE_CRIT_DAMAGE;
    public double maxMana = StatManager.BASE_MAX_MANA;
}
