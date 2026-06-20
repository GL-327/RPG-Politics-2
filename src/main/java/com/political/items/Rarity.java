package com.political.items;

import net.minecraft.ChatFormatting;

/**
 * The shared item rarity ladder used across the whole mod. Every item — vanilla or
 * custom — resolves to one of these, which scales its inferred base stats and colours
 * its name/lore (Hypixel-Skyblock style).
 */
public enum Rarity {
    COMMON("Common", ChatFormatting.WHITE, 1.00),
    UNCOMMON("Uncommon", ChatFormatting.GREEN, 1.30),
    RARE("Rare", ChatFormatting.BLUE, 1.70),
    EPIC("Epic", ChatFormatting.DARK_PURPLE, 2.20),
    LEGENDARY("Legendary", ChatFormatting.GOLD, 2.90),
    MYTHIC("Mythic", ChatFormatting.LIGHT_PURPLE, 3.80);

    public final String display;
    public final ChatFormatting color;
    /** Multiplier applied to inferred base stats for vanilla items. */
    public final double mult;

    Rarity(String display, ChatFormatting color, double mult) {
        this.display = display;
        this.color = color;
        this.mult = mult;
    }

    public Rarity up() {
        Rarity[] v = values();
        return v[Math.min(v.length - 1, ordinal() + 1)];
    }

    public static Rarity byId(String id) {
        if (id == null) return null;
        try {
            return valueOf(id.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
