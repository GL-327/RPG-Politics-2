package com.political.expansion2.mobs;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.minecraft.world.item.ItemStack;

public final class MobSpec2 {

    public static final class Drop {
        public final float chance;
        public final Supplier<ItemStack> factory;

        Drop(float chance, Supplier<ItemStack> factory) {
            this.chance = chance;
            this.factory = factory;
        }
    }

    public final String id;
    public final String name;
    public final MobRole2 role;

    public double maxHealth = 20.0;
    public double attackDamage = 3.0;
    public double armor = 0.0;
    public double speed = 0.25;
    public double knockbackResist = 0.0;
    public double followRange = 32.0;
    public float scale = 1.0f;

    public float width = 0.6f;
    public float height = 1.95f;
    public boolean fireImmune = false;
    public MobCategory category = MobCategory.MONSTER;
    public boolean brute = false;

    public boolean attacksVillagers = false;
    public boolean ignites = false;
    public float lifesteal = 0.0f;
    public Holder<MobEffect> onHitEffect = null;
    public int onHitDuration = 100;
    public int onHitAmplifier = 0;
    public Holder<MobEffect> auraEffect = null;
    public int auraAmplifier = 0;
    public boolean callsLightning = false;

    public String summonAddId = null;
    public int summonAddCount = 0;

    public int coinMin = 0;
    public int coinMax = 0;
    public final List<Drop> drops = new ArrayList<>();

    public int spawnWeight = 0;
    public int minGroup = 1;
    public int maxGroup = 1;
    public Predicate<BiomeSelectionContext> biomeSelector = BiomeSelectors.foundInOverworld();

    public EntityType<ExpansionMob2> type;

    private MobSpec2(String id, String name, MobRole2 role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public static MobSpec2 of(String id, String name, MobRole2 role) {
        return new MobSpec2(id, name, role);
    }

    public MobSpec2 stats(double health, double damage, double armor, double speed) {
        this.maxHealth = health;
        this.attackDamage = damage;
        this.armor = armor;
        this.speed = speed;
        return this;
    }

    public MobSpec2 resist(double knockbackResist, double followRange) {
        this.knockbackResist = knockbackResist;
        this.followRange = followRange;
        return this;
    }

    public MobSpec2 size(float width, float height, float scale) {
        this.width = width;
        this.height = height;
        this.scale = scale;
        return this;
    }

    public MobSpec2 fireproof() {
        this.fireImmune = true;
        return this;
    }

    public MobSpec2 brute() {
        this.brute = true;
        return this;
    }

    public MobSpec2 raidsVillages() {
        this.attacksVillagers = true;
        return this;
    }

    public MobSpec2 ignites() {
        this.ignites = true;
        return this;
    }

    public MobSpec2 lifesteal(float amount) {
        this.lifesteal = amount;
        return this;
    }

    public MobSpec2 onHit(Holder<MobEffect> effect, int duration, int amplifier) {
        this.onHitEffect = effect;
        this.onHitDuration = duration;
        this.onHitAmplifier = amplifier;
        return this;
    }

    public MobSpec2 aura(Holder<MobEffect> effect, int amplifier) {
        this.auraEffect = effect;
        this.auraAmplifier = amplifier;
        return this;
    }

    public MobSpec2 lightning() {
        this.callsLightning = true;
        return this;
    }

    public MobSpec2 summons(String addId, int count) {
        this.summonAddId = addId;
        this.summonAddCount = count;
        return this;
    }

    public MobSpec2 coins(int min, int max) {
        this.coinMin = min;
        this.coinMax = max;
        return this;
    }

    public MobSpec2 drop(float chance, Supplier<ItemStack> factory) {
        this.drops.add(new Drop(chance, factory));
        return this;
    }

    public MobSpec2 spawn(Predicate<BiomeSelectionContext> selector, int weight, int minGroup, int maxGroup) {
        this.biomeSelector = selector;
        this.spawnWeight = weight;
        this.minGroup = minGroup;
        this.maxGroup = maxGroup;
        return this;
    }
}
