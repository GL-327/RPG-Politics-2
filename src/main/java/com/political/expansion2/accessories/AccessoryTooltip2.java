package com.political.expansion2.accessories;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Skyblock-style lore for Phase-2 accessories and consumables. */
public final class AccessoryTooltip2 {

    private AccessoryTooltip2() {}

    public static Component name(AccessoryDef2 def) {
        return Component.literal(def.displayName).withStyle(def.rarity.color, ChatFormatting.BOLD);
    }

    public static List<Component> lore(AccessoryDef2 def) {
        List<Component> lines = new ArrayList<>();
        AccessoryDef2.Bonus b = def.bonus;

        stat(lines, "Health", b.health, "", ChatFormatting.RED);
        stat(lines, "Defense", b.defense, "", ChatFormatting.GREEN);
        stat(lines, "Strength", b.strength, "", ChatFormatting.RED);
        stat(lines, "Toughness", b.toughness, "", ChatFormatting.GREEN);
        stat(lines, "Knockback Resist", b.knockbackResist * 100, "%", ChatFormatting.GREEN);
        stat(lines, "Speed", b.speed, "", ChatFormatting.WHITE);
        stat(lines, "Attack Speed", b.attackSpeed, "%", ChatFormatting.YELLOW);
        stat(lines, "Crit Chance", b.critChance, "%", ChatFormatting.BLUE);
        stat(lines, "Crit Damage", b.critDamage, "%", ChatFormatting.BLUE);
        stat(lines, "Ferocity", b.ferocity, "", ChatFormatting.RED);
        stat(lines, "Luck", b.luck, "", ChatFormatting.GREEN);
        stat(lines, "Mana Regen", b.manaRegen, "/s", ChatFormatting.AQUA);
        stat(lines, "Cursed Energy", b.cursedRegen, "/s", ChatFormatting.DARK_PURPLE);

        if (!b.effects.isEmpty()) {
            lines.add(Component.empty());
            for (AccessoryDef2.EffectSpec e : b.effects) {
                lines.add(Component.literal("Grants ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(e.label()).withStyle(ChatFormatting.GOLD)));
            }
        }

        lines.add(Component.empty());
        lines.add(Component.literal("Passive: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("works from your inventory").withStyle(ChatFormatting.GREEN)));
        lines.add(Component.literal(def.flavor).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        lines.add(Component.literal(footer(def.rarity.display, def.type.name())).withStyle(def.rarity.color, ChatFormatting.BOLD));
        return lines;
    }

    public static Component name(ConsumableDef2 def) {
        return Component.literal(def.displayName).withStyle(def.rarity.color, ChatFormatting.BOLD);
    }

    public static List<Component> lore(ConsumableDef2 def) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(def.flavor).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        lines.add(Component.empty());
        lines.add(Component.literal("Ability: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("Consume").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append(Component.literal("  RIGHT CLICK").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
        lines.add(Component.literal(footer(def.rarity.display, def.type.name())).withStyle(def.rarity.color, ChatFormatting.BOLD));
        return lines;
    }

    private static void stat(List<Component> lines, String label, double value, String suffix, ChatFormatting color) {
        if (value == 0) return;
        MutableComponent line = Component.literal(label + ": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("+" + fmt(value) + suffix).withStyle(color));
        lines.add(line);
    }

    private static String fmt(double v) {
        return v == (int) v ? String.valueOf((int) v) : String.format(Locale.ROOT, "%.1f", v);
    }

    private static String footer(String rarity, String type) {
        return rarity.toUpperCase(Locale.ROOT) + " " + type.toUpperCase(Locale.ROOT);
    }
}
