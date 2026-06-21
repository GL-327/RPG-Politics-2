# Workstream C — Library + Maximal VFX / Graphical Effects — Handoff

Status: **BUILD GREEN** (`.\gradlew.bat compileJava compileClientJava` and `.\gradlew.bat build` both SUCCESSFUL). All deliverables compile, including the JEI plugin against the real JEI API.

This workstream is purely **additive** and lives only in packages it owns:
`com.political.vfx`, `com.political.vfx.particle`, `com.political.vfx.client`,
`com.political.sound` (new file only), and `com.political.client.compat.jei`.
No existing public method was removed or renamed; no shared file was edited.

---

## 1. VfxHelper expansions (`src/main/java/com/political/vfx/VfxHelper.java`)

All new routines are server-driven through the existing `sendParticles` / `playSound`
path (mixin-free, additive, cosmetic only). Existing routines are untouched.
Originally reimplemented from alexscaves particle *behaviours* (no copied code/art).

New low-level / shape routines:

- `lightningBolt(level, particle, from, to, segments, jaggedness)` — jagged polyline bolt.
- `forkedLightning(level, particle, from, to, segments, forks)` — bolt with random branches.
- `chainLightning(level, particle, source, List<Vec3> targets)` — hops source→t0→t1….
- `chainLightningWeb(level, particle, source, Vec3... targets)` — star-burst arcs.
- `arc(level, particle, a, b, sag, points)` — parabolic sagging tether/whip.
- `blackHoleImplosion(level, swirl, core, center, radius, points)` — inward accretion swarm.
- `implosionFlash(level, particle, center, radius)` — outward detonation (nova+shell+core).
- `twinOrbCollide(level, orb, beamP, a, b, beamDir, beamLength)` — two orbs collide → beam.
- `meteor(level, head, trail, from, to)` — falling streak + impact splash.
- `swordSlash(level, particle, origin, dir, radius, arcDeg, tiltDeg, points)` — crescent arc.
- `crossSlash(level, particle, origin, dir, radius)` — "X" finisher.
- `channelBeam(level, core, pulse, origin, dir, length, phase)` — sustained beam w/ travelling pulse.
- `helixBeam(level, particle, origin, dir, length, radius, turns, strands, points)` — DNA-strand beam.
- `runeCircle(level, ringP, glyph, center, radius, glyphs, phase)` — rotating ground rune circle.
- `sigil(level, particle, center, radius, n, skip)` — star-polygon sigil (e.g. n=5,skip=2 pentagram).
- `domeBand(level, particle, center, radius, yFrac, points)` — latitude band ring of a sphere.
- `domeRibs(level, particle, center, radius, ribs, pointsPerRib)` — vertical dome ribs.
- `debrisBurst(level, particle, center, count, power)` — outward rubble with upward bias.
- `impactCrater(level, dust, debris, center, radius)` — shockwave + debris + dust puff.
- `domainWall(level, particle, center, radius, height, phase)` — lingering cylindrical wall.
- `domainCeiling(level, particle, center, radius, height)` — domed ceiling cap.

New element-themed wrappers (pull particles/sounds from `VfxElement`):

- `elementLightning`, `elementChain`, `elementBlackHole`, `elementBlackHoleCollapse`,
  `elementSlash`, `elementCrossSlash`, `elementChannel`, `elementHelixBeam`,
  `elementRuneCircle`, `elementDomainWall`, `elementMeteor`, `elementTwinOrb`.

New sound overload:

- `playAt(level, Vec3, SoundEvent, SoundSource, volume, pitch)` — explicit category for
  Sound-Physics friendliness (see §4).

> Phase-driven routines (`channelBeam`, `runeCircle`, `domainWall`, `domainPulse`, `orbit`)
> are designed to be called **each tick** with an advancing `phase` for animation.

`VfxElement.java` was **not** modified (no new elements were needed).

---

## 2. Custom particle types (scaffolding — needs 2 one-liners to go live)

The MC 26 particle pipeline was rewritten (no `TextureSheetParticle`; quad render-state +
`SingleQuadParticle.Layer`). The custom particle client renderer is implemented on the new
API (`SimpleAnimatedParticle`), so **no render mixin is required**.

Files:

- `com.political.vfx.particle.VfxParticles` (main) — 6 `SimpleParticleType`s via
  `FabricParticleTypes.simple()`, registered into `BuiltInRegistries.PARTICLE_TYPE`:
  `cursed_ember`, `void_mote`, `arc_spark`, `rune_glyph`, `radiant_mote`, `cinder`.
- `com.political.vfx.VfxBootstrap#init()` (main) — calls `VfxParticles.register()` and `VfxSounds.register()`.
- `com.political.vfx.client.VfxRisingParticle` (client) — original animated quad particle + `Provider`.
- `com.political.vfx.client.VfxClientBootstrap#initClient()` (client) — registers a provider per type
  via `ParticleProviderRegistry`.
- Assets (original art, authored by `tools/gen-vfx-particles.js`):
  - `assets/politicalserver/textures/particle/<name>.png` (6× 8×8 sprites)
  - `assets/politicalserver/particles/<name>.json` (texture definitions)

### Registration one-liners to wire (NOT done here, to avoid editing shared entrypoints)

```java
// RpgPoliticsMod#onInitialize() (common, src/main):
com.political.vfx.VfxBootstrap.init();

// PoliticalClient#onInitializeClient() (client, src/client):
com.political.vfx.client.VfxClientBootstrap.initClient();   // call AFTER VfxBootstrap.init()
```

Until wired, the types are compile-only scaffolding and every `VfxHelper` routine runs on
**vanilla** `ParticleTypes`, so nothing is blocked. After wiring, callers may pass the custom
types directly, e.g. `VfxHelper.runeCircle(level, VfxParticles.RUNE_GLYPH, VfxParticles.ARC_SPARK, center, r, 8, phase)`.
Both bootstrap calls are idempotent.

---

## 3. JEI recipe categories (real, against the stable JEI API)

`PoliticalJeiPlugin` (client) now implements `registerCategories` + `registerRecipes`. It stays a
safe no-op without JEI (loaded only via JEI's `@JeiPlugin` scan). Three real categories built on
`mezz.jei.api.recipe.category.AbstractRecipeCategory`:

- `category.GearAbilityCategory` (`politicalserver:gear_ability`) — gear/serum/relic → granted ability.
- `category.EconomyConversionCategory` (`politicalserver:economy_conversion`) — bank/coin/note conversions.
- `category.CursedRelicCategory` (`politicalserver:cursed_relic`) — 2×2 cursed-energy/relic crafting.

Supporting files:

- `recipe.GearAbilityRecipe`, `recipe.EconomyConversionRecipe`, `recipe.CursedRelicRecipe` — display records.
- `PoliticalJeiRecipes` — builds the display rows. **Items are resolved by registry id with a vanilla
  fallback** (`BuiltInRegistries.ITEM.getOptional(...).orElse(Items.PAPER)`), so it never hard-couples
  to other workstreams' item classes and never throws on a missing id.

Categories use slots (`setRecipe`) + arrow & header text (`createRecipeExtras` →
`addRecipeArrow` / `addText`) + rich tooltips (`addRichTooltipCallback`).

### Integration follow-up

- Update the placeholder item ids in `PoliticalJeiRecipes` (`mod("...")` / `mc("...")`) once the
  gear / economy / cursed-relic systems finalize their registry names. Current real ids used:
  `compound_v`, `temp_v`, `v1`, `anti_v` (from `com.political.power.ModItems`); the rest are vanilla
  stand-ins (gold, emerald, echo_shard, …) shown until real coin/relic items land.
- No entrypoint wiring needed — JEI discovers the plugin automatically.

---

## 4. Sounds (additive) + Sound Physics Remastered notes

- `com.political.sound.VfxSounds` (new file; **does not touch `ModSounds`**) registers 9 VFX cue
  events: `vfx_chain_lightning`, `vfx_black_hole_charge`, `vfx_black_hole_collapse`,
  `vfx_channel_loop`, `vfx_rune_hum`, `vfx_domain_wall`, `vfx_meteor_impact`, `vfx_slash`,
  `vfx_implosion`. Registered by `VfxBootstrap.init()` (idempotent).
- Sound definitions fragment: **`assets/politicalserver/sounds.vfx.json`** (each id mapped to layered
  vanilla events, mirroring the `ModSounds` `"type":"event"` approach).
  - **Action required:** Minecraft loads only **one** `assets/<ns>/sounds.json` per namespace, so the
    integration agent must **merge `sounds.vfx.json` into `assets/politicalserver/sounds.json`** (top-level
    key merge). Until merged, the events register fine and are simply silent. I did not edit the shared
    `sounds.json` to avoid clobbering concurrent edits.
- **Sound Physics Remastered** (runtime-auto-active) applies reverb/occlusion to *world* sounds. All
  VFX/combat cues should play through world categories (`PLAYERS` / `HOSTILE` / `BLOCKS`), never
  `MASTER` / `MUSIC`. `VfxHelper.playAt(...)` defaults to `PLAYERS`; the new
  `playAt(..., SoundSource, ...)` overload and `VfxSounds.play(..., SoundSource, ...)` let callers pick
  `HOSTILE` for mobs/bosses and `BLOCKS` for trap/structure cues. No further tuning needed —
  SPR processes these automatically; no compile-time dependency exists.

---

## 5. Client render mixin — intentionally NOT added

The optional world-space domain/cursed-energy overlay was evaluated and **skipped** to keep the build
green and avoid risk against MC 26's rewritten render pipeline. Fabric already exposes
`WorldRenderEvents` (e.g. `AFTER_TRANSLUCENT`) which covers a domain overlay pass without a mixin, so a
render mixin would be redundant per the "prefer Fabric callbacks" guidance.

- `src/client/resources/politicalserver.client.mixins.json` `"client"` array is left **empty** (untouched).
- If a mixin is later desired, add the class to `com.political.client.mixin` and append its name to that
  `"client"` array (re-read the file immediately before editing).

---

## 6. Lang fragment

- **`assets/politicalserver/lang/en_us.vfx.json`** — new keys only (JEI category titles +
  VFX-sound subtitles). **Action required:** merge these keys into `en_us.json` (the shared file was not
  edited). Keys:
  - `jei.politicalserver.category.gear_ability`, `…economy_conversion`, `…cursed_relic`
  - `subtitles.politicalserver.vfx_*` (9 entries)

---

## 7. Files created / changed

Created (main):
- `src/main/java/com/political/vfx/VfxBootstrap.java`
- `src/main/java/com/political/vfx/particle/VfxParticles.java`
- `src/main/java/com/political/sound/VfxSounds.java`
- `src/main/resources/assets/politicalserver/particles/{cursed_ember,void_mote,arc_spark,rune_glyph,radiant_mote,cinder}.json`
- `src/main/resources/assets/politicalserver/textures/particle/{…}.png` (6)
- `src/main/resources/assets/politicalserver/sounds.vfx.json`
- `src/main/resources/assets/politicalserver/lang/en_us.vfx.json`
- `tools/gen-vfx-particles.js`

Created (client):
- `src/client/java/com/political/vfx/client/VfxRisingParticle.java`
- `src/client/java/com/political/vfx/client/VfxClientBootstrap.java`
- `src/client/java/com/political/client/compat/jei/PoliticalJeiRecipes.java`
- `src/client/java/com/political/client/compat/jei/recipe/{GearAbilityRecipe,EconomyConversionRecipe,CursedRelicRecipe}.java`
- `src/client/java/com/political/client/compat/jei/category/{GearAbilityCategory,EconomyConversionCategory,CursedRelicCategory}.java`

Expanded (own package, additive only):
- `src/main/java/com/political/vfx/VfxHelper.java` (≈30 new routines; existing API untouched)
- `src/client/java/com/political/client/compat/jei/PoliticalJeiPlugin.java` (added category/recipe registration)

---

## 8. Assumptions & follow-ups

- **Assumption:** other workstreams own `ModItems`/economy/relic registries; JEI rows resolve real ids
  where known and fall back to vanilla otherwise. Tighten ids in `PoliticalJeiRecipes` when finalized.
- **Assumption:** custom particle/sound registration will be wired via the two one-liners in §2 from the
  common + client initializers (not done here to avoid editing shared entrypoints concurrently).
- **Merges needed:** `sounds.vfx.json` → `sounds.json`; `en_us.vfx.json` → `en_us.json`.
- **Texture regen:** `node tools/gen-vfx-particles.js` re-authors the 6 particle sprites + defs.
- **Build status:** `.\gradlew.bat build` → BUILD SUCCESSFUL (compileJava + compileClientJava verified;
  JEI plugin compiles against the JEI API on the classpath).
- **Future:** if a richer domain overlay is wanted, prefer `WorldRenderEvents`; only add a render mixin
  if a callback truly doesn't exist.
