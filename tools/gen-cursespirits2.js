/**
 * Phase-2 cursed spirit texture + lang generator (com.political.expansion2.curses).
 *
 * Writes 64x64 humanoid skins to assets/politicalserver/textures/entity/spirit2_*.png
 * and lang fragment tools/lang-fragments/cursespirits2.json.
 *
 * Usage: node tools/gen-cursespirits2.js
 */
const { Jimp } = require('jimp');
const path = require('path');
const fs = require('fs');
const SPECIES = require('./spirit2-species-data');

const ENTITY_TEX = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'politicalserver', 'textures', 'entity');
const LANG_OUT = path.join(__dirname, 'lang-fragments', 'cursespirits2.json');
const SIZE = 64;

function clamp(v) { return Math.max(0, Math.min(255, Math.round(v))); }
function rgba(c, a = 255) { return ((clamp(c[0]) << 24) | (clamp(c[1]) << 16) | (clamp(c[2]) << 8) | a) >>> 0; }
function rng(seed) { let s = seed >>> 0 || 1; return () => { s = (s * 1664525 + 1013904223) >>> 0; return s / 4294967296; }; }
function hash(str) { let h = 2166136261; for (let i = 0; i < str.length; i++) { h ^= str.charCodeAt(i); h = Math.imul(h, 16777619); } return h >>> 0; }

function drawSpirit(img, spec, seed) {
  const r = rng(seed);
  const base = spec.base;
  const eye = spec.eye;
  const accent = spec.boss
    ? [Math.min(255, base[0] * 1.4), Math.min(255, base[1] * 0.8), Math.min(255, base[2] * 0.6)]
    : [eye[0] * 0.6, eye[1] * 0.6, eye[2] * 0.6];

  for (let y = 0; y < SIZE; y++) {
    for (let x = 0; x < SIZE; x++) {
      const shade = 0.76 + 0.36 * (y / SIZE) + (r() - 0.5) * 0.14;
      img.setPixelColor(rgba([base[0] * shade, base[1] * shade, base[2] * shade]), x, y);
    }
  }
  const dark = [base[0] * 0.42, base[1] * 0.42, base[2] * 0.42];

  // Head front (8..15, 8..15)
  for (let x = 8; x <= 15; x++) img.setPixelColor(rgba(dark), x, 9);
  for (const ex of [9, 13]) {
    img.setPixelColor(rgba(eye), ex, 11);
    img.setPixelColor(rgba(eye), ex + 1, 11);
    img.setPixelColor(rgba([eye[0] * 0.55, eye[1] * 0.55, eye[2] * 0.55]), ex, 12);
  }
  for (let x = 9; x <= 14; x++) img.setPixelColor(rgba(dark), x, 14 - (x % 2));

  // Model-specific accents on body front
  const model = spec.model || 'GAUNT';
  if (model === 'WINGED') {
    for (let y = 16; y <= 19; y++) for (let x = 0; x <= 7; x++) img.setPixelColor(rgba(accent), x, y);
    for (let y = 16; y <= 19; y++) for (let x = 56; x <= 63; x++) img.setPixelColor(rgba(accent), x, y);
  } else if (model === 'SERPENT') {
    for (let y = 48; y <= 63; y++) { img.setPixelColor(rgba(accent), 36, y); img.setPixelColor(rgba(accent), 37, y); }
  } else if (model === 'TOOL') {
    for (let y = 20; y <= 31; y++) { img.setPixelColor(rgba(accent), 44, y); img.setPixelColor(rgba(accent), 45, y); }
  } else if (model === 'CORPSE') {
    for (let x = 20; x <= 27; x++) for (let y = 20; y <= 24; y++) img.setPixelColor(rgba(dark), x, y);
  } else if (model === 'HULKING') {
    for (let x = 16; x <= 19; x++) for (let y = 20; y <= 23; y++) img.setPixelColor(rgba(accent), x, y);
    for (let x = 28; x <= 31; x++) for (let y = 20; y <= 23; y++) img.setPixelColor(rgba(accent), x, y);
  } else if (model === 'HORNED') {
    img.setPixelColor(rgba(accent), 56, 0); img.setPixelColor(rgba(accent), 57, 1);
    img.setPixelColor(rgba(accent), 60, 0); img.setPixelColor(rgba(accent), 61, 1);
  }

  // Cursed energy seam
  for (let y = 20; y <= 31; y++) {
    const glow = (y % 2 === 0) ? eye : [eye[0] * 0.65, eye[1] * 0.65, eye[2] * 0.65];
    img.setPixelColor(rgba(glow), 23, y);
    img.setPixelColor(rgba(glow), 24, y);
  }

  // Boss crown mark
  if (spec.boss) {
    for (let x = 10; x <= 13; x++) img.setPixelColor(rgba(accent), x, 8);
  }

  for (let i = 0; i < (spec.boss ? 80 : 55); i++) {
    const x = Math.floor(r() * SIZE), y = Math.floor(r() * SIZE);
    if (r() > 0.5) img.setPixelColor(rgba(dark), x, y);
    else img.setPixelColor(rgba([base[0] * 1.22, base[1] * 1.22, base[2] * 1.22]), x, y);
  }
}

async function main() {
  fs.mkdirSync(ENTITY_TEX, { recursive: true });
  const lang = {};
  let count = 0;
  for (const sp of SPECIES) {
    const img = new Jimp({ width: SIZE, height: SIZE, color: 0x00000000 });
    drawSpirit(img, sp, hash(sp.id));
    await img.write(path.join(ENTITY_TEX, `${sp.id}.png`));
    lang[`entity.politicalserver.${sp.id}`] = sp.name;
    count++;
  }
  fs.mkdirSync(path.dirname(LANG_OUT), { recursive: true });
  fs.writeFileSync(LANG_OUT, JSON.stringify(lang, null, 2) + '\n');
  console.log('spirit2 textures:', count, '->', ENTITY_TEX);
  console.log('spirit2 lang keys:', count, '->', LANG_OUT);
}

main().catch(e => { console.error(e); process.exit(1); });
