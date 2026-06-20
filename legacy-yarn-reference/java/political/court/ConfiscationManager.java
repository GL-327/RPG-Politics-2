package com.political.court;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles the "confiscation" outcome of a court execution: the judge seizes the
 * accused's equipped armour plus a couple of random inventory items as evidence,
 * which are deposited into the judge's inventory (or dropped at their feet).
 */
public final class ConfiscationManager {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final int MAX_EXTRA_ITEMS = 2;

    private ConfiscationManager() {}

    public static void seize(ServerPlayerEntity judge, ServerPlayerEntity accused) {
        List<ItemStack> seized = new ArrayList<>();

        // Armour first.
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armor = accused.getEquippedStack(slot);
            if (armor != null && !armor.isEmpty()) {
                seized.add(armor.copy());
                accused.equipStack(slot, ItemStack.EMPTY);
            }
        }

        // Then a couple of random items from the main inventory.
        List<Integer> nonEmpty = new ArrayList<>();
        int limit = Math.min(36, accused.getInventory().size());
        for (int i = 0; i < limit; i++) {
            if (!accused.getInventory().getStack(i).isEmpty()) {
                nonEmpty.add(i);
            }
        }
        Collections.shuffle(nonEmpty);
        for (int i = 0; i < Math.min(MAX_EXTRA_ITEMS, nonEmpty.size()); i++) {
            int slot = nonEmpty.get(i);
            seized.add(accused.getInventory().getStack(slot).copy());
            accused.getInventory().setStack(slot, ItemStack.EMPTY);
        }

        if (seized.isEmpty()) {
            judge.sendMessage(Text.literal("The accused carried nothing of value to confiscate.")
                    .formatted(Formatting.GRAY), false);
            return;
        }

        for (ItemStack stack : seized) {
            judge.getInventory().offerOrDrop(stack);
        }
        judge.sendMessage(Text.literal("Confiscated " + seized.size() + " item(s) as evidence.")
                .formatted(Formatting.LIGHT_PURPLE), false);
    }
}
