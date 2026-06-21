package com.political.client;

import com.political.items.ItemStats;
import com.political.items.Rarity;
import com.political.items.RpgItems;
import com.political.items.Variant;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Adds a live Skyblock-style tooltip to every vanilla item: computed stat lines, then a rarity
 * footer (and a Cursed/Unique tag). Custom RPG gear, reforged items and cursed tools already
 * carry baked-in lore from {@link ItemStats}/{@link RpgItems}, so those are skipped here to
 * avoid doubling up — giving one consistent format across the whole game.
 */
public final class ItemTooltips {

    private ItemTooltips() {}

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> append(stack, lines));
    }

    private static void append(ItemStack stack, List<Component> lines) {
        if (stack == null || stack.isEmpty()) return;

        CompoundTag tag = ItemStats.tagOf(stack);
        boolean alreadyDecorated = !tag.getStringOr(RpgItems.ITEM_ID_KEY, "").isEmpty()
                || !tag.getStringOr(ItemStats.RARITY, "").isEmpty()
                || !tag.getStringOr(ItemStats.VARIANT, "").isEmpty()
                || tag.getIntOr(ItemStats.CURSED_GRADE, 0) > 0;
        if (alreadyDecorated) return; // its persistent lore already shows the Skyblock block

        ItemStats.Sheet s = ItemStats.compute(stack);
        Rarity rarity = ItemStats.rarityOf(stack);
        Variant variant = ItemStats.variantOf(stack);

        boolean hasStats = !empty(s);
        if (hasStats) {
            lines.add(Component.literal(""));
            stat(lines, "Health", s.health, "", ChatFormatting.RED);
            stat(lines, "Defense", s.defense, "", ChatFormatting.GREEN);
            stat(lines, "Strength", s.strength, "", ChatFormatting.YELLOW);
            stat(lines, "Crit Chance", s.critChance, "%", ChatFormatting.BLUE);
            stat(lines, "Crit Damage", s.critDamage, "%", ChatFormatting.BLUE);
            stat(lines, "Ferocity", s.ferocity, "", ChatFormatting.RED);
            stat(lines, "Speed", s.speed, "", ChatFormatting.WHITE);
            stat(lines, "Mana", s.intelligence, "", ChatFormatting.AQUA);
            stat(lines, "Cursed Energy", s.cursed, "", ChatFormatting.DARK_PURPLE);
        }

        lines.add(Component.literal(""));
        if (variant == Variant.CURSED) {
            lines.add(Component.literal("Cursed (Grade " + ItemStats.cursedGradeOf(stack) + ")")
                    .withStyle(variant.color, ChatFormatting.BOLD));
        } else if (variant == Variant.UNIQUE) {
            lines.add(Component.literal("Unique").withStyle(variant.color, ChatFormatting.BOLD));
        }
        lines.add(Component.literal(rarity.display.toUpperCase()).withStyle(rarity.color, ChatFormatting.BOLD));
    }

    private static boolean empty(ItemStats.Sheet s) {
        return s.health == 0 && s.defense == 0 && s.strength == 0 && s.intelligence == 0 && s.cursed == 0
                && s.critChance == 0 && s.critDamage == 0 && s.ferocity == 0 && s.speed == 0 && s.attackSpeed == 0;
    }

    private static void stat(List<Component> lines, String name, double value, String suffix, ChatFormatting color) {
        if (value == 0) return;
        lines.add(Component.literal(name + ": +" + (int) Math.round(value) + suffix).withStyle(color));
    }
}
