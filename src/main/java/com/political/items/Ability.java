package com.political.items;

import net.minecraft.ChatFormatting;

/**
 * Gear abilities carried on items via {@code custom_data} (key {@code rpg_abilities},
 * a comma-separated list of {@link #name()} values). The {@link com.political.combat.AbilityEngine}
 * reads these on attack, block-break, item-use, and on a periodic equipment tick.
 *
 * <p>Every ability is implemented through Fabric events + vanilla mechanics (no mixins),
 * which keeps the feature robust on the 26.2 client/server pipeline.
 */
public enum Ability {
    // --- Weapon / combat (on-attack) ---
    LIFESTEAL("Lifesteal", "Heal for a portion of the damage you deal.", ChatFormatting.DARK_RED),
    IGNITE("Ignite", "Set struck enemies ablaze.", ChatFormatting.GOLD),
    THUNDER_STRIKE("Thunder Strike", "Chance to call lightning on your target.", ChatFormatting.YELLOW),
    EXECUTE("Execute", "Bonus damage against low-health foes.", ChatFormatting.RED),
    CRIT_STRIKE("Critical Strike", "Chance to deal heavy critical damage.", ChatFormatting.LIGHT_PURPLE),
    POISON("Venom", "Poison the enemies you hit.", ChatFormatting.DARK_GREEN),
    FROST("Frostbite", "Slow and chill enemies on hit.", ChatFormatting.AQUA),
    KNOCKBACK("Concussive", "Launch enemies back with each blow.", ChatFormatting.WHITE),
    WITHER_TOUCH("Wither Touch", "Inflict wither on your target.", ChatFormatting.DARK_GRAY),

    // --- Armor / passive (equipment tick) ---
    FLIGHT("Creative Flight", "Soar freely while worn.", ChatFormatting.AQUA),
    FIRE_AURA("Fire Aura", "Burn nearby hostile mobs.", ChatFormatting.GOLD),
    NIGHT_VISION("Night Vision", "See clearly in the dark.", ChatFormatting.BLUE),
    WATER_BREATHING("Aquatic", "Breathe underwater.", ChatFormatting.AQUA),
    SPEED("Swiftness", "Move faster.", ChatFormatting.WHITE),
    HASTE("Haste", "Mine and attack faster.", ChatFormatting.GOLD),
    REGEN("Regeneration", "Slowly recover health.", ChatFormatting.LIGHT_PURPLE),
    ABSORPTION("Battle Wards", "Gain bonus absorption hearts.", ChatFormatting.YELLOW),
    FALL_IMMUNE("Featherfall", "Immune to fall damage.", ChatFormatting.WHITE),
    FIRE_IMMUNE("Fire Immunity", "Immune to fire and lava.", ChatFormatting.GOLD),
    RESISTANCE("Fortified", "Take reduced damage from all sources.", ChatFormatting.GRAY),

    // --- Tools (block-break) ---
    INSTANT_MINE("Instant Break", "Mine blocks almost instantly.", ChatFormatting.GOLD),
    VEIN_MINE("Vein Miner", "Break a whole ore vein at once.", ChatFormatting.GREEN),
    TUNNEL_3X3("Excavator", "Break a 3x3 tunnel as you dig.", ChatFormatting.GREEN),
    AUTO_SMELT("Auto-Smelt", "Smelt ores as you mine them.", ChatFormatting.GOLD),
    TREE_FELLER("Tree Feller", "Fell an entire tree in one chop.", ChatFormatting.DARK_GREEN),
    FORTUNE_TOUCH("Prospector", "Mined ores yield extra drops.", ChatFormatting.GREEN),

    // --- Economy / progression (flags read elsewhere) ---
    COIN_BOOST("Midas Touch", "Earn more coins from rewards.", ChatFormatting.YELLOW),
    XP_BOOST("Scholar", "Gain more bounty XP.", ChatFormatting.GREEN),
    EXPLOSIVE("Detonation", "Chance to explode on impact.", ChatFormatting.RED);

    public final String displayName;
    public final String description;
    public final ChatFormatting color;

    Ability(String displayName, String description, ChatFormatting color) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
    }

    public static Ability byId(String id) {
        try {
            return valueOf(id.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
