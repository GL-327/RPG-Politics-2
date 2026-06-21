# Integration & Mega-Build Decomposition Plan

Concrete, prioritized decomposition of the "build our mod from these mods" mega
change set. Companion to `JAR_AUDIT.md`. Target: **MC 26.2 Fabric, Java 25,
non-remapping Loom**, currently **mixin-free**.

This plan is written so that each workstream can be handed to its own agent.
Every workstream lists: **(1) files/packages to touch, (2) reference material in
`reference/extracted/`, (3) architecture decisions.**

---

## 0. Architecture decision — TARGETED MIXINS APPROVED ✅

**Decided:** default to event/Fabric-API-based code; **use targeted mixins
wherever they genuinely unlock capability** (deep render/visibility hooks,
animation rigs, input handling, advanced VFX). Keep each mixin **narrow and
well-documented**; do **not** mixin where a clean event/override already works.

### Mixin infrastructure (set up & building green)
- Packages: `com.political.mixin` (common/server) and
  `com.political.client.mixin` (client) — each has a `package-info.java` with
  the usage policy.
- Configs: `src/main/resources/politicalserver.mixins.json` (`mixins`/`server`)
  and `src/client/resources/politicalserver.client.mixins.json` (`client`,
  `environment: client`). `compatibilityLevel: JAVA_25` (confirmed valid — same
  as artifacts).
- Wired in `fabric.mod.json` `"mixins"` (client one tagged `environment:client`).
- `build.gradle` → `loom { mixin { defaultRefmapName = "politicalserver.refmap.json" } }`.
  Non-remapping Loom ⇒ official Mojang names in mixin targets, refmap is
  effectively identity (no Yarn lookup needed).
- **To add a mixin:** drop the class in the right package and add its name to the
  matching array. Prefer `@Inject(cancellable)` / `@ModifyVariable` over
  `@Overwrite`. Verify with `.\gradlew.bat build`.

### Still prefer events/overrides when they suffice
- **Curse visibility** → keep using `Entity#isInvisibleTo(Player)` (cleaner than
  a render mixin); see Workstream A.
- Combat/stat VFX → our own call sites already work.

Reserve mixins for: world-space cursed-energy/domain render overlays, animation
rig hooks, technique input handling, and vanilla damage/attribute math with no
event.

---

### (historical) Why mixin-free was the default before sign-off

Why mixin-free is winning so far and should remain the default:

- The codebase already achieves a *lot* without mixins: rich server-driven VFX
  (`vfx/VfxHelper.java`), custom mobs (`curse/CurseEntity` extends `Zombie`),
  no-mixin client screens (`RpgScreen` pattern), HUD, networking
  (`net/ModNetworking`), tooltips, creative tabs, worldgen.
- Mixin-free = **resilient to MC updates** (26.2 → 26.3 won't break injection
  points) and **conflict-free** with other mods — important when the whole point
  is layering content from many sources.
- Several "needs a mixin" features actually don't:
  - **Curses visible only to high cursed-energy players** → override
    `Entity#isInvisibleTo(Player)` on `CurseEntity`/`CursedSpirit2Entity` and
    gate on the *viewing* player's synced cursed energy (the local player on the
    client). **No mixin required.** (Details in Workstream A.)
  - Player animations → the **Not Enough Animations** dependency already does
    procedural body animation game-wide; our own animated entities use custom
    `EntityModel`s (already done for curses/archetypes).
  - Crit/ferocity/impact VFX → already injected at our own call sites.

When a mixin *is* justified (keep this list short and explicit):

- Deep client render hooks that have **no Fabric callback** (e.g. a world-space
  cursed-energy overlay shader, or hiding a vanilla entity from specific players
  if `isInvisibleTo` proves insufficient).
- Intercepting vanilla damage/attribute math that has no event.

If/when we add the first mixin, do it **isolated**: a new `politicalserver.mixins.json`
+ `accessWidener` already-supported by the build, client-only where possible, and
documented in `docs/integration/MIXINS.md`. **Do not** retrofit existing systems.

*(The above is retained as rationale; the final decision is targeted-mixins
approved — see top of §0. Mixins are now first-class but used sparingly.)*

---

## Priority order

All three now run in **parallel** as independent background workstreams (the
reference material and mixin infra are ready). Rough emphasis:

1. **A — JJK overhaul** (the centerpiece; builds on curse/power/vfx + the
   extracted JujutsuCraft/cursedfate blueprint).
2. **C — Library + VFX** (lowest risk; JEI wired; makes A & B look spectacular).
3. **B — Content porting** (largest surface; datapack-first to stay safe & green).

Each workstream is internally parallelizable and green-build-safe (add behind
new packages/registries; never break existing entrypoints).

---

## Workstream A — JJK complete overhaul

Goal: show-accurate-*feeling* (not IP-infringing) cursed energy, techniques,
domain expansions, curses-as-entities, slick no-mixin GUIs, custom animations.
**All art/names original** (see `JAR_AUDIT.md` §C).

### Files/packages to touch
- `com.political.curse` — `CurseEntity`, `CurseManager`, `ModEntities`,
  `CursedGear`, `CursedTrait`, `CursedSpiritEntity`, `SpiritSpecies`.
- `com.political.expansion2.curses` — `CursedSpirit2Entity`, `CurseSpirits2`,
  `SpiritSpecies2` (the 90-species roster).
- `com.political.power` + `com.political.expansion2.powers` (`Power2`,
  `PowerManager2`) — technique catalog + cast pipeline.
- `com.political.vfx` (`VfxHelper`, `VfxElement`) — already has
  `domainExpansion`, `domainPulse`, `auraColumn`, beams/cones/novas. **Extend**
  with technique-specific routines (e.g. `cursedSlash`, `hollowPurple`-style
  twin-orb collide → name it originally like `voidLance`).
- `com.political.net` — new C2S/S2C packets for technique select/cast, domain
  open/close, cursed-energy sync (`StatSyncS2C` pattern).
- `com.political.combat` (`CombatEngine`, `AbilityEngine`, `RpgStats`,
  `StatManager`) — cursed-energy as a stat/resource; technique damage.
- Client: `com.political.client` no-mixin screens — add `TechniqueScreen`
  (clone the `PowersScreen2` pattern), domain HUD overlay in `PoliticalClient`
  (already registers `rpg_hud`). Curse rendering: `curse/CurseRenderer`,
  `CurseModels`, `expansion2.curses.Spirit2Renderer/Spirit2Models`.

### Curse visibility (the headline mechanic) — mixin-free recipe
1. Sync each player's cursed energy to **all** clients (tracked data / attachment
   or a periodic `StatSyncS2C` broadcast keyed by player UUID; the client keeps a
   small map, and for the *local* player it already has `ClientRpgState`).
2. On `CurseEntity` / `CursedSpirit2Entity`, override:
   ```java
   @Override public boolean isInvisibleTo(Player viewer) {
       if (super.isInvisibleTo(viewer)) return true;
       return !CursedEnergy.canPerceive(viewer, this.getGrade());
   }
   ```
   On the client, `viewer` is the local player → it reads local CE. Low-CE
   players literally cannot see the curse; high-CE players can. No mixin.
3. Optional: a faint "presence" cue for low-CE players (sound via `VfxHelper`/
   Sound Physics, screen vignette) so curses feel *sensed but unseen*.

### Reference material (extracted)
- **alexscaves** `client/particle` (163), `client/render/entity` (111),
  `server/entity/ai` (94), `server/potion` (10): patterns for technique
  particles, custom effects, and complex creature AI — **reimplement**, don't
  copy (LGPL).
- **deeperdarker** `content/entities/animations` (7) + sculk ambience: blueprint
  for curse animations + an eerie cursed/sculk dimension (AGPL → observe only).
- **JujutsuCraft** (`reference/extracted/JujutsuCraft-ver50.1-forge-1.20.1/`) —
  **the primary blueprint.** Mine `net/mcreator/jujutsucraft/procedures` (1170
  technique/domain/AI logic class *names*), `entity` (1116 curse/projectile
  names), `client` GUIs (`SelectTechniqueScreen`, overlays), `potion` (58 cursed
  states), and `assets/jujutsucraft/lang/en_us.json` (1737 keys) for the full
  technique/domain/grade vocabulary. **Reimplement with ORIGINAL names + art.**
- **cursedfate** (`reference/extracted/cursedfate-1.0.39/`) — secondary JJK
  blueprint (2306 procedures, 141 states); cross-reference for richer original
  reinterpretation. Anime IP → mechanics only.

### Architecture decisions
- Cursed energy = a first-class resource stat in `combat/RpgStats` + persisted
  via the existing player attachment/progression path (`ProgressionManager`).
- Domains = a server-side timed area effect using `VfxHelper.domainExpansion` +
  `domainPulse`, with a "sure-hit" gameplay zone (entities inside take the
  technique each tick) — pure server logic, no mixin.
- Techniques = data-driven entries (enum/registry like `Power2`) so new ones are
  cheap; each maps to a VFX routine + a gameplay resolver.

---

## Workstream B — Content porting (biomes, structures, accessories, creatures)

Goal: maximal content, **datapack/worldgen-first** to stay legally safe and
build-green. Author original assets; reuse only MIT art (artifacts/naturalist)
with credit.

### B1. Biomes (datapack worldgen — no TerraBlender)
- **Touch:** new `src/main/resources/data/politicalserver/worldgen/biome/…`,
  `…/configured_feature`, `…/placed_feature`, plus Fabric **Biome Modification
  API** in a new `com.political.world.biome` package (mixin-free).
- **Reference:** `better-end` (1562 png, 6332 json — worldgen JSON *shape*),
  `Oh-The-Biomes-Weve-Gone` (biome/feature/surface-rule JSON), and (when
  supplied) `Terralith_26.1` (datapack-style, closest to us). Study structure;
  author original biomes. Better End assets are partly CC BY-NC-SA → **do not
  ship**.
- **Decision:** prefer pure datapack worldgen + Biome Modification API over
  porting TerraBlender (a 26.1.2 lib we can't load and don't need).

### B2. Structures (dungeons, villages)
- **Touch:** `com.political.world.structures`, `com.political.world` dungeon
  stack (`DungeonGenerator`, `DungeonRegistry`, `DungeonPlan`, `DungeonType`,
  `StructureGenerator`, `SettlementGenerator`, `VillageOverlayManager`).
- **Reference:** `DungeonsArise` (489 json — jigsaw template-pool/processor
  layout) and (when supplied) `Towns&Towers`, `BetterVillage`. All
  All-Rights-Reserved → **study layout, author our own `.nbt` templates** with
  the existing `StructureIO`/`BuildBuffer` pipeline.
- **Decision:** keep our existing procedural+template hybrid; add a jigsaw-style
  pool format inspired by DungeonsArise for larger set-piece dungeons.

### B3. Accessories  ⭐ (port from artifacts — MIT, closest version)
- **Touch:** `com.political.expansion2` accessory stack already exists —
  `Accessories2`, `Accessories2Catalog`, `AccessoryDef2`, `AccessoryTooltip2`,
  `Armor2SetBonusHandler`; plus `com.political.items` (`RelicItems`,
  `ItemActiveAbility*`), `content/CreativeCatalog`.
- **Reference:** `artifacts-fabric-15.0.0` (MC 26.1.2, **MIT**) — `component.
  ability` (25 abilities), `attribute` modifiers, equip-slot model, loot
  injection. This is our **best port source**; mechanics + (credited) art are
  reusable. Map artifact abilities onto our `AccessoryDef2`/ability engine.
- **Decision:** extend the existing expansion2 accessory system rather than
  adopting cardinal-components/trinkets (avoid a heavy new dep); reuse artifacts'
  *ability ideas* and on-equip passive model.

### B4. Creatures
- **Touch:** `com.political.expansion`/`expansion2` mob stacks
  (`ExpansionMobs2`, `ExpansionMob2`), client renderers/models already present.
- **Reference:** `naturalist` (MIT — `entity/ai/goal` (17), `entity/mob` (66),
  geckolib models) and `alexscaves` `entity/living` (141, LGPL → observe). Use
  vanilla `Goal`s + our existing custom `EntityModel`s (we don't need GeckoLib).

---

## Workstream C — Library + maximal VFX / graphical effects

Goal: wire libraries and push graphical richness, all mixin-free.

### C1. Dependencies (status)
- **JEI 30.1.0.10** — ✅ wired (`compileOnly` API + `runtimeOnly` full jar) and a
  guarded plugin exists: `com.political.client.compat.jei.PoliticalJeiPlugin`.
  **Next:** add custom recipe categories (gear-ability "recipes", bank/economy
  conversions, cursed-energy crafting) in that plugin's `registerCategories` /
  `registerRecipes`.
- **Sound Physics Remastered 1.5.1+26.2** — ✅ wired (`runtimeOnly`, Modrinth).
  Pure runtime; makes our `vfx`/`ModSounds` cues reverberate/occlude for free.
- **Not Enough Animations 1.12.4** — ✅ wired (`runtimeOnly`, Modrinth). Player
  body animation game-wide; complements custom entity animations.
- **Manual drop:** `libs/*.jar` (gitignored, dev-runtime) for any extra
  26.2-Fabric jar. See `libs/README.md`.

### C2. VFX expansion
- **Touch:** `com.political.vfx` (`VfxHelper`, `VfxElement`), `com.political.sound`
  (`ModSounds`), call sites in `combat/AbilityEngine`, `power`/`powers` cast
  paths, `world` dungeon traps/boss spawners.
- **Reference:** `alexscaves` `client/particle` (163) for advanced particle
  *behaviors* to reimplement as new `VfxHelper` routines + (if needed) custom
  `ParticleType`s registered in a new `com.political.vfx.particle` package
  (server-spawned via existing `sendParticles` path — mixin-free).
- **Decision:** keep VFX **server-driven** (the current design) so it stays
  mixin-free and works in multiplayer; only add a client mixin if we want a true
  post-processing/shader pass (out of scope for now).

### C3. JEI plugin build-out (concrete next step)
In `PoliticalJeiPlugin`, register categories for our custom systems so players can
browse them. Guarded automatically (class only loads under JEI). Keep using only
stable API (`IModPlugin` defaults) and verify each addition with
`.\gradlew.bat build`.

---

## Build / environment notes for any agent picking this up
- OS **Windows / PowerShell**: chain with `;` (not `&&`); use PowerShell-native
  cmds. Directory listing via `cmd /c "dir /b ..."` is reliable here.
- Build: **`.\gradlew.bat build`** from repo root; keep it **GREEN** every step.
- MC **26.2**, Fabric, Java 25, **non-remapping Loom** → use plain
  `compileOnly`/`runtimeOnly` (NOT `modImplementation`). `ResourceLocation` is
  `net.minecraft.resources.Identifier`; construct via
  `Identifier.fromNamespaceAndPath(...)`.
- History is **mixin-free**; honor the architecture decision above.
- Reference assets live in `reference/extracted/<mod>/` (gitignored). Textures
  are generated with Node scripts in `tools/` (`pixel-art-lib.js`,
  `regenerate-all.js`, `gen-*.js`) — author **original** art there.
- Entry points: `com.political.RpgPoliticsMod` (main, `MOD_ID="politicalserver"`)
  + `com.political.client.PoliticalClient` (client).

## Blockers / decisions needed
1. **Mixin architecture sign-off** (see §0).
2. **Supply the 12 missing JARs** (esp. JujutsuCraft, Terralith, Towns&Towers,
   BetterVillage, BoP) to extract their real assets/mechanics — see
   `JAR_AUDIT.md` §B "Not supplied".
3. Confirm "original art/names only" for JJK content (assumed yes per the
   creative-liberty / legal-safety direction).
