package com.political.curse.spirits;

import net.minecraft.ChatFormatting;

import java.util.EnumSet;
import java.util.Set;

/**
 * The full roster of cursed spirits (Jujutsu-Kaisen flavoured), inspired by JujutsuCraft's curse
 * line-up and Prominence II mob design. Each entry is a self-describing data record: registry id,
 * display name, natural grade band (1 = Grade 4 ... 5 = Special Grade), base attributes, the client
 * {@link ModelKind} + hitbox used to render it, its {@link Behavior} kit, a loot tier and a boss flag.
 *
 * <p>One {@code EntityType} is registered per species (see {@link ModSpirits}); the shared
 * {@link CursedSpiritEntity} resolves its species from its type at construction. Grade scaling on top
 * of these base stats happens in {@code CurseManager.manifest}.
 */
public enum SpiritSpecies {

    // ───────────────────────────── Grade 4 — fodder / swarm (band 1) ─────────────────────────────
    CURSE_WISP("curse_wisp", "Cursed Wisp", 1, 14, 3, 0.34, 0.6f, 0.0,
            ModelKind.GAUNT, 0.5f, 1.2f, ChatFormatting.GRAY, 1, false,
            EnumSet.of(Behavior.FAST_SWARM)),
    GRUDGE_LARVA("grudge_larva", "Grudge Larva", 1, 16, 3, 0.22, 0.55f, 0.0,
            ModelKind.SWARM_TINY, 0.5f, 0.8f, ChatFormatting.DARK_GREEN, 1, false,
            EnumSet.of(Behavior.POISON_AURA)),
    SHADOW_IMP("shadow_imp", "Shadow Imp", 1, 15, 4, 0.30, 0.7f, 0.0,
            ModelKind.GAUNT, 0.55f, 1.4f, ChatFormatting.DARK_GRAY, 1, false,
            EnumSet.of(Behavior.TELEPORT, Behavior.FAST_SWARM)),
    BILE_CRAWLER("bile_crawler", "Bile Crawler", 1, 18, 4, 0.26, 0.7f, 0.1,
            ModelKind.HULKING, 0.7f, 1.0f, ChatFormatting.DARK_GREEN, 1, false,
            EnumSet.of(Behavior.WITHER_TOUCH)),

    // ───────────────────────────── Grade 3 — common curses (band 2) ──────────────────────────────
    SLITMOUTH_CURSE("slitmouth_curse", "Slit-Mouth Curse", 2, 26, 6, 0.28, 1.0f, 0.1,
            ModelKind.HORNED, 0.7f, 2.3f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior.WITHER_TOUCH)),
    BRUTE_CURSE("brute_curse", "Brute Curse", 2, 34, 7, 0.24, 1.1f, 0.3,
            ModelKind.HULKING, 0.9f, 2.4f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior.MELEE_BRUISER)),
    SPITTER_CURSE("spitter_curse", "Spitter Curse", 2, 24, 5, 0.26, 0.95f, 0.1,
            ModelKind.GAUNT, 0.7f, 2.2f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior.RANGED_BLAST)),
    HEX_CURSE("hex_curse", "Hex Curse", 2, 26, 5, 0.27, 0.95f, 0.1,
            ModelKind.HORNED, 0.7f, 2.2f, ChatFormatting.LIGHT_PURPLE, 2, false,
            EnumSet.of(Behavior.FROST_AURA)),

    // ───────────────────────────── Grade 2 — dangerous curses (band 3) ───────────────────────────
    HORNED_CURSE("horned_curse", "Horned Curse", 3, 40, 9, 0.27, 1.25f, 0.35,
            ModelKind.HORNED, 0.9f, 2.8f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior.MELEE_BRUISER)),
    VEIL_CURSE("veil_curse", "Veil Curse", 3, 36, 8, 0.30, 1.1f, 0.2,
            ModelKind.GAUNT, 0.8f, 2.6f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior.TELEPORT)),
    PLAGUE_CURSE("plague_curse", "Plague Curse", 3, 42, 8, 0.25, 1.2f, 0.3,
            ModelKind.HULKING, 0.95f, 2.6f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior.POISON_AURA, Behavior.REGEN)),
    EMBER_CURSE("ember_curse", "Ember Curse", 3, 38, 9, 0.27, 1.1f, 0.3,
            ModelKind.HORNED, 0.85f, 2.6f, ChatFormatting.DARK_PURPLE, 3, false,
            EnumSet.of(Behavior.FIRE_BLAST, Behavior.FIRE_IMMUNE)),

    // ───────────────────────────── Grade 1 — calamity curses (band 4) ────────────────────────────
    FLAME_CALAMITY("flame_calamity", "Flame Calamity", 4, 70, 12, 0.27, 1.4f, 0.5,
            ModelKind.HORNED, 1.1f, 3.2f, ChatFormatting.RED, 4, false,
            EnumSet.of(Behavior.FIRE_BLAST, Behavior.FIRE_IMMUNE, Behavior.SHOCKWAVE)),
    FLORA_CALAMITY("flora_calamity", "Flora Calamity", 4, 80, 11, 0.24, 1.45f, 0.55,
            ModelKind.HULKING, 1.2f, 3.0f, ChatFormatting.RED, 4, false,
            EnumSet.of(Behavior.FROST_AURA, Behavior.REGEN, Behavior.SUMMONER)),
    TIDE_CALAMITY("tide_calamity", "Tide Calamity", 4, 78, 12, 0.26, 1.4f, 0.5,
            ModelKind.HULKING, 1.15f, 3.1f, ChatFormatting.RED, 4, false,
            EnumSet.of(Behavior.RANGED_BLAST, Behavior.SUMMONER)),
    TRANSFIGURED_SOUL("transfigured_soul", "Transfigured Soul", 4, 72, 13, 0.30, 1.3f, 0.45,
            ModelKind.GAUNT, 1.0f, 3.0f, ChatFormatting.RED, 4, false,
            EnumSet.of(Behavior.WITHER_TOUCH, Behavior.LIFE_DRAIN, Behavior.TELEPORT)),

    // ───────────────────────────── Special Grade — named bosses (band 5) ─────────────────────────
    FINGER_BEARER("finger_bearer", "Finger Bearer", 5, 220, 20, 0.28, 1.9f, 0.7,
            ModelKind.HORNED, 1.6f, 4.2f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior.MELEE_BRUISER, Behavior.SHOCKWAVE, Behavior.ENRAGE)),
    RUIN_SOVEREIGN("ruin_sovereign", "Ruin Sovereign", 5, 260, 18, 0.26, 2.1f, 0.8,
            ModelKind.HORNED, 1.8f, 4.6f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior.RANGED_BLAST, Behavior.SHOCKWAVE, Behavior.SUMMONER, Behavior.ENRAGE)),
    ROT_KING("rot_king", "Rot King", 5, 240, 16, 0.24, 2.0f, 0.8,
            ModelKind.HULKING, 1.7f, 4.4f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior.POISON_AURA, Behavior.SUMMONER, Behavior.REGEN, Behavior.SHOCKWAVE)),
    DISGRACED_SOUL("disgraced_soul", "Disgraced One", 5, 230, 19, 0.32, 1.7f, 0.6,
            ModelKind.GAUNT, 1.4f, 4.0f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior.WITHER_TOUCH, Behavior.LIFE_DRAIN, Behavior.TELEPORT, Behavior.ENRAGE)),
    CATACLYSM_CURSE("cataclysm_curse", "Cataclysm Curse", 5, 250, 18, 0.27, 2.0f, 0.8,
            ModelKind.HORNED, 1.7f, 4.4f, ChatFormatting.DARK_RED, 5, true,
            EnumSet.of(Behavior.FIRE_BLAST, Behavior.FIRE_IMMUNE, Behavior.SHOCKWAVE, Behavior.TELEPORT));

    /** Client model archetype a species renders with (all ride the humanoid rig). */
    public enum ModelKind { GAUNT, SWARM_TINY, HULKING, HORNED }

    private final String id;
    private final String displayName;
    private final int gradeBand;
    private final double baseHealth;
    private final double baseDamage;
    private final double moveSpeed;
    private final float baseScale;
    private final double knockbackResist;
    private final ModelKind modelKind;
    private final float hitboxWidth;
    private final float hitboxHeight;
    private final ChatFormatting nameColor;
    private final int lootTier;
    private final boolean boss;
    private final Set<Behavior> behaviors;

    SpiritSpecies(String id, String displayName, int gradeBand, double baseHealth, double baseDamage,
                  double moveSpeed, float baseScale, double knockbackResist, ModelKind modelKind,
                  float hitboxWidth, float hitboxHeight, ChatFormatting nameColor, int lootTier,
                  boolean boss, Set<Behavior> behaviors) {
        this.id = id;
        this.displayName = displayName;
        this.gradeBand = gradeBand;
        this.baseHealth = baseHealth;
        this.baseDamage = baseDamage;
        this.moveSpeed = moveSpeed;
        this.baseScale = baseScale;
        this.knockbackResist = knockbackResist;
        this.modelKind = modelKind;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.nameColor = nameColor;
        this.lootTier = lootTier;
        this.boss = boss;
        this.behaviors = behaviors;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public int gradeBand() { return gradeBand; }
    public double baseHealth() { return baseHealth; }
    public double baseDamage() { return baseDamage; }
    public double moveSpeed() { return moveSpeed; }
    public float baseScale() { return baseScale; }
    public double knockbackResist() { return knockbackResist; }
    public ModelKind modelKind() { return modelKind; }
    public float hitboxWidth() { return hitboxWidth; }
    public float hitboxHeight() { return hitboxHeight; }
    public ChatFormatting nameColor() { return nameColor; }
    public int lootTier() { return lootTier; }
    public boolean boss() { return boss; }

    public boolean has(Behavior b) { return behaviors.contains(b); }

    /** Lower-cased registry/command id -> species, or null. */
    public static SpiritSpecies byId(String id) {
        for (SpiritSpecies s : values()) if (s.id.equalsIgnoreCase(id)) return s;
        return null;
    }
}
