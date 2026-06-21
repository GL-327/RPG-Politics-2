package com.political.expansion2.melee;

import com.political.items.ItemStats;
import com.political.items.Rarity;
import com.political.items.SkyblockTooltipBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Skyblock-style tooltip builder for Phase-2 melee weapons. */
public final class Melee2TooltipBuilder {

    private Melee2TooltipBuilder() {}

    public static List<Component> build(ItemStack stack, Melee2Weapon weapon) {
        List<Component> lines = new ArrayList<>();
        ItemStats.Sheet s = ItemStats.compute(stack);
        Rarity rarity = ItemStats.rarityOf(stack);

        int gs = SkyblockTooltipBuilder.gearScore(s);
        int total = SkyblockTooltipBuilder.totalGearScore(s, rarity);
        lines.add(Component.literal("Gear Score: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(gs)).withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(" (" + total + ")").withStyle(ChatFormatting.DARK_GRAY)));

        skyStat(lines, "Damage", s.damage, totalScale(s.damage, rarity), "", ChatFormatting.RED);
        skyStat(lines, "Strength", s.strength, totalScale(s.strength, rarity), "", ChatFormatting.RED);
        if (s.defense > 0) skyStat(lines, "Defense", s.defense, totalScale(s.defense, rarity), "", ChatFormatting.GREEN);
        if (s.health > 0) skyStat(lines, "Health", s.health, totalScale(s.health, rarity), "", ChatFormatting.RED);
        if (s.critChance > 0) skyStat(lines, "Crit Chance", s.critChance, s.critChance, "%", ChatFormatting.RED);
        if (s.critDamage > 0) skyStat(lines, "Crit Damage", s.critDamage, totalScale(s.critDamage, rarity), "%", ChatFormatting.RED);
        if (s.ferocity > 0) skyStat(lines, "Ferocity", s.ferocity, totalScale(s.ferocity, rarity), "", ChatFormatting.RED);
        if (s.speed > 0) skyStat(lines, "Speed", s.speed, totalScale(s.speed, rarity), "", ChatFormatting.WHITE);
        if (s.intelligence > 0) skyStat(lines, "Mana", s.intelligence, totalScale(s.intelligence, rarity), "", ChatFormatting.AQUA);

        Melee2Ability ability = weapon == null ? null : weapon.ability;
        if (ability != null) {
            lines.add(Component.empty());
            lines.add(Component.literal("Ability: ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(ability.displayName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .append(Component.literal("  RIGHT CLICK").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
            lines.add(Component.literal(ability.description).withStyle(ChatFormatting.GRAY));
            if (ability.manaCost > 0) {
                lines.add(Component.literal("Mana Cost: ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(String.valueOf(ability.manaCost)).withStyle(ChatFormatting.AQUA)));
            }
            lines.add(Component.literal("Cooldown: ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(ability.cooldownSeconds + "s").withStyle(ChatFormatting.GREEN)));
        }

        lines.add(Component.empty());
        lines.add(Component.literal("This item can be reforged!").withStyle(ChatFormatting.DARK_GRAY));
        String type = weapon == null ? "WEAPON" : weapon.archetype.toUpperCase(Locale.ROOT);
        lines.add(Component.literal(rarity.display.toUpperCase(Locale.ROOT) + " " + type)
                .withStyle(rarity.color, ChatFormatting.BOLD));
        return lines;
    }

    private static double totalScale(double base, Rarity rarity) {
        return base * rarity.mult * 1.8;
    }

    private static void skyStat(List<Component> lines, String label, double base, double total,
                                String suffix, ChatFormatting color) {
        if (base == 0) return;
        MutableComponent line = Component.literal(label + ": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("+" + fmt(base) + suffix).withStyle(color));
        if (total > base + 0.01) {
            line.append(Component.literal(" (+" + fmt(total) + suffix + ")").withStyle(ChatFormatting.DARK_GRAY));
        }
        lines.add(line);
    }

    private static String fmt(double v) {
        return v == (int) v ? String.valueOf((int) v) : String.format(Locale.ROOT, "%.2f", v);
    }
}
