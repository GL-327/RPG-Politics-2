# Expansion 3 — Custom Sound Design

Owner package: `com.political.sound.**`
Mixin-free Fabric / MC 26.2. Adds a mod-owned sound-event registry so every system can
reference stable named sounds instead of reaching for vanilla `SoundEvents` ad&nbsp;hoc.

References: existing registry patterns in `com.political.power.ModItems` and
`com.political.curse.ModEntities`; ad-hoc vanilla `playSound` usage across
`PowerManager2`, `Power2Effects`, `CursedSpirit2Entity`, `FlightManager`, etc.

---

## Files added

| File | Role |
| --- | --- |
| `src/main/java/com/political/sound/ModSounds.java` | Registers ~43 `SoundEvent`s + `play(...)` helpers |
| `src/main/resources/assets/politicalserver/sounds.json` | Maps each custom id to **layered vanilla** sounds (`"type": "event"`) |
| `src/main/resources/assets/politicalserver/sounds/README.txt` | Placeholder + instructions for dropping real `.ogg` later |
| `docs/expansion3/sounds.md` | This document |

Wired from `RpgPoliticsMod.onInitialize()` via `com.political.sound.ModSounds.register();`
(first registration, before items/blocks).

---

## Why no `.ogg` files yet

We cannot author real audio binaries here, so **nothing is silent**: every custom sound
event maps in `sounds.json` to one or more **vanilla** sound events using `"type": "event"`.
The client plays a sensible vanilla approximation today. To upgrade later, drop real `.ogg`
files into `assets/politicalserver/sounds/` and flip the matching `sounds.json` entries from
`"type": "event"` to a file reference (`{ "name": "politicalserver:ui/click" }`). The Java
side never changes — only the client-side `sounds.json` mapping decides what audio plays.

> Note on "layering": a single `sounds.json` event picks **one** of its listed sounds at
> random (variation), it does not stack them. For a genuinely layered/stacked effect, call
> `ModSounds.playLayered(...)` from code with several events.

---

## Registered sound events (43)

All ids live under the `politicalserver:` namespace. Java constant in `ModSounds`.

### Power / ability casts
| id | constant | vanilla approximation |
| --- | --- | --- |
| `power_cast_fire` | `POWER_CAST_FIRE` | firecharge.use + blaze.shoot |
| `power_cast_frost` | `POWER_CAST_FROST` | glass.break + powder_snow.break |
| `power_cast_void` | `POWER_CAST_VOID` | enderman.teleport + soul_escape |
| `power_cast_lightning` | `POWER_CAST_LIGHTNING` | lightning.impact + trident.thunder |
| `power_cast_heal` | `POWER_CAST_HEAL` | amethyst.chime + beacon.activate |
| `power_cast_domain_open` | `POWER_CAST_DOMAIN_OPEN` | beacon.power_select + warden.sonic_boom |
| `power_cast_ultimate` | `POWER_CAST_ULTIMATE` | warden.sonic_boom + generic.explode |
| `domain_collapse` | `DOMAIN_COLLAPSE` | glass.break + wither.break_block |
| `heal_pulse` | `HEAL_PULSE` | amethyst.chime |
| `shield_break` | `SHIELD_BREAK` | shield.break + anvil.land |

### Melee / weapons
| id | constant | vanilla approximation |
| --- | --- | --- |
| `melee_crit` | `MELEE_CRIT` | attack.crit + attack.strong |
| `weapon_swing_heavy` | `WEAPON_SWING_HEAVY` | attack.sweep + mace.smash_ground |
| `melee_combo_finish` | `MELEE_COMBO_FINISH` | mace.smash_ground_heavy + attack.knockback |
| `parry` | `PARRY` | shield.block + anvil.use |

### Mobs / bosses
| id | constant | vanilla approximation |
| --- | --- | --- |
| `spirit_screech` | `SPIRIT_SCREECH` | warden.agitated + ghast.scream |
| `spirit_attack` | `SPIRIT_ATTACK` | evoker.cast_spell + ghast.shoot |
| `spirit_summon` | `SPIRIT_SUMMON` | evoker.prepare_summon |
| `spirit_death` | `SPIRIT_DEATH` | zombie_villager.converted + soul_escape |
| `boss_roar` | `BOSS_ROAR` | ravager.roar + ender_dragon.growl |
| `boss_phase` | `BOSS_PHASE` | wither.spawn + ender_dragon.flap |
| `boss_spawn` | `BOSS_SPAWN` | wither.spawn |

### GUI / UI
| id | constant | vanilla approximation |
| --- | --- | --- |
| `ui_open` | `UI_OPEN` | barrel.open + ui.toast.in |
| `ui_click` | `UI_CLICK` | ui.button.click |
| `ui_close` | `UI_CLOSE` | barrel.close + ui.toast.out |
| `ui_error` | `UI_ERROR` | note_block.bass + villager.no |
| `ui_tab` | `UI_TAB` | book.page_turn |

### Dungeons
| id | constant | vanilla approximation |
| --- | --- | --- |
| `dungeon_ambient` | `DUNGEON_AMBIENT` | ambient.cave + warden.heartbeat |
| `dungeon_trap` | `DUNGEON_TRAP` | piston.extend + tnt.primed |
| `dungeon_chest_unlock` | `DUNGEON_CHEST_UNLOCK` | chest.locked + vault.open_shutter |
| `dungeon_secret` | `DUNGEON_SECRET` | amethyst_cluster.place + toast.challenge_complete |
| `dungeon_boss_gate` | `DUNGEON_BOSS_GATE` | end_portal.spawn + iron_door.open |

### Progression / economy
| id | constant | vanilla approximation |
| --- | --- | --- |
| `level_up` | `LEVEL_UP` | player.levelup + toast.challenge_complete |
| `rank_up` | `RANK_UP` | toast.challenge_complete + beacon.power_select |
| `quest_complete` | `QUEST_COMPLETE` | player.levelup + note_block.chime |
| `coin_gain` | `COIN_GAIN` | experience_orb.pickup + amethyst.hit |
| `coin_large` | `COIN_LARGE` | experience_orb.pickup + vault.eject_item |
| `trade_complete` | `TRADE_COMPLETE` | villager.yes + experience_orb.pickup |
| `election_win` | `ELECTION_WIN` | toast.challenge_complete + firework.launch |

### Movement / flight
| id | constant | vanilla approximation |
| --- | --- | --- |
| `flight_boom` | `FLIGHT_BOOM` | warden.sonic_boom + generic.explode |
| `flight_loop` | `FLIGHT_LOOP` | elytra.flying |
| `dash` | `DASH` | attack.sweep + trident.riptide |

### Curse system
| id | constant | vanilla approximation |
| --- | --- | --- |
| `curse_absorb` | `CURSE_ABSORB` | soul_escape + soul_sand.break |
| `curse_apply` | `CURSE_APPLY` | elder_guardian.curse + sculk.spread |
| `curse_cleanse` | `CURSE_CLEANSE` | amethyst.chime + zombie_villager.cure |

`sounds.json` coverage: **43 / 43** custom ids defined; every id resolves to live vanilla
audio so nothing is silent.

---

## `play(...)` API

```java
import com.political.sound.ModSounds;

// World sound for everyone in range (server-side):
ModSounds.play(level, x, y, z, ModSounds.POWER_CAST_FIRE, SoundSource.PLAYERS, 1.0f, 1.0f);

// At an entity (auto picks PLAYERS / HOSTILE category):
ModSounds.play(level, player, ModSounds.MELEE_CRIT);
ModSounds.play(level, spirit, ModSounds.SPIRIT_SCREECH, 1.0f, 0.7f);

// Personal UI / chime heard only by one player:
ModSounds.playToPlayer(serverPlayer, ModSounds.UI_CLICK);
ModSounds.playToPlayer(serverPlayer, ModSounds.LEVEL_UP, 1.0f, 1.0f);

// True layered/stacked one-shot:
ModSounds.playLayered(level, x, y, z, SoundSource.PLAYERS, 1.0f, 1.0f,
        ModSounds.POWER_CAST_ULTIMATE, ModSounds.BOSS_ROAR);

// Lookup by short id (e.g. for data-driven callers):
SoundEvent s = ModSounds.byId("dungeon_trap");
```

All helpers are null-safe and no-op on the client / for null events.

---

## Integration call sites

These existing systems are owned by other waves, so calls are **listed, not force-edited**
(except the `register()` wiring in `RpgPoliticsMod`, which mirrors every other subsystem).
Replace the ad-hoc vanilla `playSound` with the matching `ModSounds` call.

| File | Location | Current sound | Suggested `ModSounds` call |
| --- | --- | --- | --- |
| `power/PowerManager.java` | ability casts / domain activations | various | `POWER_CAST_*`, `POWER_CAST_DOMAIN_OPEN`, `POWER_CAST_ULTIMATE` |
| `expansion2/powers/PowerManager2.java:165` | `AMETHYST_BLOCK_CHIME` cast | amethyst chime | `play(level, player, POWER_CAST_HEAL)` |
| `expansion2/powers/PowerManager2.java:256` | `RAVAGER_ROAR` ultimate | ravager roar | `play(level, p, BOSS_ROAR)` |
| `expansion2/powers/Power2Effects.java:106` | beam/explosion | explode/beacon | `play(level, x,y,z, POWER_CAST_ULTIMATE)` |
| `expansion2/powers/Power2Effects.java:137` | `WARDEN_SONIC_BOOM` | sonic boom | `play(level, cx,cy,cz, POWER_CAST_VOID)` |
| `combat/AbilityEngine.java` | crit / heavy swing | — | `MELEE_CRIT`, `WEAPON_SWING_HEAVY`, `MELEE_COMBO_FINISH` |
| `expansion/melee/MeleeAbilityEngine.java` | melee ability fx | — | `MELEE_CRIT`, `MELEE_COMBO_FINISH` |
| `expansion2/melee/Melee2AbilityEngine.java:795` | `EXPERIENCE_ORB_PICKUP` | xp pickup | `play(level, p, MELEE_COMBO_FINISH)` |
| `expansion/ranged/RangedAbilityEngine.java` | ranged cast fx | — | `POWER_CAST_FIRE` / `POWER_CAST_LIGHTNING` |
| `expansion2/ranged/RangedAbilityEngine2.java:66,271-274` | `soundFor(cast)` switch | mixed | route through `POWER_CAST_*` by cast type |
| `curse/spirits/CursedSpiritEntity.java:154-232` | evoker/teleport/boom/summon/spawn | mixed | `SPIRIT_ATTACK`, `POWER_CAST_VOID`, `BOSS_PHASE`, `SPIRIT_SUMMON`, `BOSS_SPAWN` |
| `expansion2/curses/CursedSpirit2Entity.java:180-398` | warden/ghast/evoker/wither | mixed | `SPIRIT_SCREECH`, `SPIRIT_ATTACK`, `SPIRIT_SUMMON`, `SPIRIT_DEATH`, `BOSS_PHASE` |
| `curse/CurseManager.java` (`manifest`/`spawn*`) | — | — | `play(level, pos, CURSE_APPLY)` / `CURSE_ABSORB` on death, `CURSE_CLEANSE` on cure |
| `curse/CursedGear.java:87` | `SOUL_ESCAPE` | soul escape | `play(level, target, CURSE_ABSORB)` |
| `flight/FlightManager.java:234` | `WARDEN_SONIC_BOOM` boom | sonic boom | `play(level, player, FLIGHT_BOOM)` (+ `FLIGHT_LOOP` while flying, `DASH` on burst) |
| `world/dungeons/DungeonManager.java` | dungeon generated / boss gate | — | `DUNGEON_AMBIENT` (ambient loop), `DUNGEON_BOSS_GATE`, `DUNGEON_TRAP`, `DUNGEON_CHEST_UNLOCK` |
| `world/dungeons/DungeonLoot.java` | chest open | — | `play(level, pos, DUNGEON_CHEST_UNLOCK)` |
| GUI: `client/RpgScreen`, `PowersScreen`, `BankScreen`, `GovScreen`, `DevMenuScreen`, `VillagerDialogueScreen`, `expansion2/powers/PowersScreen2` | open / button click / close / tab switch | — | `playToPlayer(...)` is server-only; for client screens use `Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.UI_CLICK, 1.0f))` in `init()` / `onClose()` / button callbacks (`UI_OPEN`, `UI_CLICK`, `UI_CLOSE`, `UI_TAB`, `UI_ERROR`) |
| `politics/ElectionManager.java` | election resolved | — | `playToPlayer(winner, ELECTION_WIN)` |
| `economy/*` (`BankManager`, `CurrencyCommands`, `MarketManager`) | coin/trade gains | — | `COIN_GAIN`, `COIN_LARGE`, `TRADE_COMPLETE` |
| `politics/DataManager` / leveling | level / rank up | — | `LEVEL_UP`, `RANK_UP` |
| `expansion2/quests/Expansion2QuestManager.java` | quest complete | — | `playToPlayer(player, QUEST_COMPLETE)` |
| `combat/StatManager.java` | shield/absorption break | — | `SHIELD_BREAK`, `PARRY` |

> Client GUI screens run client-side and have no `ServerPlayer`, so they must play UI sounds
> through `Minecraft.getInstance().getSoundManager()` (UI `SimpleSoundInstance`) rather than
> `playToPlayer`. The custom event ids are valid on both sides because they are registered in
> the common initializer.

---

## Dropping real `.ogg` assets later

1. Encode mono Ogg Vorbis files and place them under
   `src/main/resources/assets/politicalserver/sounds/` (suggested subfolders: `power/`,
   `ui/`, `mob/`, `dungeon/`, `progress/`, `flight/`, `curse/`).
2. In `sounds.json`, change the relevant entry's `sounds` array from
   `{ "name": "minecraft:...", "type": "event" }` to `{ "name": "politicalserver:power/cast_fire" }`
   (path under `sounds/`, no extension, no leading `sounds/`).
3. No Java changes required — `ModSounds` already owns the event ids.

See also `assets/politicalserver/sounds/README.txt`.
