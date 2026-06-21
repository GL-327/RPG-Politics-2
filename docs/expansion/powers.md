# Powers & Viltrumite Flight expansion

Owner package(s): `com.political.power.**` (existing) and **new** `com.political.flight.**`.
Everything here is mixin-free (Fabric events / registries / networking + vanilla mechanics) for MC 26.2.

---

## 1. Viltrumite flight findings (`viltrumitecore-1.1.0.jar`)

Extracted and inspected the jar:

- **Loader / MC version:** Fabric, `minecraft": "1.20.1"`, `fabricloader >=0.19.2`, `fabric-api: *`. It is **not** 26.2 and **uses mixins** (`viltrumitecore.mixins.json` + client mixins), so it cannot be used directly.
- **Crucial detail:** `viltrumitecore` does **not** contain the flight itself. Its `fabric.mod.json` declares a hard dependency `"viltrumiteflight": ">=1.3.0"` — flight lives in a **separate `viltrumiteflight` mod that is not present in Downloads**. `viltrumitecore` only *hooks* it via `mixin/CoreFlightHookMixin`, which I disassembled:

  ```
  // CoreFlightHookMixin.overrideFlightCollision(CallbackInfo ci)
  if (player instanceof ViltrumiteFlightPlayer vfp) {
      if (vfp.getFlightThrottle() >= 0.6f) ci.cancel();   // ignore flight collision when boosting
  }
  ```

  So the authoritative flight API is `viltrumiteflight.util.ViltrumiteFlightPlayer.getFlightThrottle() : float`, a **0..1 throttle**, and once it reaches **0.6** the player's flight collision is cancelled (they barrel through). `viltrumitecore` itself only adds the *other* Invincible mechanics (dash, punch, grab, super-speed, block, race screen, capes) — confirmed from its class list and the `ViltrumiteCorePlayer` interface (`startDash`, `getPunchStrength`, `isSuperSpeed`, etc.). Those are out of scope for flight.

### How the flight was reimplemented (throttle-based momentum flight)

Reconstructed faithfully from the throttle/boost-threshold design (the well-known "Invincible / Viltrumite" momentum flight):

- **Activation:** double-tap jump (vanilla, because we grant `abilities.mayfly`) **or** the new `G` key (`key.politicalserver.flight_toggle`).
- **Throttle:** holding the forward key while flying ramps a 0..1 throttle (`+0.03`/tick up, bleeds `-0.06`/tick); ease-in (`throttle²`) curve.
- **Speed / acceleration:** velocity is lerped (`0.22`) toward `look * speed`, where `speed = 0.35 .. 2.4` blocks/tick — far faster than vanilla creative flight, with real momentum/coasting.
- **Vertical control:** at speed you fly where you look (pitch = climb/dive); while hovering (not pressing forward), `jump`/`sneak` give fine vertical control.
- **Boost threshold `0.6`** (mirrors the source): from here particles intensify and the server applies a **ram knockback** (anything in the flight path is knocked aside and damaged `4 + 6*throttle`), plus fall-damage immunity.
- **Sound/particles:** `ENDER_DRAGON_FLAP` on takeoff, `WARDEN_SONIC_BOOM` (quiet, high-pitch) when crossing boost, `CLOUD` trail + `END_ROD` speed-lines scaling with throttle.
- **Energy/stamina:** the base Viltrumite flight has **none** (viltrumites fly indefinitely), so this is stamina-free. Timed grants (from powers) expire on their own; passive grants (knowing a flight power) last as long as you keep the power.

**Architecture (mixin-free):** the server grants vanilla `mayfly`; the client (position-authoritative for its own player) does the acceleration/steering and streams its throttle to the server via `FlightInputC2S` for the shared authoritative effects.

New classes:
- `com.political.flight.FlightManager` (`register()`) — common: payload registration + receiver, server tick (mayfly maintenance, timed expiry, passive-power detection, fall immunity, ram).
- `com.political.flight.FlightClient` (`registerClient()`) — client: keybind + `ClientTickEvents` momentum driver + FX.
- `com.political.flight.FlightInputC2S` — C2S `{ float throttle, boolean flying }`.

---

## 2. Unified flight (everything except vanilla creative)

`FlightManager` is now the **single authority** for non-creative flight. It is reason-counted (`enable/disable(player, reason)`), so multiple sources compose and `mayfly` is only revoked when the last reason clears. Creative/spectator are detected and **left to vanilla** (server: `isCreative()/isSpectator()`; client: `abilities.instabuild`).

Rerouted every existing raw-`mayfly` grant:

| Source | Before | After |
| --- | --- | --- |
| `PowerManager.grantFlight` (powers `FLIGHT`, `STAR_POWER`, `ICARUS_DIVE`) | set `abilities.mayfly` directly + local `FLIGHT_UNTIL` map | `FlightManager.enableTimed(p, ms)` |
| `PowerManager.tick` timed-flight expiry | toggled `mayfly`/`flying` off | removed — `FlightManager` owns expiry |
| `AbilityEngine.manageFlight` (armour `Ability.FLIGHT` "Creative Flight") | set `abilities.mayfly` + `GRANTED_FLIGHT` set | `FlightManager.setArmorFlight(p, shouldFly)` |
| `PowerManager.onPlayerRemoved` / `AbilityEngine.onPlayerRemoved` | local cleanup | also call `FlightManager.onPlayerRemoved(uuid)` |

No other `mayfly` grants exist in `src/main` (checked: `STORM_PLATE` uses the `STORM_SURGE` *Speed* active ability, not flight; legacy `legacy-yarn-reference/**` is not compiled).

---

## 3. Compound V merge

- `FlightManager.serverTick` passively grants Viltrumite flight to any player who **knows** a flight power: `flight`, `star_power`, or the new `icarus_dive` (reason `power`). So a Compound V user who rolls/learns Flight can fly with this system permanently (no need to re-activate), and `ICARUS_DIVE` both grants flight and rockets them forward.
- Compound V powers are rolled from `Power.ofOrigin(COMPOUND_V)` in `Serums`, so the new powers below are automatically part of the Compound V pool.

---

## 4. New powers (19 total) — all wired into `Power` enum + `PowerManager.cast()`

Activated via the existing power key / `/power use` / Powers GUI. Costs are Mana (Compound V) or Cursed Energy (techniques); cooldown shown in seconds.

### Compound V (10)
| id | name | cost | cd(s) | effect |
| --- | --- | --- | --- | --- |
| `icarus_dive` | Icarus Dive | 35 | 8 | grants Viltrumite flight (30s) + forward missile launch |
| `heat_vision_overload` | Heat Vision Overload | 48 | 5.5 | big fiery explosive beam |
| `ground_pound` | Ground Pound | 32 | 6 | AoE knock-away + slowness + damage |
| `shock_pulse` | Shock Pulse | 28 | 4.5 | lightning on target + AoE shock |
| `titan_grip` | Titan Grip | 35 | 5 | single-target heavy hit + slam down |
| `afterimage` | Afterimage | 26 | 7 | Speed IV + Jump + brief invis |
| `regen_surge` | Regen Surge | 34 | 10 | regen/absorption/fire-res + cleanse + heal |
| `wind_blast` | Wind Blast | 24 | 4 | cone knockback |
| `meteor_drop` | Meteor Drop | 50 | 9 | targeted AoE crater (fire + launch) |
| `seismic_slam` | Seismic Slam | 30 | 6 | AoE launch skyward |

### Cursed techniques (9) — grade-gated in `PowerCommands.TECHNIQUE_GRADE`
| id | name | cost | cd(s) | grade | effect |
| --- | --- | --- | --- | --- | --- |
| `maximum_meteor` | Maximum: Meteor | 95 | 21 | 5 | huge targeted fire AoE |
| `fire_arrow_fuga` | Fire Arrow: Fuga | 42 | 6 | 3 | piercing fire beam |
| `coffin_iron_mountain` | Coffin of the Iron Mountain | 60 | 13 | 4 | defensive aquatic domain + AoE |
| `self_embodiment` | Self-Embodiment of Perfection | 100 | 30 | 5 | Mahito domain: heavy AoE wither/weaken |
| `soul_transfiguration` | Soul Transfiguration | 45 | 8 | 3 | single-target wither/weaken/slow |
| `chimera_shadow_garden` | Chimera Shadow Garden | 90 | 25 | 5 | Megumi domain: summon 6 shikigami + self-buff |
| `nue_strike` | Nue | 35 | 6.5 | 2 | lightning dive-bomb on target/aim |
| `gravity_well` | Gravity Well | 70 | 15 | 4 | pull-in + crush AoE |
| `malevolent_shrine` | Malevolent Shrine | 110 | 35 | 5 | Sukuna domain: cleave cone + AoE |

Design driven by the reference packs as requested: JJK pack (domains/cursed techniques → Maximum: Meteor, Fuga, Coffin of the Iron Mountain, Self-Embodiment, Chimera Shadow Garden, Nue, Malevolent Shrine), and viltrumitecore (hero/Invincible powers → Icarus Dive, Heat Vision Overload, Ground Pound, Titan Grip).

No new **items** were added (powers are abilities, not items), so **no new item/model JSON and no new textures are required**, and `tools/gen-powers.js` was not needed.

---

## 5. What the integration agent must wire

1. **Common init** (next to the existing `PowerManager.register();`, e.g. in `RpgPoliticsMod`'s main init):
   ```java
   com.political.flight.FlightManager.register();
   ```
   This self-registers the `FlightInputC2S` C2S type + receiver and its own `ServerTickEvents.END_SERVER_TICK` handler — **no edits to `ModNetworking` are needed.**

2. **Client init** (in `PoliticalClient.onInitializeClient()`):
   ```java
   com.political.flight.FlightClient.registerClient();
   ```
   Registers the `G` keybind (`key.politicalserver.flight_toggle`) and the client tick momentum driver.

3. **Lang:** merge `tools/lang-fragments/powers.json` into `assets/politicalserver/lang/en_us.json`. It includes the keybind label `key.politicalserver.flight_toggle` (**required** so the controls screen isn't blank) plus optional `power.politicalserver.<id>(.desc)` strings.

4. **Cleanup is already wired** — `PowerManager.onPlayerRemoved` and `AbilityEngine.onPlayerRemoved` both delegate to `FlightManager.onPlayerRemoved`, which are already called on DISCONNECT. Nothing else to add.

5. **Payloads:** one new C2S `politicalserver:flight_input` (registered by `FlightManager.register()`); no S2C (the client learns flight availability from the synced vanilla `mayfly` ability).
