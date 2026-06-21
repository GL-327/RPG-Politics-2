/**
 * Texture audit: enumerate required texture ids from Java registries vs PNGs on disk.
 * Usage: node audit-textures.js
 */
const fs = require('fs');
const path = require('path');

const ROOT = path.join(__dirname, '..');
const JAVA = path.join(ROOT, 'src', 'main', 'java');
const TEX = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'politicalserver', 'textures');
const MODELS_ITEM = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'politicalserver', 'models', 'item');
const MODELS_BLOCK = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'politicalserver', 'models', 'block');
const ITEM_DEFS = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'politicalserver', 'items');

function walk(dir, ext, out = []) {
  if (!fs.existsSync(dir)) return out;
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) walk(p, ext, out);
    else if (e.name.endsWith(ext)) out.push(p);
  }
  return out;
}

function collectJavaIds() {
  const items = new Set();
  const blocks = new Set();
  const entities = new Set();
  const javaFiles = walk(JAVA, '.java');
  const idPatterns = [
    /Identifier\.fromNamespaceAndPath\(\s*["']politicalserver["']\s*,\s*["']([a-z0-9_]+)["']/g,
    /fromNamespaceAndPath\(MOD_ID,\s*["']([a-z0-9_]+)["']/g,
    /fromNamespaceAndPath\(NAMESPACE,\s*["']([a-z0-9_]+)["']/g,
    /registerBlock\(\s*["']([a-z0-9_]+)["']/g,
    /registerItem\(\s*["']([a-z0-9_]+)["']/g,
    /["']((?:wpn2?|rng2?|arm2?|acc2?|food2?|dec2?|quest2|mob2|spirit2)_[a-z0-9_]+)["']/g,
    /id\(\)\s*\{\s*return\s*["']([a-z0-9_]+)["']/g,
    /String\s+id\s*=\s*["']([a-z0-9_]+)["']/g,
  ];
  for (const file of javaFiles) {
    const rel = path.relative(JAVA, file).replace(/\\/g, '/');
    if (!rel.includes('political/') && !rel.includes('expansion')) continue;
    const text = fs.readFileSync(file, 'utf8');
    for (const re of idPatterns) {
      let m;
      re.lastIndex = 0;
      while ((m = re.exec(text))) {
        const id = m[1];
        if (id.startsWith('mob2_') || id.startsWith('spirit2_') || id.includes('entity')) entities.add(id);
        else if (id.startsWith('dec_') || id.startsWith('dec2_') || id.endsWith('_block') || id.includes('bricks') || id.includes('pillar')) blocks.add(id);
        else items.add(id);
      }
    }
    // RpgItem enum entries
    const enumMatch = text.match(/enum RpgItem \{([\s\S]*?)\n\}/);
    if (enumMatch) {
      for (const line of enumMatch[1].split('\n')) {
        const em = line.match(/^\s*([A-Z][A-Z0-9_]*)\s*\(/);
        if (em) items.add(em[1].toLowerCase());
      }
    }
  }
  return { items, blocks, entities };
}

function pngSet(subdir) {
  const dir = path.join(TEX, subdir);
  const s = new Set();
  for (const f of walk(dir, '.png')) s.add(path.basename(f, '.png'));
  return s;
}

function modelRefs(dir) {
  const refs = new Set();
  for (const f of walk(dir, '.json')) {
    try {
      const j = JSON.parse(fs.readFileSync(f, 'utf8'));
      const t = j.textures;
      if (t) for (const v of Object.values(t)) {
        const m = String(v).match(/politicalserver:(?:item|block)\/([a-z0-9_]+)/);
        if (m) refs.add(m[1]);
      }
    } catch (_) { /* skip */ }
  }
  return refs;
}

function main() {
  const { items: javaItems, blocks: javaBlocks, entities: javaEntities } = collectJavaIds();
  const itemPngs = pngSet('item');
  const blockPngs = pngSet('block');
  const entityPngs = pngSet('entity');

  const missingItem = [...javaItems].filter(id => !itemPngs.has(id)).sort();
  const missingBlock = [...javaBlocks].filter(id => !blockPngs.has(id)).sort();
  const missingEntity = [...javaEntities].filter(id => !entityPngs.has(id)).sort();

  const orphanItems = [...itemPngs].filter(id => !javaItems.has(id)).length;
  const orphanBlocks = [...blockPngs].filter(id => !javaBlocks.has(id)).length;

  const itemModels = modelRefs(MODELS_ITEM);
  const missingModels = [...itemPngs].filter(id => !itemModels.has(id)).sort();

  const report = {
    counts: {
      item_png: itemPngs.size,
      block_png: blockPngs.size,
      entity_png: entityPngs.size,
      total_png: itemPngs.size + blockPngs.size + entityPngs.size,
      java_item_ids: javaItems.size,
      java_block_ids: javaBlocks.size,
      java_entity_ids: javaEntities.size,
    },
    missing: {
      items: missingItem,
      blocks: missingBlock,
      entities: missingEntity,
      item_models: missingModels.slice(0, 50),
    },
    orphans: { items: orphanItems, blocks: orphanBlocks },
  };

  const outPath = path.join(__dirname, 'audit-report.json');
  fs.writeFileSync(outPath, JSON.stringify(report, null, 2));
  console.log('Texture audit');
  console.log('  PNGs:', report.counts.total_png, `(item ${report.counts.item_png}, block ${report.counts.block_png}, entity ${report.counts.entity_png})`);
  console.log('  Missing items:', missingItem.length);
  console.log('  Missing blocks:', missingBlock.length);
  console.log('  Missing entities:', missingEntity.length);
  console.log('  Missing item models:', missingModels.length);
  if (missingItem.length) console.log('  Sample missing items:', missingItem.slice(0, 10).join(', '));
  console.log('  ->', outPath);
}

main();
