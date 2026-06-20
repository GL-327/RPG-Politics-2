package com.political.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/** Builds {@link RpgItem} stacks with stat NBT, abilities, custom name, and lore. */
public final class RpgItems {

    public static final String ITEM_ID_KEY = "rpg_item_id";
    public static final String ABILITIES_KEY = "rpg_abilities";

    private RpgItems() {}

    public static ItemStack create(RpgItem def) {
        ItemStack stack = new ItemStack(def.baseItem);

        CompoundTag tag = new CompoundTag();
        tag.putString(ITEM_ID_KEY, def.id());
        if (def.health != 0) tag.putInt("rpg_health", def.health);
        if (def.defense != 0) tag.putInt("rpg_defense", def.defense);
        if (def.strength != 0) tag.putInt("rpg_strength", def.strength);
        if (def.intelligence != 0) tag.putInt("rpg_intelligence", def.intelligence);
        if (def.abilities.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (Ability a : def.abilities) {
                if (sb.length() > 0) sb.append(',');
                sb.append(a.name());
            }
            tag.putString(ABILITIES_KEY, sb.toString());
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(def.displayName).withStyle(def.rarity.color, ChatFormatting.BOLD));

        List<Component> lore = new ArrayList<>();
        addStat(lore, "Health", def.health, ChatFormatting.RED);
        addStat(lore, "Defense", def.defense, ChatFormatting.GREEN);
        addStat(lore, "Strength", def.strength, ChatFormatting.YELLOW);
        addStat(lore, "Mana", def.intelligence, ChatFormatting.AQUA);
        if (def.abilities.length > 0) {
            lore.add(Component.literal(""));
            for (Ability a : def.abilities) {
                lore.add(Component.literal("\u25C6 " + a.displayName).withStyle(a.color, ChatFormatting.BOLD)
                        .copy().append(Component.literal(" - " + a.description).withStyle(ChatFormatting.GRAY)));
            }
        }
        lore.add(Component.literal(""));
        lore.add(Component.literal(def.rarity.name()).withStyle(def.rarity.color, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(lore));

        return stack;
    }

    private static void addStat(List<Component> lore, String name, int value, ChatFormatting color) {
        if (value == 0) return;
        lore.add(Component.literal(name + ": +" + value).withStyle(color));
    }

    public static String idOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        String id = data.copyTag().getStringOr(ITEM_ID_KEY, "");
        return id.isEmpty() ? null : id;
    }

    /** Reads the ability list carried on a stack's {@code custom_data}. */
    public static List<Ability> abilitiesOf(ItemStack stack) {
        List<Ability> out = new ArrayList<>();
        if (stack == null || stack.isEmpty()) return out;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return out;
        String raw = data.copyTag().getStringOr(ABILITIES_KEY, "");
        if (raw.isEmpty()) return out;
        for (String token : raw.split(",")) {
            Ability a = Ability.byId(token);
            if (a != null) out.add(a);
        }
        return out;
    }
}
