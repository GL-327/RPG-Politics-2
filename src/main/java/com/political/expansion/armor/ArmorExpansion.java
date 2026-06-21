package com.political.expansion.armor;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Public entry point for the {@code arm_} armour-set expansion.
 *
 * <p><b>Integration wiring</b> (done by the integration agent, since shared files are off-limits here):
 * <ul>
 *   <li>Call {@link #register()} once from the mod initializer (alongside the other {@code register()}
 *       calls) so the full-set-bonus tick runs.</li>
 *   <li>Add {@link #items()} to a creative tab (e.g. in {@code CreativeCatalog#gear}) so the pieces are
 *       obtainable in survival/creative.</li>
 * </ul>
 *
 * <p>Everything else is automatic: piece stats flow through {@code ItemStats}/{@code StatManager} and
 * per-piece passive abilities through the existing {@code AbilityEngine} equipment tick, because the
 * pieces carry the same {@code custom_data} keys as the core {@code RpgItem}s.
 */
public final class ArmorExpansion {

    private ArmorExpansion() {}

    /** Wires the self-contained full-set-bonus equipment tick. Safe to call exactly once. */
    public static void register() {
        ArmorSetBonusHandler.register();
    }

    /** One {@link ItemStack} per armour piece (14 sets x 4 = 56), ready for creative tabs. */
    public static List<ItemStack> items() {
        return ArmorItems.items();
    }
}
