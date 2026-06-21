# Integration Status — RPG Politics 2 Mega-Integration

**Date:** 2025-06-21  
**Target:** Minecraft 26.2 · Fabric · Java 25  
**Build:** `.\gradlew.bat clean build` → **BUILD SUCCESSFUL** (JEI deprecation warnings only)

---

## Summary

All three parallel workstreams (A JJK, B Content, C VFX/Library) are **wired and live**. Shared entrypoints, lang, and sounds were merged by the integration pass. No package conflicts required resolution — each stream used disjoint packages as planned.

---

## Wired entrypoints

### `RpgPoliticsMod.onInitialize()` (common)

| Call | Workstream | Purpose |
| --- | --- | --- |
| `com.political.vfx.VfxBootstrap.init()` | C | Registers 6 custom particle types + 9 VFX sound events |
| `com.political.content.ContentBootstrap.init()` | B | Ability accessories, ambient creatures, biome feature injection |
| `com.political.curse.JjkBootstrap.init()` | A | Techniques, domains, CE providers, JJK networking, server tick |

`JjkBootstrap.init()` already calls `JjkNetworking.registerA()` — **do not** also register those payloads in `ModNetworking`.

### `PoliticalClient.onInitializeClient()` (client)

| Call | Workstream | Purpose |
| --- | --- | --- |
| `com.political.vfx.client.VfxClientBootstrap.initClient()` | C | Particle providers for custom types |
| `com.political.content.client.ContentClientBootstrap.initClient()` | B | Creature models + renderers |
| `com.political.client.JjkClientBootstrap.initClient()` | A | Technique screen, domain HUD, keybinds, CE sync |

Order: VFX client bootstrap runs after common `VfxBootstrap.init()` (Fabric runs common init first).

---

## Resource merges

| Source fragment | Merged into | Keys added |
| --- | --- | --- |
| `lang/en_us.jjk.json` | `lang/en_us.json` | Keybind labels, technique/domain names |
| `lang/en_us.content.json` | `lang/en_us.json` | Accessories, creatures, biomes |
| `lang/en_us.vfx.json` | `lang/en_us.json` | JEI categories, VFX subtitles |
| `sounds.vfx.json` | `sounds.json` | 9 VFX sound event definitions |

**Total lang keys after merge:** 1692 (sorted alphabetically). Fragment files remain in-tree for reference/regen but are not loaded by Minecraft.

---

## Workstream inventory

### A — JJK Complete Overhaul ✅ LIVE

- **Packages:** `com.political.curse.{energy,technique,domain}`, `com.political.net.Jjk*`, `com.political.client.{JjkClientBootstrap,TechniqueScreen,DomainHud,CursedClientState}`
- **Mixins:** None (curse visibility via `Entity#isInvisibleTo`)
- **Controls:** G = techniques, V = domain, Z/X/C/B = bound slots

### B — Content Porting ✅ LIVE

- **Packages:** `com.political.content.*`, `com.political.world.biome.ContentBiomes`, `com.political.world.structures.*`, `com.political.expansion2.accessories.AbilityAccessories2*`
- **Registered:** 9 ability accessories, 3 ambient creatures, 2 biome decorative features
- **Mixins:** None

### C — Library + VFX ✅ LIVE

- **Packages:** `com.political.vfx.*`, `com.political.sound.VfxSounds`, `com.political.client.compat.jei.*`
- **Registered:** 6 particle types, 9 sound events, 3 JEI categories (auto-discovered via `@JeiPlugin`)
- **Mixins:** None (render overlay deferred to `WorldRenderEvents` if needed)

---

## Conflict resolution

No duplicate registrations or overlapping packages were found. All three streams used additive, self-contained bootstraps with handoff manifests under `docs/integration/handoff/`.

---

## Still TODO (non-blocking)

| Item | Owner | Notes |
| --- | --- | --- |
| Grade-scaled max CE in `StatManager#compute` | A polish | Add `SorcererGrade.maxCursedEnergyFor(...)` term — optional |
| Persist client technique slot bindings | A | Currently reset on client restart |
| NEA casting-arm client mixin | A | Optional animation hook |
| Technique/domain pixel-art icons | A | Replace abbreviation tiles in `TechniqueScreen` |
| Standalone biomes in worldgen | B | 5 biome JSONs exist; need multi-noise parameter-list datapack |
| Structure auto-scatter | B | `ContentStructures.planInto(...)` ready; wire to `StructureManager` or debug command |
| Creative-tab entries for ability accessories | B | Iterate `AbilityAccessories2.items()` in `ModTabs` |
| Tighten JEI placeholder item ids | C | `PoliticalJeiRecipes` uses vanilla fallbacks for economy/relic rows |
| Domain overlay pass | C | Prefer `WorldRenderEvents.AFTER_TRANSLUCENT` over mixin |
| Political/economy idea-recovery from old builds | B §9 | Proposals only — verify against existing `combat`/`progression` before porting |

---

## Build verification

```
.\gradlew.bat clean build
BUILD SUCCESSFUL in ~17s
```

Warnings: JEI API deprecations in `GearAbilityCategory`, `EconomyConversionCategory`, `CursedRelicCategory` (pre-existing from workstream C; non-fatal).

---

## Reference docs

- `docs/integration/JAR_AUDIT.md` — extracted JAR inventory
- `docs/integration/PLAN.md` — integration plan
- `docs/integration/handoff/A_jjk.md` — JJK handoff
- `docs/integration/handoff/B_content.md` — content handoff
- `docs/integration/handoff/C_vfx.md` — VFX/JEI handoff
