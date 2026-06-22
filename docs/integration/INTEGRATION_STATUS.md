# Integration Status — RPG Politics 2 Mega-Integration

**Date:** 2025-06-22  
**Target:** Minecraft 26.2 · Fabric · Java 25  
**Build:** `.\gradlew.bat clean build`  
**Runtime:** **Standalone** — Fabric Loader + Fabric API + `politicalserver` only (see `docs/STANDALONE.md`)

---

## Summary

All three parallel workstreams (A JJK, B Content, C VFX/Library) are **wired and live**. Reference-mod mechanics are **ported natively** into `com.political.*` and `data/politicalserver/` — no required companion mods (JEI, NEA, Sound Physics, JujutsuCraft, Terralith, etc.).

---

## Standalone mod policy

| Removed from Gradle runtime | Replacement |
| --- | --- |
| `runtimeOnly` JEI full jar | `compileOnly` JEI API; `@JeiPlugin` loads only when player installs JEI |
| `runtimeOnly` Not Enough Animations | Native `HumanoidModelCastPoseMixin` + `TechniqueCastS2C` cast-pose sync |
| `runtimeOnly` Sound Physics Remastered | Native sound categories in `VfxSounds` / `VfxHelper` |
| `runtimeOnly fileTree(libs/*.jar)` | Removed — prevents wrong-version jars entering dev classpath |

`fabric.mod.json` declares JEI / NEA / Sound Physics under **`suggests`** only.

**Player crash fix:** legacy **1.16.5** NEA / TRansition / TRender jars in `mods/` must be deleted. Our mod does not bundle them.

---

## Wired entrypoints

### `RpgPoliticsMod.onInitialize()` (common)

| Call | Workstream | Purpose |
| --- | --- | --- |
| `com.political.vfx.VfxBootstrap.init()` | C | Registers 6 custom particle types + 9 VFX sound events |
| `com.political.content.ContentBootstrap.init()` | B | Ability accessories, ambient creatures, biome feature injection |
| `com.political.curse.JjkBootstrap.init()` | A | Techniques, domains, CE providers, JJK networking, server tick |

### `PoliticalClient.onInitializeClient()` (client)

| Call | Workstream | Purpose |
| --- | --- | --- |
| `com.political.vfx.client.VfxClientBootstrap.initClient()` | C | Particle providers for custom types |
| `com.political.content.client.ContentClientBootstrap.initClient()` | B | Creature models + renderers |
| `com.political.client.JjkClientBootstrap.initClient()` | A | Technique screen, domain HUD, keybinds, CE sync, cast-pose animation |

---

## Native ports (reference → our packages)

### A — JJK (`com.political.curse.jjk`)

| File | Ported from | Notes |
| --- | --- | --- |
| `JjkProcedures.java` | cursedfate / JujutsuCraft radial pulses | radialBurst, lifeSiphonRing, vortexPull, barrierPulse |
| `JjkPortedTechniques.java` | cursedfate procedure patterns | +5 techniques (sunder_ring, soul_vortex, crimson_harvest, sanctum_pulse, void_lance_storm) |
| `HumanoidModelCastPoseMixin.java` | NotEnoughAnimations cast arms | Native client mixin, no NEA dep |
| `TechniqueCastS2C.java` | — | Multplayer cast-pose broadcast |

### B — Flight (`com.political.flight`)

| Behavior | Ported from viltrumitecore |
| --- | --- |
| Throttle / boost threshold 0.6 | ✅ (pre-existing) |
| High-speed ram knockback | ✅ (pre-existing) |
| Boost collision bypass (`noPhysics`) | ✅ **new** — mirrors EntityCollisionMixin |

### B — Accessories (`com.political.expansion2.accessories`)

| Addition | Ported from Artifacts |
| --- | --- |
| `SWIFTSTRIDE` ability + Swiftstride Anklet | Running Shoes / speed-on-sprint pattern |

### B — Worldgen (`data/politicalserver/worldgen/`)

| Addition | Ported from Terralith |
| --- | --- |
| `shattered_coast` biome + overworld parameter slot | alpha_islands coastal mood (vanilla features only, original name) |

---

## Workstream inventory

### A — JJK Complete Overhaul ✅ LIVE

- **Packages:** `com.political.curse.{energy,technique,domain,jjk}`, networking, client bootstrap
- **Mixins:** `HumanoidModelCastPoseMixin` (client cast pose)
- **Controls:** G = techniques, V = domain, Z/X/C/B = bound slots

### B — Content Porting ✅ LIVE

- **Packages:** `com.political.content.*`, worldgen biomes (6), ability accessories (10)
- **Mixins:** None (server-side)

### C — Library + VFX ✅ LIVE

- **Packages:** `com.political.vfx.*`, `com.political.client.compat.jei.*` (optional compileOnly)
- **JEI:** 3 categories when JEI installed; zero hard dependency

---

## Build verification

```
.\gradlew.bat clean build
```

Expected: **BUILD SUCCESSFUL**. JEI deprecation warnings in compat classes are non-fatal (compileOnly).

---

## Reference docs

- `docs/STANDALONE.md` — player + developer standalone policy
- `docs/integration/JAR_AUDIT.md` — extracted JAR inventory (reference only)
- `docs/integration/PLAN.md` — integration plan
- `docs/integration/handoff/` — per-stream handoffs
