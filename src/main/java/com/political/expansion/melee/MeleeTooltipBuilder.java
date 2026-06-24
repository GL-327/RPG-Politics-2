package com.political.expansion.melee;

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

/**
 * Hypixel-Skyblock-style tooltip for melee expansion weapons. Deliberately mirrors
 * {@link com.political.items.SkyblockTooltipBuilder} (and reuses its gear-score maths) so the
 * layout matches the rest of the mod, while resolving this expansion's own {@link MeleeAbility}
 * block instead of the shared {@code ItemActiveAbility} enum.
 */
public final class MeleeTooltipBuilder {

    private MeleeTooltipBuilder() {}

    public static List<Component> build(ItemStack stack, MeleeWeapon weapon) {
        List<Component> lines = new ArrayList<>();
        ItemStats.Sheet s = ItemStats.compute(stack);
        Rarity rarity = ItemStats.rarityOf(stack);

        int gs = SkyblockTooltipBuilder.gearScore(s);
        lines.add(StatDisplay.gearScoreLine(gs));

        StatDisplay.line(lines, "Damage", s.damage, "", ChatFormatting.RED);
        StatDisplay.line(lines, "Strength", s.strength, "", ChatFormatting.RED);
        StatDisplay.line(lines, "Defense", s.defense, "", ChatFormatting.GREEN);
        StatDisplay.line(lines, "Health", s.health, "", ChatFormatting.RED);
        StatDisplay.line(lines, "Crit Chance", s.critChance, "%", ChatFormatting.RED);
        StatDisplay.line(lines, "Crit Damage", s.critDamage, "%", ChatFormatting.RED);
        StatDisplay.line(lines, "Ferocity", s.ferocity, "", ChatFormatting.RED);
        StatDisplay.line(lines, "Speed", s.speed, "", ChatFormatting.WHITE);
        StatDisplay.line(lines, "Mana", s.intelligence, "", ChatFormatting.AQUA);

        MeleeAbility ability = weapon == null ? null : weapon.ability;
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
}
