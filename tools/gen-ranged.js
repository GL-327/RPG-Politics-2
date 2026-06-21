/**
 * 16x16 pixel-art texture + model generator for the RANGED & MAGIC expansion
 * (all ids prefixed `arc_`).  Uses the shared pixel-art-lib so every archetype
 * (bow / crossbow / throwing / wand / staff / tome / orb) shares the same
 * high-quality shading: crisp outline, 5-tone ramps, bright cutting edges,
 * glowing gem cores and a consistent upper-left rim light.
 *
 *   cd tools && node gen-ranged.js      (requires tools/node_modules — jimp)
 *
 * It only writes textures/item/arc_*.png, models/item/arc_*.json and
 * items/arc_*.json — it never touches non-arc assets.
 */
const { P, W, generateItemBatch, buildMontage } = require('./pixel-art-lib');

const SPECS = {
  // Bows
  arc_huntbow: W('bow', { grip: P.wood, blade: P.iron }),
  arc_windbow: W('bow', { grip: P.aqua, blade: P.wind, string: P.wind[4] }),
  arc_emberbow: W('bow', { grip: P.ember, blade: P.ember, string: P.gold[4] }),
  arc_soulbow: W('bow', { grip: P.obsidian, blade: P.soul, string: P.soul[4] }),
  arc_frostbow: W('bow', { grip: P.frost, blade: P.frost, string: P.frost[4] }),
  arc_stormbow: W('bow', { grip: P.darkiron, blade: P.thunder, string: P.thunder[4] }),
  arc_voidbow: W('bow', { grip: P.void, blade: P.amethyst, string: P.amethyst[4] }),

  // Crossbows
  arc_boltcaster: W('crossbow', { metal: P.iron, blade: P.wood }),
  arc_venombolt: W('crossbow', { metal: P.darkiron, blade: P.poison }),
  arc_glacialbolt: W('crossbow', { metal: P.silver, blade: P.frost }),
  arc_infernobolt: W('crossbow', { metal: P.netherite, blade: P.ember }),
  arc_thunderbolt: W('crossbow', { metal: P.darkiron, blade: P.thunder }),

  // Throwing weapons
  arc_throwing_knives: W('knife', { blade: P.silver, grip: P.shadow }),
  arc_chakram: W('chakram', { metal: P.silver, accent: P.gold[4] }),
  arc_throwing_stars: W('shuriken', { metal: P.steel, accent: P.aqua[4] }),
  arc_kunai: W('kunai', { blade: P.cursed, grip: P.obsidian }),
  arc_javelin: W('javelin', { blade: P.aqua, grip: P.wood, metal: P.thunder }),

  // Wands
  arc_emberwand: W('wand', { grip: P.wood, gem: P.ember, metal: P.gold }),
  arc_frostwand: W('wand', { grip: P.silver, gem: P.frost, metal: P.frost }),
  arc_galewand: W('wand', { grip: P.wood, gem: P.wind, metal: P.silver }),
  arc_lifewand: W('wand', { grip: P.wood, gem: P.emerald, metal: P.gold }),
  arc_arcanewand: W('wand', { grip: P.silver, gem: P.arcane, metal: P.amethyst }),

  // Staves
  arc_pyrostaff: W('staff', { grip: P.netherite, gem: P.ember, metal: P.gold }),
  arc_cryostaff: W('staff', { grip: P.silver, gem: P.frost, metal: P.frost }),
  arc_lifestaff: W('staff', { grip: P.wood, gem: P.emerald, metal: P.gold }),
  arc_stormstaff: W('staff', { grip: P.darkiron, gem: P.thunder, metal: P.thunder }),
  arc_voidstaff: W('staff', { grip: P.obsidian, gem: P.void, metal: P.amethyst }),

  // Tomes / Spellbooks
  arc_pyromancy_tome: W('book', { metal: P.crimson, accent: P.gold[4] }),
  arc_frost_grimoire: W('book', { metal: P.frost, accent: P.silver[4] }),
  arc_shadow_tome: W('book', { metal: P.shadow, accent: P.amethyst[4] }),
  arc_storm_codex: W('book', { metal: P.thunder, accent: P.gold[4] }),
  arc_holy_codex: W('book', { metal: P.holy, accent: P.gold[4] }),
  arc_necro_tome: W('book', { metal: P.necro, accent: P.poison[4] }),

  // Orbs
  arc_arcane_orb: W('orb', { gem: P.arcane }),
  arc_soul_orb: W('orb', { gem: P.soul }),
};

async function main() {
  await generateItemBatch(SPECS, { label: 'ranged items', finish: { glow: true, rim: true } });
  await buildMontage(Object.keys(SPECS), 'ranged_montage.png', { cols: 9 });
  console.log('done.');
}

main().catch(e => { console.error(e); process.exit(1); });
