package com.political.expansion.ranged;

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

/**
 * Builds {@link RangedWeapon} stacks. Mirrors {@code com.political.items.RpgItems#create} exactly:
 * authoritative Skyblock stats in {@code custom_data}, a custom {@code ITEM_MODEL} override,
 * stripped vanilla attribute modifiers ({@link ItemAttributeModifiers#EMPTY}), a rarity-coloured
 * name, and a rich tooltip. The only difference is that the active cast is resolved from this
 * package's {@link RangedCast} instead of the shared {@code ItemActiveAbility} enum.
 */
public final class RangedItems {

    /** Marks a stack as belonging to this expansion's cast engine. */
    public static final String CAST_KEY = "arc_cast";

    private RangedItems() {}

    public static ItemStack create(RangedWeapon def) {
        ItemStack stack = new ItemStack(def.baseItem);

        CompoundTag tag = new CompoundTag();
        // Reuse the shared item-id + rarity keys so ItemStats/StatManager treat this as custom gear.
        tag.putString(RpgItems.ITEM_ID_KEY, def.id());
        tag.putString(ItemStats.RARITY, def.rarity.name());
        if (def.intelligence != 0) tag.putInt(ItemStats.INTELLIGENCE, def.intelligence);
        if (def.damage != 0) tag.putInt(ItemStats.DAMAGE, def.damage);
        if (def.strength != 0) tag.putInt(ItemStats.STRENGTH, def.strength);
        if (def.critChance != 0) tag.putInt(ItemStats.CRIT_CHANCE, def.critChance);
        if (def.critDamage != 0) tag.putInt(ItemStats.CRIT_DAMAGE, def.critDamage);
        if (def.ferocity != 0) tag.putInt(ItemStats.FEROCITY, def.ferocity);
        // Our own marker so the cast engine can resolve the spell without the shared enum.
        tag.putString(CAST_KEY, def.cast.name());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        stack.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("politicalserver", def.id()));
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(def.displayName).withStyle(def.rarity.color, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(RangedTooltip.build(stack, def)));
        return stack;
    }

    /** Resolves the {@link RangedWeapon} carried by a stack, or {@code null}. */
    public static RangedWeapon weaponOf(ItemStack stack) {
        String id = RpgItems.idOf(stack);
        return id == null ? null : RangedWeapon.byId(id);
    }

    /** Resolves the {@link RangedCast} carried by a stack, or {@code null}. */
    public static RangedCast castOf(ItemStack stack) {
        RangedWeapon w = weaponOf(stack);
        return w == null ? null : w.cast;
    }
}
