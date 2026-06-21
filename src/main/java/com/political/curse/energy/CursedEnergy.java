package com.political.curse.energy;

import net.minecraft.world.entity.player.Player;

/**
 * Side-aware facade over a player's cursed-energy capacity, plus the curse-perception rule used by
 * {@code CurseEntity#isInvisibleTo}.
 *
 * <p>The actual numbers live in two different places depending on side:
 * <ul>
 *   <li><b>Server</b> &mdash; {@code com.political.combat.StatManager} is the single source of truth
 *       for a player's current/max cursed energy.</li>
 *   <li><b>Client</b> &mdash; {@code ClientRpgState} holds the value last synced from the server.</li>
 * </ul>
 * Neither of those can be referenced from this common class (one is server-flavoured, one lives in the
 * client source set), so both sides install a small {@link MaxProvider}/{@link CurrentProvider} at init
 * time. This keeps {@link #canPerceive(Player, int)} usable identically on both logical sides &mdash;
 * which matters because {@code Entity#isInvisibleTo} is evaluated server-side (for entity tracking) and
 * client-side (for rendering).</p>
 */
public final class CursedEnergy {

    /** Returns a viewer's <i>maximum</i> cursed-energy capacity (innate sensitivity). */
    public interface MaxProvider {
        double maxCursedEnergy(Player viewer);
    }

    /** Returns a viewer's <i>current</i> cursed-energy pool. */
    public interface CurrentProvider {
        double cursedEnergy(Player viewer);
    }

    private static MaxProvider serverMax = p -> 0.0;
    private static MaxProvider clientMax = p -> 0.0;
    private static CurrentProvider serverCurrent = p -> 0.0;
    private static CurrentProvider clientCurrent = p -> 0.0;

    private CursedEnergy() {}

    // ------------------------------------------------------------------
    // Provider installation (called once from the bootstraps)
    // ------------------------------------------------------------------

    public static void installServerProviders(MaxProvider max, CurrentProvider current) {
        if (max != null) serverMax = max;
        if (current != null) serverCurrent = current;
    }

    public static void installClientProviders(MaxProvider max, CurrentProvider current) {
        if (max != null) clientMax = max;
        if (current != null) clientCurrent = current;
    }

    // ------------------------------------------------------------------
    // Side-aware reads
    // ------------------------------------------------------------------

    public static double maxOf(Player viewer) {
        if (viewer == null) return 0.0;
        return viewer.level().isClientSide() ? clientMax.maxCursedEnergy(viewer) : serverMax.maxCursedEnergy(viewer);
    }

    public static double currentOf(Player viewer) {
        if (viewer == null) return 0.0;
        return viewer.level().isClientSide() ? clientCurrent.cursedEnergy(viewer) : serverCurrent.cursedEnergy(viewer);
    }

    // ------------------------------------------------------------------
    // Perception
    // ------------------------------------------------------------------

    /**
     * Minimum cursed-energy <i>capacity</i> a viewer needs to perceive a curse of the given internal
     * grade (1 = weakest "Grade 4" curse … 5 = Special Grade). Stronger curses radiate more presence
     * and are therefore easier to notice, so the requirement drops as the grade rises.
     */
    public static double requiredCapacity(int curseGrade) {
        return switch (Math.max(1, Math.min(5, curseGrade))) {
            case 1 -> 40.0;
            case 2 -> 28.0;
            case 3 -> 18.0;
            case 4 -> 9.0;
            default -> 3.0; // special grade — even a fledgling sorcerer feels it
        };
    }

    /**
     * Whether {@code viewer} can perceive a curse of {@code curseGrade}. Creative/spectator players
     * always see everything; otherwise the viewer must have enough innate cursed-energy capacity.
     */
    public static boolean canPerceive(Player viewer, int curseGrade) {
        if (viewer == null) return true;
        if (viewer.isCreative() || viewer.isSpectator()) return true;
        return maxOf(viewer) >= requiredCapacity(curseGrade);
    }
}
