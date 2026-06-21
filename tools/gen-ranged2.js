/**
 * Texture + model generator for Expansion 2 ranged weapons (arc2_* ids).
 * Uses the shared pixel-art-lib draw pipeline (bow / crossbow / chakram / gun /
 * wand / staff / tome / orb / focus) so phase-2 ranged gear stays cohesive with
 * the rest of the pack.
 *   cd tools && node gen-ranged2.js
 */
const { P, generateItemBatch, buildMontage } = require('./pixel-art-lib');
const { WEAPONS } = require('./ranged2-catalog');

// element -> palette bundle used to tint each archetype
const TINT = {
  ember: { gem: P.ember, blade: P.ember, metal: P.gold, grip: P.wood },
  frost: { gem: P.frost, blade: P.frost, metal: P.silver, grip: P.silver },
  thunder: { gem: P.thunder, blade: P.thunder, metal: P.darkiron, grip: P.darkiron },
  void: { gem: P.void, blade: P.amethyst, metal: P.obsidian, grip: P.obsidian },
  arcane: { gem: P.arcane, blade: P.arcane, metal: P.amethyst, grip: P.silver },
  poison: { gem: P.poison, blade: P.poison, metal: P.darkiron, grip: P.leather },
  shadow: { gem: P.shadow, blade: P.shadow, metal: P.obsidian, grip: P.shadow },
  holy: { gem: P.holy, blade: P.holy, metal: P.gold, grip: P.gold },
  emerald: { gem: P.emerald, blade: P.emerald, metal: P.gold, grip: P.wood },
  blood: { gem: P.blood, blade: P.blood, metal: P.crimson, grip: P.leather },
  necro: { gem: P.necro, blade: P.necro, metal: P.shadow, grip: P.obsidian },
  soul: { gem: P.soul, blade: P.soul, metal: P.aqua, grip: P.obsidian },
  wind: { gem: P.wind, blade: P.wind, metal: P.aqua, grip: P.wood },
  aqua: { gem: P.aqua, blade: P.aqua, metal: P.steel, grip: P.silver },
  gold: { gem: P.gold, blade: P.gold, metal: P.gold, grip: P.wood },
  crimson: { gem: P.crimson, blade: P.crimson, metal: P.crimson, grip: P.leather },
  steel: { gem: P.steel, blade: P.steel, metal: P.steel, grip: P.iron },
  iron: { gem: P.iron, blade: P.iron, metal: P.iron, grip: P.leather },
  cursed: { gem: P.cursed, blade: P.cursed, metal: P.amethyst, grip: P.obsidian },
};

const FN_MAP = { bow: 'bow', crossbow: 'crossbow', chakram: 'chakram', gun: 'gun', wand: 'wand', staff: 'staff', tome: 'book', orb: 'orb', focus: 'focus' };

function specFor(w) {
  const t = TINT[w.tint] || TINT.arcane;
  return { fn: FN_MAP[w.cat], ...t, accent: t.gem[4], string: t.gem[4] };
}

async function main() {
  const SPECS = {};
  for (const w of WEAPONS) SPECS['arc2_' + w.name.toLowerCase()] = specFor(w);
  await generateItemBatch(SPECS, { label: 'ranged2 items', finish: { glow: true, rim: true } });
  await buildMontage(Object.keys(SPECS), 'ranged2_montage.png', { cols: 12, scale: 6 });
  console.log('done.');
}

main().catch(e => { console.error(e); process.exit(1); });
