package com.political.expansion2.armor;

import net.minecraft.world.item.ItemStack;
import java.util.List;

public final class Armor2Expansion {
    private Armor2Expansion() {}
    public static void register() { Armor2SetBonusHandler.register(); }
    public static List<ItemStack> items() { return Armor2Items.items(); }
}
