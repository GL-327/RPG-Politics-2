package com.political.curse;

import com.political.politics.DataManager;
import net.minecraft.server.level.ServerPlayer;

/**
 * Read-only helper around the sorcerer-grade ladder that already lives in {@link DataManager}
 * ({@code sorcererGrade}/{@code setSorcererGrade}, persisted, and auto-promoted by exorcisms).
 *
 * <p>Grades use the same inverted JJK scale as the rest of the mod: {@code 0} = non-sorcerer,
 * {@code 1} = Grade&nbsp;4 (weakest), … {@code 5} = Special Grade (strongest). This class adds the
 * JJK-overhaul semantics on top of that ladder without modifying {@code DataManager} or
 * {@code ProgressionManager}: technique gating and a suggested cursed-energy ceiling per grade.</p>
 */
public final class SorcererGrade {

    public static final int NONE = 0;
    public static final int GRADE_4 = 1;
    public static final int GRADE_3 = 2;
    public static final int GRADE_2 = 3;
    public static final int GRADE_1 = 4;
    public static final int SPECIAL = 5;

    /** Base capacity every awakened sorcerer carries, before grade scaling. */
    private static final double BASE_CAPACITY = 30.0;
    /** Extra max cursed energy granted per grade step. */
    private static final double CAPACITY_PER_GRADE = 26.0;

    private SorcererGrade() {}

    public static int of(ServerPlayer player) {
        return DataManager.sorcererGrade(player.getStringUUID());
    }

    public static int of(String uuid) {
        return DataManager.sorcererGrade(uuid);
    }

    public static String label(int grade) {
        return DataManager.gradeLabel(grade);
    }

    /** Whether a player of {@code playerGrade} meets a technique/domain {@code requiredGrade}. */
    public static boolean meets(int playerGrade, int requiredGrade) {
        return playerGrade >= requiredGrade;
    }

    /**
     * Suggested maximum cursed energy for a sorcerer of the given grade. The live HUD maximum is owned
     * by {@code StatManager.compute}; integration can fold this in (see the handoff manifest) so that
     * climbing the grade ladder visibly raises the cursed-energy ceiling.
     */
    public static double maxCursedEnergyFor(int grade) {
        if (grade <= 0) return 0.0;
        return BASE_CAPACITY + grade * CAPACITY_PER_GRADE;
    }
}
