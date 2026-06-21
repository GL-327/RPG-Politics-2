/**
 * Core phase-1 texture + model generator (RpgItem, CursedGear, RelicItems, GovItems, base blocks).
 * Uses shared pixel-art-lib for drawing pipeline + finishSprite (AO + outline).
 * Usage: node generate-textures.js
 */
const { Jimp } = require('jimp');
const path = require('path');
const fs = require('fs');
const {
  P, W, canvas, draw, block, finishSprite, writePng,
  generateItemBatch, generateBlockBatch, buildMontage,
  ITEM_TEX, BLOCK_TEX, ENTITY_TEX, HANDHELD,
} = require('./pixel-art-lib');

const ITEM_SPECS = {
  compound_v: W('potion', { liquid: P.steel }),
  temp_v: W('potion', { liquid: P.ember }),
  v1: W('potion', { liquid: P.arcane }),
  anti_v: W('potion', { liquid: P.poison }),
  cursed_dagger: W('dagger', { blade: P.cursed, grip: P.obsidian, gem: P.amethyst }),
  cursed_blade: W('sword', { blade: P.cursed, grip: P.obsidian, gem: P.amethyst, metal: P.amethyst }),
  soul_cleaver: W('cleaver', { blade: P.void, grip: P.obsidian, metal: P.amethyst }),
  cursed_polearm: W('halberd', { blade: P.poison, grip: P.obsidian, metal: P.cursed }),
  cursed_greatsword: W('greatsword', { blade: P.void, grip: P.obsidian, gem: P.cursed, metal: P.amethyst }),
  cursed_whip: W('whip', { blade: P.crimson, grip: P.obsidian }),
  mana_crystal: W('gem', { gem: P.frost }),
  cursed_essence: W('gem', { gem: P.cursed }),
  exorcism_token: W('coin', { metal: P.silver, accent: P.amethyst[3] }),
  grade_scroll: W('scroll', { accent: P.gold[3], metal: P.gold }),
  awakening_stone: W('gem', { gem: P.arcane }),
  bounty_seal: W('coin', { metal: P.blood, accent: P.gold[4] }),
  reforge_stone: W('gem', { gem: P.silver }),
  crown: W('crown', { metal: P.gold, gem: P.ruby }),
  gavel: W('gavel', { metal: P.wood, accent: P.gold[2] }),
  decree_scroll: W('scroll', { accent: P.crimson[3], metal: P.wood }),
  ballot: W('sheet', { accent: P.lapis[3] }),
  treasury_note: W('sheet', { accent: P.emerald[2], band: P.emerald[2] }),
  coin_pouch: W('pouch', { metal: P.leather }),
  passport: W('book', { metal: P.lapis, accent: P.gold[3] }),
  dev_menu: W('star', { gem: P.amethyst }),
  aegis_blade: W('sword', { blade: P.silver, grip: P.lapis, gem: P.lapis, metal: P.gold }),
  ember_staff: W('staff', { grip: P.copper, gem: P.ember, metal: P.gold }),
  void_reaver: W('sword', { blade: P.void, grip: P.obsidian, gem: P.amethyst, metal: P.amethyst }),
  storm_edge: W('sword', { blade: P.steel, grip: P.darkiron, gem: P.thunder, metal: P.thunder }),
  inferno_brand: W('axe', { blade: P.ember, grip: P.netherite }),
  frostmourne: W('sword', { blade: P.frost, grip: P.steel, gem: P.frost, metal: P.silver }),
  venom_fang: W('sword', { blade: P.poison, grip: P.leather, gem: P.poison, metal: P.iron }),
  thundercaller: W('trident', { metal: P.thunder, grip: P.darkiron }),
  midas_edge: W('sword', { blade: P.gold, grip: P.leather, gem: P.gold, metal: P.gold }),
  abyssal_blade: W('sword', { blade: P.soul, grip: P.obsidian, gem: P.soul, metal: P.darkiron }),
  daniels_pickaxe: W('pickaxe', { metal: P.netherite, grip: P.wood, gem: P.amethyst }),
  titan_drill: W('pickaxe', { metal: P.darkiron, grip: P.netherite }),
  excavator_spade: W('shovel', { metal: P.netherite, grip: P.wood }),
  worldcleaver: W('axe', { blade: P.netherite, grip: P.wood }),
  sentinel_helm: W('helmet', { metal: P.iron, accent: P.steel[3] }),
  wardens_plate: W('chest', { metal: P.netherite, accent: P.diamond[2] }),
  titan_leggings: W('legs', { metal: P.steel, accent: P.diamond[2] }),
  voidwalker_boots: W('boots', { metal: P.silver, accent: P.amethyst[3] }),
  inferno_helm: W('helmet', { metal: P.ember, accent: P.gold[3] }),
  inferno_plate: W('chest', { metal: P.ember, accent: P.gold[3] }),
  inferno_legs: W('legs', { metal: P.ember, accent: P.gold[3] }),
  inferno_boots: W('boots', { metal: P.ember, accent: P.gold[3] }),
  storm_helm: W('helmet', { metal: P.steel, accent: P.thunder[3] }),
  storm_plate: W('wings', { metal: P.steel }),
  storm_legs: W('legs', { metal: P.steel, accent: P.thunder[3] }),
  storm_boots: W('boots', { metal: P.steel, accent: P.thunder[3] }),
  void_helm: W('helmet', { metal: P.void, accent: P.amethyst[3] }),
  void_plate: W('chest', { metal: P.void, accent: P.amethyst[3] }),
  void_legs: W('legs', { metal: P.void, accent: P.amethyst[3] }),
  void_boots: W('boots', { metal: P.void, accent: P.amethyst[3] }),
  lapis_helm: W('helmet', { metal: P.lapis, accent: P.frost[3] }),
  lapis_plate: W('chest', { metal: P.lapis, accent: P.frost[3] }),
  emerald_helm: W('helmet', { metal: P.emerald, accent: P.gold[3] }),
  emerald_plate: W('chest', { metal: P.emerald, accent: P.gold[3] }),
  tide_helm: W('helmet', { metal: P.aqua, accent: P.diamond[3] }),
  tide_boots: W('boots', { metal: P.aqua, accent: P.gold[3] }),
  nightfall_scythe: W('scythe', { blade: P.void, grip: P.obsidian }),
  phantom_blade: W('sword', { blade: P.shadow, grip: P.obsidian, gem: P.amethyst, metal: P.silver }),
  radiant_halberd: W('halberd', { blade: P.gold, grip: P.wood, metal: P.gold }),
  crystal_staff: W('staff', { grip: P.silver, gem: P.frost, metal: P.diamond }),
  soul_bow: W('bow', { grip: P.wood, accent: P.soul[3], blade: P.soul }),
  dragonslayer: W('axe', { blade: P.ember, grip: P.netherite }),
  arachno_claw: W('claw', { metal: P.shadow, grip: P.crimson }),
  moonlit_katana: W('katana', { blade: P.silver, grip: P.lapis, metal: P.gold }),
  ghost_dagger: W('dagger', { blade: P.soul, grip: P.shadow, gem: P.frost }),
  solar_spear: W('spear', { blade: P.ember, grip: P.gold, metal: P.gold }),
  berserkers_axe: W('axe', { blade: P.iron, grip: P.crimson }),
  shadow_shield: W('shield', { metal: P.shadow, accent: P.amethyst[3] }),
  ocean_trident: W('trident', { metal: P.aqua, grip: P.wood }),
  vampire_fang: W('dagger', { blade: P.blood, grip: P.obsidian, gem: P.crimson }),
  skull_mace: W('mace', { metal: P.bone, grip: P.netherite, gem: P.crimson }),
  necro_crown: W('crown', { metal: P.necro, gem: P.poison }),
  necro_garb: W('chest', { metal: P.necro, accent: P.poison[3] }),
  necro_legs: W('legs', { metal: P.necro, accent: P.poison[3] }),
  necro_boots: W('boots', { metal: P.necro, accent: P.poison[3] }),
  radiant_diadem: W('crown', { metal: P.gold, gem: P.diamond }),
  radiant_vestments: W('chest', { metal: P.gold, accent: P.diamond[3] }),
  radiant_leggings: W('legs', { metal: P.gold, accent: P.diamond[3] }),
  radiant_sandals: W('boots', { metal: P.gold, accent: P.diamond[3] }),
  starforge_hammer: W('mace', { metal: P.silver, grip: P.netherite, gem: P.thunder }),
  moonlight_pick: W('pickaxe', { metal: P.silver, grip: P.wood, gem: P.frost }),
  arcane_hoe: W('hoe', { metal: P.arcane, grip: P.wood }),
  timber_axe: W('axe', { blade: P.iron, grip: P.wood }),
  prospector_shovel: W('shovel', { metal: P.iron, grip: P.wood }),
};

const BLOCK_SPECS = {
  castle_bricks: W('bricks', { pal: P.stone }),
  castle_pillar: W('pillar', { pal: P.stone }),
  royal_banner_block: W('banner', { pal: P.cloth_red }),
  town_hall_bricks: W('bricks', { pal: P.terracotta }),
  paved_road: W('road', { pal: P.asphalt }),
  cobble_street: W('cobble', { pal: P.stone }),
  street_lamp: W('lamp', { pal: P.darkiron }),
  modern_facade: W('facade', { pal: P.quartz }),
  modern_window: W('window', { pal: P.steel }),
  civic_marker: W('marker', { pal: P.amethyst }),
};

async function generateVillager() {
  fs.mkdirSync(ENTITY_TEX, { recursive: true });
  const img = new Jimp({ width: 64, height: 64, color: 0x00000000 });
  for (let y = 0; y < 64; y++) for (let x = 0; x < 64; x++) {
    let col = 0x00000000;
    if (y >= 8 && y <= 24 && x >= 20 && x <= 44) col = 0x4a6741ff;
    if (y >= 4 && y <= 12 && x >= 24 && x <= 40) col = 0xd4a574ff;
    if (y >= 2 && y <= 6 && x >= 22 && x <= 42) col = 0x3a2a4aff;
    if (col) img.setPixelColor(col >>> 0, x, y);
  }
  await img.write(path.join(ENTITY_TEX, 'rpg_villager.png'));
}

async function main() {
  await generateItemBatch(ITEM_SPECS, { label: 'core items', finish: { glow: true, rim: true } });
  await generateBlockBatch(BLOCK_SPECS, { label: 'core blocks' });
  await generateVillager();
  const ids = [...Object.keys(ITEM_SPECS), ...Object.keys(BLOCK_SPECS)];
  await buildMontage(ids, 'core_montage.png', { cols: 12 });
  console.log('done.');
}

main().catch(e => { console.error(e); process.exit(1); });
