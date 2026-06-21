/**
 * Texture + model + lang generator for Workstream B (CONTENT) — the artifacts-inspired ability
 * accessories (acc2_ab_*) and the original ambient creatures (meadow_stag / ridgeback_tortoise /
 * glimmermoth). Original art only; no reference assets are read.
 *
 * Outputs:
 *   - assets/politicalserver/textures/item/acc2_ab_*.png   (16x16 item sprites)
 *   - assets/politicalserver/models/item/acc2_ab_*.json     (generated item models)
 *   - assets/politicalserver/items/acc2_ab_*.json           (item model definitions)
 *   - assets/politicalserver/textures/entity/<creature>.png (creature skins, model-sized)
 *   - assets/politicalserver/lang/en_us.content.json        (NEW lang keys for A to merge)
 *
 * Usage: node tools/gen-content2.js
 */
const { Jimp } = require('jimp');
const { finishSprite } = require('./pixel-art-lib');
const path = require('path');
const fs = require('fs');

const ROOT = path.join(__dirname, '..');
const ASSETS = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'politicalserver');
const ITEM_TEX = path.join(ASSETS, 'textures', 'item');
const ITEM_MODELS = path.join(ASSETS, 'models', 'item');
const ITEM_DEFS = path.join(ASSETS, 'items');
const ENTITY_TEX = path.join(ASSETS, 'textures', 'entity');
const LANG_OUT = path.join(ASSETS, 'lang', 'en_us.content.json');
const PREVIEW_DIR = path.join(ROOT, '.texref');

// ------------------------------------------------------------------
// 16x16 pixel-art scaffolding (mirrors gen-accessories2.js primitives)
// ------------------------------------------------------------------
const N = 16;
function canvas() { return Array.from({ length: N }, () => Array.from({ length: N }, () => null)); }
function inb(x, y) { return x >= 0 && x < N && y >= 0 && y < N; }
function px(cv, x, y, c) { x = Math.round(x); y = Math.round(y); if (inb(x, y) && c) cv[y][x] = c; }
function fillRect(cv, x0, y0, x1, y1, c) { for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) px(cv, x, y, c); }
function line(cv, x0, y0, x1, y1, c) {
  x0 = Math.round(x0); y0 = Math.round(y0); x1 = Math.round(x1); y1 = Math.round(y1);
  const dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
  const sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
  let err = dx + dy;
  for (;;) { px(cv, x0, y0, c); if (x0 === x1 && y0 === y1) break; const e2 = 2 * err; if (e2 >= dy) { err += dy; x0 += sx; } if (e2 <= dx) { err += dx; y0 += sy; } }
}
function disc(cv, cx, cy, r, c) { for (let y = -r; y <= r; y++) for (let x = -r; x <= r; x++) if (x * x + y * y <= r * r + r * 0.4) px(cv, cx + x, cy + y, c); }
function ring(cv, cx, cy, rx, ry, c) { for (let a = 0; a < 360; a += 12) px(cv, cx + Math.round(rx * Math.cos(a * Math.PI / 180)), cy + Math.round(ry * Math.sin(a * Math.PI / 180)), c); }

const P = {
  gold: [[96,60,12],[168,118,24],[226,176,40],[246,214,96],[255,248,188]],
  iron: [[38,40,48],[92,98,110],[150,156,168],[200,206,216],[240,244,250]],
  silver: [[44,48,66],[96,104,130],[150,158,186],[202,208,228],[244,248,255]],
  ember: [[64,18,8],[150,54,16],[224,104,28],[248,168,58],[255,228,150]],
  stone: [[58,58,64],[90,90,98],[122,122,132],[156,156,166],[188,188,198]],
  aqua: [[14,56,72],[26,112,144],[60,178,198],[140,222,234],[214,252,255]],
  amber: [[96,52,12],[170,108,26],[226,158,44],[246,200,96],[255,236,170]],
  emerald: [[10,58,34],[22,124,70],[44,184,108],[120,224,160],[200,250,222]],
  arcane: [[28,12,52],[64,30,118],[110,60,186],[160,110,224],[212,176,250]],
  sky: [[40,86,140],[70,130,196],[120,178,234],[182,220,250],[230,248,255]],
  obsidian: [[10,8,16],[30,24,42],[54,46,72],[84,74,108],[120,108,148]],
  jade: [[16,64,44],[34,120,84],[64,176,124],[132,216,176],[206,248,224]],
};
const WHITE = [255, 255, 255];

const draw = {};
// Reuse the established silhouettes; tinted per ability for an original-but-coherent look.
draw.charm = (cv, s) => { const m = s.metal || P.silver, gem = s.gem || P.sky; ring(cv, 8, 3, 2, 2, m[2]); px(cv, 8, 1, m[3]); fillRect(cv, 6, 5, 9, 6, m[2]); for (let y = 7; y <= 14; y++) { const w = Math.max(1, 4 - Math.abs(y - 10)); for (let x = 8 - w; x <= 8 + w; x++) { let c = gem[2]; if (x < 8) c = gem[3]; if (x > 8 || y >= 12) c = gem[1]; px(cv, x, y, c); } } px(cv, 7, 8, gem[4]); px(cv, 6, 9, WHITE); };
draw.amulet = (cv, s) => { const m = s.metal || P.gold, gem = s.gem || P.aqua; for (let i = 0; i < 5; i++) { px(cv, 3+i, 2+i, m[3]); px(cv, 13-i, 2+i, m[3]); px(cv, 3+i, 3+i, m[1]); px(cv, 13-i, 3+i, m[1]); } disc(cv, 8, 10, 4, m[2]); ring(cv, 8, 10, 4, 4, m[1]); disc(cv, 8, 10, 2, gem[2]); px(cv, 7, 9, gem[4]); px(cv, 9, 11, gem[1]); px(cv, 8, 10, gem[3]); px(cv, 5, 8, m[4]); };
draw.band = (cv, s) => { const m = s.metal || P.gold, gem = s.gem; for (let y = -4; y <= 4; y++) for (let x = -4; x <= 4; x++) { const d = x*x+y*y; if (d <= 20 && d >= 5) { let c = m[2]; if (x < 0) c = m[3]; if (x > 1 || y > 2) c = m[1]; px(cv, 8+x, 9+y, c); } } if (gem) { px(cv, 8, 4, gem[3]); px(cv, 8, 5, gem[2]); px(cv, 7, 4, gem[4]); } px(cv, 5, 8, m[4]); };
draw.totem = (cv, s) => { const m = s.metal || P.stone, ac = s.accent || P.gold; fillRect(cv, 5, 4, 10, 14, m[2]); line(cv, 5, 4, 5, 14, m[3]); line(cv, 10, 4, 10, 14, m[1]); fillRect(cv, 4, 3, 11, 6, m[2]); line(cv, 4, 3, 11, 3, m[3]); px(cv, 6, 5, m[0]); px(cv, 9, 5, m[0]); line(cv, 6, 8, 9, 8, m[0]); px(cv, 7, 10, ac[3]); px(cv, 8, 10, ac[2]); px(cv, 7, 11, ac[2]); px(cv, 8, 11, ac[1]); px(cv, 5, 4, m[4]); };
draw.talisman = (cv, s) => { const m = s.metal || P.gold, ac = s.accent || P.amber, gem = s.gem; px(cv, 8, 1, m[3]); px(cv, 7, 2, m[2]); px(cv, 9, 2, m[2]); px(cv, 8, 2, m[1]); disc(cv, 8, 9, 5, m[1]); disc(cv, 8, 9, 4, m[2]); const e = gem ? gem[3] : ac[3]; line(cv, 8, 6, 8, 12, e); line(cv, 5, 9, 11, 9, e); px(cv, 8, 9, gem ? gem[4] : ac[4]); px(cv, 5, 7, m[4]); };
draw.relic = (cv, s) => { const m = s.metal || P.gold, gem = s.gem || P.jade; for (let y = 2; y <= 14; y++) for (let x = 2; x <= 14; x++) { if (Math.abs(x - 8) + Math.abs(y - 8) <= 6) { let c = m[2]; if (x < 8) c = m[3]; if (x > 8 || y > 8) c = m[1]; px(cv, x, y, c); } } for (let y = 5; y <= 11; y++) for (let x = 5; x <= 11; x++) { if (Math.abs(x - 8) + Math.abs(y - 8) <= 3) { let c = gem[2]; if (x < 8) c = gem[3]; if (x > 8) c = gem[1]; px(cv, x, y, c); } } px(cv, 7, 6, gem[4]); px(cv, 6, 6, WHITE); px(cv, 4, 6, m[4]); };
draw.rune = (cv, s) => { const m = s.metal || P.obsidian, gem = s.gem || P.sky; fillRect(cv, 4, 4, 11, 11, m[1]); line(cv, 4, 4, 4, 11, m[3]); line(cv, 11, 4, 11, 11, m[0]); line(cv, 4, 4, 11, 4, m[3]); line(cv, 4, 11, 11, 11, m[0]); line(cv, 5, 6, 10, 6, gem[3]); line(cv, 5, 10, 10, 10, gem[3]); line(cv, 7, 5, 7, 11, gem[2]); px(cv, 7, 8, gem[4]); px(cv, 8, 7, gem[4]); px(cv, 6, 9, gem[2]); };

// One entry per acc2_ab_* (id, draw fn, palette tints). Names live in the lang map below.
const ACCESSORIES = [
  { id: 'acc2_ab_aetherwing',       fn: 'charm',    metal: 'silver', gem: 'sky' },
  { id: 'acc2_ab_lodestone_locket', fn: 'amulet',   metal: 'iron',   gem: 'sky' },
  { id: 'acc2_ab_cinderheart',      fn: 'band',     metal: 'ember',  gem: 'ember' },
  { id: 'acc2_ab_bulwark_totem',    fn: 'totem',    metal: 'stone',  accent: 'iron' },
  { id: 'acc2_ab_tidecaller',       fn: 'amulet',   metal: 'silver', gem: 'aqua' },
  { id: 'acc2_ab_highstep',         fn: 'band',     metal: 'gold',   gem: 'sky' },
  { id: 'acc2_ab_owls_eye',         fn: 'talisman', metal: 'gold',   gem: 'amber' },
  { id: 'acc2_ab_verdant_idol',     fn: 'relic',    metal: 'gold',   gem: 'jade' },
  { id: 'acc2_ab_featherfall',      fn: 'rune',     metal: 'obsidian', gem: 'sky' },
];

// Lang strings (item.* + entity.* keys merged by Workstream A into en_us.json).
const LANG = {
  'item.politicalserver.acc2_ab_aetherwing': 'Aetherwing Charm',
  'item.politicalserver.acc2_ab_lodestone_locket': 'Lodestone Locket',
  'item.politicalserver.acc2_ab_cinderheart': 'Cinderheart Band',
  'item.politicalserver.acc2_ab_bulwark_totem': 'Bulwark Totem',
  'item.politicalserver.acc2_ab_tidecaller': 'Tidecaller Pendant',
  'item.politicalserver.acc2_ab_highstep': 'Highstep Anklet',
  'item.politicalserver.acc2_ab_owls_eye': "Owl's Eye Talisman",
  'item.politicalserver.acc2_ab_verdant_idol': 'Verdant Sustenance Idol',
  'item.politicalserver.acc2_ab_featherfall': 'Featherfall Sigil',
  'entity.politicalserver.meadow_stag': 'Meadow Stag',
  'entity.politicalserver.ridgeback_tortoise': 'Ridgeback Tortoise',
  'entity.politicalserver.glimmermoth': 'Glimmermoth',
  // Biome display names (used by /locate output & datapack tooling).
  'biome.politicalserver.auric_steppe': 'Auric Steppe',
  'biome.politicalserver.ashen_barrens': 'Ashen Barrens',
  'biome.politicalserver.mistveil_fen': 'Mistveil Fen',
  'biome.politicalserver.gloamwood': 'Gloamwood',
  'biome.politicalserver.frostpetal_tundra': 'Frostpetal Tundra',
};

function rgba(c) { const a = c[3] == null ? 255 : c[3]; return ((c[0] << 24) | (c[1] << 16) | (c[2] << 8) | a) >>> 0; }
function pal(name) { return P[name] || P.gold; }

async function writeCanvas(cv, dir, name) {
  const img = new Jimp({ width: N, height: N, color: 0x00000000 });
  for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) { const c = cv[y][x]; if (c) img.setPixelColor(rgba(c), x, y); }
  await img.write(path.join(dir, `${name}.png`));
}

function writeJSON(file, obj) { fs.mkdirSync(path.dirname(file), { recursive: true }); fs.writeFileSync(file, JSON.stringify(obj, null, 2) + '\n'); }
function itemModel(id) { return { parent: 'minecraft:item/generated', textures: { layer0: `politicalserver:item/${id}` } }; }
function itemDef(id) { return { model: { type: 'minecraft:model', model: `politicalserver:item/${id}` } }; }

async function generateAccessories() {
  for (const d of [ITEM_TEX, ITEM_MODELS, ITEM_DEFS]) fs.mkdirSync(d, { recursive: true });
  for (const a of ACCESSORIES) {
    const spec = { metal: a.metal ? pal(a.metal) : null, gem: a.gem ? pal(a.gem) : null, accent: a.accent ? pal(a.accent) : null };
    const cv = canvas();
    draw[a.fn](cv, spec);
    finishSprite(cv, spec);
    await writeCanvas(cv, ITEM_TEX, a.id);
    writeJSON(path.join(ITEM_MODELS, `${a.id}.json`), itemModel(a.id));
    writeJSON(path.join(ITEM_DEFS, `${a.id}.json`), itemDef(a.id));
  }
  console.log(`accessories: ${ACCESSORIES.length} sprites + models + defs`);
}

// ------------------------------------------------------------------
// Creature skins — model-sized, shaded fills (original placeholder art).
// ------------------------------------------------------------------
function shadeFill(img, w, h, base, top, accent) {
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      // top third lighter, bottom darker — readable directional shading.
      let c = base;
      if (y < h * 0.33) c = top;
      else if (y > h * 0.72) c = [Math.round(base[0]*0.7), Math.round(base[1]*0.7), Math.round(base[2]*0.7)];
      img.setPixelColor(rgba(c), x, y);
    }
  }
  // a couple of accent speckles for life
  if (accent) {
    for (let i = 0; i < Math.floor(w * h / 40); i++) {
      const x = (i * 7) % w, y = (i * 13) % h;
      img.setPixelColor(rgba(accent), x, y);
    }
  }
}

async function generateCreatures() {
  fs.mkdirSync(ENTITY_TEX, { recursive: true });
  const skins = [
    { id: 'meadow_stag',        w: 64, h: 32, base: P.amber[2],  top: P.amber[3],  accent: P.amber[4] },
    { id: 'ridgeback_tortoise', w: 64, h: 32, base: P.jade[1],   top: P.jade[2],   accent: P.emerald[3] },
    { id: 'glimmermoth',        w: 32, h: 32, base: P.arcane[2], top: P.arcane[3], accent: P.sky[4] },
  ];
  for (const s of skins) {
    const img = new Jimp({ width: s.w, height: s.h, color: 0x00000000 });
    shadeFill(img, s.w, s.h, s.base, s.top, s.accent);
    await img.write(path.join(ENTITY_TEX, `${s.id}.png`));
  }
  console.log(`creatures: ${skins.length} entity skins`);
}

function generateLang() {
  writeJSON(LANG_OUT, LANG);
  console.log('lang:', LANG_OUT);
}

async function buildPreview() {
  fs.mkdirSync(PREVIEW_DIR, { recursive: true });
  const scale = 8, cols = 9, pad = 4, cell = N * scale + pad;
  const sheet = new Jimp({ width: cols * cell + pad, height: cell + pad, color: 0x2b2b38ff });
  for (let i = 0; i < ACCESSORIES.length; i++) {
    try {
      const img = await Jimp.read(path.join(ITEM_TEX, `${ACCESSORIES[i].id}.png`));
      img.resize({ w: N * scale, h: N * scale, mode: 'nearestNeighbor' });
      sheet.composite(img, pad + i * cell, pad);
    } catch (_) { /* skip */ }
  }
  await sheet.write(path.join(PREVIEW_DIR, 'content2_accessories.png'));
}

async function main() {
  await generateAccessories();
  await generateCreatures();
  generateLang();
  await buildPreview();
  console.log('done.');
}

main().catch(e => { console.error(e); process.exit(1); });
