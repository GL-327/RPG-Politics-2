package com.political.guide;

import com.political.items.Rarity;
import com.political.politics.DataManager;
import com.political.power.Power;
import com.political.expansion2.powers.Power2;
import com.political.world.dungeons.DungeonType;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the encyclopedia's chapters. Content is pulled live from the mod's own data
 * ({@link Power}, {@link Power2}, {@link DungeonType}, {@link Rarity}, grade ladder) so the
 * guide stays accurate as that content evolves, mixed with curated player-facing prose.
 *
 * <p>Pure data — no client/render classes — so it is cheap to construct and easy to test.</p>
 */
public final class GuideContent {

    /** Paragraph styles the screen knows how to render. */
    public enum Style { TITLE, HEADER, BODY, BULLET, NOTE, SPACER }

    /** A single styled paragraph of raw (un-wrapped) text. */
    public record Block(Style style, String text) {}

    /** A named chapter: an ordered list of blocks. */
    public record Chapter(String title, List<Block> blocks) {}

    private GuideContent() {}

    private static final List<Chapter> CHAPTERS = build();

    public static List<Chapter> chapters() {
        return CHAPTERS;
    }

    public static int count() {
        return CHAPTERS.size();
    }

    // ------------------------------------------------------------------

    private static List<Chapter> build() {
        List<Chapter> out = new ArrayList<>();
        out.add(gettingStarted());
        out.add(cursedEnergyAndGrades());
        out.add(powersAndTechniques());
        out.add(compoundVAndFlight());
        out.add(bindingVowsAndSkyblock());
        out.add(weaponsAndRarities());
        out.add(armorAndSetBonuses());
        out.add(accessories());
        out.add(dungeons());
        out.add(bestiary());
        out.add(economy());
        out.add(politics());
        out.add(commands());
        return out;
    }

    // --- chapter builder helpers ---

    private static final class B {
        final List<Block> list = new ArrayList<>();
        B title(String t) { list.add(new Block(Style.TITLE, t)); return this; }
        B head(String t) { list.add(new Block(Style.HEADER, t)); return this; }
        B body(String t) { list.add(new Block(Style.BODY, t)); return this; }
        B bullet(String t) { list.add(new Block(Style.BULLET, t)); return this; }
        B note(String t) { list.add(new Block(Style.NOTE, t)); return this; }
        B gap() { list.add(new Block(Style.SPACER, "")); return this; }
    }

    private static Chapter ch(String title, B b) {
        return new Chapter(title, b.list);
    }

    // ------------------------------------------------------------------
    // 1. Getting Started
    // ------------------------------------------------------------------

    private static Chapter gettingStarted() {
        B b = new B();
        b.title("Welcome, citizen.");
        b.body("This realm fuses an action RPG with a living, elected government. You will fight cursed "
                + "spirits, awaken superhuman powers, raid dungeons, trade on a real economy, and vote for "
                + "(or become) the people who rule it all.");
        b.gap();
        b.head("Your first hour");
        b.bullet("Open your stats & abilities HUD \u2014 health, Mana and Cursed Energy bars sit above the hotbar.");
        b.bullet("Press K (or run /powers) to open the Powers & Serums menu.");
        b.bullet("Drink a Compound V serum or run /cursed awaken to gain your first ability.");
        b.bullet("Earn coins from mobs and quests, then run /bank to store them safely.");
        b.bullet("Run /dungeon locate to find the nearest dungeon and start your first raid.");
        b.gap();
        b.head("Two paths of power");
        b.body("Compound V serums grant 'hero' powers (lasers, flight, super strength). Cursed Energy lets "
                + "you channel Jujutsu techniques and Domain Expansions. You can pursue both \u2014 they share "
                + "the same energy pool.");
        b.gap();
        b.note("Tip: nearly every system has both a chat command and a clean in-game menu. Use whichever you prefer.");
        return ch("Getting Started", b);
    }

    // ------------------------------------------------------------------
    // 2. Cursed Energy & Grades
    // ------------------------------------------------------------------

    private static Chapter cursedEnergyAndGrades() {
        B b = new B();
        b.title("Cursed Energy & Sorcerer Grades");
        b.body("Cursed Energy (CE) is the fuel for every Jujutsu technique. Awaken it with /cursed awaken, "
                + "then learn techniques you have the grade for. Exorcising curses raises your grade.");
        b.gap();
        b.head("The grade ladder");
        // Pulled live from DataManager.gradeLabel + the documented promotion thresholds.
        int[] thresholds = {3, 10, 25, 60, 120};
        for (int g = 1; g <= 5; g++) {
            String req = "exorcise " + thresholds[g - 1] + " curses";
            b.bullet(DataManager.gradeLabel(g) + "  \u2014  " + req);
        }
        b.note("Grade 4 is the weakest awakened rank; Special Grade is the pinnacle. Higher grades unlock "
                + "stronger techniques and deeper CE reserves.");
        b.gap();
        b.head("Growing your reserves");
        b.bullet("Some Compound V powers permanently raise your maximum CE (e.g. Adaptive Biology, +80).");
        b.bullet("Passive techniques like CE Efficiency lower the cost of your techniques over time.");
        b.bullet("Six Eyes instantly refills your CE when channeled.");
        b.gap();
        b.head("Aptitude");
        b.body("Run /cursed info to see your cursed aptitude. Some bodies channel no CE at all \u2014 but cursed "
                + "tools still answer to them, and you can forge curses into weapons with /cursed forge.");
        return ch("Cursed Energy & Grades", b);
    }

    // ------------------------------------------------------------------
    // 3. Powers & Techniques
    // ------------------------------------------------------------------

    private static Chapter powersAndTechniques() {
        B b = new B();
        b.title("Cursed Techniques");
        b.body("Learn techniques with /cursed learn <id> once you meet the grade. Select one and unleash it "
                + "with the power key or /power use. Cost is in Cursed Energy; cooldowns are shown in seconds.");
        b.gap();
        b.head("Core techniques (" + Power.ofOrigin(Power.Origin.CURSED_TECHNIQUE).size() + ")");
        for (Power p : Power.ofOrigin(Power.Origin.CURSED_TECHNIQUE)) {
            b.bullet(p.displayName + "  \u2014  " + p.energyCost + " CE, " + (p.cooldownTicks / 20) + "s");
        }
        b.gap();
        b.head("Expansion II techniques");
        b.body("A second wave of techniques, domains, ultimates and passives. Domains and ultimates are the "
                + "most expensive abilities in the game. Counts by category:");
        for (Power2.Category cat : Power2.Category.values()) {
            int n = Power2.ofCategory(cat).size();
            b.bullet(cat.label + ": " + n + " abilities");
        }
        b.gap();
        b.head("Domain Expansions");
        for (Power2 p : Power2.ofCategory(Power2.Category.DOMAIN)) {
            b.bullet(p.displayName + "  \u2014  " + p.energyCost + " CE (min grade " + p.minGrade + ")");
        }
        b.gap();
        b.head("Ultimates");
        for (Power2 p : Power2.ofCategory(Power2.Category.ULTIMATE)) {
            b.bullet(p.displayName + "  \u2014  " + p.energyCost + " CE");
        }
        b.gap();
        b.note("See the Commands chapter for the full /power and /cursed command list.");
        return ch("Powers & Techniques", b);
    }

    // ------------------------------------------------------------------
    // 4. Compound V & Flight
    // ------------------------------------------------------------------

    private static Chapter compoundVAndFlight() {
        B b = new B();
        b.title("Compound V & Flight");
        b.body("Compound V rewrites your biology and grants hero powers. Drink a serum (right-click) or use "
                + "/v. Powers cost the same shared energy pool and fire with the power key or /power use.");
        b.gap();
        b.head("Serums");
        b.bullet("Compound V \u2014 the full serum; grants a hero power.");
        b.bullet("Temp V \u2014 a temporary, unstable surge of power.");
        b.bullet("V1 \u2014 an early, weaker formula.");
        b.bullet("Anti-V \u2014 strips powers away.");
        b.gap();
        b.head("Hero powers (" + Power.ofOrigin(Power.Origin.COMPOUND_V).size() + ")");
        for (Power p : Power.ofOrigin(Power.Origin.COMPOUND_V)) {
            b.bullet(p.displayName + "  \u2014  " + p.energyCost + " energy, " + (p.cooldownTicks / 20) + "s");
        }
        b.gap();
        b.head("Viltrumite powers");
        b.body("Expansion II adds Viltrumite and named-hero powers \u2014 raw flying-brick combat.");
        for (Power2 p : Power2.ofCategory(Power2.Category.VILTRUMITE)) {
            b.bullet(p.displayName + "  \u2014  " + p.energyCost + " energy");
        }
        b.gap();
        b.head("Flight");
        b.body("Powers such as Flight, Star Power, Icarus Dive and Supersonic grant Viltrumite flight. Hold "
                + "jump to ascend and steer with your look direction; your throttle ramps up the longer you fly. "
                + "Flight Mastery (passive) makes that ramp faster.");
        return ch("Compound V & Flight", b);
    }

    // ------------------------------------------------------------------
    // 4b. Binding Vows & Skyblock Stats
    // ------------------------------------------------------------------

    private static Chapter bindingVowsAndSkyblock() {
        B b = new B();
        b.title("Binding Vows & Skyblock Stats");
        b.body("Every item and every sorcerer now runs on a unified Skyblock stat sheet. "
                + "Defense reduces incoming damage through the same EHP curve as Hypixel Skyblock; "
                + "Strength scales melee both additively and multiplicatively; ferocity adds full-damage extra strikes.");
        b.gap();
        b.head("Binding Vows (kraku)");
        b.bullet("Revealed Technique \u2014 lay your technique bare for massive output at higher CE cost.");
        b.bullet("Overtime \u2014 cheaper, harder hits paid for with a steady vitality toll.");
        b.bullet("Throughput Vow \u2014 forsake defence for raw force.");
        b.bullet("Sacrificial Pact \u2014 Special Grade only. Overwhelming power, grievous HP cost.");
        b.body("Toggle vows with /cursed vows and /cursed vow <id>. They stack \u2014 swear carefully.");
        b.gap();
        b.head("Live JJK auras");
        b.bullet("ZONE / PRIMED \u2014 Black Flash distortion window (melee crit surge).");
        b.bullet("RCT \u2014 Reverse Cursed Technique healing toggle (/cursed rct).");
        b.bullet("FLOW \u2014 cursed-energy reinforcement (/cursed flow).");
        b.bullet("SD / FB \u2014 Simple Domain and Falling Blossom ward against sure-hit domains.");
        b.gap();
        b.head("Item prefixes");
        b.bullet("Cursed \u2014 grants a cursed-technique ability (right-click channels CE).");
        b.bullet("Unique \u2014 grants a normal active ability.");
        b.bullet("Plain items carry stats only \u2014 not every item has an ability.");
        b.gap();
        b.head("Staff editor");
        b.bullet("/sbs or /skyblockstats \u2014 open the Skyblock stat editor for the held item.");
        b.note("Gamemasters only. Edit rarity, every stat, prefix, and bound ability in one sleek panel.");
        return ch("Binding Vows & Stats", b);
    }

    // ------------------------------------------------------------------
    // 5. Weapons & Rarities
    // ------------------------------------------------------------------

    private static Chapter weaponsAndRarities() {
        B b = new B();
        b.title("Weapons & Rarities");
        b.body("Every item \u2014 vanilla or custom \u2014 resolves to a rarity that colours its name and scales "
                + "its stats, Hypixel-Skyblock style. Higher rarity means a bigger stat multiplier.");
        b.gap();
        b.head("The rarity ladder");
        for (Rarity r : Rarity.values()) {
            b.bullet(r.display + "  \u2014  x" + String.format("%.2f", r.mult) + " base stats");
        }
        b.gap();
        b.head("Weapon families");
        b.bullet("Melee I & II \u2014 dozens of swords, axes, scythes, whips and mauls with active abilities.");
        b.bullet("Ranged I & II \u2014 bows, guns and arcane casters with charged shots and special casts.");
        b.bullet("Cursed Arsenal \u2014 weapons that hold a Cursed Grade; forge your own with /cursed forge.");
        b.bullet("Relics & arcane orbs \u2014 utility items that trigger powers or restore resources.");
        b.gap();
        b.head("Reading a weapon");
        b.body("Hover any RPG weapon to see its rarity, damage, stat bonuses and the active ability bound to "
                + "it. Many weapons trigger their ability on right-click; watch the cooldown in the tooltip.");
        b.gap();
        b.note("Reforge Stones (dungeon loot) can push an item up the rarity ladder.");
        return ch("Weapons & Rarities", b);
    }

    // ------------------------------------------------------------------
    // 6. Armor & Set Bonuses
    // ------------------------------------------------------------------

    private static Chapter armorAndSetBonuses() {
        B b = new B();
        b.title("Armor & Set Bonuses");
        b.body("Armor pieces share the same rarity ladder as weapons and grant defensive and offensive stats. "
                + "Wearing a full matching set unlocks a powerful set bonus.");
        b.gap();
        b.head("How sets work");
        b.bullet("Each set has helmet, chestplate, leggings and boots.");
        b.bullet("Wear all four pieces of the same set to activate its set bonus.");
        b.bullet("Partial sets still give per-piece stats, but no bonus.");
        b.bullet("The bonus is listed in green on each piece's tooltip when the set is complete.");
        b.gap();
        b.head("Set bonus styles");
        b.bullet("Stat sets \u2014 flat boosts to Defense, Strength, Crit, Ferocity or Speed.");
        b.bullet("Resource sets \u2014 raise your max Mana or Cursed Energy and speed regeneration.");
        b.bullet("Trigger sets \u2014 unleash an effect on hit, on kill, or when you take damage.");
        b.gap();
        b.head("Armor I & II");
        b.body("Armor I covers the classic RPG sets; Armor II (Expansion II) adds higher-tier sets themed "
                + "around the dungeons and bosses you will face. Mix toward the set that fits your build, then "
                + "complete it for the payoff.");
        return ch("Armor & Set Bonuses", b);
    }

    // ------------------------------------------------------------------
    // 7. Accessories
    // ------------------------------------------------------------------

    private static Chapter accessories() {
        B b = new B();
        b.title("Accessories");
        b.body("Accessories are passive trinkets that buff you just by being carried. They stack with armor "
                + "and weapons to round out your build.");
        b.gap();
        b.head("Kinds of accessory");
        b.bullet("Stat charms & talismans \u2014 steady boosts to a core stat.");
        b.bullet("Mana & arcane focuses \u2014 deepen your Mana or Cursed Energy pool.");
        b.bullet("Warding talismans \u2014 reduce incoming damage or resist debuffs.");
        b.bullet("Relics (e.g. the Godslayer Relic) \u2014 rare, build-defining trinkets from elite dungeons.");
        b.gap();
        b.head("Consumables");
        b.body("Alongside accessories you will find potions and consumables \u2014 minor and major Mana "
                + "potions, cure-alls and buff foods \u2014 for use mid-fight.");
        b.gap();
        b.note("Accessories also share the rarity ladder, so an Epic charm outperforms its Common cousin.");
        return ch("Accessories", b);
    }

    // ------------------------------------------------------------------
    // 8. Dungeons
    // ------------------------------------------------------------------

    private static Chapter dungeons() {
        B b = new B();
        b.title("Dungeons");
        b.body("Dungeons generate across the world, each with its own theme, tier, mobs and boss. Find them "
                + "with /dungeon locate and /dungeon list, then fight inward to the boss and its loot.");
        b.gap();
        b.head("Archetypes (" + DungeonType.values().length + ")");
        for (DungeonType t : DungeonType.values()) {
            String tags = (t.cursed ? "cursed " : "") + (t.underground ? "underground " : "") + (t.flooded ? "flooded " : "");
            b.bullet(t.display + "  [" + t.tier.name().toLowerCase() + "]  " + tags.trim());
        }
        b.gap();
        b.head("Bosses & loot");
        for (DungeonType t : DungeonType.values()) {
            b.bullet(t.display + ": boss " + pretty(t.mobBoss) + ", loot incl. " + pretty(t.lootC));
        }
        b.gap();
        b.head("Raiding tips");
        b.bullet("Match your gear tier to the dungeon's tier before going deep.");
        b.bullet("Elite mobs guard the path; the boss waits at the heart.");
        b.bullet("Use the Structure Compass to track the structure as you explore.");
        return ch("Dungeons", b);
    }

    // ------------------------------------------------------------------
    // 9. Mobs & Spirits Bestiary
    // ------------------------------------------------------------------

    private static Chapter bestiary() {
        B b = new B();
        b.title("Mobs & Spirits Bestiary");
        b.body("The world is stalked by custom RPG mobs, cursed spirits and named bosses far deadlier than "
                + "vanilla creatures. Defeating them raises your grade, fills bounties and drops RPG loot.");
        b.gap();
        b.head("Cursed spirits");
        b.body("Cursed spirits spawn from negative energy and come in many species and grades. Exorcising "
                + "them is how sorcerers climb the grade ladder. Some are summonable bosses.");
        b.gap();
        b.head("RPG mobs");
        b.bullet("Common \u2014 outlaws, revenants, cultists, bone legionnaires and the like.");
        b.bullet("Elite \u2014 brutes, champions, heralds and wraiths that guard dungeons.");
        b.bullet("Bosses \u2014 named sovereigns, marshals and liches at the heart of each dungeon.");
        b.gap();
        b.head("Sample dungeon bosses");
        // Distinct boss list pulled from dungeon archetypes.
        List<String> seen = new ArrayList<>();
        for (DungeonType t : DungeonType.values()) {
            String boss = pretty(t.mobBoss);
            if (!seen.contains(boss)) {
                seen.add(boss);
                b.bullet(boss + "  (" + t.display + ")");
            }
        }
        b.gap();
        b.head("Bounties");
        b.body("Notorious targets carry bounties. Run /bounty to view active marks and claim rewards for "
                + "hunting them down.");
        return ch("Mobs & Spirits", b);
    }

    // ------------------------------------------------------------------
    // 10. Economy
    // ------------------------------------------------------------------

    private static Chapter economy() {
        B b = new B();
        b.title("Economy");
        b.body("Coins are the lifeblood of the realm \u2014 earned from mobs, quests and trade, spent on gear, "
                + "homes and taxes. A national bank, player market and auction house tie it all together.");
        b.gap();
        b.head("The Bank");
        b.bullet("Run /bank to open the bank menu and deposit, withdraw or check your balance.");
        b.bullet("Banked coins earn daily interest \u2014 your money grows while it is safe.");
        b.bullet("Your wallet is what you carry; the bank is what survives death.");
        b.gap();
        b.head("Trading");
        b.bullet("/shop \u2014 browse and buy from the server shop, or sell your held item.");
        b.bullet("/market \u2014 the player-driven market for listing and buying goods.");
        b.bullet("/auction \u2014 browse and bid on the auction house.");
        b.gap();
        b.head("Currencies & transfers");
        b.bullet("/coins \u2014 check your coin balance.");
        b.bullet("/pay <player> <amount> \u2014 send coins to another player.");
        b.bullet("/credits and /convert \u2014 a premium currency you can convert to and from coins.");
        b.bullet("/baltop \u2014 see the wealthiest players on the server.");
        b.gap();
        b.note("Taxes feed the treasury that the elected government spends \u2014 see the Politics chapter.");
        return ch("Economy", b);
    }

    // ------------------------------------------------------------------
    // 11. Politics
    // ------------------------------------------------------------------

    private static Chapter politics() {
        B b = new B();
        b.title("Politics");
        b.body("This server is run by an elected government. Players hold offices, levy taxes, pass judgment "
                + "and can be impeached. Open the governance menu with /gov.");
        b.gap();
        b.head("Offices");
        b.bullet("Chair \u2014 head of government; controls the treasury and chair perks.");
        b.bullet("Vice Chair \u2014 deputy; holds vice perks.");
        b.bullet("Judge \u2014 wields justice: imprison, pardon, exile and smite.");
        b.bullet("Dictator \u2014 a rare, sweeping authority outside the normal order.");
        b.gap();
        b.head("Elections");
        b.bullet("Elections run on a schedule; campaign and vote with /vote.");
        b.bullet("/politics shows the current state of the government.");
        b.bullet("/impeach lets citizens move against a sitting official.");
        b.gap();
        b.head("Taxes & treasury");
        b.bullet("/tax status \u2014 see the current tax rate and what you owe.");
        b.bullet("/tax pay (or the Pay Tax button) \u2014 settle your dues.");
        b.bullet("The treasury funds public works and perks the government can enable.");
        b.gap();
        b.head("Justice & home");
        b.bullet("Judges can imprison, pardon or exile lawbreakers.");
        b.bullet("/sethome, /home, /spawn and /checkpoint help you get around safely.");
        b.gap();
        b.head("Perks");
        b.body("Office-holders can activate perks that benefit themselves or the whole settlement. Run "
                + "/perk list to see what is available and /perk active for what is currently running.");
        return ch("Politics", b);
    }

    // ------------------------------------------------------------------
    // 12. Commands Reference
    // ------------------------------------------------------------------

    private static Chapter commands() {
        B b = new B();
        b.title("Commands Reference");
        b.note("Most everyday systems have a menu too, but these commands always work.");
        b.gap();
        b.head("Guide");
        b.bullet("/guide \u2014 open this field manual.");
        b.gap();
        b.head("Powers, Serums & Sorcery");
        b.bullet("/powers \u2014 open the Powers & Serums menu (also the K key).");
        b.bullet("/power list | known | select <id> | use | info <id>");
        b.bullet("/v compound | temp | v1 | anti \u2014 drink a serum.");
        b.bullet("/cursed awaken | info | learn <id> | forge | rct | flow | vows | vow <id>");
        b.bullet("/sbs or /skyblockstats \u2014 Skyblock stat editor (staff).");
        b.bullet("/power2 list | learn <id> \u2014 Expansion II abilities.");
        b.gap();
        b.head("Dungeons & Mobs");
        b.bullet("/dungeon list | locate | types");
        b.bullet("/bounty \u2014 view and claim bounties.");
        b.gap();
        b.head("Economy");
        b.bullet("/bank | /coins | /pay <player> <amount>");
        b.bullet("/shop | /market | /auction");
        b.bullet("/credits | /convert | /baltop");
        b.gap();
        b.head("Politics & Justice");
        b.bullet("/gov | /politics | /vote | /impeach");
        b.bullet("/tax status | pay   /treasury   /perk list");
        b.bullet("/court \u2014 the court & domain system.");
        b.gap();
        b.head("Player & Home");
        b.bullet("/sethome | /home | /spawn | /checkpoint | /party");
        b.bullet("/settlement list | here | rank | advance");
        b.gap();
        b.note("Admin/OP commands (summon, grant, role, election, etc.) exist for server staff.");
        return ch("Commands", b);
    }

    // ------------------------------------------------------------------

    /** Turn an internal id like "mob2_archmage_sovereign" into "Archmage Sovereign". */
    private static String pretty(String id) {
        if (id == null || id.isEmpty()) return id;
        String s = id;
        for (String prefix : new String[]{"mob2_", "mob_", "wpn_", "acc_", "arc_", "food_", "v1"}) {
            if (s.startsWith(prefix) && !s.equals("v1")) { s = s.substring(prefix.length()); break; }
        }
        s = s.replace('_', ' ');
        StringBuilder sb = new StringBuilder(s.length());
        boolean cap = true;
        for (char c : s.toCharArray()) {
            if (c == ' ') { cap = true; sb.append(c); }
            else if (cap) { sb.append(Character.toUpperCase(c)); cap = false; }
            else sb.append(c);
        }
        return sb.toString();
    }
}
