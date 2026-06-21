# Expansion 3 — Civics (Economy & Political Systems)

Deepens the namesake **economy & political** systems. Everything is additive,
mixin-free, and persists through the existing `DataManager` / `PoliticsData`
save system. New gameplay lives in the `com.political.civics` package; a few
existing `economy` / `politics` classes were extended (never rewritten).

---

## 1. Integration hooks (the integration agent must wire these)

All new wiring is funnelled through **`com.political.civics.CivicsBootstrap`**.
Add three calls to `RpgPoliticsMod.onInitialize()`:

| Location in `RpgPoliticsMod` | Call to add |
|---|---|
| Inside `CommandRegistrationCallback.EVENT` lambda | `CivicsBootstrap.registerCommands(dispatcher);` |
| Inside `ServerTickEvents.END_SERVER_TICK` lambda | `CivicsBootstrap.tick(server);` |
| Inside the existing `ServerLivingEntityEvents.AFTER_DEATH` handler (where `killer` is the responsible `ServerPlayer`) | `CivicsBootstrap.onEntityKilled(entity, killer);` |

No new load/save wiring is needed — the new fields are plain members of
`PoliticsData` and are serialized by the existing Gson save.

The `tools/lang-fragments/civics.json` fragment must be merged into the built
`lang/en_us.json` by the usual lang-merge step.

---

## 2. New data fields (added to `PoliticsData`, all additive)

```
// Jobs
Map<String,String>  civicJob            // uuid -> Job.name()
Map<String,Integer> civicJobXp          // uuid -> lifetime job xp
Map<String,Long>    civicJobLastWage    // uuid -> last daily wage time
Map<String,Long>    civicJobLastWork    // uuid -> last /job work time

// Taxation tiers
boolean taxTieredEnabled                // progressive brackets vs flat

// Civic offices + campaigns
Map<String,String>  civicOffices        // office id -> holder uuid
boolean officeElectionActive
String  officeElectionOffice
long    officeElectionEnd
List<String>        officeCandidates
Map<String,Integer> officeVotes
List<String>        officeVoted
Map<String,String>  manifestos          // uuid -> manifesto text
Map<String,Integer> campaignFunds       // uuid -> coins pledged this race

// Laws / decrees
List<String>        activeLaws          // CivicLaw.name()
Map<String,Long>    lawEnactedAt
long                lastWelfareTime

// Justice
Map<String,Integer> criminalRecord      // uuid -> offenses
Map<String,Integer> fines               // uuid -> outstanding fine coins
Map<String,Integer> wanted              // uuid -> bounty reward coins
Map<String,Integer> bail                // uuid -> bail coins

// Treasury sovereign wealth fund + public works
long    treasuryFund                    // principal invested
double  treasuryFundUnits               // synthetic index units
double  treasuryFundIndex               // live index price
long    lastFundTick
Map<String,Integer> publicWorks         // project id -> coins invested
List<String>        completedWorks      // completed project ids

// Factions / parties
Map<String, com.political.civics.Faction> factions
Map<String,String>  factionOf           // uuid -> faction id
```

---

## 3. Economy features

### Jobs & daily income (`Job`, `JobManager`)
- 8 professions (Miner, Farmer, Hunter, Guard, Merchant, Banker, Builder, Diplomat),
  each with a base daily wage and flavour.
- Daily wage is paid to online workers from the **treasury** (minted only if the
  treasury is dry, kept small to avoid inflation). Wage scales with job level.
- `/job work` is an active payout on a 5-minute cooldown that also builds job XP
  (level = `floor(sqrt(xp/200))`, capped 25).
- Hunters gain bonus XP from bounty kills; Guards gain bonus XP from claiming a
  wanted bounty (wired via the death hook).

### Progressive taxation tiers (`TaxManager`, extended)
- `/tax tiers enable|disable` switches the daily collection between the flat rate
  and progressive brackets: `<1,000` exempt, `1,000–9,999` base rate,
  `10,000–49,999` base+5%, `50,000+` base+10%.
- The **Tax Holiday** law suspends collection entirely.

### Auction house polish (`AuctionManager`, extended)
- Listing fee of 2% (min 5) flows to the treasury, **waived under Free Market**.
- `/auction cancel <id>` lets sellers reclaim a listing.
- Listings expire after 1 hour and are returned to online sellers.

### Markets price-balancing (`MarketManager`, extended)
- A trading **spread** (2%, halved to 1% under Free Market) is applied on buy/sell
  and routed to the treasury, giving the state a passive economic income.

### Sovereign wealth fund + public works (`TreasuryFund`, `PublicProject`)
- The **Treasurer** can `/fund invest` / `/fund divest` treasury coins into a
  synthetic index that random-walks (slight upward bias) — the state can grow or
  lose reserves.
- The **Mayor** can `/publicworks fund <project> <coins>` to build 6 projects
  (Roads, Granary, Aqueduct, Hospital, University, Walls). Each completed project
  grants a permanent server-wide buff to enrolled citizens.

---

## 4. Political features

### Multi-office elections with campaigns (`CivicOffice`, `OfficeManager`)
- Three municipal offices: **Mayor**, **Judge**, **Treasurer** (distinct from the
  national Chair/Vice/Judge roles).
- Flow: Chair/OP opens a race (`/office start <office>`), candidates self-nominate
  with a **manifesto** (`/office stand <manifesto>`), pledge **campaign funds**
  (`/office fund <coins>` → boosts effective vote, capped at +5, coins go to the
  treasury), and players vote (`/office vote <player>`). Winner takes the office on
  a 10-minute timer; their faction gains influence.

### Laws / decrees (`CivicLaw`, `LawManager`)
- 7 enactable laws, each with a treasury cost and a live effect: Tax Holiday,
  Free Market Act, Economic Stimulus, Public Healthcare, Martial Law, Welfare
  Programme, Conscription. Effects are applied each tick (status effects, treasury
  upkeep, welfare stipends) and read by the economy systems.

### Justice flow (`JusticeManager`)
- **Fines** (`/fine impose`, `/fine pay`) and **criminal records**.
- A **wanted board** (`/wanted post|clear|list`) — a wanted player's death pays the
  killer the bounty (death hook), feeding bounty payouts into the justice loop.
- **Bail** (`/bail set`, `/bail pay`) lets prisoners buy early release; bail and
  fines flow to the treasury. Integrates with the existing `PrisonManager`.

### Citizenship & public works funding
- Public-works buffs only apply to enrolled citizens (existing `citizenship`),
  giving citizenship tangible perks. Welfare law stipends target the poorest
  online citizens.

---

## 5. Diplomacy — factions / parties (`Faction`, `FactionIdeology`, `FactionManager`)
- Players can `/faction found <ideology> <tag> <name>` (2,000 coins), recruit
  (`/faction join`), `/faction donate` to a party war-chest, and set a `/faction motto`.
- 7 ideologies, each conferring a standing status-effect perk to online members.
- Winning a civic office grants the winner's faction **influence**.

---

## 6. Commands added

| Command | Who | Purpose |
|---|---|---|
| `/civics` | all | Personal civics overview |
| `/job`, `/jobs`, `/job list\|join\|quit\|work\|status` | all | Profession & income |
| `/tax tiers enable\|disable` | OP | Toggle progressive tax (extends `/tax`) |
| `/office status\|stand\|vote\|fund` | all | Campaign in office races |
| `/office start\|end` | OP/Chair | Run office elections |
| `/office set <office> <player>` | OP | Appoint an office holder |
| `/law list\|active` | all | Inspect laws |
| `/law enact\|repeal <law>` | OP/Chair | Govern by decree |
| `/fine status\|pay` | all | Manage your fines |
| `/fine impose <player> <amount>` | OP/Judge | Levy a fine |
| `/wanted list` | all | View the bounty board |
| `/wanted post\|clear <player> [reward]` | OP/Judge | Manage bounties |
| `/bail pay` | all | Buy release |
| `/bail set <player> <amount>` | OP/Judge | Set bail |
| `/faction list\|info\|found\|join\|leave\|donate\|motto` | all | Parties |
| `/fund status\|invest\|divest` | all / Treasurer+OP | Sovereign wealth fund |
| `/publicworks status\|list\|fund` | all / Mayor+OP | Civic projects |

No existing command was removed or renamed — `/tax`, `/auction`, `/crypto`,
`/stocks`, `/bank`, `/treasury`, `/vote`, `/gov`, `/politics`, `/perk`, `/impeach`,
`/role`, `/imprison`, etc. all keep their original behaviour. `/tax` and
`/auction` only gained new sub-commands.

---

## 7. GUI changes
- `GovMenu.handleAction` gained additive, server-side action cases (`payfine`,
  `paybail`, `jobwork`, `officevote`, `enactlaw`) routed over the **existing**
  `GovActionC2S` channel. The `GovMenuS2C` payload was **not** changed, so the
  current `GovScreen` client keeps working unchanged; a future client update can
  add buttons that send these action strings (an optional follow-up, not required).
