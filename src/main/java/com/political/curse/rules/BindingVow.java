package com.political.curse.rules;

import com.political.curse.SorcererGrade;
import net.minecraft.ChatFormatting;

/**
 * Binding Vows (束縛, <i>kraku</i>) — the canonical JJK risk/reward contracts a sorcerer swears to
 * amplify their cursed energy in exchange for a self-imposed restriction or cost. Each vow is a
 * persistent toggle (see {@link JjkRules}) that multiplies cursed-technique output and/or cursed-energy
 * cost and may carry a permanent body toll (reduced maximum health) for as long as it is sworn.
 *
 * <p>Vows stack multiplicatively, so swearing several at once is potent but punishing — exactly the
 * over-extension that defines the mechanic in the source material.</p>
 */
public enum BindingVow {

    /**
     * "Revealed Technique" — binding that comes from explaining your cursed technique to the opponent.
     * In canon this sharply raises a technique's power (Hollow Purple, etc.) at the cost of surrendering
     * the element of surprise: here, far greater output but every cast costs more cursed energy.
     */
    OATH_OF_SECRECY("Revealed Technique", ChatFormatting.GOLD, SorcererGrade.GRADE_4,
            1.45, 1.40, 0.0,
            "Lay your cursed technique bare. Its output soars, but every cast demands more cursed energy."),

    /**
     * "Overtime" — a time/condition vow that lends extra power now against a steady toll. Cheaper, harder
     * casts traded for a permanent bite out of your vitality while sworn.
     */
    OVERTIME("Overtime", ChatFormatting.RED, SorcererGrade.GRADE_3,
            1.20, 0.72, 8.0,
            "Borrow against your future. Techniques cost less and hit harder while a steady toll gnaws your body."),

    /**
     * "Throughput Vow" — pour everything into raw output by forsaking your own defence. The classic
     * glass-cannon binding: large damage gain paid for in maximum health.
     */
    THROUGHPUT("Throughput Vow", ChatFormatting.DARK_PURPLE, SorcererGrade.GRADE_2,
            1.30, 1.00, 14.0,
            "Forsake your guard for force. Greatly increased output at a heavy cost to your vitality."),

    /**
     * "Sacrificial Pact" — the most extreme vow available to a special-grade sorcerer: overwhelming
     * power bought with a grievous, lasting wound to the body.
     */
    BLOOD_PACT("Sacrificial Pact", ChatFormatting.DARK_RED, SorcererGrade.SPECIAL,
            1.70, 0.90, 30.0,
            "Stake your very flesh. Overwhelming output, but your maximum health is gravely diminished.");

    public final String display;
    public final ChatFormatting color;
    public final int requiredGrade;
    /** Multiplier applied to cursed-technique / domain output while sworn. */
    public final double outputMultiplier;
    /** Multiplier applied to cursed-energy costs while sworn (&lt;1 = cheaper). */
    public final double ceCostMultiplier;
    /** Flat maximum-health penalty (raw points) paid while the vow is sworn. */
    public final double healthPenalty;
    public final String description;

    BindingVow(String display, ChatFormatting color, int requiredGrade,
               double outputMultiplier, double ceCostMultiplier, double healthPenalty, String description) {
        this.display = display;
        this.color = color;
        this.requiredGrade = requiredGrade;
        this.outputMultiplier = outputMultiplier;
        this.ceCostMultiplier = ceCostMultiplier;
        this.healthPenalty = healthPenalty;
        this.description = description;
    }

    public String id() {
        return name().toLowerCase();
    }

    public static BindingVow byId(String id) {
        if (id == null) return null;
        try {
            return valueOf(id.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
