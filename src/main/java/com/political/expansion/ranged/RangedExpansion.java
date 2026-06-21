package com.political.expansion.ranged;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Public entry point for the ranged &amp; magic weapon expansion.
 *
 * <p><b>Integration:</b> call {@link #register()} once from the mod initializer (alongside the
 * other {@code *.register()} calls in {@code RpgPoliticsMod#onInitialize}) to wire up the
 * RIGHT CLICK cast engine, and add {@link #items()} to a creative tab (e.g. in
 * {@code CreativeCatalog#gear}) to surface every weapon in-game.
 */
public final class RangedExpansion {

    private RangedExpansion() {}

    /** Registers this expansion's self-contained cast engine (UseItemCallback-driven). */
    public static void register() {
        RangedAbilityEngine.register();
    }

    /** Every ranged/magic weapon as a fully-built, tooltip-decorated stack for creative tabs. */
    public static List<ItemStack> items() {
        List<ItemStack> out = new ArrayList<>();
        for (RangedWeapon w : RangedWeapon.values()) {
            out.add(RangedItems.create(w));
        }
        return out;
    }
}
