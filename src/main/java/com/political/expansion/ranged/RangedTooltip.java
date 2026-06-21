package com.political.expansion.ranged;

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
 * Hypixel-Skyblock-style tooltip for ranged &amp; magic weapons. Reuses the shared
 * {@link ItemStats} stat computation and {@link SkyblockTooltipBuilder} gear-score helpers,
 * then renders this expansion's own {@link RangedCast} ability block (the shared tooltip
 * builder can't see our casts because they aren't in the shared ability enum).
 */
public final class RangedTooltip {

    private RangedTooltip() {}

    public static List<Component> build(ItemStack stack, RangedWeapon def) {
        List<Component> lines = new ArrayList<>();
        ItemStats.Sheet s = ItemStats.compute(stack);
        Rarity rarity = def.rarity;

        int gs = SkyblockTooltipBuilder.gearScore(s);
        int total = SkyblockTooltipBuilder.totalGearScore(s, rarity);
        lines.add(Component.literal("Gear Score: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(gs)).withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(" (" + total + ")").withStyle(ChatFormatting.DARK_GRAY)));

        stat(lines, "Damage", s.damage, scale(s.damage, rarity), "", ChatFormatting.RED);
        stat(lines, "Strength", s.strength, scale(s.strength, rarity), "", ChatFormatting.RED);
        if (s.critChance > 0) stat(lines, "Crit Chance", s.critChance, s.critChance, "%", ChatFormatting.RED);
        if (s.critDamage > 0) stat(lines, "Crit Damage", s.critDamage, scale(s.critDamage, rarity), "%", ChatFormatting.RED);
        if (s.ferocity > 0) stat(lines, "Ferocity", s.ferocity, scale(s.ferocity, rarity), "", ChatFormatting.RED);
        if (s.intelligence > 0) stat(lines, "Intelligence", s.intelligence, scale(s.intelligence, rarity), "", ChatFormatting.AQUA);

        RangedCast cast = def.cast;
        lines.add(Component.empty());
        lines.add(Component.literal("Ability: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(cast.displayName).withStyle(cast.color, ChatFormatting.BOLD))
                .append(Component.literal("  RIGHT CLICK").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
        lines.add(Component.literal(cast.description).withStyle(ChatFormatting.GRAY));
        if (cast.manaCost > 0) {
            lines.add(Component.literal("Mana Cost: ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(String.valueOf(cast.manaCost)).withStyle(ChatFormatting.AQUA)));
        }
        lines.add(Component.literal("Cooldown: ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(cast.cooldownSeconds + "s").withStyle(ChatFormatting.GREEN)));

        lines.add(Component.empty());
        lines.add(Component.literal("Spell power scales with Intelligence.").withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.literal("This item can be reforged!").withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.literal(rarity.display.toUpperCase(Locale.ROOT) + " " + def.category.footer)
                .withStyle(rarity.color, ChatFormatting.BOLD));
        return lines;
    }

    private static double scale(double base, Rarity rarity) {
        return base * rarity.mult * 1.8;
    }

    private static void stat(List<Component> lines, String label, double base, double total,
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
