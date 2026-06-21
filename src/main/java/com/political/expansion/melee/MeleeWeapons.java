package com.political.expansion.melee;

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

/**
 * The melee weapon expansion registry. Each {@link MeleeWeapon} is registered as its own
 * vanilla-free {@link Item} (id {@code politicalserver:wpn_*}) and decorated with the mod's
 * Skyblock stat system:
 *
 * <ul>
 *   <li>combat stats carried entirely as {@code custom_data} ({@link ItemStats} keys);</li>
 *   <li>{@code ATTRIBUTE_MODIFIERS} set to {@link ItemAttributeModifiers#EMPTY} — no vanilla
 *       attack damage / speed lines;</li>
 *   <li>{@code ITEM_MODEL} pointing at the weapon's own texture model;</li>
 *   <li>a Skyblock tooltip via {@link MeleeTooltipBuilder} including the unique right-click ability.</li>
 * </ul>
 *
 * <p><b>Integration notes:</b> call {@link #register()} from the mod initializer (alongside the
 * other {@code *.register()} calls), and add {@link #displays()} to a creative tab. The right-click
 * active-ability handler is wired up automatically inside {@link #register()} via
 * {@link MeleeAbilityEngine#register()}.
 */
public final class MeleeWeapons {

    public static final String MOD_ID = "politicalserver";

    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();
    private static final Map<String, MeleeWeapon> BY_ID = new LinkedHashMap<>();
    private static final Map<Item, MeleeWeapon> BY_ITEM = new LinkedHashMap<>();

    private MeleeWeapons() {}

    /** All registered melee weapon items, in catalogue order. */
    public static List<Item> items() {
        return new ArrayList<>(ITEMS.values());
    }

    /** Fully decorated display stacks (stats + ability tooltip), for creative tabs. */
    public static List<ItemStack> displays() {
        List<ItemStack> out = new ArrayList<>();
        for (MeleeWeapon w : MeleeWeapon.values()) out.add(create(w));
        return out;
    }

    public static MeleeWeapon byId(String id) {
        return id == null ? null : BY_ID.get(id);
    }

    public static MeleeWeapon byItem(Item item) {
        return item == null ? null : BY_ITEM.get(item);
    }

    /** Resolves the weapon definition carried by a stack (via its {@code rpg_item_id}). */
    public static MeleeWeapon byStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        MeleeWeapon byItem = BY_ITEM.get(stack.getItem());
        if (byItem != null) return byItem;
        return byId(RpgItems.idOf(stack));
    }

    public static void register() {
        for (MeleeWeapon w : MeleeWeapon.values()) {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(MOD_ID, w.id));
            Item item = new Item(new Item.Properties().stacksTo(1).setId(key));
            Item registered = Registry.register(BuiltInRegistries.ITEM, key, item);
            ITEMS.put(w.id, registered);
            BY_ID.put(w.id, w);
            BY_ITEM.put(registered, w);
        }
        MeleeAbilityEngine.register();
    }

    /** Builds a fully decorated stack for the given weapon. */
    public static ItemStack create(MeleeWeapon w) {
        Item item = ITEMS.get(w.id);
        ItemStack stack = item != null ? new ItemStack(item) : new ItemStack(net.minecraft.world.item.Items.IRON_SWORD);

        CompoundTag tag = new CompoundTag();
        // Stamping rpg_item_id forces ItemStats onto the authoritative "explicit stats" path
        // (so these custom_data values are used verbatim, never re-inferred from a base item).
        tag.putString(RpgItems.ITEM_ID_KEY, w.id);
        tag.putString(ItemStats.RARITY, w.rarity.name());
        MeleeWeapon.Stats s = w.stats;
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
        // Skyblock-governed gear carries no vanilla attribute modifiers (attack damage/speed).
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(w.displayName).withStyle(w.rarity.color, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(MeleeTooltipBuilder.build(stack, w)));
        return stack;
    }
}
