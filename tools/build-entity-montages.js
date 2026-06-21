/**
 * Build entity texture QA montages in .texref/
 * Usage: node build-entity-montages.js
 */
const { Jimp } = require('jimp');
const path = require('path');
const fs = require('fs');

const ENTITY_TEX = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'politicalserver', 'textures', 'entity');
const PREVIEW = path.join(__dirname, '..', '.texref');

async function montage(prefix, outName, cols = 8) {
  const ids = fs.readdirSync(ENTITY_TEX)
    .filter(f => f.startsWith(prefix) && f.endsWith('.png'))
    .map(f => f.replace('.png', ''))
    .sort();
  if (!ids.length) return;
  const scale = 2;
  const pad = 4;
  const cell = 64 * scale + pad;
  const rows = Math.ceil(ids.length / cols);
  const sheet = new Jimp({ width: cols * cell + pad, height: rows * cell + pad, color: 0xff1a1a22 });
  for (let i = 0; i < ids.length; i++) {
    const img = await Jimp.read(path.join(ENTITY_TEX, ids[i] + '.png'));
    img.resize({ w: 64 * scale, h: 64 * scale, mode: 'nearestNeighbor' });
    sheet.composite(img, pad + (i % cols) * cell, pad + Math.floor(i / cols) * cell);
  }
  fs.mkdirSync(PREVIEW, { recursive: true });
  await sheet.write(path.join(PREVIEW, outName));
  console.log(outName, ids.length, 'textures');
}

async function main() {
  await montage('mob2_', 'mobs2_montage.png', 10);
  await montage('spirit2_', 'spirits2_montage.png', 10);
  await montage('mob_', 'mobs_montage.png', 6);
  const all = fs.readdirSync(ENTITY_TEX).filter(f => f.endsWith('.png') && !f.startsWith('mob2_') && !f.startsWith('spirit2_') && !f.startsWith('mob_'));
  if (all.length) {
    const ids = all.map(f => f.replace('.png', '')).sort();
    const scale = 2, pad = 4, cols = 8, cell = 64 * scale + pad;
    const rows = Math.ceil(ids.length / cols);
    const sheet = new Jimp({ width: cols * cell + pad, height: rows * cell + pad, color: 0xff1a1a22 });
    for (let i = 0; i < ids.length; i++) {
      const img = await Jimp.read(path.join(ENTITY_TEX, ids[i] + '.png'));
      img.resize({ w: 64 * scale, h: 64 * scale, mode: 'nearestNeighbor' });
      sheet.composite(img, pad + (i % cols) * cell, pad + Math.floor(i / cols) * cell);
    }
    await sheet.write(path.join(PREVIEW, 'entities_misc_montage.png'));
    console.log('entities_misc_montage.png', ids.length);
  }
}

main().catch(e => { console.error(e); process.exit(1); });
