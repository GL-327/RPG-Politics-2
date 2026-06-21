package com.political.expansion2.armor;

import com.political.items.Ability;
import com.political.items.ItemStats;
import com.political.items.RpgItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

public final class Armor2Items {
    public static final String SET_KEY = "arm2_set";
    private static final String NAMESPACE = "politicalserver";
    private Armor2Items() {}

    public static ItemStack create(ArmorSet set, ArmorSet.Slot slot) {
        ArmorSet.Piece piece = set.pieceFor(slot);
        String id = set.pieceId(slot);
        ItemStack stack = new ItemStack(set.baseItem(slot));
        CompoundTag tag = new CompoundTag();
        tag.putString(RpgItems.ITEM_ID_KEY, id);
        tag.putString(ItemStats.RARITY, set.rarity.name());
        tag.putString(SET_KEY, set.key);
        if (piece.health != 0) tag.putInt(ItemStats.HEALTH, piece.health);
        if (piece.defense != 0) tag.putInt(ItemStats.DEFENSE, piece.defense);
        if (piece.strength != 0) tag.putInt(ItemStats.STRENGTH, piece.strength);
        if (piece.intelligence != 0) tag.putInt(ItemStats.INTELLIGENCE, piece.intelligence);
        if (piece.speed != 0) tag.putInt(ItemStats.SPEED, piece.speed);
        if (piece.critChance != 0) tag.putInt(ItemStats.CRIT_CHANCE, piece.critChance);
        if (piece.critDamage != 0) tag.putInt(ItemStats.CRIT_DAMAGE, piece.critDamage);
        if (piece.ferocity != 0) tag.putInt(ItemStats.FEROCITY, piece.ferocity);
        if (piece.abilities.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (Ability a : piece.abilities) {
                if (sb.length() > 0) sb.append(',');
                sb.append(a.name());
            }
            tag.putString(RpgItems.ABILITIES_KEY, sb.toString());
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(NAMESPACE, id));
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(piece.displayName).withStyle(set.rarity.color, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(Armor2Tooltip.build(stack, set, piece)));
        return stack;
    }

    public static String setKeyOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        String key = data.copyTag().getStringOr(SET_KEY, "");
        return key.isEmpty() ? null : key;
    }

    public static List<ItemStack> items() {
        List<ItemStack> out = new ArrayList<>();
        for (ArmorSet set : ArmorSet.values()) {
            for (ArmorSet.Slot slot : ArmorSet.Slot.values()) out.add(create(set, slot));
        }
        return out;
    }
}
