# Expansion 3 — Config System & Global Balance Review

`politicalserver` is mixin-free and runs on Minecraft 26.2. This expansion adds a single,
JSON-backed server configuration so owners can tune the mod without touching code, plus a
catalogue-wide balance audit with conservative safety clamps wired into the core combat / stat /
power / spawn paths.

- **Package:** `com.political.config`
- **File on disk:** `config/politicalserver.json` (Fabric config dir, via `FabricLoader.getInstance().getConfigDir()`)
- **Reload:** `/politicalconfig reload` (op / gamemaster level)
- **Inspect:** `/politicalconfig` or `/politicalconfig show`

Every key defaults to the value the mod already hard-coded, so a fresh install (or a missing /
partial file) behaves exactly as before. The file is (re)written on load so new keys always appear
for editing.

---

## 1. How it works

- `PoliticalConfig.load()` runs as the **first line** of `RpgPoliticsMod.onInitialize()`, before any
  mob types register, so registration-time scaling (mob HP/damage, spawn weights, natural-spawn
  flags) reads live values.
- The live instance is reachable from anywhere via `PoliticalConfig.get()`. Per-tick / per-hit
  systems (damage, crit, regen, dungeon scatter, flight) read it directly, so a reload takes effect
  immediately without a restart.
- Registration-time values that are cached in static fields (natural-spawn flags) are pushed by
  `PoliticalConfig.apply()` on every load/reload. Note: mob **attribute** scaling and **spawn
  weight** multipliers are applied when entity types register at startup, so changing those keys
  requires a game restart to fully take effect (a reload updates everything else).
- A hand-edited file is defensively `sanitize()`d (negatives floored, rates clamped to `0..1`,
  etc.) so a bad edit can never crash the mod; on parse failure it falls back to defaults.

---

## 2. Config keys

### Combat — player melee damage (`combat/AbilityEngine`)

| Key | Type | Default | Effect |
|---|---|---|---|
| `combatDamageEnabled` | bool | `true` | Master switch for the Skyblock damage replacement. When `false`, the attack callback returns `PASS` and vanilla combat runs (no custom damage, crit, ferocity, on-hit abilities, cursed-gear bonus, or Black Flash). |
| `damageBaseFlat` | double | `5.0` | Flat base added to every hit (the `5.0` in the original formula). |
| `damageMultiplier` | double | `1.0` | Scales the whole computed hit (`base + weapon dmg + strength·scale`). |
| `strengthDamageScale` | double | `0.15` | Bonus damage per point of Strength. |
| `maxHitDamage` | double | `5000.0` | Hard clamp on any single melee or crit hit. Safety net (current max legit hit ≈ 1.5k). |
| `critEnabled` | bool | `true` | Master switch for critical strikes. |
| `critChanceMultiplier` | double | `1.0` | Scales rolled crit chance. |
| `critDamageMultiplier` | double | `1.0` | Scales crit-damage bonus %. |
| `maxCritChancePercent` | double | `100.0` | Clamp on effective crit chance. |
| `ferocityEnabled` | bool | `true` | Master switch for ferocity extra-strikes. |

### Resource regeneration (`combat/StatManager`)

| Key | Type | Default | Effect |
|---|---|---|---|
| `manaRegenMultiplier` | double | `1.0` | Scales per-tick mana regen. |
| `cursedEnergyRegenMultiplier` | double | `1.0` | Scales per-tick cursed-energy regen. |

### Player stat clamps (`combat/StatManager`) — safety nets

| Key | Type | Default | Effect |
|---|---|---|---|
| `maxPlayerDefense` | double | `1000.0` | Clamp on the computed defense stat before it maps onto vanilla armor (`defense·0.15`). |
| `maxPlayerHealth` | double | `1024.0` | Clamp on computed max health (vanilla `MAX_HEALTH` attribute also caps at 1024). |

### Mob spawning & scaling (`expansion/mobs`, `expansion2/mobs`)

| Key | Type | Default | Effect |
|---|---|---|---|
| `naturalMobSpawnsEnabled` | bool | `true` | When `false`, custom expansion mobs never spawn naturally (manual + boss spawns still work). Pushed to both rosters' `naturalSpawnsEnabled` flags. |
| `mobSpawnWeightMultiplier` | double | `1.0` | Scales each custom mob's biome spawn weight (never drops a still-enabled mob below 1). |
| `mobHealthMultiplier` | double | `1.0` | Scales custom mob max-health at type registration. |
| `mobDamageMultiplier` | double | `1.0` | Scales custom mob attack-damage at type registration. |
| `maxMobHealth` | double | `5000.0` | Clamp on any custom mob's max health (current catalogue max ≈ 320). |
| `maxMobDamage` | double | `200.0` | Clamp on any custom mob's attack damage (current catalogue max ≈ 22). |

### Dungeons (`world/dungeons/DungeonManager`)

| Key | Type | Default | Effect |
|---|---|---|---|
| `dungeonsEnabled` | bool | `true` | When `false`, dungeons stop auto-scattering near players (manual `/dungeon` summon + in-flight builds still finish). |
| `dungeonSpawnRate` | double | `0.22` | Per-cell scatter chance `0..1`. |

### Rewards (`expansion/mobs`, `expansion2/mobs` death rewards)

| Key | Type | Default | Effect |
|---|---|---|---|
| `coinRewardMultiplier` | double | `1.0` | Scales coins dropped by custom mob kills. |

### Feature switches per content pack

| Key | Type | Default | Effect |
|---|---|---|---|
| `powersEnabled` | bool | `true` | When `false`, no power / cursed technique activates (`PowerManager.activateSelected` refuses). |
| `flightEnabled` | bool | `true` | When `false`, all non-creative (Viltrumite / gear / power) flight is suppressed (`FlightManager.wantsFlight` returns `false`). Creative/spectator flight untouched. |

---

## 3. Wired values (call-sites)

| File (owned) | Keys consumed |
|---|---|
| `combat/AbilityEngine.onAttack` | `combatDamageEnabled`, `damageBaseFlat`, `damageMultiplier`, `strengthDamageScale`, `maxHitDamage` |
| `combat/AbilityEngine.applyCritAndFerocity` | `critEnabled`, `critChanceMultiplier`, `critDamageMultiplier`, `maxCritChancePercent`, `ferocityEnabled`, `maxHitDamage` |
| `combat/StatManager.apply` | `maxPlayerHealth`, `maxPlayerDefense` |
| `combat/StatManager.tickAll` | `manaRegenMultiplier`, `cursedEnergyRegenMultiplier` |
| `power/PowerManager.activateSelected` | `powersEnabled` |
| `flight/FlightManager.wantsFlight` | `flightEnabled` |
| `expansion2/mobs/ExpansionMobs2` + `ExpansionMob2.createAttributes` | `mobSpawnWeightMultiplier`, `coinRewardMultiplier`, `mobHealthMultiplier`, `mobDamageMultiplier`, `maxMobHealth`, `maxMobDamage`, `naturalMobSpawnsEnabled` |
| `expansion/mobs/ExpansionMobs` + `ExpansionMob.createAttributes` | same as above (Phase-1 roster) |
| `world/dungeons/DungeonManager` | `dungeonsEnabled`, `dungeonSpawnRate` |

All wiring is **additive**: when every key holds its default, the computed result is identical to
the pre-config code path.

---

## 4. Balance review (audit)

The catalogue was enumerated end-to-end. The headline finding is that it is **internally
consistent** — stat ladders scale smoothly by rarity / role and there were no broken outliers that
distort the existing curve. The clamps below are therefore **safety nets**, set well above any real
value so current content is unchanged, while preventing extreme stat-stacking or aggressive config
multipliers from producing game-breaking / overflow numbers.

### Melee weapons (`expansion2/melee`, mirrored in `expansion`)
Damage by rarity is a clean ladder, identical across all 8 themes:

| Rarity | Weapon `damage` | Strength |
|---|---|---|
| Common | 26–28 | 16–17 |
| Uncommon | 46–48 | 30–31 |
| Rare | 66–68 | 42–43 |
| Epic | 90–92 | 60–61 |
| Legendary | 121–123 | 86–87 |
| **Mythic** | **165–167** | **125–126** |

Mythic top end (167 base + 152% crit damage) is the intended ceiling — ~6.4× Common, no anomalies.
Effective per-swing damage with full Mythic gear + accessories + powers tops out around ~1.5k
(weapon + strength scaling + crit + ferocity), comfortably under the `maxHitDamage = 5000` net.

### Armor (`expansion2/armor`)
Full-set defense totals climb smoothly: Common ≈ 38–48 → Legendary ≈ 148–150 → **Mythic ≈ 248–270**
(Infinity 270, Omniknight 258, Apocalypse 248). Mapped to vanilla armor as `defense·0.15`, even the
Mythic peak (~40 armor points) lands inside vanilla's effective reduction band. Full Mythic health
totals reach ~590 HP (base 100 + ~494 gear), under the `maxPlayerHealth = 1024` vanilla cap.
No outliers; clamps `maxPlayerDefense = 1000` / `maxPlayerHealth = 1024` are pure safety.

### Mob HP / damage (`expansion/mobs`, `expansion2/mobs`, named bosses)
Role-based ladder, consistent across all biomes/themes:

| Role | HP | Attack |
|---|---|---|
| Trash (hostile) | 28 | 5 |
| Neutral / beast | 40–50 | 4 |
| Miniboss | 120 | 13 |
| Boss | 280–320 | 18 |
| Named NPC boss | 180–300 | 12–22 |

Catalogue max HP ≈ 320 and max attack ≈ 22 — both far under the new clamps `maxMobHealth = 5000` /
`maxMobDamage = 200`, which only bite if an owner sets a large `mobHealthMultiplier` /
`mobDamageMultiplier` (e.g. a 10× HP server still caps a 320-HP boss at 3200, not unbounded).

### Powers (`power/PowerManager`)
Ability damage values (e.g. Hollow Purple 40, Maximum Meteor 30, Malevolent Shrine 20-in-cone) sit
within the weapon/mob curve and were left as-is. No clamps were applied to power numbers — they are
gated instead by the `powersEnabled` switch and existing energy costs / cooldowns.

### Issues found / fixed
- **No absurd per-item or per-mob outliers** were found; the data set is balanced as authored.
- **Risk fixed:** previously *nothing* bounded final player hit damage, player defense/health, or
  mob attributes, so future content or a large config multiplier could overflow or trivialise
  combat. Added conservative clamps at the core owned call-sites (`maxHitDamage`, `maxCritChancePercent`,
  `maxPlayerDefense`, `maxPlayerHealth`, `maxMobHealth`, `maxMobDamage`). Defaults are intentionally
  generous so **current content is unchanged**.

> Scope note: per the ownership boundary, clamps were applied only in core files this expansion owns
> (`combat/**`, `power/**`, `expansion*/mobs`, `world/dungeons`, `flight/**`). The item/armor
> catalogues themselves (owned by other systems) were audited read-only and left untouched.

---

## 5. Integration hooks

**Load (once, first thing in init):**
```java
// RpgPoliticsMod.onInitialize()
com.political.config.PoliticalConfig.load();   // reads config/politicalserver.json, applies flags
```

**Command registration (inside the CommandRegistrationCallback):**
```java
com.political.config.ConfigCommands.register(dispatcher);   // /politicalconfig [reload|show]
```

**Runtime access (anywhere):**
```java
com.political.config.PoliticalConfig cfg = com.political.config.PoliticalConfig.get();
double dmgMult = cfg.damageMultiplier;
```

**Reload at runtime:** `/politicalconfig reload` calls `PoliticalConfig.load()` again, which
re-reads the file, re-`sanitize()`s, re-`apply()`s the natural-spawn flags, and rewrites the file.
Per-tick / per-hit systems pick up new values on their next tick; mob attribute & spawn-weight
scaling needs a restart.
