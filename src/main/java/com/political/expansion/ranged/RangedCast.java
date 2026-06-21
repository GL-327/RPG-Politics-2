package com.political.expansion.ranged;

import net.minecraft.ChatFormatting;

/**
 * Self-contained RIGHT CLICK spells for the ranged &amp; magic expansion. These mirror the
 * design of {@code com.political.items.ItemActiveAbility} but live entirely inside this
 * package so the shared enum is never touched. Each cast carries its display text, a
 * base Mana cost, a cooldown, and base power/range values; {@link RangedAbilityEngine}
 * resolves the actual behaviour and scales damage with the caster's Intelligence/Mana.
 */
public enum RangedCast {
    FIREBOLT("Fire Bolt", "Hurl a searing bolt that ignites and scorches the first foe in your sights.",
            30, 4, 13f, 26, ChatFormatting.GOLD),
    FLAME_WAVE("Flame Wave", "Sweep a roaring wall of fire across a wide frontal cone.",
            45, 8, 11f, 11, ChatFormatting.GOLD),
    METEOR("Meteor Strike", "Call down a meteor at your aim point for massive fiery splash damage.",
            70, 14, 22f, 22, ChatFormatting.RED),
    FROST_VOLLEY("Frost Volley", "Loose a frozen volley that chills, slows, and shatters foes ahead.",
            40, 7, 12f, 18, ChatFormatting.AQUA),
    BLIZZARD("Blizzard", "Conjure a swirling blizzard around your target zone, freezing all within.",
            65, 12, 14f, 9, ChatFormatting.AQUA),
    CHAIN_LIGHTNING("Chain Lightning", "Strike your target with lightning that arcs to nearby enemies.",
            55, 10, 15f, 24, ChatFormatting.YELLOW),
    STORM_FIELD("Storm Field", "Saturate an area with cascading lightning strikes.",
            80, 16, 16f, 20, ChatFormatting.YELLOW),
    SOUL_BEAM("Soul Beam", "Channel a draining beam of souls, healing you for part of the damage.",
            50, 9, 13f, 24, ChatFormatting.DARK_AQUA),
    VOID_LANCE("Void Lance", "Pierce reality with a void lance that rends everything in a long line.",
            85, 16, 26f, 30, ChatFormatting.DARK_PURPLE),
    WIND_SHEAR("Wind Shear", "Blast a gust that hurls back and staggers nearby enemies.",
            25, 5, 7f, 12, ChatFormatting.WHITE),
    ARROW_VOLLEY("Arrow Volley", "Rain a fan of phantom arrows across your forward arc.",
            35, 6, 10f, 22, ChatFormatting.GREEN),
    EXPLOSIVE_SHOT("Explosive Shot", "Fire a charge that detonates on impact, blasting foes apart.",
            50, 10, 18f, 24, ChatFormatting.RED),
    POISON_BARRAGE("Poison Barrage", "Spray a venomous barrage that poisons and withers a cone of foes.",
            45, 8, 9f, 16, ChatFormatting.DARK_GREEN),
    KNIFE_FAN("Knife Fan", "Flick a fan of blades that shred and bleed close-range targets.",
            20, 4, 8f, 9, ChatFormatting.GRAY),
    STAR_STORM("Star Storm", "Whirl a storm of razor stars, slashing everything around you.",
            40, 7, 9f, 9, ChatFormatting.WHITE),
    JAVELIN_PIERCE("Javelin Pierce", "Impale a single target on a thrown lightning javelin.",
            45, 8, 20f, 20, ChatFormatting.AQUA),
    RICOCHET("Ricochet", "Throw a chakram that ricochets between every nearby enemy.",
            40, 7, 12f, 14, ChatFormatting.WHITE),
    MAGIC_MISSILE("Magic Missile", "Snap an unerring arcane missile into your target.",
            35, 5, 16f, 28, ChatFormatting.LIGHT_PURPLE),
    ARCANE_ORB("Arcane Orb", "Detonate raw arcane energy in a ring around you.",
            45, 8, 12f, 9, ChatFormatting.LIGHT_PURPLE),
    SHADOW_BOLT("Shadow Bolt", "Lance a beam of shadow that withers and blinds your foes.",
            50, 9, 14f, 26, ChatFormatting.DARK_GRAY),
    DECAY_FIELD("Decay Field", "Rot the world around you, withering and weakening every nearby creature.",
            70, 13, 13f, 10, ChatFormatting.DARK_GRAY),
    HEAL_BEAM("Renewal Beam", "Project a beam of life, healing yourself and allies before you.",
            40, 7, 0f, 16, ChatFormatting.GREEN),
    HOLY_NOVA("Holy Nova", "Erupt with holy light: smiting enemies and mending nearby allies.",
            60, 11, 12f, 11, ChatFormatting.YELLOW);

    public final String displayName;
    public final String description;
    /** Base Mana cost before any cost modifiers. */
    public final int manaCost;
    public final int cooldownSeconds;
    /** Base power used by {@link RangedAbilityEngine} (damage, heal, etc.). */
    public final float power;
    /** Effective range / radius in blocks. */
    public final int range;
    public final ChatFormatting color;

    RangedCast(String displayName, String description, int manaCost, int cooldownSeconds,
               float power, int range, ChatFormatting color) {
        this.displayName = displayName;
        this.description = description;
        this.manaCost = manaCost;
        this.cooldownSeconds = cooldownSeconds;
        this.power = power;
        this.range = range;
        this.color = color;
    }
}
