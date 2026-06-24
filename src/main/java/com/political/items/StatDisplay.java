package com.political.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.Locale;

/** Shared Skyblock stat formatting: clean numbers, no grey inflated totals on gear. */
public final class StatDisplay {

    private StatDisplay() {}

    /**
     * Hypixel-style rounding: values 10+ snap to nearest 5; single-digit values stay as-is.
     */
    public static double snap(double value) {
        if (value <= 0) return 0;
        if (value < 10) return value;
        return Math.round(value / 5.0) * 5.0;
    }

    public static void snapSheet(ItemStats.Sheet s) {
        s.health = snap(s.health);
        s.defense = snap(s.defense);
        s.strength = snap(s.strength);
        s.intelligence = snap(s.intelligence);
        s.cursed = snap(s.cursed);
        s.damage = snap(s.damage);
        s.critChance = snap(s.critChance);
        s.critDamage = snap(s.critDamage);
        s.ferocity = snap(s.ferocity);
        s.speed = snap(s.speed);
        s.attackSpeed = snap(s.attackSpeed);
    }

    public static String fmt(double v) {
        double r = snap(v);
        return r == (int) r ? String.valueOf((int) r) : String.format(Locale.ROOT, "%.1f", r);
    }

    /** One stat line — base value only (no grey parenthetical inflated totals). */
    public static void line(List<Component> lines, String label, double value, String suffix, ChatFormatting color) {
        if (value <= 0) return;
        lines.add(Component.literal(label + ": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("+" + fmt(value) + suffix).withStyle(color)));
    }

    public static MutableComponent gearScoreLine(int score) {
        return Component.literal("Gear Score: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(score)).withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
