/**
 * Entity-texture generator for the cursed-spirit roster (owned by the curse agent).
 *
 * Writes ONE 64x64 humanoid skin per SpiritSpecies into
 *   src/main/resources/assets/politicalserver/textures/entity/<id>.png
 * and nothing else (no models/lang/blockstates), so it never collides with other agents.
 *
 * Each skin is a shaded cursed-flesh body on the standard humanoid UV layout with glowing eyes and a
 * cursed-energy seam down the torso; colour is themed per grade band + species element. Per-grade
 * size is handled in-engine by the SCALE attribute, so the texture itself is grade-agnostic.
 *
 * Usage (only run if tools/node_modules exists, else leave PNGs for the integration agent):
 *   node tools/gen-cursespirits.js
 */
const { Jimp } = require('jimp');
const path = require('path');
const fs = require('fs');

const ENTITY_TEX = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'politicalserver', 'textures', 'entity');
const SIZE = 64;

// id -> { base:[r,g,b], eye:[r,g,b] }  (mirrors SpiritSpecies.java)
const SPIRITS = {
  // Grade 4 — fodder / swarm
  curse_wisp:        { base: [90, 96, 120],  eye: [180, 210, 255] },
  grudge_larva:      { base: [70, 96, 60],   eye: [150, 230, 120] },
  shadow_imp:        { base: [44, 44, 56],   eye: [160, 120, 230] },
  bile_crawler:      { base: [70, 86, 52],   eye: [180, 230, 120] },
  // Grade 3 — common
  slitmouth_curse:   { base: [96, 60, 120],  eye: [235, 120, 235] },
  brute_curse:       { base: [86, 70, 110],  eye: [220, 150, 255] },
  spitter_curse:     { base: [80, 64, 120],  eye: [200, 255, 160] },
  hex_curse:         { base: [72, 80, 128],  eye: [150, 220, 255] },
  // Grade 2 — dangerous
  horned_curse:      { base: [70, 40, 96],   eye: [225, 120, 255] },
  veil_curse:        { base: [56, 48, 84],   eye: [185, 160, 255] },
  plague_curse:      { base: [56, 72, 48],   eye: [175, 240, 120] },
  ember_curse:       { base: [96, 52, 40],   eye: [255, 170, 80] },
  // Grade 1 — calamity
  flame_calamity:    { base: [120, 46, 30],  eye: [255, 205, 95] },
  flora_calamity:    { base: [60, 96, 56],   eye: [185, 255, 140] },
  tide_calamity:     { base: [40, 80, 110],  eye: [150, 230, 255] },
  transfigured_soul: { base: [110, 70, 96],  eye: [255, 150, 200] },
  // Special Grade — bosses
  finger_bearer:     { base: [96, 30, 30],   eye: [255, 95, 70] },
  ruin_sovereign:    { base: [70, 24, 40],   eye: [255, 80, 120] },
  rot_king:          { base: [70, 72, 40],   eye: [205, 255, 90] },
  disgraced_soul:    { base: [90, 60, 80],   eye: [255, 120, 180] },
  cataclysm_curse:   { base: [110, 40, 28],  eye: [255, 185, 70] },
};

function clamp(v) { return Math.max(0, Math.min(255, Math.round(v))); }

// deterministic per-id PRNG so regenerating is stable
function rng(seed) { let s = seed >>> 0 || 1; return () => { s = (s * 1664525 + 1013904223) >>> 0; return s / 4294967296; }; }
function hash(str) { let h = 2166136261; for (let i = 0; i < str.length; i++) { h ^= str.charCodeAt(i); h = Math.imul(h, 16777619); } return h >>> 0; }

function rgba(c, a = 255) { return ((clamp(c[0]) << 24) | (clamp(c[1]) << 16) | (clamp(c[2]) << 8) | a) >>> 0; }

function drawSpirit(img, spec, seed) {
  const r = rng(seed);
  const base = spec.base;
  // Body fills the whole sheet so every humanoid face is coloured.
  for (let y = 0; y < SIZE; y++) {
    for (let x = 0; x < SIZE; x++) {
      const shade = 0.78 + 0.34 * (y / SIZE) + (r() - 0.5) * 0.12;
      img.setPixelColor(rgba([base[0] * shade, base[1] * shade, base[2] * shade]), x, y);
    }
  }
  const dark = [base[0] * 0.45, base[1] * 0.45, base[2] * 0.45];
  // Head front face region (x 8..15, y 8..15): brow shadow + two glowing eyes.
  for (let x = 8; x <= 15; x++) img.setPixelColor(rgba(dark), x, 9);
  for (const ex of [9, 13]) {
    img.setPixelColor(rgba(spec.eye), ex, 11);
    img.setPixelColor(rgba(spec.eye), ex + 1, 11);
    img.setPixelColor(rgba([spec.eye[0] * 0.6, spec.eye[1] * 0.6, spec.eye[2] * 0.6]), ex, 12);
  }
  // Jagged mouth seam on the face.
  for (let x = 9; x <= 14; x++) img.setPixelColor(rgba(dark), x, 14 - (x % 2));
  // Cursed-energy seam glowing down the torso front (body front x 20..27, y 20..31).
  for (let y = 20; y <= 31; y++) {
    const glow = (y % 2 === 0) ? spec.eye : [spec.eye[0] * 0.7, spec.eye[1] * 0.7, spec.eye[2] * 0.7];
    img.setPixelColor(rgba(glow), 23, y);
    img.setPixelColor(rgba(glow), 24, y);
  }
  // Vein flecks scattered over the body for texture.
  for (let i = 0; i < 60; i++) {
    const x = Math.floor(r() * SIZE), y = Math.floor(r() * SIZE);
    if (r() > 0.5) img.setPixelColor(rgba(dark), x, y);
    else img.setPixelColor(rgba([base[0] * 1.25, base[1] * 1.25, base[2] * 1.25]), x, y);
  }
}

async function main() {
  fs.mkdirSync(ENTITY_TEX, { recursive: true });
  let count = 0;
  for (const [id, spec] of Object.entries(SPIRITS)) {
    const img = new Jimp({ width: SIZE, height: SIZE, color: 0x00000000 });
    drawSpirit(img, spec, hash(id));
    await img.write(path.join(ENTITY_TEX, `${id}.png`));
    count++;
  }
  console.log('cursed-spirit textures written:', count, '->', ENTITY_TEX);
}

main().catch(e => { console.error(e); process.exit(1); });
