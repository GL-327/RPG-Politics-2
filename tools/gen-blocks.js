/**
 * High-quality 16x16 *tiling* block-texture + asset generator for the
 * politicalserver expansion decoration palette (com.political.expansion.blocks).
 *
 * Style goals (emulating Prominence II / RAD2 building & decoration packs such as
 * Macaw's, Chipped and Rustic):
 *   - seamless wrap-around tiling so blocks read cleanly when stacked
 *   - 5-tone shading ramps per material (shadow / dark / mid / light / specular)
 *   - believable masonry: running-bond bricks, basketweave, fish-scale shingles,
 *     standing-seam metal roofing, fluted columns, veined marble, speckled granite
 *   - glowing lamp/lantern cores for the light-emitting blocks
 *
 * It also (re)writes the matching block models, blockstates and item-definition
 * files so every expansion block is wired consistently with the repo pattern
 * (parent minecraft:block/cube_all, variant "" -> block model, item def -> model).
 *
 * This file is intentionally standalone (does NOT touch tools/generate-textures.js)
 * and only writes assets for ids prefixed `dec_`.
 *
 * Usage: node gen-blocks.js   (requires tools/node_modules with jimp installed)
 */
const { Jimp } = require('jimp');
const path = require('path');
const fs = require('fs');

const ASSETS = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'politicalserver');
const BLOCK_TEX = path.join(ASSETS, 'textures', 'block');
const BLOCK_MODELS = path.join(ASSETS, 'models', 'block');
const ITEM_DEFS = path.join(ASSETS, 'items');
const BLOCKSTATES = path.join(ASSETS, 'blockstates');
const PREVIEW_DIR = path.join(__dirname, '..', '.texref');

// ----------------------------------------------------------------------------
// Canvas helpers
// ----------------------------------------------------------------------------
const N = 16;
function canvas() { return Array.from({ length: N }, () => Array.from({ length: N }, () => null)); }
function inb(x, y) { return x >= 0 && x < N && y >= 0 && y < N; }
function px(cv, x, y, c) { x = Math.round(x); y = Math.round(y); if (inb(x, y) && c) cv[y][x] = c; }
// wrapping plot — keeps patterns seamless across the 16x16 tile boundary
function pxw(cv, x, y, c) { x = ((Math.round(x) % N) + N) % N; y = ((Math.round(y) % N) + N) % N; if (c) cv[y][x] = c; }
function fillRect(cv, x0, y0, x1, y1, c) { for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) px(cv, x, y, c); }
function line(cv, x0, y0, x1, y1, c) {
  x0 = Math.round(x0); y0 = Math.round(y0); x1 = Math.round(x1); y1 = Math.round(y1);
  const dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
  const sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
  let err = dx + dy;
  for (;;) {
    px(cv, x0, y0, c);
    if (x0 === x1 && y0 === y1) break;
    const e2 = 2 * err;
    if (e2 >= dy) { err += dy; x0 += sx; }
    if (e2 <= dx) { err += dx; y0 += sy; }
  }
}
function disc(cv, cx, cy, r, c) {
  for (let y = -r; y <= r; y++) for (let x = -r; x <= r; x++)
    if (x * x + y * y <= r * r + r * 0.4) px(cv, cx + x, cy + y, c);
}
function mix(a, b, t) { return [Math.round(a[0] + (b[0] - a[0]) * t), Math.round(a[1] + (b[1] - a[1]) * t), Math.round(a[2] + (b[2] - a[2]) * t), 255]; }
function rng(seed) { let s = seed >>> 0; return () => { s = (s * 1664525 + 1013904223) >>> 0; return s / 4294967296; }; }
function pick(arr, r) { return arr[Math.floor(r() * arr.length) % arr.length]; }
function shade(cv, x, y, base, r) {
  const v = (r() - 0.5) * 0.22;
  px(cv, x, y, [
    Math.max(0, Math.min(255, Math.round(base[2][0] * (1 + v)))),
    Math.max(0, Math.min(255, Math.round(base[2][1] * (1 + v)))),
    Math.max(0, Math.min(255, Math.round(base[2][2] * (1 + v)))),
  ]);
}

const WHITE = [255, 255, 255];

// ----------------------------------------------------------------------------
// Palettes — 5 stops: [shadow, dark, mid, light, specular]
// ----------------------------------------------------------------------------
const P = {
  red_brick:    [[64, 24, 18], [120, 46, 34], [168, 70, 52], [200, 104, 84], [226, 150, 130]],
  blonde_brick: [[120, 98, 52], [170, 142, 84], [206, 178, 116], [230, 210, 158], [246, 234, 196]],
  charcoal:     [[16, 16, 20], [34, 34, 40], [56, 56, 64], [82, 82, 92], [112, 112, 124]],
  ivory:        [[150, 142, 124], [196, 188, 168], [222, 216, 198], [238, 234, 222], [250, 248, 240]],
  stone:        [[58, 58, 64], [90, 90, 98], [122, 122, 132], [156, 156, 166], [188, 188, 198]],
  terracotta:   [[78, 42, 24], [128, 70, 40], [170, 100, 58], [200, 132, 84], [226, 168, 120]],
  azure:        [[18, 58, 96], [34, 98, 150], [60, 150, 206], [120, 196, 236], [196, 234, 252]],
  emerald:      [[10, 58, 34], [22, 124, 70], [44, 184, 108], [120, 224, 160], [200, 250, 222]],
  white_marble: [[150, 150, 158], [196, 196, 202], [224, 224, 228], [240, 240, 244], [252, 252, 255]],
  black_marble: [[12, 12, 16], [28, 28, 34], [48, 48, 56], [78, 78, 88], [120, 120, 134]],
  rose_marble:  [[120, 78, 86], [176, 120, 128], [212, 168, 176], [234, 204, 210], [250, 234, 238]],
  cobalt:       [[14, 28, 82], [26, 56, 150], [44, 98, 208], [96, 150, 238], [180, 212, 255]],
  granite_grey: [[52, 52, 58], [84, 84, 92], [120, 120, 128], [150, 150, 160], [182, 182, 192]],
  granite_pink: [[96, 58, 54], [150, 98, 90], [190, 140, 128], [214, 176, 162], [236, 212, 200]],
  crimson:      [[48, 8, 14], [112, 22, 30], [176, 42, 48], [222, 88, 82], [252, 168, 150]],
  jade:         [[10, 52, 36], [24, 110, 76], [48, 168, 118], [120, 216, 172], [200, 248, 222]],
  amber:        [[96, 52, 8], [168, 104, 18], [224, 154, 40], [246, 196, 96], [255, 232, 168]],
  violet:       [[36, 16, 60], [84, 40, 134], [140, 76, 196], [186, 128, 228], [230, 196, 252]],
  quartz:       [[150, 142, 132], [196, 190, 180], [224, 220, 212], [240, 238, 232], [252, 252, 248]],
  sand:         [[140, 116, 70], [186, 160, 104], [214, 192, 136], [234, 218, 176], [248, 238, 210]],
  iron:         [[38, 40, 48], [92, 98, 110], [150, 156, 168], [200, 206, 216], [240, 244, 250]],
  gold:         [[96, 60, 12], [168, 118, 24], [226, 176, 40], [246, 214, 96], [255, 248, 188]],
  slate:        [[24, 26, 34], [44, 48, 60], [70, 76, 92], [100, 108, 128], [140, 150, 172]],
  copper:       [[78, 38, 22], [150, 76, 44], [200, 114, 72], [230, 158, 112], [250, 200, 166]],
  thatch:       [[96, 72, 28], [150, 116, 52], [196, 160, 84], [224, 196, 128], [244, 226, 170]],
  cream:        [[150, 138, 108], [200, 188, 156], [226, 216, 188], [240, 234, 214], [250, 246, 232]],
  timberwood:   [[36, 22, 12], [64, 40, 22], [92, 60, 34], [120, 84, 50], [150, 114, 76]],
  basalt:       [[16, 16, 22], [34, 34, 42], [54, 54, 64], [80, 80, 92], [112, 112, 126]],
  banner_blue:  [[14, 28, 82], [26, 56, 150], [44, 98, 208], [96, 150, 238], [180, 212, 255]],
  banner_green: [[10, 52, 30], [22, 110, 60], [44, 168, 96], [110, 214, 150], [196, 248, 210]],
  // glow ramps for light-emitting cores
  glow_white:   [[120, 118, 90], [200, 196, 140], [236, 232, 170], [250, 248, 210], [255, 255, 238]],
  glow_warm:    [[120, 70, 20], [200, 140, 40], [244, 196, 80], [252, 224, 140], [255, 248, 200]],
  glow_red:     [[90, 20, 20], [170, 50, 40], [224, 96, 72], [244, 150, 120], [255, 200, 170]],
  rune:         [[20, 80, 90], [40, 150, 160], [80, 210, 210], [150, 240, 235], [220, 255, 252]],
};

// fleck/accent colour sets (plain RGB lists)
const MOSS = [[28, 52, 22], [52, 86, 36], [86, 124, 52]];
const PATINA = [[60, 140, 120], [90, 176, 150], [120, 200, 176]];
const GRANITE_FLECK = [[210, 210, 220], [120, 70, 70], [70, 70, 80]];
const PINK_FLECK = [[230, 210, 200], [150, 90, 80], [110, 70, 70]];

// ----------------------------------------------------------------------------
// Block draw functions (all seamless / tile-safe)
// ----------------------------------------------------------------------------
const block = {};

// running-bond brick masonry, with optional moss speckle
block.bricks = (cv, pal, opts = {}) => {
  const r = rng(opts.seed || 7);
  fillRect(cv, 0, 0, 15, 15, pal[0]); // mortar
  for (let row = 0; row < 4; row++) {
    const y0 = row * 4;
    const offset = (row % 2) * 4;
    for (let bx = -1; bx < 4; bx++) {
      const x0 = bx * 8 + offset;
      for (let y = y0; y < y0 + 3; y++) for (let x = x0; x < x0 + 7; x++) {
        const xx = ((x % 16) + 16) % 16;
        let c = pal[2];
        if (y === y0) c = pal[3];
        if (y === y0 + 2) c = pal[1];
        px(cv, xx, y, c);
        if (r() > 0.8) shade(cv, xx, y, pal, r);
      }
    }
  }
  if (opts.moss) for (let i = 0; i < 46; i++) { if (r() > 0.4) pxw(cv, Math.floor(r() * 16), Math.floor(r() * 16), pick(opts.moss, r)); }
};

// basketweave / parquet (tiles perfectly on an 8px grid)
block.weave = (cv, pal) => {
  const r = rng(5);
  fillRect(cv, 0, 0, 15, 15, pal[0]); // grout
  for (let by = 0; by < 16; by += 8) for (let bx = 0; bx < 16; bx += 8) {
    const horiz = (((bx + by) / 8) % 2) === 0;
    for (let plank = 0; plank < 8; plank += 3) {
      for (let d = 0; d < 2; d++) {
        const along = plank + d;
        for (let t = 0; t < 8; t++) {
          let x, y;
          if (horiz) { x = bx + t; y = by + along; } else { x = bx + along; y = by + t; }
          let c = d === 0 ? pal[3] : pal[2];
          if (t === 7) c = pal[1];
          px(cv, x, y, c);
          if (r() > 0.88) shade(cv, x, y, pal, r);
        }
      }
    }
  }
};

// square ceramic tiles (2x2 of 8px) with grout joints
block.tile = (cv, pal) => {
  const r = rng(9);
  fillRect(cv, 0, 0, 15, 15, pal[1]); // grout
  for (let cy = 0; cy < 2; cy++) for (let cx = 0; cx < 2; cx++) {
    const x0 = cx * 8, y0 = cy * 8;
    for (let y = y0; y < y0 + 7; y++) for (let x = x0; x < x0 + 7; x++) {
      let c = pal[2];
      if (x === x0 || y === y0) c = pal[3];
      if (x === x0 + 6 || y === y0 + 6) c = pal[1];
      px(cv, x, y, c);
      if (r() > 0.9) shade(cv, x, y, pal, r);
    }
    px(cv, x0 + 1, y0 + 1, pal[4]);
  }
};

// two-tone checkerboard (4px squares)
block.checker = (cv, pal, opts = {}) => {
  const a = pal, b = opts.alt || P.charcoal;
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const cell = (Math.floor(x / 4) + Math.floor(y / 4)) % 2;
    const ramp = cell === 0 ? a : b;
    let c = ramp[2];
    if ((x % 4) === 0 || (y % 4) === 0) c = ramp[1];
    if ((x % 4) === 1 && (y % 4) === 1) c = ramp[3];
    px(cv, x, y, c);
  }
};

// polished marble: bright base with wandering veins
block.marble = (cv, pal, opts = {}) => {
  const r = rng(opts.seed || 3);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const n = r();
    let c = pal[3];
    if (n > 0.92) c = pal[4]; else if (n < 0.12) c = pal[2];
    px(cv, x, y, c);
  }
  const vein = opts.vein || pal[1];
  const starts = opts.veins || [[0, 3], [6, 0], [11, 0], [3, 9]];
  for (const start of starts) {
    let x = start[0], y = start[1];
    for (let i = 0; i < 24; i++) {
      pxw(cv, x, y, vein);
      if (r() > 0.6) pxw(cv, x + 1, y, mix(vein, pal[3], 0.45));
      if (r() > 0.35) x += 1;
      if (r() > 0.2) y += 1;
      if (r() > 0.85) x += 1;
    }
  }
};

// speckled granite/igneous stone
block.granite = (cv, pal, opts = {}) => {
  const r = rng(opts.seed || 17);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const n = r();
    let c = pal[2];
    if (n > 0.8) c = pal[3]; else if (n > 0.62) c = pal[1]; else if (n < 0.1) c = pal[0]; else if (n < 0.18) c = pal[4];
    px(cv, x, y, c);
  }
  if (opts.fleck) for (let i = 0; i < 28; i++) pxw(cv, Math.floor(r() * 16), Math.floor(r() * 16), pick(opts.fleck, r));
};

// flat-coloured plaster/render facade with subtle panel seams
block.facade = (cv, pal) => {
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    let c = pal[2];
    if ((x + y) % 8 === 0) c = pal[3];
    px(cv, x, y, c);
  }
  fillRect(cv, 0, 7, 15, 8, pal[1]);
  fillRect(cv, 7, 0, 8, 15, pal[1]);
  px(cv, 1, 1, pal[4]); px(cv, 9, 9, pal[4]);
};

// fluted column / pillar shaft
block.column = (cv, pal) => {
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    let c = pal[2];
    if (x < 2 || x > 13) c = pal[1];
    else if (x < 4) c = pal[3];
    px(cv, x, y, c);
  }
  for (const gx of [4, 8, 11]) for (let y = 0; y < 16; y++) px(cv, gx, y, pal[1]);
  for (const gx of [5, 9, 12]) for (let y = 0; y < 16; y++) px(cv, gx, y, pal[3]);
  fillRect(cv, 0, 0, 15, 1, pal[3]); fillRect(cv, 0, 14, 15, 15, pal[1]);
};

// caged lamp/lantern with glowing core
block.lantern = (cv, pal, opts = {}) => {
  const glow = opts.glow || P.glow_warm;
  fillRect(cv, 0, 0, 15, 15, pal[1]);     // frame
  fillRect(cv, 2, 2, 13, 13, pal[2]);
  for (let y = 2; y <= 13; y++) for (let x = 2; x <= 13; x++) {
    const d = Math.abs(x - 7.5) + Math.abs(y - 7.5);
    let c = glow[2]; if (d < 3) c = glow[4]; else if (d < 5) c = glow[3];
    px(cv, x, y, c);
  }
  for (const gx of [2, 7, 8, 13]) for (let y = 2; y <= 13; y++) px(cv, gx, y, pal[1]); // cage bars
  fillRect(cv, 2, 2, 13, 2, pal[3]); fillRect(cv, 2, 13, 13, 13, pal[0]);
  for (const [x, y] of [[1, 1], [14, 1], [1, 14], [14, 14]]) px(cv, x, y, pal[3]); // corner bolts
};

// soft paper lantern: warm diffuse glow with light ribbing
block.paper = (cv, pal, opts = {}) => {
  const glow = opts.glow || P.glow_red;
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const d = Math.abs(x - 7.5) + Math.abs(y - 7.5);
    let c = pal[2];
    if (d < 3) c = glow[4]; else if (d < 6) c = glow[3]; else if (d < 8) c = mix(glow[2], pal[2], 0.5);
    px(cv, x, y, c);
  }
  for (const gx of [3, 8, 12]) for (let y = 0; y < 16; y++) px(cv, gx, y, mix(pal[1], glow[1], 0.4)); // ribs
  fillRect(cv, 0, 0, 15, 0, pal[1]); fillRect(cv, 0, 15, 15, 15, pal[1]);
};

// fish-scale roof shingles (offset rows)
block.shingles = (cv, pal) => {
  const r = rng(11);
  fillRect(cv, 0, 0, 15, 15, pal[1]);
  for (let row = 0; row < 4; row++) {
    const y0 = row * 4;
    const off = (row % 2) * 4;
    for (let sx = -1; sx < 4; sx++) {
      const x0 = sx * 8 + off;
      for (let y = 0; y < 4; y++) for (let x = 0; x < 7; x++) {
        const xx = ((x0 + x) % 16 + 16) % 16, yy = y0 + y;
        let c = pal[2];
        if (y === 0) c = pal[3];
        if (y === 3) c = pal[0];           // overlap shadow under the scale
        if (x === 0 || x === 6) c = pal[1];
        px(cv, xx, yy, c);
        if (r() > 0.86) shade(cv, xx, yy, pal, r);
      }
    }
  }
};

// straw thatch roofing
block.thatch = (cv, pal) => {
  const r = rng(23);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const n = r(); let c = pal[2];
    if (n > 0.7) c = pal[3]; else if (n < 0.25) c = pal[1];
    px(cv, x, y, c);
  }
  for (let y = 3; y < 16; y += 5) for (let x = 0; x < 16; x++) px(cv, x, y, pal[0]); // binding cords
  for (let i = 0; i < 30; i++) { const x = Math.floor(r() * 16), y = Math.floor(r() * 16), len = 2 + Math.floor(r() * 3); for (let k = 0; k < len; k++) pxw(cv, x, y + k, pal[4]); }
};

// standing-seam metal roof (vertical seams + optional patina)
block.metalroof = (cv, pal, opts = {}) => {
  const r = rng(opts.seed || 29);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    let c = pal[2];
    if ((x % 4) === 0) c = pal[3];
    if ((x % 4) === 3) c = pal[1];
    px(cv, x, y, c);
  }
  for (let x = 3; x < 16; x += 4) for (let y = 0; y < 16; y++) px(cv, x, y, pal[0]); // raised seams
  for (let x = 0; x < 16; x++) px(cv, x, 8, mix(pal[1], pal[0], 0.5));               // panel seam
  if (opts.patina) for (let i = 0; i < 30; i++) pxw(cv, Math.floor(r() * 16), Math.floor(r() * 16), pick(opts.patina, r));
};

// ornate framed stone with central diamond boss
block.ornate = (cv, pal, opts = {}) => {
  const ac = opts.accent || pal[4];
  fillRect(cv, 0, 0, 15, 15, pal[2]);
  fillRect(cv, 0, 0, 15, 0, pal[3]); fillRect(cv, 0, 15, 15, 15, pal[0]);
  fillRect(cv, 0, 0, 0, 15, pal[3]); fillRect(cv, 15, 0, 15, 15, pal[1]);
  fillRect(cv, 2, 2, 13, 13, pal[1]);
  fillRect(cv, 3, 3, 12, 12, pal[2]);
  const cx = 7.5, cy = 7.5;
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const d = Math.abs(x - cx) + Math.abs(y - cy);
    if (d > 2.0 && d < 3.6) px(cv, x, y, ac);
  }
  px(cv, 7, 7, ac); px(cv, 8, 8, ac); px(cv, 7, 8, ac); px(cv, 8, 7, ac);
  for (const [x, y] of [[2, 2], [13, 2], [2, 13], [13, 13]]) px(cv, x, y, pal[4]);
};

// carved Greek-key (fret) relief
block.carved = (cv, pal) => {
  fillRect(cv, 0, 0, 15, 15, pal[2]);
  fillRect(cv, 0, 0, 15, 1, pal[3]); fillRect(cv, 0, 14, 15, 15, pal[1]);
  const g = pal[1], h = pal[3];
  for (let bx = 0; bx < 16; bx += 8) {
    line(cv, bx + 1, 4, bx + 6, 4, g);
    line(cv, bx + 6, 4, bx + 6, 10, g);
    line(cv, bx + 6, 10, bx + 2, 10, g);
    line(cv, bx + 2, 10, bx + 2, 7, g);
    line(cv, bx + 2, 7, bx + 4, 7, g);
    line(cv, bx + 1, 3, bx + 6, 3, h);
  }
};

// dark stone with glowing rune ring
block.runic = (cv, pal, opts = {}) => {
  const r = rng(opts.seed || 41);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const n = r(); let c = pal[2];
    if (n > 0.82) c = pal[3]; else if (n < 0.2) c = pal[1];
    px(cv, x, y, c);
  }
  const ru = opts.glow || P.rune;
  for (let a = 0; a < 360; a += 20) { const x = 8 + Math.round(5 * Math.cos(a * Math.PI / 180)), y = 8 + Math.round(5 * Math.sin(a * Math.PI / 180)); px(cv, x, y, ru[3]); }
  line(cv, 8, 4, 8, 11, ru[4]); line(cv, 5, 7, 11, 7, ru[4]);
  px(cv, 6, 5, ru[3]); px(cv, 10, 9, ru[3]); px(cv, 8, 8, WHITE);
};

// smooth stucco/plaster render with a hairline crack
block.plaster = (cv, pal) => {
  const r = rng(43);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const n = r(); let c = pal[3];
    if (n > 0.85) c = pal[4]; else if (n < 0.18) c = pal[2];
    px(cv, x, y, c);
  }
  let x = 4, y = 0;
  for (let i = 0; i < 15; i++) { pxw(cv, x, y, pal[1]); y++; if (r() > 0.6) x += (r() > 0.5 ? 1 : -1); }
};

// tudor timber-frame: cream daub panel crossed by dark beams + brace
block.timber = (cv, pal, opts = {}) => {
  const wood = opts.wood || P.timberwood;
  const fill = opts.fill || P.cream;
  const r = rng(47);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) px(cv, x, y, fill[3]);
  for (let i = 0; i < 22; i++) { const x = 4 + Math.floor(r() * 8), y = 4 + Math.floor(r() * 8); px(cv, x, y, fill[2]); }
  fillRect(cv, 0, 0, 15, 2, wood[2]); fillRect(cv, 0, 13, 15, 15, wood[2]);
  fillRect(cv, 0, 0, 2, 15, wood[2]); fillRect(cv, 13, 0, 15, 15, wood[2]);
  for (let i = 0; i < 16; i++) { pxw(cv, i, 15 - i, wood[2]); pxw(cv, i, 14 - i, wood[1]); } // diagonal brace
  fillRect(cv, 0, 0, 15, 0, wood[3]); fillRect(cv, 0, 15, 15, 15, wood[1]);
};

// layered sedimentary sandstone
block.sandstone = (cv, pal) => {
  const r = rng(53);
  for (let y = 0; y < 16; y++) {
    let base = pal[2];
    if (y % 5 === 0) base = pal[1];
    if (y % 5 === 1) base = pal[3];
    for (let x = 0; x < 16; x++) {
      let c = base;
      if (r() > 0.85) c = mix(base, pal[3], 0.4); else if (r() < 0.12) c = mix(base, pal[1], 0.4);
      px(cv, x, y, c);
    }
  }
};

// polished columnar basalt
block.polished = (cv, pal) => {
  const r = rng(59);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const n = r(); let c = pal[2];
    if (n > 0.85) c = pal[3]; else if (n < 0.18) c = pal[1];
    px(cv, x, y, c);
  }
  for (const gx of [5, 10]) for (let y = 0; y < 16; y++) px(cv, gx, y, pal[1]);
  fillRect(cv, 0, 0, 15, 0, pal[3]);
};

// hanging cloth banner with gold emblem
block.banner = (cv, pal, opts = {}) => {
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    let c = pal[2];
    if (x < 2) c = pal[3]; if (x > 13) c = pal[1];
    px(cv, x, y, c);
  }
  for (const fx of [4, 8, 12]) for (let y = 0; y < 16; y++) px(cv, fx, y, pal[1]);
  for (const fx of [5, 9, 13]) for (let y = 0; y < 16; y++) px(cv, fx, y, pal[3]);
  const g = opts.emblem || P.gold;
  line(cv, 8, 3, 8, 11, g[3]); line(cv, 5, 7, 11, 7, g[3]);
  px(cv, 8, 2, g[4]); px(cv, 6, 5, g[2]); px(cv, 10, 5, g[2]); disc(cv, 8, 7, 1, g[2]);
};

// ----------------------------------------------------------------------------
// SPECS — id -> { fn, pal, ...opts }   (ids must match DecoBlocks.java)
// ----------------------------------------------------------------------------
function B(fn, pal, opts) { return Object.assign({ fn, pal }, opts); }

const BLOCK_SPECS = {
  // ---- Decorative bricks ----
  dec_red_brick:          B('bricks', P.red_brick),
  dec_blonde_brick:       B('bricks', P.blonde_brick, { seed: 12 }),
  dec_charcoal_brick:     B('bricks', P.charcoal, { seed: 19 }),
  dec_ivory_brick:        B('bricks', P.ivory, { seed: 25 }),
  dec_mossy_castle_brick: B('bricks', P.stone, { seed: 31, moss: MOSS }),
  dec_herringbone_brick:  B('weave', P.terracotta),

  // ---- Tiles ----
  dec_checkered_tile:     B('checker', P.ivory, { alt: P.charcoal }),
  dec_terracotta_tile:    B('tile', P.terracotta),
  dec_azure_tile:         B('tile', P.azure),
  dec_emerald_tile:       B('tile', P.emerald),

  // ---- Marble ----
  dec_white_marble:       B('marble', P.white_marble, { vein: [120, 120, 130] }),
  dec_black_marble:       B('marble', P.black_marble, { vein: [150, 150, 162], seed: 8 }),
  dec_rose_marble:        B('marble', P.rose_marble, { vein: [150, 96, 104], seed: 14 }),
  dec_cobalt_marble:      B('marble', P.cobalt, { vein: [180, 212, 255], seed: 22 }),
  dec_marble_pillar:      B('column', P.white_marble),

  // ---- Granite ----
  dec_grey_granite:       B('granite', P.granite_grey, { fleck: GRANITE_FLECK }),
  dec_pink_granite:       B('granite', P.granite_pink, { fleck: PINK_FLECK, seed: 27 }),

  // ---- Stained facades ----
  dec_crimson_facade:     B('facade', P.crimson),
  dec_cobalt_facade:      B('facade', P.cobalt),
  dec_jade_facade:        B('facade', P.jade),
  dec_amber_facade:       B('facade', P.amber),
  dec_violet_facade:      B('facade', P.violet),

  // ---- Columns / pillars ----
  dec_fluted_column:      B('column', P.quartz),
  dec_sandstone_column:   B('column', P.sand),

  // ---- Lamps & lanterns (light-emitting) ----
  dec_iron_lantern:       B('lantern', P.iron, { glow: P.glow_white }),
  dec_gold_lantern:       B('lantern', P.gold, { glow: P.glow_warm }),
  dec_glowstone_lamp:     B('lantern', P.sand, { glow: P.glow_warm }),
  dec_paper_lantern:      B('paper', P.crimson, { glow: P.glow_red }),

  // ---- Roofing ----
  dec_slate_shingles:     B('shingles', P.slate),
  dec_red_shingles:       B('shingles', P.red_brick),
  dec_copper_roof:        B('metalroof', P.copper, { patina: PATINA }),
  dec_thatch_roof:        B('thatch', P.thatch),

  // ---- Ornate stone ----
  dec_ornate_stone:       B('ornate', P.stone, { accent: P.gold[3] }),
  dec_carved_stone:       B('carved', P.stone),
  dec_runic_stone:        B('runic', P.basalt, { glow: P.rune }),

  // ---- Plaster & structural ----
  dec_cream_plaster:      B('plaster', P.cream),
  dec_timber_frame:       B('timber', P.timberwood, { wood: P.timberwood, fill: P.cream }),
  dec_sandstone_block:    B('sandstone', P.sand),
  dec_polished_basalt:    B('polished', P.basalt),

  // ---- Banners ----
  dec_blue_banner_block:  B('banner', P.banner_blue, { emblem: P.gold }),
  dec_green_banner_block: B('banner', P.banner_green, { emblem: P.gold }),
  dec_gold_banner_block:  B('banner', P.gold, { emblem: P.ivory }),
};

// ----------------------------------------------------------------------------
// Rendering + asset writing
// ----------------------------------------------------------------------------
async function writePng(cv, dir, name) {
  const img = new Jimp({ width: N, height: N, color: 0x00000000 });
  for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) {
    const c = cv[y][x]; if (!c) continue;
    const a = c[3] == null ? 255 : c[3];
    img.setPixelColor(((c[0] << 24) | (c[1] << 16) | (c[2] << 8) | a) >>> 0, x, y);
  }
  await img.write(path.join(dir, `${name}.png`));
}

function writeJSON(file, obj) { fs.writeFileSync(file, JSON.stringify(obj, null, 2) + '\n'); }
function blockItemDef(id) { return { model: { type: 'minecraft:model', model: `politicalserver:block/${id}` } }; }
function blockModel(id) { return { parent: 'minecraft:block/cube_all', textures: { all: `politicalserver:block/${id}` } }; }
function blockState(id) { return { variants: { '': { model: `politicalserver:block/${id}` } } }; }

async function generateBlocks() {
  for (const [id, spec] of Object.entries(BLOCK_SPECS)) {
    const cv = canvas();
    const fn = block[spec.fn];
    if (!fn) { console.warn('no block fn for', spec.fn); continue; }
    fn(cv, spec.pal, spec);
    await writePng(cv, BLOCK_TEX, id);
    writeJSON(path.join(BLOCK_MODELS, `${id}.json`), blockModel(id));
    writeJSON(path.join(BLOCKSTATES, `${id}.json`), blockState(id));
    writeJSON(path.join(ITEM_DEFS, `${id}.json`), blockItemDef(id));
  }
  console.log('expansion blocks:', Object.keys(BLOCK_SPECS).length);
}

// montage of everything we produced (for visual QA)
async function buildPreview() {
  fs.mkdirSync(PREVIEW_DIR, { recursive: true });
  const ids = Object.keys(BLOCK_SPECS);
  const scale = 8, cols = 8, pad = 4, cell = N * scale + pad;
  const rows = Math.ceil(ids.length / cols);
  const sheet = new Jimp({ width: cols * cell + pad, height: rows * cell + pad, color: 0x2b2b33ff });
  for (let i = 0; i < ids.length; i++) {
    try {
      const img = await Jimp.read(path.join(BLOCK_TEX, `${ids[i]}.png`));
      img.resize({ w: N * scale, h: N * scale, mode: 'nearestNeighbor' });
      sheet.composite(img, pad + (i % cols) * cell, pad + Math.floor(i / cols) * cell);
    } catch (e) { /* skip */ }
  }
  await sheet.write(path.join(PREVIEW_DIR, 'blocks_montage.png'));
  console.log('preview ->', path.join(PREVIEW_DIR, 'blocks_montage.png'));
}

async function main() {
  for (const d of [BLOCK_TEX, BLOCK_MODELS, ITEM_DEFS, BLOCKSTATES]) fs.mkdirSync(d, { recursive: true });
  await generateBlocks();
  await buildPreview();
  console.log('done.');
}

main().catch(e => { console.error(e); process.exit(1); });
