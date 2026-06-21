# Workstream A — JJK Complete Overhaul — Handoff Manifest

Centerpiece feature: a cursed-energy resource layer, ten original cursed techniques, three original
Domain Expansions, sorcerer-grade gating, curses that are visible only to players who can perceive
them, and slick mixin-free GUIs/HUD. Everything is **additive** and **self-contained** — nothing in a
shared file was rewritten. The whole feature comes online with **three one-line calls** (below).

---

## 1. Integration one-liners (the only wiring you need)

| Where | Add this line |
| --- | --- |
| `com.political.RpgPoliticsMod#onInitialize` (common) | `com.political.curse.JjkBootstrap.init();` |
| `com.political.client.PoliticalClient#onInitializeClient` (client) | `com.political.client.JjkClientBootstrap.initClient();` |

`JjkBootstrap.init()` already calls `com.political.net.JjkNetworking.registerA()` internally, so you do
**not** need to touch `ModNetworking`. If you would rather register the payloads from `ModNetworking`
instead, call `JjkNetworking.registerA()` there and delete that line from `JjkBootstrap` — **do not do
both**, or the payload types register twice and the client crashes on join.

That's it. Without these calls everything still **compiles** (classes are just dormant).

---

## 2. Files & packages created

### `com.political.curse` (edited 2 existing + new files)
- `JjkBootstrap.java` *(new)* — common bootstrap: registers techniques/domains, installs server cursed-energy providers, calls `JjkNetworking.registerA()`, wires the server tick (domains + cursed-energy broadcast) and disconnect cleanup.
- `SorcererGrade.java` *(new)* — read-only helpers over `DataManager.sorcererGrade` (gating + suggested max-CE per grade).
- `CurseHexBoltGoal.java` *(new)* — custom vanilla `Goal`: cursed-energy ranged hex bolt for curses.
- `CurseEntity.java` *(edited)* — added `registerGoals()` (adds the hex-bolt goal atop inherited zombie goals), `isInvisibleTo(Player)` perception override, and a `baseInvisibleTo` helper.

### `com.political.expansion2.curses` (edited 1 existing)
- `CursedSpirit2Entity.java` *(edited)* — `isInvisibleTo(Player)` override; **boss** spirits stay visible to everyone, non-boss spirits use the perception gate. Confirmed these still use their existing custom models/renderers (registered via `Spirit2Client`); no renderer changes were made.

### `com.political.curse.energy` *(new package)*
- `CursedEnergy.java` — side-aware facade + `canPerceive(Player viewer, int curseGrade)` + perception threshold table + pluggable server/client providers.
- `CursedEnergyManager.java` — server gameplay API (current/max/spend/add) delegating to the existing `StatManager` (single source of truth), plus `broadcast`/`syncAllTo`.

### `com.political.curse.technique` *(new package)*
- `CursedTechnique.java` (data model), `TechniqueResolver.java` (functional), `TechniqueContext.java` (targeting + player-attributed damage helpers), `TechniqueRegistry.java` (ordered registry, `knownFor(grade)`), `CursedTechniques.java` (**10 original techniques**), `TechniqueManager.java` (cast: grade/CE/cooldown checks → resolve → spend → VFX; `openMenu`).

### `com.political.curse.domain` *(new package)*
- `DomainEffect.java` (functional), `CursedDomain.java` (data model), `DomainRegistry.java`, `Domains.java` (**3 original domains**), `DomainManager.java` (timed sure-hit zones; `expand`, `tick`).

### `com.political.net` *(new files only)*
- `CursedEnergySyncS2C.java` — broadcast `[entityId, current, max, grade]` to all clients.
- `TechniqueMenuS2C.java` — opens/refreshes the technique screen.
- `TechniqueActionC2S.java` — `open` / `cast` / `cast_slot`.
- `DomainActionC2S.java` — expand a domain (blank id = best available).
- `JjkNetworking.java` — `registerA()` registers all of the above + serverbound receivers.

### `com.political.client` *(new files only, client source set)*
- `CursedClientState.java` — synced grade/CE, known techniques + domains, client-side slot bindings, per-entity CE table.
- `JjkClientBootstrap.java` — `initClient()`: client CE providers, S2C receivers, keybinds, HUD registration.
- `TechniqueScreen.java` — `RpgScreen`-style select/bind/cast screen + domain activation button.
- `DomainHud.java` — left-edge HUD: sorcerer grade + the four key-bound techniques (themed colours).

### Resources
- `src/main/resources/assets/politicalserver/lang/en_us.jjk.json` *(new)* — lang fragment (see §6).

---

## 3. The 10 cursed techniques (all original names/mechanics)

| id | name | element | CE | cooldown | req. grade | effect |
| --- | --- | --- | --- | --- | --- | --- |
| `severing_edge` | Severing Edge | BLOOD | 6 | 0.6s | Grade 4 | melee slash cone |
| `hollow_lance` | Hollow Lance | VOID | 14 | 1.5s | Grade 3 | piercing line beam |
| `riftpalm` | Riftpalm | ARCANE | 10 | 1.2s | Grade 4 | knockback nova |
| `ashen_pyre` | Ashen Pyre | FIRE | 12 | 2.0s | Grade 4 | igniting cone |
| `frostbind_coil` | Frostbind Coil | FROST | 12 | 2.5s | Grade 3 | slowing line |
| `stormcall_brand` | Stormcall Brand | LIGHTNING | 18 | 3.0s | Grade 2 | targeted lightning |
| `grave_tether` | Grave Tether | BLOOD | 14 | 2.25s | Grade 3 | drain + self-heal |
| `warding_sigil` | Warding Sigil | HOLY | 10 | 6.0s | Grade 4 | self buff |
| `shade_step` | Shade Step | VOID | 8 | 1.0s | Grade 4 | forward blink |
| `verdant_snare` | Verdant Snare | NATURE | 16 | 3.5s | Grade 2 | AoE root + poison |

(Internal grade scale: 1 = Grade 4 … 5 = Special Grade, matching the rest of the mod.)

**3 domains:** `garden_of_severing_bloom` (NATURE, req Grade 2), `tomb_of_the_hollow_maw` (VOID, req Grade 1),
`cathedral_of_still_light` (HOLY, req Special Grade). Each is a timed, anchored, sure-hit zone reusing
`VfxHelper.domainExpansion`/`domainPulse`.

---

## 4. Controls (registered client-side, no shared-file edits)

- **G** — open Cursed Techniques screen
- **V** — expand best available domain
- **Z / X / C / B** — cast the technique bound to slots 1–4 (bind them in the screen)

---

## 5. Mixins

**None added.** Per the brief, curse visibility uses the `Entity#isInvisibleTo(Player)` override rather
than a mixin, and all VFX/casting is server-driven. Nothing to register in
`politicalserver.mixins.json` or `politicalserver.client.mixins.json`.

> Not Enough Animations note: I evaluated a client `@Inject` to drive a casting arm-swing/pose off
> technique input. It is **not** required for the slice and was skipped to keep the build clean. If you
> want it later, the clean hook is the local-player input/animation tick on the client; it can be a
> targeted `@Inject(cancellable=false)` listed under the `client` array of
> `politicalserver.client.mixins.json` (package `com.political.client.mixin`). Reported, not added.

---

## 6. Lang

New keys live in `assets/politicalserver/lang/en_us.jjk.json`. **Minecraft only loads `en_us.json`**, so
please **merge** this fragment into `assets/politicalserver/lang/en_us.json` (you own that file). Gameplay
does not depend on it — the screens read English names straight from the registries — but the keybind
labels in Options and any future localisation do.

---

## 7. Cursed energy / max-CE integration (optional polish)

Cursed energy reuses the existing `StatManager` pool (already persisted + synced), so techniques/domains
spend the same CE the HUD shows. **One optional enhancement** to make grades visibly raise the CE
ceiling (deliverable #4): in `com.political.combat.StatManager#compute`, add the grade term, e.g.

```java
s.maxCursedEnergy += com.political.curse.SorcererGrade.maxCursedEnergyFor(
        com.political.politics.DataManager.sorcererGrade(player.getStringUUID()));
```

This is the only change that touches a non-A file and is **not** required for a green build.

---

## 8. Assumptions

- `RpgPoliticsMod#onInitialize` runs on both client and server (standard Fabric), so the technique/domain
  registries are populated on the client too; `JjkClientBootstrap` also bootstraps them defensively.
- Sorcerer grade persistence + auto-promotion already exist in `DataManager`/`ProgressionManager`; a
  technique is "known" once the player's grade meets its requirement (no extra persistence needed).
- Slot bindings are intentionally client-side only (no server round-trip); they reset on client restart.
  Persisting them is a follow-up if desired.
- Curses already have custom models/renderers (`CurseModels`/`CurseRenderer`, `Spirit2Client`); the
  visibility override is rendering-agnostic and does not alter them.

---

## 9. Build status

- **My code compiles cleanly.** `./gradlew compileJava` reports **zero** errors in any Workstream-A
  package (`com.political.curse**`, `com.political.net.Jjk*`/new packets, `com.political.client` JJK files).
- At hand-off time the **full** `build`/`compileJava` was red **only** because of two *untracked,
  in-progress* files owned by other concurrent workstreams — `src/main/java/com/political/content/creatures/ContentCreature.java`
  (missing `isFood` override) and `src/main/java/com/political/expansion2/accessories/AbilityAccessories2.java`
  (`hasImpulse` symbol). Both are outside my packages and I did not edit them. Once those workstreams
  finish, the build should be green with no further changes from A.

---

## 10. Follow-ups (nice-to-have, not blocking)

1. Merge the lang fragment into `en_us.json` (§6).
2. Optionally fold `SorcererGrade.maxCursedEnergyFor` into `StatManager#compute` (§7).
3. Optionally persist client slot bindings.
4. Optionally add the NEA casting-animation client mixin (§5).
5. Original pixel-art icons for techniques/domains via `tools/` could replace the abbreviation tiles in
   `TechniqueScreen` (currently colour-themed text tiles — no textures needed, stays green).
