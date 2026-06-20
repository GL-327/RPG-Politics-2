package com.political.court;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Seizes the convicted player's gear and transfers it to the judge. */
public final class ConfiscationManager {

    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private ConfiscationManager() {}

    public static void seize(ServerPlayer judge, ServerPlayer accused) {
        int seized = 0;

        for (EquipmentSlot slot : ARMOR) {
            ItemStack stack = accused.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                giveOrDrop(judge, stack.copy());
                accused.setItemSlot(slot, ItemStack.EMPTY);
                seized++;
            }
        }

        Inventory inv = accused.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                giveOrDrop(judge, stack.copy());
                inv.setItem(i, ItemStack.EMPTY);
                seized++;
            }
        }

        accused.sendSystemMessage(Component.literal("Your possessions have been confiscated by the court.")
                .withStyle(ChatFormatting.RED));
        judge.sendSystemMessage(Component.literal("Confiscated " + seized + " item stack(s) from the accused.")
                .withStyle(ChatFormatting.GOLD));
    }

    private static void giveOrDrop(ServerPlayer judge, ItemStack stack) {
        if (!judge.getInventory().add(stack)) {
            judge.drop(stack, false);
        }
    }
}
