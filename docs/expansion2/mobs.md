# Phase-2 RPG Creatures (`expansion2` mobs)

Second-wave non-curse creature content inspired by Prominence II / RAD2 / RPG modpack factions and
elemental lineages. All code lives in `com.political.expansion2.mobs` (server) and the matching
client package. Entity ids use the **`mob2_`** prefix.

- **No mixins.** One spec-driven entity class (`ExpansionMob2`) with vanilla AI goals.
- **115 creatures** total (see breakdown below).
- **Coins on kill** via `DataManager.addCoins` (same hook pattern as phase-1 `ExpansionMobs`).
- **Mount-like neutrals** (`SKITTISH` plains/desert/mountain/swamp beasts) flee from players; no
  riding API — visual flavour only.
- **Boss phase-2** at 50% HP: speed + resistance, summon adds, broadcast message.

## Totals

| Role | Count | Notes |
|------|------:|-------|
| HOSTILE | 56 | Faction grunts + elemental scouts |
| NEUTRAL | 11 | Wildlife + elemental beasts (retaliate only) |
| SKITTISH | 5 | Mount-like visuals, flee from players |
| MINIBOSS | 27 | Boss bar, rare natural spawn (weight 1) |
| BOSS | 16 | Boss bar + phase-2 enrage; **no natural spawn** |
| **Total** | **115** | |

### Content buckets

- **Elemental lineages (63):** fire, ice, storm, earth, shadow, holy, arcane — each 9 creatures
  (5 hostile, 1 neutral beast, 2 mini-bosses, 1 raid boss).
- **Faction armies (35):** bandits, knights, cultists, undead, demons, fey, constructs — each 5
  (3 hostile, 1 mini-boss, 1 raid boss).
- **Raid lieutenants (6):** cross-lineage mini-bosses (`mob2_raid_*_warden`).
- **Extra raid bosses (2):** `mob2_cataclysm_herald`, `mob2_eternal_necromancer`.
- **Wildlife (5)** + **mount-like neutrals (4).**

## Public API (integration agent)

```java
com.political.expansion2.mobs.ExpansionMobs2.register();
com.political.expansion2.mobs.ExpansionMobs2Client.registerClient();
com.political.expansion2.mobs.MobCommands2.register(dispatcher);
ExpansionMobs2.allTypes(); // List<EntityType<ExpansionMob2>>
ExpansionMobs2.ids();      // all mob2_ ids
```

### Integration steps (off-limits files)

1. **Common init** — in `RpgPoliticsMod.onInitialize()`:
   ```java
   com.political.expansion2.mobs.ExpansionMobs2.register();
   ```
2. **Commands** — in the existing `CommandRegistrationCallback` lambda:
   ```java
   com.political.expansion2.mobs.MobCommands2.register(dispatcher);
   ```
3. **Client** — in `PoliticalClient.onInitializeClient()`:
   ```java
   com.political.expansion2.mobs.ExpansionMobs2Client.registerClient();
   ```
4. **Lang** — merge `tools/lang-fragments/mobs2.json` into `en_us.json`.
5. **Textures** — run `node tools/gen-mobs2.js` (115 PNGs under
   `assets/politicalserver/textures/entity/mob2_*.png`).

## Commands (`/rpgmob2`, op only)

| Command | Description |
|---------|-------------|
| `/rpgmob2 list` | List all 115 creature ids |
| `/rpgmob2 summon <id> [count]` | Spawn near executor (1–20) |
| `/rpgmob2 boss <id>` | Spawn a single creature (intended for raid bosses) |
| `/rpgmob2 spawns` | Toggle natural spawning for phase-2 creatures |

Phase-1 `/rpgmob` remains separate; both sets can coexist.

## Tooling

| File | Purpose |
|------|---------|
| `tools/mob2-roster.json` | Source-of-truth roster data |
| `tools/build-mob2-roster.js` | Regenerate `mob2-roster.json` from templates |
| `tools/gen-mob2-roster.js` | Emit `MobRoster2.java`, `mobs2.json`, `mob2-ids.js` |
| `tools/gen-mobs2.js` | Generate all entity textures |

After editing the roster JSON or build script:
```powershell
cd tools; node build-mob2-roster.js; node gen-mob2-roster.js; node gen-mobs2.js
```

## Design notes

- Natural spawns use `Monster.checkMonsterSpawnRules` (dark + on ground). Neutrals still use
  `MobCategory.MONSTER` so they appear at night — same intentional behaviour as phase-1.
- Drops are vanilla items only in code; docs may reference existing RPG item ids for future loot
  tables (e.g. `RADIANT_HALBERD` on holy bosses, `NIGHTFALL_SCYTHE` on undead raid bosses,
  `THUNDERCALLER` on storm bosses) without touching other packages.
- Bosses summon lower-tier adds from the same lineage/faction on phase 2 (see `summons` in
  `MobRoster2.java`).

## Sample roster (full list via `/rpgmob2 list`)

| Lineage | Example ids |
|---------|-------------|
| Fire | `mob2_ember_scout` … `mob2_infernal_sovereign` |
| Ice | `mob2_frost_shardling` … `mob2_frost_queen` |
| Storm | `mob2_spark_imp` … `mob2_thunder_god` |
| Earth | `mob2_stone_scout` … `mob2_earthbound_titan` |
| Shadow | `mob2_shade_scout` … `mob2_shadow_emperor` |
| Holy | `mob2_light_scout` … `mob2_seraph_lord` |
| Arcane | `mob2_arcane_scout` … `mob2_archmage_sovereign` |
| Bandits | `mob2_highway_robber` … `mob2_bandit_king` |
| Knights | `mob2_squire_errant` … `mob2_grand_marshal` |
| Cultists | `mob2_blood_acolyte` … `mob2_apocalypse_herald` |
| Undead | `mob2_rot_walker` … `mob2_dread_lich` |
| Demons | `mob2_imp_scout` … `mob2_demon_prince` |
| Fey | `mob2_pixie_trickster` … `mob2_fey_queen` |
| Constructs | `mob2_rust_automaton` … `mob2_colossus_prime` |

Role legend: H = hostile, N = neutral, S = skittish, MB = mini-boss, B = boss (phased).
