package com.political.economy;

import com.political.politics.DataManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

/** Fixed-price buying and selling against the {@link ShopItem} catalog. */
public final class ShopManager {

    private ShopManager() {}

    public static boolean buy(ServerPlayer player, ShopItem shopItem, int qty) {
        int cost = shopItem.buyPrice * qty;
        if (!DataManager.removeCoins(player.getStringUUID(), cost)) return false;
        ItemStack stack = new ItemStack(shopItem.item, qty);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        return true;
    }

    /** Sells the player's entire main-hand stack if it is in the catalog; returns coins earned, or -1. */
    public static int sellHeld(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) return -1;
        ShopItem shopItem = ShopItem.byItem(held.getItem());
        if (shopItem == null) return -1;
        int earned = shopItem.sellPrice * held.getCount();
        DataManager.addCoins(player.getStringUUID(), earned);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return earned;
    }
}
