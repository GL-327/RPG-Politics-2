package com.political.expansion2.ranged;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Public entry point for Expansion 2 ranged &amp; magic weapons ({@code arc2_} ids).
 *
 * <p><b>Integration:</b> call {@link #register()} from the mod initializer and add
 * {@link #items()} to a creative tab.
 */
public final class RangedExpansion2 {

    private RangedExpansion2() {}

    public static void register() {
        RangedAbilityEngine2.register();
    }

    public static List<ItemStack> items() {
        List<ItemStack> out = new ArrayList<>();
        for (RangedWeapon2 w : RangedWeapon2.values()) {
            out.add(RangedItems2.create(w));
        }
        return out;
    }
}
