/**
 * Run every texture generator in dependency-safe order.
 * Usage: node regenerate-all.js
 */
const { spawnSync } = require('child_process');
const path = require('path');

const SCRIPTS = [
  'generate-textures.js',
  'gen-melee.js',
  'gen-melee2.js',
  'gen-ranged.js',
  'gen-ranged2.js',
  'gen-armor.js',
  'gen-armor2.js',
  'gen-accessories.js',
  'gen-accessories2.js',
  'gen-blocks.js',
  'gen-blocks2.js',
  'gen-food.js',
  'gen-food2.js',
  'gen-mobs.js',
  'gen-mobs2.js',
  'gen-cursespirits.js',
  'gen-cursespirits2.js',
  'gen-quests2.js',
  'gen-guide.js',
  'build-entity-montages.js',
];

const toolsDir = __dirname;
let failed = 0;
const results = [];

for (const script of SCRIPTS) {
  const full = path.join(toolsDir, script);
  const t0 = Date.now();
  process.stdout.write(`\n=== ${script} ===\n`);
  const r = spawnSync(process.execPath, [full], { cwd: toolsDir, stdio: 'inherit', encoding: 'utf8' });
  const ms = Date.now() - t0;
  if (r.status !== 0) {
    failed++;
    results.push({ script, ok: false, ms });
    console.error(`FAILED: ${script} (exit ${r.status})`);
  } else {
    results.push({ script, ok: true, ms });
  }
}

console.log('\n--- regenerate-all summary ---');
for (const r of results) console.log(`${r.ok ? 'OK' : 'FAIL'} ${r.script} (${r.ms}ms)`);
if (failed) process.exit(1);
console.log('All generators completed.');
