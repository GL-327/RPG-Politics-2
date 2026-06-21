# Expansion — Decoration & Building Blocks

A large expansion of the "neo-medieval modern" settlement palette: **42 decorative
full-cube blocks** for building and decoration, inspired by the building/decoration
mod families used in Prominence II / RAD2 packs (Macaw's, Chipped, Rustic, etc.).

All blocks are plain `Block` full cubes (no custom render layers) — the look comes
entirely from seamless, hand-tuned 16×16 tiling textures. Every block has a matching
block model, blockstate, item-definition JSON and item texture, following the exact
pattern of `com.political.content.ModBlocks`.

- **Code:** `com/political/expansion/blocks/DecoBlocks.java`
- **Registry/id prefix:** `dec_` (namespace `politicalserver`)
- **Texture generator:** `tools/gen-blocks.js` (Node + jimp; standalone)
- **Translations:** `tools/lang-fragments/blocks.json`

## Block list (42)

### Decorative bricks (6)
| ID | Name | Notes |
|----|------|-------|
| `dec_red_brick` | Red Brick | Running-bond masonry |
| `dec_blonde_brick` | Blonde Brick | Sandy-yellow brick |
| `dec_charcoal_brick` | Charcoal Brick | Near-black brick |
| `dec_ivory_brick` | Ivory Brick | Off-white brick |
| `dec_mossy_castle_brick` | Mossy Castle Brick | Stone brick with moss speckle |
| `dec_herringbone_brick` | Herringbone Brick | Basketweave/parquet bond |

### Tiles (4)
| ID | Name | Notes |
|----|------|-------|
| `dec_checkered_tile` | Checkered Tile | Ivory/charcoal checkerboard |
| `dec_terracotta_tile` | Terracotta Tile | Grouted square tiles |
| `dec_azure_tile` | Azure Tile | Grouted square tiles |
| `dec_emerald_tile` | Emerald Tile | Grouted square tiles |

### Marble (5)
| ID | Name | Notes |
|----|------|-------|
| `dec_white_marble` | White Marble | Veined polished marble |
| `dec_black_marble` | Black Marble | Veined polished marble |
| `dec_rose_marble` | Rose Marble | Veined polished marble |
| `dec_cobalt_marble` | Cobalt Marble | Veined polished marble |
| `dec_marble_pillar` | Marble Pillar | Fluted marble column |

### Granite (2)
| ID | Name | Notes |
|----|------|-------|
| `dec_grey_granite` | Grey Granite | Speckled igneous stone |
| `dec_pink_granite` | Pink Granite | Speckled igneous stone |

### Stained facades (5)
| ID | Name | Notes |
|----|------|-------|
| `dec_crimson_facade` | Crimson Facade | Rich-colour rendered facade |
| `dec_cobalt_facade` | Cobalt Facade | Rich-colour rendered facade |
| `dec_jade_facade` | Jade Facade | Rich-colour rendered facade |
| `dec_amber_facade` | Amber Facade | Rich-colour rendered facade |
| `dec_violet_facade` | Violet Facade | Rich-colour rendered facade |

### Columns / pillars (2)
| ID | Name | Notes |
|----|------|-------|
| `dec_fluted_column` | Fluted Column | Quartz-white fluted shaft |
| `dec_sandstone_column` | Sandstone Column | Sandy fluted shaft |

### Lamps & lanterns — light-emitting (4)
| ID | Name | Light | Notes |
|----|------|-------|-------|
| `dec_iron_lantern` | Iron Lantern | 14 | Caged cool-white glow |
| `dec_gold_lantern` | Gold Lantern | 15 | Caged warm glow |
| `dec_glowstone_lamp` | Glowstone Lamp | 15 | Warm glow lamp |
| `dec_paper_lantern` | Paper Lantern | 13 | Soft diffuse red glow |

### Roofing (4)
| ID | Name | Notes |
|----|------|-------|
| `dec_slate_shingles` | Slate Shingles | Fish-scale roof |
| `dec_red_shingles` | Red Shingles | Fish-scale roof |
| `dec_copper_roof` | Copper Roof | Standing-seam metal with patina |
| `dec_thatch_roof` | Thatch Roof | Straw thatch |

### Ornate stone (3)
| ID | Name | Light | Notes |
|----|------|-------|-------|
| `dec_ornate_stone` | Ornate Stone | – | Framed panel with gold boss |
| `dec_carved_stone` | Carved Stone | – | Greek-key (fret) relief |
| `dec_runic_stone` | Runic Stone | 8 | Glowing rune ring |

### Plaster & structural (4)
| ID | Name | Notes |
|----|------|-------|
| `dec_cream_plaster` | Cream Plaster | Smooth stucco render |
| `dec_timber_frame` | Timber Frame | Tudor daub + dark beams |
| `dec_sandstone_block` | Sandstone Block | Layered sedimentary |
| `dec_polished_basalt` | Polished Basalt | Columnar dark stone |

### Banners (3)
| ID | Name | Notes |
|----|------|-------|
| `dec_blue_banner_block` | Blue Banner Block | Cloth with gold emblem |
| `dec_green_banner_block` | Green Banner Block | Cloth with gold emblem |
| `dec_gold_banner_block` | Gold Banner Block | Cloth with ivory emblem |

## Integration (required)

This module is self-contained and does **not** register itself. The integration
agent must:

1. **Register the blocks** during mod init — call `DecoBlocks.register()` alongside
   the existing `ModBlocks.register()` (e.g. in `RpgPoliticsMod`). It must run during
   common/registration init so the BLOCK + ITEM registry entries exist.
2. **Add to the creative tab** — in `CreativeCatalog.build(...)`, append the expansion
   blocks to the "RPG — Settlements & Build" tab:

   ```java
   for (var block : com.political.expansion.blocks.DecoBlocks.blocks()) {
       out.accept(new ItemStack(block));
   }
   ```
3. **Merge translations** — fold `tools/lang-fragments/blocks.json` into
   `assets/politicalserver/lang/en_us.json`.

## Regenerating textures

```bash
cd tools
node gen-blocks.js
```

Requires `tools/node_modules` (jimp). Writes PNGs to
`assets/politicalserver/textures/block/` plus block models, blockstates and item
definitions, and a QA montage to `.texref/blocks_montage.png`. Only touches `dec_`
ids — it never modifies the core `ModBlocks` assets or `generate-textures.js`.
