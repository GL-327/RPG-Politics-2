/**
 * Expansion 2 decorative block texture + asset generator (com.political.expansion2.blocks).
 * Seamless 16x16 tiling textures; ids prefixed dec2_.
 * Usage: node gen-blocks2.js
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

const N = 16;
function canvas() { return Array.from({ length: N }, () => Array.from({ length: N }, () => null)); }
function inb(x, y) { return x >= 0 && x < N && y >= 0 && y < N; }
function px(cv, x, y, c) { x = Math.round(x); y = Math.round(y); if (inb(x, y) && c) cv[y][x] = c; }
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
function ramp(base, seed) {
  const r = rng(seed);
  const f = (i, d) => Math.max(0, Math.min(255, Math.round(base[i] * (1 + d))));
  return [
    [f(0, -0.35), f(1, -0.35), f(2, -0.35)],
    [f(0, -0.15), f(1, -0.15), f(2, -0.15)],
    base.slice(),
    [f(0, 0.12), f(1, 0.12), f(2, 0.12)],
    [f(0, 0.28), f(1, 0.28), f(2, 0.28)],
  ];
}

const WHITE = [255, 255, 255];
const MOSS = [[28, 52, 22], [52, 86, 36], [86, 124, 52]];
const PATINA = [[60, 140, 120], [90, 176, 150], [120, 200, 176]];
const RUST = [[96, 48, 24], [150, 72, 36], [180, 96, 48]];
const GLOW_W = [[120, 118, 90], [200, 196, 140], [236, 232, 170], [250, 248, 210], [255, 255, 238]];
const GLOW_WARM = [[120, 70, 20], [200, 140, 40], [244, 196, 80], [252, 224, 140], [255, 248, 200]];
const GLOW_CYAN = [[20, 80, 100], [40, 160, 190], [80, 220, 240], [150, 245, 255], [220, 255, 255]];
const GLOW_PRISM = [[80, 40, 120], [140, 80, 200], [200, 140, 255], [230, 190, 255], [255, 240, 255]];

const P = {
  rust: ramp([168, 72, 48], 1), sage: ramp([120, 140, 96], 2), navy: ramp([36, 52, 110], 3),
  wine: ramp([110, 36, 52], 4), pearl: ramp([220, 216, 204], 5), ash: ramp([130, 130, 138], 6),
  sunset: ramp([210, 120, 52], 7), frost: ramp([180, 210, 228], 8), olive: ramp([120, 130, 64], 9),
  plum: ramp([120, 64, 120], 10), coral: ramp([220, 130, 110], 11), midnight: ramp([28, 28, 40], 12),
  honey: ramp([210, 160, 48], 13), storm: ramp([80, 100, 130], 14), dust: ramp([180, 160, 120], 15),
  mint: ramp([140, 210, 170], 16), lavender: ramp([170, 140, 210], 17), burnt: ramp([48, 32, 28], 18),
  weathered: ramp([110, 110, 100], 19), glazed: ramp([230, 220, 200], 20),
  oak: ramp([140, 100, 56], 21), spruce: ramp([90, 68, 44], 22), birch: ramp([210, 190, 140], 23),
  dark_oak: ramp([64, 44, 28], 24), acacia: ramp([170, 96, 52], 25), cherry: ramp([180, 100, 88], 26),
  mangrove: ramp([110, 52, 44], 27), walnut: ramp([96, 64, 40], 28), teak: ramp([150, 110, 60], 29),
  cedar: ramp([130, 80, 48], 30), pine: ramp([120, 96, 56], 31), mahogany: ramp([120, 52, 36], 32),
  bamboo: ramp([180, 170, 80], 33), ebony: ramp([40, 32, 28], 34), maple: ramp([170, 110, 56], 35),
  steel: ramp([150, 156, 168], 36), bronze: ramp([170, 120, 60], 37), tin: ramp([180, 186, 192], 38),
  lead: ramp([100, 104, 110], 39), silver: ramp([200, 206, 214], 40), platinum: ramp([220, 224, 230], 41),
  nickel: ramp([170, 176, 182], 42), brass: ramp([200, 170, 60], 43), pewter: ramp([130, 134, 140], 44),
  rusty: ramp([130, 80, 48], 45), oxidized: ramp([80, 140, 120], 46), titanium: ramp([190, 194, 200], 47),
  chrome: ramp([210, 214, 220], 48), gold: ramp([226, 176, 40], 49),
  ruby: ramp([180, 40, 60], 50), sapphire: ramp([40, 80, 180], 51), emerald: ramp([40, 160, 90], 52),
  amethyst: ramp([140, 80, 180], 53), topaz: ramp([220, 180, 60], 54), opal: ramp([200, 210, 230], 55),
  onyx: ramp([32, 32, 40], 56), aqua: ramp([80, 180, 200], 57), garnet: ramp([150, 40, 60], 58),
  citrine: ramp([220, 170, 50], 59), peridot: ramp([120, 190, 80], 60), tourmaline: ramp([180, 80, 120], 61),
  frosted: ramp([220, 224, 230], 62), cobalt: ramp([44, 98, 208], 63), prism: ramp([180, 140, 220], 64),
  charcoal: ramp([40, 40, 48], 65), forest: ramp([48, 100, 56], 66), ocean: ramp([40, 90, 150], 67),
  amber: ramp([210, 140, 40], 68), violet: ramp([120, 60, 160], 69), zinc: ramp([160, 168, 176], 70),
  iron: ramp([120, 126, 136], 71), terracotta: ramp([170, 100, 58], 72), cedar_roof: ramp([130, 80, 48], 73),
  thatch: ramp([196, 160, 84], 74), clay: ramp([160, 80, 52], 75), snow: ramp([230, 236, 244], 76),
  moss_roof: ramp([80, 120, 64], 77), obsidian: ramp([24, 20, 36], 78), golden: ramp([220, 180, 60], 79),
  stone: ramp([122, 122, 132], 80), sand: ramp([214, 192, 136], 81), slate: ramp([70, 76, 92], 82),
  marble: ramp([224, 224, 228], 83), crimson: ramp([176, 42, 48], 84), azure: ramp([44, 98, 208], 85),
  banner_em: ramp([44, 168, 96], 86), banner_gold: ramp([226, 176, 40], 87), banner_vio: ramp([140, 76, 196], 88),
  banner_snow: ramp([230, 236, 244], 89), banner_onyx: ramp([32, 32, 40], 90), banner_copper: ramp([200, 114, 72], 91),
  banner_silver: ramp([200, 206, 214], 92), banner_royal: ramp([80, 40, 140], 93),
  limestone: ramp([210, 200, 170], 94), sandstone: ramp([214, 192, 136], 95), ornate_m: ramp([240, 240, 244], 96),
  ornate_obs: ramp([48, 44, 60], 97), concrete_w: ramp([240, 240, 244], 98), concrete_g: ramp([150, 150, 158], 99),
  concrete_c: ramp([56, 56, 64], 100), panel_b: ramp([44, 98, 208], 101), panel_r: ramp([176, 42, 48], 102),
  panel_g: ramp([44, 168, 96], 103), panel_y: ramp([226, 176, 40], 104), glass_w: ramp([200, 220, 240], 105),
  metal_clad: ramp([130, 136, 146], 106), neon: ramp([40, 200, 220], 107),
  brazier: ramp([80, 40, 28], 108), sconce: ramp([200, 170, 60], 109), crystal: ramp([180, 220, 255], 110),
  oil: ramp([150, 100, 48], 111),
};

const block = {};

block.bricks = (cv, pal, opts = {}) => {
  const r = rng(opts.seed || 7);
  fillRect(cv, 0, 0, 15, 15, pal[0]);
  for (let row = 0; row < 4; row++) {
    const y0 = row * 4, offset = (row % 2) * 4;
    for (let bx = -1; bx < 4; bx++) {
      const x0 = bx * 8 + offset;
      for (let y = y0; y < y0 + 3; y++) for (let x = x0; x < x0 + 7; x++) {
        const xx = ((x % 16) + 16) % 16;
        let c = pal[2]; if (y === y0) c = pal[3]; if (y === y0 + 2) c = pal[1];
        px(cv, xx, y, c); if (r() > 0.8) shade(cv, xx, y, pal, r);
      }
    }
  }
  if (opts.moss) for (let i = 0; i < 46; i++) { if (r() > 0.4) pxw(cv, Math.floor(r() * 16), Math.floor(r() * 16), pick(MOSS, r)); }
  if (opts.char) for (let i = 0; i < 20; i++) { if (r() > 0.5) pxw(cv, Math.floor(r() * 16), Math.floor(r() * 16), mix(pal[0], [20, 20, 20], 0.5)); }
};

block.glazed = (cv, pal) => {
  block.tile(cv, pal);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) if ((x + y) % 4 === 0) px(cv, x, y, pal[4]);
};

block.tile = (cv, pal) => {
  const r = rng(9);
  fillRect(cv, 0, 0, 15, 15, pal[1]);
  for (let cy = 0; cy < 2; cy++) for (let cx = 0; cx < 2; cx++) {
    const x0 = cx * 8, y0 = cy * 8;
    for (let y = y0; y < y0 + 7; y++) for (let x = x0; x < x0 + 7; x++) {
      let c = pal[2]; if (x === x0 || y === y0) c = pal[3]; if (x === x0 + 6 || y === y0 + 6) c = pal[1];
      px(cv, x, y, c); if (r() > 0.9) shade(cv, x, y, pal, r);
    }
    px(cv, x0 + 1, y0 + 1, pal[4]);
  }
};

block.trim = (cv, pal, opts = {}) => {
  const r = rng(opts.seed || 33);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const plank = Math.floor(y / 4);
    let c = plank % 2 === 0 ? pal[2] : pal[3];
    if (y % 4 === 0) c = pal[1];
    if (y % 4 === 3) c = pal[0];
    px(cv, x, y, c);
    if (r() > 0.88) shade(cv, x, y, pal, r);
  }
  for (let y = 0; y < 16; y += 4) for (let x = 0; x < 16; x++) px(cv, x, y, pal[1]);
};

block.metalplate = (cv, pal, opts = {}) => {
  const r = rng(opts.seed || 37);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    let c = pal[2];
    if ((x + y) % 6 === 0) c = pal[3];
    if ((x - y + 16) % 8 === 0) c = pal[1];
    px(cv, x, y, c);
  }
  if (opts.rust) for (let i = 0; i < 24; i++) pxw(cv, Math.floor(r() * 16), Math.floor(r() * 16), pick(RUST, r));
  if (opts.patina) for (let i = 0; i < 24; i++) pxw(cv, Math.floor(r() * 16), Math.floor(r() * 16), pick(PATINA, r));
};

block.weave = (cv, pal) => {
  const r = rng(5);
  fillRect(cv, 0, 0, 15, 15, pal[0]);
  for (let by = 0; by < 16; by += 8) for (let bx = 0; bx < 16; bx += 8) {
    const horiz = (((bx + by) / 8) % 2) === 0;
    for (let plank = 0; plank < 8; plank += 3) for (let d = 0; d < 2; d++) {
      const along = plank + d;
      for (let t = 0; t < 8; t++) {
        let x, y; if (horiz) { x = bx + t; y = by + along; } else { x = bx + along; y = by + t; }
        let c = d === 0 ? pal[3] : pal[2]; if (t === 7) c = pal[1];
        px(cv, x, y, c); if (r() > 0.88) shade(cv, x, y, pal, r);
      }
    }
  }
};

block.gemglass = (cv, pal, opts = {}) => {
  const r = rng(opts.seed || 55);
  fillRect(cv, 0, 0, 15, 15, mix(pal[3], WHITE, 0.35));
  for (let y = 0; y < 16; y += 4) for (let x = 0; x < 16; x += 4) {
    fillRect(cv, x, y, x + 3, y + 3, pal[2]);
    px(cv, x, y, pal[4]); px(cv, x + 3, y + 3, pal[1]);
  }
  if (opts.glow) {
    const g = opts.glow;
    for (let y = 4; y < 12; y++) for (let x = 4; x < 12; x++) {
      const d = Math.abs(x - 7.5) + Math.abs(y - 7.5);
      if (d < 4) px(cv, x, y, mix(pxColor(cv, x, y) || pal[2], g[3], 0.55));
    }
  }
};
function pxColor(cv, x, y) { return cv[y] && cv[y][x] ? cv[y][x].slice(0, 3) : null; }

block.frosted = (cv, pal) => {
  const r = rng(62);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    let c = mix(pal[3], WHITE, 0.4 + r() * 0.2);
    px(cv, x, y, c);
  }
  for (let i = 0; i < 12; i++) { const x = Math.floor(r() * 16), y = Math.floor(r() * 16); disc(cv, x, y, 1, mix(pal[4], WHITE, 0.5)); }
};

block.prism = (cv, pal) => {
  block.gemglass(cv, pal, { seed: 64, glow: GLOW_PRISM });
  for (let a = 0; a < 360; a += 45) {
    const x = 8 + Math.round(4 * Math.cos(a * Math.PI / 180)), y = 8 + Math.round(4 * Math.sin(a * Math.PI / 180));
    px(cv, x, y, GLOW_PRISM[4]);
  }
};

block.shingles = (cv, pal) => {
  const r = rng(11);
  fillRect(cv, 0, 0, 15, 15, pal[1]);
  for (let row = 0; row < 4; row++) {
    const y0 = row * 4, off = (row % 2) * 4;
    for (let sx = -1; sx < 4; sx++) {
      const x0 = sx * 8 + off;
      for (let y = 0; y < 4; y++) for (let x = 0; x < 7; x++) {
        const xx = ((x0 + x) % 16 + 16) % 16, yy = y0 + y;
        let c = pal[2]; if (y === 0) c = pal[3]; if (y === 3) c = pal[0]; if (x === 0 || x === 6) c = pal[1];
        px(cv, xx, yy, c); if (r() > 0.86) shade(cv, xx, yy, pal, r);
      }
    }
  }
};

block.thatch = (cv, pal) => {
  const r = rng(23);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const n = r(); let c = pal[2]; if (n > 0.7) c = pal[3]; else if (n < 0.25) c = pal[1];
    px(cv, x, y, c);
  }
  for (let y = 3; y < 16; y += 5) for (let x = 0; x < 16; x++) px(cv, x, y, pal[0]);
};

block.metalroof = (cv, pal, opts = {}) => {
  const r = rng(opts.seed || 29);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    let c = pal[2]; if ((x % 4) === 0) c = pal[3]; if ((x % 4) === 3) c = pal[1];
    px(cv, x, y, c);
  }
  for (let x = 3; x < 16; x += 4) for (let y = 0; y < 16; y++) px(cv, x, y, pal[0]);
  for (let x = 0; x < 16; x++) px(cv, x, 8, mix(pal[1], pal[0], 0.5));
  if (opts.moss) for (let i = 0; i < 30; i++) pxw(cv, Math.floor(r() * 16), Math.floor(r() * 16), pick(MOSS, r));
};

block.shakes = (cv, pal) => {
  const r = rng(73);
  for (let row = 0; row < 8; row++) {
    const y0 = row * 2, off = (row % 2) * 4;
    for (let sx = -1; sx < 3; sx++) {
      const x0 = sx * 8 + off;
      for (let y = 0; y < 2; y++) for (let x = 0; x < 7; x++) {
        const xx = ((x0 + x) % 16 + 16) % 16;
        let c = pal[2]; if (y === 0) c = pal[3];
        px(cv, xx, y0 + y, c); if (r() > 0.85) shade(cv, xx, y0 + y, pal, r);
      }
    }
  }
};

block.path = (cv, pal, opts = {}) => {
  const r = rng(opts.seed || 80);
  fillRect(cv, 0, 0, 15, 15, pal[1]);
  if (opts.style === 'cobble') {
    for (let cy = 0; cy < 4; cy++) for (let cx = 0; cx < 4; cx++) {
      const x0 = cx * 4 + (cy % 2) * 2, y0 = cy * 4;
      for (let y = y0; y < y0 + 3; y++) for (let x = x0; x < x0 + 3; x++) {
        pxw(cv, x, y, pal[2 + (Math.floor(r() * 2))]);
      }
    }
  } else if (opts.style === 'gravel') {
    for (let i = 0; i < 120; i++) pxw(cv, Math.floor(r() * 16), Math.floor(r() * 16), pick([pal[1], pal[2], pal[3]], r));
  } else if (opts.style === 'hex') {
    for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
      const hx = (x + (Math.floor(y / 4) % 2) * 2) % 4, hy = y % 4;
      let c = pal[2]; if (hx === 0 || hy === 0) c = pal[1];
      px(cv, x, y, c);
    }
  } else if (opts.style === 'marble') {
    block.marble(cv, pal, { seed: 83, vein: pal[1] });
    return;
  } else if (opts.style === 'cracked') {
    for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) px(cv, x, y, pal[2]);
    line(cv, 2, 0, 5, 16, pal[0]); line(cv, 10, 0, 8, 16, pal[0]); line(cv, 0, 8, 16, 6, pal[0]);
  } else {
    for (let y = 0; y < 16; y += 4) for (let x = 0; x < 16; x += 4) {
      fillRect(cv, x + 1, y + 1, x + 2, y + 2, pal[3]);
      px(cv, x, y, pal[1]);
    }
  }
  if (opts.moss) for (let i = 0; i < 20; i++) pxw(cv, Math.floor(r() * 16), Math.floor(r() * 16), pick(MOSS, r));
};

block.marble = (cv, pal, opts = {}) => {
  const r = rng(opts.seed || 3);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const n = r(); let c = pal[3]; if (n > 0.92) c = pal[4]; else if (n < 0.12) c = pal[2];
    px(cv, x, y, c);
  }
  const vein = opts.vein || pal[1];
  let x = 0, y = 4;
  for (let i = 0; i < 20; i++) { pxw(cv, x, y, vein); if (r() > 0.5) x++; else y++; }
};

block.banner = (cv, pal, opts = {}) => {
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    let c = pal[2]; if (x < 2) c = pal[3]; if (x > 13) c = pal[1];
    px(cv, x, y, c);
  }
  for (const fx of [4, 8, 12]) for (let y = 0; y < 16; y++) px(cv, fx, y, pal[1]);
  const g = opts.emblem || P.gold;
  line(cv, 8, 3, 8, 11, g[3]); line(cv, 5, 7, 11, 7, g[3]);
  px(cv, 8, 7, g[4]);
};

block.ornate = (cv, pal, opts = {}) => {
  const ac = opts.accent || pal[4];
  fillRect(cv, 0, 0, 15, 15, pal[2]);
  fillRect(cv, 0, 0, 15, 0, pal[3]); fillRect(cv, 0, 15, 15, 15, pal[0]);
  fillRect(cv, 0, 0, 0, 15, pal[3]); fillRect(cv, 15, 0, 15, 15, pal[1]);
  fillRect(cv, 2, 2, 13, 13, pal[1]); fillRect(cv, 3, 3, 12, 12, pal[2]);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const d = Math.abs(x - 7.5) + Math.abs(y - 7.5);
    if (d > 2.0 && d < 3.6) px(cv, x, y, ac);
  }
  px(cv, 7, 7, ac); px(cv, 8, 8, ac);
};

block.carved = (cv, pal) => {
  fillRect(cv, 0, 0, 15, 15, pal[2]);
  fillRect(cv, 0, 0, 15, 1, pal[3]); fillRect(cv, 0, 14, 15, 15, pal[1]);
  for (let bx = 0; bx < 16; bx += 8) {
    line(cv, bx + 1, 4, bx + 6, 4, pal[1]); line(cv, bx + 6, 4, bx + 6, 10, pal[1]);
    line(cv, bx + 6, 10, bx + 2, 10, pal[1]); line(cv, bx + 2, 10, bx + 2, 7, pal[1]);
  }
};

block.lantern = (cv, pal, opts = {}) => {
  const glow = opts.glow || GLOW_WARM;
  fillRect(cv, 0, 0, 15, 15, pal[1]); fillRect(cv, 2, 2, 13, 13, pal[2]);
  for (let y = 2; y <= 13; y++) for (let x = 2; x <= 13; x++) {
    const d = Math.abs(x - 7.5) + Math.abs(y - 7.5);
    let c = glow[2]; if (d < 3) c = glow[4]; else if (d < 5) c = glow[3];
    px(cv, x, y, c);
  }
  for (const gx of [2, 7, 8, 13]) for (let y = 2; y <= 13; y++) px(cv, gx, y, pal[1]);
};

block.brazier = (cv, pal) => {
  fillRect(cv, 0, 0, 15, 15, pal[1]);
  fillRect(cv, 3, 8, 12, 14, pal[0]);
  for (let y = 4; y < 10; y++) for (let x = 4; x < 12; x++) {
    const d = Math.abs(x - 7.5) + Math.abs(y - 6.5);
    px(cv, x, y, d < 3 ? GLOW_WARM[4] : mix(pal[2], GLOW_WARM[3], 0.4));
  }
  fillRect(cv, 0, 0, 15, 2, pal[2]); fillRect(cv, 0, 14, 15, 15, pal[0]);
};

block.sconce = (cv, pal) => {
  fillRect(cv, 0, 0, 15, 15, mix(pal[1], [40, 40, 48], 0.3));
  fillRect(cv, 6, 4, 9, 12, pal[2]);
  disc(cv, 8, 7, 2, GLOW_WARM[4]);
  for (let y = 5; y < 10; y++) px(cv, 6, y, pal[0]);
};

block.crystal = (cv, pal) => {
  fillRect(cv, 0, 0, 15, 15, pal[1]);
  for (let y = 3; y < 13; y++) for (let x = 3; x < 13; x++) {
    const d = Math.abs(x - 7.5) + Math.abs(y - 7.5);
    px(cv, x, y, d < 4 ? mix(pal[3], GLOW_W[4], 0.6) : pal[2]);
  }
  line(cv, 8, 2, 8, 14, pal[3]); line(cv, 4, 8, 12, 8, pal[3]);
};

block.neon = (cv, pal) => {
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) px(cv, x, y, mix(pal[1], [30, 30, 36], 0.5));
  fillRect(cv, 0, 6, 15, 9, pal[3]);
  for (let x = 0; x < 16; x++) { px(cv, x, 6, GLOW_CYAN[4]); px(cv, x, 9, pal[0]); }
  for (let y = 7; y < 9; y++) for (let x = 0; x < 16; x++) px(cv, x, y, GLOW_CYAN[3]);
};

block.modern = (cv, pal) => {
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    let c = pal[2]; if ((x + y) % 8 === 0) c = pal[3];
    px(cv, x, y, c);
  }
  fillRect(cv, 0, 7, 15, 8, pal[1]); fillRect(cv, 7, 0, 8, 15, pal[1]);
};

block.glasswall = (cv, pal) => {
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) px(cv, x, y, mix(pal[2], WHITE, 0.45));
  for (let x = 0; x < 16; x += 4) for (let y = 0; y < 16; y++) px(cv, x, y, pal[1]);
  for (let y = 0; y < 16; y += 4) for (let x = 0; x < 16; x++) px(cv, x, y, pal[1]);
};

function B(fn, pal, opts) { return Object.assign({ fn, pal }, opts || {}); }

const BLOCK_SPECS = {
  dec2_rust_brick: B('bricks', P.rust), dec2_sage_brick: B('bricks', P.sage, { seed: 2 }),
  dec2_navy_brick: B('bricks', P.navy, { seed: 3 }), dec2_wine_brick: B('bricks', P.wine, { seed: 4 }),
  dec2_pearl_brick: B('bricks', P.pearl, { seed: 5 }), dec2_ash_brick: B('bricks', P.ash, { seed: 6 }),
  dec2_sunset_brick: B('bricks', P.sunset, { seed: 7 }), dec2_frost_brick: B('bricks', P.frost, { seed: 8 }),
  dec2_olive_brick: B('bricks', P.olive, { seed: 9 }), dec2_plum_brick: B('bricks', P.plum, { seed: 10 }),
  dec2_coral_brick: B('bricks', P.coral, { seed: 11 }), dec2_midnight_brick: B('bricks', P.midnight, { seed: 12 }),
  dec2_honey_brick: B('bricks', P.honey, { seed: 13 }), dec2_storm_brick: B('bricks', P.storm, { seed: 14 }),
  dec2_dust_brick: B('bricks', P.dust, { seed: 15 }), dec2_mint_brick: B('bricks', P.mint, { seed: 16 }),
  dec2_lavender_brick: B('bricks', P.lavender, { seed: 17 }), dec2_burnt_brick: B('bricks', P.burnt, { seed: 18, char: true }),
  dec2_weathered_brick: B('bricks', P.weathered, { seed: 19, moss: true }), dec2_glazed_brick: B('glazed', P.glazed),

  dec2_oak_trim: B('trim', P.oak), dec2_spruce_trim: B('trim', P.spruce, { seed: 22 }),
  dec2_birch_trim: B('trim', P.birch, { seed: 23 }), dec2_dark_oak_trim: B('trim', P.dark_oak, { seed: 24 }),
  dec2_acacia_trim: B('trim', P.acacia, { seed: 25 }), dec2_cherry_trim: B('trim', P.cherry, { seed: 26 }),
  dec2_mangrove_trim: B('trim', P.mangrove, { seed: 27 }), dec2_walnut_trim: B('trim', P.walnut, { seed: 28 }),
  dec2_teak_trim: B('trim', P.teak, { seed: 29 }), dec2_cedar_trim: B('trim', P.cedar, { seed: 30 }),
  dec2_pine_trim: B('trim', P.pine, { seed: 31 }), dec2_mahogany_trim: B('trim', P.mahogany, { seed: 32 }),
  dec2_bamboo_trim: B('trim', P.bamboo, { seed: 33 }), dec2_ebony_trim: B('trim', P.ebony, { seed: 34 }),
  dec2_maple_trim: B('trim', P.maple, { seed: 35 }),

  dec2_steel_plate: B('metalplate', P.steel), dec2_bronze_plate: B('metalplate', P.bronze, { seed: 37 }),
  dec2_tin_plate: B('metalplate', P.tin, { seed: 38 }), dec2_lead_plate: B('metalplate', P.lead, { seed: 39 }),
  dec2_silver_plate: B('metalplate', P.silver, { seed: 40 }), dec2_platinum_plate: B('metalplate', P.platinum, { seed: 41 }),
  dec2_nickel_plate: B('metalplate', P.nickel, { seed: 42 }), dec2_brass_plate: B('metalplate', P.brass, { seed: 43 }),
  dec2_pewter_plate: B('metalplate', P.pewter, { seed: 44 }), dec2_rusty_iron: B('metalplate', P.rusty, { rust: true }),
  dec2_oxidized_copper: B('metalplate', P.oxidized, { patina: true }), dec2_titanium_plate: B('metalplate', P.titanium, { seed: 47 }),
  dec2_chrome_plate: B('metalplate', P.chrome, { seed: 48 }), dec2_iron_weave: B('weave', P.steel),
  dec2_gold_plate: B('metalplate', P.gold, { seed: 49 }),

  dec2_ruby_glass: B('gemglass', P.ruby, { glow: GLOW_WARM }), dec2_sapphire_glass: B('gemglass', P.sapphire, { seed: 51 }),
  dec2_emerald_glass: B('gemglass', P.emerald, { glow: GLOW_WARM }), dec2_amethyst_glass: B('gemglass', P.amethyst, { seed: 53 }),
  dec2_topaz_glass: B('gemglass', P.topaz, { seed: 54 }), dec2_opal_glass: B('gemglass', P.opal, { glow: GLOW_W }),
  dec2_onyx_glass: B('gemglass', P.onyx, { seed: 56 }), dec2_aquamarine_glass: B('gemglass', P.aqua, { seed: 57 }),
  dec2_garnet_glass: B('gemglass', P.garnet, { seed: 58 }), dec2_citrine_glass: B('gemglass', P.citrine, { glow: GLOW_WARM }),
  dec2_peridot_glass: B('gemglass', P.peridot, { seed: 60 }), dec2_tourmaline_glass: B('gemglass', P.tourmaline, { seed: 61 }),
  dec2_frosted_glass: B('frosted', P.frosted), dec2_cobalt_glass: B('gemglass', P.cobalt, { seed: 63 }),
  dec2_prism_glass: B('prism', P.prism),

  dec2_charcoal_shingles: B('shingles', P.charcoal), dec2_forest_shingles: B('shingles', P.forest, { seed: 66 }),
  dec2_ocean_shingles: B('shingles', P.ocean, { seed: 67 }), dec2_amber_shingles: B('shingles', P.amber, { seed: 68 }),
  dec2_violet_shingles: B('shingles', P.violet, { seed: 69 }), dec2_zinc_roof: B('metalroof', P.zinc, { seed: 70 }),
  dec2_iron_roof: B('metalroof', P.iron, { seed: 71 }), dec2_terracotta_roof: B('shingles', P.terracotta, { seed: 72 }),
  dec2_cedar_shakes: B('shakes', P.cedar_roof), dec2_palm_thatch: B('thatch', P.thatch),
  dec2_clay_roof: B('shingles', P.clay, { seed: 75 }), dec2_snow_roof: B('metalroof', P.snow, { seed: 76 }),
  dec2_moss_roof: B('metalroof', P.moss_roof, { moss: true }), dec2_obsidian_roof: B('shingles', P.obsidian, { seed: 78 }),
  dec2_golden_roof: B('metalroof', P.golden, { seed: 79 }),

  dec2_cobble_path: B('path', P.stone, { style: 'cobble' }), dec2_brick_path: B('path', P.rust, { style: 'brick', seed: 81 }),
  dec2_gravel_path: B('path', P.dust, { style: 'gravel' }), dec2_slate_path: B('path', P.slate, { seed: 82 }),
  dec2_sandstone_path: B('path', P.sand, { seed: 81 }), dec2_mossy_path: B('path', P.stone, { moss: true, seed: 83 }),
  dec2_cracked_path: B('path', P.stone, { style: 'cracked' }), dec2_hex_path: B('path', P.concrete_g, { style: 'hex' }),
  dec2_flagstone_path: B('path', P.limestone, { seed: 84 }), dec2_marble_path: B('path', P.marble, { style: 'marble' }),

  dec2_crimson_banner: B('banner', P.crimson, { emblem: P.gold }), dec2_azure_banner: B('banner', P.azure, { emblem: P.gold }),
  dec2_emerald_banner: B('banner', P.banner_em, { emblem: P.gold }), dec2_gold_banner: B('banner', P.banner_gold, { emblem: P.pearl }),
  dec2_violet_banner: B('banner', P.banner_vio, { emblem: P.gold }), dec2_snow_banner: B('banner', P.banner_snow, { emblem: P.steel }),
  dec2_onyx_banner: B('banner', P.banner_onyx, { emblem: P.gold }), dec2_copper_banner: B('banner', P.banner_copper, { emblem: P.gold }),
  dec2_silver_banner: B('banner', P.banner_silver, { emblem: P.crimson }), dec2_royal_banner: B('banner', P.banner_royal, { emblem: P.gold }),

  dec2_ornate_limestone: B('ornate', P.limestone, { accent: P.gold[3] }), dec2_ornate_sandstone: B('ornate', P.sandstone, { accent: P.amber[3] }),
  dec2_ornate_marble: B('ornate', P.ornate_m, { accent: P.gold[3] }), dec2_ornate_obsidian: B('ornate', P.ornate_obs, { accent: P.violet[3] }),
  dec2_carved_limestone: B('carved', P.limestone), dec2_carved_sandstone: B('carved', P.sandstone),
  dec2_brazier: B('brazier', P.brazier),
  dec2_wall_sconce: B('sconce', P.sconce), dec2_crystal_lamp: B('crystal', P.crystal), dec2_oil_lantern: B('lantern', P.oil, { glow: GLOW_WARM }),

  dec2_concrete_white: B('modern', P.concrete_w), dec2_concrete_grey: B('modern', P.concrete_g),
  dec2_concrete_charcoal: B('modern', P.concrete_c), dec2_panel_blue: B('modern', P.panel_b),
  dec2_panel_red: B('modern', P.panel_r), dec2_panel_green: B('modern', P.panel_g),
  dec2_panel_yellow: B('modern', P.panel_y), dec2_glass_wall: B('glasswall', P.glass_w),
  dec2_metal_cladding: B('metalroof', P.metal_clad, { seed: 106 }), dec2_neon_panel: B('neon', P.neon),
};

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
    if (!fn) { console.warn('no block fn for', spec.fn, id); continue; }
    fn(cv, spec.pal, spec);
    await writePng(cv, BLOCK_TEX, id);
    writeJSON(path.join(BLOCK_MODELS, `${id}.json`), blockModel(id));
    writeJSON(path.join(BLOCKSTATES, `${id}.json`), blockState(id));
    writeJSON(path.join(ITEM_DEFS, `${id}.json`), blockItemDef(id));
  }
  console.log('expansion2 blocks:', Object.keys(BLOCK_SPECS).length);
}

async function buildPreview() {
  fs.mkdirSync(PREVIEW_DIR, { recursive: true });
  const ids = Object.keys(BLOCK_SPECS);
  const scale = 6, cols = 12, pad = 4, cell = N * scale + pad;
  const rows = Math.ceil(ids.length / cols);
  const sheet = new Jimp({ width: cols * cell + pad, height: rows * cell + pad, color: 0x2b2b33ff });
  for (let i = 0; i < ids.length; i++) {
    try {
      const img = await Jimp.read(path.join(BLOCK_TEX, `${ids[i]}.png`));
      img.resize({ w: N * scale, h: N * scale, mode: 'nearestNeighbor' });
      sheet.composite(img, pad + (i % cols) * cell, pad + Math.floor(i / cols) * cell);
    } catch (e) { /* skip */ }
  }
  await sheet.write(path.join(PREVIEW_DIR, 'blocks2_montage.png'));
  console.log('preview ->', path.join(PREVIEW_DIR, 'blocks2_montage.png'));
}

async function main() {
  for (const d of [BLOCK_TEX, BLOCK_MODELS, ITEM_DEFS, BLOCKSTATES]) fs.mkdirSync(d, { recursive: true });
  await generateBlocks();
  await buildPreview();
  console.log('done.');
}

main().catch(e => { console.error(e); process.exit(1); });
