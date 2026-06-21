# Phase-2 Cursed Spirits (`com.political.expansion2.curses`)

**Delivered:** 90 new cursed spirit species (zero overlap with phase-1's 21). Entity ids are prefixed `spirit2_`.

## Package layout

| File | Role |
|------|------|
| `SpiritSpecies2.java` | Generated enum — stats, model kind, behaviors, loot tier (edit `tools/spirit2-species-data.js`) |
| `Behavior2.java` | 28 combat traits (14 inherited + 14 new) |
| `CursedSpirit2Entity.java` | Shared entity — mixin-free ability tick loop |
| `CurseSpirits2.java` | Registry, spawn API, manifest, death rewards |
| `Spirit2Client` / `Spirit2Renderer` / `Spirit2Models` | Client renderers (8 model archetypes) |

## Grade bands

| Band | JJK grade | Count | Examples |
|------|-----------|-------|----------|
| 1 | Grade 4 fodder | 32 | Mote Flea, Grudge Gnat, Void Mote, Husk Flea |
| 2 | Grade 3 common | 15 | Slit Whisperer, Mirror Curse, Knife Whisper |
| 3 | Grade 2 dangerous | 15 | Horned Devourer, Shikigami Fox/Owl, Curse Armament |
| 4 | Grade 1 calamity | 8 | Volcano/Forest/Ocean/Plague Calamity, Domain Storm |
| 5 | Special Grade boss | 20 | Smallpox Deity, Cursed Womb, Dragon/Rainbow Dragon, Domain Emperor |

## New mechanics (Behavior2)

- **DOMAIN_FIELD** — AoE slowness/weakness zone
- **CURSED_BOLT** — soul projectile + weakness
- **BLINDNESS_CURSE** / **VOICE_CURSE** — sensory debuffs
- **CURSE_SEAL** — weakness + slowness combo
- **SWARM_REPLICATE** — splits at 50% HP
- **CHAIN_CURSE** — spreads wither/weakness to second target
- **GRAVITY_PULL** — pulls players inward
- **DISASTER_ERUPT** — fire knockback burst (volcano archetype)
- **MAXIMUM_BURST** — large radial magic explosion
- **SHIKIGAMI_CALL** / **NECROMANCY_RISE** — themed minion summons
- **ARMAMENT_FORM** — cursed-tool enhanced melee
- **RAINBOW_BEAM** — multi-colour hitscan (rainbow dragon)

## Public API

```java
CurseSpirits2.register();                    // common init
Spirit2Client.registerClient();              // client init
CurseSpirits2.spawnAt(level, pos, grade);    // random species for grade
CurseSpirits2.spawnSpeciesAt(level, pos, grade, SpiritSpecies2.DRAGON_CURSE);
CurseSpirits2.randomForGrade(5);
CurseSpirits2.allTypes();
CurseSpirits2.isSpirit2(entity);
```

## Assets & tooling

- **Textures:** `node tools/gen-cursespirits2.js` → 90× 64×64 PNGs in `textures/entity/spirit2_*.png`
- **Lang:** `tools/lang-fragments/cursespirits2.json` (merge into `en_us.json` by integration agent)
- **Enum regen:** `node tools/gen-spirit2-enum.js` after editing `tools/spirit2-species-data.js`

## Integration hooks (not wired — owned by integration agent)

1. **`RpgPoliticsMod.onInitialize()`** — add `CurseSpirits2.register();`
2. **`PoliticalClient.onInitializeClient()`** — add `Spirit2Client.registerClient();`
3. **`CurseManager.spawnAt()`** — optionally mix phase-2 spirits, e.g. 50% `CurseSpirits2.spawnAt(...)` at high grades
4. **`CurseManager.onEntityDeath()`** — phase-2 has its own death hook; no change required unless unified messaging desired
5. **Lang merge** — fold `cursespirits2.json` into `assets/.../lang/en_us.json`

## Sample Special Grade bosses

| Id | Display name | Signature kit |
|----|--------------|---------------|
| `spirit2_smallpox_deity` | Smallpox Deity | poison + chain curse + domain + enrage |
| `spirit2_cursed_womb` | Cursed Womb | summoner + swarm replicate + life drain |
| `spirit2_dragon_curse` | Dragon Curse | fire blast + shockwave + ranged |
| `spirit2_rainbow_dragon` | Rainbow Dragon Curse | rainbow beam + teleport + enrage |
| `spirit2_domain_emperor` | Domain Emperor | domain field + curse seal + maximum burst |
| `spirit2_maximum_technique_echo` | Maximum Technique Echo | maximum burst + shockwave + teleport |
| `spirit2_finger_bearer_alpha` | Finger Bearer Alpha | bruiser + shockwave + summoner |
| `spirit2_cataclysm_archon` | Cataclysm Archon | disaster erupt + fire + teleport |

## Loot scaling

Death rewards scale by `lootTier` (1–5) and `boss` flag: cursed energy, coins, bone/skull remnants, nether star on bosses. Tags: `rpg_curse2_<grade>`, `rpg_curse2_<id>`.
