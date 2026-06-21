/**
 * Generates expansion2 armour Java enum, lang fragment, and gen-armor2.js palette map
 * from a single catalogue. Run: node gen-armor2-sources.js
 */
const fs = require('fs');
const path = require('path');

const ROOT = path.join(__dirname, '..');
const JAVA = path.join(ROOT, 'src', 'main', 'java', 'com', 'political', 'expansion2', 'armor', 'ArmorSet.java');
const LANG = path.join(__dirname, 'lang-fragments', 'armor2.json');
const GEN_TEX = path.join(__dirname, 'gen-armor2.js');

// bonus op -> Java snippet (player param = p)
const BONUS_OPS = {
  'speed:0': 'Bonus.effect(p, MobEffects.SPEED, 0)',
  'speed:1': 'Bonus.effect(p, MobEffects.SPEED, 1)',
  'speed:2': 'Bonus.effect(p, MobEffects.SPEED, 2)',
  'strength:0': 'Bonus.effect(p, MobEffects.STRENGTH, 0)',
  'strength:1': 'Bonus.effect(p, MobEffects.STRENGTH, 1)',
  'strength:2': 'Bonus.effect(p, MobEffects.STRENGTH, 2)',
  'resistance:0': 'Bonus.effect(p, MobEffects.RESISTANCE, 0)',
  'resistance:1': 'Bonus.effect(p, MobEffects.RESISTANCE, 1)',
  'resistance:2': 'Bonus.effect(p, MobEffects.RESISTANCE, 2)',
  'absorption:0': 'Bonus.effect(p, MobEffects.ABSORPTION, 0)',
  'absorption:1': 'Bonus.effect(p, MobEffects.ABSORPTION, 1)',
  'absorption:3': 'Bonus.effect(p, MobEffects.ABSORPTION, 3)',
  'regen:0': 'Bonus.effect(p, MobEffects.REGENERATION, 0)',
  'regen:1': 'Bonus.effect(p, MobEffects.REGENERATION, 1)',
  'regen:2': 'Bonus.effect(p, MobEffects.REGENERATION, 2)',
  'regen:3': 'Bonus.effect(p, MobEffects.REGENERATION, 3)',
  'health_boost:1': 'Bonus.effect(p, MobEffects.HEALTH_BOOST, 1)',
  'health_boost:2': 'Bonus.effect(p, MobEffects.HEALTH_BOOST, 2)',
  'health_boost:3': 'Bonus.effect(p, MobEffects.HEALTH_BOOST, 3)',
  'health_boost:4': 'Bonus.effect(p, MobEffects.HEALTH_BOOST, 4)',
  'fire_res': 'Bonus.effect(p, MobEffects.FIRE_RESISTANCE, 0)',
  'water_breathing': 'Bonus.effect(p, MobEffects.WATER_BREATHING, 0)',
  'night_vision': 'Bonus.effect(p, MobEffects.NIGHT_VISION, 0)',
  'jump:1': 'Bonus.effect(p, MobEffects.JUMP_BOOST, 1)',
  'jump:2': 'Bonus.effect(p, MobEffects.JUMP_BOOST, 2)',
  'jump:3': 'Bonus.effect(p, MobEffects.JUMP_BOOST, 3)',
  'haste:0': 'Bonus.effect(p, MobEffects.HASTE, 0)',
  'haste:1': 'Bonus.effect(p, MobEffects.HASTE, 1)',
  'slow_falling': 'Bonus.effect(p, MobEffects.SLOW_FALLING, 0)',
  'saturation': 'Bonus.effect(p, MobEffects.SATURATION, 0)',
  'invisibility': 'Bonus.effect(p, MobEffects.INVISIBILITY, 0)',
  'heal:1': 'Bonus.heal(p, 1.0f)',
  'heal:2': 'Bonus.heal(p, 2.0f)',
  'heal:3': 'Bonus.heal(p, 3.0f)',
  'ignite:4': 'Bonus.igniteNearby(p, 4.0, 80)',
  'ignite:6': 'Bonus.igniteNearby(p, 6.0, 100)',
  'slow_nearby': 'Bonus.afflictNearby(p, MobEffects.SLOWNESS, 5.0, 60, 1)',
  'wither_nearby': 'Bonus.afflictNearby(p, MobEffects.WITHER, 4.0, 60, 0)',
  'poison_nearby': 'Bonus.afflictNearby(p, MobEffects.POISON, 4.0, 60, 0)',
  'weakness_nearby': 'Bonus.afflictNearby(p, MobEffects.WEAKNESS, 5.0, 60, 0)',
  'blind_nearby': 'Bonus.afflictNearby(p, MobEffects.BLINDNESS, 4.0, 40, 0)',
};

const SETS = [
  // ---- ELEMENTS (9) ----
  { key: 'pyrofire', name: 'Pyrofire', rarity: 'COMMON', mat: 'LEATHER', palette: 'ember',
    bonus: 'Ember Skin', lines: ['+Fire Resistance and +Speed I.', 'Warmth in any biome.'],
    bonusOps: ['fire_res', 'speed:0'],
    pieces: [
      ['Helmet', 18, 8, 8, 0, 0], ['Chestplate', 30, 14, 12, 0, 0, ['FIRE_IMMUNE']],
      ['Leggings', 24, 11, 10, 0, 0], ['Boots', 18, 8, 6, 0, 2],
    ]},
  { key: 'hydroflow', name: 'Hydroflow', rarity: 'COMMON', mat: 'LEATHER', palette: 'aqua',
    bonus: 'Tidal Grace', lines: ['+Water Breathing and +Regeneration I.'],
    bonusOps: ['water_breathing', 'regen:0'],
    pieces: [
      ['Helmet', 18, 8, 0, 10, 0, ['WATER_BREATHING']], ['Chestplate', 30, 14, 0, 14, 0],
      ['Leggings', 24, 11, 0, 12, 0], ['Boots', 18, 8, 0, 8, 0, ['WATER_BREATHING']],
    ]},
  { key: 'geostone', name: 'Geostone', rarity: 'COMMON', mat: 'IRON', palette: 'darkiron',
    bonus: 'Bedrock Stance', lines: ['+Resistance I and +Slowness immunity feel.'],
    bonusOps: ['resistance:0', 'health_boost:1'],
    pieces: [
      ['Helmet', 22, 14, 0, 0, 0], ['Chestplate', 38, 22, 0, 0, 0, ['RESISTANCE']],
      ['Leggings', 30, 18, 0, 0, 0], ['Boots', 22, 12, 0, 0, 0],
    ]},
  { key: 'aerowind', name: 'Aerowind', rarity: 'UNCOMMON', mat: 'LEATHER', palette: 'silver',
    bonus: 'Gale Step', lines: ['+Speed II and +Jump Boost I.', 'Feather-light landings.'],
    bonusOps: ['speed:1', 'jump:1', 'slow_falling'],
    pieces: [
      ['Helmet', 24, 10, 0, 8, 4], ['Chestplate', 38, 16, 0, 12, 6],
      ['Leggings', 30, 13, 0, 0, 8, ['SPEED']], ['Boots', 22, 10, 0, 0, 10, ['SPEED', 'FALL_IMMUNE']],
    ]},
  { key: 'electrovolt', name: 'Electrovolt', rarity: 'UNCOMMON', mat: 'IRON', palette: 'thunder',
    bonus: 'Static Field', lines: ['+Haste I and +Speed I.', 'Crackle with energy.'],
    bonusOps: ['haste:0', 'speed:0'],
    pieces: [
      ['Helmet', 26, 12, 10, 8, 0], ['Chestplate', 42, 18, 14, 12, 0, ['HASTE']],
      ['Leggings', 32, 15, 12, 0, 2], ['Boots', 24, 11, 8, 0, 4],
    ]},
  { key: 'cryofrost', name: 'Cryofrost', rarity: 'UNCOMMON', mat: 'IRON', palette: 'frost',
    bonus: 'Permafrost Veil', lines: ['+Resistance I and slows nearby foes.'],
    bonusOps: ['resistance:0', 'slow_nearby'],
    pieces: [
      ['Helmet', 26, 14, 0, 12, 0, ['WATER_BREATHING']], ['Chestplate', 42, 20, 0, 16, 0],
      ['Leggings', 32, 16, 0, 12, 0], ['Boots', 24, 12, 0, 8, 0, ['FALL_IMMUNE']],
    ]},
  { key: 'verdantlife', name: 'Verdantlife', rarity: 'UNCOMMON', mat: 'LEATHER', palette: 'emerald',
    bonus: 'Photosynthesis', lines: ['+Regeneration I and +Saturation.'],
    bonusOps: ['regen:0', 'saturation'],
    pieces: [
      ['Helmet', 24, 10, 0, 10, 0, ['REGEN']], ['Chestplate', 38, 16, 0, 14, 0, ['REGEN']],
      ['Leggings', 30, 13, 0, 12, 0], ['Boots', 22, 10, 0, 8, 0],
    ]},
  { key: 'umbralshadow', name: 'Umbralshadow', rarity: 'RARE', mat: 'LEATHER', palette: 'shadow',
    bonus: 'Veil of Night', lines: ['+Night Vision, +Speed I, +Invisibility pulse.'],
    bonusOps: ['night_vision', 'speed:0', 'invisibility'],
    pieces: [
      ['Helmet', 40, 18, 12, 0, 5, 8, 0, ['NIGHT_VISION']], ['Chestplate', 62, 28, 18, 0, 6, 10, 15],
      ['Leggings', 48, 22, 14, 0, 8, 6, 10], ['Boots', 36, 16, 10, 0, 12, 0, 0, ['SPEED']],
    ]},
  { key: 'radiantlight', name: 'Radiantlight', rarity: 'RARE', mat: 'GOLD', palette: 'gold',
    bonus: 'Solar Ward', lines: ['+Regeneration I, +Resistance I, +Fire Resistance.'],
    bonusOps: ['regen:0', 'resistance:0', 'fire_res'],
    pieces: [
      ['Helmet', 42, 20, 0, 18, 0, ['NIGHT_VISION']], ['Chestplate', 68, 32, 0, 24, 0, ['REGEN']],
      ['Leggings', 52, 24, 0, 18, 0], ['Boots', 38, 18, 0, 14, 0, ['FIRE_IMMUNE']],
    ]},

  // ---- JJK GRADES (5) ----
  { key: 'grade4', name: 'Grade IV', rarity: 'RARE', mat: 'LEATHER', palette: 'iron',
    bonus: 'Fourth Grade', lines: ['+Speed I and +Jump Boost I.', 'Entry-level sorcerer kit.'],
    bonusOps: ['speed:0', 'jump:1'],
    pieces: [
      ['Helmet', 38, 16, 8, 14, 0], ['Chestplate', 58, 26, 12, 20, 0],
      ['Leggings', 46, 20, 10, 16, 0], ['Boots', 34, 14, 6, 12, 3],
    ]},
  { key: 'grade3', name: 'Grade III', rarity: 'EPIC', mat: 'IRON', palette: 'steel',
    bonus: 'Semi-Grade', lines: ['+Strength I, +Resistance I, +Haste I.'],
    bonusOps: ['strength:0', 'resistance:0', 'haste:0'],
    pieces: [
      ['Helmet', 48, 24, 18, 20, 0], ['Chestplate', 72, 38, 28, 28, 0, ['RESISTANCE']],
      ['Leggings', 56, 30, 22, 22, 0], ['Boots', 42, 20, 16, 16, 4],
    ]},
  { key: 'grade2', name: 'Grade II', rarity: 'LEGENDARY', mat: 'DIAMOND', palette: 'diamond',
    bonus: 'Grade 1 Sorcerer', lines: ['+Strength II, +Speed I, +Regeneration I.'],
    bonusOps: ['strength:1', 'speed:0', 'regen:0'],
    pieces: [
      ['Helmet', 58, 28, 32, 30, 0, 6, 18, 8], ['Chestplate', 92, 46, 50, 42, 0, 8, 28, 12, ['RESISTANCE']],
      ['Leggings', 70, 34, 38, 34, 0, 6, 20, 10], ['Boots', 50, 24, 28, 24, 6, 0, 0, 8],
    ]},
  { key: 'grade1', name: 'Grade I', rarity: 'LEGENDARY', mat: 'DIAMOND', palette: 'amethyst',
    bonus: 'Special Grade', lines: ['+Strength II, +Resistance I, +Absorption II.'],
    bonusOps: ['strength:1', 'resistance:1', 'absorption:1'],
    pieces: [
      ['Helmet', 62, 30, 36, 35, 0, 8, 22, 10], ['Chestplate', 98, 50, 55, 48, 0, 10, 32, 15, ['ABSORPTION']],
      ['Leggings', 74, 36, 42, 38, 0, 8, 24, 12], ['Boots', 52, 26, 30, 28, 8, 0, 0, 10],
    ]},
  { key: 'special_grade', name: 'Special Grade', rarity: 'MYTHIC', mat: 'NETHERITE', palette: 'void',
    bonus: 'Domain Expansion', lines: ['+Strength II, +Speed II, +Regeneration III.', '+Resistance II, +Absorption III, +Night Vision.'],
    bonusOps: ['strength:1', 'speed:1', 'regen:2', 'resistance:1', 'absorption:3', 'night_vision'],
    pieces: [
      ['Helmet', 88, 48, 45, 45, 0, 10, 30, 15, ['NIGHT_VISION']],
      ['Chestplate', 150, 82, 65, 65, 0, 12, 40, 20, ['FLIGHT', 'RESISTANCE']],
      ['Leggings', 115, 62, 50, 50, 0, 10, 30, 15], ['Boots', 80, 42, 35, 35, 14, 0, 0, 12, ['SPEED', 'FALL_IMMUNE']],
    ]},

  // ---- CLASS / ROLE THEMES (11) ----
  { key: 'viltrumite', name: 'Viltrumite', rarity: 'LEGENDARY', mat: 'NETHERITE', palette: 'blood',
    bonus: 'Conqueror\'s Might', lines: ['+Strength II, +Resistance II, +Speed II.', '+Regeneration II and lifedraw.'],
    bonusOps: ['strength:1', 'resistance:1', 'speed:1', 'regen:1', 'heal:2'],
    pieces: [
      ['Helmet', 65, 32, 42, 0, 8, 8, 25, 12], ['Chestplate', 105, 52, 62, 0, 10, 10, 35, 18, ['RESISTANCE']],
      ['Leggings', 78, 38, 48, 0, 12, 8, 25, 12], ['Boots', 55, 28, 35, 0, 16, 0, 0, 10, ['SPEED', 'FALL_IMMUNE']],
    ]},
  { key: 'hero', name: 'Hero', rarity: 'EPIC', mat: 'DIAMOND', palette: 'steel',
    bonus: 'Inspiring Presence', lines: ['+Resistance I, +Regeneration I, +Absorption I.'],
    bonusOps: ['resistance:0', 'regen:0', 'absorption:0'],
    pieces: [
      ['Helmet', 50, 26, 20, 15, 0, ['NIGHT_VISION']], ['Chestplate', 78, 42, 30, 22, 0, ['REGEN', 'ABSORPTION']],
      ['Leggings', 60, 32, 24, 18, 0], ['Boots', 44, 22, 18, 12, 4, ['FALL_IMMUNE']],
    ]},
  { key: 'villain', name: 'Villain', rarity: 'EPIC', mat: 'NETHERITE', palette: 'shadow',
    bonus: 'Malice Aura', lines: ['+Strength I, +Speed I.', 'Weakens nearby foes.'],
    bonusOps: ['strength:0', 'speed:0', 'weakness_nearby'],
    pieces: [
      ['Helmet', 48, 22, 22, 0, 6, 6, 18, 8, ['NIGHT_VISION']], ['Chestplate', 74, 36, 32, 0, 8, 8, 25, 12],
      ['Leggings', 58, 28, 26, 0, 10, 6, 18, 8], ['Boots', 42, 18, 20, 0, 12, 0, 0, 6],
    ]},
  { key: 'royal', name: 'Royal', rarity: 'EPIC', mat: 'GOLD', palette: 'gold',
    bonus: 'Crown\'s Blessing', lines: ['+Health Boost II, +Regeneration I, +Resistance I.'],
    bonusOps: ['health_boost:2', 'regen:0', 'resistance:0'],
    pieces: [
      ['Helmet', 52, 24, 0, 22, 0], ['Chestplate', 82, 44, 0, 30, 0, ['ABSORPTION']],
      ['Leggings', 62, 34, 0, 24, 0], ['Boots', 46, 22, 0, 18, 0],
    ]},
  { key: 'pirate', name: 'Pirate', rarity: 'RARE', mat: 'LEATHER', palette: 'leather',
    bonus: 'Plunderer\'s Luck', lines: ['+Speed II and +Water Breathing.'],
    bonusOps: ['speed:1', 'water_breathing'],
    pieces: [
      ['Helmet', 36, 14, 12, 0, 4], ['Chestplate', 55, 22, 18, 0, 6],
      ['Leggings', 44, 18, 14, 0, 4], ['Boots', 32, 12, 10, 0, 8, ['WATER_BREATHING', 'FALL_IMMUNE']],
    ]},
  { key: 'samurai', name: 'Samurai', rarity: 'EPIC', mat: 'IRON', palette: 'darkiron',
    bonus: 'Bushido', lines: ['+Strength I, +Resistance I, +Haste I.'],
    bonusOps: ['strength:0', 'resistance:0', 'haste:0'],
    pieces: [
      ['Helmet', 50, 28, 28, 0, 0, 5, 15, 8], ['Chestplate', 76, 44, 38, 0, 0, 8, 22, 12],
      ['Leggings', 58, 34, 30, 0, 0, 6, 18, 10], ['Boots', 44, 22, 22, 0, 4, 0, 0, 6],
    ]},
  { key: 'necromancer', name: 'Necromancer', rarity: 'EPIC', mat: 'LEATHER', palette: 'necro',
    bonus: 'Grave Pact', lines: ['+Night Vision, +Regeneration I.', 'Withers nearby foes.'],
    bonusOps: ['night_vision', 'regen:0', 'wither_nearby'],
    pieces: [
      ['Helmet', 46, 20, 18, 28, 0, ['NIGHT_VISION']], ['Chestplate', 72, 34, 28, 40, 0],
      ['Leggings', 56, 28, 22, 32, 0], ['Boots', 40, 18, 16, 24, 0],
    ]},
  { key: 'paladin', name: 'Paladin', rarity: 'EPIC', mat: 'GOLD', palette: 'gold',
    bonus: 'Divine Aegis', lines: ['+Resistance I, +Fire Resistance, +Regeneration II.'],
    bonusOps: ['resistance:0', 'fire_res', 'regen:1'],
    pieces: [
      ['Helmet', 52, 30, 15, 20, 0, ['FIRE_IMMUNE']], ['Chestplate', 84, 48, 22, 28, 0, ['REGEN', 'RESISTANCE']],
      ['Leggings', 64, 36, 18, 22, 0], ['Boots', 46, 24, 12, 16, 0, ['FALL_IMMUNE']],
    ]},
  { key: 'assassin', name: 'Assassin', rarity: 'RARE', mat: 'LEATHER', palette: 'shadow',
    bonus: 'Silent Kill', lines: ['+Speed II, +Strength I, +Invisibility.'],
    bonusOps: ['speed:1', 'strength:0', 'invisibility'],
    pieces: [
      ['Helmet', 38, 16, 16, 0, 8, 8, 20, 10, ['NIGHT_VISION']], ['Chestplate', 58, 26, 24, 0, 10, 10, 28, 15],
      ['Leggings', 46, 20, 20, 0, 12, 8, 20, 10], ['Boots', 34, 14, 14, 0, 14, 0, 0, 8, ['SPEED']],
    ]},
  { key: 'druid', name: 'Druid', rarity: 'EPIC', mat: 'LEATHER', palette: 'emerald',
    bonus: 'Wildshape', lines: ['+Regeneration II, +Saturation, +Health Boost I.'],
    bonusOps: ['regen:1', 'saturation', 'health_boost:1'],
    pieces: [
      ['Helmet', 54, 22, 0, 24, 0, ['REGEN']], ['Chestplate', 86, 38, 0, 32, 0, ['REGEN']],
      ['Leggings', 66, 30, 0, 26, 0], ['Boots', 48, 20, 0, 18, 0, ['WATER_BREATHING']],
    ]},
  { key: 'engineer', name: 'Engineer', rarity: 'RARE', mat: 'IRON', palette: 'steel',
    bonus: 'Overclock', lines: ['+Haste II and +Speed I.'],
    bonusOps: ['haste:1', 'speed:0'],
    pieces: [
      ['Helmet', 36, 16, 0, 18, 0, ['NIGHT_VISION']], ['Chestplate', 56, 26, 0, 26, 0, ['HASTE']],
      ['Leggings', 44, 20, 0, 20, 0], ['Boots', 32, 14, 0, 14, 0],
    ]},

  // ---- EXTRA THEMES (8) ----
  { key: 'stormcaller', name: 'Stormcaller', rarity: 'LEGENDARY', mat: 'DIAMOND', palette: 'thunder',
    bonus: 'Tempest Lord', lines: ['+Speed II, +Jump Boost III, +Haste I, +Slow Falling.'],
    bonusOps: ['speed:1', 'jump:2', 'haste:0', 'slow_falling'],
    pieces: [
      ['Helmet', 58, 26, 0, 35, 8], ['Chestplate', 94, 44, 0, 42, 10, ['HASTE']],
      ['Leggings', 72, 34, 0, 32, 12, ['SPEED']], ['Boots', 52, 24, 0, 24, 16, ['SPEED', 'FALL_IMMUNE']],
    ]},
  { key: 'dragonkin', name: 'Dragonkin', rarity: 'LEGENDARY', mat: 'NETHERITE', palette: 'ember',
    bonus: 'Dragonheart', lines: ['+Strength II, +Fire Resistance, +Absorption II.', 'Ignites nearby foes.'],
    bonusOps: ['strength:1', 'fire_res', 'absorption:1', 'ignite:4'],
    pieces: [
      ['Helmet', 60, 30, 38, 0, 0, 6, 20, 10, ['FIRE_IMMUNE']], ['Chestplate', 96, 48, 55, 0, 0, 8, 30, 15, ['FIRE_AURA', 'FIRE_IMMUNE']],
      ['Leggings', 74, 36, 42, 0, 0, 6, 22, 10], ['Boots', 52, 26, 30, 0, 0, 0, 0, 8, ['FIRE_IMMUNE']],
    ]},
  { key: 'phoenix', name: 'Phoenix', rarity: 'LEGENDARY', mat: 'GOLD', palette: 'ember',
    bonus: 'Rebirth', lines: ['+Regeneration III, +Fire Resistance, +Health Boost II.'],
    bonusOps: ['regen:2', 'fire_res', 'health_boost:2', 'heal:1'],
    pieces: [
      ['Helmet', 58, 28, 20, 25, 0, ['FIRE_IMMUNE']], ['Chestplate', 94, 46, 32, 38, 0, ['REGEN', 'FIRE_IMMUNE']],
      ['Leggings', 72, 34, 26, 30, 0], ['Boots', 50, 24, 18, 22, 0, ['FALL_IMMUNE']],
    ]},
  { key: 'voidwalker', name: 'Voidwalker', rarity: 'LEGENDARY', mat: 'NETHERITE', palette: 'void',
    bonus: 'Null Field', lines: ['+Resistance II, +Night Vision.', 'Blinds nearby foes.'],
    bonusOps: ['resistance:1', 'night_vision', 'blind_nearby'],
    pieces: [
      ['Helmet', 62, 32, 30, 35, 0, ['NIGHT_VISION']], ['Chestplate', 98, 50, 45, 48, 0, ['RESISTANCE']],
      ['Leggings', 76, 38, 36, 38, 0], ['Boots', 54, 28, 26, 28, 0, ['FALL_IMMUNE']],
    ]},
  { key: 'bloodraven', name: 'Bloodraven', rarity: 'LEGENDARY', mat: 'NETHERITE', palette: 'blood',
    bonus: 'Crimson Feast', lines: ['+Strength II, +Absorption II, +Regeneration I.', 'Lifedraw heal.'],
    bonusOps: ['strength:1', 'absorption:1', 'regen:0', 'heal:3'],
    pieces: [
      ['Helmet', 58, 28, 38, 0, 0, 8, 22, 12], ['Chestplate', 96, 46, 58, 0, 0, 10, 32, 18, ['ABSORPTION']],
      ['Leggings', 74, 34, 44, 0, 0, 8, 24, 12], ['Boots', 52, 24, 32, 0, 6, 0, 0, 10],
    ]},
  { key: 'ironclad', name: 'Ironclad', rarity: 'RARE', mat: 'IRON', palette: 'iron',
    bonus: 'Iron Will', lines: ['+Resistance I and +Absorption I.'],
    bonusOps: ['resistance:0', 'absorption:0'],
    pieces: [
      ['Helmet', 40, 26, 0, 0, 0], ['Chestplate', 65, 42, 0, 0, 0, ['RESISTANCE']],
      ['Leggings', 50, 32, 0, 0, 0], ['Boots', 36, 22, 0, 0, 0],
    ]},
  { key: 'crystalweaver', name: 'Crystalweaver', rarity: 'RARE', mat: 'DIAMOND', palette: 'diamond',
    bonus: 'Prismatic Shield', lines: ['+Resistance I, +Regeneration I, +Mana feel.'],
    bonusOps: ['resistance:0', 'regen:0'],
    pieces: [
      ['Helmet', 38, 18, 0, 22, 0], ['Chestplate', 62, 30, 0, 32, 0],
      ['Leggings', 48, 24, 0, 26, 0], ['Boots', 36, 16, 0, 18, 0],
    ]},
  { key: 'sandstrider', name: 'Sandstrider', rarity: 'RARE', mat: 'LEATHER', palette: 'gold',
    bonus: 'Desert Mirage', lines: ['+Speed II, +Fire Resistance, +Jump Boost I.'],
    bonusOps: ['speed:1', 'fire_res', 'jump:1'],
    pieces: [
      ['Helmet', 34, 12, 8, 0, 6], ['Chestplate', 52, 20, 12, 0, 8],
      ['Leggings', 42, 16, 10, 0, 6], ['Boots', 30, 12, 8, 0, 10, ['SPEED', 'FALL_IMMUNE']],
    ]},

  // ---- MYTHIC (3) ----
  { key: 'omniknight', name: 'Omniknight', rarity: 'MYTHIC', mat: 'NETHERITE', palette: 'gold',
    bonus: 'Paragon', lines: ['+Resistance II, +Strength II, +Speed II.', '+Regeneration III, +Absorption IV, +Health Boost IV.'],
    bonusOps: ['resistance:1', 'strength:1', 'speed:1', 'regen:2', 'absorption:3', 'health_boost:4', 'fire_res'],
    pieces: [
      ['Helmet', 92, 52, 42, 38, 0, ['NIGHT_VISION', 'FIRE_IMMUNE']],
      ['Chestplate', 165, 92, 68, 58, 0, ['FLIGHT', 'REGEN', 'ABSORPTION']],
      ['Leggings', 125, 68, 52, 48, 0, ['RESISTANCE']], ['Boots', 85, 46, 38, 32, 14, ['SPEED', 'FALL_IMMUNE']],
    ]},
  { key: 'apocalypse', name: 'Apocalypse', rarity: 'MYTHIC', mat: 'NETHERITE', palette: 'blood',
    bonus: 'End of Days', lines: ['+Strength II, +Resistance II.', 'Withers and poisons nearby foes; lifedraw.'],
    bonusOps: ['strength:1', 'resistance:1', 'wither_nearby', 'poison_nearby', 'heal:2'],
    pieces: [
      ['Helmet', 90, 50, 55, 0, 0, 10, 35, 20, ['NIGHT_VISION']],
      ['Chestplate', 155, 88, 75, 0, 0, 12, 45, 25, ['RESISTANCE']],
      ['Leggings', 118, 66, 58, 0, 0, 10, 35, 18], ['Boots', 82, 44, 42, 0, 0, 0, 0, 15],
    ]},
  { key: 'infinity', name: 'Infinity', rarity: 'MYTHIC', mat: 'NETHERITE', palette: 'amethyst',
    bonus: 'Absolute Zero', lines: ['+Resistance II, +Strength II, +Speed II, +Regeneration III.', '+Health Boost IV, +Absorption IV, +Night Vision, +Fire Resistance.'],
    bonusOps: ['resistance:1', 'strength:1', 'speed:1', 'regen:2', 'health_boost:4', 'absorption:3', 'night_vision', 'fire_res'],
    pieces: [
      ['Helmet', 95, 55, 48, 48, 0, 12, 38, 18, ['NIGHT_VISION']],
      ['Chestplate', 175, 95, 72, 72, 0, 15, 50, 25, ['FLIGHT', 'FIRE_IMMUNE', 'REGEN']],
      ['Leggings', 132, 72, 55, 55, 0, 12, 38, 20, ['RESISTANCE']],
      ['Boots', 92, 48, 40, 40, 16, 0, 0, 15, ['SPEED', 'FALL_IMMUNE']],
    ]},
];

const SLOTS = ['HELMET', 'CHEST', 'LEGS', 'BOOTS'];
const SLOT_SUFFIX = { HELMET: 'helmet', CHEST: 'chestplate', LEGS: 'leggings', BOOTS: 'boots' };
const SLOT_WORD = { HELMET: 'Helmet', CHEST: 'Chestplate', LEGS: 'Leggings', BOOTS: 'Boots' };

function escapeJava(s) {
  return s.replace(/\\/g, '\\\\').replace(/"/g, '\\"').replace(/'/g, "\\'");
}

function pieceCall(slot, set, piece) {
  const slotEnum = slot;
  const slotWord = SLOT_WORD[slot];
  const displayName = piece[0] && piece[0] !== 'Helmet' && piece[0] !== 'Chestplate' && piece[0] !== 'Leggings' && piece[0] !== 'Boots'
      ? piece[0]
      : `${set.name} ${slotWord}`;
  const nums = piece.slice(1);
  // [h, d, s, i, spd] or [h, d, s, i, spd, cc, cd, fer] or with abilities at end
  let abilities = [];
  const last = nums[nums.length - 1];
  if (Array.isArray(last)) {
    abilities = last;
    nums.pop();
  }
  const [h, d, s, i, spd, cc = 0, cd = 0, fer = 0] = nums;
  const ab = abilities.length ? `, ${abilities.map(a => `Ability.${a}`).join(', ')}` : '';
  if (cc || cd || fer) {
    return `piece(Slot.${slotEnum}, "${escapeJava(displayName)}", ${h}, ${d}, ${s}, ${i}, ${spd}, ${cc}, ${cd}, ${fer}${ab})`;
  }
  return `piece(Slot.${slotEnum}, "${escapeJava(displayName)}", ${h}, ${d}, ${s}, ${i}, ${spd}${ab})`;
}

function bonusLambda(ops) {
  const body = ops.map(o => BONUS_OPS[o]).join('; ');
  return `p -> { ${body}; }`;
}

function genArmorSetJava() {
  const entries = SETS.map(set => {
    const pieces = set.pieces.map((p, i) => pieceCall(SLOTS[i], set, p)).join(',\n            ');
    const lines = set.lines.map(l => `"${escapeJava(l)}"`).join(', ');
    return `    ${set.key.toUpperCase().replace(/[^A-Z0-9]/g, '_')}("${set.key}", "${escapeJava(set.name)}", Rarity.${set.rarity}, Mat.${set.mat},
            "${escapeJava(set.bonus)}", new String[]{${lines}},
            ${bonusLambda(set.bonusOps)},
            ${pieces})`;
  }).join(',\n\n');

  return `package com.political.expansion2.armor;

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

${entries};

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
`;
}

function genLang() {
  const lang = {};
  const slotWords = { helmet: 'Helmet', chestplate: 'Chestplate', leggings: 'Leggings', boots: 'Boots' };
  for (const set of SETS) {
    for (const slot of ['helmet', 'chestplate', 'leggings', 'boots']) {
      const id = `arm2_${set.key}_${slot}`;
      lang[`item.politicalserver.${id}`] = `${set.name} ${slotWords[slot]}`;
    }
  }
  return lang;
}

function genArmor2JsPalettes() {
  const lines = SETS.map(s => `  ${s.key}: { metal: P.${s.palette}, accent: P.${s.palette === 'gold' ? 'diamond' : s.palette}[3] || P.gold[3] },`).join('\n');
  return lines;
}

// --- run ---
const ARMOR_DIR = path.dirname(JAVA);
fs.mkdirSync(ARMOR_DIR, { recursive: true });
fs.writeFileSync(JAVA, genArmorSetJava());
fs.writeFileSync(LANG, JSON.stringify(genLang(), null, 2) + '\n');

const SUPPORT = {
  'Bonus.java': `package com.political.expansion2.armor;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

final class Bonus {
    private static final int DURATION = 100;
    private Bonus() {}
    static void effect(ServerPlayer player, Holder<MobEffect> effect, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, DURATION, amplifier, true, false));
    }
    static void heal(ServerPlayer player, float amount) {
        if (player.getHealth() < player.getMaxHealth()) player.heal(amount);
    }
    static void igniteNearby(ServerPlayer player, double radius, int fireTicks) {
        for (Monster m : player.level().getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(radius))) {
            m.setRemainingFireTicks(fireTicks);
        }
    }
    static void afflictNearby(ServerPlayer player, Holder<MobEffect> effect, double radius, int duration, int amplifier) {
        for (Monster m : player.level().getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(radius))) {
            m.addEffect(new MobEffectInstance(effect, duration, amplifier, true, true));
        }
    }
}
`,
  'Armor2Items.java': `package com.political.expansion2.armor;

import com.political.items.Ability;
import com.political.items.ItemStats;
import com.political.items.RpgItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

public final class Armor2Items {
    public static final String SET_KEY = "arm2_set";
    private static final String NAMESPACE = "politicalserver";
    private Armor2Items() {}

    public static ItemStack create(ArmorSet set, ArmorSet.Slot slot) {
        ArmorSet.Piece piece = set.pieceFor(slot);
        String id = set.pieceId(slot);
        ItemStack stack = new ItemStack(set.baseItem(slot));
        CompoundTag tag = new CompoundTag();
        tag.putString(RpgItems.ITEM_ID_KEY, id);
        tag.putString(ItemStats.RARITY, set.rarity.name());
        tag.putString(SET_KEY, set.key);
        if (piece.health != 0) tag.putInt(ItemStats.HEALTH, piece.health);
        if (piece.defense != 0) tag.putInt(ItemStats.DEFENSE, piece.defense);
        if (piece.strength != 0) tag.putInt(ItemStats.STRENGTH, piece.strength);
        if (piece.intelligence != 0) tag.putInt(ItemStats.INTELLIGENCE, piece.intelligence);
        if (piece.speed != 0) tag.putInt(ItemStats.SPEED, piece.speed);
        if (piece.critChance != 0) tag.putInt(ItemStats.CRIT_CHANCE, piece.critChance);
        if (piece.critDamage != 0) tag.putInt(ItemStats.CRIT_DAMAGE, piece.critDamage);
        if (piece.ferocity != 0) tag.putInt(ItemStats.FEROCITY, piece.ferocity);
        if (piece.abilities.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (Ability a : piece.abilities) {
                if (sb.length() > 0) sb.append(',');
                sb.append(a.name());
            }
            tag.putString(RpgItems.ABILITIES_KEY, sb.toString());
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(NAMESPACE, id));
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(piece.displayName).withStyle(set.rarity.color, ChatFormatting.BOLD));
        stack.set(DataComponents.LORE, new ItemLore(Armor2Tooltip.build(stack, set, piece)));
        return stack;
    }

    public static String setKeyOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        String key = data.copyTag().getStringOr(SET_KEY, "");
        return key.isEmpty() ? null : key;
    }

    public static List<ItemStack> items() {
        List<ItemStack> out = new ArrayList<>();
        for (ArmorSet set : ArmorSet.values()) {
            for (ArmorSet.Slot slot : ArmorSet.Slot.values()) out.add(create(set, slot));
        }
        return out;
    }
}
`,
  'Armor2SetBonusHandler.java': `package com.political.expansion2.armor;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class Armor2SetBonusHandler {
    private static final int INTERVAL = 20;
    private static final EquipmentSlot[] ARMOR = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
    private Armor2SetBonusHandler() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % INTERVAL == 0) tick(server);
        });
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ArmorSet worn = wornSet(player);
            if (worn != null) worn.bonus.apply(player);
        }
    }

    private static ArmorSet wornSet(ServerPlayer player) {
        String setKey = null;
        for (EquipmentSlot slot : ARMOR) {
            String key = Armor2Items.setKeyOf(player.getItemBySlot(slot));
            if (key == null) return null;
            if (setKey == null) setKey = key;
            else if (!setKey.equals(key)) return null;
        }
        if (setKey == null) return null;
        for (ArmorSet set : ArmorSet.values()) if (set.key.equals(setKey)) return set;
        return null;
    }
}
`,
  'Armor2Tooltip.java': `package com.political.expansion2.armor;

import com.political.items.Ability;
import com.political.items.ItemStats;
import com.political.items.Rarity;
import com.political.items.SkyblockTooltipBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Armor2Tooltip {
    private Armor2Tooltip() {}

    public static List<Component> build(ItemStack stack, ArmorSet set, ArmorSet.Piece piece) {
        List<Component> lines = new ArrayList<>();
        ItemStats.Sheet s = ItemStats.compute(stack);
        Rarity rarity = ItemStats.rarityOf(stack);
        int gs = SkyblockTooltipBuilder.gearScore(s);
        int total = SkyblockTooltipBuilder.totalGearScore(s, rarity);
        lines.add(Component.literal("Gear Score: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(gs)).withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(" (" + total + ")").withStyle(ChatFormatting.DARK_GRAY)));
        skyStat(lines, "Strength", s.strength, scale(s.strength, rarity), "", ChatFormatting.RED);
        skyStat(lines, "Defense", s.defense, scale(s.defense, rarity), "", ChatFormatting.GREEN);
        skyStat(lines, "Health", s.health, scale(s.health, rarity), "", ChatFormatting.RED);
        skyStat(lines, "Crit Chance", s.critChance, s.critChance, "%", ChatFormatting.RED);
        skyStat(lines, "Crit Damage", s.critDamage, scale(s.critDamage, rarity), "%", ChatFormatting.RED);
        skyStat(lines, "Ferocity", s.ferocity, scale(s.ferocity, rarity), "", ChatFormatting.RED);
        skyStat(lines, "Speed", s.speed, scale(s.speed, rarity), "", ChatFormatting.WHITE);
        skyStat(lines, "Mana", s.intelligence, scale(s.intelligence, rarity), "", ChatFormatting.AQUA);
        if (piece.abilities.length > 0) {
            lines.add(Component.empty());
            for (Ability a : piece.abilities) {
                lines.add(Component.literal("Ability: ").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(a.displayName).withStyle(a.color, ChatFormatting.BOLD)));
                lines.add(Component.literal(a.description).withStyle(ChatFormatting.GRAY));
            }
        }
        lines.add(Component.empty());
        lines.add(Component.literal("Full Set Bonus: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(set.bonusName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
        for (String line : set.bonusLines) lines.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("Wear all 4 " + set.displayName + " pieces to activate.").withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.empty());
        lines.add(Component.literal("This item can be reforged!").withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.literal(rarity.display.toUpperCase(Locale.ROOT) + " ARMOR").withStyle(rarity.color, ChatFormatting.BOLD));
        return lines;
    }

    private static double scale(double base, Rarity rarity) { return base * rarity.mult * 1.8; }

    private static void skyStat(List<Component> lines, String label, double base, double total, String suffix, ChatFormatting color) {
        if (base == 0) return;
        MutableComponent line = Component.literal(label + ": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("+" + fmt(base) + suffix).withStyle(color));
        if (total > base + 0.01) line.append(Component.literal(" (+" + fmt(total) + suffix + ")").withStyle(ChatFormatting.DARK_GRAY));
        lines.add(line);
    }

    private static String fmt(double v) { return v == (int) v ? String.valueOf((int) v) : String.format(Locale.ROOT, "%.2f", v); }
}
`,
  'Armor2Expansion.java': `package com.political.expansion2.armor;

import net.minecraft.world.item.ItemStack;
import java.util.List;

public final class Armor2Expansion {
    private Armor2Expansion() {}
    public static void register() { Armor2SetBonusHandler.register(); }
    public static List<ItemStack> items() { return Armor2Items.items(); }
}
`,
};

for (const [name, content] of Object.entries(SUPPORT)) {
  fs.writeFileSync(path.join(ARMOR_DIR, name), content);
}

// Patch gen-armor2.js SETS block if file exists, else write full file from template
const paletteBlock = genArmor2JsPalettes();
console.log(`Generated ${SETS.length} sets (${SETS.length * 4} pieces)`);
console.log('  ->', JAVA);
console.log('  ->', LANG);
console.log('  ->', ARMOR_DIR, `(${Object.keys(SUPPORT).length} support classes)`);
console.log('Palette keys for gen-armor2.js:\n', paletteBlock);

// Export for gen-armor2.js embedding
fs.writeFileSync(path.join(__dirname, '.armor2-palettes.txt'), paletteBlock);
console.log('Run: node gen-armor2-sources.js && node gen-armor2.js');
