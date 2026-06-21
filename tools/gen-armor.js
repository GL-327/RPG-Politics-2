/**
 * Armour-expansion texture + model generator for the politicalserver mod (arm_ items).
 *
 * Uses the shared pixel-art-lib (draw.helmet/chest/legs/boots + finishSprite) so the
 * armour pieces share the exact same high-quality shading pipeline as core gear:
 * crisp silhouette outline, 5-tone shading ramps, rivets/bevels, set-themed accent
 * trim + chest gem, and a consistent upper-left rim light.
 *
 * The id list here MUST stay in sync with ArmorSet#pieceId in the Java source
 * (arm_<set>_<helmet|chestplate|leggings|boots>).
 *
 * Usage: node gen-armor.js   (requires tools/node_modules with jimp installed)
 */
const {
  P, canvas, draw, finishSprite, writePng, writeJSON, itemModel, itemDef,
  ITEM_TEX, ITEM_MODELS, ITEM_DEFS, buildMontage,
} = require('./pixel-art-lib');
const fs = require('fs');
const path = require('path');

// ----------------------------------------------------------------------------
// Set palettes — keep set keys in sync with ArmorSet#key.  `accent` is the
// rarity/theme-tinted trim colour drawn over the plate (helmet crest, chest
// trim + gem, waistband, boot cuffs).
// ----------------------------------------------------------------------------
const SETS = {
  recruit:      { metal: P.leather,   accent: P.iron[3] },
  guardian:     { metal: P.iron,      accent: P.steel[3] },
  ranger:       { metal: P.necro,     accent: P.gold[3] },
  frostguard:   { metal: P.frost,     accent: P.diamond[3] },
  emberforge:   { metal: P.ember,     accent: P.gold[3] },
  tempest:      { metal: P.steel,     accent: P.thunder[3] },
  verdant:      { metal: P.emerald,   accent: P.gold[3] },
  abyssal:      { metal: P.aqua,      accent: P.amethyst[3] },
  shadowstalker:{ metal: P.shadow,    accent: P.amethyst[3] },
  bloodmoon:    { metal: P.blood,     accent: [248, 158, 140] },
  solaris:      { metal: P.gold,      accent: P.diamond[3] },
  wraith:       { metal: P.necro,     accent: P.poison[3] },
  titanforge:   { metal: P.netherite, accent: P.diamond[3] },
  celestial:    { metal: P.amethyst,  accent: P.diamond[4] },
};

const SLOTS = { helmet: 'helmet', chestplate: 'chest', leggings: 'legs', boots: 'boots' };

async function generate() {
  for (const d of [ITEM_TEX, ITEM_MODELS, ITEM_DEFS]) fs.mkdirSync(d, { recursive: true });
  const ids = [];
  for (const [setKey, spec] of Object.entries(SETS)) {
    for (const [slotSuffix, fnName] of Object.entries(SLOTS)) {
      const id = `arm_${setKey}_${slotSuffix}`;
      const cv = canvas();
      draw[fnName](cv, spec);
      finishSprite(cv, spec, { rim: true });
      await writePng(cv, ITEM_TEX, id);
      writeJSON(path.join(ITEM_MODELS, `${id}.json`), itemModel(id, false));
      writeJSON(path.join(ITEM_DEFS, `${id}.json`), itemDef(id));
      ids.push(id);
    }
  }
  console.log('armour pieces:', ids.length);
  return ids;
}

async function main() {
  const ids = await generate();
  await buildMontage(ids, 'armor_montage.png', { cols: 8 });
  console.log('done.');
}

main().catch(e => { console.error(e); process.exit(1); });
