/**
 * Texture + asset generator for dungeon-themed blocks (dungeon_* ids).
 * Standalone — does not modify tools/generate-textures.js.
 *
 * Usage: node tools/gen-dungeon-blocks.js
 */
const { Jimp } = require('jimp');
const path = require('path');
const fs = require('fs');

const ASSETS = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'politicalserver');
const BLOCK_TEX = path.join(ASSETS, 'textures', 'block');
const ITEM_TEX = path.join(ASSETS, 'textures', 'item');
const BLOCK_MODELS = path.join(ASSETS, 'models', 'block');
const ITEM_DEFS = path.join(ASSETS, 'items');
const BLOCKSTATES = path.join(ASSETS, 'blockstates');

const N = 16;
function canvas() { return Array.from({ length: N }, () => Array.from({ length: N }, () => [0, 0, 0, 0])); }
function px(cv, x, y, c) { if (x >= 0 && x < N && y >= 0 && y < N && c) cv[y][x] = c; }
function pxw(cv, x, y, c) { px(cv, ((x % N) + N) % N, ((y % N) + N) % N, c); }
function fill(cv, c) { for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) px(cv, x, y, c); }
function fillRect(cv, x0, y0, x1, y1, c) { for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) px(cv, x, y, c); }
function rng(seed) { let s = seed >>> 0; return () => { s = (s * 1664525 + 1013904223) >>> 0; return s / 4294967296; }; }
function shade(base, v) {
  return [Math.max(0, Math.min(255, base[0] + v)), Math.max(0, Math.min(255, base[1] + v)), Math.max(0, Math.min(255, base[2] + v)), 255];
}

const P = {
  stone: [[80, 78, 76], [110, 108, 104], [140, 136, 130], [168, 164, 156], [190, 186, 178]],
  moss: [[48, 72, 36], [64, 96, 48], [82, 118, 58], [100, 140, 72], [120, 162, 88]],
  purple: [[48, 16, 64], [72, 28, 96], [96, 44, 128], [120, 64, 160], [148, 92, 192]],
  cyan: [[16, 48, 64], [24, 72, 96], [36, 104, 128], [52, 136, 160], [72, 168, 192]],
  obsidian: [[12, 8, 20], [24, 16, 36], [40, 28, 52], [56, 40, 72], [72, 56, 96]],
  crystal: [[64, 24, 96], [96, 48, 140], [128, 72, 180], [160, 108, 210], [200, 148, 240]],
  water: [[24, 56, 88], [36, 80, 120], [48, 108, 152], [64, 136, 180], [88, 168, 208]],
};

function bricks(cv, pal, r) {
  fill(cv, pal[1]);
  for (let y = 0; y < N; y++) {
    const off = (Math.floor(y / 4) % 2) * 4;
    for (let x = 0; x < N; x++) {
      const bx = (x + off) % 8;
      const by = y % 4;
      if (bx === 0 || by === 0) pxw(cv, x, y, pal[0]);
      else pxw(cv, x, y, shade(pal[2], (r() - 0.5) * 20));
    }
  }
}

function cracked(cv, pal, r) {
  bricks(cv, pal, r);
  for (let i = 0; i < 6; i++) {
    let x = Math.floor(r() * N), y = Math.floor(r() * N);
    for (let j = 0; j < 5; j++) { pxw(cv, x + j, y, pal[0]); pxw(cv, x, y + j, pal[0]); }
  }
}

function mossy(cv, pal, moss, r) {
  cracked(cv, pal, r);
  for (let i = 0; i < 40; i++) pxw(cv, Math.floor(r() * N), Math.floor(r() * N), moss[2 + Math.floor(r() * 2)]);
}

function glow(cv, pal, r) {
  fill(cv, pal[1]);
  const cx = 8, cy = 8;
  for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) {
    const d = Math.hypot(x - cx, y - cy);
    if (d < 6) px(cv, x, y, pal[Math.min(4, 2 + Math.floor(d / 2))]);
    else px(cv, x, y, shade(pal[1], (r() - 0.5) * 16));
  }
}

function mosaic(cv, pal, r) {
  for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) {
    const t = ((x >> 2) + (y >> 2)) % 2;
    px(cv, x, y, t ? pal[2] : pal[3]);
    if (r() < 0.08) px(cv, x, y, pal[4]);
  }
}

const BLOCKS = {
  dungeon_cracked_stone: { fn: cracked, pal: P.stone, seed: 101 },
  dungeon_cursed_brick: { fn: bricks, pal: P.purple, seed: 202 },
  dungeon_soul_lantern: { fn: glow, pal: P.cyan, seed: 303 },
  dungeon_boss_altar: { fn: glow, pal: P.purple, seed: 404 },
  dungeon_mossy_crypt: { fn: mossy, pal: P.stone, moss: P.moss, seed: 505 },
  dungeon_obsidian_brick: { fn: bricks, pal: P.obsidian, seed: 606 },
  dungeon_crystal_tile: { fn: mosaic, pal: P.crystal, seed: 707 },
  dungeon_flooded_mosaic: { fn: mosaic, pal: P.water, seed: 808 },
};

async function writePng(cv, dir, name) {
  fs.mkdirSync(dir, { recursive: true });
  const img = new Jimp({ width: N, height: N, color: 0x00000000 });
  for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) {
    const c = cv[y][x];
    if (!c || c[3] === 0) continue;
    img.setPixelColor(((c[0] << 24) | (c[1] << 16) | (c[2] << 8) | (c[3] ?? 255)) >>> 0, x, y);
  }
  await img.write(path.join(dir, `${name}.png`));
}

function writeJSON(file, obj) { fs.writeFileSync(file, JSON.stringify(obj, null, 2) + '\n'); }
function blockModel(id) { return { parent: 'minecraft:block/cube_all', textures: { all: `politicalserver:block/${id}` } }; }
function blockState(id) { return { variants: { '': { model: `politicalserver:block/${id}` } } }; }
function blockItemDef(id) { return { model: { type: 'minecraft:model', model: `politicalserver:block/${id}` } }; }

async function main() {
  for (const [id, spec] of Object.entries(BLOCKS)) {
    const cv = canvas();
    const r = rng(spec.seed);
    if (spec.fn === mossy) spec.fn(cv, spec.pal, spec.moss, r);
    else spec.fn(cv, spec.pal, r);
    await writePng(cv, BLOCK_TEX, id);
    writeJSON(path.join(BLOCK_MODELS, `${id}.json`), blockModel(id));
    writeJSON(path.join(BLOCKSTATES, `${id}.json`), blockState(id));
    writeJSON(path.join(ITEM_DEFS, `${id}.json`), blockItemDef(id));
    console.log('block', id);
  }

  // Structure compass item texture (simple gold compass rose)
  const cv = canvas();
  const r = rng(909);
  fill(cv, [0, 0, 0, 0]);
  for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) {
    const d = Math.hypot(x - 8, y - 8);
    if (d < 7 && d > 2) px(cv, x, y, d < 4 ? [220, 180, 60, 255] : [180, 140, 40, 255]);
  }
  px(cv, 8, 3, [255, 80, 80, 255]);
  px(cv, 8, 4, [255, 80, 80, 255]);
  await writePng(cv, ITEM_TEX, 'structure_compass');
  writeJSON(path.join(ASSETS, 'models', 'item', 'structure_compass.json'), {
    parent: 'minecraft:item/generated',
    textures: { layer0: 'politicalserver:item/structure_compass' }
  });
  writeJSON(path.join(ITEM_DEFS, 'structure_compass.json'), {
    model: { type: 'minecraft:model', model: 'politicalserver:item/structure_compass' }
  });
  console.log('item structure_compass');
  console.log('Done —', Object.keys(BLOCKS).length, 'dungeon blocks');
}

main().catch(e => { console.error(e); process.exit(1); });
