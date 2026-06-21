package com.political.expansion2.accessories;

import com.political.items.Rarity;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import java.util.ArrayList;
import java.util.List;

/** Definition of a single Phase-2 Skyblock-style accessory ({@code acc2_*}). */
public final class AccessoryDef2 {

    public enum Type {
        TALISMAN, RING, AMULET, CHARM, BAND, TOTEM, ARTIFACT, RELIC, BADGE, RUNE
    }

    public record EffectSpec(Holder<MobEffect> effect, int amplifier, String label) {}

    public static final class Bonus {
        public double health;
        public double defense;
        public double strength;
        public double toughness;
        public double knockbackResist;
        public double speed;
        public double attackSpeed;
        public double luck;
        public double critChance;
        public double critDamage;
        public double ferocity;
        public double manaRegen;
        public double cursedRegen;
        public final List<EffectSpec> effects = new ArrayList<>();

        public Bonus health(double v) { this.health = v; return this; }
        public Bonus defense(double v) { this.defense = v; return this; }
        public Bonus strength(double v) { this.strength = v; return this; }
        public Bonus toughness(double v) { this.toughness = v; return this; }
        public Bonus knockback(double v) { this.knockbackResist = v; return this; }
        public Bonus speed(double v) { this.speed = v; return this; }
        public Bonus attackSpeed(double v) { this.attackSpeed = v; return this; }
        public Bonus luck(double v) { this.luck = v; return this; }
        public Bonus critChance(double v) { this.critChance = v; return this; }
        public Bonus critDamage(double v) { this.critDamage = v; return this; }
        public Bonus ferocity(double v) { this.ferocity = v; return this; }
        public Bonus manaRegen(double v) { this.manaRegen = v; return this; }
        public Bonus cursedRegen(double v) { this.cursedRegen = v; return this; }

        public Bonus effect(Holder<MobEffect> effect, int amplifier, String label) {
            this.effects.add(new EffectSpec(effect, amplifier, label));
            return this;
        }
    }

    public final String id;
    public final String displayName;
    public final Type type;
    public final Rarity rarity;
    public final String flavor;
    public final Bonus bonus;

    public AccessoryDef2(String id, String displayName, Type type, Rarity rarity, String flavor, Bonus bonus) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.rarity = rarity;
        this.flavor = flavor;
        this.bonus = bonus;
    }

    public static Bonus bonus() {
        return new Bonus();
    }
}
