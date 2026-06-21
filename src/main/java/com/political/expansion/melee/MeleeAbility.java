package com.political.expansion.melee;

/**
 * Self-contained RIGHT CLICK active abilities for the melee weapon expansion.
 *
 * <p>This mirrors the design of {@code com.political.items.ItemActiveAbility} but lives
 * entirely inside the {@code com.political.expansion.melee} package so the expansion never
 * has to edit the shared enum. Each {@link MeleeWeapon} references exactly one of these, and
 * {@link MeleeAbilityEngine} resolves + casts them.
 *
 * <p>Mana cost is paid out of the player's Skyblock Mana pool (intelligence); the cooldown is
 * tracked per-player by {@link MeleeAbilityEngine}.
 */
public enum MeleeAbility {
    // ---------------- Common ----------------
    CLEAVING_SLASH("Cleaving Slash", "Slash every foe in a short cone in front of you.", 20, 6),
    QUICK_JAB("Quick Jab", "Dash a short step and stab the foe you look at.", 15, 5),
    CRUSHING_BLOW("Crushing Blow", "Smash a single target, briefly slowing it.", 20, 7),
    TIMBER_HACK("Timber Hack", "Hack apart enemies in a tight wedge.", 18, 6),
    BRACED_LUNGE("Braced Lunge", "Lunge forward and knock back your target.", 18, 6),

    // ---------------- Uncommon ----------------
    RIPOSTE("Riposte", "Parry forward, damaging foes and gaining brief Resistance.", 25, 8),
    BLEEDING_STAB("Bleeding Stab", "Stab the target, leaving deep Poison.", 22, 7),
    REND("Rend", "Tear at a cone of foes, weakening them.", 25, 9),
    SKEWER("Skewer", "Impale a target from range and pull yourself in.", 24, 8),
    CONCUSSION("Concussion", "Daze nearby foes with heavy Slowness.", 26, 9),

    // ---------------- Rare ----------------
    VALIANT_SWEEP("Valiant Sweep", "A wide sweeping arc that knocks foes back.", 35, 10),
    SHADOW_FLICKER("Shadow Flicker", "Blink behind your target and strike for bonus damage.", 40, 12),
    STATIC_REACH("Static Reach", "Electrify foes along a long line ahead.", 38, 11),
    IAIDO_DRAW("Iaido Draw", "An instant draw-cut that bleeds and slows.", 36, 10),
    REAPING_ARC("Reaping Arc", "Sweep a crescent of withering soul energy.", 40, 12),

    // ---------------- Epic ----------------
    EARTHSHATTER("Earthshatter", "Slam the ground, launching everything around you.", 55, 16),
    RENDING_FRENZY("Rending Frenzy", "A flurry of poisoned claw strikes on nearby foes.", 50, 14),
    PERMAFROST_CHOP("Permafrost Chop", "Freeze and shatter all enemies around you.", 55, 15),
    MAGMA_CLEAVE("Magma Cleave", "Ignite a fiery cone of enemies.", 52, 14),
    LASHING_COIL("Lashing Coil", "Lash and reel in distant foes.", 50, 13),
    LEVIN_THROW("Levin Throw", "Hurl a bolt of lightning at your target.", 55, 15),

    // ---------------- Legendary ----------------
    COMET_SMASH("Comet Smash", "Crash down for massive area damage and knockback.", 75, 22),
    CRESCENT_ECLIPSE("Crescent Eclipse", "An eclipsing arc dealing heavy crit damage and Slowness.", 70, 20),
    SOUL_HARVEST("Soul Harvest", "Reap the souls of nearby foes, healing yourself.", 75, 22),
    SWEEPING_VORTEX("Sweeping Vortex", "A vortex pulls foes in and rips a cone apart.", 72, 21),
    SEISMIC_SLAM("Seismic Slam", "A quake that stuns and crushes everything nearby.", 75, 23),
    THOUSAND_CUTS("Thousand Cuts", "Blink between nearby foes, cutting each one.", 70, 20),

    // ---------------- Mythic ----------------
    DIVINE_EXECUTION("Divine Execution", "Execute the wounded with a burst of holy light.", 95, 30),
    OBLIVION_RIFT("Oblivion Rift", "Collapse a void rift that withers all nearby foes.", 100, 32),
    CATACLYSM("Cataclysm", "Erupt the earth in fire and force around you.", 100, 33),
    REAP_ETERNAL("Reap the Eternal", "A massive withering reap that drains life from all foes.", 95, 30);

    public final String displayName;
    public final String description;
    public final int manaCost;
    public final int cooldownSeconds;

    MeleeAbility(String displayName, String description, int manaCost, int cooldownSeconds) {
        this.displayName = displayName;
        this.description = description;
        this.manaCost = manaCost;
        this.cooldownSeconds = cooldownSeconds;
    }
}
