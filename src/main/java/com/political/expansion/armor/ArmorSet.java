package com.political.expansion.armor;

import com.political.items.Ability;
import com.political.items.Rarity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Self-contained armour-set catalogue for the {@code arm_} expansion.
 *
 * <p>Every set is four pieces (helmet / chestplate / leggings / boots). Pieces carry
 * <b>Skyblock custom_data stats only</b> ({@code rpg_health}, {@code rpg_defense}, …) exactly like
 * {@link com.political.items.RpgItem}; they never apply vanilla armour or toughness. Stat NBT is
 * read by {@link com.political.items.ItemStats}/{@link com.political.combat.StatManager}, and the
 * per-piece passive {@link Ability} list is applied by the existing
 * {@link com.political.combat.AbilityEngine} equipment tick (it scans every armour slot, regardless
 * of item id).
 *
 * <p>On top of that, each set grants a <b>full-set bonus</b> when all four pieces are worn together.
 * The bonus is applied by our own {@link ArmorSetBonusHandler} equipment tick (we do not touch the
 * shared {@code StatManager}/{@code AbilityEngine}); it reads the player's worn pieces and applies
 * the set's {@link SetEffect}.
 */
public enum ArmorSet {

    // ---------------------------------------------------------------- COMMON
    RECRUIT("recruit", "Recruit", Rarity.COMMON, Mat.LEATHER,
            "Basic Training", new String[]{"+Speed I and never go hungry."},
            p -> { Bonus.effect(p, MobEffects.SPEED, 0); Bonus.effect(p, MobEffects.SATURATION, 0); },
            piece(Slot.HELMET, "Recruit Helmet", 20, 10, 0, 0, 0),
            piece(Slot.CHEST, "Recruit Tunic", 35, 18, 0, 0, 0),
            piece(Slot.LEGS, "Recruit Leggings", 28, 14, 0, 0, 0),
            piece(Slot.BOOTS, "Recruit Boots", 20, 10, 0, 0, 2)),

    // ---------------------------------------------------------------- UNCOMMON
    GUARDIAN("guardian", "Guardian", Rarity.UNCOMMON, Mat.IRON,
            "Bulwark", new String[]{"+Resistance I and +Absorption I.", "Hold the line."},
            p -> { Bonus.effect(p, MobEffects.RESISTANCE, 0); Bonus.effect(p, MobEffects.ABSORPTION, 0); },
            piece(Slot.HELMET, "Guardian Helm", 35, 22, 0, 0, 0),
            piece(Slot.CHEST, "Guardian Cuirass", 60, 40, 0, 0, 0, Ability.RESISTANCE),
            piece(Slot.LEGS, "Guardian Greaves", 45, 30, 0, 0, 0),
            piece(Slot.BOOTS, "Guardian Sabatons", 30, 20, 0, 0, 0)),

    RANGER("ranger", "Ranger", Rarity.UNCOMMON, Mat.LEATHER,
            "Pathfinder", new String[]{"+Speed II and +Jump Boost II.", "Cover ground fast."},
            p -> { Bonus.effect(p, MobEffects.SPEED, 1); Bonus.effect(p, MobEffects.JUMP_BOOST, 1); },
            piece(Slot.HELMET, "Ranger Hood", 22, 10, 0, 10, 0, Ability.NIGHT_VISION),
            piece(Slot.CHEST, "Ranger Jerkin", 35, 16, 0, 15, 0),
            piece(Slot.LEGS, "Ranger Trousers", 28, 12, 0, 0, 3),
            piece(Slot.BOOTS, "Ranger Treads", 20, 8, 0, 0, 5, Ability.SPEED, Ability.FALL_IMMUNE)),

    // ---------------------------------------------------------------- RARE
    FROSTGUARD("frostguard", "Frostguard", Rarity.RARE, Mat.DIAMOND,
            "Permafrost", new String[]{"+Resistance I, +Water Breathing.", "Chills nearby foes (Slowness)."},
            p -> { Bonus.effect(p, MobEffects.RESISTANCE, 0); Bonus.effect(p, MobEffects.WATER_BREATHING, 0);
                   Bonus.afflictNearby(p, MobEffects.SLOWNESS, 5.0, 60, 1); },
            piece(Slot.HELMET, "Frostguard Helm", 45, 28, 0, 20, 0, Ability.WATER_BREATHING),
            piece(Slot.CHEST, "Frostguard Plate", 75, 50, 0, 25, 0),
            piece(Slot.LEGS, "Frostguard Greaves", 58, 38, 0, 15, 0),
            piece(Slot.BOOTS, "Frostguard Boots", 40, 24, 0, 10, 0, Ability.FALL_IMMUNE)),

    EMBERFORGE("emberforge", "Emberforge", Rarity.RARE, Mat.GOLD,
            "Inferno Heart", new String[]{"+Fire Resistance and +Strength I.", "Ignites nearby foes."},
            p -> { Bonus.effect(p, MobEffects.FIRE_RESISTANCE, 0); Bonus.effect(p, MobEffects.STRENGTH, 0);
                   Bonus.igniteNearby(p, 4.0, 80); },
            piece(Slot.HELMET, "Emberforge Helm", 40, 22, 20, 0, 0, Ability.FIRE_IMMUNE),
            piece(Slot.CHEST, "Emberforge Plate", 65, 35, 30, 0, 0, Ability.FIRE_AURA, Ability.FIRE_IMMUNE),
            piece(Slot.LEGS, "Emberforge Greaves", 50, 28, 22, 0, 0),
            piece(Slot.BOOTS, "Emberforge Boots", 35, 18, 15, 0, 0, Ability.FIRE_IMMUNE)),

    // ---------------------------------------------------------------- EPIC
    TEMPEST("tempest", "Tempest", Rarity.EPIC, Mat.DIAMOND,
            "Eye of the Storm", new String[]{"+Speed II, +Jump Boost III.", "+Haste I and Slow Falling."},
            p -> { Bonus.effect(p, MobEffects.SPEED, 1); Bonus.effect(p, MobEffects.JUMP_BOOST, 2);
                   Bonus.effect(p, MobEffects.HASTE, 0); Bonus.effect(p, MobEffects.SLOW_FALLING, 0); },
            piece(Slot.HELMET, "Tempest Visor", 45, 24, 0, 25, 5),
            piece(Slot.CHEST, "Tempest Mantle", 70, 40, 0, 30, 8),
            piece(Slot.LEGS, "Tempest Greaves", 55, 30, 0, 0, 10, Ability.SPEED),
            piece(Slot.BOOTS, "Tempest Striders", 40, 20, 0, 0, 14, Ability.SPEED, Ability.FALL_IMMUNE)),

    VERDANT("verdant", "Verdant", Rarity.EPIC, Mat.GOLD,
            "Wild Growth", new String[]{"+Regeneration II and +Saturation.", "+Health Boost II."},
            p -> { Bonus.effect(p, MobEffects.REGENERATION, 1); Bonus.effect(p, MobEffects.SATURATION, 0);
                   Bonus.effect(p, MobEffects.HEALTH_BOOST, 1); },
            piece(Slot.HELMET, "Verdant Crown", 60, 26, 0, 20, 0, Ability.REGEN),
            piece(Slot.CHEST, "Verdant Robe", 100, 44, 0, 25, 0, Ability.REGEN),
            piece(Slot.LEGS, "Verdant Leggings", 78, 34, 0, 18, 0),
            piece(Slot.BOOTS, "Verdant Sandals", 52, 22, 0, 12, 0, Ability.WATER_BREATHING)),

    ABYSSAL("abyssal", "Abyssal", Rarity.EPIC, Mat.DIAMOND,
            "Tides of the Deep", new String[]{"+Water Breathing and +Night Vision.", "+Regeneration I and +Resistance I."},
            p -> { Bonus.effect(p, MobEffects.WATER_BREATHING, 0); Bonus.effect(p, MobEffects.NIGHT_VISION, 0);
                   Bonus.effect(p, MobEffects.REGENERATION, 0); Bonus.effect(p, MobEffects.RESISTANCE, 0); },
            piece(Slot.HELMET, "Abyssal Helm", 45, 24, 0, 40, 0, Ability.WATER_BREATHING, Ability.NIGHT_VISION),
            piece(Slot.CHEST, "Abyssal Mantle", 70, 40, 0, 55, 0, Ability.WATER_BREATHING),
            piece(Slot.LEGS, "Abyssal Greaves", 55, 30, 0, 40, 0),
            piece(Slot.BOOTS, "Abyssal Boots", 40, 22, 0, 30, 0, Ability.WATER_BREATHING)),

    SHADOWSTALKER("shadowstalker", "Shadowstalker", Rarity.EPIC, Mat.NETHERITE,
            "Night's Veil", new String[]{"+Speed II, +Strength I.", "+Jump Boost II and Night Vision."},
            p -> { Bonus.effect(p, MobEffects.SPEED, 1); Bonus.effect(p, MobEffects.STRENGTH, 0);
                   Bonus.effect(p, MobEffects.JUMP_BOOST, 1); Bonus.effect(p, MobEffects.NIGHT_VISION, 0); },
            piece(Slot.HELMET, "Shadowstalker Hood", 45, 22, 20, 0, 6, 5, 15, 0, Ability.NIGHT_VISION),
            piece(Slot.CHEST, "Shadowstalker Cloak", 70, 38, 30, 0, 8, 8, 25, 0),
            piece(Slot.LEGS, "Shadowstalker Leggings", 55, 28, 22, 0, 10, 6, 15, 0),
            piece(Slot.BOOTS, "Shadowstalker Boots", 40, 18, 15, 0, 14, 0, 0, 0, Ability.SPEED)),

    // ---------------------------------------------------------------- LEGENDARY
    BLOODMOON("bloodmoon", "Bloodmoon", Rarity.LEGENDARY, Mat.NETHERITE,
            "Crimson Feast", new String[]{"+Strength II, +Absorption II.", "+Regeneration I and steady lifedraw."},
            p -> { Bonus.effect(p, MobEffects.STRENGTH, 1); Bonus.effect(p, MobEffects.ABSORPTION, 1);
                   Bonus.effect(p, MobEffects.REGENERATION, 0); Bonus.heal(p, 2.0f); },
            piece(Slot.HELMET, "Bloodmoon Helm", 55, 26, 35, 0, 0, 5, 20, 10),
            piece(Slot.CHEST, "Bloodmoon Plate", 95, 48, 55, 0, 0, 8, 30, 15, Ability.ABSORPTION),
            piece(Slot.LEGS, "Bloodmoon Greaves", 72, 36, 40, 0, 0, 6, 20, 10),
            piece(Slot.BOOTS, "Bloodmoon Boots", 50, 24, 28, 0, 6, 0, 0, 8)),

    SOLARIS("solaris", "Solaris", Rarity.LEGENDARY, Mat.GOLD,
            "Solar Blessing", new String[]{"+Regeneration II, +Resistance I.", "+Fire Resistance, +Health Boost II, +Saturation."},
            p -> { Bonus.effect(p, MobEffects.REGENERATION, 1); Bonus.effect(p, MobEffects.RESISTANCE, 0);
                   Bonus.effect(p, MobEffects.FIRE_RESISTANCE, 0); Bonus.effect(p, MobEffects.HEALTH_BOOST, 1);
                   Bonus.effect(p, MobEffects.SATURATION, 0); },
            piece(Slot.HELMET, "Solaris Diadem", 65, 34, 0, 30, 0, Ability.NIGHT_VISION, Ability.FIRE_IMMUNE),
            piece(Slot.CHEST, "Solaris Vestments", 110, 58, 0, 40, 0, Ability.REGEN, Ability.ABSORPTION),
            piece(Slot.LEGS, "Solaris Leggings", 82, 44, 0, 28, 0),
            piece(Slot.BOOTS, "Solaris Sandals", 56, 30, 0, 20, 0, Ability.FALL_IMMUNE)),

    WRAITH("wraith", "Wraith", Rarity.LEGENDARY, Mat.NETHERITE,
            "Curse of the Wraith", new String[]{"+Resistance II, +Strength II.", "Night Vision; withers nearby foes."},
            p -> { Bonus.effect(p, MobEffects.RESISTANCE, 1); Bonus.effect(p, MobEffects.STRENGTH, 1);
                   Bonus.effect(p, MobEffects.NIGHT_VISION, 0); Bonus.afflictNearby(p, MobEffects.WITHER, 4.0, 60, 0); },
            piece(Slot.HELMET, "Wraith Crown", 58, 30, 40, 0, 0, Ability.NIGHT_VISION),
            piece(Slot.CHEST, "Wraith Shroud", 98, 52, 60, 0, 0, Ability.RESISTANCE),
            piece(Slot.LEGS, "Wraith Greaves", 74, 40, 44, 0, 0),
            piece(Slot.BOOTS, "Wraith Boots", 52, 28, 30, 0, 0, Ability.FALL_IMMUNE)),

    // ---------------------------------------------------------------- MYTHIC
    TITANFORGE("titanforge", "Titanforge", Rarity.MYTHIC, Mat.NETHERITE,
            "Unbreakable", new String[]{"+Resistance II, +Absorption IV.", "+Health Boost IV and +Fire Resistance."},
            p -> { Bonus.effect(p, MobEffects.RESISTANCE, 1); Bonus.effect(p, MobEffects.ABSORPTION, 3);
                   Bonus.effect(p, MobEffects.HEALTH_BOOST, 3); Bonus.effect(p, MobEffects.FIRE_RESISTANCE, 0); },
            piece(Slot.HELMET, "Titanforge Helm", 90, 55, 0, 0, 0),
            piece(Slot.CHEST, "Titanforge Aegis", 160, 95, 0, 0, 0, Ability.RESISTANCE, Ability.ABSORPTION),
            piece(Slot.LEGS, "Titanforge Greaves", 120, 70, 0, 0, 0),
            piece(Slot.BOOTS, "Titanforge Boots", 80, 48, 0, 0, 0, Ability.FIRE_IMMUNE)),

    CELESTIAL("celestial", "Celestial", Rarity.MYTHIC, Mat.NETHERITE,
            "Ascendant", new String[]{"+Resistance II, +Strength II, +Speed II.", "+Regeneration III, +Health Boost IV.",
                    "+Fire Resistance and Night Vision."},
            p -> { Bonus.effect(p, MobEffects.RESISTANCE, 1); Bonus.effect(p, MobEffects.STRENGTH, 1);
                   Bonus.effect(p, MobEffects.SPEED, 1); Bonus.effect(p, MobEffects.REGENERATION, 2);
                   Bonus.effect(p, MobEffects.HEALTH_BOOST, 3); Bonus.effect(p, MobEffects.FIRE_RESISTANCE, 0);
                   Bonus.effect(p, MobEffects.NIGHT_VISION, 0); },
            piece(Slot.HELMET, "Celestial Crown", 95, 50, 40, 40, 0, Ability.NIGHT_VISION),
            piece(Slot.CHEST, "Celestial Aegis", 170, 85, 60, 60, 0, Ability.FLIGHT, Ability.FIRE_IMMUNE),
            piece(Slot.LEGS, "Celestial Greaves", 130, 65, 45, 45, 0, Ability.RESISTANCE),
            piece(Slot.BOOTS, "Celestial Striders", 90, 45, 30, 30, 12, Ability.SPEED, Ability.FALL_IMMUNE));

    // ------------------------------------------------------------------
    // Structure
    // ------------------------------------------------------------------

    /** Applies a set's full-set bonus to a player (once per equipment tick while fully worn). */
    @FunctionalInterface
    public interface SetEffect {
        void apply(net.minecraft.server.level.ServerPlayer player);
    }

    public enum Slot {
        HELMET("helmet", "Helmet", EquipmentSlot.HEAD),
        CHEST("chestplate", "Chestplate", EquipmentSlot.CHEST),
        LEGS("leggings", "Leggings", EquipmentSlot.LEGS),
        BOOTS("boots", "Boots", EquipmentSlot.FEET);

        public final String idSuffix;
        public final String word;
        public final EquipmentSlot vanilla;

        Slot(String idSuffix, String word, EquipmentSlot vanilla) {
            this.idSuffix = idSuffix;
            this.word = word;
            this.vanilla = vanilla;
        }
    }

    /** A single armour piece: Skyblock stats + passive abilities. No vanilla armour values. */
    public static final class Piece {
        public final Slot slot;
        public final String displayName;
        public final int health, defense, strength, intelligence, speed;
        public final int critChance, critDamage, ferocity;
        public final Ability[] abilities;

        Piece(Slot slot, String displayName, int health, int defense, int strength, int intelligence, int speed,
              int critChance, int critDamage, int ferocity, Ability[] abilities) {
            this.slot = slot;
            this.displayName = displayName;
            this.health = health;
            this.defense = defense;
            this.strength = strength;
            this.intelligence = intelligence;
            this.speed = speed;
            this.critChance = critChance;
            this.critDamage = critDamage;
            this.ferocity = ferocity;
            this.abilities = abilities;
        }
    }

    /** Vanilla base item per slot (determines equip slot + worn armour layer; stats are NBT). */
    public enum Mat {
        LEATHER(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS),
        IRON(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS),
        GOLD(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS),
        DIAMOND(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS),
        NETHERITE(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);

        final Item helmet, chest, legs, boots;

        Mat(Item helmet, Item chest, Item legs, Item boots) {
            this.helmet = helmet;
            this.chest = chest;
            this.legs = legs;
            this.boots = boots;
        }

        Item itemFor(Slot slot) {
            return switch (slot) {
                case HELMET -> helmet;
                case CHEST -> chest;
                case LEGS -> legs;
                case BOOTS -> boots;
            };
        }
    }

    public final String key;
    public final String displayName;
    public final Rarity rarity;
    public final Mat material;
    public final String bonusName;
    public final String[] bonusLines;
    public final SetEffect bonus;
    public final Piece helmet, chest, legs, boots;

    ArmorSet(String key, String displayName, Rarity rarity, Mat material,
             String bonusName, String[] bonusLines, SetEffect bonus,
             Piece helmet, Piece chest, Piece legs, Piece boots) {
        this.key = key;
        this.displayName = displayName;
        this.rarity = rarity;
        this.material = material;
        this.bonusName = bonusName;
        this.bonusLines = bonusLines;
        this.bonus = bonus;
        this.helmet = helmet;
        this.chest = chest;
        this.legs = legs;
        this.boots = boots;
    }

    public Piece[] pieces() {
        return new Piece[]{helmet, chest, legs, boots};
    }

    public Piece pieceFor(Slot slot) {
        return switch (slot) {
            case HELMET -> helmet;
            case CHEST -> chest;
            case LEGS -> legs;
            case BOOTS -> boots;
        };
    }

    public Item baseItem(Slot slot) {
        return material.itemFor(slot);
    }

    /** {@code arm_<set>_<slot>}; e.g. {@code arm_celestial_chestplate}. */
    public String pieceId(Slot slot) {
        return "arm_" + key + "_" + slot.idSuffix;
    }

    /** Resolves the owning set + slot from a piece id, or {@code null} if it isn't ours. */
    public static ArmorSet byId(String id) {
        if (id == null || !id.startsWith("arm_")) return null;
        for (ArmorSet set : values()) {
            for (Slot slot : Slot.values()) {
                if (set.pieceId(slot).equals(id)) return set;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Piece factories (keep enum constants compact)
    // ------------------------------------------------------------------

    static Piece piece(Slot slot, String name, int health, int defense, int strength, int intelligence, int speed,
                       Ability... abilities) {
        return new Piece(slot, name, health, defense, strength, intelligence, speed, 0, 0, 0,
                abilities == null ? new Ability[0] : abilities);
    }

    static Piece piece(Slot slot, String name, int health, int defense, int strength, int intelligence, int speed,
                       int critChance, int critDamage, int ferocity, Ability... abilities) {
        return new Piece(slot, name, health, defense, strength, intelligence, speed, critChance, critDamage, ferocity,
                abilities == null ? new Ability[0] : abilities);
    }
}
