package com.political.items;

import net.minecraft.ChatFormatting;

/**
 * An optional special variant layered on top of an item's rarity (Minecraft-Dungeons
 * style). A "Unique Common" is a perfectly valid item.
 *
 * <ul>
 *   <li>{@link #NONE} — an ordinary item of its rarity.</li>
 *   <li>{@link #UNIQUE} — boosts the item's base stats and grants a small extra.</li>
 *   <li>{@link #CURSED} — a cursed tool: adds Cursed Energy + Strength scaled by the
 *       grade it was cursed to (stored separately as {@code rpg_cursed_grade}).</li>
 * </ul>
 */
public enum Variant {
    NONE("", null),
    UNIQUE("Unique", ChatFormatting.GOLD),
    CURSED("Cursed", ChatFormatting.DARK_PURPLE);

    public final String display;
    public final ChatFormatting color;

    Variant(String display, ChatFormatting color) {
        this.display = display;
        this.color = color;
    }

    /** Flat multiplier applied to base stats for Unique items. */
    public static final double UNIQUE_MULT = 1.5;

    public static Variant byId(String id) {
        if (id == null) return null;
        try {
            return valueOf(id.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
