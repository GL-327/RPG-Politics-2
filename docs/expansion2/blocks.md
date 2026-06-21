# Expansion 2 — Decoration & Building Blocks

Phase-2 palette expansion: **120 decorative full-cube blocks** for settlement
building — bricks, wood trims, metals, gems/glass, roofing, paths, banners,
ornate stone, modern facades, and light sources.

All blocks are plain `Block` full cubes (no mixins, no custom render layers).
Every block has seamless 16×16 tiling textures plus matching block model,
blockstate, and item-definition JSON.

- **Code:** `com/political/expansion2/blocks/DecoBlocks2.java`
- **Registry/id prefix:** `dec2_` (namespace `politicalserver`)
- **Texture generator:** `tools/gen-blocks2.js` (Node + jimp)
- **Translations:** `tools/lang-fragments/blocks2.json`

## Block count by category (120)

| Category | Count | Notes |
|----------|------:|-------|
| Brick variants | 20 | Running-bond, glazed, weathered/mossy |
| Wood trims | 15 | Horizontal plank trim boards |
| Metals | 15 | Brushed plate, weave, rust/patina |
| Gems / glass | 15 | Faceted gem-glass; several emit light |
| Roof types | 15 | Shingles, shakes, thatch, standing-seam |
| Paths | 10 | Cobble, gravel, hex, marble, etc. |
| Banners / flags | 10 | Cloth panels with emblem |
| Ornate | 10 | Framed stone, carved relief, **4 light sources** |
| Modern facade | 10 | Concrete, coloured panels, glass wall, neon |
| **Total** | **120** | **17 blocks emit light** |

### Light-emitting blocks

| ID | Light | Category |
|----|------:|----------|
| `dec2_ruby_glass` | 5 | Gem |
| `dec2_sapphire_glass` | 4 | Gem |
| `dec2_emerald_glass` | 5 | Gem |
| `dec2_amethyst_glass` | 4 | Gem |
| `dec2_topaz_glass` | 4 | Gem |
| `dec2_opal_glass` | 6 | Gem |
| `dec2_aquamarine_glass` | 4 | Gem |
| `dec2_garnet_glass` | 4 | Gem |
| `dec2_citrine_glass` | 5 | Gem |
| `dec2_peridot_glass` | 4 | Gem |
| `dec2_tourmaline_glass` | 4 | Gem |
| `dec2_cobalt_glass` | 4 | Gem |
| `dec2_prism_glass` | 10 | Gem |
| `dec2_brazier` | 14 | Ornate |
| `dec2_wall_sconce` | 13 | Ornate |
| `dec2_crystal_lamp` | 15 | Ornate |
| `dec2_oil_lantern` | 12 | Ornate |
| `dec2_neon_panel` | 12 | Modern |

## Integration (wired)

1. **Register** — `DecoBlocks2.register()` is called from `RpgPoliticsMod#onInitialize`
   immediately after Phase-1 `DecoBlocks.register()`.
2. **Creative tab** — `CreativeCatalog.build(...)` appends `DecoBlocks2.blocks()` to
   the "RPG — Settlements & Build" tab after Phase-1 decoration blocks.
3. **Translations** — `tools/lang-fragments/blocks2.json` merged into
   `assets/politicalserver/lang/en_us.json`.

## Regenerating textures & assets

```bash
cd tools
node gen-blocks2.js
```

Writes PNGs to `assets/politicalserver/textures/block/`, plus block models,
blockstates, item definitions, and a QA montage at `.texref/blocks2_montage.png`.
Only touches `dec2_` ids — never modifies Phase-1 `dec_` assets or `gen-blocks.js`.

## API

```java
DecoBlocks2.register();          // register all 120 blocks + block items
List<Block> all = DecoBlocks2.blocks();  // declaration order for creative tabs
Map<String, Block> map = DecoBlocks2.ALL; // id -> block lookup
```

## Ownership boundaries

- **Owned:** `com/political/expansion2/blocks/**`, `dec2_` ids, `gen-blocks2.js`,
  `lang-fragments/blocks2.json`, this doc.
- **Do not touch:** `com/political/expansion/blocks/**`, `ModBlocks.java`, Phase-1
  `dec_` assets, mixins.
