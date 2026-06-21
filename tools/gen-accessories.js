/**
 * High-quality 16x16 pixel-art texture + model generator for the `accessories` expansion
 * (talismans / rings / amulets / charms / totems / artifacts / relics) and its consumables
 * (potions / elixirs / vials / foods / scrolls).
 *
 * Mirrors tools/generate-textures.js' style goals (near-black silhouette outline, 5-tone
 * shading ramps, specular highlights, faceted gems) so the new items sit beside the
 * existing Skyblock-grade art. Studied Prominence II accessory art + Croptopia food art
 * as quality references.
 *
 * Writes ONLY `acc_*` item textures, item models and item-definition files — it never
 * touches any other agent's assets.
 *
 * Usage: node gen-accessories.js
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
// Canvas helpers
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
function disc(cv, cx, cy, r, c) {
  for (let y = -r; y <= r; y++) for (let x = -r; x <= r; x++)
    if (x * x + y * y <= r * r + r * 0.4) px(cv, cx + x, cy + y, c);
}
function ring(cv, cx, cy, rx, ry, c) {
  for (let a = 0; a < 360; a += 12) { px(cv, cx + Math.round(rx * Math.cos(a * Math.PI / 180)), cy + Math.round(ry * Math.sin(a * Math.PI / 180)), c); }
}
function darken(c, f) { return [Math.round(c[0] * f), Math.round(c[1] * f), Math.round(c[2] * f), c[3] == null ? 255 : c[3]]; }
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
  iron: [[38, 40, 48], [92, 98, 110], [150, 156, 168], [200, 206, 216], [240, 244, 250]],
  steel: [[28, 40, 58], [70, 96, 128], [120, 156, 196], [176, 206, 232], [228, 244, 255]],
  diamond: [[28, 86, 92], [44, 150, 150], [92, 206, 200], [160, 236, 230], [224, 255, 252]],
  gold: [[96, 60, 12], [168, 118, 24], [226, 176, 40], [246, 214, 96], [255, 248, 188]],
  netherite: [[18, 16, 20], [42, 38, 46], [78, 70, 82], [116, 106, 120], [156, 146, 160]],
  darkiron: [[22, 24, 30], [52, 56, 66], [92, 98, 112], [140, 146, 160], [190, 196, 208]],
  bone: [[92, 84, 64], [150, 140, 110], [200, 190, 158], [230, 224, 200], [252, 250, 238]],
  wood: [[40, 26, 14], [74, 48, 26], [108, 74, 42], [140, 100, 60], [170, 130, 86]],
  leather: [[48, 30, 18], [86, 54, 30], [120, 80, 46], [156, 110, 72], [190, 146, 104]],
  cursed: [[26, 8, 38], [66, 24, 98], [120, 52, 168], [176, 108, 224], [226, 190, 255]],
  void: [[14, 6, 26], [44, 20, 70], [88, 44, 126], [140, 86, 186], [198, 158, 232]],
  crimson: [[48, 8, 14], [112, 22, 30], [176, 42, 48], [222, 88, 82], [252, 168, 150]],
  ember: [[64, 18, 8], [150, 54, 16], [224, 104, 28], [248, 168, 58], [255, 228, 150]],
  frost: [[26, 56, 86], [44, 112, 158], [96, 178, 220], [170, 224, 242], [230, 252, 255]],
  poison: [[18, 48, 12], [42, 104, 26], [92, 168, 44], [150, 214, 76], [214, 250, 150]],
  thunder: [[78, 62, 8], [170, 142, 22], [230, 202, 46], [250, 232, 116], [255, 250, 200]],
  aqua: [[14, 56, 72], [26, 112, 144], [60, 178, 198], [140, 222, 234], [214, 252, 255]],
  emerald: [[10, 58, 34], [22, 124, 70], [44, 184, 108], [120, 224, 160], [200, 250, 222]],
  lapis: [[14, 28, 82], [26, 56, 150], [44, 98, 208], [96, 150, 238], [180, 212, 255]],
  shadow: [[8, 8, 12], [26, 26, 36], [54, 54, 70], [92, 92, 116], [140, 140, 168]],
  silver: [[44, 48, 66], [96, 104, 130], [150, 158, 186], [202, 208, 228], [244, 248, 255]],
  necro: [[14, 22, 16], [34, 56, 38], [64, 98, 64], [104, 140, 96], [150, 186, 134]],
  arcane: [[28, 12, 52], [64, 30, 118], [110, 60, 186], [160, 110, 224], [212, 176, 250]],
  soul: [[12, 28, 38], [24, 76, 96], [56, 150, 168], [128, 214, 224], [208, 250, 255]],
  blood: [[44, 6, 10], [104, 18, 22], [168, 38, 40], [214, 80, 72], [248, 158, 140]],
  copper: [[78, 38, 22], [150, 76, 44], [200, 114, 72], [230, 158, 112], [250, 200, 166]],
  paper: [[120, 104, 72], [178, 160, 120], [214, 200, 162], [238, 230, 204], [252, 248, 234]],
  obsidian: [[10, 8, 16], [30, 24, 42], [54, 46, 72], [84, 74, 108], [120, 108, 148]],
  ruby: [[60, 8, 18], [140, 22, 42], [206, 44, 72], [240, 96, 116], [255, 170, 186]],
  amethyst: [[36, 16, 60], [84, 40, 134], [140, 76, 196], [186, 128, 228], [230, 196, 252]],
  jade: [[10, 52, 36], [24, 110, 76], [48, 168, 118], [120, 216, 172], [200, 248, 222]],
  stone: [[58, 58, 64], [90, 90, 98], [122, 122, 132], [156, 156, 166], [188, 188, 198]],
  // extra food palettes (Croptopia-style warmth)
  bread: [[96, 56, 22], [150, 96, 40], [196, 138, 70], [224, 176, 110], [244, 214, 160]],
  meat: [[72, 22, 20], [128, 52, 40], [176, 84, 64], [210, 128, 100], [238, 178, 150]],
  soup: [[96, 52, 18], [150, 92, 34], [196, 138, 58], [220, 172, 92], [240, 206, 140]],
  bowl: [[96, 88, 76], [150, 138, 118], [196, 184, 160], [224, 214, 190], [244, 238, 220]],
};
const WHITE = [255, 255, 255];

// ----------------------------------------------------------------------------
// Draw functions
// ----------------------------------------------------------------------------
const draw = {};

draw.ring = (cv, s) => {
  const m = s.metal || P.gold, gem = s.gem || P.ruby;
  for (let y = -4; y <= 4; y++) for (let x = -4; x <= 4; x++) {
    const d = x * x + y * y;
    if (d <= 18 && d >= 5) { let c = m[2]; if (x < 0) c = m[3]; if (x > 1 || y > 2) c = m[1]; px(cv, 8 + x, 10 + y, c); }
  }
  disc(cv, 8, 4, 2, gem[2]);
  px(cv, 7, 3, gem[4]); px(cv, 9, 5, gem[1]); px(cv, 8, 4, gem[3]);
  px(cv, 5, 9, m[4]);
};

draw.band = (cv, s) => {
  const m = s.metal || P.gold, gem = s.gem;
  for (let y = -4; y <= 4; y++) for (let x = -4; x <= 4; x++) {
    const d = x * x + y * y;
    if (d <= 20 && d >= 5) { let c = m[2]; if (x < 0) c = m[3]; if (x > 1 || y > 2) c = m[1]; px(cv, 8 + x, 9 + y, c); }
  }
  if (gem) { px(cv, 8, 4, gem[3]); px(cv, 8, 5, gem[2]); px(cv, 7, 4, gem[4]); }
  px(cv, 5, 8, m[4]);
};

draw.amulet = (cv, s) => {
  const m = s.metal || P.gold, gem = s.gem || P.lapis;
  for (let i = 0; i < 5; i++) { px(cv, 3 + i, 2 + i, m[3]); px(cv, 13 - i, 2 + i, m[3]); px(cv, 3 + i, 3 + i, m[1]); px(cv, 13 - i, 3 + i, m[1]); }
  disc(cv, 8, 10, 4, m[2]);
  ring(cv, 8, 10, 4, 4, m[1]);
  disc(cv, 8, 10, 2, gem[2]);
  px(cv, 7, 9, gem[4]); px(cv, 9, 11, gem[1]); px(cv, 8, 10, gem[3]);
  px(cv, 5, 8, m[4]);
};

draw.talisman = (cv, s) => {
  const m = s.metal || P.gold, ac = s.accent || P.crimson, gem = s.gem;
  px(cv, 8, 1, m[3]); px(cv, 7, 2, m[2]); px(cv, 9, 2, m[2]); px(cv, 8, 2, m[1]);
  disc(cv, 8, 9, 5, m[1]);
  disc(cv, 8, 9, 4, m[2]);
  px(cv, 5, 7, m[3]); px(cv, 6, 6, m[3]); px(cv, 6, 7, m[3]); px(cv, 5, 8, m[3]);
  const e = gem ? gem[3] : ac[3];
  line(cv, 8, 6, 8, 12, e); line(cv, 5, 9, 11, 9, e);
  px(cv, 8, 9, gem ? gem[4] : ac[4]);
  px(cv, 5, 7, m[4]);
};

draw.charm = (cv, s) => {
  const m = s.metal || P.silver, gem = s.gem || P.amethyst;
  ring(cv, 8, 3, 2, 2, m[2]); px(cv, 8, 1, m[3]);
  fillRect(cv, 6, 5, 9, 6, m[2]); px(cv, 6, 5, m[3]); px(cv, 9, 6, m[1]);
  for (let y = 7; y <= 14; y++) { const w = Math.max(1, 4 - Math.abs(y - 10)); for (let x = 8 - w; x <= 8 + w; x++) { let c = gem[2]; if (x < 8) c = gem[3]; if (x > 8 || y >= 12) c = gem[1]; px(cv, x, y, c); } }
  px(cv, 7, 8, gem[4]); px(cv, 6, 9, WHITE);
};

draw.feather = (cv, s) => {
  const m = s.metal || P.silver;
  for (let i = 0; i < 12; i++) { px(cv, 4 + i, 14 - i, m[2]); px(cv, 4 + i, 15 - i, m[1]); }
  for (let i = 2; i < 11; i++) { const x = 4 + i, y = 14 - i; line(cv, x, y, x - 2, y - 2, m[3]); }
  px(cv, 15, 2, m[4]); px(cv, 14, 3, m[3]); px(cv, 13, 4, m[4]);
};

draw.clover = (cv, s) => {
  const g = s.gem || P.emerald;
  disc(cv, 6, 6, 2, g[2]); disc(cv, 10, 6, 2, g[2]); disc(cv, 6, 10, 2, g[2]); disc(cv, 10, 10, 2, g[2]);
  px(cv, 8, 8, g[1]);
  px(cv, 5, 5, g[3]); px(cv, 9, 5, g[3]); px(cv, 5, 9, g[3]); px(cv, 9, 9, g[3]); px(cv, 5, 5, g[4]);
  for (let y = 10; y <= 14; y++) px(cv, 9 + (y - 10), y, P.necro[2]);
};

draw.lantern = (cv, s) => {
  const m = s.metal || P.darkiron, glow = s.gem || P.soul;
  px(cv, 8, 1, m[3]); px(cv, 7, 2, m[2]); px(cv, 9, 2, m[2]);
  fillRect(cv, 5, 3, 10, 4, m[2]); line(cv, 5, 3, 10, 3, m[3]);
  for (let y = 5; y <= 11; y++) for (let x = 6; x <= 9; x++) { const d = Math.abs(x - 7.5) + Math.abs(y - 8); let c = glow[2]; if (d < 2) c = glow[4]; else if (d < 3.5) c = glow[3]; px(cv, x, y, c); }
  for (let y = 5; y <= 12; y++) { px(cv, 5, y, m[2]); px(cv, 10, y, m[1]); }
  px(cv, 7, 6, m[1]); px(cv, 8, 9, m[1]);
  fillRect(cv, 5, 12, 10, 13, m[2]); line(cv, 5, 13, 10, 13, m[1]);
};

draw.totem = (cv, s) => {
  const m = s.metal || P.stone, ac = s.accent || P.gold;
  fillRect(cv, 5, 4, 10, 14, m[2]);
  line(cv, 5, 4, 5, 14, m[3]); line(cv, 10, 4, 10, 14, m[1]);
  fillRect(cv, 4, 3, 11, 6, m[2]); line(cv, 4, 3, 11, 3, m[3]); px(cv, 11, 6, m[1]);
  px(cv, 6, 5, m[0]); px(cv, 9, 5, m[0]); line(cv, 6, 8, 9, 8, m[0]);
  px(cv, 7, 10, ac[3]); px(cv, 8, 10, ac[2]); px(cv, 7, 11, ac[2]); px(cv, 8, 11, ac[1]);
  px(cv, 5, 4, m[4]);
};

draw.artifact = (cv, s) => {
  const m = s.metal || P.gold, gem = s.gem || P.arcane;
  disc(cv, 8, 7, 4, gem[2]);
  disc(cv, 8, 7, 3, gem[3]);
  px(cv, 6, 5, gem[4]); px(cv, 7, 6, WHITE); px(cv, 10, 9, gem[1]);
  ring(cv, 8, 7, 5, 4, m[2]);
  fillRect(cv, 5, 12, 11, 13, m[2]); fillRect(cv, 6, 13, 10, 14, m[1]); line(cv, 5, 12, 11, 12, m[3]);
  px(cv, 8, 11, m[3]);
};

draw.relic = (cv, s) => {
  const m = s.metal || P.gold, gem = s.gem || P.void;
  for (let y = 2; y <= 14; y++) for (let x = 2; x <= 14; x++) { if (Math.abs(x - 8) + Math.abs(y - 8) <= 6) { let c = m[2]; if (x < 8) c = m[3]; if (x > 8 || y > 8) c = m[1]; px(cv, x, y, c); } }
  for (let y = 5; y <= 11; y++) for (let x = 5; x <= 11; x++) { if (Math.abs(x - 8) + Math.abs(y - 8) <= 3) { let c = gem[2]; if (x < 8) c = gem[3]; if (x > 8) c = gem[1]; px(cv, x, y, c); } }
  px(cv, 7, 6, gem[4]); px(cv, 6, 6, WHITE);
  px(cv, 4, 6, m[4]); px(cv, 8, 2, m[3]); px(cv, 8, 14, m[1]);
};

draw.potion = (cv, s) => {
  const liq = s.liquid || P.crimson;
  const glass = [196, 214, 220];
  fillRect(cv, 7, 1, 8, 2, P.wood[2]); px(cv, 7, 1, P.wood[3]);
  fillRect(cv, 7, 3, 8, 5, glass);
  disc(cv, 7, 10, 4, glass);
  for (let y = 8; y <= 13; y++) for (let x = 3; x <= 11; x++) {
    if ((x - 7) * (x - 7) + (y - 10) * (y - 10) <= 11) { let c = liq[2]; if (y >= 12) c = liq[1]; if (y === 8) c = liq[3]; px(cv, x, y, c); }
  }
  line(cv, 5, 8, 9, 8, liq[3]);
  px(cv, 4, 9, WHITE); px(cv, 4, 10, [230, 240, 245]); px(cv, 5, 12, liq[4]);
};

draw.vial = (cv, s) => {
  const liq = s.liquid || P.cursed;
  const glass = [196, 214, 220];
  fillRect(cv, 6, 1, 8, 2, P.wood[2]); px(cv, 6, 1, P.wood[3]);
  fillRect(cv, 6, 3, 8, 13, glass);
  for (let y = 7; y <= 12; y++) for (let x = 6; x <= 8; x++) { let c = liq[2]; if (y >= 11) c = liq[1]; if (y === 7) c = liq[3]; px(cv, x, y, c); }
  px(cv, 6, 13, liq[1]); px(cv, 7, 13, liq[1]); px(cv, 8, 13, liq[1]);
  px(cv, 6, 5, WHITE); px(cv, 7, 9, liq[4]);
};

draw.scroll = (cv, s) => {
  const p = P.paper, rod = s.metal || P.wood, ru = s.accent || P.crimson;
  fillRect(cv, 3, 2, 12, 3, rod[2]); line(cv, 3, 2, 12, 2, rod[3]); px(cv, 3, 3, rod[1]);
  fillRect(cv, 3, 12, 12, 13, rod[2]); line(cv, 3, 13, 12, 13, rod[1]);
  fillRect(cv, 4, 4, 11, 11, p[2]);
  line(cv, 4, 4, 4, 11, p[3]); line(cv, 11, 4, 11, 11, p[1]); line(cv, 4, 4, 11, 4, p[3]);
  for (let y = 6; y <= 10; y += 2) line(cv, 5, y, 10, y, p[0]);
  px(cv, 7, 7, ru[2]); px(cv, 8, 8, ru[3]); px(cv, 7, 9, ru[2]); px(cv, 8, 7, ru[4]);
  px(cv, 5, 5, p[4]);
};

draw.bread = (cv, s) => {
  const b = s.crust || P.bread;
  for (let y = 4; y <= 12; y++) for (let x = 2; x <= 13; x++) {
    const dx = (x - 7.5) / 6, dy = (y - 8) / 4.5;
    if (dx * dx + dy * dy <= 1) { let c = b[2]; if (y <= 5) c = b[3]; if (y >= 11) c = b[1]; if (x <= 3) c = b[3]; px(cv, x, y, c); }
  }
  line(cv, 5, 6, 6, 5, b[4]); line(cv, 8, 6, 9, 5, b[4]); line(cv, 11, 6, 12, 5, b[4]);
  px(cv, 4, 6, b[4]);
  if (s.accent) { px(cv, 7, 8, s.accent[3]); px(cv, 9, 9, s.accent[2]); }
};

draw.stew = (cv, s) => {
  const bowl = P.bowl, soup = s.liquid || P.soup;
  for (let y = 7; y <= 9; y++) for (let x = 3; x <= 12; x++) { const dx = (x - 7.5) / 5, dy = (y - 8) / 1.5; if (dx * dx + dy * dy <= 1) px(cv, x, y, y === 7 ? soup[3] : soup[2]); }
  for (let y = 9; y <= 13; y++) for (let x = 3; x <= 12; x++) { const dx = (x - 7.5) / 5; if (dx * dx <= 1 - (y - 9) / 6) { let c = bowl[2]; if (x <= 4) c = bowl[3]; if (x >= 11 || y >= 12) c = bowl[1]; px(cv, x, y, c); } }
  line(cv, 3, 8, 12, 8, bowl[3]); line(cv, 3, 9, 12, 9, bowl[1]);
  px(cv, 6, 8, P.meat[2]); px(cv, 9, 8, P.bread[2]); px(cv, 7, 7, soup[4]);
  px(cv, 4, 10, bowl[4]);
};

draw.drumstick = (cv, s) => {
  const meat = s.meat || P.meat, bone = P.bone;
  disc(cv, 9, 7, 4, meat[2]);
  px(cv, 7, 5, meat[3]); px(cv, 11, 9, meat[1]); px(cv, 8, 6, meat[4]); px(cv, 10, 6, meat[3]);
  for (let i = 0; i < 5; i++) { px(cv, 6 - i, 9 + i, bone[2]); px(cv, 5 - i, 9 + i, bone[3]); }
  disc(cv, 2, 13, 1, bone[3]);
  if (s.glaze) { px(cv, 8, 5, s.glaze[3]); px(cv, 10, 8, s.glaze[2]); px(cv, 7, 8, s.glaze[4]); }
};

draw.jerky = (cv, s) => {
  const meat = s.meat || P.meat;
  for (let i = 0; i < 11; i++) { for (let w = -2; w <= 2; w++) { let c = meat[2]; if (w <= -1) c = meat[3]; if (w >= 1) c = meat[1]; px(cv, 3 + i + w, 12 - i, c); } }
  px(cv, 6, 9, meat[4]); px(cv, 9, 6, meat[4]); px(cv, 5, 10, meat[0]); px(cv, 8, 7, meat[0]);
};

// ----------------------------------------------------------------------------
// SPECS — every id is an `acc_*` item from com.political.expansion.accessories
// ----------------------------------------------------------------------------
function W(fn, opts) { return Object.assign({ fn }, opts); }

const ITEM_SPECS = {
  // ---- Accessories ----
  acc_warding_talisman: W('talisman', { metal: P.steel, accent: P.frost }),
  acc_vigor_ring: W('ring', { metal: P.gold, gem: P.ruby }),
  acc_berserker_band: W('band', { metal: P.darkiron, gem: P.crimson }),
  acc_swiftness_charm: W('charm', { metal: P.silver, gem: P.frost }),
  acc_scholar_amulet: W('amulet', { metal: P.gold, gem: P.lapis }),
  acc_cursed_seal: W('talisman', { metal: P.obsidian, gem: P.cursed }),
  acc_titan_heart: W('artifact', { metal: P.darkiron, gem: P.crimson }),
  acc_assassin_ring: W('ring', { metal: P.shadow, gem: P.amethyst }),
  acc_bruiser_totem: W('totem', { metal: P.stone, accent: P.gold }),
  acc_phoenix_charm: W('charm', { metal: P.gold, gem: P.ember }),
  acc_aqua_pendant: W('amulet', { metal: P.silver, gem: P.aqua }),
  acc_owl_talisman: W('talisman', { metal: P.bone, gem: P.amethyst }),
  acc_feather_charm: W('feather', { metal: P.silver }),
  acc_miners_band: W('band', { metal: P.gold, gem: P.thunder }),
  acc_lucky_clover: W('clover', { gem: P.emerald }),
  acc_guardian_artifact: W('artifact', { metal: P.steel, gem: P.diamond }),
  acc_warlords_signet: W('ring', { metal: P.gold, gem: P.blood }),
  acc_arcane_orb: W('artifact', { metal: P.amethyst, gem: P.arcane }),
  acc_vampiric_charm: W('charm', { metal: P.darkiron, gem: P.blood }),
  acc_golem_core: W('artifact', { metal: P.copper, gem: P.emerald }),
  acc_windrunner_anklet: W('band', { metal: P.silver, gem: P.frost }),
  acc_dragon_scale: W('talisman', { metal: P.ember, gem: P.crimson }),
  acc_soul_lantern_charm: W('lantern', { metal: P.darkiron, gem: P.soul }),
  acc_executioners_emblem: W('talisman', { metal: P.darkiron, gem: P.blood }),
  acc_sentinel_aegis: W('relic', { metal: P.silver, gem: P.diamond }),
  acc_godslayer_relic: W('relic', { metal: P.gold, gem: P.void }),

  // ---- Consumables: potions / elixirs / vials ----
  acc_minor_healing_potion: W('potion', { liquid: P.crimson }),
  acc_greater_healing_potion: W('potion', { liquid: P.ruby }),
  acc_mana_potion: W('potion', { liquid: P.lapis }),
  acc_greater_mana_potion: W('potion', { liquid: P.frost }),
  acc_cursed_energy_vial: W('vial', { liquid: P.cursed }),
  acc_elixir_of_strength: W('potion', { liquid: P.blood }),
  acc_elixir_of_swiftness: W('potion', { liquid: P.frost }),
  acc_elixir_of_iron_skin: W('potion', { liquid: P.steel }),
  acc_elixir_of_haste: W('potion', { liquid: P.thunder }),
  acc_elixir_of_the_phoenix: W('potion', { liquid: P.ember }),
  acc_berserk_brew: W('potion', { liquid: P.blood }),
  acc_invisibility_draught: W('vial', { liquid: P.silver }),
  acc_night_owl_tonic: W('potion', { liquid: P.lapis }),
  acc_gills_brew: W('potion', { liquid: P.aqua }),
  acc_titan_tonic: W('potion', { liquid: P.crimson }),

  // ---- Consumables: foods ----
  acc_hearty_stew: W('stew', { liquid: P.soup }),
  acc_golden_feast: W('drumstick', { meat: P.meat, glaze: P.gold }),
  acc_spirit_bread: W('bread', { crust: P.bread, accent: P.aqua }),
  acc_cursed_jerky: W('jerky', { meat: P.crimson }),

  // ---- Consumables: scrolls ----
  acc_scroll_of_recall: W('scroll', { metal: P.wood, accent: P.arcane }),
  acc_scroll_of_blink: W('scroll', { metal: P.amethyst, accent: P.amethyst }),
  acc_scroll_of_ascension: W('scroll', { metal: P.silver, accent: P.frost }),
  acc_scroll_of_warding: W('scroll', { metal: P.gold, accent: P.emerald }),
  acc_scroll_of_haste: W('scroll', { metal: P.copper, accent: P.thunder }),
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

async function generateItems() {
  for (const [id, spec] of Object.entries(ITEM_SPECS)) {
    const cv = canvas();
    const fn = draw[spec.fn];
    if (!fn) { console.warn('no draw fn for', spec.fn); continue; }
    fn(cv, spec);
    const ref = spec.gem || spec.metal || spec.liquid || spec.crust || spec.meat || P.shadow;
    finishSprite(cv, spec);
    await writePng(cv, ITEM_TEX, id);
    writeJSON(path.join(ITEM_MODELS, `${id}.json`), itemModel(id));
    writeJSON(path.join(ITEM_DEFS, `${id}.json`), itemDef(id));
  }
  console.log('accessory items:', Object.keys(ITEM_SPECS).length);
}

async function buildPreview() {
  fs.mkdirSync(PREVIEW_DIR, { recursive: true });
  const ids = Object.keys(ITEM_SPECS);
  const scale = 8, cols = 10, pad = 4, cell = N * scale + pad;
  const rows = Math.ceil(ids.length / cols);
  const sheet = new Jimp({ width: cols * cell + pad, height: rows * cell + pad, color: 0x2b2b38ff });
  for (let i = 0; i < ids.length; i++) {
    try {
      const img = await Jimp.read(path.join(ITEM_TEX, `${ids[i]}.png`));
      img.resize({ w: N * scale, h: N * scale, mode: 'nearestNeighbor' });
      sheet.composite(img, pad + (i % cols) * cell, pad + Math.floor(i / cols) * cell);
    } catch (e) { /* skip */ }
  }
  await sheet.write(path.join(PREVIEW_DIR, 'accessories_montage.png'));
  console.log('preview ->', path.join(PREVIEW_DIR, 'accessories_montage.png'));
}

async function main() {
  for (const d of [ITEM_TEX, ITEM_MODELS, ITEM_DEFS]) fs.mkdirSync(d, { recursive: true });
  await generateItems();
  await buildPreview();
  console.log('done.');
}

main().catch(e => { console.error(e); process.exit(1); });
