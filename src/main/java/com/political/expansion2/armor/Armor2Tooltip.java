package com.political.expansion2.armor;

import com.political.items.Ability;
import com.political.items.ItemStats;
import com.political.items.Rarity;
import com.political.items.SkyblockTooltipBuilder;
import com.political.items.StatDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Armor2Tooltip {
    private Armor2Tooltip() {}

    public static List<Component> build(ItemStack stack, ArmorSet set, ArmorSet.Piece piece) {
        List<Component> lines = new ArrayList<>();
        ItemStats.Sheet s = ItemStats.compute(stack);
        Rarity rarity = ItemStats.rarityOf(stack);
        lines.add(StatDisplay.gearScoreLine(SkyblockTooltipBuilder.gearScore(s)));
        StatDisplay.line(lines, "Strength", s.strength, "", ChatFormatting.RED);
        StatDisplay.line(lines, "Defense", s.defense, "", ChatFormatting.GREEN);
        StatDisplay.line(lines, "Health", s.health, "", ChatFormatting.RED);
        StatDisplay.line(lines, "Crit Chance", s.critChance, "%", ChatFormatting.RED);
        StatDisplay.line(lines, "Crit Damage", s.critDamage, "%", ChatFormatting.RED);
        StatDisplay.line(lines, "Ferocity", s.ferocity, "", ChatFormatting.RED);
        StatDisplay.line(lines, "Speed", s.speed, "", ChatFormatting.WHITE);
        StatDisplay.line(lines, "Mana", s.intelligence, "", ChatFormatting.AQUA);
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
        for (String line : set.bonusLines) lines.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("Wear all 4 " + set.displayName + " pieces to activate.").withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.empty());
        lines.add(Component.literal("This item can be reforged!").withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.literal(rarity.display.toUpperCase(Locale.ROOT) + " ARMOR").withStyle(rarity.color, ChatFormatting.BOLD));
        return lines;
    }
}
