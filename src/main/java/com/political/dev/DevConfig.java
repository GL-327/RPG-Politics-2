package com.political.dev;

import com.political.politics.DataManager;
import com.political.politics.PoliticsData;

import java.util.ArrayList;
import java.util.List;

/** Server-side bridge between {@link DevConfigKey} and the live {@link PoliticsData}. */
public final class DevConfig {

    private DevConfig() {}

    public static List<Float> snapshot() {
        List<Float> out = new ArrayList<>();
        for (DevConfigKey k : DevConfigKey.values()) out.add(get(k));
        return out;
    }

    public static float get(DevConfigKey k) {
        PoliticsData d = DataManager.data();
        return switch (k) {
            case CURSE_SPAWNING -> d.curseSpawningEnabled ? 1 : 0;
            case ELECTIONS -> d.electionSystemEnabled ? 1 : 0;
            case TAX -> d.taxEnabled ? 1 : 0;
            case CURSE_SPAWN_CHANCE -> (float) d.curseNaturalSpawnChance;
            case CURSED_LOOT_CHANCE -> (float) d.cursedObjectLootChance;
            case DEATH_THRESHOLD -> d.deathCurseThreshold;
            case DEATH_CURSE_CHANCE -> (float) d.deathCurseChance;
            case ATTRACT_CHANCE -> (float) d.cursedObjectAttractChance;
            case MANA_REGEN -> (float) d.manaRegenRate;
            case POWER_COST_MULT -> (float) d.powerCostMultiplier;
            case TAX_PERCENT -> d.taxPercent;
            case SETTLEMENT_GEN -> d.settlementGenEnabled ? 1 : 0;
            case SETTLEMENT_GRID -> d.settlementGridChunks;
            case SETTLEMENT_CHANCE -> (float) d.settlementSpawnChance;
        };
    }

    public static void set(DevConfigKey k, float raw) {
        float v = k.clamp(raw);
        PoliticsData d = DataManager.data();
        switch (k) {
            case CURSE_SPAWNING -> d.curseSpawningEnabled = v >= 0.5f;
            case ELECTIONS -> d.electionSystemEnabled = v >= 0.5f;
            case TAX -> d.taxEnabled = v >= 0.5f;
            case CURSE_SPAWN_CHANCE -> d.curseNaturalSpawnChance = v;
            case CURSED_LOOT_CHANCE -> d.cursedObjectLootChance = v;
            case DEATH_THRESHOLD -> d.deathCurseThreshold = Math.round(v);
            case DEATH_CURSE_CHANCE -> d.deathCurseChance = v;
            case ATTRACT_CHANCE -> d.cursedObjectAttractChance = v;
            case MANA_REGEN -> d.manaRegenRate = v;
            case POWER_COST_MULT -> d.powerCostMultiplier = v;
            case TAX_PERCENT -> d.taxPercent = Math.round(v);
            case SETTLEMENT_GEN -> d.settlementGenEnabled = v >= 0.5f;
            case SETTLEMENT_GRID -> d.settlementGridChunks = Math.round(v);
            case SETTLEMENT_CHANCE -> d.settlementSpawnChance = v;
        }
    }
}
