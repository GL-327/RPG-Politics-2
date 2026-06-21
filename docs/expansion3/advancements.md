# Expansion 3 — Advancements / Progression Tree

A deep, data-driven advancement tree that gives players long-term goals across **every** major
system in the mod: cursed energy / jujutsu, Compound V powers, the RPG arsenal, world dungeons,
curse hunting, and elected politics + economy.

- **Data:** `src/main/resources/data/politicalserver/advancement/**` (pure JSON)
- **Custom granter:** `src/main/java/com/political/progression/ProgressionManager.java`
- **Lang fragment:** `tools/lang-fragments/advancements.json`
- **Namespace:** `politicalserver` · **MC:** 26.2 · **No mixins**

## Branches (6 tabs, 63 advancements)

Each branch root carries a `background`, so it shows up as its own advancement **tab**.

| Tab (root) | Advancements | Detection | Theme |
|---|--:|---|---|
| **Sorcery** (`sorcery/root`) | 11 | custom state + 1 item | Awaken cursed energy, climb Grade 4 → Special Grade, learn techniques, Black Flash, Six Eyes, Domain Expansion, obtain a cursed tool |
| **Hero** (`hero/root`) | 10 | 2 item + 8 custom state | Obtain & drink Compound V, manifest Strength / Heat Vision / Speed / Healing / Flight, Viltrumite Might, Star Power, collect the V variants |
| **Arsenal** (`arsenal/root`) | 12 | vanilla `inventory_changed` | Obtain each weapon rarity Common → **Mythic** (melee + ranged), a full **Mythic Celestial set**, accessories, the Godslayer Relic |
| **Explorer** (`explorer/root`) | 13 | vanilla `inventory_changed` + `player_killed_entity` | Get the Structure Compass, defeat the boss of all **10 dungeon types**, loot dungeon tokens, and the *Dungeon Master* capstone |
| **Hunter** (`hunter/root`) | 7 | custom death hook + vanilla kills | Exorcise curses by grade (Grade 2 / Grade 1 / Special Grade), 50-curse veteran, slay roaming mini-bosses and world bosses |
| **Politics & Economy** (`politics/root`) | 10 | custom state | Citizenship, vote, Officer → Councilor, hold office, civic-duty taxes, bank 1k/10k/100k, 1M-coin Tycoon |

Frames: `task` (most), `goal` (notable milestones), `challenge` (capstones such as Special Grade,
Star Power, Mythic gear, full set, Dungeon Master, world boss, Tycoon — several award bonus XP).

## How detection works

The tree deliberately leans on **vanilla criteria** wherever possible:

- **`minecraft:inventory_changed`** — obtaining serums, weapons (by rarity), armor pieces / full set,
  accessories, relics, the Structure Compass, dungeon loot tokens. Item-id lists are taken straight
  from the registered items (`wpn_*`, `arc_*`, `arm_*`, `acc_*`, `compound_v`, `cursed_*`, etc.).
- **`minecraft:player_killed_entity`** — every dungeon boss and the Hunter mini-boss / world-boss
  nodes. Each `mob_*` / `mob2_*` is a real registered `EntityType`, so the `entity_properties`
  predicate matches on `{ "type": "politicalserver:mob2_<id>" }` directly.
- **`minecraft:impossible`** — used by goals that vanilla cannot observe (sorcerer grade, known
  powers/techniques, citizenship, civic rank, office, bank balance, curse grade on exorcism). These
  carry a single `granted` criterion and are awarded from Java (see below).

### Dungeon-boss mapping (Explorer)

| Dungeon | Boss entity |
|---|---|
| Cursed Crypt | `mob_lich_sovereign` |
| Bandit Hideout | `mob2_bandit_king` |
| Ancient Ruins | `mob_warlord_kael` |
| Viltrumite Lab | `mob2_grand_marshal` |
| Sorcerer Sanctum | `mob2_archmage_sovereign` |
| Dragon's Vault | `mob2_infernal_sovereign` |
| Flooded Temple | `mob2_frost_queen` |
| Netherite Vault | `mob2_pit_commander` |
| Overgrown Catacombs | `mob2_dread_lich` |
| Crystal Caverns | `mob2_arcane_titan` |

## Custom granter — `ProgressionManager`

`com.political.progression.ProgressionManager` awards the `impossible`-triggered advancements via
`ServerPlayer.getAdvancements()`. It is fully **self-contained** and installs its own Fabric
listeners — it does **not** edit any shared file.

### Integration (one line)

Add to `RpgPoliticsMod#onInitialize`, next to the other `register()` calls:

```java
com.political.progression.ProgressionManager.register();
```

That's all that's required. `register()` installs:

1. **`END_SERVER_TICK` poll** (~2×/sec) — reads each online player's persistent state and grants
   any earned state-based advancement (idempotent; completed advancements short-circuit). Covers:
   sorcerer grade (1–5 → Grade 4 … Special Grade), known Compound V powers / cursed techniques /
   specific abilities, Six Eyes trait, domain techniques, citizenship, civic rank, elected office,
   bank balance milestones, net-worth Tycoon, civic-duty taxes, and lifetime exorcism counts
   (`root`, 50-curse `veteran`).
2. **`AFTER_DEATH` listener** — when a player kills a `CurseEntity`, grants the Hunter grade nodes
   based on the curse's internal grade (≥3 → Grade 2, ≥4 → Grade 1, =5 → Special Grade).

### Optional explicit hooks

For instant feedback the moment an action happens (instead of waiting for the next poll), callers
may invoke these idempotent helpers from existing systems:

| Hook | Awards |
|---|---|
| `ProgressionManager.onVote(ServerPlayer)` | `politics/vote` |
| `ProgressionManager.onTaxPaid(ServerPlayer)` | `politics/civic_duty` |
| `ProgressionManager.onCurseExorcised(ServerPlayer, int internalGrade)` | `hunter/root` + grade nodes |
| `ProgressionManager.onDungeonBossDefeated(ServerPlayer, String dungeonTypeId)` | `explorer/<type>` |
| `ProgressionManager.refresh(ServerPlayer)` | re-runs the full state poll for one player |

> **Mapping note (26.2):** the granter looks up advancements with
> `server.getAdvancements().get(Identifier.fromNamespaceAndPath("politicalserver", path))` and awards
> with `player.getAdvancements().award(holder, criterion)`. If a future mapping renames
> `ServerAdvancementManager#get`, only that one line in `grant(...)` needs adjusting.

## Translations

Merge `tools/lang-fragments/advancements.json` into
`src/main/resources/assets/politicalserver/lang/en_us.json`. Keys follow the vanilla convention
`advancements.politicalserver.<branch>.<name>.title` / `.description`.

## Notes

- Backgrounds use vanilla advancement textures (`end`, `nether`, `stone`, `adventure`, `husbandry`).
  A custom tab background can be dropped in later by editing each `*/root.json` `display.background`.
- Every icon references a real registered item (mod or vanilla), so the tree renders with no missing
  textures even before any custom advancement art is added.
- Adding a goal later: drop a new JSON under the branch folder; if it needs mod state, add one line
  to `ProgressionManager.poll(...)` (or call a hook).
