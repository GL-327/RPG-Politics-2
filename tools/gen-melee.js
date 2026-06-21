/**
 * MELEE WEAPON EXPANSION texture generator — uses shared pixel-art-lib.
 * Usage: node gen-melee.js
 */
const { P, W, generateItemBatch, buildMontage } = require('./pixel-art-lib');

const SPECS = {
  wpn_iron_cutlass: W('sword', { blade: P.iron, grip: P.leather, metal: P.steel }),
  wpn_bronze_dirk: W('dagger', { blade: P.copper, grip: P.leather, metal: P.gold, gem: P.copper }),
  wpn_stone_bludgeon: W('mace', { metal: P.stone, grip: P.wood }),
  wpn_woodsman_hatchet: W('axe', { blade: P.iron, grip: P.wood }),
  wpn_militia_spear: W('spear', { blade: P.iron, grip: P.wood, metal: P.steel }),
  wpn_steel_saber: W('sword', { blade: P.steel, grip: P.darkiron, metal: P.silver }),
  wpn_hunters_kris: W('dagger', { blade: P.poison, grip: P.leather, gem: P.poison, metal: P.iron }),
  wpn_bearded_axe: W('axe', { blade: P.steel, grip: P.wood }),
  wpn_war_pike: W('spear', { blade: P.steel, grip: P.darkiron, metal: P.silver }),
  wpn_iron_morningstar: W('mace', { metal: P.darkiron, grip: P.wood, gem: P.crimson }),
  wpn_knights_longsword: W('sword', { blade: P.silver, grip: P.cloth_red, metal: P.gold, gem: P.ruby }),
  wpn_shadow_kunai: W('dagger', { blade: P.shadow, grip: P.obsidian, gem: P.amethyst, metal: P.silver }),
  wpn_storm_glaive: W('halberd', { blade: P.thunder, grip: P.darkiron, metal: P.thunder }),
  wpn_tempest_katana: W('katana', { blade: P.frost, grip: P.lapis, metal: P.silver }),
  wpn_grave_scythe: W('scythe', { blade: P.necro, grip: P.obsidian }),
  wpn_dragonbone_greatsword: W('greatsword', { blade: P.bone, grip: P.netherite, gem: P.ember, metal: P.darkiron }),
  wpn_venom_claws: W('claw', { metal: P.poison, grip: P.leather }),
  wpn_frost_cleaver: W('cleaver', { blade: P.frost, grip: P.steel, metal: P.silver }),
  wpn_ember_waraxe: W('axe', { blade: P.ember, grip: P.netherite }),
  wpn_serpent_whip: W('whip', { blade: P.jade, grip: P.leather }),
  wpn_thunderspike_spear: W('spear', { blade: P.thunder, grip: P.darkiron, metal: P.thunder }),
  wpn_celestial_claymore: W('greatsword', { blade: P.frost, grip: P.lapis, gem: P.diamond, metal: P.silver }),
  wpn_moonshadow_katana: W('katana', { blade: P.amethyst, grip: P.obsidian, metal: P.silver }),
  wpn_soulreaper_scythe: W('scythe', { blade: P.soul, grip: P.obsidian }),
  wpn_warlords_halberd: W('halberd', { blade: P.crimson, grip: P.netherite, metal: P.gold }),
  wpn_titanbreaker_maul: W('mace', { metal: P.netherite, grip: P.netherite, gem: P.thunder }),
  wpn_phantom_daggers: W('dagger', { blade: P.void, grip: P.shadow, gem: P.amethyst, metal: P.silver }),
  wpn_godslayer_blade: W('sword', { blade: P.gold, grip: P.cloth_red, gem: P.ruby, metal: P.gold }),
  wpn_voidrend_greatsword: W('greatsword', { blade: P.void, grip: P.obsidian, gem: P.amethyst, metal: P.amethyst }),
  wpn_ragnarok_axe: W('axe', { blade: P.ember, grip: P.crimson }),
  wpn_eternity_scythe: W('scythe', { blade: P.arcane, grip: P.obsidian }),
};

// holy palette for mythic godslayer
P.holy = P.holy || [[120, 96, 32], [196, 168, 70], [240, 224, 140], [252, 246, 206], [255, 255, 244]];
SPECS.wpn_godslayer_blade = W('sword', { blade: P.holy, grip: P.cloth_red, gem: P.ruby, metal: P.gold });

async function main() {
  await generateItemBatch(SPECS, { label: 'melee weapons', alwaysHandheld: true, finish: { glow: true, rim: true } });
  await buildMontage(Object.keys(SPECS), 'melee_montage.png', { cols: 8 });
  console.log('done.');
}

main().catch(e => { console.error(e); process.exit(1); });
