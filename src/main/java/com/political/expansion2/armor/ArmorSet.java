package com.political.expansion2.armor;

import com.political.items.Ability;
import com.political.items.Rarity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Self-contained armour-set catalogue for the {@code arm2_} expansion (Phase 2).
 *
 * <p>Every set is four pieces. Pieces carry Skyblock {@code custom_data} stats only; full-set
 * bonuses are applied by {@link Armor2SetBonusHandler}.
 */
public enum ArmorSet {

    PYROFIRE("pyrofire", "Pyrofire", Rarity.COMMON, Mat.LEATHER,
            "Ember Skin", new String[]{"+Fire Resistance and +Speed I.", "Warmth in any biome."},
            p -> { Bonus.effect(p, MobEffects.FIRE_RESISTANCE, 0); Bonus.effect(p, MobEffects.SPEED, 0); },
            piece(Slot.HELMET, "Pyrofire Helmet", 18, 8, 8, 0, 0),
            piece(Slot.CHEST, "Pyrofire Chestplate", 30, 14, 12, 0, 0, Ability.FIRE_IMMUNE),
            piece(Slot.LEGS, "Pyrofire Leggings", 24, 11, 10, 0, 0),
            piece(Slot.BOOTS, "Pyrofire Boots", 18, 8, 6, 0, 2)),

    HYDROFLOW("hydroflow", "Hydroflow", Rarity.COMMON, Mat.LEATHER,
            "Tidal Grace", new String[]{"+Water Breathing and +Regeneration I."},
            p -> { Bonus.effect(p, MobEffects.WATER_BREATHING, 0); Bonus.effect(p, MobEffects.REGENERATION, 0); },
            piece(Slot.HELMET, "Hydroflow Helmet", 18, 8, 0, 10, 0, Ability.WATER_BREATHING),
            piece(Slot.CHEST, "Hydroflow Chestplate", 30, 14, 0, 14, 0),
            piece(Slot.LEGS, "Hydroflow Leggings", 24, 11, 0, 12, 0),
            piece(Slot.BOOTS, "Hydroflow Boots", 18, 8, 0, 8, 0, Ability.WATER_BREATHING)),

    GEOSTONE("geostone", "Geostone", Rarity.COMMON, Mat.IRON,
            "Bedrock Stance", new String[]{"+Resistance I and +Slowness immunity feel."},
            p -> { Bonus.effect(p, MobEffects.RESISTANCE, 0); Bonus.effect(p, MobEffects.HEALTH_BOOST, 1); },
            piece(Slot.HELMET, "Geostone Helmet", 22, 14, 0, 0, 0),
            piece(Slot.CHEST, "Geostone Chestplate", 38, 22, 0, 0, 0, Ability.RESISTANCE),
            piece(Slot.LEGS, "Geostone Leggings", 30, 18, 0, 0, 0),
            piece(Slot.BOOTS, "Geostone Boots", 22, 12, 0, 0, 0)),

    AEROWIND("aerowind", "Aerowind", Rarity.UNCOMMON, Mat.LEATHER,
            "Gale Step", new String[]{"+Speed II and +Jump Boost I.", "Feather-light landings."},
            p -> { Bonus.effect(p, MobEffects.SPEED, 1); Bonus.effect(p, MobEffects.JUMP_BOOST, 1); Bonus.effect(p, MobEffects.SLOW_FALLING, 0); },
            piece(Slot.HELMET, "Aerowind Helmet", 24, 10, 0, 8, 4),
            piece(Slot.CHEST, "Aerowind Chestplate", 38, 16, 0, 12, 6),
            piece(Slot.LEGS, "Aerowind Leggings", 30, 13, 0, 0, 8, Ability.SPEED),
            piece(Slot.BOOTS, "Aerowind Boots", 22, 10, 0, 0, 10, Ability.SPEED, Ability.FALL_IMMUNE)),

    ELECTROVOLT("electrovolt", "Electrovolt", Rarity.UNCOMMON, Mat.IRON,
            "Static Field", new String[]{"+Haste I and +Speed I.", "Crackle with energy."},
            p -> { Bonus.effect(p, MobEffects.HASTE, 0); Bonus.effect(p, MobEffects.SPEED, 0); },
            piece(Slot.HELMET, "Electrovolt Helmet", 26, 12, 10, 8, 0),
            piece(Slot.CHEST, "Electrovolt Chestplate", 42, 18, 14, 12, 0, Ability.HASTE),
            piece(Slot.LEGS, "Electrovolt Leggings", 32, 15, 12, 0, 2),
            piece(Slot.BOOTS, "Electrovolt Boots", 24, 11, 8, 0, 4)),

    CRYOFROST("cryofrost", "Cryofrost", Rarity.UNCOMMON, Mat.IRON,
            "Permafrost Veil", new String[]{"+Resistance I and slows nearby foes."},
            p -> { Bonus.effect(p, MobEffects.RESISTANCE, 0); Bonus.afflictNearby(p, MobEffects.SLOWNESS, 5.0, 60, 1); },
            piece(Slot.HELMET, "Cryofrost Helmet", 26, 14, 0, 12, 0, Ability.WATER_BREATHING),
            piece(Slot.CHEST, "Cryofrost Chestplate", 42, 20, 0, 16, 0),
            piece(Slot.LEGS, "Cryofrost Leggings", 32, 16, 0, 12, 0),
            piece(Slot.BOOTS, "Cryofrost Boots", 24, 12, 0, 8, 0, Ability.FALL_IMMUNE)),

    VERDANTLIFE("verdantlife", "Verdantlife", Rarity.UNCOMMON, Mat.LEATHER,
            "Photosynthesis", new String[]{"+Regeneration I and +Saturation."},
            p -> { Bonus.effect(p, MobEffects.REGENERATION, 0); Bonus.effect(p, MobEffects.SATURATION, 0); },
            piece(Slot.HELMET, "Verdantlife Helmet", 24, 10, 0, 10, 0, Ability.REGEN),
            piece(Slot.CHEST, "Verdantlife Chestplate", 38, 16, 0, 14, 0, Ability.REGEN),
            piece(Slot.LEGS, "Verdantlife Leggings", 30, 13, 0, 12, 0),
            piece(Slot.BOOTS, "Verdantlife Boots", 22, 10, 0, 8, 0)),

    UMBRALSHADOW("umbralshadow", "Umbralshadow", Rarity.RARE, Mat.LEATHER,
            "Veil of Night", new String[]{"+Night Vision, +Speed I, +Invisibility pulse."},
            p -> { Bonus.effect(p, MobEffects.NIGHT_VISION, 0); Bonus.effect(p, MobEffects.SPEED, 0); Bonus.effect(p, MobEffects.INVISIBILITY, 0); },
            piece(Slot.HELMET, "Umbralshadow Helmet", 40, 18, 12, 0, 5, 8, 0, 0, Ability.NIGHT_VISION),
            piece(Slot.CHEST, "Umbralshadow Chestplate", 62, 28, 18, 0, 6, 10, 15, 0),
            piece(Slot.LEGS, "Umbralshadow Leggings", 48, 22, 14, 0, 8, 6, 10, 0),
            piece(Slot.BOOTS, "Umbralshadow Boots", 36, 16, 10, 0, 12, Ability.SPEED)),

    RADIANTLIGHT("radiantlight", "Radiantlight", Rarity.RARE, Mat.GOLD,
            "Solar Ward", new String[]{"+Regeneration I, +Resistance I, +Fire Resistance."},
            p -> { Bonus.effect(p, MobEffects.REGENERATION, 0); Bonus.effect(p, MobEffects.RESISTANCE, 0); Bonus.effect(p, MobEffects.FIRE_RESISTANCE, 0); },
            piece(Slot.HELMET, "Radiantlight Helmet", 42, 20, 0, 18, 0, Ability.NIGHT_VISION),
            piece(Slot.CHEST, "Radiantlight Chestplate", 68, 32, 0, 24, 0, Ability.REGEN),
            piece(Slot.LEGS, "Radiantlight Leggings", 52, 24, 0, 18, 0),
            piece(Slot.BOOTS, "Radiantlight Boots", 38, 18, 0, 14, 0, Ability.FIRE_IMMUNE)),

    GRADE4("grade4", "Grade IV", Rarity.RARE, Mat.LEATHER,
            "Fourth Grade", new String[]{"+Speed I and +Jump Boost I.", "Entry-level sorcerer kit."},
            p -> { Bonus.effect(p, MobEffects.SPEED, 0); Bonus.effect(p, MobEffects.JUMP_BOOST, 1); },
            piece(Slot.HELMET, "Grade IV Helmet", 38, 16, 8, 14, 0),
            piece(Slot.CHEST, "Grade IV Chestplate", 58, 26, 12, 20, 0),
            piece(Slot.LEGS, "Grade IV Leggings", 46, 20, 10, 16, 0),
            piece(Slot.BOOTS, "Grade IV Boots", 34, 14, 6, 12, 3)),

    GRADE3("grade3", "Grade III", Rarity.EPIC, Mat.IRON,
            "Semi-Grade", new String[]{"+Strength I, +Resistance I, +Haste I."},
            p -> { Bonus.effect(p, MobEffects.STRENGTH, 0); Bonus.effect(p, MobEffects.RESISTANCE, 0); Bonus.effect(p, MobEffects.HASTE, 0); },
            piece(Slot.HELMET, "Grade III Helmet", 48, 24, 18, 20, 0),
            piece(Slot.CHEST, "Grade III Chestplate", 72, 38, 28, 28, 0, Ability.RESISTANCE),
            piece(Slot.LEGS, "Grade III Leggings", 56, 30, 22, 22, 0),
            piece(Slot.BOOTS, "Grade III Boots", 42, 20, 16, 16, 4)),

    GRADE2("grade2", "Grade II", Rarity.LEGENDARY, Mat.DIAMOND,
            "Grade 1 Sorcerer", new String[]{"+Strength II, +Speed I, +Regeneration I."},
            p -> { Bonus.effect(p, MobEffects.STRENGTH, 1); Bonus.effect(p, MobEffects.SPEED, 0); Bonus.effect(p, MobEffects.REGENERATION, 0); },
            piece(Slot.HELMET, "Grade II Helmet", 58, 28, 32, 30, 0, 6, 18, 8),
            piece(Slot.CHEST, "Grade II Chestplate", 92, 46, 50, 42, 0, 8, 28, 12, Ability.RESISTANCE),
            piece(Slot.LEGS, "Grade II Leggings", 70, 34, 38, 34, 0, 6, 20, 10),
            piece(Slot.BOOTS, "Grade II Boots", 50, 24, 28, 24, 6, 0, 0, 8)),

    GRADE1("grade1", "Grade I", Rarity.LEGENDARY, Mat.DIAMOND,
            "Special Grade", new String[]{"+Strength II, +Resistance I, +Absorption II."},
            p -> { Bonus.effect(p, MobEffects.STRENGTH, 1); Bonus.effect(p, MobEffects.RESISTANCE, 1); Bonus.effect(p, MobEffects.ABSORPTION, 1); },
            piece(Slot.HELMET, "Grade I Helmet", 62, 30, 36, 35, 0, 8, 22, 10),
            piece(Slot.CHEST, "Grade I Chestplate", 98, 50, 55, 48, 0, 10, 32, 15, Ability.ABSORPTION),
            piece(Slot.LEGS, "Grade I Leggings", 74, 36, 42, 38, 0, 8, 24, 12),
            piece(Slot.BOOTS, "Grade I Boots", 52, 26, 30, 28, 8, 0, 0, 10)),

    SPECIAL_GRADE("special_grade", "Special Grade", Rarity.MYTHIC, Mat.NETHERITE,
            "Domain Expansion", new String[]{"+Strength II, +Speed II, +Regeneration III.", "+Resistance II, +Absorption III, +Night Vision."},
            p -> { Bonus.effect(p, MobEffects.STRENGTH, 1); Bonus.effect(p, MobEffects.SPEED, 1); Bonus.effect(p, MobEffects.REGENERATION, 2); Bonus.effect(p, MobEffects.RESISTANCE, 1); Bonus.effect(p, MobEffects.ABSORPTION, 3); Bonus.effect(p, MobEffects.NIGHT_VISION, 0); },
            piece(Slot.HELMET, "Special Grade Helmet", 88, 48, 45, 45, 0, 10, 30, 15, Ability.NIGHT_VISION),
            piece(Slot.CHEST, "Special Grade Chestplate", 150, 82, 65, 65, 0, 12, 40, 20, Ability.FLIGHT, Ability.RESISTANCE),
            piece(Slot.LEGS, "Special Grade Leggings", 115, 62, 50, 50, 0, 10, 30, 15),
            piece(Slot.BOOTS, "Special Grade Boots", 80, 42, 35, 35, 14, 0, 0, 12, Ability.SPEED, Ability.FALL_IMMUNE)),

    VILTRUMITE("viltrumite", "Viltrumite", Rarity.LEGENDARY, Mat.NETHERITE,
            "Conqueror\'s Might", new String[]{"+Strength II, +Resistance II, +Speed II.", "+Regeneration II and lifedraw."},
            p -> { Bonus.effect(p, MobEffects.STRENGTH, 1); Bonus.effect(p, MobEffects.RESISTANCE, 1); Bonus.effect(p, MobEffects.SPEED, 1); Bonus.effect(p, MobEffects.REGENERATION, 1); Bonus.heal(p, 2.0f); },
            piece(Slot.HELMET, "Viltrumite Helmet", 65, 32, 42, 0, 8, 8, 25, 12),
            piece(Slot.CHEST, "Viltrumite Chestplate", 105, 52, 62, 0, 10, 10, 35, 18, Ability.RESISTANCE),
            piece(Slot.LEGS, "Viltrumite Leggings", 78, 38, 48, 0, 12, 8, 25, 12),
            piece(Slot.BOOTS, "Viltrumite Boots", 55, 28, 35, 0, 16, 0, 0, 10, Ability.SPEED, Ability.FALL_IMMUNE)),

    HERO("hero", "Hero", Rarity.EPIC, Mat.DIAMOND,
            "Inspiring Presence", new String[]{"+Resistance I, +Regeneration I, +Absorption I."},
            p -> { Bonus.effect(p, MobEffects.RESISTANCE, 0); Bonus.effect(p, MobEffects.REGENERATION, 0); Bonus.effect(p, MobEffects.ABSORPTION, 0); },
            piece(Slot.HELMET, "Hero Helmet", 50, 26, 20, 15, 0, Ability.NIGHT_VISION),
            piece(Slot.CHEST, "Hero Chestplate", 78, 42, 30, 22, 0, Ability.REGEN, Ability.ABSORPTION),
            piece(Slot.LEGS, "Hero Leggings", 60, 32, 24, 18, 0),
            piece(Slot.BOOTS, "Hero Boots", 44, 22, 18, 12, 4, Ability.FALL_IMMUNE)),

    VILLAIN("villain", "Villain", Rarity.EPIC, Mat.NETHERITE,
            "Malice Aura", new String[]{"+Strength I, +Speed I.", "Weakens nearby foes."},
            p -> { Bonus.effect(p, MobEffects.STRENGTH, 0); Bonus.effect(p, MobEffects.SPEED, 0); Bonus.afflictNearby(p, MobEffects.WEAKNESS, 5.0, 60, 0); },
            piece(Slot.HELMET, "Villain Helmet", 48, 22, 22, 0, 6, 6, 18, 8, Ability.NIGHT_VISION),
            piece(Slot.CHEST, "Villain Chestplate", 74, 36, 32, 0, 8, 8, 25, 12),
            piece(Slot.LEGS, "Villain Leggings", 58, 28, 26, 0, 10, 6, 18, 8),
            piece(Slot.BOOTS, "Villain Boots", 42, 18, 20, 0, 12, 0, 0, 6)),

    ROYAL("royal", "Royal", Rarity.EPIC, Mat.GOLD,
            "Crown\'s Blessing", new String[]{"+Health Boost II, +Regeneration I, +Resistance I."},
            p -> { Bonus.effect(p, MobEffects.HEALTH_BOOST, 2); Bonus.effect(p, MobEffects.REGENERATION, 0); Bonus.effect(p, MobEffects.RESISTANCE, 0); },
            piece(Slot.HELMET, "Royal Helmet", 52, 24, 0, 22, 0),
            piece(Slot.CHEST, "Royal Chestplate", 82, 44, 0, 30, 0, Ability.ABSORPTION),
            piece(Slot.LEGS, "Royal Leggings", 62, 34, 0, 24, 0),
            piece(Slot.BOOTS, "Royal Boots", 46, 22, 0, 18, 0)),

    PIRATE("pirate", "Pirate", Rarity.RARE, Mat.LEATHER,
            "Plunderer\'s Luck", new String[]{"+Speed II and +Water Breathing."},
            p -> { Bonus.effect(p, MobEffects.SPEED, 1); Bonus.effect(p, MobEffects.WATER_BREATHING, 0); },
            piece(Slot.HELMET, "Pirate Helmet", 36, 14, 12, 0, 4),
            piece(Slot.CHEST, "Pirate Chestplate", 55, 22, 18, 0, 6),
            piece(Slot.LEGS, "Pirate Leggings", 44, 18, 14, 0, 4),
            piece(Slot.BOOTS, "Pirate Boots", 32, 12, 10, 0, 8, Ability.WATER_BREATHING, Ability.FALL_IMMUNE)),

    SAMURAI("samurai", "Samurai", Rarity.EPIC, Mat.IRON,
            "Bushido", new String[]{"+Strength I, +Resistance I, +Haste I."},
            p -> { Bonus.effect(p, MobEffects.STRENGTH, 0); Bonus.effect(p, MobEffects.RESISTANCE, 0); Bonus.effect(p, MobEffects.HASTE, 0); },
            piece(Slot.HELMET, "Samurai Helmet", 50, 28, 28, 0, 0, 5, 15, 8),
            piece(Slot.CHEST, "Samurai Chestplate", 76, 44, 38, 0, 0, 8, 22, 12),
            piece(Slot.LEGS, "Samurai Leggings", 58, 34, 30, 0, 0, 6, 18, 10),
            piece(Slot.BOOTS, "Samurai Boots", 44, 22, 22, 0, 4, 0, 0, 6)),

    NECROMANCER("necromancer", "Necromancer", Rarity.EPIC, Mat.LEATHER,
            "Grave Pact", new String[]{"+Night Vision, +Regeneration I.", "Withers nearby foes."},
            p -> { Bonus.effect(p, MobEffects.NIGHT_VISION, 0); Bonus.effect(p, MobEffects.REGENERATION, 0); Bonus.afflictNearby(p, MobEffects.WITHER, 4.0, 60, 0); },
            piece(Slot.HELMET, "Necromancer Helmet", 46, 20, 18, 28, 0, Ability.NIGHT_VISION),
            piece(Slot.CHEST, "Necromancer Chestplate", 72, 34, 28, 40, 0),
            piece(Slot.LEGS, "Necromancer Leggings", 56, 28, 22, 32, 0),
            piece(Slot.BOOTS, "Necromancer Boots", 40, 18, 16, 24, 0)),

    PALADIN("paladin", "Paladin", Rarity.EPIC, Mat.GOLD,
            "Divine Aegis", new String[]{"+Resistance I, +Fire Resistance, +Regeneration II."},
            p -> { Bonus.effect(p, MobEffects.RESISTANCE, 0); Bonus.effect(p, MobEffects.FIRE_RESISTANCE, 0); Bonus.effect(p, MobEffects.REGENERATION, 1); },
            piece(Slot.HELMET, "Paladin Helmet", 52, 30, 15, 20, 0, Ability.FIRE_IMMUNE),
            piece(Slot.CHEST, "Paladin Chestplate", 84, 48, 22, 28, 0, Ability.REGEN, Ability.RESISTANCE),
            piece(Slot.LEGS, "Paladin Leggings", 64, 36, 18, 22, 0),
            piece(Slot.BOOTS, "Paladin Boots", 46, 24, 12, 16, 0, Ability.FALL_IMMUNE)),

    ASSASSIN("assassin", "Assassin", Rarity.RARE, Mat.LEATHER,
            "Silent Kill", new String[]{"+Speed II, +Strength I, +Invisibility."},
            p -> { Bonus.effect(p, MobEffects.SPEED, 1); Bonus.effect(p, MobEffects.STRENGTH, 0); Bonus.effect(p, MobEffects.INVISIBILITY, 0); },
            piece(Slot.HELMET, "Assassin Helmet", 38, 16, 16, 0, 8, 8, 20, 10, Ability.NIGHT_VISION),
            piece(Slot.CHEST, "Assassin Chestplate", 58, 26, 24, 0, 10, 10, 28, 15),
            piece(Slot.LEGS, "Assassin Leggings", 46, 20, 20, 0, 12, 8, 20, 10),
            piece(Slot.BOOTS, "Assassin Boots", 34, 14, 14, 0, 14, 0, 0, 8, Ability.SPEED)),

    DRUID("druid", "Druid", Rarity.EPIC, Mat.LEATHER,
            "Wildshape", new String[]{"+Regeneration II, +Saturation, +Health Boost I."},
            p -> { Bonus.effect(p, MobEffects.REGENERATION, 1); Bonus.effect(p, MobEffects.SATURATION, 0); Bonus.effect(p, MobEffects.HEALTH_BOOST, 1); },
            piece(Slot.HELMET, "Druid Helmet", 54, 22, 0, 24, 0, Ability.REGEN),
            piece(Slot.CHEST, "Druid Chestplate", 86, 38, 0, 32, 0, Ability.REGEN),
            piece(Slot.LEGS, "Druid Leggings", 66, 30, 0, 26, 0),
            piece(Slot.BOOTS, "Druid Boots", 48, 20, 0, 18, 0, Ability.WATER_BREATHING)),

    ENGINEER("engineer", "Engineer", Rarity.RARE, Mat.IRON,
            "Overclock", new String[]{"+Haste II and +Speed I."},
            p -> { Bonus.effect(p, MobEffects.HASTE, 1); Bonus.effect(p, MobEffects.SPEED, 0); },
            piece(Slot.HELMET, "Engineer Helmet", 36, 16, 0, 18, 0, Ability.NIGHT_VISION),
            piece(Slot.CHEST, "Engineer Chestplate", 56, 26, 0, 26, 0, Ability.HASTE),
            piece(Slot.LEGS, "Engineer Leggings", 44, 20, 0, 20, 0),
            piece(Slot.BOOTS, "Engineer Boots", 32, 14, 0, 14, 0)),

    STORMCALLER("stormcaller", "Stormcaller", Rarity.LEGENDARY, Mat.DIAMOND,
            "Tempest Lord", new String[]{"+Speed II, +Jump Boost III, +Haste I, +Slow Falling."},
            p -> { Bonus.effect(p, MobEffects.SPEED, 1); Bonus.effect(p, MobEffects.JUMP_BOOST, 2); Bonus.effect(p, MobEffects.HASTE, 0); Bonus.effect(p, MobEffects.SLOW_FALLING, 0); },
            piece(Slot.HELMET, "Stormcaller Helmet", 58, 26, 0, 35, 8),
            piece(Slot.CHEST, "Stormcaller Chestplate", 94, 44, 0, 42, 10, Ability.HASTE),
            piece(Slot.LEGS, "Stormcaller Leggings", 72, 34, 0, 32, 12, Ability.SPEED),
            piece(Slot.BOOTS, "Stormcaller Boots", 52, 24, 0, 24, 16, Ability.SPEED, Ability.FALL_IMMUNE)),

    DRAGONKIN("dragonkin", "Dragonkin", Rarity.LEGENDARY, Mat.NETHERITE,
            "Dragonheart", new String[]{"+Strength II, +Fire Resistance, +Absorption II.", "Ignites nearby foes."},
            p -> { Bonus.effect(p, MobEffects.STRENGTH, 1); Bonus.effect(p, MobEffects.FIRE_RESISTANCE, 0); Bonus.effect(p, MobEffects.ABSORPTION, 1); Bonus.igniteNearby(p, 4.0, 80); },
            piece(Slot.HELMET, "Dragonkin Helmet", 60, 30, 38, 0, 0, 6, 20, 10, Ability.FIRE_IMMUNE),
            piece(Slot.CHEST, "Dragonkin Chestplate", 96, 48, 55, 0, 0, 8, 30, 15, Ability.FIRE_AURA, Ability.FIRE_IMMUNE),
            piece(Slot.LEGS, "Dragonkin Leggings", 74, 36, 42, 0, 0, 6, 22, 10),
            piece(Slot.BOOTS, "Dragonkin Boots", 52, 26, 30, 0, 0, 0, 0, 8, Ability.FIRE_IMMUNE)),

    PHOENIX("phoenix", "Phoenix", Rarity.LEGENDARY, Mat.GOLD,
            "Rebirth", new String[]{"+Regeneration III, +Fire Resistance, +Health Boost II."},
            p -> { Bonus.effect(p, MobEffects.REGENERATION, 2); Bonus.effect(p, MobEffects.FIRE_RESISTANCE, 0); Bonus.effect(p, MobEffects.HEALTH_BOOST, 2); Bonus.heal(p, 1.0f); },
            piece(Slot.HELMET, "Phoenix Helmet", 58, 28, 20, 25, 0, Ability.FIRE_IMMUNE),
            piece(Slot.CHEST, "Phoenix Chestplate", 94, 46, 32, 38, 0, Ability.REGEN, Ability.FIRE_IMMUNE),
            piece(Slot.LEGS, "Phoenix Leggings", 72, 34, 26, 30, 0),
            piece(Slot.BOOTS, "Phoenix Boots", 50, 24, 18, 22, 0, Ability.FALL_IMMUNE)),

    VOIDWALKER("voidwalker", "Voidwalker", Rarity.LEGENDARY, Mat.NETHERITE,
            "Null Field", new String[]{"+Resistance II, +Night Vision.", "Blinds nearby foes."},
            p -> { Bonus.effect(p, MobEffects.RESISTANCE, 1); Bonus.effect(p, MobEffects.NIGHT_VISION, 0); Bonus.afflictNearby(p, MobEffects.BLINDNESS, 4.0, 40, 0); },
            piece(Slot.HELMET, "Voidwalker Helmet", 62, 32, 30, 35, 0, Ability.NIGHT_VISION),
            piece(Slot.CHEST, "Voidwalker Chestplate", 98, 50, 45, 48, 0, Ability.RESISTANCE),
            piece(Slot.LEGS, "Voidwalker Leggings", 76, 38, 36, 38, 0),
            piece(Slot.BOOTS, "Voidwalker Boots", 54, 28, 26, 28, 0, Ability.FALL_IMMUNE)),

    BLOODRAVEN("bloodraven", "Bloodraven", Rarity.LEGENDARY, Mat.NETHERITE,
            "Crimson Feast", new String[]{"+Strength II, +Absorption II, +Regeneration I.", "Lifedraw heal."},
            p -> { Bonus.effect(p, MobEffects.STRENGTH, 1); Bonus.effect(p, MobEffects.ABSORPTION, 1); Bonus.effect(p, MobEffects.REGENERATION, 0); Bonus.heal(p, 3.0f); },
            piece(Slot.HELMET, "Bloodraven Helmet", 58, 28, 38, 0, 0, 8, 22, 12),
            piece(Slot.CHEST, "Bloodraven Chestplate", 96, 46, 58, 0, 0, 10, 32, 18, Ability.ABSORPTION),
            piece(Slot.LEGS, "Bloodraven Leggings", 74, 34, 44, 0, 0, 8, 24, 12),
            piece(Slot.BOOTS, "Bloodraven Boots", 52, 24, 32, 0, 6, 0, 0, 10)),

    IRONCLAD("ironclad", "Ironclad", Rarity.RARE, Mat.IRON,
            "Iron Will", new String[]{"+Resistance I and +Absorption I."},
            p -> { Bonus.effect(p, MobEffects.RESISTANCE, 0); Bonus.effect(p, MobEffects.ABSORPTION, 0); },
            piece(Slot.HELMET, "Ironclad Helmet", 40, 26, 0, 0, 0),
            piece(Slot.CHEST, "Ironclad Chestplate", 65, 42, 0, 0, 0, Ability.RESISTANCE),
            piece(Slot.LEGS, "Ironclad Leggings", 50, 32, 0, 0, 0),
            piece(Slot.BOOTS, "Ironclad Boots", 36, 22, 0, 0, 0)),

    CRYSTALWEAVER("crystalweaver", "Crystalweaver", Rarity.RARE, Mat.DIAMOND,
            "Prismatic Shield", new String[]{"+Resistance I, +Regeneration I, +Mana feel."},
            p -> { Bonus.effect(p, MobEffects.RESISTANCE, 0); Bonus.effect(p, MobEffects.REGENERATION, 0); },
            piece(Slot.HELMET, "Crystalweaver Helmet", 38, 18, 0, 22, 0),
            piece(Slot.CHEST, "Crystalweaver Chestplate", 62, 30, 0, 32, 0),
            piece(Slot.LEGS, "Crystalweaver Leggings", 48, 24, 0, 26, 0),
            piece(Slot.BOOTS, "Crystalweaver Boots", 36, 16, 0, 18, 0)),

    SANDSTRIDER("sandstrider", "Sandstrider", Rarity.RARE, Mat.LEATHER,
            "Desert Mirage", new String[]{"+Speed II, +Fire Resistance, +Jump Boost I."},
            p -> { Bonus.effect(p, MobEffects.SPEED, 1); Bonus.effect(p, MobEffects.FIRE_RESISTANCE, 0); Bonus.effect(p, MobEffects.JUMP_BOOST, 1); },
            piece(Slot.HELMET, "Sandstrider Helmet", 34, 12, 8, 0, 6),
            piece(Slot.CHEST, "Sandstrider Chestplate", 52, 20, 12, 0, 8),
            piece(Slot.LEGS, "Sandstrider Leggings", 42, 16, 10, 0, 6),
            piece(Slot.BOOTS, "Sandstrider Boots", 30, 12, 8, 0, 10, Ability.SPEED, Ability.FALL_IMMUNE)),

    OMNIKNIGHT("omniknight", "Omniknight", Rarity.MYTHIC, Mat.NETHERITE,
            "Paragon", new String[]{"+Resistance II, +Strength II, +Speed II.", "+Regeneration III, +Absorption IV, +Health Boost IV."},
            p -> { Bonus.effect(p, MobEffects.RESISTANCE, 1); Bonus.effect(p, MobEffects.STRENGTH, 1); Bonus.effect(p, MobEffects.SPEED, 1); Bonus.effect(p, MobEffects.REGENERATION, 2); Bonus.effect(p, MobEffects.ABSORPTION, 3); Bonus.effect(p, MobEffects.HEALTH_BOOST, 4); Bonus.effect(p, MobEffects.FIRE_RESISTANCE, 0); },
            piece(Slot.HELMET, "Omniknight Helmet", 92, 52, 42, 38, 0, Ability.NIGHT_VISION, Ability.FIRE_IMMUNE),
            piece(Slot.CHEST, "Omniknight Chestplate", 165, 92, 68, 58, 0, Ability.FLIGHT, Ability.REGEN, Ability.ABSORPTION),
            piece(Slot.LEGS, "Omniknight Leggings", 125, 68, 52, 48, 0, Ability.RESISTANCE),
            piece(Slot.BOOTS, "Omniknight Boots", 85, 46, 38, 32, 14, Ability.SPEED, Ability.FALL_IMMUNE)),

    APOCALYPSE("apocalypse", "Apocalypse", Rarity.MYTHIC, Mat.NETHERITE,
            "End of Days", new String[]{"+Strength II, +Resistance II.", "Withers and poisons nearby foes; lifedraw."},
            p -> { Bonus.effect(p, MobEffects.STRENGTH, 1); Bonus.effect(p, MobEffects.RESISTANCE, 1); Bonus.afflictNearby(p, MobEffects.WITHER, 4.0, 60, 0); Bonus.afflictNearby(p, MobEffects.POISON, 4.0, 60, 0); Bonus.heal(p, 2.0f); },
            piece(Slot.HELMET, "Apocalypse Helmet", 90, 50, 55, 0, 0, 10, 35, 20, Ability.NIGHT_VISION),
            piece(Slot.CHEST, "Apocalypse Chestplate", 155, 88, 75, 0, 0, 12, 45, 25, Ability.RESISTANCE),
            piece(Slot.LEGS, "Apocalypse Leggings", 118, 66, 58, 0, 0, 10, 35, 18),
            piece(Slot.BOOTS, "Apocalypse Boots", 82, 44, 42, 0, 0, 0, 0, 15)),

    INFINITY("infinity", "Infinity", Rarity.MYTHIC, Mat.NETHERITE,
            "Absolute Zero", new String[]{"+Resistance II, +Strength II, +Speed II, +Regeneration III.", "+Health Boost IV, +Absorption IV, +Night Vision, +Fire Resistance."},
            p -> { Bonus.effect(p, MobEffects.RESISTANCE, 1); Bonus.effect(p, MobEffects.STRENGTH, 1); Bonus.effect(p, MobEffects.SPEED, 1); Bonus.effect(p, MobEffects.REGENERATION, 2); Bonus.effect(p, MobEffects.HEALTH_BOOST, 4); Bonus.effect(p, MobEffects.ABSORPTION, 3); Bonus.effect(p, MobEffects.NIGHT_VISION, 0); Bonus.effect(p, MobEffects.FIRE_RESISTANCE, 0); },
            piece(Slot.HELMET, "Infinity Helmet", 95, 55, 48, 48, 0, 12, 38, 18, Ability.NIGHT_VISION),
            piece(Slot.CHEST, "Infinity Chestplate", 175, 95, 72, 72, 0, 15, 50, 25, Ability.FLIGHT, Ability.FIRE_IMMUNE, Ability.REGEN),
            piece(Slot.LEGS, "Infinity Leggings", 132, 72, 55, 55, 0, 12, 38, 20, Ability.RESISTANCE),
            piece(Slot.BOOTS, "Infinity Boots", 92, 48, 40, 40, 16, 0, 0, 15, Ability.SPEED, Ability.FALL_IMMUNE));

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

    /** {@code arm2_<set>_<slot>}. */
    public String pieceId(Slot slot) {
        return "arm2_" + key + "_" + slot.idSuffix;
    }

    public static ArmorSet byId(String id) {
        if (id == null || !id.startsWith("arm2_")) return null;
        for (ArmorSet set : values()) {
            for (Slot slot : Slot.values()) {
                if (set.pieceId(slot).equals(id)) return set;
            }
        }
        return null;
    }

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
