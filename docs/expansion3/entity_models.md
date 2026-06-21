# Expansion 3 — Entity Model & Animation Overhaul

Mixin-free deep overhaul of every custom mob/spirit model. All custom creatures previously shared a
couple of plain humanoid silhouettes (`STANDARD`/`BRUTE` + four spirit kinds). They now render with a
shared library of **10 distinct, animated archetypes** under a new
`com.political.client.model` package, mapped per-species/mob, with additive glow overlays on bosses.

Everything is client-side and mixin-free, built on the vanilla model/layer/render-state API
(`HumanoidModel`, `ModelLayerLocation`, `LayerDefinition`, `HumanoidMobRenderer`, `EyesLayer`). No
server roster data was changed.

---

## 1. The archetype library (`src/client/java/com/political/client/model`)

Every archetype subclasses `HumanoidModel<S>` (so it inherits robust vanilla head-tracking,
limb-swing and attack motion) and adds its own geometry + appendage animation in `setupAnim`.

| Archetype          | Class             | Silhouette & live animation |
|--------------------|-------------------|-----------------------------|
| `GAUNT_HUMANOID`   | `GauntModel`      | Thin, stooped; long clawed forearms that flex on an idle cadence; permanent breathing hunch. |
| `HULKING_BRUTE`    | `BruteModel`      | Inflated body, jagged shoulder spikes, arms splayed wide; slow weighty shoulder-roll. |
| `SERPENTINE`       | `SerpentModel`    | Floating torso, **no legs**, 3-segment tail; hover bob + travelling sine-wave tail sway; arms tucked as fins. |
| `MULTI_ARMED`      | `MultiArmModel`   | Second lower pair of arms swinging on an offset cadence; lower arms counter-splay while attacking. |
| `WINGED`           | `WingedModel`     | Large membrane wings; idle flutter that deepens into hard beats while moving, synced hover bob. |
| `QUADRUPED`        | `QuadrupedModel`  | Re-posed to all-fours (horizontal spine, arms→front legs, legs→hind legs, snouted head on a low neck); diagonal trot gait. |
| `CRAWLER`          | `CrawlerModel`    | Low six-limbed body (4 biped limbs splayed + a mid leg pair); fast scuttle twitch. |
| `CLOAKED_SPIRIT`   | `CloakedModel`    | Hood + swept horns, robe skirt instead of legs; slow hover bob, sleeve drift, cloth-sway hem. |
| `BOSS_COLOSSUS`    | `ColossusModel`   | Massively bulked, 5-horn crown, heavy pauldrons, wide power stance; ponderous sway. |
| `TINY_SWARM`       | `SwarmModel`      | Shrunken body + over-sized head + twitching antennae; quick hop & nervous limb jitter. |

Support classes:
- `Archetype` — the enum catalogue (stable ids: `gaunt`, `brute`, `serpent`, `multiarm`, `winged`,
  `quadruped`, `crawler`, `cloaked`, `colossus`, `swarm`).
- `ArchetypeModels` — registry + factory. Owns one baked layer per archetype
  (`politicalserver:archetype_<id>`), exposes `registerLayers()` (idempotent) and
  `bake(Archetype, EntityRendererProvider.Context)`.
- `ArchetypeMapper` — maps a non-curse creature to its best-fit archetype from name keywords + brute
  flag + boss tier (used by the expansion mob rosters, which carry no explicit model kind).
- `GlowOverlayLayer` — `EyesLayer`-based fullbright additive overlay for boss-tier creatures (reuses
  the entity's own texture as the glow mask, so no extra art is required).

---

## 2. Species → archetype remapping

### Phase-1 cursed spirits (`com.political.curse.spirits`, 21 species)
`SpiritModels.archetypeFor(ModelKind)`:
| ModelKind   | Archetype        |
|-------------|------------------|
| `GAUNT`     | `GAUNT_HUMANOID` |
| `SWARM_TINY`| `TINY_SWARM`     |
| `HULKING`   | `HULKING_BRUTE`  |
| `HORNED`    | `CLOAKED_SPIRIT` (hooded + horned) |

### Phase-2 cursed spirits (`com.political.expansion2.curses`, 90 species)
`Spirit2Models.archetypeFor(ModelKind)`:
| ModelKind   | Archetype        |
|-------------|------------------|
| `GAUNT`     | `GAUNT_HUMANOID` |
| `SWARM_TINY`| `TINY_SWARM`     |
| `HULKING`   | `HULKING_BRUTE`  |
| `HORNED`    | `CLOAKED_SPIRIT` |
| `WINGED`    | `WINGED`         |
| `SERPENT`   | `SERPENTINE`     |
| `TOOL`      | `MULTI_ARMED`    |
| `CORPSE`    | `CRAWLER`        |

### Expansion mobs (`com.political.expansion.mobs` 23, `com.political.expansion2.mobs` 115)
These carry no model kind, so `ArchetypeMapper.forCreature(name, brute, isBoss, isMiniBoss)` chooses:
- **Boss role** → `BOSS_COLOSSUS`.
- Name keywords → `TINY_SWARM` (imp/sprite/wisp/mote/flea/gnat/larva…), `WINGED`
  (eagle/vulture/owl/dragon/seraph/wing…), `SERPENTINE` (serpent/naga/hydra/wyrm…),
  `QUADRUPED` (wolf/hound/panther/bear/fox/ram/goat/steed/camel/turtle/stag/salamander/beast…),
  `CLOAKED_SPIRIT` (wraith/shade/phantom/banshee/revenant/acolyte/cultist/lich/necromancer…),
  `MULTI_ARMED` (automaton/clockwork/golem/construct/siege/marshal/marauder),
  `CRAWLER` (crawler/spider/tick/lurker).
- Else mini-boss/brute → `HULKING_BRUTE`; otherwise `GAUNT_HUMANOID`.

Per-creature **scale** is unchanged (driven server-side by the entity `SCALE` attribute / spec
`scale`); the spirit renderers keep the existing per-grade shadow scaling.

---

## 3. Boss visual upgrades

- **Phase-1 spirits**: all `boss()` species (the 5 Special-Grade curses) get a `GlowOverlayLayer`.
- **Phase-2 spirits**: all `boss()` species (Special-Grade roster) get a `GlowOverlayLayer` and keep
  their own kind's archetype (e.g. Dragon Curse stays `WINGED`, Nine-Tailed Shadow stays
  `SERPENTINE`) so bosses stay thematically distinct, not generic.
- **Expansion mobs (both phases)**: full bosses render as the over-sized, crowned `BOSS_COLOSSUS`;
  bosses **and** mini-bosses both receive a `GlowOverlayLayer`.

The glow is mixin-free: an `EyesLayer` re-render with `RenderType.eyes(texture)` (fullbright,
additive), so lit areas bloom into a menacing halo while the silhouette/scale already read as larger.

---

## 4. Textures / UV alignment

The archetype models bolt extra cubes (horns, wings, tails, extra arms, antennae, pauldrons, spikes,
snouts, hoods, skirts) onto UV regions outside the standard humanoid faces.

- **Spirits (phase 1 & 2)**: their texture generators already flood-fill the entire 64×64 sheet, so
  every appendage UV is already covered. **No spirit texture regen required** (and those generators
  are owned by the curse agent — left untouched).
- **Expansion mobs (phase 1 & 2)**: `tools/gen-mobs.js` and `tools/gen-mobs2.js` previously painted
  only the humanoid faces, leaving appendage UVs transparent. Both were updated to **flood-fill the
  sheet with a shaded body tone first**, then paint the humanoid faces on top (body appearance
  unchanged; appendages now sample a coloured pixel).

**Regenerated by this wave** (run with the bundled `tools/node_modules` jimp):
```
node tools/gen-mobs.js     # 23 textures  -> assets/politicalserver/textures/entity/mob_*.png
node tools/gen-mobs2.js    # 115 textures -> assets/politicalserver/textures/entity/mob2_*.png
```
Verified: regenerated sheets are now 100% opaque (4096/4096 px), so no appendage renders transparent.

---

## 5. Integration hooks — what the integration agent must call

**No new wiring is required.** This wave only changed the *bodies* of the four existing
`registerClient()` methods (their names/signatures are unchanged), and `PoliticalClient` already
calls all four. For reference, the required calls (already present in
`PoliticalClient.onInitializeClient()`) are:

```java
com.political.curse.spirits.SpiritClient.registerClient();        // phase-1 spirits
com.political.expansion2.curses.Spirit2Client.registerClient();   // phase-2 spirits
com.political.expansion.mobs.ExpansionMobsClient.registerClient(); // phase-1 mobs
com.political.expansion2.mobs.ExpansionMobs2Client.registerClient();// phase-2 mobs
```

Each of those now:
1. calls `com.political.client.model.ArchetypeModels.registerLayers()` (idempotent — safe to call
   from all four; only the first call registers the shared archetype layers), and
2. registers an archetype-backed renderer per entity type, auto-adding `GlowOverlayLayer` for
   boss/mini-boss tiers.

The base `CurseEntity` renderer (`com.political.client.CurseRenderer`, registered directly in
`PoliticalClient`) is **not** part of this wave and is left unchanged.

### Notes for the build/integration agent
- Removed (now unused): `ExpansionMobModels`, `ExpansionMob2Models`. `SpiritModels`/`Spirit2Models`
  were reduced to `archetypeFor(ModelKind)` lookup tables.
- Renderer generics changed from `ZombieModel<…>` to `HumanoidModel<…>` (archetype models extend
  `HumanoidModel`).
- Requires no new resource pack entries beyond the regenerated mob PNGs above.
