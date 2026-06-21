/**
 * Shared catalogue for expansion2 accessories (acc2_*).
 * Used by gen-accessories2.js for textures, lang, docs, and Java catalog generation.
 */

const RARITIES = ['COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY', 'MYTHIC'];

const ACC_TYPES = [
  { key: 'talisman', type: 'TALISMAN', draw: 'talisman', baseRarity: 0 },
  { key: 'ring', type: 'RING', draw: 'ring', baseRarity: 0 },
  { key: 'charm', type: 'CHARM', draw: 'charm', baseRarity: 1 },
  { key: 'band', type: 'BAND', draw: 'band', baseRarity: 1 },
  { key: 'amulet', type: 'AMULET', draw: 'amulet', baseRarity: 2 },
  { key: 'badge', type: 'BADGE', draw: 'badge', baseRarity: 2 },
  { key: 'rune', type: 'RUNE', draw: 'rune', baseRarity: 3 },
  { key: 'totem', type: 'TOTEM', draw: 'totem', baseRarity: 3 },
  { key: 'artifact', type: 'ARTIFACT', draw: 'artifact', baseRarity: 4 },
  { key: 'relic', type: 'RELIC', draw: 'relic', baseRarity: 5 },
];

const THEMES = [
  {
    key: 'ward', label: 'Ward', prefix: 'Warding', palette: { metal: 'steel', gem: 'diamond', accent: 'frost' },
    flavor: 'Turns aside blows with quiet resolve.',
    stats: (tier) => ({ health: 8 + tier * 12, defense: 6 + tier * 10, toughness: tier * 0.8 }),
    effects: (tier) => tier >= 3 ? [{ effect: 'RESISTANCE', amp: 0, label: 'Resistance I' }] : [],
  },
  {
    key: 'vigor', label: 'Vigor', prefix: 'Vigor', palette: { metal: 'gold', gem: 'ruby', accent: 'crimson' },
    flavor: 'Life pulses through the metal like a second heartbeat.',
    stats: (tier) => ({ health: 15 + tier * 18, knockback: 0.05 + tier * 0.06 }),
    effects: (tier) => tier >= 4 ? [{ effect: 'REGENERATION', amp: 0, label: 'Regeneration I' }] : [],
  },
  {
    key: 'berserk', label: 'Berserk', prefix: 'Berserker', palette: { metal: 'darkiron', gem: 'crimson', accent: 'blood' },
    flavor: 'It hungers for the thrill of the fray.',
    stats: (tier) => ({ strength: 8 + tier * 8, ferocity: 2 + tier * 2, critDamage: 10 + tier * 12 }),
    effects: (tier) => tier >= 2 ? [{ effect: 'STRENGTH', amp: 0, label: 'Strength I' }] : [],
  },
  {
    key: 'swift', label: 'Swift', prefix: 'Swift', palette: { metal: 'silver', gem: 'frost', accent: 'aqua' },
    flavor: 'Your steps grow light and quick.',
    stats: (tier) => ({ speed: 8 + tier * 8, attackSpeed: 4 + tier * 5 }),
    effects: (tier) => [{ effect: 'SPEED', amp: Math.min(tier, 1), label: tier >= 2 ? 'Speed II' : 'Speed I' }],
  },
  {
    key: 'scholar', label: 'Scholar', prefix: 'Scholar', palette: { metal: 'gold', gem: 'lapis', accent: 'arcane' },
    flavor: 'Arcane study replenishes the mind.',
    stats: (tier) => ({ manaRegen: 1 + tier * 1.5, luck: tier * 0.5 }),
    effects: (tier) => tier >= 3 ? [{ effect: 'NIGHT_VISION', amp: 0, label: 'Night Vision' }] : [],
  },
  {
    key: 'cursed', label: 'Cursed', prefix: 'Cursed', palette: { metal: 'obsidian', gem: 'cursed', accent: 'void' },
    flavor: 'A sealed sliver of cursed energy seeps back into you.',
    stats: (tier) => ({ cursedRegen: 1 + tier * 0.8, strength: tier * 4 }),
    effects: (tier) => tier >= 4 ? [{ effect: 'DARKNESS', amp: 0, label: 'Darkness (harmless aura)' }] : [],
  },
  {
    key: 'assassin', label: 'Assassin', prefix: 'Shadow', palette: { metal: 'shadow', gem: 'amethyst', accent: 'void' },
    flavor: 'Strike from the shadows, swift and sure.',
    stats: (tier) => ({ critChance: 4 + tier * 4, critDamage: 12 + tier * 14, speed: tier * 4 }),
    effects: (tier) => tier >= 3 ? [{ effect: 'INVISIBILITY', amp: 0, label: 'Invisibility (faint)' }] : [],
  },
  {
    key: 'phoenix', label: 'Phoenix', prefix: 'Phoenix', palette: { metal: 'gold', gem: 'ember', accent: 'crimson' },
    flavor: 'Wreathed in embers that never burn its bearer.',
    stats: (tier) => ({ health: 10 + tier * 10, defense: tier * 6 }),
    effects: (tier) => [{ effect: 'FIRE_RESISTANCE', amp: 0, label: 'Fire Resistance' }],
  },
  {
    key: 'frost', label: 'Frost', prefix: 'Frost', palette: { metal: 'steel', gem: 'frost', accent: 'aqua' },
    flavor: 'Cold clarity sharpens every movement.',
    stats: (tier) => ({ defense: 5 + tier * 8, speed: tier * 5 }),
    effects: (tier) => tier >= 2 ? [{ effect: 'SLOW_FALLING', amp: 0, label: 'Slow Falling' }] : [],
  },
  {
    key: 'void', label: 'Void', prefix: 'Void', palette: { metal: 'netherite', gem: 'void', accent: 'arcane' },
    flavor: 'The abyss whispers power into your bones.',
    stats: (tier) => ({
      health: tier * 15, defense: tier * 10, strength: tier * 8,
      critChance: tier * 3, manaRegen: tier * 1.2, cursedRegen: tier * 0.8,
    }),
    effects: (tier) => tier >= 4 ? [{ effect: 'STRENGTH', amp: 0, label: 'Strength I' }] : [],
  },
];

const MYTHIC_STANDALONES = [
  {
    id: 'acc2_paragon_crest', name: 'Paragon Crest', type: 'BADGE', rarity: 'MYTHIC',
    flavor: 'The mark of one who has surpassed every trial.',
    stats: { health: 100, defense: 50, strength: 30, manaRegen: 6, critChance: 10 },
    effects: [{ effect: 'RESISTANCE', amp: 0, label: 'Resistance I' }],
    palette: { metal: 'gold', gem: 'diamond', accent: 'arcane', draw: 'badge' },
  },
  {
    id: 'acc2_eternal_sigil', name: 'Eternal Sigil', type: 'RUNE', rarity: 'MYTHIC',
    flavor: 'A rune carved before the first age of kings.',
    stats: { health: 80, defense: 40, cursedRegen: 5, manaRegen: 8 },
    effects: [{ effect: 'REGENERATION', amp: 1, label: 'Regeneration II' }],
    palette: { metal: 'obsidian', gem: 'void', accent: 'soul', draw: 'rune' },
  },
  {
    id: 'acc2_sovereign_relic', name: 'Sovereign Relic', type: 'RELIC', rarity: 'MYTHIC',
    flavor: 'The crown-jewel of a fallen empire.',
    stats: { health: 140, defense: 70, strength: 50, critDamage: 80, ferocity: 15 },
    effects: [{ effect: 'STRENGTH', amp: 1, label: 'Strength II' }],
    palette: { metal: 'gold', gem: 'ruby', accent: 'blood', draw: 'relic' },
  },
  {
    id: 'acc2_ascendant_orb', name: 'Ascendant Orb', type: 'ARTIFACT', rarity: 'MYTHIC',
    flavor: 'A miniature star bound in gold filigree.',
    stats: { manaRegen: 12, speed: 30, attackSpeed: 25, luck: 4 },
    effects: [{ effect: 'JUMP_BOOST', amp: 2, label: 'Jump Boost III' }],
    palette: { metal: 'gold', gem: 'arcane', accent: 'frost', draw: 'artifact' },
  },
  {
    id: 'acc2_abaddon_talisman', name: 'Abaddon Talisman', type: 'TALISMAN', rarity: 'MYTHIC',
    flavor: 'Named for the end of all things.',
    stats: { strength: 70, critChance: 25, critDamage: 120, ferocity: 22, cursedRegen: 4 },
    effects: [{ effect: 'STRENGTH', amp: 1, label: 'Strength II' }, { effect: 'FIRE_RESISTANCE', amp: 0, label: 'Fire Resistance' }],
    palette: { metal: 'darkiron', gem: 'void', accent: 'blood', draw: 'talisman' },
  },
];

// ---- Consumables ----

const POTION_LINES = [
  { suffix: 'healing_draught', name: 'Healing Draught', type: 'POTION', rarity: 'COMMON', frac: 0.25, resource: 'health', msg: 'Warmth knits your wounds.', color: 'RED' },
  { suffix: 'healing_potion', name: 'Healing Potion', type: 'POTION', rarity: 'UNCOMMON', frac: 0.50, resource: 'health', msg: 'Your wounds close swiftly.', color: 'RED' },
  { suffix: 'greater_healing_potion', name: 'Greater Healing Potion', type: 'POTION', rarity: 'RARE', frac: 1.0, resource: 'health', msg: 'You are made whole again.', color: 'RED' },
  { suffix: 'mana_draught', name: 'Mana Draught', type: 'POTION', rarity: 'COMMON', frac: 0.25, resource: 'mana', msg: 'Mana trickles back.', color: 'AQUA' },
  { suffix: 'mana_potion', name: 'Mana Potion', type: 'POTION', rarity: 'UNCOMMON', frac: 0.50, resource: 'mana', msg: 'Your mind fills with mana.', color: 'AQUA' },
  { suffix: 'greater_mana_potion', name: 'Greater Mana Potion', type: 'POTION', rarity: 'RARE', frac: 1.0, resource: 'mana', msg: 'Mana floods your veins.', color: 'AQUA' },
  { suffix: 'cursed_vial', name: 'Cursed Energy Vial', type: 'POTION', rarity: 'RARE', frac: 0.40, resource: 'cursed', msg: 'Cursed energy boils within.', color: 'DARK_PURPLE' },
  { suffix: 'greater_cursed_vial', name: 'Greater Cursed Vial', type: 'POTION', rarity: 'EPIC', frac: 0.80, resource: 'cursed', msg: 'Your curse surges to the brim.', color: 'DARK_PURPLE' },
  { suffix: 'vitality_tonic', name: 'Vitality Tonic', type: 'POTION', rarity: 'UNCOMMON', frac: 0.35, resource: 'health', msg: 'Vitality returns.', color: 'RED', extra: [{ effect: 'REGENERATION', sec: 30, amp: 0 }] },
  { suffix: 'arcane_tonic', name: 'Arcane Tonic', type: 'POTION', rarity: 'UNCOMMON', frac: 0.35, resource: 'mana', msg: 'Arcane currents stir.', color: 'AQUA', extra: [{ effect: 'NIGHT_VISION', sec: 120, amp: 0 }] },
  { suffix: 'spirit_tonic', name: 'Spirit Tonic', type: 'POTION', rarity: 'RARE', frac: 0.30, resource: 'cursed', msg: 'Spirits lend their strength.', color: 'DARK_PURPLE', extra: [{ effect: 'REGENERATION', sec: 20, amp: 0 }] },
  { suffix: 'restoration_flask', name: 'Restoration Flask', type: 'POTION', rarity: 'EPIC', action: 'TRIPLE_RESTORE', msg: 'Body, mind, and curse align.', color: 'LIGHT_PURPLE' },
];

const ELIXIRS = [
  { suffix: 'strength', name: 'Elixir of Strength', rarity: 'RARE', effects: [{ effect: 'STRENGTH', sec: 90, amp: 2 }], msg: 'Power surges into your limbs.', color: 'RED', liquid: 'blood' },
  { suffix: 'swiftness', name: 'Elixir of Swiftness', rarity: 'UNCOMMON', effects: [{ effect: 'SPEED', sec: 120, amp: 2 }], msg: 'The world slows around you.', color: 'WHITE', liquid: 'frost' },
  { suffix: 'iron_skin', name: 'Elixir of Iron Skin', rarity: 'RARE', effects: [{ effect: 'RESISTANCE', sec: 60, amp: 1 }], msg: 'Your skin hardens like iron.', color: 'GREEN', liquid: 'steel' },
  { suffix: 'haste', name: 'Elixir of Haste', rarity: 'UNCOMMON', effects: [{ effect: 'HASTE', sec: 120, amp: 2 }], msg: 'Your hands move in a blur.', color: 'GOLD', liquid: 'thunder' },
  { suffix: 'phoenix', name: 'Elixir of the Phoenix', rarity: 'EPIC', effects: [{ effect: 'FIRE_RESISTANCE', sec: 60, amp: 0 }, { effect: 'REGENERATION', sec: 60, amp: 1 }], msg: 'Phoenix fire wreathes you.', color: 'GOLD', liquid: 'ember' },
  { suffix: 'berserk', name: 'Berserk Brew', rarity: 'EPIC', effects: [{ effect: 'STRENGTH', sec: 90, amp: 3 }, { effect: 'SPEED', sec: 90, amp: 1 }], msg: 'A red haze descends.', color: 'DARK_RED', liquid: 'blood' },
  { suffix: 'invisibility', name: 'Invisibility Draught', rarity: 'RARE', effects: [{ effect: 'INVISIBILITY', sec: 45, amp: 0 }], msg: 'You fade from sight.', color: 'GRAY', liquid: 'silver' },
  { suffix: 'night_owl', name: 'Night Owl Tonic', rarity: 'COMMON', effects: [{ effect: 'NIGHT_VISION', sec: 300, amp: 0 }], msg: 'Your eyes drink in the dark.', color: 'BLUE', liquid: 'lapis' },
  { suffix: 'gills', name: 'Gills Brew', rarity: 'UNCOMMON', effects: [{ effect: 'WATER_BREATHING', sec: 180, amp: 0 }, { effect: 'DOLPHINS_GRACE', sec: 180, amp: 0 }], msg: 'You breathe the deep.', color: 'AQUA', liquid: 'aqua' },
  { suffix: 'titan', name: 'Titan Tonic', rarity: 'EPIC', effects: [{ effect: 'HEALTH_BOOST', sec: 120, amp: 1 }, { effect: 'ABSORPTION', sec: 120, amp: 1 }], msg: 'You swell with titanic vitality.', color: 'RED', liquid: 'crimson' },
  { suffix: 'frost', name: 'Frost Elixir', rarity: 'RARE', effects: [{ effect: 'SLOW_FALLING', sec: 90, amp: 0 }, { effect: 'RESISTANCE', sec: 60, amp: 0 }], msg: 'Ice crystallizes on your skin.', color: 'AQUA', liquid: 'frost' },
  { suffix: 'poison', name: 'Antidote Elixir', rarity: 'UNCOMMON', effects: [{ effect: 'POISON', sec: 1, amp: 0, clear: true }], msg: 'Toxins burn away.', color: 'GREEN', liquid: 'poison', action: 'CLEAR_POISON' },
  { suffix: 'rage', name: 'Rage Elixir', rarity: 'LEGENDARY', effects: [{ effect: 'STRENGTH', sec: 60, amp: 4 }, { effect: 'SPEED', sec: 60, amp: 2 }], msg: 'Unbridled fury takes hold.', color: 'DARK_RED', liquid: 'blood' },
  { suffix: 'clarity', name: 'Clarity Elixir', rarity: 'RARE', effects: [{ effect: 'HASTE', sec: 90, amp: 1 }, { effect: 'NIGHT_VISION', sec: 90, amp: 0 }], msg: 'Your senses sharpen.', color: 'YELLOW', liquid: 'thunder' },
  { suffix: 'void', name: 'Void Elixir', rarity: 'LEGENDARY', effects: [{ effect: 'STRENGTH', sec: 45, amp: 2 }, { effect: 'INVISIBILITY', sec: 45, amp: 0 }], msg: 'The void embraces you.', color: 'DARK_PURPLE', liquid: 'void' },
];

const SCROLLS = [
  { suffix: 'recall', name: 'Scroll of Recall', rarity: 'RARE', action: 'RECALL', flavor: 'Teleports you to world spawn.', msg: 'The runes pull you home.', color: 'LIGHT_PURPLE', accent: 'arcane' },
  { suffix: 'blink', name: 'Scroll of Blink', rarity: 'UNCOMMON', action: 'BLINK', value: 8, flavor: 'Blink 8 blocks forward.', msg: 'Space folds around you.', color: 'LIGHT_PURPLE', accent: 'amethyst' },
  { suffix: 'blink_far', name: 'Scroll of Far Blink', rarity: 'RARE', action: 'BLINK', value: 16, flavor: 'Blink 16 blocks forward.', msg: 'You leap through folded space.', color: 'LIGHT_PURPLE', accent: 'void' },
  { suffix: 'ascension', name: 'Scroll of Ascension', rarity: 'RARE', action: 'ASCEND', flavor: 'Teleports you to the surface.', msg: 'You rise toward the sky.', color: 'LIGHT_PURPLE', accent: 'frost' },
  { suffix: 'warding', name: 'Scroll of Warding', rarity: 'EPIC', action: 'BUFF', effects: [{ effect: 'RESISTANCE', sec: 60, amp: 1 }, { effect: 'ABSORPTION', sec: 60, amp: 1 }], flavor: 'Resistance II + Absorption II.', msg: 'A protective ward flares.', color: 'GREEN', accent: 'emerald' },
  { suffix: 'haste', name: 'Scroll of Haste', rarity: 'UNCOMMON', action: 'BUFF', effects: [{ effect: 'SPEED', sec: 60, amp: 1 }, { effect: 'HASTE', sec: 60, amp: 1 }], flavor: 'Speed II + Haste II.', msg: 'Quickening runes race across your skin.', color: 'YELLOW', accent: 'thunder' },
  { suffix: 'fury', name: 'Scroll of Fury', rarity: 'EPIC', action: 'BUFF', effects: [{ effect: 'STRENGTH', sec: 45, amp: 2 }, { effect: 'SPEED', sec: 45, amp: 1 }], flavor: 'Strength III + Speed II.', msg: 'Battle runes ignite.', color: 'RED', accent: 'crimson' },
  { suffix: 'sanctuary', name: 'Scroll of Sanctuary', rarity: 'LEGENDARY', action: 'BUFF', effects: [{ effect: 'REGENERATION', sec: 30, amp: 2 }, { effect: 'RESISTANCE', sec: 30, amp: 2 }], flavor: 'Regeneration III + Resistance III.', msg: 'Holy light shelters you.', color: 'GOLD', accent: 'arcane' },
  { suffix: 'mana', name: 'Scroll of Arcane Flow', rarity: 'RARE', action: 'MANA_FRAC', value: 0.60, flavor: 'Restores 60% Mana.', msg: 'Arcane currents surge.', color: 'AQUA', accent: 'lapis' },
  { suffix: 'heal', name: 'Scroll of Mending', rarity: 'RARE', action: 'HEAL_FRAC', value: 0.50, flavor: 'Heals 50% max Health.', msg: 'Mending runes knit your flesh.', color: 'RED', accent: 'ruby' },
  { suffix: 'curse', name: 'Scroll of Cursed Binding', rarity: 'EPIC', action: 'CURSED_FRAC', value: 0.50, flavor: 'Restores 50% Cursed Energy.', msg: 'Dark runes bind power to you.', color: 'DARK_PURPLE', accent: 'cursed' },
  { suffix: 'levitation', name: 'Scroll of Levitation', rarity: 'EPIC', action: 'BUFF', effects: [{ effect: 'LEVITATION', sec: 8, amp: 0 }, { effect: 'SLOW_FALLING', sec: 30, amp: 0 }], flavor: 'Brief levitation then slow fall.', msg: 'You are lifted by ancient glyphs.', color: 'WHITE', accent: 'frost' },
];

const FOODS = [
  { suffix: 'hearty_stew', name: 'Hearty Stew', rarity: 'COMMON', draw: 'stew', action: 'FOOD_HEAL', heal: 0.12, saturation: 2, msg: 'The hot stew restores you.', color: 'GOLD', liquid: 'soup' },
  { suffix: 'golden_feast', name: 'Golden Feast', rarity: 'RARE', draw: 'drumstick', action: 'FOOD_GOLDEN', msg: 'You feast like royalty.', color: 'GOLD', meat: 'meat', glaze: 'gold' },
  { suffix: 'spirit_bread', name: 'Spirit Bread', rarity: 'UNCOMMON', draw: 'bread', action: 'FOOD_MANA', mana: 0.25, saturation: 2, msg: 'Blessed bread feeds body and mind.', color: 'AQUA', crust: 'bread', accent: 'aqua' },
  { suffix: 'cursed_jerky', name: 'Cursed Jerky', rarity: 'RARE', draw: 'jerky', action: 'FOOD_CURSED', cursed: 0.25, saturation: 2, msg: 'Bitter, but it stokes your curse.', color: 'DARK_PURPLE', meat: 'crimson' },
  { suffix: 'mushroom_risotto', name: 'Mushroom Risotto', rarity: 'UNCOMMON', draw: 'stew', action: 'FOOD_BUFF', effects: [{ effect: 'REGENERATION', sec: 15, amp: 0 }], heal: 0.08, saturation: 3, msg: 'Earthy warmth spreads through you.', color: 'GREEN', liquid: 'soup' },
  { suffix: 'sea_salt_fish', name: 'Sea-Salt Fish', rarity: 'COMMON', draw: 'drumstick', action: 'FOOD_BUFF', effects: [{ effect: 'WATER_BREATHING', sec: 60, amp: 0 }], heal: 0.10, saturation: 2, msg: "The sea's blessing lingers.", color: 'AQUA', meat: 'meat' },
  { suffix: 'honey_cake', name: 'Honey Cake', rarity: 'UNCOMMON', draw: 'bread', action: 'FOOD_BUFF', effects: [{ effect: 'SPEED', sec: 30, amp: 0 }], heal: 0.08, saturation: 3, msg: 'Sweet energy courses through you.', color: 'YELLOW', crust: 'bread', accent: 'gold' },
  { suffix: 'war_ration', name: 'War Ration', rarity: 'RARE', draw: 'jerky', action: 'FOOD_BUFF', effects: [{ effect: 'STRENGTH', sec: 30, amp: 0 }], heal: 0.15, saturation: 3, msg: "Soldier's fuel for the front line.", color: 'RED', meat: 'meat' },
  { suffix: 'starfruit_tart', name: 'Starfruit Tart', rarity: 'EPIC', draw: 'bread', action: 'FOOD_TRIPLE', msg: 'Cosmic sweetness restores all.', color: 'LIGHT_PURPLE', crust: 'bread', accent: 'arcane' },
  { suffix: 'void_soup', name: 'Void Soup', rarity: 'LEGENDARY', draw: 'stew', action: 'FOOD_VOID', msg: 'The abyss feeds you and takes nothing.', color: 'DARK_PURPLE', liquid: 'void' },
];

const BOMBS = [
  { suffix: 'flash', name: 'Flash Bomb', rarity: 'UNCOMMON', action: 'BOMB_FLASH', radius: 6, msg: 'A blinding flash erupts!', color: 'YELLOW', liquid: 'thunder' },
  { suffix: 'fire', name: 'Fire Bomb', rarity: 'RARE', action: 'BOMB_FIRE', radius: 4, msg: 'Flames burst outward!', color: 'RED', liquid: 'ember' },
  { suffix: 'frost', name: 'Frost Bomb', rarity: 'RARE', action: 'BOMB_FROST', radius: 5, msg: 'Ice shards explode!', color: 'AQUA', liquid: 'frost' },
  { suffix: 'poison', name: 'Poison Bomb', rarity: 'RARE', action: 'BOMB_POISON', radius: 5, msg: 'Toxic vapors spread!', color: 'GREEN', liquid: 'poison' },
  { suffix: 'smoke', name: 'Smoke Bomb', rarity: 'COMMON', action: 'BOMB_SMOKE', radius: 4, msg: 'Thick smoke billows out!', color: 'GRAY', liquid: 'silver' },
  { suffix: 'holy', name: 'Holy Bomb', rarity: 'EPIC', action: 'BOMB_HOLY', radius: 5, msg: 'Radiant force blasts undead!', color: 'GOLD', liquid: 'arcane' },
  { suffix: 'void', name: 'Void Bomb', rarity: 'LEGENDARY', action: 'BOMB_VOID', radius: 6, msg: 'Reality tears at the seams!', color: 'DARK_PURPLE', liquid: 'void' },
  { suffix: 'gravity', name: 'Gravity Bomb', rarity: 'EPIC', action: 'BOMB_GRAVITY', radius: 5, msg: 'Enemies are pulled inward!', color: 'LIGHT_PURPLE', liquid: 'void' },
  { suffix: 'shock', name: 'Shock Bomb', rarity: 'UNCOMMON', action: 'BOMB_SHOCK', radius: 4, msg: 'Lightning crackles outward!', color: 'YELLOW', liquid: 'thunder' },
  { suffix: 'healing', name: 'Healing Bomb', rarity: 'RARE', action: 'BOMB_HEAL', radius: 5, msg: 'Restorative mist washes over allies!', color: 'RED', liquid: 'crimson' },
  { suffix: 'cursed', name: 'Cursed Bomb', rarity: 'EPIC', action: 'BOMB_CURSED', radius: 5, msg: 'Cursed energy detonates!', color: 'DARK_PURPLE', liquid: 'cursed' },
];

function clampRarity(idx) {
  return RARITIES[Math.max(0, Math.min(RARITIES.length - 1, idx))];
}

function buildAccessories() {
  const items = [];
  for (const theme of THEMES) {
    for (let tier = 0; tier < ACC_TYPES.length; tier++) {
      const accType = ACC_TYPES[tier];
      const rarityIdx = Math.min(RARITIES.length - 1, accType.baseRarity + Math.floor(tier / 3));
      const id = `acc2_${theme.key}_${accType.key}`;
      const name = `${theme.prefix} ${capitalize(accType.key)}`;
      const stats = theme.stats(tier);
      const effects = theme.effects(tier);
      items.push({
        kind: 'accessory',
        id,
        name,
        type: accType.type,
        rarity: clampRarity(rarityIdx),
        flavor: theme.flavor,
        stats,
        effects,
        texture: { fn: accType.draw, ...theme.palette },
      });
    }
  }
  for (const s of MYTHIC_STANDALONES) {
    items.push({
      kind: 'accessory',
      id: s.id,
      name: s.name,
      type: s.type,
      rarity: s.rarity,
      flavor: s.flavor,
      stats: s.stats,
      effects: s.effects,
      texture: s.palette,
    });
  }
  return items;
}

function buildConsumables() {
  const items = [];
  for (const p of POTION_LINES) {
    items.push({
      kind: 'consumable',
      id: `acc2_${p.suffix}`,
      name: p.name,
      type: p.type,
      rarity: p.rarity,
      flavor: flavorForConsumable(p),
      successMsg: p.msg,
      msgColor: p.color,
      stackSize: 16,
      action: buildAction(p),
      texture: { fn: p.type === 'POTION' ? 'potion' : 'vial', liquid: p.liquid || resourceLiquid(p.resource) },
    });
  }
  for (const e of ELIXIRS) {
    items.push({
      kind: 'consumable',
      id: `acc2_elixir_${e.suffix}`,
      name: e.name,
      type: 'ELIXIR',
      rarity: e.rarity,
      flavor: elixirFlavor(e),
      successMsg: e.msg,
      msgColor: e.color,
      stackSize: 16,
      action: e.action ? { kind: e.action } : { kind: 'BUFF', effects: e.effects },
      texture: { fn: 'potion', liquid: e.liquid },
    });
  }
  for (const s of SCROLLS) {
    items.push({
      kind: 'consumable',
      id: `acc2_scroll_${s.suffix}`,
      name: s.name,
      type: 'SCROLL',
      rarity: s.rarity,
      flavor: s.flavor,
      successMsg: s.msg,
      msgColor: s.color,
      stackSize: 16,
      action: scrollAction(s),
      texture: { fn: 'scroll', metal: 'wood', accent: s.accent },
    });
  }
  for (const f of FOODS) {
    items.push({
      kind: 'consumable',
      id: `acc2_food_${f.suffix}`,
      name: f.name,
      type: 'FOOD',
      rarity: f.rarity,
      flavor: foodFlavor(f),
      successMsg: f.msg,
      msgColor: f.color,
      stackSize: 16,
      action: { kind: f.action, ...f },
      texture: { fn: f.draw, liquid: f.liquid, meat: f.meat, glaze: f.glaze, crust: f.crust, accent: f.accent },
    });
  }
  for (const b of BOMBS) {
    items.push({
      kind: 'consumable',
      id: `acc2_bomb_${b.suffix}`,
      name: b.name,
      type: 'BOMB',
      rarity: b.rarity,
      flavor: `Detonates on use. Radius ${b.radius} blocks.`,
      successMsg: b.msg,
      msgColor: b.color,
      stackSize: 8,
      action: { kind: b.action, radius: b.radius },
      texture: { fn: 'bomb', liquid: b.liquid },
    });
  }
  return items;
}

function buildCatalog() {
  return [...buildAccessories(), ...buildConsumables()];
}

function capitalize(s) {
  return s.charAt(0).toUpperCase() + s.slice(1);
}

function resourceLiquid(r) {
  return { health: 'crimson', mana: 'lapis', cursed: 'cursed' }[r] || 'crimson';
}

function flavorForConsumable(p) {
  if (p.action === 'TRIPLE_RESTORE') return 'Restores Health, Mana, and Cursed Energy.';
  if (p.frac && p.resource === 'health') return `Restores ${Math.round(p.frac * 100)}% of max Health.`;
  if (p.frac && p.resource === 'mana') return `Restores ${Math.round(p.frac * 100)}% of max Mana.`;
  if (p.frac && p.resource === 'cursed') return `Restores ${Math.round(p.frac * 100)}% of max Cursed Energy.`;
  return p.name;
}

function elixirFlavor(e) {
  if (e.action === 'CLEAR_POISON') return 'Cures poison and grants brief immunity.';
  return e.effects.map(x => `${x.effect} ${x.sec}s`).join(' + ');
}

function foodFlavor(f) {
  if (f.action === 'FOOD_HEAL') return 'Sates hunger and heals a little.';
  if (f.action === 'FOOD_GOLDEN') return 'Saturation, Regeneration, and Absorption.';
  if (f.action === 'FOOD_MANA') return 'Sates hunger and restores Mana.';
  if (f.action === 'FOOD_CURSED') return 'Sates hunger and restores Cursed Energy.';
  if (f.action === 'FOOD_TRIPLE') return 'Fully restores Health, Mana, and Cursed Energy.';
  if (f.action === 'FOOD_VOID') return 'Massive heal plus Strength and Resistance.';
  return 'Restorative food consumed on use.';
}

function buildAction(p) {
  if (p.action === 'TRIPLE_RESTORE') return { kind: 'TRIPLE_RESTORE' };
  const base = p.resource === 'health' ? 'HEAL_FRAC' : p.resource === 'mana' ? 'MANA_FRAC' : 'CURSED_FRAC';
  const action = { kind: base, value: p.frac };
  if (p.extra) action.extra = p.extra;
  return action;
}

function scrollAction(s) {
  if (s.action === 'BUFF') return { kind: 'BUFF', effects: s.effects };
  if (s.action === 'BLINK') return { kind: 'BLINK', value: s.value };
  if (s.action === 'RECALL') return { kind: 'RECALL' };
  if (s.action === 'ASCEND') return { kind: 'ASCEND' };
  return { kind: s.action, value: s.value };
}

module.exports = { buildCatalog, buildAccessories, buildConsumables, THEMES, ACC_TYPES, RARITIES };

if (require.main === module) {
  const cat = buildCatalog();
  const acc = cat.filter(x => x.kind === 'accessory').length;
  const con = cat.filter(x => x.kind === 'consumable').length;
  console.log(`accessories: ${acc}, consumables: ${con}, total: ${cat.length}`);
}
