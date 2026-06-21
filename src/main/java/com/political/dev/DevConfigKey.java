package com.political.dev;

/**
 * The schema for the Developer Menu. Shared by client (labels/widget type) and server
 * (reading/writing the live {@link com.political.politics.PoliticsData}). Values travel as
 * floats in ordinal order; booleans use 0/1.
 */
public enum DevConfigKey {
    CURSE_SPAWNING("Curse Spawning", true, 1, 0, 1),
    ELECTIONS("Election System", true, 1, 0, 1),
    TAX("Taxation", true, 1, 0, 1),
    CURSE_SPAWN_CHANCE("Curse Spawn Chance", false, 0.01f, 0f, 0.5f),
    CURSED_LOOT_CHANCE("Cursed Loot Chance", false, 0.005f, 0f, 0.2f),
    DEATH_THRESHOLD("Death-Curse Threshold", false, 1f, 1f, 200f),
    DEATH_CURSE_CHANCE("Death-Curse Chance", false, 0.01f, 0f, 1f),
    ATTRACT_CHANCE("Cursed Lure Chance", false, 0.01f, 0f, 1f),
    MANA_REGEN("Mana Regen Rate", false, 0.005f, 0f, 0.5f),
    POWER_COST_MULT("Power Cost x", false, 0.1f, 0f, 3f),
    TAX_PERCENT("Tax Percent", false, 1f, 0f, 50f),
    SETTLEMENT_GEN("Settlement Scatter", true, 1, 0, 1),
    SETTLEMENT_GRID("Settlement Grid (chunks)", false, 4f, 16f, 256f),
    SETTLEMENT_CHANCE("Settlement Spawn Chance", false, 0.05f, 0f, 1f);

    public final String label;
    public final boolean isBool;
    public final float step;
    public final float min;
    public final float max;

    DevConfigKey(String label, boolean isBool, float step, float min, float max) {
        this.label = label;
        this.isBool = isBool;
        this.step = step;
        this.min = min;
        this.max = max;
    }

    public float clamp(float v) {
        return Math.max(min, Math.min(max, v));
    }

    public static DevConfigKey byName(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
