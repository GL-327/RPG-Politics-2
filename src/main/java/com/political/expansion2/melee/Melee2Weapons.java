package com.political.expansion2.melee;

import com.political.items.ItemStats;
import com.political.items.RpgItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Phase-2 melee weapon registry ({@code wpn2_*} ids). */
public final class Melee2Weapons {

    public static final String MOD_ID = "politicalserver";

    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();
    private static final Map<String, Melee2Weapon> BY_ID = new LinkedHashMap<>();
    private static final Map<Item, Melee2Weapon> BY_ITEM = new LinkedHashMap<>();

    private Melee2Weapons() {}

    public static List<Item> items() {
        return new ArrayList<>(ITEMS.values());
    }

    public static List<ItemStack> displays() {
        List<ItemStack> out = new ArrayList<>();
        for (Melee2Weapon w : Melee2Weapon.values()) out.add(create(w));
        return out;
    }

    public static Melee2Weapon byId(String id) {
        return id == null ? null : BY_ID.get(id);
    }

    public static Melee2Weapon byItem(Item item) {
        return item == null ? null : BY_ITEM.get(item);
    }

    public static Melee2Weapon byStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        Melee2Weapon byItem = BY_ITEM.get(stack.getItem());
        if (byItem != null) return byItem;
        return byId(RpgItems.idOf(stack));
    }

    public static void register() {
        for (Melee2Weapon w : Melee2Weapon.values()) {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(MOD_ID, w.id));
            Item item = new Item(new Item.Properties().stacksTo(1).setId(key));
            Item registered = Registry.register(BuiltInRegistries.ITEM, key, item);
            ITEMS.put(w.id, registered);
            BY_ID.put(w.id, w);
            BY_ITEM.put(registered, w);
        }
        Melee2AbilityEngine.register();
    }

    public static ItemStack create(Melee2Weapon w) {
        Item item = ITEMS.get(w.id);
        ItemStack stack = item != null ? new ItemStack(item) : new ItemStack(net.minecraft.world.item.Items.IRON_SWORD);

        CompoundTag tag = new CompoundTag();
        tag.putString(RpgItems.ITEM_ID_KEY, w.id);
        tag.putString(ItemStats.RARITY, w.rarity.name());
        Melee2Weapon.Stats s = w.stats;
        if (s.damage != 0) tag.putInt(ItemStats.DAMAGE, s.damage);
        if (s.strength != 0) tag.putInt(ItemStats.STRENGTH, s.strength);
        if (s.intelligence != 0) tag.putInt(ItemStats.INTELLIGENCE, s.intelligence);
        if (s.health != 0) tag.putInt(ItemStats.HEALTH, s.health);
        if (s.defense != 0) tag.putInt(ItemStats.DEFENSE, s.defense);
        if (s.critChance != 0) tag.putInt(ItemStats.CRIT_CHANCE, s.critChance);
        if (s.critDamage != 0) tag.putInt(ItemStats.CRIT_DAMAGE, s.critDamage);
        if (s.ferocity != 0) tag.putInt(ItemStats.FEROCITY, s.ferocity);
        if (s.speed != 0) tag.putInt(ItemStats.SPEED, s.speed);
        if (s.attackSpeed != 0) tag.putInt(ItemStats.ATTACK_SPEED, s.attackSpeed);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        stack.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(MOD_ID, w.id));
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(w.displayName).withStyle(w.rarity.color, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(Melee2TooltipBuilder.build(stack, w)));
        return stack;
    }
}
