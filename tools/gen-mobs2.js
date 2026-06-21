/**
 * 64x64 humanoid entity-texture generator for phase-2 creatures (com.political.expansion2.mobs).
 *
 * Palettes are derived from creature id keywords (element/faction lineage) with per-id hue jitter
 * so all 115 textures stay distinct without hand-authoring each entry.
 *
 * Output: src/main/resources/assets/politicalserver/textures/entity/mob2_*.png
 * Usage:  node tools/gen-mobs2.js
 */
const { Jimp } = require('jimp');
const path = require('path');
const fs = require('fs');
const MOB2_IDS = require('./mob2-ids');

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
function paintPart(img, rects, base) {
  fillFace(img, ...rects.top, shade(base, 1.18));
  fillFace(img, ...rects.bottom, shade(base, 0.65));
  fillFace(img, ...rects.front, base);
  fillFace(img, ...rects.back, shade(base, 0.85));
  fillFace(img, ...rects.right, shade(base, 0.78));
  fillFace(img, ...rects.left, shade(base, 0.92));
}
const HEAD = { top: [8,0,15,7], bottom: [16,0,23,7], right: [0,8,7,15], front: [8,8,15,15], left: [16,8,23,15], back: [24,8,31,15] };
const BODY = { top: [20,16,27,19], bottom: [28,16,35,19], right: [16,20,19,31], front: [20,20,27,31], left: [28,20,31,31], back: [32,20,39,31] };
const ARM_R = { top: [44,16,47,19], bottom: [48,16,51,19], right: [40,20,43,31], front: [44,20,47,31], left: [48,20,51,31], back: [52,20,55,31] };
const ARM_L = { top: [36,48,39,51], bottom: [40,48,43,51], right: [32,52,35,63], front: [36,52,39,63], left: [40,52,43,63], back: [44,52,47,63] };
const LEG_R = { top: [4,16,7,19], bottom: [8,16,11,19], right: [0,20,3,31], front: [4,20,7,31], left: [8,20,11,31], back: [12,20,15,31] };
const LEG_L = { top: [20,48,23,51], bottom: [24,48,27,51], right: [16,52,19,63], front: [20,52,23,63], left: [24,52,27,63], back: [28,52,31,63] };
function px(img, x, y, c) { if (x >= 0 && x < W && y >= 0 && y < H) img.setPixelColor(rgba(c[0], c[1], c[2], 255), x, y); }

function hashId(id) {
  let h = 0;
  for (let i = 0; i < id.length; i++) h = ((h << 5) - h + id.charCodeAt(i)) | 0;
  return Math.abs(h);
}

const LINEAGE = [
  { keys: ['ember','flame','magma','cinder','pyre','salamander','volcano','inferno','infernal','raid_flame'], base: [150,54,16], accent: [248,168,58], eye: [255,230,120] },
  { keys: ['frost','glacial','ice','blizzard','rime','arctic','glacier','frost','raid_frost'], base: [60,110,150], accent: [220,250,255], eye: [180,240,255] },
  { keys: ['spark','thunder','storm','gale','tempest','lightning','cataclysm','raid_storm'], base: [50,60,90], accent: [250,232,116], eye: [255,255,200] },
  { keys: ['stone','mud','boulder','earth','root','cave','granite','quake','earthbound'], base: [90,80,60], accent: [160,140,100], eye: [200,180,140] },
  { keys: ['shade','night','shadow','void','phantom','abyss','raid_void'], base: [36,36,60], accent: [140,120,200], eye: [200,160,255] },
  { keys: ['light','solar','radiant','zealot','sun','dawn','seraph','aegis','raid_solar'], base: [200,190,120], accent: [255,240,180], eye: [255,255,220] },
  { keys: ['arcane','mana','spell','rune','ether','archmage','raid_arcane'], base: [100,76,160], accent: [186,128,228], eye: [230,196,252] },
  { keys: ['highway','cutthroat','smuggler','marauder','bandit'], base: [90,60,40], accent: [200,60,50], eye: [40,40,40] },
  { keys: ['squire','order','broken','crusader','grand','marshal','paladin','knight'], base: [120,124,132], accent: [246,214,96], eye: [120,220,255] },
  { keys: ['blood','ritual','doom','heretic','apocalypse','cultist'], base: [40,30,60], accent: [160,110,224], eye: [200,120,255] },
  { keys: ['rot','tomb','banshee','death','dread','lich','necromancer','undead'], base: [110,120,100], accent: [150,200,130], eye: [120,255,180] },
  { keys: ['imp','hellhound','demon','pit','prince'], base: [150,24,18], accent: [255,80,40], eye: [255,120,60] },
  { keys: ['pixie','spriggan','unseelie','fey'], base: [70,130,70], accent: [150,214,76], eye: [200,255,120] },
  { keys: ['rust','clockwork','iron_golem','siege','colossus','construct','automaton'], base: [120,122,130], accent: [160,162,172], eye: [120,200,255] },
  { keys: ['marsh','highland','jungle','canyon','river','wild','buffalo','ram','panther','vulture','turtle'], base: [100,90,70], accent: [140,120,80], eye: [80,60,40] },
  { keys: ['plains_steed','desert_camel','mountain_goat','swamp_draft','steed','camel','goat','draft'], base: [130,110,80], accent: [180,150,100], eye: [60,40,30] },
];

function paletteFor(id) {
  const body = id.replace('mob2_', '');
  let pal = LINEAGE[LINEAGE.length - 2];
  for (const entry of LINEAGE) {
    if (entry.keys.some(k => body.includes(k))) { pal = entry; break; }
  }
  const h = hashId(id);
  const jitter = (n, spread) => Math.max(0, Math.min(255, n + (h % spread) - spread / 2));
  const skin = [jitter(pal.base[0], 30), jitter(pal.base[1], 30), jitter(pal.base[2], 30)];
  const cloth = shade(skin, 0.82);
  const boot = shade(cloth, 0.72);
  const accent = [jitter(pal.accent[0], 24), jitter(pal.accent[1], 24), jitter(pal.accent[2], 24)];
  const eye = pal.eye;
  if (body.includes('boss') || body.includes('sovereign') || body.includes('queen') || body.includes('god') || body.includes('prince') || body.includes('prime') || body.includes('lich') || body.includes('marshal') || body.includes('emperor') || body.includes('herald') || body.includes('necromancer') || body.includes('titan') || body.includes('colossus')) {
    return { skin: shade(skin, 0.9), cloth: shade(cloth, 0.85), boot, accent: shade(accent, 1.15), eye: shade(eye, 1.2) };
  }
  return { skin, cloth, boot, accent, eye };
}

async function generate(id) {
  const st = paletteFor(id);
  const img = new Jimp({ width: W, height: H, color: 0x00000000 });
  // Background fill so bolted-on appendage cubes on the shared archetype models (horns, wings,
  // tails, extra arms, antennae, pauldrons, spikes, snouts) sample a coloured pixel instead of
  // transparency; the humanoid faces are painted on top, leaving the body appearance unchanged.
  fillFace(img, 0, 0, W - 1, H - 1, shade(st.skin, 0.8));
  paintPart(img, HEAD, st.skin);
  paintPart(img, BODY, st.cloth);
  paintPart(img, ARM_R, st.skin);
  paintPart(img, ARM_L, st.skin);
  paintPart(img, LEG_R, st.boot);
  paintPart(img, LEG_L, st.boot);
  px(img, 10, 11, st.eye); px(img, 13, 11, st.eye);
  px(img, 10, 12, shade(st.eye, 0.7)); px(img, 13, 12, shade(st.eye, 0.7));
  for (let x = 9; x <= 14; x++) px(img, x, 13, shade(st.skin, 0.7));
  for (let x = 20; x <= 27; x++) px(img, x, 26, st.accent);
  px(img, 23, 23, st.accent); px(img, 24, 23, st.accent);
  for (let x = 4; x <= 7; x++) px(img, x, 30, st.accent);
  for (let x = 20; x <= 23; x++) px(img, x, 62, st.accent);
  for (let y = 20; y <= 21; y++) {
    for (let x = 44; x <= 47; x++) px(img, x, y, st.accent);
    for (let x = 36; x <= 39; x++) px(img, x, y, st.accent);
  }
  fs.mkdirSync(ENTITY_TEX, { recursive: true });
  await img.write(path.join(ENTITY_TEX, `${id}.png`));
}

async function main() {
  for (const id of MOB2_IDS) await generate(id);
  console.log('phase-2 entity textures written:', MOB2_IDS.length, '->', ENTITY_TEX);
}

main().catch(e => { console.error(e); process.exit(1); });
