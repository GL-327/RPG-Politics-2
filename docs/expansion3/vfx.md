# Expansion 3 — Visual Effects (`com.political.vfx`)

A mixin-free, server-driven VFX library that makes powers, abilities, crits,
domains, dungeon traps, and bosses feel impactful. Everything is spawned through
`ServerLevel.sendParticles` + `ServerLevel.playSound` using **vanilla** particles
and sounds proven to compile in this code base (Minecraft 26.2, Mojang mappings).

> **Ownership / coordination.** This package is fully self-contained. It only
> *adds* cosmetic particles/sounds — it never touches damage, movement, cooldowns,
> or any game state. The call sites listed below are **recommendations for the
> integration agent**; nothing in existing engines (`AbilityEngine`,
> `PowerManager`/`PowerManager2`, melee/ranged engines) has been modified, to avoid
> conflicting with the polish agent's prior work.

## Files

| File | Purpose |
| --- | --- |
| `vfx/VfxElement.java` | 8 element themes (particles + sounds + accent tint). |
| `vfx/VfxHelper.java` | ~25 reusable, parameterized effect routines. |

---

## 1. Element themes

`VfxElement` bundles a coherent look per element. `core` = dense body particle,
`trail` = lighter halo/wisp, `spark` = snappy impact particle, plus a cast and an
impact `SoundEvent` and an accent `tintRgb`.

| Element | core | trail | spark | cast sound | impact sound | tint |
| --- | --- | --- | --- | --- | --- | --- |
| `FIRE` | `FLAME` | `LAVA` | `SMOKE` | `FIRECHARGE_USE` | `FIRECHARGE_USE` | `0xFF6A1A` |
| `FROST` | `SNOWFLAKE` | `CLOUD` | `SNOWFLAKE` | `AMETHYST_BLOCK_CHIME` | `AMETHYST_BLOCK_CHIME` | `0x9FE3FF` |
| `VOID` | `REVERSE_PORTAL` | `PORTAL` | `SQUID_INK` | `WARDEN_SONIC_BOOM` | `WARDEN_SONIC_BOOM` | `0x7A2BD6` |
| `LIGHTNING` | `ELECTRIC_SPARK` | `END_ROD` | `CRIT` | `WARDEN_SONIC_BOOM` | `WARDEN_SONIC_BOOM` | `0xFFF066` |
| `HOLY` | `END_ROD` | `GLOW` | `ENCHANTED_HIT` | `BEACON_ACTIVATE` | `AMETHYST_BLOCK_CHIME` | `0xFFF4C2` |
| `NATURE` | `HAPPY_VILLAGER` | `SPORE_BLOSSOM_AIR` | `COMPOSTER` | `BEACON_AMBIENT` | `BEACON_AMBIENT` | `0x6FCB3A` |
| `BLOOD` | `DAMAGE_INDICATOR` | `CRIT` | `DAMAGE_INDICATOR` | `RAVAGER_ROAR` | `WARDEN_ROAR` | `0xB81B1B` |
| `ARCANE` | `ENCHANTED_HIT` | `WITCH` | `ENCHANT` | `EVOKER_CAST_SPELL` | `AMETHYST_BLOCK_CHIME` | `0xC06CFF` |

Accessors: `element.theme()`, `.core()`, `.trail()`, `.spark()`, `.castSound()`,
`.impactSound()`, `.tintRgb()`, `.pitch()`.

---

## 2. Routine catalog (`VfxHelper`)

All methods are `static`. `level` is a `ServerLevel`; points are `Vec3` (or raw
doubles). Element-themed wrappers pull particles + sounds from a `VfxElement`.

### Primitives
| Method | Effect |
| --- | --- |
| `spawn(level, particle, x,y,z, count, sx,sy,sz, speed)` | Raw `sendParticles` wrapper (null-safe). |
| `burst(level, particle, at, count, spread, speed)` | Localized point burst. |
| `playAt(level, at\|x,y,z, sound, vol, pitch)` | Position sound (PLAYERS). |

### Beams & lines
| Method | Effect |
| --- | --- |
| `beam(level, particle, origin, dir, length, step, perStep, jitter)` | Straight beam along a direction. |
| `beamBetween(level, particle, a, b, perBlock, jitter)` | Beam connecting two points (tethers, chain lightning). |
| `sparkleTrail(level, particle, from, to)` | Scattered motion trail (dashes, projectiles). |

### Cones
| `cone(level, particle, origin, dir, length, halfAngleDeg, rings, perRing)` | Expanding cone fan (breaths, sweeps). |

### Rings, novas & shockwaves
| Method | Effect |
| --- | --- |
| `ring(level, particle, center, radius, points, speed)` | Flat static ring. |
| `novaRing(level, particle, center, radius, points, outwardSpeed)` | Outward-bursting ring (one-tick expansion). |
| `shockwave(level, particle, center, maxRadius, rings)` | Several concentric ground rings (slams). |

### Spirals, vortices, pillars & orbits
| Method | Effect |
| --- | --- |
| `spiral(level, particle, base, radius, height, turns, points)` | Ascending helix. |
| `vortex(level, particle, base, radius, height, turns, points)` | Inward-collapsing funnel. |
| `pillar(level, particle, base, height, perBlock, jitter)` | Vertical pillar (smite/strike telegraph). |
| `orbit(level, particle, center, radius, yOffset, points, phase)` | One-frame orbital ring; advance `phase` to animate. |

### Domes & domain expansion
| Method | Effect |
| --- | --- |
| `domeShell(level, particle, center, radius, points, hemisphere)` | Hollow sphere/hemisphere shell (golden-angle spread). |
| `domainExpansion(level, element, center, maxRadius)` | **Showcase**: 3 nested shells + core flash + floor ring + themed boom. |
| `domainPulse(level, element, center, radius, phase)` | Cheap per-tick rotating perimeter + rising wisps. |

### Ground decals
| `groundCrack(level, particle, center, length, spokes)` | Radial jagged cracks along the floor (earth-shatter traps). |

### Combat feedback
| Method | Effect |
| --- | --- |
| `hitSpark(level, target, element)` | Themed impact spark + impact sound on a struck entity. |
| `critSpark(level, target, multiplier)` | Bright crit burst scaled by crit-damage multiplier + crit chime. |
| `ferocitySpark(level, target, extraHits)` | One slash-spark fan per bonus hit + sweep sound. |
| `damageFeedback(level, target, element, intensity)` | Cheap themed sparks, `intensity` 0..1, no sound spam. |

### Auras, level-up & bosses
| Method | Effect |
| --- | --- |
| `auraColumn(level, entity, element, phase)` | Rising swirl around an entity (buff/charged state). |
| `levelUpBurst(level, entity)` | Pillar + halo + spiral + chime (level-up/awakening). |
| `bossPhaseBurst(level, element, center)` | Dome flash + shockwave + pillar + roar (spawn/enrage). |

### Element-themed wrappers (the everyday calls)
| Method | Effect |
| --- | --- |
| `elementBurst(level, element, at, scale)` | "Power fired" point burst + cast sound. |
| `elementBeam(level, element, origin, dir, length)` | Themed beam + cast sound. |
| `elementCone(level, element, origin, dir, length, halfAngleDeg)` | Themed cone + cast sound. |
| `elementNova(level, element, center, radius)` | Themed expanding AoE ring + cast sound. |
| `elementTrail(level, element, from, to)` | Themed motion trail. |

---

## 3. Optional sound-agent hook (`ModSounds`)

The sound agent's `com.political.sound.ModSounds` **is present** and exposes custom
`SoundEvent`s. `VfxHelper`/`VfxElement` deliberately do **not** import it (so this
package compiles standalone), but because every VFX sound argument is a plain
`SoundEvent`, the integration agent can pass `ModSounds.*` directly, or simply play
the custom sound alongside the VFX. Recommended pairings:

| VFX routine | Default vanilla sound | Recommended `ModSounds` swap |
| --- | --- | --- |
| `elementBurst`/`elementBeam`/`elementCone` (FIRE) | `FIRECHARGE_USE` | `ModSounds.POWER_CAST_FIRE` |
| ... (FROST) | `AMETHYST_BLOCK_CHIME` | `ModSounds.POWER_CAST_FROST` |
| ... (VOID) | `WARDEN_SONIC_BOOM` | `ModSounds.POWER_CAST_VOID` |
| ... (LIGHTNING) | `WARDEN_SONIC_BOOM` | `ModSounds.POWER_CAST_LIGHTNING` |
| ... (HOLY) | `BEACON_ACTIVATE` | `ModSounds.POWER_CAST_HEAL` |
| `domainExpansion` | `WARDEN_SONIC_BOOM` + `BEACON_ACTIVATE` | `ModSounds.POWER_CAST_DOMAIN_OPEN` |
| `bossPhaseBurst` | `ENDER_DRAGON_GROWL` | `ModSounds.BOSS_PHASE` / `BOSS_SPAWN` |
| `critSpark` | `PLAYER_ATTACK_CRIT` | `ModSounds.MELEE_CRIT` |
| `levelUpBurst` | `BEACON_ACTIVATE` + `EXPERIENCE_ORB_PICKUP` | `ModSounds.LEVEL_UP` |
| trap routines | (none) | `ModSounds.DUNGEON_TRAP` |

Two clean integration patterns:

```java
// (a) play the VFX silently, then layer the custom sound:
VfxHelper.elementNova(level, VfxElement.FIRE, center, 6.0);
ModSounds.play(level, center.x, center.y, center.z, ModSounds.POWER_CAST_FIRE);

// (b) drive a low-level routine and pass the custom sound straight in:
VfxHelper.burst(level, VfxElement.VOID.core(), at, 40, 1.0, 0.05);
VfxHelper.playAt(level, at, ModSounds.POWER_CAST_VOID, 1.4f, 0.7f);
```

---

## 4. Recommended call sites & integration hooks

### 4.1 Combat crits / ferocity — `combat/AbilityEngine.java`

In `applyCritAndFerocity(attacker, target, level, base)`:

- **Crit branch** (currently spawns inline `ParticleTypes.CRIT` at lines ~142-143):
  replace with
  ```java
  VfxHelper.critSpark(level, target, s.critDamage / 100.0);
  ```
- **Ferocity loop** (the `for (int i = 0; i < extra; i++)` block): after the loop,
  ```java
  VfxHelper.ferocitySpark(level, target, extra);
  ```
- **Base hit / on-hit abilities** in `onAttack(...)`: for elemental on-hit effects
  add a `hitSpark`, e.g. `IGNITE -> VfxHelper.hitSpark(level, target, VfxElement.FIRE);`,
  `FROST -> ...FROST`, `WITHER_TOUCH -> ...VOID`, `POISON -> ...NATURE`,
  `THUNDER_STRIKE -> ...LIGHTNING`.

### 4.2 Powers — `expansion2/powers/PowerManager2.java` (`cast(...)`)

Append a themed VFX after each `yield true;` (purely additive). Element mapping by
power family:

| Power family / examples | Element | Recommended call |
| --- | --- | --- |
| `HOMELANDER_BEAM`, `STARLIGHT_BOLT`, `BIRD_STRIKE`, `GAMMA_RAY` | `LIGHTNING` / `HOLY` | `elementBeam(level, …, p.getEyePosition(), p.getViewVector(1f), range)` |
| `VILTRUMITE_*` melee (`MEGA_PUNCH`, `THUNDERCLAP`, `IMPACT_CRATER`) | `LIGHTNING` | `elementNova(level, LIGHTNING, p.position(), 9)` / `bossPhaseBurst` for ultimates |
| `SOLDIER_BOY_CHARGE`, `BLOOD_BOILING`, `ULTIMATE_STAR_FALL` | `FIRE` | `elementCone(level, FIRE, eye, view, len, 25)` |
| `ICE_FORMATION`, `DEEP_TIDAL_CRUSH` | `FROST` | `elementCone` / `elementNova` |
| `BLACK_NOIR_STRIKE`, `RIKA_MANIFEST`, `DEATH_SWARMING`, `CURSED_SPIRIT_MANIPULATION` | `VOID` | `hitSpark` on target / `elementBurst` |
| `ATOM_EVE_SHIFT`, `CURSE_COLLAGE`, `BUTCHER_BERSERK` (heals/buffs) | `HOLY` / `NATURE` | `auraColumn(level, p, …, phase)` + `levelUpBurst` on big heals |
| `DISASTER_PLANTS` | `NATURE` | `elementNova(level, NATURE, p.position(), 9)` |
| `RYAN_OUTBURST`, `FRENCHIE_ARSENAL`, `ULTIMATE_MAXIMUM_UZUMAKI` (AoE blasts) | `FIRE`/`VOID` | `bossPhaseBurst` or `elementNova` at impact `at` |
| `ULTIMATE_*` (all) | per theme | layer `bossPhaseBurst` + `ModSounds.POWER_CAST_ULTIMATE` |

Generic fallback in `activate(...)` (after the existing
`AMETHYST_BLOCK_CHIME`): `VfxHelper.elementBurst(level, elementFor(power), player.position(), 1.0);`

### 4.3 Domains — `PowerManager2.castDomain(...)` and `tickDomains(...)`

- On open (`castDomain`, replacing/augmenting `Power2Effects.domainRing`):
  ```java
  VfxHelper.domainExpansion(level, VfxElement.VOID, p.position(), 13.0);
  ```
- Per tick while active (`tickDomains`):
  ```java
  VfxHelper.domainPulse(sl, VfxElement.VOID, p.position(), 12.0, (tickCounter % 360) * Math.PI / 180.0);
  ```
- On collapse/expiry: pair with `ModSounds.DOMAIN_COLLAPSE`.

> Element per domain: most JJK domains read best as `VOID`; `DOMAIN_THUNDER_GAAIS`
> → `LIGHTNING`, `DOMAIN_HORIZON` → `HOLY`.

### 4.4 Melee & ranged engines

- `expansion2/melee/Melee2AbilityEngine.java` & `expansion/melee/MeleeAbilityEngine.java`:
  swap the inline `particleCone(...)` calls for `VfxHelper.elementCone(...)` keyed
  off the weapon's element; add `VfxHelper.hitSpark(level, target, element)` on hit
  and `VfxHelper.ferocitySpark` on combo finishers.
- `expansion2/ranged/RangedAbilityEngine2.java`: use `elementBeam` for `BEAM/AIM/CHAIN`
  casts and `elementTrail(level, element, lastPos, hitPos)` for projectile streaks;
  `elementNova` for `HOLY_NOVA`/`SANCTUM`.

### 4.5 Dungeon traps — `world/dungeons/DungeonLoot.java` (post-op handlers)

Traps are placed as blocks (`arrowTrap` = dispenser + pressure plate, `soulFireTrap`
= soul fire) by `DungeonGenUtil`; fire the VFX where the post-op **triggers/arms**
in `DungeonLoot`:

| Trap (`DungeonPlan.PostKind`) | Element | Recommended call |
| --- | --- | --- |
| `ARROW_TRAP` | `ARCANE` | `VfxHelper.cone(level, VfxElement.ARCANE.spark(), dispenser, facingVec, 6, 8, 5, 4)` on trigger |
| `SOUL_FIRE_TRAP` | `VOID` | `VfxHelper.pillar(level, ParticleTypes.SOUL_FIRE_FLAME, pos, 3, 4, 0.1)` + `groundCrack(level, VOID.spark(), pos, 3, 6)` |
| earth/slam traps (future) | `BLOOD`/`FIRE` | `VfxHelper.shockwave(level, …, center, 5, 3)` + `ModSounds.DUNGEON_TRAP` |

Pair any trap activation with `ModSounds.DUNGEON_TRAP`.

### 4.6 Bosses — `expansion2/npc/Expansion2BossSpawner.java`

- **Spawn** (`spawn(...)`, right after `level.addFreshEntity(mob)`, ~line 71):
  ```java
  VfxHelper.bossPhaseBurst(level, elementFor(boss), mob.position());
  // optionally: ModSounds.play(level, mob, ModSounds.BOSS_SPAWN, 1.5f, 0.8f);
  ```
- **Phase / enrage transition** (`tickEnrage(...)`, inside the
  `le.getHealth() < le.getMaxHealth() * 0.35f && !le.hasEffect(STRENGTH)` block — it
  fires exactly once):
  ```java
  if (level instanceof ServerLevel sl)
      VfxHelper.bossPhaseBurst(sl, VfxElement.BLOOD, le.position());
  // optionally: ModSounds.play(level, le, ModSounds.BOSS_PHASE, 1.6f, 0.7f);
  ```
- **Ambient channel** while a boss is alive: periodic `auraColumn(level, mob, element, phase)`.

Suggested boss → element: `PYRION`→`FIRE`, `ASHARA`→`ARCANE`, `MORBIDIUS`→`VOID`,
`KAGURO`→`VOID`, `SYLVA`→`NATURE`, `RAZIEL`→`HOLY`, default→`BLOOD`.

### 4.7 Progression / flight (bonus hooks)

- Level-up / power awakening (`power/PowerManager`, `combat/StatManager`):
  `VfxHelper.levelUpBurst(level, player)` + `ModSounds.LEVEL_UP`.
- Flight boom / dash (`flight/FlightManager`): `VfxHelper.sparkleTrail(level, FROST.trail(), lastPos, player.position())` + `ModSounds.DASH`.

---

## 5. Notes

- **Future enhancement:** colored `DustParticleOptions` per element (true tints).
  Deferred because the MC 26.2 dust constructor signature could not be verified from
  decompiled sources here; the `tintRgb` field is already stored on each theme for
  when that lands. Until then, themes use distinctive vanilla particles, which read
  clearly and are guaranteed to compile.
- All routines are **stateless and side-effect-free** beyond particles/sounds, so
  they are safe to call from any server thread context already used by the engines
  above (server tick / event callbacks).
