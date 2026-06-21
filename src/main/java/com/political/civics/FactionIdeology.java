package com.political.civics;

import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.Holder;

/**
 * The platform a faction runs on. Each ideology confers a small standing perk to
 * online members (a vanilla status effect), giving parties mechanical identity.
 */
public enum FactionIdeology {
    LIBERAL("Liberal", ChatFormatting.YELLOW, MobEffects.HASTE,
            "Free enterprise — members work faster (Haste)."),
    CONSERVATIVE("Conservative", ChatFormatting.DARK_RED, MobEffects.RESISTANCE,
            "Law & order — members are hardier (Resistance)."),
    SOCIALIST("Socialist", ChatFormatting.RED, MobEffects.REGENERATION,
            "Mutual aid — members slowly recover (Regeneration)."),
    NATIONALIST("Nationalist", ChatFormatting.GOLD, MobEffects.STRENGTH,
            "Strength through unity — members hit harder (Strength)."),
    TECHNOCRAT("Technocrat", ChatFormatting.AQUA, MobEffects.NIGHT_VISION,
            "Knowledge is power — members see in the dark (Night Vision)."),
    AGRARIAN("Agrarian", ChatFormatting.GREEN, MobEffects.SATURATION,
            "Salt of the earth — members stay well-fed (Saturation)."),
    CENTRIST("Centrist", ChatFormatting.GRAY, MobEffects.LUCK,
            "Pragmatic middle — members enjoy a touch of Luck.");

    public final String displayName;
    public final ChatFormatting color;
    public final Holder<MobEffect> effect;
    public final String description;

    FactionIdeology(String displayName, ChatFormatting color, Holder<MobEffect> effect, String description) {
        this.displayName = displayName;
        this.color = color;
        this.effect = effect;
        this.description = description;
    }

    public static FactionIdeology byId(String id) {
        if (id == null) return CENTRIST;
        for (FactionIdeology i : values()) {
            if (i.name().equalsIgnoreCase(id) || i.displayName.equalsIgnoreCase(id)) return i;
        }
        return CENTRIST;
    }
}
