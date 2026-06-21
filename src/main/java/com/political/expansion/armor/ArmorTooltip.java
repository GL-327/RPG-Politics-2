package com.political.expansion.armor;

import com.political.items.Ability;
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

/**
 * Rich Hypixel-Skyblock-style tooltip for armour pieces: gear score, coloured stats, per-piece
 * passive abilities, and a highlighted <b>Full Set Bonus</b> block (name + description). Mirrors the
 * layout of {@link SkyblockTooltipBuilder} so armour reads consistently with the rest of the gear.
 */
public final class ArmorTooltip {

    private ArmorTooltip() {}

    public static List<Component> build(ItemStack stack, ArmorSet set, ArmorSet.Piece piece) {
        List<Component> lines = new ArrayList<>();
        ItemStats.Sheet s = ItemStats.compute(stack);
        Rarity rarity = ItemStats.rarityOf(stack);

        int gs = SkyblockTooltipBuilder.gearScore(s);
        int total = SkyblockTooltipBuilder.totalGearScore(s, rarity);
        lines.add(Component.literal("Gear Score: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(gs)).withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(" (" + total + ")").withStyle(ChatFormatting.DARK_GRAY)));

        skyStat(lines, "Strength", s.strength, scale(s.strength, rarity), "", ChatFormatting.RED);
        skyStat(lines, "Defense", s.defense, scale(s.defense, rarity), "", ChatFormatting.GREEN);
        skyStat(lines, "Health", s.health, scale(s.health, rarity), "", ChatFormatting.RED);
        skyStat(lines, "Crit Chance", s.critChance, s.critChance, "%", ChatFormatting.RED);
        skyStat(lines, "Crit Damage", s.critDamage, scale(s.critDamage, rarity), "%", ChatFormatting.RED);
        skyStat(lines, "Ferocity", s.ferocity, scale(s.ferocity, rarity), "", ChatFormatting.RED);
        skyStat(lines, "Speed", s.speed, scale(s.speed, rarity), "", ChatFormatting.WHITE);
        skyStat(lines, "Mana", s.intelligence, scale(s.intelligence, rarity), "", ChatFormatting.AQUA);

        if (piece.abilities.length > 0) {
            lines.add(Component.empty());
            for (Ability a : piece.abilities) {
                lines.add(Component.literal("Ability: ").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(a.displayName).withStyle(a.color, ChatFormatting.BOLD)));
                lines.add(Component.literal(a.description).withStyle(ChatFormatting.GRAY));
            }
        }

        lines.add(Component.empty());
        lines.add(Component.literal("Full Set Bonus: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(set.bonusName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
        for (String line : set.bonusLines) {
            lines.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
        }
        lines.add(Component.literal("Wear all 4 " + set.displayName + " pieces to activate.")
                .withStyle(ChatFormatting.DARK_GRAY));

        lines.add(Component.empty());
        lines.add(Component.literal("This item can be reforged!").withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.literal(rarity.display.toUpperCase(Locale.ROOT) + " ARMOR")
                .withStyle(rarity.color, ChatFormatting.BOLD));
        return lines;
    }

    private static double scale(double base, Rarity rarity) {
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
