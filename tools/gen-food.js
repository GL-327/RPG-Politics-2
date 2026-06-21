/**
 * High-quality 16x16 pixel-art FOOD texture + model generator for the
 * politicalserver mod's food expansion (com/political/expansion/food).
 *
 * Style matches tools/generate-textures.js:
 *   - near-black auto outline around every silhouette
 *   - 5-tone shading ramps per surface (shadow / dark / mid / light / specular)
 *   - rounded, appetising forms with specular highlights
 *
 * It writes, for every `food_*` id:
 *   - textures/item/<id>.png         (the sprite)
 *   - models/item/<id>.json          (parent item/generated)
 *   - items/<id>.json                (item-definition pointing at the model)
 *
 * It ONLY touches food_* assets, so it is safe to run alongside other agents.
 *
 * Usage: node gen-food.js   (requires tools/node_modules with jimp)
 */
const { Jimp } = require('jimp');
const { finishSprite } = require('./pixel-art-lib');
const path = require('path');
const fs = require('fs');

const ASSETS = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'politicalserver');
const ITEM_TEX = path.join(ASSETS, 'textures', 'item');
const ITEM_MODELS = path.join(ASSETS, 'models', 'item');
const ITEM_DEFS = path.join(ASSETS, 'items');
const PREVIEW_DIR = path.join(__dirname, '..', '.texref');

// ----------------------------------------------------------------------------
// Canvas helpers (shared style with generate-textures.js)
// ----------------------------------------------------------------------------
const N = 16;
function canvas() { return Array.from({ length: N }, () => Array.from({ length: N }, () => null)); }
function inb(x, y) { return x >= 0 && x < N && y >= 0 && y < N; }
function px(cv, x, y, c) { x = Math.round(x); y = Math.round(y); if (inb(x, y) && c) cv[y][x] = c; }
function get(cv, x, y) { return inb(x, y) ? cv[y][x] : null; }
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
function darken(c, f) { return [Math.round(c[0] * f), Math.round(c[1] * f), Math.round(c[2] * f), c[3] == null ? 255 : c[3]]; }

// shaded sphere: light from upper-left, dark to lower-right
function sphere(cv, cx, cy, r, pal) {
  for (let y = -r; y <= r; y++) for (let x = -r; x <= r; x++) {
    if (x * x + y * y <= r * r + r * 0.5) {
      let c = pal[2];
      if (x <= -r + 1 || y <= -r + 1) c = pal[3];
      if (x >= r - 1 || y >= r - 1) c = pal[1];
      if (x <= -r && y <= -r) c = pal[1];
      px(cv, cx + x, cy + y, c);
    }
  }
  px(cv, cx - Math.round(r * 0.4), cy - Math.round(r * 0.4), pal[4]);
  px(cv, cx - Math.round(r * 0.4) + 1, cy - Math.round(r * 0.4), pal[3]);
}

function outline(cv, col) {
  const add = [];
  for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) {
    if (cv[y][x]) continue;
    if (get(cv, x, y - 1) || get(cv, x, y + 1) || get(cv, x - 1, y) || get(cv, x + 1, y)) add.push([x, y]);
  }
  for (const [x, y] of add) cv[y][x] = col;
}

// ----------------------------------------------------------------------------
// Palettes — 5 stops: [shadow, dark, mid, light, specular]
// ----------------------------------------------------------------------------
const P = {
  red:       [[80, 8, 10], [150, 22, 26], [206, 44, 44], [238, 92, 80], [255, 168, 150]],
  berry:     [[22, 20, 60], [44, 42, 112], [74, 78, 172], [122, 132, 214], [186, 196, 244]],
  grape:     [[44, 18, 66], [86, 40, 122], [132, 72, 172], [180, 124, 212], [218, 184, 238]],
  orange:    [[120, 52, 8], [182, 92, 16], [228, 136, 30], [246, 180, 76], [255, 216, 142]],
  peach:     [[150, 70, 50], [206, 120, 92], [238, 166, 132], [250, 202, 172], [255, 230, 208]],
  yellow:    [[120, 96, 12], [184, 150, 24], [230, 198, 42], [246, 224, 98], [255, 246, 172]],
  green:     [[26, 70, 18], [54, 118, 34], [94, 168, 54], [148, 208, 94], [206, 242, 160]],
  leaf:      [[20, 64, 24], [40, 110, 44], [74, 158, 70], [128, 200, 110], [196, 240, 168]],
  brown:     [[60, 34, 14], [104, 62, 26], [150, 96, 46], [190, 138, 84], [222, 184, 134]],
  crust:     [[80, 46, 18], [128, 78, 32], [176, 116, 56], [210, 156, 94], [236, 200, 146]],
  meat:      [[80, 24, 22], [140, 50, 42], [190, 86, 70], [218, 130, 108], [240, 178, 156]],
  cooked:    [[78, 40, 20], [132, 76, 40], [180, 116, 66], [210, 154, 100], [234, 194, 144]],
  bowl:      [[40, 44, 54], [78, 86, 104], [120, 132, 156], [172, 184, 208], [222, 232, 246]],
  cream:     [[150, 146, 136], [196, 192, 182], [224, 222, 214], [242, 240, 234], [255, 255, 252]],
  choco:     [[38, 20, 10], [78, 44, 22], [116, 68, 36], [152, 98, 58], [192, 138, 92]],
  pink:      [[140, 40, 78], [196, 72, 118], [230, 120, 162], [246, 170, 200], [255, 214, 230]],
  nori:      [[10, 28, 18], [22, 54, 36], [40, 86, 58], [70, 120, 86], [112, 162, 122]],
  tan:       [[120, 90, 58], [170, 138, 100], [208, 184, 150], [232, 218, 192], [250, 244, 228]],
  white:     [[148, 150, 156], [192, 196, 204], [222, 226, 232], [240, 242, 246], [255, 255, 255]],
  coffee:    [[40, 22, 14], [78, 46, 28], [112, 70, 42], [150, 100, 64], [188, 140, 96]],
  glass:     [[120, 150, 168], [168, 196, 210], [200, 224, 234], [226, 242, 248], [248, 254, 255]],
  juice:     [[150, 70, 8], [206, 110, 18], [238, 152, 36], [250, 192, 88], [255, 222, 150]],
  lime:      [[110, 120, 10], [168, 184, 26], [206, 224, 56], [228, 244, 120], [244, 252, 186]],
  cocoa:     [[44, 24, 14], [86, 50, 28], [124, 78, 46], [158, 110, 72], [196, 152, 110]],
  steel:     [[40, 44, 54], [86, 94, 112], [136, 146, 168], [186, 196, 216], [232, 240, 250]],
};
const WHITE = [255, 255, 255];

// ----------------------------------------------------------------------------
// Food draw archetypes.  `s` is the spec: { pal, pal2, ... }
// ----------------------------------------------------------------------------
const draw = {};

// generic round fruit with optional little stem + leaf
draw.fruit = (cv, s) => {
  const p = s.pal;
  sphere(cv, 8, 9, 5, p);
  if (s.stem !== false) { px(cv, 8, 3, P.brown[1]); px(cv, 9, 3, P.brown[2]); }
  if (s.leaf !== false) { px(cv, 9, 3, P.leaf[2]); px(cv, 10, 2, P.leaf[3]); px(cv, 11, 3, P.leaf[1]); }
};

// slightly squat tomato/onion
draw.bulb = (cv, s) => {
  const p = s.pal;
  for (let y = -4; y <= 5; y++) for (let x = -5; x <= 5; x++) {
    if ((x * x) / 26 + (y * y) / 22 <= 1) {
      let c = p[2];
      if (x <= -3 || y <= -3) c = p[3];
      if (x >= 3 || y >= 4) c = p[1];
      px(cv, 8 + x, 9 + y, c);
    }
  }
  px(cv, 5, 6, p[4]);
  if (s.crown) { px(cv, 7, 3, P.leaf[2]); px(cv, 8, 2, P.leaf[3]); px(cv, 9, 3, P.leaf[1]); px(cv, 8, 4, P.leaf[2]); }
  if (s.papery) { line(cv, 6, 14, 10, 14, p[0]); px(cv, 8, 13, p[0]); }
};

draw.strawberry = (cv, s) => {
  const p = P.red;
  // tapered heart-ish body
  const rows = [[6, 10], [5, 11], [5, 11], [5, 11], [6, 10], [7, 9], [8, 8]];
  for (let i = 0; i < rows.length; i++) {
    const y = 6 + i; const [a, b] = rows[i];
    for (let x = a; x <= b; x++) {
      let c = p[2];
      if (x <= a + 1) c = p[3];
      if (x >= b - 1) c = p[1];
      if (i >= 5) c = p[1];
      px(cv, x, y, c);
    }
  }
  // seeds
  for (const [x, y] of [[7, 7], [9, 8], [6, 9], [10, 9], [8, 10], [7, 11]]) px(cv, x, y, P.yellow[3]);
  // green calyx
  for (const [x, y] of [[6, 5], [7, 5], [8, 5], [9, 5], [10, 5], [8, 4]]) px(cv, x, y, P.leaf[2]);
  px(cv, 7, 4, P.leaf[3]); px(cv, 9, 4, P.leaf[1]);
  px(cv, 6, 7, p[4]);
};

draw.cluster = (cv, s) => {
  const p = s.pal;
  const balls = s.balls || [[6, 7], [9, 7], [7, 9], [10, 10], [8, 11], [11, 8], [5, 10]];
  for (const [cx, cy] of balls) {
    for (let y = -2; y <= 2; y++) for (let x = -2; x <= 2; x++) {
      if (x * x + y * y <= 4) {
        let c = p[2]; if (x <= -1 && y <= -1) c = p[3]; if (x >= 1 && y >= 1) c = p[1]; px(cv, cx + x, cy + y, c);
      }
    }
    px(cv, cx - 1, cy - 1, p[4]);
  }
  if (s.stem !== false) { px(cv, 8, 3, P.brown[2]); px(cv, 8, 4, P.brown[1]); px(cv, 9, 4, P.leaf[2]); px(cv, 10, 3, P.leaf[3]); }
};

draw.banana = (cv, s) => {
  const p = P.yellow;
  const pts = [[4, 5], [4, 7], [5, 9], [6, 11], [8, 12], [10, 12], [12, 11]];
  for (let i = 0; i < pts.length; i++) {
    const [x, y] = pts[i];
    for (let k = -1; k <= 1; k++) { px(cv, x + k, y, p[2]); }
    px(cv, x, y - 1, p[3]); px(cv, x, y + 1, p[1]);
    if (i > 0) line(cv, pts[i - 1][0], pts[i - 1][1], x, y, p[2]);
  }
  for (const [x, y] of pts) { px(cv, x, y - 1, p[3]); }
  px(cv, 4, 5, P.brown[1]); px(cv, 12, 11, P.brown[1]); // tips
  px(cv, 7, 9, p[4]);
};

draw.citrus = (cv, s) => {
  const p = s.pal;
  for (let y = -4; y <= 4; y++) for (let x = -5; x <= 5; x++) {
    if ((x * x) / 27 + (y * y) / 18 <= 1) {
      let c = p[2]; if (x <= -3 || y <= -2) c = p[3]; if (x >= 3 || y >= 3) c = p[1]; px(cv, 8 + x, 9 + y, c);
    }
  }
  px(cv, 5, 7, p[4]); px(cv, 13, 9, p[1]);
  px(cv, 3, 9, p[1]); px(cv, 13, 8, p[3]); // nubs
};

draw.pear = (cv, s) => {
  const p = P.green;
  // narrow top, round bottom
  const rows = [[8, 8], [7, 9], [7, 9], [6, 10], [5, 11], [5, 11], [6, 10]];
  for (let i = 0; i < rows.length; i++) {
    const y = 6 + i; const [a, b] = rows[i];
    for (let x = a; x <= b; x++) { let c = p[2]; if (x <= a + 1) c = p[3]; if (x >= b - 1) c = p[1]; px(cv, x, y, c); }
  }
  px(cv, 8, 4, P.brown[2]); px(cv, 8, 5, P.brown[1]); px(cv, 9, 4, P.leaf[2]);
  px(cv, 6, 8, p[4]);
};

draw.pineapple = (cv, s) => {
  const body = P.yellow;
  // crown
  for (const [x, y] of [[7, 1], [8, 0], [9, 1], [6, 2], [10, 2], [8, 2]]) px(cv, x, y, P.leaf[3]);
  line(cv, 8, 1, 8, 4, P.leaf[2]); line(cv, 6, 3, 7, 4, P.leaf[1]); line(cv, 10, 3, 9, 4, P.leaf[2]);
  // body
  for (let y = 5; y <= 14; y++) for (let x = 4; x <= 11; x++) {
    const w = (y >= 13) ? (y - 12) : 0;
    if (x >= 4 + w && x <= 11 - w) {
      let c = body[2]; if (x <= 5) c = body[3]; if (x >= 10 || y >= 13) c = body[1]; px(cv, x, y, c);
    }
  }
  // diamond cross-hatch
  for (let y = 5; y <= 13; y++) for (let x = 4; x <= 11; x++) {
    if (((x + y) % 3 === 0) || ((x - y + 16) % 3 === 0)) px(cv, x, y, body[1]);
  }
  px(cv, 6, 6, body[4]);
};

draw.coconut = (cv, s) => {
  const p = P.brown;
  sphere(cv, 8, 9, 5, p);
  // fibrous lines
  for (const x of [6, 8, 10]) line(cv, x, 5, x, 13, p[1]);
  // three eyes
  px(cv, 7, 7, p[0]); px(cv, 9, 7, p[0]); px(cv, 8, 9, p[0]);
  px(cv, 5, 6, p[4]);
};

draw.corn = (cv, s) => {
  const k = P.yellow;
  // husk leaves at base
  line(cv, 4, 13, 7, 9, P.leaf[2]); line(cv, 12, 13, 9, 9, P.leaf[1]);
  // cob
  for (let y = 2; y <= 12; y++) for (let x = 6; x <= 9; x++) {
    const w = (y <= 3 || y >= 11) ? 1 : 0;
    if (x >= 6 + w && x <= 9 - w) {
      let c = k[2]; if (x <= 6) c = k[3]; if (x >= 9) c = k[1]; px(cv, x, y, c);
    }
  }
  // kernel speckle
  for (let y = 3; y <= 11; y++) for (let x = 6; x <= 9; x++) if ((x + y) % 2 === 0) px(cv, x, y, k[1]);
  px(cv, 7, 4, k[4]);
};

draw.leafy = (cv, s) => {
  const p = P.leaf;
  for (let y = -4; y <= 4; y++) for (let x = -5; x <= 5; x++) {
    if (x * x + y * y <= 26) {
      let c = p[2]; if (x <= -2 && y <= 0) c = p[3]; if (x >= 2 || y >= 2) c = p[1]; px(cv, 8 + x, 8 + y, c);
    }
  }
  // crinkled veins
  line(cv, 8, 5, 8, 12, p[3]); line(cv, 5, 8, 11, 8, p[1]);
  line(cv, 6, 6, 10, 10, p[1]); line(cv, 10, 6, 6, 10, p[3]);
  px(cv, 5, 6, p[4]);
};

draw.cucumber = (cv, s) => {
  const p = P.green;
  for (let i = 0; i <= 10; i++) {
    const x = 3 + i, y = 12 - i;
    for (let k = -1; k <= 1; k++) px(cv, x + k, y + k, p[2]);
    px(cv, x - 1, y, p[3]); px(cv, x + 1, y, p[1]);
  }
  // bumps + ends
  px(cv, 3, 12, p[1]); px(cv, 13, 2, p[3]);
  for (const t of [0.3, 0.6]) { const x = 3 + 10 * t, y = 12 - 10 * t; px(cv, x, y, p[4]); }
};

draw.pumpkinWedge = (cv, s) => {
  const p = P.orange;
  // a triangular wedge (slice) with rind on the outer arc
  for (let y = 3; y <= 13; y++) {
    const w = Math.round((y - 3) * 0.6);
    for (let x = 7 - w; x <= 7 + w; x++) {
      let c = p[2]; if (x <= 7 - w + 1) c = p[3]; if (x >= 7 + w - 1) c = p[1]; px(cv, x, y, c);
    }
  }
  // green rind along the bottom arc
  for (let x = 0; x <= 12; x++) { const xx = 1 + x; if (get(cv, xx, 13)) px(cv, xx, 13, P.leaf[2]); }
  line(cv, 1, 13, 13, 13, P.leaf[1]);
  // seeds
  px(cv, 7, 8, P.cream[3]); px(cv, 6, 10, P.cream[2]); px(cv, 8, 10, P.cream[2]);
  px(cv, 6, 6, p[4]);
};

draw.chili = (cv, s) => {
  const p = P.red;
  const pts = [[6, 3], [6, 5], [7, 7], [8, 9], [9, 11], [9, 13]];
  for (let i = 0; i < pts.length; i++) {
    const [x, y] = pts[i];
    for (let k = -1; k <= 1; k++) px(cv, x + k, y, p[2]);
    px(cv, x - 1, y, p[3]); px(cv, x + 1, y, p[1]);
    if (i > 0) line(cv, pts[i - 1][0], pts[i - 1][1], x, y, p[2]);
  }
  px(cv, 9, 13, p[0]); // pointed tip
  px(cv, 6, 2, P.leaf[2]); px(cv, 7, 2, P.leaf[3]); px(cv, 5, 3, P.leaf[1]); // stem
  px(cv, 6, 5, p[4]);
};

draw.bread = (cv, s) => {
  const p = P.crust;
  for (let y = 5; y <= 12; y++) for (let x = 3; x <= 12; x++) {
    if ((x - 7.5) * (x - 7.5) / 24 + (y - 8.5) * (y - 8.5) / 14 <= 1) {
      let c = p[2]; if (x <= 5 || y <= 6) c = p[3]; if (x >= 11 || y >= 11) c = p[1]; px(cv, x, y, c);
    }
  }
  // slashes on top
  line(cv, 5, 7, 8, 6, p[1]); line(cv, 8, 8, 11, 7, p[1]);
  px(cv, 5, 7, p[4]); px(cv, 6, 6, P.tan[4]);
};

draw.cheese = (cv, s) => {
  const p = P.yellow;
  // wedge
  for (let y = 5; y <= 12; y++) {
    const x1 = 13 - Math.round((y - 5) * 0.2);
    for (let x = 3; x <= x1; x++) {
      let c = p[2]; if (y <= 6) c = p[3]; if (y >= 11 || x >= x1 - 1) c = p[1]; px(cv, x, y, c);
    }
  }
  // rind + holes
  fillRect(cv, 3, 5, 13, 5, P.orange[2]);
  px(cv, 6, 8, p[1]); px(cv, 9, 9, p[1]); px(cv, 7, 11, p[1]); px(cv, 11, 8, p[1]);
  px(cv, 4, 6, p[4]);
};

draw.bowl = (cv, s) => {
  const food = s.pal;
  // food mound
  for (let y = 6; y <= 9; y++) for (let x = 3; x <= 12; x++) {
    if ((x - 7.5) * (x - 7.5) / 22 + (y - 9) * (y - 9) / 9 <= 1) {
      let c = food[2]; if (y <= 6) c = food[3]; if (y >= 8) c = food[1]; px(cv, x, y, c);
    }
  }
  // garnish flecks
  if (s.garnish) for (const [x, y] of [[5, 7], [9, 6], [11, 7]]) px(cv, x, y, s.garnish);
  // ceramic bowl
  const b = P.bowl;
  for (let y = 9; y <= 13; y++) for (let x = 2; x <= 13; x++) {
    const w = (y >= 12) ? (y - 11) * 2 : 0;
    if (x >= 2 + w && x <= 13 - w) {
      let c = b[2]; if (x <= 3) c = b[3]; if (x >= 12 || y >= 12) c = b[1]; px(cv, x, y, c);
    }
  }
  line(cv, 2, 9, 13, 9, b[3]); // rim
  px(cv, 3, 10, b[4]);
};

draw.meat = (cv, s) => {
  const p = P.cooked;
  // drumstick: round meat top, bone bottom-left
  sphere(cv, 9, 7, 4, p);
  // bone
  for (let i = 0; i <= 4; i++) px(cv, 6 - i, 9 + i, P.cream[3]);
  px(cv, 2, 13, P.cream[4]); px(cv, 3, 13, P.cream[2]);
  // sear marks
  px(cv, 9, 5, p[0]); px(cv, 11, 7, p[0]); px(cv, 8, 8, p[0]);
  px(cv, 7, 5, p[4]);
};

draw.steak = (cv, s) => {
  const p = P.meat;
  for (let y = 4; y <= 12; y++) for (let x = 3; x <= 13; x++) {
    if ((x - 8) * (x - 8) / 30 + (y - 8) * (y - 8) / 20 <= 1) {
      let c = p[2]; if (x <= 5 || y <= 5) c = p[3]; if (x >= 11 || y >= 11) c = p[1]; px(cv, x, y, c);
    }
  }
  // grill marks
  line(cv, 5, 6, 9, 10, P.cooked[0]); line(cv, 8, 5, 12, 9, P.cooked[0]);
  // fat trim
  px(cv, 4, 7, P.cream[3]); px(cv, 5, 6, P.cream[2]);
  px(cv, 6, 6, p[4]);
};

draw.taco = (cv, s) => {
  const shell = P.crust;
  // folded U shell
  for (let y = 5; y <= 13; y++) for (let x = 3; x <= 13; x++) {
    const d = Math.abs(x - 8);
    const top = 11 - d; // U shape
    if (y >= top && y <= 13 && (x <= 5 || x >= 11 || y >= 11)) {
      let c = shell[2]; if (x <= 5) c = shell[3]; if (x >= 11) c = shell[1]; px(cv, x, y, c);
    }
  }
  // filling peeking on top
  fillRect(cv, 5, 6, 11, 8, P.leaf[2]);
  for (const [x, y] of [[6, 6], [9, 7], [10, 6]]) px(cv, x, y, P.red[2]);
  px(cv, 7, 7, P.cream[3]); // cheese
  px(cv, 5, 9, shell[4]);
};

draw.salad = (cv, s) => {
  // greens in a glass bowl
  for (let y = 4; y <= 9; y++) for (let x = 3; x <= 12; x++) {
    if ((x - 7.5) * (x - 7.5) / 24 + (y - 8) * (y - 8) / 14 <= 1) {
      let c = P.leaf[2]; if ((x + y) % 2 === 0) c = P.leaf[3]; if ((x + y) % 3 === 0) c = P.leaf[1]; px(cv, x, y, c);
    }
  }
  // veg bits
  px(cv, 5, 6, P.red[3]); px(cv, 10, 6, P.red[2]); px(cv, 8, 5, P.orange[3]); px(cv, 6, 8, P.yellow[3]);
  // glass bowl
  for (let y = 9; y <= 13; y++) for (let x = 3; x <= 12; x++) {
    const w = (y >= 12) ? (y - 11) * 2 : 0;
    if (x >= 3 + w && x <= 12 - w) { let c = P.glass[2]; if (x <= 4) c = P.glass[3]; if (x >= 11) c = P.glass[1]; px(cv, x, y, c); }
  }
  px(cv, 4, 10, WHITE);
};

draw.pizza = (cv, s) => {
  const crust = P.crust, cheese = P.yellow;
  // triangle slice, point at top
  for (let y = 2; y <= 13; y++) {
    const w = Math.round((y - 2) * 0.55);
    for (let x = 8 - w; x <= 8 + w; x++) {
      let c = cheese[2]; if (x <= 8 - w + 1) c = cheese[3]; if (x >= 8 + w - 1) c = cheese[1]; px(cv, x, y, c);
    }
  }
  // crust band at the bottom
  for (let x = 1; x <= 15; x++) { if (get(cv, x, 13)) px(cv, x, 13, crust[2]); if (get(cv, x, 12)) px(cv, x, 12, crust[1]); }
  line(cv, 2, 13, 14, 13, crust[3]);
  // pepperoni
  for (const [x, y] of [[8, 6], [6, 10], [10, 10]]) { px(cv, x, y, P.red[2]); px(cv, x, y, P.red[3]); px(cv, x - 1, y, P.red[1]); }
  px(cv, 8, 4, cheese[4]);
};

draw.burger = (cv, s) => {
  const bun = P.crust, patty = P.cooked, lettuce = P.leaf, cheese = P.yellow;
  // top bun (dome)
  for (let y = 3; y <= 6; y++) for (let x = 3; x <= 12; x++) {
    if ((x - 7.5) * (x - 7.5) / 24 + (y - 6) * (y - 6) / 10 <= 1) { let c = bun[2]; if (y <= 4) c = bun[3]; px(cv, x, y, c); }
  }
  // sesame
  px(cv, 6, 4, bun[4]); px(cv, 9, 5, bun[4]); px(cv, 8, 4, bun[4]);
  // lettuce
  fillRect(cv, 3, 7, 12, 7, lettuce[3]); line(cv, 3, 7, 12, 7, lettuce[2]);
  // cheese drips
  fillRect(cv, 3, 8, 12, 8, cheese[3]); px(cv, 5, 9, cheese[2]); px(cv, 10, 9, cheese[2]);
  // patty
  fillRect(cv, 3, 9, 12, 10, patty[2]); line(cv, 3, 10, 12, 10, patty[1]);
  // bottom bun
  fillRect(cv, 3, 11, 12, 12, bun[2]); line(cv, 3, 12, 12, 12, bun[1]);
  px(cv, 4, 4, bun[4]);
};

draw.sushi = (cv, s) => {
  // top-down roll: nori ring, rice, salmon center
  for (let y = -5; y <= 5; y++) for (let x = -5; x <= 5; x++) {
    const d = x * x + y * y;
    if (d <= 28) {
      let c;
      if (d >= 20) c = P.nori[2];
      else if (d >= 5) c = P.cream[3];
      else c = P.orange[3];
      px(cv, 8 + x, 8 + y, c);
    }
  }
  // shading
  px(cv, 5, 5, P.cream[4]); px(cv, 8, 8, P.orange[4]); px(cv, 11, 11, P.nori[1]);
  px(cv, 8, 8, P.red[3]);
};

draw.chocolate = (cv, s) => {
  const p = P.choco;
  for (let y = 4; y <= 13; y++) for (let x = 4; x <= 11; x++) {
    let c = p[2]; if (x <= 5 || y <= 5) c = p[3]; if (x >= 10 || y >= 12) c = p[1]; px(cv, x, y, c);
  }
  // segment grooves
  for (const gx of [7, 9]) line(cv, gx, 4, gx, 13, p[0]);
  for (const gy of [7, 10]) line(cv, 4, gy, 11, gy, p[0]);
  px(cv, 5, 5, p[4]); px(cv, 6, 5, p[3]);
};

draw.iceCream = (cv, s) => {
  const cone = P.crust, scoop = s.pal || P.pink;
  // cone (triangle, point down)
  for (let y = 8; y <= 14; y++) {
    const w = Math.round((14 - y) * 0.7);
    for (let x = 8 - w; x <= 8 + w; x++) { let c = cone[2]; if (x <= 8 - w + 1) c = cone[3]; px(cv, x, y, c); }
  }
  // waffle hatch
  for (let y = 9; y <= 13; y++) for (let x = 5; x <= 11; x++) if (get(cv, x, y) && (x + y) % 2 === 0) px(cv, x, y, cone[1]);
  // scoops
  sphere(cv, 6, 6, 3, scoop);
  sphere(cv, 10, 6, 3, scoop);
  sphere(cv, 8, 4, 3, scoop);
  px(cv, 7, 3, scoop[4]);
};

draw.pie = (cv, s) => {
  const crust = P.crust, fill = s.pal || P.red;
  // triangle slice point up
  for (let y = 3; y <= 12; y++) {
    const w = Math.round((y - 3) * 0.6);
    for (let x = 8 - w; x <= 8 + w; x++) {
      let c = fill[2]; if (x <= 8 - w + 1) c = fill[3]; if (x >= 8 + w - 1) c = fill[1]; px(cv, x, y, c);
    }
  }
  // crust lattice + base
  for (let x = 1; x <= 15; x++) { if (get(cv, x, 12)) px(cv, x, 12, crust[2]); if (get(cv, x, 11)) px(cv, x, 11, crust[1]); }
  line(cv, 2, 12, 14, 12, crust[3]);
  line(cv, 6, 7, 9, 4, crust[3]); line(cv, 7, 9, 11, 6, crust[2]);
  px(cv, 8, 5, fill[4]);
};

draw.donut = (cv, s) => {
  const dough = P.crust, glaze = s.pal || P.pink;
  for (let y = -5; y <= 5; y++) for (let x = -5; x <= 5; x++) {
    const d = x * x + y * y;
    if (d <= 28 && d >= 4) {
      let c = (d >= 18) ? dough[2] : glaze[2];
      if (x <= -3 || y <= -3) c = (d >= 18) ? dough[3] : glaze[3];
      if (x >= 3 || y >= 3) c = (d >= 18) ? dough[1] : glaze[1];
      px(cv, 8 + x, 8 + y, c);
    }
  }
  // glaze ring sits on the upper face
  for (let y = -4; y <= 1; y++) for (let x = -4; x <= 4; x++) {
    const d = x * x + y * y; if (d <= 20 && d >= 4) px(cv, 8 + x, 8 + y, glaze[2]);
  }
  // sprinkles
  for (const [x, y] of [[6, 5], [9, 4], [11, 7], [5, 8]]) px(cv, x, y, [60 + (x * 37) % 180, (y * 53) % 200, 200]);
  px(cv, 5, 5, glaze[4]);
};

draw.jar = (cv, s) => {
  const g = P.glass, lid = P.steel;
  // lid
  fillRect(cv, 4, 2, 11, 3, lid[2]); line(cv, 4, 2, 11, 2, lid[3]); line(cv, 4, 3, 11, 3, lid[1]);
  // jar body
  for (let y = 4; y <= 14; y++) for (let x = 3; x <= 12; x++) {
    const w = (y >= 13) ? 1 : 0;
    if (x >= 3 + w && x <= 12 - w) { let c = g[2]; if (x <= 4) c = g[3]; if (x >= 11) c = g[1]; px(cv, x, y, c); }
  }
  // cookies inside
  for (const [x, y] of [[6, 9], [9, 8], [7, 11], [10, 11]]) { sphere(cv, x, y, 2, P.choco); }
  for (const [x, y] of [[6, 9], [9, 8], [7, 11]]) px(cv, x, y, P.choco[0]); // chips
  px(cv, 4, 5, WHITE);
};

draw.caramel = (cv, s) => {
  const p = P.orange;
  // wrapped candy: square center + twisted ends
  fillRect(cv, 6, 6, 10, 11, p[2]);
  line(cv, 6, 6, 10, 6, p[3]); line(cv, 6, 11, 10, 11, p[1]); line(cv, 6, 6, 6, 11, p[3]); line(cv, 10, 6, 10, 11, p[1]);
  // wrapper twists
  for (const [x, y] of [[4, 7], [3, 8], [4, 9]]) px(cv, x, y, P.cream[2]);
  for (const [x, y] of [[12, 7], [13, 8], [12, 9]]) px(cv, x, y, P.cream[1]);
  px(cv, 7, 7, p[4]); px(cv, 8, 9, P.brown[1]);
};

// drink in a tall glass; liquid fills lower portion
draw.glass = (cv, s) => {
  const g = P.glass, liq = s.pal;
  // glass outline body
  for (let y = 3; y <= 14; y++) for (let x = 5; x <= 10; x++) {
    const inside = (x >= 6 && x <= 9);
    if (inside) {
      let c = (y >= 6) ? liq[2] : g[2];
      if (y >= 6 && x === 6) c = liq[3];
      if (y >= 6 && x === 9) c = liq[1];
      if (y === 6) c = liq[3];
      px(cv, x, y, c);
    } else {
      px(cv, x, y, g[1]); // glass walls
    }
  }
  // rim + shine + straw
  line(cv, 5, 3, 10, 3, g[3]);
  px(cv, 6, 8, liq[4]); px(cv, 6, 5, WHITE);
  if (s.straw) { line(cv, 9, 1, 8, 7, s.straw); px(cv, 9, 1, s.straw); }
  if (s.foam) { fillRect(cv, 6, 5, 9, 6, P.cream[3]); px(cv, 7, 5, P.cream[4]); }
};

// hot drink in a mug
draw.mug = (cv, s) => {
  const liq = s.pal, mug = P.white;
  // mug body
  for (let y = 5; y <= 13; y++) for (let x = 3; x <= 10; x++) {
    let c = mug[2]; if (x <= 4) c = mug[3]; if (x >= 9 || y >= 12) c = mug[1]; px(cv, x, y, c);
  }
  // liquid surface
  fillRect(cv, 4, 5, 9, 6, liq[2]); line(cv, 4, 5, 9, 5, liq[3]);
  px(cv, 6, 6, liq[4]);
  // handle
  line(cv, 11, 6, 12, 7, mug[2]); line(cv, 12, 7, 12, 9, mug[2]); line(cv, 11, 10, 12, 9, mug[2]);
  // steam
  px(cv, 5, 3, P.white[4]); px(cv, 6, 2, P.white[3]); px(cv, 8, 3, P.white[4]); px(cv, 8, 2, P.white[3]);
  px(cv, 4, 6, mug[4]);
  if (s.topping) { px(cv, 6, 5, s.topping); px(cv, 8, 5, s.topping); } // marshmallows / froth
};

// ----------------------------------------------------------------------------
// SPECS — id -> { fn, palette fields }.  All ids are prefixed food_.
// ----------------------------------------------------------------------------
function W(fn, opts) { return Object.assign({ fn }, opts || {}); }

const FOOD_SPECS = {
  // fruits
  food_strawberry: W('strawberry'),
  food_blueberries: W('cluster', { pal: P.berry }),
  food_grapes: W('cluster', { pal: P.grape, balls: [[7, 5], [9, 6], [6, 8], [10, 8], [8, 9], [7, 11], [9, 11]] }),
  food_orange: W('fruit', { pal: P.orange }),
  food_peach: W('fruit', { pal: P.peach }),
  food_banana: W('banana'),
  food_mango: W('fruit', { pal: P.orange, leaf: true }),
  food_lemon: W('citrus', { pal: P.yellow }),
  food_pear: W('pear'),
  food_cherries: W('cluster', { pal: P.red, balls: [[6, 9], [10, 9]] }),
  food_pineapple: W('pineapple'),
  food_coconut: W('coconut'),

  // vegetables & crops
  food_tomato: W('bulb', { pal: P.red, crown: true }),
  food_corn: W('corn'),
  food_lettuce: W('leafy'),
  food_onion: W('bulb', { pal: P.tan, papery: true }),
  food_cucumber: W('cucumber'),
  food_pumpkin_wedge: W('pumpkinWedge'),
  food_chili_pepper: W('chili'),
  food_garlic: W('bulb', { pal: P.white }),

  // staples
  food_rice_bowl: W('bowl', { pal: P.cream, garnish: P.leaf[2] }),
  food_bread_roll: W('bread'),
  food_cheese_wheel: W('cheese'),

  // meals
  food_roasted_chicken_meal: W('meat'),
  food_beef_stew: W('bowl', { pal: P.cooked, garnish: P.orange[3] }),
  food_grilled_steak: W('steak'),
  food_fish_taco: W('taco'),
  food_veggie_salad: W('salad'),
  food_cheese_pizza_slice: W('pizza'),
  food_burger: W('burger'),
  food_noodle_soup: W('bowl', { pal: P.yellow, garnish: P.leaf[2] }),
  food_sushi_roll: W('sushi'),
  food_mushroom_risotto: W('bowl', { pal: P.tan, garnish: P.brown[1] }),

  // desserts
  food_chocolate_bar: W('chocolate'),
  food_ice_cream: W('iceCream', { pal: P.pink }),
  food_apple_pie_slice: W('pie', { pal: P.red }),
  food_glazed_donut: W('donut', { pal: P.pink }),
  food_cookie_jar: W('jar'),
  food_caramel: W('caramel'),

  // drinks
  food_apple_juice: W('glass', { pal: P.juice, straw: P.red[2] }),
  food_lemonade: W('glass', { pal: P.lime, straw: P.green[2] }),
  food_coffee: W('mug', { pal: P.coffee }),
  food_milkshake: W('glass', { pal: P.cream, straw: P.red[2], foam: true }),
  food_berry_smoothie: W('glass', { pal: P.grape, straw: P.green[2], foam: true }),
  food_hot_cocoa: W('mug', { pal: P.cocoa, topping: P.cream[4] }),
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
function itemModel(id) { return { parent: 'minecraft:item/generated', textures: { layer0: `politicalserver:item/${id}` } }; }
function itemDef(id) { return { model: { type: 'minecraft:model', model: `politicalserver:item/${id}` } }; }

async function generate() {
  for (const d of [ITEM_TEX, ITEM_MODELS, ITEM_DEFS]) fs.mkdirSync(d, { recursive: true });
  let n = 0;
  for (const [id, spec] of Object.entries(FOOD_SPECS)) {
    const cv = canvas();
    const fn = draw[spec.fn];
    if (!fn) { console.warn('no draw fn for', spec.fn); continue; }
    fn(cv, spec);
    const ref = spec.pal || P.brown;
    finishSprite(cv, spec);
    await writePng(cv, ITEM_TEX, id);
    writeJSON(path.join(ITEM_MODELS, `${id}.json`), itemModel(id));
    writeJSON(path.join(ITEM_DEFS, `${id}.json`), itemDef(id));
    n++;
  }
  console.log('food items:', n);
}

async function buildPreview() {
  fs.mkdirSync(PREVIEW_DIR, { recursive: true });
  const ids = Object.keys(FOOD_SPECS);
  const scale = 8, cols = 8, pad = 4, cell = N * scale + pad;
  const rows = Math.ceil(ids.length / cols);
  const sheet = new Jimp({ width: cols * cell + pad, height: rows * cell + pad, color: 0x2b2f3aff });
  for (let i = 0; i < ids.length; i++) {
    try {
      const img = await Jimp.read(path.join(ITEM_TEX, `${ids[i]}.png`));
      img.resize({ w: N * scale, h: N * scale, mode: 'nearestNeighbor' });
      sheet.composite(img, pad + (i % cols) * cell, pad + Math.floor(i / cols) * cell);
    } catch (e) { /* skip */ }
  }
  await sheet.write(path.join(PREVIEW_DIR, 'food_montage.png'));
  console.log('preview ->', path.join(PREVIEW_DIR, 'food_montage.png'));
}

async function main() {
  await generate();
  await buildPreview();
  console.log('done.');
}

main().catch(e => { console.error(e); process.exit(1); });
