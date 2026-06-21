# Texture Audit — politicalserver

Last run: 2026-06-21 (mega-overhaul regeneration)

## Summary

| Category | PNG count | Generator | QA montage |
|----------|-----------|-----------|------------|
| Core items (RpgItem, CursedGear, Relics, Gov, serums) | 89 | `generate-textures.js` | `.texref/core_montage.png` |
| Core blocks | 10 | `generate-textures.js` | (in core montage) |
| Melee expansion | 31 | `gen-melee.js` | `.texref/melee_montage.png` |
| Melee2 expansion | 96 | `gen-melee2.js` | `.texref/melee2_montage.png` |
| Ranged expansion | 35 | `gen-ranged.js` | `.texref/ranged_montage.png` |
| Ranged2 expansion | 92 | `gen-ranged2.js` | `.texref/ranged2_montage.png` |
| Armor expansion | 56 | `gen-armor.js` | `.texref/armor_montage.png` |
| Armor2 expansion | 144 | `gen-armor2.js` | `.texref/armor2_montage.png` |
| Accessories expansion | 50 | `gen-accessories.js` | `.texref/accessories_montage.png` |
| Accessories2 expansion | 165 | `gen-accessories2.js` | `.texref/accessories2_montage.png` |
| Deco blocks | 42 | `gen-blocks.js` | `.texref/blocks_montage.png` |
| Deco blocks2 | 120 | `gen-blocks2.js` | `.texref/blocks2_montage.png` |
| Food expansion | 45 | `gen-food.js` | `.texref/food_montage.png` |
| Food2 expansion | 160 | `gen-food2.js` | `.texref/food2_montage.png` |
| Quest2 items | 15 | `gen-quests2.js` | `.texref/quests2_montage.png` |
| Phase-1 mobs | 23 | `gen-mobs.js` | `.texref/mobs_montage.png` |
| Phase-2 mobs | 115 | `gen-mobs2.js` | `.texref/mobs2_montage.png` |
| Cursed spirits (P1) | 21 | `gen-cursespirits.js` | `.texref/entities_misc_montage.png` |
| Cursed spirits2 | 90 | `gen-cursespirits2.js` | `.texref/spirits2_montage.png` |
| Villager + misc entities | 23 | `generate-textures.js` / `gen-mobs.js` | `.texref/entities_misc_montage.png` |

**Total on disk: 1,414 PNGs** (980 item · 183 block · 251 entity)

**Regenerated this pass: ~1,400** across 18 generator scripts (`node tools/regenerate-all.js`).

## Shared pixel-art library

All item/block generators now share `tools/pixel-art-lib.js`:

- **5-tone ramps** per material (shadow → specular)
- **`finishSprite()`** pipeline: ambient-occlusion interior corners + near-black silhouette outline
- **Material helpers**: `metalSheen`, `leatherGrain`, `woodGrain`, `gemFacets`, `foodGloss`, `magicGlow`
- **Batch writers**: `generateItemBatch`, `generateBlockBatch`, `buildMontage`
- **Archetype draw functions**: swords, armor pieces, gems, blocks, etc.

Refactored to use the lib directly: `generate-textures.js`, `gen-melee.js`, `gen-melee2.js`.  
Patched to use `finishSprite`: ranged, armor, accessories, food, quests generators.

## Quality improvements

Compared to the prior duplicated-helper generators:

1. **Stronger silhouettes** — darker outline tint (42% of shadow tone vs 50%)
2. **Ambient occlusion** — interior corner pixels darkened for depth/readability at 16×16
3. **Optional magic glow** on gem/arcane core items (`finish: { glow: true }`)
4. **Unified montages** — all QA sheets under `.texref/` (food montages moved from `.texref-food*`)
5. **Entity review sheets** — 64×64 montages at 2× scale for mobs/spirits

Style reference: Hypixel Skyblock item clarity, JJK cursed-tool purple accents, hand-crafted pixel clusters at native resolution.

## Model wiring

- Every generated item writes `models/item/<id>.json` + `items/<id>.json`
- Every generated block writes `models/block/<id>.json` + `blockstates/<id>.json` + block item def
- **ITEM_MODEL component** verified on:
  - `RpgItems.create()` → all RpgItem gear
  - `MeleeWeapons` / `Melee2Weapons` → all expansion melee
  - `RangedItems` / `RangedItems2` → all expansion ranged
  - `ArmorItems` / `Armor2Items` → all expansion armor stacks
- Registered items (CursedGear, RelicItems, GovItems, accessories, food) use **item definition JSON** at `assets/politicalserver/items/<id>.json` — correct for MC 26.2 without per-stack override

## Missing / intentionally skipped

| ID | Status |
|----|--------|
| `dungeon_compass` | **Intentionally skipped** — dungeon feature removed; no texture/model |
| `gen-powers.js` | **Not needed** — powers are abilities, not items (see `docs/expansion/powers.md`) |

No other generator-backed texture IDs are missing PNGs or model JSON.

## Re-run commands

```powershell
cd tools
npm install
node regenerate-all.js
node audit-textures.js
```

## Files owned by texture pipeline

- `tools/pixel-art-lib.js` — shared drawing library
- `tools/generate-textures.js` + all `tools/gen-*.js`
- `tools/regenerate-all.js`, `tools/audit-textures.js`, `tools/build-entity-montages.js`
- `src/main/resources/assets/politicalserver/textures/**`
- `.texref/**` — QA montages (not shipped in mod JAR)
