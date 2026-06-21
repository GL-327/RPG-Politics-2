/**
 * Builds tools/mob2-roster.json (source of truth for phase-2 creatures).
 */
const fs = require('fs');
const path = require('path');

const BIOMES = {
  fire: 'IS_BADLANDS',
  ice: 'IS_TAIGA',
  storm: 'IS_MOUNTAIN',
  earth: 'IS_HILL',
  shadow: 'IS_FOREST',
  holy: 'IS_PLAINS',
  arcane: 'IS_JUNGLE',
  bandit: 'IS_SAVANNA',
  knight: 'IS_PLAINS',
  cultist: 'IS_FOREST',
  undead: 'IS_TAIGA',
  demon: 'IS_BADLANDS',
  fey: 'IS_BIRCH_FOREST',
  construct: 'IS_MOUNTAIN',
  wild: 'overworld',
  mount: 'IS_PLAINS',
};

function mob(id, name, role, lineage, opts = {}) {
  const isBoss = role === 'BOSS';
  const isMini = role === 'MINIBOSS';
  const isNeutral = role === 'NEUTRAL' || role === 'SKITTISH';
  const tier = isBoss ? 4 : isMini ? 3 : isNeutral ? 1 : 2;
  const hp = opts.hp ?? (tier === 4 ? 280 + (opts.bossExtra || 0) : tier === 3 ? 120 : tier === 2 ? 28 : 40);
  const dmg = opts.dmg ?? (tier === 4 ? 18 : tier === 3 ? 13 : tier === 2 ? 5 : 4);
  const armor = opts.armor ?? (tier === 4 ? 12 : tier === 3 ? 10 : tier === 2 ? 2 : 3);
  const speed = opts.speed ?? (tier === 4 ? 0.28 : tier === 3 ? 0.24 : 0.30);
  const scale = opts.scale ?? (tier >= 3 ? 1.25 : 1.0);
  const w = opts.w ?? (tier >= 3 ? 0.9 : 0.6);
  const h = opts.h ?? (tier >= 3 ? 2.4 : 1.95);
  const coinMin = opts.coinMin ?? (tier === 4 ? 150 : tier === 3 ? 40 : tier === 2 ? 3 : 2);
  const coinMax = opts.coinMax ?? (tier === 4 ? 300 : tier === 3 ? 80 : tier === 2 ? 8 : 6);
  const spawnBiome = opts.spawnBiome ?? BIOMES[lineage] ?? 'overworld';
  const spawn = isBoss && !opts.naturalBoss
    ? null
    : opts.spawn ?? { biome: spawnBiome, weight: tier === 3 ? 1 : tier === 2 ? 8 : 5, min: 1, max: tier === 2 ? 2 : 1 };

  const entry = {
    id: `mob2_${id}`,
    name,
    role,
    hp, dmg, armor, speed,
    kb: opts.kb ?? (tier >= 3 ? 0.7 : 0.1),
    range: opts.range ?? (tier >= 3 ? 40 : 32),
    w, h, scale,
    coinMin, coinMax,
    lineage,
  };
  if (opts.brute || tier >= 3) entry.brute = true;
  if (opts.raids) entry.raids = true;
  if (opts.fireproof || lineage === 'fire' || lineage === 'demon') entry.fireproof = true;
  if (opts.ignite || (lineage === 'fire' && tier === 2)) entry.ignite = true;
  if (opts.lightning || (lineage === 'storm' && tier >= 2)) entry.lightning = true;
  if (opts.lifesteal) entry.lifesteal = opts.lifesteal;
  if (opts.onHit) { entry.onHit = opts.onHit; entry.onHitDur = opts.onHitDur; entry.onHitAmp = opts.onHitAmp; }
  if (opts.aura) { entry.aura = opts.aura; entry.auraAmp = opts.auraAmp; }
  if (opts.summon) { entry.summon = opts.summon; entry.summonCount = opts.summonCount ?? 3; }
  if (opts.drops) entry.drops = opts.drops;
  else entry.drops = defaultDrops(lineage, tier);
  if (spawn) entry.spawn = spawn;
  return entry;
}

function defaultDrops(lineage, tier) {
  const common = {
    fire: [{ item: 'COAL', min: 1, max: 2, chance: 0.7 }, { item: 'MAGMA_CREAM', min: 1, max: 1, chance: 0.3 }],
    ice: [{ item: 'PACKED_ICE', min: 1, max: 1, chance: 0.5 }, { item: 'SNOWBALL', min: 1, max: 3, chance: 0.6 }],
    storm: [{ item: 'COPPER_INGOT', min: 1, max: 2, chance: 0.6 }],
    earth: [{ item: 'COBBLESTONE', min: 2, max: 4, chance: 0.8 }, { item: 'IRON_NUGGET', min: 1, max: 3, chance: 0.4 }],
    shadow: [{ item: 'GLOWSTONE_DUST', min: 1, max: 1, chance: 0.3 }, { item: 'ROTTEN_FLESH', min: 1, max: 2, chance: 0.5 }],
    holy: [{ item: 'GOLD_NUGGET', min: 1, max: 2, chance: 0.5 }, { item: 'QUARTZ', min: 1, max: 1, chance: 0.3 }],
    arcane: [{ item: 'AMETHYST_SHARD', min: 1, max: 2, chance: 0.5 }, { item: 'REDSTONE', min: 1, max: 2, chance: 0.4 }],
    bandit: [{ item: 'LEATHER', min: 1, max: 2, chance: 0.6 }, { item: 'EMERALD', min: 1, max: 1, chance: 0.15 }],
    knight: [{ item: 'IRON_INGOT', min: 1, max: 1, chance: 0.4 }, { item: 'BONE', min: 1, max: 2, chance: 0.5 }],
    cultist: [{ item: 'REDSTONE', min: 1, max: 2, chance: 0.5 }, { item: 'SPIDER_EYE', min: 1, max: 1, chance: 0.3 }],
    undead: [{ item: 'BONE', min: 1, max: 3, chance: 0.8 }, { item: 'ROTTEN_FLESH', min: 1, max: 2, chance: 0.7 }],
    demon: [{ item: 'BLAZE_POWDER', min: 1, max: 1, chance: 0.3 }, { item: 'MAGMA_CREAM', min: 1, max: 1, chance: 0.4 }],
    fey: [{ item: 'GLOW_BERRIES', min: 1, max: 2, chance: 0.5 }, { item: 'OAK_SAPLING', min: 1, max: 1, chance: 0.3 }],
    construct: [{ item: 'IRON_INGOT', min: 1, max: 2, chance: 0.6 }, { item: 'REDSTONE', min: 1, max: 1, chance: 0.4 }],
    wild: [{ item: 'LEATHER', min: 1, max: 2, chance: 0.5 }],
    mount: [{ item: 'LEATHER', min: 1, max: 3, chance: 0.4 }, { item: 'WHEAT', min: 1, max: 2, chance: 0.3 }],
  };
  const drops = [...(common[lineage] || common.wild)];
  if (tier === 3) drops.push({ item: 'DIAMOND', min: 1, max: 1, chance: 0.2 });
  if (tier === 4) drops.push({ item: 'DIAMOND', min: 2, max: 4, chance: 1.0 }, { item: 'EMERALD', min: 3, max: 6, chance: 0.8 });
  return drops;
}

const roster = [];

// ---- Elemental lineages (7 x 9 = 63) ----
const elementals = [
  ['fire', [
    ['ember_scout', 'Ember Scout', 'HOSTILE', { onHit: 'SLOWNESS' }],
    ['flame_acolyte', 'Flame Acolyte', 'HOSTILE', { aura: 'WEAKNESS' }],
    ['magma_brute', 'Magma Brute', 'HOSTILE', { brute: true, ignite: true }],
    ['cinder_stalker', 'Cinder Stalker', 'HOSTILE', { speed: 0.34 }],
    ['pyre_cultist', 'Pyre Cultist', 'HOSTILE', { onHit: 'POISON' }],
    ['salamander_beast', 'Salamander Beast', 'NEUTRAL', { fireproof: true }],
    ['volcano_warden', 'Volcano Warden', 'MINIBOSS', { ignite: true }],
    ['inferno_champion', 'Inferno Champion', 'MINIBOSS', { ignite: true, bossExtra: 20 }],
    ['infernal_sovereign', 'Infernal Sovereign', 'BOSS', { summon: 'mob2_ember_scout', summonCount: 4, ignite: true }],
  ]],
  ['ice', [
    ['frost_shardling', 'Frost Shardling', 'HOSTILE', { onHit: 'SLOWNESS' }],
    ['glacial_hunter', 'Glacial Hunter', 'HOSTILE', {}],
    ['ice_acolyte', 'Ice Acolyte', 'HOSTILE', { aura: 'SLOWNESS', auraAmp: 0 }],
    ['blizzard_herald', 'Blizzard Herald', 'HOSTILE', { onHit: 'SLOWNESS', onHitAmp: 1 }],
    ['rime_wraith', 'Rime Wraith', 'HOSTILE', { onHit: 'BLINDNESS' }],
    ['arctic_fox_beast', 'Arctic Fox Beast', 'NEUTRAL', {}],
    ['glacier_titan', 'Glacier Titan', 'MINIBOSS', { onHit: 'SLOWNESS', onHitAmp: 1 }],
    ['frost_colossus', 'Frost Colossus', 'MINIBOSS', { bossExtra: 15 }],
    ['frost_queen', 'Frost Queen', 'BOSS', { summon: 'mob2_frost_shardling', summonCount: 5, onHit: 'SLOWNESS', onHitAmp: 1 }],
  ]],
  ['storm', [
    ['spark_imp', 'Spark Imp', 'HOSTILE', { lightning: true }],
    ['thunder_acolyte', 'Thunder Acolyte', 'HOSTILE', { lightning: true }],
    ['storm_brute', 'Storm Brute', 'HOSTILE', { brute: true }],
    ['gale_stalker', 'Gale Stalker', 'HOSTILE', { speed: 0.33 }],
    ['tempest_cultist', 'Tempest Cultist', 'HOSTILE', { aura: 'WEAKNESS' }],
    ['storm_eagle_beast', 'Storm Eagle Beast', 'NEUTRAL', { lightning: true }],
    ['tempest_archon', 'Tempest Archon', 'MINIBOSS', { lightning: true }],
    ['lightning_lord', 'Lightning Lord', 'MINIBOSS', { lightning: true }],
    ['thunder_god', 'Thunder God', 'BOSS', { summon: 'mob2_spark_imp', summonCount: 4, lightning: true }],
  ]],
  ['earth', [
    ['stone_scout', 'Stone Scout', 'HOSTILE', {}],
    ['mud_brute', 'Mud Brute', 'HOSTILE', { brute: true }],
    ['boulder_stalker', 'Boulder Stalker', 'HOSTILE', { onHit: 'SLOWNESS' }],
    ['earth_knight', 'Earth Knight', 'HOSTILE', { raids: true }],
    ['root_cultist', 'Root Cultist', 'HOSTILE', { onHit: 'POISON' }],
    ['cave_bear_beast', 'Cave Bear Beast', 'NEUTRAL', { brute: true }],
    ['granite_colossus', 'Granite Colossus', 'MINIBOSS', {}],
    ['quake_sentinel', 'Quake Sentinel', 'MINIBOSS', { onHit: 'SLOWNESS', onHitAmp: 1 }],
    ['earthbound_titan', 'Earthbound Titan', 'BOSS', { summon: 'mob2_stone_scout', summonCount: 5 }],
  ]],
  ['shadow', [
    ['shade_scout', 'Shade Scout', 'HOSTILE', { onHit: 'BLINDNESS' }],
    ['night_acolyte', 'Night Acolyte', 'HOSTILE', { aura: 'WEAKNESS' }],
    ['shadow_brute', 'Shadow Brute', 'HOSTILE', { brute: true }],
    ['void_stalker', 'Void Stalker', 'HOSTILE', { speed: 0.35 }],
    ['phantom_wraith', 'Phantom Wraith', 'HOSTILE', { onHit: 'BLINDNESS' }],
    ['shadow_wolf_beast', 'Shadow Wolf Beast', 'NEUTRAL', {}],
    ['abyss_knight', 'Abyss Knight', 'MINIBOSS', { onHit: 'WITHER', onHitDur: 60 }],
    ['void_reaper', 'Void Reaper', 'MINIBOSS', { lifesteal: 2 }],
    ['shadow_emperor', 'Shadow Emperor', 'BOSS', { summon: 'mob2_shade_scout', summonCount: 4, aura: 'WEAKNESS', auraAmp: 1 }],
  ]],
  ['holy', [
    ['light_scout', 'Light Scout', 'HOSTILE', {}],
    ['solar_acolyte', 'Solar Acolyte', 'HOSTILE', { aura: 'GLOWING' }],
    ['radiant_knight', 'Radiant Knight', 'HOSTILE', { raids: true }],
    ['zealot_guard', 'Zealot Guard', 'HOSTILE', {}],
    ['sun_herald', 'Sun Herald', 'HOSTILE', { onHit: 'GLOWING' }],
    ['dawn_stag_beast', 'Dawn Stag Beast', 'NEUTRAL', {}],
    ['seraph_champion', 'Seraph Champion', 'MINIBOSS', {}],
    ['aegis_sentinel', 'Aegis Sentinel', 'MINIBOSS', { bossExtra: 10 }],
    ['seraph_lord', 'Seraph Lord', 'BOSS', { summon: 'mob2_light_scout', summonCount: 3 }],
  ]],
  ['arcane', [
    ['arcane_scout', 'Arcane Scout', 'HOSTILE', {}],
    ['mana_wisp', 'Mana Wisp', 'HOSTILE', { speed: 0.36 }],
    ['spell_brute', 'Spell Brute', 'HOSTILE', { brute: true }],
    ['rune_stalker', 'Rune Stalker', 'HOSTILE', { onHit: 'WEAKNESS' }],
    ['ether_cultist', 'Ether Cultist', 'HOSTILE', { aura: 'WEAKNESS' }],
    ['arcane_owl_beast', 'Arcane Owl Beast', 'NEUTRAL', {}],
    ['arcane_titan', 'Arcane Titan', 'MINIBOSS', {}],
    ['rune_warden', 'Rune Warden', 'MINIBOSS', { onHit: 'SLOWNESS' }],
    ['archmage_sovereign', 'Archmage Sovereign', 'BOSS', { summon: 'mob2_arcane_scout', summonCount: 4, aura: 'WEAKNESS', auraAmp: 1 }],
  ]],
];

for (const [lineage, entries] of elementals) {
  for (const [id, name, role, opts] of entries) {
    roster.push(mob(id, name, role, lineage, opts));
  }
}

// ---- Faction armies (7 x 5 = 35) ----
const factions = [
  ['bandit', [
    ['highway_robber', 'Highway Robber', 'HOSTILE', { raids: true }],
    ['cutthroat', 'Cutthroat', 'HOSTILE', { speed: 0.32 }],
    ['smuggler', 'Smuggler', 'HOSTILE', {}],
    ['marauder_captain', 'Marauder Captain', 'MINIBOSS', { raids: true }],
    ['bandit_king', 'Bandit King', 'BOSS', { summon: 'mob2_highway_robber', summonCount: 4, raids: true }],
  ]],
  ['knight', [
    ['squire_errant', 'Squire Errant', 'HOSTILE', {}],
    ['order_knight', 'Order Knight', 'HOSTILE', { raids: true }],
    ['broken_paladin', 'Broken Paladin', 'HOSTILE', { onHit: 'WEAKNESS' }],
    ['crusader_veteran', 'Crusader Veteran', 'MINIBOSS', { raids: true }],
    ['grand_marshal', 'Grand Marshal', 'BOSS', { summon: 'mob2_order_knight', summonCount: 3, raids: true }],
  ]],
  ['cultist', [
    ['blood_acolyte', 'Blood Acolyte', 'HOSTILE', { onHit: 'POISON' }],
    ['ritual_guard', 'Ritual Guard', 'HOSTILE', {}],
    ['doom_prophet', 'Doom Prophet', 'HOSTILE', { aura: 'WEAKNESS' }],
    ['heretic_inquisitor', 'Heretic Inquisitor', 'MINIBOSS', { aura: 'WEAKNESS', auraAmp: 1 }],
    ['apocalypse_herald', 'Apocalypse Herald', 'BOSS', { summon: 'mob2_blood_acolyte', summonCount: 5, aura: 'WITHER', auraAmp: 0 }],
  ]],
  ['undead', [
    ['rot_walker', 'Rot Walker', 'HOSTILE', { onHit: 'HUNGER' }],
    ['tomb_guard', 'Tomb Guard', 'HOSTILE', {}],
    ['banshee_shade', 'Banshee Shade', 'HOSTILE', { onHit: 'BLINDNESS' }],
    ['death_knight', 'Death Knight', 'MINIBOSS', { onHit: 'WITHER', onHitDur: 80 }],
    ['dread_lich', 'Dread Lich', 'BOSS', { summon: 'mob2_tomb_guard', summonCount: 5, aura: 'WEAKNESS', auraAmp: 1 }],
  ]],
  ['demon', [
    ['imp_scout', 'Imp Scout', 'HOSTILE', { fireproof: true }],
    ['hellhound', 'Hellhound', 'HOSTILE', { ignite: true }],
    ['demon_soldier', 'Demon Soldier', 'HOSTILE', { brute: true }],
    ['pit_commander', 'Pit Commander', 'MINIBOSS', { ignite: true }],
    ['demon_prince', 'Demon Prince', 'BOSS', { summon: 'mob2_imp_scout', summonCount: 4, ignite: true }],
  ]],
  ['fey', [
    ['pixie_trickster', 'Pixie Trickster', 'HOSTILE', { speed: 0.38 }],
    ['spriggan', 'Spriggan', 'HOSTILE', { onHit: 'POISON' }],
    ['unseelie_stalker', 'Unseelie Stalker', 'HOSTILE', {}],
    ['unseelie_knight', 'Unseelie Knight', 'MINIBOSS', { onHit: 'BLINDNESS' }],
    ['fey_queen', 'Fey Queen', 'BOSS', { summon: 'mob2_pixie_trickster', summonCount: 4 }],
  ]],
  ['construct', [
    ['rust_automaton', 'Rust Automaton', 'HOSTILE', {}],
    ['clockwork_soldier', 'Clockwork Soldier', 'HOSTILE', {}],
    ['iron_golem_sentry', 'Iron Golem Sentry', 'HOSTILE', { brute: true }],
    ['siege_construct', 'Siege Construct', 'MINIBOSS', { brute: true }],
    ['colossus_prime', 'Colossus Prime', 'BOSS', { summon: 'mob2_rust_automaton', summonCount: 3 }],
  ]],
];

for (const [lineage, entries] of factions) {
  for (const [id, name, role, opts] of entries) {
    roster.push(mob(id, name, role, lineage, opts));
  }
}

// ---- Raid lieutenant mini-bosses (6) ----
const lieutenants = [
  ['raid_flame_warden', 'Raid Flame Warden', 'fire', { ignite: true }],
  ['raid_frost_warden', 'Raid Frost Warden', 'ice', { onHit: 'SLOWNESS', onHitAmp: 1 }],
  ['raid_storm_warden', 'Raid Storm Warden', 'storm', { lightning: true }],
  ['raid_void_warden', 'Raid Void Warden', 'shadow', { aura: 'BLINDNESS' }],
  ['raid_solar_warden', 'Raid Solar Warden', 'holy', {}],
  ['raid_arcane_warden', 'Raid Arcane Warden', 'arcane', { aura: 'WEAKNESS' }],
];
for (const [id, name, lineage, opts] of lieutenants) {
  roster.push(mob(id, name, 'MINIBOSS', lineage, { ...opts, spawn: { biome: 'overworld', weight: 1, min: 1, max: 1 } }));
}

// ---- Extra raid bosses (2) ----
roster.push(mob('cataclysm_herald', 'Cataclysm Herald', 'BOSS', 'storm', {
  lightning: true, summon: 'mob2_tempest_cultist', summonCount: 6, bossExtra: 40,
}));
roster.push(mob('eternal_necromancer', 'Eternal Necromancer', 'BOSS', 'undead', {
  aura: 'WITHER', auraAmp: 0, summon: 'mob2_rot_walker', summonCount: 6, bossExtra: 30,
}));

// ---- Neutral wildlife (5) ----
const wildlife = [
  ['marsh_buffalo', 'Marsh Buffalo', 'NEUTRAL', { brute: true, spawnBiome: 'overworld' }],
  ['highland_ram', 'Highland Ram', 'NEUTRAL', { spawnBiome: 'IS_MOUNTAIN' }],
  ['jungle_panther', 'Jungle Panther', 'NEUTRAL', { speed: 0.34, spawnBiome: 'IS_JUNGLE' }],
  ['canyon_vulture', 'Canyon Vulture', 'SKITTISH', { spawnBiome: 'IS_BADLANDS' }],
  ['river_turtle_beast', 'River Turtle Beast', 'NEUTRAL', { spawnBiome: 'overworld', hp: 50, armor: 8 }],
];
for (const [id, name, role, opts] of wildlife) {
  roster.push(mob(id, name, role, 'wild', opts));
}

// ---- Mount-like neutrals (4, visual only) ----
const mounts = [
  ['plains_steed', 'Plains Steed', 'SKITTISH', { spawnBiome: 'IS_PLAINS', speed: 0.36, scale: 1.1 }],
  ['desert_camel_beast', 'Desert Camel Beast', 'SKITTISH', { spawnBiome: 'IS_SAVANNA', scale: 1.15 }],
  ['mountain_goat_beast', 'Mountain Goat Beast', 'SKITTISH', { spawnBiome: 'IS_MOUNTAIN', speed: 0.35 }],
  ['swamp_draft_beast', 'Swamp Draft Beast', 'SKITTISH', { spawnBiome: 'overworld', scale: 1.2 }],
];
for (const [id, name, role, opts] of mounts) {
  roster.push(mob(id, name, role, 'mount', opts));
}

const out = path.join(__dirname, 'mob2-roster.json');
fs.writeFileSync(out, JSON.stringify(roster, null, 2));
console.log('mob2-roster.json:', roster.length, 'creatures');
const roles = roster.reduce((a, m) => { a[m.role] = (a[m.role] || 0) + 1; return a; }, {});
console.log('roles:', roles);
