# JAR Audit — "build our mod from these mods"

Foundational research pass for the mega change set. This catalogs every attached
reference JAR, its real target version/loader, what is inside it, whether it can
be a **real runtime dependency** on our target (**Minecraft 26.2, Fabric, Java
25**), and whether we should **port/reimplement** it or keep it **reference-only**
(including IP/licensing honesty).

> Our target: `minecraft 26.2`, `fabricloader >=0.19.0`, `fabric-api
> 0.152.2+26.2`, `java 25`, **non-remapping Loom 1.17.11** (26.x ships
> deobfuscated with Mojang names; note `ResourceLocation` is mapped as
> `net.minecraft.resources.Identifier` in this tree). Authored **mixin-free** so
> far (Fabric API events + networking + no-mixin client screens).

## TL;DR

- **UPDATE (round 2):** the 12 previously-missing JARs were supplied in
  `C:\Users\Gathe\Videos\` and are now **all extracted and audited**, plus **two
  bonus political-system references** (`CivilCraft`, `Economies-and-Politics` —
  older builds of *this* mod, namespace `politicalserver`). **21 jars total**
  now under `reference/extracted/`.
- **JJK drivers in hand:** **JujutsuCraft** (3493 classes, 1170 technique
  "procedures", 1116 entities, SelectTechnique GUIs, domain-expansion entities,
  grades) and **cursedfate** (3436 classes, 2306 procedures, 1857 png). Both are
  **anime IP / All Rights Reserved** → **port mechanics, author original
  art/names** (see §C).
- The 3 runtime-compatible deps (JEI, Sound Physics Remastered, Not Enough
  Animations) are **wired and building green** via Maven (local jars not needed).
- **Only ~26.x/1.21.11 mods are realistically runtime-loadable** on 26.2 Fabric.
  Everything 1.20.1 / 1.21–1.21.1 and every Forge/NeoForge jar is **port /
  reference-only**.
- **Architecture:** **targeted mixins APPROVED** (round 2). Infra is set up:
  `com.political.mixin` (+ `com.political.client.mixin`),
  `politicalserver.mixins.json` + `politicalserver.client.mixins.json`, wired in
  `fabric.mod.json`, refmap named in `build.gradle` (`loom { mixin { … } }`).
- **Extracted to** `reference/extracted/<modname>/` (gitignored). Never shipped.

---

## A. Confirmed runtime dependencies (wired into `build.gradle`)

All three were verified live against their Maven/registry and **resolve + build
green** on 26.2 Fabric. None are bundled into our jar (dev-runtime / compile-only
scopes). Versions live in `gradle.properties`.

| Mod | Coordinate (repo) | Scope | Verified |
| --- | --- | --- | --- |
| **JEI 30.1.0.10** | `mezz.jei:jei-26.2-fabric-api:30.1.0.10` + `…-fabric:…` (blamejared maven) | `compileOnly` (API) + `runtimeOnly` (full) | `maven-metadata.xml` → `<release>30.1.0.10</release>`; common-api pulled transitively |
| **Sound Physics Remastered 1.5.1+26.2** | `maven.modrinth:sound-physics-remastered:fabric-1.5.1+26.2` | `runtimeOnly` | Modrinth API: game `26.2`, loader `fabric`, published 2026-06-18 |
| **Not Enough Animations 1.12.4** | `maven.modrinth:not-enough-animations:1.12.4` | `runtimeOnly` | Modrinth API: game `26.2`, loader `fabric`, published 2026-06-18 |

JEI also ships a real (guarded) plugin: `com.political.client.compat.jei.PoliticalJeiPlugin`.

---

## B. Per-JAR audit

### Present & extracted (7)

#### 1. `artifacts-fabric-15.0.0.jar`  ⭐ highest-value port
- **Target:** Fabric, **MC 26.1.2**, **Java 25**, loader 0.19.2, Loom 1.15.5. License **MIT**.
- **Closest mod to our target by far** (26.1.2 vs our 26.2; same Java; same loader family).
- **Inside:** 118 png, 7 ogg, 272 classes, 374 json. ns: `artifacts`, `minecraft`. Data namespaces show integration with `accessories`, `curios`, `trinkets`, `origins`.
- **Notable mechanics (package map):** `component.ability` (25 ability classes) + `ability.mobeffect` / `ability.retaliation`, `attribute` (custom attribute modifiers), `equipment` + client item renderers/mesh/model, `entity` (mimic), `loot` (loot-injected drops), `network`, cardinal-components-based equip slots, JEI/REI-agnostic. Wearable accessories with on-equip passive abilities — **exactly the "artifacts-style accessory system" the plan calls for.**
- **Runtime dep on 26.2?** **No** (26.1.2 + needs cardinal-components/expandability bundled jars). But it is the **#1 reimplementation reference** and its assets are **MIT** (attribution-friendly).
- **Verdict:** **PORT mechanics + may reuse MIT assets/textures with attribution.** Map onto our accessory/relic system (`com.political.items.RelicItems`, `content.CreativeCatalog`).

#### 2. `alexscaves-2.0.2.jar`  ⭐ VFX goldmine
- **Target:** **Forge 1.20.1** (`forge [47.1.3,)`, loader `[46,)`), requires `citadel`. License **LGPL**. Mixin-heavy (`citadel.mixins.json`, `alexscaves.mixins.json`).
- **Inside:** **1263 png, 1474 ogg(!), 1303 classes, 3305 json.** ns: `alexscaves`, `minecraft`; data: `alexscaves`, `create`, `forge`.
- **Notable mechanics:** `client/particle` (**163 particle classes**), `client/render/entity` (111), `client/model` (85), `server/entity/living` (141) + `entity/ai` (94), `server/block` (141) + blockentities/fluids, `server/level/feature` (52) + `structure`/`carver`/`surface`/`biome` (custom cave biomes), `server/potion` (10 custom effects), `server/inventory`, `compat/jei` (9). A masterclass in **particles, custom biomes, creature AI, and screen-shake/VFX**.
- **Runtime dep on 26.2?** **No** (Forge 1.20.1, LGPL, mixin + citadel-dependent). Cannot load.
- **Verdict:** **Reference-only for VFX/biome/AI patterns; reimplement.** LGPL means don't copy code verbatim into our (MIT) tree; reimplement behaviors. Original art only.

#### 3. `naturalist-1.0.2-neoforge-1.21.1.jar`
- **Target:** **NeoForge 1.21.1**, needs **GeckoLib 4.7+**. License **MIT**.
- **Inside:** 253 png, 248 ogg, 211 classes, 623 json. ns: `naturalist` (+ `xaerominimap` compat).
- **Notable mechanics:** `server/entity/mob` (66), `entity/ai/goal` (17 custom goals), `entity/base`, geckolib `client/model` (24) + `renderer` (25) + layers, custom navigation, advancement criteria, spawn/biome modifiers. Realistic animal behaviors.
- **Runtime dep on 26.2?** **No** (NeoForge + GeckoLib + 1.21.1).
- **Verdict:** **Reference for creature AI/behavior + animation rigging.** MIT assets reusable with attribution; reimplement AI with vanilla `Goal`s (we are mixin-free) or our own animation approach. Feeds the "creatures" workstream.

#### 4. `deeperdarker-fabric-1.21-1.3.3-plus-b.jar`
- **Target:** **Fabric ~1.21 (1.21.1)**, Java 21, needs `owo`. License **AGPL-3.0** (copyleft).
- **Inside:** 209 png, 73 ogg, 207 classes, 1763 json. ns: `deeperdarker`.
- **Notable mechanics:** `world/otherside` (an **entire custom dimension** + `gen` + `structures`), `content/entities` (16) + `animations` (7) + `goals`, `content/blocks/vegetation` (14), `content/enchantments`, custom portal API, sculk-themed content/network.
- **Runtime dep on 26.2?** **No** (1.21.1 Fabric + AGPL is viral — must NOT copy code).
- **Verdict:** **Reference-only**, reimplement. Best as a blueprint for a custom dimension + sculk-adjacent ambience and entity animations (relevant to curses).

#### 5. `better-end-21.0.11.jar`
- **Target:** **Fabric 1.21 / 1.21.1**, Java 21, needs `bclib`/`wover`/`wunderlib`. License **MIT (code); CC BY-NC-SA 4.0 for some assets.**
- **Inside:** **1562 png, 43 ogg, 624 classes, 6332 json.** ns: `betterend` (+ compat ns). Heavy worldgen JSON.
- **Notable mechanics:** full End overhaul — biomes, terrain features, plants, mobs, mechanics; integrations for REI/EMI.
- **Runtime dep on 26.2?** **No** (1.21.1 + hard bclib/wover/wunderlib chain).
- **Verdict:** **Reference for End/worldgen JSON structure.** **Do NOT ship the CC BY-NC-SA assets.** Use the *datapack/worldgen JSON shape* as a learning template; author original biomes.

#### 6. `DungeonsArise-1.21.1-2.1.68-fabric-release.jar`
- **Target:** **Fabric ~1.21**, Java 21. License **All Rights Reserved.**
- **Inside:** 1 png, 0 ogg, **6 classes, 489 json** (it is essentially a **structure datapack** — NBT templates + structure/pool/processor JSON). ns: `dungeons_arise`.
- **Notable mechanics:** large jigsaw roguelike dungeons (data-driven). Almost no code.
- **Runtime dep on 26.2?** **No** (1.21 + All Rights Reserved → cannot redistribute its structures).
- **Verdict:** **Reference-only.** Study the jigsaw/template-pool/processor JSON layout (directly relevant to `com.political.world.structures` / `dungeons`), then **author our own structure NBTs**. Do not copy their `.nbt`.

#### 7. `Oh-The-Biomes-Weve-Gone-Fabric-4.4.0.jar`  (filename says 4.4.0)
- **Target:** **Fabric, MC 1.21.11** (manifest `Fabric-Minecraft-Version: 1.21.11`, loader 0.19.3, Loom 1.17.480 — *same loom/loader family as ours*), needs **TerraBlender 21.11**, `corgilib`, `ohthetreesyoullgrow`, **GeckoLib 5.4.4**. License **All Rights Reserved.**
- **Inside:** **1118 png, 11 ogg, 278 classes, 7943 json.** ns: `biomeswevegone`.
- **Notable mechanics:** large biome pack via **TerraBlender** region API; surface rules, features, trees, custom blocks.
- **Runtime dep on 26.2?** **Closest of the worldgen mods** (1.21.11 loader/loom match) but **still not 26.2**, hard TerraBlender/corgilib/geckolib chain, and **All Rights Reserved**.
- **Verdict:** **Reference-only**; reimplement biomes as **datapack worldgen** (no TerraBlender) or via Fabric Biome Modification API. Author original biomes.

### Supplied round 2 & extracted (12 + 2 bonus)

#### 8. `JujutsuCraft-ver50.1-forge-1.20.1.jar`  ⭐⭐ PRIMARY JJK reference
- **Target:** **Forge 1.20.1** (MCreator-generated), deps **GeckoLib** +
  **playeranimator**. License **All Rights Reserved**. Has `mixins.jujutsucraft.json`.
- **Inside:** **1165 png, 28 ogg, 3493 classes, 1526 json.** ns `jujutsucraft`. Pkgs:
  `procedures` (**1170** — the technique/domain/AI logic), `entity` (**1116** —
  curses, projectiles, characters), `item` (456), `client` (554 — models,
  renderers, GUIs), `potion` (58 — cursed states/effects), `network` (25),
  `block` (54 — domain/barrier blocks), `init` (40).
- **Design extracted (lang has 1737 keys):** cursed **techniques** selectable via
  `SelectTechniqueScreen` / `SelectTechnique2Screen` + profession select;
  **domain expansion** (`AIDomainExpansionEntityProcedure`, `AIDomainLogicProcedure`,
  `BreakDomainProcedure`, `domain_expansion_entity`, domain/barrier blocks);
  **reverse cursed technique**, **simple domain**, **anti-infinity**, **black
  flash**; **cursed-spirit grades** (grade 1…special); mastery items
  (`item_master_domain_expansion`, `…reverse_cursed_technique`); HUD overlays
  (six-eyes, backstep, key). Effects via `effect.jujutsucraft.cursed_technique`.
- **Runtime dep on 26.2?** **No** (Forge 1.20.1 + GeckoLib/playeranimator + ARR).
- **Verdict:** **THE mechanics blueprint for Workstream A.** Classes are
  compiled (MCreator) — study **class/lang/asset *names* and structure**, then
  **reimplement with ORIGINAL names + art**. Do **not** ship its textures, sounds,
  character names, or technique/domain names (anime IP — §C).

#### 9. `cursedfate-1.0.39.jar`  ⭐ secondary JJK reference
- **Target:** MCreator mod, has `mixins.cursedfate.json` + refmap (Forge-family).
  License effectively **All Rights Reserved** (anime IP).
- **Inside:** **1857 png, 82 ogg, 3436 classes, 574 json.** ns `craftkaisen`,
  `cursedfate`, `photon`. Pkgs: `procedures` (**2306**), `entity` (250),
  `item` (301), `client` (270), `potion` (141), `network` (34).
- **Verdict:** **Reference + reimplement** (anime IP). Cross-check techniques/
  states against JujutsuCraft for the richest original reinterpretation. The
  `photon` namespace appears to be a rendering helper — pattern reference only.

#### 10. `Terralith_26.1_v2.6.2_Fabric.jar`  ⭐ best biome port source
- **Target:** **MC 26.1 Fabric — pure datapack** (1959 json, **11 classes** only).
  ns `terralith`; data integrates `c`, `tectonic`, `sereneseasons`.
- **Verdict:** **Closest-to-us worldgen and the easiest to port** — it is almost
  entirely worldgen JSON. Study the biome/feature/noise/surface-rule JSON and
  port the *approach* to 26.2 datapack worldgen with **original biome names**.
  License: free to study; author our own datapack rather than redistributing.

#### 11. `BiomesOPlenty-fabric-26.1.2-26.1.2.0.22.jar`
- **Target:** **MC 26.1.2 Fabric** (NeoForge-cross via data ns). 662 png, 4424
  json, 298 classes. ns `biomesoplenty`. Needs TerraBlender + serene seasons.
- **Verdict:** **Reference/reimplement.** Biome + block palettes; author original
  biomes via datapack/Biome Modification API.

#### 12. `TerraBlender-fabric-26.1.2-26.1.2.0.2.jar`
- **Target:** **MC 26.1.2 Fabric** biome-region **library** (67 classes, 14 json).
- **Verdict:** **Reference-only.** We prefer datapack worldgen + Fabric Biome
  Modification API and likely will **not** adopt TerraBlender (a 26.1.2 lib we
  can't load on 26.2). Keep as API-shape reference only.

#### 13. `t_and_t-fabric-neoforge-1.13.11.jar` (Towns & Towers)
- **Target:** Fabric/NeoForge — **pure structure datapack** (367 json, **0
  classes**) using **cristellib**. data ns `towns_and_towers`, `kaisyn`, `cristellib`.
- **Verdict:** **Study jigsaw/template-pool layout; author our own** village/road
  structures into `com.political.world.structures`. (Structure NBT/JSON is the
  author's IP — do not redistribute.)

#### 14. `bettervillage-forge-1.21.10-3.3.1-all.jar`
- **Target:** **Forge 1.21.10** (6 json, 6 classes + structure NBT). ns `bettervillage`.
- **Verdict:** **Reference-only**; reimplement village building set.

#### 15. `viltrumitecore-1.1.0.jar`
- **Target:** small flight/hero-tech lib (76 classes, 6 png/6 ogg). ns `viltrumitecore`.
- **Verdict:** **Reference-only** — already inspired `flight/FlightManager`;
  mine for any remaining flight/hover/landing feel.

#### 16. `modernfix-neoforge-5.27.17+mc26.1.2.jar`
- **Target:** **NeoForge** 26.1.2 perf mod. **Cannot load on Fabric.**
- **Verdict:** **Drop entirely.** (Not extracted.)

#### 17. `CivilCraft-Mod-V3.4.8-Vanilla+.jar`  (bonus — our own lineage)
- **Inside:** **ns `politicalserver`** (!), 250 classes, 7 json, 1 png. An older/
  sibling build of **this** mod's political system.
- **Verdict:** **Idea-recovery reference for Workstream B / politics.** Same
  namespace = safe to lift our own prior mechanics. Mine `politics`, `economy`,
  `gov`, `court`, `civics` for features to restore/extend.

#### 18. `Economies-and-Politics-Mod-V1.0.jar`  (bonus — our own lineage)
- **Inside:** **ns `politicalserver`**, 67 classes, 3 json. Earlier build of this mod.
- **Verdict:** **Idea-recovery reference**; same as above, older.

> `jei-26.2-fabric-30.1.0.10.jar` was also supplied and extracted (1032 files)
> as an **API reference** for building JEI recipe categories — but it is already
> wired via Maven, so the local jar is reference-only. `sound-physics` / `NEA`
> local jars are runtime-only (nothing to port) and were not extracted.

---

## C. IP / licensing guidance (read before porting anything)

Our mod is **MIT**. To keep it shippable and legally safe (the same "creative
liberty to avoid legal issues" the user wants — consistent with the Hypixel
Skyblock concern):

1. **Anime-IP mods (JujutsuCraft, cursedfate):** Jujutsu Kaisen is copyrighted/
   trademarked. **Do not ship their textures, models, sounds, character names, or
   domain/technique names verbatim.** **Reimplement the *mechanics*** (cursed
   energy, techniques, domain expansions, curse grades) with **original,
   evocative-but-distinct art and naming.** Game *mechanics/systems are generally
   not copyrightable*; specific expression (art/audio/text/character names) is.
2. **AGPL-3.0 (deeperdarker):** copyleft & viral — **do not copy its code** into
   our MIT tree. Reimplement behavior from observation only.
3. **LGPL (alexscaves):** avoid copying source; reimplement. Original art only.
4. **All Rights Reserved (DungeonsArise, BiomesWeveGone):** **no redistribution**
   of their assets/structures. Use only as a *structural learning reference*;
   author our own.
5. **CC BY-NC-SA assets (some Better End art):** **non-commercial + share-alike**
   — incompatible with shipping in a normal mod. **Do not ship.**
6. **MIT (artifacts, naturalist):** **reusable with attribution.** Keep a
   `NOTICE`/credits entry if we reuse their textures. These are our safest
   asset/code references.

**Rule of thumb for this project:** *port mechanics, write original art/names.*
Only reuse art wholesale from **MIT** sources (artifacts, naturalist) with credit.

---

## D. Extraction index

Extracted under `reference/extracted/` (gitignored, research-only, never shipped):

```
reference/extracted/alexscaves-2.0.2/
reference/extracted/artifacts-fabric-15.0.0/
reference/extracted/better-end-21.0.11/
reference/extracted/deeperdarker-fabric-1.21-1.3.3-plus-b/
reference/extracted/DungeonsArise-1.21.1-2.1.68-fabric-release/
reference/extracted/naturalist-1.0.2-neoforge-1.21.1/
reference/extracted/Oh-The-Biomes-Weve-Gone-Fabric-4.4.0/
```

Re-extract (PowerShell) example:

```powershell
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::ExtractToDirectory(
  "C:\Users\Gathe\Downloads\<file>.jar",
  "reference\extracted\<name>")
```

---

## E. Blockers — RESOLVED (round 2)

1. ~~12 of 19 JARs missing~~ → **RESOLVED.** All supplied in `C:\Users\Gathe\Videos\`,
   extracted and audited above (incl. JujutsuCraft + cursedfate + 2 bonus
   political refs).
2. ~~Mixin architecture call~~ → **RESOLVED: targeted mixins APPROVED.** Default
   stays event/Fabric-API-based; mixins are used narrowly where they unlock
   capability (render/visibility/animation/input/VFX). Infra is set up and
   building green (see TL;DR + `PLAN.md` §0).

Standing reminders (not blockers): JJK content must be **original art/names**;
do not ship ARR/anime-IP assets (§C).
