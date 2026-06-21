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
        int total = totalGearScore(s, rarity);
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
        if (s.cursed > 0) skyStat(lines, "Cursed Energy", s.cursed, totalScale(s.cursed, rarity), "", ChatFormatting.DARK_PURPLE);

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
        String id = RpgItems.idOf(stack);
        if (id != null) return ItemActiveAbility.forItem(id);
        return null;
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
