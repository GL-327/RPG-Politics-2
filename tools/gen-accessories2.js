/**
 * Texture + model + lang + Java catalog generator for expansion2 accessories (acc2_*).
 * Usage: node gen-accessories2.js
 */
const { Jimp } = require('jimp');
const { finishSprite } = require('./pixel-art-lib');
const path = require('path');
const fs = require('fs');
const { buildCatalog } = require('./accessories2-catalog');

const ROOT = path.join(__dirname, '..');
const ASSETS = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'politicalserver');
const ITEM_TEX = path.join(ASSETS, 'textures', 'item');
const ITEM_MODELS = path.join(ASSETS, 'models', 'item');
const ITEM_DEFS = path.join(ASSETS, 'items');
const PREVIEW_DIR = path.join(ROOT, '.texref');
const JAVA_CATALOG = path.join(ROOT, 'src', 'main', 'java', 'com', 'political', 'expansion2', 'accessories', 'Accessories2Catalog.java');
const LANG_OUT = path.join(__dirname, 'lang-fragments', 'accessories2.json');
const DOC_OUT = path.join(ROOT, 'docs', 'expansion2', 'accessories.md');

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
  for (;;) { px(cv, x0, y0, c); if (x0 === x1 && y0 === y1) break; const e2 = 2 * err; if (e2 >= dy) { err += dy; x0 += sx; } if (e2 <= dx) { err += dx; y0 += sy; } }
}
function disc(cv, cx, cy, r, c) { for (let y = -r; y <= r; y++) for (let x = -r; x <= r; x++) if (x * x + y * y <= r * r + r * 0.4) px(cv, cx + x, cy + y, c); }
function ring(cv, cx, cy, rx, ry, c) { for (let a = 0; a < 360; a += 12) px(cv, cx + Math.round(rx * Math.cos(a * Math.PI / 180)), cy + Math.round(ry * Math.sin(a * Math.PI / 180)), c); }
function darken(c, f) { return [Math.round(c[0] * f), Math.round(c[1] * f), Math.round(c[2] * f), c[3] == null ? 255 : c[3]]; }
function outline(cv, col) {
  const add = [];
  for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) {
    if (cv[y][x]) continue;
    if (get(cv, x, y - 1) || get(cv, x, y + 1) || get(cv, x - 1, y) || get(cv, x + 1, y)) add.push([x, y]);
  }
  for (const [x, y] of add) cv[y][x] = col;
}

const P = {
  iron: [[38,40,48],[92,98,110],[150,156,168],[200,206,216],[240,244,250]],
  steel: [[28,40,58],[70,96,128],[120,156,196],[176,206,232],[228,244,255]],
  diamond: [[28,86,92],[44,150,150],[92,206,200],[160,236,230],[224,255,252]],
  gold: [[96,60,12],[168,118,24],[226,176,40],[246,214,96],[255,248,188]],
  netherite: [[18,16,20],[42,38,46],[78,70,82],[116,106,120],[156,146,160]],
  darkiron: [[22,24,30],[52,56,66],[92,98,112],[140,146,160],[190,196,208]],
  bone: [[92,84,64],[150,140,110],[200,190,158],[230,224,200],[252,250,238]],
  wood: [[40,26,14],[74,48,26],[108,74,42],[140,100,60],[170,130,86]],
  cursed: [[26,8,38],[66,24,98],[120,52,168],[176,108,224],[226,190,255]],
  void: [[14,6,26],[44,20,70],[88,44,126],[140,86,186],[198,158,232]],
  crimson: [[48,8,14],[112,22,30],[176,42,48],[222,88,82],[252,168,150]],
  ember: [[64,18,8],[150,54,16],[224,104,28],[248,168,58],[255,228,150]],
  frost: [[26,56,86],[44,112,158],[96,178,220],[170,224,242],[230,252,255]],
  poison: [[18,48,12],[42,104,26],[92,168,44],[150,214,76],[214,250,150]],
  thunder: [[78,62,8],[170,142,22],[230,202,46],[250,232,116],[255,250,200]],
  aqua: [[14,56,72],[26,112,144],[60,178,198],[140,222,234],[214,252,255]],
  emerald: [[10,58,34],[22,124,70],[44,184,108],[120,224,160],[200,250,222]],
  lapis: [[14,28,82],[26,56,150],[44,98,208],[96,150,238],[180,212,255]],
  shadow: [[8,8,12],[26,26,36],[54,54,70],[92,92,116],[140,140,168]],
  silver: [[44,48,66],[96,104,130],[150,158,186],[202,208,228],[244,248,255]],
  arcane: [[28,12,52],[64,30,118],[110,60,186],[160,110,224],[212,176,250]],
  soul: [[12,28,38],[24,76,96],[56,150,168],[128,214,224],[208,250,255]],
  blood: [[44,6,10],[104,18,22],[168,38,40],[214,80,72],[248,158,140]],
  copper: [[78,38,22],[150,76,44],[200,114,72],[230,158,112],[250,200,166]],
  paper: [[120,104,72],[178,160,120],[214,200,162],[238,230,204],[252,248,234]],
  obsidian: [[10,8,16],[30,24,42],[54,46,72],[84,74,108],[120,108,148]],
  ruby: [[60,8,18],[140,22,42],[206,44,72],[240,96,116],[255,170,186]],
  amethyst: [[36,16,60],[84,40,134],[140,76,196],[186,128,228],[230,196,252]],
  stone: [[58,58,64],[90,90,98],[122,122,132],[156,156,166],[188,188,198]],
  bread: [[96,56,22],[150,96,40],[196,138,70],[224,176,110],[244,214,160]],
  meat: [[72,22,20],[128,52,40],[176,84,64],[210,128,100],[238,178,150]],
  soup: [[96,52,18],[150,92,34],[196,138,58],[220,172,92],[240,206,140]],
  bowl: [[96,88,76],[150,138,118],[196,184,160],[224,214,190],[244,238,220]],
};
const WHITE = [255,255,255];

const draw = {};
draw.ring = (cv, s) => { const m = s.metal || P.gold, gem = s.gem || P.ruby; for (let y = -4; y <= 4; y++) for (let x = -4; x <= 4; x++) { const d = x*x+y*y; if (d <= 18 && d >= 5) { let c = m[2]; if (x < 0) c = m[3]; if (x > 1 || y > 2) c = m[1]; px(cv, 8+x, 10+y, c); } } disc(cv, 8, 4, 2, gem[2]); px(cv, 7, 3, gem[4]); px(cv, 9, 5, gem[1]); px(cv, 8, 4, gem[3]); px(cv, 5, 9, m[4]); };
draw.band = (cv, s) => { const m = s.metal || P.gold, gem = s.gem; for (let y = -4; y <= 4; y++) for (let x = -4; x <= 4; x++) { const d = x*x+y*y; if (d <= 20 && d >= 5) { let c = m[2]; if (x < 0) c = m[3]; if (x > 1 || y > 2) c = m[1]; px(cv, 8+x, 9+y, c); } } if (gem) { px(cv, 8, 4, gem[3]); px(cv, 8, 5, gem[2]); px(cv, 7, 4, gem[4]); } px(cv, 5, 8, m[4]); };
draw.amulet = (cv, s) => { const m = s.metal || P.gold, gem = s.gem || P.lapis; for (let i = 0; i < 5; i++) { px(cv, 3+i, 2+i, m[3]); px(cv, 13-i, 2+i, m[3]); px(cv, 3+i, 3+i, m[1]); px(cv, 13-i, 3+i, m[1]); } disc(cv, 8, 10, 4, m[2]); ring(cv, 8, 10, 4, 4, m[1]); disc(cv, 8, 10, 2, gem[2]); px(cv, 7, 9, gem[4]); px(cv, 9, 11, gem[1]); px(cv, 8, 10, gem[3]); px(cv, 5, 8, m[4]); };
draw.talisman = (cv, s) => { const m = s.metal || P.gold, ac = s.accent || P.crimson, gem = s.gem; px(cv, 8, 1, m[3]); px(cv, 7, 2, m[2]); px(cv, 9, 2, m[2]); px(cv, 8, 2, m[1]); disc(cv, 8, 9, 5, m[1]); disc(cv, 8, 9, 4, m[2]); px(cv, 5, 7, m[3]); px(cv, 6, 6, m[3]); px(cv, 6, 7, m[3]); px(cv, 5, 8, m[3]); const e = gem ? gem[3] : ac[3]; line(cv, 8, 6, 8, 12, e); line(cv, 5, 9, 11, 9, e); px(cv, 8, 9, gem ? gem[4] : ac[4]); px(cv, 5, 7, m[4]); };
draw.charm = (cv, s) => { const m = s.metal || P.silver, gem = s.gem || P.amethyst; ring(cv, 8, 3, 2, 2, m[2]); px(cv, 8, 1, m[3]); fillRect(cv, 6, 5, 9, 6, m[2]); px(cv, 6, 5, m[3]); px(cv, 9, 6, m[1]); for (let y = 7; y <= 14; y++) { const w = Math.max(1, 4 - Math.abs(y - 10)); for (let x = 8 - w; x <= 8 + w; x++) { let c = gem[2]; if (x < 8) c = gem[3]; if (x > 8 || y >= 12) c = gem[1]; px(cv, x, y, c); } } px(cv, 7, 8, gem[4]); px(cv, 6, 9, WHITE); };
draw.totem = (cv, s) => { const m = s.metal || P.stone, ac = s.accent || P.gold; fillRect(cv, 5, 4, 10, 14, m[2]); line(cv, 5, 4, 5, 14, m[3]); line(cv, 10, 4, 10, 14, m[1]); fillRect(cv, 4, 3, 11, 6, m[2]); line(cv, 4, 3, 11, 3, m[3]); px(cv, 11, 6, m[1]); px(cv, 6, 5, m[0]); px(cv, 9, 5, m[0]); line(cv, 6, 8, 9, 8, m[0]); px(cv, 7, 10, ac[3]); px(cv, 8, 10, ac[2]); px(cv, 7, 11, ac[2]); px(cv, 8, 11, ac[1]); px(cv, 5, 4, m[4]); };
draw.artifact = (cv, s) => { const m = s.metal || P.gold, gem = s.gem || P.arcane; disc(cv, 8, 7, 4, gem[2]); disc(cv, 8, 7, 3, gem[3]); px(cv, 6, 5, gem[4]); px(cv, 7, 6, WHITE); px(cv, 10, 9, gem[1]); ring(cv, 8, 7, 5, 4, m[2]); fillRect(cv, 5, 12, 11, 13, m[2]); fillRect(cv, 6, 13, 10, 14, m[1]); line(cv, 5, 12, 11, 12, m[3]); px(cv, 8, 11, m[3]); };
draw.relic = (cv, s) => { const m = s.metal || P.gold, gem = s.gem || P.void; for (let y = 2; y <= 14; y++) for (let x = 2; x <= 14; x++) { if (Math.abs(x - 8) + Math.abs(y - 8) <= 6) { let c = m[2]; if (x < 8) c = m[3]; if (x > 8 || y > 8) c = m[1]; px(cv, x, y, c); } } for (let y = 5; y <= 11; y++) for (let x = 5; x <= 11; x++) { if (Math.abs(x - 8) + Math.abs(y - 8) <= 3) { let c = gem[2]; if (x < 8) c = gem[3]; if (x > 8) c = gem[1]; px(cv, x, y, c); } } px(cv, 7, 6, gem[4]); px(cv, 6, 6, WHITE); px(cv, 4, 6, m[4]); px(cv, 8, 2, m[3]); px(cv, 8, 14, m[1]); };
draw.badge = (cv, s) => { const m = s.metal || P.gold, gem = s.gem || P.diamond; for (let y = 3; y <= 13; y++) for (let x = 4; x <= 11; x++) { const dx = Math.abs(x - 7.5), dy = Math.abs(y - 8); if (dx + dy <= 5.5) { let c = m[2]; if (x < 7) c = m[3]; if (y < 6) c = m[3]; px(cv, x, y, c); } } disc(cv, 7, 8, 2, gem[2]); px(cv, 6, 7, gem[4]); px(cv, 8, 9, gem[1]); px(cv, 4, 5, m[4]); };
draw.rune = (cv, s) => { const m = s.metal || P.obsidian, gem = s.gem || P.arcane; fillRect(cv, 4, 4, 11, 11, m[1]); line(cv, 4, 4, 4, 11, m[3]); line(cv, 11, 4, 11, 11, m[0]); line(cv, 4, 4, 11, 4, m[3]); line(cv, 4, 11, 11, 11, m[0]); line(cv, 5, 6, 10, 6, gem[3]); line(cv, 5, 10, 10, 10, gem[3]); line(cv, 7, 5, 7, 11, gem[2]); px(cv, 7, 8, gem[4]); px(cv, 8, 7, gem[4]); px(cv, 6, 9, gem[2]); };
draw.potion = (cv, s) => { const liq = s.liquid || P.crimson; const glass = [196,214,220]; fillRect(cv, 7, 1, 8, 2, P.wood[2]); px(cv, 7, 1, P.wood[3]); fillRect(cv, 7, 3, 8, 5, glass); disc(cv, 7, 10, 4, glass); for (let y = 8; y <= 13; y++) for (let x = 3; x <= 11; x++) { if ((x-7)*(x-7)+(y-10)*(y-10) <= 11) { let c = liq[2]; if (y >= 12) c = liq[1]; if (y === 8) c = liq[3]; px(cv, x, y, c); } } line(cv, 5, 8, 9, 8, liq[3]); px(cv, 4, 9, WHITE); px(cv, 5, 12, liq[4]); };
draw.vial = (cv, s) => { const liq = s.liquid || P.cursed; const glass = [196,214,220]; fillRect(cv, 6, 1, 8, 2, P.wood[2]); fillRect(cv, 6, 3, 8, 13, glass); for (let y = 7; y <= 12; y++) for (let x = 6; x <= 8; x++) { let c = liq[2]; if (y >= 11) c = liq[1]; if (y === 7) c = liq[3]; px(cv, x, y, c); } px(cv, 6, 5, WHITE); px(cv, 7, 9, liq[4]); };
draw.scroll = (cv, s) => { const p = P.paper, rod = s.metal || P.wood, ru = s.accent || P.crimson; fillRect(cv, 3, 2, 12, 3, rod[2]); line(cv, 3, 2, 12, 2, rod[3]); fillRect(cv, 3, 12, 12, 13, rod[2]); line(cv, 3, 13, 12, 13, rod[1]); fillRect(cv, 4, 4, 11, 11, p[2]); line(cv, 4, 4, 4, 11, p[3]); line(cv, 11, 4, 11, 11, p[1]); for (let y = 6; y <= 10; y += 2) line(cv, 5, y, 10, y, p[0]); px(cv, 7, 7, ru[2]); px(cv, 8, 8, ru[3]); px(cv, 7, 9, ru[2]); px(cv, 5, 5, p[4]); };
draw.bread = (cv, s) => { const b = s.crust || P.bread; for (let y = 4; y <= 12; y++) for (let x = 2; x <= 13; x++) { const dx = (x-7.5)/6, dy = (y-8)/4.5; if (dx*dx+dy*dy <= 1) { let c = b[2]; if (y <= 5) c = b[3]; if (y >= 11) c = b[1]; px(cv, x, y, c); } } line(cv, 5, 6, 6, 5, b[4]); if (s.accent) { px(cv, 7, 8, s.accent[3]); px(cv, 9, 9, s.accent[2]); } };
draw.stew = (cv, s) => { const bowl = P.bowl, soup = s.liquid || P.soup; for (let y = 7; y <= 9; y++) for (let x = 3; x <= 12; x++) { const dx = (x-7.5)/5, dy = (y-8)/1.5; if (dx*dx+dy*dy <= 1) px(cv, x, y, y === 7 ? soup[3] : soup[2]); } for (let y = 9; y <= 13; y++) for (let x = 3; x <= 12; x++) { const dx = (x-7.5)/5; if (dx*dx <= 1 - (y-9)/6) { let c = bowl[2]; if (x <= 4) c = bowl[3]; if (x >= 11 || y >= 12) c = bowl[1]; px(cv, x, y, c); } } px(cv, 6, 8, P.meat[2]); px(cv, 4, 10, bowl[4]); };
draw.drumstick = (cv, s) => { const meat = s.meat || P.meat, bone = P.bone; disc(cv, 9, 7, 4, meat[2]); for (let i = 0; i < 5; i++) { px(cv, 6-i, 9+i, bone[2]); px(cv, 5-i, 9+i, bone[3]); } disc(cv, 2, 13, 1, bone[3]); if (s.glaze) { px(cv, 8, 5, s.glaze[3]); px(cv, 10, 8, s.glaze[2]); } };
draw.jerky = (cv, s) => { const meat = s.meat || P.meat; for (let i = 0; i < 11; i++) for (let w = -2; w <= 2; w++) { let c = meat[2]; if (w <= -1) c = meat[3]; if (w >= 1) c = meat[1]; px(cv, 3+i+w, 12-i, c); } px(cv, 6, 9, meat[4]); };
draw.bomb = (cv, s) => { const liq = s.liquid || P.crimson; disc(cv, 8, 9, 4, liq[2]); disc(cv, 8, 9, 3, liq[3]); px(cv, 6, 7, liq[4]); px(cv, 8, 3, P.shadow[3]); line(cv, 8, 3, 8, 5, P.shadow[2]); px(cv, 9, 2, P.thunder[4]); px(cv, 7, 2, P.thunder[3]); px(cv, 10, 10, liq[1]); };

function resolvePalette(tex) {
  const out = {};
  for (const [k, v] of Object.entries(tex)) {
    if (k === 'fn' || k === 'draw') continue;
    out[k] = typeof v === 'string' ? P[v] : v;
  }
  out.fn = tex.fn || tex.draw;
  return out;
}

const MOB = {
  RESISTANCE: 'MobEffects.RESISTANCE', REGENERATION: 'MobEffects.REGENERATION', STRENGTH: 'MobEffects.STRENGTH',
  SPEED: 'MobEffects.SPEED', NIGHT_VISION: 'MobEffects.NIGHT_VISION', DARKNESS: 'MobEffects.DARKNESS',
  INVISIBILITY: 'MobEffects.INVISIBILITY', FIRE_RESISTANCE: 'MobEffects.FIRE_RESISTANCE', SLOW_FALLING: 'MobEffects.SLOW_FALLING',
  JUMP_BOOST: 'MobEffects.JUMP_BOOST', WATER_BREATHING: 'MobEffects.WATER_BREATHING', DOLPHINS_GRACE: 'MobEffects.DOLPHINS_GRACE',
  HASTE: 'MobEffects.HASTE', HEALTH_BOOST: 'MobEffects.HEALTH_BOOST', ABSORPTION: 'MobEffects.ABSORPTION',
  POISON: 'MobEffects.POISON', LEVITATION: 'MobEffects.LEVITATION', SATURATION: 'MobEffects.SATURATION',
  WEAKNESS: 'MobEffects.WEAKNESS', WITHER: 'MobEffects.WITHER', LUCK: 'MobEffects.LUCK',
};

const CHAT = {
  RED: 'ChatFormatting.RED', AQUA: 'ChatFormatting.AQUA', DARK_PURPLE: 'ChatFormatting.DARK_PURPLE',
  WHITE: 'ChatFormatting.WHITE', GREEN: 'ChatFormatting.GREEN', GOLD: 'ChatFormatting.GOLD',
  DARK_RED: 'ChatFormatting.DARK_RED', GRAY: 'ChatFormatting.GRAY', BLUE: 'ChatFormatting.BLUE',
  LIGHT_PURPLE: 'ChatFormatting.LIGHT_PURPLE', YELLOW: 'ChatFormatting.YELLOW',
};

function jesc(s) { return s.replace(/\\/g, '\\\\').replace(/"/g, '\\"'); }
function fmtNum(n) {
  if (Number.isInteger(n)) return String(n);
  const r = Math.round(n * 10) / 10;
  return r === Math.floor(r) ? String(Math.floor(r)) : String(r);
}

function bonusJava(stats, effects) {
  const parts = ['AccessoryDef2.bonus()'];
  const map = { health: 'health', defense: 'defense', strength: 'strength', toughness: 'toughness',
    knockback: 'knockback', speed: 'speed', attackSpeed: 'attackSpeed', luck: 'luck',
    critChance: 'critChance', critDamage: 'critDamage', ferocity: 'ferocity', manaRegen: 'manaRegen', cursedRegen: 'cursedRegen' };
  for (const [k, m] of Object.entries(map)) {
    const v = stats[k];
    if (v && v !== 0) parts.push(`.${m}(${fmtNum(v)})`);
  }
  for (const e of effects || []) {
    parts.push(`.effect(${MOB[e.effect]}, ${e.amp}, "${jesc(e.label)}")`);
  }
  return parts.join('');
}

function actionJava(item) {
  const a = item.action;
  switch (a.kind) {
    case 'HEAL_FRAC': {
      let code = `sp -> { Accessories2.healFrac(sp, ${a.value})`;
      if (a.extra) {
        for (const e of a.extra) code += `; Accessories2.buff(sp, ${MOB[e.effect]}, ${e.sec}, ${e.amp})`;
        code += '; return true; }';
      } else code = `sp -> Accessories2.healFrac(sp, ${a.value})`;
      return code;
    }
    case 'MANA_FRAC': {
      if (a.extra) {
        let code = `sp -> { Accessories2.restoreMana(sp, ${a.value})`;
        for (const e of a.extra) code += `; Accessories2.buff(sp, ${MOB[e.effect]}, ${e.sec}, ${e.amp})`;
        return code + '; return true; }';
      }
      return `sp -> Accessories2.restoreMana(sp, ${a.value})`;
    }
    case 'CURSED_FRAC': {
      if (a.extra) {
        let code = `sp -> { if (!Accessories2.restoreCursed(sp, ${a.value})) return false`;
        for (const e of a.extra) code += `; Accessories2.buff(sp, ${MOB[e.effect]}, ${e.sec}, ${e.amp})`;
        return code + '; return true; }';
      }
      return `sp -> Accessories2.restoreCursed(sp, ${a.value})`;
    }
    case 'TRIPLE_RESTORE': return 'Accessories2::tripleRestore';
    case 'CLEAR_POISON': return 'Accessories2::clearPoison';
    case 'RECALL': return 'Accessories2::recallToSpawn';
    case 'ASCEND': return 'Accessories2::ascendToSurface';
    case 'BLINK': return `sp -> Accessories2.blinkForward(sp, ${a.value})`;
    case 'BUFF': {
      const lines = a.effects.map(e => `Accessories2.buff(sp, ${MOB[e.effect]}, ${e.sec}, ${e.amp})`);
      return `sp -> { ${lines.join('; ')}; return true; }`;
    }
    case 'FOOD_HEAL': return `sp -> Accessories2.foodHeal(sp, ${a.heal}, ${a.saturation})`;
    case 'FOOD_GOLDEN': return 'Accessories2::foodGolden';
    case 'FOOD_MANA': return `sp -> Accessories2.foodMana(sp, ${a.mana}, ${a.saturation})`;
    case 'FOOD_CURSED': return `sp -> Accessories2.foodCursed(sp, ${a.cursed}, ${a.saturation})`;
    case 'FOOD_TRIPLE': return 'Accessories2::foodTriple';
    case 'FOOD_VOID': return 'Accessories2::foodVoid';
    case 'FOOD_BUFF': {
      let code = `sp -> { Accessories2.foodHeal(sp, ${a.heal || 0.1}, ${a.saturation || 2})`;
      for (const e of (a.effects || [])) code += `; Accessories2.buff(sp, ${MOB[e.effect]}, ${e.sec}, ${e.amp})`;
      return code + '; return true; }';
    }
    case 'BOMB_FLASH': return `sp -> Accessories2.bombFlash(sp, ${a.radius})`;
    case 'BOMB_FIRE': return `sp -> Accessories2.bombFire(sp, ${a.radius})`;
    case 'BOMB_FROST': return `sp -> Accessories2.bombFrost(sp, ${a.radius})`;
    case 'BOMB_POISON': return `sp -> Accessories2.bombPoison(sp, ${a.radius})`;
    case 'BOMB_SMOKE': return `sp -> Accessories2.bombSmoke(sp, ${a.radius})`;
    case 'BOMB_HOLY': return `sp -> Accessories2.bombHoly(sp, ${a.radius})`;
    case 'BOMB_VOID': return `sp -> Accessories2.bombVoid(sp, ${a.radius})`;
    case 'BOMB_GRAVITY': return `sp -> Accessories2.bombGravity(sp, ${a.radius})`;
    case 'BOMB_SHOCK': return `sp -> Accessories2.bombShock(sp, ${a.radius})`;
    case 'BOMB_HEAL': return `sp -> Accessories2.bombHeal(sp, ${a.radius})`;
    case 'BOMB_CURSED': return `sp -> Accessories2.bombCursed(sp, ${a.radius})`;
    default: throw new Error('unknown action ' + a.kind);
  }
}

function generateJava(catalog) {
  const accLines = catalog.filter(x => x.kind === 'accessory').map(item =>
    `        Accessories2.acc(out, "${item.id}", "${jesc(item.name)}", AccessoryDef2.Type.${item.type}, Rarity.${item.rarity},\n` +
    `                "${jesc(item.flavor)}",\n                ${bonusJava(item.stats, item.effects)});`);
  const conLines = catalog.filter(x => x.kind === 'consumable').map(item =>
    `        Accessories2.con(out, "${item.id}", "${jesc(item.name)}", ConsumableDef2.Type.${item.type}, Rarity.${item.rarity},\n` +
    `                "${jesc(item.flavor)}", "${jesc(item.successMsg)}", ${CHAT[item.msgColor]}, ${item.stackSize},\n                ${actionJava(item)});`);

  const src = `package com.political.expansion2.accessories;

import com.political.items.Rarity;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffects;

import java.util.List;

/** Generated by tools/gen-accessories2.js — do not edit by hand. */
final class Accessories2Catalog {

    private Accessories2Catalog() {}

    static void buildAccessories(List<AccessoryDef2> out) {
${accLines.join('\n')}
    }

    static void buildConsumables(List<ConsumableDef2> out) {
${conLines.join('\n')}
    }
}
`;
  fs.mkdirSync(path.dirname(JAVA_CATALOG), { recursive: true });
  fs.writeFileSync(JAVA_CATALOG, src);
}

function generateLang(catalog) {
  const lang = {};
  for (const item of catalog) lang[`item.politicalserver.${item.id}`] = item.name;
  fs.mkdirSync(path.dirname(LANG_OUT), { recursive: true });
  fs.writeFileSync(LANG_OUT, JSON.stringify(lang, null, 2) + '\n');
}

function statSummary(stats) {
  const bits = [];
  if (stats.health) bits.push(`+${stats.health} Health`);
  if (stats.defense) bits.push(`+${stats.defense} Defense`);
  if (stats.strength) bits.push(`+${stats.strength} Strength`);
  if (stats.manaRegen) bits.push(`+${stats.manaRegen} Mana/s`);
  if (stats.cursedRegen) bits.push(`+${stats.cursedRegen} Cursed/s`);
  if (stats.critChance) bits.push(`+${stats.critChance}% Crit`);
  return bits.slice(0, 3).join(', ') || itemFlavor(stats);
}

function generateDocs(catalog) {
  const acc = catalog.filter(x => x.kind === 'accessory');
  const con = catalog.filter(x => x.kind === 'consumable');
  const accRows = acc.map(a => `| \`${a.id}\` | ${a.type} | ${a.rarity} | ${a.flavor.slice(0, 60)}${a.flavor.length > 60 ? '…' : ''} |`).join('\n');
  const conRows = con.map(c => `| \`${c.id}\` | ${c.type} | ${c.rarity} | ${c.flavor.slice(0, 60)}${c.flavor.length > 60 ? '…' : ''} |`).join('\n');

  const md = `# Accessories2 Expansion

Package: \`com.political.expansion2.accessories\`
Item id prefix: \`acc2_\`
No mixins. Separate attribute modifier IDs (\`acc2_*\`) from Phase-1 \`acc_*\`.

## Totals

| Category | Count |
|----------|------:|
| Accessories (passive inventory) | ${acc.length} |
| Consumables (right-click) | ${con.length} |
| **Total** | **${catalog.length}** |

Consumables include potions, elixirs, scrolls, foods-on-use, and throwable bombs.

## Behaviour

Accessories aggregate bonuses once per second via inventory scan (Prominence / Hypixel style).
Consumables fire through \`UseItemCallback\`. Tooltips use Skyblock layout via \`AccessoryTooltip2\`.

## Public API

- \`Accessories2.register()\`
- \`Accessories2.items()\`
- \`Accessories2.display(Item)\`

## Integration

1. Call \`Accessories2.register()\` in \`RpgPoliticsMod.onInitialize()\`.
2. Merge \`tools/lang-fragments/accessories2.json\` into \`en_us.json\`.
3. Creative tab: iterate \`Accessories2.items()\` with \`Accessories2.display(item)\`.
4. Rerun \`node tools/gen-accessories2.js\` for textures/models.

## Accessories (${acc.length})

| Id | Type | Rarity | Notes |
|----|------|--------|-------|
${accRows}

## Consumables (${con.length})

| Id | Type | Rarity | Notes |
|----|------|--------|-------|
${conRows}
`;
  fs.mkdirSync(path.dirname(DOC_OUT), { recursive: true });
  fs.writeFileSync(DOC_OUT, md);
}

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

async function generateTextures(catalog) {
  for (const item of catalog) {
    const spec = resolvePalette(item.texture);
    const cv = canvas();
    const fn = draw[spec.fn];
    if (!fn) { console.warn('missing draw', spec.fn, item.id); continue; }
    fn(cv, spec);
    const ref = spec.gem || spec.metal || spec.liquid || spec.crust || spec.meat || P.shadow;
    finishSprite(cv, spec);
    await writePng(cv, ITEM_TEX, item.id);
    writeJSON(path.join(ITEM_MODELS, `${item.id}.json`), itemModel(item.id));
    writeJSON(path.join(ITEM_DEFS, `${item.id}.json`), itemDef(item.id));
  }
}

async function buildPreview(catalog) {
  fs.mkdirSync(PREVIEW_DIR, { recursive: true });
  const ids = catalog.map(x => x.id);
  const scale = 8, cols = 12, pad = 4, cell = N * scale + pad;
  const rows = Math.ceil(ids.length / cols);
  const sheet = new Jimp({ width: cols * cell + pad, height: rows * cell + pad, color: 0x2b2b38ff });
  for (let i = 0; i < ids.length; i++) {
    try {
      const img = await Jimp.read(path.join(ITEM_TEX, `${ids[i]}.png`));
      img.resize({ w: N * scale, h: N * scale, mode: 'nearestNeighbor' });
      sheet.composite(img, pad + (i % cols) * cell, pad + Math.floor(i / cols) * cell);
    } catch (_) { /* skip */ }
  }
  await sheet.write(path.join(PREVIEW_DIR, 'accessories2_montage.png'));
}

async function main() {
  const catalog = buildCatalog();
  const acc = catalog.filter(x => x.kind === 'accessory').length;
  const con = catalog.filter(x => x.kind === 'consumable').length;
  console.log(`catalog: ${acc} accessories, ${con} consumables, ${catalog.length} total`);

  generateJava(catalog);
  generateLang(catalog);
  generateDocs(catalog);
  console.log('wrote', JAVA_CATALOG);
  console.log('wrote', LANG_OUT);
  console.log('wrote', DOC_OUT);

  for (const d of [ITEM_TEX, ITEM_MODELS, ITEM_DEFS]) fs.mkdirSync(d, { recursive: true });
  await generateTextures(catalog);
  await buildPreview(catalog);
  console.log('textures/models:', catalog.length);
  console.log('done.');
}

main().catch(e => { console.error(e); process.exit(1); });
