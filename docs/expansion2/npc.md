# Expansion 2 — NPCs & Dialogue

Fallout-style villager content in `com.political.expansion2.npc`. Reuses core
`DialogueNode` / `DialogueOpenS2C` / `VillagerDialogueScreen` — no new client screen.

## Content counts

| Category | Count |
|----------|------:|
| Villager archetypes (`NpcArchetype`) | **35** |
| Named NPC bosses (`NamedNpcBoss`) | **12** |
| Dialogue tree nodes (per-archetype branches) | **7 nodes × 35** |
| NPC service actions (`Expansion2NpcServices`) | **30+** |

## Archetypes

Blacksmith, Enchanter, Sorcerer, Bounty Broker, Banker, Politician, Healer,
Cursed Goods Merchant, Armor Smith, Weapon Smith, Alchemist, Scout, Mercenary,
Tax Collector, Election Clerk, Spirit Hunter, Curse Scholar, Rune Carver,
Gem Cutter, Herbalist, Innkeeper, Fence, Loan Shark, Auctioneer, Town Crier,
Diplomat, Warden, Exorcist, Shrine Keeper, Beast Tamer, Mapmaker, Relic Dealer,
Grimoire Seller, Summoner, Doomsday Prophet.

Each villager is tagged on `ENTITY_LOAD` with a stable archetype from UUID hash
and renamed `"<Name> the <Archetype>"`.

## Named bosses

Enhanced **vindicators** with scaled HP/damage, enrage below 35% HP, coin rewards,
and quest hooks. IDs: `valdris`, `morgrim`, `sylva`, `raziel`, `croft`, `blackwood`,
`morbidius`, `ashara`, `garrick`, `kaguro`, `thorn`, `pyrion`.

## Integration (off-limits files)

### 1. Bootstrap — `RpgPoliticsMod.onInitialize()`

Register **before** `VillagerManager.register()` and **before** `DialogueManager.register()`:

```java
com.political.expansion2.Expansion2Bootstrap.register();
```

### 2. Dialogue choice delegate — `DialogueManager.handleChoice`

At the **top** of `handleChoice`:

```java
if (com.political.expansion2.npc.Expansion2DialogueBridge.handleChoice(player, choiceId, action)) return;
```

Expansion 2 actions use the `exp2:` prefix (`exp2:nav:…`, `exp2:svc:…`, `exp2:quest:…`, `exp2:farewell`).

### 3. Election vote hook — `ElectionManager.castVote`

After a successful vote:

```java
com.political.expansion2.quests.Expansion2QuestManager.onVote(voter);
```

### 4. Translations

Merge `tools/lang-fragments/npc2.json` into `assets/politicalserver/lang/en_us.json`.

### 5. Commands (auto-registered by bootstrap)

- `/exp2npc list` — archetype/boss counts (op)
- `/exp2npc boss <id>` — spawn named boss (op)

## Player flow

1. Right-click villager (not shift) → Expansion 2 dialogue if tagged.
2. Branch: lore / services / quests / rumors / boss hints.
3. Services spend coin and call public APIs (`DataManager`, `BankManager`, `StatManager`, `BountyManager`, `VillagerManager.runBlacksmith`).
4. Quest contracts listed per archetype; turn in at matching giver NPC.

Shift + right-click still opens vanilla villager trades.
