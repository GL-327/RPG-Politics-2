/**
 * 64x64 humanoid entity-texture generator for the RPG creature set (com.political.expansion.mobs).
 *
 * Every creature shares the vanilla humanoid skin UV layout (so the shared model rig maps
 * cleanly). Each texture is shaded per face (top=light, front=base, sides=mid, bottom=shadow)
 * with a small palette of base / shade / light / accent / eye colours per creature, giving each
 * one a distinct silhouette colour, belt/boot trim and glowing eyes. Style direction is loosely
 * emulated from Prominence II / RAD2 creature packs (ashen knights, elementals, undead, bandits,
 * trolls and bosses).
 *
 * Output: src/main/resources/assets/politicalserver/textures/entity/<id>.png
 * Usage:  node gen-mobs.js   (requires tools/node_modules with jimp; otherwise skipped by agent)
 */
const { Jimp } = require('jimp');
const path = require('path');
const fs = require('fs');

const ENTITY_TEX = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'politicalserver', 'textures', 'entity');
const W = 64, H = 64;

function rgba(r, g, b, a = 255) { return ((r << 24) | (g << 16) | (b << 8) | a) >>> 0; }
function shade(c, f) {
  return [Math.max(0, Math.min(255, Math.round(c[0] * f))),
          Math.max(0, Math.min(255, Math.round(c[1] * f))),
          Math.max(0, Math.min(255, Math.round(c[2] * f)))];
}

function fillFace(img, x0, y0, x1, y1, c) {
  const col = rgba(c[0], c[1], c[2], 255);
  for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) {
    if (x >= 0 && x < W && y >= 0 && y < H) img.setPixelColor(col, x, y);
  }
}

// Paint one cuboid part across its six skin faces.
// rects = { top,bottom,front,back,right,left } each [x0,y0,x1,y1]
function paintPart(img, rects, base) {
  fillFace(img, ...rects.top, shade(base, 1.18));
  fillFace(img, ...rects.bottom, shade(base, 0.65));
  fillFace(img, ...rects.front, base);
  fillFace(img, ...rects.back, shade(base, 0.85));
  fillFace(img, ...rects.right, shade(base, 0.78));
  fillFace(img, ...rects.left, shade(base, 0.92));
}

const HEAD = {
  top: [8, 0, 15, 7], bottom: [16, 0, 23, 7],
  right: [0, 8, 7, 15], front: [8, 8, 15, 15], left: [16, 8, 23, 15], back: [24, 8, 31, 15],
};
const BODY = {
  top: [20, 16, 27, 19], bottom: [28, 16, 35, 19],
  right: [16, 20, 19, 31], front: [20, 20, 27, 31], left: [28, 20, 31, 31], back: [32, 20, 39, 31],
};
const ARM_R = {
  top: [44, 16, 47, 19], bottom: [48, 16, 51, 19],
  right: [40, 20, 43, 31], front: [44, 20, 47, 31], left: [48, 20, 51, 31], back: [52, 20, 55, 31],
};
const ARM_L = {
  top: [36, 48, 39, 51], bottom: [40, 48, 43, 51],
  right: [32, 52, 35, 63], front: [36, 52, 39, 63], left: [40, 52, 43, 63], back: [44, 52, 47, 63],
};
const LEG_R = {
  top: [4, 16, 7, 19], bottom: [8, 16, 11, 19],
  right: [0, 20, 3, 31], front: [4, 20, 7, 31], left: [8, 20, 11, 31], back: [12, 20, 15, 31],
};
const LEG_L = {
  top: [20, 48, 23, 51], bottom: [24, 48, 27, 51],
  right: [16, 52, 19, 63], front: [20, 52, 23, 63], left: [24, 52, 27, 63], back: [28, 52, 31, 63],
};

function px(img, x, y, c) { if (x >= 0 && x < W && y >= 0 && y < H) img.setPixelColor(rgba(c[0], c[1], c[2], 255), x, y); }

async function generate(id, st) {
  const img = new Jimp({ width: W, height: H, color: 0x00000000 });

  const skin = st.skin;
  const cloth = st.cloth || shade(skin, 0.8);
  const boot = st.boot || shade(cloth, 0.7);
  const accent = st.accent || [200, 200, 210];
  const eye = st.eye || [255, 60, 60];

  // Background fill: the creatures now ride shared archetype models with bolted-on appendage cubes
  // (horns, wings, tails, extra arms, antennae, pauldrons, spikes, snouts) whose UVs land outside
  // the vanilla humanoid faces. Flood-filling first guarantees those cubes sample a coloured pixel
  // instead of transparency; the humanoid faces below are painted on top, so the body is unchanged.
  fillFace(img, 0, 0, W - 1, H - 1, shade(skin, 0.8));

  paintPart(img, HEAD, skin);
  paintPart(img, BODY, cloth);
  paintPart(img, ARM_R, skin);
  paintPart(img, ARM_L, skin);
  paintPart(img, LEG_R, boot);
  paintPart(img, LEG_L, boot);

  // glowing eyes on the head front (rect 8..15 x 8..15)
  px(img, 10, 11, eye); px(img, 13, 11, eye);
  px(img, 10, 12, shade(eye, 0.7)); px(img, 13, 12, shade(eye, 0.7));

  // brow / face shadow line
  for (let x = 9; x <= 14; x++) px(img, x, 13, shade(skin, 0.7));

  // chest emblem / trim on body front
  for (let x = 20; x <= 27; x++) px(img, x, 26, accent);
  px(img, 23, 23, accent); px(img, 24, 23, accent);
  px(img, 23, 24, shade(accent, 1.2)); px(img, 24, 24, shade(accent, 1.2));

  // boot cuffs
  for (let x = 4; x <= 7; x++) px(img, x, 30, accent);
  for (let x = 20; x <= 23; x++) px(img, x, 62, accent);

  // shoulder trim
  for (let y = 20; y <= 21; y++) { for (let x = 44; x <= 47; x++) px(img, x, y, accent); for (let x = 36; x <= 39; x++) px(img, x, y, accent); }

  fs.mkdirSync(ENTITY_TEX, { recursive: true });
  await img.write(path.join(ENTITY_TEX, `${id}.png`));
}

// id -> palette. skin = main body, cloth = torso, boot = legs, accent = trim, eye = glow.
const STYLES = {
  mob_ashen_knight:      { skin: [120, 124, 132], cloth: [70, 74, 82], boot: [50, 52, 60], accent: [200, 206, 216], eye: [120, 220, 255] },
  mob_bandit_outlaw:     { skin: [150, 110, 72], cloth: [90, 60, 40], boot: [60, 42, 28], accent: [200, 60, 50], eye: [40, 40, 40] },
  mob_bandit_brute:      { skin: [150, 110, 72], cloth: [70, 70, 78], boot: [50, 40, 30], accent: [180, 150, 60], eye: [40, 40, 40] },
  mob_grave_revenant:    { skin: [110, 130, 100], cloth: [60, 70, 60], boot: [40, 48, 40], accent: [150, 200, 130], eye: [180, 255, 140] },
  mob_ember_fiend:       { skin: [60, 24, 18], cloth: [150, 54, 16], boot: [90, 30, 12], accent: [248, 168, 58], eye: [255, 230, 120] },
  mob_frost_revenant:    { skin: [120, 170, 200], cloth: [60, 110, 150], boot: [40, 80, 120], accent: [220, 250, 255], eye: [180, 240, 255] },
  mob_storm_herald:      { skin: [80, 90, 120], cloth: [50, 60, 90], boot: [36, 44, 70], accent: [250, 232, 116], eye: [255, 250, 180] },
  mob_venom_cultist:     { skin: [70, 110, 50], cloth: [40, 70, 30], boot: [28, 50, 22], accent: [150, 214, 76], eye: [200, 255, 120] },
  mob_bone_legionnaire:  { skin: [220, 214, 190], cloth: [120, 116, 100], boot: [80, 78, 66], accent: [200, 196, 176], eye: [120, 200, 255] },
  mob_wraith:            { skin: [60, 60, 90], cloth: [36, 36, 60], boot: [24, 24, 44], accent: [140, 120, 200], eye: [200, 160, 255] },
  mob_plague_bearer:     { skin: [110, 120, 80], cloth: [70, 80, 50], boot: [48, 56, 34], accent: [160, 180, 80], eye: [200, 220, 120] },
  mob_cultist_acolyte:   { skin: [70, 60, 90], cloth: [40, 30, 60], boot: [28, 22, 44], accent: [160, 110, 224], eye: [200, 120, 255] },
  mob_gnoll_raider:      { skin: [160, 130, 80], cloth: [110, 80, 50], boot: [70, 52, 32], accent: [200, 90, 50], eye: [255, 200, 80] },
  mob_forest_troll:      { skin: [90, 120, 70], cloth: [70, 60, 40], boot: [50, 42, 28], accent: [120, 160, 80], eye: [255, 220, 80] },
  mob_stone_sentinel:    { skin: [120, 122, 130], cloth: [90, 92, 100], boot: [70, 72, 80], accent: [160, 162, 172], eye: [120, 200, 255] },
  mob_marsh_lurker:      { skin: [60, 90, 80], cloth: [40, 64, 56], boot: [28, 46, 40], accent: [120, 180, 150], eye: [160, 255, 200] },
  mob_crystal_warden:    { skin: [150, 120, 200], cloth: [100, 76, 160], boot: [70, 50, 120], accent: [186, 128, 228], eye: [230, 196, 252] },
  mob_ironclad_champion: { skin: [150, 156, 168], cloth: [90, 96, 110], boot: [60, 64, 76], accent: [246, 214, 96], eye: [120, 220, 255] },
  mob_blighted_ogre:     { skin: [110, 130, 80], cloth: [80, 70, 50], boot: [56, 50, 34], accent: [150, 214, 76], eye: [200, 255, 120] },
  mob_frostking_sentinel:{ skin: [150, 190, 220], cloth: [70, 120, 170], boot: [48, 90, 140], accent: [230, 252, 255], eye: [200, 240, 255] },
  mob_warlord_kael:      { skin: [90, 50, 50], cloth: [60, 24, 28], boot: [40, 16, 20], accent: [246, 214, 96], eye: [255, 80, 60] },
  mob_storm_tyrant:      { skin: [60, 70, 110], cloth: [40, 50, 90], boot: [28, 36, 70], accent: [250, 232, 116], eye: [255, 255, 200] },
  mob_lich_sovereign:    { skin: [110, 120, 110], cloth: [40, 50, 44], boot: [28, 36, 30], accent: [140, 110, 224], eye: [120, 255, 180] },
};

async function main() {
  let n = 0;
  for (const [id, st] of Object.entries(STYLES)) { await generate(id, st); n++; }
  console.log('entity textures written:', n, '->', ENTITY_TEX);
}

main().catch(e => { console.error(e); process.exit(1); });
