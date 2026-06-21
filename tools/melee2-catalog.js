/**
 * Shared catalogue for expansion2 melee (wpn2_*).
 * Used by gen-melee2.js (textures) and gen-melee2-java.js (Java enums + ability engine).
 */
const THEMES = [
  { key: 'cursed', label: 'Cursed', prefix: 'Cursed', palette: 'cursed', grip: 'obsidian' },
  { key: 'prom', label: 'Prominence', prefix: 'Prominence', palette: 'holy', grip: 'cloth_red' },
  { key: 'elem', label: 'Elemental', prefix: 'Primal', palette: 'ember', grip: 'wood' },
  { key: 'void', label: 'Void', prefix: 'Void', palette: 'void', grip: 'obsidian' },
  { key: 'cel', label: 'Celestial', prefix: 'Celestial', palette: 'frost', grip: 'lapis' },
  { key: 'blood', label: 'Blood', prefix: 'Sanguine', palette: 'blood', grip: 'leather' },
  { key: 'tech', label: 'Tech', prefix: 'Arc', palette: 'steel', grip: 'darkiron' },
  { key: 'nat', label: 'Nature', prefix: 'Verdant', palette: 'emerald', grip: 'wood' },
];

const TYPES = [
  { key: 'sword', fn: 'sword', label: 'Blade', rarity: 'COMMON' },
  { key: 'dagger', fn: 'dagger', label: 'Dagger', rarity: 'COMMON' },
  { key: 'mace', fn: 'mace', label: 'Mace', rarity: 'UNCOMMON' },
  { key: 'axe', fn: 'axe', label: 'Axe', rarity: 'UNCOMMON' },
  { key: 'spear', fn: 'spear', label: 'Spear', rarity: 'RARE' },
  { key: 'halberd', fn: 'halberd', label: 'Halberd', rarity: 'RARE' },
  { key: 'katana', fn: 'katana', label: 'Katana', rarity: 'EPIC' },
  { key: 'scythe', fn: 'scythe', label: 'Scythe', rarity: 'EPIC' },
  { key: 'greatsword', fn: 'greatsword', label: 'Greatsword', rarity: 'LEGENDARY' },
  { key: 'cleaver', fn: 'cleaver', label: 'Cleaver', rarity: 'LEGENDARY' },
  { key: 'claw', fn: 'claw', label: 'Claws', rarity: 'MYTHIC' },
  { key: 'whip', fn: 'whip', label: 'Whip', rarity: 'MYTHIC' },
];

const RARITY_STATS = {
  COMMON: { dmg: 26, str: 16, cc: 10, cd: 22, mana: 18, cdSec: 6 },
  UNCOMMON: { dmg: 42, str: 28, cc: 12, cd: 35, mana: 24, cdSec: 8 },
  RARE: { dmg: 58, str: 38, cc: 15, cd: 45, mana: 35, cdSec: 10 },
  EPIC: { dmg: 78, str: 54, cc: 18, cd: 58, mana: 50, cdSec: 14 },
  LEGENDARY: { dmg: 105, str: 78, cc: 22, cd: 78, mana: 70, cdSec: 20 },
  MYTHIC: { dmg: 145, str: 115, cc: 28, cd: 130, mana: 95, cdSec: 28 },
};

// 12 distinct cast patterns per theme block (one per weapon type)
const CAST_PATTERNS = [
  { kind: 'CONE', range: 6, tight: 0.5, dmg: 1.0, particle: 'SOUL' },
  { kind: 'TARGET', range: 6, dmg: 1.1, particle: 'CRIT', effect: 'POISON', dur: 100, amp: 1 },
  { kind: 'AROUND', range: 5, dmg: 0.95, particle: 'CRIT', effect: 'SLOWNESS', dur: 80, amp: 2 },
  { kind: 'CONE', range: 7, tight: 0.45, dmg: 1.05, particle: 'CRIT', effect: 'WEAKNESS', dur: 120, amp: 1 },
  { kind: 'BLINK_STRIKE', range: 8, blink: 3, dmg: 1.15, particle: 'SWEEP_ATTACK' },
  { kind: 'CONE', range: 10, tight: 0.55, dmg: 1.0, particle: 'ELECTRIC_SPARK', effect: 'SLOWNESS', dur: 60, amp: 1 },
  { kind: 'CONE', range: 8, tight: 0.4, dmg: 1.2, particle: 'SWEEP_ATTACK', knock: 1.2 },
  { kind: 'CONE', range: 7, tight: 0.45, dmg: 1.1, particle: 'SOUL', effect: 'WITHER', dur: 100, amp: 1 },
  { kind: 'AROUND', range: 7, dmg: 1.25, particle: 'EXPLOSION', knock: 1.3 },
  { kind: 'AROUND', range: 6, dmg: 1.15, particle: 'SNOWFLAKE', effect: 'SLOWNESS', dur: 140, amp: 3 },
  { kind: 'MULTI_BLINK', range: 10, dmg: 1.3, particle: 'CRIT' },
  { kind: 'CONE', range: 12, tight: 0.6, dmg: 1.0, particle: 'CRIT', pull: 1.1 },
];

const THEME_FLAVOR = {
  cursed: ['Domain Expansion', 'Cursed Energy', 'Inverted', 'Black Flash', 'Shrine', 'Ten Shadows', 'Limitless', 'Malevolent', 'Shibuya', 'Jujutsu', 'Grade One', 'Special Grade'],
  prom: ['Radiant', 'Solar', 'Nova', 'Ascendant', 'Paragon', 'Zenith', 'Apex', 'Luminous', 'Halcyon', 'Empyrean', 'Transcendent', 'Apotheosis'],
  elem: ['Inferno', 'Glacier', 'Tempest', 'Tidal', 'Quake', 'Plasma', 'Magma', 'Cryo', 'Storm', 'Ember', 'Frostfire', 'Cataclysm'],
  void: ['Rift', 'Null', 'Oblivion', 'Abyssal', 'Event Horizon', 'Entropy', 'Annihilation', 'Singularity', 'Eclipse', 'Dark Matter', 'Phase', 'Unmaking'],
  cel: ['Starfall', 'Nebula', 'Cosmic', 'Aurora', 'Supernova', 'Orbit', 'Pulsar', 'Quasar', 'Lunar', 'Solar Wind', 'Constellation', 'Divine'],
  blood: ['Hemorrhage', 'Crimson', 'Sanguine', 'Exsanguinate', 'Vitae', 'Carnage', 'Thirst', 'Coagulate', 'Arterial', 'Scarlet', 'Transfusion', 'Requiem'],
  tech: ['Pulse', 'Overclock', 'Circuit', 'Ion', 'Plasma', 'Rail', 'Photon', 'Quantum', 'Nano', 'Fusion', 'Voltage', 'Singularity'],
  nat: ['Thorn', 'Bloom', 'Root', 'Canopy', 'Spore', 'Pollen', 'Grove', 'Wild', 'Feral', 'Moss', 'Petal', 'Overgrowth'],
};

const THEME_PARTICLES = {
  cursed: ['SOUL', 'REVERSE_PORTAL', 'WITCH', 'SOUL', 'END_ROD', 'SOUL'],
  prom: ['END_ROD', 'FIREWORK', 'GLOW', 'END_ROD', 'FIREWORK', 'END_ROD'],
  elem: ['FLAME', 'SNOWFLAKE', 'ELECTRIC_SPARK', 'BUBBLE', 'EXPLOSION', 'LAVA'],
  void: ['REVERSE_PORTAL', 'PORTAL', 'SOUL', 'WITCH', 'REVERSE_PORTAL', 'END_ROD'],
  cel: ['END_ROD', 'FIREWORK', 'GLOW', 'END_ROD', 'FIREWORK', 'END_ROD'],
  blood: ['CRIT', 'DAMAGE_INDICATOR', 'CRIT', 'SOUL', 'CRIT', 'DAMAGE_INDICATOR'],
  tech: ['ELECTRIC_SPARK', 'CRIT', 'ELECTRIC_SPARK', 'FIREWORK', 'ELECTRIC_SPARK', 'CRIT'],
  nat: ['HAPPY_VILLAGER', 'SPORE_BLOSSOM_AIR', 'COMPOSTER', 'HAPPY_VILLAGER', 'SPORE_BLOSSOM_AIR', 'COMPOSTER'],
};

function abilityEnumName(theme, typeIdx) {
  return `${theme.key.toUpperCase()}_${['SLASH','STAB','CRUSH','REND','LUNGE','REACH','DRAW','REAP','SMASH','CHOP','FRENZY','LASH'][typeIdx]}`;
}

function abilityDisplay(theme, typeIdx) {
  const verb = THEME_FLAVOR[theme.key][typeIdx];
  const type = TYPES[typeIdx];
  return `${verb} ${type.label === 'Blade' ? 'Cut' : type.label.replace(/s$/, '')}`;
}

function abilityDesc(theme, typeIdx, pattern) {
  const t = theme.label.toLowerCase();
  switch (pattern.kind) {
    case 'CONE': return `Unleash ${t} energy in a cone ahead${pattern.effect ? ', afflicting foes.' : '.'}`;
    case 'TARGET': return `Strike a foe with concentrated ${t} force.`;
    case 'AROUND': return `Erupt ${t} power around you, hitting all nearby foes.`;
    case 'BLINK_STRIKE': return `Blink forward and deliver a ${t} strike.`;
    case 'MULTI_BLINK': return `Blink between nearby foes, cutting each with ${t} fury.`;
    default: return `Channel ${t} power through this weapon.`;
  }
}

function statBonus(rarity, typeIdx) {
  const b = {};
  if (rarity === 'RARE' || rarity === 'EPIC') { b.hp = 20; b.def = 10; }
  if (rarity === 'LEGENDARY' || rarity === 'MYTHIC') { b.hp = 35; b.def = 15; b.intel = 25; }
  if (typeIdx === 1 || typeIdx === 10) b.spd = rarity === 'MYTHIC' ? 14 : 8;
  if (typeIdx === 6 || typeIdx === 5) b.intel = b.intel || (rarity === 'EPIC' ? 20 : 15);
  if (typeIdx === 8 || typeIdx === 2) b.fer = rarity === 'MYTHIC' ? 15 : 6;
  return b;
}

function buildCatalog() {
  const weapons = [];
  let typeIdxGlobal = 0;
  for (const theme of THEMES) {
    for (let ti = 0; ti < TYPES.length; ti++) {
      const type = TYPES[ti];
      const id = `wpn2_${theme.key}_${type.key}`;
      const rarity = type.rarity;
      const rs = RARITY_STATS[rarity];
      const pattern = { ...CAST_PATTERNS[ti] };
      const themeParticles = THEME_PARTICLES[theme.key];
      pattern.particle = themeParticles[ti % themeParticles.length];
      if (theme.key === 'elem') {
        if (ti % 3 === 0) pattern.ignite = true;
        if (ti % 3 === 1) pattern.effect = 'SLOWNESS';
        if (ti % 3 === 2) pattern.effect = 'POISON';
      }
      if (theme.key === 'blood' && !pattern.effect) pattern.effect = 'POISON';
      if (theme.key === 'void' && pattern.kind === 'AROUND') pattern.effect = 'WITHER';
      if (theme.key === 'cel' && pattern.kind === 'CONE') pattern.heal = 0.15;
      if (theme.key === 'cursed' && pattern.kind !== 'TARGET') pattern.effect = pattern.effect || 'WITHER';
      if (theme.key === 'tech' && pattern.particle === 'ELECTRIC_SPARK') pattern.lightning = pattern.kind === 'TARGET';
      if (theme.key === 'nat' && pattern.kind === 'AROUND') pattern.heal = 0.2;

      const bonus = statBonus(rarity, ti);
      const abName = abilityEnumName(theme, ti);
      const displayName = `${theme.prefix} ${type.label}`;
      weapons.push({
        id,
        displayName,
        archetype: type.key,
        theme: theme.key,
        rarity,
        stats: {
          damage: rs.dmg + ti * 2,
          strength: rs.str + ti,
          critChance: rs.cc + (ti % 3) * 2,
          critDamage: rs.cd + ti * 2,
          ferocity: bonus.fer || 0,
          intelligence: bonus.intel || (rarity === 'MYTHIC' ? 55 : rarity === 'LEGENDARY' ? 30 : 0),
          health: bonus.hp || 0,
          defense: bonus.def || 0,
          speed: bonus.spd || 0,
        },
        ability: {
          enumName: abName,
          displayName: abilityDisplay(theme, ti),
          description: abilityDesc(theme, ti, pattern),
          manaCost: rs.mana + ti * 2,
          cooldownSeconds: rs.cdSec + Math.floor(ti / 4),
          pattern,
        },
        texture: { fn: type.fn, palette: theme.palette, grip: theme.grip },
      });
      typeIdxGlobal++;
    }
  }
  return weapons;
}

module.exports = { THEMES, TYPES, buildCatalog };
