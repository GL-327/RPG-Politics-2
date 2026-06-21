// Builds a scaled montage of all PNGs in a folder for visual study.
// Usage: node montage.js <srcDir> <outPng> [scale] [cols]
const { Jimp } = require('jimp');
const path = require('path');
const fs = require('fs');

async function main() {
  const src = process.argv[2];
  const out = process.argv[3];
  const scale = parseInt(process.argv[4] || '12', 10);
  const cols = parseInt(process.argv[5] || '7', 10);
  const files = fs.readdirSync(src).filter(f => f.endsWith('.png')).sort();
  const cell = 16 * scale;
  const pad = 6;
  const label = 10;
  const cellW = cell + pad;
  const cellH = cell + pad + label;
  const rows = Math.ceil(files.length / cols);
  const W = cols * cellW + pad;
  const H = rows * cellH + pad;
  const sheet = new Jimp({ width: W, height: H, color: 0xff2b2b32 });
  let slot = 0;
  for (let i = 0; i < files.length; i++) {
    let img;
    try { img = await Jimp.read(path.join(src, files[i])); }
    catch (e) { console.log('skip', files[i], e.message); continue; }
    // crop to top-left 16x16 in case of animated/atlas sheets
    if (img.bitmap.width > img.bitmap.height) img.crop({ x: 0, y: 0, w: img.bitmap.height, h: img.bitmap.height });
    else if (img.bitmap.height > img.bitmap.width) img.crop({ x: 0, y: 0, w: img.bitmap.width, h: img.bitmap.width });
    img.resize({ w: cell, h: cell, mode: 'nearestNeighbor' });
    const cx = pad + (slot % cols) * cellW;
    const cy = pad + Math.floor(slot / cols) * cellH;
    sheet.composite(img, cx, cy);
    slot++;
  }
  await sheet.write(out);
  console.log('wrote', out, files.length, 'tiles');
  console.log(files.join('\n'));
}
main().catch(e => { console.error(e); process.exit(1); });
