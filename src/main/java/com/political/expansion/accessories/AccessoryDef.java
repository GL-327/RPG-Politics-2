package com.political.expansion.accessories;

import com.political.items.Rarity;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * Definition of a single Skyblock-style accessory (talisman / ring / artifact / relic).
 * Accessories grant their {@link Bonus} passively while the item simply sits in the
 * player's inventory — they never need to be equipped. The bonuses are applied by
 * {@link Accessories}' self-contained inventory-tick handler (vanilla attribute
 * modifiers + vanilla effects + StatManager resource trickle), so nothing in the
 * shared stat pipeline ({@code StatManager} / {@code ItemStats}) is edited.
 */
public final class AccessoryDef {

    /** Cosmetic classification used only for the tooltip footer label. */
    public enum Type {
        TALISMAN, RING, AMULET, CHARM, BAND, TOTEM, ARTIFACT, RELIC
    }

    /** A single passive {@link MobEffect} an accessory grants while carried. */
    public record EffectSpec(Holder<MobEffect> effect, int amplifier, String label) {}

    /**
     * The bundle of passive stats an accessory grants. Stat fields mirror the mod's
     * Skyblock stat vocabulary; they are translated to tangible vanilla attributes
     * (or resource trickle) at apply-time.
     */
    public static final class Bonus {
        public double health;          // -> MAX_HEALTH (flat)
        public double defense;         // -> ARMOR (scaled like StatManager: x0.15)
        public double strength;        // -> ATTACK_DAMAGE (x0.05) tangible melee
        public double toughness;       // -> ARMOR_TOUGHNESS (flat)
        public double knockbackResist; // -> KNOCKBACK_RESISTANCE (0..1)
        public double speed;           // -> MOVEMENT_SPEED (x0.005 multiplied base)
        public double attackSpeed;     // -> ATTACK_SPEED (x0.01 multiplied base)
        public double luck;            // -> LUCK (flat)
        public double critChance;      // tooltip only (informational)
        public double critDamage;      // tooltip only (informational)
        public double ferocity;        // tooltip only (informational)
        public double manaRegen;       // mana restored per second via StatManager
        public double cursedRegen;     // cursed energy restored per second via StatManager
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

    public final String id;            // registry path, e.g. "acc_warding_talisman"
    public final String displayName;
    public final Type type;
    public final Rarity rarity;
    public final String flavor;        // single-line lore description
    public final Bonus bonus;

    public AccessoryDef(String id, String displayName, Type type, Rarity rarity, String flavor, Bonus bonus) {
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
