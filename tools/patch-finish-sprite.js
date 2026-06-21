/** Patch item generators to use finishSprite from pixel-art-lib */
const fs = require('fs');
const path = require('path');

const files = [
  'gen-ranged.js', 'gen-ranged2.js', 'gen-armor.js', 'gen-armor2.js',
  'gen-accessories.js', 'gen-accessories2.js', 'gen-food.js', 'gen-food2.js', 'gen-quests2.js',
];

for (const f of files) {
  const p = path.join(__dirname, f);
  if (!fs.existsSync(p)) continue;
  let s = fs.readFileSync(p, 'utf8');
  if (!s.includes("require('./pixel-art-lib')")) {
    s = s.replace(
      /^(const \{ Jimp \} = require\('jimp'\);)/m,
      "$1\nconst { finishSprite } = require('./pixel-art-lib');"
    );
  }
  s = s.replace(
    /\s*const ref = spec\.blade \|\| spec\.metal \|\| spec\.gem[^\n]*\n\s*outline\(cv, darken\(ref\[0\], [^)]+\)\);/g,
    '\n    finishSprite(cv, spec);'
  );
  s = s.replace(
    /\s*outline\(cv, darken\(spec\.metal\[0\], [^)]+\)\);/g,
    '\n      finishSprite(cv, spec);'
  );
  s = s.replace(
    /\s*outline\(cv, darken\(paletteSpec\.metal\[0\], [^)]+\)\);/g,
    '\n      finishSprite(cv, paletteSpec);'
  );
  s = s.replace(
    /\s*outline\(cv, darken\(ref\[0\], [^)]+\)\);/g,
    '\n    finishSprite(cv, spec);'
  );
  fs.writeFileSync(p, s);
  console.log('patched', f);
}
