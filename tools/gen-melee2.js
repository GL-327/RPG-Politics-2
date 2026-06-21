/**
 * MELEE2 expansion texture generator — uses shared pixel-art-lib.
 * Usage: node gen-melee2.js
 */
const { P, generateItemBatch, buildMontage } = require('./pixel-art-lib');
const { buildCatalog } = require('./melee2-catalog');

P.holy = P.holy || [[120, 96, 32], [196, 168, 70], [240, 224, 140], [252, 246, 206], [255, 255, 244]];

function buildSpecs() {
  const specs = {};
  for (const w of buildCatalog()) {
    const blade = P[w.texture.palette] || P.iron;
    const grip = P[w.texture.grip] || P.wood;
    specs[w.id] = { fn: w.texture.fn, blade, grip, metal: blade, gem: blade };
  }
  return specs;
}

async function main() {
  const SPECS = buildSpecs();
  await generateItemBatch(SPECS, { label: 'melee2 textures', alwaysHandheld: true, finish: { glow: true, rim: true } });
  await buildMontage(Object.keys(SPECS), 'melee2_montage.png', { cols: 12 });
  console.log('done.');
}

main().catch(e => { console.error(e); process.exit(1); });
