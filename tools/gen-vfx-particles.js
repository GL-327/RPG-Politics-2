/**
 * Original particle textures + particle definitions for the com.political.vfx
 * custom particle types (Workstream C).
 *
 * Authors small (8x8) original pixel-art sprites — soft radial glows, sparks and
 * glyphs — entirely procedurally (no external art). Writes:
 *   - assets/politicalserver/textures/particle/<name>.png
 *   - assets/politicalserver/particles/<name>.json   ({ "textures": [...] })
 *
 * Usage: node gen-vfx-particles.js
 */
const { Jimp } = require('jimp');
const path = require('path');
const fs = require('fs');

const ROOT = path.join(__dirname, '..');
const ASSETS = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'politicalserver');
const TEX_DIR = path.join(ASSETS, 'textures', 'particle');
const DEF_DIR = path.join(ASSETS, 'particles');
const NS = 'politicalserver';
const S = 8;

fs.mkdirSync(TEX_DIR, { recursive: true });
fs.mkdirSync(DEF_DIR, { recursive: true });

function rgba(r, g, b, a) {
  return (((r & 255) << 24) | ((g & 255) << 16) | ((b & 255) << 8) | (a & 255)) >>> 0;
}
function clamp(v) { return Math.max(0, Math.min(255, Math.round(v))); }

// Soft radial glow: bright core fading to transparent edges, tinted by [r,g,b].
function radialGlow(img, cr, cg, cb, coreBoost) {
  const c = (S - 1) / 2;
  for (let y = 0; y < S; y++) {
    for (let x = 0; x < S; x++) {
      const d = Math.sqrt((x - c) ** 2 + (y - c) ** 2) / (c + 0.5);
      let a = Math.pow(Math.max(0, 1 - d), 1.8);
      if (a <= 0.02) continue;
      const lift = (coreBoost || 0) * Math.pow(Math.max(0, 1 - d), 4);
      img.setPixelColor(rgba(clamp(cr + 255 * lift), clamp(cg + 255 * lift), clamp(cb + 255 * lift), clamp(a * 255)), x, y);
    }
  }
}

// 4-point star spark.
function starSpark(img, cr, cg, cb) {
  const c = (S - 1) / 2;
  for (let y = 0; y < S; y++) {
    for (let x = 0; x < S; x++) {
      const dx = Math.abs(x - c), dy = Math.abs(y - c);
      const onArm = (dx < 0.7 || dy < 0.7);
      const dist = Math.max(dx, dy) / (c + 0.5);
      if (!onArm) continue;
      const a = Math.pow(Math.max(0, 1 - dist), 1.5);
      if (a <= 0.05) continue;
      const core = Math.pow(Math.max(0, 1 - Math.sqrt(dx * dx + dy * dy) / (c + 0.5)), 3);
      img.setPixelColor(rgba(clamp(cr + 90 * core), clamp(cg + 90 * core), clamp(cb + 60 * core), clamp(a * 255)), x, y);
    }
  }
}

// Angular diamond glyph with hollow centre.
function glyph(img, cr, cg, cb) {
  const c = (S - 1) / 2;
  for (let y = 0; y < S; y++) {
    for (let x = 0; x < S; x++) {
      const m = (Math.abs(x - c) + Math.abs(y - c)) / (c + 0.5);
      const edge = Math.abs(m - 0.85);
      if (edge > 0.18) continue;
      const a = 1 - edge / 0.18;
      img.setPixelColor(rgba(cr, cg, cb, clamp(a * 255)), x, y);
    }
  }
}

const SPECS = {
  cursed_ember:  (img) => radialGlow(img, 122, 43, 214, 0.5),
  void_mote:     (img) => radialGlow(img, 53, 16, 107, 0.25),
  arc_spark:     (img) => starSpark(img, 255, 240, 102),
  rune_glyph:    (img) => glyph(img, 192, 108, 255),
  radiant_mote:  (img) => radialGlow(img, 255, 244, 194, 0.7),
  cinder:        (img) => radialGlow(img, 255, 106, 26, 0.6),
};

(async () => {
  for (const [name, draw] of Object.entries(SPECS)) {
    const img = new Jimp({ width: S, height: S, color: 0x00000000 });
    draw(img);
    await img.write(path.join(TEX_DIR, name + '.png'));
    fs.writeFileSync(
      path.join(DEF_DIR, name + '.json'),
      JSON.stringify({ textures: [`${NS}:${name}`] }, null, 2) + '\n');
    console.log('wrote', name);
  }
  console.log('VFX particle assets generated.');
})().catch((e) => { console.error(e); process.exit(1); });
