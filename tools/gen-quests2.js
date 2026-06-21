/**
 * 16x16 quest-item texture generator for Expansion 2 (com.political.expansion2.quests).
 *
 * Writes for every quest2_* id:
 *   - textures/item/<id>.png
 *   - models/item/<id>.json
 *   - items/<id>.json
 *
 * Usage: node tools/gen-quests2.js
 */
const { Jimp } = require('jimp');
const { finishSprite } = require('./pixel-art-lib');
const path = require('path');
const fs = require('fs');

const ASSETS = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'politicalserver');
const ITEM_TEX = path.join(ASSETS, 'textures', 'item');
const ITEM_MODELS = path.join(ASSETS, 'models', 'item');
const ITEM_DEFS = path.join(ASSETS, 'items');

const N = 16;
function canvas() { return Array.from({ length: N }, () => Array.from({ length: N }, () => null)); }
function inb(x, y) { return x >= 0 && x < N && y >= 0 && y < N; }
function px(cv, x, y, c) { x = Math.round(x); y = Math.round(y); if (inb(x, y) && c) cv[y][x] = c; }
function fillRect(cv, x0, y0, x1, y1, c) { for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) px(cv, x, y, c); }
function darken(c, f) { return [Math.round(c[0] * f), Math.round(c[1] * f), Math.round(c[2] * f), 255]; }

const ITEMS = {
  quest2_bounty_seal: { pal: [[40, 30, 20], [120, 90, 50], [200, 170, 80], [255, 220, 120]], shape: 'medallion' },
  quest2_cursed_relic: { pal: [[30, 10, 40], [80, 30, 90], [140, 60, 160], [200, 100, 220]], shape: 'orb' },
  quest2_grimoire_page: { pal: [[50, 40, 30], [120, 100, 70], [200, 180, 140], [240, 230, 200]], shape: 'page' },
  quest2_shrine_offering: { pal: [[60, 40, 20], [140, 100, 50], [200, 160, 80], [255, 210, 120]], shape: 'bowl' },
  quest2_map_fragment: { pal: [[40, 50, 30], [100, 120, 70], [160, 180, 110], [220, 230, 170]], shape: 'page' },
  quest2_election_ballot: { pal: [[30, 30, 50], [80, 80, 120], [140, 140, 200], [200, 200, 255]], shape: 'page' },
  quest2_spirit_tag: { pal: [[20, 20, 30], [60, 40, 80], [120, 80, 160], [180, 140, 220]], shape: 'tag' },
  quest2_boss_token: { pal: [[50, 10, 10], [120, 30, 30], [200, 60, 60], [255, 120, 120]], shape: 'medallion' },
  quest2_bank_ledger: { pal: [[30, 40, 30], [70, 100, 70], [120, 160, 120], [180, 220, 180]], shape: 'book' },
  quest2_herb_satchel: { pal: [[40, 50, 20], [80, 110, 50], [130, 170, 80], [180, 220, 120]], shape: 'bag' },
  quest2_rune_shard: { pal: [[20, 30, 50], [50, 80, 120], [90, 140, 200], [150, 200, 255]], shape: 'shard' },
  quest2_merc_contract: { pal: [[40, 35, 30], [90, 80, 70], [150, 130, 110], [210, 190, 170]], shape: 'page' },
  quest2_cursed_coin: { pal: [[30, 10, 40], [80, 30, 90], [140, 70, 150], [200, 120, 220]], shape: 'coin' },
  quest2_awakening_stone: { pal: [[20, 20, 40], [60, 50, 100], [120, 100, 180], [200, 180, 255]], shape: 'orb' },
  quest2_diplomat_seal: { pal: [[20, 30, 60], [50, 80, 140], [100, 140, 200], [160, 200, 255]], shape: 'medallion' },
};

function drawShape(cv, shape, pal) {
  const o = [10, 10, 12];
  if (shape === 'medallion') {
    for (let y = 3; y < 13; y++) for (let x = 4; x < 12; x++) {
      const dx = x - 8, dy = y - 8;
      if (dx * dx + dy * dy <= 20) px(cv, x, y, dx * dx + dy * dy <= 8 ? pal[3] : pal[2]);
    }
    fillRect(cv, 7, 2, 8, 3, pal[1]);
  } else if (shape === 'orb') {
    for (let y = 2; y < 14; y++) for (let x = 3; x < 13; x++) {
      const dx = x - 8, dy = y - 8;
      if (dx * dx + dy * dy <= 36) px(cv, x, y, dy < 0 ? pal[3] : pal[2]);
    }
  } else if (shape === 'page') {
    fillRect(cv, 4, 2, 11, 13, pal[2]);
    fillRect(cv, 4, 2, 5, 13, pal[1]);
    for (let y = 4; y < 12; y += 2) fillRect(cv, 6, y, 10, y, pal[0]);
  } else if (shape === 'book') {
    fillRect(cv, 3, 3, 12, 12, pal[2]);
    fillRect(cv, 3, 3, 4, 12, pal[1]);
    fillRect(cv, 8, 4, 11, 11, pal[3]);
  } else if (shape === 'bowl') {
    for (let y = 6; y < 12; y++) for (let x = 4; x < 12; x++) {
      if ((x - 8) * (x - 8) + (y - 10) * (y - 10) < 16) px(cv, x, y, pal[2]);
    }
    fillRect(cv, 6, 5, 9, 6, pal[3]);
  } else if (shape === 'tag') {
    fillRect(cv, 5, 3, 10, 12, pal[2]);
    px(cv, 5, 3, pal[3]); px(cv, 10, 3, pal[3]);
    fillRect(cv, 7, 6, 8, 10, pal[0]);
  } else if (shape === 'bag') {
    fillRect(cv, 4, 5, 11, 12, pal[2]);
    fillRect(cv, 5, 3, 10, 6, pal[1]);
    fillRect(cv, 6, 7, 9, 10, pal[3]);
  } else if (shape === 'shard') {
    for (let i = 0; i < 10; i++) {
      px(cv, 8, 2 + i, pal[3]);
      px(cv, 7, 3 + i, pal[2]);
      px(cv, 9, 3 + i, pal[1]);
    }
  } else if (shape === 'coin') {
    for (let y = 5; y < 11; y++) for (let x = 5; x < 11; x++) {
      if ((x - 8) ** 2 + (y - 8) ** 2 <= 9) px(cv, x, y, pal[2]);
    }
    px(cv, 6, 6, pal[3]);
  }
  // outline
  for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) {
    if (cv[y][x]) {
      for (const [dx, dy] of [[1,0],[-1,0],[0,1],[0,-1]]) {
        if (!get(cv, x + dx, y + dy)) px(cv, x + dx, y + dy, o);
      }
    }
  }
}
function get(cv, x, y) { return inb(x, y) ? cv[y][x] : null; }

async function writeItem(id, spec) {
  const cv = canvas();
  drawShape(cv, spec.shape, spec.pal);
  const img = new Jimp({ width: N, height: N, color: 0x00000000 });
  for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) {
    const c = cv[y][x];
    if (c) img.setPixelColor(((c[0] << 24) | (c[1] << 16) | (c[2] << 8) | 255) >>> 0, x, y);
  }
  fs.mkdirSync(ITEM_TEX, { recursive: true });
  fs.mkdirSync(ITEM_MODELS, { recursive: true });
  fs.mkdirSync(ITEM_DEFS, { recursive: true });
  await img.write(path.join(ITEM_TEX, id + '.png'));
  fs.writeFileSync(path.join(ITEM_MODELS, id + '.json'), JSON.stringify({
    parent: 'minecraft:item/generated',
    textures: { layer0: 'politicalserver:item/' + id }
  }, null, 2));
  fs.writeFileSync(path.join(ITEM_DEFS, id + '.json'), JSON.stringify({
    model: { type: 'minecraft:model', model: 'politicalserver:item/' + id }
  }, null, 2));
  console.log('Generated', id);
}

(async () => {
  const { buildMontage } = require('./pixel-art-lib');
  for (const [id, spec] of Object.entries(ITEMS)) await writeItem(id, spec);
  await buildMontage(Object.keys(ITEMS), 'quests2_montage.png', { cols: 5 });
  console.log('Done —', Object.keys(ITEMS).length, 'quest items');
})();
