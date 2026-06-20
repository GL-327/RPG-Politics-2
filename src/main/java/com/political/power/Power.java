package com.political.power;

import net.minecraft.ChatFormatting;

/**
 * The unified power roster. Both The Boys' Compound&nbsp;V powers and Jujutsu Kaisen
 * cursed techniques are modelled here: each costs <b>Energy</b> (the shared resource
 * that is also Cursed Energy) and has a cooldown. {@link PowerManager} implements each
 * one through events and vanilla mechanics (no mixins).
 */
public enum Power {
    // ---------------- Compound V ----------------
    LASER_EYES("Laser Eyes", Origin.COMPOUND_V, 18, 40,
            "Fire a searing beam from your eyes, burning what you look at."),
    PYROKINESIS("Pyrokinesis", Origin.COMPOUND_V, 25, 60,
            "Hurl a burst of flame, igniting everything ahead."),
    CRYOKINESIS("Cryokinesis", Origin.COMPOUND_V, 25, 60,
            "Freeze and slow every foe around you."),
    SONIC_SCREAM("Sonic Scream", Origin.COMPOUND_V, 30, 80,
            "Unleash a deafening blast that hurls enemies back."),
    SUPER_LEAP("Super Leap", Origin.COMPOUND_V, 12, 30,
            "Launch yourself high into the air."),
    SUPER_STRENGTH("Super Strength", Origin.COMPOUND_V, 30, 200,
            "Surge with strength and resilience for a short time."),
    SPEEDSTER("Speedster", Origin.COMPOUND_V, 20, 120,
            "Move and strike at blinding speed."),
    FLIGHT("Flight", Origin.COMPOUND_V, 40, 200,
            "Take to the skies under your own power."),
    INVISIBILITY("Invisibility", Origin.COMPOUND_V, 28, 160,
            "Vanish from sight."),
    TELEKINESIS("Telekinesis", Origin.COMPOUND_V, 18, 40,
            "Violently fling whatever you are looking at."),
    TELEPORT("Teleport", Origin.COMPOUND_V, 24, 60,
            "Blink forward through space."),
    LIFEDRAIN("Lifesteal", Origin.COMPOUND_V, 28, 100,
            "Rip life from nearby foes to heal yourself."),
    HEALING("Healing Factor", Origin.COMPOUND_V, 32, 160,
            "Rapidly knit your wounds back together."),
    CHEST_BLAST("Chest Blast", Origin.COMPOUND_V, 40, 120,
            "Project a devastating blast straight ahead."),
    STORMFRONT("Stormfront", Origin.COMPOUND_V, 45, 140,
            "Call down lightning on your target."),
    FORCEFIELD("Forcefield", Origin.COMPOUND_V, 35, 200,
            "Wrap yourself in a protective barrier."),
    STAR_POWER("Star Power", Origin.COMPOUND_V, 60, 400,
            "Become near-untouchable: flight, strength and resistance at once."),
    PETRIFYING_GAZE("Petrifying Gaze", Origin.COMPOUND_V, 30, 120,
            "Lock eyes to freeze a foe in place."),
    MIND_CONTROL("Mind Control", Origin.COMPOUND_V, 35, 160,
            "Bend a nearby creature to fight for you."),
    HEAD_POP("Head Pop", Origin.COMPOUND_V, 70, 300,
            "Focus lethal force on whatever you are looking at."),
    ADAPTIVE_BIOLOGY("Adaptive Biology", Origin.COMPOUND_V, 30, 200,
            "Your rewritten body resonates with latent cursed energy, deepening your reserves.", 80),
    REGENERATIVE_CODE("Regenerative Code", Origin.COMPOUND_V, 40, 220,
            "Cellular overdrive heals you and awakens dormant cursed energy.", 40),

    // ---------------- Jujutsu Kaisen cursed techniques ----------------
    LIMITLESS_BLUE("Cursed Technique: Blue", Origin.CURSED_TECHNIQUE, 30, 80,
            "Limitless \u2014 Blue. Drag nearby entities violently toward you."),
    LIMITLESS_RED("Cursed Technique: Red", Origin.CURSED_TECHNIQUE, 45, 140,
            "Limitless \u2014 Red. Blast everything away with reversed force."),
    HOLLOW_PURPLE("Hollow Purple", Origin.CURSED_TECHNIQUE, 90, 400,
            "Collide Blue and Red into an annihilating forward wave."),
    DISMANTLE("Dismantle", Origin.CURSED_TECHNIQUE, 24, 50,
            "Cleaving slashes that shred enemies ahead."),
    CLEAVE("Cleave", Origin.CURSED_TECHNIQUE, 30, 70,
            "A single, decisive cursed slash."),
    DIVERGENT_FIST("Divergent Fist", Origin.CURSED_TECHNIQUE, 20, 40,
            "Strike with a delayed second impact that sends foes flying."),
    BLACK_FLASH("Black Flash", Origin.CURSED_TECHNIQUE, 25, 100,
            "Empower your next blow with a distorted, critical impact."),
    REVERSE_CURSED("Reverse Cursed Technique", Origin.CURSED_TECHNIQUE, 50, 200,
            "Convert cursed energy into rapid healing."),
    TEN_SHADOWS("Ten Shadows", Origin.CURSED_TECHNIQUE, 50, 300,
            "Summon shadow beasts to fight at your side."),
    CURSED_SPEECH("Cursed Speech", Origin.CURSED_TECHNIQUE, 35, 120,
            "Speak a command imbued with cursed energy, stunning all ahead."),
    DOMAIN_EXPANSION("Domain Expansion", Origin.CURSED_TECHNIQUE, 100, 600,
            "Manifest your innate domain: every foe within is struck without fail."),
    INFINITY("Infinity", Origin.CURSED_TECHNIQUE, 55, 220,
            "Fold the space before you so nothing can truly reach \u2014 briefly untouchable."),
    SIX_EYES("Six Eyes", Origin.CURSED_TECHNIQUE, 0, 240,
            "Perfect perception: instantly refill your cursed energy and sharpen your senses."),
    SIMPLE_DOMAIN("Simple Domain", Origin.CURSED_TECHNIQUE, 40, 160,
            "A defensive circle that wards and repels all who draw near."),
    WORLD_CUTTING_SLASH("World-Cutting Slash", Origin.CURSED_TECHNIQUE, 60, 200,
            "A single slash that severs everything in a wide arc before you.");

    /** Where a power comes from, which controls how it is acquired and displayed. */
    public enum Origin {
        COMPOUND_V("Compound V", ChatFormatting.RED),
        CURSED_TECHNIQUE("Cursed Technique", ChatFormatting.DARK_PURPLE);

        public final String label;
        public final ChatFormatting color;

        Origin(String label, ChatFormatting color) {
            this.label = label;
            this.color = color;
        }
    }

    public final String displayName;
    public final Origin origin;
    public final int energyCost;
    public final int cooldownTicks;
    public final String description;
    /** Compound V powers that permanently raise the wielder's maximum Cursed Energy. */
    public final int cursedEnergyBonus;

    Power(String displayName, Origin origin, int energyCost, int cooldownTicks, String description) {
        this(displayName, origin, energyCost, cooldownTicks, description, 0);
    }

    Power(String displayName, Origin origin, int energyCost, int cooldownTicks, String description, int cursedEnergyBonus) {
        this.displayName = displayName;
        this.origin = origin;
        this.energyCost = energyCost;
        this.cooldownTicks = cooldownTicks;
        this.description = description;
        this.cursedEnergyBonus = cursedEnergyBonus;
    }

    /** Sum of cursed-energy capacity granted by a player's known powers. */
    public static double cursedEnergyBonus(java.util.List<String> knownPowerIds) {
        double sum = 0;
        for (String id : knownPowerIds) {
            Power p = byId(id);
            if (p != null) sum += p.cursedEnergyBonus;
        }
        return sum;
    }

    public String id() {
        return name().toLowerCase();
    }

    public static Power byId(String id) {
        if (id == null) return null;
        try {
            return valueOf(id.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static java.util.List<Power> ofOrigin(Origin origin) {
        java.util.List<Power> out = new java.util.ArrayList<>();
        for (Power p : values()) if (p.origin == origin) out.add(p);
        return out;
    }
}
