/**
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
// Canvas helpers
// ----------------------------------------------------------------------------
function canvas() { return Array.from({ length: N }, () => Array.from({ length: N }, () => null)); }
function inb(x, y) { return x >= 0 && x < N && y >= 0 && y < N; }
function px(cv, x, y, c) { x = Math.round(x); y = Math.round(y); if (inb(x, y) && c) cv[y][x] = c; }
function get(cv, x, y) { return inb(x, y) ? cv[y][x] : null; }

function fillRect(cv, x0, y0, x1, y1, c) {
  for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) px(cv, x, y, c);
}
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
function darken(c, f) { return [Math.round(c[0] * f), Math.round(c[1] * f), Math.round(c[2] * f), c[3] == null ? 255 : c[3]]; }
function mix(a, b, t) { return [Math.round(a[0] + (b[0] - a[0]) * t), Math.round(a[1] + (b[1] - a[1]) * t), Math.round(a[2] + (b[2] - a[2]) * t), 255]; }
function clampColor(c) {
  return [
    Math.max(0, Math.min(255, Math.round(c[0]))),
    Math.max(0, Math.min(255, Math.round(c[1]))),
    Math.max(0, Math.min(255, Math.round(c[2]))),
    c[3] == null ? 255 : c[3],
  ];
}
function lighten(c, f) { return clampColor([c[0] * f, c[1] * f, c[2] * f, c[3]]); }
// Build a 5-stop [shadow,dark,mid,light,specular] ramp from a single base colour.
function ramp5(base) { return [darken(base, 0.45), darken(base, 0.7), [base[0], base[1], base[2], 255], lighten(base, 1.2), lighten(base, 1.5)]; }

function ring(cv, cx, cy, r, c) {
  for (let a = 0; a < 360; a += 8) px(cv, cx + Math.round(r * Math.cos(a * Math.PI / 180)), cy + Math.round(r * Math.sin(a * Math.PI / 180)), c);
}

// Consistent upper-left key light.  Brightens silhouette edges facing the light
// and darkens edges facing away, leaving interior detail (and thin diagonal
// blades, whose edges face both ways) untouched.  Gives every solid shape a
// crisp 2-tone rim without flattening the drawn material shading.
function rimLight(cv, opts = {}) {
  const up = opts.hi || 1.32, down = opts.lo || 0.72;
  const snap = cv.map(r => r.map(c => (c ? c.slice() : null)));
  const filled = (x, y) => inb(x, y) && snap[y][x];
  for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) {
    const c = snap[y][x]; if (!c) continue;
    const lightOpen = !filled(x - 1, y) || !filled(x, y - 1);
    const shadowOpen = !filled(x + 1, y) || !filled(x, y + 1);
    if (lightOpen && !shadowOpen) cv[y][x] = clampColor([c[0] * up, c[1] * up, c[2] * up, c[3]]);
    else if (shadowOpen && !lightOpen) cv[y][x] = clampColor([c[0] * down, c[1] * down, c[2] * down, c[3]]);
  }
}

// Small faceted gem inlay with a single sparkle pixel, lit from the upper-left.
function gemInlay(cv, cx, cy, gem, r = 1) {
  for (let y = -r; y <= r; y++) for (let x = -r; x <= r; x++) {
    if (x * x + y * y > r * r + r * 0.4) continue;
    let c = gem[2];
    if (x < 0 || y < 0) c = gem[3];
    if (x > 0 || y > 0) c = gem[1];
    px(cv, cx + x, cy + y, c);
  }
  px(cv, cx, cy, gem[3]);
  px(cv, cx - (r > 0 ? 1 : 0), cy - (r > 0 ? 1 : 0), gem[4]);
}

// add a 1px dark outline around the silhouette (4-neighbour)
function outline(cv, col) {
  const add = [];
  for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) {
    if (cv[y][x]) continue;
    if (get(cv, x, y - 1) || get(cv, x, y + 1) || get(cv, x - 1, y) || get(cv, x + 1, y)) add.push([x, y]);
  }
  for (const [x, y] of add) cv[y][x] = col;
}

// deterministic small PRNG for block speckle
function rng(seed) { let s = seed >>> 0; return () => { s = (s * 1664525 + 1013904223) >>> 0; return s / 4294967296; }; }

// ----------------------------------------------------------------------------
// Palettes — 5 stops: [shadow, dark, mid, light, specular]
// ----------------------------------------------------------------------------
const P = {
  iron:      [[38, 40, 48], [92, 98, 110], [150, 156, 168], [200, 206, 216], [240, 244, 250]],
  steel:     [[28, 40, 58], [70, 96, 128], [120, 156, 196], [176, 206, 232], [228, 244, 255]],
  diamond:   [[28, 86, 92], [44, 150, 150], [92, 206, 200], [160, 236, 230], [224, 255, 252]],
  gold:      [[96, 60, 12], [168, 118, 24], [226, 176, 40], [246, 214, 96], [255, 248, 188]],
  netherite: [[18, 16, 20], [42, 38, 46], [78, 70, 82], [116, 106, 120], [156, 146, 160]],
  darkiron:  [[22, 24, 30], [52, 56, 66], [92, 98, 112], [140, 146, 160], [190, 196, 208]],
  bone:      [[92, 84, 64], [150, 140, 110], [200, 190, 158], [230, 224, 200], [252, 250, 238]],
  wood:      [[40, 26, 14], [74, 48, 26], [108, 74, 42], [140, 100, 60], [170, 130, 86]],
  leather:   [[48, 30, 18], [86, 54, 30], [120, 80, 46], [156, 110, 72], [190, 146, 104]],
  cursed:    [[26, 8, 38], [66, 24, 98], [120, 52, 168], [176, 108, 224], [226, 190, 255]],
  void:      [[14, 6, 26], [44, 20, 70], [88, 44, 126], [140, 86, 186], [198, 158, 232]],
  crimson:   [[48, 8, 14], [112, 22, 30], [176, 42, 48], [222, 88, 82], [252, 168, 150]],
  ember:     [[64, 18, 8], [150, 54, 16], [224, 104, 28], [248, 168, 58], [255, 228, 150]],
  frost:     [[26, 56, 86], [44, 112, 158], [96, 178, 220], [170, 224, 242], [230, 252, 255]],
  poison:    [[18, 48, 12], [42, 104, 26], [92, 168, 44], [150, 214, 76], [214, 250, 150]],
  thunder:   [[78, 62, 8], [170, 142, 22], [230, 202, 46], [250, 232, 116], [255, 250, 200]],
  aqua:      [[14, 56, 72], [26, 112, 144], [60, 178, 198], [140, 222, 234], [214, 252, 255]],
  emerald:   [[10, 58, 34], [22, 124, 70], [44, 184, 108], [120, 224, 160], [200, 250, 222]],
  lapis:     [[14, 28, 82], [26, 56, 150], [44, 98, 208], [96, 150, 238], [180, 212, 255]],
  shadow:    [[8, 8, 12], [26, 26, 36], [54, 54, 70], [92, 92, 116], [140, 140, 168]],
  silver:    [[44, 48, 66], [96, 104, 130], [150, 158, 186], [202, 208, 228], [244, 248, 255]],
  necro:     [[14, 22, 16], [34, 56, 38], [64, 98, 64], [104, 140, 96], [150, 186, 134]],
  arcane:    [[28, 12, 52], [64, 30, 118], [110, 60, 186], [160, 110, 224], [212, 176, 250]],
  soul:      [[12, 28, 38], [24, 76, 96], [56, 150, 168], [128, 214, 224], [208, 250, 255]],
  blood:     [[44, 6, 10], [104, 18, 22], [168, 38, 40], [214, 80, 72], [248, 158, 140]],
  copper:    [[78, 38, 22], [150, 76, 44], [200, 114, 72], [230, 158, 112], [250, 200, 166]],
  paper:     [[120, 104, 72], [178, 160, 120], [214, 200, 162], [238, 230, 204], [252, 248, 234]],
  obsidian:  [[10, 8, 16], [30, 24, 42], [54, 46, 72], [84, 74, 108], [120, 108, 148]],
  ruby:      [[60, 8, 18], [140, 22, 42], [206, 44, 72], [240, 96, 116], [255, 170, 186]],
  amethyst:  [[36, 16, 60], [84, 40, 134], [140, 76, 196], [186, 128, 228], [230, 196, 252]],
  jade:      [[10, 52, 36], [24, 110, 76], [48, 168, 118], [120, 216, 172], [200, 248, 222]],
  stone:     [[58, 58, 64], [90, 90, 98], [122, 122, 132], [156, 156, 166], [188, 188, 198]],
  terracotta:[[78, 42, 24], [128, 70, 40], [170, 100, 58], [200, 132, 84], [226, 168, 120]],
  quartz:    [[150, 142, 132], [196, 190, 180], [224, 220, 212], [240, 238, 232], [252, 252, 248]],
  asphalt:   [[18, 18, 22], [30, 30, 36], [44, 44, 52], [58, 58, 68], [74, 74, 86]],
  cloth_red: [[80, 12, 16], [150, 24, 30], [196, 44, 50], [228, 86, 84], [250, 150, 140]],
  holy:      [[120, 96, 32], [196, 168, 70], [240, 224, 140], [252, 246, 206], [255, 255, 244]],
  wind:      [[70, 104, 110], [120, 168, 176], [176, 218, 222], [212, 244, 246], [244, 255, 255]],
};

const WHITE = [255, 255, 255], BLACK = [0, 0, 0];

// ----------------------------------------------------------------------------
// Generic weapon helpers
// ----------------------------------------------------------------------------
// Anti-diagonal blade from base (bx,by) to tip (tx,ty), half-width w.
// Renders a crisp bright cutting edge on the upper-left, a graded body, an
// optional central fuller groove, a dark spine on the lower-right and a clean
// pointed tip.  No scattered specular flecks (which read as noise at 16px).
function bladeFill(cv, bx, by, tx, ty, w, ramp, opts = {}) {
  const steps = Math.max(Math.abs(tx - bx), Math.abs(ty - by));
  const fuller = opts.fuller && w >= 2;
  for (let i = 0; i <= steps; i++) {
    const t = i / steps;
    const cx = bx + (tx - bx) * t, cy = by + (ty - by) * t;
    let ww = w;
    if (t > 0.9) ww = Math.max(0, w - 2);
    else if (t > 0.74) ww = Math.max(0, w - 1); // taper to a point
    for (let k = -ww; k <= ww; k++) {
      let col;
      if (k === -ww) col = ramp[3];                            // crisp light cutting edge
      else if (k === ww) col = (t < 0.5) ? ramp[0] : ramp[1];  // dark spine (darker near base)
      else if (fuller && k === 0) col = ramp[1];               // central fuller groove
      else col = ramp[2];                                      // body mid
      px(cv, cx + k, cy + k, col);
    }
  }
  // crisp pointed tip + a single specular glint partway down the edge
  px(cv, tx, ty, ramp[4]);
  px(cv, tx - 1, ty + 1, ramp[3]);
  const gx = bx + (tx - bx) * 0.32, gy = by + (ty - by) * 0.32;
  px(cv, gx - w, gy - w, ramp[4]);
}

// Wrapped/bound grip from (gx,gy) toward down-left for `len`, with a banded
// wrap pattern (alternating light/dark binding) and a touch of thickness.
function grip(cv, gx, gy, len, ramp) {
  for (let i = 0; i < len; i++) {
    const x = gx - i, y = gy + i;
    const band = (i % 2 === 0);
    px(cv, x, y, band ? ramp[3] : ramp[2]);          // wrap ridge
    px(cv, x + 1, y + 1, band ? ramp[1] : ramp[0]);  // shadow side thickness
  }
}

// Diagonal crossguard centred at (cx,cy), spanning +/-s along (1,1), beefed up
// with perpendicular thickness and an optional centre gem inlay.
function crossguard(cv, cx, cy, s, ramp, gem) {
  for (let k = -s; k <= s; k++) px(cv, cx + k, cy + k, k < 0 ? ramp[3] : k > 0 ? ramp[1] : ramp[2]);
  px(cv, cx - 1, cy + 1, ramp[1]); px(cv, cx + 1, cy - 1, ramp[3]); // thickness
  if (gem) { px(cv, cx, cy, gem[3]); px(cv, cx, cy, gem[4]); }
}
function pommel(cv, x, y, gem) {
  px(cv, x, y, gem[3]); px(cv, x + 1, y, gem[1]);
  px(cv, x, y + 1, gem[1]); px(cv, x + 1, y + 1, gem[0]);
  px(cv, x, y, gem[4]); // sparkle
}

// ----------------------------------------------------------------------------
// Archetype draw functions.  `s` is the spec: {blade, grip, gem, metal, accent,...}
// ----------------------------------------------------------------------------
const draw = {};

draw.sword = (cv, s) => {
  const b = s.blade, g = s.grip || P.wood, gem = s.gem || b, m = s.metal || P.gold;
  bladeFill(cv, 5, 11, 14, 2, 1, b);   // long, lean blade
  crossguard(cv, 4, 12, 2, m, s.gem ? gem : null);
  grip(cv, 3, 13, 2, g);
  pommel(cv, 1, 14, gem);
};

draw.dagger = (cv, s) => {
  const b = s.blade, g = s.grip || P.leather, gem = s.gem || b, m = s.metal || P.silver;
  bladeFill(cv, 6, 10, 12, 4, 1, b);
  crossguard(cv, 5, 11, 1, m, s.gem ? gem : null);
  grip(cv, 4, 12, 3, g);
  pommel(cv, 2, 14, gem);
};

draw.greatsword = (cv, s) => {
  const b = s.blade, g = s.grip || P.wood, gem = s.gem || b, m = s.metal || P.darkiron;
  bladeFill(cv, 5, 12, 14, 2, 2, b, { fuller: true });
  crossguard(cv, 4, 13, 3, m, s.gem ? gem : null);
  grip(cv, 3, 14, 2, g);
  pommel(cv, 1, 15, gem);
};

draw.cleaver = (cv, s) => {
  const b = s.blade, g = s.grip || P.wood, m = s.metal || P.darkiron;
  // broad chopper blade in the upper area with a chamfered front corner
  for (let y = 2; y <= 9; y++) for (let x = 4; x <= 13; x++) {
    if (y === 2 && x >= 12) continue;   // chamfer top-front corner
    let c = b[2];
    if (x <= 5) c = b[3];               // light back-left
    if (x >= 12) c = b[1];              // shadow front-right
    if (y === 2) c = b[1];              // dark top spine
    if (y >= 8) c = b[4];               // bright bottom cutting edge
    px(cv, x, y, c);
  }
  line(cv, 4, 9, 13, 9, b[4]);          // sharpened edge
  line(cv, 6, 3, 11, 3, b[3]);          // upper sheen band
  px(cv, 7, 5, b[4]);                   // specular glint
  px(cv, 11, 4, b[0]); px(cv, 6, 7, b[0]); // rivets
  // bolster + wrapped handle down-left from the blade heel
  crossguard(cv, 5, 9, 1, m);
  grip(cv, 4, 10, 4, g);
};

draw.katana = (cv, s) => {
  const b = s.blade, g = s.grip || P.crimson, m = s.metal || P.gold;
  // slim single-edge slightly-curved blade, base lower-left to tip upper-right
  for (let i = 0; i <= 10; i++) {
    const x = 4 + i, y = 11 - i + (i > 7 ? 1 : 0);
    px(cv, x - 1, y - 1, b[4]); // bright cutting edge (upper)
    px(cv, x, y, b[2]);         // body
    px(cv, x + 1, y + 1, b[1]); // spine (lower)
  }
  px(cv, 14, 1, b[4]);          // tip
  // circular tsuba guard
  disc(cv, 4, 11, 1, m[2]); px(cv, 3, 10, m[4]); px(cv, 5, 12, m[1]);
  grip(cv, 3, 12, 3, g);
  pommel(cv, 1, 14, g);
};

draw.scythe = (cv, s) => {
  const b = s.blade, g = s.grip || P.wood, m = s.metal || P.gold;
  // snath (pole), faintly diagonal, lower-right up to the head
  for (let y = 4; y <= 15; y++) {
    const x = 10 - Math.floor((15 - y) / 5);
    px(cv, x, y, g[3]); px(cv, x + 1, y, g[1]);
  }
  crossguard(cv, 9, 4, 1, m);   // binding collar
  // sweeping curved blade (C-curve from head toward lower-left)
  const arc = [[9, 3], [7, 2], [5, 2], [3, 3], [2, 5], [2, 7], [3, 8]];
  for (let i = 1; i < arc.length; i++) line(cv, arc[i - 1][0], arc[i - 1][1], arc[i][0], arc[i][1], b[2]);
  for (const [x, y] of arc) { px(cv, x, y + 1, b[4]); px(cv, x - 1, y, b[1]); }
  px(cv, 2, 6, b[4]); px(cv, 5, 2, b[3]);
};

draw.staff = (cv, s) => {
  const g = s.grip || P.wood, gem = s.gem || P.arcane, m = s.metal || P.gold;
  // diagonal shaft with a lit and a shadow side
  for (let i = 0; i <= 9; i++) { px(cv, 4 + i, 14 - i, g[3]); px(cv, 4 + i, 15 - i, g[1]); px(cv, 5 + i, 15 - i, g[0]); }
  // ornate claw holder cradling the orb
  px(cv, 11, 6, m[3]); px(cv, 12, 6, m[2]); px(cv, 11, 4, m[3]); px(cv, 15, 4, m[1]); px(cv, 15, 6, m[1]);
  crossguard(cv, 11, 6, 1, m);
  // glowing orb gem
  disc(cv, 13, 4, 2, gem[2]);
  gemInlay(cv, 13, 4, gem, 1);
  px(cv, 11, 2, gem[3]); // halo glint
  magicGlow(cv, 13, 4, 3, gem[4], 60);
};

draw.wand = (cv, s) => {
  const g = s.grip || P.wood, gem = s.gem || P.arcane, m = s.metal || P.gold;
  for (let i = 0; i <= 7; i++) { px(cv, 5 + i, 13 - i, g[3]); px(cv, 5 + i, 14 - i, g[1]); }
  px(cv, 11, 6, m[2]); px(cv, 12, 6, m[3]); // collar
  disc(cv, 13, 4, 2, gem[2]);
  gemInlay(cv, 13, 4, gem, 1);
  magicGlow(cv, 13, 4, 2, gem[4], 55);
};

draw.halberd = (cv, s) => {
  const b = s.blade, g = s.grip || P.wood, m = s.metal || P.gold;
  // long pole
  for (let i = 0; i <= 12; i++) { px(cv, 3 + i, 14 - i, g[3]); px(cv, 3 + i, 15 - i, g[1]); }
  // top thrusting spike
  bladeFill(cv, 12, 4, 15, 1, 1, b);
  px(cv, 15, 0, b[4]);
  // axe-head crescent on the left of the head
  for (let y = 2; y <= 7; y++) for (let x = 7; x <= 12; x++) {
    if ((x - 7) + Math.abs(y - 4) <= 4) {
      let c = b[2];
      if (y === 2 || x === 7) c = b[3];
      if (y >= 6) c = b[4];   // bright lower cutting edge
      if (x >= 11) c = b[1];
      px(cv, x, y, c);
    }
  }
  px(cv, 8, 3, b[4]);
  crossguard(cv, 12, 5, 1, m); // socket band
};

draw.spear = (cv, s) => {
  const b = s.blade, g = s.grip || P.wood, m = s.metal || P.gold;
  for (let i = 0; i <= 11; i++) { px(cv, 3 + i, 14 - i, g[3]); px(cv, 3 + i, 15 - i, g[1]); }
  // leaf-shaped spearhead at the tip
  bladeFill(cv, 10, 6, 15, 1, 1, b, { fuller: true });
  px(cv, 15, 0, b[4]);
  px(cv, 11, 4, b[3]); px(cv, 12, 6, b[1]); // leaf widening
  crossguard(cv, 11, 5, 1, m); // socket
};

draw.trident = (cv, s) => {
  const m = s.metal || P.aqua;
  // shaft (handheld model rotates it to look diagonal in-hand)
  for (let y = 6; y <= 15; y++) { px(cv, 7, y, m[3]); px(cv, 8, y, m[2]); px(cv, 9, y, m[1]); }
  px(cv, 8, 15, m[0]);
  // crossbar joining the three tines
  fillRect(cv, 4, 5, 11, 6, m[2]);
  line(cv, 4, 5, 11, 5, m[3]); line(cv, 4, 6, 11, 6, m[1]);
  // three barbed tines pointing up; centre tine longest
  for (const tx of [5, 8, 11]) {
    const top = tx === 8 ? 0 : 2;
    for (let y = top; y <= 5; y++) { px(cv, tx - 1, y, m[3]); px(cv, tx, y, m[2]); px(cv, tx + 1, y, m[1]); }
    px(cv, tx, top, m[4]);
    px(cv, tx === 8 ? tx + 2 : tx + 1, top + 1, m[2]); // barb
  }
  px(cv, 5, 7, m[4]);
};

draw.whip = (cv, s) => {
  const b = s.blade, g = s.grip || P.leather, m = s.metal || P.darkiron;
  // wrapped handle lower-left with a pommel knob
  grip(cv, 4, 14, 3, g);
  crossguard(cv, 5, 13, 1, m);
  // tapering coiled lash sweeping up and over
  const pts = [[5, 12], [7, 11], [9, 9], [11, 10], [12, 8], [13, 6], [12, 4], [10, 2], [8, 2]];
  for (let i = 1; i < pts.length; i++) line(cv, pts[i - 1][0], pts[i - 1][1], pts[i][0], pts[i][1], b[1]);
  for (const [x, y] of pts) { px(cv, x, y, b[2]); px(cv, x, y - 1, b[3]); }
  px(cv, 9, 9, b[4]); px(cv, 12, 5, b[4]);
  px(cv, 8, 2, b[4]); // bright cracking tip
};

draw.bow = (cv, s) => {
  const w = s.grip || P.wood, str = s.accent || (s.string) || [232, 232, 238];
  // C-shaped limb on the right with rounded recurve tips
  const arc = [[10, 2], [12, 3], [13, 5], [14, 8], [13, 11], [12, 13], [10, 14]];
  for (let i = 1; i < arc.length; i++) line(cv, arc[i - 1][0], arc[i - 1][1], arc[i][0], arc[i][1], w[2]);
  for (const [x, y] of arc) { px(cv, x - 1, y, w[3]); px(cv, x + 1, y, w[1]); }
  px(cv, 10, 2, w[4]); px(cv, 10, 14, w[1]); // nock tips
  // bowstring drawn back to the nocked arrow
  line(cv, 10, 2, 6, 8, str); line(cv, 10, 14, 6, 8, str);
  // nocked arrow toward lower-left
  line(cv, 6, 8, 2, 12, s.blade ? s.blade[2] : w[1]);
  px(cv, 2, 12, s.blade ? s.blade[4] : [206, 210, 220]); // arrowhead
  px(cv, 7, 8, s.blade ? s.blade[3] : w[3]);             // fletching
};

draw.mace = (cv, s) => {
  const m = s.metal || P.darkiron, g = s.grip || P.wood, gem = s.gem;
  // wrapped handle diagonal lower-left
  grip(cv, 5, 13, 5, g);
  crossguard(cv, 6, 12, 1, m);
  // round flanged head top-right
  disc(cv, 11, 5, 3, m[2]);
  // shading + spikes/studs
  px(cv, 9, 3, m[3]); px(cv, 10, 4, m[4]); px(cv, 9, 4, m[3]);
  px(cv, 13, 7, m[1]); px(cv, 12, 7, m[1]);
  const studs = [[11, 2], [8, 5], [14, 5], [11, 8]];
  for (const [x, y] of studs) { px(cv, x, y, m[3]); }
  px(cv, 11, 1, m[4]); px(cv, 7, 5, m[3]); px(cv, 15, 5, m[1]);
  if (gem) gemInlay(cv, 11, 5, gem, 1);
};

draw.axe = (cv, s) => {
  const b = s.blade, g = s.grip || P.wood;
  // handle diagonal
  for (let i = 0; i <= 9; i++) { px(cv, 4 + i, 14 - i, g[3]); px(cv, 4 + i, 15 - i, g[1]); }
  // single-bit axe head: filled wedge with a curved cutting edge
  for (let y = 2; y <= 9; y++) for (let x = 7; x <= 14; x++) {
    const edge = (14 - x) + Math.abs(y - 5.5);
    if (edge <= 5) {
      let c = b[2];
      if (x >= 13) c = b[1];                 // back of head shadow
      if (y <= 3 || (x <= 9 && y <= 5)) c = b[3]; // top + inner light
      if (x <= 8) c = b[4];                  // bright cutting edge (front)
      px(cv, x, y, c);
    }
  }
  line(cv, 7, 5, 8, 4, b[4]);                // edge shine
  px(cv, 11, 5, b[0]); px(cv, 11, 6, b[1]);  // eye/bolt
  px(cv, 10, 3, b[4]);
};

draw.pickaxe = (cv, s) => {
  const m = s.metal || P.netherite, g = s.grip || P.wood, gem = s.gem;
  // handle
  for (let i = 0; i <= 10; i++) { px(cv, 8, 14 - i, g[3]); px(cv, 9, 14 - i, g[1]); }
  // curved double-point head across the top with a 2px rim
  const arc = [[2, 5], [3, 4], [5, 3], [8, 2], [11, 3], [13, 4], [14, 5]];
  for (let i = 1; i < arc.length; i++) line(cv, arc[i - 1][0], arc[i - 1][1], arc[i][0], arc[i][1], m[2]);
  for (const [x, y] of arc) { px(cv, x, y - 1, m[3]); px(cv, x, y + 1, m[1]); }
  px(cv, 2, 5, m[4]); px(cv, 14, 5, m[4]);   // bright tips
  px(cv, 8, 2, m[4]); px(cv, 8, 3, m[3]);    // crown highlight
  px(cv, 6, 3, m[3]); px(cv, 10, 3, m[3]);
  if (gem) gemInlay(cv, 8, 4, gem, 1);        // socket gem
};

draw.shovel = (cv, s) => {
  const m = s.metal || P.iron, g = s.grip || P.wood;
  // handle diagonal upper-right
  for (let i = 0; i <= 7; i++) { px(cv, 8 + i, 9 - i, g[3]); px(cv, 9 + i, 9 - i, g[1]); px(cv, 8 + i, 10 - i, g[2]); }
  // socket collar
  fillRect(cv, 6, 8, 8, 9, m[1]); px(cv, 6, 8, m[2]);
  // rounded spade blade lower-left
  for (let y = 8; y <= 14; y++) for (let x = 2; x <= 8; x++) {
    const w = (y >= 13) ? (y - 12) : 0;
    if (x >= 2 + w && x <= 8 - w) {
      let c = m[2];
      if (x <= 3) c = m[3];
      if (x >= 7) c = m[1];
      if (y >= 13) c = m[4];   // bright digging edge
      px(cv, x, y, c);
    }
  }
  line(cv, 2, 8, 8, 8, m[3]); // top bevel
  px(cv, 3, 10, m[4]);        // sheen
};

draw.hoe = (cv, s) => {
  const m = s.metal || P.iron, g = s.grip || P.wood;
  for (let i = 0; i <= 10; i++) { px(cv, 4 + i, 14 - i, g[3]); px(cv, 4 + i, 15 - i, g[1]); }
  // L-shaped head at the top
  fillRect(cv, 9, 2, 14, 3, m[2]);
  fillRect(cv, 9, 2, 10, 5, m[2]);
  line(cv, 9, 2, 14, 2, m[3]);
  line(cv, 9, 4, 14, 4, m[1]); // under-bevel
  px(cv, 14, 3, m[1]); px(cv, 9, 2, m[4]); px(cv, 13, 2, m[4]);
};

draw.claw = (cv, s) => {
  const m = s.metal || P.shadow, g = s.grip || P.leather;
  // gauntlet / grip base lower-left with knuckle rivets
  fillRect(cv, 2, 11, 7, 14, g[2]);
  line(cv, 2, 11, 7, 11, g[3]); line(cv, 2, 14, 7, 14, g[1]);
  px(cv, 3, 12, g[4]); px(cv, 6, 12, m[3]); px(cv, 4, 13, m[1]); // rivets
  // three fanned talons of increasing reach with bright tips
  const claws = [
    [[6, 11], [8, 8], [9, 5], [9, 3]],
    [[7, 11], [9, 9], [11, 6], [12, 4]],
    [[7, 12], [10, 11], [13, 9], [14, 7]],
  ];
  for (const c of claws) {
    for (let i = 1; i < c.length; i++) line(cv, c[i - 1][0], c[i - 1][1], c[i][0], c[i][1], m[2]);
    for (const p of c) px(cv, p[0] - 1, p[1] - 1, m[3]); // top edge highlight
    for (const p of c) px(cv, p[0] + 1, p[1] + 1, m[1]); // underside shadow
    const tip = c[c.length - 1]; px(cv, tip[0], tip[1], m[4]);
  }
};

draw.shield = (cv, s) => {
  const m = s.metal || P.iron, ac = s.accent || P.gold[2];
  for (let y = 2; y <= 14; y++) for (let x = 3; x <= 12; x++) {
    const w = (y < 11) ? 0 : (y - 10);          // taper to point at bottom
    if (x >= 3 + w && x <= 12 - w) {
      let c = m[2];
      if (x <= 4 || y <= 3) c = m[3];
      if (x >= 11 || y >= 12) c = m[1];
      px(cv, x, y, c);
    }
  }
  // raised rim border + corner studs
  line(cv, 3, 2, 12, 2, m[1]); line(cv, 4, 3, 11, 3, m[4]);
  for (const [x, y] of [[4, 3], [11, 3], [4, 10], [11, 10]]) px(cv, x, y, m[4]);
  // central boss + emblem cross
  disc(cv, 7, 7, 1, m[3]);
  line(cv, 7, 4, 7, 10, ac); line(cv, 5, 7, 10, 7, ac);
  px(cv, 7, 7, ac); px(cv, 6, 4, m[4]);
};

draw.helmet = (cv, s) => {
  const m = s.metal || P.iron, ac = s.accent;
  // rounded dome
  for (let y = 3; y <= 8; y++) for (let x = 4; x <= 11; x++) {
    if (y === 3 && (x < 5 || x > 10)) continue;
    let c = m[2];
    if (x <= 5 || y === 3) c = m[3];
    if (x >= 10) c = m[1];
    px(cv, x, y, c);
  }
  // brow ridge + side rivets
  line(cv, 5, 8, 10, 8, m[3]);
  px(cv, 4, 6, m[1]); px(cv, 11, 6, m[1]);
  // visor slit with a faint inner glow line
  fillRect(cv, 4, 9, 11, 11, m[1]);
  fillRect(cv, 5, 10, 10, 10, [10, 10, 14]);
  line(cv, 5, 9, 10, 9, m[2]);
  // crest / trim accent
  if (ac) { px(cv, 7, 2, ac); px(cv, 8, 2, ac); px(cv, 7, 1, ac); line(cv, 4, 9, 11, 9, ac); }
  px(cv, 5, 4, m[4]); px(cv, 6, 4, m[3]);
};

draw.chest = (cv, s) => {
  const m = s.metal || P.iron, ac = s.accent;
  // pauldrons (shoulders) with rivets
  fillRect(cv, 2, 4, 4, 6, m[2]); fillRect(cv, 11, 4, 13, 6, m[2]);
  line(cv, 2, 4, 4, 4, m[3]); line(cv, 11, 4, 13, 4, m[3]);
  px(cv, 3, 5, m[4]); px(cv, 12, 5, m[1]);
  // torso plate with breast contour
  for (let y = 4; y <= 13; y++) for (let x = 5; x <= 10; x++) {
    let c = m[2];
    if (x <= 5) c = m[3];
    if (x >= 10 || y >= 12) c = m[1];
    px(cv, x, y, c);
  }
  line(cv, 7, 4, 7, 13, m[1]);          // centre seam
  line(cv, 6, 5, 6, 10, m[3]);          // left pectoral highlight
  line(cv, 9, 5, 9, 10, m[1]);          // right pectoral shadow
  // belt + trim + chest gem
  fillRect(cv, 5, 11, 10, 11, m[1]);
  if (ac) { line(cv, 5, 7, 10, 7, ac); px(cv, 4, 5, ac); px(cv, 13, 5, ac); gemInlay(cv, 8, 9, ramp5(ac), 0); }
  px(cv, 6, 5, m[4]);
};

draw.legs = (cv, s) => {
  const m = s.metal || P.iron, ac = s.accent;
  // waistband with belt buckle
  fillRect(cv, 4, 3, 11, 5, m[2]); line(cv, 4, 3, 11, 3, m[3]);
  px(cv, 7, 4, m[4]); px(cv, 8, 4, m[1]); // buckle
  if (ac) line(cv, 4, 4, 11, 4, ac);
  // two greaves with a centre gap + knee plates
  for (let y = 6; y <= 14; y++) {
    for (let x = 4; x <= 6; x++) px(cv, x, y, x === 4 ? m[3] : m[2]);
    for (let x = 9; x <= 11; x++) px(cv, x, y, x === 11 ? m[1] : m[2]);
  }
  line(cv, 4, 6, 4, 14, m[3]);
  line(cv, 11, 6, 11, 14, m[1]);
  line(cv, 4, 9, 6, 9, m[1]); line(cv, 9, 9, 11, 9, m[1]); // knee bands
  px(cv, 5, 7, m[4]); px(cv, 10, 7, m[3]);
};

draw.boots = (cv, s) => {
  const m = s.metal || P.iron, ac = s.accent;
  // two boots side by side
  for (const off of [2, 9]) {
    fillRect(cv, off, 5, off + 4, 11, m[2]);     // ankle/leg
    fillRect(cv, off, 12, off + 5, 13, m[1]);    // sole/foot extends forward
    fillRect(cv, off, 12, off + 4, 12, m[2]);
    line(cv, off, 5, off, 11, m[3]);             // left highlight
    line(cv, off + 4, 5, off + 4, 11, m[1]);
    line(cv, off, 13, off + 5, 13, m[0]);        // sole shadow
    px(cv, off + 1, 6, m[4]);
    px(cv, off + 2, 9, m[1]);                    // ankle rivet
    if (ac) line(cv, off, 5, off + 4, 5, ac);    // cuff trim
  }
};

draw.wings = (cv, s) => {
  const m = s.metal || P.silver, ac = s.accent;
  // central body
  fillRect(cv, 7, 4, 8, 11, m[1]);
  line(cv, 7, 4, 7, 11, m[2]);
  // two elytra wings sweeping out & down with feathered tips
  for (let i = 0; i < 6; i++) {
    const y = 4 + i;
    const left = 6 - i, right = 9 + i;
    line(cv, left, y, 6, y, m[2]);
    line(cv, 9, y, right, y, m[2]);
    px(cv, left, y, m[3]); px(cv, right, y, m[1]);
  }
  // ribs / veins
  line(cv, 4, 9, 6, 6, m[3]); line(cv, 11, 9, 9, 6, m[3]);
  if (ac) { line(cv, 7, 4, 8, 4, ac); }
  px(cv, 5, 7, m[4]);
};

draw.potion = (cv, s) => {
  const liq = s.liquid || P.crimson;
  const glass = [196, 214, 220];
  // cork
  fillRect(cv, 7, 1, 8, 2, P.wood[2]); px(cv, 7, 1, P.wood[3]);
  // neck
  fillRect(cv, 7, 3, 8, 5, glass);
  // round bulb
  disc(cv, 7, 10, 4, glass);
  // liquid inside (lower portion)
  for (let y = 8; y <= 13; y++) for (let x = 3; x <= 11; x++) {
    if ((x - 7) * (x - 7) + (y - 10) * (y - 10) <= 11) {
      let c = liq[2];
      if (y >= 12) c = liq[1];
      if (y === 8) c = liq[3];
      px(cv, x, y, c);
    }
  }
  line(cv, 5, 8, 9, 8, liq[3]);        // liquid surface
  // glass shine
  px(cv, 4, 9, WHITE); px(cv, 4, 10, [230, 240, 245]); px(cv, 5, 12, liq[4]);
};

draw.gem = (cv, s) => {
  const g = s.gem || P.diamond;
  // faceted hexagonal gem
  const rows = [
    [6, 9], [5, 10], [4, 11], [4, 11], [4, 11], [5, 10], [6, 9], [7, 8],
  ];
  for (let i = 0; i < rows.length; i++) {
    const y = 3 + i; const [a, b] = rows[i];
    for (let x = a; x <= b; x++) {
      let c = g[2];
      if (x <= a + 1) c = g[3];
      if (x >= b - 1) c = g[1];
      if (i <= 1) c = g[3];
      if (i >= 6) c = g[1];
      px(cv, x, y, c);
    }
  }
  // facet lines + specular
  line(cv, 7, 4, 5, 7, g[1]); line(cv, 8, 4, 10, 7, g[1]);
  line(cv, 4, 6, 11, 6, g[0]);
  px(cv, 6, 4, g[4]); px(cv, 5, 5, WHITE); px(cv, 9, 9, g[4]);
};

draw.coin = (cv, s) => {
  const m = s.metal || P.gold, ac = s.accent;
  disc(cv, 8, 8, 5, m[2]);
  // rim
  for (let a = 0; a < 360; a += 12) { const x = 8 + Math.round(5 * Math.cos(a * Math.PI / 180)), y = 8 + Math.round(5 * Math.sin(a * Math.PI / 180)); px(cv, x, y, m[1]); }
  line(cv, 5, 4, 7, 4, m[4]); px(cv, 4, 6, m[3]);   // top-left shine
  // embossed emblem (star or rune)
  const e = ac || m[4];
  px(cv, 8, 5, e); px(cv, 8, 11, e); px(cv, 5, 8, e); px(cv, 11, 8, e);
  px(cv, 8, 8, e); px(cv, 7, 7, m[3]); px(cv, 9, 9, m[1]);
};

draw.pouch = (cv, s) => {
  const l = s.metal || P.leather, gold = P.gold;
  // bag body (rounded, wider at bottom)
  for (let y = 6; y <= 14; y++) for (let x = 3; x <= 12; x++) {
    const w = (y < 8) ? (8 - y) : 0;
    if (x >= 3 + w && x <= 12 - w) {
      let c = l[2]; if (x <= 4) c = l[3]; if (x >= 11 || y >= 13) c = l[1]; px(cv, x, y, c);
    }
  }
  // drawstring neck + ties
  fillRect(cv, 6, 4, 9, 6, l[1]);
  line(cv, 5, 4, 10, 4, l[3]);
  px(cv, 5, 3, gold[2]); px(cv, 10, 3, gold[2]);
  // coin peeking
  px(cv, 8, 5, gold[3]); px(cv, 7, 5, gold[2]); px(cv, 8, 4, gold[4]);
  px(cv, 5, 8, l[4]);
};

draw.scroll = (cv, s) => {
  const p = P.paper, rod = s.metal || P.wood, ru = s.accent || [120, 40, 30];
  // top & bottom rolled ends
  fillRect(cv, 3, 2, 12, 3, rod[2]); line(cv, 3, 2, 12, 2, rod[3]); px(cv, 3, 3, rod[1]);
  fillRect(cv, 3, 12, 12, 13, rod[2]); line(cv, 3, 13, 12, 13, rod[1]);
  // parchment sheet
  fillRect(cv, 4, 4, 11, 11, p[2]);
  line(cv, 4, 4, 4, 11, p[3]); line(cv, 11, 4, 11, 11, p[1]);
  line(cv, 4, 4, 11, 4, p[3]);
  // rune / writing lines
  for (let y = 6; y <= 10; y += 2) line(cv, 5, y, 10, y, p[0]);
  px(cv, 7, 7, ru); px(cv, 8, 8, ru); px(cv, 7, 9, ru); // seal mark
  px(cv, 5, 5, p[4]);
};

draw.sheet = (cv, s) => {
  const p = P.paper, ac = s.accent || [60, 110, 200], band = s.band;
  // a slightly tilted paper sheet
  for (let y = 2; y <= 14; y++) for (let x = 3; x <= 12; x++) {
    let c = p[2];
    if (x <= 4) c = p[3]; if (x >= 11 || y >= 13) c = p[1];
    px(cv, x, y, c);
  }
  line(cv, 3, 2, 12, 2, p[3]); px(cv, 4, 3, p[4]);
  // ruled lines
  for (let y = 5; y <= 11; y += 2) line(cv, 5, y, 10, y, p[0]);
  if (band) fillRect(cv, 3, 7, 12, 8, band);     // banknote band
  // emblem / checkbox
  fillRect(cv, 6, 9, 9, 12, p[3]); line(cv, 6, 9, 9, 9, p[0]);
  px(cv, 7, 10, ac); px(cv, 8, 11, ac); px(cv, 6, 11, ac);
};

draw.book = (cv, s) => {
  const cover = s.metal || P.cursed, ac = s.accent || P.gold[3], p = P.paper;
  // cover
  for (let y = 2; y <= 14; y++) for (let x = 3; x <= 12; x++) {
    let c = cover[2]; if (x <= 4) c = cover[3]; if (x >= 11 || y >= 13) c = cover[1]; px(cv, x, y, c);
  }
  // ridged spine
  fillRect(cv, 3, 2, 4, 14, cover[1]); px(cv, 3, 4, cover[0]); px(cv, 3, 8, cover[0]); px(cv, 3, 12, cover[0]);
  // gilded page edges on the right
  fillRect(cv, 12, 3, 12, 13, p[3]); px(cv, 12, 4, p[4]); px(cv, 12, 9, p[4]);
  // gilt border + clasp
  line(cv, 5, 2, 11, 2, ac); line(cv, 5, 13, 11, 13, darken(ac, 0.7));
  px(cv, 11, 7, ac); px(cv, 12, 7, ac); // clasp band
  // embossed arcane emblem
  line(cv, 7, 5, 8, 5, ac); line(cv, 6, 6, 9, 6, ac); px(cv, 7, 7, ac); px(cv, 8, 8, ac);
  px(cv, 7, 9, ac); px(cv, 5, 4, cover[4]);
};

draw.crown = (cv, s) => {
  const m = s.metal || P.gold, gem = s.gem || P.ruby;
  // band
  fillRect(cv, 3, 10, 12, 12, m[2]);
  line(cv, 3, 10, 12, 10, m[3]); line(cv, 3, 12, 12, 12, m[1]);
  // points
  const peaks = [3, 6, 8, 11];
  for (const x of peaks) { line(cv, x, 10, x, 5, m[2]); px(cv, x, 5, m[4]); px(cv, x - 1, 9, m[3]); }
  line(cv, 3, 9, 12, 9, m[3]);
  // jewels on band
  px(cv, 5, 11, gem[3]); px(cv, 8, 11, gem[3]); px(cv, 10, 11, gem[2]);
  px(cv, 7, 4, gem[4]); // top jewel
  px(cv, 4, 11, m[4]);
};

draw.gavel = (cv, s) => {
  const m = s.metal || P.wood, band = s.accent || P.gold[2];
  // cylindrical head (horizontal) upper area
  for (let y = 3; y <= 7; y++) for (let x = 3; x <= 11; x++) {
    let c = m[2]; if (y === 3) c = m[3]; if (y === 7) c = m[1]; px(cv, x, y, c);
  }
  // metal bands
  fillRect(cv, 4, 3, 4, 7, band); fillRect(cv, 10, 3, 10, 7, band);
  line(cv, 4, 3, 11, 3, m[4]);
  // handle diagonal down-right
  for (let i = 0; i <= 7; i++) { px(cv, 8 + i, 7 + i, m[2]); px(cv, 9 + i, 7 + i, m[1]); }
  px(cv, 5, 4, m[4]);
};

draw.star = (cv, s) => {
  const c = s.gem || P.amethyst;
  // 4-point sparkle
  line(cv, 8, 1, 8, 14, c[2]); line(cv, 1, 8, 14, 8, c[2]);
  line(cv, 8, 3, 8, 12, c[3]); line(cv, 3, 8, 12, 8, c[3]);
  disc(cv, 8, 8, 2, c[3]);
  // diagonal small rays
  px(cv, 5, 5, c[1]); px(cv, 11, 5, c[1]); px(cv, 5, 11, c[1]); px(cv, 11, 11, c[1]);
  disc(cv, 8, 8, 1, WHITE);
  px(cv, 8, 2, c[4]); px(cv, 2, 8, c[4]);
};

// --- Ranged / magic archetypes (shared so gen-ranged* need not duplicate) ---
draw.crossbow = (cv, s) => {
  const m = s.metal || P.iron, limb = s.blade || P.wood, str = s.string || s.accent || [235, 235, 240];
  // stock (diagonal lower-left)
  for (let i = 0; i <= 7; i++) { px(cv, 3 + i, 13 - i, m[3]); px(cv, 4 + i, 13 - i, m[1]); px(cv, 3 + i, 14 - i, m[2]); }
  // bow limbs (horizontal across the top) with recurve tips
  for (let x = 3; x <= 13; x++) { px(cv, x, 3, limb[3]); px(cv, x, 4, limb[2]); px(cv, x, 5, limb[1]); }
  px(cv, 2, 4, limb[2]); px(cv, 14, 4, limb[2]); px(cv, 3, 5, limb[1]); px(cv, 13, 5, limb[1]);
  // drawn string + loaded bolt
  line(cv, 3, 4, 8, 8, str); line(cv, 13, 4, 8, 8, str);
  bladeFill(cv, 8, 8, 11, 3, 1, P.silver);
  px(cv, 11, 2, [222, 222, 232]);
  px(cv, 6, 11, m[1]); px(cv, 7, 12, m[0]); // trigger
  px(cv, 5, 6, limb[4]);
};

draw.knife = (cv, s) => {
  const b = s.blade, g = s.grip || P.leather;
  bladeFill(cv, 7, 9, 13, 3, 1, b);
  crossguard(cv, 6, 10, 1, s.metal || P.silver);
  grip(cv, 5, 11, 3, g);
  px(cv, 3, 13, g[1]);
};

draw.kunai = (cv, s) => {
  const b = s.blade, g = s.grip || P.obsidian;
  bladeFill(cv, 7, 9, 12, 4, 1, b);
  grip(cv, 6, 10, 3, g);
  // ring pommel
  px(cv, 3, 13, g[2]); px(cv, 4, 13, g[1]); px(cv, 3, 14, g[1]); px(cv, 4, 14, g[3]);
};

draw.shuriken = (cv, s) => {
  const m = s.metal || P.silver, ac = s.accent;
  line(cv, 8, 2, 8, 13, m[2]); line(cv, 2, 8, 13, 8, m[2]);
  line(cv, 4, 4, 11, 11, m[2]); line(cv, 11, 4, 4, 11, m[2]);
  disc(cv, 8, 8, 2, m[3]);
  px(cv, 8, 8, [18, 18, 22]);
  for (const [x, y] of [[8, 2], [8, 13], [2, 8], [13, 8]]) px(cv, x, y, m[4]);
  if (ac) px(cv, 6, 6, ac);
};

draw.javelin = (cv, s) => {
  const b = s.blade, g = s.grip || P.wood, m = s.metal || P.gold;
  for (let i = 0; i <= 12; i++) { px(cv, 2 + i, 14 - i, g[3]); px(cv, 2 + i, 15 - i, g[1]); }
  bladeFill(cv, 11, 5, 15, 1, 1, b);
  px(cv, 15, 0, b[4]);
  crossguard(cv, 11, 5, 1, m);
};

draw.chakram = (cv, s) => {
  const m = s.metal || P.silver, ac = s.accent || P.gold[3];
  ring(cv, 8, 8, 6, m[1]);
  ring(cv, 8, 8, 5, m[2]);
  ring(cv, 8, 8, 4, m[3]);
  for (const [x, y] of [[8, 1], [8, 15], [1, 8], [15, 8], [3, 3], [13, 3], [3, 13], [13, 13]]) px(cv, x, y, m[4]);
  // upper-left rim catches the light
  px(cv, 4, 4, m[4]); px(cv, 6, 6, m[4]); px(cv, 8, 3, ac);
};

draw.orb = (cv, s) => {
  const g = s.gem || P.arcane, m = s.metal || P.gold;
  for (let y = 3; y <= 13; y++) for (let x = 3; x <= 13; x++) {
    const dx = x - 8, dy = y - 8;
    if (dx * dx + dy * dy <= 25) {
      let c = g[2];
      if (dx * dx + (dy + 1) * (dy + 1) <= 9) c = g[3];
      if (dx <= -2 && dy <= -1) c = g[4];
      if (dx >= 2 || dy >= 3) c = g[1];
      px(cv, x, y, c);
    }
  }
  px(cv, 6, 5, WHITE); px(cv, 5, 6, g[4]);
  // gilded stand
  fillRect(cv, 5, 14, 11, 15, m[1]); px(cv, 6, 14, m[2]); px(cv, 10, 14, m[0]);
  magicGlow(cv, 8, 8, 4, g[4], 45);
};

draw.gun = (cv, s) => {
  const m = s.metal || P.steel, ac = s.gem || s.blade || P.arcane;
  // receiver + barrel
  fillRect(cv, 2, 9, 11, 11, m[2]);
  line(cv, 2, 9, 11, 9, m[3]); line(cv, 2, 11, 11, 11, m[1]);
  fillRect(cv, 10, 6, 14, 9, m[2]);
  line(cv, 10, 6, 14, 6, m[3]);
  px(cv, 15, 6, ac[3]); px(cv, 15, 7, ac[2]); // muzzle glow
  // grip + trigger
  fillRect(cv, 3, 11, 5, 14, m[1]); px(cv, 4, 12, m[2]);
  px(cv, 6, 11, m[0]);
  // energy cell
  px(cv, 6, 8, ac[4]); px(cv, 7, 8, ac[3]); px(cv, 8, 8, ac[2]);
};

draw.focus = (cv, s) => {
  const g = s.gem || P.arcane, m = s.metal || P.gold;
  // floating ring + suspended gem core + handle
  ring(cv, 8, 6, 5, m[2]);
  for (let a = 30; a < 360; a += 90) px(cv, 8 + Math.round(5 * Math.cos(a * Math.PI / 180)), 6 + Math.round(5 * Math.sin(a * Math.PI / 180)), m[4]);
  disc(cv, 8, 6, 2, g[2]);
  gemInlay(cv, 8, 6, g, 1);
  for (let i = 0; i <= 6; i++) { px(cv, 8, 8 + i, m[2]); px(cv, 9, 8 + i, m[0]); }
  px(cv, 8, 14, m[3]);
  magicGlow(cv, 8, 6, 3, g[4], 55);
};

draw.compass = (cv, s) => {
  const m = s.metal || P.copper, n = s.accent || P.crimson[3];
  disc(cv, 8, 8, 5, m[2]);
  disc(cv, 8, 8, 4, [30, 34, 40]);     // dark face
  // needle
  line(cv, 8, 4, 8, 8, n); line(cv, 8, 8, 8, 12, [200, 200, 210]);
  px(cv, 8, 4, n); px(cv, 8, 8, m[4]);
  // rim shine
  px(cv, 5, 5, m[4]); px(cv, 11, 11, m[1]);
};

// ----------------------------------------------------------------------------
// Block draw functions (tile-safe, no outline)
// ----------------------------------------------------------------------------
const block = {};

function shade(cv, x, y, base, r) {
  const v = (r() - 0.5) * 0.22;
  const c = [
    Math.max(0, Math.min(255, Math.round(base[2][0] * (1 + v)))),
    Math.max(0, Math.min(255, Math.round(base[2][1] * (1 + v)))),
    Math.max(0, Math.min(255, Math.round(base[2][2] * (1 + v)))),
  ];
  px(cv, x, y, c);
}

block.bricks = (cv, pal) => {
  const r = rng(7);
  const mortar = pal[0];
  fillRect(cv, 0, 0, 15, 15, mortar);
  const rowH = 4;
  for (let row = 0; row < 4; row++) {
    const y0 = row * rowH;
    const offset = (row % 2) * 4;
    for (let bx = -1; bx < 4; bx++) {
      const x0 = bx * 8 + offset;
      // brick spans 7 wide, 3 tall within the row
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
};

block.pillar = (cv, pal) => {
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    let c = pal[2];
    if (x < 2 || x > 13) c = pal[1];
    else if (x < 4) c = pal[3];
    px(cv, x, y, c);
  }
  // vertical grooves
  for (const gx of [4, 8, 11]) for (let y = 0; y < 16; y++) px(cv, gx, y, pal[1]);
  for (const gx of [5, 9, 12]) for (let y = 0; y < 16; y++) px(cv, gx, y, pal[3]);
  // capital bands top & bottom
  fillRect(cv, 0, 0, 15, 1, pal[3]); fillRect(cv, 0, 14, 15, 15, pal[1]);
};

block.cobble = (cv, pal) => {
  const r = rng(13);
  fillRect(cv, 0, 0, 15, 15, pal[0]);
  const stones = [[2, 2, 3], [8, 2, 3], [13, 3, 2], [4, 7, 3], [10, 7, 3], [2, 12, 3], [8, 12, 3], [13, 11, 2]];
  for (const [cx, cy, rr] of stones) {
    for (let y = -rr; y <= rr; y++) for (let x = -rr; x <= rr; x++) {
      if (x * x + y * y <= rr * rr + 1) {
        const xx = ((cx + x) % 16 + 16) % 16, yy = ((cy + y) % 16 + 16) % 16;
        let c = pal[2]; if (x <= -rr + 1 || y <= -rr + 1) c = pal[3]; if (x >= rr - 1 || y >= rr - 1) c = pal[1];
        px(cv, xx, yy, c);
        if (r() > 0.85) shade(cv, xx, yy, pal, r);
      }
    }
  }
};

block.road = (cv, pal) => {
  const r = rng(21);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const t = r();
    let c = pal[2];
    if (t > 0.85) c = pal[3]; else if (t < 0.18) c = pal[1];
    px(cv, x, y, c);
  }
  // faint expansion-joint seams
  for (let y = 0; y < 16; y++) px(cv, 0, y, pal[1]);
  for (let x = 0; x < 16; x++) px(cv, x, 0, pal[1]);
};

block.facade = (cv, pal) => {
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    let c = pal[2];
    if ((x + y) % 8 === 0) c = pal[3];
    px(cv, x, y, c);
  }
  // panel seams
  fillRect(cv, 0, 7, 15, 8, pal[1]);
  fillRect(cv, 7, 0, 8, 15, pal[1]);
  px(cv, 1, 1, pal[4]); px(cv, 9, 9, pal[4]);
};

block.window = (cv, pal) => {
  // frame
  fillRect(cv, 0, 0, 15, 15, P.darkiron[1]);
  fillRect(cv, 2, 2, 13, 13, pal[2]);
  // glass gradient + reflection streak
  for (let y = 2; y <= 13; y++) for (let x = 2; x <= 13; x++) {
    let c = mix(pal[1], pal[3], (x + y) / 26);
    px(cv, x, y, c);
  }
  line(cv, 4, 11, 9, 4, pal[4]); line(cv, 5, 12, 10, 5, pal[3]);
  // mullions
  fillRect(cv, 7, 2, 8, 13, P.darkiron[1]);
  fillRect(cv, 2, 7, 13, 8, P.darkiron[1]);
};

block.lamp = (cv, pal) => {
  // metal casing
  fillRect(cv, 0, 0, 15, 15, pal[1]);
  fillRect(cv, 2, 2, 13, 13, pal[2]);
  // glowing core
  const glow = P.thunder;
  for (let y = 3; y <= 12; y++) for (let x = 3; x <= 12; x++) {
    const d = Math.abs(x - 7.5) + Math.abs(y - 7.5);
    let c = glow[2]; if (d < 3) c = glow[4]; else if (d < 5) c = glow[3];
    px(cv, x, y, c);
  }
  // bolts in corners
  for (const [x, y] of [[1, 1], [14, 1], [1, 14], [14, 14]]) px(cv, x, y, pal[3]);
};

block.marker = (cv, pal) => {
  const r = rng(33);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const t = r(); let c = pal[2]; if (t > 0.8) c = pal[3]; else if (t < 0.2) c = pal[1]; px(cv, x, y, c);
  }
  // glowing rune ring
  const ru = P.frost;
  disc(cv, 8, 8, 4, null);
  for (let a = 0; a < 360; a += 30) { const x = 8 + Math.round(4 * Math.cos(a * Math.PI / 180)), y = 8 + Math.round(4 * Math.sin(a * Math.PI / 180)); px(cv, x, y, ru[3]); }
  line(cv, 8, 5, 8, 11, ru[4]); line(cv, 6, 8, 10, 8, ru[4]);
  px(cv, 8, 8, WHITE);
};

block.banner = (cv, pal) => {
  // cloth field
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    let c = pal[2];
    if (x < 2) c = pal[3]; if (x > 13) c = pal[1];
    px(cv, x, y, c);
  }
  // hanging folds
  for (const fx of [4, 8, 12]) for (let y = 0; y < 16; y++) px(cv, fx, y, pal[1]);
  for (const fx of [5, 9, 13]) for (let y = 0; y < 16; y++) px(cv, fx, y, pal[3]);
  // gold emblem (fleur-ish)
  const g = P.gold;
  line(cv, 8, 3, 8, 11, g[3]); line(cv, 5, 7, 11, 7, g[3]);
  px(cv, 8, 2, g[4]); px(cv, 6, 5, g[2]); px(cv, 10, 5, g[2]); disc(cv, 8, 7, 1, g[2]);
};

// ----------------------------------------------------------------------------

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
  if (opts.rim) rimLight(cv, { hi: opts.rimHi, lo: opts.rimLo });
  if (opts.ao !== false) ambientOcclusion(cv, opts.aoStrength || 0.28);
  const ref = refRamp(spec);
  outline(cv, darken(ref[0], opts.outlineDarken || 0.42));
}

const HANDHELD = new Set(['sword', 'dagger', 'greatsword', 'cleaver', 'katana', 'scythe', 'staff', 'wand',
  'halberd', 'spear', 'trident', 'whip', 'bow', 'mace', 'axe', 'pickaxe', 'shovel', 'hoe', 'claw',
  'crossbow', 'knife', 'kunai', 'shuriken', 'javelin', 'chakram', 'gun', 'focus']);

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

function writeJSON(file, obj) { fs.writeFileSync(file, JSON.stringify(obj, null, 2) + '\n'); }
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
  canvas, inb, px, get, fillRect, line, disc, ring, darken, mix, clampColor, lighten, ramp5, outline, rng,
  ambientOcclusion, finishSprite, rimLight, gemInlay, metalSheen, leatherGrain, woodGrain, gemFacets, foodGloss, magicGlow, refRamp,
  bladeFill, grip, crossguard, pommel, draw, block,
  ASSETS, ITEM_TEX, BLOCK_TEX, ENTITY_TEX, ITEM_MODELS, BLOCK_MODELS, ITEM_DEFS, BLOCKSTATES, PREVIEW_DIR,
  writePng, writeJSON, itemModel, itemDef, blockItemDef, blockModel, blockState,
  generateItemBatch, generateBlockBatch, buildMontage,
};
