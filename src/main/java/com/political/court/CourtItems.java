package com.political.court;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

/**
 * "The Gavel" - the judge's execution weapon. It is a rebranded Mace carrying a
 * {@code the_gavel} marker in custom data, so it never collides with vanilla maces.
 */
public final class CourtItems {

    public static final String GAVEL_KEY = "the_gavel";

    private CourtItems() {}

    /** Must be called while a world is loaded (26.2 forbids early ItemStack creation). */
    public static ItemStack createGavel() {
        ItemStack stack = new ItemStack(Items.MACE);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(GAVEL_KEY, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("The Gavel").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        return stack;
    }

    public static boolean isGavel(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.is(Items.MACE)) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBooleanOr(GAVEL_KEY, false);
    }
}
