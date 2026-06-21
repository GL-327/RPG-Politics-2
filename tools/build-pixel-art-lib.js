/** One-shot: extract shared drawing code from generate-textures.js into pixel-art-lib.js */
const fs = require('fs');
const path = require('path');

const src = fs.readFileSync(path.join(__dirname, 'generate-textures.js'), 'utf8');
const start = src.indexOf('// Canvas helpers');
const end = src.indexOf('// SPECS');
const body = src.slice(start, end);

const header = `/**
 * Shared high-quality pixel-art library for politicalserver texture generators.
 * 5-tone ramps, silhouette outlines, ambient-occlusion corners, material patterns.
 */
const { Jimp } = require('jimp');
const path = require('path');
const fs = require('fs');

const ROOT = path.join(__dirname, '..');
const ASSETS = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'politicalserver');
const ITEM_TEX = path.join(ASSETS, 'textures', 'item');
const BLOCK_TEX = path.join(ASSETS, 'textures', 'block');
const ENTITY_TEX = path.join(ASSETS, 'textures', 'entity');
const ITEM_MODELS = path.join(ASSETS, 'models', 'item');
const BLOCK_MODELS = path.join(ASSETS, 'models', 'block');
const ITEM_DEFS = path.join(ASSETS, 'items');
const BLOCKSTATES = path.join(ASSETS, 'blockstates');
const PREVIEW_DIR = path.join(ROOT, '.texref');

const N = 16;
`;

const tail = `
// Ambient occlusion: darken interior corner pixels for depth
function ambientOcclusion(cv, strength = 0.35) {
  for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) {
    if (!cv[y][x]) continue;
    let n = 0;
    if (get(cv, x - 1, y) && get(cv, x, y - 1)) n++;
    if (get(cv, x + 1, y) && get(cv, x, y - 1)) n++;
    if (get(cv, x - 1, y) && get(cv, x, y + 1)) n++;
    if (get(cv, x + 1, y) && get(cv, x, y + 1)) n++;
    if (n >= 2) cv[y][x] = darken(cv[y][x], 1 - strength);
  }
}

function metalSheen(cv, x0, y0, x1, y1, ramp) {
  for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) {
    if (!get(cv, x, y)) continue;
    if ((x + y) % 3 === 0) px(cv, x, y, ramp[4]);
    else if ((x + y) % 5 === 0) px(cv, x, y, ramp[3]);
  }
}

function leatherGrain(cv, x0, y0, x1, y1, ramp, seed = 17) {
  const r = rng(seed);
  for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) {
    if (!get(cv, x, y)) continue;
    if (r() > 0.72) px(cv, x, y, ramp[r() > 0.5 ? 1 : 3]);
  }
}

function woodGrain(cv, x0, y0, x1, y1, ramp, seed = 23) {
  for (let y = y0; y <= y1; y++) {
    const wave = Math.sin(y * 0.9) * 0.5;
    for (let x = x0; x <= x1; x++) {
      if (!get(cv, x, y)) continue;
      if (Math.abs(x - (x0 + x1) / 2 + wave) < 0.6) px(cv, x, y, ramp[1]);
      else if ((x + y) % 4 === 0) px(cv, x, y, ramp[3]);
    }
  }
}

function gemFacets(cv, cx, cy, r, ramp) {
  for (let y = cy - r; y <= cy + r; y++) for (let x = cx - r; x <= cx + r; x++) {
    if (!get(cv, x, y)) continue;
    const dx = x - cx, dy = y - cy;
    const facet = (dx + dy + cx + cy) % 3;
    px(cv, x, y, ramp[facet === 0 ? 3 : facet === 1 ? 2 : 1]);
  }
  px(cv, cx - 1, cy - 1, ramp[4]);
  px(cv, cx, cy, mix(ramp[3], WHITE, 0.35));
}

function foodGloss(cv, pts, col = WHITE) {
  for (const [x, y] of pts) px(cv, x, y, col);
}

function magicGlow(cv, cx, cy, radius, color, alpha = 80) {
  for (let y = cy - radius; y <= cy + radius; y++) for (let x = cx - radius; x <= cx + radius; x++) {
    const d = Math.sqrt((x - cx) ** 2 + (y - cy) ** 2);
    if (d > radius || !inb(x, y)) continue;
    const existing = get(cv, x, y);
    const t = 1 - d / radius;
    if (!existing) { px(cv, x, y, [color[0], color[1], color[2], Math.round(alpha * t)]); continue; }
    px(cv, x, y, mix(existing, color, t * 0.45));
  }
}

function refRamp(spec) {
  return spec.blade || spec.metal || spec.gem || spec.liquid || spec.grip || P.shadow;
}

function finishSprite(cv, spec, opts = {}) {
  if (opts.glow && spec.gem) magicGlow(cv, 8, 6, opts.glowRadius || 3, spec.gem[3], opts.glowAlpha || 70);
  if (opts.ao !== false) ambientOcclusion(cv, opts.aoStrength || 0.28);
  const ref = refRamp(spec);
  outline(cv, darken(ref[0], opts.outlineDarken || 0.42));
}

const HANDHELD = new Set(['sword', 'dagger', 'greatsword', 'cleaver', 'katana', 'scythe', 'staff', 'wand',
  'halberd', 'spear', 'trident', 'whip', 'bow', 'mace', 'axe', 'pickaxe', 'shovel', 'hoe', 'claw']);

async function writePng(cv, dir, name, size = N) {
  const img = new Jimp({ width: size, height: size, color: 0x00000000 });
  const h = cv.length, w = cv[0].length;
  for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) {
    const c = cv[y][x]; if (!c) continue;
    const a = c[3] == null ? 255 : c[3];
    img.setPixelColor(((c[0] << 24) | (c[1] << 16) | (c[2] << 8) | a) >>> 0, x, y);
  }
  fs.mkdirSync(dir, { recursive: true });
  await img.write(path.join(dir, name + '.png'));
}

function writeJSON(file, obj) { fs.writeFileSync(file, JSON.stringify(obj, null, 2) + '\\n'); }
function itemModel(id, handheld) {
  return { parent: handheld ? 'minecraft:item/handheld' : 'minecraft:item/generated', textures: { layer0: 'politicalserver:item/' + id } };
}
function itemDef(id) { return { model: { type: 'minecraft:model', model: 'politicalserver:item/' + id } }; }
function blockItemDef(id) { return { model: { type: 'minecraft:model', model: 'politicalserver:block/' + id } }; }
function blockModel(id) { return { parent: 'minecraft:block/cube_all', textures: { all: 'politicalserver:block/' + id } }; }
function blockState(id) { return { variants: { '': { model: 'politicalserver:block/' + id } } }; }

async function generateItemBatch(specs, opts = {}) {
  const texDir = opts.texDir || ITEM_TEX;
  const modelDir = opts.modelDir || ITEM_MODELS;
  const defDir = opts.defDir || ITEM_DEFS;
  for (const d of [texDir, modelDir, defDir]) fs.mkdirSync(d, { recursive: true });
  let count = 0;
  for (const [id, spec] of Object.entries(specs)) {
    const cv = canvas();
    const fn = draw[spec.fn];
    if (!fn) { console.warn('no draw fn for', spec.fn, id); continue; }
    fn(cv, spec);
    finishSprite(cv, spec, opts.finish || {});
    await writePng(cv, texDir, id);
    const handheld = opts.alwaysHandheld || HANDHELD.has(spec.fn);
    writeJSON(path.join(modelDir, id + '.json'), itemModel(id, handheld));
    writeJSON(path.join(defDir, id + '.json'), itemDef(id));
    count++;
  }
  console.log((opts.label || 'items') + ':', count);
  return count;
}

async function generateBlockBatch(specs, opts = {}) {
  const texDir = opts.texDir || BLOCK_TEX;
  for (const d of [texDir, BLOCK_MODELS, BLOCKSTATES, ITEM_DEFS]) fs.mkdirSync(d, { recursive: true });
  let count = 0;
  for (const [id, spec] of Object.entries(specs)) {
    const cv = canvas();
    const fn = block[spec.fn];
    if (!fn) { console.warn('no block fn for', spec.fn); continue; }
    fn(cv, spec.pal || spec.palette);
    await writePng(cv, texDir, id);
    writeJSON(path.join(BLOCK_MODELS, id + '.json'), blockModel(id));
    writeJSON(path.join(BLOCKSTATES, id + '.json'), blockState(id));
    writeJSON(path.join(ITEM_DEFS, id + '.json'), blockItemDef(id));
    count++;
  }
  console.log((opts.label || 'blocks') + ':', count);
  return count;
}

async function buildMontage(ids, outName, opts = {}) {
  const scale = opts.scale || 8;
  const cols = opts.cols || 12;
  const pad = opts.pad || 4;
  const texDir = opts.texDir || ITEM_TEX;
  const cell = N * scale + pad;
  const rows = Math.ceil(ids.length / cols);
  fs.mkdirSync(PREVIEW_DIR, { recursive: true });
  const sheet = new Jimp({ width: cols * cell + pad, height: rows * cell + pad, color: opts.bg || 0xff1e1e28 });
  for (let i = 0; i < ids.length; i++) {
    try {
      const img = await Jimp.read(path.join(texDir, ids[i] + '.png'));
      img.resize({ w: N * scale, h: N * scale, mode: 'nearestNeighbor' });
      sheet.composite(img, pad + (i % cols) * cell, pad + Math.floor(i / cols) * cell);
    } catch (e) { /* skip */ }
  }
  const out = path.join(PREVIEW_DIR, outName);
  await sheet.write(out);
  console.log('montage ->', out);
}

function W(fn, opts) { return Object.assign({ fn }, opts); }

module.exports = {
  N, WHITE, BLACK, P, HANDHELD, W,
  canvas, inb, px, get, fillRect, line, disc, darken, mix, outline, rng,
  ambientOcclusion, finishSprite, metalSheen, leatherGrain, woodGrain, gemFacets, foodGloss, magicGlow, refRamp,
  bladeFill, grip, crossguard, pommel, draw, block,
  ASSETS, ITEM_TEX, BLOCK_TEX, ENTITY_TEX, ITEM_MODELS, BLOCK_MODELS, ITEM_DEFS, BLOCKSTATES, PREVIEW_DIR,
  writePng, writeJSON, itemModel, itemDef, blockItemDef, blockModel, blockState,
  generateItemBatch, generateBlockBatch, buildMontage,
};
`;

// Remove duplicate const N from body
const fixedBody = body.replace(/^const N = 16;\r?\n/m, '');

fs.writeFileSync(path.join(__dirname, 'pixel-art-lib.js'), header + fixedBody + tail);
console.log('pixel-art-lib.js created');
