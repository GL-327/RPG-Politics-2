/**
 * Phase-2 armour texture + model generator (arm2_ items).
 * Uses the shared pixel-art-lib draw pipeline so phase-2 sets stay visually
 * cohesive with phase-1 armour (same plate shading, rivets, trim + rim light).
 * Usage: node gen-armor2.js
 */
const {
  P, canvas, draw, finishSprite, writePng, writeJSON, itemModel, itemDef,
  ITEM_TEX, ITEM_MODELS, ITEM_DEFS, buildMontage,
} = require('./pixel-art-lib');
const fs = require('fs');
const path = require('path');

function spec(palette) {
  const metal = P[palette] || P.iron;
  return { metal, accent: metal[3] };
}

// Keep in sync with ArmorSet#key in com.political.expansion2.armor
const SETS = {
  pyrofire: spec('ember'),
  hydroflow: spec('aqua'),
  geostone: spec('darkiron'),
  aerowind: spec('silver'),
  electrovolt: spec('thunder'),
  cryofrost: spec('frost'),
  verdantlife: spec('emerald'),
  umbralshadow: spec('shadow'),
  radiantlight: spec('gold'),
  grade4: spec('iron'),
  grade3: spec('steel'),
  grade2: spec('diamond'),
  grade1: spec('amethyst'),
  special_grade: spec('void'),
  viltrumite: spec('blood'),
  hero: spec('steel'),
  villain: spec('shadow'),
  royal: spec('gold'),
  pirate: spec('leather'),
  samurai: spec('darkiron'),
  necromancer: spec('necro'),
  paladin: spec('gold'),
  assassin: spec('shadow'),
  druid: spec('emerald'),
  engineer: spec('steel'),
  stormcaller: spec('thunder'),
  dragonkin: spec('ember'),
  phoenix: spec('ember'),
  voidwalker: spec('void'),
  bloodraven: spec('blood'),
  ironclad: spec('iron'),
  crystalweaver: spec('diamond'),
  sandstrider: spec('gold'),
  omniknight: spec('gold'),
  apocalypse: spec('blood'),
  infinity: spec('amethyst'),
};

const SLOTS = { helmet: 'helmet', chestplate: 'chest', leggings: 'legs', boots: 'boots' };

async function generate() {
  for (const d of [ITEM_TEX, ITEM_MODELS, ITEM_DEFS]) fs.mkdirSync(d, { recursive: true });
  const ids = [];
  for (const [setKey, paletteSpec] of Object.entries(SETS)) {
    for (const [slotSuffix, fnName] of Object.entries(SLOTS)) {
      const id = `arm2_${setKey}_${slotSuffix}`;
      const cv = canvas();
      draw[fnName](cv, paletteSpec);
      finishSprite(cv, paletteSpec, { rim: true });
      await writePng(cv, ITEM_TEX, id);
      writeJSON(path.join(ITEM_MODELS, `${id}.json`), itemModel(id, false));
      writeJSON(path.join(ITEM_DEFS, `${id}.json`), itemDef(id));
      ids.push(id);
    }
  }
  console.log('armour2 pieces:', ids.length);
  return ids;
}

async function main() {
  const ids = await generate();
  await buildMontage(ids, 'armor2_montage.png', { cols: 8 });
  console.log('done.');
}

main().catch(e => { console.error(e); process.exit(1); });
