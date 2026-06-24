package com.political.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Hypixel Skyblock-style item tooltip layout (gear score, colored stats, ability block, footer).
 */
public final class SkyblockTooltipBuilder {

    private SkyblockTooltipBuilder() {}

    public static int gearScore(ItemStats.Sheet s) {
        return (int) Math.round(s.damage * 2 + s.strength * 1.5 + s.defense + s.health * 0.5
                + s.critChance + s.critDamage * 0.3 + s.ferocity + s.intelligence * 0.4 + s.cursed);
    }

    public static int totalGearScore(ItemStats.Sheet s, Rarity rarity) {
        return (int) Math.round(gearScore(s) * rarity.mult * 1.8);
    }

    public static List<Component> build(ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        ItemStats.Sheet s = ItemStats.compute(stack);
        Rarity rarity = ItemStats.rarityOf(stack);
        Variant variant = ItemStats.variantOf(stack);
        ItemStats.Kind kind = ItemStats.kindOf(stack.getItem());

        int gs = gearScore(s);
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
        StatDisplay.line(lines, "Cursed Energy", s.cursed, "", ChatFormatting.DARK_PURPLE);

        // A "Cursed" prefix shows its bound cursed technique (right-click), styled in cursed purple.
        com.political.power.Power cursed = PrefixAbilities.cursedPowerOf(stack);
        if (cursed != null) {
            lines.add(Component.empty());
            lines.add(Component.literal("Cursed Technique: ").withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal(cursed.displayName).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                    .append(Component.literal("  RIGHT CLICK").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
            lines.add(Component.literal(cursed.description).withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal("Cursed Energy: ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(String.valueOf(cursed.energyCost)).withStyle(ChatFormatting.DARK_PURPLE)));
        }

        ItemActiveAbility active = resolveActive(stack);
        if (active != null) {
            lines.add(Component.empty());
            lines.add(Component.literal("Ability: ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(active.displayName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .append(Component.literal("  RIGHT CLICK").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
            lines.add(Component.literal(active.description).withStyle(ChatFormatting.GRAY));
            if (active.manaCost > 0) {
                lines.add(Component.literal("Mana Cost: ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(String.valueOf(active.manaCost)).withStyle(ChatFormatting.AQUA)));
            }
            lines.add(Component.literal("Cooldown: ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(active.cooldownSeconds + "s").withStyle(ChatFormatting.GREEN)));
        } else {
            List<Ability> passives = RpgItems.abilitiesOf(stack);
            if (!passives.isEmpty()) {
                lines.add(Component.empty());
                for (Ability a : passives) {
                    lines.add(Component.literal("Ability: ").withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(a.displayName).withStyle(a.color, ChatFormatting.BOLD)));
                    lines.add(Component.literal(a.description).withStyle(ChatFormatting.GRAY));
                }
            }
        }

        lines.add(Component.empty());
        lines.add(Component.literal("This item can be reforged!").withStyle(ChatFormatting.DARK_GRAY));
        if (variant == Variant.CURSED) {
            lines.add(Component.literal("Cursed (Grade " + ItemStats.cursedGradeOf(stack) + ")")
                    .withStyle(variant.color, ChatFormatting.BOLD));
        } else if (variant == Variant.UNIQUE) {
            lines.add(Component.literal("Unique").withStyle(variant.color, ChatFormatting.BOLD));
        }
        lines.add(Component.literal(rarityFooter(rarity, kind)).withStyle(rarity.color, ChatFormatting.BOLD));
        return lines;
    }

    private static ItemActiveAbility resolveActive(ItemStack stack) {
        // A "Unique" prefix's bound ability takes precedence, then the item's own active ability.
        ItemActiveAbility prefix = PrefixAbilities.uniqueAbilityOf(stack);
        if (prefix != null) return prefix;
        String id = RpgItems.idOf(stack);
        if (id != null) return ItemActiveAbility.forItem(id);
        return null;
    }

    private static double totalScale(double base, Rarity rarity) {
        return base * rarity.mult * 1.8;
    }

    private static String rarityFooter(Rarity rarity, ItemStats.Kind kind) {
        String type = switch (kind) {
            case SWORD -> "SWORD";
            case AXE -> "AXE";
            case RANGED -> "BOW";
            case TOOL -> "TOOL";
            case HELMET, CHEST, LEGS, BOOTS -> "ARMOR";
            default -> "ITEM";
        };
        return rarity.display.toUpperCase(Locale.ROOT) + " " + type;
    }
}
