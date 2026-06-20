package com.political.economy;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Fixed-price shop catalog. */
public enum ShopItem {
    DIAMOND("diamond", Items.DIAMOND, 120, 60),
    IRON("iron_ingot", Items.IRON_INGOT, 24, 12),
    GOLD("gold_ingot", Items.GOLD_INGOT, 40, 20),
    LOG("oak_log", Items.OAK_LOG, 6, 2),
    BREAD("bread", Items.BREAD, 10, 4),
    EMERALD("emerald", Items.EMERALD, 150, 75),
    ARROW("arrow", Items.ARROW, 4, 1),
    ENDER_PEARL("ender_pearl", Items.ENDER_PEARL, 80, 30);

    public final String id;
    public final Item item;
    public final int buyPrice;
    public final int sellPrice;

    ShopItem(String id, Item item, int buyPrice, int sellPrice) {
        this.id = id;
        this.item = item;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public static ShopItem byId(String id) {
        for (ShopItem s : values()) {
            if (s.id.equalsIgnoreCase(id)) return s;
        }
        return null;
    }

    public static ShopItem byItem(Item item) {
        for (ShopItem s : values()) {
            if (s.item == item) return s;
        }
        return null;
    }
}
