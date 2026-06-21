package com.political.expansion2.ranged;

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

public final class RangedItems2 {

    public static final String CAST_KEY = "arc2_cast";

    private RangedItems2() {}

    public static ItemStack create(RangedWeapon2 def) {
        ItemStack stack = new ItemStack(def.baseItem);

        CompoundTag tag = new CompoundTag();
        tag.putString(RpgItems.ITEM_ID_KEY, def.id());
        tag.putString(ItemStats.RARITY, def.rarity.name());
        if (def.intelligence != 0) tag.putInt(ItemStats.INTELLIGENCE, def.intelligence);
        if (def.damage != 0) tag.putInt(ItemStats.DAMAGE, def.damage);
        if (def.strength != 0) tag.putInt(ItemStats.STRENGTH, def.strength);
        if (def.critChance != 0) tag.putInt(ItemStats.CRIT_CHANCE, def.critChance);
        if (def.critDamage != 0) tag.putInt(ItemStats.CRIT_DAMAGE, def.critDamage);
        if (def.ferocity != 0) tag.putInt(ItemStats.FEROCITY, def.ferocity);
        tag.putString(CAST_KEY, def.cast.name());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        stack.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("politicalserver", def.id()));
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(def.displayName).withStyle(def.rarity.color, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(RangedTooltip2.build(stack, def)));
        return stack;
    }

    public static RangedWeapon2 weaponOf(ItemStack stack) {
        String id = RpgItems.idOf(stack);
        return id == null ? null : RangedWeapon2.byId(id);
    }

    public static RangedCast2 castOf(ItemStack stack) {
        RangedWeapon2 w = weaponOf(stack);
        if (w != null) return w.cast;
        if (stack == null || stack.isEmpty()) return null;
        var data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        String castName = data.copyTag().getStringOr(CAST_KEY, "");
        if (castName.isEmpty()) return null;
        try {
            return RangedCast2.valueOf(castName);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
