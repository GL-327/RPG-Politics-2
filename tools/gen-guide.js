/**
 * 16x16 icon generator for the in-game guide (com.political.guide).
 *
 * Writes for the Field Manual item:
 *   - textures/item/guide_field_manual.png
 *   - models/item/guide_field_manual.json
 *   - items/guide_field_manual.json
 *
 * Usage: node tools/gen-guide.js
 */
const { Jimp } = require('jimp');
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
function get(cv, x, y) { return inb(x, y) ? cv[y][x] : null; }
function fillRect(cv, x0, y0, x1, y1, c) { for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) px(cv, x, y, c); }

// Palette: a deep navy leather tome with gold trim and an arcane emblem.
const OUTLINE = [12, 10, 16];
const COVER = [38, 54, 92];
const COVER_HI = [70, 96, 150];
const COVER_LO = [24, 34, 60];
const SPINE = [22, 30, 54];
const PAGES = [228, 222, 198];
const PAGES_HI = [248, 245, 232];
const GOLD = [255, 200, 87];
const GOLD_HI = [255, 232, 160];

function drawBook(cv) {
  // Page block (right edge of the closed book) behind the cover.
  fillRect(cv, 11, 3, 13, 13, PAGES);
  for (let y = 3; y <= 13; y += 2) px(cv, 13, y, PAGES_HI);

  // Front cover.
  fillRect(cv, 3, 2, 12, 14, COVER);
  // Top highlight + bottom shade for a little volume.
  fillRect(cv, 3, 2, 12, 2, COVER_HI);
  fillRect(cv, 3, 14, 12, 14, COVER_LO);
  fillRect(cv, 3, 3, 3, 13, COVER_HI);

  // Spine on the left.
  fillRect(cv, 2, 2, 3, 14, SPINE);

  // Gold border frame on the cover.
  for (let x = 5; x <= 11; x++) { px(cv, x, 4, GOLD); px(cv, x, 12, GOLD); }
  for (let y = 4; y <= 12; y++) { px(cv, 5, y, GOLD); px(cv, 11, y, GOLD); }

  // Arcane emblem: a four-point star inside the frame.
  px(cv, 8, 6, GOLD_HI);
  px(cv, 8, 7, GOLD);
  px(cv, 8, 8, GOLD_HI);
  px(cv, 8, 9, GOLD);
  px(cv, 8, 10, GOLD_HI);
  px(cv, 6, 8, GOLD);
  px(cv, 7, 8, GOLD_HI);
  px(cv, 9, 8, GOLD_HI);
  px(cv, 10, 8, GOLD);
  px(cv, 7, 7, GOLD); px(cv, 9, 7, GOLD);
  px(cv, 7, 9, GOLD); px(cv, 9, 9, GOLD);

  // Silhouette outline (4-neighbour).
  const add = [];
  for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) {
    if (cv[y][x]) continue;
    if (get(cv, x, y - 1) || get(cv, x, y + 1) || get(cv, x - 1, y) || get(cv, x + 1, y)) add.push([x, y]);
  }
  for (const [x, y] of add) cv[y][x] = OUTLINE;
}

async function writeItem(id) {
  const cv = canvas();
  drawBook(cv);
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
  await writeItem('guide_field_manual');
  console.log('Done — guide icon');
})();
