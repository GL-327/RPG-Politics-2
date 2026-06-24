/**
 * Imports Terralith (MIT datapack), Biomes O' Plenty, and Biomes We've Gone biome
 * definitions into src/main/resources. BOP/BWG custom blocks are Java-only, so their
 * feature lists are replaced with vanilla-compatible templates while keeping climate,
 * colors, and spawns.
 */
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { execSync } from 'child_process';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(__dirname, '..');
const RES = path.join(ROOT, 'src', 'main', 'resources');
const DATA = path.join(RES, 'data');
const ASSETS = path.join(RES, 'assets');

const JARS = {
  bop: 'c:\\Users\\Arthur\\Downloads\\BiomesOPlenty-fabric-26.1.2-26.1.2.0.22.jar',
  bwg: 'c:\\Users\\Arthur\\Downloads\\Oh-The-Biomes-Weve-Gone-Fabric-4.4.0.jar',
};
const TERRALITH = path.join(ROOT, '.extract', 'terralith', 'full');
const STAGING = path.join(ROOT, '.extract', 'import-staging');

function readJson(file) {
  const text = fs.readFileSync(file, 'utf8').replace(/^\uFEFF/, '');
  return JSON.parse(text);
}

const VANILLA_FEATURES = readJson(
  path.join(DATA, 'politicalserver', 'worldgen', 'biome', 'prairie_expanse.json')
).features;

function ensureDir(p) {
  fs.mkdirSync(p, { recursive: true });
}

function copyDir(src, dest) {
  if (!fs.existsSync(src)) return;
  ensureDir(dest);
  if (process.platform === 'win32') {
    try {
      execSync(`robocopy "${src}" "${dest}" /E /NFL /NDL /NJH /NJS /nc /ns /np`, { stdio: 'ignore' });
    } catch (e) {
      // robocopy exit codes 0-7 indicate success/partial copy
      const code = e.status ?? 0;
      if (code > 7) throw e;
    }
  } else {
    execSync(`cp -a "${src}/." "${dest}/"`, { stdio: 'inherit' });
  }
}

function extractJar(jarPath, destDir) {
  ensureDir(destDir);
  if (process.platform === 'win32') {
    execSync(
      `powershell -NoProfile -Command "` +
        `$d='${destDir.replace(/'/g, "''")}'; if(Test-Path $d){Remove-Item $d -Recurse -Force}; ` +
        `Add-Type -AssemblyName System.IO.Compression.FileSystem; ` +
        `[System.IO.Compression.ZipFile]::ExtractToDirectory('${jarPath.replace(/'/g, "''")}', $d)"`,
      { stdio: 'inherit' }
    );
  } else {
    execSync(`unzip -o -q "${jarPath}" -d "${destDir}"`, { stdio: 'inherit' });
  }
}

function stripModRefs(obj, modId) {
  if (Array.isArray(obj)) {
    const out = [];
    for (const v of obj) {
      if (typeof v === 'string' && v.startsWith(`${modId}:`)) continue;
      const cleaned = stripModRefs(v, modId);
      if (cleaned !== undefined && !(Array.isArray(cleaned) && cleaned.length === 0 && typeof v === 'object')) {
        out.push(cleaned);
      }
    }
    return out;
  }
  if (obj && typeof obj === 'object') {
    const out = {};
    for (const [k, v] of Object.entries(obj)) {
      if (typeof v === 'string' && v.includes(`${modId}:`)) continue;
      out[k] = stripModRefs(v, modId);
    }
    return out;
  }
  return obj;
}

function sanitizeBiome(raw, modId) {
  const biome = stripModRefs(JSON.parse(JSON.stringify(raw)), modId);
  biome.features = JSON.parse(JSON.stringify(VANILLA_FEATURES));
  return biome;
}

function biomeParameters(temperature, downfall, index, total) {
  const t = typeof temperature === 'number' ? temperature : 0.5;
  const h = typeof downfall === 'number' ? downfall : 0.5;
  const band = index / Math.max(1, total);
  const weirdMin = -0.95 + band * 1.8;
  const weirdMax = weirdMin + 0.08;
  return {
    temperature: [Math.max(-1, t * 2 - 1.05), Math.min(1, t * 2 - 0.85)],
    humidity: [Math.max(-1, h * 2 - 1.05), Math.min(1, h * 2 - 0.85)],
    continentalness: [-0.15, 0.35],
    erosion: [-0.35, 0.35],
    weirdness: [weirdMin, weirdMax],
    depth: 0,
    offset: (index % 97) * 0.0001,
  };
}

function collectBiomeEntries(biomeDir, namespace) {
  if (!fs.existsSync(biomeDir)) return [];
  const files = fs.readdirSync(biomeDir).filter(f => f.endsWith('.json'));
  return files.map((file, i) => {
    const id = file.replace(/\.json$/, '');
    const raw = readJson(path.join(biomeDir, file));
    const cleaned = sanitizeBiome(raw, namespace);
    fs.writeFileSync(path.join(biomeDir, file), JSON.stringify(cleaned, null, 4));
    return {
      biome: `${namespace}:${id}`,
      parameters: biomeParameters(raw.temperature, raw.downfall, i, files.length),
    };
  });
}

function mergeOverworld(extraEntries) {
  const overworldPath = path.join(DATA, 'minecraft', 'dimension', 'overworld.json');
  const raw = readJson(overworldPath);
  const src = raw.generator?.biome_source;
  if (!src || !Array.isArray(src.biomes)) {
    throw new Error('Expected minecraft:dimension/overworld.json with generator.biome_source.biomes[]');
  }
  const existing = new Set(src.biomes.map(b => b.biome));
  for (const entry of extraEntries) {
    if (!existing.has(entry.biome)) {
      src.biomes.push(entry);
      existing.add(entry.biome);
    }
  }
  fs.writeFileSync(overworldPath, JSON.stringify(raw, null, 4));
  console.log(`Overworld biomes: ${src.biomes.length} total (+${extraEntries.length} requested)`);
}

console.log('=== Terralith datapack ===');
copyDir(path.join(TERRALITH, 'data', 'terralith'), path.join(DATA, 'terralith'));
copyDir(path.join(TERRALITH, 'data', 'c'), path.join(DATA, 'c'));
copyDir(path.join(TERRALITH, 'data', 'minecraft', 'worldgen'), path.join(DATA, 'minecraft', 'worldgen'));
copyDir(path.join(TERRALITH, 'data', 'minecraft', 'dimension'), path.join(DATA, 'minecraft', 'dimension'));
copyDir(path.join(TERRALITH, 'assets', 'terralith'), path.join(ASSETS, 'terralith'));
console.log('Terralith data + assets copied.');

console.log('=== Biomes O Plenty ===');
ensureDir(STAGING);
extractJar(JARS.bop, path.join(STAGING, 'bop'));
copyDir(path.join(STAGING, 'bop', 'data', 'biomesoplenty', 'worldgen', 'biome'), path.join(DATA, 'biomesoplenty', 'worldgen', 'biome'));
copyDir(path.join(STAGING, 'bop', 'assets', 'biomesoplenty', 'lang'), path.join(ASSETS, 'biomesoplenty', 'lang'));
const bopEntries = collectBiomeEntries(path.join(DATA, 'biomesoplenty', 'worldgen', 'biome'), 'biomesoplenty');
console.log(`BOP biomes sanitized: ${bopEntries.length}`);

console.log('=== Biomes We\'ve Gone ===');
extractJar(JARS.bwg, path.join(STAGING, 'bwg'));
copyDir(path.join(STAGING, 'bwg', 'data', 'biomeswevegone', 'worldgen', 'biome'), path.join(DATA, 'biomeswevegone', 'worldgen', 'biome'));
copyDir(path.join(STAGING, 'bwg', 'assets', 'biomeswevegone', 'lang'), path.join(ASSETS, 'biomeswevegone', 'lang'));
const bwgEntries = collectBiomeEntries(path.join(DATA, 'biomeswevegone', 'worldgen', 'biome'), 'biomeswevegone');
console.log(`BWG biomes sanitized: ${bwgEntries.length}`);

console.log('=== Politicalserver biomes ===');
const polEntries = collectBiomeEntries(path.join(DATA, 'politicalserver', 'worldgen', 'biome'), 'politicalserver');

console.log('=== Merge overworld.json ===');
mergeOverworld([...polEntries, ...bopEntries, ...bwgEntries]);
console.log('Done.');
