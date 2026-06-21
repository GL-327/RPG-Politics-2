# Workstream B — Content Porting — Handoff Manifest

**Mod id:** `politicalserver` · **MC 26.2 / Fabric / Java 25** · **Build status: GREEN**
(`.\gradlew.bat build` → `BUILD SUCCESSFUL`; only pre-existing JEI deprecation warnings from other
workstreams remain.)

All content below lives in **new files only**. Nothing is wired into the shared entrypoints yet, so
the slices are dormant until the two registration lines (see [§7](#7-registration-one-liners)) are
added. Classes compile and are registry-safe today.

---

## 1. Biomes (datapack worldgen + Fabric Biome Modification API)

Five original biomes authored as **datapack JSON** (no TerraBlender). Studied biome/feature JSON
shape from Terralith; all names, colours and feature mixes are original.

**Data** (`src/main/resources/data/politicalserver/worldgen/`):

| Kind | Files |
|------|-------|
| `biome/` | `auric_steppe.json`, `ashen_barrens.json`, `mistveil_fen.json`, `gloamwood.json`, `frostpetal_tundra.json` |
| `configured_feature/` | `auric_bloom.json` (golden flower patch), `gloomcap_cluster.json` (dim mushroom cluster) |
| `placed_feature/` | `auric_bloom.json`, `gloomcap_cluster.json` |

**Injector** — `com.political.world.biome.ContentBiomes` (`register()`): uses
`BiomeModifications.addFeature(...)` to scatter the two original placed features into thematically
matching vanilla biomes (`auric_bloom` → plains/meadow/savanna/snowy; `gloomcap_cluster` → forests +
swamps).

> **Assumption / follow-up:** the five standalone biome JSONs are valid and loadable, but making them
> *generate* as first-class overworld biomes requires adding them to a multi-noise parameter-list
> datapack. This is intentionally left out of code to stay TerraBlender-free and non-invasive — it is
> a pure-datapack follow-up. The decorative features above generate immediately once `register()` is
> called.

---

## 2. Structures (original set on the Build/BuildBuffer pipeline)

Studied jigsaw/template-pool layout from Towns & Towers / Dungeons Arise; authored **original**
layouts from the existing `Build` / `SurfaceGenUtil` primitives (no copied NBT/templates), captured
into a `BuildBuffer` (same deferred pipeline the settlement & dungeon systems use), then drained +
post-processed via `StructureLoot`.

**New files** (`com.political.world.structures`):

- `ContentStructureKind` — enum of 3 originals: `WAYSTONE_SHRINE`, `RANGER_OUTPOST`, `LEYLINE_NEXUS`.
  Each borrows an existing `StructureType` **only** for its loot table + site bookkeeping; geometry is
  independent.
- `ContentStructures` — `placeNow(ServerLevel, cx, cz, kind)` builds into a `BuildBuffer`, streams it
  into the world, and applies loot/mob/villager/cursed-spirit post-ops. `planInto(...)` exposes the
  deferred plan for integration into auto-scatter.

> **Integration follow-up:** these are command/placer-ready but not auto-scattered. To add to world
> scatter, call `ContentStructures.planInto(buffer, level, cx, cz, kind, rng)` from `StructureManager`
> (or add a debug command `/political content struct <kind>` calling `ContentStructures.placeNow`).
> Left out of shared files to avoid conflicts.

---

## 3. Accessories (artifacts-inspired ability wearables)

Extends the `expansion2` accessory system with **9** original wearable accessories that map artifact
**ability** ideas onto vanilla-API effects. Reimplemented from MIT Artifacts mechanics (no asset
reuse). All in `com.political.expansion2.accessories` (new files — existing `Accessories2*` untouched).

**New files:**

- `AccessoryAbility2` — enum of abilities: `FLIGHT`, `MAGNET`, `FIRE_WARD`, `ANTI_KNOCKBACK`,
  `AQUATIC`, `STEP_ASSIST`, `NIGHT_SIGHT`, `GRAZING`, `FEATHERFALL`.
- `AbilityAccessoryDef2` — data record for one ability accessory.
- `AbilityAccessoriesCatalog2` — the 9 definitions (original names + flavour).
- `AbilityAccessories2` — the engine: registers items, and on a server tick applies the active
  effect while the accessory is held/worn (creative-flight grant/revoke, item+XP magnetism, fire
  resistance, knockback-resistance & step-height attribute modifiers, aquatic effects, night vision,
  slow-falling, grass grazing). Self-contained tooltip decoration. Public: `register()`, `items()`.

| Id | Name | Type | Rarity | Ability |
|----|------|------|--------|---------|
| `acc2_ab_aetherwing` | Aetherwing Charm | CHARM | MYTHIC | Creative-style flight |
| `acc2_ab_lodestone_locket` | Lodestone Locket | AMULET | RARE | Item/XP magnet (7 blocks) |
| `acc2_ab_cinderheart` | Cinderheart Band | BAND | EPIC | Fire & lava immunity |
| `acc2_ab_bulwark_totem` | Bulwark Totem | TOTEM | UNCOMMON | Knockback immunity |
| `acc2_ab_tidecaller` | Tidecaller Pendant | AMULET | RARE | Water breathing + dolphin's grace + deep sight |
| `acc2_ab_highstep` | Highstep Anklet | BAND | UNCOMMON | Step up full blocks |
| `acc2_ab_owls_eye` | Owl's Eye Talisman | TALISMAN | COMMON | Permanent night vision |
| `acc2_ab_verdant_idol` | Verdant Sustenance Idol | RELIC | UNCOMMON | Replenishes hunger on grass |
| `acc2_ab_featherfall` | Featherfall Sigil | RUNE | EPIC | Negates fall damage |

Textures/models/item-defs generated for all 9 (see [§5](#5-generated-assets--tools)).

---

## 4. Creatures (2-4 originals with vanilla-Goal AI + custom models)

Three original ambient creatures, vanilla-AI driven (behaviour shape studied from MIT Naturalist;
stats/names/art original). Breeding intentionally disabled (ambient fauna).

**Common/server** (`com.political.content.creatures`):

- `CreatureSpecies` — enum: `MEADOW_STAG` (prey, forests/meadows/plains), `RIDGEBACK_TORTOISE`
  (stoic, swamp/mangrove/beach), `GLIMMERMOTH` (prey, dark forest/taiga). Stats, dimensions, spawn
  weights and per-species `biomeSelector()`.
- `ContentCreature` — `Animal` subclass; vanilla goals (`FloatGoal`, `PanicGoal`,
  `WaterAvoidingRandomStrollGoal`, `LookAtPlayerGoal`, `RandomLookAroundGoal`, conditional
  `HurtByTargetGoal`). `isFood`/breeding return null/false.
- `ContentCreatures` — registers `EntityType`s, default attributes, ground spawn rules, and injects
  natural biome spawns via `BiomeModifications`. Public: `register()`, `type(species)`.

**Client** (`com.political.content.creatures.client`):

- `StagModel`, `TortoiseModel`, `MothModel` — bespoke `EntityModel`s with idle/walk animation.
- `CreatureRenderState`, `CreatureRenderer` — shared render state + generic `MobRenderer`.
- `ContentCreaturesClient` (`registerClient()`) — registers model layers + renderers per species.

Entity skins generated: `textures/entity/{meadow_stag,ridgeback_tortoise,glimmermoth}.png`.

---

## 5. Generated assets & tools

**New generator:** `tools/gen-content2.js` (self-contained; reuses `tools/pixel-art-lib.js`
`finishSprite`). Run with `node tools/gen-content2.js`. Produces:

- `assets/politicalserver/textures/item/acc2_ab_*.png` (9 × 16×16 sprites)
- `assets/politicalserver/models/item/acc2_ab_*.json` + `assets/politicalserver/items/acc2_ab_*.json`
- `assets/politicalserver/textures/entity/{meadow_stag,ridgeback_tortoise,glimmermoth}.png`
- `.texref/content2_accessories.png` (preview montage)
- the lang fragment (next section)

---

## 6. Lang fragment

**New keys** written to `src/main/resources/assets/politicalserver/lang/en_us.content.json`
(item names ×9, entity names ×3, biome names ×5). **Please merge into `en_us.json`.**

---

## 7. Registration one-liners

These are the **only** shared-file edits needed to activate everything. They are deliberately left
for the integrator:

```java
// RpgPoliticsMod.onInitialize()  (common)
com.political.content.ContentBootstrap.init();

// PoliticalClient.onInitializeClient()  (client)
com.political.content.client.ContentClientBootstrap.initClient();
```

`ContentBootstrap.init()` calls `AbilityAccessories2.register()`, `ContentCreatures.register()`,
`ContentBiomes.register()`. `ContentClientBootstrap.initClient()` calls
`ContentCreaturesClient.registerClient()` (kept in the client source set because Loom separates it).

Optional creative-tab wiring (no shared edit required to build): iterate
`AbilityAccessories2.items()` in `ModTabs`/`CreativeCatalog`.

---

## 8. Mixins

**None.** Everything uses the Fabric Biome Modification API, datapack worldgen, vanilla
`EntityType`/`Goal`/attribute modifiers, registries, and server tick events. No mixin configs touched.

---

## 9. Political / economy idea-recovery (from prior builds)

Scanned the two older `politicalserver` builds under `reference/extracted/`:
`CivilCraft-Mod-V3.4.8-Vanilla+` (the richest) and `Economies-and-Politics-Mod-V1.0` (an earlier
subset). Both are server-side election mods built on the **sgui** server-GUI library; their feature
surface is visible from the mixin rosters + datapack data.

The current tree already covers most political/economy systems (`economy/` has
Auction/Bank/Currency/Market/Shop; `gov/` has Election/Perk/Tax/Prison/Dictator; `court/`, `civics/`,
`bounty/` exist). So the recovery value is mostly in **RPG-combat / world systems** the old builds had.
Cross-check each against existing `combat`/`progression`/`items` packages before implementing.

**Candidates worth restoring** (mixin → proposed home):

| Prior feature (mixin) | What it did | Proposed home |
|-----------------------|-------------|---------------|
| `BountyDamageMixin`, `BountyDefenseMixin` | Bonus damage vs / defense against players with active bounties | `bounty/` (likely partly present — verify combat hooks) |
| `BerserkerDamageMixin` | Low-HP rage damage scaling (RPG class flavour) | `combat/` or `progression/` |
| `LevelLockedWeaponMixin` | Gate weapon use behind player RPG level | `progression/` + `items/` |
| `CustomEnchantDamageMixin`, `EnchantmentScreenHandlerMixin` | Custom enchants + custom enchanting table | `items/` / `combat/` |
| `ArrowHeadshotMixin`, `ArrowHomingMixin`, `ArrowDamageMixin` | Headshot crits, homing arrows, tuned arrow damage | `combat/` |
| `LootModifierMixin`, `LootTableMixin` | Dynamic loot scaling / injection | `economy/` (loot→economy) or `world/` |
| `MiningSpeedMixin`, `OreDropMixin` | Skill-based mining speed + scaled ore drops | `progression/` + `economy/` |
| `TradeOfferMixin`, `VillagerInteractionMixin`, `VillagerLightningMixin` | Custom villager trades, interaction gating, lightning→witch conversion | `economy/` (`MarketManager`/villager shops) |
| `TotemDisableMixin` | Disable Totem of Undying in PvP/arena | `combat/` or `court/` (justice/arena) |
| `AuctionNPCDamageMixin` | Protect auction-house NPCs from damage | `economy/AuctionManager` companion helper |
| `PatrolSpawnerMixin`, `PhantomSpawnerMixin`, `SpawnHelperMixin`, `SlimeEntityMixin` | Tuned hostile spawning (server balance) | `world/` spawn tuning |
| `WardenDropMixin`, `WardenEntityMixin`, `EndGatewayMixin` | Endgame: warden drops + custom End gateway | `world/` / `progression/` endgame |
| Custom dimension `fracture_3` (`data/.../dimension{,_type}/`) | A bespoke "fracture" dimension (datapack) | `world/` + datapack dimension (recover the JSON) |
| Crafting gating (`AnvilCrafting`, `Smithing`, `Grindstone`, `Crafting*` mixins) | Recipe/result restrictions for economy balance | `economy/` recipe-policy helper |

**Self-contained helper added (optional, compiles, unregistered):** none required for green — the
above are proposals. If desired I can add a `com.political.content.recovery` package of
self-contained helpers (e.g. a headshot damage calculator, a bounty-damage multiplier function) that
the owning workstreams can call, without touching their files.

---

## 10. File inventory (new files)

**Common (`src/main/java`):**
- `com/political/world/biome/ContentBiomes.java`
- `com/political/world/structures/ContentStructureKind.java`, `ContentStructures.java`
- `com/political/expansion2/accessories/AccessoryAbility2.java`, `AbilityAccessoryDef2.java`, `AbilityAccessoriesCatalog2.java`, `AbilityAccessories2.java`
- `com/political/content/creatures/CreatureSpecies.java`, `ContentCreature.java`, `ContentCreatures.java`
- `com/political/content/ContentBootstrap.java`

**Client (`src/client/java`):**
- `com/political/content/creatures/client/CreatureRenderState.java`, `StagModel.java`, `TortoiseModel.java`, `MothModel.java`, `CreatureRenderer.java`, `ContentCreaturesClient.java`
- `com/political/content/client/ContentClientBootstrap.java`

**Resources (`src/main/resources`):**
- `data/politicalserver/worldgen/biome/*.json` (5), `configured_feature/*.json` (2), `placed_feature/*.json` (2)
- `assets/politicalserver/lang/en_us.content.json`
- `assets/politicalserver/textures/item/acc2_ab_*.png` (9) + `models/item/acc2_ab_*.json` (9) + `items/acc2_ab_*.json` (9)
- `assets/politicalserver/textures/entity/{meadow_stag,ridgeback_tortoise,glimmermoth}.png`

**Tools:** `tools/gen-content2.js`

---

## 11. Assumptions & follow-ups

1. Standalone biomes need a multi-noise parameter-list datapack to physically generate (decorative
   features already inject). — datapack-only follow-up.
2. Structures are placer/command-ready; not auto-scattered (no shared-file edit). Wire via
   `StructureManager` or a debug command.
3. Entity skins are original shaded placeholders sized to each model's UV; they read clearly in-world
   but can be refined with proper UV-mapped art via `gen-content2.js`.
4. Creatures, accessories and biome injection are dormant until the [§7](#7-registration-one-liners)
   lines are added.
5. Idea-recovery items in §9 are proposals — verify against current `combat`/`progression`/`items`
   before implementing; ownership belongs to those packages' workstreams.
