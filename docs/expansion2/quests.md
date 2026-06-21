# Expansion 2 — Quests & Bounties

Quest system in `com.political.expansion2.quests`. Progress stored in
`{world}/expansion2_quests.json` (separate from `PoliticsData.activeQuests` slayer quests).

## Content counts

| Category | Count |
|----------|------:|
| Registered quests (`QuestRegistry`) | **66** |
| Quest kinds (`QuestKind`) | **14** |
| Quest items (`quest2_*`) | **15** |

## Quest kinds

| Kind | Examples |
|------|----------|
| `FETCH` | Iron bundle, glow berries, diamonds |
| `DELIVER` / `HOLD_ITEM` | Bounty seal, cursed relic, map fragment |
| `KILL_SPIRIT` | Wisp cull, horned harvest, flame calamity |
| `KILL_MOB` | Zombie patrol, blaze hunt |
| `BANK_DEPOSIT` | Secure savings (500), vault builder (2000) |
| `COLLECT_COINS` | Coin hoarder, tainted investment |
| `AWAKEN_CE` | Reach 100 CE or max cursed energy |
| `ELECTION` | Vote during active election |
| `BOUNTY` | Starts `BountyManager.startQuest` (UNDEAD, VOID, …) |
| `EXORCISE_COUNT` / `REACH_GRADE` | Lifetime exorcisms, sorcerer grade |
| `BOSS` | Defeat each of 12 named NPC bosses |
| `DEPOSIT_TREASURY` | Patron of state |

## Public API hooks used

- `BountyManager.startQuest` / `questStatus` — bounty-type quests
- `DataManager.addCoins` / `removeCoins` / `addCredits` / `addTreasury`
- `BankManager.balance` / `deposit` (via player bank GUI elsewhere)
- `StatManager.getCursedEnergy` / `getMaxCursedEnergy`
- `DataManager.sorcererGrade` / `cursesExorcised`
- `ElectionManager.isElectionActive` + `Expansion2QuestManager.onVote`
- Spirit kills via `CursedSpiritEntity.species()` on death event

## Integration

### Bootstrap

```java
com.political.expansion2.Expansion2Bootstrap.register();
```

Registers items, quest storage, death/tick hooks, and commands.

### Election vote (see also npc.md)

```java
Expansion2QuestManager.onVote(voter);
```

### Translations & textures

1. Merge `tools/lang-fragments/quests2.json` into `en_us.json`.
2. Run `node tools/gen-quests2.js` for `quest2_*` item PNGs/models.

## Commands

| Command | Description |
|---------|-------------|
| `/exp2quest list` | Total registered quest count |
| `/exp2quest offers <ARCHETYPE>` | Quest IDs for an archetype (e.g. `BOUNTY_BROKER`) |
| `/exp2quest accept <id>` | Accept a quest |
| `/exp2quest status` | Active quest progress |
| `/exp2quest abandon` | Drop active quest |
| `/exp2quest boss <id>` | Spawn named boss |
| `/exp2quest give <item> [count]` | Op — grant quest item |

## Dialogue registration

Quest UI is embedded in NPC dialogue (`exp2:quest:list`, `exp2:quest:turnin`). Requires
`Expansion2DialogueBridge.handleChoice` delegate in `DialogueManager` (see `npc.md`).
