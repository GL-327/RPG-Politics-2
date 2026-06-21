package com.political.expansion.mobs;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;

/**
 * Immutable-ish definition of one custom RPG creature. A single {@link ExpansionMob} class is
 * driven entirely by its spec, so adding a new creature is just adding a spec to
 * {@link ExpansionMobs}. The spec carries server data (stats, AI flags, drops, spawn rules) plus
 * a little client data (texture id + which body model to bake) so both source sets can read it.
 */
public final class MobSpec {

    /** A single weighted item drop. */
    public static final class Drop {
        public final float chance;
        public final Supplier<ItemStack> factory;
        Drop(float chance, Supplier<ItemStack> factory) {
            this.chance = chance;
            this.factory = factory;
        }
    }

    // identity
    public final String id;
    public final String name;
    public final MobRole role;

    // attributes
    public double maxHealth = 20.0;
    public double attackDamage = 3.0;
    public double armor = 0.0;
    public double speed = 0.25;
    public double knockbackResist = 0.0;
    public double followRange = 32.0;
    public float scale = 1.0f;

    // body / type
    public float width = 0.6f;
    public float height = 1.95f;
    public boolean fireImmune = false;
    public MobCategory category = MobCategory.MONSTER;
    public boolean brute = false; // bake the bulkier horned model on the client

    // ai flavour
    public boolean attacksVillagers = false;
    public boolean ignites = false;
    public float lifesteal = 0.0f;
    public Holder<MobEffect> onHitEffect = null;
    public int onHitDuration = 100;
    public int onHitAmplifier = 0;
    public Holder<MobEffect> auraEffect = null;
    public int auraAmplifier = 0;
    public boolean callsLightning = false;

    // boss-only
    public String summonAddId = null;  // id of the minion summoned on phase 2
    public int summonAddCount = 0;

    // drops + economy
    public int coinMin = 0;
    public int coinMax = 0;
    public final List<Drop> drops = new ArrayList<>();

    // natural spawn (weight <= 0 means "no natural spawn"; command/rare only)
    public int spawnWeight = 0;
    public int minGroup = 1;
    public int maxGroup = 1;
    public Predicate<BiomeSelectionContext> biomeSelector = BiomeSelectors.foundInOverworld();

    // filled in at registration time
    public EntityType<ExpansionMob> type;

    private MobSpec(String id, String name, MobRole role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public static MobSpec of(String id, String name, MobRole role) {
        return new MobSpec(id, name, role);
    }

    // ---- fluent builders -------------------------------------------------

    public MobSpec stats(double health, double damage, double armor, double speed) {
        this.maxHealth = health;
        this.attackDamage = damage;
        this.armor = armor;
        this.speed = speed;
        return this;
    }

    public MobSpec resist(double knockbackResist, double followRange) {
        this.knockbackResist = knockbackResist;
        this.followRange = followRange;
        return this;
    }

    public MobSpec size(float width, float height, float scale) {
        this.width = width;
        this.height = height;
        this.scale = scale;
        return this;
    }

    public MobSpec fireproof() {
        this.fireImmune = true;
        return this;
    }

    public MobSpec brute() {
        this.brute = true;
        return this;
    }

    public MobSpec raidsVillages() {
        this.attacksVillagers = true;
        return this;
    }

    public MobSpec ignites() {
        this.ignites = true;
        return this;
    }

    public MobSpec lifesteal(float amount) {
        this.lifesteal = amount;
        return this;
    }

    public MobSpec onHit(Holder<MobEffect> effect, int duration, int amplifier) {
        this.onHitEffect = effect;
        this.onHitDuration = duration;
        this.onHitAmplifier = amplifier;
        return this;
    }

    public MobSpec aura(Holder<MobEffect> effect, int amplifier) {
        this.auraEffect = effect;
        this.auraAmplifier = amplifier;
        return this;
    }

    public MobSpec lightning() {
        this.callsLightning = true;
        return this;
    }

    public MobSpec summons(String addId, int count) {
        this.summonAddId = addId;
        this.summonAddCount = count;
        return this;
    }

    public MobSpec coins(int min, int max) {
        this.coinMin = min;
        this.coinMax = max;
        return this;
    }

    public MobSpec drop(float chance, Supplier<ItemStack> factory) {
        this.drops.add(new Drop(chance, factory));
        return this;
    }

    public MobSpec spawn(Predicate<BiomeSelectionContext> selector, int weight, int minGroup, int maxGroup) {
        this.biomeSelector = selector;
        this.spawnWeight = weight;
        this.minGroup = minGroup;
        this.maxGroup = maxGroup;
        return this;
    }
}
